package org.fun4j.compiler.expressions;

import org.fun4j.Cons;
import org.fun4j.compiler.Expression;
import org.objectweb.asm.MethodVisitor;

/**
 * An expression for lisp vararg semantics.
 * all arguments are passed in the argument array.
 * they are copied into a cons list and this list is used as a single argument to the actual function body.
 */
public class VarArgs
        extends Expression {

    Expression exp;


    public VarArgs(Expression expr) {
        exp = expr;
    }



    public String toString() {
        return "varargs(" + exp + ")";
    }

    /**
     * apply(Object... args) is a varargs method. Thus all local variables are
     * passed in in one array.
     * All elements of this array are copied into a cons list 
     * and this list is used as a single argument to the function.
     * That is the list is stored back as args[0]
     * So the user code sees a single argument list.
     */
    public void compile(final MethodVisitor mv) {
        lineIncrement = -1;
        writeLineInfo(mv);
        // prepareArgs converts the arguments array into a Cons list of arguments, if applicable
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "org/fun4j/compiler/expressions/VarArgs", "prepareArgs", "([Ljava/lang/Object;)[Ljava/lang/Object;");
        mv.visitVarInsn(ASTORE, 1);
        
        // compile the actual body:
        exp.compile(mv);
    }
    
    /**
     * This method prepares the argument array of a method call to comply with the
     * Lisp varargs semantics.
     * @param args
     * @return the resulting Object array conating a single cons object with all elements of the original input array
     */
    public static Object[] prepareArgs(Object[] args) {
        if (!(args == null || (args.length == 1 && (args[0] == null || args[0] instanceof Cons)))) {
            Cons arglist = arrToSingleCons(args);
            args = new Object[1];
            args[0] = arglist;
        }
        return args;
    }

    /**
     * This method converts an Object array to a Cons list containing all elements
     * of the array.
     * @param args the input array
     * @return the resulting Cons list
     */
    public static Cons arrToSingleCons(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        else {
            Cons result = new Cons(args[0], null);
            Cons last = result;
            for (int i = 1; i < args.length; i++) {
                Cons newLast = new Cons(args[i], null);
                last.setTl(newLast);
                last = newLast;
            }
            return result;
        }
    }
    

}