package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * An explicit TailRecursion expression.
 */
public class TailRecurse
        extends Expression {

    Expression[] args;

    public TailRecurse(final Expression... e1) {
        args = e1;
    }

    public String toString() {
        String arglist = "";
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                arglist += args[i];
                if (i < args.length - 1) {
                    arglist += ",";
                }
            }
        }
        return "TCO_LOOP(" + arglist + ")";
    }

    /**
     * compare this code to {@link Recurse#compile(MethodVisitor)}. Recurse
     * creates a normal this.apply() method invocation. to avoid the performance
     * impact and the stack growth, Tail recurse just updates the local
     * variables with the values for the "recursive" call, but instead of a
     * costly recursive call it just creates a jump to the first instruction of
     * the apply() method.
     * 
     */
    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        if (args != null) {
            // push new array
            mv.visitLdcInsn(args.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            // mv.visitInsn(DUP);
            for (int i = 0; i < args.length; i++) {
                mv.visitInsn(DUP);
                mv.visitLdcInsn(i);
                args[i].compile(mv);
                mv.visitInsn(AASTORE);
            }
        }
        // update local variables for "recursive" re-entry
        mv.visitVarInsn(ASTORE, 1);
        // jump to first instruction of the method
        mv.visitJumpInsn(GOTO, methodEntry);
    }
}