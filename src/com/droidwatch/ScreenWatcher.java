package com.droidwatch;

/**
 * ScreenWatcher.java
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
import android.net.Uri;
import android.util.Log;

/** This class detects changes to the screen's lock status (e.g., locked, unlocked, off). **/
public class ScreenWatcher extends BroadcastReceiver
{
	// Initialize constants and variables
	public static final String TAG = "ScreenWatcher";
	
	/**
	 * This method handles a broadcasted intent.
	 * 
	 * @param context	The application context.
	 * @param intent	The broadcasted intent.
	 */
	@Override
	public void onReceive(final Context context, Intent intent) 
	{
		// Get screen status
		String action = intent.getAction();
		
		// Log event
		Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
		ContentValues values; 
		if (action.equals(Intent.ACTION_SCREEN_OFF))
		{
			values = new ContentValues();
			values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
			values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Screen Off");
			values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
			context.getContentResolver().insert(eventsUri, values);
			
			//Log.i(TAG, "Screen Off");
		}
		
		else if (action.equals(Intent.ACTION_SCREEN_ON))
		{
			values = new ContentValues();
			values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
			values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Screen Locked");
			values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
			context.getContentResolver().insert(eventsUri, values);
		
			//Log.i(TAG, "Screen Locked");
		}
		
		else if(action.equals(Intent.ACTION_USER_PRESENT))
		{
			values = new ContentValues();
			values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
			values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Screen Unlocked");
			values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
			context.getContentResolver().insert(eventsUri, values);
			
			//Log.i(TAG, "Screen Unlocked");
		}
	}
}