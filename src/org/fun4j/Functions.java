package org.fun4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.proxy.Enhancer;

import org.fun4j.compiler.BaseFunction;
import org.fun4j.compiler.BasePredicate;
import org.fun4j.compiler.CallInterceptor;

/**
 * This Class contains all methods of the Fun4j API for creating Functions and working with them.
 * 
 * @author Thomas Mahler
 */
public class Functions {

    /**
     * composes two functions f(x) and g(x) to f(g(x)).
     * 
     * @param f a Funxtion f(x)
     * @param g a Function g(x)
     * @return the composed Function f o g (x)
     */
    public static Function compose(final Function f, final Function g) {
        return new Function() {
            @Override
            public Object apply(Object... args) {
                return f.apply(g.apply(args));
            }
        };
    }

    /**
     * composes a set of functions <code>functions[1..n]</code> to a new function
     * <code>functions[1] o functions[2] o ... functions[n]</code>
     * 
     * @param functions the set of functions <code>functions[1..n]</code>
     * @return the composed function <code>functions[1] o functions[2] o ... functions[n]</code>
     */
    public static Function compose(final Function... functions) {
        if (functions == null) {
            return null;
        }
        else {
            Function result = Functions.identity;
            for (int i = functions.length - 1; i >= 0; i--) {
                result = compose(functions[i], result);
            }
            return result;
        }
    }

    /**
     * produces a Function that returns a constant value.
     * 
     * @param obj the value to be returned by the constant function.
     * @return the resulting Function f with f.apply() = obj
     */
    public static Function constant(final Object obj) {
        return new Function() {
            @Override
            public Object apply(Object... args) {
                return obj;
            }
        };
    }

    /**
     * the identity Function identity(x) = x
     */
    public static Function identity = new Function() {
        @Override
        public Object apply(Object... args) {
            return args[0];
        }
    };

    /**
     * this method takes a {@link Method} instance as input and returns a {@link Function} that wraps the input method.
     * Useful to reach Java Methods around as functions.
     * If the Return type is <code>boolean</code> or <code>Boolean</code> a {@link Predicate} instance is returned.
     * 
     * @param method the method to be wrapped
     * @return the {@link Function} wrapper
     */
    public static Function functionFromMethod(final Method method) {
        // construct wrapper Function instance
        final Function result = new BaseFunction() {
            @Override
            public Object apply(Object... args) {
                Object instance;
                Object[] arguments;
                // special treatment for static methods:
                if (Modifier.isStatic(method.getModifiers())) {
                    instance = null;
                    arguments = args;
                }
                else {
                    instance = args[0];
                    // if arguments are present build up argument array
                    if (args.length > 1) {
                        arguments = new Object[args.length - 1];
                        System.arraycopy(args, 1, arguments, 0, arguments.length);                        
                    }
                    else {
                        arguments = null;
                    }
                }
                // perform actual call to the wrapped method:
                try {
                    return method.invoke(instance, arguments);
                }
                catch (IllegalArgumentException e) {
                    return e;
                }
                catch (IllegalAccessException e) {
                    return e;
                }
                catch (InvocationTargetException e) {
                    return e;
                }
            }
        };
        // special handling for Predicate functions
        Class<?> type = method.getReturnType();
        if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            return new BasePredicate() {
                @Override
                public Boolean apply(Object... args) {
                    return (Boolean) result.apply(args);
                }
            };
        }
        else {
            return result;
        }
    }

    /**
     * this method allows to build a {@link Function} based on a Java method call. The idea for this utility was taken
     * from the Funcito framework. The aim is to avoid usage of Java Reflection in user code. Instead of
     * <code>Function size = functionFor(Box.class.getMethod("contains", Double.class, Double.class));</code> one can
     * write in Funcito style: <code>Function size = functionFor(callsTo(Box.class).contains(0.0, 0.0));</code> In the
     * Reflection approach the method-name must be given as a String argument, which is error prone as it can not be
     * verified by the java compiler. In addition the explicit naming of the parameter types is inconvenient and also
     * can't be verified at compile time. In the Funcito approach these issues are avoided by mocking an actual method
     * invocation. This is easy to write and read and can be verified at compile time.
     * 
     * @param ignored must be a mocked instance represented by <code>callsTo(Some.class).someMethod()</code> expression
     * @return the {@link Function} wrapper instance.
     */
    public static Function functionFor(Object ignored) {
        // looks up the last method call from the CallInterceptors internal map
        // and wraps it with Function instance.
        return functionFromMethod(CallInterceptor.getLastCall());
    }

    /**
     * this method is used to create a mock instance that memorizes the last {@link Method} call so that a subsequent
     * invocation of {@link functionFor} may use this Method instance to build a Function instance wrapper from it.
     * 
     * @param type the type for which a mock instance has to be created
     * @return the mock instance
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public static <T> T callsTo(Class<T> type) throws InstantiationException, IllegalAccessException {
        // the returned mock will delegate all Method calls to the
        // CallInterceptor
        // The CallInterceptor stores the last call as a {@link Method} instance
        // so
        // that it may be retrieved later in a call to
        // <code>CallInterceptor.getLastCall()</code>
        return (T) Enhancer.create(type, new CallInterceptor());
    }

}
