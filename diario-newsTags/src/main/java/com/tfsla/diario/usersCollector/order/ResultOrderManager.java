package com.tfsla.diario.usersCollector.order;

import java.util.ArrayList;
import java.util.List;

import com.tfsla.diario.usersCollector.order.OrderDirective;

public class ResultOrderManager {
	public static List<OrderDirective> getOrderConfiguration(String order)
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
		}
		
		
		return orderBy;
	}

}
