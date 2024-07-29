package com.tfsla.opencms.dev.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Modela al parametro que puede recibir un Collector
 * 
 * @author lgassman
 */
public class CollectorParameter {

	private String link;
	private String type;

	private Collection<ParameterConverter> converters;
	
	public CollectorParameter() {
	}
	
	public CollectorParameter setValue(String parameter) {
		Enumeration tokenizer = new StringTokenizer(parameter, "|");
		while (tokenizer.hasMoreElements()) {
			processToken((String) tokenizer.nextElement());
		}
		return this;
	}
	
	
	private void processToken(String token) {
		for(ParameterConverter<CollectorParameter> converter :  getConverters()) {
			 if(converter.canConvert(token, this)) {
				 converter.convert(token, this);
			 }
		 }
	}

	protected synchronized Collection<ParameterConverter> getConverters() {
		if(this.converters == null) {
			this.converters = new ArrayList<ParameterConverter>();
			this.addBasicConverters(this.converters);
			this.addConverters(this.converters);
		}
		return this.converters;
	};

	private void addBasicConverters(Collection<ParameterConverter> converters) {
		converters.add(LinkAndTypeParameterConverter.getInstance());
		converters.add(NamedParameterConverter.getInstance());
	}

	/**
	 * Agrega converters especiales para las implementaciones
	 * Para overrride
	 * @param name
	 */
	protected void addConverters(Collection<ParameterConverter> name) {
	}

	/**
	 * Es el param típico de los Collectors que vienen con Opencms
	 * @return this.getLink() + "|" + this.getType();
	 */
	public String getParam() {
		return this.getLink() + "|" + this.getType();
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setType(String type) {
		this.type  = type;
	}
	
	public String getLink() {
		return this.link;
	}
	
	public String getType() {
		return this.type;
	}
	
}

