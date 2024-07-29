package com.tfsla.opencms.webusers.params;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SimpleParams extends A_PrimaryParam {

	@Override
	public String generateSubClause() {
		return name + " " + operator + " ? " ;
	}

	public void setValue(String value) {
		this.dataType = DATATYPE_STRING;
		this.value = value;
	}

	public void setValue(Date value) {
		this.dataType = DATATYPE_DATE;
		this.value = value;
	}

	public void setValue(int value) {
		this.dataType = DATATYPE_INTEGER;
		this.value = value;
	}

	@Override
	public List<Object> getParams() {
		List<Object> values = new ArrayList<Object>();
		values.add(value);
		return values;
	}
}
