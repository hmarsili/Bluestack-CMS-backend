package com.tfsla.cdnIntegration.service.cdnConnector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClientBuilder;
import com.amazonaws.services.cloudfront.model.AccessDeniedException;
import com.amazonaws.services.cloudfront.model.BatchTooLargeException;
import com.amazonaws.services.cloudfront.model.CreateInvalidationRequest;
import com.amazonaws.services.cloudfront.model.CreateInvalidationResult;
import com.amazonaws.services.cloudfront.model.GetDistributionRequest;
import com.amazonaws.services.cloudfront.model.GetDistributionResult;
import com.amazonaws.services.cloudfront.model.InconsistentQuantitiesException;
import com.amazonaws.services.cloudfront.model.InvalidArgumentException;
import com.amazonaws.services.cloudfront.model.InvalidationBatch;
import com.amazonaws.services.cloudfront.model.InvalidationSummary;
import com.amazonaws.services.cloudfront.model.ListInvalidationsRequest;
import com.amazonaws.services.cloudfront.model.ListInvalidationsResult;
import com.amazonaws.services.cloudfront.model.MissingBodyException;
import com.amazonaws.services.cloudfront.model.NoSuchDistributionException;
import com.amazonaws.services.cloudfront.model.Paths;
import com.amazonaws.services.cloudfront.model.TooManyInvalidationsInProgressException;
import com.tfsla.cdnIntegration.model.InteractionResponse;
import com.tfsla.utils.UrlLinkHelper;

public class CloudFront extends A_ContentDeliveryNetwork {
	private static final Log LOG = CmsLog.getLog(CloudFront.class);
		
	private AWSCredentials awsCredential;
	private String distribution;
	private String amzAccessKey;
	private String amzSecretKey;
	
	public CloudFront() {
		this.name = "cloudFront";
	}
	
	public I_ContentDeliveryNetwork create() {
		return new CloudFront();
	}

	public I_ContentDeliveryNetwork configure(String siteName,String publication) {
		
		isActive = CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "isActive", false);
		maxFilesToInvalidate = CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "maxFilesToInvalidate", 10);
		amzAccessKey = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "amzAccessKey", "");
		amzSecretKey = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "amzSecretKey", "");
		distribution = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "distribution", "");
		retries = CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "retries", 0);
		awsCredential = new BasicAWSCredentials(amzAccessKey, amzSecretKey);
		return this;
	}

	public InteractionResponse invalidateCacheFile(String file) {
		InteractionResponse result = null;
		
		List<String> oneFile = new ArrayList<String>();
		oneFile.add(file);
		try {
			result = invalidateCacheFiles(oneFile);
		} catch (Exception ex) {
			LOG.error("Error creando invalidation para " + file , ex);
		}
		return result;
	}

	public InteractionResponse invalidateCacheFiles(List<String> files)
			throws Exception {
		LOG.debug("Comenzando el proceso de purga de cache en Cloudfront");
		InteractionResponse result = new InteractionResponse();
		
		//proceso los nombres de los files (necesita path relativos)
		List<String> filesAux = new ArrayList<String>();
		for (String file : files) {
			if (file.contains("//")){
				try {
					file = file.substring(file.indexOf("/", file.indexOf("//") + 3));
					LOG.debug("CloudFront - Archivo a purgar: " + file);
				} catch (Exception ex) {
					//si no respeta el formato lo ejecuta de la manera convencional
					LOG.info("Cloudfront - Mantiene el mismo nombre de archivo para: " + file  );
				}
			}
			filesAux.add(file);
		}
		
		//Creo la informacion a pasar por el batch
		Paths paths = new Paths();
		paths.setItems(filesAux);
		paths.setQuantity(files.size());
		
		String callerReference =  String.valueOf(new Date().getTime()); //ver que poner
		LOG.debug("CloudFront - Creando invalidation branch - callerReference" + callerReference);
		
		//Se arma el batch
		InvalidationBatch invalidationBatch = new  InvalidationBatch(paths, callerReference);
		
		LOG.debug("CloudFront - Creando invalidation Request");
		//Se crea el request a la distribucion correspondiente
		CreateInvalidationRequest request =  new CreateInvalidationRequest(distribution, invalidationBatch);
		
		//Armo el pedido
		LOG.debug("CloudFront - Creando client Request");
		AmazonCloudFrontClient client = (AmazonCloudFrontClient) AmazonCloudFrontClientBuilder.standard().withCredentials(
				new AWSStaticCredentialsProvider(awsCredential)).build();
		try {
			LOG.debug("CloudFront - Creando client Invalidation");
			CreateInvalidationResult invalidationResult = client.createInvalidation(request);
			LOG.debug("CloudFront - status code: " + invalidationResult.getSdkHttpMetadata().getHttpStatusCode() );
			if (invalidationResult.getSdkHttpMetadata().getHttpStatusCode() == 201) {	
				result.setSuccess(true);
				result.setResponseMsg(invalidationResult.getSdkHttpMetadata().getHttpStatusCode() + "-" + invalidationResult.getLocation());
			} else {
				result.setSuccess(false);
				result.setResponseMsg(invalidationResult.getSdkHttpMetadata().getHttpStatusCode() + "-" + invalidationResult.getLocation());
		
			}
			LOG.debug("CloudFront -  status code - " + invalidationResult.getSdkHttpMetadata().getHttpStatusCode()); 
		} catch (AccessDeniedException ex) { //- Access denied.
			result.setResponseMsg("Error al realidar la ivalidacion" );
			result.addError("acces Denied" + ex.getMessage());
			result.setSuccess(false);
			LOG.error("Error creando invalidation: access denied " , ex);
		} catch (MissingBodyException ex) {
			result.addError("El request no cuenta con un cuerpo" + ex.getMessage());
			result.setSuccess(false);
			LOG.error("El request no cuenta con un cuerpo " , ex);
		} catch (InvalidArgumentException ex ) {
			result.addError("El argunmento es invalido" + ex.getMessage());
			result.setSuccess(false);
			LOG.error("El argumento es invalido" , ex);
		} catch (NoSuchDistributionException ex ) {
			result.addError("La distribucion no existe" + ex.getMessage());
			result.setSuccess(false);
			LOG.error("La distribucion no existe" , ex);
		} catch (BatchTooLargeException ex) {
			result.addError("Batch too large" + ex.getMessage());
			result.setSuccess(false);
			LOG.error("Batch too large" , ex);
		}catch (TooManyInvalidationsInProgressException ex) {
			result.addError("Se excedio el numero de invalidaciones permitidas en progreso" + ex.getMessage());
			result.setSuccess(false);
			LOG.error("Se excedio el numero de invalidaciones permitidas en progreso" , ex);
		}catch (InconsistentQuantitiesException ex) {
			result.addError("El valor de la cantidad y el tamano de los items no concuerda" + ex.getMessage());
			result.setSuccess(false);
			LOG.error("El valor de la cantidad y el tamano de los items no concuerda" , ex);
		} catch (Exception ex) {
			result.addError("Error al ejecutar createInvalidation" + ex.getMessage());
			result.setSuccess(false);
			LOG.error("Error al ejecutar createInvalidation" , ex);
		}
		return result;
	}

	public String getCachedName(CmsObject cmsObject, CmsResource resource) {
		return UrlLinkHelper.getUrlFriendlyLink(resource, cmsObject, true, true);
	}

	public int getMaxPackageSendRetries() {
		return this.getRetries();
	}

	public InteractionResponse test() throws Exception {
		InteractionResponse result = new InteractionResponse();
		
		try {
			AmazonCloudFrontClient client = new AmazonCloudFrontClient(awsCredential);
			
			GetDistributionRequest getDistributionRequest = new GetDistributionRequest(distribution);
			GetDistributionResult distributionResult = client.getDistribution( getDistributionRequest);
			
			result.setSuccess(true);
			result.setResponseMsg("Se obtuvieron los datos de la distribucion: " + distributionResult.getDistribution().getDomainName());
		} catch (AccessDeniedException ex) {
			result.setSuccess(false);
			result.addError(ex.getMessage());
			result.setResponseMsg("access denied");
		} catch (NoSuchDistributionException ex) {
			result.setSuccess(false);
			result.addError(ex.getMessage());
			result.setResponseMsg("No se encuentra la distribucion indicada");
		}
		return result;
	}

	public InteractionResponse getInvalidationStatus(String invalidationId) {
		InteractionResponse result = new InteractionResponse();
		
		LOG.debug("CloudFront - Buscando invalidation  - callerReference" + invalidationId);
			
		LOG.debug("CloudFront - Creando get invalidation Request");
		//Se crea el request a la distribucion correspondiente
		ListInvalidationsRequest request =  new ListInvalidationsRequest(distribution);
		
		//Armo el pedido
		LOG.debug("CloudFront - Creando client Request");
		AmazonCloudFrontClient client = new AmazonCloudFrontClient(awsCredential);
		try {
			LOG.debug("CloudFront - Creando client getInvalidation");
			ListInvalidationsResult invalidationResult = client.listInvalidations(request);
			LOG.debug("CloudFront - cantidad de invalidations: " + invalidationResult.getInvalidationList().getItems() );
			result.setSuccess(true);
			String resultado = "";
			for (InvalidationSummary element : invalidationResult.getInvalidationList().getItems()) {
				resultado += "invalidation: " + element.getId() + " status: " + element.getStatus() + "; "; 
			}
			result.setResponseMsg(resultado);
		} catch (Exception	ex) {
			LOG.debug("CloudFront - error buscando invalidation:" + invalidationId );
			result.addError("CloudFront - error buscando invalidation:" + invalidationId);
			result.setSuccess(false);
		}
		return result; 
		
	}
}
