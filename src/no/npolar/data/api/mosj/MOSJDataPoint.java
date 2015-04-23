package no.npolar.data.api.mosj;

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
 * 
 * Any data point consists mainly of a timestamp and 1–5 values.
 *
 * @deprecated Use {@link no.npolar.data.api.TimeSeriesDataPoint} instead.
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class MOSJDataPoint {
    /* high/low and max/min stuff should be in time series */
    protected boolean hasHi = false;
    protected boolean hasLo = false;
    
    protected Date dateTime = null;
    protected double max = 0;   // maximum value (?)
    protected double hi = 0;    // upper value (?)
    protected double val = 0;   // value
    protected double lo = 0;    // lower value (?)
    protected double min = 0;   // minimum value (?)
    protected String dateTimeAccuracy = null;
    
    private SimpleDateFormat timestampFormat = null;
    
    public static final String PATTERN_DATE_API = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // 1871-06-01T12:00:00Z
    public static final String NUMBER_FORMAT_LOCALE_HIGHCHARTS = "en";
    
    private Locale displayLocale = null;
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(MOSJDataPoint.class);
    
    public MOSJDataPoint(double val, String dateTime, SimpleDateFormat timestampFormat, String dateTimeAccuracy, Locale locale) {
        this.displayLocale = locale;
        this.dateTimeAccuracy = dateTimeAccuracy;
        this.val = val;
        try {
            this.dateTime = new SimpleDateFormat(PATTERN_DATE_API, locale).parse(dateTime);
        } catch (Exception e) {
            // should log this
        }
        
        this.timestampFormat = timestampFormat;
    }
    
    public boolean valIsInt() { return val % 1 == 0; }
    
    public double getVal() {
        return val;
    }
    
    /**
     * Gets the value formatted according to the format. If the format is null
     * then this will just return String.valueOf(value).
     * 
     * @param format The format to use, f.ex. "#.00000" See {@link java.text.DecimalFormat}
     * @return The value, formatted as requested.
     */
    public String getVal(String format) {
        if (format == null)
            return String.valueOf(getVal());
        else {
            //NumberFormat nf = 
            DecimalFormat df = (DecimalFormat)NumberFormat.getInstance(new Locale(NUMBER_FORMAT_LOCALE_HIGHCHARTS)); // Highslide needs 3.14, not 3,14
            df.applyPattern(format);
            return df.format(val);
        }
    }
    
    public String getHighLow(String format, String separator) {
        if (this.hasHiLo()) {
            if (separator == null)
                separator = ",";
            
            if (format == null) {
                return String.valueOf(lo) + separator + String.valueOf(hi);
            } else {
                //NumberFormat nf = 
                DecimalFormat df = (DecimalFormat)NumberFormat.getInstance(new Locale(NUMBER_FORMAT_LOCALE_HIGHCHARTS)); // Highslide needs 3.14, not 3,14
                df.applyPattern(format);
                return df.format(lo) + separator + df.format(hi);
            }
        }
        return null;
        
    }
    
    public Date getTimestamp() { return this.dateTime; }
    
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
    
    public String getTimestamp(SimpleDateFormat df) {
        return df.format(dateTime);
    }
    
    public MOSJDataPoint setHi(double hi) {
        this.hi = hi;
        this.hasHi = true;
        return this;
    }
    public MOSJDataPoint setLo(double lo) {
        this.lo = lo;
        this.hasLo = true;
        return this;
    }
    
    public boolean hasHiLo() { return hasHi && hasLo; } /* Should be in time series */
    
    /*
     * Gets the datetime for this data point, formatted according to its own
     * defined accuracy level.
     * 
     * @return The datetime for this data point, formatted according to its own defined accuracy level.
     * @deprecated  Use {@link MOSJDataPoint#getTimestamp(java.text.SimpleDateFormat) } instead (the date format is known by the time series).
     *
    public String formatDate() {
        if (dateTime == null)
            return null;
        
        String f = null;
        
        if (dateTimeAccuracy == null) {
            f = PATTERN_DATE_API;
        } else if (dateTimeAccuracy.indexOf(MOSJTimeSeries.DATE_FORMAT_UNIX_SECOND) > -1) { // Second
            f = "yyyy-MM-dd HH:mm:ss";
        } else if (dateTimeAccuracy.indexOf(MOSJTimeSeries.DATE_FORMAT_UNIX_MINUTE) > -1) { // Minute
            f = "yyyy-MM-dd HH:mm:00";
        } else if (dateTimeAccuracy.indexOf(MOSJTimeSeries.DATE_FORMAT_UNIX_HOUR) > -1) {   // Hour
            f = "yyyy-MM-dd HH:00:00";
        } else if (dateTimeAccuracy.indexOf(MOSJTimeSeries.DATE_FORMAT_UNIX_DATE) > -1) {   // Day of month (date)
            f = "d MMM yyyy";
        } else if (dateTimeAccuracy.indexOf(MOSJTimeSeries.DATE_FORMAT_UNIX_MONTH) > -1) {  // Month
            f = "MMMM yyyy";
        } else if (dateTimeAccuracy.indexOf(MOSJTimeSeries.DATE_FORMAT_UNIX_YEAR) > -1) {   // Year
            f = "yyyy";
        } else {                                                                            // Default
            f = "'" + dateTimeAccuracy + "'"; // No format, use the "format" as a literal, e.g. "2007/08" or "1990–2010"
        }
        return new SimpleDateFormat(f, displayLocale).format(dateTime);
    }
    //*/
    /*public String getDateTimeAccuracy() { return dateTimeAccuracy; }*/
}
