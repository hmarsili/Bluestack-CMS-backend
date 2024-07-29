package com.tfsla.opencms.webusers.openauthorization.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.openid4java.message.ax.FetchResponse;
import org.w3c.dom.Document;

import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConversionException;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidFormatException;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidPathException;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidPropertyException;

import net.sf.json.JSONObject;

public abstract class UserProfileDataManager {
	
	protected ProviderConfiguration configuration;
	protected UserProfileData data;
	
	protected UserProfileDataManager(ProviderConfiguration configuration, UserProfileData data) {
		this.configuration = configuration;
		this.data = data;
	}
	
	public static UserProfileDataManager getInstance(ProviderConfiguration configuration, UserProfileData data) throws InvalidFormatException {
		if(configuration.getFormat().equals("json") && data.getProviderResponse().getClass().equals(JSONObject.class)) {
			JSONUserProfileDataManager dataManager = new JSONUserProfileDataManager(configuration, data);
			return dataManager;
		}
		if(configuration.getFormat().equals("fetchResponse") && data.getProviderResponse().getClass().equals(FetchResponse.class)) {
			FetchResponseProfileDataManager dataManager = new FetchResponseProfileDataManager(configuration, data);
			return dataManager;
		}
		if(configuration.getFormat().equals("document") && data.getProviderResponse().getClass().equals(Document.class)) {
			DocumentProfileDataManager dataManager = new DocumentProfileDataManager(configuration, data);
			return dataManager;
		}
		InvalidFormatException exception = new InvalidFormatException();
		exception.setFormat(configuration.getFormat());
		throw exception;
	}
	
	public void updateUserProfileData() throws InvalidPropertyException, InvalidConversionException, InvalidPathException, InvalidFormatException {
		
		//Loop entre campos configurados para el provider
		for(Entry<String, ProviderField> fieldInfo : this.configuration.getFields().entrySet()) {
			ProviderField field = fieldInfo.getValue();
			String path = field.getPath();
			
			//Implementado en clases heredadas
			Object objectValue = getObjectValue(this.data.getProviderResponse(), path);
			String fieldConverter = field.getConverter();
			String propertySetter = field.getProperty();
			String entryName = field.getEntryName();
			
			//Procesar en caso de que el campo sea una lista
			if(field.getType().equals("list")) {
				
				//Implementado en clases heredadas
				ArrayList<ProviderListField> list = getListValue(objectValue, field.getListIdField(), field.getListValueField());
				ArrayList<ProviderListField> listToAdd = new ArrayList<ProviderListField>();
				ProviderListField listItem = null;
				
				for(ProviderListField item : list) {
					listItem = item;
					if(fieldConverter != null && !fieldConverter.equals("")) {
						listItem.setValue(this.getConvertedValue(objectValue, field.getConverterParameter(), fieldConverter));
					}
					
					listToAdd.add(listItem);
				}
				
				data.putList(entryName, listToAdd);
				continue;
			}
			
			//Aplicar converter si corresponde
			if(fieldConverter != null && !fieldConverter.equals("")) {
				objectValue = this.getConvertedValue(objectValue, field.getConverterParameter(), fieldConverter);
			}
			
			//Setear property si corresponde
			if(propertySetter != null && !propertySetter.equals("")) {
				this.setUserProfileDataProperty(data, propertySetter, objectValue);
			}
			
			//Agregar en Info adicional si corresponde
			if(entryName != null && !entryName.equals("")) {
				data.putAdditionalInfo(entryName, objectValue);
			}
		}
	}
	
	protected abstract Object getObjectValue(Object providerResponse, String path) throws InvalidPathException;
	protected abstract ArrayList<ProviderListField> getListValue(Object objectList, String idField, String valueField);
	
	private void setUserProfileDataProperty(UserProfileData data, String propertyName, Object value) throws InvalidPropertyException {
		if(value == null) return;
		try {
			Method method = data.getClass().getMethod(propertyName, value.getClass());
			method.invoke(data, value);
		} catch (Exception e) {
			e.printStackTrace();
			InvalidPropertyException exception = new InvalidPropertyException();
			exception.setInnerException(e);
			exception.setPropertyName(propertyName);
			exception.setValue(value);
			throw exception;
		}
	}
	
	private Object getConvertedValue(Object objectValue, Object parameter, String fieldConverter) throws InvalidConversionException {
		Object objectConverted = null;
		try {
			Class<?> c = Class.forName(fieldConverter);
			IValueConverter interfaceType = (IValueConverter)c.newInstance();
			objectConverted = ((IValueConverter)interfaceType).convert(objectValue, parameter, data);
		} catch (Exception e) {
			e.printStackTrace();
			InvalidConversionException exception = new InvalidConversionException();
			exception.setInnerException(e);
			exception.setValue(objectValue);
			exception.setValueConverterType(fieldConverter);
			exception.printStackTrace();
			//throw exception;
		}
		return objectConverted;
	}
}
