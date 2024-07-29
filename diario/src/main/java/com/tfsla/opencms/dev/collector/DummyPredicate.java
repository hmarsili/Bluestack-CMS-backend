package com.tfsla.opencms.dev.collector;

import org.apache.commons.collections.Predicate;

public class DummyPredicate extends ExtensiblePredicate {
	
	private boolean value;
	public static Predicate TRUE = new DummyPredicate(true);
	public static Predicate FALSE = new DummyPredicate(false);

	public DummyPredicate(boolean value) {
		this.value = value;
	}

	public boolean evaluate(Object object) {
		return this.value;
	};
	
}
