package org.fun4j.compiler.expressions;

import org.fun4j.Cons;
import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A Tl() expression.
 */
public class Tl
        extends Expression {

    Expression e;

    public Tl(final Expression e) {
        this.e = e;
    }

    public String toString() {
        return "(cdr " + e + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        e.compile(mv);
        mv.visitTypeInsn(CHECKCAST, getName(Cons.class));
        mv.visitMethodInsn(INVOKEVIRTUAL, getName(Cons.class), "getTl", "()Ljava/lang/Object;");
    }
}