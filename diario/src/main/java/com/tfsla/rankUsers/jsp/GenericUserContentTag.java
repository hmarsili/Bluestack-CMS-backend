package com.tfsla.rankUsers.jsp;

import java.util.Iterator;

import org.opencms.jsp.AbstractOpenCmsTag;
import org.opencms.main.CmsException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;

import org.opencms.flex.CmsFlexController;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;

import com.tfsla.rankUsers.service.RankService;
import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.DataCollectorManager;
import com.tfsla.statistics.service.I_statisticsDataCollector;

public class GenericUserContentTag  extends AbstractOpenCmsTag {
	
	private static final Log LOG = CmsLog.getLog(GenericUserContentTag.class);

	private String resourceName;
	private String counter;
	private String value="1";


	private CmsObject cms;
	
	private PageContext pageContext;

	public GenericUserContentTag()
	{
		super();
	}
	
	@Override
	public int doStartTag() throws JspException {
		
    	cms = CmsFlexController.getCmsObject(pageContext.getRequest());

		RankService rService = new RankService();
	
		CmsResource res=null;
		try {
			res = cms.readResource(getResourceName());
		} catch (CmsException e) {
			LOG.error("Atencion! Error al obtener el recurso.",e);
		}

		CmsUser user=getUser(res);

		if (user!=null)
			rService.addUserHit(user, cms, Integer.parseInt(counter), Integer.parseInt(value));
		
		return SKIP_BODY;
	}
	
	private CmsUser getUser(CmsResource res)
	{

		CmsUser user = cms.getRequestContext().currentUser();
		

		HttpSession session = pageContext.getSession();
		String sessionId = session.getId();

		TfsHitPage page = new TfsHitPage();

		page.setAutor(res.getUserCreated().getStringValue());
		
		TfsKeyValue[] values=null;
        DataCollectorManager dataCollectorManager = DataCollectorManager.getInstance();
		for (Iterator it = dataCollectorManager.getDataCollectors().iterator();it.hasNext();)
		{
			I_statisticsDataCollector dCollector = (I_statisticsDataCollector) it.next(); 
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

		if (page.getAutor()==null)
			return null;
		
		CmsUser autor=null;
		try {
			autor = cms.readUser(new CmsUUID(page.getAutor()));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return autor;
		
	}
	private String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCounter() {
		return counter;
	}

	public void setCounter(String counter) {
		this.counter = counter;
	}


	public void setPageContext(PageContext arg0) {
		super.setPageContext(arg0);
		this.pageContext = arg0;

	}

}
