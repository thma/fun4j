package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * An i++ expression.
 */
public class Add1
        extends Expression {

    Expression e;

    public Add1(final Expression e1) {
        e = e1;
    }

    public String toString() {
        return "(add1 " + e + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        // compiles e1, e2, and adds an instruction to multiply the two values
        e.compile(mv);
        if (useBigIntArithmetics()) {
            mv.visitTypeInsn(CHECKCAST, "java/math/BigInteger");
            mv.visitFieldInsn(GETSTATIC, "java/math/BigInteger", "ONE", "Ljava/math/BigInteger;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/math/BigInteger", "add",
                    "(Ljava/math/BigInteger;)Ljava/math/BigInteger;");
        }
        else {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");

            mv.visitInsn(ICONST_1);
            mv.visitInsn(IADD);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        }
    }
}