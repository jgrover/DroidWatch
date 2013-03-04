package com.droidwatch;

/**
 * DroidWatch.java
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

import com.droidwatch.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

/** This class displays an activity when the app is selected from the launch menu. **/
public class DroidWatch extends Activity
{
	public static final String TAG = "DroidWatchActivity";

	/**
	 * This method creates an activity.
	 * 
	 * @param savedInstanceState
	 */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_droidwatch);
    }
    
    /**
     * This method pops up the user consent banner.
     */
    @Override
    public void onStart()
    {
    	super.onStart();
    	
    	//Pop up consent dialog
    	Intent alertIntent = new Intent();      
		alertIntent.setClass( this , ConsentBanner.class );           
		alertIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);      
		startActivity( alertIntent );
    }

    /**
     * This method displays the options menu.
     * 
     * @param menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_droidwatch, menu);
        return true;
    }
}