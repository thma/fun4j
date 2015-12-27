package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * A logical "and" expression.
 */
public class And
        extends BinaryExp {

    public And(final Expression e1, final Expression e2) {
        super(e1, e2);
        returnType = Boolean.class;
    }
    
    public String toString() {
        return "(and " + e1 +" " + e2 + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        // prepare to create the resulting Boolean object
        mv.visitTypeInsn(NEW, "java/lang/Boolean");
        mv.visitInsn(DUP);
        // compiles e1, expected to push a Boolean
        e1.compile(mv);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        // Boolean -> bool
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        // tests if e1 is false
        mv.visitInsn(DUP);
        Label end = new Label();
        mv.visitJumpInsn(IFEQ, end);
        // case where e1 is true : e1 && e2 is equal to e2
        mv.visitInsn(POP);
        e2.compile(mv);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
        // Boolean -> bool
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        // if e1 is false, e1 && e2 is equal to e1:
        // we jump directly to this label, without evaluating e2
        mv.visitLabel(end);
        // create a Boolean from the bool value
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Boolean", "<init>", "(Z)V");
    }
}