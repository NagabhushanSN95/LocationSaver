// Shree KRISHNAya Namaha

package com.krishna.nagabhushansn95.locationsaver.updates;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.krishna.nagabhushansn95.locationsaver.database.DatabaseAdapter;
import com.krishna.nagabhushansn95.locationsaver.database.MyLocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Update01To03
{
	// In version 3, SQL Database was introduced. In versions 1&2, locations were saved in SD Card. So, move them to Database
	public Update01To03(Context cxt)
	{
		ArrayList<MyLocation> myLocations = readLocationsFromSD();
		new DatabaseAdapter(cxt).addAllLocations(myLocations);
	}

	private ArrayList<MyLocation> readLocationsFromSD()
	{
		ArrayList<MyLocation> myLocations = new ArrayList<>();
		File folder = new File(Environment.getExternalStoragePublicDirectory("Chaturvedi"), "Location Saver");
		if(!folder.exists())
		{
			folder.mkdirs();
		}

		String fileName = "Locations.txt";
		File locationsFile = new File(folder, fileName);
		if(!locationsFile.exists())
		{
			return null;
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
				myLocation.setID(myLocations.size()+1);
				myLocations.add(myLocation);
			}
		}
		catch (IOException e)
		{
			Log.d("Update01To03",e.getMessage(),e.fillInStackTrace());
		}

		// Delete Locations File
		locationsFile.delete();

		return myLocations;
	}
}
