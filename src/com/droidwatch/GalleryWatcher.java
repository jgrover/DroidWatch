package com.droidwatch;

/**
 * GalleryWatcher.java
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
import android.provider.MediaStore.Images.Media;
import android.util.Log;

/** This class detects newly added photos to the Android Gallery. **/
public class GalleryWatcher extends ContentObserver
{
	// Initialize constants and variables
	private static final String TAG = "PhotoWatcher";
	private Context context;
	
	/**
	 * Constructor required for content observers.
	 * 
	 * @param handler
	 * @param context
	 */
	public GalleryWatcher(Handler handler, Context context) 
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
		 queryPhotos();
	 }
	 
	 /**
	  * This method finds and identifies newly added pictures.
	  */
	 private void queryPhotos()
	 {
		 // Query photo database
		 Cursor photoCursor = null;
		 String[] photoProj = new String[]{ Media._ID, Media.DISPLAY_NAME, Media.DATE_TAKEN };
		 try
		 {
			 photoCursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, photoProj, null, null, "date_added DESC");
		 }
		 catch(Exception e)
		 {
			 Log.e(TAG, "Unable to query content provider");
			 return;
		 }
		 
		 // Parse query results
		 if (photoCursor == null)
		 {
			 Log.e(TAG, "Unable to query content provider");
			 return;
		 }
		 
		 // Get most recent photo
		 long id = -1;
		 Date date = null;
		 String fileName = null;
		 if (photoCursor.moveToFirst() && photoCursor.getCount() > 0) 
		 {
			 id = photoCursor.getLong(photoCursor.getColumnIndex(Media._ID));
			 fileName = photoCursor.getString(photoCursor.getColumnIndex(Media.DISPLAY_NAME));
			 date = new Date(photoCursor.getLong(photoCursor.getColumnIndex(Media.DATE_TAKEN)));
		 }
		 
		 if (id == -1 || fileName == null || date == null)
		 {
			 Log.e(TAG, "Unable to query photo database");
			 return;
		 }
		 
		 // Check the photo's date_added time to see if it's new
		 Calendar cal = Calendar.getInstance();
		 cal.add(Calendar.SECOND, -5);
		 long fiveSecondsAgo = cal.getTimeInMillis();
		 if (date.getTime() > fiveSecondsAgo)
		 {
			 // Initialize DroidWatch events table URI
			 Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
			 
			 // Check to see if the photo is already in the local database
			 String[] projection = new String[]{DroidWatchDatabase.DETECTOR_COLUMN, DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN};
			 String selection = DroidWatchDatabase.DETECTOR_COLUMN+" = ? AND "+DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN+" = ?";
			 String[] selectionArgs = new String[]{TAG, fileName};
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
				 // Insert newly added picture to DroidWatch
				 ContentValues values = new ContentValues();
				 values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
				 values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "Photo Added");
				 values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, date.getTime());
				 values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, fileName);
				 values.put(DroidWatchDatabase.ADDITIONAL_INFO_COLUMN, "PhotoID:"+id);
				 context.getContentResolver().insert(eventsUri, values);
				
				 //Log.i(TAG, "ID: "+id+" FileName: "+fileName+" Date: "+date.toString());
			 }
		}
	}
}