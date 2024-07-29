package com.tfsla.diario.usersCollector.comparators;
import java.util.Comparator;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import com.tfsla.diario.usersCollector.order.OrderDirective;
import com.tfsla.diario.newsCollector.comparators.AscDescComparator;
public class PropertyComparator extends AscDescComparator<CmsUser>{

	
	private String propertyName;
	private Comparator<CmsUser> comparator = new compareString();

	private class compareLong implements Comparator<CmsUser>
	{
		public int compare(CmsUser obj1, CmsUser obj2) {
			return readPropertyAsLong(obj1).compareTo(readPropertyAsLong(obj2));
		}	
	}

	private class compareInteger implements Comparator<CmsUser>
	{
		public int compare(CmsUser obj1, CmsUser obj2) {
			return readPropertyAsInteger(obj1).compareTo(readPropertyAsInteger(obj2));
		}	
	}

	private class compareString implements Comparator<CmsUser>
	{
		public int compare(CmsUser obj1, CmsUser obj2) {
			return readPropertyAsLowerCase(obj1).compareTo(readPropertyAsLowerCase(obj2));
		}	
	}

	private class compareDate implements Comparator<CmsUser>
	{
		public int compare(CmsUser obj1, CmsUser obj2) {
			return readPropertyAsInteger(obj1).compareTo(readPropertyAsInteger(obj2));
		}	
	}

	public PropertyComparator(CmsObject cmsObject, String propertyName, boolean asc) {
		super(asc);
		this.propertyName = propertyName;
	}

	public PropertyComparator(CmsObject cmsObject, String propertyName, String propertyType, boolean asc) {
		super(asc);
		this.propertyName = propertyName;
		if (propertyType.equals(OrderDirective.TYPE_DATE))
			comparator = new compareDate();
		else if (propertyType.equals(OrderDirective.TYPE_INTEGER))
			comparator = new compareInteger();
		else if (propertyType.equals(OrderDirective.TYPE_STRING))
			comparator = new compareString();
		else if (propertyType.equals(OrderDirective.TYPE_LONG))
			comparator = new compareLong();
	}

	@Override
	protected int naturalCompare(CmsUser obj1, CmsUser obj2) {
		return comparator.compare(obj1,obj2);
	}
	
	protected String readProperty(CmsUser user) {
		String value;
		try {
			
		//value = this.cmsObject.readPropertyObject(user, this.propertyName, false).getValue();
		//TODO hacer una funcion que busque el valor nombre del campo segun el property name para todos los campos del user inclusivee los adicionales y el tipo
			value=readFieldValue(user,this.propertyName);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return value != null ? value : "";
	}
	
	protected String readPropertyAsLowerCase(CmsUser user) {
		return this.readProperty(user).toLowerCase();
	}
	
	protected Integer readPropertyAsInteger(CmsUser user) {
		try {
			return Integer.parseInt(this.readProperty(user));
		}
		catch(NumberFormatException ex) {
			return Integer.MAX_VALUE;
		}
	}

	protected Long readPropertyAsLong(CmsUser user) {
		try {
			return Long.parseLong(this.readProperty(user));
		}
		catch(NumberFormatException ex) {
			return Long.MAX_VALUE;
		}
	}
	protected String readFieldValue(CmsUser usr, String namefield){
		String rs=null;
		if (namefield.equals("OU")){
			rs=usr.getOuFqn();
		}
		if (namefield.equals("NAME")){
			rs=usr.getName();
		}
		if (namefield.equals("EMAIL")){
			rs=usr.getEmail();
		}
		//TODO deberia ser factible de ordenar por todos los campos incluso los adicionales(ver si agregarlos como valid order en OrderDirective -Verificar el tipo de  dato adicional
		
		if(usr.getAdditionalInfo().containsKey(namefield)){
			
			rs=usr.getAdditionalInfo(namefield).toString();
		}
			
		
		return rs;
		
	}
	
}
