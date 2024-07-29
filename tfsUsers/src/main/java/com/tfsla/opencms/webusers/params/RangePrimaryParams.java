package com.tfsla.opencms.webusers.params;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RangePrimaryParams extends A_PrimaryParam {

	private Object startValue;
	private Object endValue;
	
	@Override
	public String generateSubClause() {
		return name + " >= ? AND " + name + " <= ?" ;
	}

	public Object getStartValue() {
		return startValue;
	}

	public void setStartValue(Object startValue) {
		if (startValue instanceof String) 
			dataType = DATATYPE_STRING;
		else if (startValue instanceof Date) 
			dataType = DATATYPE_DATE;
		else if (startValue instanceof Integer) 
			dataType = DATATYPE_INTEGER;
		this.startValue = startValue;
	}

	public Object getEndValue() {
		return endValue;
	}

	public void setEndValue(Object endValue) {
		this.endValue = endValue;
	}

	@Override
	public List<Object> getParams() {
		List<Object> values = new ArrayList<Object>();
		values.add(startValue);
		values.add(endValue);
		return values;
	}

}
