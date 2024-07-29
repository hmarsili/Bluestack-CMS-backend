package com.tfsla.opencms.webusers.params;

import java.util.ArrayList;
import java.util.List;

public class GroupParam extends A_Param {

	private List<Object> values = new ArrayList<Object>();

	public String generateSubClause() {
		String clause = 
				" CMS_USERS.USER_ID IN (" +
				" SELECT USER_ID FROM CMS_GROUPUSERS " + 
				" WHERE GROUP_ID IN ( ? ";
		for (int j=1; j<values.size();j++)
			clause += ", ?";
		clause += ")";

				
		clause += ")";
				
		return clause;
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
