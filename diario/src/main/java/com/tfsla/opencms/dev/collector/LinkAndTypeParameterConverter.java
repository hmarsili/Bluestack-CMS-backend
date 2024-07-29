package com.tfsla.opencms.dev.collector;

public class LinkAndTypeParameterConverter implements ParameterConverter<CollectorParameter> {

	private static LinkAndTypeParameterConverter instance;

	public static synchronized LinkAndTypeParameterConverter getInstance() {
		if (LinkAndTypeParameterConverter.instance == null) {
			LinkAndTypeParameterConverter.instance = new LinkAndTypeParameterConverter();
		}
		return LinkAndTypeParameterConverter.instance;
	}

	private LinkAndTypeParameterConverter() {
		super();
	}
	
	
	public void convert(String param, CollectorParameter parameter) {
		if(parameter.getLink() == null) {
			parameter.setLink(param);
		}
		else {
			parameter.setType(param);
		}
	}

	public boolean canConvert(String param, CollectorParameter parameter) {
		return (parameter.getLink() == null) || (parameter.getType() == null);
	}


	
	

}
