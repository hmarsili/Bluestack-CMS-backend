package com.tfsla.templateManager.service;

import java.util.*;

import com.tfsla.templateManager.service.itemsProcessors.IncludePageProcessor;
import com.tfsla.templateManager.service.itemsProcessors.NoticiaProcessor;
import com.tfsla.templateManager.service.itemsProcessors.bannerProcessor;

public class ItemProcessorProvider {

	//TODO: Esta lista deberia cargarse desde un xml con el digester
	static private List<A_ItemProcessor> itemsType = new ArrayList<A_ItemProcessor>();
	
	static
	{
		itemsType.add((A_ItemProcessor)new NoticiaProcessor());
		itemsType.add((A_ItemProcessor)new IncludePageProcessor());
		itemsType.add((A_ItemProcessor)new bannerProcessor());
	}
	
	static public A_ItemProcessor getItemProcessor(String type)
	{
		A_ItemProcessor item = new A_ItemProcessor(type)  {

			@Override
			public A_ItemProcessor clone() {
				return null;
			}

			@Override
			public void printDOJOGlobalConf() {}

			@Override
			public void printDOJOHTML() {}
			
		};
		
		int idx = itemsType.indexOf(item);
		
		if (idx > -1)
			return itemsType.get(idx).clone();
		
		return null;
	}


}
