package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * True constant.
 */
public class CstTrue
        extends Expression {
    
    public CstTrue() {
        lineIncrement = 0;
        returnType = Boolean.class;
    }
    
    public String toString() {
        return "true";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        mv.visitTypeInsn(NEW, "java/lang/Boolean");
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Boolean", "<init>", "(Z)V");
    }
}