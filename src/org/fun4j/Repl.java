package org.fun4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.fun4j.compiler.BaseFunction;
import org.fun4j.compiler.Compiler;
import org.fun4j.compiler.Expression;
import org.fun4j.compiler.MissingArgumentException;
import org.fun4j.compiler.Parser;
import org.fun4j.compiler.PreCompiler;
import org.fun4j.compiler.RunTime;

/**
 * The fun4j LISP Read eval print loop.
 * 
 * @author Thomas Mahler
 * 
 */
public class Repl {
    
    private Scanner sysinScanner = null;

    /**
     * starts up the interactive REPL.
     * 
     * @param args
     */
    public static void main(String[] args) {
        new Repl().mainLoop();
    }

    /**
     * The Toplevel read / compile / apply loop
     */
    protected void mainLoop() {
        System.out.println("Welcome to fun4j [" + Version.getVersionLong() + "]");
        // initialize runtime environment
        RunTime.wakeup();
        long count = 0;
        Compiler c = new Compiler();
        PreCompiler pc = new PreCompiler();

        boolean flag = true;

        while (flag) {
            count++;
            System.out.print("input: ");
            try {
                String input = readLine();
                if ("(quit)".equalsIgnoreCase(input) || input == null) {
                    System.out.println("good bye...");
                    flag = false;
                    break;
                }
                BaseFunction.indent = -1;
                long start = System.currentTimeMillis();

                Object term = Parser.parse(input);
                Object result = null;
                if (term != null) {
                    String name = "ReplTerm";
                    if (term.toString().startsWith("(define")) {
                        name = ((Cons) ((Cons) term).getTl()).getHd() + "";
                    } 
                    // precompile -> compile -> apply
                    Expression exp = pc.precompile(term);
                    Function fun = c.compile(exp, name);
                    if (fun != null) {
                    	try {
                    		result = fun.apply();
                    	}
                    	catch (MissingArgumentException mae) {
                    		result = new PartialApplication(fun,new Object[0]);
                    	}
                    }
                }
                long now = System.currentTimeMillis();
                System.out.println("" + (now - start) + " msecs" );                
                String log = "value" + count;
                System.out.println(log + ": " + result);
                RunTime.define(log, result);
            }
            catch (Throwable t) {
                System.out.println(t.getMessage());
                t.printStackTrace();
            }
        }
    }

    /**
     * read a single line from stdin and return as String
     * @throws IOException 
     */
    private String readLine() throws IOException {
        if (sysinScanner == null) {
            sysinScanner = new Scanner(System.in); 
        }
        return sysinScanner.nextLine();           
    }

    /**
     * loads the lisp file filename
     * 
     * @param filename
     *            the file to load
     */
    public static void load(String filename) {
    	Object term = null;
    	InputStream is = null;
        try {
            // try to find filename in the Filesystem or as loadable resource on
            // the ClassPath
            
            try {
                // lookup filesytem first
                is = new FileInputStream(filename);
            }
            catch (FileNotFoundException e) {
                // lookup from classpath, as the basic lisp files lisp/*.lsp are
                // part of the executable jar
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                is = cl.getResourceAsStream(filename);
            }            
            Scanner scanner = new Scanner(is);
            String allLines = "";

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // remove comments from code
                int comPos = line.indexOf(";;");
                // System.out.println(line + " : " + comPos);
                if (comPos >= 0) {
                    line = line.substring(0, comPos);
                }

                if (!line.isEmpty()) {
                    allLines += line;
                }
            }
            // dispose all the resources after using them.
            scanner.close();
            is.close();

            StringTokenizer tokens = new StringTokenizer(allLines, Parser.separators, true);
            Compiler c = new Compiler();
            PreCompiler pc = new PreCompiler();
            
            while (tokens.hasMoreTokens()) {
                term = Parser.parse(tokens);
                if (term != null) {
                    // stop the eval-loop on EOF
                    if ((term != null) && term.toString().startsWith("EOF")) { break; }
                    // System.out.println(term);                    
                    String name = "LoadedFromFile";
                    if ((term != null) && term.toString().startsWith("(define")) {
                        name = ((Cons) ((Cons) term).getTl()).getHd() + "";
                    } 
                    // now compile the expression evaluate it
                    Expression exp = pc.precompile(term);
                    // System.out.println(exp);
                    Function fun = c.compile(exp, name);
                    //System.out.println(fun);
                    if (fun != null) {
                        fun.apply();
                    }
                }
            }
            System.out.println("loaded file " + filename);
        }

        catch (Throwable t) {
            System.out.println("error in loading file " + filename + " : " + t.getMessage());
            t.printStackTrace();
            System.out.println("last term: " + term);
            
        }
    }

}
