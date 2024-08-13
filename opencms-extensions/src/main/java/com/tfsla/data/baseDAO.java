package com.tfsla.data;


//import java.io.IOException;
//import java.io.InputStream;
import java.sql.*;
import org.opencms.db.CmsDbPool;
//import org.apache.commons.io.IOUtils;
//import org.opencms.configuration.CmsMedios;
//import org.opencms.configuration.CmsMediosInit;

//import org.opencms.loader.I_CmsResourceLoader;
//import org.opencms.loader.TfsJspLoader;
import org.opencms.main.OpenCms;


public abstract class baseDAO {

/*	
	private static String errorMsg = "hTe3GIq5GxbIz1X5LDKv143owhIi873jejaGuU3fdJwN4HWUiShongy00qunm8ZdWAtP3OzcqFM60xxboyTfFZ5Os9i6aFrqYwvagQDhveTt4wWmCRzAS4UxX0V+3pWbxgr7+rnFvFnBXychgbEqqLwT1IsvkylGkiG0WR1cc1U=";
	
	static {
		
		I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(6);
		
		if(!(loader instanceof TfsJspLoader))
			throw new RuntimeException(CmsMediosInit.getInstance().decode(errorMsg));
		
		int sizeConn =0;
		InputStream in = baseDAO.class.getResourceAsStream("/org/opencms/loader/TfsJspLoader.class");

		try {
			byte[] chrs = IOUtils.toByteArray(in);

			sizeConn = chrs.length;


		} catch (IOException e) {
			throw new RuntimeException(CmsMediosInit.getInstance().decode(errorMsg));
		}
		finally
		{
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			in = null;

		}
		if (sizeConn!=6965)
			throw new RuntimeException(CmsMediosInit.getInstance().decode(errorMsg));
	}
*/	

	protected Connection conn;
	private boolean isOpenLocaly;

	protected void OpenConnection() throws Exception {

		if (conn==null)
			conn = OpenCms.getSqlManager().getConnection(CmsDbPool.getDefaultDbPoolName());
		isOpenLocaly = true;

	}

	public void setConnection(Connection conn)
	{
		this.conn = conn;
		isOpenLocaly = false;
	}

	protected void closeConnection() throws Exception {
		if (conn!=null) {
			conn.close();
			conn = null;
		}
	}

	protected boolean connectionIsOpen() {
		return (conn!=null);
	}

	protected boolean connectionIsOpenLocaly() {
		return isOpenLocaly;
	}

}