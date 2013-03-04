package com.droidwatch;

/**
 * LogcatWatcher.java
 * @author Justin Grover
 * 
 * NOTICE - This software is intended to serve as prototype material.
 * 
 * Copyright 2013 Justin Grover
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/** This class detects third-party app logs (filtered logcat output). **/
public class LogcatWatcher extends BroadcastReceiver
{
	// Initialize constants and variables
	public static final String TAG = "LogcatWatcher";
	public static long COLLECTION_INTERVAL = 1000 * 60 * 60;
	
	/**
	 * This method sets the collection interval.
	 * 
	 * @param interval	Collection interval.
	 */
	public void setInterval(long logcatInterval)
	{
		COLLECTION_INTERVAL = logcatInterval;
	}
	
	/**
	 * This method handles the broadcasted alarm intent.
	 * 
	 * @param context	The application's context.
	 * @param intent	The broadcasted intent.
	 */
	@Override
    public void onReceive(Context context, Intent intent) 
    {  
		try
		{
			// Initialize variables
			String line;
			Pattern pidPattern = Pattern.compile("[(][ 0-9][ 0-9][ 0-9][ 0-9][0-9][)][:]");
			Pattern datePattern = Pattern.compile("\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
			Matcher pidMatcher;
			Matcher dateMatcher;
			Calendar cal=Calendar.getInstance();
			int year=cal.get(Calendar.YEAR);
			Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
			
			// Get running processes
			ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
			HashMap<Integer,String> processMap = new HashMap<Integer,String>();
			for(RunningAppProcessInfo proc:am.getRunningAppProcesses())
				processMap.put(proc.pid, proc.processName);
			
			// Filter out some of the built-in app activity (noise)
			String filters = " InputDispatcher:S BatteryService:S StatusBarPolicy:S PhoneUtils:S " +
					"NotificationService:S AlarmManagerService:S dalvikvm:S UnlockClock:S ActivityManager:S " +
					"Email:S LogsProvider:S SocialHub:S Zygote:S Database:S ConnectivityService:S WimaxService:S " +
					"MediaUploader:S SecDownloader:S WifiApBroadcastReceiver:S PlayerDriver:S PVPlayer:S WimaxHandler:S " +
					"DMApp:S ServiceManager:S SyncmlBootReceiver:S SAN_SERVICE:S SyncmlService:S SANService:S " +
					"MediaPlayerService:S AlarmManager:S EventLogService:S AudioTrack:S SensorManager:S " +
					"GlassLockScreenMissedEventWidget:S NetworkStateTracker:S WimaxStateTracker:S WimaxMonitor:S " +
					"GTalkService:S lights:S MediaPlayer:S ActivityThread:S pppd:S dhcpcd:S [FT]-Server:S " +
					"Tethering:S TelephonyRegistry:S Smack/Packet:S VLG_EXCEPTION:S System.out:S RingtoneManager:S libnetutils:S " +
					"TextToSpeech:S TextToSpeech.java:S SynthProxy:S ProcessStats:S BrowserSettings:S dalvikvm-heap:S " +
					"AudioService:S PhotoAppWidgetProvider:S Launcher:S Settings:S GlassLockScreenMusicWidget:S " +
					"GlassLockScreenFMRadioWidget:S PowerManagerService:S OrientationDebug:S OmaDrmConfigService:S " +
					"ScreenCaptureAction:S WindowManager:S InputManagerService:S GLThread:S ALSAModule:S InputReader:S " +
					"GlassLockScreen:S KeyguardViewMediator:S AudioPolicyManager:S AudioFlinger:S PackageManager:S DmAppInfo:S " +
					"DebugDb:S PackageInfoItemFactory:S DebugPlacement:S EglHelper:S Main:S KeyCharacterMap:S MediaExtractor:S " +
					"OggExtractor:S OMXCodec:S PhoneInfoReceiver:S PackageInfoHelper:S FeatureCheckerImpl:S IconDebug:S " +
					"DebugFolder:S installd:S *:I";
			
			// Execute logcat
			Process process = Runtime.getRuntime().exec("logcat -v time -d "+filters);
			
			// Parse logcat output line by line
			String found;
			int pid = -1;
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = bufferedReader.readLine()) != null)
			{
				// Get the process information that generated the line
				pidMatcher = pidPattern.matcher(line);
				if (pidMatcher.find())
				{
					found = pidMatcher.group(0);
					found = found.replaceAll("[() :]", "");
					try
					{
						pid = Integer.parseInt(found);
					}
					catch(Exception e)
					{
						continue;
					}
					
					// Get processName running at a given PID
					String appName = processMap.get(pid);
					
					// Skip processes with no name
					if (appName == null)
						continue;
					
					// Skip known built-in processes
					if (appName.equals("system") || appName.equals("com.droidwatch") 
						|| appName.equals("com.android.MtpApplication") || appName.equals("com.sec.android.app.twlauncher")
						|| appName.equals("com.android.packageinstaller") || appName.equals("android.tts")
						|| appName.startsWith("com.android") || appName.startsWith("com.google")
						|| appName.startsWith("com.sec.android"))
						continue;
					
					// Get date from the log event
					dateMatcher = datePattern.matcher(line);
					if (dateMatcher.find())
					{
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
						String logDate = dateMatcher.group(0);
						logDate=year+"-"+logDate;
						Date date = df.parse(logDate);
						
						// Insert third-party app log into DroidWatch
						ContentValues values = new ContentValues();
						values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
						values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Logcat");
						values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, date.getTime());
						values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, appName);
						values.put(DroidWatchDatabase.ADDITIONAL_INFO_COLUMN, line);
						context.getContentResolver().insert(eventsUri, values);
						
						//Log.i(TAG, "App: "+appName+" Log: "+line);
					}
				}
			}
			
			// Clear logcat logs
			bufferedReader.close();
			Runtime.getRuntime().exec("logcat -c");
		}
		catch(Exception e)
		{
			Log.e(TAG, "Unable to run logcat: "+e.getMessage());
			return;
		}
    }

	/**
	 * This method sets a new alarm.
	 * 
	 * @param context
	 * @param am
	 */
	public void setAlarm(Context context, AlarmManager am)
	{
		if (COLLECTION_INTERVAL<=0)
			return;
		
        Intent logcatIntent = new Intent(context, LogcatWatcher.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, logcatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), COLLECTION_INTERVAL, pi);
    }

	/**
	 * This method cancels an existing alarm.
	 * 
	 * @param context
	 * @param am
	 */
    public void cancelAlarm(Context context, AlarmManager am)
    {
    	if (COLLECTION_INTERVAL<=0)
    		return;
    	
    	Intent intent = new Intent(context, LogcatWatcher.class);
    	PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }
}
