package com.droidwatch;

/**
 * AppWatcher.java
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

/** This class detects app installs and removals. **/ 
public class AppWatcher extends BroadcastReceiver
{
	// Initialize variables and constants
	public static final String TAG = "AppWatcher";
	
	/**
	 * This method handles the broadcasted intent.
	 * 
	 * @param context	The application context.
	 * @param intent	The broadcasted intent.
	 */
	@Override
	public void onReceive(final Context context, Intent intent)
	{
		// Initialize Events Log URI
		Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
		
		// Get data from the received intent
		Uri data = intent.getData();
        String pkgName = data.getEncodedSchemeSpecificPart();
        String action = intent.getAction();
        
        // Log application installs & removals
		ContentValues values; 
		if (action.equals(Intent.ACTION_PACKAGE_ADDED))
		{
			values = new ContentValues();
			values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
			values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Package Installed");
			values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
			values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, pkgName);	
			context.getContentResolver().insert(eventsUri, values);
			//Log.i(TAG, "Package Installed: "+pkgName);
		}
		
		else if (action.equals(Intent.ACTION_PACKAGE_REMOVED))
		{
			values = new ContentValues();
			values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
			values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Package Removed");
			values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
			values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, pkgName);	
			context.getContentResolver().insert(eventsUri, values);
			//Log.i(TAG, "Package Removed: "+pkgName);
		}
	}
}