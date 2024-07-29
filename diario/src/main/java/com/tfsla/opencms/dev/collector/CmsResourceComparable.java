package com.tfsla.opencms.dev.collector;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.CmsPriorityResourceCollector;
import org.opencms.main.CmsException;

import com.tfsla.exceptions.ApplicationException;

/**
 * Un comparator más piola que el que viene con el OpenCms que no se entiende
 * @author lgassman
 *
 */
public class CmsResourceComparable implements Comparable<CmsResourceComparable> {

	private Comparable priority;
	private Comparable date;
	private int orderPriority;
	private int orderDate;
	
	
	/**
	 * 
	 * @param priority
	 * @param date
	 * @param asc si hoy es mayor que ayer
	 */
	public CmsResourceComparable(Comparable priority, Comparable date, boolean priorityAsc, boolean dateAsc) {
		super();
		this.priority = priority;
		this.date = date;
		this.orderPriority = priorityAsc ? -1 : 1;
		this.orderDate = dateAsc ? -1 : 1;
	}

	@SuppressWarnings("unchecked")
	public int compareTo(CmsResourceComparable o) {
		if(this.priority.equals(o.priority)) {
			return this.date.compareTo(o.date) * this.orderDate;
		} 
		else {
			return this.priority.compareTo(o.priority) * this.orderPriority ;
		}
	}
	
	/**
	 * @param dateAsc si hoy es mayor que ayer
	 * @param priorityAsc si 5 es mayor que 4
	 * @param resource
	 * @return
	 */
	public static CmsResourceComparable create(CmsObject cms, CmsResource resource, boolean priorityAsc,  boolean dateAsc) {
        Comparable priority;
        Comparable date;
        
        try {
        	priority = cms.readPropertyObject(resource, CmsPriorityResourceCollector.PROPERTY_PRIORITY, false).getValue();
        	if(priority == null) {
        		priority = "";
        	}
        } catch (CmsException e) {
        	throw new ApplicationException("algo anduvo mal", e);
        }

        date = resource.getDateLastModified();
        if(date.equals(0)) {
        	date = resource.getDateCreated();
        }
        return new CmsResourceComparable(priority, date, priorityAsc, dateAsc);
	}


}
