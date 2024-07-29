package com.tfsla.diario.playlistCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;

import com.tfsla.diario.newsCollector.A_Collector;
import com.tfsla.diario.playlistCollector.order.OrderDirective;
import com.tfsla.diario.playlistCollector.order.ResultOrderManager;

public abstract class A_playlistCollector extends A_Collector {
	
protected List<OrderDirective> supportedOrders = new ArrayList<OrderDirective>();
	
	public abstract boolean canCollect(Map<String,Object> parameters);
	
	public abstract List<String> collectPlaylist(Map<String,Object> parameters , CmsObject cms);
	
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
