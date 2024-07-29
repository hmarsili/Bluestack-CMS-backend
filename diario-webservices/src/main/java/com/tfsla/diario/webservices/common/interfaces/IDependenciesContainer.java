package com.tfsla.diario.webservices.common.interfaces;

import java.util.Hashtable;

import com.tfsla.diario.webservices.core.TfsWebServiceDependencyResolver;

/**
 * Represents a container to be used into the Dependencies Resolver
 * @see TfsWebServiceDependencyResolver 
 */
public interface IDependenciesContainer {
	@SuppressWarnings("rawtypes")
	Hashtable<Class, Class> getDependencies();
}
