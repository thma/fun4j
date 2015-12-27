package org.fun4j.compiler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.fun4j.Function;
import org.fun4j.compiler.expressions.Recurse;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;


/**
 * The fun4j functional AST to Java ByteCode compiler. This class provides methods to compile
 * {@link Expression} objects into {@link Function} instances.
 * 
 * The {@link Function#apply(Object...)} method will invoke the compiled AST expression.
 * 
 * @author Thomas Mahler
 */
public class Compiler extends ClassLoader {

    // global flag to turn on/off writing of temporay class files for debugging
    public static boolean writeFile = false;

    private static boolean useBigInts = true;

    public static Boolean setUseBigInts(Boolean newValue) {
        Boolean oldValue = useBigInts;
        useBigInts = newValue;
        return oldValue;
    }

    public static boolean useBigInts() {
        return useBigInts;
    }

    public static Boolean setWriteFile(Boolean newValue) {
        Boolean oldValue = writeFile;
        writeFile = newValue;
        return oldValue;
    }

    private static long count = 0;
    
    private static Hashtable<Class<?>, byte[]> bytecodeTable = new Hashtable<Class<?>, byte[]>();

    /**
     * The public constructor is used to set up the parent classloader hierarchy in the proper way.
     * If this was not done the Compiler would cause classloading issues in OSGI plugins.
     */
    public Compiler(){
        super(Compiler.class.getClassLoader());
    }
    
    public long getCount() {
        return count;
    }


    /**
     * compiles an expression into an executable Function.
     * 
     * @param expr the expression to compile
     * @param name the name to be used for the Java class
     * @return the compiled Function
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public BaseFunction compile(Expression expr, String name) throws CompilationException {
        count++;
        if (name == null) {
            name = "TempClass";
        }
        name = cleanup(name) + "_" + count;

        // simple Tail Call Optimization (TCO)
        // 1. compute all tails of the Exp
        ArrayList<Expression> tails = expr.getTailCalls();
        // 2. Iterate over all tails.
        // if it is a recursive call, set a flag to allow generation of
        // optimized byte code
        for (Expression tail : tails) {
            if (tail instanceof Recurse) {
                ((Recurse) tail).setTailRecursive(true);
            }
        }
        if (writeFile) {
            String filename = RunTime.getTempDir() + "/" + name + ".lsp";
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(expr.toString().getBytes());
                fos.close();
            }
            catch (FileNotFoundException e) {
                throw new CompilationException(filename + " not found", e);
            }
            catch (IOException e) {
                throw new CompilationException("can't write to file " + filename, e);
            }
        }

        // now we do the actual ASM based compilation
        byte[] b = expr.compile(name);
                
        if (writeFile) {
            String filename = RunTime.getTempDir() + "/" + name + ".class";
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(b);
                fos.close();
            }
            catch (FileNotFoundException e) {
                throw new CompilationException(filename + " not found", e);
            }
            catch (IOException e) {
                throw new CompilationException("can't write to file " + filename, e);
            }
        }        
        return updateClassDefinition(b);         
    }
    
    private BaseFunction updateClassDefinition(byte[] barr) throws CompilationException {
        @SuppressWarnings({ "rawtypes" })
        Class funClass = defineClass(null, barr, 0, barr.length);
        BaseFunction fun;
        try {
            fun = (BaseFunction) funClass.newInstance();
        }
        catch (InstantiationException e) {
            throw new CompilationException("Can't instantiate the compiled class. " + e.getMessage(), e);
        }
        catch (IllegalAccessException e) {
            throw new CompilationException("Illegal access: " + e.getMessage(), e);
        }        
        bytecodeTable.put(funClass, barr);
        return fun;
    }

    /**
     * this method replaces invalid characters from class names
     * @param name
     * @return the cleansed version
     */
    private String cleanup(String name) {
        String result = name.replace(".", "DOT");
        result = result.replace("*", "STAR");
        return result;
    }


    /**
     * this method modifies the bytecode of the <code>baseFun.apply()</code> method.
     * It adds a calls to <code>baseFun.traceIn()</code> and <code>baseFun.traceOut()</code>
     * to write trace information to sysout on entering and returning from the <code>baseFun.apply()</code> method.
     * @param baseFun the function that is to be enhanced with tracing functionality
     * @return a trace enabled copy of the original Function baseFun
     * @throws IOException
     * @throws CompilationException
     */
    @SuppressWarnings("unchecked")
    public BaseFunction injectTracing(BaseFunction baseFun) throws IOException, CompilationException {
        byte[] barr = bytecodeTable.get(baseFun.getClass());
        ClassReader cr = new ClassReader(barr);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, 0);
        for (MethodNode methodNode : (List<MethodNode>) classNode.methods) {
            if ("apply".equals(methodNode.name)) {
                // insert the entry tracing code (call to BaseFunction.traceIn(baseFun.getName()))
                InsnList beginList = new InsnList();
                beginList.add(new LdcInsnNode(baseFun.getName()));
                beginList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                beginList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/fun4j/compiler/BaseFunction", "traceIn",
                    "(Ljava/lang/String;[Ljava/lang/Object;)V"));
                methodNode.instructions.insert(beginList);

                // now the exit tracing code (call to BaseFunction.traceOut(baseFun.getName(), result))
                Iterator<AbstractInsnNode> insnNodes = methodNode.instructions.iterator();
                while (insnNodes.hasNext()) {
                    AbstractInsnNode insn = insnNodes.next();
                    int opcode = insn.getOpcode();
                    if (opcode == Opcodes.IRETURN || opcode == Opcodes.RETURN || opcode == Opcodes.ARETURN
                            || opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
                        InsnList endList = new InsnList();
                        // stack top element contains result, thus duplicate it
                        endList.add(new InsnNode(Opcodes.DUP));
                        // push the function name
                        endList.add(new LdcInsnNode(baseFun.getName()));
                        // swap function name and result to match method signature of traceOut
                        endList.add(new InsnNode(Opcodes.SWAP));
                        // the actual call to traceOut(name, result)
                        endList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/fun4j/compiler/BaseFunction",
                            "traceOut", "(Ljava/lang/String;Ljava/lang/Object;)V"));
                        methodNode.instructions.insertBefore(insn, endList);
                    }
                }
                // only handle apply method
                break;
            }
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return (BaseFunction) updateClassDefinition(cw.toByteArray());
    }








}