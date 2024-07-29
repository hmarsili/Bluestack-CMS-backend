package com.tfsla.opencms.dev.collector;

import org.apache.commons.collections.Predicate;
import org.opencms.file.CmsObject;

public interface PredicateFactory {

	Predicate make(CmsObject cms, String collectorName, CollectorParameter parameter);

}
