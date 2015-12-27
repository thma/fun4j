package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;
import org.fun4j.compiler.RunTime;
import org.objectweb.asm.MethodVisitor;

/**
 * A set! expression. 
 * Used to set a local or global variable as in (set! n 8)
 */
public class SetVar
        extends BinaryExp {

    public SetVar(final Expression var, final Expression value) {
        super(var, value);
    }

    public String toString() {
        return "(set! " +e1 + " " + e2 +")";
    }

    public void compile(final MethodVisitor mv) {
        writeLineInfo(mv);
        
        // handle local variable
        if (e1 instanceof Var) {
            int index = ((Var) e1).getIndex();
            // push args array
            mv.visitVarInsn(ALOAD, 1);
            // push index of local var
            mv.visitLdcInsn(index);        
            // push value:
            e2.compile(mv);
            // assign to variable
            mv.visitInsn(AASTORE);
            // finally return value:
            e2.compile(mv);            
        }
        //handle global variable
        else if (e1 instanceof Lookup) {
            String strName = ((Lookup) e1).getStrName();
            Expression eName = ((Lookup) e1).geteName();   
            // push variable name
            if (strName != null) {
                mv.visitLdcInsn(strName);
            }
            else {
                eName.compile(mv);
                mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            }
            //push value
            e2.compile(mv);            
            //emit call to RunTime.define
            mv.visitMethodInsn(INVOKESTATIC, getName(RunTime.class), "define", "(Ljava/lang/String;Ljava/lang/Object;)V");
            // finally return value:
            e2.compile(mv);   
        }
        else {
            throw new RuntimeException("compilation error in " + this + ": " + e1 + " is not a local or global variable");
        }     
    }
}