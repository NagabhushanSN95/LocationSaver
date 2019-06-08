// Shree KRISHNAya Namaha

package com.chaturvedi.locationsaver.database;

import java.util.StringTokenizer;

public class MyLocation
{
	private String createdTime;
	private String modifiedTime;
	private String name;
	private double latitude;
	private double longitude;
	private String address;
	private String notes;

	public MyLocation(String name, String location, String address, String notes)
	{
		this.createdTime = null;
		this.modifiedTime = null;
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
