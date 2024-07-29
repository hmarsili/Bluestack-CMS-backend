package com.tfsla.opencms.dev.collector;

import org.apache.commons.collections.Predicate;

public abstract class ExtensiblePredicate implements Predicate {

	public ExtensiblePredicate() {
		super();
	}
	
	public ExtensiblePredicate and(Predicate predicate) {
		return new AndPredicate(this, predicate);
	}

}
