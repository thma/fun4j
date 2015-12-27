package org.fun4j.compiler;

import static org.fun4j.Cons.append;
import static org.fun4j.compiler.RunTime.isAtom;
import static org.fun4j.compiler.Symbols.HOOK_COMPILE;
import static org.fun4j.compiler.Symbols.HOOK_MACRO_EXPAND;
import static org.fun4j.compiler.Symbols.LAMBDA;
import static org.fun4j.compiler.Symbols.LAZY;
import static org.fun4j.compiler.Symbols.MACRO;
import static org.fun4j.compiler.Symbols.NIL;
import static org.fun4j.compiler.Symbols.OP_ADD;
import static org.fun4j.compiler.Symbols.OP_ADD1;
import static org.fun4j.compiler.Symbols.OP_AND;
import static org.fun4j.compiler.Symbols.OP_ATOMP;
import static org.fun4j.compiler.Symbols.OP_BIGINTS;
import static org.fun4j.compiler.Symbols.OP_CAAR;
import static org.fun4j.compiler.Symbols.OP_CADR;
import static org.fun4j.compiler.Symbols.OP_CAR;
import static org.fun4j.compiler.Symbols.OP_CDAR;
import static org.fun4j.compiler.Symbols.OP_CDDR;
import static org.fun4j.compiler.Symbols.OP_CDR;
import static org.fun4j.compiler.Symbols.OP_COMPILE;
import static org.fun4j.compiler.Symbols.OP_COMPILEDP;
import static org.fun4j.compiler.Symbols.OP_CONS;
import static org.fun4j.compiler.Symbols.OP_CONS_COLON;
import static org.fun4j.compiler.Symbols.OP_DEBUG_ENABLE;
import static org.fun4j.compiler.Symbols.OP_DEFINE;
import static org.fun4j.compiler.Symbols.OP_DIV;
import static org.fun4j.compiler.Symbols.OP_DOT;
import static org.fun4j.compiler.Symbols.OP_EVAL;
import static org.fun4j.compiler.Symbols.OP_EXPLODE;
import static org.fun4j.compiler.Symbols.OP_GEQ;
import static org.fun4j.compiler.Symbols.OP_GT;
import static org.fun4j.compiler.Symbols.OP_HD;
import static org.fun4j.compiler.Symbols.OP_IF;
import static org.fun4j.compiler.Symbols.OP_IMPLODE;
import static org.fun4j.compiler.Symbols.OP_JAVAFUNCTION;
import static org.fun4j.compiler.Symbols.OP_LEQ;
import static org.fun4j.compiler.Symbols.OP_LISTP;
import static org.fun4j.compiler.Symbols.OP_LOAD;
import static org.fun4j.compiler.Symbols.OP_LOOKUP;
import static org.fun4j.compiler.Symbols.OP_MUL;
import static org.fun4j.compiler.Symbols.OP_NOT;
import static org.fun4j.compiler.Symbols.OP_NULLP;
import static org.fun4j.compiler.Symbols.OP_NUMBERP;
import static org.fun4j.compiler.Symbols.OP_NUMEQ;
import static org.fun4j.compiler.Symbols.OP_OR;
import static org.fun4j.compiler.Symbols.OP_PRETTY;
import static org.fun4j.compiler.Symbols.OP_PRIN;
import static org.fun4j.compiler.Symbols.OP_PRINT;
import static org.fun4j.compiler.Symbols.OP_PRINTDEPTH;
import static org.fun4j.compiler.Symbols.OP_PRINTLENGTH;
import static org.fun4j.compiler.Symbols.OP_PROGN;
import static org.fun4j.compiler.Symbols.OP_READ;
import static org.fun4j.compiler.Symbols.OP_REM;
import static org.fun4j.compiler.Symbols.OP_STATICFUNCTION;
import static org.fun4j.compiler.Symbols.OP_SET;
import static org.fun4j.compiler.Symbols.OP_SUB;
import static org.fun4j.compiler.Symbols.OP_SUB1;
import static org.fun4j.compiler.Symbols.OP_SYMBOLP;
import static org.fun4j.compiler.Symbols.OP_THREAD;
import static org.fun4j.compiler.Symbols.OP_TL;
import static org.fun4j.compiler.Symbols.OP_TRACE;
import static org.fun4j.compiler.Symbols.OP_UNTRACE;
import static org.fun4j.compiler.Symbols.OP_ZEROP;
import static org.fun4j.compiler.Symbols.QUOTE;
import static org.fun4j.compiler.RunTime.handleHook;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.fun4j.Collections;
import org.fun4j.Cons;
import org.fun4j.Function;
import org.fun4j.compiler.expressions.Add;
import org.fun4j.compiler.expressions.Add1;
import org.fun4j.compiler.expressions.And;
import org.fun4j.compiler.expressions.Apply;
import org.fun4j.compiler.expressions.CallStatic;
import org.fun4j.compiler.expressions.ConsOp;
import org.fun4j.compiler.expressions.CstDecimal;
import org.fun4j.compiler.expressions.CstFalse;
import org.fun4j.compiler.expressions.CstInt;
import org.fun4j.compiler.expressions.CstNull;
import org.fun4j.compiler.expressions.CstObj;
import org.fun4j.compiler.expressions.CstString;
import org.fun4j.compiler.expressions.CstTrue;
import org.fun4j.compiler.expressions.Div;
import org.fun4j.compiler.expressions.GT;
import org.fun4j.compiler.expressions.Geq;
import org.fun4j.compiler.expressions.Hd;
import org.fun4j.compiler.expressions.If;
import org.fun4j.compiler.expressions.Leq;
import org.fun4j.compiler.expressions.Lookup;
import org.fun4j.compiler.expressions.Mul;
import org.fun4j.compiler.expressions.Not;
import org.fun4j.compiler.expressions.Nullp;
import org.fun4j.compiler.expressions.NumEq;
import org.fun4j.compiler.expressions.Or;
import org.fun4j.compiler.expressions.Progn;
import org.fun4j.compiler.expressions.Recurse;
import org.fun4j.compiler.expressions.Rem;
import org.fun4j.compiler.expressions.SetVar;
import org.fun4j.compiler.expressions.Sub;
import org.fun4j.compiler.expressions.Sub1;
import org.fun4j.compiler.expressions.Tl;
import org.fun4j.compiler.expressions.Var;
import org.fun4j.compiler.expressions.VarArgs;
import org.fun4j.compiler.expressions.Zerop;

/**
 * The fun4j LISP to lambda-AST pre-compiler. This class provides methods to precompile Parsed
 * LISP terms into {@link Expression} objects.
 * 
 * The AST Compiler {@link Compiler} can be used to compile those AST Expressions
 * to Java Bytecode: {@link Compiler#compile(Expression, String)}.
 * 
 * @author Thomas Mahler
 *
 */
public class PreCompiler {
    
    private Compiler compiler = new Compiler();
    
    /**
     * this flag controls whether inlining will be done whenever possible.
     * (debugging inlined code may be tricky thus we have this flag to turn it off)
     */
    public static boolean preferInlining = false;
    
    /**
     * precompiles a parsed lambda term into an {@link Expression} AST.
     * @param term the lambda term to precompile
     * @return the resulting expression
     */
    public Expression precompile(Object term) throws CompilationException {
        // call compile hook:
        handleHook(HOOK_COMPILE, term);
        return precompile(term, null, null);
    }
    
    /**
     * precompiles a Lisp term into an AST expression
     * 
     * @param term the Lisp term
     * @param boundVariables the currently bound variables
     * @return the resulting Expression
     * @throws CompilationException 
     */
    private Expression precompile(Object term, ArrayList<String> boundVariables, String currentFunction) throws CompilationException {
        Expression result = null;
        if (term == null) {
            return new CstNull();
        }
        else if (isAtom(term)) {
            if (term instanceof BigInteger) {
                BigInteger value = (BigInteger) term;
                if (Compiler.useBigInts()) {
                    return new CstInt(value);
                }
                else {
                    return new CstInt(value.intValue());
                }
            }
            else if (term instanceof Integer) {
                return new CstInt(term);
            }
            else if (term instanceof BigDecimal) {
                return new CstDecimal(term);
            }
            else if (term instanceof Collection<?>) {
                return new CstObj(term);
            }
            else if (term instanceof String) {
                // special symbols like true and false
                String symbol = (String) term;
                if (NIL.equalsIgnoreCase(symbol) || "null".equalsIgnoreCase(symbol)) {
                    return new CstNull();
                }
                // strings delimited by " " evaluate to themselves
                else if (symbol.startsWith("\"")) {
                    return new CstObj(symbol);
                }
                // locally bound variable
                else if (boundVariables != null && boundVariables.contains(symbol)) {
                    int index = boundVariables.indexOf(symbol);
                    return new Var(index, symbol);
                }
                else if ("true".equalsIgnoreCase(symbol) || "T".equalsIgnoreCase(symbol) || "#t".equalsIgnoreCase(symbol)) {
                    return new CstTrue();
                }
                else if ("false".equalsIgnoreCase(symbol) || "F".equalsIgnoreCase(symbol) || "#f".equalsIgnoreCase(symbol)) {
                    return new CstFalse();
                }   
                // handle Java class literals
                else if (symbol.endsWith(".class")) {
                    return new CstObj(createClassFor(symbol));
                }
                // unbound variable
                else {
                    return new Lookup(symbol);
                }
            }
            else if (term instanceof Function) { return new CstObj(term); }
        }

        // it's a Cons Object
        else if (term instanceof Cons) {
            Cons termC = (Cons) term;
            Object hd = termC.getHd();
            if (isAtom(hd)) {
                if (hd instanceof String) {
                    String symbol = (String) hd;
                    Object symValue = null;
                    try {
                        symValue = RunTime.lookup(symbol);
                    }
                    catch (Exception ignored) {
                        
                    }
                    // handle specials first:
                    if (OP_DEFINE.equalsIgnoreCase(symbol)) {
                        // expand (define (symbol params) body) to (define symbol (lambda params body))
                        Object value;
                        if (((Cons) termC.getTl()).getHd() instanceof Cons) {
                            symbol = ((Cons) ((Cons) termC.getTl()).getHd()).getHd().toString();
                            Object params = ((Cons) ((Cons) termC.getTl()).getHd()).getTl();                            
                            //Cons params = (Cons) ((Cons) ((Cons) termC.getTl()).getHd()).getTl();                            
                            Object body = ((Cons) ((Cons) termC.getTl()).getTl()).getHd();                            
                            value = new Cons(LAMBDA, new Cons(params, new Cons(body, null)));                            
                            //System.out.println("define expands to: " + value);
                        }
                        else {
                            // handle (define symbol value)
                            symbol = ((Cons) termC.getTl()).getHd().toString();
                            value = ((Cons) ((Cons) termC.getTl()).getTl()).getHd();
                        }
                        if (value != null && !isAtom(value) && ((Cons) value).getHd() != null
                                && LAMBDA.equalsIgnoreCase(((Cons) value).getHd().toString())) {
                            // value is a lambda: compile a function
                            // add name of currently compiled function to locals
                            // vars
                            if (boundVariables == null) {
                                boundVariables = new ArrayList<String>();
                            }
                            //boundVariables.add(symbol);
                            Expression val = precompileLambda((Cons)value, boundVariables, symbol);
                            // System.out.println(val);
                            Function fun = compiler.compile(val, symbol);
                            RunTime.define(symbol, fun);
                            return new CstString(symbol);
                        }
                        if (value != null && !isAtom(value) && ((Cons) value).getHd() != null
                                && LAZY.equalsIgnoreCase(((Cons) value).getHd().toString())) {
                            // value is a macro: compile a function that does not evaluate its arguemnts on execution.
                            // this is done by setting the flag setMacro(true).
                            // when precompiling an expression with such a macro function this flag is used to
                            // trigger quoting of arguments on method invocation

                            // add name of currently compiled function to locals
                            if (boundVariables == null) {
                                boundVariables = new ArrayList<String>();
                            }
                            //boundVariables.add(symbol);
                            Expression val = precompile(value, boundVariables, symbol);
                            // System.out.println(val);
                            BaseFunction fun = (BaseFunction) compiler.compile(val, symbol);
                            fun.setLazy(true);
                            RunTime.define(symbol, fun);
                            return new CstString(symbol);
                        }
                        if (value != null && !isAtom(value) && ((Cons) value).getHd() != null
                                && MACRO.equalsIgnoreCase(((Cons) value).getHd().toString())) {
                            // value is a macro: compile a function that does not evaluate its arguments on execution.
                            // this is done by setting the flag setMacro(true).
                            // when pre-compiling an expression with such a macro function this flag is used to
                            // trigger quoting of arguments on method invocation

                            // add name of currently compiled function to locals
                            if (boundVariables == null) {
                                boundVariables = new ArrayList<String>();
                            }
                            //boundVariables.add(symbol);
                            Expression val = precompileLambda((Cons)value, boundVariables, symbol);
                            //System.out.println(val);
                            BaseFunction fun = (BaseFunction) compiler.compile(val, symbol);
                            fun.setMacro(true);
                            RunTime.define(symbol, fun);
                            return new CstString(symbol);
                        }
                        else {
                            // evaluate it and store the resulting value
                            Expression val = precompile(value, boundVariables, currentFunction);
                            Function fun = compiler.compile(val, symbol);
                            Object computedValue = fun.apply();
                            RunTime.define(symbol, computedValue);
                            return new CstObj(computedValue);
                        }
                    }
                    else if (QUOTE.equalsIgnoreCase(symbol)) {
                        Object t = ((Cons) ((Cons) term).getTl()).getHd();
                        if (t instanceof BigInteger) {
                            BigInteger value = (BigInteger) t;
                            if (Compiler.useBigInts()) {
                                return new CstInt(value);
                            }
                            else {
                                return new CstInt(value.intValue());
                            }
                        }
                        else if (t instanceof Integer) {
                            return new CstInt(t);
                        }
                        else if (t instanceof BigDecimal) {
                            return new CstDecimal(t);

                        }
                        else if (t instanceof String) {
                            return new CstString((String) t);
                        }
                        else if (t instanceof Cons) {
                            return new CstObj(t);
                        }
                        else if (t == null) {
                            return new CstNull();
                        }
                        else {
                            throw new RuntimeException("can't handle '" + t);
                        }
                    }
                    else if (LAMBDA.equalsIgnoreCase(symbol)) {
                        return precompileLambda(termC, boundVariables, currentFunction);
                    }
                    else if (MACRO.equalsIgnoreCase(symbol)) {
                        return precompileLambda(termC, boundVariables, currentFunction);
                    }
                    else if (LAZY.equalsIgnoreCase(symbol)) {
                        return precompileLambda(termC, boundVariables, currentFunction);
                    }
                    // handle .-expressions
                    // 1. Constructors
                    else if (symbol.endsWith(".")) {

                        String className = symbol.substring(0, symbol.length()-1);
                        Cons env = (Cons) termC.getTl();
                        Expression[] args = precompileArgs(env, boundVariables, currentFunction);
                        
                        Expression[] params;
                        if (args == null) {
                        	params = new Expression[1];
                        }
                        else {
                            params = new Expression[args.length+1];
                            System.arraycopy(args, 0, params, 1, args.length);
                        }
                        params[0] = new CstString(className);
                        
                        
                        return new CallStatic(RunTime.class, "construct", "([Ljava/lang/Object;)Ljava/lang/Object;", params);

                        
                    }
                    
                    // handle recursive calls
                    else if (symbol.equals(currentFunction)){
                        return new Recurse(symbol, precompileArgs((Cons) termC.getTl(), boundVariables, currentFunction));
                    }  
                    // handle recursion or higher order function
                    else if (boundVariables != null && boundVariables.contains(symbol)) {
                        int index = boundVariables.indexOf(symbol);
                        // calling a higher order function that is passed in
                        // as a bound variable
                        
                        // modify call site so that all bound variables from the outer environment are added to the function invocation
                        Cons args = (Cons) termC.getTl();
                        Cons outerEnv = Collections.asCons(boundVariables);
                        args = append(args, outerEnv);
                        return new Apply(new Var(index, symbol), precompileArgs(args, boundVariables, currentFunction));
                    }
                    // handle lazy (non-strict) functions 
                    else if (symValue instanceof BaseFunction && ((BaseFunction)symValue).isLazy() ) {
                        // modify call site so that all bound variables from the outer environment are added to the function invocation
                        Cons env = (Cons) termC.getTl();
                        Cons outerEnv = Collections.asCons(boundVariables);
                        env = append(env, outerEnv);
                    	Expression[] args = precompileArgsQuoted(env, boundVariables, currentFunction);
                        Expression expr = new Apply(new CstObj(symValue), args);                        
                        return expr;
                    }
                    // handle macros
                    else if (symValue instanceof BaseFunction && ((BaseFunction)symValue).isMacro() ) {
                        // modify call site so that all bound variables from the outer environment are added to the function invocation
                        Cons env = (Cons) termC.getTl();
//                        Cons outerEnv = Collections.asCons(boundVariables);
//                        env = append(env, outerEnv);

                        Expression[] args = precompileArgsQuoted(env, boundVariables, currentFunction);
                        Expression expr = new Apply(new CstObj(symValue), args);
                        
                        Function macro = compiler.compile(expr, symbol);
                        Object expandedMacro = macro.apply();
                        handleHook(HOOK_MACRO_EXPAND, expandedMacro);
                        expr = precompile(expandedMacro, boundVariables, currentFunction);
                        return expr;
                    }      
                    // handle primitives and globally defined functions
                    else {
                        Cons env = (Cons) termC.getTl();
                        Expression[] args = precompileArgs(env, boundVariables, currentFunction);
                        Expression expr;
                        Expression inlinedOperation = null;
                        try {
                            inlinedOperation = getOperationForSymbol(symbol, args);
                        }
                        catch (Throwable t) {
                            // ignore
                        }
                        if (symValue instanceof Function) {
                            // the symbol is a defined as a function in the global environment:
                            if (preferInlining && inlinedOperation != null) {
                                // if a primitive operation is found and inlining is preferred, then use it 
                                expr = inlinedOperation;
                            } 
                            else {
                                // use static binding, that is no lookup at run time, as function is already known at compile time !
                                //expr = new Apply(new CstObj(symValue), args); 
                                
                                // use dynamic binding:
                                expr = new Apply(new Lookup(symbol), args);
                            }
                        }
                        else {
                            // the symbol is NOT a function in the gloabl environment:
                            if (inlinedOperation != null) {
                                // if a primitive operation is found: use it!
                                expr = inlinedOperation;
                            }
                            else {
                                // no, it's not a primitive, so emit code for lookup:
                                expr = new Apply(new Lookup(symbol), args);
                            }
                        }
                        return expr;
                    }
                }
                else if (hd instanceof Function) {                    
                    Expression expr = new Apply(new CstObj(hd), precompileArgs((Cons) termC.getTl(), boundVariables, currentFunction));
                    return expr;
                }
                else {
                    throw new UnsupportedOperationException("invalid function: " + hd.toString());
                }

            }
            // hd is a cons too
            else {
                Expression expHd = precompile(hd, boundVariables, currentFunction);
                BaseFunction fun = (BaseFunction) compiler.compile(expHd, null);

                // macros
                if (fun.isMacro()) {
                    // modify call site so that all bound variables from the outer environment are added to the function invocation
                    Cons args = (Cons) termC.getTl();
                    Cons outerEnv = Collections.asCons(boundVariables);
                    args = append(args, outerEnv);

                    Expression expr = new Apply(new CstObj(fun), precompileArgsQuoted(args, boundVariables, currentFunction));
                    
                    Function macro = compiler.compile(expr, "macro");
                    Object expandedMacro = macro.apply();
                    System.out.println("macro expands to: " + expandedMacro);
                    
                    expr = precompile(expandedMacro, boundVariables, currentFunction);
                    return expr;
                    
                }
                //handle bound vars of lambdas
                else if (Symbols.LAMBDA.equals(((Cons)hd).getHd().toString())) {                   
                    // modify call site so that all bound variables from the outer environment are added to the function invocation
                    Cons args = (Cons) termC.getTl();
                    Cons outerEnv = Collections.asCons(boundVariables);
                    args = append(args, outerEnv);
                    return new Apply(new CstObj(fun), precompileArgs(args, boundVariables, currentFunction));
                }
                // hd is an @ 
                else {
                    // modify call site so that all bound variables from the outer environment are added to the function invocation
                    Cons args = (Cons) termC.getTl();
                    Cons outerEnv = Collections.asCons(boundVariables);
                    args = append(args, outerEnv);

                    new Apply(new CstObj(fun), precompileArgs(args, boundVariables, currentFunction));
                }
            }
        }
        // it's not an atom and not a Cons: we can't handle it
        else {
            // unknown object type:
            throw new RuntimeException("unknown type: " + term + ", " + term.getClass());
        }

        return result;
    }
    

    /**
     * 
     * @param symbol
     * @return
     * @throws CompilationException
     */
    private Class<?> createClassFor(String symbol) throws CompilationException {
        String type = symbol.substring(0, symbol.length()-6);
        Class<?> clazz;
        try {
            clazz = Class.forName(type);
        }
        catch (ClassNotFoundException e) {
            throw new CompilationException("Class not found:" + type, e);
        }
        return clazz;
    }


    /**
     * precompile a lambdaTerm like (lambda (x y) (+ x y)) to an AST Expression.
     * 
     * @param lambdaTerm
     * @param boundVariables
     * @return
     * @throws CompilationException 
     */
    private Expression precompileLambda(Cons lambdaTerm, ArrayList<String> boundVariables, String currentFunction) throws CompilationException  {
        Object vars = ((Cons) lambdaTerm.getTl()).getHd();
        ArrayList<String> localVars;
        boolean useVarArgs = false;
        if (vars == null || vars instanceof Cons) {
            Cons boundVars = (Cons) vars;
            localVars = extractLocalVars(boundVars);
        }
        else {
            useVarArgs = true;
            localVars = new ArrayList<String>();
            localVars.add(vars.toString());
        }
        Object body = ((Cons) ((Cons) lambdaTerm.getTl()).getTl()).getHd();

        // add all formal variables to list of bound variables
        if (boundVariables != null) {
            localVars.addAll(boundVariables);
        }
        Expression result = precompile(body, localVars, currentFunction);
        if (useVarArgs) {
            return new VarArgs(result);
        }
        else {
            return result;
        }
    }    

    private Expression[] precompileArgs(Cons args, ArrayList<String> boundVariables, String currentFunction) throws CompilationException {
        if (args == null) {
            return null;
        }
        else {
            ArrayList<Expression> list = new ArrayList<Expression>();

            while (args != null) {
                Object first = args.getHd();
                if (first instanceof Cons && Symbols.LAMBDA.equalsIgnoreCase(((Cons) first).getHd().toString())) {
                    Expression expLambda = precompileLambda((Cons)first, boundVariables, currentFunction);
                    Function funLambda = compiler.compile(expLambda, "Lambda");
                    list.add(new CstObj(funLambda));
                }
                else {
                    list.add(precompile(first, boundVariables, currentFunction));
                }
                args = (Cons) args.getTl();
            }

            Expression[] result = new Expression[list.size()];
            list.toArray(result);
            return result;
        }
    }
    
    private Expression[] precompileArgsQuoted(Cons args, ArrayList<String> boundVariables, String currentFunction) throws CompilationException {
        if (args == null) {
            return null;
        }
        else {
            ArrayList<Expression> list = new ArrayList<Expression>();

            while (args != null) {
                list.add(precompile(quote(args.getHd()), boundVariables, currentFunction));
                args = (Cons) args.getTl();
            }

            Expression[] result = new Expression[list.size()];
            list.toArray(result);
            return result;
        }
    }
    
    /**
     * lookup of basic operations known to the compiler
     * 
     * @param symbol
     *            the symbol to be looked up
     * @param args
     *            the Operations arguments
     * @return the expression representing the actual operation call
     */
    private Expression getOperationForSymbol(String symbol, Expression[] args) {
        if (symbol == null) {
            throw new UnsupportedOperationException("invalid operation: null");
        }
        else if (symbol.equalsIgnoreCase(OP_ADD)) {
            return new Add(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_ADD1)) {
            return new Add1(args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_SUB)) {
            return new Sub(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_SUB1)) {
            return new Sub1(args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_MUL)) {
            return new Mul(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_DIV)) {
            return new Div(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_REM)) {
            return new Rem(args[0], args[1]);
        }

        // logic
        else if (symbol.equalsIgnoreCase(OP_AND)) {
            return new And(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_OR)) {
            return new Or(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_NOT)) {
            return new Not(args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_IF)) {
            return new If(args[0], args[1], args[2]);
        }
        else if (symbol.equalsIgnoreCase(OP_ZEROP)) {
            return new Zerop(args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_NUMEQ)) {
            return new NumEq(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_GEQ)) {
            return new Geq(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_LEQ)) {
            return new Leq(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_GT)) {
            return new GT(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_LOOKUP)) {
            return new Lookup(args[0]);
        }

        // list operations
        else if (symbol.equalsIgnoreCase(OP_CONS)) {
            return new ConsOp(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_CONS_COLON)) {
            return new ConsOp(args[0], args[1]);
        }
        else if (symbol.equalsIgnoreCase(OP_CAR)) {
            return new Hd(args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_HD)) {
            return new Hd(args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_CDR)) {
            return new Tl(args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_TL)) {
            return new Tl(args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_CAAR)) {
            return new Hd(new Hd(args[0]));
        }
        else if (symbol.equalsIgnoreCase(OP_CADR)) {
            return new Hd(new Tl(args[0]));
        }
        else if (symbol.equalsIgnoreCase(OP_CDAR)) {
            return new Tl(new Hd(args[0]));
        }
        else if (symbol.equalsIgnoreCase(OP_CDDR)) {
            return new Tl(new Tl(args[0]));
        }
        else if (symbol.equalsIgnoreCase(OP_NULLP)) {
            return new Nullp(args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_SYMBOLP)) {
            // return new Symbolp(args[0]);
            return new CallStatic(RunTime.class, "isSymbol", "(Ljava/lang/Object;)Ljava/lang/Boolean;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_NUMBERP)) {
            // return new Numberp(args[0]);
            return new CallStatic(RunTime.class, "isNumber", "(Ljava/lang/Object;)Ljava/lang/Boolean;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_ATOMP)) {
            // return new Atomp(args[0]);
            return new CallStatic(RunTime.class, "isAtom", "(Ljava/lang/Object;)Ljava/lang/Boolean;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_LISTP)) {
            return new CallStatic(RunTime.class, "isList", "(Ljava/lang/Object;)Ljava/lang/Boolean;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_COMPILEDP)) {
            return new CallStatic(RunTime.class, "isCompiled", "(Ljava/lang/Object;)Ljava/lang/Boolean;", args[0]);
        }
        // symbols
        else if (symbol.equalsIgnoreCase(OP_IMPLODE)) {
            return new CallStatic(RunTime.class, OP_IMPLODE, "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_EXPLODE)) {
            return new CallStatic(RunTime.class, OP_EXPLODE, "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }

        // other stuff
        else if (symbol.equalsIgnoreCase(OP_LOAD)) {
            return new CallStatic(RunTime.class, OP_LOAD, "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_TRACE)) {
            return new CallStatic(RunTime.class, "trace", "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_UNTRACE)) {
            return new CallStatic(RunTime.class, "untrace", "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_BIGINTS)) {
            return new CallStatic(Compiler.class, "setUseBigInts", "(Ljava/lang/Boolean;)Ljava/lang/Boolean;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_DEBUG_ENABLE)) {
            return new CallStatic(Compiler.class, "setWriteFile", "(Ljava/lang/Boolean;)Ljava/lang/Boolean;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_PRINTDEPTH)) {
            if (Compiler.useBigInts()) {
                return new CallStatic(Cons.class, OP_PRINTDEPTH, "(Ljava/math/BigInteger;)Ljava/math/BigInteger;",
                        args[0]);
            }
            else {
                return new CallStatic(Cons.class, OP_PRINTDEPTH, "(Ljava/lang/Integer;)Ljava/lang/Integer;", args[0]);
            }
        }
        else if (symbol.equalsIgnoreCase(OP_PRINTLENGTH)) {
            if (Compiler.useBigInts()) {
                return new CallStatic(Cons.class, OP_PRINTLENGTH, "(Ljava/math/BigInteger;)Ljava/math/BigInteger;",
                        args[0]);
            }
            else {
                return new CallStatic(Cons.class, OP_PRINTLENGTH, "(Ljava/lang/Integer;)Ljava/lang/Integer;", args[0]);
            }
        }
        // set! operation
        else if (symbol.equalsIgnoreCase(OP_SET)) {
            return new SetVar(args[0], args[1]);
        }

        // read + print
        else if (symbol.equalsIgnoreCase(OP_READ)) {
            Expression arg = new CstNull();
            if (args != null && args[0] != null) {
                arg = args[0];
            }
            return new CallStatic(RunTime.class, OP_READ, "(Ljava/lang/Object;)Ljava/lang/Object;", arg);
        }
        else if (symbol.equalsIgnoreCase(OP_PRINT)) {
            return new CallStatic(RunTime.class, OP_PRINT, "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_PRIN)) {
            return new CallStatic(RunTime.class, OP_PRIN, "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_PRETTY)){
            return new CallStatic(RunTime.class, OP_PRETTY, "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }

        // compile & co.
        else if (symbol.equalsIgnoreCase(OP_COMPILE)) {
            return new CallStatic(RunTime.class, OP_COMPILE, "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_EVAL)) {
            return new CallStatic(RunTime.class, OP_EVAL, "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }
        else if (symbol.equalsIgnoreCase(OP_PROGN)){
        	return new Progn(args);
        }
        // call java org.fun4j.Function instances
        else if (symbol.equalsIgnoreCase(OP_JAVAFUNCTION)) {
            return new CallStatic(RunTime.class, "instantiateJavaFunction", "(Ljava/lang/String;)Lorg/fun4j/Function;",
                    args[0]);
        }
        // call Java static methods
        else if (symbol.equalsIgnoreCase(OP_STATICFUNCTION)) {
            String classname = args[0].toString().replace('.', '/');
            String method = args[1].toString();
            String signature = args[2].toString();
            // trim " " 
            signature = signature.substring(1, signature.length()-1);            
            Expression[] arguments = Arrays.copyOfRange(args, 3, args.length);
            return new CallStatic(classname, method, signature, arguments);
        }
        // call java methods via reflection
        else if (symbol.equalsIgnoreCase(OP_DOT)) {
            return new CallStatic(RunTime.class, "invokeDynamic", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", args);   
        }        
        else if (symbol.equalsIgnoreCase("dump")) {
            return new CallStatic(RunTime.class, "dump", "()Ljava/lang/Object;");
        }
        else if (symbol.equalsIgnoreCase("fasl")) {
            return new CallStatic(RunTime.class, "fasl", "()Ljava/lang/Object;");
        }  
        
        // execute as thread
        else if (symbol.equalsIgnoreCase(OP_THREAD)) {
            return new CallStatic(RunTime.class, "thread", "(Ljava/lang/Object;)Ljava/lang/Object;", args[0]);
        }

        // unknown symbol:
        return null;
    }
  
    private ArrayList<String> extractLocalVars(Cons boundVars) {
        ArrayList<String> result = new ArrayList<String>();
        while (boundVars != null) {
            String var = boundVars.getHd().toString();
            result.add(var);
            boundVars = (Cons) boundVars.getTl();
        }
        return result;
    }


    
    private Cons quote(Object x) {
        return new Cons(QUOTE, new Cons(x, null));
    }
    
}
