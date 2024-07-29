package com.tfsla.opencms.webusers;

import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.opencms.file.CmsObject;

import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;


public class TfsComunityStats {
	

	public class ResultValue {
		private String key;
		private int value;
		private float perc;
		
		public float getPerc() {return perc;}
		public void setPerc(float perc) {this.perc = perc;}
		public String getKey() {return key;}
		public int getValue() {return value;}
		public void setKey(String key) {this.key = key;}
		public void setValue(int value) {this.value = value;}

	}
	
	public List<ResultValue> get(CmsObject cms, String key, int count, int offset) {
		QueryBuilder<List<ResultValue>> queryBuilder = new QueryBuilder<List<ResultValue>>(cms);
		queryBuilder.setSQLQuery("select DATA_VALUE,COUNT(*) from CMS_USERDATA WHERE DATA_KEY=? AND DATA_VALUE<>'' GROUP BY DATA_VALUE ORDER BY COUNT(*) DESC LIMIT ?,?;");

		queryBuilder.addParameter(key);
		queryBuilder.addParameter(offset);
		queryBuilder.addParameter(count);
		
		final int total = getTotalUsers(cms);
		
		ResultSetProcessor<List<ResultValue>> proc = new ResultSetProcessor<List<ResultValue>>() {

			private List<ResultValue> results=new ArrayList<ResultValue>();

			public void processTuple(ResultSet rs) {
				
				try {
					ResultValue result = new ResultValue();
					result.setKey(rs.getString(1));
					result.setValue(rs.getInt(2));
					result.setPerc((float)result.getValue() / (float)total);
					results.add(result);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			public List<ResultValue> getResult() {
				return results;
			}

		};

		return queryBuilder.execute(proc);

	}

	public List<ResultValue> get(CmsObject cms, String key1, String key2, int count, int offset) {
		QueryBuilder<List<ResultValue>> queryBuilder = new QueryBuilder<List<ResultValue>>(cms);
		queryBuilder.setSQLQuery(
				"	SELECT " +
			    "DATA1.DATA_VALUE, " +
			    "DATA2.DATA_VALUE, " +
			    "COUNT(*) " +
			    "FROM CMS_USERDATA DATA1 " +
			    "INNER JOIN CMS_USERDATA DATA2 " +
			    "ON DATA1.USER_ID = DATA2.USER_ID " +
			    "WHERE " +
			    "DATA1.DATA_KEY=? " +
			    "AND DATA1.DATA_VALUE<>'' " +
			    "AND DATA2.DATA_KEY=? " +
			    "AND DATA2.DATA_VALUE<>'' " +
			    
			    "GROUP BY " +
			    "DATA1.DATA_VALUE, " +
			    "DATA2.DATA_VALUE " +
			    "ORDER BY COUNT(*) DESC " +
			    "LIMIT ?,?;");

		queryBuilder.addParameter(key1);
		queryBuilder.addParameter(key2);
		queryBuilder.addParameter(offset);
		queryBuilder.addParameter(count);

		final int total = getTotalUsers(cms);

		ResultSetProcessor<List<ResultValue>> proc = new ResultSetProcessor<List<ResultValue>>() {

			private List<ResultValue> results=new ArrayList<ResultValue>();

			public void processTuple(ResultSet rs) {
				
				try {
					ResultValue result = new ResultValue();
					result.setKey(rs.getString(1) + "||" + rs.getString(2));
					result.setValue(rs.getInt(3));
					result.setPerc((float)result.getValue() / (float)total);

					results.add(result);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			public List<ResultValue> getResult() {
				return results;
			}

		};

		return queryBuilder.execute(proc);

	}
	
	public int getTotalUsers(CmsObject cms) {
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(cms);
		queryBuilder.setSQLQuery(
				"	SELECT " +
			    "COUNT(*) " +
			    "FROM CMS_USERS ");
		
		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private Integer total = 0;

			public void processTuple(ResultSet rs) {
				
				try {
					total = rs.getInt(1);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			public Integer getResult() {
				return total;
			}

		};

		return queryBuilder.execute(proc);

	}



}
