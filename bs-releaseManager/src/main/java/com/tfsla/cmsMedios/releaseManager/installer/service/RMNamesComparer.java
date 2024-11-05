package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.util.Comparator;

public class RMNamesComparer implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		if(o1 == null || o2 == null || o1.length() < 6 || o2.length() < 6) {
			return 0;
		}
		
		String rm1 = o1.substring(o1.length()-2) + o1.substring(o1.length()-6, o1.length()-2);
		String rm2 = o2.substring(o2.length()-2) + o2.substring(o2.length()-6, o2.length()-2);
		
		return rm1.compareTo(rm2);
	}

}
