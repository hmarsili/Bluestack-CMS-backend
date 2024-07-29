package com.tfsla.diario.usersCollector.order;

import com.tfsla.diario.order.A_OrderDirective;
import com.tfsla.diario.usersCollector.order.OrderDirective;

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
	
	public static final OrderDirective ORDER_BY_MOSTGENERALRANK  = new  OrderDirective("most-general-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTRECEIVEDVIEWS  = new  OrderDirective("most-receivedviews-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTPUBLICATEDNEWS  = new  OrderDirective("most-publicatednews-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTRECEIVEDRECOMENDATIONS  = new  OrderDirective("most-receivedrecomendations-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTRECEIVEDCOMMENTS  = new  OrderDirective("most-receivedcomments-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTMAKEDCOMMENTS  = new  OrderDirective("most-makedcomments-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTDECLINEDCOMMENTS  = new  OrderDirective("most-declinedcomments-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCOUNTVALORATIONS  = new  OrderDirective("most-countvalorations-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTAVERAGEVALORATIONS  = new  OrderDirective("most-averagevalorations-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTPOSITIVEVALORATIONS  = new  OrderDirective("most-positivevalorations-ranked","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTENEGATIVEVALORATIONS  = new  OrderDirective("most-negativevalorations-ranked","","","",TYPE_INTEGER,false);
	
	public static final OrderDirective ORDER_BY_MOSTCUSTOM1  = new  OrderDirective("most-custom1","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM2  = new  OrderDirective("most-custom2","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM3  = new  OrderDirective("most-custom3","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM4  = new  OrderDirective("most-custom4","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM5  = new  OrderDirective("most-custom5","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM6  = new  OrderDirective("most-custom6","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM7  = new  OrderDirective("most-custom7","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM8  = new  OrderDirective("most-custom8","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM9  = new  OrderDirective("most-custom9","","","",TYPE_INTEGER,false);
	public static final OrderDirective ORDER_BY_MOSTCUSTOM10  = new  OrderDirective("most-custom10","","","",TYPE_INTEGER,false);

	
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
		availableOrders.add(ORDER_BY_MOSTGENERALRANK);
		availableOrders.add(ORDER_BY_MOSTRECEIVEDVIEWS);
		availableOrders.add(ORDER_BY_MOSTPUBLICATEDNEWS);
		availableOrders.add(ORDER_BY_MOSTRECEIVEDRECOMENDATIONS);
		availableOrders.add(ORDER_BY_MOSTRECEIVEDCOMMENTS);
		availableOrders.add(ORDER_BY_MOSTMAKEDCOMMENTS);
		availableOrders.add(ORDER_BY_MOSTDECLINEDCOMMENTS);
		availableOrders.add(ORDER_BY_MOSTCOUNTVALORATIONS);
		availableOrders.add(ORDER_BY_MOSTAVERAGEVALORATIONS);
		availableOrders.add(ORDER_BY_MOSTPOSITIVEVALORATIONS);
		availableOrders.add(ORDER_BY_MOSTENEGATIVEVALORATIONS);
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
