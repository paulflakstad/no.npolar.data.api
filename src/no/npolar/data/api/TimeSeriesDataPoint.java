package no.npolar.data.api;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a single data point.
 * <p>
 * Data points are the essential part of a time series. Any data point consists 
 * mainly of a <strong>timestamp</strong> and <strong>1–5 values</strong>. * 
 *
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class TimeSeriesDataPoint {
    /** Compares two data points by their timestamps, will sort chronologically. */
    public static final Comparator<TimeSeriesDataPoint> COMPARE_TIMESTAMP = new Comparator<TimeSeriesDataPoint>() {
        @Override
        public int compare(TimeSeriesDataPoint o1, TimeSeriesDataPoint o2) {
            return o1.getTimestamp().toString().compareTo(o2.getTimestamp().toString());
        }
    };
    /** Value identifier: Main value. */
    public static final int VALUE_MAIN = 0;
    /** Value identifier: Low value. */
    public static final int VALUE_LOW = 1;
    /** Value identifier: High value. */
    public static final int VALUE_HIGH = 2;
    /** Value identifier: Minimum value. */
    public static final int VALUE_MIN = 3;
    /** Value identifier: Maximum value. */
    public static final int VALUE_MAX = 4;
    
    /* high/low and max/min stuff should be in time series */
    protected boolean hasHigh = false;
    protected boolean hasLow = false;
    protected boolean hasMax = false;
    protected boolean hasMin = false;
    
    //protected Date dateTime = null;
    protected double max = 0;   // maximum value (?)
    protected double high = 0;    // upper value (?)
    protected Double val = null;   // main value
    protected double low = 0;    // lower value (?)
    protected double min = 0;   // minimum value (?)
    //protected String dateTimeAccuracy = null;
    
    TimeSeriesTimestamp timestamp = null;
    
    private SimpleDateFormat timestampFormat = null;
    //** Pattern that fits the API timestamps. Used to parse timestamps read from the API. */
    //public static final String PATTERN_DATE_API = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // 1871-06-01T12:00:00Z
    //public static final String NUMBER_FORMAT_LOCALE_HIGHCHARTS = "en";
    /** Preferred locale to use when getting/constructing language-specific data. */
    private Locale displayLocale = null;
    /** Default number format pattern. */
    public static final String DEFAULT_NUMBER_FORMAT = "#.######################";
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(TimeSeriesDataPoint.class);
    
    
    /**
     * Creates a new data point, based on the given details.
     * 
     * @param val The value. (High-low / max-min must be set after construction.)
     * @param timestamp The timestamp.
     * @param displayLocale The locale to use when getting/constructing language-specific stuff.
     */
    public TimeSeriesDataPoint(double val, TimeSeriesTimestamp timestamp, Locale displayLocale) {
        this.displayLocale = displayLocale;
        this.val = val;
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the absolute minimum value contained in this data point.
     * <p>
     * The value is either the minimum, low or main value.
     * 
     * @return The absolute minimum value contained in this data point.
     */
    public double getAbsMin() {
        if (hasMin)
            return get(VALUE_MIN);
        if (hasLow)
            return get(VALUE_LOW);
        return get(VALUE_MAIN);
    }
    
    /**
     * Gets the absolute maximum value contained in this data point.
     * <p>
     * The value is either the maximum, high or main value.
     * 
     * @return The absolute maximum value contained in this data point.
     */
    public double getAbsMax() {
        if (hasMax)
            return get(VALUE_MAX);
        if (hasHigh)
            return get(VALUE_HIGH);
        return get(VALUE_MAIN);
    }
    
    //public String getDateTimeAccuracy() { return this.dateTimeAccuracy; }
    
    /**
     * Gets the value identified by the given value key.
     * 
     * @param valueKey The value key, for example {@link #VALUE_MAIN}.
     * @return The value identified by the given value key, or fallback to the main value.
     */
    public double get(int valueKey) {
        switch (valueKey) {
            case VALUE_MAIN:
                return val;
            case VALUE_LOW:
                return low;
            case VALUE_HIGH:
                return high;
            case VALUE_MIN:
                return min;
            case VALUE_MAX:
                return max;
            default:
                return val;
        }
    }
    
    /**
     * Gets the value identified by the given value key, formatted according to 
     * the given format.
     * 
     * @param valueKey The value key, for example {@link #VALUE_MAIN}.
     * @param format The format pattern.
     * @return The value identified by the given value key, or fallback to the main value.
     * @see #formatNumber(double, java.lang.String, java.util.Locale) 
     */
    public String get(int valueKey, String format) {
        switch (valueKey) {
            case VALUE_MAIN:
                return formatNumber(val, format, displayLocale);
            case VALUE_LOW:
                return formatNumber(low, format, displayLocale);
            case VALUE_HIGH:
                return formatNumber(high, format, displayLocale);
            case VALUE_MIN:
                return formatNumber(min, format, displayLocale);
            case VALUE_MAX:
                return formatNumber(max, format, displayLocale);
            default:
                return formatNumber(val, format, displayLocale);
        }
    }
    
    /**
     * Gets the number of values contained in this data point.
     * <p>
     * If high/low values exist, 3 is returned. Otherwise 1 is returned.
     * 
     * @return The number of values contained in this data point.
     */
    public int getPointCount() { 
        if (this.hasMinMax())
            return 5;
        if (this.hasHighLow())
            return 3;
        else
            return 1;
    }
    
    /**
     * Checks if the value is an integer or not.
     * 
     * @return True if the value is an integer, false if not.
     */
    public boolean isIntValue() { return val % 1 == 0; }
    
    /**
     * Gets the (raw) value.
     * 
     * @return The (raw) value.
     */
    public double getValue() { return val; }
    
    /**
     * Gets the value, formatted according to the given format.
     *
     * @return The value, formatted according to the given format.
     * @see TimeSeriesDataPoint#formatNumber(double, java.lang.String, java.util.Locale) 
     */
    public String getValue(String format) {
        return formatNumber(getValue(), format, null);
        /*
        if (format == null)
            return String.valueOf(getValue());
        else {
            DecimalFormat df = (DecimalFormat)NumberFormat.getInstance(new Locale(NUMBER_FORMAT_LOCALE_HIGHCHARTS)); // Highcharts needs 3.14, not 3,14
            df.applyPattern(format);
            return df.format(val);
        }
        */
    }
    
    /**
     * Gets the value, formatted according to the given format and locale. 
     * 
     * @return The value, formatted according to the given format and locale. 
     * @see #formatNumber(double, java.lang.String, java.util.Locale) 
     */
    public String getValue(String format, Locale locale) {
        return formatNumber(getValue(), format, locale);
        /*
        if (locale == null) {
            return getValue(format);
        }
        else {
            DecimalFormat df = (DecimalFormat)NumberFormat.getInstance(locale);
            df.applyPattern(format);
            return df.format(val);
        }
        */
    }
    
    /**
     * Gets all the values, formatted according to the given format and locale.
     * <p>
     * Separate values are comma-separated in the returned string.
     * 
     * @return All the values, comma-separated and formatted according to the given format and locale.
     * @see #formatNumber(double, java.lang.String, java.util.Locale) 
     */
    public String getAllValues(String format, Locale locale) {
        String s = formatNumber(this.min, format, locale) 
                + ", " + formatNumber(this.low, format, locale)
                + ", " + formatNumber(this.val, format, locale)
                + ", " + formatNumber(this.high, format, locale)
                + ", " + formatNumber(this.max, format, locale);
        return s;
    }
    
    /**
     * Formats a number according to the given format and locale.
     * <p>
     * If both format and locale are <code>null</code>, no formatting will be 
     * applied. Instead, the given number is simply converted to a string.
     * 
     * @param number The number to format.
     * @param format The format to use, f.ex. "#.00000" See {@link java.text.DecimalFormat}. If <code>null</code>, {@link TimeSeriesDataPoint#DEFAULT_NUMBER_FORMAT} is used.
     * @param locale The locale to use. If <code>null</code>, the "current" locale is used.
     * @return The given number, formatted according to the given format and locale.
     */
    protected String formatNumber(double number, String format, Locale locale) {
        if (format == null && locale == null) {
            // No format or locale set, just convert to string
            return String.valueOf(number);
        } 
        
        // Make sure we have format & locale set
        if (locale == null) {
            locale = displayLocale;
        }
        if (format == null) {
            format = DEFAULT_NUMBER_FORMAT;
        }
            
        // Do the formatting
        DecimalFormat df = (DecimalFormat)NumberFormat.getInstance(locale);
        df.applyPattern(format);
        return df.format(number);
    }
    
    /**
     * Gets the high and low values, separated, formatted and localized 
     * according to the given format pattern and locale.
     * 
     * @param format The format pattern.
     * @param separator The separator. If null, the default (comma) is be used.
     * @param locale The locale.
     * @return The high and low values.
     */
    public String getHighLow(String format, String separator, Locale locale) {
        if (this.hasHighLow()) {
            if (separator == null)
                separator = ",";
            
            if (format == null) {
                return String.valueOf(low) + separator + String.valueOf(high);
            } else {
                return formatNumber(low, format, locale) + separator + formatNumber(high, format, locale);
                /*
                DecimalFormat df = (DecimalFormat)NumberFormat.getInstance(new Locale(NUMBER_FORMAT_LOCALE_HIGHCHARTS)); // Highslide needs 3.14, not 3,14
                df.applyPattern(format);
                return df.format(low) + separator + df.format(high);
                */
            }
        }
        return null;
        
    }
    
    /**
     * Gets the (raw) timestamp.
     * 
     * @return The (raw) timestamp.
     */
    /*public Date getTimestamp() { 
        if (this.timestamp != null) {
            return this.timestamp.getTime();
        }
        return this.dateTime; 
    }*/
    
    /**
     * Gets the timestamp.
     * 
     * @return The timestamp.
     */
    public TimeSeriesTimestamp getTimestamp() {
        return this.timestamp;
    }
    
    /**
     * Gets the timestamp, formatted according to the configured timestamp format.
     * 
     * @return The timestamp, formatted according to the configured timestamp format.
     */
    /*public String getTimestampFormatted() {
        return getTimestamp(this.timestampFormat);
    }*/
    
    /**
     * Gets a string representation of this instance, consisting of the main 
     * value and the timestamp.
     * 
     * @return A string representation of this instance.
     */
    @Override
    public String toString() { 
        String s = "";
        if (this.isIntValue()) {
            s += val.intValue();
        } else {
            s += val;
        }
        //s += " (" + formatDate() + ")";
        //s += " (" + getTimestampFormatted() + ")";
        s += " (" + getTimestamp().toString() + ")";
        return s;
    }
    
    /**
     * Gets the timestamp, formatted according to the given format.
     * 
     * @return The timestamp, formatted according to the given format.
     */
    /*public String getTimestamp(SimpleDateFormat df) {
        return df.format(dateTime);
    }*/
    
    /**
     * Sets the high value.
     * 
     * @param val The new high value.
     * @return This instance, updated.
     */
    public TimeSeriesDataPoint setHigh(double val) {
        this.high = val;
        this.hasHigh = true;
        return this;
    }
    
    /**
     * Sets the low value.
     * 
     * @param val The new low value.
     * @return This instance, updated.
     */
    public TimeSeriesDataPoint setLow(double val) {
        this.low = val;
        this.hasLow = true;
        return this;
    }
    
    /**
     * Sets the max value.
     * 
     * @param val The new max value.
     * @return This instance, updated.
     */
    public TimeSeriesDataPoint setMax(double val) {
        this.max = val;
        this.hasMax = true;
        return this;
    }
    
    /**
     * Sets the min value.
     * 
     * @param val The new min value.
     * @return This instance, updated.
     */
    public TimeSeriesDataPoint setMin(double val) {
        this.min = val;
        this.hasMin = true;
        return this;
    }
    
    /** 
     * Determines whether or not this data point has high and low values set.
     * 
     * @return True if both high and low values are set, false if not. 
     */
    public boolean hasHighLow() { return hasHigh && hasLow; } /* Should be in TimeSeries (?) */
    
    /** 
     * Determines whether or not this data point has min and max values set.
     * 
     * @return True if both min and max values are set, false if not. 
     */
    public boolean hasMinMax() { return hasMin && hasMax; } /* Should be in TimeSeries (?) */
    
    /** 
     * Determines whether or not this data point has a high value set.
     * 
     * @return True if this data point has a high value set, false if not. 
     */
    public boolean hasHigh() { return hasHigh; }
    
    /** 
     * Determines whether or not this data point has a low value set.
     * 
     * @return True if this data point has a low value set, false if not. 
     */
    public boolean hasLow() { return hasLow; }
    
    /** 
     * Determines whether or not this data point has a max value set.
     * 
     * @return True if this data point has a max value set, false if not. 
     */
    public boolean hasMax() { return hasMax; }
    
    /** 
     * Determines whether or not this data point has a min value set.
     * 
     * @return True if this data point has a min value set, false if not. 
     */
    public boolean hasMin() { return hasMin; }
    
    /** 
     * Determines whether or not this data point has a main value set.
     * 
     * @return True if this data point has a main value set, false if not. 
     */
    public boolean hasVal() { return val != null; }
    
    /*
     * Gets the datetime for this data point, formatted according to its own
     * defined accuracy level.
     * 
     * @return The datetime for this data point, formatted according to its own defined accuracy level.
     * @deprecated  Use {@link TimeSeriesDataPoint#getTimestamp(java.text.SimpleDateFormat) } instead (the date format is known by the time series).
     *
    public String formatDate() {
        if (dateTime == null)
            return null;
        
        String f = null;
        
        if (dateTimeAccuracy == null) {
            f = PATTERN_DATE_API;
        } else if (dateTimeAccuracy.indexOf(TimeSeries.DATE_FORMAT_UNIX_SECOND) > -1) { // Second
            f = "yyyy-MM-dd HH:mm:ss";
        } else if (dateTimeAccuracy.indexOf(TimeSeries.DATE_FORMAT_UNIX_MINUTE) > -1) { // Minute
            f = "yyyy-MM-dd HH:mm:00";
        } else if (dateTimeAccuracy.indexOf(TimeSeries.DATE_FORMAT_UNIX_HOUR) > -1) {   // Hour
            f = "yyyy-MM-dd HH:00:00";
        } else if (dateTimeAccuracy.indexOf(TimeSeries.DATE_FORMAT_UNIX_DATE) > -1) {   // Day of month (date)
            f = "d MMM yyyy";
        } else if (dateTimeAccuracy.indexOf(TimeSeries.DATE_FORMAT_UNIX_MONTH) > -1) {  // Month
            f = "MMMM yyyy";
        } else if (dateTimeAccuracy.indexOf(TimeSeries.DATE_FORMAT_UNIX_YEAR) > -1) {   // Year
            f = "yyyy";
        } else {                                                                            // Default
            f = "'" + dateTimeAccuracy + "'"; // No format, use the "format" as a literal, e.g. "2007/08" or "1990–2010"
        }
        return new SimpleDateFormat(f, displayLocale).format(dateTime);
    }
    //*/
    /*public String getDateTimeAccuracy() { return dateTimeAccuracy; }*/
}
