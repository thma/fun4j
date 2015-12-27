package test.org.fun4j;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses( { 
    TCO.class,
    TestApplyInline.class,
    TestJavaLispIntegration.class,
    TestTemplate.class,
    Curry.class,
    TestCons.class,
    TestVarargs.class,
    Assembler.class,
    TestLisp.class,
    TestFunctionalJavaExamples.class,
    Letrec.class
})

public class AllUnitTests {
    

}
