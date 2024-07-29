package com.tfsla.opencms.dev.collector;

import java.util.Comparator;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

/** 
 * @deprecated use CmsResourceComparatorBuilder
 * @author lgassman
 */
public class CmsResourceComparator implements Comparator<CmsResource> {

	
	private CmsObject cms;
	private boolean priotiryAsc;
	private boolean dateAsc;

	/**
	 * 
	 * @param object
	 * @param dateAsc si hoy es mayor que ayer
	 * @param priorityAsc si 5 es mayor que 4
	 */
	public CmsResourceComparator(CmsObject object, boolean priorityAsc, boolean dateAsc) {
		super();
		this.cms = object;
		this.priotiryAsc = priorityAsc;
		this.dateAsc = dateAsc;
	}

	public int compare(CmsResource o1, CmsResource o2) {
		return CmsResourceComparable.create(this.cms, o1, this.priotiryAsc, this.dateAsc).compareTo(CmsResourceComparable.create(this.cms, o2, this.priotiryAsc, this.dateAsc));
	}

}
