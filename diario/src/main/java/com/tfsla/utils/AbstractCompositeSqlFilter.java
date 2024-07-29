package com.tfsla.utils;

import java.util.Iterator;
import java.util.List;

import com.tfsla.collections.CollectionFactory;

public abstract class AbstractCompositeSqlFilter extends AbstractSqlFilter {

	private List<SQLFilter> filters = CollectionFactory.createList();

	public AbstractCompositeSqlFilter() {
		super();
	}
	
	public AbstractCompositeSqlFilter(SQLFilter sql1, SQLFilter sql2) {
		this();
		if(sql1 != null) {
			this.filters.add(sql1);
		}
		if(sql1 != null) {
			this.filters.add(sql2);
		}
	}
	
	protected List<SQLFilter> getFilters() {
		return this.filters;
	}

	@Override
	public String toWhereSql() {
		StringBuffer buffer = new StringBuffer();
		Iterator<SQLFilter> iter =this.getFilters().iterator();
		while(iter.hasNext()) {
			SQLFilter filter = iter.next();
			buffer.append(filter.toWhereSql());
			if(iter.hasNext()) {
				buffer.append(" " + this.getOperator() +" ");
			}
		}
		return buffer.toString();
	}

	protected abstract String getOperator();

	
}
