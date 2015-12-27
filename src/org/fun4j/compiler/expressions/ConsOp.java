package org.fun4j.compiler.expressions;

import org.fun4j.Cons;
import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A cons expression.
 */
public class ConsOp
        extends BinaryExp {

    public ConsOp(final Expression e1, final Expression e2) {
        super(e1, e2);
    }

    public String toString() {
        return "(cons " + e1 + " " + e2 + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        mv.visitTypeInsn(NEW, getName(Cons.class));
        mv.visitInsn(DUP);
        // compiles e1, e2, and adds an instruction to cons the two values
        e1.compile(mv);

        e2.compile(mv);

        mv.visitMethodInsn(INVOKESPECIAL, getName(Cons.class), "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V");
        // mv.visitInsn(ARETURN);
    }
}