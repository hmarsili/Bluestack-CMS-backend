package com.tfsla.rankViews.jsp;

import java.rmi.RemoteException;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;


import com.tfsla.rankViews.service.RankService;
import com.tfsla.statistics.SoapConfig;
import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.CachedHitsList;
import com.tfsla.statistics.service.DataCollectorManager;
import com.tfsla.statistics.service.I_statisticsDataCollector;

public class CountHitTag implements Tag  {

	private static final Log LOG = CmsLog.getLog(CountHitTag.class);
	
	private PageContext pageContext;
	
	
	public int doEndTag() throws JspException {

	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    CmsUser user = cms.getRequestContext().currentUser();
	    
	    String url = cms.getRequestContext().getUri();
        
		String sitePath = getSiteName(cms);
		String siteName = sitePath.replaceFirst("/sites/", "");

		CmsResource res=null;
		try {
			res = cms.readResource(url);
		} catch (CmsException e) {
			LOG.error("Atencion! Error al obtener el recurso.",e);
			return 0;
		}

		TfsHitPage page = new TfsHitPage();
		page.setCantidad(1);
		page.setURL(url);
		page.setSitio(siteName);
		page.setTipoContenido("" + res.getTypeId());
		page.setComentarios(0);
		page.setRecomendacion(0);
		page.setValoracion(0);
		
		String sessionId = pageContext.getSession().getId();
		
		page.setAutor(res.getUserCreated().getStringValue());
		
		TfsKeyValue[] values=null;
        DataCollectorManager dataCollectorManager = DataCollectorManager.getInstance();
		for (Iterator<I_statisticsDataCollector> it = dataCollectorManager.getDataCollectors().iterator();it.hasNext();)
		{
			I_statisticsDataCollector dCollector =  it.next(); 
			TfsKeyValue[] valuesCollector=null;
			try {
				valuesCollector = dCollector.collect(cms, res, user, sessionId, page);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			values = (TfsKeyValue[]) ArrayUtils.addAll(values, valuesCollector); 

		}
				
		page.setValues(values);
		try {
			
			if (SoapConfig.getInstance().isUseCachedViewHits())
				CachedHitsList.getInstance().addHit(page);
			else
			{
				RankService rService = new RankService();
				rService.addHit(page);				
			}
		} catch (RemoteException e) {
			LOG.error("Atencion! Error al intentar actualizar las estadisticas.",e);
		}
		        
			
		return 0;
	}

	public int doStartTag() throws JspException {
		return 0;
	}

	public Tag getParent() {
		return null;
	}


	public void setPageContext(PageContext arg0) {
		this.pageContext = arg0;
	}

	public void setParent(Tag arg0) {
		
	}

	private String getSiteName(CmsObject cms)
	{
		CmsSite site = OpenCms.getSiteManager().getCurrentSite(cms);
		String siteName = site.getSiteRoot(); 
		return siteName;
	}

	public void release() {
		
	}



}
