package com.tfsla.diario.auditActions;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.A_CmsModuleAction;
import org.opencms.module.CmsModule;
import org.xml.sax.SAXException;

import com.tfsla.diario.admin.widgets.I_TfsWidget;
import com.tfsla.diario.admin.widgets.TfsWidgetManager;
import com.tfsla.diario.imageVariants.TfsImageExportListener;

public class TfsAdminCmsMediosModuleAction extends A_CmsModuleAction {

	private static final Log LOG = CmsLog.getLog(TfsAdminCmsMediosModuleAction.class);
	
	static private CmsObject adminCmsObject=null;
	
	public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module)
	{
		initWidgets();
	    
		OpenCms.addCmsEventListener(new TfsAuditActionsListener());
		TfsAdminCmsMediosModuleAction.adminCmsObject= adminCms;
		

		LOG.info("Agregando evento de exportacion de imagenes en EVENT_BEFORE_PUBLISH_PROJECT.");
		int [] eventBeforePublish = {I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT};
		OpenCms.addCmsEventListener(new TfsImageExportListener(), eventBeforePublish);
		
		try{
 			Messages.get().getBundle(adminCms.getRequestContext().getLocale()).getResourceBundle();
 		}catch(Exception e){
 			// Do not throw anything.
 			LOG.error("Error en obtener mensajes de modulo: ", e);
 		}

	}

	private void initWidgets() {
		Digester digester = new Digester();
		
		digester.setValidating(false);
		digester.push(this);
		digester.addObjectCreate(
				"widgets/widget",
				Exception.class.getName(),
				I_TfsWidget.A_CLASS
	            );
		
	    digester.addSetNext("widgets/widget", "addTfsWidget");

	    InputStream is = null;
	    try {
	    	is = TfsAdminCmsMediosModuleAction.class
            .getClassLoader()
            .getResourceAsStream("com/tfsla/diario/admin/TfsWidget.xml");
	    	
			digester.parse(is
                    );

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void addTfsWidget(I_TfsWidget widget) {
		TfsWidgetManager.addWidget(widget);

    }

	public static CmsObject getAdminCmsObject() {
		return adminCmsObject;
	}

	public static void setAdminCmsObject(CmsObject adminCmsObject) {
		TfsAdminCmsMediosModuleAction.adminCmsObject = adminCmsObject;
	}
}
