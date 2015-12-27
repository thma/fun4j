package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * A nullp() expression.
 */
public class Nullp
        extends Expression {

    Expression e;

    public Nullp(final Expression e) {
        this.e = e;
        returnType = Boolean.class;
    }

    public String toString() {
        return "(null? " + e + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        e.compile(mv);

        Label iftrue = new Label();
        Label end = new Label();
        // if (exp == null) goto iftrue
        mv.visitJumpInsn(IFNULL, iftrue);
        // case where !null : pushes false and jump to "end"
        mv.visitTypeInsn(NEW, "java/lang/Boolean");
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Boolean", "<init>", "(Z)V");

        mv.visitJumpInsn(GOTO, end);
        // case where e1 >= e2 : pushes true
        mv.visitLabel(iftrue);

        mv.visitTypeInsn(NEW, "java/lang/Boolean");
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Boolean", "<init>", "(Z)V");

        mv.visitLabel(end);
    }
}