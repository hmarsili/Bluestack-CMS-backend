package com.tfsla.admanager;

import static com.google.api.ads.common.lib.utils.Builder.DEFAULT_CONFIGURATION_FILENAME;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import com.google.api.ads.admanager.axis.factory.AdManagerServices;
import com.google.api.ads.admanager.axis.utils.v201908.ReportDownloader;
import com.google.api.ads.admanager.axis.utils.v201908.StatementBuilder;
import com.google.api.ads.admanager.axis.v201908.Column;
import com.google.api.ads.admanager.axis.v201908.DateRangeType;
import com.google.api.ads.admanager.axis.v201908.Dimension;
import com.google.api.ads.admanager.axis.v201908.ExportFormat;
import com.google.api.ads.admanager.axis.v201908.ReportDownloadOptions;
import com.google.api.ads.admanager.axis.v201908.ReportJob;
import com.google.api.ads.admanager.axis.v201908.ReportQuery;
import com.google.api.ads.admanager.axis.v201908.ReportServiceInterface;
import com.google.api.ads.admanager.axis.v201908.SavedQuery;
import com.google.api.ads.admanager.axis.v201908.SavedQueryPage;
import com.google.api.ads.admanager.axis.v201908.TimeZoneType;
import com.google.api.ads.admanager.lib.client.AdManagerSession;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.common.lib.utils.CsvFiles;
import com.google.api.client.auth.oauth2.Credential;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TfsAdManagerReporter {
	
	static String MODULE="admanager";
	
	protected static final Log LOG = CmsLog.getLog(TfsAdManagerReporter.class);
	private String site;
	private String publication;
	
	public TfsAdManagerReporter(String site, String publication) {
		this.site=site;
		this.publication = publication;
	}
	
	private AdManagerSession connect () throws Exception {
			
		CPMConfig cpmConfig = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String fileConfigPath = cpmConfig.getParam(site, publication, MODULE, "pathFile");
		
		AdManagerSession session = null;
		
		if (fileConfigPath.equals("")){
			LOG.debug("No está configurado ad manager para: " + site + " publicación: " + publication );	
			throw new Exception (" No está configurado ad manager para: " + site + " publicación: " + publication);	
		}
		try {
			// Generate a refreshable OAuth2 credential.
		    	Credential oAuth2Credential =
		    			new OfflineCredentials
		    				.Builder()
		    				.forApi(Api.AD_MANAGER).fromFile(fileConfigPath)
		    				.build()
		    				.generateCredential();

			      // Construct a AdManagerSession.
			 session = new AdManagerSession
					 .Builder()
					 .fromFile()
					 .withOAuth2Credential(oAuth2Credential)
					 .build();
		    } catch (ConfigurationLoadException cle) {
		      LOG.error ("Failed to load configuration from the "+ DEFAULT_CONFIGURATION_FILENAME+" file. Exception:" ,cle);
		    } catch (ValidationException ve) {
		      LOG.error( "Invalid configuration in the "+ DEFAULT_CONFIGURATION_FILENAME+" file. Exception: ",ve);
		    } catch (OAuthException oe) {
		      LOG.error("Failed to create OAuth credentials. Check OAuth settings in the "+DEFAULT_CONFIGURATION_FILENAME+" file. Exception: ", oe);
		    }
		return session;
	}
	
	public String generateReportWithExistingQuery (long queryId,DateRangeType dateRange ) {
		
		AdManagerSession session = null;
		
		try {
			session = connect();
		} catch (Exception e1) {
			return e1.getMessage();
		}
		
		if (session != null) {
		
			AdManagerServices adManagerServices = new AdManagerServices();
			
			ReportServiceInterface reportService =
				        adManagerServices.get(session, ReportServiceInterface.class);
	
			StatementBuilder statementBuilder =
				        new StatementBuilder()
				        .where("id = :id")
			            .orderBy("id ASC")
			            .limit(1)
			            .withBindVariableValue("id", queryId);
	
		    SavedQueryPage page=null;
			try {
				page = reportService.getSavedQueriesByStatement(statementBuilder.toStatement());
			} catch (RemoteException e) {
				LOG.error("Error al obtener SavedQueryPage:" , e);
				return "Error al obtener SavedQueryPage: "+ e.getMessage();
			}
			
			if (page!= null && page.getResults()!= null && page.getResults().length > 0 ) {
			    SavedQuery savedQuery = Iterables.getOnlyElement(Arrays.asList(page.getResults()));
			    
			    if (!savedQuery.getIsCompatibleWithApiVersion()) {
				      throw new IllegalStateException("The saved query is not compatible with this API version.");
				}
			   
			    ReportQuery reportQuery = savedQuery.getReportQuery();
			    reportQuery.setDateRangeType(dateRange);
				   
		
			    // Create report job using the saved query.
			    ReportJob reportJob = new ReportJob();
			    reportJob.setReportQuery(reportQuery);
		
			    // Run report job.
			    try {
					reportJob = reportService.runReportJob(reportJob);
				} catch (RemoteException e) {
					LOG.error("error al ejecutar el reporte",e);
					return "Error al ejecutar el reporte:" + e.getMessage();
				}
			    
			    // Create report downloader.
			    ReportDownloader reportDownloader = new ReportDownloader(reportService, reportJob.getId());
		
			    // Wait for the report to be ready.
			    try {
					reportDownloader.waitForReportReady();
				} catch (RemoteException | InterruptedException e) {
					LOG.error("Error mientras se espera por la ejecución del reporte: ",e);
					return "Error mientras se espera por la ejecución del reporte: " + e.getMessage();
				}
			    
			    // Change to your file location.
			    File file = null;
				try {
					file = File.createTempFile("adManager-report-", ".csv");
				} catch (IOException e) {
					LOG.error("Error al crear el archivo temporal: ",e);
					return "Error al crear el archivo temporal: e" + e.getMessage();
				}
		
			    LOG.debug ("Downloading report to " + file.toString());
		
			    // Download the report.
			    ReportDownloadOptions options = new ReportDownloadOptions();
			    options.setExportFormat(ExportFormat.CSV_DUMP);
			    //options.setUseGzipCompression(true);
			    URL url = null;
				try {
					url = reportDownloader.getDownloadUrl(options);
				} catch (RemoteException | MalformedURLException e) {
					LOG.error("Error al obtener la url para descargar: ",e);
					return "Error al obtener la url para descargar: " + e.getMessage();
				}
			    try {
					Resources.asByteSource(url).copyTo(Files.asByteSink(file));
				} catch (IOException e) {
					LOG.error("Error al copiar el archivo a la url",e);
					return "Error al copiar el archivo: " + e.getMessage();
				}
			    LOG.debug("finaliza el proceso de generación del archivo");
				
			    //return url.toString();
			    return file.getAbsolutePath();
			    //return "Downloading report to " + file.toString();
				
			} else {
				LOG.debug("No encuentra la query indicada");
				return "No encuentra la query indicada";
			}
		} else {
			LOG.debug("No se puede procesar el pedido. No logra obtener la session");
			return "No se puede procesar el pedido. No logra obtener la session";
		} 
			
	}
	
	public String generateQuery () throws Exception{
		AdManagerSession session = null;
		
		try {
			session = connect();
		} catch (Exception e1) {
			return e1.getMessage();
		} 
		if (session == null) {
				LOG.debug("No se puede conectar");
				return "No Se puede conectar";
		}
		AdManagerServices adManagerServices = new AdManagerServices();
			
		ReportServiceInterface reportService = adManagerServices.get(session, ReportServiceInterface.class);
	
		// Create report query.
	    ReportQuery reportQuery = new ReportQuery();
	    reportQuery.setDimensions(new Dimension[] {Dimension.DAY});
	    reportQuery.setColumns(
	        new Column[] {
	          Column.AD_EXCHANGE_AD_REQUESTS,
	          Column.AD_EXCHANGE_MATCHED_REQUESTS,
	          Column.AD_EXCHANGE_COVERAGE,
	          Column.AD_EXCHANGE_CLICKS,
	          Column.AD_EXCHANGE_ESTIMATED_REVENUE,
	          Column.AD_EXCHANGE_IMPRESSIONS,
	          Column.AD_EXCHANGE_AD_ECPM,
	          Column.AD_EXCHANGE_ACTIVE_VIEW_VIEWABLE
	        });
		    
	    reportQuery.setTimeZoneType(TimeZoneType.AD_EXCHANGE);
		    
	    reportQuery.setAdxReportCurrency("USD");

	    // Set the dynamic date range type or a custom start and end date.
	    reportQuery.setDateRangeType(DateRangeType.TODAY);
		    
	    // Create report job using the saved query.
		ReportJob reportJob = new ReportJob();
		reportJob.setReportQuery(reportQuery);
	
	    // Run report job.
	    try {
			reportJob = reportService.runReportJob(reportJob);
		} catch (RemoteException e) {
			LOG.error("error al ejecutar el reporte",e);
			return "error al ejecutar el reporte: " +e.getMessage();
		}
		    
	    // Create report downloader.
	    ReportDownloader reportDownloader = new ReportDownloader(reportService, reportJob.getId());
	
	    // Wait for the report to be ready.
	    try {
			reportDownloader.waitForReportReady();
		} catch (RemoteException | InterruptedException e) {
			LOG.error("Error mientras se espera por la ejecución del reporte: ",e);
			throw e;
			//return "Error mientras se espera la ejecución del reporte: " + e.getMessage();
		}
	    
	    // Change to your file location.
	    File file = null;
		try {
			file = File.createTempFile("inventory-report-", ".csv");
		} catch (IOException e) {
			LOG.error("Error al crear el archivo temporal: ",e);
			throw e;
			//	return "Error al crear el archivo temporal: " + e.getMessage();
		}

	    LOG.debug ("Downloading report to " + file.toString());

	    // Download the report.
	    ReportDownloadOptions options = new ReportDownloadOptions();
	    options.setExportFormat(ExportFormat.CSV_DUMP);
	    options.setUseGzipCompression(false);
	    //options.setUseGzipCompression(true);
	    URL url = null;
		try {
			url = reportDownloader.getDownloadUrl(options);
		} catch (RemoteException | MalformedURLException e) {
			LOG.error("Error al obtener la url para descargar: ",e);
			//return "Error al obtener la url para descargar: " + e.getMessage();
			throw e;
		}
	    try {
			Resources.asByteSource(url).copyTo(Files.asByteSink(file));
		} catch (IOException e) {
			LOG.error("Error al copiar el archivo a la url",e);
			//return "Error al copiar el archivo a la url: " + e.getMessage();
			throw e;
		}
	    LOG.debug("finaliza el proceso de generación del archivo");
		
	    return file.getAbsolutePath();
	} 

	

	public JSONObject processFile (String  path) {
		JSONObject listado = new JSONObject();
		
		List<String[]> rows = null;
		try {
			rows = CsvFiles.getCsvDataArray(path, true);
		} catch (IOException e) {
			listado.put("error", "no se puede conectar" + e.getMessage());
			return listado;
		}
		  
		JSONArray array =  new JSONArray ();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		for (String[] row : rows) {
		    // Additional row processing
			JSONObject element = new JSONObject();
			//element.put("DIMENSION DAY", row[0]);
			element.put("DATE", df.format(date));
			element.put("AD_EXCHANGE_AD_REQUEST", row[1]);
			element.put("AD_EXCHANGE_MATCHED_REQUEST", row[2]);
			element.put("AD_EXCHANGE_COVERAGE", row[3]);
			element.put("AD_EXCHANGE_CLICK", row[4]);
			element.put("AD_EXCHANGE_ESTIMATED_REVENUE", row[5]);
			element.put("AD_EXCHANGE_IMPRESSIONS", row[6]);
			element.put("AD_EXCHANGE_AD_ECPM", row[7]);
			element.put("AD_EXCHANGE_ACTIVE_VIEW_VIEWABLE", row[8]);
			array.add(element);
		}
		listado.put("items", array);
		return listado;
	}
}


