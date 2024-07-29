package com.tfsla.diario.ediciones.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.json.JSONObject;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.opencmsdev.NewsOnPublishEvents;
import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.UrlLinkHelper;

public class WebhookServices implements I_CmsEventListener{
	
	private static final Log LOG = CmsLog.getLog(WebhookServices.class);
	
	@Override
	public void cmsEvent(CmsEvent event) {
		
		CPMConfig _config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		//LOG.debug("WebhookServices INIT");
		
		int noticiaType;
		try {
			noticiaType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
			if (event.getType()==I_CmsEventListener.EVENT_PUBLISH_PROJECT) {
				LOG.debug("WebhookServices | es EVENT_PUBLISH_PROJECT");

				CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);

				if (pubList != null) {
					LOG.debug("WebhookServices | pubList != null");

					CmsObject cmsObject = getCmsObject(event);
	
					if (cmsObject != null) {
						LOG.debug("WebhookServices | cmsObject != null");
						
						
						//Verificar si se tiene que generar o no el envio de webhooks dependiendo el volumen total de peticiones externas que se van a generar.
						int totalNews =0 ;
						for (Iterator it = pubList.getFileList().iterator();it.hasNext();) {
							CmsResource resource = (CmsResource)it.next();
							if (resource.getTypeId()==noticiaType) {
								try {
									
									CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
									if (site==null)
										continue;
									
									cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
	
									String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();
									
									
									
									TipoEdicion tPublication = getPublicacion(resource,cmsObject);
									int idPublication = tPublication.getId();
									String publication = "" + idPublication;
									
									Boolean active = _config.getBooleanParam(siteName, publication, "webHooks", "active",false);
									if (active) {
										totalNews++;
									}					
									
								} catch (Exception e) {
										
									
								}
							}
						}
						//FIN Verificar
						if (totalNews>100) {
							LOG.warn("Massive publication of news. WebhookServices blocked. (" + totalNews + " news )");
							return;
						}
							
						for (Iterator it = pubList.getFileList().iterator();it.hasNext();) {
							final CmsResource resource = (CmsResource)it.next();
							LOG.debug("WebhookServices | resource =" + resource.getRootPath());
							LOG.debug("WebhookServices | resource.getTypeId() =" + resource.getTypeId());
							if (resource.getTypeId()==noticiaType) {
								try {
									
									CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
									if (site==null)
										continue;
									
									cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
	
									String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();
									
									boolean isNew = isNew(cmsObject,resource);
									
									
									TipoEdicion tPublication = getPublicacion(resource,cmsObject);
									int idPublication = tPublication.getId();
									String publication = "" + idPublication;
									
									Boolean active = _config.getBooleanParam(siteName, publication, "webHooks", "active",false);
									if (active) {
										LOG.debug("WebhookServices | active =" + active);
										//"url"  
										final String url = cmsObject.getSitePath(resource);
		
										//"urlFriendly"
										String urlFriendly = UrlLinkHelper.getUrlFriendlyLink(resource, cmsObject, null,false,true);
		
										String cannonical = UrlLinkHelper.getCanonicalLink(resource, cmsObject, null);
										
										List<String> endPoints = _config.getParamList(siteName, publication, "webHooks", "onPublish");
										for (String endPoint : endPoints) {
											
											final boolean isAsync = _config.getBooleanItempGroupParam(siteName, publication, "webHooks", endPoint + "-config", "isAsync",false);
											final int delay = _config.getIntegerItempGroupParam(siteName, publication, "webHooks", endPoint + "-config", "delay",0);
											final String urlEndPoint = _config.getItemGroupParam(siteName, publication, "webHooks", endPoint + "-config", "url");
											String payload = _config.getItemGroupParam(siteName, publication, "webHooks", endPoint + "-config", "payload");
											final String contentType = _config.getItemGroupParam(siteName, publication, "webHooks", endPoint + "-config", "contentType","application/json");
											final String reportEvent = _config.getItemGroupParam(siteName, publication, "webHooks", endPoint + "-config", "reportEvent","all");
											final String _charset = _config.getItemGroupParam(siteName, publication, "webHooks", endPoint + "-config", "charset","UTF-8");
											final String _protocol = _config.getItemGroupParam(siteName, publication, "webHooks", endPoint + "-config", "protocol","http");
											
											
											LOG.debug("WebhookServices | webhook url: " + url + " - reportEvent: " + reportEvent + " - isNew: " + isNew + " - isUpdate: " + resource.getState().isChanged() + " - isDelete: " + resource.getState().isDeleted());  
										
											boolean seguir = true;
											//Si la noticia es nueva y se debe enviar solo actualizaciones no continuo
											if ((isNew || resource.getState().isDeleted()) && reportEvent.trim().toLowerCase().equals("update")) {
												LOG.debug("WebhookServices | webhook url: " + url + " - la noticia es nueva y se debe enviar solo actualizaciones no continuo ");  
												seguir = false;
											}
											
											//Si la noticia es preexistente y se debe enviar solo novedades no continuo
											if (!isNew && reportEvent.trim().toLowerCase().equals("new")) {
												LOG.debug("WebhookServices | webhook url: " + url + " - la noticia es preexistente y se debe enviar solo novedades no continuo ");  
												seguir = false;
											}
												
											if (!resource.getState().isDeleted() && reportEvent.trim().toLowerCase().equals("delete")) {
												LOG.debug("WebhookServices | webhook url: " + url + " - la noticia no esta borrada y se debe enviar solo borradas no continuo ");  
												seguir = false;
											}
											
											if (seguir) {
												final Charset charset = Charset.forName(_charset);
												
												
												payload = payload.replace("${hasExpirationDate}", "" +(resource.getDateExpired()<Long.MAX_VALUE));
												payload = payload.replace("${hasReleaseDate}", "" +(resource.getDateReleased()>0));
												
												payload = payload.replace("${dateExpired}", "" +resource.getDateExpired());
												payload = payload.replace("${dateReleased}", "" +resource.getDateReleased());
												
												payload = payload.replace("${urlFriendly}", encode(urlFriendly,contentType,charset));
												payload = payload.replace("${siteRoot}", encode(url,contentType,charset));
												payload = payload.replace("${cannonical}", encode(cannonical,contentType,charset));
												
												LOG.debug("webhook url: " + url + " ( Async: " + isAsync + " | delay: " + delay + " ) - payload: " + payload);
												if (isAsync) {
													final String _payload = new String(payload);
													
													new Thread(new Runnable() {
													     @Override
													     public void run() {
													    	 try {
													    		 synchronized (this) {
														    		LOG.debug("waiting for " + urlEndPoint +" ..."); 
														    		wait(delay);
														    		LOG.debug("calling webhook " + urlEndPoint + " ..."); 
	
																	sendWebHook(urlEndPoint,_payload,contentType, charset, _protocol);
													    		 }
															} catch (IOException | InterruptedException e) {
																LOG.error("Error calling webhook url: " + url + " - payload: " + _payload + " for resource " + resource.getRootPath(),e );
															}
													     }
													}).start();
												}
												else {
													synchronized (this) {
														if (delay>0)
															wait(delay);
														sendWebHook(urlEndPoint,payload,contentType, charset, _protocol);
													}
												}
											}
										}
										
																	}								
								} catch (Exception e) {
									CmsLog.getLog(this).error("Hubo problemas enviando los webhooks",e);
								}
							}
						}
					}
				}
			}

		} catch (CmsLoaderException e) {
			LOG.error("Hubo problemas enviando los webhooks",e);
		}

		//LOG.debug("WebhookServices END");

	}

	private String encode(String text, String contentType, Charset charset) throws UnsupportedEncodingException {
		if (contentType.equals("application/json")) {
			return JSONObject.valueToString(text);
		}
		
		if (contentType.equals("application/x-www-form-urlencoded")) {
			return URLEncoder.encode(text,charset.name());
		}
		
		if (contentType.equals("application/xml")) {
			return StringEscapeUtils.escapeXml(text);
		}
		
		return text;
		
	}

	private void sendWebHook(String urlEndPoint, String payload, String contentType, Charset charset, String protocol) throws IOException {
		URL url = new URL(urlEndPoint);
		URLConnection con = url.openConnection();
		
		HttpURLConnection http = null;
		if (protocol.equals("http"))
			http = (HttpURLConnection)con;
		else
			http = (HttpsURLConnection)con;
		
		http.setRequestMethod("POST");
		http.setDoOutput(true);
		
		byte[] out = payload.toString().getBytes(charset);
		int length = out.length;
		
		LOG.debug("Calling: " + urlEndPoint);
		http.setFixedLengthStreamingMode(length);
		http.setRequestProperty("Content-Type", contentType + "; charset=" + charset.name());
		http.connect();
		try(OutputStream os = http.getOutputStream()) {
		    os.write(out);
		    os.flush();
			os.close();
		}
		catch (Exception e) {
			LOG.error("Hubo problemas enviando los webhooks",e);
		}

		int responseCode = http.getResponseCode();
		LOG.debug("Response code: " + responseCode);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				http.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		LOG.debug(response.toString());
		
	}

	private CmsObject getCmsObject(CmsEvent event) {
		CmsObject cmsObject=null;
		try {
			CmsObject cmsObjectToClone = (CmsObject)event.getData().get(I_CmsEventListener.KEY_CMS_OBJECT);
			cmsObject = CmsObjectUtils.getClone(cmsObjectToClone);
			if (cmsObject != null)
				cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
		} catch (Exception ex){
			CmsLog.getLog(this).error("Error al intentar obtener el cmsObject del evento",ex);
		}
		if (cmsObject == null) {
			cmsObject = CmsObjectUtils.loginAsAdmin();		
		}
		
		return cmsObject;
	}
	
	
	public boolean isNew(CmsObject cmso, CmsResource resource) throws Exception {
	    
	    String sitePath = cmso.getSitePath(resource);

	    return (cmso.readAllAvailableVersions(sitePath).size() == 1);
	    
	    /*
	    LOG.error("versiones: " + cmso.readAllAvailableVersions(sitePath).size());
	    if (cmso.readAllAvailableVersions(sitePath).size() > 0) {
	        I_CmsHistoryResource histRes = (I_CmsHistoryResource) cmso.readAllAvailableVersions(sitePath).get(0);
	        int publishTag = histRes.getPublishTag();
	        CmsHistoryProject project = cmso.readHistoryProject(publishTag);            
	        LOG.error(new Date(project.getPublishingDate()));
	        
	    } else {
	    	 LOG.error("sin fecha de publicacion");
	    }
	    return (cmso.readAllAvailableVersions(sitePath).size() == 0);
	    */
	    
	       
	    
	}

	private TipoEdicion getPublicacion(CmsResource resource, CmsObject cmsObject) {
		TipoEdicion tEdicion  = null;
		TipoEdicionService tEService = new TipoEdicionService();
		try {
			tEdicion = tEService.obtenerTipoEdicion(cmsObject,cmsObject.getSitePath(resource));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return tEdicion;
	}

}
