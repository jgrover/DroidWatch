package com.droidwatch;

/**
 * ContactFinder.java
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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

/** This class finds the names associated with contact addresses. **/
public class ContactFinder
{
	// Initialize constants and variables
	public static final String TAG = "ContactFinder";
	
	/**
	 * This method returns the contact name for a given address.
	 * 
	 * @param context	The application context.
	 * @param address	The provided address (phone number).
	 * @return			The associated contact name.
	 */
	public static String findContact(Context context, String address)
	{
		// Initialize the contacts content provider URI
		Uri contactURI = Phone.CONTENT_URI;
		
		// Query contacts data
		String[] projection = new String[] { Phone.DISPLAY_NAME, };
		String selectionClause = Phone.NUMBER+" = ?";
		String displayName = null;
		Cursor contactsCursor = null;
		try
		{
			contactsCursor = context.getContentResolver().query(contactURI, projection, selectionClause, new String[]{address}, null);
		}
		catch(Exception e)
		{
			Log.e(TAG, "Unable to query contacts content provider");
			return null;
		}
		
		// Parse query results
		if( contactsCursor == null)
		{
			Log.e(TAG, "Unable to query contacts content provider");
			return null;
		}
		
		if (contactsCursor.moveToFirst() && contactsCursor.getCount() > 0) 
		{ 
            try
            {
                displayName = contactsCursor.getString(contactsCursor.getColumnIndex(Phone.DISPLAY_NAME));
            }
            catch(Exception e)
            {
            	Log.e(TAG, "Unable to find the contact name");
            	return null;
            }
		}
		return displayName;
	}
}