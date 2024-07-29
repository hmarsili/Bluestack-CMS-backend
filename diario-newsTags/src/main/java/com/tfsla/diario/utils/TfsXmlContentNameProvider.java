package com.tfsla.diario.utils;

import com.tfsla.opencms.util.PropertiesProvider;

public class TfsXmlContentNameProvider {
	private static TfsXmlContentNameProvider instance = new TfsXmlContentNameProvider();
	
	private PropertiesProvider properties;
	
	private TfsXmlContentNameProvider() {
		this.properties = new PropertiesProvider(this.getClass(), "tags_names.properties");
	}
	
	public String getTagName(String key)
	{
		return this.properties.get(key);
	}

	public static TfsXmlContentNameProvider getInstance() {
		return instance;
	}
}
