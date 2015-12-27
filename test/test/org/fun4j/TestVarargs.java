package test.org.fun4j;

import static org.fun4j.Template.eval;
import static org.fun4j.Template.setUseBigInts;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.fun4j.compiler.CompilationException;

public class TestVarargs extends TestCase {

    @org.junit.Test
    public void testVargs() throws CompilationException {
        setUseBigInts(true);
        eval("(define addall (lambda liste (if (null? liste) 0 (+ (car liste) (addall (cdr liste))))))");
        BigInteger result = (BigInteger) eval("(addall 1 2 3 4 5)");
        assertEquals(BigInteger.valueOf(15), result);
        
        result = (BigInteger) eval("(addall 15)");
        assertEquals(BigInteger.valueOf(15), result);
        
        result = (BigInteger) eval("(addall)");
        assertEquals(BigInteger.valueOf(0), result);
        
        result = (BigInteger) eval("(addall nil)");
        assertEquals(BigInteger.valueOf(0), result);
        
        result = (BigInteger) eval("(addall '(1 2 3 4 5))");
        assertEquals(BigInteger.valueOf(15), result);
        
        eval("(define (addall . liste) (if (null? liste) 0 (+ (car liste) (addall (cdr liste)))))");
        result = (BigInteger) eval("(addall 1 2 3 4 5)");
        assertEquals(BigInteger.valueOf(15), result);
                
    }
    
}
