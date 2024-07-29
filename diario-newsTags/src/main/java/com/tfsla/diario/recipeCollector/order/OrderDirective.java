package com.tfsla.diario.recipeCollector.order;

import com.tfsla.diario.order.A_OrderDirective;

public class OrderDirective extends A_OrderDirective {

	public static final OrderDirective ORDER_BY_CREATIONDATE  = new  OrderDirective("creation-date","","created","",TYPE_STRING,false);
	public static final OrderDirective ORDER_BY_MODIFICATIONDATE  = new  OrderDirective("modification-date","ultimaModificacion","lastmodified","ultimaModificacion",TYPE_STRING,false);
	public static final OrderDirective ORDER_BY_CATEGORY = new  OrderDirective("category","","categoria","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_ORDER_VALUE = new OrderDirective("order","","orderVOD","",TYPE_LONG,false);	
	public static final OrderDirective ORDER_BY_ZONE  = new  OrderDirective("zone","homezone","homezone","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_PRIORITY_ZONE = new  OrderDirective("priority-zone","homepriority","homepriority","",TYPE_INTEGER,true);	
	
	public static final OrderDirective ORDER_BY_MOSTREAD  = new  OrderDirective("most-read","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCOMMENTED  = new  OrderDirective("most-commented","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTRECOMMENDED  = new  OrderDirective("most-recommended","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTVALUED = new  OrderDirective("most-valued","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTPOSITIVEVALUED  = new  OrderDirective("most-positive-evaluations","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTNEGATIVEVALUED = new  OrderDirective("most-negative-evaluations","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCOUNTERVALUED = new  OrderDirective("most-number-of-evaluations","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_TAG  = new  OrderDirective("tag","","keywords","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_AUTHOR  = new  OrderDirective("author","","internalUser","",TYPE_STRING,true);

	public static final OrderDirective ORDER_BY_EDITION  = new  OrderDirective("edition","","edicion","",TYPE_INTEGER,true);
	public static final OrderDirective ORDER_BY_PUBLICATION = new  OrderDirective("publication","","tipoEdicion","",TYPE_INTEGER,true);

	public static final OrderDirective ORDER_BY_MOSTGENERALRANK  = new  OrderDirective("most-general-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM1  = new  OrderDirective("most-custom1","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM2  = new  OrderDirective("most-custom2","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM3  = new  OrderDirective("most-custom3","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM4  = new  OrderDirective("most-custom4","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM5  = new  OrderDirective("most-custom5","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM6  = new  OrderDirective("most-custom6","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM7  = new  OrderDirective("most-custom7","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM8  = new  OrderDirective("most-custom8","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM9  = new  OrderDirective("most-custom9","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM10  = new  OrderDirective("most-custom9","","","",TYPE_INTEGER,false);

	
	static {
		availableOrders.add(ORDER_BY_CREATIONDATE);
		availableOrders.add(ORDER_BY_MODIFICATIONDATE);
		availableOrders.add(ORDER_BY_CATEGORY);
		availableOrders.add(ORDER_BY_ORDER_VALUE);
		availableOrders.add(ORDER_BY_ZONE);
		availableOrders.add(ORDER_BY_PRIORITY_ZONE);
		
		availableOrders.add(ORDER_BY_MOSTREAD);
		availableOrders.add(ORDER_BY_MOSTCOMMENTED);
		availableOrders.add(ORDER_BY_MOSTRECOMMENDED);
		availableOrders.add(ORDER_BY_MOSTVALUED);
		availableOrders.add(ORDER_BY_MOSTPOSITIVEVALUED);
		availableOrders.add(ORDER_BY_MOSTNEGATIVEVALUED);
		availableOrders.add(ORDER_BY_MOSTCOUNTERVALUED);
		availableOrders.add(ORDER_BY_ZONE);
		availableOrders.add(ORDER_BY_AUTHOR);
		availableOrders.add(ORDER_BY_CATEGORY);
		availableOrders.add(ORDER_BY_TAG);
		
		availableOrders.add(ORDER_BY_MOSTGENERALRANK);
		availableOrders.add(ORDER_BY_MOSTCUSTOM1);
		availableOrders.add(ORDER_BY_MOSTCUSTOM2);
		availableOrders.add(ORDER_BY_MOSTCUSTOM3);
		availableOrders.add(ORDER_BY_MOSTCUSTOM4);
		availableOrders.add(ORDER_BY_MOSTCUSTOM5);
		availableOrders.add(ORDER_BY_MOSTCUSTOM6);
		availableOrders.add(ORDER_BY_MOSTCUSTOM7);
		availableOrders.add(ORDER_BY_MOSTCUSTOM8);
		availableOrders.add(ORDER_BY_MOSTCUSTOM9);
		availableOrders.add(ORDER_BY_MOSTCUSTOM10);
	
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
