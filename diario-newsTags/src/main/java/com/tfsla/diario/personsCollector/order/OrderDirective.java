package com.tfsla.diario.personsCollector.order;

import com.tfsla.diario.order.A_OrderDirective;


public class OrderDirective extends A_OrderDirective{
	//public static final OrderDirective ORDER_BY_GROUP = new  OrderDirective("group","GRUPO","","",TYPE_STRING,true);	
	public static final OrderDirective ORDER_BY_OU = new  OrderDirective("ou","USER_OU","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_NAME = new  OrderDirective("name","USER_NAME","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_EMAIL = new  OrderDirective("email","USER_EMAIL","","",TYPE_STRING,true);
	//	nickname,birthdate,dni,gender,country,state,city,address,telephone,cellphone
	public static final OrderDirective ORDER_BY_ID = new  OrderDirective("id","USER_ID","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_FIRSTNAME = new  OrderDirective("firstname","USER_FIRSTNAME","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_LASTNAME = new  OrderDirective("lastname","USER_LASTNAME","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_LASTLOGIN = new  OrderDirective("lastlogin","USER_LASTLOGIN","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_DATECREATED = new  OrderDirective("datecreated","USER_DATECREATED","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_NICKNAME = new  OrderDirective("nickname","APODO","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_DNI = new  OrderDirective("dni","USER_DNI","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_GENDER = new  OrderDirective("gender","USER_GENDER","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_ZIPCODE = new  OrderDirective("zipcode","USER_ZIPCODE","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_BIRTHDATE = new  OrderDirective("birthdate","USER_BIRTHDATE","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_COUNTRY = new  OrderDirective("country","USER_COUNTRY","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_STATE = new  OrderDirective("state","USER_STATE","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_CITY = new  OrderDirective("city","USER_TOWN","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_ADDRESS = new  OrderDirective("address","USER_ADDRESS","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_TELEPHONE = new  OrderDirective("telephone","USER_TELEPHONE","","",TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_CELLPHONE = new  OrderDirective("cellphone","USER_CELLPHONE","","",TYPE_STRING,true);
	
	
	
	static {
		//availableOrders.add(ORDER_BY_GROUP);
		availableOrders.add(ORDER_BY_NAME);
		availableOrders.add(ORDER_BY_EMAIL);
		availableOrders.add(ORDER_BY_OU);
		availableOrders.add(ORDER_BY_ID);
		availableOrders.add(ORDER_BY_FIRSTNAME);
		availableOrders.add(ORDER_BY_LASTNAME);
		availableOrders.add(ORDER_BY_LASTLOGIN);
		availableOrders.add(ORDER_BY_DATECREATED);
		availableOrders.add(ORDER_BY_NICKNAME);
		availableOrders.add(ORDER_BY_DNI);
		availableOrders.add(ORDER_BY_GENDER);
		availableOrders.add(ORDER_BY_ZIPCODE);
		availableOrders.add(ORDER_BY_BIRTHDATE);
		availableOrders.add(ORDER_BY_COUNTRY);
		availableOrders.add(ORDER_BY_STATE);
		availableOrders.add(ORDER_BY_CITY);
		availableOrders.add(ORDER_BY_ADDRESS);
		availableOrders.add(ORDER_BY_TELEPHONE);
		availableOrders.add(ORDER_BY_CELLPHONE);
		
		
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
