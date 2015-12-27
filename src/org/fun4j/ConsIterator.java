package org.fun4j;

import java.util.Iterator;


public class ConsIterator implements Iterator<Object> {
    
    private Cons underlyingCons; 
    
    public ConsIterator(Cons input) {
        underlyingCons = input;
    }

    @Override
    public boolean hasNext() {
        return (underlyingCons != null);
    }

    @Override
    public Object next() {
        Object result = underlyingCons.getHd();
        underlyingCons = (Cons) underlyingCons.getTl();
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
