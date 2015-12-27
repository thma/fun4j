package org.fun4j.compiler;

import static org.fun4j.compiler.Symbols.HOOK_DEFINE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;

import org.fun4j.Collections;
import org.fun4j.Cons;
import org.fun4j.Function;
import org.fun4j.Functions;
import org.fun4j.NamedObject;
import org.fun4j.Repl;
import org.fun4j.Template;

/**
 * The Runtime environment. Providing global maps and some static helper methods.
 * 
 * @author Thomas Mahler
 * 
 */
public class RunTime {

    private static String tmpDir;

    private static HashMap<String, Object> globals = new HashMap<String, Object>();

    private static HashMap<Integer, Object> constants = new HashMap<Integer, Object>();

    public static HashMap<String, Object> getGlobals() {
        return globals;
    }
    
    // initialization of RunTime env
    static {
        setupEnv();
    }

    
    /**
     * initialize the lisp environment.
     * load all predefined functions and
     * load the init.lsp.
     * all definition are stored in the
     * RunTime globals and are thus available in any lisp code executed.
     */
    public static void setupEnv() {
        // set default tmp dir:
        setTempDir("tmp");

        // define DOT Operator
        try {            
            Template.define(".", Template.compile("(lambda varargs (_DOT (car varargs) (cadr varargs) (cddr varargs)))"));
	} 
        catch (CompilationException e) {
            e.printStackTrace();
	}
        
        // define Scheme primitives
        registerNamedObjects(SchemePrimitives.class);
        
        // load initial Scheme definitions
        Repl.load("init.scm");
        
        // load Java Api as Scheme functions:
        defineDeclaredMethodsOf(Collections.class);
        defineDeclaredMethodsOf(Functions.class);
    }
    
    
    /**
     * this method registers all static BaseFunction instances defined in the
     * input Class to the {@link RunTime#getGlobals()} environment.
     * @param clazz the Class holding the Function instances.
     */
    protected static void registerNamedObjects(Class<?> clazz) {
        Field[] allFields = clazz.getFields();
        for (Field field : allFields) {
            Object value = null;
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    value = field.get(null);
                    if (value instanceof NamedObject) {
                        String key = ((NamedObject) value).getName();
                        RunTime.define(key, value);
                    }
                }
            }
            catch (Throwable t) {
                System.err.println("error in registering Scheme primitives. Offending instance: " + value);
                t.printStackTrace();
            }
        }
    }

    /**
     * this method takes a Class as input, it wraps all declared methods of this Class as 
     * Fun4j {@link Function} instances and defines them under <code><class>.<method></code>
     * in the Fun4j environment.
     * @param clazz the input Class  
     * @throws SecurityException
     */
    protected static void defineDeclaredMethodsOf(Class<?> clazz) throws SecurityException {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            String name = clazz.getSimpleName() + "." + method.getName();
            Function fun = Functions.functionFromMethod(method);            
            define(name, fun);
        }
    }

	public static void resetEnv()   {
        globals = new HashMap<String, Object>();
        constants = new HashMap<Integer, Object>();
        setupEnv();
    }
    
    public static void setTempDir(String dir) {
        tmpDir = dir;
        new File(tmpDir).mkdir();
    }

    public static String getTempDir() {
        return tmpDir;
    }
    
    /**
     * this dummy method simply triggers loading of the class 
     * and thus execution of the static initializer.
     */
    public static void wakeup() {
        
    }

    public static Object lookup(String key) {
        if (globals.containsKey(key)) {
            return globals.get(key);
        }
        else {
            throw new RuntimeException("undefined symbol: " + key);
        }
    }

    public static void define(String key, Object term) {
        undefine(key);
        globals.put(key, term);
        handleHook(HOOK_DEFINE, key, term);
    }

    public static void undefine(String key) {
        globals.remove(key);
    }

    public static Object dump() throws IOException {
        File file = new File("dump.ser");
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(globals);
        out.close();
        fos.close();
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Object fasl() throws IOException, ClassNotFoundException {
        File file = new File("dump.ser");
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fis);
        globals = (HashMap<String, Object>) in.readObject();
        in.close();
        return null;
    }

    public static int registerConstant(Object obj) {
        int id = System.identityHashCode(obj);
        constants.put(id, obj);
        return id;
    }

    public static Object getConstant(int id) {
        Object result = constants.get(id);
        return result;
    }

    public static void unregisterConstant(int id) {
        constants.remove(id);
    }

    public static Boolean isSymbol(Object obj) {
        if (obj instanceof String && ! obj.toString().startsWith("\"")) {
            return Boolean.valueOf(true);
        }
        else {
            return Boolean.valueOf(false);
        }
    }

    public static Boolean isNumber(Object obj) {
        if (Compiler.useBigInts() && obj instanceof BigInteger) {
            return Boolean.valueOf(true);
        }
        else if (!Compiler.useBigInts() && obj instanceof Integer) {
            return Boolean.valueOf(true);
        }
        else if (obj instanceof BigDecimal) {
            return Boolean.valueOf(true);
        }
        else {
            return Boolean.valueOf(false);
        }
    }

    public static Boolean isAtom(Object obj) {
        if (isNumber(obj) || obj instanceof String || obj instanceof Function || obj instanceof Collection<?>) {
            return Boolean.valueOf(true);
        }
        else {
            return Boolean.valueOf(false);
        }
    }

    public static Boolean isList(Object obj) {
        if (obj == null || obj instanceof Cons) {
            return Boolean.valueOf(true);
        }
        else {
            return Boolean.valueOf(false);
        }
    }

    public static Boolean isCompiled(Object obj) {
        if (obj != null && obj instanceof Function) {
            return Boolean.valueOf(true);
        }
        else {
            return Boolean.valueOf(false);
        }
    }

    public static Object thread(final Object obj) throws CompilationException {
        // create executable function
        Compiler c = new Compiler();
        PreCompiler pc = new PreCompiler();
        Expression exp = pc.precompile(obj);
        final Function fun = c.compile(exp, "Thread-" + obj);

        // execute function in its own thread
        Runnable r = new Runnable() {
            @Override
            public void run() {
                fun.apply();
            }
        };
        new Thread(r).start();
        return Boolean.valueOf(true);
    }

    public static Object compile(Object obj) throws CompilationException {
        Compiler c = new Compiler();
        PreCompiler pc = new PreCompiler();
        Expression exp = pc.precompile(obj);
        return c.compile(exp, "RunTimeCompile");
    }

    public static Object eval(Object obj) throws CompilationException {
        Compiler c = new Compiler();
        PreCompiler pc = new PreCompiler();
        Expression exp = pc.precompile(obj);
        Function fun = c.compile(exp, "RunTimeCompile");
        return fun.apply();
    }


    public static Object load(Object x) {
        try {
            String file = x.toString();
            Repl.load(file);
            return Boolean.TRUE;
        }
        catch (Throwable t) {
            return t.getMessage();
        }
    }

    public static Object print(Object x) {
        System.out.println(x);
        return x;
    }

    public static Object prin(Object x) {
        System.out.print(x);
        return x;
    }
    
    public static Object pretty(Object x) {
        String out;
        if (x instanceof Cons) {
            out = ((Cons) x).prettyPrint();
        } 
        else {
            out = "" + x;
        }
        System.out.println(out);
        return x;
    }

    public static String stringTrim(String input) {
		if (input.startsWith("\"") && input.endsWith("\"")) {
			input = input.substring(1, input.length()-1);
		}
		return input;
    }
    
    public static Object explode(Object x) {
        String str = "" + x;
        str = stringTrim(str);
        Cons result = new Cons(null, null);
        Cons currentTail = result;
        for (int i = 0; i < str.length() - 1; i++) {
            currentTail.setHd(str.charAt(i));
            Cons newTail = new Cons(null, null);
            currentTail.setTl(newTail);
            currentTail = newTail;
        }
        currentTail.setHd(str.charAt(str.length() - 1));
        return result;
    }

    public static Object implode(Object x) {
        Cons list = (Cons) x;
        String result = "";
        while (list != null) {
            result += stringTrim("" + list.getHd());
            list = (Cons) list.getTl();
        }
        return result;
    }
    
    public static Object trace(Object fun) throws IOException, CompilationException {
        BaseFunction traced;
        String name = fun.toString();
        try {
            BaseFunction untraced = (BaseFunction) lookup(name);        
            define(name + "_NOTRACE", untraced);
            
            Compiler c = new Compiler();
            traced = c.injectTracing(untraced);
            define(name, traced);
            return traced;
        }
        catch (Exception e) {
            //System.out.println("can't trace " + name);
            return fun;
        }
    }

    public static Object untrace(Object fun) {
        String name = fun.toString();
        try {
            BaseFunction untraced = (BaseFunction) lookup(name + "_NOTRACE");
            define(name, untraced);
            return untraced;
        }
        catch (Exception e) {
            //System.out.println("can't untrace " + name);
            return fun;
        }
    }
    

    public static Object read(Object ignored) throws IOException {
        Compiler c = new Compiler();
        PreCompiler pc = new PreCompiler();
        BufferedReader rin = new BufferedReader(new InputStreamReader(System.in));
        String input = "'" + rin.readLine();
        Object term = Parser.parse(input);
        Object result = null;
        if (term != null) {
            Expression exp = null;
            Function fun = null;
            try {
                exp = pc.precompile(term);
                fun = c.compile(exp, "RunTimeRead");
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
            if (fun != null) {
                result = fun.apply();
            }
        }
        return result;
    }
    
    public static Object invokeDynamic(Object instance, Object method, Object argsAsCons) throws SecurityException,
            NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            NoSuchFieldException {

        Object[] args = consToArray((Cons) argsAsCons);
        @SuppressWarnings("rawtypes")
        Class[] argTypes = argTypes(args);

        // check if instance is a classname
        boolean instanceIsClassName = false;
        Class<?> clazz = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            clazz = Class.forName(qualifyIfNeeded(instance.toString()), true, cl);
            instanceIsClassName = true;
        }
        catch (ClassNotFoundException e1) {
            // ignored
        }
        // is instance the name of a class ?
        if (instanceIsClassName) {
            try {
                Method jMethod = clazz.getMethod(method.toString(), argTypes);
                return jMethod.invoke(clazz, args);
            }
            catch (NoSuchMethodException e) {
                Field jField = clazz.getField(method.toString());
                return jField.get(clazz);
            }
        }
        else {
            // no it's a normal instance
            clazz = instance.getClass();
            
            System.out.println("instance: " + instance);
            System.out.println("method: " + method);
            
            try {
                Method jMethod = clazz.getMethod(method.toString(), argTypes);
                return jMethod.invoke(instance, args);
            }
            catch (NoSuchMethodException e) {
                Field jField = clazz.getField(method.toString());
                return jField.get(instance);
            }
        }
    }

    private static String qualifyIfNeeded(String string) {
        if (string != null && ! string.contains(".")) {
            return "java.lang." + string;
        }
        else {
            return string;
        }
    }

    @SuppressWarnings("rawtypes")
    private static Class[] argTypes(Object[] args) {
        if (args == null) {
            return null;
        }
        else {
            Class[] arr = new Class[args.length];
            for (int i = 0; i< args.length; i++) {
                arr[i] = args[i].getClass();
            }
            return arr;
        }        
    }

    private static Object[] consToArray(Cons argsAsCons) {
        if (argsAsCons == null) {
            return null;
        }
        else {
            int length = length(argsAsCons);
            Object[] arr = new Object[length];
            for (int i = 0; i<length; i++) {
                arr[i] = argsAsCons.getHd();
                argsAsCons = (Cons) argsAsCons.getTl();
            }
            return arr;
        }
    }

    private static int length(Cons cons) {
        int result = 0;
        while (cons != null) {
            result++;
            cons = (Cons)cons.getTl();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Function instantiateJavaFunction(String classname) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        Class<Function> funClass = (Class<Function>) Class.forName(classname);
        Function fun = funClass.newInstance();
        return fun;
    }
    
    /**
     * allows to execute a user-defined hook at any place within fun4j Runtime.
     * 
     * @param hookName the name of the hook
     * @param values the lisp term to be compiled
     */
    public static void handleHook(String hookName, Object... values) {
        Function compileHook = (Function) RunTime.getGlobals().get(hookName);
        if (compileHook != null) {
            compileHook.apply(values);
        }
    }
    
    public static Object construct(Object... args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String classname = (String) args[0];
    	Class<?> clazz = Class.forName(classname);
        Class<?>[] parameterTypes = new Class[args.length-1];
        for (int i = 0; i<args.length-1; i++) {
            parameterTypes[i] = args[i+1].getClass();
        }
        Constructor<?> constructor = clazz.getConstructor(parameterTypes);
        
        return constructor.newInstance(args);
    }
}
