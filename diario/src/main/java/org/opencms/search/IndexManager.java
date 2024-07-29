package org.opencms.search;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.TfsContext;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.exceptions.ApplicationException;

public class IndexManager {

	private static Map<String, IndexManager> instances = new HashMap<String, IndexManager>();

	private String indexName;

	private IndexManager(String indexName) {
		this.indexName = indexName;
	}

	@SuppressWarnings("unused")
	public static synchronized void init(CmsObject cms, String indexName) {
		if (!instances.containsKey(indexName)) {
			IndexManager instance = new IndexManager(indexName);
			instances.put(indexName, instance);
		}
		else {
			throw new ApplicationException("Ya existe un manager para este indice: " + indexName);
		}
	}

	public static IndexManager getInstance(String indexName) {
		return instances.get(indexName);
	}

	public void indexResource(CmsResource resource) {
		if (resource != null) {
			List<CmsResource> list = new ArrayList<CmsResource>();
			list.add(resource);
			this.indexResourceList(list);
		}
		else {
			CmsLog.getLog(this).warn("Se quiso indexar un resource nulo", new Exception("Se quiso indexar un resource nulo"));
		}
	}

	public void indexResourceList(List<CmsResource> resources) {
		List<CmsPublishedResource> list = CollectionFactory.createList();
		for (CmsResource resource : resources) {
			list.add(new CmsPublishedResource(resource));
		}
		this.index(list);
	}

	public void index(List<CmsPublishedResource> resources) {
		new IndexThread(TfsContext.getInstance().getCmsObject(), this.indexName, resources).start();
	}

	private class IndexThread extends A_CmsReportThread {

		private List<CmsPublishedResource> list;
		private String indexName;

		protected IndexThread(CmsObject cms, String indexName, List<CmsPublishedResource> list) {
			super(cms, "IndexThread for " + indexName);
			this.list = list;
			this.indexName = indexName;

		}

		public String getReportUpdate() {
			return "";
		}

		@SuppressWarnings("static-access")
		@Override
		public void run() {
			CmsSearchManager searchManager = OpenCms.getSearchManager();
			CmsSearchIndex index = searchManager.getIndex(this.indexName);
			I_CmsReport report = new CmsLogReport(OpenCms.getLocaleManager().getDefaultLocale(), this
					.getClass());
			
			if(list != null  && !list.isEmpty()) {
				try {
					OpenCms.getSearchManager().updateIndex(index, report, list);
				}
				catch (CmsException e) {
					throw new ApplicationException("No se pudo regenear el indice online", e);
				}
			}
			else {
				report.addWarning("Se quiso indexar una lista de resources vacia" );
				CmsLog.getLog(this).warn("Se quiso indexar una lista de resources vacia");
			}
		}

	}

}
