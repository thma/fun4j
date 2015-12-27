package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * A CallStatic() expression. Represents a call to a static method
 */
public class CallStatic
        extends Expression {


    @SuppressWarnings("rawtypes")
    Class clazz = null;
    String classname;
    String methodname;
    String desc;
    Expression[] args;

    @SuppressWarnings({ "rawtypes" })
    public CallStatic(Class clazz, String methodname, String desc, Expression... args) {
        this.clazz = clazz;
        this.classname = getName(clazz);
        this.methodname = methodname;
        this.desc = desc;
        this.args = args;
    }
    
    public CallStatic(String classname, String methodname, String desc, Expression... args) {
        this.classname = classname;
        this.methodname = methodname;
        this.desc = desc;
        this.args = args;
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
        
        String clname;
        if (clazz != null) {
            clname = clazz.getSimpleName();
        }
        else {
            clname = classname;
        }
        
        return "(" + clname + "." + methodname + " " + arglist + ")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        // push all args
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                args[i].compile(mv);
            }
        }
        // invoke static method
        mv.visitMethodInsn(INVOKESTATIC, classname, methodname, desc);
    }
}