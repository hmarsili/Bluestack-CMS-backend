package com.tfsla.cdnIntegration.service.cdnConnector;

import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import com.tfsla.cdnIntegration.model.InteractionResponse;

public interface I_ContentDeliveryNetwork {

	public String getName();
	public I_ContentDeliveryNetwork create();
	public I_ContentDeliveryNetwork configure(String siteName, String publication);
	public boolean isActive();
	
	public InteractionResponse invalidateCacheFile(String file);
	public InteractionResponse invalidateCacheFiles(List<String> files) throws Exception;

	public String getCachedName(CmsObject cmsObject, CmsResource resource);
	public int getMaxFilesToInvalidate();
	public int getMaxPackageSendRetries();
	
	public InteractionResponse test() throws Exception;
	public InteractionResponse getInvalidationStatus(String invalidationId);
	
}
