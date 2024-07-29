package com.tfsla.diario.auditActions.resourceMonitor;

import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.auditActions.TfsAuditActionsListener;
import com.tfsla.diario.auditActions.data.TfsAuditActionDAO;
import com.tfsla.diario.auditActions.model.TfsAuditAction;

public class NewsMonitor extends A_ResourceMonitor {

	public NewsMonitor()
	{
		try {
			resourceType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
		} catch (CmsLoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void auditChanges(CmsObject cms, CmsXmlContent newContent,String resourceName, Locale locale) {
		
        try {
			CmsFile orgFile = cms.readFile(resourceName, CmsResourceFilter.ALL);
			CmsXmlContent orgContent = CmsXmlContentFactory.unmarshal(cms, orgFile);

			String newStateValue = newContent.getValue("estado", locale).getStringValue(cms);
			String orgStateValue = orgContent.getValue("estado", locale).getStringValue(cms);
			
			if (!newStateValue.equals(orgStateValue)) {
				TfsAuditAction action = new TfsAuditAction();

				action.setDescription(newStateValue);
				action.setActionId(TfsAuditAction.ACTION_NEWS_STATUS_CHANGED);

				TfsAuditActionsListener.extractResourceInformation(newContent.getFile(), cms, action);
				
				TfsAuditActionDAO auditDAO = new TfsAuditActionDAO();
				auditDAO.insertUserAuditEvent(action);
			
			}
			
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
