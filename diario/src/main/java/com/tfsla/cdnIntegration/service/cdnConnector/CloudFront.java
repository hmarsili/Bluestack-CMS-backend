package com.tfsla.cdnIntegration.service.cdnConnector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.AccessDeniedException;
import software.amazon.awssdk.services.cloudfront.model.BatchTooLargeException;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationRequest;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationResponse;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionRequest;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.InconsistentQuantitiesException;
import software.amazon.awssdk.services.cloudfront.model.InvalidArgumentException;
import software.amazon.awssdk.services.cloudfront.model.InvalidationBatch;
import software.amazon.awssdk.services.cloudfront.model.InvalidationSummary;
import software.amazon.awssdk.services.cloudfront.model.ListInvalidationsRequest;
import software.amazon.awssdk.services.cloudfront.model.ListInvalidationsResponse;
import software.amazon.awssdk.services.cloudfront.model.MissingBodyException;
import software.amazon.awssdk.services.cloudfront.model.NoSuchDistributionException;
import software.amazon.awssdk.services.cloudfront.model.Paths;
import software.amazon.awssdk.services.cloudfront.model.TooManyInvalidationsInProgressException;

import com.tfsla.cdnIntegration.model.InteractionResponse;
import com.tfsla.utils.UrlLinkHelper;

public class CloudFront extends A_ContentDeliveryNetwork {
	private static final Log LOG = CmsLog.getLog(CloudFront.class);
		
	private AwsBasicCredentials awsCredential;
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
		awsCredential = AwsBasicCredentials.create(amzAccessKey, amzSecretKey);
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
		Paths paths = Paths.builder()
				.items(filesAux)
				.quantity(files.size())
				.build();
		
		String callerReference =  String.valueOf(new Date().getTime()); //ver que poner
		LOG.debug("CloudFront - Creando invalidation branch - callerReference" + callerReference);
		
		//Se arma el batch
		InvalidationBatch invalidationBatch = InvalidationBatch.builder()
				.paths(paths)
				.callerReference(callerReference)
				.build();
		
		LOG.debug("CloudFront - Creando invalidation Request");
		//Se crea el request a la distribucion correspondiente
		CreateInvalidationRequest request =  CreateInvalidationRequest.builder()
				.distributionId(distribution)
				.invalidationBatch(invalidationBatch)
				.build();
		
		//Armo el pedido
		LOG.debug("CloudFront - Creando client Request");
		CloudFrontClient client = CloudFrontClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCredential))
				.build();
		
		try {
			LOG.debug("CloudFront - Creando client Invalidation");
			CreateInvalidationResponse invalidationResult = client.createInvalidation(request);
			LOG.debug("CloudFront - status code: " + invalidationResult.sdkHttpResponse().statusCode() );
			if (invalidationResult.sdkHttpResponse().statusCode() == 201) {	
				result.setSuccess(true);
				result.setResponseMsg(invalidationResult.sdkHttpResponse().statusCode() + "-" + invalidationResult.location());
			} else {
				result.setSuccess(false);
				result.setResponseMsg(invalidationResult.sdkHttpResponse().statusCode()+ "-" + invalidationResult.location());
		
			}
			LOG.debug("CloudFront -  status code - " + invalidationResult.sdkHttpResponse().statusCode()); 
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
			CloudFrontClient client = CloudFrontClient.builder()
					.credentialsProvider(StaticCredentialsProvider.create(awsCredential))
					.build();
			
			
			GetDistributionRequest getDistributionRequest = GetDistributionRequest.builder()
					.id(distribution)
					.build();
			
			GetDistributionResponse distributionResult = client.getDistribution( getDistributionRequest);
			
			result.setSuccess(true);
			result.setResponseMsg("Se obtuvieron los datos de la distribucion: " + distributionResult.distribution().domainName());
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
		ListInvalidationsRequest request =  ListInvalidationsRequest.builder()
				.distributionId(distribution)
				.build();
		
		//Armo el pedido
		LOG.debug("CloudFront - Creando client Request");
		CloudFrontClient client = CloudFrontClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCredential))
				.build();
		
		try {
			LOG.debug("CloudFront - Creando client getInvalidation");
			ListInvalidationsResponse invalidationResult = client.listInvalidations(request);
			LOG.debug("CloudFront - cantidad de invalidations: " + invalidationResult.invalidationList().items() );
			result.setSuccess(true);
			String resultado = "";
			for (InvalidationSummary element : invalidationResult.invalidationList().items()) {
				resultado += "invalidation: " + element.id() + " status: " + element.status() + "; "; 
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
