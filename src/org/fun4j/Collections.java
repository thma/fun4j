package org.fun4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * This Class contains all methods of the Fun4j Collection API.
 * @author Thomas Mahler
 */
public class Collections {

    /**
     * this utility method creates a Collection from a comma separated list of elements
     * 
     * @param elements Comma separated list of elements
     * @return the resulting Collection
     */
    @SafeVarargs
	public static <E> Collection<E> asCollection(E... elements) {
        Collection<E> result = new ArrayList<E>();
        for (E el : elements) {
            result.add(el);
        }
        return result;
    }

    /**
     * this utility method converts a Collection into a Lisp list (i.e. a Cons object)
     * 
     * @param <E> the element type of the Collection
     * @param col the collection
     * @return the resulting Cons object
     */
    @SuppressWarnings("unchecked")
    public static <E> Cons asCons(Collection<E> col) {
        if (col == null || col.size() == 0) {
            return null;
        }
        else {
            E[] elements = (E[]) col.toArray();
            Cons result = new Cons(elements[0], null);
            Cons last = result;
            for (int i = 1; i < elements.length; i++) {
                Cons newLast = new Cons(elements[i], null);
                last.setTl(newLast);
                last = newLast;
            }
            return result;
        }
    }

    /**
     * applies the Function fun to each element of the Collection col. The results of the function applications are
     * collected into a new collection, which is returned as result.
     * 
     * @param <E> element type of input Collection
     * @param <F> element type of output Collection
     * @param fun the Function to be applied to all elements of Collection col
     * @param col the collection to be mapped
     * @return the resulting collection
     */
    @SuppressWarnings("unchecked")
    public static <E, F> Collection<F> map(Function fun, Collection<E> col) {
        Collection<F> result = new ArrayList<F>(col.size());
        for (E element : col) {
            result.add((F)fun.apply(element));
        }
        return result;
    }

    /**
     * if the Predicate predicate evaluates to true for any element 
     * of the input collection true is returned, else false
     * 
     * @param <E> the element Type of the input Collection
     * @param predicate the test Predicate
     * @param collection the input Collection
     * @return true or false
     */
    public static <E> boolean exists(Predicate predicate, Collection<E> collection) {
        for (E el : collection) {
            if (predicate.apply(el)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * The first element of the input collection for which
     * predicate evaluates to true is returned. 
     * if no matching element is found <code>null<code> is returned.
     * 
     * @param <E> the element Type of the input Collection
     * @param predicate the predicate Function
     * @param collection the input Collection
     * @return the first matching element of type <E> or <code>null<code>.
     */
    public static <E> E find(Predicate predicate, Collection<E> collection) {
        for (E element : collection) {
            if (predicate.apply(element)) {
                return element;
            }
        }
        return null;
    }

    /**
     * if the Predicate predicate evaluates to true for all elements in the input collection, true is returned, else
     * false.
     * 
     * @param <E> the element type of the input collection
     * @param predicate the predicate Function
     * @param collection the input Collection
     * @return true or false
     */
    public static <E> boolean forAll(Predicate predicate, Collection<E> collection) {
        for (E el : collection) {
            if (!predicate.apply(el)) {
                return false;
            }
        }
        return true;
    }

    /**
     * all elements of the input collection that match the predicate are collected into a new Collection. This
     * Collection is returned as result
     * 
     * @param <E> the element type of the input collection
     * @param predicate the predicate Function
     * @param collection the input Collection
     * @return the filtered Collection
     */
    public static <E> Collection<E> filter(Predicate predicate, Collection<E> collection) {
        Collection<E> result = new ArrayList<E>();
        for (E el : collection) {
            if (predicate.apply(el)) {
                result.add(el);
            }
        }
        return result;
    }

    /**
     * performs a foldright of Function fun over a Collection. Useful operation to compute a total based on all elements
     * of a Collection
     * 
     * @param <E> the element type of the input collection
     * @param <F> the result type
     * @param fun the accumulating function
     * @param acc the accumulator that stores the results
     * @param col the input Collection
     * @return the reuslt of type <F>
     */
    public static <E, F> F foldright(Function fun, F acc, Collection<E> col) {
        return Collections.foldrightIterator(fun, acc, col.iterator());
    }

    /**
     * performs a foldleft operation of Function fun over the Collection col.
     * 
     * @param <E> the element type of the input Collection
     * @param <F> the result type
     * @param fun the accumulating function
     * @param acc the accumulator
     * @param col the input collection
     * @return the result of type <F>
     */
    public static <E, F> F foldleft(Function fun, F acc, Collection<E> col) {
        return Collections.foldleftIterator(fun, acc, col.iterator());
    }

    /**
     * this utility method creates a Collection from a String
     * 
     * @param string the input String
     * @return the resulting Collection
     */
    public static Collection<Character> fromString(String input) {
        Collection<Character> result = new ArrayList<Character>();
        for (int i = 0; i < input.length(); i++) {
            result.add(input.charAt(i));
        }
        return result;
    }

    /**
     * This utility method converts a Lisp list (i.e. a Cons object) into a Collection.
     * 
     * @param cons the Lisp list
     * @return the resulting Collection
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Collection fromCons(Cons cons) {
        if (cons == null) {
            return null;
        }
        else {
            Collection result = new ArrayList();
            while (cons != null) {
                result.add(cons.getHd());
                cons = (Cons) cons.getTl();
            }
            return result;
        }
    }

    /**
     * applies the Function fun to each element of the Iterator iter. The results of the function applications are
     * collected into a new collection, which is returned as result.
     * 
     * @param <E> the element type of the input Iterator
     * @param <F> the element type of the output Collection
     * @param fun the Function to be applied to each element of the Iterator
     * @param iter the input Iterator
     * @return the resulting Collection
     */
    @SuppressWarnings("unchecked")
    public static <E, F> Collection<F> mapIterator(Function fun, Iterator<E> iter) {
        Collection<F> result = new ArrayList<F>();
        while (iter.hasNext()) {
            result.add((F) fun.apply(iter.next()));
        }
        return result;
    }

    /**
     * performs a foldright of Function fun over Iterator iter. Useful operation to compute a total based on all
     * elements of an Iterator.
     * 
     * @param <E> The element type of the Iterator
     * @param <F> the result type
     * @param fun the accumulating function
     * @param acc the accumulator
     * @param iter the input Iterator
     * @return the result of type <F>
     */
    @SuppressWarnings("unchecked")
    public static <E, F> F foldrightIterator(Function fun, F acc, Iterator<E> iter) {
        if (!iter.hasNext()) {
            return acc;
        }
        else {
            return (F) fun.apply(iter.next(), foldrightIterator(fun, acc, iter));
        }
    }

    /**
     * performs a foldleft operation of Function fun over Iterator iter.
     * 
     * @param <E> the element type of the input Iterator
     * @param <F> the result type
     * @param fun the accumulating Function
     * @param acc the accumulator
     * @param iter the input operator
     * @return the result of type <F>
     */
    @SuppressWarnings("unchecked")
    public static <E, F> F foldleftIterator(Function fun, F acc, Iterator<E> iter) {
        if (!iter.hasNext()) {
            return acc;
        }
        else {
            while (iter.hasNext()) {
                acc = (F) fun.apply(acc, iter.next());
            }
            return acc;
        }
    }
       
    /**
     * sorts a List according to the ordering defined by the Predicate predicate
     * @param list the List to be sorted
     * @param predicate a predicate representing a total ordering (e.g. <code>predicate(\"(lambda (a b) (> a b))\")</code>) 
     * @return a new list with all elements sorted according to the predicate ordering
     */
    public static <E> List<E> sort(List<E> list, final Predicate predicate) {
        Comparator<E> comparator = asComparator(predicate);
        // as java.util.Collections.sort is destructive we have to clone the input list first
        List<E> result = new ArrayList<E>(list);
    	java.util.Collections.sort(result, comparator);
    	return result;
    }

    /**
     * wraps a Predicate as a {@link Comparator}.
     * the user is responsible to maintain that the predicate represents
     * a total ordering as required by the {@link Comparator} interface.
     * @param the ordering Predicate (e.g. < or >)
     * @return a Comparator wrapping the input Predicate
     */
    public static <T> Comparator<T> asComparator(final Predicate pred) {        
        return new Comparator<T>() {
            @Override
            public int compare(T x, T y) {
                if (pred.apply(x,y)) {
                    return 1;
                }
                else if (pred.apply(y,x)) {
                    return -1;
                }
                else {
                    return 0;
                }
            }   
        };  
    }
    
}
