package com.droidwatch;

/**
 * ContactCheck.java
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
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

/** This class handles the operations involving the DroidWatch contacts database table. **/
public class ContactCheck
{
	// Initialize constants and variables
	public static final String TAG = "ContactsCheck";
	
	/**
	 * This method checks the status of the DroidWatch contacts table.
	 * 
	 * @param context	The application context.
	 */
	public static void checkDroidWatchContacts(Context context)
	{
		// Initialize the DroidWatch contacts content provider URI
		Uri dwURI = DroidWatchProvider.Status.CONTENT_URI;
		
		// Check to see if the database is populated
		String[] proj = new String[]{DroidWatchDatabase.CONTACTS_FILLED_FLAG_COLUMN};
		Cursor cursor = null;
		try
		{
			cursor = context.getContentResolver().query(dwURI, proj, null, null, null);
		}
		catch(Exception e)
		{
			Log.e(TAG, "Unable to query results.db: "+e.getMessage());
			return;
		}
		
		if (cursor == null)
		{
			Log.e(TAG, "Unable to query results.db");
			return;
		}
		
		// Populate database, if necessary
		if (cursor.moveToFirst() && cursor.getCount() > 0)
		{
			int filledFlag = cursor.getInt(cursor.getColumnIndex(DroidWatchDatabase.CONTACTS_FILLED_FLAG_COLUMN));
			if (filledFlag == 0)
			{
				populateContactsTable(context);
				ContentValues values = new ContentValues();
				values.put(DroidWatchDatabase.CONTACTS_FILLED_FLAG_COLUMN, "1");
				context.getContentResolver().update(dwURI, values, null, null);
			}
		}
	}

	/**
	 * This method populates DroidWatch contacts table.
	 * 
	 * @param context	The application context.
	 */
	private static void populateContactsTable(Context context)
	{
		// Query contacts database
		Cursor contactsCursor = null;
		String[] contactsProj = new String[]{Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER};
		try
		{
			contactsCursor = context.getContentResolver().query(Phone.CONTENT_URI, contactsProj, null, null, null);
		}
		catch(Exception e)
		{
			Log.e(TAG, "Unable to query contacts content provider: "+e.getMessage());
		}
		
		// Parse and log query results
		if (contactsCursor == null)
			return;
		
		if (contactsCursor.moveToFirst() && contactsCursor.getCount() > 0)
		{
			long id = -1;
			String name = null;
			String phoneNumber = null;
			long currentTime = System.currentTimeMillis();
			ContentValues values;
			while (!contactsCursor.isAfterLast())
			{
				id = contactsCursor.getLong(contactsCursor.getColumnIndex(Phone._ID));
	            name = contactsCursor.getString(contactsCursor.getColumnIndex(Phone.DISPLAY_NAME)); 
	            phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(Phone.NUMBER));
	            
	            // Insert new contact into contact database
				values = new ContentValues();
				values.put(DroidWatchDatabase.CONTACT_ID_COLUMN, id);
				values.put(DroidWatchDatabase.CONTACT_NAME_COLUMN, name);
				values.put(DroidWatchDatabase.CONTACT_ADDED_COLUMN, currentTime);
				values.put(DroidWatchDatabase.CONTACT_NUMBER_COLUMN, phoneNumber);
        		context.getContentResolver().insert(DroidWatchProvider.Contacts.CONTENT_URI, values);
	            
	            contactsCursor.moveToNext();
			}
		}
	}	

}
