package com.tfsla.diario.newsCollector;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import com.tfsla.diario.newsCollector.comparators.CompositeComparator;
import com.tfsla.diario.newsCollector.comparators.PropertyComparator;
import com.tfsla.diario.newsCollector.order.OrderDirective;
import com.tfsla.diario.newsCollector.order.ResultOrderManager;

public abstract class A_NewsCollector extends A_Collector {
	
	
	protected List<OrderDirective> supportedOrders = new ArrayList<OrderDirective>();
	
	public abstract boolean canCollect(Map<String,Object> parameters);
	
	public abstract List<CmsResource> collectNews(Map<String,Object> parameters , CmsObject cms);
	
	protected List<CmsResource> sortNews(List<CmsResource> resources, Map<String, Object> parameters, CmsObject cms) {
		String order = (String)parameters.get("order");
		CompositeComparator<CmsResource> comp = new CompositeComparator<CmsResource>();
		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		for (OrderDirective od : orderby)		
			comp.addComparator(new PropertyComparator(cms,od.getPropertyName(),od.getType(),od.isAscending()));
		
		Collections.sort(resources, comp);
		
		return resources;
	
	}

	public boolean canOrder(String orderBy) {
		List<OrderDirective> ods = ResultOrderManager.getOrderConfiguration(orderBy);
	
		for (OrderDirective od : ods )
		{
			if (!supportedOrders.contains(od))
				return false;
		}
		return true;
	}

}
