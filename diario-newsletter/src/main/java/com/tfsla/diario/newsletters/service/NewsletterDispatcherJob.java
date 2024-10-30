package com.tfsla.diario.newsletters.service;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.diario.newsletters.common.INewsletterDispatcherService;
import com.tfsla.diario.newsletters.common.INewslettersService;
import com.tfsla.diario.newsletters.common.Newsletter;
import com.tfsla.diario.newsletters.common.NewsletterConfiguration;
import com.tfsla.diario.newsletters.common.NewsletterDispatch;

public class NewsletterDispatcherJob implements I_CmsScheduledJob {

	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		try {
			String publication = parameters.get("publication").toString();
			int newsletterID = Integer.valueOf(parameters.get("newsletterID").toString());
			String site = cms.getRequestContext().getSiteRoot();
			NewsletterConfiguration config = NewsletterConfigurationService.getConfig(site, publication);
			INewslettersService newsletterService = NewsletterServiceContainer.getInstance(INewslettersService.class);
			Newsletter newsletter = newsletterService.getNewsletter(newsletterID);
			INewsletterDispatcherService svc = NewsletterServiceContainer.getInstance(INewsletterDispatcherService.class);
			int emailsSent = 0;
			try {
				emailsSent = svc.dispatchNewsletter(newsletterID, config, cms);
			} catch (Throwable e) {
				e.printStackTrace();
				throw new Exception(e);
			}
			
			NewsletterDispatch dispatch = new NewsletterDispatch();
			dispatch.setSent(emailsSent);
			dispatch.setNewsletter(newsletter);
			svc.addDispatch(dispatch);
			
			String result = emailsSent + " emails sent for newsletter " + newsletter.getName();
			LOG.debug(result);
			return result;
		} catch(Exception e) {
			LOG.error(e);
			return e.getMessage();
		}
	}

	private Log LOG = CmsLog.getLog(this);
}
