package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A Lookup expression. 
 * Represents Function objects that are looked up from the local map of a function instance.
 */
public class LookupLocal extends Expression {

    String strName = null;

    Expression eName = null;

    public LookupLocal(final String o) {
        lineIncrement = 0;
        this.strName = o;
    }

    public LookupLocal(final Expression e) {
        lineIncrement = 0;
        this.eName = e;
    }

    public String toString() {
        String displayName = null;
        if (strName != null) {
            displayName = strName;
        }
        else {
            displayName = eName.toString();
        }
        return "(lookupLocal " + displayName + ")";
    }

    public void compile(final MethodVisitor mv) {

        writeLineInfo(mv);
        mv.visitVarInsn(ALOAD, 0);
        if (strName != null) {
            mv.visitLdcInsn(strName);
        }
        else {
            eName.compile(mv);
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/fun4j/compiler/BaseFunction", "lookupLocal", "(Ljava/lang/String;)Lorg/fun4j/Function;");

    }

}