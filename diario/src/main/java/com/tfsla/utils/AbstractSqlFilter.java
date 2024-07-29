package com.tfsla.utils;

/**
 * 
 * @author lgassman
 *
 */
public abstract class AbstractSqlFilter implements SQLFilter {

	private String having;
	
	public AbstractSqlFilter() {
		super();
	}

	public abstract String toWhereSql();
	
	
	public SQLFilter andEqual(String attribute, String value) {
		return this.and(new EqualSqlFilter(attribute, value));
	}
	
	public SQLFilter orEqual(String attribute, String value) {
		return this.or(new EqualSqlFilter(attribute, value));
	}
	
	
	public SQLFilter or(SQLFilter filter) {
		return new OrSqlFilter(this, filter);
	}
	
	public SQLFilter and(SQLFilter filter) {
		return new AndSqlFilter(this, filter);
	}
	
	public SQLFilter addHaving(String having) {
		this.having = having;
		return this;
	}
	
	public String toHavingSql() {
		return this.having;
	}

}
