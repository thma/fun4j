package test.org.fun4j;

import static org.fun4j.Template.compile;
import static org.fun4j.Template.setUseBigInts;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.fun4j.Function;
import org.fun4j.Template;
import org.fun4j.compiler.BaseFunction;
import org.fun4j.compiler.CompilationException;
import org.fun4j.compiler.Expression;
import org.fun4j.compiler.expressions.Add;
import org.fun4j.compiler.expressions.Apply;
import org.fun4j.compiler.expressions.CstFalse;
import org.fun4j.compiler.expressions.CstInt;
import org.fun4j.compiler.expressions.CstTrue;
import org.fun4j.compiler.expressions.If;
import org.fun4j.compiler.expressions.LookupLocal;
import org.fun4j.compiler.expressions.Mul;
import org.fun4j.compiler.expressions.Sub1;
import org.fun4j.compiler.expressions.Var;
import org.fun4j.compiler.expressions.Zerop;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;


public class Letrec {

	// @Test
	public void testLetrecExp() throws CompilationException {
		setUseBigInts(false);

		/*
		 * (letrec (f1 17) // 17 is Var(0) (f1 (lambda (n) (+ (f2 n) (f3 n))))
		 * // f1 is Var(1) (f2 (lambda (n) n)) // f2 is Var(2) (f3 (lambda (n)
		 * (* n (-1 n))))) // f3 is Var(3)
		 */
		Expression f1 = new Add(new Apply(new Var(2), new Var(0)), new Apply(
				new Var(3), new Var(0)));
		Expression f2 = new Var(0);
		Expression f3 = new Mul(new Var(0), new Sub1(new Var(0)));

		Expression expr = new Apply(new Var(1), new CstInt(17), f1, f2, f3);

		Function letrec = compile(expr, "letrec");
		System.out.println(letrec.apply());
	}

	// @Test
	public void testLetrecExpSimple() throws CompilationException {
		setUseBigInts(false);

		// TestFun tf = new TestFun();

		/*
		 * (letrec (f1 3 4) // 3 is Var(0), 4 is Var(1) (f1 (lambda (m n) (+ m
		 * (f2 n)))) // f1 is Var(2) (f2 (lambda (a b c) (+ a (+ b c))))) // f2
		 * is Var(3)
		 */
		Expression f1 = new Add(new Var(0), new Apply(new Var(3), new Var(1),
				new CstInt(3), new CstInt(2)));
		Expression f2 = new Add(new Var(0), new Add(new Var(1), new Apply(
				new Var(4), new Var(0), new Var(1))));
		Expression f3 = new Add(new Var(0), new Var(1));

		Function funF1 = compile(f1, "F1");
		Function funF2 = compile(f2, "F2");
		Function funF3 = compile(f3, "F3");

		System.out.println(funF1);
		System.out.println(funF2);
		System.out.println(funF3);

		// Expression expr = new Apply(new Var(1), new CstInt(17), f1);

		// Function letrecSimple = jlt.compileExpression(expr, "letrecSimple");
		System.out.println(funF1.apply(3, 4, funF1, funF2, funF3));
	}

	@Test
	public void testLoadLocalVars() throws IOException, InstantiationException,
			IllegalAccessException, CompilationException {
		setUseBigInts(false);

		Expression expSqr = new Mul(new Var(0), new Var(0));
		Function funSqr = compile(expSqr, "sqr");
		Expression f1 = new Add(new Var(0), new Apply(new LookupLocal("sqr"),
				new Var(0)));
		Function funF1 = compile(f1, "F1");

		((BaseFunction) funF1).defineLocal("sqr", funSqr);

		System.out.println(funF1.apply(10));

	}

	@Test
	public void testLetrecManual() throws CompilationException {
//		 (letrec
//		 ((even? (lambda (n) (if (zero? n) true (odd? (sub1 n)))))
//		 (odd? (lambda (n) (if (zero? n) false (even? (sub1 n))))))
//		 (odd? 37))

		Expression expEven = new If(new Zerop(new Var(0, "n")), new CstTrue(),
				new Apply(new LookupLocal("odd?"), new Sub1(new Var(0, "n"))));
		BaseFunction funEven = compile(expEven, "even");

		Expression expOdd = new If(new Zerop(new Var(0, "n")), new CstFalse(),
				new Apply(new LookupLocal("even?"), new Sub1(new Var(0, "n"))));
		BaseFunction funOdd = compile(expOdd, "odd");

		Expression expBody = new Apply(new LookupLocal("odd?"), new CstInt(139));
		BaseFunction funBody = compile(expBody, "body");

		// set local environment containing all local bindings
		funBody.defineLocal("even?", funEven);
		funBody.defineLocal("odd?", funOdd);
		funEven.setLocalEnv(funBody.getLocalEnv());
		funOdd.setLocalEnv(funBody.getLocalEnv());

		assertTrue((Boolean) funBody.apply());
	}

	@Test
	public void testLambdaLifting() throws CompilationException,
			IOException {
		setUseBigInts(false);

		try {
			System.out.println(Template.eval("ev?"));
			fail();
		} catch (RuntimeException e) {
			// undefined symbol expected
		}	
		
		try {
			System.out.println(Template.eval("od?"));
			fail();
		} catch (RuntimeException e) {
			// undefined Symbol expected
		}
		
		String liftedLambdas= 
			"((lambda (ev? od?) (od? 207 ev? od?))" +
					"(lambda (n ev? od?) (if (zero? n) true (od? (sub1 n) ev? od?)))" +
					"(lambda (n ev? od?) (if (zero? n) false (ev? (sub1 n) ev? od?))))";
		
		assertTrue((Boolean) Template.eval(liftedLambdas));
	}


    @Test
    @Ignore
    public void testLetrecWithLambdas() throws CompilationException,
            IOException {
        setUseBigInts(false);

        String letrecTerm =
            "(letrec\n" +
                "((ev? (lambda (n) (if (zero? n) true (od? (sub1 n)))))\n" +
                " (od? (lambda (n) (if (zero? n) false (ev? (sub1 n))))))\n" +
                "(od? 37))";

        assertTrue((Boolean) Template.eval(letrecTerm));
    }
}
