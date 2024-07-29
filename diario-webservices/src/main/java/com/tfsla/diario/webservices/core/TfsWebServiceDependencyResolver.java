package com.tfsla.diario.webservices.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.webservices.common.exceptions.InvalidTokenException;
import com.tfsla.diario.webservices.common.exceptions.TokenExpiredException;
import com.tfsla.diario.webservices.common.interfaces.*;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;

@SuppressWarnings("rawtypes")
public class TfsWebServiceDependencyResolver {
	
	@SuppressWarnings("unchecked")
	public static synchronized <T> T resolve(Class<T> type, Object... parameters) throws Throwable {
		try {
			Class<?> serviceType = getDependencies().get(type);
			ArrayList<Class<?>> constructorParams = new ArrayList<Class<?>>();
			ArrayList<Object> paramsToInstantiate = new ArrayList<Object>();
			for(int i=0; i<parameters.length; i++) {
				if(parameters[i] != null) {
					constructorParams.add(parameters[i].getClass());
					paramsToInstantiate.add(parameters[i]);
				}
			}
			Class<?>[] params = new Class<?>[constructorParams.size()];
			constructorParams.toArray(params);
			Constructor<?>[] constructors = serviceType.getConstructors();
			for(Constructor<?> constructor : constructors) {
				if(constructor.getParameterTypes().length == params.length) {
					try {
						T instance = (T)constructor.newInstance(paramsToInstantiate.toArray());
						return instance;
					} catch (InvocationTargetException e) {
						throw e.getTargetException();
					} catch(Exception ex) {
						continue;
					}
				}
			}
			throw new Exception(ExceptionMessages.ERROR_NO_CONSTRUCTOR);
		} catch(InvalidTokenException e) {
			LOG.error(e);
			throw e;
		} catch(TokenExpiredException e) {
			LOG.error(e);
			throw e;
		} catch(Exception e) {
			LOG.error(e);
			throw new Exception(ExceptionMessages.ERROR_UNABLE_GET_INSTANCE);
		}
	}

	public static synchronized void setContainer(IDependenciesContainer container) {
		TfsWebServiceDependencyResolver.container = container;
	}
	
	private static synchronized Hashtable<Class, Class> getDependencies() {
		return container.getDependencies();
	}
	
	private static IDependenciesContainer container = new TfsWebServiceDependenciesContainer();
	
	protected static final Log LOG = CmsLog.getLog(TfsWebServiceDependencyResolver.class);
}
