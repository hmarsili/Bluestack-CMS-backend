package com.tfsla.opencms.util;

import com.tfsla.opencms.exceptions.ProgramException;

public class ReflectionUtils {

	public static final String refIdx = "S37vvC7TvlSEojigK4S7s2SGJIYx";
	
	public static Object newInstance(String regModuleClass) {
		try {
			return Class.forName(regModuleClass).newInstance();
		}
		catch (Exception e) {
			throw ProgramException.wrap("error la intentar crear una instancia de la clase ["
					+ regModuleClass + "]", e);
		}
	}
}