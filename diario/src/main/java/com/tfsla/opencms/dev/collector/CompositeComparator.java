package com.tfsla.opencms.dev.collector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Tiene varios comparators en orden, si el primero le da 0, entonces busca por el que sigue
 * @author lgassman
 *
 * @param <T>
 */
public class CompositeComparator<T> implements Comparator<T> {

	private List<Comparator<T>> comparators =  new ArrayList<Comparator<T>>(); 
	
	public CompositeComparator() {
		super();
	}

	public CompositeComparator addComparator(Comparator<T> comparator) {
		this.comparators.add(comparator);
		return this;
	}
	
	public int compare(T arg0, T arg1) {
		for(Comparator<T> comparator: this.comparators) {
			int out = comparator.compare(arg0, arg1);
			if(out != 0) {
				return out;
			}
		}
		return 0;
	}

}
