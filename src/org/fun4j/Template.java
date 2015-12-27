package org.fun4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


import org.fun4j.compiler.BaseFunction;
import org.fun4j.compiler.BasePredicate;
import org.fun4j.compiler.CompilationException;
import org.fun4j.compiler.Compiler;
import org.fun4j.compiler.Expression;
import org.fun4j.compiler.Parser;
import org.fun4j.compiler.PreCompiler;
import org.fun4j.compiler.RunTime;

/**
 * This Class is the central facade to fun4j. It provides access to most components that fun4j users will use in their
 * code.
 * 
 * @author Thomas Mahler
 * 
 */
public class Template {

    /**
     * internal reference to the compiler
     */
    private static Compiler c = new Compiler();

    /**
     * internal reference to the precompiler
     */
    private static PreCompiler pc = new PreCompiler();

    /**
     * by using a static import on this method you can define Lisp functions in user code as shown in the following
     * example:
     * 
     * <pre>
     * import static org.fun4j.Template.fn;
     * 
     * Function add = fn("(lambda (x y) (+ x y))");
     * </pre>
     * 
     * @param lambdaTerm the lambdaTerm defining the function.
     * @return the compiled Function object. The function can be executed by calling {@link Function#apply(Object...)}.
     * @throws CompilationException
     */
    public static Function fn(String lambdaTerm) throws CompilationException {
        return compile(lambdaTerm);
    }

    /**
     * by using a static import on this method you can define Lisp predicates in user code as shown in the following
     * example:
     * 
     * <pre>
     * import static org.fun4j.Template.predicate;
     * 
     * Predicate even = predicate("(lambda (n) (= 0 (% n 2)))");
     * </pre>
     * 
     * @param lambdaTerm the lambdaTerm defining the predicate.
     * @return the compiled Predicate object. The predicate can be executed by calling
     *         {@link Predicate#apply(Object...)}.
     * @throws CompilationException
     */
    public static Predicate predicate(String lambdaTerm) throws CompilationException {
        final Function function = fn(lambdaTerm);
        return new BasePredicate() {            
            @Override
            public Boolean apply(Object... args) {
                return (Boolean) function.apply(args);
            }
        };        
    }

    /**
     * parses a Lisp term input into its AST representation. nil is parsed to null T or true is parsed to boolean true F
     * or false is parsed to boolean true Numbers are parsed to Integers / BigIntegers or BigDecimals Symbols and
     * "quoted strings" are parsed to Strings Lists are parsed into Cons Binary trees The Readmacro 'x is expanded to
     * (QUOTE x)
     * 
     * @param lispTerm
     * @return the AST representation
     */
    public static Object parse(String lispTerm) {
        return Parser.parse(lispTerm);
    }

    /**
     * compiles an AST {@link Expression} into an executable Java {@link Function}.
     * 
     * @param exp the Expression to compile
     * @param name the Name of this expression, will be used for creation of the internal Class-Name
     * @return the compiled Function
     * @throws CompilationException
     */
    public static BaseFunction compile(Expression exp, String name) throws CompilationException {
        return c.compile(exp, name);
    }

    /**
     * compiles a LISP term into an executable Java {@link Function}.
     * 
     * @param lispTerm the LISP term to be compiled
     * @return the compiled Function
     * @throws CompilationException
     */
    public static Function compile(String lispTerm) throws CompilationException {
        Object term = Parser.parse(lispTerm);
        Function result;
        try {
            Expression exp = pc.precompile(term);
            result = c.compile(exp, "TemplateCompile");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new CompilationException("Error in compile:", e);
        }
        return result;
    }

    /**
     * precompiles a LISP term into an AST {@link Expression}. This expression can be compiled to executable Java code
     * by calling {@link #compile(Expression, String)}
     * 
     * @param lispTerm the LISP term to be precompiled
     * @return the {@link Expression} representing the LISP term
     * @throws CompilationException
     */
    public static Expression precompile(String lispTerm) throws CompilationException {
        Object term = Parser.parse(lispTerm);
        return pc.precompile(term);
    }

    /**
     * binds parameters to the variables of a function. Binding will return a {@link PartialApplication} containing
     * function and arguments.
     * 
     * @param fun the function
     * @param args the parameter to be bound to the function
     * @return a {@link PartialApplication} containing function and arguments.
     */
    public static Function bind(Function fun, Object... args) {
        if (args == null || args.length == 0) {
            return fun;
        }
        else {
            return new PartialApplication(fun, args);
        }
    }

    /**
     * evaluates a LISP term.
     * 
     * @param lispTerm the term to evaluate
     * @param args optional parameters are passed in as function arguments
     * @return the result of the evaluation
     * @throws CompilationException
     */
    public static Object eval(String lispTerm) throws CompilationException {
        return compile(lispTerm).apply();
    }
    
    public static Object evalParsed(Object term) throws CompilationException {
        Expression exp = pc.precompile(term);
        Function fun = c.compile(exp, "Template.evalParsed");
        if (fun == null) {
            return null;
        }
        else {
            return fun.apply();
        }
    }

    /**
     * Applies a lisp function to arguments
     * @param lispTerm the lispterm that evaluates to a funcion
     * @param args optional parameters are passed in as function arguments
     * @return the result of the function application
     * @throws CompilationException
     */
    public static Object apply(String lispTerm, Object... args) throws CompilationException {
        return ((Function) eval(lispTerm)).apply(args);
    }

    /**
     * evaluates all lisp expression in a file.
     * 
     * @param filename the file to execute.
     */
    public static void runFile(String filename) {
        Repl.load(filename);
    }

    /**
     * binds name to value in the global Lisp environment
     * 
     * @param name the name to be used
     * @param value the value to be associated with the name.
     */
    public static void define(String name, Object value) {
        RunTime.define(name, value);
    }

    /**
     * if set to true (the default value) the Parser and Compiler will work with BigIntegers. If set to false they will
     * use Integers.
     * 
     * @param newValue the new value
     * @return the old value
     */
    public static boolean setUseBigInts(boolean newValue) {
        return Compiler.setUseBigInts(newValue);
    }

    /**
     * if set to true the Compiler will write precompiled expression into tmp/*.lsp files, compiled bytecote into
     * tmp/*.class files and debugging information is generated into the bytecode.
     * 
     * @param newValue the new value
     * @return the old value
     */
    public static boolean setWriteFile(boolean newValue) {
        boolean result = Compiler.writeFile;
        Compiler.writeFile = newValue;
        return result;
    }


    /**
     * 
     * @param outerInterface
     * @param methodBody
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T $(final Class<T> outerInterface, final String methodBody) {
        return (T) Proxy.newProxyInstance(outerInterface.getClassLoader(), new Class[] { outerInterface },
            new InvocationHandler() {

                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    return apply(methodBody, args);
                }
            });
    }

}
