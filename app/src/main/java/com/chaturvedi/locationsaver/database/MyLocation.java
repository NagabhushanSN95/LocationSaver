// Shree KRISHNAya Namaha

package com.chaturvedi.locationsaver.database;

import android.util.Log;

import java.util.Calendar;
import java.util.StringTokenizer;

public class MyLocation
{
	private int id;
	private Time createdTime;
	private Time modifiedTime;
	private String name;
	private double latitude;
	private double longitude;
	private String address;
	private String notes;

	public MyLocation(int id, Time createdTime, Time modifiedTime, String name, double latitude, double longitude, String address,
					  String notes)
	{
		this.id = id;
		this.createdTime = createdTime;
		this.modifiedTime = modifiedTime;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
		this.notes = notes;
	}

	public MyLocation(String name, String location, String address, String notes)
	{
		this.createdTime = new Time(Calendar.getInstance());
		this.modifiedTime = new Time(Calendar.getInstance());
		this.name = name;
		if(location.length() > 3)
		{
			StringTokenizer tokens = new StringTokenizer(location, ",");
			this.latitude = Double.parseDouble(tokens.nextToken().trim());
			this.longitude = Double.parseDouble(tokens.nextToken().trim());
		}
		else
		{
			this.latitude = 0;
			this.longitude = 0;
		}

		this.address = address;
		this.notes = notes;
	}

	public void setID(int ID)
	{
		this.id = ID;
	}

	public int getID()
	{
		return id;
	}

	public Time getCreatedTime()
	{
		return createdTime;
	}

	public void setModifiedTimeToCurrentTime()
	{
		modifiedTime = new Time(Calendar.getInstance());
	}

	public Time getModifiedTime()
	{
		return modifiedTime;
	}

	public String getName()
	{
		return name;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public String getLocationString()
	{
		return latitude + "," + longitude;
	}

	public String getAddress()
	{
		return address;
	}

	public String getNotes()
	{
		return notes;
	}
}
