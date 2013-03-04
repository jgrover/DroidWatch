package com.droidwatch;

/**
 * StartupIntentReceiver.java
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** This class handles the automatic app startups during boot. **/
public class StartupIntentReceiver extends BroadcastReceiver
{	
	/**
	 * This method handles the broadcasted intent.
	 * 
	 * @param context	The application context.
	 * @param intent	The broadcasted intent.
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
		{
			Intent bannerIntent = new Intent(context, ConsentBanner.class);
			bannerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(bannerIntent);
		}
	}
}