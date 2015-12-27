package org.fun4j.compiler.expressions;


import org.fun4j.Function;
import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A apply expression.
 */
public class Apply
        extends Expression {

    Expression function;
    Expression[] args;

    public Apply(final Expression fun, final Expression... e1) {

        function = fun;
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
        return "(" + function + " " + arglist + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        function.compile(mv);

        if (args == null) {
            // push empty array
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");   
        }
        else {
            // push new array
            mv.visitLdcInsn(args.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            // store all arguments in the array
            for (int i = 0; i < args.length; i++) {
                mv.visitInsn(DUP);
                mv.visitLdcInsn(i);
                args[i].compile(mv);
                mv.visitInsn(AASTORE);
            }
        }

        mv.visitMethodInsn(INVOKEINTERFACE, getName(Function.class), "apply",
                "([Ljava/lang/Object;)Ljava/lang/Object;");
    }
    
}
