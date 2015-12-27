package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.fun4j.compiler.RunTime;
import org.objectweb.asm.MethodVisitor;

/**
 * constant Object expression.
 */
public class CstObj
        extends Expression {

    Object value;

    public CstObj(final Object o) {
        lineIncrement = 0;
        this.value = o;
    }

    public String toString() {
        return "" + value;
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        // The problem is to "transport" the value Object from compile-time to
        // runtime
        // To achieve this we store value in a constants Map and compile a
        // lookup of
        // the this constants based on its id

        // store constant in map and obtain it's id
        int constantId = RunTime.registerConstant(value);
        // at runtime we push the id and perform a lookup
        mv.visitLdcInsn(constantId);
        mv.visitMethodInsn(INVOKESTATIC, getName(RunTime.class), "getConstant", "(I)Ljava/lang/Object;");

        // clean up the constants map:
        //mv.visitLdcInsn(constantId);
        //mv.visitMethodInsn(INVOKESTATIC, getName(RunTime.class), "unregisterConstant", "(I)V");
    }
}