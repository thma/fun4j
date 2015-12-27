package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A substraction expression.
 */
public class Sub
        extends BinaryExp {

    public Sub(final Expression e1, final Expression e2) {
        super(e1, e2);
    }

    public String toString() {
        return "(- " + e1 + " " + e2 +")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        // compiles e1, e2, and adds an instruction to substract the two values
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
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigInteger", "subtract",
                    "(Ljava/math/BigInteger;)Ljava/math/BigInteger;");
        }
        else {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");

            mv.visitInsn(ISUB);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        }
    }
}