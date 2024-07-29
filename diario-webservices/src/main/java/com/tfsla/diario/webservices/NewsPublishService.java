package com.tfsla.diario.webservices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.CmsLogReport;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.interfaces.INewsPublishService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.OfflineProjectService;
import com.tfsla.diario.webservices.helpers.AuditPermissionsHelper;
import com.tfsla.diario.webservices.helpers.VFSUnlockerHelper;
import com.tfsla.webusersnewspublisher.helper.expirenews.ExpireNewsService;

public class NewsPublishService extends OfflineProjectService implements INewsPublishService {

	public NewsPublishService(HttpServletRequest request) throws Throwable {
		super(request);
		String stringRequest = ServiceHelper.getRequestAsString(request);
		JSONObject jsonRequest = JSONObject.fromObject(stringRequest);
		this.requestItems = jsonRequest.getJSONArray(StringConstants.DATA);
		if(jsonRequest.containsKey(StringConstants.SITE)) {
			cms.getRequestContext().setSiteRoot(jsonRequest.getString(StringConstants.SITE));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JSON doExecute() throws Throwable {
		AuditPermissionsHelper.checkUserPermission(cms, StringConstants.PERMISSION_PUBLISH, StringConstants.PERMISSION_MODULE_NEWS);
		Throwable exception = null;
		JSONObject jsonResponse = new JSONObject();
		JSONArray items = new JSONArray();
		try {
			this.switchToOfflineSession();
			List<CmsResource> publishList = new ArrayList<CmsResource>();
			JSONObject item = null;
			ExpireNewsService service = new ExpireNewsService();
			
			for(int i=0; i<requestItems.size(); i++) {
				JSONObject jsonItem = requestItems.getJSONObject(i);
				String url = this.assertJSONParameter(StringConstants.URL, jsonItem);
				VFSUnlockerHelper.stealLock(cms, url);
				String result = StringConstants.OK;
				item = new JSONObject();
				item.put(StringConstants.URL, url);
				long releaseDate = 0;
				long expireDate = 0;
				Boolean publish = true;
				
				if(jsonItem.containsKey(StringConstants.FROM)) {
					try {
						String fromDate = jsonItem.getString(StringConstants.FROM);
						if(fromDate.toLowerCase().equals(StringConstants.NONE)) {
							releaseDate = Long.MIN_VALUE;
							item.put(StringConstants.RELEASED, StringConstants.NONE);
						} else {
							releaseDate = Long.parseLong(fromDate);
							item.put(StringConstants.RELEASED, releaseDate);
						}
						publish = false;
					} catch(Exception e) {
						result = StringConstants.WARNING;
						e.printStackTrace();
					}
				}
				
				if(jsonItem.containsKey(StringConstants.TO)) {
					try {
						String expirationDate = jsonItem.getString(StringConstants.TO);
						if(expirationDate.toLowerCase().equals(StringConstants.NONE)) {
							expireDate = Long.MIN_VALUE;
							item.put(StringConstants.EXPIRED, StringConstants.NONE);
						} else {
							expireDate = Long.parseLong(expirationDate);
							item.put(StringConstants.EXPIRED, expireDate);
						}
						publish = false;
					} catch(Exception e) {
						result = StringConstants.WARNING;
						e.printStackTrace();
					}
				}
				
				if(expireDate != 0 || releaseDate != 0) {
					try {
						if(releaseDate > expireDate && expireDate != Long.MIN_VALUE && releaseDate != Long.MIN_VALUE && expireDate != 0 && releaseDate != 0) {
							throw new Exception(ExceptionMessages.ERROR_EXPIRATION_DATES);
						}
						service.releaseAndExpire(cms, url, cms.getRequestContext().currentUser().getName(), releaseDate, expireDate);
					} catch(Exception e) {
						result = StringConstants.WARNING;
						item.put(StringConstants.MESSAGE, e.getMessage());
						e.printStackTrace();
					}
				}
				
				if(jsonItem.containsKey(StringConstants.SETLASTMODIFICATIONDATE) &&
						jsonItem.getString(StringConstants.SETLASTMODIFICATIONDATE).contentEquals("true")) {
					try {		
						CmsFile fileNew = cms.readFile(url,CmsResourceFilter.ALL);
				
						CmsXmlContent contentNew = CmsXmlContentFactory.unmarshal(cms, fileNew);
						contentNew.setAutoCorrectionEnabled(true);
						contentNew.correctXmlStructure(cms);
						contentNew.getValue("ultimaModificacion", Locale.ENGLISH).setStringValue(cms,  "" + (new Date()).getTime());
						
						String fileEncoding = cms.readPropertyObject(cms.getRequestContext().removeSiteRoot(fileNew.getRootPath()), CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());
	
						String decodedContent = contentNew.toString();
						decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");
						
						fileNew.setContents(decodedContent.getBytes(fileEncoding));
						
						cms.writeFile(fileNew);							
						cms.unlockResource(url);
						
					} catch (Exception ex) {
						result =  "Error al intentar modificar la noticia con la fecha de ultima modificacion - Publica la nota"+ ex;
						item.put(StringConstants.MESSAGE, ex.getMessage());
						ex.printStackTrace();
					}
				}
				
				item.put(StringConstants.RESULT, result);
				if(publish) {
					CmsResource cmsResource = cms.readResource(url, CmsResourceFilter.IGNORE_EXPIRATION);
					publishList.add(cmsResource);
					List relations = cms.getRelationsForResource(cmsResource, CmsRelationFilter.ALL);
					for (Object relation : relations) {
						CmsRelation rel = (CmsRelation) relation;
						CmsResource resSource = rel.getSource(cms, CmsResourceFilter.ALL); 
						if (!resSource.getResourceId().equals(cmsResource.getResourceId()))
							publishList.add(resSource);
						CmsResource resTarget = rel.getTarget(cms, CmsResourceFilter.ALL);
						if (!resTarget.getResourceId().equals(cmsResource.getResourceId()))
							publishList.add(resTarget);
					}
				}
				items.add(item);
			}
			if(publishList.size() > 0) {
				CmsPublishList listToPublish = OpenCms.getPublishManager().getPublishList(cms, publishList, false);
				OpenCms.getPublishManager().publishProject(cms,
					new CmsLogReport(Locale.getDefault(), this.getClass()),
					listToPublish
				);
			}
			jsonResponse.put(StringConstants.STATUS, StringConstants.OK);
			jsonResponse.put(StringConstants.DATA, items);
		} catch(Throwable e) {
			exception = e;
			jsonResponse.put(StringConstants.STATUS, StringConstants.ERROR);
			jsonResponse.put(StringConstants.ERROR, e.getMessage());
		} finally {
			this.restoreSession();
			if(exception != null) throw exception;
		}
		return jsonResponse;
	}
	
	protected JSONArray requestItems;
}
