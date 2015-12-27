package org.fun4j.compiler.expressions;


import org.fun4j.Function;
import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A Recursion expression.
 */
public class Recurse
        extends Expression {

    private boolean isTailRecursive = false;

    Expression[] args;
    String name = "fn";

    public Recurse(String name, final Expression... e1) {
        args = e1;
        this.name = name;
    }
    
    public Recurse(final Expression... e1) {
        args = e1;
    }

    public String toString() {
        String arglist = "";
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                arglist += args[i];
                if (i < args.length - 1) {
                    arglist += " ";
                }
            }
        }
        // arglist += "}";

        String fun = "(" + name;
        if (isTailRecursive) {
            fun += "[TCO_LOOP]";
        }

        return fun + " " + arglist + ")";
    }

    /**
     * Recurse provides a flag by which generation of TCO can be triggered. The
     * compiler enables this flag if it detects a tail call.
     */
    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        if (isTailRecursive) {
            // In case of TCO don't push 'this' onto stack, as we don't want to
            // do a recursive method call!
        }
        else {
            // push 'this'
            mv.visitVarInsn(ALOAD, 0);
        }

        if (args != null) {
            // push new array
            mv.visitLdcInsn(args.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            // mv.visitInsn(DUP);
            for (int i = 0; i < args.length; i++) {
                mv.visitInsn(DUP);
                mv.visitLdcInsn(i);
                args[i].compile(mv);
                mv.visitInsn(AASTORE);
            }
        }
        if (isTailRecursive) {
            // update local variables for TCO re-entry
            mv.visitVarInsn(ASTORE, 1);
            // jump to first instruction of the method
            mv.visitJumpInsn(GOTO, methodEntry);

        }
        else {
            // classic recursive this.apply(args) invocation
        	mv.visitMethodInsn(INVOKEINTERFACE, getName(Function.class), "apply",
                    "([Ljava/lang/Object;)Ljava/lang/Object;");
        }
    }

    public void setTailRecursive(boolean isTailRecursive) {
        this.isTailRecursive = isTailRecursive;
    }

    public boolean isTailRecursive() {
        return isTailRecursive;
    }
}