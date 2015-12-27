package org.fun4j.compiler.expressions;

import java.util.ArrayList;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * A logical "if" expression.
 */
public class If
        extends Expression {

    Expression test;
    Expression thenDo;
    Expression elseDo;

    public String toString() {
        return "(if " + test + " " + thenDo + " " + elseDo + ")";
    }

    public If(final Expression eTest, final Expression eThen, final Expression eElse) {
        test = eTest;
        thenDo = eThen;
        elseDo = eElse;
    }

    @Override
    public ArrayList<Expression> getTailCalls() {
        ArrayList<Expression> result = new ArrayList<Expression>();
        result.addAll(thenDo.getTailCalls());
        result.addAll(elseDo.getTailCalls());
        return result;
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        // compiles test, der legt zur Laufzeit ein Boolean Objekt auf den Stack
        test.compile(mv);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        // Boolean -> bool
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");

        Label ifFalse = new Label();
        Label end = new Label();
        // test (test == true)
        mv.visitJumpInsn(IFEQ, ifFalse);

        // do if true
        thenDo.compile(mv);
        mv.visitJumpInsn(GOTO, end);

        // do if false
        mv.visitLabel(ifFalse);
        elseDo.compile(mv);

        mv.visitLabel(end);
    }
}