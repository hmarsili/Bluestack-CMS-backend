package com.tfsla.webusersposts.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Locale;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.services.MailSettingsService;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.opencms.mail.MailSender;
import com.tfsla.opencms.mail.SimpleMail;
import com.tfsla.opencms.webusers.RegistrationModule;
import com.tfsla.webusersnewspublisher.helper.expirenews.Strings;
import com.tfsla.webusersposts.common.PostsNotificationType;
import com.tfsla.webusersposts.common.UserPost;

public class PostsMailingService {
	
	public synchronized static void sendMail(CmsUser cmsUser, CmsResource resource, CmsObject cms, String site, String publication, PostsNotificationType notificationType) throws Exception {
		String subject = getSubject(site, publication, notificationType);
		String mailModel = getMailModel(site, publication, notificationType);
		String messageContent = getFileContents(mailModel);
		String title = "";
		CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
		Locale locale = cms.getRequestContext().getLocale();
		I_CmsXmlContentValue value = xmlContent.getValue("titulo[1]", locale);
		if(value != null) {
			title = value.getStringValue(cms);
		} else {
			title = resource.getName();
		}
		
		SimpleMail mail = new SimpleMail();
		mail.addTo(cmsUser.getEmail());
		mail.setSubject(subject);
		//mail.setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
		mail.setFrom(MailSettingsService.getMailFrom(cms));
		mail.setValue("userName", cmsUser.getName().replace("webUser/", ""));
		mail.setValue("postTitle", title);
		//mail.setValue("userId", cmsUser.getId());
		//mail.setValue("mail", cmsUser.getEmail());
		//mail.setValue("nickname", cmsUser.getAdditionalInfo("APODO"));
		if(resource != null)
			mail.setValue("postPath", resource.getRootPath().replace(site, ""));
		mail.setHtmlContents(messageContent);
		
		MailSender.getInstance().sendMail(cmsUser, mail);
	}
	
	public synchronized static void sendNotification(UserPost post, CmsObject cms, String site, String publication, PostsNotificationType notificationType) throws Exception {
		sendNotification(post, post.getCmsResource(), cms, site, publication, notificationType);
	}
	
	public synchronized static void requestDelete(String[] recipients, String resourcePath, CmsObject cms, String site, String publication, String userName) throws Exception {
		String subject = config.getParam(site, publication, Strings.NEWSPUBLISHER_MODULE, Strings.DELETE_POSTS_NOTIFICATIONS_SUBJECT);
		String mailModel = config.getParam(site, publication, Strings.NEWSPUBLISHER_MODULE, Strings.DELETE_POSTS_NOTIFICATIONS_MODEL);
		Locale locale = cms.getRequestContext().getLocale();
		String messageContent = getFileContents(mailModel);
		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resourcePath));
		SimpleMail mail = new SimpleMail();
		for(String to : recipients) {
			mail.addTo(to);
		}
		mail.setSubject(subject);
		//mail.setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
		mail.setFrom(MailSettingsService.getMailFrom(cms));
		mail.setValue("userName", userName);
		mail.setValue("postTitle", content.getStringValue(cms, "titulo", locale));
		mail.setValue("url", resourcePath.replace(site, ""));
		mail.setHtmlContents(messageContent);
		
		MailSender.getInstance().sendMail(mail, "", "");
	}
	
	public synchronized static void sendNotification(UserPost post, CmsResource publishedItem, CmsObject cms, String site, String publication, PostsNotificationType notificationType) throws Exception {
		String subject = getSubject(site, publication, notificationType);
		String mailModel = getMailModel(site, publication, notificationType);
		
		//CmsUser cmsUser = post.getCmsUser(cms);
		CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, cms.readFile(publishedItem));
		CmsUser cmsUser = cms.readUser(xmlContent.getStringValue(cms, "autor/internalUser", cms.getRequestContext().getLocale()));
		String messageContent = getFileContents(mailModel);
		
		SimpleMail mail = new SimpleMail();
		mail.addTo(cmsUser.getEmail());
		mail.setSubject(subject);
		//mail.setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
		mail.setFrom(MailSettingsService.getMailFrom(cms));
		mail.setValue("userName", cmsUser.getName().replace("webUser/", ""));
		mail.setValue("postTitle", post.getTitle());
		//mail.setValue("userId", cmsUser.getId());
		//mail.setValue("mail", cmsUser.getEmail());
		//mail.setValue("nickname", cmsUser.getAdditionalInfo("APODO"));
		if(publishedItem != null)
			mail.setValue("postPath", publishedItem.getRootPath().replace(site, ""));
		mail.setHtmlContents(messageContent);
		
		MailSender.getInstance().sendMail(cmsUser, mail);
	}
	
	private static String getMailModel(String site, String publication, PostsNotificationType type) {
		if(type == PostsNotificationType.APPROVED)
			return config.getParam(site,publication, "newsPublisher", "approvedMailModel","postApprovedMailModel.html");
		if(type == PostsNotificationType.PENDING)
			return config.getParam(site,publication, "newsPublisher", "pendingMailModel","postPendingMailModel.html");
		return config.getParam(site,publication, "newsPublisher", "rejectedMailModel","postRejectedMailModel.html");
	}

	private static String getSubject(String site, String publication, PostsNotificationType type) {
		if(type == PostsNotificationType.APPROVED)
			return config.getParam(site,publication,"newsPublisher", "approvedMailSubject","");
		if(type == PostsNotificationType.PENDING)
			return config.getParam(site,publication,"newsPublisher", "pendingMailSubject","");
		return config.getParam(site,publication,"newsPublisher", "rejectedMailSubject","");
	}

	private static String getFileContents(String fileName) {
		if(!fileContents.containsKey(fileName)) {
			fileContents.put(fileName, readFileContents(fileName));
		}
		return fileContents.get(fileName);
	}
	
	private synchronized static String readFileContents(String fileName) {
		try {
			InputStream mailTemplate = RegistrationModule.class.getResource(fileName).openStream();
			ProgramException.assertTrue("file " + fileName + " not found", mailTemplate != null);

			StringBuffer fileData = new StringBuffer(1000);
			InputStreamReader reader = new InputStreamReader(mailTemplate);
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			String fileContents = fileData.toString();

			return fileContents;
		}
		catch (IOException e) {
			throw ProgramException.wrap("Error al intentar leer el archivo [" + fileName + "]", e);
		}
	}
	
	private static Hashtable<String, String> fileContents = new Hashtable<String, String>();
	private static CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
}
