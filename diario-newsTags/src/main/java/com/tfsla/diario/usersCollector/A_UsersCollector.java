package com.tfsla.diario.usersCollector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import com.tfsla.diario.newsCollector.comparators.CompositeComparator;
import com.tfsla.diario.newsCollector.A_Collector;
import com.tfsla.diario.usersCollector.order.OrderDirective;
import com.tfsla.diario.usersCollector.comparators.PropertyComparator;
import com.tfsla.diario.usersCollector.order.ResultOrderManager;

public abstract class A_UsersCollector extends A_Collector{
protected List<OrderDirective> supportedOrders = new ArrayList<OrderDirective>();
	
	public abstract boolean canCollect(Map<String,Object> parameters);
	
	public abstract List<CmsUser> collectUsers(Map<String,Object> parameters , CmsObject cms);
	
	protected List<CmsUser> sortUsers(List<CmsUser> users, Map<String, Object> parameters, CmsObject cms) {
		String order = (String)parameters.get("order");
		CompositeComparator<CmsUser> comp = new CompositeComparator<CmsUser>();
		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		for (OrderDirective od : orderby)		
			comp.addComparator(new PropertyComparator(cms,od.getPropertyName(),od.getType(),od.isAscending()));
		
		Collections.sort(users, comp);
		
		return users;
	
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
