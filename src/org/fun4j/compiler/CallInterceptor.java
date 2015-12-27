package org.fun4j.compiler;

import java.lang.reflect.Method;
import java.util.HashMap;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.fun4j.Functions;

/**
 * helper class used for a Funcito like construction of Function
 * instances from Java method calls.
 * see {@link Functions#functionFor(Object)} and {@link Functions#callsTo(Class)}
 * @author Thomas Mahler
 */
public class CallInterceptor implements MethodInterceptor {

    /**
     * in order to make the sequence of calls to {@link Functions#functionFor(Object)} and {@link Functions#callsTo(Class)}
     * in a typical usage scenario like <code>functionFor(callsTo(Box.class).contains(0.0, 0.0));</code>
     * thread-safe Methods are stored for each Thread.
     */
    private static HashMap<Thread, Method> lastCallMap = new HashMap<Thread, Method>();

    /**
     * this method returns the last Method call registered by <code>setLastCall(Method)</code> within the current Thread.
     * @return the registered Method instance
     */
    public static Method getLastCall() {
        return lastCallMap.get(Thread.currentThread());
    }

    /**
     * this method registers a Method as the last call from the current thread.
     * subsequent calls to <code>setLastCall(Method)</code> will replace any previously registered Method instances.
     * @param method the method to be registered
     */
    private static void setLastCall(Method method) {
        lastCallMap.put(Thread.currentThread(), method);
    }

    
    /**
     * this method intercepts all Method calls to mocks generated by {@link Functions#callsTo(Class)}. 
     * Each method call is registered by <code>setLastCall(Method)</code>
     */
    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        setLastCall(method);
        return methodProxy.invokeSuper(object, args);
    }

}