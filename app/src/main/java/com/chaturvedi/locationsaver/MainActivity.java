// Shree KRISHNAya Namaha

package com.chaturvedi.locationsaver;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.chaturvedi.locationsaver.database.DatabaseAdapter;
import com.chaturvedi.locationsaver.database.MyLocation;
import com.chaturvedi.locationsaver.database.Time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
	public static final int ADD_LOCATION_REQUEST_PERMISSION = 101;
	public static final int IMPORT_REQUEST_PERMISSION = 102;
	public static final int EXPORT_REQUEST_PERMISSION = 103;

	private DatabaseAdapter databaseAdapter;
	private ArrayList<MyLocation> locationsList;

	private LinearLayout parentLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		databaseAdapter = new DatabaseAdapter(MainActivity.this);
		buildLayout();

		// If the app is opened from other apps to save location, then create addLocation dialog
		Intent intent = getIntent();
		if ((intent != null) && (intent.getAction() != null) && (intent.getAction().equals(
				"android.intent.action.VIEW")))
		{
			String dataString = intent.getDataString();
			String[] tokens = dataString.split(":|=| ");
			buildAddLocationDialog(tokens[3]);            // tokens[3] contains latitude,longitude
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
				// buildAddLocationDialog();
				checkAddLocationPermissions();
				return true;

			case R.id.action_export:
				// exportLocationsToSD();
				checkExportPermissions();
				return true;

			case R.id.action_import:
				// importLocationsFromSD();
				checkImportPermissions();
				return true;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults)
	{
		boolean permissionGranted = (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
		switch (requestCode)
		{
			case ADD_LOCATION_REQUEST_PERMISSION:
				buildAddLocationDialog();
				break;

			case IMPORT_REQUEST_PERMISSION:
				if(permissionGranted)
				{
					importLocationsFromSD();
				}
				else
				{
					Toast.makeText(MainActivity.this, "Please provide Read permission to import locations " +
							"from SD card", Toast.LENGTH_LONG).show();
				}
				break;

			case EXPORT_REQUEST_PERMISSION:
				if(permissionGranted)
				{
					exportLocationsToSD();
				}
				else
				{
					Toast.makeText(MainActivity.this, "Please provide Write permission to export locations " +
							"to SD card", Toast.LENGTH_LONG).show();
				}
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		switch (requestCode)
		{
			case Constants.CODE_FILE_CHOOSER:
				if (resultCode == RESULT_OK)
				{
					// Get the Uri of the selected file
					String absolutePath = Objects.requireNonNull(intent.getData()).getPath();
					absolutePath = Objects.requireNonNull(absolutePath)
							.replaceAll("/document/primary:", "/storage/emulated/0/");
					System.out.println(absolutePath);
					readLocationsFromSD(absolutePath);
				}
		}
	}

	private void checkAddLocationPermissions()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			// Permission is not granted
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					ADD_LOCATION_REQUEST_PERMISSION);
		}
		else
		{
			// Permission Granted
			buildAddLocationDialog();
		}
	}

	private void checkImportPermissions()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			// Permission is not granted
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
					IMPORT_REQUEST_PERMISSION);
		}
		else
		{
			// Permission Granted
			importLocationsFromSD();
		}
	}

	private void checkExportPermissions()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			// Permission is not granted
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					EXPORT_REQUEST_PERMISSION);
		}
		else
		{
			// Permission Granted
			exportLocationsToSD();
		}
	}

	private void buildLayout()
	{
		locationsList = databaseAdapter.getAllLocations();
		sortLocationsByName();

		parentLayout = findViewById(R.id.parentLayout);
		for (MyLocation myLocation : locationsList)
		{
			displayNewLocation(myLocation);
		}
	}

	private void rebuildLayout()
	{
		parentLayout.removeAllViews();
		buildLayout();
	}

	private LinearLayout displayNewLocation(final MyLocation myLocation)
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		LinearLayout locationLayout = (LinearLayout) inflater.inflate(R.layout.layout_location,
				parentLayout, false);
		((TextView) locationLayout.findViewById(R.id.textView_name)).setText(myLocation.getName());
		parentLayout.addView(locationLayout);

		locationLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				int slno = parentLayout.indexOfChild(view);
				MyLocation myLocation1 = locationsList.get(slno);
				if ((myLocation1.getLatitude() != 0) || (myLocation1.getLongitude() != 0))
				{
//					String uri = String.format(Locale.ENGLISH, "geo:%f,%f (%s)", myLocation1
//					.getLatitude(), myLocation1.getLongitude(), myLocation1.getName());
					String geoUri =
							"http://maps.google.com/maps?q=loc:" + myLocation1.getLatitude() + "," + myLocation1.getLongitude() + " (" + myLocation1.getName() + ")";
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
						int slno = parentLayout.indexOfChild(view);
						final MyLocation myLocation1 = locationsList.get(slno);
						switch (optionNo)
						{
							case 0:                // Open in Maps
								if ((myLocation1.getLatitude() != 0) || (myLocation1.getLongitude() != 0))
								{
//									String uri = String.format(Locale.ENGLISH, "geo:%f,%f (%s)",
//									myLocation1.getLatitude(), myLocation1.getLongitude(),
//									myLocation1.getName());
									String geoUri =
											"http://maps.google.com/maps?q=loc:" + myLocation1.getLatitude() + "," + myLocation1.getLongitude() + " (" + myLocation1.getName() + ")";
									Intent intent = new Intent(Intent.ACTION_VIEW,
											Uri.parse(geoUri));
									MainActivity.this.startActivity(intent);
								}
								break;

							case 2:
								editLocation(slno);
								break;

							case 3:
								deleteLocation(slno);
								break;

							default:
								Toast.makeText(MainActivity.this, "Coming Soon",
										Toast.LENGTH_LONG).show();
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

	private void buildAddLocationDialog()
	{
		final AlertDialog.Builder addLocationBuilder = new AlertDialog.Builder(this);
		addLocationBuilder.setTitle("Add Location");
		LayoutInflater inflater = LayoutInflater.from(this);
		final LinearLayout addLocationDialogView =
				(LinearLayout) inflater.inflate(R.layout.layout_add_location, null);
		addLocationBuilder.setView(addLocationDialogView);

		final LocationManager locationManager =
				(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final LocationListener locationListener =
				new MyLocationListener((EditText) addLocationDialogView.findViewById(R.id.editText_location));
		try
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10,
					locationListener);
		}
		catch (SecurityException e)
		{
			Log.d("MainActivity", e.getMessage(), e.fillInStackTrace());
		}
		/*if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
		PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this,new String[]{Manifest.permission
			.ACCESS_FINE_LOCATION},101);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10,
			locationListener);
		}*/

		addLocationBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String name =
						((EditText) addLocationDialogView.findViewById(R.id.editText_name)).getText().toString().trim();
				String location =
						((EditText) addLocationDialogView.findViewById(R.id.editText_location)).getText().toString().trim();
				String address =
						((EditText) addLocationDialogView.findViewById(R.id.editText_address)).getText().toString().trim();
				String notes =
						((EditText) addLocationDialogView.findViewById(R.id.editText_notes)).getText().toString().trim();

				MyLocation newLocation = new MyLocation(name, location, address, notes);
				newLocation.setID(locationsList.size() + 1);
				databaseAdapter.addLocation(newLocation);
				locationsList.add(newLocation);
//				saveLocationToSD(name,location,address,notes);
				LinearLayout locationLayout = displayNewLocation(newLocation);
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

	/**
	 * This method is called when Location Saver is opened from Whatsapp or other apps to save the
	 * location sent during
	 * the intent and not the current location
	 *
	 * @param location 12.059856,24.5686846
	 */
	private void buildAddLocationDialog(String location)
	{
		final AlertDialog.Builder addLocationBuilder = new AlertDialog.Builder(this);
		addLocationBuilder.setTitle("Add Location");
		LayoutInflater inflater = LayoutInflater.from(this);
		final LinearLayout addLocationDialogView =
				(LinearLayout) inflater.inflate(R.layout.layout_add_location, null);
		((EditText) addLocationDialogView.findViewById(R.id.editText_location)).setText(location);
		addLocationBuilder.setView(addLocationDialogView);

		addLocationBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String name =
						((EditText) addLocationDialogView.findViewById(R.id.editText_name)).getText().toString().trim();
				String location =
						((EditText) addLocationDialogView.findViewById(R.id.editText_location)).getText().toString().trim();
				String address =
						((EditText) addLocationDialogView.findViewById(R.id.editText_address)).getText().toString().trim();
				String notes =
						((EditText) addLocationDialogView.findViewById(R.id.editText_notes)).getText().toString().trim();

				MyLocation newLocation = new MyLocation(name, location, address, notes);
				newLocation.setID(locationsList.size() + 1);
				databaseAdapter.addLocation(newLocation);
				locationsList.add(newLocation);
				LinearLayout locationLayout = displayNewLocation(newLocation);
				locationLayout.requestFocus();
			}
		});
		addLocationBuilder.setNegativeButton("Cancel", null);
		addLocationBuilder.show();
	}

	private void editLocation(int slno)
	{
		final MyLocation myLocation = locationsList.get(slno);
		final AlertDialog.Builder editLocationBuilder = new AlertDialog.Builder(this);
		editLocationBuilder.setTitle("Edit Location");
		LayoutInflater inflater = LayoutInflater.from(this);
		final LinearLayout editLocationDialogView =
				(LinearLayout) inflater.inflate(R.layout.layout_add_location, null);
		((EditText) editLocationDialogView.findViewById(R.id.editText_name)).setText(myLocation.getName());
		((EditText) editLocationDialogView.findViewById(R.id.editText_location)).setText(myLocation.getLocationString());
		((EditText) editLocationDialogView.findViewById(R.id.editText_address)).setText(myLocation.getAddress());
		((EditText) editLocationDialogView.findViewById(R.id.editText_notes)).setText(myLocation.getNotes());
		editLocationBuilder.setView(editLocationDialogView);

		editLocationBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String name =
						((EditText) editLocationDialogView.findViewById(R.id.editText_name)).getText().toString().trim();
				String location =
						((EditText) editLocationDialogView.findViewById(R.id.editText_location)).getText().toString().trim();
				String address =
						((EditText) editLocationDialogView.findViewById(R.id.editText_address)).getText().toString().trim();
				String notes =
						((EditText) editLocationDialogView.findViewById(R.id.editText_notes)).getText().toString().trim();

				MyLocation newLocation = new MyLocation(name, location, address, notes);
				newLocation.setID(myLocation.getID());
				newLocation.setModifiedTimeToCurrentTime();
				databaseAdapter.updateLocation(newLocation);
				locationsList.add(newLocation);
				LinearLayout locationLayout = displayNewLocation(newLocation);
				locationLayout.requestFocus();
			}
		});
		editLocationBuilder.setNegativeButton("Cancel", null);
		editLocationBuilder.show();
	}

	private void deleteLocation(final int slno)
	{
		final MyLocation myLocation1 = locationsList.get(slno);

		AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(MainActivity.this);
		deleteBuilder.setTitle("Delete Location");
		deleteBuilder.setMessage("Are you sure to delete location \"" + myLocation1.getName() +
				"\" ?");
		deleteBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				databaseAdapter.deleteLocation(myLocation1);
				locationsList.remove(slno);
				parentLayout.removeViewAt(slno);
				Toast.makeText(MainActivity.this, myLocation1.getName() + " Location Deleted",
						Toast.LENGTH_LONG).show();
			}
		});
		deleteBuilder.setNegativeButton("Cancel", null);
		deleteBuilder.show();
	}

	private void saveLocationToSD(String name, String location, String address, String notes)
	{
		File folder = new File(Environment.getExternalStoragePublicDirectory("Chaturvedi"),
				"Location Saver");
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		String fileName = "Locations.txt";
		File locationsFile = new File(folder, fileName);
		if (!locationsFile.exists())
		{
			try
			{
				locationsFile.createNewFile();
			}
			catch (IOException e)
			{
				Log.d("MainActivity", e.getMessage(), e.fillInStackTrace());
			}
		}

		try
		{
			BufferedWriter locationWriter = new BufferedWriter(new FileWriter(locationsFile,
					true));
			locationWriter.write(name + "\n");
			locationWriter.write(location + "\n");
			locationWriter.write(address + "\n");
			locationWriter.write(notes + "\n\n");
			locationWriter.close();
		}
		catch (IOException e)
		{
			Log.d("MainActivity", e.getMessage(), e.fillInStackTrace());
		}
	}

	private void exportLocationsToSD()
	{
		File folder = new File(Environment.getExternalStoragePublicDirectory("Android"),
				"Chaturvedi/Location Saver");
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		String fileName =
				"Locations-" + new Time(Calendar.getInstance()).getTimeInFileNameFormat() + ".txt";
		File locationsFile = new File(folder, fileName);
		if (!locationsFile.exists())
		{
			try
			{
				locationsFile.createNewFile();
			}
			catch (IOException e)
			{
				Log.d("MainActivity", e.getMessage(), e.fillInStackTrace());
			}
		}

		try
		{
			BufferedWriter locationWriter = new BufferedWriter(new FileWriter(locationsFile,
					true));
			locationWriter.write("Name\n");
			locationWriter.write("Coordinates\n");
			locationWriter.write("Address\n");
			locationWriter.write("Notes\n\n");

			for (MyLocation myLocation : locationsList)
			{
				locationWriter.write(myLocation.getName() + "\n");
				locationWriter.write(myLocation.getLocationString() + "\n");
				locationWriter.write(myLocation.getAddress() + "\n");
				locationWriter.write(myLocation.getNotes() + "\n\n");
			}
			locationWriter.close();

			Toast.makeText(this, "Export Data Successful", Toast.LENGTH_LONG).show();
		}
		catch (IOException e)
		{
			Log.d("MainActivity", e.getMessage(), e.fillInStackTrace());
		}
	}

	private void importLocationsFromSD()
	{
		// Start Activity to choose file
		Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
		fileIntent.setType("*/*"); // intent type to filter application based on your requirement
		startActivityForResult(fileIntent, Constants.CODE_FILE_CHOOSER);
	}

	private void readLocationsFromSD(String path)
	{
		File importFile = new File(path);

		ArrayList<MyLocation> myLocations = new ArrayList<>();
		try
		{
			BufferedReader locationsReader = new BufferedReader(new FileReader(importFile));

			// To remove initial header
			locationsReader.readLine();
			locationsReader.readLine();
			locationsReader.readLine();
			locationsReader.readLine();
			locationsReader.readLine();

			int nextID = locationsList.size() + 1;
			String line = locationsReader.readLine();
			while (line != null)
			{
				String name = line;
				String location = locationsReader.readLine().trim();
				String address = locationsReader.readLine();
				String notes = locationsReader.readLine();
				locationsReader.readLine();
				line = locationsReader.readLine();
				MyLocation myLocation = new MyLocation(name, location, address, notes);
				myLocation.setID(nextID++);
				myLocations.add(myLocation);
			}
		}
		catch (IOException | NumberFormatException e)
		{
			Log.d("MainActivity", e.getMessage(), e.fillInStackTrace());
			int numLocationsRead = myLocations.size();
			Toast.makeText(MainActivity.this, "File has been corrupted. " + numLocationsRead +
							" Locations recovered",
					Toast.LENGTH_LONG).show();
		}
		databaseAdapter.addAllLocations(myLocations);
		rebuildLayout();
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
