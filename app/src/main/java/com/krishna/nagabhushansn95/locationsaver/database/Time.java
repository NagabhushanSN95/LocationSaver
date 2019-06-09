package com.krishna.nagabhushansn95.locationsaver.database;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

public class Time
{
	private int year;
	private int month;
	private int date;
	private int hour;
	private int minute;
	private int second;
	private int millis;
	
	public Time(int year, int month, int date, int hour, int minute, int second, int millis)
	{
		this.setYear(year);
		this.setMonth(month);
		this.setDate(date);
		this.setHour(hour);
		this.setMinute(minute);
		this.setSecond(second);
		this.setMillis(millis);
	}
	
	public Time(String time)
	{
		StringTokenizer tokens = new StringTokenizer(time,"/");
		this.year = Integer.parseInt(tokens.nextToken());
		this.month = Integer.parseInt(tokens.nextToken());
		this.date = Integer.parseInt(tokens.nextToken());
		this.hour = Integer.parseInt(tokens.nextToken());
		this.minute = Integer.parseInt(tokens.nextToken());
		this.second = Integer.parseInt(tokens.nextToken());
		this.millis = Integer.parseInt(tokens.nextToken());
	}
	
	public Time(Calendar calendar)
	{
		this.year = calendar.get(Calendar.YEAR);
		this.month = calendar.get(Calendar.MONTH) + 1;
		this.date = calendar.get(Calendar.DATE);
		this.hour = calendar.get(Calendar.HOUR);
		this.minute = calendar.get(Calendar.MINUTE);
		this.second = calendar.get(Calendar.SECOND);
		this.millis = calendar.get(Calendar.MILLISECOND);
	}
	
	public String toString()
	{
		String time = year + "/" + month + "/" + date + "/" + hour + "/" + minute + "/" + second + "/" + millis;
		return time;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDate() {
		return date;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public int getMillis() {
		return millis;
	}

	public void setMillis(int millis) {
		this.millis = millis;
	}
	
	public boolean isEqualTo(Time time2)
	{
		if(this.getYear() != time2.getYear())
		{
			return false;
		}
		if(this.getMonth() != time2.getMonth())
		{
			return false;
		}
		if(this.getDate() != time2.getDate())
		{
			return false;
		}
		if(this.getHour() != time2.getHour())
		{
			return false;
		}
		if(this.getMinute() != time2.getMinute())
		{
			return false;
		}
		if(this.getSecond() != time2.getSecond())
		{
			return false;
		}
		if(this.getMillis() != time2.getMillis())
		{
			return false;
		}
		return true;
	}
	
	public boolean isNotEqualTo(Time time2)
	{
		if(!this.isEqualTo(time2))
			return true;
		else
			return false;
	}

	public String getTimeInFileNameFormat()
	{
		DecimalFormat formatter = new DecimalFormat("00");
		return year + "" + formatter.format(month) + "" + formatter.format(date) + "" + formatter.format(hour) + "" +
				formatter.format(minute) + "" + formatter.format(second);
	}
}
