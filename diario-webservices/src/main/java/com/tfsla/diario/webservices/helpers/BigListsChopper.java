package com.tfsla.diario.webservices.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a method to chop big lists into smaller lists
 */
public class BigListsChopper {
	
	/**
	 * Chops a big list into smaller lists
	 * @param list the list to be chopped
	 * @param L the size desired for each sub list
	 * @return a List of Lists with the desired size
	 */
	public static <T> List<List<T>> chopped(List<T> list, final int L) {
	    List<List<T>> parts = new ArrayList<List<T>>();
	    final int N = list.size();
	    for (int i = 0; i < N; i += L) {
	        parts.add(new ArrayList<T>(
	            list.subList(i, Math.min(N, i + L)))
	        );
	    }
	    return parts;
	}

}
