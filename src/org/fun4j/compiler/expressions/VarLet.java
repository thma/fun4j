package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A local variable expression for LET and LETREC.
 * LET and LETREC vars are handled as an additional array of parameters
 * that are passed into a function as an additonal bound variable.
 */
public class VarLet
        extends Expression {

    String name = "";
    int index;

    public VarLet(final int index) {
        this.index = index;
    }

    public VarLet(final int index, String var) {
        this.index = index;
        name = var;
    }

    public String toString() {
        return "letArgs[" + index + "]";
    }

    /**
     * apply(Object... args) is a varargs method. Thus all local variables are
     * passed in in one array and have to be looked up from there.
     */
    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);

        // push args array
        mv.visitVarInsn(ALOAD, 1);
        // load last entry
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISUB);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
        // load entry at index position
        mv.visitLdcInsn(index);
        mv.visitInsn(AALOAD);
        
    }
}