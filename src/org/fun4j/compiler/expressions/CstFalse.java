package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * False constant.
 */
public class CstFalse
        extends Expression {
    
    public CstFalse() {
        lineIncrement = 0;
        returnType = Boolean.class;
    }
    
    public String toString() {
        return "false";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        mv.visitTypeInsn(NEW, "java/lang/Boolean");
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Boolean", "<init>", "(Z)V");
    }
}