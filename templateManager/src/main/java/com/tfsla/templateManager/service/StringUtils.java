package com.tfsla.templateManager.service;

import java.util.*;

public class StringUtils {
	static public String join(List<String> list, String conjunction)
	{
	   StringBuilder sb = new StringBuilder();
	   boolean first = true;
	   for (String item : list)
	   {
	      if (first)
	         first = false;
	      else
	         sb.append(conjunction);
	      sb.append(item);
	   }
	   return sb.toString();
	}
}
