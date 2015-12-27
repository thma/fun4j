package org.fun4j.compiler;

/**
 * All symbols used by Parser and Compiler.
 * 
 * @author Thomas Mahler
 *
 */
public interface Symbols {

    // symbols
    public static final String QUOTE = "quote";
    public static final String QUASIQUOTE = "quasiquote";    
    public static final String UNQUOTE = "unquote";
    public static final String UNQUOTE_SPLICING = "unquote-splicing";
    public static final String NIL = "nil";
    public static final String LAMBDA = "lambda";
    public static final String MACRO = "macro";
    public static final String LAZY = "lazy";
    
    //operations
    // all basic operations known to the compiler
    public static final String OP_ADD = "add";
    public static final String OP_ADD1 = "add1";
    public static final String OP_AND = "and-primitive";
    public static final String OP_ATOMP = "atom?";
    public static final String OP_BIGINTS = "bigints";
    public static final String OP_CAAR = "caar";
    public static final String OP_CADR = "cadr";
    public static final String OP_CAR = "car";
    public static final String OP_CDAR = "cdar";
    public static final String OP_CDDR = "cddr";
    public static final String OP_CDR = "cdr";
    public static final String OP_COMPILE = "compile";
    public static final String OP_COMPILEDP = "compiled?";
    public static final String OP_CONS = "cons";
    public static final String OP_CONS_COLON = "::";
    public static final String OP_DEBUG_ENABLE = "debug-enable";
    public static final String OP_DEFINE = "define";
    public static final String OP_DIV = "div";
    public static final String OP_DOT = "_DOT";
    public static final String OP_EXPLODE = "explode";
    public static final String OP_EVAL = "eval";
    public static final String OP_GEQ = ">=";
    public static final String OP_GT = ">";
    public static final String OP_HD = "hd";
    public static final String OP_IF = "if";
    public static final String OP_IMPLODE = "implode";
    public static final String OP_JAVAFUNCTION = "javafunction";
    public static final String OP_LET = "let";
    public static final String OP_LEQ = "<=";
    public static final String OP_LISTP = "list?";
    public static final String OP_LOAD = "load";
    public static final String OP_LOOKUP = "lookup";
    public static final String OP_MUL = "mul";
    public static final String OP_NOT = "not";
    public static final String OP_NULLP = "null?";
    public static final String OP_NUMBERP = "number?";
    public static final String OP_NUMEQ = "=";
    public static final String OP_OR = "or-primitive";
    public static final String OP_PRETTY = "pretty";
    public static final String OP_PRIN = "prin";
    public static final String OP_PRINT = "print";    
    public static final String OP_PRINTDEPTH = "printdepth";
    public static final String OP_PRINTLENGTH = "printlength";
    public static final String OP_PROGN = "begin";
    public static final String OP_READ = "read";
    public static final String OP_REM = "%";
    public static final String OP_STATICFUNCTION = "staticfunction";
    public static final String OP_SET = "set!";
    public static final String OP_SUB = "sub";
    public static final String OP_SUB1 = "sub1";
    public static final String OP_SYMBOLP = "symbol?";
    public static final String OP_TL = "tl";
    public static final String OP_TRACE = "trace-primitive";
    public static final String OP_THREAD = "thread";
    public static final String OP_UNTRACE = "untrace-primitive";
    public static final String OP_ZEROP = "zero?";
    
    // hook names
    public static final String HOOK_COMPILE = "*compile-hook*";
    public static final String HOOK_MACRO_EXPAND = "*macro-expand-hook*";
    public static final String HOOK_DEFINE = "*define-hook*";
}
