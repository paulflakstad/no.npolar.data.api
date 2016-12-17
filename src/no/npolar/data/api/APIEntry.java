package no.npolar.data.api;

import java.util.Locale;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONObject;
import static no.npolar.data.api.APIService.DEFAULT_LOCALE_NAME;

/**
 * Base class for all API entries.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public abstract class APIEntry implements APIEntryInterface {
    /** The entry ID. */
    protected String id;
    /** The backing JSON that describes the entry. */
    protected JSONObject o;
    /** The locale to use when generating strings meant for viewing. */
    protected Locale displayLocale = null;
    
    
    /**
     * Keys (names) for entry values (fields).
     */
    public static class Key {
        /** The name of the ID property, used by all API entries. */
        public static final String ID = "id";
        /** The name of the "systems" property, used by most API entry types. */
        public static final String SYSTEMS = "systems";
        /** The name of the the generic language property, used by various API entry types. */
        public static final String LANG_GENERIC = "lang";
    }
    
    /**
     * Values that are pre-defined by the API.
     */
    public static class Val {
        /** The generic pre-defined "true" value. */
        public static final String TRUE_GENERIC = "true";
        /** The generic pre-defined "false" value. */
        public static final String FALSE_GENERIC = "false";
        /** The generic pre-defined organisation name for the Norwegian Polar Institute. */
        public static final String ORG_NPI_GENERIC = "npolar.no";
    }
    
    /**
     * Timestamp patterns that are commonly used across specific entry types.
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
        
        /**
         * Gets this pattern as a string, which can then be used for example to
         * create a SimpleDateFormat instance.
         * 
         * @return This pattern as a string.
         */
        @Override
        public String toString() {
            return this.timestampPattern;
        }
    };
    
    /**
     * Default constructor: Sets everything to <code>null</code>.
     */
    public APIEntry() {
        this(null, null);
    }
    
    /**
     * Creates an entry that holds the "raw" JSON object, and with ID as fetched
     * from the JSONObject. 
     * <p>
     * The preferred locale will be set to the 
     * {@link APIService#DEFAULT_LOCALE_NAME default locale}.
     * 
     * @param raw The "raw" JSON object.
     * @see see APIService#DEFAULT_LOCALE_NAME
     */
    public APIEntry(JSONObject raw) {
        this(raw, null);
    }
    
    /**
     * Creates an entry that holds the "raw" JSON object, and with ID as fetched
     * from the JSON object.
     * <p>
     * The preferred locale will be set to the given locale. If it is 
     * <code>null</code>, the {@link APIService#DEFAULT_LOCALE_NAME default 
     * locale} will be used.
     * 
     * @param raw The "raw" JSON object.
     * @param displayLocale The preferred locale to use. If <code>null</code>, {@link DEFAULT_LOCALE_NAME default locale} is used.
     * @see see APIService#DEFAULT_LOCALE_NAME
     */
    public APIEntry(JSONObject raw, Locale displayLocale) {
        o = raw;
        try { 
            this.id = o.getString(Key.ID);
        } catch (Exception e) { 
            id = null;
        }
        this.displayLocale = displayLocale;
        if (this.displayLocale == null) {
            this.displayLocale = new Locale(DEFAULT_LOCALE_NAME);
        }
    }
    
    /**
     * Gets this entry's ID.
     * 
     * @return This entry's ID.
     */
    @Override
    public String getId() {
        return id;
    }
    
    /**
     * @see APIEntryInterface#getJSON()
     */
    @Override
    public JSONObject getJSON() {
        return o; 
    }
    
    /**
     * Gets the configured preferred locale.
     * 
     * @return The preferred locale.
     */
    public Locale getDisplayLocale() {
        return displayLocale;
    }
}
