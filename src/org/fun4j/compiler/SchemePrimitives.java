package org.fun4j.compiler;

import static org.fun4j.compiler.RunTime.stringTrim;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

import org.fun4j.Cons;
import org.fun4j.Function;
import org.fun4j.PartialApplication;

/**
 * This class contains static definitions of {@link BaseFunction} instances. These Function instances represent
 * primitive operations as defined by the Scheme language.
 * 
 * During startup of the fun4j {@link RunTime} environment these instances are registered to the global Environment.
 * 
 * These Function instances can of course also be used within Java code by calling <code>fun.apply(...)</code>.
 * 
 * @author Thomas Mahler
 */
public class SchemePrimitives {

    /**
     * + primitive
     */
    public static BaseFunction addOp = new BaseFunction("+") {
        @SuppressWarnings("unchecked")
        @Override
        public Object apply(Object... args) {
            if (args == null || args.length < 2) {
                return new PartialApplication(this, args);
            }
            else {
                Number acc = (Number) args[0];
                for (int i = 1; i < args.length; i++) {
                    Class<Number> xclass = (Class<Number>) acc.getClass();
                    Class<Number> yclass = (Class<Number>) args[i].getClass();

                    if (xclass == yclass) {
                        if (Integer.class.isAssignableFrom(xclass)) {
                            Integer x = (Integer) acc;
                            Integer y = (Integer) args[i];
                            acc = x + y;
                        }
                        else if (BigInteger.class.isAssignableFrom(xclass)) {
                            BigInteger x = (BigInteger) acc;
                            BigInteger y = (BigInteger) args[i];
                            acc = x.add(y);
                        }
                        else if (BigDecimal.class.isAssignableFrom(xclass)) {
                            BigDecimal x = (BigDecimal) acc;
                            BigDecimal y = (BigDecimal) args[i];
                            acc = x.add(y);
                        }
                        else if (Double.class.isAssignableFrom(xclass)) {
                            Double x = (Double) acc;
                            Double y = (Double) args[i];
                            acc = x + y;
                        }
                        else {
                            throw new UnsupportedOperationException(fun + ": unknown type: "
                                    + xclass.getCanonicalName());
                        }
                    }
                    else {
                        if (acc instanceof BigDecimal || args[i] instanceof BigDecimal) {
                            BigDecimal x = BigDecimal.valueOf(acc.doubleValue());
                            BigDecimal y = BigDecimal.valueOf(((Number) args[i]).doubleValue());
                            acc = x.add(y);
                        }
                        else {
                            BigInteger x = BigInteger.valueOf(acc.longValue());
                            BigInteger y = BigInteger.valueOf(((Number) args[i]).longValue());
                            acc = x.add(y);
                        }
                    }
                }
                return acc;
            }
        }
    };

    /**
     * apply primitive
     */
    public static BaseFunction applyOp = new BaseFunction("apply") {
        @Override
        public Object apply(Object... args) {
            Object fun = args[0];
            Cons arguments = (Cons) args[1];
            System.out.println("apply: " + fun + " " + arguments);
            if (fun instanceof Function) {
                return ((Function) fun).apply(arguments.asObjectArray());
            }
            else if (fun instanceof String) {
                fun = RunTime.lookup((String) fun);
                args[0] = fun;
                return apply(arguments.asObjectArray());
            }
            else {
                throw new RuntimeException("apply: that't now a function:" + fun);
            }
        }
    };

    /**
     * car primitive
     */
    public static BaseFunction carOp = new BaseFunction("car") {
        @Override
        public Object apply(Object... args) {
            try {
                Cons cons = (Cons) args[0];
                return cons.getHd();
            }
            catch (ClassCastException e) {
                System.err.println("car: " + args[0] + " is not a cons object");
                throw e;
            }
        }
    };

    /**
     * catch primitive
     */
    public static BaseFunction catchOp = new BaseFunction("catch") {
        @SuppressWarnings("unchecked")
        @Override
        public Object apply(Object... args) {
            if (args != null) {
                Object expr = args[0];
                Object result = null;
                String exClassName = null;
                Class<Throwable> exClass = Throwable.class;
                if (args.length > 1) {
                    exClassName = "" + args[1];
                    try {
                        exClass = (Class<Throwable>) Class.forName(exClassName);
                    }
                    catch (ClassNotFoundException e) {
                        return e;
                    }
                }

                try {
                    result = RunTime.eval(expr);
                    return result;
                }
                catch (Throwable actEx) {
                    if (exClass.isInstance(actEx)) {
                        return actEx;
                    }
                    else {
                        throw new RuntimeException(actEx);
                    }
                }
            }
            else {
                return null;
            }
        }
    };

    /**
     * cdr primitive
     */
    public static BaseFunction cdrOp = new BaseFunction("cdr") {
        @Override
        public Object apply(Object... args) {
            try {
                Cons cons = (Cons) args[0];
                return cons.getTl();
            }
            catch (ClassCastException e) {
                System.err.println("cdr: " + args[0] + " is not a cons object");
                throw e;
            }
        }
    };

    /**
     * char? primitive
     */
    public static BaseFunction charpOp = new BaseFunction("char?") {
        @Override
        public Object apply(Object... args) {
            Object x = args[0];
            return (x instanceof Character);
        }
    };

    /**
     * cons primitive
     */
    public static BaseFunction consOp = new BaseFunction("cons") {
        @Override
        public Object apply(Object... args) {
            if (args == null || args.length < 2) {
                return new PartialApplication(this, args);
            }
            else {
                return new Cons(args[0], args[1]);
            }
        }
    };

    /**
     * div primitive
     */
    public static BaseFunction divOp = new BaseFunction("/") {
        @SuppressWarnings("unchecked")
        @Override
        public Object apply(Object... args) {
            Class<Number> xclass = (Class<Number>) args[0].getClass();
            Class<Number> yclass = (Class<Number>) args[1].getClass();

            if (xclass == yclass) {
                if (args[0] instanceof Integer) {
                    Integer x = (Integer) args[0];
                    Integer y = (Integer) args[1];
                    return x / y;
                }
                if (args[0] instanceof BigInteger) {
                    BigInteger x = (BigInteger) args[0];
                    BigInteger y = (BigInteger) args[1];
                    return x.divide(y);
                }
                if (args[0] instanceof BigDecimal) {
                    BigDecimal x = (BigDecimal) args[0];
                    BigDecimal y = (BigDecimal) args[1];
                    return x.divide(y);
                }
                else {
                    throw new UnsupportedOperationException(fun + ": unknown type: " + xclass.getCanonicalName());
                }
            }
            else {
                if (args[0] instanceof BigDecimal || args[1] instanceof BigDecimal) {
                    BigDecimal x = BigDecimal.valueOf(((Number) args[0]).doubleValue());
                    BigDecimal y = BigDecimal.valueOf(((Number) args[1]).doubleValue());
                    return x.divide(y);
                }
                else {
                    BigInteger x = BigInteger.valueOf(((Number) args[0]).longValue());
                    BigInteger y = BigInteger.valueOf(((Number) args[1]).longValue());
                    return x.divide(y);
                }
                // throw new UnsupportedOperationException(fun + ": can't handle types: " + xclass.getCanonicalName() +
                // ", " + yclass.getCanonicalName());
            }
        }
    };

    /**
     * eq? primitive
     */
    public static BaseFunction eqOp = new BaseFunction("eq?") {
        @Override
        public Object apply(Object... args) {
            if (args == null || args.length < 2) {
                return new PartialApplication(this, args);
            }
            else {
                return eq(args[0], args[1]);
            }
        }

        private Boolean eq(Object x, Object y) {
            if (x == null) {
                return Boolean.valueOf(y == null);
            }
            if (y == null) {
                return Boolean.valueOf(x == null);
            }
            else if (x instanceof Cons && y instanceof Cons) {
                return (x == y);
            }
            else {
                return Boolean.valueOf(x.equals(y));
            }
        }
    };

    /**
     * eqv? primitive
     */
    public static BaseFunction eqvOp = new BaseFunction("eqv?") {
        @Override
        public Object apply(Object... args) {
            if (args == null || args.length < 2) {
                return new PartialApplication(this, args);
            }
            else {
                return eq(args[0], args[1]);
            }
        }

        private Boolean eq(Object x, Object y) {
            if (x == null) {
                return Boolean.valueOf(y == null);
            }
            if (y == null) {
                return Boolean.valueOf(x == null);
            }
            else if (x instanceof Cons && y instanceof Cons) {
                return Boolean.valueOf(eqCons((Cons) x, (Cons) y));
            }
            else {
                return Boolean.valueOf(x.equals(y));
            }
        }

        private boolean eqCons(Cons x, Cons y) {
            if (x == null) {
                return (y == null);
            }
            else if (y == null) {
                return (x == null);
            }
            else {
                return (eq(x.getHd(), y.getHd()) && eq(x.getTl(), y.getTl()));
            }
        }
    };

    /**
     * exact? primitive
     */
    public static BaseFunction exactOp = new BaseFunction("exact?") {
        @Override
        public Boolean apply(Object... args) {
            Object x = args[0];
            return (x instanceof Integer || x instanceof BigInteger || isExact(x));
        }

        private boolean isExact(Object x) {
            if (x instanceof BigDecimal) {
                try {
                    // if a Bigdecimal has a fractional part of zero the following calls succeeds
                    ((BigDecimal) x).toBigIntegerExact();
                    return true;
                }
                // if the fractional part is not zero an ArithmeticException is thrown:
                catch (ArithmeticException ae) {
                    return false;
                }
            }
            return false;
        }
    };
    
    /**
     * gc primitive
     */
    public static BaseFunction gcOp = new BaseFunction("gc") {
        @Override
        public Object apply(Object... args) {
           System.out.println("free before gc: " + Runtime.getRuntime().freeMemory()); 
           Runtime.getRuntime().gc();
           Long result = Runtime.getRuntime().freeMemory();
           System.out.println("free after gc: " + result); 
           return result;
        }  
    };

    /**
     * integer? primitive
     */
    public static BaseFunction integerOp = new BaseFunction("integer?") {
        @Override
        public Boolean apply(Object... args) {
            Object x = args[0];
            return (x instanceof Integer || x instanceof BigInteger);       
        }
    };
    
    /**
     * interaction-environment primitive
     */
    public static BaseFunction interactionEnvOp = new BaseFunction("interaction-environment") {
        @Override
        public Object apply(Object... args) {
           Set<Entry<String, Object>> entries = RunTime.getGlobals().entrySet();
           Cons result = null;       
           // build up assoc list for all object defined in RunTime.getGlobals()
           for (Entry<String, Object> entry : entries) {
               Cons entryCons = new Cons(entry.getKey(), entry.getValue());
               result = new Cons(entryCons, result);
           }
           return result;
        } 
    };    
    
    /**
     * mul primitive
     */
    public static BaseFunction mulOp = new BaseFunction("*") {
        @SuppressWarnings("unchecked")
        @Override
        public Object apply(Object... args) {
            if (args == null || args.length < 2) {
                return new PartialApplication(this, args);
            }
            else {
                Number acc = (Number) args[0];
                for (int i = 1; i < args.length; i++) {
                    Class<Number> xclass = (Class<Number>) acc.getClass();
                    Class<Number> yclass = (Class<Number>) args[i].getClass();

                    if (xclass == yclass) {
                        if (Integer.class.isAssignableFrom(xclass)) {
                            Integer x = (Integer) acc;
                            Integer y = (Integer) args[i];
                            acc = x * y;
                        }
                        else if (BigInteger.class.isAssignableFrom(xclass)) {
                            BigInteger x = (BigInteger) acc;
                            BigInteger y = (BigInteger) args[i];
                            acc = x.multiply(y);
                        }
                        else if (BigDecimal.class.isAssignableFrom(xclass)) {
                            BigDecimal x = (BigDecimal) acc;
                            BigDecimal y = (BigDecimal) args[i];
                            acc = x.multiply(y);
                        }
                        else {
                            throw new UnsupportedOperationException(fun + ": unknown type: " + xclass.getCanonicalName());
                        }
                    }
                    else {
                        if (acc instanceof BigDecimal || args[i] instanceof BigDecimal) {
                            BigDecimal x = BigDecimal.valueOf(acc.doubleValue());
                            BigDecimal y = BigDecimal.valueOf(((Number) args[i]).doubleValue());
                            acc = x.multiply(y);
                        }
                        else {
                            BigInteger x = BigInteger.valueOf(acc.longValue());
                            BigInteger y = BigInteger.valueOf(((Number) args[i]).longValue());
                            acc = x.multiply(y);
                        }
                    }
                }
                return acc;
            }
        }
    };
    
    /**
     * parse primitive
     */
    public static BaseFunction parseOp = new BaseFunction("parse") {
        @Override
        public Object apply(Object... args) {
            String input  = "" + args[0];
            input = stringTrim(input);
            return Parser.parse(input);
        }
    };

    /**
     * pp primitive
     */
    public static BaseFunction ppOp = new BaseFunction("pp") {
        @Override
        public String apply(Object... args) {
            Object x = args[0];
            if (x instanceof Cons) {
                return ((Cons) x).prettyPrint();
            }
            return "" + x;  
        }
    };

    /**
     * procedure? primitive
     */
    public static BaseFunction procedurepOp = new BaseFunction("procedure?") {
        @Override
        public Boolean apply(Object... args) {
            Object x = args[0];
            return (x instanceof Function);  
        }
    };
  
    /**
     * real? primitive
     */
    public static BaseFunction realpOp = new BaseFunction("real?") {
        @Override
        public Boolean apply(Object... args) {
            Object x = args[0];
            return (x instanceof BigDecimal );      
        }
    };
    
    /**
     * set primitive
     */
    public static BaseFunction setOp = new BaseFunction("set") {
        @Override
        public Object apply(Object... args) {
            if (args == null || args.length < 2) {
                return new PartialApplication(this, args);
            }
            else {
                String key = "" + args[0];
                Object term = args[1];
                RunTime.define(key, term);
                return term;
            }
        }
    };
    
    /**
     * string? primitive
     */
    public static BaseFunction stringpOp = new BaseFunction("string?") {
        @Override
        public Boolean apply(Object... args) {
            Object x = args[0];
            return (x instanceof String && ! RunTime.isSymbol(x));  
        }
    };
    
    /**
     * sub primitive
     */
    public static BaseFunction subOp = new BaseFunction("-") {
        @SuppressWarnings("unchecked")
        @Override
        public Object apply(Object... args) {
            Class<Number> xclass = (Class<Number>) args[0].getClass();
            Class<Number> yclass = (Class<Number>) args[1].getClass();
            
            if (xclass == yclass) {
                if (args[0] instanceof Integer) {
                    Integer x = (Integer) args[0];
                    Integer y = (Integer) args[1];
                    return x - y;
                }
                if (args[0] instanceof BigInteger) {
                    BigInteger x = (BigInteger) args[0];
                    BigInteger y = (BigInteger) args[1];
                    return x.subtract(y);
                }
                if (args[0] instanceof BigDecimal) {
                    BigDecimal x = (BigDecimal) args[0];
                    BigDecimal y = (BigDecimal) args[1];
                    return x.subtract(y);
                }
                else {
                    throw new UnsupportedOperationException(fun + ": unknown type: " + xclass.getCanonicalName());
                }
            }
            else {
                if (args[0] instanceof BigDecimal || args[1] instanceof BigDecimal) {
                    BigDecimal x = BigDecimal.valueOf(((Number) args[0]).doubleValue());
                    BigDecimal y = BigDecimal.valueOf(((Number) args[1]).doubleValue());
                    return x.subtract(y);
                }
                else {
                    BigInteger x = BigInteger.valueOf(((Number) args[0]).longValue());
                    BigInteger y = BigInteger.valueOf(((Number) args[1]).longValue());
                    return x.subtract(y);
                }
                //throw new UnsupportedOperationException(fun + ": can't handle types: " + xclass.getCanonicalName() + ", " + yclass.getCanonicalName());
            } 
        }
    };
    
    /**
     * vector? primitive
     */
    public static BaseFunction vectorpOp = new BaseFunction("vector?") {
        @Override
        public Boolean apply(Object... args) {
            Object x = args[0];
            return (x instanceof Collection<?>);    
        }
    };
        
}
