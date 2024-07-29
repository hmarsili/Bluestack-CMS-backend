package com.tfsla.opencms.webusers.params;

import java.util.ArrayList;
import java.util.List;

public class MultiValuedSecundaryParam extends A_SecundaryParam {

	public MultiValuedSecundaryParam()
	{
		tableName="CMS_USERDATA";
	}
	
	private List<Object> values = new ArrayList<Object>();
	
	@Override
	protected String getCondition() {
		String condition = " AND DATA_VALUE IN ( ? ";
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
	
	public void addValue(String value) {
		values.add(value);
	}

	@Override
	public List<Object> getParams() {
		return values;
	}

}
