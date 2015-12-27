package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
//import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;

/**
 * A local variable expression.
 */
public class Var
        extends Expression {

    String name = "";
    int index;
    
    public int getIndex() {
        return index;
    }

    public Var(final int index) {
        this.index = index;
    }

    public Var(final int index, String var) {
        this.index = index;
        name = var;
    }

    public String toString() {
        return name;
    }

    
    /**
     * apply(Object... args) is a varargs method. Thus all local variables are
     * passed in in one array and have to be looked up from there.
     */
    public void compile(final MethodVisitor mv) {
        //
//        Label l_start = new Label();
//        Label l_end = new Label();
//        Label l_handler = new Label();
//        Label l_END = new Label();
//        mv.visitTryCatchBlock(l_start, l_end, l_handler, "java/lang/ArrayIndexOutOfBoundsException");
//        mv.visitLabel(l_start);        
        /////////////// actual Variable lookup happens here:
        // push args array
        mv.visitVarInsn(ALOAD, 1);
        mv.visitLdcInsn(index);
        // push args[index]
        mv.visitInsn(AALOAD);
        ////////////////////
//        mv.visitLabel(l_end);
//        mv.visitJumpInsn(Opcodes.GOTO, l_END);
//        
//        mv.visitLabel(l_handler);
//        mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/ArrayIndexOutOfBoundsException"});
//        mv.visitVarInsn(ASTORE, 2);
//
//        mv.visitTypeInsn(NEW, "org/fun4j/compiler/MissingArgumentException");
//        mv.visitInsn(DUP);
//        mv.visitLdcInsn(name + " ["+ index + "]");
//        mv.visitMethodInsn(INVOKESPECIAL, "org/fun4j/compiler/MissingArgumentException", "<init>", "(Ljava/lang/String;)V");
//        mv.visitInsn(ATHROW); 
//        /////
//        
//        mv.visitLabel(l_END);
    }
    
}