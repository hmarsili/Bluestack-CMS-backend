package com.tfsla.diario.pollsCollector;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import com.tfsla.diario.newsCollector.A_Collector;
import com.tfsla.diario.newsCollector.comparators.CompositeComparator;
import com.tfsla.diario.newsCollector.comparators.PropertyComparator;
import com.tfsla.diario.pollsCollector.order.OrderDirective;
import com.tfsla.diario.pollsCollector.order.ResultOrderManager;

public abstract class A_PollsCollector extends A_Collector {
	
	protected List<OrderDirective> supportedOrders = new ArrayList<OrderDirective>();
	
	public abstract boolean canCollect(Map<String,Object> parameters);
	
	public abstract List<String> collectPolls(Map<String,Object> parameters , CmsObject cms);
	
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
