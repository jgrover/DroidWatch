package com.droidwatch;

/**
 * MMSIncomingWatcher.java
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

import java.util.ArrayList;
import java.util.Date;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
//import android.util.Log;
import android.util.Log;

/** This class detects incoming MMS messages. **/
public class MMSIncomingWatcher extends BroadcastReceiver
{
	public static final String TAG = "IncomingMMSWatcher";
	
	/**
	 * This method handles a broadcasted intent.
	 * 
	 * @param context	The application context.
	 * @param intent	The broadcasted intent.
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(!(intent.getAction().equals("android.provider.Telephony.MMS_RECEIVED") || intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED")))
			return;
		
		// Retrieve the most recent MMS message
		ArrayList<MMS> mmsArray = MMS.getMostRecentIncomingMMS(context, TAG);
		mmsArray = MMS.addMMSPictureNames(context, mmsArray, TAG);
		mmsArray = MMS.addAddresses(context, mmsArray, TAG);
		mmsArray = MMS.addMMSText(context, mmsArray, TAG);
		
		for (MMS mms:mmsArray)
		{
			Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
			int messageID = mms.getId();
			String address = mms.getAddress();
			String displayName = ContactFinder.findContact(context, address);
			Date date = mms.getDate();
			String subject = mms.getSubject();
			String picName = mms.getPicName();
			String text = mms.getText();
			
			// Substitute text indication place-holders (could not retrieve actual text)
			if (text == null)
				text = "<No Text Included>";
			
			if (text.equals(""))
				text = "<Text Detected>";
			
			// Insert new MMS message into DroidWatch
			ContentValues values = new ContentValues();
			values.put(DroidWatchDatabase.DETECTOR_COLUMN, TAG);
			values.put(DroidWatchDatabase.EVENT_ACTION_COLUMN, "MMS Received");
			values.put(DroidWatchDatabase.EVENT_DATE_COLUMN, date.getTime());
			values.put(DroidWatchDatabase.EVENT_DESCRIPTION_COLUMN, picName);
			values.put(DroidWatchDatabase.ADDITIONAL_INFO_COLUMN, "MSG_ID:"+messageID+"; ReceiverAddress:"+address+"; ReceiverContact:"+displayName+"; Subject:"+subject+"; Text:"+text+";");
			context.getContentResolver().insert(eventsUri, values);
			
			//Log.i(TAG, "Dir: "+mms.getDirection()+" Address: "+address+" Name: "+displayName+" Sub: "+subject+" Text: "+text+" Pic: "+picName+ " Date: "+mms.getDate().toString()+" MessageID: "+messageID+" ThreadID: "+mms.getThreadId());
		}
	}
}
