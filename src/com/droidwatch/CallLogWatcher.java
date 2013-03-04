package com.droidwatch;

/**
 * CallLogWatcher.java
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
import android.provider.CallLog.Calls;

/** This class detects newly logged phone calls. **/
public class CallLogWatcher extends ContentObserver
{
	// Initialize constants and variables
	private static final String TAG = "CallWatcher";
	private Context context;
	
	/**
	 * Constructor needed for content observers.
	 * 
	 * @param handler
	 * @param context
	 */
    public CallLogWatcher(Handler handler, Context context) 
    {
        super( handler );
        this.context = context;
    }

    /**
     * This method is called upon content observer change notifications.
     * 
     * @param selfChange
     */
    @Override
    public void onChange(boolean selfChange) 
    {
        super.onChange(selfChange);
        queryCallLog();
    }
    
    /**
     * This method queries the call log database for new calls.
     */
    private void queryCallLog()
    {
    	// Query the the call database
    	Cursor callCursor = null;
    	String[] callProj = new String[]{Calls._ID, Calls.NUMBER, Calls.CACHED_NAME, Calls.DURATION, Calls.DATE, Calls.CACHED_NUMBER_TYPE, Calls.TYPE };
    	try
    	{
    		callCursor = context.getContentResolver().query(Calls.CONTENT_URI, callProj, null, null, Calls.DATE + " DESC");
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG, "Unable to query CallLog content provider");
    		return;
    	}
    	
    	// Parse query results
    	if (callCursor == null)
    	{
    		Log.e(TAG, "Unable to query CallLog content provider");
    		return;
    	}
    	
		if (callCursor.moveToFirst() && callCursor.getCount() > 0)
		{
			// Initialize DroidWatch events content provider URI
			Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
			
			// Get call event information
			int id					= callCursor.getInt(callCursor.getColumnIndex(Calls._ID));
			String contactNumber 	= callCursor.getString(callCursor.getColumnIndex(Calls.NUMBER)); 
	        String contactName 		= callCursor.getString(callCursor.getColumnIndex(Calls.CACHED_NAME)); 
	        long duration 			= callCursor.getLong(callCursor.getColumnIndex(Calls.DURATION));
	        Date callDate 			= new Date(callCursor.getLong(callCursor.getColumnIndex(Calls.DATE)));
	        String numType 			= callCursor.getString(callCursor.getColumnIndex(Calls.CACHED_NUMBER_TYPE));
	        int callType 			= callCursor.getInt(callCursor.getColumnIndex(Calls.TYPE));
	        
	        // Determine call direction
	        String direction = "";
	        if (callType==Calls.INCOMING_TYPE)
	        	direction = "Incoming";
	        else if (callType==Calls.MISSED_TYPE)
	        	direction = "Incoming - Missed";
	        else
	        	direction = "Outgoing";
	        
	        // Check to make sure this is not an old call
	        Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -5);
			long fiveMinutesAgo = cal.getTimeInMillis();
			long endCallDate = callDate.getTime() + (duration*1000L);
			if(endCallDate < fiveMinutesAgo)
				return;
	     
	        // Check to see if this search is already in the database
    		String[] projection = new String[]{DroidWatchDatabase.DETECTOR_COLUMN, DroidWatchDatabase.ADDITIONAL_INFO_COLUMN};
			String selection = DroidWatchDatabase.DETECTOR_COLUMN+" = ? AND "+DroidWatchDatabase.ADDITIONAL_INFO_COLUMN +" LIKE ?";
			String[] selectionArgs = new String[]{TAG, "%ID:"+id+";%"};
			Cursor cursor = null;
			try
			{
				cursor = context.getContentResolver().query(eventsUri, projection, selection, selectionArgs, null);
			}
			catch(Exception e)
			{
				Log.e(TAG, "Unable to query events table: "+e.getMessage());
				return;
			}
			
			// Log new calls made & received
			if (cursor == null)
			{
				Log.e(TAG, "Unable to query events table");
				return;
			}
			
			if (cursor.getCount() == 0)
			{
				// Insert phone call event into DroidWatch
				ContentValues values = new ContentValues();
				values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
				values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Phone Call");
				values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, callDate.getTime());
				values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, direction);
				values.put(DroidWatchDatabase.ADDITIONAL_INFO_COLUMN, "ID:"+id+"; Number:"+contactNumber+"; Name:"+contactName+"; Duration:"+duration+" NumType:"+numType+";");
				context.getContentResolver().insert(eventsUri, values);
				//Log.i(TAG, "Direction: "+direction+" Number: "+contactNumber+ " Name: "+contactName+" Duration: "+duration+" Date: "+callDate.toString()+" NumType: "+numType+" ID: "+id);
			}
		}
    }
}
