package com.tfsla.diario.newsCollector.comparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
