package com.tfsla.admanager;

import static com.google.api.ads.common.lib.utils.Builder.DEFAULT_CONFIGURATION_FILENAME;

import java.rmi.RemoteException;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;

import com.google.api.ads.admanager.axis.factory.AdManagerServices;
import com.google.api.ads.admanager.axis.v201908.ApiError;
import com.google.api.ads.admanager.axis.v201908.ApiException;
import com.google.api.ads.admanager.axis.v201908.Network;
import com.google.api.ads.admanager.axis.v201908.NetworkServiceInterface;
import com.google.api.ads.admanager.lib.client.AdManagerSession;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.auth.oauth2.Credential;


public class TfsAdmanagerReporterTestNetwork {

	static String MODULE="admanager";
	
	public static String testNetwork(String site, String publication) throws Exception {
		
		CPMConfig cpmConfig = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String fileConfigPath = cpmConfig.getParam(site, publication, MODULE, "pathFile");
		
		AdManagerSession session;
	    try {
	    		// Generate a refreshable OAuth2 credential.
	    		Credential oAuth2Credential =
	          new OfflineCredentials.Builder()
	              .forApi(Api.AD_MANAGER).fromFile(fileConfigPath)
	              .build()
	              .generateCredential();

	      // Construct a AdManagerSession.
	      session =
	          new AdManagerSession.Builder().fromFile().withOAuth2Credential(oAuth2Credential).build();
	    } catch (ConfigurationLoadException cle) {
	      System.err.printf(
	          "Failed to load configuration from the %s file. Exception: %s%n",
	          DEFAULT_CONFIGURATION_FILENAME, cle);
	      return "Failed to load configuration from the "+DEFAULT_CONFIGURATION_FILENAME+" file. Exception: " + cle.getMessage();
	    } catch (ValidationException ve) {
	      System.err.printf(
	          "Invalid configuration in the %s file. Exception: %s%n",
	          DEFAULT_CONFIGURATION_FILENAME, ve);
	      return "Invalid configuration in the "+DEFAULT_CONFIGURATION_FILENAME+" file. Exception: " + ve.getMessage();
	    } catch (OAuthException oe) {
	      System.err.printf(
	          "Failed to create OAuth credentials. Check OAuth settings in the %s file. "
	              + "Exception: %s%n",
	          DEFAULT_CONFIGURATION_FILENAME, oe);
	      return "Failed to create OAuth credentials. Check OAuth settings in the "+DEFAULT_CONFIGURATION_FILENAME+" file. Exception: " + oe.getMessage();
		    
	    }

	    AdManagerServices adManagerServices = new AdManagerServices();
	
	    try {
	    		// Get the NetworkService.
	        NetworkServiceInterface networkService =
	            adManagerServices.get(session, NetworkServiceInterface.class);

	        // Get the current network.
	        Network network = networkService.getCurrentNetwork();

	        return 
	            "Current network has network code '"+network.getNetworkCode()+"' and display name '"+network.getDisplayName()+"' ";
	    } catch (ApiException apiException) {
	        // ApiException is the base class for most exceptions thrown by an API request. Instances
	        // of this exception have a message and a collection of ApiErrors that indicate the
	        // type and underlying cause of the exception. Every exception object in the admanager.axis
	        // packages will return a meaningful value from toString
	        //
	        // ApiException extends RemoteException, so this catch block must appear before the
	        // catch block for RemoteException.
	        System.err.println("Request failed due to ApiException. Underlying ApiErrors:");
	        if (apiException.getErrors() != null) {
	          int i = 0;
	          for (ApiError apiError : apiException.getErrors()) {
	            System.err.printf("  Error %d: %s%n", i++, apiError);
	          }
	        }
	        return "Request failed due to ApiException. Underlying ApiErrors:";
	      } catch (RemoteException re) {
	        System.err.printf("Request failed unexpectedly due to RemoteException: %s%n", re);
	        return "Request failed unexpectedly due to RemoteException:" + re.getMessage();
	      }
	}
}
