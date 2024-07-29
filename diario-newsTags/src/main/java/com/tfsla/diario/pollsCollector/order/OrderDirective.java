package com.tfsla.diario.pollsCollector.order;

import com.tfsla.diario.order.A_OrderDirective;
import com.tfsla.diario.pollsCollector.order.OrderDirective;

public class OrderDirective extends A_OrderDirective {
	
	public static final OrderDirective ORDER_BY_GROUP = new  OrderDirective("group","GRUPO","grupo","",TYPE_STRING,true);	
	public static final OrderDirective ORDER_BY_STATUS = new  OrderDirective("satus","ESTADO_PUBLICACION","estado","",TYPE_STRING,true);	
	public static final OrderDirective ORDER_BY_CATEGORY = new  OrderDirective("category","","categorias","",TYPE_STRING,true);	

	public static final OrderDirective ORDER_BY_CLOSEDATE = new  OrderDirective("close-date","FECHA_CIERRE","fechaCierre","",TYPE_LONG,false);	
	public static final OrderDirective ORDER_BY_CREATIONDATE = new  OrderDirective("creation-date","","fechaCreacion","",TYPE_LONG,false);	
	public static final OrderDirective ORDER_BY_PUBLICATIONDATE = new  OrderDirective("publication-date","","fechaPublicacion","",TYPE_LONG,false);	
	public static final OrderDirective ORDER_BY_EXPIRATIONDATE = new  OrderDirective("expiration-date","","fechaExpiracion","",TYPE_LONG,false);	

	
	
	static {
		availableOrders.add(ORDER_BY_GROUP);
		availableOrders.add(ORDER_BY_STATUS);
		availableOrders.add(ORDER_BY_CATEGORY);
		availableOrders.add(ORDER_BY_CLOSEDATE);
		availableOrders.add(ORDER_BY_CREATIONDATE);
		availableOrders.add(ORDER_BY_PUBLICATIONDATE);
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
