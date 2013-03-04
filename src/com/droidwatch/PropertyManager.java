package com.droidwatch;

/**
 * PropertyManager.java
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

/** This class handles the requests for DroidWatch properties. **/
public class PropertyManager
{
	public static final String TAG="PropertyManager";
	
	/**
	 * This method opens the asset file that contains the DroidWatch properties.
	 * 
	 * @param context		The application context.
	 * @return				The list of properties.
	 */
	public static Properties openProperties(Context context)
	{
		Resources resources = context.getResources();
		AssetManager assetManager = resources.getAssets();

		// Read from the /assets directory
		try
		{
		    InputStream inputStream = assetManager.open("droidwatch.properties");
		    Properties properties = new Properties();
		    properties.load(inputStream);
		    inputStream.close();
		    return properties;
		} 
		catch (IOException e)
		{
		    Log.e(TAG,"Failed to open DroidWatch properties file");
		    return null;
		}
	}
}