package org.fun4j;


/**
 * A Function object that may contain any code
 * taking 0..n Objects as input and return one Object as result
 * 
 * @author Thomas Mahler
 */
public interface Function {

    /**
     * Apply this function to the given arguments.
     * 
     * @param args the arguments passed to the function call
     * @return the result of applying the function to its arguments
     * 
     */
    Object apply(Object... args);
        
}
