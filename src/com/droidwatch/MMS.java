package com.droidwatch;

/**
 * MMS.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/** This class handles operations pertaining to the collection of MMS messages. **/
public class MMS
{
	
	// Initialize Variables
	private int id;
	private int threadId;
	private String direction;
	private String subject;
	private Date date;
	private String address;
	private String picName;
	private String text;
	
	/**
	 * This method gets the text of an MMS message object. Note: the actual text of an
	 * MMS message could not be obtained on the test device.
	 * 
	 * @return	An indication of whether text was included in an MMS message.
	 */
	public String getText()
	{
		return text;
	}
	
	/**
	 * This method sets the text of an MMS message object. Note: the actual text of an
	 * MMS message could not be obtained on the test device.
	 * 
	 * @param text	An indication of whether text was included in an MMS message.
	 */
	public void setText(String text)
	{
		this.text = text;
	}
	
	/**
	 * This method gets the address of an MMS message object.
	 * 
	 * @return	The address (phone number or email address).
	 */
	public String getAddress()
	{
		return address;
	}
	
	/**
	 * This method sets the address of an MMS message object.
	 * 
	 * @param address	The phone number or email address.
	 */
	public void setAddress(String address)
	{
		this.address = address;
	}
	
	/**
	 * This method gets the date of an MMS message object.
	 * 
	 * @return	Date of the message.
	 */
	public Date getDate()
	{
		return date;
	}
	
	/**
	 * This method sets the date of an MMS message object.
	 * 
	 * @param date	Date of the message.
	 */
	public void setDate(Date date)
	{
		this.date = date;
	}
	
	/**
	 * This method gets the conversation thread id of an MMS message object.
	 * 
	 * @return	The thread ID.
	 */
	public int getThreadId()
	{
		return threadId;
	}
	
	/**
	 * This method sets the conversation thread id of an MMS message object.
	 * 
	 * @param threadId	The thread ID.
	 */
	public void setThreadId(int threadId)
	{
		this.threadId = threadId;
	}
	
	/**
	 * This method gets the messageID of an MMS message object.
	 * 
	 * @return	The message ID.
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * This method sets the messageID of an MMS message object.
	 * 
	 * @param id	The message ID.
	 */
	public void setId(int id)
	{
		this.id = id;
	}
	
	/**
	 * This method gets the message direction of an MMS message object.
	 * 
	 * @return	The message direction.
	 */
	public String getDirection()
	{
		return direction;
	}
	
	/**
	 * This method sets the message direction of an MMS message object.
	 * 
	 * @param direction	 The message direction.
	 */
	public void setDirection(String direction)
	{
		this.direction = direction;
	}
	
	/**
	 * This method gets the subject of an MMS message object.
	 * 
	 * @return	The message subject.
	 */
	public String getSubject()
	{
		return subject;
	}
	
	/**
	 * This method sets the subject of an MMS message object.
	 * 
	 * @param subject	The message subject.
	 */
	public void setSubject(String subject)
	{
		this.subject = subject;
	}
	
	/**
	 * This method gets the picture filename of an MMS message object.
	 * 
	 * @return	The filename of the picture.
	 */
	public String getPicName()
	{
		return picName;
	}
	
	/**
	 * This method sets the picture filename of an MMS message object.
	 * 
	 * @param picName	The filename of the picture.
	 */
	public void setPicName(String picName)
	{
		this.picName = picName;
	}
	
	/**
	 * This method adds picture filenames to a provided array of MMS objects.
	 * 
	 * @param context	The application context.
	 * @param mmsArray	The array of MMS objects.
	 * @param TAG		The associated TAG for logging.
	 * @return			The modified array of MMS objects.
	 */
	public static ArrayList<MMS> addMMSPictureNames(Context context, ArrayList<MMS> mmsArray, String TAG)
	{
		if (mmsArray.size() == 0)
			return mmsArray;
	
		// Initialize MMS content provider URI (undocumented)
		Uri partUri = Uri.parse("content://mms/part");	
		
		// Prepare the query parameters
		String[] partProj = new String[]{"mid","cl"};
		String partSel = "ct = ? and (";
		for (int i = 0; i < mmsArray.size(); i++)
			partSel+=" mid = ? or";
		partSel = partSel.substring(0, partSel.length()-3);
		partSel+=")";
		String[] partSelArgs = new String[mmsArray.size()+1];
		partSelArgs[0] = "image/jpeg";
		int i = 1;
		for (MMS mmsMessage:mmsArray)
			partSelArgs[i++]=Integer.toString(mmsMessage.getId());
		
		// Query MMS Part content provider for picture filenames of given MMS message objects
		Cursor mmsPartCursor = null;
		try
		{
			mmsPartCursor = context.getContentResolver().query(partUri, partProj, partSel, partSelArgs, null);
		}
		catch(Exception e)
		{
			Log.e(TAG, e.getMessage());
			return mmsArray;
		}
		
		// Parse through query results
		if (mmsPartCursor == null)
			return mmsArray;
		
		HashMap<Integer, String> picNameMap = new HashMap<Integer, String>();
		if (mmsPartCursor.moveToFirst() && mmsPartCursor.getCount() > 0)
		{
			while(!(mmsPartCursor.isAfterLast()))
			{
				String picName = "";
				int messageid = -1;
				try
				{
					messageid = mmsPartCursor.getInt(mmsPartCursor.getColumnIndex("mid"));
				}
				catch(Exception e)
				{
					Log.e(TAG, "Error getting message ID from content://mms/part");
					return mmsArray;
				}
				
				try
				{
					picName = mmsPartCursor.getString(mmsPartCursor.getColumnIndex("cl"));
				}
				catch(Exception e)
				{
					Log.e(TAG, "Error obtaining picture filename from content://mms/part");
					return mmsArray;
				}
				
				// Store picture filename information in a hashmap
				picNameMap.put(messageid, picName);
				mmsPartCursor.moveToNext();
			}			
		}
		
		// Add picture filenames to the mms objects in the array
		for (Integer m_id : picNameMap.keySet())
		{
			for(MMS mms:mmsArray)
			{
				if(mms.getId()==m_id)
					mms.setPicName(picNameMap.get(m_id));
			}
		}
		return mmsArray;
	}
	
	/**
	 * This method adds addresses to a provided array of MMS objects.
	 * 
	 * @param context	The application context.
	 * @param mmsArray	The array of MMS objects.
	 * @param TAG		The associated TAG for logging.
	 * @return			The modified array of MMS objects.
	 */
	public static ArrayList<MMS> addAddresses(Context context, ArrayList<MMS> mmsArray, String TAG)
	{
		if (mmsArray.size() == 0)
			return mmsArray;
		
		// Populate hashmap with thread_ids
		HashMap<Integer,String> threadMap = new HashMap<Integer,String>();
		for(MMS mms:mmsArray)
		{
			if (!(threadMap.containsKey(mms.getThreadId())))
				threadMap.put(mms.getThreadId(),null);
		}

		// Initialize content provider URI (undocumented)
		Uri conversationsUriUri = Uri.parse("content://mms-sms/conversations");
		
		// Prepare to query for addresses
		String[] conversationsUriProj = new String[]{"thread_id","address"};
		String conversationsUriSel = "";
		int threadCount = threadMap.keySet().size();
		if (threadCount > 0)
		{
			for (int thread_id: threadMap.keySet())
				conversationsUriSel+="thread_id = "+Integer.toString(thread_id)+" or ";
			conversationsUriSel = conversationsUriSel.substring(0, conversationsUriSel.length()-4);
		}
		
		// Query MMS-SMS conversations content provider for addresses of given MMS message objects
		Cursor conversationsUriCursor = null;
		try
		{
			conversationsUriCursor = context.getContentResolver().query(conversationsUriUri, conversationsUriProj, conversationsUriSel, null, null);
		}
		catch(Exception e)
		{
			Log.e(TAG, "Unable to query "+conversationsUriUri.toString());
			return mmsArray;
		}
		
		// Parse query results
		if(conversationsUriCursor == null)
			return mmsArray;
		
		if(conversationsUriCursor.moveToFirst() && conversationsUriCursor.getCount() > 0)
		{
			int threadId;
			String address;
			while(!(conversationsUriCursor.isAfterLast()))
			{
				threadId = conversationsUriCursor.getInt(conversationsUriCursor.getColumnIndex("thread_id"));
				address = conversationsUriCursor.getString(conversationsUriCursor.getColumnIndex("address"));
				threadMap.put(threadId, address);
				conversationsUriCursor.moveToNext();
			}
		}
		conversationsUriCursor.close();
		
		// Identify ThreadIDs with null addresses (the undocumented content provider used is unreliable)
		ArrayList<Integer> nullAddressArray = new ArrayList<Integer>();
		for (int threadId:threadMap.keySet())
		{
			if (threadMap.get(threadId) == null)
				nullAddressArray.add(threadId);
		}
	
		// Attempt to identify null addresses through alternate means
		if (nullAddressArray.size() > 0)
		{	
			// Initialize the SMS content provider URI (undocumented)
			Uri uriSMS = Uri.parse("content://sms");
			
			// Prepare to query for addresses
			String[] smsProjection = new String[]{"thread_id","address"};
			String smsSelection = "";
			for (int threadId:nullAddressArray)
				smsSelection+="thread_id = "+threadId+" or ";
			smsSelection = smsSelection.substring(0, smsSelection.length()-4);
			
			// Query SMS content provider for the addresses of a given set of thread_ids
			Cursor smsCursor = null;
			try
			{
				smsCursor = context.getContentResolver().query(uriSMS, smsProjection, smsSelection, null, null);
			}
			catch(Exception e)
			{
				Log.e(TAG, "Error querying sms database for addresses");
			}
			
			// Parse query results
			if (smsCursor == null)
			{
				Log.e(TAG, "Error querying sms database for addresses");
				return mmsArray;
			}
			
			if (smsCursor.moveToFirst() && smsCursor.getCount() > 0)
			{
				int threadID = -1;
				String address = null;
				while (!smsCursor.isAfterLast())
				{
					try
					{
						threadID = smsCursor.getInt(smsCursor.getColumnIndex("thread_id"));
					}
					catch(Exception e)
					{
						Log.e(TAG, "Error getting threadID");
						return mmsArray;
					}
					
					try
					{
						address = smsCursor.getString(smsCursor.getColumnIndex("address"));
					}
					catch(Exception e)
					{
						Log.e(TAG, "Error getting address");
						return mmsArray;
					}
					
					// Add address to the hashmap
					if (address != null)
						threadMap.put(threadID, address);
					smsCursor.moveToNext();
				}
			}
		}			
			
		// Add address to each MMS object in the array
		for (int threadId:threadMap.keySet())
		{	
			for (MMS mms:mmsArray)
			{
				if(mms.getThreadId()==threadId)
					mms.setAddress(threadMap.get(threadId));
			}
		}
		return mmsArray;
	}
	
	/**
	 * This method retrieves the most recent MMS message.
	 * 
	 * @param context	The application context.
	 * @param TAG		The associated TAG for logging.
	 * @return			An array of a single MMS message object.
	 */
	public static ArrayList<MMS> getMostRecentIncomingMMS(Context context, String TAG)
	{
		ArrayList<MMS> mmsArray = new ArrayList<MMS>();
		
		// Initialize MMS content provider URI (undocumented)
		Uri uriMMS = Uri.parse("content://mms");
		
		// Perform query for the id, thread_id, msg_box, date, and subject
		String[] mmsProjection = new String[]{"_id","thread_id","msg_box","date","sub"};
		Cursor mmsCursor = null;
		try
		{
			mmsCursor = context.getContentResolver().query(uriMMS, mmsProjection, null, null, null);
		}
		catch(Exception e)
		{
			Log.e(TAG, e.getMessage());
			return mmsArray;
		}
		
		// Parse query results
		if (mmsCursor == null)
			return mmsArray;
		
		if (mmsCursor.moveToFirst() && mmsCursor.getCount() > 0)
		{
			int msgDirection = mmsCursor.getInt(mmsCursor.getColumnIndex("msg_box"));
			int id = mmsCursor.getInt(mmsCursor.getColumnIndex("_id"));
			int threadID = mmsCursor.getInt(mmsCursor.getColumnIndex("thread_id"));
			Date date = new Date(mmsCursor.getLong(mmsCursor.getColumnIndex("date"))*1000L);
			String subject = mmsCursor.getString(mmsCursor.getColumnIndex("sub"));
			
			// Determine message direction (only capture incoming MMS messages here)
			if (msgDirection == 1)
			{
				// Set MMS Info
				MMS mmsMessage = new MMS();
				mmsMessage.setId(id);
				mmsMessage.setThreadId(threadID);
				mmsMessage.setDirection("Incoming");
				mmsMessage.setDate(date);
				mmsMessage.setSubject(subject);
				mmsArray.add(mmsMessage);
			}
		}
		return mmsArray;
	}
	
	/**
	 * This method retrieves outgoing MMS messages.
	 * 
	 * @param context		The application context.
	 * @param startingTime	The oldest time to start collecting MMS messages.
	 * @param TAG			The associated TAG for logging.
	 * @return				An array of MMS message objects.
	 */
	public static ArrayList<MMS> getMMSOutgoingMessages(Context context, long startingTime, String TAG)
	{
		ArrayList<MMS> mmsArray = new ArrayList<MMS>();

		// Initialize MMS content provider URI (undocumented)
		Uri uriMMS = Uri.parse("content://mms");
		
		// Perform query for MMS id, thread_id, msg_box, date, and subject
		String[] mmsProjection = new String[]{"_id","thread_id","msg_box","date","sub"};
		String mmsSelection = "date >= "+startingTime;
		Cursor mmsCursor = null;
		try
		{
			mmsCursor = context.getContentResolver().query(uriMMS, mmsProjection, mmsSelection, null, null);
		}
		catch(Exception e)
		{
			Log.e(TAG, e.getMessage());
			return mmsArray;
		}
		
		// Parse query results
		if (mmsCursor == null)
		{
			Log.e(TAG, "Unable to query MMS content provider");
			return mmsArray;
		}
		
		if (mmsCursor.moveToFirst() && mmsCursor.getCount() > 0)
		{
			while (!(mmsCursor.isAfterLast()))
			{
				int msgDirection = mmsCursor.getInt(mmsCursor.getColumnIndex("msg_box"));
				int id = mmsCursor.getInt(mmsCursor.getColumnIndex("_id"));
				int threadID = mmsCursor.getInt(mmsCursor.getColumnIndex("thread_id"));
				Date date = new Date(mmsCursor.getLong(mmsCursor.getColumnIndex("date"))*1000L);
				String subject = mmsCursor.getString(mmsCursor.getColumnIndex("sub"));
				
				// Determine message direction (only capture outgoing MMS messages here)
				if (msgDirection == 2)
				{	
					// Set MMS Info
					MMS mmsMessage = new MMS();
					mmsMessage.setId(id);
					mmsMessage.setThreadId(threadID);
					mmsMessage.setDirection("Outgoing");
					mmsMessage.setDate(date);
					mmsMessage.setSubject(subject);
					mmsArray.add(mmsMessage);
				}
				mmsCursor.moveToNext();
			}
		}
		return mmsArray;
	}
	
	/**
	 * This method to attempts to adds message text to an array of MMS objects. Note: actual
	 * text was not able to be retrieved. 
	 * 
	 * @param context	The application context.
	 * @param mmsArray	The array of MMS objects.
	 * @param TAG		The associated TAG for logging.
	 * @return			The modified MMS object array.
	 */
	public static ArrayList<MMS> addMMSText(Context context, ArrayList<MMS> mmsArray, String TAG)
	{
		if (mmsArray.size() == 0)
			return mmsArray;
		
		// Initialize MMS/Part content provider URI (undocumented)
		Uri partUri = Uri.parse("content://mms/part");
		
		// Prepare query parameters
		String[] partProj = new String[]{"_id","mid","_data"};
		String partSel = "ct = ? and (";
		for (int i = 0; i < mmsArray.size(); i++)
			partSel+=" mid = ? or";
		partSel = partSel.substring(0, partSel.length()-3);
		partSel+=")";
		String[] partSelArgs = new String[mmsArray.size()+1];
		partSelArgs[0] = "text/plain";
		int i = 1;
		for (MMS mmsMessage:mmsArray)
			partSelArgs[i++]=Integer.toString(mmsMessage.getId());
		
		// Query MMS Part database for the presence of text content
		Cursor mmsPartCursor = null;
		try
		{
			mmsPartCursor = context.getContentResolver().query(partUri, partProj, partSel, partSelArgs, null);
		}
		catch(Exception e)
		{
			Log.e(TAG, e.getMessage());
			return mmsArray;
		}
		
		// Parse through query results and attempt to detect text content
		if (mmsPartCursor == null)
			return mmsArray;
		
		HashMap<Integer, String> textMap = new HashMap<Integer, String>();
		if (mmsPartCursor.moveToFirst() && mmsPartCursor.getCount() > 0)
		{
			while(!(mmsPartCursor.isAfterLast()))
			{		
				long _id = -1;
				String _data = "";
				String body = "";
				int messageid = -1;
				try
				{
					_id = mmsPartCursor.getLong(mmsPartCursor.getColumnIndex("_id"));
					messageid = mmsPartCursor.getInt(mmsPartCursor.getColumnIndex("mid"));
					_data = mmsPartCursor.getString(mmsPartCursor.getColumnIndex("_data"));
				}
				catch(Exception e)
				{
					Log.e(TAG, "Error getting content from content://mms/part");
					return mmsArray;
				}
		
				if (_data != null)
					body = getMMSText(context, _id);
				
				if (body != null)
					textMap.put(messageid, body);
				
				mmsPartCursor.moveToNext();
			}			
		}
		
		// Add text field to each MMS object in the array
		for (int msgid:textMap.keySet())
		{	
			for (MMS mms:mmsArray)
			{
				if(mms.getId()==msgid)
					mms.setText(textMap.get(msgid));
			}
		}
		return mmsArray;
	}
	
	/**
	 * This method attempts to get the text of an MMS message. This
	 * code results in either a null or blank string returned for
	 * each message, depending on if there was text content included.
	 * 
	 * @param context	The application context.
	 * @param _id		The MMS message id.
	 * @return			The MMS message text.
	 */
	private static String getMMSText(Context context, long _id)
	{
		// Initialize MMS/Part/ID content provider URI (undocumented)
		Uri partURI = Uri.parse("content://mms/part/" + _id);
		
		// Attempt to extract text content
	    InputStream is = null;
	    StringBuilder sb = new StringBuilder();
	    try
	    {
	        is = context.getContentResolver().openInputStream(partURI);
	        if (is != null)
	        {
	        	InputStreamReader isr = new InputStreamReader(is, "UTF-8");
	        	BufferedReader reader = new BufferedReader(isr);
	        	String temp = reader.readLine();
	        	while (temp != null)
	        	{
	        		sb.append(temp);
	        		temp = reader.readLine();
	        	}
	        }
	    }
	    catch (IOException e) {}
	    finally
	    {
	        if (is != null)
	        {
	            try
	            {
	                is.close();
	            }
	            catch (IOException e) {}
	        }
	    }
	    return sb.toString();
	}
}