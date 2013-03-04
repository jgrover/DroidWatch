package com.droidwatch;

/**
 * Transfer.java
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
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/** This class performs scheduled data transfers. **/
public class Transfer extends BroadcastReceiver
{
	// Initialize constants and variables
	public static final String TAG = "Transfer";
	public static long TRANSFER_INTERVAL = 1000 * 60 * 60 * 2;
	public static String SSL_CERT_NAME = "ubuntu.crt";
	public static String SERVER_URL = "https://192.168.1.7/upload.php";
	
	/**
	 * This method sets the transfer interval.
	 * 
	 * @param interval	Collection interval.
	 */
	public void setInterval(long transferInterval)
	{
		TRANSFER_INTERVAL = transferInterval;
	}
	
	/**
	 * This method sets the server connection properties.
	 * 
	 * @param sslCertName	The filename of the SSL certificate included in the assets.
	 * @param serverURL		The URL of the central server.
	 */
	public void setConnectionProperties(String sslCertName, String serverURL)
	{
		SSL_CERT_NAME = sslCertName;
		SERVER_URL = serverURL;
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
		TransferManager transferManager = new TransferManager(context.getApplicationContext());
		
		// Get connection to central server
		boolean connected = transferManager.getConnection(SSL_CERT_NAME, SERVER_URL);
		if (!connected)
			return;
		
		// Insert new entry in the DroidWatch transfer table
		boolean started = transferManager.startTransfer();
		if (!started)
			return;
		
		// Transfer logged DroidWatch events to central server
		boolean transferred = transferManager.pushToServer();
		if (!transferred)
			return;

		// Wipe transferred data from the local phone database
		boolean deleted = transferManager.wipeDatabase();
		if (!deleted)
			return;
	}
	
	/**
	 * This method sets a new alarm.
	 * 
	 * @param context
	 * @param am
	 */
	public void setAlarm(Context context, AlarmManager am)
	{
		if (TRANSFER_INTERVAL<=0)
			return;
		
        Intent intent = new Intent(context, Transfer.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), TRANSFER_INTERVAL, pi);
    }

	/**
	 * This method cancels an existing alarm.
	 * 
	 * @param context
	 * @param am
	 */
    public void cancelAlarm(Context context, AlarmManager am)
    {
    	if (TRANSFER_INTERVAL<=0)
    		return;
    	
    	Intent intent = new Intent(context, Transfer.class);
    	PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }
}