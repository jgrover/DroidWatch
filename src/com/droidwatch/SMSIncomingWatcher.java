package com.droidwatch;

/**
 * SMSIncomingWatcher.java
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

import java.util.Date;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/** This class detects incoming SMS messages. **/
public class SMSIncomingWatcher extends BroadcastReceiver
{
	// Initialize constants and variables
	public static final String TAG = "IncomingSMSWatcher";
	
	/**
	 * This method handles the broadcasted intent.
	 * 
	 * @param context	The application context.
	 * @param intent	The broadcasted intent.
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(!(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")))
			return;
		
		// Scrape "extra" information out of the intent
		Bundle extras = intent.getExtras();
		if (extras != null)
		{
			// Reconstruct SMS
    		Object[] pdus = (Object[])extras.get("pdus");
    		SmsMessage sms = SmsMessage.createFromPdu((byte[])pdus[0]);
    		
    		// Extract SMS date, address, and body
    		Date date = new Date(sms.getTimestampMillis());
    		String sender = sms.getDisplayOriginatingAddress();
    		String body = sms.getDisplayMessageBody();
    		
    		// Get contact name
			String displayName = ContactFinder.findContact(context, sender);
			
			// Insert new incoming SMS message into DroidWatch
			Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
			ContentValues values = new ContentValues();
			values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
			values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "SMS Received");
			values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, date.getTime());
			values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, body);
			values.put(DroidWatchDatabase.ADDITIONAL_INFO_COLUMN, "SenderAddress:"+sender+"; SenderContact:"+displayName);
			context.getContentResolver().insert(eventsUri, values);
    		
			//Log.i(TAG, "Address: "+sender +" Contact: "+displayName+" Body: "+body +" Time: "+date.toString());
        }
	}
}