package test.org.fun4j;

import static org.fun4j.Template.compile;
import static org.fun4j.Template.setUseBigInts;
import junit.framework.TestCase;

import org.fun4j.Function;
import org.fun4j.compiler.CompilationException;
import org.fun4j.compiler.Expression;
import org.fun4j.compiler.expressions.Add;
import org.fun4j.compiler.expressions.ApplyInline;
import org.fun4j.compiler.expressions.CstInt;
import org.fun4j.compiler.expressions.If;
import org.fun4j.compiler.expressions.Mul;
import org.fun4j.compiler.expressions.NumEq;
import org.fun4j.compiler.expressions.Recurse;
import org.fun4j.compiler.expressions.Sub1;
import org.fun4j.compiler.expressions.Var;
import org.junit.Test;

public class TestApplyInline extends TestCase{
    
    @Test
    public void testApplyInline() throws CompilationException {
        setUseBigInts(false);
           
        // f(x) => x
        Expression inlinedFunction = new Var(0);
                
        // sum(n,f) => Sum(0,n) f(i)
        Expression sum = new If(new NumEq(new Var(0), new CstInt(0)), 
                                new ApplyInline(inlinedFunction, new Var(0)), 
                                new Add(new ApplyInline(inlinedFunction, new Var(0)), 
                                    new Recurse(new Sub1(new Var(0)), inlinedFunction)));
        Function fSum = compile(sum, "Sum");
        assertEquals(5050, fSum.apply(100));
        
        // f(x) => x*x
        inlinedFunction = new Mul(new Var(0,"x"), new Var(0,"x"));
        
        // sumSqr(n,f) => SumSqr(0,n) f(i)
        Expression sumSqr = new If(new NumEq(new Var(0, "n"), new CstInt(0)), 
            new ApplyInline(inlinedFunction, new Var(0, "n")), 
            new Add(new ApplyInline(inlinedFunction, new Var(0, "n")), 
                new Recurse(new Sub1(new Var(0,"n")), inlinedFunction)));
        Function fSumSqr = compile(sumSqr, "SumSqr");
        System.out.println(fSumSqr);
        assertEquals(14, fSumSqr.apply(3));
        
        // f(x) => factorial(x)
//        inlinedFunction = new If(new NumEq(new CstInt(0), new Var(0)), 
//            new CstInt(1), new Mul(new Var(0), new Recurse( new Sub1(new Var(0)))));
//        
//        Expression sumFac = new If(new NumEq(new Var(0, "n"), new CstInt(0)), 
//            new ApplyInline(inlinedFunction, new Var(0, "n")), 
//            new Add(new ApplyInline(inlinedFunction, new Var(0, "n")), 
//                new Recurse(new Sub1(new Var(0,"n")), inlinedFunction)));
//        Function fSumFac = jlt.compile(sumFac, "SumFac");
//        System.out.println(fSumFac);
//        assertEquals(14, fSumFac.apply(3));
        
    }

}
