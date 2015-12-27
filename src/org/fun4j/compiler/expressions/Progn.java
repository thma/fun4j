package org.fun4j.compiler.expressions;


import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A progn expression.
 */
public class Progn
        extends Expression {

    Expression[] exprs;

    public Progn(final Expression... expressions) {

        exprs = expressions;
    }

    public String toString() {
        String arglist = "";
        if (exprs != null) {
            for (int i = 0; i < exprs.length; i++) {
                arglist += exprs[i];
                if (i < exprs.length - 1) {
                    arglist += " ";
                }
            }
        }
        return "(progn " + arglist + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);        
        for (int i = 0; i < exprs.length; i++) {
            exprs[i].compile(mv);
            // only the value of the last expression must remain on the stack,
            // thus all other expressions are popped off:
            if (i < exprs.length-1) {
                mv.visitInsn(POP);
            }
        }

    }
    
}
