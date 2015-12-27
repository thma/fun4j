package org.fun4j;

/**
 * The NamedObject interface is used to create named objects that can be put 
 * into the runtime environment an then be looked up under their name.
 * 
 * @author Thomas Mahler
 * 
 */
public interface NamedObject {
    
    /**
     * returns the name of the object.
     */
    public String getName();
    
    /**
     * returns the documention of the object.
     */
    public String getDocumentation();
    
}
