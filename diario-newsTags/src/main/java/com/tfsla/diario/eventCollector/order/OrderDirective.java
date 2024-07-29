package com.tfsla.diario.eventCollector.order;


import com.tfsla.diario.order.A_OrderDirective;

public class OrderDirective extends A_OrderDirective {



	public static final OrderDirective ORDER_BY_CREATIONDATE  = new  OrderDirective("creation-date","","created","",TYPE_STRING,false);
	public static final OrderDirective ORDER_BY_MODIFICATIONDATE  = new  OrderDirective("modification-date","ultimaModificacion","ultimaModificacion","ultimaModificacion",TYPE_LONG,false);
	public static final OrderDirective ORDER_BY_CATEGORY = new  OrderDirective("category","","categoria","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_DATE_FROM = new OrderDirective("date-from","","fechaDesde","",TYPE_LONG,false);	
	public static final OrderDirective ORDER_BY_PRICE_VALUE = new OrderDirective("price-value","","precioValor","",TYPE_STRING,false);	
	
	
	static {
		availableOrders.add(ORDER_BY_CREATIONDATE);
		availableOrders.add(ORDER_BY_MODIFICATIONDATE);
		availableOrders.add(ORDER_BY_CATEGORY);
		availableOrders.add(ORDER_BY_DATE_FROM);
		availableOrders.add(ORDER_BY_PRICE_VALUE);
	}
	
	public OrderDirective(String name, String propertyName, String luceneName, String contentName, String type, boolean ascending)
	{
		super(name, propertyName, luceneName, contentName, type, ascending);
	}

	public OrderDirective(String name) {
		super(name);
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
}
