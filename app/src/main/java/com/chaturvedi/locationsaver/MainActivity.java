// Shree KRISHNAya Namaha

package com.chaturvedi.locationsaver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaturvedi.locationsaver.database.MyLocation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity
{
	private ArrayList<MyLocation> locationsList;

	private LinearLayout parentLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		buildLayout();
		Intent intent = getIntent();
		if(intent.getAction().equals("android.intent.action.VIEW"))
		{
			String dataString = intent.getDataString();
			String[] tokens = dataString.split(":|=| ");
			startAddLocation(tokens[3]);			// tokens[3] contains latitude,longitude
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		switch (menuItem.getItemId())
		{
			case R.id.action_add:
				startAddLocation();
				return true;
		}
		return true;
	}

	private void buildLayout()
	{
		locationsList = new ArrayList<>();
		readLocations();
		sortLocationsByName();

		parentLayout = (LinearLayout) findViewById(R.id.parentLayout);
		for(MyLocation myLocation : locationsList)
		{
			displayNewLocation(myLocation);
		}
	}

	private LinearLayout displayNewLocation(final MyLocation myLocation)
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		LinearLayout locationLayout = (LinearLayout) inflater.inflate(R.layout.layout_location,null);
		((TextView)locationLayout.findViewById(R.id.textView_name)).setText(myLocation.getName());
		parentLayout.addView(locationLayout);

		locationLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				int slno = parentLayout.indexOfChild(view);
				MyLocation myLocation1 = locationsList.get(slno);
				if((myLocation1.getLatitude()!=0) || (myLocation1.getLongitude()!=0))
				{
					String uri = String.format(Locale.ENGLISH, "geo:%f,%f (%s)", myLocation1.getLatitude(), myLocation1.getLongitude(), myLocation1.getName());
					String geoUri = "http://maps.google.com/maps?q=loc:" + myLocation1.getLatitude() + "," + myLocation1.getLongitude() + " (" + myLocation1.getName() + ")";
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
					MainActivity.this.startActivity(intent);
				}
			}
		});

		locationLayout.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(final View view)
			{
				String[] options = {"Open in Maps", "View", "Edit", "Delete"};
				AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(MainActivity.this);
				optionsBuilder.setTitle(myLocation.getName());
				optionsBuilder.setItems(options, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int optionNo)
					{
						switch (optionNo)
						{
							case 0:				// Open in Maps
								int slno = parentLayout.indexOfChild(view);
								MyLocation myLocation1 = locationsList.get(slno);
								if((myLocation1.getLatitude()!=0) || (myLocation1.getLongitude()!=0))
								{
									String uri = String.format(Locale.ENGLISH, "geo:%f,%f (%s)", myLocation1.getLatitude(), myLocation1.getLongitude(), myLocation1.getName());
									String geoUri = "http://maps.google.com/maps?q=loc:" + myLocation1.getLatitude() + "," + myLocation1.getLongitude() + " (" + myLocation1.getName() + ")";
									Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
									MainActivity.this.startActivity(intent);
								}
								break;

							default:
								Toast.makeText(MainActivity.this, "Coming Soon", Toast.LENGTH_LONG).show();
								break;
						}
					}
				});
				optionsBuilder.show();
				return true;
			}
		});

		return locationLayout;
	}

	private void readLocations()
	{
		File folder = new File(Environment.getExternalStoragePublicDirectory("Chaturvedi"), "Location Saver");
		if(!folder.exists())
		{
			folder.mkdirs();
		}

		String fileName = "Locations.txt";
		File locationsFile = new File(folder, fileName);
		if(!locationsFile.exists())
		{
			return;
		}

		try
		{
			BufferedReader locationsReader = new BufferedReader(new FileReader(locationsFile));

			// To remove initial header
			locationsReader.readLine();
			locationsReader.readLine();
			locationsReader.readLine();
			locationsReader.readLine();
			locationsReader.readLine();

			String line = locationsReader.readLine();
			while(line != null)
			{
				String name = line;
				String location = locationsReader.readLine().trim();
				String address = locationsReader.readLine();
				String notes = locationsReader.readLine();
				locationsReader.readLine();
				line = locationsReader.readLine();
				MyLocation myLocation = new MyLocation(name,location,address,notes);
				locationsList.add(myLocation);
			}
		}
		catch (IOException e)
		{
			Log.d("MainActivity",e.getMessage(),e.fillInStackTrace());
		}
	}

	private void sortLocationsByName()
	{
		Collections.sort(locationsList, new Comparator<MyLocation>()
		{
			@Override
			public int compare(MyLocation lhs, MyLocation rhs)
			{
				return lhs.getName().compareTo(rhs.getName());
			}
		});
	}

	private void startAddLocation()
	{
		final AlertDialog.Builder addLocationBuilder = new AlertDialog.Builder(this);
		addLocationBuilder.setTitle("Add Location");
		LayoutInflater inflater = LayoutInflater.from(this);
		final LinearLayout addLocationDialogView = (LinearLayout) inflater.inflate(R.layout.layout_add_location, null);
		addLocationBuilder.setView(addLocationDialogView);

		final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		final LocationListener locationListener = new MyLocationListener((EditText) addLocationDialogView.findViewById(R.id.editText_location));
		try
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10,locationListener);
		}
		catch(SecurityException e)
		{
			Log.d("MainActivity",e.getMessage(), e.fillInStackTrace());
		}
		/*if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10,locationListener);
		}*/

		addLocationBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String name = ((EditText)addLocationDialogView.findViewById(R.id.editText_name)).getText().toString().trim();
				String location = ((EditText)addLocationDialogView.findViewById(R.id.editText_location)).getText().toString().trim();
				String address = ((EditText)addLocationDialogView.findViewById(R.id.editText_address)).getText().toString().trim();
				String notes = ((EditText)addLocationDialogView.findViewById(R.id.editText_notes)).getText().toString().trim();

				saveLocation(name,location,address,notes);
				LinearLayout locationLayout = displayNewLocation(new MyLocation(name,location,address,notes));
				locationLayout.requestFocus();

				// Stop listening for location
				try
				{
					locationManager.removeUpdates(locationListener);
				}
				catch (SecurityException e)
				{
					Log.d("MainActivity", e.getMessage(), e.fillInStackTrace());
				}
			}
		});
		addLocationBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// Stop listening for location
				try
				{
					locationManager.removeUpdates(locationListener);
				}
				catch (SecurityException e)
				{
					Log.d("MainActivity", e.getMessage(), e.fillInStackTrace());
				}
			}
		});
		addLocationBuilder.show();
	}

	private void startAddLocation(String location)
	{
		final AlertDialog.Builder addLocationBuilder = new AlertDialog.Builder(this);
		addLocationBuilder.setTitle("Add Location");
		LayoutInflater inflater = LayoutInflater.from(this);
		final LinearLayout addLocationDialogView = (LinearLayout) inflater.inflate(R.layout.layout_add_location, null);
		((EditText)addLocationDialogView.findViewById(R.id.editText_location)).setText(location);
		addLocationBuilder.setView(addLocationDialogView);

		addLocationBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String name = ((EditText)addLocationDialogView.findViewById(R.id.editText_name)).getText().toString().trim();
				String location = ((EditText)addLocationDialogView.findViewById(R.id.editText_location)).getText().toString().trim();
				String address = ((EditText)addLocationDialogView.findViewById(R.id.editText_address)).getText().toString().trim();
				String notes = ((EditText)addLocationDialogView.findViewById(R.id.editText_notes)).getText().toString().trim();

				saveLocation(name,location,address,notes);
				LinearLayout locationLayout = displayNewLocation(new MyLocation(name,location,address,notes));
				locationLayout.requestFocus();
			}
		});
		addLocationBuilder.setNegativeButton("Cancel", null);
		addLocationBuilder.show();
	}

	private void saveLocation(String name, String location, String address, String notes)
	{
		File folder = new File(Environment.getExternalStoragePublicDirectory("Chaturvedi"), "Location Saver");
		if(!folder.exists())
		{
			folder.mkdirs();
		}

		String fileName = "Locations.txt";
		File locationsFile = new File(folder, fileName);
		if(!locationsFile.exists())
		{
			try
			{
				locationsFile.createNewFile();
			}
			catch (IOException e)
			{
				Log.d("MainActivity",e.getMessage(),e.fillInStackTrace());
			}
		}

		try
		{
			BufferedWriter locationWriter = new BufferedWriter(new FileWriter(locationsFile,true));
			locationWriter.write(name + "\n");
			locationWriter.write(location + "\n");
			locationWriter.write(address + "\n");
			locationWriter.write(notes + "\n\n");
			locationWriter.close();
		}
		catch (IOException e)
		{
			Log.d("MainActivity",e.getMessage(),e.fillInStackTrace());
		}
	}

	private class MyLocationListener implements LocationListener
	{
		private EditText locationEditText;

		public MyLocationListener(EditText locationField)
		{
			locationEditText = locationField;
		}
		@Override
		public void onLocationChanged(Location location)
		{
			locationEditText.setText(location.getLatitude() + ", " + location.getLongitude());
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{

		}

		@Override
		public void onProviderEnabled(String provider)
		{

		}

		@Override
		public void onProviderDisabled(String provider)
		{

		}
	}
}
