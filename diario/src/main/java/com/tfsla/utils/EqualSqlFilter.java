package com.tfsla.utils;

public class EqualSqlFilter extends AbstractSqlFilter {

	private String sql;

	public EqualSqlFilter(String attributeName, String attributeValue) {
		super();
		this.sql = attributeName + " = '" + attributeValue + "'";
	}

	@Override
	public String toWhereSql() {
		return this.sql;
	}

}
