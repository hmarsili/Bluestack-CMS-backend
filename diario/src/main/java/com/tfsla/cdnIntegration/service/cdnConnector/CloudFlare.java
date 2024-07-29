package com.tfsla.cdnIntegration.service.cdnConnector;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDeleteWithBody;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;

import com.tfsla.cdnIntegration.model.InteractionResponse;
import com.tfsla.utils.UrlLinkHelper;

public class CloudFlare extends A_ContentDeliveryNetwork {

	private static final Log LOG = CmsLog.getLog(CloudFlare.class);
	
	protected String zone;
	protected String[] devices =  {"mobile", "desktop", "tablet"};
	
	public CloudFlare() {
		this.name = "cloudFlare";
	}
	
	public I_ContentDeliveryNetwork create() {
		return new CloudFlare();
	}

	public InteractionResponse invalidateCacheFile(String file) {
		
		InteractionResponse result = new InteractionResponse();
		return result;
		//NOT IMPLEMENTED (FOR FUTURE USE)
	}

	public InteractionResponse invalidateCacheFiles(List<String> files) throws URISyntaxException, ClientProtocolException, IOException {
		
		InteractionResponse result = new InteractionResponse();
				
		URI uri = new URIBuilder()
		        .setScheme("https")
		        .setHost("api.cloudflare.com")
		        .setPath("/client/v4/zones/" + zone +"/purge_cache")
		        .build();
		
		LOG.debug("CDN - URI CLoudflare" + uri.toString());
		
		

		for (String device : devices) {
				SSLConnectionSocketFactory sslConnectionSocketFactory = 
					    new SSLConnectionSocketFactory(SSLContexts.createDefault(),          
					                                   new String[] { "TLSv1.2" },                                            
					                                   null, 
					           SSLConnectionSocketFactory.getDefaultHostnameVerifier());
	
				
				
				CloseableHttpClient httpclient = HttpClientBuilder.create()
	                    .setSSLSocketFactory(sslConnectionSocketFactory)
	                    .build();
                    
			HttpDeleteWithBody httpdelete = new HttpDeleteWithBody(uri);
			httpdelete.setHeader("X-Auth-Email",user);
			httpdelete.setHeader("X-Auth-Key",key);
			httpdelete.setHeader("Content-Type","application/json");
			httpdelete.setHeader("CF-Device-Type",device);
		
			String body = "{\"files\":[";

			int j=0;
			for (String file : files) {
				if (j>0) body+=",";
				body+="{\"url\":\"" + file + "\",\"headers\":{\"CF-Device-Type\":\""+ device + "\"}}";
				j++;
			}
			body+="]}";
			
			httpdelete.setEntity(new StringEntity(body));
				
			LOG.debug("CDN Device: " + device + " - " +  IOUtils.toString( httpdelete.getEntity().getContent() ));
			CloseableHttpResponse responseDelete =null;
			try {
				responseDelete = httpclient.execute(httpdelete);
				
				if (responseDelete.getStatusLine().getStatusCode() != 200) {
					
					result.setSuccess(false);
					result.setResponseMsg(responseDelete.getStatusLine().getStatusCode() + " - " + responseDelete.getStatusLine().getReasonPhrase());
					
					String s = IOUtils.toString( responseDelete.getEntity().getContent() );
					LOG.debug( "CDN response:  " + s);
					
					JSONObject json = (JSONObject) JSONSerializer.toJSON(s);
					
					
					JSONArray errors = json.getJSONArray("errors");
					for (int i = 0; i < errors.size(); ++i) {
					    JSONObject err = errors.getJSONObject(i);
					    String message = err.getString("message");
					    String code = err.getString("code");
					    
					    result.addError(message + " (errCode:" + code + ")");
					    
					    LOG.debug("CDN Cloudflare - "+device +" - Error: " + message + " (errCode:" + code + ")");
					}
				}
				else {
				
					String s = IOUtils.toString( responseDelete.getEntity().getContent() );
					LOG.debug( "CDN response:  " + s);
					
					JSONObject json = (JSONObject) JSONSerializer.toJSON(s);
				
					
					boolean success = json.getBoolean("success");
					
					if (success) {
						JSONObject res = json.getJSONObject("result");
						String id = res.getString("id");
						
						result.setInteractionId(id);
					}
					else {
						JSONArray errors = json.getJSONArray("errors");
						for (int i = 0; i < errors.size(); ++i) {
						    JSONObject err = errors.getJSONObject(i);
						    String message = err.getString("message");
						    String code = err.getString("code");
						    
						    result.addError(message + " (errCode:" + code + ")");
						    LOG.debug("CDN Error - "+device +" : " + " (errCode:" + code + ")");
						}
					}
				    result.setSuccess(success);
				    
				}
			}
			catch (Exception e) {
				LOG.error("CDN Error en el proceso de purga de cdn.",e);
				result.setSuccess(false);
				result.setResponseMsg(e.getMessage());
			}
			
			finally {
				if (responseDelete != null)
					responseDelete.close();
			}
		}
		
		//Si se genero algun error entonces se setea como erroneo el paquete
		if (result.getErrorList() != null && result.getErrorList().size() >0)
			result.setSuccess(false);
		
		return result;
	}

	public String getCachedName(CmsObject cmsObject, CmsResource resource) {
		return UrlLinkHelper.getUrlFriendlyLink(resource, cmsObject, false, true);
	}

	public I_ContentDeliveryNetwork configure(String siteName, String publication) {

		isActive = CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "isActive", false);
		maxFilesToInvalidate = CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "maxFilesToInvalidate", 10);
		user = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "user", "");
		key = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "key", "");
		zone = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "zone", "");
		retries = CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "retries", 0);
		return this;
	}

	public InteractionResponse test() throws ClientProtocolException, IOException, URISyntaxException {

		InteractionResponse result = new InteractionResponse();
		
		URI uri = new URIBuilder()
				.setScheme("https")
				.setHost("api.cloudflare.com")
				.setPath("/client/v4/zones")
				.build();

		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpGet httpget = new HttpGet(uri);
		httpget.setHeader("X-Auth-Email",user);
		httpget.setHeader("X-Auth-Key",key);
		httpget.setHeader("Content-Type","application/json");
		CloseableHttpResponse responseGet = httpclient.execute(httpget);
		try {
		
			if (responseGet.getStatusLine().getStatusCode() != 200) {
				result.setSuccess(false);
				result.setResponseMsg(responseGet.getStatusLine().getStatusCode() + " - " + responseGet.getStatusLine().getReasonPhrase());
					
				String s = IOUtils.toString( responseGet.getEntity().getContent() );
				JSONObject json = (JSONObject) JSONSerializer.toJSON(s);
				
				JSONArray errors = json.getJSONArray("errors");
				for (int i = 0; i < errors.size(); ++i) {
				    JSONObject err = errors.getJSONObject(i);
				    String message = err.getString("message");
				    String code = err.getString("code");
				    
				    result.addError(message + " (errCode:" + code + ")");
				    
				}

			}
			
			
			String s = IOUtils.toString( responseGet.getEntity().getContent() );
			JSONObject json = (JSONObject) JSONSerializer.toJSON(s);
		
			boolean success = json.getBoolean("success");
			
			if (success) {
				String msg="";
				JSONArray results = json.getJSONArray("result");
				for (int i = 0; i < results.size(); ++i) {
				    JSONObject res = results.getJSONObject(i);
				    String nombre = res.getString("name");
				    String id = res.getString("id");
				    String status = res.getString("status");
				    msg+= nombre + " (" + id + "): " + status + "\n";
				}			
				result.setResponseMsg(msg);
			}
			else {
				JSONArray errors = json.getJSONArray("errors");
				for (int i = 0; i < errors.size(); ++i) {
				    JSONObject err = errors.getJSONObject(i);
				    String message = err.getString("message");
				    String code = err.getString("code");
				    
				    result.addError(message + " (errCode:" + code + ")");
				    
				}
			}
		    result.setSuccess(success);
		}
		catch (Exception e) {
				result.setSuccess(false);
				result.setResponseMsg(e.getMessage());
		}
		finally {
		    responseGet.close();
		}

		return result;
		
	}

	public int getMaxPackageSendRetries() {
		return this.getRetries();
	}

	public InteractionResponse getInvalidationStatus(String invalidationId) {
		// TODO Auto-generated method stub
		return null;
	}


}
