package test.org.fun4j;

import static org.fun4j.Template.compile;
import static org.fun4j.Template.eval;
import static org.fun4j.Template.setUseBigInts;

import java.io.IOException;
import java.math.BigInteger;

import junit.framework.TestCase;

import org.fun4j.Cons;
import org.fun4j.Function;
import org.fun4j.compiler.BaseFunction;
import org.fun4j.compiler.CompilationException;
import org.fun4j.compiler.Expression;
import org.fun4j.compiler.RunTime;
import org.fun4j.compiler.expressions.Add;
import org.fun4j.compiler.expressions.Add1;
import org.fun4j.compiler.expressions.Apply;
import org.fun4j.compiler.expressions.ConsOp;
import org.fun4j.compiler.expressions.CstInt;
import org.fun4j.compiler.expressions.CstNull;
import org.fun4j.compiler.expressions.Geq;
import org.fun4j.compiler.expressions.Hd;
import org.fun4j.compiler.expressions.If;
import org.fun4j.compiler.expressions.Lookup;
import org.fun4j.compiler.expressions.Mul;
import org.fun4j.compiler.expressions.NumEq;
import org.fun4j.compiler.expressions.Progn;
import org.fun4j.compiler.expressions.Recurse;
import org.fun4j.compiler.expressions.SetVar;
import org.fun4j.compiler.expressions.Sub;
import org.fun4j.compiler.expressions.Sub1;
import org.fun4j.compiler.expressions.TailRecurse;
import org.fun4j.compiler.expressions.Var;
import org.fun4j.compiler.expressions.Zerop;

public class Assembler
        extends TestCase {

    public void testAll() throws CompilationException {
        setUseBigInts(false);

        // f(x) => x*x
        Expression expPower = new Mul(new Var(0), new Var(0));

        Function power = compile(expPower, "Power");
        System.out.println(power.apply(7));

        RunTime.define("power", power);

        Expression expAdd = new Add(new Var(0), new Var(1));
        Function add = compile(expAdd, "add");

        System.out.println(power.apply(add.apply(3, 4)));

        // sum(n,f) => Sum(0,n) f(i)
        Expression sum = new If(new NumEq(new Var(0), new CstInt(0)), new Apply(new Var(1), new Var(0)), new Add(
                new Apply(new Var(1), new Var(0)), new Recurse(new Sub1(new Var(0)), new Var(1))));
        Function iSum = compile(sum, null);

        // id: f(x) => x
        Function id = compile(new Var(0), "Id");

        // testing higher order functions, computing sum(x) and sum(x*x)
        System.out.println(iSum.apply(100, id));
        System.out.println(iSum.apply(2, power));

        // f(x) => factorial(x)
        Expression fac = new If(new NumEq(new CstInt(0), new Var(0)), new CstInt(1), new Mul(new Var(0), new Recurse(
                new Sub1(new Var(0)))));

        Function iFac = compile(fac, "Fac");
        RunTime.define("fac", iFac);
        System.out.println(iFac.apply(10, 0));

        // f(x) => fib(x)
        Expression fib = new If(new NumEq(new CstInt(0), new Var(0)), new CstInt(0), new If(new NumEq(new CstInt(1),
                new Var(0)), new CstInt(1), new Add(new Recurse(new Sub1(new Var(0))), new Recurse(new Sub(new Var(0),
                new CstInt(2))))));

        Function iFib = compile(fib, "Fib");

        long start = System.currentTimeMillis();
        System.out.println(iFib.apply(30));
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("time [ms]: " + elapsed);

        Fibo nativeFib = new Fibo();
        start = System.currentTimeMillis();
        System.out.println(nativeFib.apply(30));
        elapsed = System.currentTimeMillis() - start;
        System.out.println("time [ms]: " + elapsed);
        System.out.println("recursice calls " + nativeFib.count);

        // String[] arg = {"Fib"};
        // ASMifierClassVisitor.main(arg);

        // f(n,m) => a(n,m)
        Expression ack = new If(new NumEq(new Var(0), new CstInt(0)), new Add1(new Var(1)), new If(new NumEq(
                new Var(1), new CstInt(0)), new Recurse(new Sub1(new Var(0)), new CstInt(1)), new Recurse(new Sub1(
                new Var(0)), new Recurse(new Var(0), new Sub1(new Var(1))))));

        Function iAck = compile(ack, null);
        System.out.println(iAck.apply(3, 3));

        // f(x,y,z) => tak(x,y,z)
        Expression tak = new If(new Geq(new Var(1), new Var(0)), new Var(2), new Recurse(new Recurse(new Sub1(
                new Var(0)), new Var(1), new Var(2)), new Recurse(new Sub1(new Var(1)), new Var(2), new Var(0)),
                new Recurse(new Sub1(new Var(2)), new Var(0), new Var(1))));

        Function iTak = compile(tak, "Tak");
        System.out.println(iTak.apply(18, 12, 6));

        Object[] ints = { 19, 13, 7 };
        System.out.println(iTak.apply(ints));

        Expression test = new Hd(new ConsOp(new CstInt(1), new ConsOp(new CstInt(2), new ConsOp(new CstInt(3),
                new CstNull()))));
        Function iTest = compile(test, "Test");
        System.out.println(iTest.apply());
        System.out.println(new Cons(1, new Cons(2, new Cons(3, null))).getHd());

        // TCO
        // "if (args[0]==0) {args[1]} else {this.apply(args[0]-1,args[2],args[1] + args[2])}"
        Expression fib2 = new If(new NumEq(new Var(0), new CstInt(0)), new Var(1), new TailRecurse(
                new Sub1(new Var(0)), new Var(2), new Add(new Var(1), new Var(2))));
        Function iFib2 = compile(fib2, "Fib2");

        start = System.currentTimeMillis();
        System.out.println(iFib2.apply(4000, 0, 1));
        elapsed = System.currentTimeMillis() - start;
        System.out.println("time [ms]: " + elapsed);

        Expression fib3 = new If(new NumEq(new Var(0), new CstInt(0)), new Var(1), new Recurse(new Sub1(new Var(0)),
                new Var(2), new Add(new Var(1), new Var(2))));
        Function iFib3 = compile(fib3, "Fib3");
        start = System.currentTimeMillis();
        System.out.println(iFib3.apply(4000, 0, 1));
        elapsed = System.currentTimeMillis() - start;
        System.out.println("time [ms]: " + elapsed);

        RunTime.define("fibo", nativeFib);
        Function iFibo = compile("(fibo 20)");
        System.out.println(iFibo.apply());

    }
    
    public void testZerop() throws CompilationException, IOException {
        setUseBigInts(false);
        // f(i) => factorial(i)
        Expression expFac = 
          new If(new Zerop(new Var(0,"i")), 
                new CstInt(1), 
                new Mul(new Var(0), new Recurse(new Sub1(new Var(0)))));

        Function fac = compile(expFac, "Fac");
        assertEquals(3628800, fac.apply(10));
        
        setUseBigInts(true);
        fac = compile(expFac, "Fac");
        assertEquals(BigInteger.valueOf(3628800), fac.apply(BigInteger.TEN));

    }


    public void testJavaFunction() {
        Function hash = new Function() {
            @Override
            public Object apply(Object... args) {
                return args[0].hashCode();
            }
        };

        System.out.println(hash.apply("hello world"));

        Function add1 = new Function() {
            @Override
            public Object apply(Object... args) {
                return (Integer) args[0] + (Integer) args[1];
            }
        };

        System.out.println(add1.apply(17, 6));
    }
    
    public void testProgn() throws CompilationException {
        setUseBigInts(false);
        Expression expPower = new Mul(new Var(0), new Var(0));
        Expression expDouble = new Add(new Var(0), new Var(0));
        Expression expZero = new Sub(new Var(0), new Var(0));
        
        Expression prog = new Progn(expDouble, expZero, expPower);
        Function progFun = compile(prog, "prog");
        assertEquals(144,progFun.apply(12));
        
    }
    
    public void testSetVar() throws CompilationException {
        setUseBigInts(false);
        // test locals vars
        Expression expPower = new Mul(new Var(0), new Var(0));
        Expression expSet = new SetVar(new Var(0), new Add(new Var(0), new Var(0)));
        Expression prog = new Progn(expPower, expSet, expPower);
        Function progFun = compile(prog, "prog");
        assertEquals(196,progFun.apply(7));
        
        // test global vars
        expSet = new SetVar(new Lookup("test"), new CstInt(100));
        progFun = compile(expSet, "prog");
        progFun.apply();
        assertEquals(104, eval("(+ test 4)"));
    }

}

class Fibo
        extends BaseFunction {

    public long count = 0;

    @Override
    public Object apply(Object... args) {
        count++;
        int i = ((Integer) args[0]).intValue();
        if (i == 0) {
            return new Integer(0);
        }
        else if (i == 1) {
            return new Integer(1);
        }
        else {
            int j = (Integer) apply(i - 1);
            int k = (Integer) apply(i - 2);
            return new Integer(j + k);
        }
    }
}
