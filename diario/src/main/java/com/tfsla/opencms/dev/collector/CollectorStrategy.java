package com.tfsla.opencms.dev.collector;

import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

public interface CollectorStrategy {
	
	public List<CmsResource> find(CmsObject cms, String collectorName, CollectorParameter parameter);

	public boolean usePaging();
}
