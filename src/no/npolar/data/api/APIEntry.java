package no.npolar.data.api;

import no.npolar.data.api.util.APIUtil;

/**
 * Base class for all API entries.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public abstract class APIEntry {
    
    public class Key {
        public static final String ID = "id";
        public static final String SYSTEMS = "systems";
    }
    
    public class Val {
        
    }
    
    /**
     * Commonly supported timestamp patterns. 
     * <p>
     * To get a date format based on any timestamp that uses one of these 
     * patterns, use {@link APIUtil#getTimestampFormat(java.lang.String)}.
     */
    public enum TimestampPattern {
        TIME_MILLIS("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        ,TIME("yyyy-MM-dd'T'HH:mm:ss'Z'")
        ,DATE("yyyy-MM-dd")
        ,MONTH("yyyy-MM")
        ,YEAR("yyyy")
        ;
        
        private String timestampPattern = "yyyy";
        
        TimestampPattern(String timestampPattern) {
            this.timestampPattern = timestampPattern;
        }
        
        /**
         * Gets the TimestampPattern type that is associated with the given 
         * pattern string.
         * 
         * @param timestampPattern The pattern string.
         * @return The TimestampPattern type that is associated with the given pattern string, or null if none.
         */
        public static TimestampPattern forString(String timestampPattern) {
            for (TimestampPattern t : values()) {
                if (timestampPattern.equals(t.toString()))
                    return t;
            }
            return null;
        }
        
        @Override
        public String toString() {
            return this.timestampPattern;
        }
    };
}
