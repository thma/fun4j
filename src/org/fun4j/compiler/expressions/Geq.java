package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * A "Greater then or equal" expression.
 */
public class Geq
        extends BinaryExp {

    public Geq(final Expression e1, final Expression e2) {
        super(e1, e2);
        returnType = Boolean.class;
    }

    public String toString() {
        return "(>= " + e1 + " " + e2 + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        // compiles e1, e2, and adds the instructions to compare the two values
        e1.compile(mv);
        if (useBigIntArithmetics()) {
            mv.visitTypeInsn(CHECKCAST, "java/math/BigInteger");
        }
        else {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
        }

        e2.compile(mv);
        if (useBigIntArithmetics()) {
            mv.visitTypeInsn(CHECKCAST, "java/math/BigInteger");
        }
        else {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
        }


        Label iftrue = new Label();
        Label end = new Label();
        if (useBigIntArithmetics()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigInteger", "compareTo", "(Ljava/math/BigInteger;)I");
            mv.visitJumpInsn(IFGE, iftrue);
        }
        else {
            mv.visitJumpInsn(IF_ICMPGE, iftrue);
        }
        
        
        // case where !(e1 >= e2) : pushes false and jump to "end"
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