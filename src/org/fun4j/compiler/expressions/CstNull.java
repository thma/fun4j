package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * null constant.
 */
public class CstNull
        extends Expression {
    
    public CstNull() {
        lineIncrement = 0;
    }

    public String toString() {
        return "null";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        mv.visitInsn(ACONST_NULL);
    }
}