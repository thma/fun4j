package test.org.fun4j;

import static org.fun4j.Template.eval;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.fun4j.Cons;
import org.fun4j.compiler.CompilationException;


public class TestCons extends TestCase {

    public void testSetCons() {
        Cons cons = new Cons(null, null);
        cons.setHd(7);
        cons.setTl(8);
        assertEquals(7, cons.getHd());
        assertEquals(8,cons.getTl());
        
    }
    
    public void testEquals() {
        Cons dit = new Cons(1,new Cons(2, null));
        Cons dat = new Cons(dit, null);
        
        assertFalse(dit.equals(798));
        
        assertFalse(dit.equals(null));
        assertFalse(dit.equals(dat));
        assertTrue(dit.equals(dit));
        assertTrue(dat.equals(dat));
        
        assertFalse(dat.equals(new Cons(dit, 12)));
        
        dat = new Cons(null, dit);
        
        assertTrue(dat.equals(new Cons(null, dit)));
        assertFalse(dat.equals(new Cons(null, null)));
        assertFalse(dat.equals(null));
        
        dat.setTl(dat);
        assertFalse(dat.equals(dit));
        assertFalse(dit.equals(dat));
        assertTrue(dat.equals(dat));
        
        dit = new Cons(null, null);
        assertTrue(dit.equals(new Cons(null, null)));
        
        assertFalse(dit.equals(new Cons(null, 8)));
        
    }
    
    public void testPrintDepth() throws CompilationException {
        Cons.printdepth(3);
        Cons.printdepth(BigInteger.valueOf(3));
        Cons.printlength(3);
        Cons.printlength(BigInteger.valueOf(3));
        
        Cons list = (Cons) eval("'(1 2 3 4 5 6 7 8 9 10)");
        assertEquals("(1 2 3 4 ***)", list.toString());
        list.prettyPrint();
        
        list = (Cons) eval("'(+ (+ (+ (+ (+ (+ (+ 1) 2) 3) 4) 5) 6 ) 7 )");
        assertEquals("(+ (+ (+ (+ *** 4) 5) 6) 7)", list.toString());
        list.prettyPrint();
        
        new Cons(1,2).toString();
        new Cons(1,2).prettyPrint();
        new Cons(null,2).toString();
        new Cons(null,2).prettyPrint();        
        
    }
    
    public void testPrettyPrint() throws CompilationException {
        Cons list = (Cons) eval("'(lambda (n m) (if (= n 0) m (+ m n)))");
        list.prettyPrint();
    }
    
}
