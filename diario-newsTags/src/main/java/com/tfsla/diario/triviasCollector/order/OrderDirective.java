package com.tfsla.diario.triviasCollector.order;

import com.tfsla.diario.order.A_OrderDirective;
import com.tfsla.diario.triviasCollector.order.OrderDirective;

public class OrderDirective extends A_OrderDirective {
	
	public static final OrderDirective ORDER_BY_STATUS = new  OrderDirective("status","","status","",TYPE_STRING,true);	
	public static final OrderDirective ORDER_BY_CLOSEDATE = new  OrderDirective("close-date","","closeDate","",TYPE_LONG,true);	
	public static final OrderDirective ORDER_BY_STARTDATE = new  OrderDirective("start-date","","startDate","",TYPE_LONG,false);	
	public static final OrderDirective ORDER_BY_CATEGORY = new  OrderDirective("category","","category","",TYPE_STRING,true);	
	public static final OrderDirective ORDER_BY_MODIFICATIONDATE  = new  OrderDirective("modification-date","ultimaModificacion","lastmodified","ultimaModificacion",TYPE_STRING,false);
	
	
	static {
		availableOrders.add(ORDER_BY_STATUS);
		availableOrders.add(ORDER_BY_CATEGORY);
		availableOrders.add(ORDER_BY_CLOSEDATE);
		availableOrders.add(ORDER_BY_STARTDATE);
		availableOrders.add(ORDER_BY_MODIFICATIONDATE);
	}
	
	public static boolean isValidOrder(String name)
	{
		return availableOrders.contains(new OrderDirective(name));
	}

	public static OrderDirective getOrder(String name)
	{
		int pos = availableOrders.indexOf(new OrderDirective(name));
		if (pos>=0)
		{
			A_OrderDirective od = availableOrders.get(pos);
			return new OrderDirective(od.getName(),od.getPropertyName(), od.getLuceneName(), od.getContentName(), od.getType(), od.isAscending());
		}
		
		return null;
	}

	public OrderDirective(String name, String propertyName, String luceneName, String contentName, String type, boolean ascending)
	{
		super(name, propertyName, luceneName, contentName, type, ascending);
	}

	public OrderDirective(String name) {
		super(name);
	}

	
	
}
