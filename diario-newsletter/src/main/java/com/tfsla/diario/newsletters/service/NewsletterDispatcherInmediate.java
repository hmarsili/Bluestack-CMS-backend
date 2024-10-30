package com.tfsla.diario.newsletters.service;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import com.tfsla.diario.newsletters.common.INewsletterDispatcherService;
import com.tfsla.diario.newsletters.common.INewslettersService;
import com.tfsla.diario.newsletters.common.Newsletter;
import com.tfsla.diario.newsletters.common.NewsletterConfiguration;
import com.tfsla.diario.newsletters.common.NewsletterDispatch;

public class NewsletterDispatcherInmediate {

	private static Log LOG = CmsLog.getLog(NewsletterDispatcherInmediate.class);
	
	public static String sendNewsletter (String publication, Integer newsletterID, CmsObject cms) {
		try {
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
			
			String result = "Newsletter inmmidiate: "+ emailsSent + " emails sent for newsletter " + newsletter.getName() + " by user: " + cms.getRequestContext().currentUser().getFullName();
			LOG.info(result);
			return result;
		} catch(Exception e) {
			LOG.error("Error en el envio inmediato de newsletter para newsletter ID: "+newsletterID ,e);
			return "ERROR: " + e.getMessage();
		}
	}
}
