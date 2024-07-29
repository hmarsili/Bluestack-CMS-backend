package com.tfsla.utils;

import java.util.List;

public class ListUtils {

	public static List truncate(List list, int size) {
		return (size >= list.size()) ? list : list.subList(0, size);
	}

}
