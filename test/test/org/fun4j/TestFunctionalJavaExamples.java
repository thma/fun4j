package test.org.fun4j;

import static org.fun4j.Collections.asCollection;
import static org.fun4j.Collections.foldleft;
import static org.fun4j.Collections.foldright;
import static org.fun4j.Collections.filter;
import static org.fun4j.Collections.sort;
import static org.fun4j.Functions.callsTo;
import static org.fun4j.Functions.functionFor;
import static org.fun4j.Functions.functionFromMethod;
import static org.fun4j.Template.compile;
import static org.fun4j.Template.define;
import static org.fun4j.Template.eval;
import static org.fun4j.Template.fn;
import static org.fun4j.Template.predicate;
import static org.fun4j.Template.setUseBigInts;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.fun4j.Collections;
import org.fun4j.Function;
import org.fun4j.Predicate;
import org.fun4j.compiler.CompilationException;


public class TestFunctionalJavaExamples extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setUseBigInts(false);
    }

    public void testExists() throws CompilationException, SecurityException, NoSuchMethodException {
        Collection<String> col = asCollection("Hello", "There", "what", "DAY", "iS", "iT");
        final Predicate isLowerCase = (Predicate) functionFromMethod(Character.class.getMethod("isLowerCase", char.class));
        define("isLowerCase", isLowerCase);
        // 1. define function in Java Syntax:
        assertTrue(
            Collections.exists(new Predicate() {
                public Boolean apply(Object... args) {
                    return Collections.forAll(isLowerCase, Collections.fromString((String) args[0]));
                }
            }, col)
        );
    
        // 2. define function in LISP Syntax:    
        assertTrue(
            Collections.exists(predicate("(lambda (str) (Collections.forAll isLowerCase (Collections.fromString str) ))"), col)
        );    
    }

    public void testFilter() throws CompilationException {
        Collection<Integer> col = asCollection(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);
        Collection<Integer> result = filter(predicate("(lambda (n) (zero? (% n 2)))"), col);
        System.out.println(result);
        assertEquals("[44, 22, 90, 98, 1078, 6, 64, 6, 42]", result.toString());
    }

    public void testFoldleft() throws CompilationException {
        Collection<Integer> col = asCollection(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);
        Integer result = foldleft(fn("(lambda (i j) (+ i j))"), 0, col);
        System.out.println(result);
        assertEquals(new Integer(1774), result);
    }

    public void testForAll() throws CompilationException, SecurityException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        Collection<String> col = asCollection("hello", "There", "what", "day", "is", "it");
        final Predicate isLowerCase = (Predicate) functionFromMethod(Character.class.getMethod("isLowerCase", char.class));
        define("isLowerCase", isLowerCase);
               
        // 1. define function in Java Syntax:
        assertFalse(
            Collections.forAll(new Predicate() {
                public Boolean apply(Object... args) {
                    return Collections.forAll(isLowerCase, Collections.fromString((String) args[0]));
                }
            }, col)
        );

        // 2. define function in LISP Syntax:
        assertFalse(
            Collections.forAll(predicate("(lambda (str) (Collections.forAll isLowerCase (Collections.fromString str) ))"), col)
        );
    }
    
    public void testMap() throws CompilationException {
        Collection<Integer> ints = asCollection(1, 2, 3);
        Collection<Integer> actual = Collections.map(fn("(lambda (i) (+ i 42))"), ints);
        assertEquals(asCollection(43, 44, 45), actual);
    }
    
    public void testSort() throws CompilationException {
        Predicate intOrd = predicate("(lambda (x y) (> x y))");
        List<Integer> list = (List<Integer>) asCollection(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);

        assertEquals(asCollection(1, 3, 6, 6, 22, 42, 44, 64, 67, 77, 79, 90, 97, 98, 1078), sort(list,intOrd));
        assertEquals(asCollection(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42), list);
    }

    public void testFuncitoStyleFunctionBuilding() throws CompilationException, SecurityException,
            NoSuchMethodException, InstantiationException, IllegalAccessException {
        //0.
        Function sin = functionFromMethod(Math.class.getMethod("sin", double.class));
        System.out.println(sin.apply(Math.PI/2.0));
        
        // 1a. getSize:
        Function size = functionFromMethod(Box.class.getMethod("getSize", (Class<?>[]) null));
        Box box = new Box(0.0, 0.0, 100.0, 200.0);
        assertEquals(20000.0, size.apply(box));

        // 1b. getSize funcito style:
        size = functionFor(callsTo(Box.class).getSize());
        assertEquals(20000.0, size.apply(box));

        // 2a. contains:
        Function contains = functionFromMethod(Box.class.getMethod("contains", Double.class, Double.class));
        assertTrue((Boolean) contains.apply(box, 40.0, 40.0));
        assertFalse((Boolean) contains.apply(box, 40.0, 400.0));

        //2b. contains funcito style:
        contains = functionFor(callsTo(Box.class).contains(0.0, 0.0));
        assertTrue((Boolean) contains.apply(box, 40.0, 40.0));
        assertFalse((Boolean) contains.apply(box, 40.0, 400.0));

        // 3a. getConstant:
        Function staticConstant = functionFromMethod(Box.class.getMethod("getConstant"));
        assertEquals(42, staticConstant.apply());
        
        // 3b. getConstant funcito style: static methods not supported due to problem in CGLIB
        // static methods lead to errors in cglib proxy code generation
        // @SuppressWarnings("static-access")
        // staticConstant = functionFor(callsTo(Box.class).getConstant());
        // assertEquals(42, staticConstant.apply());
    }

    public void testFoldright() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Function add = compile("(lambda (x y) (+ x y))");
        Integer actual = (Integer) foldright(add, 0, col);
        assertEquals(new Integer(55), actual);

        Function mul = compile("(lambda (x y) (* x y))");
        eval("(define fac (lambda (n) (if (= n 0) 1 (* n (fac (sub1 n))))))");

        actual = Collections.foldright(mul, 1, col);
        Integer expected = (Integer) eval("(fac 10)");

        assertEquals(expected, actual);
    }

    public void testTemplateFilterEven() throws CompilationException {
        Collection<Integer> col = asCollection(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Predicate even = predicate("(lambda (n) (zero? (% n 2)))");
        Collection<Integer> result = Collections.filter(even, col);
        assertEquals("[2, 4, 6, 8, 10]", result.toString());
    }

}
