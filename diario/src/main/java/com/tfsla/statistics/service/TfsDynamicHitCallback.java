package com.tfsla.statistics.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import com.tfsla.rankViews.service.RankService;

public class TfsDynamicHitCallback extends CmsJspActionElement {

	private static final Log LOG = CmsLog.getLog(TfsDynamicHitCallback.class);

	public TfsDynamicHitCallback(PageContext page, HttpServletRequest request, HttpServletResponse response) {
		super(page, request, response);

		String id = request.getParameter("id");
		if (id!=null) {	
			CmsResource res;
			try {
				res = this.getCmsObject().readResource(new CmsUUID(id));
			
				RankService rService = new RankService();
				rService.countHitView(res, this.getCmsObject(), page.getSession());
				
			} catch (NumberFormatException e) {
				LOG.error("Error al intentar registrar la visita al recurso + " + id, e);
			} catch (CmsException e) {
				LOG.error("Error al intentar registrar la visita al recurso + " + id, e);
			};
		}
		else
		{
			LOG.error("Error al intentar registrar la visita al recurso. Falta identificador del recurso");
		}

	}
}
