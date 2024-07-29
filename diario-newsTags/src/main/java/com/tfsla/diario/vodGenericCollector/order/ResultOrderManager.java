package com.tfsla.diario.vodGenericCollector.order;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class ResultOrderManager {

	
	protected static final Log LOG = CmsLog.getLog(ResultOrderManager.class);
	
	public static List<OrderDirective> getOrderConfiguration(String order)
	{
		return getOrderConfiguration(order, false);
	}
	
	public static List<OrderDirective> getOrderConfiguration(String order, boolean allowCustomOrders)
	{
		List<OrderDirective> orderBy = new ArrayList<OrderDirective>();
		
		if (order==null)
			return orderBy;
		
		String[] parts = order.split(",");
		for (String part : parts)
		{
			part = part.toLowerCase();
			
			boolean ascending = (part.contains(" asc"));
			boolean descending = (part.contains(" desc"));
			
			part = part.replace(" asc", "");
			part = part.replace(" desc", "");

			part = part.trim();
			
			if (OrderDirective.isValidOrder(part))
			{
				OrderDirective od = OrderDirective.getOrder(part);
				if (ascending)
					od.setAscending(true);
				if (descending)
					od.setAscending(false);
				orderBy.add(od);
			}
			//order "custom"
			else if (allowCustomOrders){
				String luceneName = "";
				String partLow = part.toLowerCase();
				
				int idxfinal = partLow.length();
				int idx = partLow.indexOf(" as ");
				if (idx!=-1 && idx <= idxfinal)
					idxfinal = idx;
				idx = partLow.indexOf(" asc");
				if (idx!=-1 && idx <= idxfinal)
					idxfinal = idx;
				idx = partLow.indexOf(" desc");
				if (idx!=-1 && idx <= idxfinal)
					idxfinal = idx;
				
				luceneName = part.substring(0, idxfinal).trim();
				
				String[] type = partLow.split(" as ");
				String dataType = OrderDirective.TYPE_STRING;
				if (type.length >1)
					dataType = OrderDirective.getDataType(type[1].toLowerCase());
				
				OrderDirective od = new OrderDirective("custom","",luceneName,"",dataType,ascending);
				orderBy.add(od);
			}
		}
		
		
		return orderBy;
	}
}
