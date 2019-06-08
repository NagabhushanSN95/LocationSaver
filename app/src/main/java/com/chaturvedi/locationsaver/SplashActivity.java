// Shree KRISHNAya Namaha
// Author: Nagabhushan S N

package com.chaturvedi.locationsaver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.Toast;

import com.chaturvedi.locationsaver.updates.Update01To03;

public class SplashActivity extends Activity
{
	private static final String ALL_PREFERENCES = "AllPreferences";
	private static final String KEY_APP_VERSION = "AppVersionNo";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_splash);

//		new Update01To03(SplashActivity.this);
		checkForUpdates();
//		readPreferences();

		Intent mainIntent = new Intent(this, MainActivity.class);
		startActivity(mainIntent);
		finish();
	}

	private void checkForUpdates()
	{
		int currentVersionNo = 0, previousVersionNo=0;
		
		// Get the Current Version No Of The Current App
		try
		{
			currentVersionNo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
		}
		catch (NameNotFoundException e)
		{
			Toast.makeText(getApplicationContext(), "Error In Retrieving Version No In\n" +
					"SplashActivity\\checkForUpdates\n" + e.getMessage(), Toast.LENGTH_LONG).show();
		}
		
		// Get the version no stored in the preferences. This contains the version no of the app, when it was 
		// previously opened. So, if the app is updated now, this field contains version no of old app.
		// So, update classes can be run
		SharedPreferences preferences = getSharedPreferences(ALL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		if(preferences.contains(KEY_APP_VERSION))
		{
			previousVersionNo = preferences.getInt(KEY_APP_VERSION, 0);
		}
		else
		{
			initialize();
			return;		// If App Version doesn't exist, then the App is opened for 1st time. So, no need to check for updates
		}
		
		// Compare the version of current and previous App. If the previous app was of old version, 
		// run the Update Classes
		if(currentVersionNo > previousVersionNo)
		{
			if(previousVersionNo < 3)
			{
				new Update01To03(SplashActivity.this);
			}
			editor.putInt(KEY_APP_VERSION, currentVersionNo);
			editor.commit();
		}
	}
	
	/*private void readPreferences()
	{
		SharedPreferences preferences = getSharedPreferences(ALL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.commit();
	}*/

	private void initialize()
	{
		SharedPreferences preferences = getSharedPreferences(ALL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();

		// Get the Current Version No Of The Current App
		int currentVersionNo = 0;
		try
		{
			currentVersionNo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
		}
		catch (NameNotFoundException e)
		{
			Toast.makeText(getApplicationContext(), "Error In Retrieving Version No In\n" +
					"SplashActivity\\initialize\n" + e.getMessage(), Toast.LENGTH_LONG).show();
		}
		editor.putInt(KEY_APP_VERSION,currentVersionNo);

		editor.commit();
	}
}
