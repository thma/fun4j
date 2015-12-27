package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.fun4j.compiler.RunTime;
import org.fun4j.compiler.Symbols;
import org.objectweb.asm.MethodVisitor;

/**
 * A Lookup expression. Represents Objects that are looked up in the RunTime global Map
 */
public class Lookup extends Expression {

    String strName = null;

    public String getStrName() {
        return strName;
    }

    public Expression geteName() {
        return eName;
    }
    
    Expression eName = null;

    public Lookup(final String o) {
        lineIncrement = 0;
        this.strName = o;
    }

    public Lookup(final Expression e) {
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
        return "(lookup " + displayName + ")";
    }

    public void compile(final MethodVisitor mv) {

        writeLineInfo(mv);
        if (strName != null) {
            mv.visitLdcInsn(strName);
        }
        else {
            eName.compile(mv);
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
        }
        mv.visitMethodInsn(INVOKESTATIC, getName(RunTime.class), Symbols.OP_LOOKUP,
            "(Ljava/lang/String;)Ljava/lang/Object;");
    }

}