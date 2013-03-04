package com.droidwatch;

/**
 * ConsentBanner.java
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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.DialogInterface; 
import android.content.Intent;
import android.os.Bundle; 
import android.util.Log;

/** This class handles the displayed consent banner. **/
public class ConsentBanner extends Activity
{
	// Initialize constants and variables
	public static final String TAG = "ConsentBannerActivity";

	/**
	 * This method handles the creation of the activity.
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		AlertDialog dialogBox = makeAndShowDialogBox();
		dialogBox.show();
	}

	/**
	 * This method pops up the user consent banner and starts/stops the watcher service.
	 * 
	 * @return	The selected option.
	 */
	private AlertDialog makeAndShowDialogBox()
	{
		// Get text values for user consent banner
		String consentBannerText = getString(R.string.user_consent_banner_text);
		String consentBannerTitle = getString(R.string.user_consent_banner_title);
		
		// Pop up consent dialog
		AlertDialog myDialogBox = 

	        	new AlertDialog.Builder(this)
	        	.setTitle(consentBannerTitle) 
	        	.setMessage(consentBannerText)
	        	.setPositiveButton("Accept", new DialogInterface.OnClickListener()
	        	{ 
	        		public void onClick(DialogInterface dialog, int whichButton)
	        		{	
	        			// User accepted the terms
	        			Log.v(TAG,"User Consent Banner - Accepted");
	        			
	        			// Close GUI
	        			finish();
	        			
	        			//Start the service
	        	        if(!isServiceRunning())
	        	        {  	       
	        	    	    Intent serviceIntent = new Intent("com.droidwatch.WatcherService");
	        	    	    startService(serviceIntent);
	        	        }
	        		}              
	        	})
	        	.setNegativeButton("Reject", new DialogInterface.OnClickListener()
	        	{ 
	        		public void onClick(DialogInterface dialog, int whichButton)
	        		{ 
	        			finish();
	        			if(isServiceRunning())
	        			{
	        	    	    Intent serviceStopIntent = new Intent("com.droidwatch.WatcherService");
	        	    	    stopService(serviceStopIntent);
	        	        }
	        		} 
	        	})
	        	.create(); 	
	        	return myDialogBox;
	}
	
	/**
	 * This method checks to see if the watcher service is running.
	 * @return
	 */
	private boolean isServiceRunning() 
	{
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
	    {
	        if ("com.droidwatch.WatcherService".equals(service.service.getClassName()))
	            return true;
	    }
	    return false;
	}
}