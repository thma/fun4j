package org.fun4j.compiler.expressions;

import java.math.BigDecimal;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * Integer constant.
 */
public class CstDecimal
        extends Expression {

    Object value;

    public CstDecimal(final Object o) {
        lineIncrement = 0;
        this.value = o;
    }

    public String toString() {
        return "" + value;
    }

    public void compile(final MethodVisitor mv) {   
        writeLineInfo(mv);
        mv.visitLdcInsn(((BigDecimal) value).doubleValue());
        mv.visitMethodInsn(INVOKESTATIC, "java/math/BigDecimal", "valueOf", "(D)Ljava/math/BigDecimal;");
    }
}