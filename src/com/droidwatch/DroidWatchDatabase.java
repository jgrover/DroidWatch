package com.droidwatch;

/**
 * DroidWatchDatabase.java
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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/** This class provides the schemas for the DroidWatch database tables. **/
public class DroidWatchDatabase extends SQLiteOpenHelper
{
	public static final String TAG = "DroidWatchDatabase";
	
	public static final String DATABASE_NAME = "results.db";
	public static final int DATABASE_VERSION = 17;
	
    public static final String TRANSFERS_TABLE = "transfers";
    public static final String TRANSFER_ID_COLUMN = "_id";
    public static final String TRANSFERS_COMPLETED_COLUMN = "transfer_complete";
    public static final String TRANSFERS_STARTDATE_COLUMN = "transfer_start_time";
    public static final String TRANSFERS_DEVICE_ID_COLUMN = "device_id";
    private static final String TRANSFERS_TABLE_CREATE =
            "CREATE TABLE " + TRANSFERS_TABLE + " (" +
            TRANSFER_ID_COLUMN + " INTEGER primary key, " +
            TRANSFERS_COMPLETED_COLUMN + " BOOLEAN default 0, " +
            TRANSFERS_STARTDATE_COLUMN + " DATETIME default (strftime('%s', 'now')), " +
            TRANSFERS_DEVICE_ID_COLUMN + " TEXT);";
    
    public static final String EVENTS_TABLE = "events";
    public static final String EVENT_ID_COLUMN = "_id";
    public static final String DETECTOR_COLUMN = "detector";
    public static final String DETECT_DATE_COLUMN = "detected";
    public static final String EVENT_ACTION_COLUMN = "action";
    public static final String EVENT_DATE_COLUMN= "event_occurred";
    public static final String EVENT_DESCRIPTION_COLUMN = "description";
    public static final String ADDITIONAL_INFO_COLUMN = "additional_info";
    private static final String EVENTS_TABLE_CREATE = 
    		"CREATE TABLE  " + EVENTS_TABLE + " (" +
    		EVENT_ID_COLUMN + " INTEGER primary key, " +
    		DETECTOR_COLUMN + " TEXT, " +
    		DETECT_DATE_COLUMN + " DATETIME default (strftime('%s', 'now')), " +
    		EVENT_ACTION_COLUMN + " TEXT, " + 
    		EVENT_DATE_COLUMN + " DATETIME, " + 
    		EVENT_DESCRIPTION_COLUMN + " TEXT," +
    		ADDITIONAL_INFO_COLUMN + " TEXT);";
    
    public static final String CALENDAR_TABLE = "calendar";
    public static final String CALENDAR_ID_PKEY_COLUMN = "_id";
    public static final String CALENDAR_EVENT_ID_COLUMN = "event_id";
    public static final String CALENDAR_EVENT_NAME_COLUMN = "name";
    public static final String CALENDAR_EVENT_DATE_COLUMN = "date";
    public static final String CALENDAR_EVENT_ADDED_COLUMN = "added";
    public static final String CALENDAR_TABLE_CREATE = 
    		"CREATE TABLE "+ CALENDAR_TABLE + " (" +
    		CALENDAR_ID_PKEY_COLUMN +" INTEGER primary key, " +
    		CALENDAR_EVENT_ID_COLUMN + " INTEGER, " +
    		CALENDAR_EVENT_NAME_COLUMN + " TEXT, " +
    		CALENDAR_EVENT_DATE_COLUMN + " DATETIME, " +
    		CALENDAR_EVENT_ADDED_COLUMN + " DATETIME);";
    				
    
    public static final String CONTACTS_TABLE = "contacts";
    public static final String CONTACT_ID_PKEY_COLUMN = "_id";
    public static final String CONTACT_ID_COLUMN = "contact_id";
    public static final String CONTACT_NUMBER_COLUMN = "number";
    public static final String CONTACT_NAME_COLUMN = "name";
    public static final String CONTACT_ADDED_COLUMN = "added";
    private static final String CONTACTS_TABLE_CREATE = 
    		"CREATE TABLE "+ CONTACTS_TABLE +" ("+
    		CONTACT_ID_PKEY_COLUMN + " INTEGER primary key, " +
    		CONTACT_ID_COLUMN + " INTEGER, " +
    		CONTACT_NUMBER_COLUMN + " TEXT, " + 
    		CONTACT_NAME_COLUMN + " TEXT, " +
    		CONTACT_ADDED_COLUMN + " DATETIME);";
    
    public static final String STATUS_TABLE = "status";
    public static final String CONTACTS_FILLED_FLAG_COLUMN = "contacts_is_filled";
    public static final String CALENDAR_FILLED_FLAG_COLUMN = "calendar_is_filled";
    private static final String STATUS_TABLE_CREATE =
    		"CREATE TABLE " + STATUS_TABLE +" ("+
    		CONTACTS_FILLED_FLAG_COLUMN + " BOOLEAN NOT NULL, " +
    		CALENDAR_FILLED_FLAG_COLUMN + " BOOLEAN NOT NULL);";
    private static final String STATUS_TABLE_INSERT = 
    		"INSERT INTO " + STATUS_TABLE + " ("+
    		CONTACTS_FILLED_FLAG_COLUMN+","+CALENDAR_FILLED_FLAG_COLUMN+") VALUES (0,0);";

    /**
     * Constructor required for SQLiteOpenHelper
     * 
     * @param c		The application context.
     */
    public DroidWatchDatabase(Context c)
    {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method creates the DroidWatch SQLite database.
     * 
     * @param db	The database to create.
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
    	Log.w(TAG, "Upgrading database");
        db.execSQL(TRANSFERS_TABLE_CREATE);
        db.execSQL(EVENTS_TABLE_CREATE);
        db.execSQL(CONTACTS_TABLE_CREATE);
        db.execSQL(CALENDAR_TABLE_CREATE);
        db.execSQL(STATUS_TABLE_CREATE);
        db.execSQL(STATUS_TABLE_INSERT);
    }

    /**
     * This method upgrades an existing DroidWatch SQLite database.
     * 
     * @param db	The database to upgrade.
     * @param oldVersion
     * @param newVersion
     */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TRANSFERS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + CALENDAR_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + STATUS_TABLE);
		onCreate(db);
	}
}