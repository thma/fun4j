package org.fun4j;


/**
 * A Predicate object that may contain any code
 * taking 0..n Objects as input and return one {@link Boolean} value as result.
 * 
 * @author Thomas Mahler
 */
public interface Predicate extends Function{

    /**
     * Apply this function to the given arguments.
     * 
     * @param args the arguments passed to the function call
     * @return the {@link Boolean} result of applying the function to its arguments. 
     * 
     */
	@Override
    Boolean apply(Object... args);
        
}
