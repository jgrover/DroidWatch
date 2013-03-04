package com.droidwatch;

/**
 * DeviceAccountWatcher.java
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

import java.util.HashMap;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

/** This class detects accounts associated with the device. **/
public class DeviceAccountWatcher 
{
	// Initialize constants and variables
	public final static String TAG = "DeviceAccountRetriever";
	
	/**
	 * This method identifies device accounts.
	 * 
	 * @param context	The application context.
	 */
	public static void findAccounts(Context context)
	{
		// Loop through and log available accounts
		HashMap<String,String> accountMap = new HashMap<String,String>();
		Account[] accounts = AccountManager.get(context.getApplicationContext()).getAccounts();
		for (Account account : accounts)
			accountMap.put(account.type, account.name);
		
		for (String key:accountMap.keySet())
		{
			Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
			ContentValues values = new ContentValues();
			values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
			values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Device Account Detected");
			values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, System.currentTimeMillis());
			values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, accountMap.get(key));
			values.put(DroidWatchDatabase.ADDITIONAL_INFO_COLUMN, key);
			context.getContentResolver().insert(eventsUri, values);
		}
	}
}