package org.fun4j.compiler;

import static org.fun4j.compiler.Symbols.NIL;
import static org.fun4j.compiler.Symbols.QUOTE;
import static org.fun4j.compiler.Symbols.QUASIQUOTE;
import static org.fun4j.compiler.Symbols.UNQUOTE;
import static org.fun4j.compiler.Symbols.UNQUOTE_SPLICING;
import static org.fun4j.compiler.Symbols.LAMBDA;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.StringTokenizer;
import java.util.Vector;

import org.fun4j.Cons;

/**
 * Very simple Parser that reads a "lisp"-expressions and parses it to an
 * abstract syntax tree.
 * The AST is not represented as an intermediary data type but as closely to Java
 * data types as possible. This is done to allow smooth integration of Lisp and Java code.
 */
public class Parser {

	/**
	 * string containing all delimiters known to the tokenizer (i.e. our lexer)
	 */
    public static final String separators = "'` \t\n\r\f()[]{},@\"";
    
    /**
     * read a lisp expression from a string, return an abstract syntax tree
     * 
     * nil is parsed to null
     * Numbers are parsed to Integers / BigIntegers or BigDecimals
     * Symbols are parsed to Strings
     * Lists are parsed into Cons Binary trees
     * The Readmacro 'x is expanded to (quote x) 
     * The readmacro `(+ ,(+ 4 4) ,@(- 3 1)) is expanded to (quasiquote (+ (unquote (+ 4 4)) (unquote-splicing (- 3 1))))
     * 
     */
    public static Object parse(String s) {
        StringTokenizer tokens = new StringTokenizer(s, separators, true);
        return parse(tokens);
    }

    /**
     * read lisp expression, return a lambda syntax tree 
     */
    public static Object parse(StringTokenizer tokens) {
        if (!tokens.hasMoreTokens()) {
            return null;
        }
        String first = tokens.nextToken();

        if (first.equals("(")) return parseList(tokens, true);
        else if (first.equals("[")) return parseVector(null,tokens);
        else if (first.equals(")") || first.equals("]")) return null;
        else if (first.equals(NIL)) return null;
        else if (first.equals("\\")) {return LAMBDA;}
        else if (first.equals("'")) {
            return parseReaderMacro(tokens, QUOTE);
        }
        else if (first.equals("`")) {
        	return parseReaderMacro(tokens, QUASIQUOTE);
        }
        else if (first.equals(",")) {
        	return parseReaderMacro(tokens, UNQUOTE);
        }
        else if (first.equals("@")) {
            return "@";
        }
        else if (first.equals("\"")) {
            return parseString(tokens);
        }
        else if ((first.charAt(0) == '-' && first.length() > 1) || (first.charAt(0) >= '0' && first.charAt(0) <= '9')) {
            return parseNumber(first);
        }
        else if (separators.contains(first)) return parse(tokens);
        
        else return first;
    }


    private static Number parseNumber(String first) {
        if (first.contains(".") || first.contains("E")) {
            return new BigDecimal(first);
        }
        else {
            if (Compiler.useBigInts()) {                
                return new BigInteger(first);
            }
            else {
                return new Integer(first);
            }
        }
    }

    
    /**
     * parse the next expression and wrap it with a macro call.
     * @param tokens the token stream 
     * @param macroSymbol the macro call to be constructed
     * @return
     */
    private static Object parseReaderMacro(StringTokenizer tokens, String macroSymbol) {
    	Object x = parse(tokens);
    	if (UNQUOTE.equals(macroSymbol) && "@".equals(x)) {
    		return new Cons(UNQUOTE_SPLICING, new Cons(parse(tokens), null));
    	}
    	else {
    		
    		return new Cons(macroSymbol, new Cons(x, null));
    	}
    }

    /**
     * parse a String from the token stream
     * @param tokens the token stream
     * @return the resulting string
     */
    private static String parseString(StringTokenizer tokens) {
        String result = "";
        while (tokens.hasMoreTokens()) {
            String next = tokens.nextToken();
            if ("\"".equals(next)) {
                break;
            }
            else {
                result += next;
            }
        }
        return "\"" + result + "\"";
    }

    /**
     * read a list of tokens, return a lambda syntax tree
     */
    private static Object parseList(StringTokenizer tokens, boolean isFirstPosition) {
        String first = tokens.nextToken();        
        // distinguish DOT operator from pairs (x . y): 
        if (isFirstPosition && first.equals(".")) {
        	return new Cons(first, parseList(tokens, false));
        }
        if (first.equals("(")) return new Cons(parseList(tokens, true), parseList(tokens, false));
        else if (first.equals(")")) {
            return null;
        }
        else if (first.equals(NIL)) return new Cons(null, parseList(tokens, false));
        else if (first.equals("\\")) return new Cons(LAMBDA, parseList(tokens, false));
        else if (first.equals("'")) {
            Object x = parseReaderMacro(tokens, QUOTE);
            Object y = parseList(tokens, false);
            return new Cons(x, y);
        }
        else if (first.equals("`")) {
            Object x = parseReaderMacro(tokens, QUASIQUOTE);  	
            Object y = parseList(tokens, false);
            return new Cons(x, y);
        }
        else if (first.equals(",")) {
            Object x = parseReaderMacro(tokens, UNQUOTE);
            Object y = parseList(tokens, false);
            return new Cons(x, y);
        }
        else if (first.equals("\"")){
            return new Cons(parseString(tokens), parseList(tokens, false));
        }
        // parse a pair (x . y): 
        if (first.equals(".")){
            // skip next blank
        	tokens.nextToken();
        	Object second = parse(tokens);
        	// skip next )
        	tokens.nextToken();
            return second;        	
        }
        else if (first.equals("[")) {            
            return new Cons(parseVector(null, tokens), parseList(tokens, false)) ;
        }
        else if ((first.charAt(0) == '-' && first.length() > 1) || (first.charAt(0) >= '0' && first.charAt(0) <= '9')) {
            Number number = parseNumber(first);
            return new Cons(number, parseList(tokens, false));
        }
        else if (separators.contains(first)) {
            return parseList(tokens, isFirstPosition);
        }
        else return new Cons(first, parseList(tokens, false));

    }



	@SuppressWarnings("unchecked")
    private static Vector<?> parseVector(Vector<?> result, StringTokenizer tokens) {
        String first = tokens.nextToken();

        if (first.equals("]")) {
            return result;
        }

        else if (first.equals("[")) {
            Vector<?> front = parseVector(null, tokens);

            return recurseIntoParseVector((Vector<Vector<?>>) result, tokens, front);
        }
        else if (first.equals(NIL)) {
            return recurseIntoParseVector((Vector<Object>) result, tokens, null);
        }
        else if (first.equals("\\")) {
            return recurseIntoParseVector((Vector<Object>) result, tokens, LAMBDA);
        }
        else if (first.equals("'")) {
            Object quoted = parseReaderMacro(tokens, QUOTE);
            return recurseIntoParseVector((Vector<Object>) result, tokens, quoted);
        }
        else if (first.equals("`")) {
            Object qquoted = parseReaderMacro(tokens, QUASIQUOTE);
            return recurseIntoParseVector((Vector<Object>) result, tokens, qquoted);
        }
        else if (first.equals(",")) {
            Object quoted = parseReaderMacro(tokens, UNQUOTE);
            return recurseIntoParseVector((Vector<Object>) result, tokens, quoted);
        }
        else if (first.equals("\"")) {
            String str = parseString(tokens);
            return recurseIntoParseVector((Vector<String>) result, tokens, str);
        }
        else if ((first.charAt(0) == '-' && first.length() > 1) || (first.charAt(0) >= '0' && first.charAt(0) <= '9')) {
            Number number = parseNumber(first);
            return recurseIntoParseVector((Vector<Number>) result, tokens, number);
        }
        else if (separators.contains(first)) {
            return parseVector(result, tokens);
        }
        else {
            return recurseIntoParseVector((Vector<String>) result, tokens, first);
        }
    }

    /**
     * @param result
     * @param tokens
     * @param instanceToAdd
     * @return
     */
    @SuppressWarnings("unchecked")
    protected static <T> Vector<T> recurseIntoParseVector(Vector<T> result, StringTokenizer tokens, T instanceToAdd) {
        if (result == null) {
            Vector<T> temp = new Vector<T>();
            temp.add(instanceToAdd);
            return (Vector<T>) parseVector(temp, tokens);
        }
        else {
            ((Vector<T>)result).add(instanceToAdd);
            return (Vector<T>) parseVector(result, tokens);
        }
    }

 
}