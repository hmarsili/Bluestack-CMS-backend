package com.tfsla.opencms.webusers.params;

import java.util.Date;


public abstract class A_SecundaryParam extends A_SimpleParam {
	
	protected String tableAlias;
	protected String tableName;
	protected abstract String getCondition();

	protected String getTableAlias() {
		Date now = new Date();
		return tableAlias + now.getTime();
	}
	
	public String generateSubClause() {
		String clause = 
				" CMS_USERS.USER_ID IN (" +
				" SELECT USER_ID FROM " + tableName + " " + this.getTableAlias() + 
				" WHERE DATA_KEY = '" + name + "'" +
				getCondition() +
				")";
				
		return clause;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
