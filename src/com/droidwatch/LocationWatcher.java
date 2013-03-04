package com.droidwatch;

/**
 * LocationWatcher.java
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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;

/** This class detects last known locations. **/
public class LocationWatcher extends BroadcastReceiver
{	
	// Initialize constants and variables
	public static final String TAG = "LocationWatcher";
	public static long COLLECTION_INTERVAL = 1000 * 60 * 60;
	
	/**
	 * This method sets the collection interval.
	 * 
	 * @param interval	Collection interval.
	 */
	public void setInterval(long locationInterval)
	{
		COLLECTION_INTERVAL = locationInterval;		
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
		// Get Location Service
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		// Specify Accuracy & Provider
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String bestProvider = locationManager.getBestProvider(criteria,true);
		
		// Get Last Known Location
		Location lastLocation = null;
		try
		{
			lastLocation = locationManager.getLastKnownLocation(bestProvider);
		}
		catch(Exception e)
		{
			Log.e(TAG, "Error: Last location not found");
			return;
		}
		
		// Log location
		if (lastLocation != null)
		{
			// Initialize the DroidWatch events content provider URI
			Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
			
			// Make sure the location is not already listed
			String[] projection = new String[]{DroidWatchDatabase.DETECTOR_COLUMN, DroidWatchDatabase.EVENT_DATE_COLUMN};
			String selection = DroidWatchDatabase.DETECTOR_COLUMN+" = ? AND "+DroidWatchDatabase.EVENT_DATE_COLUMN +" = ?";
			String[] selectionArgs = new String[]{TAG, String.valueOf(lastLocation.getTime())};
			Cursor cursor = null;
			try
			{
				cursor = context.getContentResolver().query(eventsUri, projection, selection, selectionArgs, null);
			}
			catch(Exception e)
			{
				Log.e(TAG, "Unable to query events table");
				return;
			}
			
			// Log search event
			if (cursor == null)
			{
				Log.e(TAG, "Unable to query events table");
				return;
			}
			
			if (cursor.getCount() == 0)
			{	
				// Insert the new location into DroidWatch
				ContentValues values = new ContentValues();
				values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
				values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "LastKnownLocation Received");
				values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, lastLocation.getTime());
				values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, "Lat:"+lastLocation.getLatitude()+"; Lng:"+lastLocation.getLongitude());
				context.getContentResolver().insert(eventsUri, values);
	
				//Log.i(TAG, "Time: "+lastLocation.getTime()+" Lat: " + lastLocation.getLatitude() +" Lng: " + lastLocation.getLongitude());
			}
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
		
        Intent locationIntent = new Intent(context, LocationWatcher.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
    	
    	Intent intent = new Intent(context, LocationWatcher.class);
    	PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }
}
