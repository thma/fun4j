package test.org.fun4j;

import static org.fun4j.Collections.*;
import static org.fun4j.Functions.functionFromMethod;
import static org.fun4j.Template.define;
import static org.fun4j.Template.eval;
import static org.fun4j.Template.fn;
import static org.fun4j.Template.setUseBigInts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.fun4j.Cons;
import org.fun4j.Function;
import org.fun4j.compiler.BaseFunction;
import org.fun4j.compiler.CompilationException;
import org.fun4j.compiler.Parser;

public class TestJavaLispIntegration extends TestCase {
    
    public void testJavaFunction() throws CompilationException {

        // that's the java way to define a function:
        Function hash = new BaseFunction() {

            @Override
            public Object apply(Object... args) {
                if (args == null) {
                    throw new RuntimeException("null is not allowed");
                }
                else {
                    Object x = args[0];
                    return new Integer(x.hashCode());
                }
            }   
        }; 
        define("hash", hash);
        eval("(define map (lambda (fun list) (if (null? list) nil (cons (fun (car list)) (map fun (cdr list))))))");
        Object result = eval("(map hash '(hallo welt))");
        
        String expected = "(" + hash.apply("hallo") + " " + hash.apply("welt") + ")";
        assertEquals(expected, result.toString());
    }
    
    public void testLispFunction() throws CompilationException {
        // Java version of "map"
        Function map = new BaseFunction() {
            @Override
            public Object apply(Object... args) {
                Function fun = (Function) args[0];
                Cons list = (Cons) args[1];
                if (list == null) {
                    return null;
                }
                else {
                    return new Cons(fun.apply(list.getHd()), this.apply(fun, list.getTl()));
                }
            }  
        };
        
        define("map", map);    
        //useBigInts(true);
        eval("(define squared (lambda (n) (* n n)))");
        Cons result = (Cons) eval("(map squared '(1 2 3 4))");
        assertEquals(asCollection(1, 4, 9, 16).toString(), fromCons(result).toString());
    }
    
    public void testLispFunctionWithCollection() throws CompilationException {
        setUseBigInts(false);
        
        // mapping Collections
        Function map = new BaseFunction() {
            @Override
            public Object apply(Object... args) {
                Function fun = (Function) args[0];
                @SuppressWarnings("unchecked")
                Collection<Object> list = (Collection<Object>) args[1];
                if (list == null) {
                    return null;
                }
                else {
                    ArrayList<Object> result = new ArrayList<Object>();
                    for (Object object : list) {
                        result.add(fun.apply(object));
                    }
                    return result;
                 }
            }  
        };
        
        //define("map", map);    
        Function squared = fn("(lambda (n) (* n n))");         
        Collection<Integer> list = asCollection(1,2,3,4);        
        Object result = map.apply(squared, list); 
        
        assertEquals("[1, 4, 9, 16]", result.toString());
    }
    
    public void testJavaCollection() throws CompilationException {
        setUseBigInts(false);        
        Function sqr = fn("(lambda (n) (* n n))");         
        Collection<Integer> col = asCollection(1,2,3,4);               
        Object result = map(sqr, col);
        
        assertEquals("[1, 4, 9, 16]", result.toString());
    }

//    @Ignore
//    public void testJavaAccess() throws CompilationException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchFieldException {
//        Box rect = new Box(0.0,0.0,10.0,10.0);
//        define("rect", rect);
//
//        assertEquals(rect.getSize(), eval("(. rect 'getSize)"));
//
//        assertTrue((Boolean)eval("(. rect 'contains 5.0 6.0)"));
//
//        assertFalse((Boolean)eval("(. rect 'contains 23.0 13.0)"));
//
//        Box rect1 = new Box(0.0,0.0,5.0,5.0);
//        Box rect2 = new Box(0.0,0.0,9.0,9.0);
//
//        Collection<Box> col = asCollection(rect, rect1, rect2);
//        Function getSize = fn("(lambda (rec) (. rec 'getSize))");
//        Collection<?> result = map(getSize, col);
//        System.out.println(asCons(result));
//
//        Function add = fn("(lambda (x y) (+ x y))");
//        assertEquals(rect.getSize() + rect1.getSize() + rect2.getSize(), foldright(add, 0.0, result) );
//
//        result = map(fn("(lambda (rec) (. rec 'getSize))"), col);
//        System.out.println(asCons(result));
//
//        Method method = Box.class.getMethod("getSize", (Class<?>[]) null);
//        Function funGetSize = functionFromMethod(method);
//        funGetSize.apply(rect);
//
//        System.out.println(funGetSize.apply(rect));
//
//        System.out.println(asCons(result));
//
//        System.out.println(eval("(. rect 'getHeight)"));
//
//        System.out.println(eval("(. rect 'height)"));
//
//        System.out.println(eval("(. 'Class 'getName )"));
//
//        System.out.println(eval("(. 'System 'getProperty \"java.vm.version\")"));
//        System.out.println(eval("(. 'System 'getProperties )"));
//
//        System.out.println(eval("(. 'Class 'getMethods )"));
//
//        System.out.println(eval("(. 'Math 'PI)"));
//
//        System.out.println(eval("(. 'Math 'random )"));
//
//        System.out.println(eval("(. 'test.org.fun4j.Box 'CONSTANT )"));
//
//
//        System.out.println(eval("(. rect 'height)"));
//
//        define("col", result);
//        System.out.println(eval("(. col 'size)"));
//
//
//        define("col", eval("[[1 2 3][1 2][3 4 5][8 9]]"));
//        System.out.println(eval("(. col 'size)"));
//        System.out.println(eval("(. col 'lastElement)"));
//
//        System.out.println(eval("(. [[a b c][d e][f g h][i j]] 'size)"));
//        System.out.println(eval("(. [1 2 3 4] 'lastElement)"));
//    }

    public void testDottedPairs() throws CompilationException {
    	
    	assertEquals("B", ((Cons)Parser.parse("(A . B)")).getTl() );
    	
    	assertEquals("END", eval("(last '(1 (A . B) 2 3 4 END))"));
    }
    
    public void testParser() throws CompilationException {
        
        System.out.println(Parser.parse("\"jurp\""));
        
        System.out.println(Parser.parse("[1 2 3 4 5]"));
        
        System.out.println(Parser.parse("[first a bnb jhjh kj zui last]"));
        
        System.out.println(Parser.parse("[\"first\" \"a\" \"bnb\" \"jhjh\" \"kj\" \"zui\" \"last\" ]"));

        System.out.println(Parser.parse("['first '13 '\"a\" '[1 2 3] 'lastElement]"));
        
        System.out.println(Parser.parse("[`first `13 `\"a\" `[1 2 3] `lastElement]"));
        
        System.out.println(Parser.parse("[[1 2 3] [4 5 6] [7 8 9]]"));     
        
        System.out.println(Parser.parse("(define getproperties (lambda () (staticfunction 'java.lang.System 'getProperties \"()Ljava/util/Properties;\" )))"));  

        System.out.println(Parser.parse(" \"()Ljava/util/Properties;\" "));
        
        System.out.println(Parser.parse("(12 \"()Ljava/util/Properties;\" 13)"));
        
        eval("(define getproperties (lambda () (staticfunction 'java.lang.System 'getProperties  \"()Ljava/util/Properties;\" )))");
    
    }
    
    public void testQuasiQuote() throws CompilationException {
        setUseBigInts(false);
        eval("(define add5 (macro (n) `(+ 5 ,n)))");
        assertEquals(12, eval("(add5 7)"));        
        assertEquals(12, eval("(add5 (+ 3 4))"));        
        assertEquals(12, eval("(add5 (eval (list '+ '3 '4)))"));
        assertEquals(12, eval("(add5 (eval `(+ ,3 4)))"));
                
        eval("(define let (macro (binding body) " +
             "   `((lambda ,(extractVars binding) ,body)" +
             "     ,@(extractValues binding)))) ");
        
        assertEquals(3, eval("(let ((a 1)(b 2)) (+ b a))"));
    }
    
    public void testTrampoline() {
        System.out.println(even(3000));
    }
    
    private boolean even(int n) {
        if (n == 0) {
            return true;
        }
        else {
            return odd(n -1);
        }
    }
    
    private boolean odd(int n) {
        if (n == 0) {
            return false;
        }
        else {
            return even(n -1);
        }
    }
    
}
