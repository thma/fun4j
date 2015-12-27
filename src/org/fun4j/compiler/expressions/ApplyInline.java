package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A apply expression.
 */
public class ApplyInline
        extends Expression {

    Expression function;
    Expression[] args;

    public ApplyInline(final Expression fun, final Expression... e1) {
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
        if (args != null) {
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
        // update local variables for inline call
        mv.visitVarInsn(ASTORE, 1);
        
        // execute inlined code
        function.compile(mv);
        
    }
    
}
