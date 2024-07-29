package com.tfsla.opencms.dev.collector;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.Predicate;

/**
 * Si esta vacio, devuelve true
 * @author lgassman
 *
 */
public class AndPredicate extends ExtensiblePredicate {

	private Collection<Predicate> predicates = new ArrayList<Predicate>(); 
	
	public AndPredicate(Predicate left, Predicate right) {
		this();
		this.predicates.add(left);
		this.predicates.add(right);
	}

	public AndPredicate() {
	}

	public boolean evaluate(Object object) {
		boolean isTrue = true;
		for(Predicate predicate : this.predicates) {
			isTrue &= predicate.evaluate(object);
			if(!isTrue) {
				return false;
			}
		}
		return isTrue;
	}

	@Override
	public ExtensiblePredicate and(Predicate predicate) {
		this.predicates.add(predicate);
		return this;
	}

}
