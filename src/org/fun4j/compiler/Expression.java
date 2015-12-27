package org.fun4j.compiler;

import java.util.ArrayList;


import org.fun4j.Function;
import org.fun4j.Predicate;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 
 * Expression baseclass. Expression and its subclasses form the AST
 * representation of precompiled functional expressions.
 * 
 * The toString() returns a human-readable representation of that structure
 * 
 * All Subclasses must implement a method {@link #compile(MethodVisitor)} that
 * contains ASM code to generate the specific jvm bytecode for that subclass.
 * 
 * @author Thomas Mahler
 */
public abstract class Expression implements Opcodes {

    // TODO not thread safe !!!
    protected static Label methodEntry = null;
    
    protected static int linenumber = 3;
    
    protected int lineIncrement = 1;
    
    protected Class<?> returnType = Object.class;

    /**
     * Returns the byte code of a Function class instance representing this
     * expression.
     * 
     * The base idea is that an Expression object is compiled into a Subclass of
     * {@link BaseFunction} and thus implements the interface {@link Function}.
     * 
     * Thus the method {@link BaseFunction#apply(Object...)} which is declared as 
     * abstract in {@link BaseFunction} is constructed here.
     * 
     * In case of covariance (as of now only for {@link Precicate}s) covariant extensions of
     * {@link BaseFunction will be used to construct the function instance.}
     * 
     * //TODO this method is synchronized to avoid parallel invocations from
     * different threads (the methodEntry label is not thread safe)
     * 
     */
    synchronized byte[] compile(final String name) {
    	Class<?> ifClass = Function.class;
    	Class<?> implClass = BaseFunction.class;
    	
    	// if the return type of an Expression deviates from Object.class we have to some extra work to support covariant 
    	// derivatives of the Function Interface
    	if (returnType == Boolean.class) {
    		ifClass = Predicate.class;
    		implClass = BasePredicate.class;
    	}
    	
        // class header
        String[] itfs = { getName(ifClass) };
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, name, null, getName(implClass), itfs);

        cw.visitSource(/*RunTime.getTempDir() + "/" + */name + ".class", null);
        
        // default public constructor
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, getName(implClass), "<init>", "()V");

        // set field pcode with a pseudo code representation (useful for
        // debugging)
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(this.toString());
        mv.visitFieldInsn(PUTFIELD, getName(implClass), "pcode", "Ljava/lang/String;");

        // set field fun with name of function
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(name);
        mv.visitFieldInsn(PUTFIELD, getName(implClass), "fun", "Ljava/lang/String;");
        
        
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        // end of constructor
         
        // method apply        
        mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, "apply", "([Ljava/lang/Object;)L"+ getName(returnType) + ";", null, null);
        methodEntry = new Label();
        mv.visitLabel(methodEntry);
        
        linenumber = 3;
        
//        // handle fun4j exceptions
//        Label l_start = new Label();
//        Label l_end = new Label();
//        Label l_handler = new Label();
//        Label l_END = new Label();
//        mv.visitTryCatchBlock(l_start, l_end, l_handler, "org/fun4j/compiler/MissingArgumentException");
//        mv.visitLabel(l_start);   
        
        // compile the actual expression:
        compile(mv);
//        //mv.visitInsn(ARETURN);
//        mv.visitLabel(l_end);
//        mv.visitJumpInsn(Opcodes.GOTO, l_END);
//        
//        mv.visitLabel(l_handler);
//        mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"org/fun4j/compiler/MissingArgumentException"});
//        mv.visitVarInsn(ASTORE, 2);
//
//        mv.visitTypeInsn(NEW, "org/fun4j/PartialApplication");
//        mv.visitInsn(DUP);
//        mv.visitVarInsn(ALOAD, 0); // push "this"
//        mv.visitVarInsn(ALOAD, 1); // push argument array
//        mv.visitMethodInsn(INVOKESPECIAL, "org/fun4j/PartialApplication", "<init>", "(Lorg/fun4j/Function;[Ljava/lang/Object;)V");
//        
//        
//        mv.visitLabel(l_END);
        mv.visitInsn(ARETURN);
        
        
        
        // max stack and max locals automatically computed
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        // end apply method 
        
        // in case of a covariant return type a bridge method must be generated, that delegates to the covariant apply method,
        // that contains the actual user code
        if (returnType != Object.class) {
        	mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_VARARGS + ACC_SYNTHETIC, "apply", "([Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        	mv.visitCode();
        	mv.visitVarInsn(ALOAD, 0);
        	mv.visitVarInsn(ALOAD, 1);
        	mv.visitMethodInsn(INVOKEVIRTUAL, getName(implClass), "apply", "([Ljava/lang/Object;)L" + getName(returnType) + ";");
        	mv.visitInsn(ARETURN);
        	mv.visitMaxs(2, 2);
        	mv.visitEnd();
        }

        return cw.toByteArray();
    }

    /**
     * Compile this expression. Subclasses will enter their specific ASM code
     * here.
     */
    public abstract void compile(MethodVisitor mv);

    protected void writeLineInfo(MethodVisitor mv) {
        linenumber = linenumber + lineIncrement;
        Label label = new Label();
        mv.visitLabel(label);
        mv.visitLineNumber(linenumber, label);
    }
    
    /**
     * compute all "tails" of an Expression. The compiler needs this for
     * analysis of Tail recursive code.
     * 
     * @return the Array containing all tail calls
     */
    public ArrayList<Expression> getTailCalls() {
        ArrayList<Expression> result = new ArrayList<Expression>();
        result.add(this);
        return result;
    }

    protected boolean useBigIntArithmetics() {
        return Compiler.useBigInts();
    }
    
    @SuppressWarnings({ "rawtypes" })
    protected String getName(Class clazz) {
        String result = clazz.getCanonicalName().replace('.', '/');
        return result;
    }
    
}