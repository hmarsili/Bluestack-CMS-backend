package com.tfsla.diario.webservices.core.services;

import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.webservices.IPushService;
import com.tfsla.diario.webservices.PushNotificationServices.PushServiceConfiguration;
import com.tfsla.diario.webservices.common.PushItemResult;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.data.PushClientDAO;
import com.tfsla.diario.webservices.helpers.PublicationHelper;
import com.tfsla.diario.webservices.helpers.PushConfigurationHelper;

public abstract class PushNotificationService implements IPushService {
	
	public PushNotificationService() {
		this(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
	}
	
	public PushNotificationService(String site) {
		this(site, PublicationHelper.getCurrentPublication(site));
	}
	
	public PushNotificationService(String site, String publication) {
		this.config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		this.site = site;
		this.publication = publication;
		this.module = PushServiceConfiguration.getModuleName();
		this.LOG = CmsLog.getLog(this);
	}
	
	public abstract void stopMessageService();
	
	public abstract void startMessageService();

	public void push(Hashtable<String, Hashtable<String, String>> messages) {
		this.pushMessages(messages, this.site, this.publication);
	}
	
	public void push(Hashtable<String, Hashtable<String, String>> messages, String site, String publication) {
		this.pushMessages(messages, site, publication);
	}
	
	/**
	 * Broadcasts a messages to a platform Push Notification Service
	 * by using the provider API
	 * @param messages the messages to be sent to the Push service
	 */
	public Hashtable<String, PushItemResult> pushMessages(Hashtable<String, Hashtable<String, String>> messages, String site, String publication) {
		PushClientDAO dao = new PushClientDAO();
		Hashtable<String, PushItemResult> ret = new Hashtable<String, PushItemResult>();
		Exception noClientsException = null;
		
		try {
			//Get the clients to push the notifications to
			dao.openConnection();
			List<String> clients = dao.getClients(this.getPlatform(), site, publication, !PushConfigurationHelper.isSiteManaged(site, publication));
			if(clients == null || clients.size() == 0) {
				//This exception will be thrown if no clients are registered
				noClientsException = new Exception(String.format(ExceptionMessages.NO_CLIENTS_FOR_PLATFORM, this.getPlatform()));
			}
			
			//Loop through all the messages to be delivered
			for(String key : messages.keySet()) {
				try {
					if(noClientsException != null) {
						throw noClientsException;
					}
					
					//Try to send the message to all the clients registered
					this.sendMessage(clients, messages.get(key));
					
					//Message sent Ok, it will be notified to the job
					ret.put(key, PushItemResult.OK);
				} catch(Exception ex) {
					LOG.error(String.format(ExceptionMessages.ERROR_PUSHING_ITEM, key, this.getPlatform()), ex);
					ret.put(key, PushItemResult.FAIL);
				}
			}
			
		} catch(Exception ex) {
			LOG.error(String.format(ExceptionMessages.ERROR_CONNECTING_PUSH_SERVICE, this.getPlatform()), ex);
		} finally {
			try {
				dao.closeConnection();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	protected abstract String getPlatform();
	
	protected abstract void sendMessage(List<String> clients, Hashtable<String, String> message) throws Exception;
	
	protected abstract void sendMessage(String client, Hashtable<String, String> message) throws Exception;
	
	protected CPMConfig config;
	protected String site;
	protected String publication;
	protected String module;
	protected Log LOG;
	
	public void setSite(String site) {
		this.site = site;
	}
	
	public void setPublication(String publication) {
		this.publication = publication;
	}
}
