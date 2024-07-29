package com.tfsla.diario.VODCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;

import com.tfsla.diario.VODCollector.order.OrderDirective;
import com.tfsla.diario.VODCollector.order.ResultOrderManager;
import com.tfsla.diario.newsCollector.A_Collector;

public abstract class A_VODCollector extends A_Collector {

protected List<OrderDirective> supportedOrders = new ArrayList<OrderDirective>();
	
	public abstract boolean canCollect(Map<String,Object> parameters);
	
	public abstract List<String> collectEvent(Map<String,Object> parameters , CmsObject cms);
	
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
