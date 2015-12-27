package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * String constant.
 */
public class CstString
        extends Expression {

    public String value;

    public CstString(final String o) {
        lineIncrement = 0;
        this.value = o;
    }

    public String toString() {
        return value;
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        mv.visitLdcInsn(value);
    }
}