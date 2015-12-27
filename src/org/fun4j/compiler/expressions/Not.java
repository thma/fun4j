package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A logical "not" expression.
 */
public class Not
        extends Expression {

    Expression e;

    public Not(final Expression e) {
        this.e = e;
        returnType = Boolean.class;
    }
    
    public String toString() {
        return "(not " + e + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        // prepare to create the resulting Boolean object
        mv.visitTypeInsn(NEW, "java/lang/Boolean");
        mv.visitInsn(DUP);
        // computes !e1 by evaluating 1 - e1
        mv.visitLdcInsn(new Integer(1));
        e.compile(mv);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        // Boolean -> bool
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        mv.visitInsn(ISUB);
        // create a Boolean from the bool value
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Boolean", "<init>", "(Z)V");

    }
}