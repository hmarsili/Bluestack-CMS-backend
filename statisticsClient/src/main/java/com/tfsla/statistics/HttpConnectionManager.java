package com.tfsla.statistics;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.kernel.http.HTTPConstants;
//import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.HttpMethodRetryHandler;
//import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
//import org.apache.commons.httpclient.params.HttpClientParams;
//import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
//import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class HttpConnectionManager {

	static Log LOG = CmsLog.getLog(HttpConnectionManager.class);
	
	private static HttpConnectionManager  instance = null;
	
	
	public static HttpConnectionManager getInstance()
	{
		if (instance==null)
			instance = new HttpConnectionManager();
		
		return instance;
	}
	
	private HttpConnectionManager()
	{
		initConfiguration();
	}
	
	private ConfigurationContext configurationContext=null;
	private MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager=null;
	
	protected void initConfiguration()
    {
     	multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager(); 

     	try {

     		configurationContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null,null);
     	
	        HttpConnectionManagerParams params = new HttpConnectionManagerParams(); 
	        HttpMethodRetryHandler retry_handler = new DefaultHttpMethodRetryHandler(SoapConfig.getInstance().getConnectionRetryCount(), true);
	        HttpClientParams httpClientParams = new HttpClientParams();params.setParameter(HttpMethodParams.RETRY_HANDLER, retry_handler);
	        
	        
	        params.setDefaultMaxConnectionsPerHost(SoapConfig.getInstance().getMaxConnectionsPerHost());

	        
	        //params.setDefaultMaxConnectionsPerHost(800); 
	        multiThreadedHttpConnectionManager.setParams(params); 
	        
	        
	        HttpClient httpClient = new HttpClient(httpClientParams, multiThreadedHttpConnectionManager); 
	        
	        
	        configurationContext.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

	        configurationContext.setProperty(HTTPConstants.SO_TIMEOUT, SoapConfig.getInstance().getSocketTimeOutMilliSeconds());
	        configurationContext.setProperty(HTTPConstants.CONNECTION_TIMEOUT, SoapConfig.getInstance().getConnectionTimeOutMilliSeconds());

	        configurationContext.setProperty(HTTPConstants.HTTP_PROTOCOL_VERSION,HTTPConstants.HEADER_PROTOCOL_10);

	        	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

     }

	public ConfigurationContext getConfigurationContext() {
		return configurationContext;
	}
	
	public void shutdown()
	{
		multiThreadedHttpConnectionManager.closeIdleConnections(0);
		multiThreadedHttpConnectionManager.shutdown(); 
	}
	
	public void clean()
	{
		LOG.info("Cleaning idle connections.");
		multiThreadedHttpConnectionManager.closeIdleConnections(0);
		
	}
}
