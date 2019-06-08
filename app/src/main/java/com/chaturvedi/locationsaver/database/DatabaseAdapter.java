// Shree KRISHNAya Namaha

package com.chaturvedi.locationsaver.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseAdapter extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "LocationSaver";
	private static final String TABLE_LOCATIONS = "Locations";

	private static final String KEY_ID = "id";
	private static final String KEY_CREATED_TIME = "created_time";
	private static final String KEY_MODIFIED_TIME = "modified_time";
	private static final String KEY_NAME = "name";
	private static final String KEY_LATITUDE = "latitude";
	private static final String KEY_LONGITUDE = "longitude";
	private static final String KEY_ADDRESS = "address";
	private static final String KEY_NOTES = "notes";

	// Table Create Statement
	private static String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("+
			KEY_ID + " INTEGER PRIMARY KEY," +
			KEY_CREATED_TIME + " TEXT," +
			KEY_MODIFIED_TIME + " TEXT," +
			KEY_NAME + " TEXT," +
			KEY_LATITUDE + " DOUBLE," +
			KEY_LONGITUDE + " DOUBLE," +
			KEY_ADDRESS + " TEXT,"+
			KEY_NOTES + " TEXT" + ")";


	public DatabaseAdapter(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// Create The Tables
		db.execSQL(CREATE_LOCATIONS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
		// Create tables again
		onCreate(db);
	}

	// Getting Number Of Locations
	public int getNumLocations()
	{
		String countQuery = "SELECT * FROM " + TABLE_LOCATIONS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int numTransactions = cursor.getCount();
		cursor.close();
		db.close();
		return numTransactions;
	}

	/**
	 * Adds A New Location
	 *
	 */
	public void addLocation(MyLocation myLocation)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ID, myLocation.getID());
		values.put(KEY_CREATED_TIME, myLocation.getCreatedTime().toString());
		values.put(KEY_MODIFIED_TIME, myLocation.getModifiedTime().toString());
		values.put(KEY_NAME, myLocation.getName());
		values.put(KEY_LATITUDE, myLocation.getLatitude());
		values.put(KEY_LONGITUDE, myLocation.getLongitude());
		values.put(KEY_ADDRESS, myLocation.getAddress());
		values.put(KEY_NOTES, myLocation.getNotes());

		// Inserting Row
		db.insert(TABLE_LOCATIONS, null, values);
		db.close(); // Closing database connection
	}

	public void addAllLocations(ArrayList<MyLocation> locations)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		for(MyLocation myLocation: locations)
		{
			ContentValues values = new ContentValues();
			values.put(KEY_ID, myLocation.getID());
			values.put(KEY_CREATED_TIME, myLocation.getCreatedTime().toString());
			values.put(KEY_MODIFIED_TIME, myLocation.getModifiedTime().toString());
			values.put(KEY_NAME, myLocation.getName());
			values.put(KEY_LATITUDE, myLocation.getLatitude());
			values.put(KEY_LONGITUDE, myLocation.getLongitude());
			values.put(KEY_ADDRESS, myLocation.getAddress());
			values.put(KEY_NOTES, myLocation.getNotes());

			db.insert(TABLE_LOCATIONS, null, values);
		}
		db.close();
	}

	// Getting single Location
	public MyLocation getLocation(int id)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_LOCATIONS, new String[] { KEY_ID, KEY_CREATED_TIME,
						KEY_MODIFIED_TIME, KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE, KEY_ADDRESS, KEY_NOTES,}, KEY_ID + "=?",
						new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		MyLocation myLocation = new MyLocation(cursor.getInt(0), new Time(cursor.getString(1)),
				new Time(cursor.getString(2)), cursor.getString(3), cursor.getDouble(4),
				cursor.getDouble(5), cursor.getString(6), cursor.getString(7));
		cursor.close();
		db.close();
		return myLocation;
	}

	// Getting All Locations
	public ArrayList<MyLocation> getAllLocations()
	{
		ArrayList<MyLocation> locationsList = new ArrayList<>();
		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_LOCATIONS;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if (cursor.moveToFirst())
		{
			do
			{
				MyLocation myLocation = new MyLocation(cursor.getInt(0), new Time(cursor.getString(1)),
						new Time(cursor.getString(2)), cursor.getString(3), cursor.getDouble(4),
						cursor.getDouble(5), cursor.getString(6), cursor.getString(7));
				locationsList.add(myLocation);
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return locationsList;
	}

	// Updating single Location
	public void updateLocation(MyLocation myLocation)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_CREATED_TIME, myLocation.getCreatedTime().toString());
		values.put(KEY_MODIFIED_TIME, myLocation.getModifiedTime().toString());
		values.put(KEY_NAME, myLocation.getName());
		values.put(KEY_LATITUDE, myLocation.getLatitude());
		values.put(KEY_LONGITUDE, myLocation.getLongitude());
		values.put(KEY_ADDRESS, myLocation.getAddress());
		values.put(KEY_NOTES, myLocation.getName());
		// updating row
		db.update(TABLE_LOCATIONS, values, KEY_ID + " = ?",
				new String[] { String.valueOf(myLocation.getID()) });
		db.close();
	}

	// Deleting single Location
	public void deleteLocation(MyLocation myLocation)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_LOCATIONS, KEY_ID + " = ?", new String[] { String.valueOf(myLocation.getID()) });
		//Update IDs of next Locations
		for(int i = myLocation.getID(); i<= getNumLocations(); i++)
		{
			ContentValues values = new ContentValues();
			values.put(KEY_ID, i);
			// updating row
			db.update(TABLE_LOCATIONS, values, KEY_ID + " = ?", new String[] { String.valueOf(i) });
			/*if(db.isOpen())
			{
				db.update(TABLE_LOCATIONS, values, KEY_ID + " = ?",
						new String[] { String.valueOf(i) });
			}
			else
			{
				db = this.getWritableDatabase();
				db.update(TABLE_LOCATIONS, values, KEY_ID + " = ?",
						new String[] { String.valueOf(i+1) });
			}*/
		}
		db.close();
	}

	/**
	 * Deletes All The Locations Along With The Table and then creates empty table
	 */
	public void deleteAllLocations()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
		db.execSQL(CREATE_LOCATIONS_TABLE);
		db.close();
	}
}

