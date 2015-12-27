package org.fun4j.compiler;

/**
 * This Exception is used to signal missing arguments in variable lookup during function execution.
 * e.g. ((lambda (n) (* n n)) ) will throw such an exception, as the lookup of the local variable n fails.
 * 
 * @author Thomas Mahler
 */
public class MissingArgumentException extends RuntimeException {


	/**
	 * cereal ;-)
	 */
    private static final long serialVersionUID = -5838002727234540305L;
    

    /**
     * public constructor
     */
    public MissingArgumentException(String msg) {
        super(msg);
    }
}
