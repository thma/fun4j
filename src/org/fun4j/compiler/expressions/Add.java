package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * An addition expression.
 */
public class Add
        extends BinaryExp {

    public Add(final Expression e1, final Expression e2) {
        super(e1, e2);
    }

    public String toString() {
        return "(+ " + e1 + " " + e2 + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        // compiles e1, e2, and adds an instruction to add the two values
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
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigInteger", "add",
                    "(Ljava/math/BigInteger;)Ljava/math/BigInteger;");
        }
        else {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");

            mv.visitInsn(IADD);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        }
    }
}