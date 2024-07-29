package com.tfsla.data;

import java.sql.*;
import org.opencms.db.CmsDbPool;
import org.opencms.main.OpenCms;

public class TransactionConnection {
	
	public static String tranConn = "BgQCfg6FTaEyJ8t6G";

	private Connection conn;
	
	public Connection createConnection() throws Exception {
		if (conn==null)
			conn = OpenCms.getSqlManager().getConnection(CmsDbPool.getDefaultDbPoolName());
		
		conn.setAutoCommit(false);
		return conn;
	}
	
	public void closeConnection(boolean commit) throws Exception  {
		if (conn!=null) {
			if (commit)
				conn.commit();
			else
				conn.rollback();
			
			conn.close();
			conn = null;
		}	
	}
	
}
