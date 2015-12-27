package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * Integer constant.
 */
public class CstInt
        extends Expression {

    Object value;

    public CstInt(final Object o) {
        lineIncrement = 0;
        this.value = o;
    }

    public String toString() {
        return "" + value;
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        if (useBigIntArithmetics()) {
        	Number numValue  = (Number) value;    	
            mv.visitLdcInsn(numValue.longValue());
            mv.visitMethodInsn(INVOKESTATIC, "java/math/BigInteger", "valueOf", "(J)Ljava/math/BigInteger;");
        }
        else {
            // pushes the constant's value onto the stack
            mv.visitLdcInsn(value);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        }
    }
}