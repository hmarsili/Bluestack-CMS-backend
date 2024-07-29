package com.tfsla.opencms.webusers.rankings.data;

import java.util.Hashtable;

import com.tfsla.opencms.webusers.rankings.UserDimension;

public abstract class RankingQueryGenerator {
	
	protected Hashtable<String, Integer> tableJoins = new Hashtable<String, Integer>();
	protected static final Hashtable<String, String> operators = new Hashtable<String, String>() {{
		put("eq", " = ");
		put("neq", " <> ");
		put("gt", " > ");
		put("lt", " < ");
		put("like", " like ");
	}};
	
	protected Boolean skipDimension(UserDimension dimension) {
		return dimension.getTable() == null || 
				dimension.getTable().equals("") ||
				dimension.getName() == null ||
				dimension.getName().equals("");
	}
	
	protected String getTableAlias(String table) {
		Integer q = 0;
		
		if(tableJoins.keySet().contains(table)) {
			q = tableJoins.get(table);
			q++;
			tableJoins.remove(table);
		}
		tableJoins.put(table, q);
		
		return table + q.toString();
	}
}
