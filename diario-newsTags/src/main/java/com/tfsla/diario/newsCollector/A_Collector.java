package com.tfsla.diario.newsCollector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class A_Collector {

	public A_Collector() {
		super();
	}

	protected boolean paramValueIsMultivalued(String paramValue) {
		return paramValue.contains(",");
	}

	protected String[] getValues(String paramValue) {
		if (paramValue==null)
			return null;
		
		if (paramValue.trim().toLowerCase().equals("null") || paramValue.trim().toLowerCase().equals("undefined"))
			return null;
		
		String[] values = paramValue.split(",");
		for (int j=0;j<values.length;j++)
			values[j] = values[j].trim();
		
		return values;
	}

	protected boolean paramValueIsEmpty(Object paramValue) {
		if (paramValue==null)
			return true;
		
		if (paramValue instanceof String)
			return ((String)paramValue).trim().isEmpty();
		
		return false;
	}

	protected boolean paramValueIsStructuredValue(String paramValue) {
		if (paramValue.contains(":"))
			return true;
		
		return false;
	}

	protected boolean paramValueIsBooleanValue(String paramValue) {
		if (paramValue.contains(" AND "))
			return true;
		
		if (paramValue.contains(" OR "))
			return true;
		if (paramValue.contains(" NOT "))
			return true;
		
		return false;
	
	}

	protected Date parseDateTime(String value) {
		if (value==null)
			return null;
		
		if (value.matches("\\d{8}"))
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		if (value.matches("\\d{8}\\s\\d{4}"))
		{
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hhmm");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		if (value.matches("\\d+h"))
		{
			value = value.replace("h", "");
			int hours = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.HOUR, -1* hours);
			return cal.getTime();
		}
	
		if (value.matches("\\d+d"))
		{
			value = value.replace("d", "");
			int days = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.DAY_OF_YEAR, -1* days);
			return cal.getTime();
		}
	
		if (value.matches("\\d+M"))
		{
			value = value.replace("M", "");
			int month = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.MONTH, -1* month);
			return cal.getTime();
		}
	
		if (value.matches("\\d+y"))
		{
			value = value.replace("y", "");
			int year = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.YEAR, -1* year);
			return cal.getTime();
		}
	
		return null;
	}

	protected int parseToHours(String value) {
		if (value==null)
			return 0;
		
		if (value.matches("\\d+h"))
		{
			value = value.replace("h", "");
			int hours = Integer.parseInt(value);
	
			return hours;
		}
	
		if (value.matches("\\d+d"))
		{
			value = value.replace("d", "");
			int days = Integer.parseInt(value);
			
			return days * 24;
		}
	
		if (value.matches("\\d+M"))
		{
			value = value.replace("M", "");
			int month = Integer.parseInt(value);
			
			return month * 30 * 24;
		}
	
		if (value.matches("\\d+y"))
		{
			value = value.replace("y", "");
			int year = Integer.parseInt(value);
			
			return year * 365 * 24;
		}
	
		return 0;
	}

}