package com.droidwatch;

/**
 * TransferManager.java
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

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

/** This class handles transfer operations to the central server. **/
public class TransferManager
{
	private URL url = null;
	private HttpsURLConnection connection = null;
	private DataOutputStream dos = null;
	private String boundary = "*****";
	private String twoHyphens = "--";
	private String lineEnd = "\r\n";
	private int bytesRead, bytesAvailable, bufferSize;
	private byte[] buffer;
	private int maxBufferSize = 1*1024*1024;
	private String filename = "results.db";
	private FileInputStream fileInputStream = null;
	private long transferID = -1;
	private Context context = null;
	private static final Uri transfersUri = DroidWatchProvider.Transfers.CONTENT_URI;
	private static final Uri eventsUri = DroidWatchProvider.Events.CONTENT_URI;
	
	/**
	 * Constructor used to set the context.
	 * 
	 * @param context	The application context.
	 */
	public TransferManager(Context context)
	{
		this.context = context;
	}
	
	/**
	 * This method gets a device ID.
	 * 
	 * @return	The device ID.
	 */
	private String getDeviceID()
	{
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}
	
	/**
	 * This method gets the most recently completed transfer timestamp.
	 * 
	 * @param context	The application context.
	 * @return			The most recent timestamp (epoch format).
	 */
	public static long getMostRecentCompletedTransferTime(Context context)
	{
		// Query DroidWatch transfer table for most recent time
		long startTime = -1;
		String[] projection = new String[]{DroidWatchProvider.Transfers.STARTDATE};
		String selection = DroidWatchProvider.Transfers.COMPLETED+" = ?";
		String[] selectionArgs = new String[]{"1"};
		String sortOrder = DroidWatchProvider.Transfers.STARTDATE+ " DESC";
		Cursor cursor = null;
		try
		{
			cursor = context.getContentResolver().query(transfersUri, projection, selection, selectionArgs, sortOrder);
		}
		catch(Exception e)
		{
			Log.e(Transfer.TAG, "Unable to query transfers table: "+e.getMessage());
			return -1;
		}
		
		// Parse query results
		if (cursor == null)
		{
			Log.e(Transfer.TAG, "Unable to query transfers table");
			return -1;
		}
		
		// Grab the transfer time
		if (cursor.moveToFirst() && cursor.getCount() > 0)
			startTime = cursor.getLong(cursor.getColumnIndex(DroidWatchProvider.Transfers.STARTDATE));
		return startTime;
	}
	
	/**
	 * This method retrieves the transfer start time for a given transfer ID.
	 * 
	 * @return 	The starting timestamp (epoch format).
	 */
	private long getTransferStartTime()
	{
		// Prepare query
		long startTime = -1;
		String[] projection = new String[]{DroidWatchProvider.Transfers.STARTDATE};
		String selection = DroidWatchProvider.Transfers._ID+" = ?";
		String[] selectionArgs = new String[]{String.valueOf(transferID)};
		Cursor cursor = null;
		try
		{
			cursor = context.getContentResolver().query(transfersUri, projection, selection, selectionArgs, null);
		}
		catch(Exception e)
		{
			Log.e(Transfer.TAG, "Unable to query transfers table: "+e.getMessage());
			return -1;
		}
		
		// Parse results
		if (cursor == null)
		{
			Log.e(Transfer.TAG, "Unable to query transfers table");
			return -1;
		}
		
		// Grab transfer start time
		if (cursor.moveToFirst() && cursor.getCount() > 0)
			startTime = cursor.getLong(cursor.getColumnIndex(DroidWatchProvider.Transfers.STARTDATE));
		return startTime;
	}
	
	/**
	 * This method inserts a new transfer (after a connection has been established).
	 * 
	 * @return The transfer status.
	 */
	public boolean startTransfer()
	{
		// Insert new entry in the DroidWatch transfer table
		ContentValues values = new ContentValues();
		values.put(DroidWatchProvider.Transfers.COMPLETED, 0);
		values.put(DroidWatchProvider.Transfers.DEVICE_ID, getDeviceID());
		Uri itemUri = context.getContentResolver().insert(transfersUri, values);
		transferID = Long.parseLong(itemUri.getLastPathSegment());
		//Log.i(Transfer.TAG, "ID: "+transferID);
		
		if (transferID >= 0)
			return true;
		return false;
	}

	/**
	 * This method attempts to begin a new transfer.
	 * 
	 * @return	The transfer status.
	 */
	public boolean getConnection(String certFileName, String serverURL)
	{
		// Prepare for HTTPS connection
		CertificateFactory cf = null;
		try
		{
			cf = CertificateFactory.getInstance("X.509");
		}
		catch (CertificateException e)
		{
			Log.e(Transfer.TAG, "Certificate Error: "+e.getMessage());
			return false;
		}
		
		// Retrieve self-signed certificate
        InputStream caInput = null;
		try
		{
			caInput = new BufferedInputStream(context.getAssets().open(certFileName));
		}
		catch (FileNotFoundException e)
		{
			Log.e(Transfer.TAG, "File Not Found: "+e.getMessage());
			return false;
		}
		catch (IOException e)
		{
			Log.e(Transfer.TAG, "IOError: "+e.getMessage());
			return false;
		}
		
		// Incorporate self-signed certificate
        Certificate ca = null;
        try
        {
            ca = cf.generateCertificate(caInput);
        }
        catch(CertificateException e)
        {
        	Log.e(Transfer.TAG, "Certificate Error: "+e.getMessage());
        	return false;
        }
        
        try
        {
        	caInput.close();
        }
        catch (IOException e)
        {
        	Log.e(Transfer.TAG, "IOError: "+e.getMessage());
        	return false;
        }

        // Create a KeyStore containing the trusted certificate
        KeyStore keyStore = null;
        TrustManagerFactory tmf = null;
        SSLContext sslContext = null;
		try
		{
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);
	        keyStore.setCertificateEntry("ca", ca); 
	        
	        tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(keyStore);
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);
		}
		catch (KeyStoreException e)
		{
			Log.e(Transfer.TAG,"KeyStore Error: "+e.getMessage());
			return false;
		}
		catch (CertificateException e)
		{
			Log.e(Transfer.TAG, "Certificate Error: "+e.getMessage());
			return false;
		}
		catch (NoSuchAlgorithmException e)
		{
			Log.e(Transfer.TAG, "NoSuchAlgorithm Error: "+e.getMessage());
			return false;
		}
		catch (IOException e)
		{
			Log.e(Transfer.TAG, "IOException Error: "+e.getMessage());
			return false;
		}
		catch (KeyManagementException e)
		{
			Log.e(Transfer.TAG, "KeyManagementException Error: "+e.getMessage());
			return false;
		}
		
		// Connect to webserver
		try
		{
			url = new URL(serverURL);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setSSLSocketFactory(sslContext.getSocketFactory());
		} 
		catch (MalformedURLException e)
		{
			Log.e(Transfer.TAG, "Invalid URL");
			return false;
		}
		catch (IOException e)
		{
			Log.e(Transfer.TAG, "Unable to connect");
			return false;
		}
		catch (Exception e)
		{
			Log.e(Transfer.TAG, "Error: "+e.getMessage());
			return false;
		}
		
		//Log.i(Transfer.TAG, "Connected successfully");
		return true;
	}

	/**
	 * This method performs the data transfer.
	 * 
	 * @return The transfer status.
	 */
	public boolean pushToServer()
	{	
		// Find the file path of the SQLite database to transfer.
		String filePath = context.getDatabasePath(filename).getAbsolutePath();
		try
		{
			fileInputStream = new FileInputStream(filePath);
		}
		catch (FileNotFoundException e)
		{
			Log.e(Transfer.TAG, "Error: "+e.getMessage());
			return false;
		}	
		
		// Prepare to push the database file to the server using HTTPS POST
		try
		{
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
		}
		catch (Exception e)
		{
			Log.e(Transfer.TAG, "Error setting connection properties: "+e.getMessage());
			return false;
		}
		
		try
		{
			connection.setRequestMethod("POST");
		}
		catch (ProtocolException e)
		{
			Log.e(Transfer.TAG, "Error: "+e.getMessage());
			return false;
		}
		
		// Prepare header information
		try
		{
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
		}
		catch (Exception e)
		{
			Log.e(Transfer.TAG, "Error setting connection properties: "+e.getMessage());
			return false;
		}
		
		try
		{
			OutputStream os = connection.getOutputStream();
			if (os == null)
			{
				Log.e(Transfer.TAG, "outputstream is null");
				return false;
			}	
			dos = new DataOutputStream(os);
		}
		catch(Exception e)
		{
			Log.e(Transfer.TAG, "Error creating output stream: "+e.getMessage());
			return false;
		}
		
		try
		{
	        dos.writeBytes(twoHyphens + boundary + lineEnd);
	        dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + filename +"\"" + lineEnd);
	        dos.writeBytes(lineEnd);
		}
		catch (Exception e)
		{
			Log.e(Transfer.TAG,"Error writing initial output stream: "+e.getMessage());
			return false;
		}
		
		// Create a buffer of maximum size
		try
		{
	        bytesAvailable = fileInputStream.available();
	        bufferSize = Math.min(bytesAvailable, maxBufferSize);
	        buffer = new byte[bufferSize];
		}
		catch (Exception e)
		{
			Log.e(Transfer.TAG, "Error creating buffer: "+e.getMessage());
			return false;
		}
        
        // Write file into form and POST it to the server
		try
		{
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		}
		catch(Exception e)
		{
			Log.e(Transfer.TAG, "Error reading input file: "+e.getMessage());
			return false;
		}
		
        while (bytesRead > 0)
        {
			try
			{
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
	            bufferSize = Math.min(bytesAvailable,maxBufferSize);
	            bytesRead = fileInputStream.read(buffer, 0,bufferSize);
			}
			catch (IOException e)
			{
				Log.e(Transfer.TAG, "Error: "+e.getMessage());
				return false;
			}
        }
        
        // Verify a successful transfer
        int serverResponseCode = 0;
        try
        {
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			
			// Responses from the server (code and message)
	        serverResponseCode = connection.getResponseCode();

	        // Close streams
	        fileInputStream.close();
	        dos.flush();
	        dos.close();
	        
	        //Log.i(Transfer.TAG,"Code: "+serverResponseCode);
        }
        catch (IOException e)
        {
			Log.e(Transfer.TAG, "Error: "+e.getMessage());
			return false;
		}
        
        if (serverResponseCode != 200)
        	return false;
        return true;
	}

	/**
	 * This method performs a wipe of the local DroidWatch app database after a successful transfer.
	 * 
	 * @return The transfer status.
	 */
	public boolean wipeDatabase()
	{
		// Delete all events before the starting transfer time
		long startTime = getTransferStartTime();
		if (startTime < 0)
			return false;
		
		int deleteCount = context.getContentResolver().delete(eventsUri, DroidWatchProvider.Events.DETECTED+" < ?", new String[]{String.valueOf(startTime)});
		if (deleteCount < 0)
			return false;
		
		// Mark transfer as complete
		ContentValues values = new ContentValues();
		values.put(DroidWatchDatabase.TRANSFERS_COMPLETED_COLUMN, 1);
		int updateCount = context.getContentResolver().update(transfersUri, values, DroidWatchProvider.Transfers._ID+" = ?", new String[]{String.valueOf(transferID)});
		if (updateCount <= 0)
			return false;
		
		return true;
	}
}