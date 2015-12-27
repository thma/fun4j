package org.fun4j.compiler;

import java.util.Arrays;
import java.util.HashMap;

import org.fun4j.NamedObject;
import org.fun4j.Function;

/**
 * abstract baseclass implementing the Function interface.
 * This class is used by the compiler to produce new function instances by implementing
 * the abstract method {@link #apply(Object...)}.
 * 
 * This class can also be used by user code to implement Functions.
 * 
 * @author Thomas Mahler
 * 
 */
public abstract class BaseFunction implements Function, NamedObject {
    public static int indent = -1;
    
    protected String fun = "fn";
    protected String pcode = "";
    protected String documentation = "";
    protected HashMap<String, Function> localEnv = null;
    
    public HashMap<String, Function> getLocalEnv() {
		return localEnv;
	}

	public void setLocalEnv(HashMap<String, Function> localEnv) {
		this.localEnv = localEnv;
	}

	public String getName() {
        return fun;
    }
    
    public String getDocumentation() {
        return documentation;
    }
    
    /**
     * if true function is a macro, that is arguments are not evaluated before invocation.
     */
    protected boolean macro = false;
    
    /**
     * if true function is lazy, i.e. non-strict, that is arguments are not evaluated before invocation.
     */
    protected boolean lazy = false;

    public BaseFunction() {
    }
    
    /**
     * create a named BaseFunction instance
     * @param name the name of the function
     */
    public BaseFunction(String name) {
        fun = name;
        pcode = name;
    }
    
    

    /**
     * The compiler writes a human readable String representation of the
     * precompiled AST into the field pcode.
     */
    public String toString() {
        return pcode;
    }

    protected static void traceOut(String fun, Object result) {
        if (indent < 0) indent = 0;
        char[] chars = new char[indent];
        Arrays.fill(chars, ' ');
        System.out.println(new String(chars) + "<-" + fun + ": " + result);
        indent--;
    }

    protected static void traceIn(String fun, Object... args) {
        String arglist = "";
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                arglist += args[i];
                if (i < args.length - 1) {
                    arglist += ",";
                }
            }
        }
        indent++;
        char[] chars = new char[indent];
        Arrays.fill(chars, ' ');
        System.out.println(new String(chars) + "->" + fun + "(" + arglist + ")");
    }

    
    public void setMacro(boolean macro) {
        this.macro = macro;
    }

    public boolean isMacro() {
        return macro;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public boolean isLazy() {
        return lazy;
    }
    
    public void defineLocal(String name, Function function) {
        if (localEnv == null) {
            localEnv = new HashMap<String, Function>();
        }
        localEnv.put(name, function);
    }
    
    public Function lookupLocal(String name) {
        return localEnv.get(name);
    }



}
