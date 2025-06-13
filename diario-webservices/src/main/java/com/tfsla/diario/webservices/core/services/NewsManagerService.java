package com.tfsla.diario.webservices.core.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.Helpers.ImportRulesManager;
import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.SessionManager;
import com.tfsla.diario.webservices.helpers.AuditPermissionsHelper;
import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.webusersposts.helper.XmlContentHelper;

public abstract class NewsManagerService extends OfflineProjectService {

	protected boolean processFields = false;
	protected Date folderDate = null; 
	
	private static final Log LOG = CmsLog.getLog(NewsManagerService.class);

	public NewsManagerService(HttpServletRequest request) throws Throwable {
		super(request);
		String stringRequest = ServiceHelper.getRequestAsString(request);
		this.jsonRequest = JSONObject.fromObject(stringRequest);
		this.requestItems = jsonRequest.getJSONArray(StringConstants.DATA);
		if(jsonRequest.containsKey(StringConstants.NEWSTYPE)) {
			this.newsType = jsonRequest.getString(StringConstants.NEWSTYPE);
		}
		if(jsonRequest.containsKey(StringConstants.PUBLICATION)) {
			this.publication = jsonRequest.getString(StringConstants.PUBLICATION);
		}
		System.out.println("NewsManagerService - Publication: " + this.publication);
		
		
		if(jsonRequest.containsKey(StringConstants.SITE)) {
			this.site = jsonRequest.getString(StringConstants.SITE);
		}
		
		System.out.println("NewsManagerService - Site: " + this.site);
		
		if(jsonRequest.containsKey(StringConstants.PROCESS_FIELDS)) {
			this.processFields = jsonRequest.getString(StringConstants.PROCESS_FIELDS).toLowerCase().equals("true");
		}
		
		if(jsonRequest.containsKey(StringConstants.FOLDER_DATE)) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			folderDate = sdf.parse(jsonRequest.getString(StringConstants.FOLDER_DATE));
			
		}
		
	}

	@Override
	protected JSON doExecute() throws Throwable {
		this.permissionLevel = AuditPermissionsHelper.checkUserPermission(cms, this.getPermissionRequired(), StringConstants.PERMISSION_MODULE_NEWS);
		
		ImportRulesManager impManager=null;
		if (this.processFields) {
			impManager = ImportRulesManager.getPublicationImportRules(site, publication);
			impManager.setRequest(request);
		}
		
		Throwable exception = null;
		JSONArray jsonResponse = new JSONArray();
		try {
			this.switchToOfflineSession();
			CmsObject cmsClone = CmsObjectUtils.getClone(cms);
			
			if (this.processFields) {
				impManager.setCms(cmsClone);
				impManager.setFolderDate(folderDate);
				
			}
			//Iterar por todas las noticias recividas en el request
			for(int i=0; i<requestItems.size(); i++) {
				JSONObject item = new JSONObject();
				String resourceName = null;
				
				String report = "";
				try {

					// Clono el cmsObject y me aseguro que este en el offline.
					// Un problema encontrado es que si este webservice es llamado en corto tiempo por algun motivo el cmsobject pasa al online.
					cmsClone = CmsObjectUtils.getClone(cms);
					SessionManager.switchToProject(cmsClone, SessionManager.PROJECT_OFFLINE);
					SessionManager.selectSiteRoot(cmsClone, this.site);
					
					//Obtener CmsFile (crearlo o cargarlo)
					JSONArray noticia = requestItems.getJSONArray(i);
					CmsFile cmsFile = this.getCmsFile(noticia, cmsClone);
					CmsXmlContent xmlContent = this.getXmlContent(cmsFile, cmsClone);
					JSONObject itemNoticia = null;
					resourceName = "";
					
					//LOG.info("xmlContent " + xmlContent);

					//Setear todas las properties del contenido en base a elementos en el array del request
					for(int index=0; index<noticia.size(); index++) {
						itemNoticia = noticia.getJSONObject(index);
						for(Object key : itemNoticia.keySet()) {
							
							String keyString = key.toString();
							
							LOG.debug("clave a procesar:" + keyString);
							if(skipParameters != null) {
								//Saltear este parámetro (para url en edición, no setear en XML)
								if(skipParameters.contains(keyString)) continue;
							}

							I_CmsXmlContentValue value = xmlContent.getValue(keyString, locale);
							LOG.debug("campo xsd a de la clave: " + value);

							if(value != null) {
								String content = itemNoticia.getString(keyString);

								if (this.processFields) {
									
									content = impManager.executeRules(keyString, content);
									report += impManager.getReport();
								}
								value.setStringValue(cmsClone, content);
								
								LOG.debug("se guarda el contenido en el campo: " + content);

							} else {
								String elementName = keyString.contains("[") ? keyString.substring(0, keyString.indexOf("[")) : keyString;
								LOG.debug("se debe generar el sub valor: " + elementName);

								xmlContent.addValue(cmsClone, elementName, locale, xmlContent.getIndexCount(elementName, locale));
								LOG.debug("se agrega al xsd el contenido:" + elementName);
								
								value = xmlContent.getValue(keyString, locale);
								LOG.debug("campo xsd a de la clave: " + keyString);

								String content = itemNoticia.getString(keyString);

								if (this.processFields) {
									content = impManager.executeRules(elementName, content);
									report += impManager.getReport();

								}

								// ini NAA-3251 remplazamos por si hay un subnivel
								//value.setStringValue(cmsClone, content);

								if(value != null) {
									
									value.setStringValue(cmsClone, content);
									LOG.debug("se comenta  el contenido en el campo: " + content);
									
								} else {
									String elementNameSpl[] = keyString.split("/");
									LOG.debug("Tercer subnivel split: " + elementNameSpl.length);
									
									String elementNameSubNivel = elementNameSpl[1].contains("[") ? elementNameSpl[1].substring(0, elementNameSpl[1].indexOf("[")) : elementNameSpl[1];
									LOG.debug("se debe generar el sub valor: " + elementNameSubNivel);
									
									String path = elementNameSpl[0]+"/"+elementNameSpl[1];
									LOG.debug("bajo el path " + path);

									xmlContent.addValue(cmsClone, path, locale, xmlContent.getIndexCount(path, locale));
									LOG.debug("se agrega al xsd el contenido: " + path);
									
									value = xmlContent.getValue(keyString, locale);
									LOG.debug("campo xsd de la clave: " + keyString);

									content = itemNoticia.getString(keyString);

									if (this.processFields) {
										content = impManager.executeRules(elementName, content);
										report += impManager.getReport();

									}
									
									value.setStringValue(cmsClone, content);
								}
								
								// fin NAA-3251 
						}
					}
					}
					resourceName = cmsClone.getSitePath(cmsFile);
					cmsFile.setContents(xmlContent.marshal());
					cmsClone.writeFile(cmsFile);
					cmsClone.unlockResource(resourceName);
					
					item.put(StringConstants.STATUS, StringConstants.OK);
					item.put(StringConstants.INDEX, i);
					item.put(StringConstants.NAME, resourceName);
					item.put(StringConstants.REPORT, report);
				} catch(Exception e) {
					item.put(StringConstants.STATUS, StringConstants.ERROR);
					item.put(StringConstants.INDEX, i);
					item.put(StringConstants.ERROR, e.getMessage());
					LOG.error("Error creating resource " + resourceName + " en " + cmsClone.getRequestContext().getSiteRoot() + " (" + cmsClone.getRequestContext().currentProject().getName()  + "|" + cmsClone.getRequestContext().currentUser().getFullName() + " )" ,e);
					this.onItemError(resourceName);
				}
				jsonResponse.add(item);
			}
		} catch(Throwable e) {
			exception = e;
		} finally {
			this.restoreSession();
			if(exception != null) throw exception;
		}
		return jsonResponse;
	}

	protected void onItemError(String resourceName) {
		//overrided on NewsAddService
	}
	
	protected CmsXmlContent getXmlContent(CmsFile cmsFile, CmsObject cmsObject) throws Exception {
		XmlContentHelper contentHelper = new XmlContentHelper(cms);
		if (!requestItems.toString().contains("autor[1]/nombre[1]") && !requestItems.toString().contains("internalUser")) 
			contentHelper.setXmlContentValue("autor/internalUser", cms.getRequestContext().currentUser().getName());
		return contentHelper.getXmlContent();
	}
	
	protected abstract CmsFile getCmsFile(JSONArray noticia, CmsObject cmsObject) throws Exception;
	protected abstract int getPermissionRequired();
	
	protected int permissionLevel;
	protected ArrayList<String> skipParameters;
	protected String newsType;
	protected JSONArray requestItems;
	protected JSONObject jsonRequest;
}