package com.tfsla.opencms.webusers.params;

import java.util.List;

public abstract class A_Param {

	public static final String OPERATOR_EQUALS = "=";
	public static final String OPERATOR_LIKE = "LIKE";
	public static final String OPERATOR_GREATERTHAN = ">";
	public static final String OPERATOR_LOWERTHAN = "<";
	public static final String OPERATOR_GREATEROREQUALSTHAN = ">=";
	public static final String OPERATOR_LOWEROREQUALSTHAN = "<=";
	public static final String OPERATOR_DISTINT = "<>";
	public static final String OPERATOR_NOTLIKE = "NOT LIKE";

		
	protected String name;
	protected String operator = OPERATOR_EQUALS;

	public A_Param() {
		super();
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public abstract String generateSubClause();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public abstract List<Object> getParams();

}