package org.fun4j;

public class Version {
    
    /** 
     * this field holds the major release number.
     */
    private static String RELEASE = "2.0";
    
    /**
     * this field holds the revision number.
     */
    private static String REVISION = "366";
    
    /**
     * returns the short version String: 2.0
     */
    public static String getVersion() {
        return RELEASE;
    }
    
    /**
     * returns the long version String: 2.0.366
     */
    public static String getVersionLong() {
        return RELEASE + "." + REVISION;
    }

}
