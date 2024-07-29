package com.tfsla.opencms.webusers.params;

import java.util.ArrayList;
import java.util.List;

public class RangeSecundaryParams extends A_SecundaryParam {

	private String startValue;
	private String endValue;
	
	public RangeSecundaryParams()
	{
		tableName="CMS_USERDATA";
	}
	
	@Override
	protected String getCondition() {
		return " AND DATA_VALUE >= ? AND DATA_VALUE <= ?" ;
	}

	public String getStartValue() {
		return startValue;
	}

	public void setStartValue(String startValue) {
		this.startValue = startValue;
	}

	public String getEndValue() {
		return endValue;
	}

	public void setEndValue(String endValue) {
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
