package org.fun4j.compiler;

import org.fun4j.Predicate;

/**
 * Abstract base clase used by the compiler to generate {@link Predicate} instances.
 * The compiler will also generate the apply method and the bridge method to handle the covariant 
 * signature of the {@link Predicate#apply(Object...)} method.
 */
public abstract class BasePredicate extends BaseFunction implements Predicate {
	

	/**
	 * public constructor
	 */
	public BasePredicate() {
		
	}

}
