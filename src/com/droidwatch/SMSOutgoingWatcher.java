package com.droidwatch;

/**
 * SMSOutgoingWatcher.java
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

import java.util.Calendar;
import java.util.Date;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/** This class detects outgoing SMS messages. **/
public class SMSOutgoingWatcher extends ContentObserver
{
	// Initialize constants and variables
	public static final String TAG = "OutgoingSMSWatcher";
	private Context context;
	
	/**
	 * Constructor needed for content observers.
	 * 
	 * @param handler
	 * @param context
	 */
    public SMSOutgoingWatcher(Handler handler, Context context) 
    {
        super( handler );
        this.context = context;
    }

    /**
     * This method handles the content observer change notifications.
     * 
     * @param selfChange
     */
    @Override
    public void onChange(boolean selfChange) 
    {
        super.onChange(selfChange);
        querySMSLog();
    }
    
    /**
     * This method attempts to find newly logged outgoing SMS messages.
     */
    private void querySMSLog()
    {	
    	// Initialize SMS content provider URI (undocumented)
    	Uri uriSMS = Uri.parse("content://sms");
    	
    	// Query for SMS id, body, address, date, and type
    	String[] smsProj = new String[]{"_id","body","address","date","type"};
    	Cursor smsCursor = null;
    	try
    	{
    		smsCursor = context.getContentResolver().query(uriSMS, smsProj, null, null, null);
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG, "Unable to query URI: "+uriSMS.toString());
    		return;
    	}
    	
    	// Parse query results
    	if (smsCursor == null)
    	{
    		Log.e(TAG, "Unable to query SMS content provider");
    		return;
    	}
    	
    	if (smsCursor.moveToFirst() && smsCursor.getCount() > 0)
		{	
    		int messageID 		= -1;
    		String body 		= null;
    		String address 		= null;
    		Date date 			= null;
    		int smsType 		= -1;
    		
			// Get SMS message
			try
			{
				messageID		= smsCursor.getInt(smsCursor.getColumnIndex("_id"));
				body 			= smsCursor.getString(smsCursor.getColumnIndex("body"));
				address 		= smsCursor.getString(smsCursor.getColumnIndex("address"));
				date 			= new Date(smsCursor.getLong(smsCursor.getColumnIndex("date")));
				smsType 		= Integer.valueOf(smsCursor.getString(smsCursor.getColumnIndex("type")));
			} 
			catch (Exception e) 
			{
				Log.e(TAG, "Unable to log SMS");
				return;
			}
			
			if (messageID == -1 || body == null || address == null || date == null || smsType == -1)
			{
				Log.e(TAG, "Unable to log SMS");
				return;
			}
			
			// Get contact name
			String displayName = ContactFinder.findContact(context, address);
			
			// Log any newly logged outgoing SMS messages
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, -5);
			long fiveSecondsAgo = cal.getTimeInMillis();
			if (date.getTime() > fiveSecondsAgo && smsType == 4)
			{
				// Check the DroidWatch content provider to make sure event is not a duplicate
				Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
				String[] projection = new String[]{DroidWatchDatabase.DETECTOR_COLUMN, DroidWatchDatabase.ADDITIONAL_INFO_COLUMN};
				String selection = DroidWatchDatabase.DETECTOR_COLUMN+" = ? AND "+DroidWatchDatabase.ADDITIONAL_INFO_COLUMN+" LIKE ?";
				String[] selectionArgs = new String[]{TAG, "%MSG_ID:"+messageID+";%"};
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
				
				if (cursor == null)
				{
					Log.e(TAG, "Unable to query events table");
					return;
				}
				
				if (cursor.getCount() == 0)
				{
					// Insert new event into DroidWatch
					ContentValues values = new ContentValues();
					values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
					values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "SMS Sent");
					values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, date.getTime());
					values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, body);
					values.put(DroidWatchDatabase.ADDITIONAL_INFO_COLUMN, "MSG_ID:"+messageID+"; ReceiverAddress:"+address+"; ReceiverContact:"+displayName+";");
					context.getContentResolver().insert(eventsUri, values);
					
					//Log.i(TAG, "SMS Type: "+smsType+" DisplayName: "+displayName+" Number: "+address+ " Date: "+date.toString()+" Body: "+body+" ID: "+messageID);
				}	
			}
    	}       
    }
}
