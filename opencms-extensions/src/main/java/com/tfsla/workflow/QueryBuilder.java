package com.tfsla.workflow;

//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Random;
//import org.apache.commons.io.IOUtils;
//import org.opencms.configuration.CmsMediosInit;
//import org.opencms.loader.I_CmsResourceLoader;
//import org.opencms.loader.TfsJspLoader;
//import org.opencms.main.OpenCms;
//import com.tfsla.data.baseDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.opencms.db.CmsDbContext;
import org.opencms.db.SecurityManagerAccesor;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsObject;
import com.tfsla.exceptions.ApplicationException;
import com.tfsla.utils.TFSDriversContainer;

/**
 * Arma un query.
 * Va usando recursos de la base de datos, a medida que se va armando la query
 * (por ejemplo, pide la connection ni bien se construye)
 * Si se llega a necesitar que ese pedido sea lazy, entonces hay que escribir
 * una subclase.
 *
 *
 * @author lgassman
 * @param <T>
 */
public class QueryBuilder<T> {

	private CmsSqlManager sqlManager;
	private CmsDbContext dbContext;

	private ResultSet res = null;
    private PreparedStatement stmt = null;
    private Connection conn = null;
	private int nextColumn = 1;

	private String query;
	private List<Object> parameters = new ArrayList<Object>();
	private boolean open = true;

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

	public QueryBuilder(CmsSqlManager sqlManager, CmsDbContext dbc) {
		
		this.sqlManager = sqlManager;
		this.dbContext = dbc;
		try {
			this.conn = this.sqlManager.getConnection(this.dbContext);
		} catch (SQLException e) {
			throw new ApplicationException("No se pudo conseguir la connection para " + dbc, e);
		}
	}


	public QueryBuilder(CmsObject cms) {
		this(TFSDriversContainer.getInstance().getSqlManager(), SecurityManagerAccesor.getCmsDbContext(cms));
/*		
		if (cms.getRequestContext().currentProject().isOnlineProject() &&  !OpenCms.getSiteManager().getCurrentSite(cms).getUrl().equals("/") && !cms.getRequestContext().getUri().startsWith("/system/")) {
	
			boolean restrictive = CmsMediosInit.getInstance().restrictiveMode(cms);
			long views = CmsMediosInit.getInstance().getViews(cms);
			long maxViews = CmsMediosInit.getInstance().getPermViews(cms);
			
			Random rnd = new Random();
			int rNro = rnd.nextInt(10000);
			int license = rNro>=1000 ? 0 : CmsMediosInit.getInstance().checkLicense(cms);
			
			if (license!=0 || restrictive && maxViews<views || views==-1L) {
				//Log LOG = CmsLog.getLog(QueryBuilder.class);
				//LOG.error("license: " + license + " - restrictive: " + restrictive + " - views: " + views);
				throw new RuntimeException(CmsMediosInit.getInstance().decode(errorMsg));
			}
	   	}
*/
	}


	public QueryBuilder<T> setQueryKeyWithoutProject(String queryKey) {
		this.query = queryKey;
		try {
			this.stmt = this.sqlManager.getPreparedStatement(this.conn, this.query);
		} catch (SQLException e) {
			this.close();
			throw new ApplicationException("No se pudo armar el preparedStament para " + queryKey, e);
		}
		return this;
	}

	public QueryBuilder<T> setQueryKeyInThisProject(String queryKey) {
		this.query = queryKey;
		try {
			this.stmt = this.sqlManager.getPreparedStatement(this.conn, this.dbContext.currentProject(), this.query);
		} catch (SQLException e) {
			this.close();
			throw new ApplicationException("No se pudo armar el preparedStament para " + queryKey, e);
		}
		return this;
	}

	public QueryBuilder<T> setSQLQuery(String sql) {
		this.query = sql;
		try {
			this.stmt = this.sqlManager.getPreparedStatementForSql(this.conn, sql);
		} catch (SQLException e) {
			this.close();
			throw new ApplicationException("No se pudo armar el preparedStament para " + sql, e);
		}
		return this;
	}

	public QueryBuilder<T> addParameter(Date value) {
		try {
			this.parameters.add(value);
			if (value!=null)
				this.stmt.setTimestamp(this.nextColumn++, new Timestamp(value.getTime()));
			else
				this.stmt.setTimestamp(this.nextColumn++, null);
			return this;
		} catch (SQLException e) {
			this.close();
			throw new ApplicationException("No se pudo setear el valor " + value + " en la pos " + (this.nextColumn - 1)  + " en " + this.stmt , e);
		}
	}


	public QueryBuilder<T> addParameter(int value) {
		try {
			this.parameters.add(value);
			this.stmt.setInt(this.nextColumn++, value);
			return this;
		} catch (SQLException e) {
			this.close();
			throw new ApplicationException("No se pudo setear el valor " + value + " en la pos " + (this.nextColumn - 1)  + " en " + this.stmt , e);
		}
	}

	public QueryBuilder<T> addParameter(long value) {
		try {
			this.parameters.add(value);
			this.stmt.setLong(this.nextColumn++, value);
			return this;
		} catch (SQLException e) {
			this.close();
			throw new ApplicationException("No se pudo setear el valor " + value + " en la pos " + (this.nextColumn - 1)  + " en " + this.stmt , e);
		}
	}

	public QueryBuilder<T> addParameter(String value) {
		try {
			this.parameters.add(value);
			this.stmt.setString(this.nextColumn++, value);
			return this;
		} catch (SQLException e) {
			this.close();
			throw new ApplicationException("No se pudo setear el valor " + value + " en la pos " + (this.nextColumn - 1)  + " en " + this.stmt , e);
		}
	}

	public boolean execute() {
        try {
        	this.logExecuting();
            return this.stmt.execute();
        }
        catch (Exception exc) {
        	throw new ApplicationException("Error ejecutando la query " + this, exc);
        } finally {
        	this.close();
        }
	}



	public T execute(ResultSetProcessor<T> processor) {
        try {
        	this.logExecuting();
        	this.res = this.stmt.executeQuery();

            while (this.res.next()) {
                processor.processTuple(this.res);
            }
            return processor.getResult();
        }
        catch (Exception exc) {
        	throw new ApplicationException("Error ejecutando la query " + this, exc);
        } finally {
        	this.close();
        }
	}

	private synchronized void close() {
		if(this.open ) {
			this.sqlManager.closeAll(this.dbContext, this.conn, this.stmt, this.res);
			this.open = false;
		}
	}

	@Override
	public String toString() {
		return this.query + this.parameters;
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
	}


	private void logExecuting() {
//		System.out.println("Executing query: " + this);
	}



	public QueryBuilder<T> addParameter(Object value) {
		if (value instanceof String) 
			addParameter((String) value);
		else if (value instanceof Date) 
			addParameter((Date) value);
		else if (value instanceof Integer) 
			addParameter(((Integer) value).intValue());
		else if (value instanceof Long) 
			addParameter(((Long) value).longValue());
		
		return this;
		
	}
}
