package com.tfsla.diario.ediciones.services;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

/**
 * Provides services to manage mail settings 
 */
public class MailSettingsService {
	protected static final Log LOG = CmsLog.getLog(MailSettingsService.class);
	
	/**
	 * Retrieves default mail from configuration
	 * @return String with default mail from value
	 */
	public static synchronized String getMailFromDefault() {
		return DEFAULT_MAIL;
	}
	
	/**
	 * Retrieves mail from configuration depending on current CMS context
	 * @param cms CmsObject instance related to current context
	 * @return String with mail from value for current context
	 */
	public static synchronized String getMailFrom(CmsObject cms) {
		String site = cms.getRequestContext().getSiteRoot();
		String publication = "1";
		try {
			publication = String.valueOf(PublicationService.getCurrentPublicationId(cms));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return getMailFrom(site, publication);
	}
	
	/**
	 * Retrieves mail from configuration for a specific site and publication
	 * @param site specific site the configuration will be looked for 
	 * @param publication specific publication the configuration will be looked for
	 * @return String with mail from value for site and configuration
	 */
	public static synchronized String getMailFrom(String site, String publication) {
		try {
			String email = CONFIG.getParam(site, publication, "mailSettings", "mailFrom", DEFAULT_MAIL);
			LOG.info("site" + site + " - Publicacion:" + publication + " - mail: " + email);

			if(email == null || email.equals("")) email = DEFAULT_MAIL;
			return email;
		} catch(Exception e) {
			LOG.error("error al buscar el mail:", e);
			e.printStackTrace();
			return DEFAULT_MAIL;
		}
	}
	
	private static final String DEFAULT_MAIL = OpenCms.getSystemInfo().getMailSettings().getMailFromDefault();
	private static CPMConfig CONFIG = CmsMedios.getInstance().getCmsParaMediosConfiguration();
}