package test.org.fun4j;

import static org.fun4j.Template.compile;
import static org.fun4j.Template.eval;
import static org.fun4j.Template.fn;
import static org.fun4j.Template.precompile;
import static org.fun4j.Template.setUseBigInts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.fun4j.Collections;
import org.fun4j.Function;
import org.fun4j.PartialApplication;
import org.fun4j.Repl;
import org.fun4j.Template;
import org.fun4j.compiler.BaseFunction;
import org.fun4j.compiler.CompilationException;
import org.fun4j.compiler.Compiler;
import org.fun4j.compiler.Expression;
import org.fun4j.compiler.RunTime;




public class TestLisp
        extends TestCase {
    
    public void testMacrosLet() throws CompilationException {
        setUseBigInts(false);
        eval("(define list (lambda elements (if (null? elements) null (cons (car elements) (list (cdr elements))))))");
        eval("(define append (lambda (a b) (if (null? a) b (cons (car a) (append (cdr a) b)))))");
        eval("(define extractVars (lambda (binding) (if (null? binding) nil (cons (caar binding) (extractVars (cdr binding))))))");
        eval("(define extractValues (lambda (binding) (if (null? binding)  nil (cons (car (cdar binding)) (extractValues (cdr binding))))))");
        eval("(define let (macro (binding body) (append (cons (list 'lambda (extractVars binding) body) nil) (extractValues binding))))");
        
        int result = (Integer) eval("(let ((a 1)(b 2)) (+ b a))");
        assertEquals (3, result);
        
        result = (Integer) eval("(let ((a 7)(b 11)) (* a b))");
        assertEquals (77, result);
        
        eval("(define test (lambda (x y) (let ((a (* 2 x)) (b (* 3 y))) (+ a b))))");

        result = (Integer) eval("(test 2 3)");
        assertEquals(13, result);
        
        result = (Integer) eval("(test 1 1)");
        assertEquals(5, result);
               
    }
    
    public void testStackHandling() throws CompilationException {
        // the following might cause a stack overflow on some machines. 
    	// It's here, just to avoid regressions wrt. to stack handling, (a 3 11) is the maximum that I get on my box.
        eval("(define a (lambda (n m) (if (zero? n) (add1 m) (if (zero? m) (a (sub1 n) 1) (a (sub1 n) (a n (sub1 m)))))))");
//        assertEquals(16381, eval("(a 3 11)"));
    }
    
    public void testMutualRecursion() throws CompilationException {
        setUseBigInts(false);
        eval("(define even? (lambda (n) (if (= n 0) true (odd? (sub1 n)))))");
        eval("(define odd?  (lambda (n) (if (= n 0) false (even? (sub1 n)))))");
        
        assertTrue((Boolean) eval("(even? 16)"));
        assertTrue((Boolean) eval("(odd? 17)"));
        
        assertFalse((Boolean) eval("(even? 31)"));
        assertFalse((Boolean) eval("(odd? 18)"));     
    }
    
    public void testLambda() throws CompilationException {
        RunTime.undefine("even?");
        RunTime.undefine("odd?");
        eval("(define fac (lambda (n) (if (= n 0) 1 (* n (fac (sub1 n))))))");
        
        //((lambda (fun n) (fun n)) (lambda (n) (* n n)) 7)
        Expression exp = precompile("(lambda (fun n) (fun n))");
        System.out.println(exp);

        exp = precompile("((lambda (fun n) (fun n)) fac 7)");
        System.out.println(exp);
        
        Function test = compile(exp, null);
        System.out.println(test.apply());
        
        exp = precompile("((lambda (fun arg) (fun arg)) (lambda (n) (* n n)) 7)");
        System.out.println(exp);
        test = compile(exp, null);
        System.out.println(test.apply());
        
        exp = precompile("((lambda (arg fun) (fun arg)) 12 (lambda (n) (* n n)))");
        System.out.println(exp);
        test = compile(exp, null);
        System.out.println(test.apply());    
        
        assertEquals(eval("(fac 5)"), eval("(* 1 2 3 4 5)"));
    }
    
    public void testLexicalScope() throws CompilationException {
        RunTime.undefine("even?");
        RunTime.undefine("odd?");
        Expression exp = precompile(
        "((lambda (even? odd?) (odd? 37 even? odd?)) " +
        "		(lambda (n even? odd?) (if (= n 0) true (odd? (sub1 n) even? odd?))) "+
        "		(lambda (n even? odd?) (if (= n 0) false (even? (sub1 n) even? odd?))))");
        
        Function test = compile(exp, null);
        assertTrue((Boolean)test.apply()); 
    }
    
    public void testClosure() throws CompilationException {      
    	Template.setUseBigInts(false);
    	assertEquals(eval("'(3 6 9)"), eval("(map (lambda (i) (* 3 i)) '(1 2 3))"));
    	
    	Function add5 = fn("(lambda (x) ((lambda (y) (+ x y)) 5))");
    	assertEquals(12, add5.apply(7)); 
    	
    	Function add = fn("(lambda (x) (lambda (y) (+ x y)))");
    	assertEquals(12, add.apply(5,7)); 
    	
    	add5 = new PartialApplication(add, Collections.asCollection(5).toArray());
    	System.out.println(add5.apply(7));
    }
    
//    public void testLetrec() throws CompilationException {
//        String term = "(letrec" +
//        			  "  ((even? (lambda (n) (if (zero? n) true (odd? (sub1 n)))))" +
//	                  "   (odd? (lambda (n) (if (zero? n) false (even? (sub1 n))))))" +
//                      "	 (odd? 37))";
//        Expression exp = precompile(term);
//        Function fun = compile(exp, null);
//        assertTrue((Boolean)fun.apply());
//    }
    
    public void testRepl() {
        InputStream inOrig = System.in;
        String input = "(fac 10)\n(quit)\n";
        InputStream is = new ByteArrayInputStream(input.getBytes());
        System.setIn(is);
        Repl.main(null);
        
        input = "($%& 10)\n(quit)\n";
        is = new ByteArrayInputStream(input.getBytes());
        System.setIn(is);
        Repl.main(null);
        
        Repl.load(null);
        Repl.load("qs.scm");
        
        System.setIn(inOrig);
    }
    
    public void testLogging() throws CompilationException, IOException {
        BaseFunction fac = (BaseFunction) RunTime.lookup("fac");
        System.out.println(fac.apply(10));
        Compiler c = new Compiler();
        fac = c.injectTracing(fac);
        // now function calls should be traced to sysout
        System.out.println(fac.apply(10));
    }

}

       
    


