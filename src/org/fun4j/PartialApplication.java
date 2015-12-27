package org.fun4j;

import java.util.Arrays;



/**
 * A PartialApplication binds arguments to some or all of a functions variables.
 * Thus a closure contains a {@link Function} and an array of Objects representing the values of the bound variables). 
 * 
 * A PartialApplication is a Function and can be executed with a call to {@link #apply(Object...)}.
 * 
 * Example: 
 * <pre>
 * // define a function with two variables 
 * (define add (lambda (x y) (+ x y))) 
 * 
 * // calling this function with only one argument results in a PartialApplication
 * (define add17 (add 17)) ==> add0(args) => args[0] + args[1][17]
 * 
 * // This closure is a function with only one free variable
 * // Thus apply add17 to 4 returns 21:
 * (add17 4) ==> 21
 * </pre>
 * 
 * @author Thomas Mahler
 *
 */
public class PartialApplication implements Function {
    
    private Function fun;
    private Object[] args;

    public PartialApplication(Function function, Object[] arguments) {
        fun = function;
        args = arguments;    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object apply(Object... boundArgs) {
        if (boundArgs == null) {
            return fun.apply(args);
        }
        else if (args == null) {
        	// apply function to provided arguments
            return fun.apply(boundArgs);
        }
        else {
            // add bound args to existing argument array
            int offset = args.length;
            Object[] arglist = Arrays.copyOf(args, offset + boundArgs.length);
            for (int i = 0; i < boundArgs.length; i++) {
                arglist[offset + i] = boundArgs[i];
            }
            // apply function to all provided arguments
            return fun.apply(arglist);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String arglist = "";
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                arglist += args[i];
                if (i < args.length - 1) {
                    arglist += ",";
                }
            }
        }
        return fun + "[" + arglist + "]";
    }

}
