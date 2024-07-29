package com.tfsla.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionFactory<E> {

	public static <E> Collection<E> createCollection() {
		return new ArrayList<E>();
	}

	public static <E> List<E> createList() {
		return new ArrayList<E>();
	}

	public static <E> List<E> createList(Collection<E> collection) {
		return new ArrayList<E>(collection);
	}

	public static <E> Set<E> createSetList() {
		return new LinkedHashSet<E>();
	}

	public static <K, V> Map<K, V> createMap() {
		return new HashMap<K, V>();
	}

	public static <E> List<E> createList(E[] elements) {
		return Arrays.asList(elements);
	}
}
