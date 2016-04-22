package no.npolar.data.api;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A time series timestamp (or "time marker") describes a point in time.
 * <p>
 * The different types (see the TYPE_XXX constants) indicates the 
 * accuracy/resolution of the timestamp.
 * <p>
 * Timestamps of "literal" type are used literally as-is, e.g. "2007/2008".
 * <p>
 * Tested on time series stored in the NPIDC. These time series use date or year.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public class TimeSeriesTimestamp implements Comparable<TimeSeriesTimestamp> {
    /** Comparator that can be used to sort timestamps chronologically. */
    public static final Comparator<TimeSeriesTimestamp> CHRONOLOGICAL = new Comparator<TimeSeriesTimestamp>() {
        @Override
        public int compare(TimeSeriesTimestamp o1, TimeSeriesTimestamp o2) {
            //if ((o1.getType() == TYPE_LITERAL && o2.getType() == TYPE_LITERAL) 
            //        || (o1.getType() != TYPE_LITERAL && o2.getType() != TYPE_LITERAL)) {
                return o1.toString().compareTo(o2.toString());
            //} else {
                //throw new UnsupportedOperationException("A literal timestamp cannot be compared to a non-literal one.");
            //}
        }
    };
    /** The default time of day, used if missing. */
    public static final String DEFAULT_CLOCKTIME = "12:00:00";
    /** The default day of month (date), used if missing. */
    public static final String DEFAULT_DAY_OF_MONTH = "01";
    /** The default month of year (month), used if missing. */
    public static final String DEFAULT_MONTH_OF_YEAR = "01";
    /** Date addon: appended to date-only timestamps to create complete timestamps (down to the second). See {@link #PATTERN_TIME_STANDARD}. */
    public static final String ADDON_FOR_DATE = "T" + DEFAULT_CLOCKTIME + "Z";
    /** Month addon: appended to month-only timestamps to create complete timestamps (down to the second). See {@link #PATTERN_TIME_STANDARD}. */
    public static final String ADDON_FOR_MONTH = "-" + DEFAULT_DAY_OF_MONTH + ADDON_FOR_DATE;
    /** Year addon: appended to year-only timestamps to create complete timestamps (down to the second). See {@link #PATTERN_TIME_STANDARD}. */
    public static final String ADDON_FOR_YEAR = "-" + DEFAULT_MONTH_OF_YEAR + ADDON_FOR_MONTH;
    
    /** This date format pattern describes how this class represents a complete, "standard" timestamp. */
    public static final String PATTERN_TIME_STANDARD = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // 1871-06-01T12:00:00Z
    /** This class' "standard" date format, uses {@link #PATTERN_TIME_STANDARD}. */
    public static final SimpleDateFormat DATE_FORMAT_STANDARD = new SimpleDateFormat(PATTERN_TIME_STANDARD);
    /** Supported timestamp patterns. Note that the order is important here, index must correspond to the TYPE_XXXX integer value. */
    public static final String[] PATTERNS_SUPPORTED = new String[] { 
        "yyyy",
        "yyyy-MM",
        "yyyy-MM-dd",
        PATTERN_TIME_STANDARD
    };
    /** Timestamp type: year. The value must equal the corresponding patterns index in {@link #PATTERNS_SUPPORTED}. */
    public static final int TYPE_YEAR = 0;
    /** Timestamp type: month. The value must equal the corresponding patterns index in {@link #PATTERNS_SUPPORTED}. */
    public static final int TYPE_MONTH = 1;
    /** Timestamp type: date. The value must equal the corresponding patterns index in {@link #PATTERNS_SUPPORTED}. */
    public static final int TYPE_DATE = 2;
    /** Timestamp type: time (down to the second). The value must equal the corresponding patterns index in {@link #PATTERNS_SUPPORTED}. */
    public static final int TYPE_TIME = 3;
    /** Timestamp type: literal (use as-is). */
    public static final int TYPE_LITERAL = -1;
    /** Timestamp type: unknown. */
    public static final int TYPE_UNKNOWN = Integer.MIN_VALUE;
    
    /** Holds the standard timestamp. */
    private String timestamp = null;
    /** Holds the original timestamp. */
    private String original = null;
    /** Holds the year. */
    private int year = Integer.MIN_VALUE;
    /** Holds the type. */
    private int type = TYPE_UNKNOWN;
    /** Holds the time. */
    private Date time = null;
    //private String utcDate = null;
    /** Holds the native date format. */
    private SimpleDateFormat nativeDateFormat = null;
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(TimeSeriesTimestamp.class);
    
    /**
     * Creates a new timestamp, based on the given year.
     * <p>
     * The resulting timestamp will represent 1 Jan of that year.
     * 
     * @param year The year.
     */
    public TimeSeriesTimestamp(int year) {
        this.original = String.valueOf(year);
        this.year = year;
        timestamp = "" + this.year + ADDON_FOR_YEAR;
        type = TYPE_YEAR;
    }
    
    /**
     * Creates a new timestamp, based on the given long value.
     * 
     * @see java.util.Date#setTime() 
     * @param millis The long representation of a date(time).
     */
    public TimeSeriesTimestamp(long millis) {
        this.original = String.valueOf(millis);
        Date tempDate = new Date(millis);
        timestamp = DATE_FORMAT_STANDARD.format(tempDate);
        year = Integer.parseInt(timestamp.substring(0, 4));
        type = TYPE_TIME;
    }
    
    /**
     * Creates a new timestamp of the given type.
     * <p>
     * Knowing the type improves performance, as it does not have to be sniffed.
     * 
     * @param timestamp The timestamp.
     * @param type The timestamp type.
     */
    public TimeSeriesTimestamp(String timestamp, int type) {
        this.original = timestamp;
        this.type = type;
        
        // Handle case: a "real" type was given but the actual timestamp 
        // was a literal (like e.g. "2015/2016")
        if (this.type != TYPE_LITERAL && this.type != TYPE_UNKNOWN) {
            try {
                if (PATTERNS_SUPPORTED[this.type].length() != timestamp.length())  {
                    this.type = TYPE_LITERAL;
                }
            } catch (Exception e) {
                this.type = TYPE_LITERAL;
            }
        }
        
        if (this.type != TYPE_LITERAL && this.type != TYPE_UNKNOWN) {
            this.timestamp = timestamp + getDefaultAddon(type);
        } else {
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Creates a new timestamp.
     * <p>
     * The timestamp type is determined by sniffing the given timestamp. This  
     * may cause a performance hit, so consider using 
     * {@link #TimeSeriesTimestamp(java.lang.String, int)} whenever the type is 
     * known in advance.
     * 
     * @param timestamp The timestamp.
     */
    public TimeSeriesTimestamp(String timestamp) {
        this.type = TYPE_UNKNOWN;
        this.original = timestamp;
        
        for (int i = PATTERNS_SUPPORTED.length-1; i >= 0; i--) {
            
            try {
                if (timestamp.length() != PATTERNS_SUPPORTED[i].length()) {
                    continue;
                }
                // Rely on the parse attempt failing if the pattern does not match
                new SimpleDateFormat(PATTERNS_SUPPORTED[i]).parse(timestamp);
                
                // Reaching this point => parse did not fail 
                type = i; // Relies on a match between types and the index of the corresponding pattern in the "supported patterns" array
                if (type == TYPE_TIME) {
                    this.timestamp = timestamp;
                } else if (type != TYPE_LITERAL && type != TYPE_UNKNOWN) {
                    //this.timestamp = DATE_FORMAT_STANDARD.format(DATE_FORMAT_STANDARD.parse(timestamp + getDefaultAddon(type)));
                    this.timestamp = timestamp + getDefaultAddon(type);
                }
                // We have now determined a type, so break the sniffer loop
                break;
            } catch (Exception e) {}
        }
        
        if (type < 0) {
            // Means no supported patterns matched => assume literal (e.g. "2002/2003")
            type = TYPE_LITERAL;
            this.timestamp = timestamp;
        }
        /*
        if (type >= TYPE_YEAR && type <= TYPE_TIME) {
            try {
                // Create the utc date
                if (type == TYPE_YEAR) {
                    utcDate = original + ",0,1";
                } else if (type == TYPE_MONTH) {
                    String[] timestampParts = original.split("-");
                    utcDate = "" + timestampParts[0] + "," + (Integer.parseInt(timestampParts[1])-1) + ",1";
                } else {
                    String[] timestampParts = original.substring(0,10).split("-");
                    utcDate = "" + timestampParts[0] + "," + (Integer.parseInt(timestampParts[1])-1) + "," + timestampParts[2];
                }
            } catch (Exception e) {
                //System.out.println("ERROR creating UTC date string: " + e.getMessage());
            }
        }
        */
    }
    
    /**
     * Gets the string that needs to be appended to timestamps of the given type, 
     * in order to turn them into "complete" timestamps (accurate down to the 
     * second).
     * 
     * @param type The timestamp type. See the TYPE_XXX constants.
     * @return The string that needs to be appended to timestamps of the given type, in order to "complete" them.
     */
    private String getDefaultAddon(int type) {
        if (type == TYPE_DATE) {
            return ADDON_FOR_DATE;
        }
        if (type == TYPE_MONTH) {
            return ADDON_FOR_MONTH;
        }
        if (type == TYPE_YEAR) {
            return ADDON_FOR_YEAR;
        }
        return "";
    }
    
    /**
     * Gets the type for this timestamp.
     * 
     * @return The type for this timestamp.
     */
    public int getType() {
        return this.type;
    }
    
    /*public Date getTime(SimpleDateFormat dateFormat) throws ParseException {
        SimpleDateFormat df = dateFormat;
        if (df == null) {
            df = DATE_FORMAT_STANDARD;
        } else {
            df.parse(timestamp); // Validity check for given date format (throw exception HERE if invalid)
        }
        
        try {
            return df.parse(timestamp);
        } catch (Exception e) {
            return null;
        }
    }*/
    
    /**
     * Gets a {@link java.util.Date} representation of this timestamp. 
     * <p>
     * The returned date represents 1 Jan 12:00:00 (or any partial time piece 
     * therein) for timestamps that are not originally defined down to the 
     * second.
     * 
     * @return A {@link java.util.Date} representation of this timestamp. 
     */
    public Date getTime() {
        if (time == null) {
            try { 
                time = DATE_FORMAT_STANDARD.parse(timestamp);
                return time;
            } catch (Exception e) {
                return null;
            }
        } else {
            return time;
        }
        /*try {
            return getTime(null);
        } catch (Exception e) {
            return null;
        }*/
    }
    
    /**
     * Gets the original timestamp, as provided when this instance was created.
     * 
     * @return The original timestamp, as provided when this instance was created.
     */
    public String getOriginal() {
        return original;
    }
    
    /*
    public String getUTCDate() {
        return utcDate;
    }
    */
    
    /**
     * Gets the native date format pattern, that is, the pattern that is 
     * consistent with what the original timestamp string actually looks like.
     * 
     * @return The native date format pattern.
     */
    public String getNativeDateFormatPattern() {
        if (this.getType() == TYPE_LITERAL) {
            return "'" + this.timestamp + "'";
        }
        else if (this.getType() == TYPE_UNKNOWN) {
            return "'NO DATE'";
        } else {
            try {
                return PATTERNS_SUPPORTED[this.getType()];
            } catch (Exception e ) {
                return null;
            }
        }
    }
    
    /**
     * @return True if this timestamp is of type year, false otherwise.
     */
    public boolean isYearType() {
        return this.getType() == TYPE_YEAR;
    }
    
    /**
     * @return True if this timestamp is of the given type, false otherwise.
     */
    public boolean isType(int type) {
        return this.getType() == type;
    }
    
    /** 
     * @return True if this timestamp is more precise than the given type, false otherwise.
     */
    public boolean isMorePreciseThan(int type) {
        return this.getType() > type;
    }
    
    /** 
     * @return True if this timestamp is of type literal, false otherwise. 
     */
    public boolean isLiteralType() {
        return this.getType() == TYPE_LITERAL;
    }
    
    @Override
    public String toString() {
        //System.out.println("Type is " + this.getType() + ", original is '" + original + "'.");
        if (isLiteralType()) {
            return getOriginal();
        }
        return format(getNativeDateFormat());
    }
    
    /**
     * Formats the timestamp using the given date format.
     * 
     * @param returnFormat The format to apply.
     * @return The timestamp, formatted using the given date format.
     */
    public String toString(SimpleDateFormat returnFormat) {
        if (isLiteralType())
            return getOriginal();
        return format(returnFormat);
    }
    
    /**
     * Formats the timestamp using the given date format.
     * 
     * @param returnFormat The format to apply.
     * @return The timestamp, formatted using the given date format.
     */
    public String format(SimpleDateFormat returnFormat) {
        try {
            return returnFormat.format(this.getTime());
        } catch (Exception e) {
            return timestamp; // Literal
        }
    }
    
    /**
     * Formats the timestamp "natively", that is, using the native date format.
     * <p>
     * This method is just an alias for the {@link #toString()} method.
     *
     * @return The timestamp, formatted using its native date format.
     */
    public String formatNatively() {
        return toString();
    }
    
    /**
     * Gets the native date format, which should be consistent with what the 
     * original timestamp string actually looks like.
     * 
     * @return The native date format, or <code>null</code> if none can be determined.
     * @see #getNativeDateFormatPattern() 
     */
    public SimpleDateFormat getNativeDateFormat() {
        if (nativeDateFormat == null) {
            try {
                nativeDateFormat = new SimpleDateFormat(getNativeDateFormatPattern());
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Cannot determine native date format for timestamp '" + this.original + "'.", e);
                }
            }
        }
        return nativeDateFormat;
    }
    
    @Override
    public int compareTo(TimeSeriesTimestamp other) {
        //return this.toString().compareTo(other.toString());
        return this.timestamp.compareTo(other.timestamp);
    }
    
    @Override
    public int hashCode() {
        /*return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(timestamp).toHashCode();*/
        return this.timestamp.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof TimeSeriesTimestamp))
            return false;
        if (obj == this)
            return true;

        TimeSeriesTimestamp rhs = (TimeSeriesTimestamp)obj;
        return this.timestamp.equals(rhs.timestamp);
    }
    
    /**
     * Determines whether or not this timestamp has a year set.
     * 
     * @return True if this timestamp has a year set, false otherwise.
     */
    public boolean hasYear() { return this.year > Integer.MIN_VALUE; }
}
