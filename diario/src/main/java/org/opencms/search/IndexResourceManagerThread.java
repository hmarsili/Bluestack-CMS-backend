package org.opencms.search;


import java.util.List;

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchManager;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.exceptions.ApplicationException;

/**
 * @deprecated use {@link IndexManager}
 * @author lgassman
 *
 */
public class IndexResourceManagerThread extends A_CmsReportThread {

	
	private CmsResource resource;
	private String indexName;

	public IndexResourceManagerThread(CmsObject cms, CmsResource resource, String indexName) {
		super(cms, indexName + " thread for " + resource);
		this.resource = resource;
		this.indexName = indexName;
	}

	@Override
	public String getReportUpdate() {
		return "";
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		CmsSearchManager searchManager = OpenCms.getSearchManager();
		CmsSearchIndex index = searchManager.getIndex(this.indexName);
		List<CmsPublishedResource> list = CollectionFactory.createList();
		list.add(new CmsPublishedResource(this.resource));
		
		I_CmsReport report = new CmsLogReport(OpenCms.getLocaleManager().getDefaultLocale(), this.getClass());
		try {
			OpenCms.getSearchManager().updateIndex(index, report, list);
		}
		catch (CmsException e) {
			throw new ApplicationException("No se pudo regenear el indice online", e);
		}
	}

}
