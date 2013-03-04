package com.droidwatch;

/**
 * LocationProviderWatcher.java
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

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;

/** This class detects changes to the location provider settings. **/
public class LocationProviderWatcher extends BroadcastReceiver
{
	// Initialize constants and variables
	public static final String TAG = "ProviderChangeWatcher";

	/**
	 * This method handles a broadcasted intent.
	 * 
	 * @param context	The application context.
	 * @param intent	The broadcasted intent.
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(!(intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)))
			return;
		
		// Check status of each provider
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		// Store status of each provider
		String networkStatus = "";
		if(networkEnabled)
			networkStatus = "on";
		else
			networkStatus = "off";
		
		String gpsStatus = "";
		if(gpsEnabled)
			gpsStatus = "on";
		else
			gpsStatus = "off";
		
		// Log the event in DroidWatch
		Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
		ContentValues values; 
		values = new ContentValues();
		values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
		values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Provider Status Changed");
		values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
		values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, "Network:"+networkStatus+"; GPS:"+gpsStatus+";");	
		context.getContentResolver().insert(eventsUri, values);
		
		//Log.i(TAG, "change in provider detected.  Network: "+networkStatus+" GPS: "+gpsStatus);
	}
}
