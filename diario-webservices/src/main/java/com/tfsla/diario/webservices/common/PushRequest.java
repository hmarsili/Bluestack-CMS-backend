package com.tfsla.diario.webservices.common;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.PushNotificationTypes;
import com.tfsla.diario.webservices.helpers.PushTypeHelper;

/**
 * Represents Push requests to be scheduled (or already scheduled) 
 */
public class PushRequest {

	/**
	 * Generates a PushRequest to be scheduled based on a CmsResource, but this request
	 * will have a special date to push and a push type
	 * @param cms session CmsObject instance
	 * @param resource the CmsResource to be pushed
	 * @param site session site
	 * @param publication session publication
	 * @param type push type
	 * @param date scheduled date to push
	 * @return PushRequest instance based on the CmsResource provided
	 * @throws Exception
	 */
	public static PushRequest getRequestFromResource(CmsObject cms, CmsResource resource, String site, String publication, String type, Date date) throws Exception {
		CmsFile cmsFile = cms.readFile(resource);
		CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, cmsFile);
		PushRequest request = getRequestFromResource(cms, resource, xmlContent, site, publication);
		if(request.getPushType() == null || request.getPushType().equals("") || request.getPushType().equals(PushNotificationTypes.NINGUNO)) {
			request.setPushType(type);
		}
		if(request.getPushType().equals(PushNotificationTypes.EN_COLA)) {
			return request;
		}
		if(date != null) {
			request.setPushDate(date);
		} else {
			request.setPushDate(getPushDate(xmlContent, cms, cms.getRequestContext().getLocale(), request.getPushType()));
		}
		return request;
	}
	
	/**
	 * Generates a PushRequest to be scheduled based on a CmsResource
	 * @param cms session CmsObject instance
	 * @param resource the CmsResource to be pushed
	 * @param site session site
	 * @param publication session publication
	 * @return PushRequest instance based on the CmsResource provided
	 * @throws Exception
	 */
	public static PushRequest getRequestFromResource(CmsObject cms, CmsResource resource, String site, String publication) throws Exception {
		CmsFile cmsFile = cms.readFile(resource);
		CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, cmsFile);
		Locale locale = cms.getRequestContext().getLocale();
		PushRequest request = getRequestFromResource(cms, resource, xmlContent, site, publication);
		if(request.getPushType().equals(PushNotificationTypes.EN_COLA)) {
			return request;
		}
		request.setPushDate(getPushDate(xmlContent, cms, locale, request.getPushType()));
		return request;
	}
	
	/**
	 * Generates a PushRequest instance with their basic properties initialized
	 * @param cms session CmsObject instance
	 * @param resource the CmsResource to be pushed
	 * @param xmlContent resource's XML content
	 * @param site session site
	 * @param publication session publication
	 * @return a PushRequest instance with their basic properties initialized
	 * @throws CmsXmlException
	 * @throws CmsException
	 */
	private static PushRequest getRequestFromResource(CmsObject cms, CmsResource resource, CmsXmlContent xmlContent, String site, String publication) throws CmsXmlException, CmsException {
		PushRequest request = new PushRequest();
		String title = xmlContent.getStringValue(cms, "tituloPush", cms.getRequestContext().getLocale());
		if (title == null || title.equals("")) {
			title = xmlContent.getStringValue(cms, "titulo", cms.getRequestContext().getLocale());
		}
		request.setStructureId(resource.getStructureId().toString());
		request.setPath(resource.getRootPath());
		request.setPushType(PushTypeHelper.getPushTypeFromResource(resource, cms, xmlContent));
		request.setTitle(title);
		request.setSite(site);
		request.setPublication(publication);
		return request;
	}
	
	/**
	 * Retrieves the date to be scheduled for pushing out the CmsResource
	 * @param xmlContent the CmsResource Xml Content
	 * @param cms process CmsObject
	 * @param locale a Locale to be used to parse the date
	 * @return a Date instance retrieved from the Xml
	 * @throws Exception 
	 */
	private synchronized static Date getPushDate(CmsXmlContent xmlContent, CmsObject cms, Locale locale, String pushType) throws Exception {
		if(pushType.equals(PushNotificationTypes.INMEDIATO)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.MINUTE, 2);
			return calendar.getTime();
		}
		String date = xmlContent.getStringValue(cms, "schedulePush", locale);
		if(date != null && !date.equals("")) {
			long dateLong = Long.parseLong(date);
			return new Date(dateLong);
		}
		
		throw new Exception(ExceptionMessages.MISSING_SCHEDULE_DATE);
	}
	
	public Date getPushDate() {
		return pushDate;
	}
	public void setPushDate(Date pushDate) {
		this.pushDate = pushDate;
	}
	public String getPushType() {
		return pushType;
	}
	public void setPushType(String pushType) {
		this.pushType = pushType;
	}
	public String getStructureId() {
		return structureId;
	}
	public void setStructureId(String structureId) {
		this.structureId = structureId;
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public String getPublication() {
		return publication;
	}
	public void setPublication(String publication) {
		this.publication = publication;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	private Date pushDate;
	private String pushType;
	private String structureId;
	private String site;
	private String publication;
	private String title;
	private String path;
}
