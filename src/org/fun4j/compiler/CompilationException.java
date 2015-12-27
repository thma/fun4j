package org.fun4j.compiler;

/**
 * This Exception wraps all Exception than can occur inside the Fun4J compiler.
 * The general idea is that user code only has to catch one generic compile exception.
 * @author Thomas Mahler
 *
 */
public class CompilationException
        extends Exception {

    private static final long serialVersionUID = 1985058720747970591L;

    /**
     * public constructor
     * @param message
     * @param cause
     */
    public CompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}
