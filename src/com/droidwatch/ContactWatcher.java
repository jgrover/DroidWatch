package com.droidwatch;

/**
 * ContactWatcher.java
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

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;
import android.provider.ContactsContract.CommonDataKinds.Phone;

/** This class detects newly added contacts. **/
public class ContactWatcher extends ContentObserver
{
	// Initialize constants and variables
	private static final String TAG = "ContactWatcher";
	private Context context;
	
	/**
	 * Constructor required for content observers.
	 * 
	 * @param handler
	 * @param context
	 */
	public ContactWatcher(Handler handler, Context context) 
    {
        super( handler );
        this.context = context;
    }
	
	/** 
	 * This method handles the content observer change notifications.
	 */
	@Override
	public void onChange(boolean selfChange) 
	{
		super.onChange(selfChange);
		queryContacts();
	}

	/**
	 * This method looks for new contacts in the contacts content provider and compares
	 * them to what exists already in the DroidWatch contacts table.
	 */
	private void queryContacts()
	{
		// Look for new contacts
		Cursor contactsCursor = null;
		String[] contactsProj = new String[]{Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER};
        String sortOrder = "_id DESC";
		try
		{
			contactsCursor = context.getContentResolver().query(Phone.CONTENT_URI, contactsProj, null, null, sortOrder);
		}
		catch(Exception e)
		{
			Log.e(TAG, "Unable to query contacts content provider");
			return;
		}
		
		// Parse the query results
		if (contactsCursor == null)
		{
			Log.e(TAG, "Unable to query contacts content provider");
			return;
		}
		
		if (contactsCursor.moveToFirst() && contactsCursor.getCount() > 0)
		{
			long id = contactsCursor.getLong(contactsCursor.getColumnIndex(Phone._ID));
            String name = contactsCursor.getString(contactsCursor.getColumnIndex(Phone.DISPLAY_NAME)); 
            String phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(Phone.NUMBER));
            
            // Check to see if this contact already exists in the DroidWatch contacts table
            String[] projection = new String[]{DroidWatchProvider.Contacts._ID,};
            String selection = "contact_id = "+String.valueOf(id);
            Cursor cursor = null;
            try
            {
            	cursor = context.getContentResolver().query(DroidWatchProvider.Contacts.CONTENT_URI, projection, selection, null, null);
            }
            catch(Exception e)
            {
            	Log.e(TAG, "Error querying contacts table in results.db: "+e.getMessage());
            	return;
            }
            
            // Log newly added contact
            if (cursor == null)
            {
            	Log.e(TAG, "Error querying contacts table in results.db");
            	return;
            }
            
        	if (cursor.getCount() == 0)
        	{
        		// Get current time
        		long currentTime = System.currentTimeMillis();
				
        		// Insert new contact into the DroidWatch contacts table
				ContentValues contactValues = new ContentValues();
				contactValues.put(DroidWatchDatabase.CONTACT_ID_COLUMN, id);
				contactValues.put(DroidWatchDatabase.CONTACT_NAME_COLUMN, name);
				contactValues.put(DroidWatchDatabase.CONTACT_ADDED_COLUMN, currentTime);
				contactValues.put(DroidWatchDatabase.CONTACT_NUMBER_COLUMN, phoneNumber);
        		context.getContentResolver().insert(DroidWatchProvider.Contacts.CONTENT_URI, contactValues);
        		
        		// Insert new contact into the events table
        		ContentValues eventValues = new ContentValues();
				eventValues.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
				eventValues.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Contact Added");
				eventValues.put(DroidWatchDatabase.EVENT_DATE_COLUMN, currentTime);
				eventValues.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, name);
				eventValues.put(DroidWatchDatabase.ADDITIONAL_INFO_COLUMN, "ID:"+id+"; Number:"+phoneNumber+";");
				context.getContentResolver().insert(DroidWatchProvider.Events.CONTENT_URI, eventValues);
				//Log.i(TAG, "Contact Name: "+name+" Phone: "+phoneNumber+" ID: "+id);
        	}
		}
	}
}