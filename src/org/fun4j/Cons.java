package org.fun4j;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Simple Binary Tree. This class represents a Lisp cons object. All Lisp data structures are internally represented as
 * conses.
 * 
 * @author Thomas Mahler
 * 
 */
public class Cons {

    private static int printdepth = 100;

    private static int printlength = 100;

    private Object hd;

    private Object tl;

    /**
     * constructs a new Cons based on head and tail
     * 
     * @param head the head Object
     * @param tail the tail Object
     */
    public Cons(Object head, Object tail) {
        hd = head;
        tl = tail;
    }

    /**
     * returns the head of a the Cons object
     * 
     * @return the head
     */
    public Object getHd() {
        return hd;
    }

    /**
     * sets the head of this Cons object
     * 
     * @param hd the new head element
     */
    public void setHd(Object hd) {
        this.hd = hd;
    }

    /**
     * returns the tail of a Cons object.
     * 
     * @return the tail
     */
    public Object getTl() {
        return tl;
    }

    /**
     * sets the tail of a Cons object
     * 
     * @param tl the tail to set
     */
    public void setTl(Object tl) {
        this.tl = tl;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        else if (this == obj) {
            return true;
        }
        else if (!(obj instanceof Cons)) {
            return false;
        }
        Cons that = (Cons) obj;
        if (this.getHd() == null) {
            if (that.getHd() == null) {
                if (this.getTl() == null) {
                    if (that.getTl() == null) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else {
                    return this.getTl().equals(that.getTl());
                }
            }
            else {
                return false;
            }
        }
        else if (this.getHd().equals(that.getHd())) {
            if (this.getTl() == null) {
                if (that.getTl() == null) {
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                return this.getTl().equals(that.getTl());
            }
        }
        else {
            return false;
        }
    }

    /**
     * set the printdepth for list printing. Useful to avoid endless loops when printing circular Cons objects
     * 
     * @param newValue the new value
     * @return the old value
     */
    public static Integer printdepth(Integer newValue) {
        Integer result = printdepth;
        printdepth = newValue;
        return result;
    }

    /**
     * set the printlength for list printing.
     * 
     * @param newValue the new value
     * @return the old value
     */
    public static Integer printlength(Integer newValue) {
        Integer result = printlength;
        printlength = newValue;
        return result;
    }

    /**
     * set the printdepth for list printing. Useful to avoid endless loops when printing circular Cons objects
     * 
     * @param newValue the new value
     * @return the old value
     */
    public static BigInteger printdepth(BigInteger newValue) {
        BigInteger result = BigInteger.valueOf(printdepth);
        printdepth = newValue.intValue();
        return result;
    }

    /**
     * set the printlength for list printing.
     * 
     * @param newValue the new value
     * @return the old value
     */
    public static BigInteger printlength(BigInteger newValue) {
        BigInteger result = BigInteger.valueOf(printlength);
        printlength = newValue.intValue();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return toString1(0, 0);
    }

    /**
     * print out a Cons respecting printdepth and printlength
     * 
     * @param depth
     * @param length
     * @return the String representation of the Cons
     */
    public String toString1(int depth, int length) {
        if (depth > printdepth)
            return "***";
        String str = new String("(");
        depth++;
        Object liste = this;
        while (!(liste == null || (length > printlength))) {
            length++;
            // atom
            if (!(liste instanceof Cons)) {
                str += ". ";
                str += liste.toString();
                break;
            }
            else {
                Cons list = (Cons) liste;
                // cons
                // atom
                if (list.getHd() == null) {
                    str += "nil";
                }
                else if (!(list.getHd() instanceof Cons)) {
                    str += list.getHd().toString();
                }
                // cons
                else {
                    str += ((Cons) list.getHd()).toString1(depth, 0);
                }

                if (!(list.getTl() == null)) {
                    str += " ";
                }
                liste = list.getTl();
            }
        }
        if (length > printlength)
            str += "***";
        str += new String(")");
        return str;
    }

    /**
     * simple prettyprinting for Cons objects.
     * 
     * @return the String representation
     */
    public String prettyPrint() {
        return prettyPrint1(0, 0, 0);
    }

    /**
     * pretty print a Cons respecting printdepth and printlength
     * 
     * @param depth
     * @param length
     * @param indent
     * @return the String representation
     */
    public String prettyPrint1(int depth, int length, int indent) {
        if (depth > printdepth)
            return "***";
        String str = new String("(");
        depth++;
        Object liste = this;
        while (!(liste == null || (length > printlength))) {
            length++;
            // atom
            if (!(liste instanceof Cons)) {
                str += ". ";
                str += liste.toString();
                break;
            }
            else {
                Cons list = (Cons) liste;
                // cons
                // atom
                if (list.getHd() == null) {
                    str += "nil";
                }
                else if (!(list.getHd() instanceof Cons)) {
                    str += list.getHd().toString();
                }
                // cons
                else {
                    indent += 2;
                    str += "\n" + spaces(indent) + ((Cons) list.getHd()).prettyPrint1(depth, 0, indent);
                }
                if (!(list.getTl() == null)) {
                    str += " ";
                }
                liste = list.getTl();
            }
        }
        if (length > printlength)
            str += "***";
        str += new String(")");
        return str;
    }

    /**
     * returns a String of n blanks
     * 
     * @param n
     * @return the String
     */
    private String spaces(int n) {
        char[] chars = new char[n];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    public Cons last() {
        Cons cons = this;
        while (cons.getTl() != null) {
            cons = (Cons) cons.getTl();
        }
        return cons;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] asArray() {
        Cons cons = this;
        T[] result = (T[]) new Object[this.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (T) cons.getHd();
            cons = (Cons) cons.getTl();
        }
        return result;
    }

    public Object[] asObjectArray() {
        Cons cons = this;
        Object[] result = new Object[this.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = cons.getHd();
            cons = (Cons) cons.getTl();
        }
        return result;
    }

    public int length() {
        int result = 1;
        Cons cons = this;
        while (cons.getTl() != null) {
            cons = (Cons) cons.getTl();
            result++;
        }
        return result;
    }

    public Cons append(Cons second) {
        Cons first = this;
        Cons result = null;
        while (first != null) {
            Cons front = new Cons(first.getHd(), null);
            if (result == null) {
                result = front;
            }
            else {
                result.last().setTl(front);
            }
            first = (Cons) first.getTl();
        }
        result.last().setTl(second);
        return result;
    }

    public Cons appendWoc(Cons second) {
        Cons first = this;
        while (first.getTl() != null) {
            first = (Cons) first.getTl();
        }
        first.setTl(second);
        return this;
    }

    public static Cons append(Cons first, Cons second) {
        if (first == null) {
            return second;
        }
        else {
            return first.append(second);
        }
    }

}
