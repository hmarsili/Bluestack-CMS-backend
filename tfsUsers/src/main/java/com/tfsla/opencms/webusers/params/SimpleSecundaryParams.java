package com.tfsla.opencms.webusers.params;

import java.util.ArrayList;
import java.util.List;

public class SimpleSecundaryParams extends A_SecundaryParam {
	
	public SimpleSecundaryParams() {
		tableName="CMS_USERDATA";
	}
	 
	@Override
	protected String getCondition() {
		if (operator.toLowerCase().equals("like") )
			return " AND LCASE(CONVERT(DATA_VALUE USING UTF8)) " + operator + " LCASE(?) " ;
		else
			return " AND DATA_VALUE " + operator + " ? " ;
	}

	public void setValue(Object value) {
		this.value = value.toString();
	}

	@Override
	public List<Object> getParams() {
		List<Object> values = new ArrayList<Object>();
		values.add(value);
		return values;
	}
}
