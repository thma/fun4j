package test.org.fun4j;

import static org.fun4j.Template.compile;
import static org.fun4j.Template.eval;
import static org.fun4j.Template.setUseBigInts;

import org.fun4j.Function;
import org.fun4j.compiler.CompilationException;
import org.fun4j.compiler.Expression;
import org.fun4j.compiler.RunTime;
import org.fun4j.compiler.expressions.CstInt;
import org.fun4j.compiler.expressions.If;
import org.fun4j.compiler.expressions.Recurse;
import org.fun4j.compiler.expressions.Sub1;
import org.fun4j.compiler.expressions.Var;
import org.fun4j.compiler.expressions.Zerop;
import org.junit.Test;

import test.org.fun4j.TailCall;
import static test.org.fun4j.TailCall.*;



public class TCO {
    
    private void print(Object o) {
        System.out.println(o);
    }
    
    @Test
    public void testTCO() throws CompilationException {
        setUseBigInts(false);        
        eval("(define a (lambda (n m) (if (zero? 0) (add1 m) (if (zero? 0) (a (sub1 n) 1) (a (sub1 n) (a n (sub1 m)))))))");
        Function a = (Function) RunTime.lookup("a");
        
        System.out.println(a.apply(3,4));
    }
    
    @Test
    public void testTCO2() throws CompilationException {
        setUseBigInts(false);
        //(define countdown (lambda (n) (if (zero? n) 0 (countdown (sub1 n)))))
        Expression expCount = new If(new Zerop(new Var(0, "n")), 
                                     new CstInt(0),
                                     new Recurse(new Sub1(new Var(0, "n"))));
        
        Function count = compile(expCount, null);
        // this would cause a stack overflow without tail call optimization:
        count.apply(1000000);
    }
    
    @Test
    public void testBigInts() throws CompilationException {
        print(eval("(* 7 6)"));
        
        print(eval("(+ 7 6)"));
        
        print(eval("(- 7 6)"));
        
        print(eval("(/ 7 6)"));
        
        print(eval("(= 7 6)"));
        
        print(eval("(+ 7 6)"));
        
        print(eval("(sub1 76)"));
        
        print(eval("(add1 76)"));
        
        print(eval("(if true true false)"));
        
        print(eval("(if (= 1 1) (* 12 3) 17)"));
        
        print(eval("(define fun (lambda (x) (* x x)))"));
        
        print(eval("(fun 16)"));
        
    }
    
    @Test
    public void testTrampoline() {
    	System.out.println(even(122256243).invoke());
    }

  public static TailCall<Boolean> even(final int n) {
    return (n == 0)
        ? done(true)
        : call(new TailCall<Boolean>() {
      @Override
      public TailCall<Boolean> apply() {
        return odd(n-1);
      }
    });
  }

  public static TailCall<Boolean> odd(final int n) {
    return (n == 0)
        ? done(false)
        : call(new TailCall<Boolean>() {
      @Override
      public TailCall<Boolean> apply() {
        return even(n-1);
      }
    });
  }
}



