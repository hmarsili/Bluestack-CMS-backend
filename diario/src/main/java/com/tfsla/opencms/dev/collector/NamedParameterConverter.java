package com.tfsla.opencms.dev.collector;

import org.apache.commons.beanutils.BeanUtils;

import com.tfsla.exceptions.ApplicationException;

/**
 * entiende un parametro del tipo attributeName:attributeValue y llama al setAttributeName(attributeValue)
 * 
 * @author lgassman
 */
public class NamedParameterConverter<T extends CollectorParameter> implements ParameterConverter<T> {

	private static NamedParameterConverter instance;

	public static synchronized NamedParameterConverter getInstance() {
		if (NamedParameterConverter.instance == null) {
			NamedParameterConverter.instance = new NamedParameterConverter();
		}
		return NamedParameterConverter.instance;
	}

	private NamedParameterConverter() {
		super();
	}

	public void convert(String param, T zoneParameter) {
		int index = param.indexOf(":");
		String name = param.substring(0, index);
		String value = param.substring(index + 1);
		try {
			BeanUtils.setProperty(zoneParameter, name, value);
		} catch (Exception e) {
			throw new ApplicationException("No se pudo setear el atributo" + name + " con el valor " + value
					+ " en zoneParaemter", e);
		}
	}

	public boolean canConvert(String param, T zoneParameter) {
		return param.contains(":");
	}

}
