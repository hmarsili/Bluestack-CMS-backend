package com.tfsla.diario.newsCollector.comparators;

import java.util.*;

import org.opencms.file.CmsResource;

public class MapComparator<T> extends AscDescComparator<CmsResource> {

	static private class IntegerComparator implements Comparator<Integer>
	{
		public int compare(Integer arg0, Integer arg1) {
			return arg0.compareTo(arg1);
		}
		
	}
	
	static public IntegerComparator INTEGER_COMPARATOR = new IntegerComparator();
	
	Map<CmsResource,T> comparatorValues = new HashMap<CmsResource,T>();
	Comparator<T> comp;
	
	public MapComparator(boolean asc, Map<CmsResource,T> values, Comparator<T> comp) {
		super(asc);
		
		comparatorValues = values;
		this.comp = comp;
	}

	@Override
	protected int naturalCompare(CmsResource obj1, CmsResource obj2) {
		T value1 = comparatorValues.get(obj1);
		T value2 = comparatorValues.get(obj2);
		
		return comp.compare(value1, value2);
	}

}
