package test.org.fun4j;

import junit.framework.TestCase;

import org.fun4j.Function;
import org.fun4j.compiler.BaseFunction;
import org.junit.Test;
import static org.fun4j.Template.bind;

public class Curry extends TestCase {

    @Test
    public void testCurry() {
        Function mul = new BaseFunction() {
            @Override
            public Object apply(Object... args) {
                Integer x = (Integer) args[0];
                Integer y = (Integer) args[1];
                return x * y;
            }
        };

        Function mul12 = bind(mul, 12);
        assertEquals(144, mul12.apply(12));
        
        Function mulSelf = bind(mul12, 11);
        assertEquals(132, mulSelf.apply());
        
        Function mul42 = bind(mul, 6, 7);
        assertEquals(42, mul42.apply());
        assertEquals(42,mul42.apply((Object[])null));
        mul42.toString();
        
        Function add = new Function(){
            @Override
            public Object apply(Object... args) {
                return (Integer) args[0] + (Integer) args[1];
            }    
        };   
        
        Function add100 = bind(add, 100);
        assertEquals(123, add100.apply(23));
        
        Function add200 = bind(add100, 100);
        assertEquals(200, add200.apply());
        
        Function add17and6 = bind(add, 17, 6);
        assertEquals(23, add17and6.apply());
        
        Function mulNull = bind(mul);
        assertEquals(mul, mulNull);
    }
    
    public void testTaxExample() {
        Function mul = new BaseFunction() {
            public Object apply(Object... args) {
                Double x = (Double) args[0];
                Double y = (Double) args[1];
                return x * y;
            }
        }; 
        
        Function germanVat = bind(mul, 0.19); 
        assertEquals(94.05 , germanVat.apply(495.00));
        
    }
    
    
    
}
