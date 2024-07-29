package com.tfsla.opencmsdev.encuestas;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import com.tfsla.workflow.QueryBuilder;

/**
 * Clase util para tirar queries en otro thread y no bloquear al usuario.
 * 
 * @author jpicasso
 */
public class DelayedSQLThread extends Thread {

	private CmsObject cms;
	private String sql;

	public DelayedSQLThread(CmsObject cms, String sql) {
		this.cms = cms;
		this.sql = sql;
	}

	@Override
	public void run() {
		try {
			new QueryBuilder<String>(this.cms).setSQLQuery(this.sql).execute();
		}
		catch (Exception e) {
			try {
				CmsLog.getLog(this.getClass()).error(e);
			}
			catch (Exception e2) {
				// en el horno
			}
		}

	}
}