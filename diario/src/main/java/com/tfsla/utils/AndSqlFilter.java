package com.tfsla.utils;


public class AndSqlFilter extends  AbstractCompositeSqlFilter  {

		public AndSqlFilter() {
			super();
		}
		
		public AndSqlFilter(SQLFilter sql1, SQLFilter sql2) {
			super(sql1, sql2);
		}

		@Override
		public SQLFilter and(SQLFilter filter) {
			if(filter != null) {
				this.getFilters().add(filter);
			}
			return this;
		}

		@Override
		protected String getOperator() {
			return "AND";
		}

}
