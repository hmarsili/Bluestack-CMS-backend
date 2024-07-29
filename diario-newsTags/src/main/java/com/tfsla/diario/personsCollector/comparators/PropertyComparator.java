package com.tfsla.diario.personsCollector.comparators;
import java.util.Comparator;

import org.opencms.file.CmsObject;

import com.tfsla.diario.terminos.model.Persons;
import com.tfsla.diario.personsCollector.order.OrderDirective;
import com.tfsla.diario.newsCollector.comparators.AscDescComparator;
public class PropertyComparator extends AscDescComparator<Persons>{

	
	private String propertyName;
	private Comparator<Persons> comparator = new compareString();

	private class compareLong implements Comparator<Persons>
	{
		public int compare(Persons obj1, Persons obj2) {
			return readPropertyAsLong(obj1).compareTo(readPropertyAsLong(obj2));
		}	
	}

	private class compareInteger implements Comparator<Persons>
	{
		public int compare(Persons obj1, Persons obj2) {
			return readPropertyAsInteger(obj1).compareTo(readPropertyAsInteger(obj2));
		}	
	}

	private class compareString implements Comparator<Persons>
	{
		public int compare(Persons obj1, Persons obj2) {
			return readPropertyAsLowerCase(obj1).compareTo(readPropertyAsLowerCase(obj2));
		}	
	}

	private class compareDate implements Comparator<Persons>
	{
		public int compare(Persons obj1, Persons obj2) {
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
	protected int naturalCompare(Persons obj1, Persons obj2) {
		return comparator.compare(obj1,obj2);
	}
	
	protected String readProperty(Persons person) {
		String value;
		try {
			
		//value = this.cmsObject.readPropertyObject(user, this.propertyName, false).getValue();
		//TODO hacer una funcion que busque el valor nombre del campo segun el property name para todos los campos del user inclusivee los adicionales y el tipo
			value=readFieldValue(person,this.propertyName);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return value != null ? value : "";
	}
	
	protected String readPropertyAsLowerCase(Persons person) {
		return this.readProperty(person).toLowerCase();
	}
	
	protected Integer readPropertyAsInteger(Persons person) {
		try {
			return Integer.parseInt(this.readProperty(person));
		}
		catch(NumberFormatException ex) {
			return Integer.MAX_VALUE;
		}
	}

	protected Long readPropertyAsLong(Persons person) {
		try {
			return Long.parseLong(this.readProperty(person));
		}
		catch(NumberFormatException ex) {
			return Long.MAX_VALUE;
		}
	}
	protected String readFieldValue(Persons person, String namefield){
		String rs=null;
		if (namefield.equals("NICKNAME")){
			rs=person.getNickname();
		}
		if (namefield.equals("NAME")){
			rs=person.getName();
		}
		if (namefield.equals("EMAIL")){
			rs=person.getEmail();
		}
		//TODO deberia ser factible de ordenar por todos los campos 
		
		
			
		
		return rs;
		
	}
	
}
