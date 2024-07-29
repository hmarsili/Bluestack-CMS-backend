package com.tfsla.opencms.webusers.params;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class FilterParam extends A_SimpleParam {
	
	public FilterParam() {
		this.params = new ArrayList<A_Param>();
	}
	
	private ArrayList<A_Param> params;
	
	public void addFilter(A_Param filter) {
		params.add(filter);
	}
	
	@Override
	public String generateSubClause() {
		ArrayList<String> stmnts = new ArrayList<String>();
		for(A_Param filter : this.params) {
			stmnts.add(filter.generateSubClause());
		}
		return "( " + StringUtils.join(stmnts.toArray(), " OR ") + " )";
	}

	@Override
	public List<Object> getParams() {
		List<Object> values = new ArrayList<Object>();
		for(A_Param param : this.params) {
			for(Object value : param.getParams()) {
				values.add(value);
			}
		}
		return values;
	}

}
