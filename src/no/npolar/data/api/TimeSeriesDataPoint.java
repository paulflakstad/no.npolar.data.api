package no.npolar.data.api;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a single data point. Data points are used mainly as the essential 
 * part of a time series.
 * <p>
 * Any data point consists mainly of a timestamp and 1–5 values. * 
 *
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class TimeSeriesDataPoint {
    public static final int VALUE_MAIN = 0;
    public static final int VALUE_LOW = 1;
    public static final int VALUE_HIGH = 2;
    public static final int VALUE_MIN = 3;
    public static final int VALUE_MAX = 4;
    /* high/low and max/min stuff should be in time series */
    protected boolean hasHigh = false;
    protected boolean hasLow = false;
    protected boolean hasMax = false;
    protected boolean hasMin = false;
    
    protected Date dateTime = null;
    protected double max = 0;   // maximum value (?)
    protected double high = 0;    // upper value (?)
    protected double val = 0;   // value
    protected double low = 0;    // lower value (?)
    protected double min = 0;   // minimum value (?)
    protected String dateTimeAccuracy = null;
    
    private SimpleDateFormat timestampFormat = null;
    /** Pattern that fits the API timestamps. Used to parse timestamps read from the API. */
    //public static final String PATTERN_DATE_API = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // 1871-06-01T12:00:00Z
    //public static final String NUMBER_FORMAT_LOCALE_HIGHCHARTS = "en";
    /** Preferred locale to use when getting/constructing language-specific data. */
    private Locale displayLocale = null;
    /** Default number format pattern. */
    public static final String DEFAULT_NUMBER_FORMAT = "#.######################";
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(TimeSeriesDataPoint.class);
    
    /**
     * Constructs a new data point based on the given details.
     * 
     * @param val The value. (High-low / max-min must be set after construction.)
     * @param dateTime The timestamp string.
     * @param timestampFormat The timestamp format.
     * @param dateTimeAccuracy The timestamp accuracy level.
     * @param displayLocale The locale to use when getting/constructing language-specific stuff.
     */
    public TimeSeriesDataPoint(double val, String dateTime, SimpleDateFormat timestampFormat, String dateTimeAccuracy, Locale displayLocale) {
        this.displayLocale = displayLocale;
        this.dateTimeAccuracy = dateTimeAccuracy;
        this.val = val;
        try {
            this.dateTime = new SimpleDateFormat(TimeSeries.PATTERN_DATE_API, displayLocale).parse(dateTime);
        } catch (Exception e) {
            //e.printStackTrace();
            // should log this
        }
        
        this.timestampFormat = timestampFormat;
    }
    
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
     * Checks if the value is an integer or not.
     * 
     * @return True if the value is an integer, false if not.
     */
    public boolean valIsInt() { return val % 1 == 0; }
    
    /**
     * Gets the (raw) value.
     * 
     * @return The (raw) value.
     */
    public double getVal() { return val; }
    
    /**
     * Gets the value, formatted according to the given format.
     *
     * @see TimeSeriesDataPoint#formatNumber(double, java.lang.String, java.util.Locale) 
     */
    public String getVal(String format) {
        return formatNumber(getVal(), format, null);
        /*
        if (format == null)
            return String.valueOf(getVal());
        else {
            DecimalFormat df = (DecimalFormat)NumberFormat.getInstance(new Locale(NUMBER_FORMAT_LOCALE_HIGHCHARTS)); // Highcharts needs 3.14, not 3,14
            df.applyPattern(format);
            return df.format(val);
        }
        */
    }
    
    /**
     * Gets the number of values contained in this data point.
     * <p>
     * If high/low values exist, 3 is returned. Otherwise 1 is returned.
     * 
     * @return The number of values contained in this data point.
     */
    public int getPointCount() { 
        if (this.hasHighLow())
            return 3;
        else
            return 1;
    }
    
    /**
     * Gets the value, formatted according to the given format and locale. 
     * 
     * @see TimeSeriesDataPoint#formatNumber(double, java.lang.String, java.util.Locale) 
     */
    public String getVal(String format, Locale locale) {
        return formatNumber(getVal(), format, locale);
        /*
        if (locale == null) {
            return getVal(format);
        }
        else {
            DecimalFormat df = (DecimalFormat)NumberFormat.getInstance(locale);
            df.applyPattern(format);
            return df.format(val);
        }
        */
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
    public Date getTimestamp() { return this.dateTime; }
    
    /**
     * Gets the timestamp, formatted according to the configured timestamp format.
     * 
     * @return The timestamp, formatted according to the configured timestamp format.
     */
    public String getTimestampFormatted() {
            return getTimestamp(this.timestampFormat);
    }
    
    @Override
    public String toString() { 
        String s = "";
        if (this.valIsInt()) {
            s += (int)val;
        } else {
            s += val;
        }
        //s += " (" + formatDate() + ")";
        s += " (" + getTimestampFormatted() + ")";
        return s;
    }
    
    
    
    /**
     * Gets the timestamp, formatted according to the given format.
     * 
     * @return The timestamp, formatted according to the given format.
     */
    public String getTimestamp(SimpleDateFormat df) {
        return df.format(dateTime);
    }
    
    public TimeSeriesDataPoint setHigh(double val) {
        this.high = val;
        this.hasHigh = true;
        return this;
    }
    public TimeSeriesDataPoint setLow(double val) {
        this.low = val;
        this.hasLow = true;
        return this;
    }
    public TimeSeriesDataPoint setMax(double val) {
        this.max = val;
        this.hasMax = true;
        return this;
    }
    public TimeSeriesDataPoint setMin(double val) {
        this.min = val;
        this.hasMin = true;
        return this;
    }
    
    public boolean hasHighLow() { return hasHigh && hasLow; } /* Should be in TimeSeries */
    
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
