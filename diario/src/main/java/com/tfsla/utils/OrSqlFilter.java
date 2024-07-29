package com.tfsla.utils;




public class OrSqlFilter extends AbstractCompositeSqlFilter {

	public OrSqlFilter() {
		super();
	}
	
	public OrSqlFilter(SQLFilter sql1, SQLFilter sql2) {
		super(sql1, sql2);
	}

	@Override
	public SQLFilter or(SQLFilter filter) {
		if(filter != null) {
			this.getFilters().add(filter);
		}
		return this;
	}

	@Override
	protected String getOperator() {
		return "OR";
	}

}
