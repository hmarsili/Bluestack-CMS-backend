package com.tfsla.cdnIntegration.service.cdnConnector;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;

import com.tfsla.cdnIntegration.model.InteractionResponse;
import com.tfsla.cdnIntegration.util.ClientCredential;
import com.tfsla.cdnIntegration.util.EdgeGridV1Signer;
import com.tfsla.utils.UrlLinkHelper;

public class Akamai extends A_ContentDeliveryNetwork {

	private static final Log LOG = CmsLog.getLog(Akamai.class);

	private String access_token;
	private String client_secret;
	private String client_token;
	private String host;
	private String network;
	
	public Akamai() {
		this.name = "akamai";
	}
	
	public I_ContentDeliveryNetwork create() {
		return new Akamai();
	}

	public InteractionResponse invalidateCacheFile(String file) {
		
		InteractionResponse result = new InteractionResponse();
		List<String> oneFile = new ArrayList<String>();
		oneFile.add(file);
		try {
			result = invalidateCacheFiles(oneFile);
		} catch (Exception ex) {
			LOG.error("Error creando invalidation para " + file , ex);
		}
		
		return result;
		
	}

	public InteractionResponse invalidateCacheFiles(List<String> files) throws URISyntaxException, ClientProtocolException, IOException {
		
		InteractionResponse result = new InteractionResponse();

		String path = "/ccu/v3/invalidate/url/" + network;
		URI uri = new URI("https", host,path,null,null);
		
		LOG.debug("Akamai - uri:" + uri.toString());
			
		ClientCredential credentials = new ClientCredential();
		credentials.setAccessToken(access_token);
		credentials.setClientSecret(client_secret);
		credentials.setClientToken(client_token);
		credentials.setHost(host);
				
		CloseableHttpClient httpclient =  HttpClientBuilder.create().build();
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type","application/json");

			
		String body = "{ \"objects\":[";

		int j=0;
		for (String file : files) {
			if (j>0) body+=",";
			body+="\"" + file + "\"";
			j++;
		}
		body+="]}";
		
		httppost.setEntity(new StringEntity(body));
		LOG.debug("Akamai - json:" + IOUtils.toString( httppost.getEntity().getContent() ));
		EdgeGridV1Signer signer = new EdgeGridV1Signer();
		try {
			String authorization = signer.getSignature(httppost, credentials);
			httppost.setHeader("Authorization",authorization);
		} catch (Exception ex) {
			LOG.error("Akamai - error al generar la autorizacion. ", ex);
		}
		CloseableHttpResponse responseDelete = httpclient.execute(httppost);
		
		try {
			if (responseDelete.getStatusLine().getStatusCode() != 201) {
				
				result.setSuccess(false);
				result.setResponseMsg(responseDelete.getStatusLine().getStatusCode() + " - " + responseDelete.getStatusLine().getReasonPhrase());

				String s = IOUtils.toString( responseDelete.getEntity().getContent() );
				JSONObject json = (JSONObject) JSONSerializer.toJSON(s);
				
				LOG.debug("Akamai - response: " + s);
							
				String httpStatus = json.getString("httpStatus");
				String detail = json.getString("detail");
				String purgeId = json.getString("purgeId");
						    
			    result.addError("Akamai - (errCode:" + httpStatus + ") - purgeId: " + purgeId + " - detail: " + detail);
			    
			    LOG.debug("Akamai -(errCode:" + httpStatus + ") - purgeId: " + purgeId + " - detail: " + detail);
				
			} else {
			
				String s = IOUtils.toString( responseDelete.getEntity().getContent() );
				JSONObject json = (JSONObject) JSONSerializer.toJSON(s);
			
				LOG.debug("Akamai - response: " + s);
				
				 result.setSuccess(true);
				
				 String purgeId = json.getString("purgeId");
					
				result.setInteractionId(purgeId);
			}
		} catch (Exception e) {
			LOG.error("Akamai - Error en el proceso de purga de cdn.",e);
			result.setSuccess(false);
			result.setResponseMsg("Akamai - Error en el proceso de purga de cdn." +e.getMessage());
		} finally {
			responseDelete.close();
		}
	
		return result;
	}

	public String getCachedName(CmsObject cmsObject, CmsResource resource) {
		return UrlLinkHelper.getUrlFriendlyLink(resource, cmsObject, false, true);
	}

	public I_ContentDeliveryNetwork configure(String siteName, String publication) {

		isActive = CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "isActive", false);
		
		maxFilesToInvalidate = CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "maxFilesToInvalidate", 10);
		
		retries = CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "retries", 0);
		
		access_token = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "access_token", "");
        client_secret = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "client_secret", "");
        client_token = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "client_token", "");
        host = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "host", "");
        network = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "network", "");
		return this;
	}

	public InteractionResponse test() throws ClientProtocolException, IOException, URISyntaxException {

		InteractionResponse result = new InteractionResponse();
		
		String path = "/ccu/v3/invalidate/url/" + network;
		URI uri = new URI("https", host,path,null,null);
		
		String body = "{\"objects\": [\"\"]}";
		CloseableHttpClient httpclient =  HttpClientBuilder.create().build();	
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type","application/json");
		httppost.setEntity(new StringEntity(body));
		
		LOG.debug(IOUtils.toString( httppost.getEntity().getContent() ));
		
		ClientCredential credentials = new ClientCredential();
		credentials.setAccessToken(access_token);
		credentials.setClientSecret(client_secret);
		credentials.setClientToken(client_token);
		credentials.setHost(host);
	
		EdgeGridV1Signer signer = new EdgeGridV1Signer();
		try {
			String authorization = signer.getSignature(httppost, credentials);
			httppost.setHeader("Authorization",authorization);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		CloseableHttpResponse responseDelete = httpclient.execute(httppost);
		try {
			if (responseDelete.getStatusLine().getStatusCode() != 201) {
				
				result.setSuccess(false);
				result.setResponseMsg(responseDelete.getStatusLine().getStatusCode() + " - " + responseDelete.getStatusLine().getReasonPhrase());

				String s = IOUtils.toString( responseDelete.getEntity().getContent() );
				JSONObject json = (JSONObject) JSONSerializer.toJSON(s);
				
				LOG.debug(s);
							
				String httpStatus = json.getString("status");
				String detail = json.getString("detail");
				String requestId = json.getString("requestId");
						    
			    result.addError("(errCode:" + httpStatus + ") - requestId: " + requestId + " - detail: " + detail);
			    
			    LOG.debug("(errCode:" + httpStatus + ") - requestId: " + requestId + " - detail: " + detail);
				
			} else {
			
				String s = IOUtils.toString( responseDelete.getEntity().getContent() );
				JSONObject json = (JSONObject) JSONSerializer.toJSON(s);
			
				LOG.debug(s);
				
				 result.setSuccess(true);
				
				 String purgeId = json.getString("purgeId");
					
				result.setInteractionId(purgeId);
			}
		} catch (Exception e) {
			LOG.error("Error en el proceso de purga de cdn.",e);
			result.setSuccess(false);
			result.setResponseMsg(e.getMessage());
		} finally {
			responseDelete.close();
		}
	
		return result;
	
			
		
	/*	LOG.debug(uri.toString());
		ClientCredential credentials = ClientCredential.builder()
		        .accessToken(access_token)
		        .clientToken(client_token)
		        .clientSecret(client_secret)
		        .host(host)
		        .build();
		
			
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type","application/json");
		
		
	
		
		HttpTransport HTTP_TRANSPORT = new ApacheHttpTransport();
		HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();

		//It is important to send the Request with the Content-Type application/json
		HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(uri),ByteArrayContent.fromString("application/json", body));
		 
		//Set the Host Header of the OPEN API Call based on the OPEN API Credential Property File Value 'Host'
		HttpHeaders headers = request.getHeaders();
		headers.set("Host", host);
		
		
		GoogleHttpClientEdgeGridRequestSigner requestSigner = new GoogleHttpClientEdgeGridRequestSigner(credentials);
		try {
			requestSigner.sign(request);
			HttpResponse response = request.execute();
		} catch (RequestSigningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//send the request to the OPEN API Interface via HTTP POST
	
		
		return result;
		*/
	}

	public int getMaxPackageSendRetries() {
		return this.getRetries();
	}

	public InteractionResponse getInvalidationStatus(String invalidationId) {
		// TODO Auto-generated method stub
		return null;
	}


}