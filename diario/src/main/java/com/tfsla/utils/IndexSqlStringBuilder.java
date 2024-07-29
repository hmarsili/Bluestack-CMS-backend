package com.tfsla.utils;

import com.tfsla.opencmsdev.module.TfsConstants;


/**
 * Construye un SQLString para buscar noticias por palabras clave
 * @author lgassman
 *
 */
public class IndexSqlStringBuilder {

	private String head;
	private SQLFilter filter = new AndSqlFilter();
	private String groupBy;
	private String orderBy = "";
	private Integer ubication ;
	
	public IndexSqlStringBuilder() {
		super();
		// el count es para ordenar por cantidad de palabras que matchean
		this.head = "SELECT RESOURCE, COUNT(*) as CANT FROM TFS_INDEX ";
		this.groupBy = " GROUP BY RESOURCE, WORD ";
		this.setOrderBy("CANT");
	}

	public String toSql() {
		return this.head + this.getWhere() + this.groupBy + this.getHaving() + this.orderBy; 
	}

	private String getHaving() {
		String string = this.filter.toHavingSql();
		return (string != null) ? " HAVING " + string + " ": "";
	}

	private String getWhere() {
		String filter = this.filter.toWhereSql();
		if(!"".equals(filter) && this.ubication  != null ) {
			return "WHERE (" + filter + ") AND ( UBICATION = " + this.ubication + " )";
		}
		else if(!"".equals(filter)) {
			return "WHERE " + filter;
		}
		return "";
	}

	public IndexSqlStringBuilder setOrderBy(String attribute) {
		this.orderBy = "ORDER BY " + attribute;
		return this;
	}
	
	public IndexSqlStringBuilder setSqlFilter(SQLFilter filter) {
		this.filter = filter;
		return this;
	}

	public IndexSqlStringBuilder setUbication(String ubication) {
		if(TfsConstants.TITULO_UBICATION.equals(ubication)) {
			this.ubication = TfsConstants.TITULO_UBICATION_KEY;
		}
		else if(TfsConstants.KEYWORD_UBICATION.equals(ubication)) {
			this.ubication = TfsConstants.KEYWORD_UBICATION_KEY;
		}
		return this;
	}
}