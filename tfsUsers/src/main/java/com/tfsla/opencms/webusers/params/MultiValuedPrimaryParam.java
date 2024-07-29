package com.tfsla.opencms.webusers.params;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MultiValuedPrimaryParam extends A_PrimaryParam {

	private List<Object> values = new ArrayList<Object>();
	
	@Override
	public String generateSubClause() {
		String condition = name + " IN ( ? ";
		for (int j=1; j<values.size();j++)
		{
			condition += ", ?";
		}
		condition += ")";
		return condition;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}
	
	public void addValue(Object value) {
		if (value instanceof String) 
			dataType = DATATYPE_STRING;
		else if (value instanceof Date) 
			dataType = DATATYPE_DATE;
		else if (value instanceof Integer) 
			dataType = DATATYPE_INTEGER;
		values.add(value);
	}

	@Override
	public List<Object> getParams() {
		return values;
	}

}
