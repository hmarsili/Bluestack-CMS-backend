package com.tfsla.opencms.webusers.rankings.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.opencms.main.CmsLog;

import com.tfsla.opencms.webusers.openauthorization.data.OpenAuthorizationDAO;
import com.tfsla.opencms.webusers.rankings.*;

public class RankingsDAO extends OpenAuthorizationDAO {
	
	public ArrayList<RankingItem> getSimpleReport(UserDimension dimension, ArrayList<RankingReportFilter> filters) {
		return this.getSimpleReport(dimension, null, filters);
	}
	
	public ArrayList<RankingItem> getSimpleReport(UserDimension dimensionX, UserDimension dimensionY, ArrayList<RankingReportFilter> filters) {
		
		ArrayList<RankingItem> ret = new ArrayList<RankingItem>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			SimpleReportQueryGenerator queryGenerator = new SimpleReportQueryGenerator();
			stmt = queryGenerator.getReportStatement(dimensionX, dimensionY, filters, conn);
			rs = stmt.executeQuery();
			
			if(dimensionY != null) {
				String campo = null;
				RankingItem item = new RankingItem();
				while(rs.next()) {
					if(!rs.getString("CAMPO").equals(campo) || campo == null) {
						item = new RankingItem();
						item.setName(rs.getString("CAMPO"));
						ret.add(item);
						campo = rs.getString("CAMPO");
					}
					
					RankingItem subItem = new RankingItem();
					subItem.setName(rs.getString("SUBCAMPO"));
					subItem.setCount(Integer.parseInt(rs.getString("CANTIDAD")));
					item.getItems().add(subItem);
					item.setCount(item.getCount() + subItem.getCount());
				}
			} else {
				while (rs.next()) {
					RankingItem item = new RankingItem();
					item.setName(rs.getString("CAMPO"));
					item.setCount(Integer.parseInt(rs.getString("CANTIDAD")));
					ret.add(item);
				}
			}
		} catch(Exception e) {
			CmsLog.getLog(this).error(e.getMessage());
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
}
