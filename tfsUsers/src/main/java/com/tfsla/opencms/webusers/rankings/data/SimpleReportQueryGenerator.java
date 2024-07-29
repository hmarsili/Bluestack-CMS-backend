package com.tfsla.opencms.webusers.rankings.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.tfsla.opencms.webusers.rankings.RankingReportFilter;
import com.tfsla.opencms.webusers.rankings.UserDimension;

public class SimpleReportQueryGenerator extends RankingQueryGenerator {
	
	public PreparedStatement getReportStatement(UserDimension dimension, ArrayList<RankingReportFilter> filters, Connection conn) throws SQLException {
		return this.getReportStatement(dimension, null, filters, conn);
	}
	
	public PreparedStatement getReportStatement(UserDimension dimension1, UserDimension dimension2, ArrayList<RankingReportFilter> filters, Connection conn) throws SQLException {
		ArrayList<String> sqlFilters = new ArrayList<String>();
		ArrayList<String> sqlGroups = new ArrayList<String>();
		String sqlFilter = "";
		String select2 = "";
		String alias2 = "";

		tableJoins.put(dimension1.getTable(), 1);
		
		if(dimension2 != null) {
			alias2 = this.getTableAlias(dimension2.getTable());
			select2 = String.format(", %s.DATA_VALUE as SUBCAMPO ", alias2);
		}
		
		String sqlSelect = String.format("select COUNT(*) as CANTIDAD, %s.DATA_VALUE as CAMPO %s from %s ", 
				dimension1.getTable(), select2, dimension1.getTable());
		
		sqlGroups.add("CMS_USERDATA.DATA_VALUE");
		sqlFilters.add(String.format(" CMS_USERDATA.DATA_KEY = '%s' ", dimension1.getEntryName()));
		
		if(dimension2 != null) {
			sqlSelect += String.format(" inner join %s as %s on %s.USER_ID = %s.USER_ID ", 
					dimension2.getTable(),
					alias2,
					alias2,
					dimension1.getTable());
			sqlGroups.add(String.format("%s.DATA_VALUE", alias2));
			sqlFilters.add(String.format(" %s.DATA_KEY = '%s' ", alias2, dimension2.getEntryName()));
		}
		
		for(RankingReportFilter filter : filters) {
			if(this.skipDimension(filter.getDimension())) continue;
			
			String tableName = filter.getDimension().getTable();
			String tableAlias = this.getTableAlias(tableName);
			String operator = operators.keySet().contains(filter.getOperator()) ? operators.get(filter.getOperator()) : "=";
			String entryName = filter.getDimension().getEntryName();
			
			sqlSelect += String.format(" inner join %s as %s ", tableName, tableAlias);
			sqlSelect += String.format(" on %s.USER_ID = %s.USER_ID ", dimension1.getTable(), tableAlias);
			
			if(tableName.equals("CMS_USERDATA")) {
				sqlFilters.add(String.format(" %s.DATA_KEY = '%s' ", tableAlias, entryName));
				sqlFilters.add(String.format(" %s.DATA_VALUE %s ? ", tableAlias, operator, filter.getValue()));
			} else {
				sqlFilters.add(String.format(" %s.%s %s ? ", tableAlias, entryName, operator, filter.getValue()));
			}
		}
		
		if(sqlFilters.size() > 0)
			sqlFilter = " where " + StringUtils.join(sqlFilters.toArray(), " and ");
		
		String sqlGroupBy = " group by " + StringUtils.join(sqlGroups.toArray(), ", ");
		String sqlOrderBy = " order by CANTIDAD desc ";
		
		if(dimension2 != null) {
			sqlOrderBy = " order by CAMPO asc, CANTIDAD desc ";
		}
		
		PreparedStatement stmt = conn.prepareStatement(sqlSelect + sqlFilter + sqlGroupBy + sqlOrderBy);
		
		int i = 1;
		for(RankingReportFilter filter : filters) {
			if(this.skipDimension(filter.getDimension())) continue;
			stmt.setString(i, filter.getValue());
		}
		return stmt;
	}
	
}
