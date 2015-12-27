package test.org.fun4j;

import static org.fun4j.Collections.asCollection;
import static org.fun4j.Collections.asCons;
import static org.fun4j.Collections.exists;
import static org.fun4j.Collections.filter;
import static org.fun4j.Collections.foldleft;
import static org.fun4j.Collections.foldright;
import static org.fun4j.Collections.foldrightIterator;
import static org.fun4j.Collections.forAll;
import static org.fun4j.Collections.fromCons;
import static org.fun4j.Collections.map;
import static org.fun4j.Functions.compose;
import static org.fun4j.Functions.constant;
import static org.fun4j.Template.compile;
import static org.fun4j.Template.eval;
import static org.fun4j.Template.fn;
import static org.fun4j.Template.parse;
import static org.fun4j.Template.predicate;
import static org.fun4j.Template.runFile;
import static org.fun4j.Template.setUseBigInts;
import static org.fun4j.Template.setWriteFile;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.fun4j.Collections;
import org.fun4j.Cons;
import org.fun4j.Function;
import org.fun4j.Predicate;
import org.fun4j.Template;
import org.fun4j.Version;
import org.fun4j.compiler.CompilationException;

public class TestTemplate extends TestCase {

    public void testTemplateMap() throws CompilationException {
        setWriteFile(true);
        Collection<Integer> col = asCollection(1, 2, 3, 4);
        Function x3 = compile("(lambda (x) (* x (* x x)))");
        Collection<Integer> result = map(x3, col);
        assertEquals("[1, 8, 27, 64]", result.toString());
        setWriteFile(false);
    }

    public void testTemplateMap2() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4);
        Function square = compile("(lambda (x) (* x x))");
        Collection<Integer> result = map(square, col);
        assertEquals("[1, 4, 9, 16]", result.toString());
    }

    public void testTemplateMapJava() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4);

        Function pow2 = new Function() {
            @Override
            public Object apply(Object... args) {
                return (Integer) args[0] * (Integer) args[0];
            }
        };

        Collection<Integer> result = map(pow2, col);
        assertEquals("[1, 4, 9, 16]", result.toString());

        // define a predicate that returns true for even numbers
        Predicate even = new Predicate() {
            @Override
            public Boolean apply(Object... args) {
                Integer i = (Integer) args[0];
                return (i % 2 == 0);
            }
        };

        // compute pow2 for all even numbers in col
        result = map(pow2, filter(even, col));
        assertEquals("[4, 16]", result.toString());
    }

    public void testCovariance() throws CompilationException {
        // define a predicate that returns true for even numbers
        Predicate even = new Predicate() {
            @Override
            public Boolean apply(Object... args) {
                Integer i = (Integer) args[0];
                return (i % 2 == 0);
            }
        };

        printMethods(even);

        Function fun = even;
        printMethods(fun);
        System.out.println(fun.apply(39));

        even = (Predicate) fun;
        printMethods(even);
        System.out.println(even.apply(32));

        setUseBigInts(false);
        even = predicate("(\\ (n) (= 0 (% n 2)))");
        printMethods(even);
        System.out.println(even.apply(32));

    }

    private void printMethods(Object fun) {
        Method[] methods = fun.getClass().getMethods();
        for (Method method : methods) {
            if ("apply".equals(method.getName())) {
                System.out.println(method.toGenericString());
            }
        }
    }

    public void testTemplateExists() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4);
        Predicate predicate = (Predicate) fn("(lambda (n) (= 3 n))");
        boolean result = exists(predicate, col);
        assertTrue(result);
        col = asCollection(11, 12, 13, 14);
        result = exists(predicate, col);
        assertFalse(result);
    }

    public void testTemplateForAll() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4);
        Predicate predicate = predicate("(lambda (n) (> 10 n))");
        boolean result = forAll(predicate, col);
        assertTrue(result);
        col = asCollection(6, 7, 8, 10);
        result = forAll(predicate, col);
        assertFalse(result);
    }

    public void testTemplateFilter() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Predicate predicate = predicate("(lambda (n) (> 5 n))");
        Collection<Integer> result = filter(predicate, col);
        assertEquals("[1, 2, 3, 4]", result.toString());
    }

    @SuppressWarnings("unchecked")
    public void testConsToCollection() throws CompilationException {
        Cons lispList = (Cons) eval("'(around the world in eighty days)");
        Collection<Object> result = fromCons(lispList);
        assertTrue(result.contains("world"));

        result = fromCons((Cons) null);
        assertNull(result);
    }

    public void testCollectionToCons() {
        Collection<Integer> col = asCollection(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Cons ints = asCons(col);
        assertEquals("(1 2 3 4 5 6 7 8 9 10)", ints.toString());

        ints = asCons(null);
        assertNull(ints);
    }

    public void testFoldright() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Function add = compile("(lambda (x y) (+ x y))");
        Integer actual = foldrightIterator(add, 0, col.iterator());
        assertEquals(new Integer(55), actual);

        Function mul = compile("(lambda (x y) (* x y))");
        eval("(define fac (lambda (n) (if (= n 0) 1 (* n (fac (sub1 n))))))");

        actual = foldright(mul, 1, col);
        Integer expected = (Integer) eval("(fac 10)");

        assertEquals(expected, actual);
    }

    public void testFoldrightJava() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Function add = new Function() {
            @Override
            public Object apply(Object... args) {
                return (Integer) args[0] + (Integer) args[1];
            }
        };
        Integer sum = foldright(add, 0, col);
        assertEquals(new Integer(55), sum);
    }

    public void testFoldleft() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Function cons2 = compile("(lambda (ys x) (cons x ys))");
        Cons reversed = foldleft(cons2, null, col);
        assertEquals(new Integer(10), reversed.getHd());

        col = new ArrayList<Integer>();
        reversed = foldleft(cons2, null, col);
        assertNull(reversed);
    }

    public void testRunFile() throws CompilationException {
        runFile("qs.scm");
        Cons sorted = (Cons) eval("(qs '(8 5 7 9 0 3 1 4 2 6 ))");
        Cons expected = (Cons) parse("(0 1 2 3 4 5 6 7 8 9)");
        assertEquals(expected, sorted);

    }

    public void testCompose() throws CompilationException {
        Function f = compile("(lambda (x) (+ 1 x))");
        Function g = compile("(lambda (x) (* 2 x))");
        Function h = compile("(lambda (x) (- x 3))");

        Function oh = compose(h);
        assertEquals(new Integer(7), oh.apply(10));

        Function goh = compose(g, h);
        assertEquals(new Integer(14), goh.apply(10));

        Function fogoh = compose(f, compose(g, h));
        assertEquals(new Integer(15), fogoh.apply(10));

        fogoh = compose(f, g, h);
        assertEquals(new Integer(15), fogoh.apply(10));

        assertEquals(null, compose((Function[]) null));
    }

    public void testConstant() {
        BigInteger fourtyTwo = BigInteger.valueOf(42);
        Function f = constant(fourtyTwo);

        assertEquals(fourtyTwo, f.apply());
    }

    public void testFn() throws CompilationException {
        Function n2 = fn("(lambda (n) (* n n))");
        assertEquals(new Integer(144), n2.apply(12));
    }

    public void testVersion() {
        new Version();
        Version.getVersion();
        Version.getVersionLong();
    }

    public void testEval() {
        try {
            eval("(lambda (* n n))S");
            fail("should not compile");
        }
        catch (CompilationException e) {
        }
    }

    public void testTemplateFilterEven() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Predicate even = predicate("(lambda (n) (= 0 (% n 2)))");
        Collection<Integer> result = filter(even, col);
        assertEquals("[2, 4, 6, 8, 10]", result.toString());
    }
    
    public void testSort() throws CompilationException {
    	 List<Integer> col = (List<Integer>) asCollection(10, 8, 9, 6, 7, 4, 5, 2, 3, 1);
    	 Predicate gt = predicate("(lambda (a b) (> a b))");
    	 assertTrue(gt.apply(3, 2));
    	 assertFalse(gt.apply(3, 3));
    	 assertFalse(gt.apply(2, 3));
    	 List<Integer> result = Collections.sort(col, gt);    	 
    	 assertEquals((List<Integer>) asCollection(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), result);
    	 // really non-destructive ?
    	 assertEquals(asCollection(10, 8, 9, 6, 7, 4, 5, 2, 3, 1), col);
    }

}
