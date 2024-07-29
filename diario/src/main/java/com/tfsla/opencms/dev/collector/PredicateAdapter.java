package com.tfsla.opencms.dev.collector;

import org.apache.commons.collections.Predicate;


/**
 * Construye un predicate interno usando los parámetros que recibe en el contructor, y usa ese interno.
 * 
 * @author lgassman
 *
 */
public abstract class PredicateAdapter implements Predicate {

	private Predicate internal;
	
	public PredicateAdapter() {
		super();
	}
	
	public boolean evaluate(Object object) {
		return this.getInternalPredicate().evaluate(object);
	}

	private synchronized Predicate getInternalPredicate() {
		if(this.internal == null) {
			this.internal = this.makeInternalPredicate();
		}
		return this.internal;
	}

	protected abstract Predicate makeInternalPredicate();

}
