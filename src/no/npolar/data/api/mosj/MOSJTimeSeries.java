/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package no.npolar.data.api.mosj;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import no.npolar.data.api.MOSJDataSet;
import no.npolar.data.api.MOSJDataUnit;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A MOSJTimeSeries instance represents a time series, which is in essence a 
 * collection (list) of data points ({@link MOSJDataPoint} instances). The time 
 * series also contains a title, datetime accuracy (for the data points) and 
 * other meta data.
 * 
 * Example time series (randomly chosen): 
 * http://apptest.data.npolar.no:9000/monitoring/timeseries/e5b14b14-2143-539a-a03d-cd5c10fb80a3
 * 
 * 
 * @deprecated Use {@link no.npolar.data.api.TimeSeries} instead.
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class MOSJTimeSeries {
    
    public static final String DATE_FORMAT_UNIX_YEAR = "%Y";
    public static final String DATE_FORMAT_UNIX_MONTH = "%m";
    public static final String DATE_FORMAT_UNIX_DATE = "%d";
    public static final String DATE_FORMAT_UNIX_HOUR = "%H";
    public static final String DATE_FORMAT_UNIX_MINUTE = "%M";
    public static final String DATE_FORMAT_UNIX_SECOND = "%S";
    
    public static final String PATTERN_DATE_API = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // 1871-06-01T12:00:00Z
    
    private String id = null;
    private JSONObject apiStructure = null;
    private String dateTimeAccuracy = null;
    private Locale displayLocale = null;
    private MOSJDataUnit unit = null;
    
    private List<MOSJDataPoint> dataPoints = null;
    
    protected boolean hasHi = false;
    protected boolean hasLo = false;
    
    private SimpleDateFormat timestampFormat = null;
    
    public static final String API_KEY_TITLES = "titles";
    public static final String API_KEY_TITLE = "title";
    public static final String API_KEY_ID = "id";
    //public static final String API_KEY_DATA_POINTS = "points";
    public static final String API_KEY_DATA_POINTS = "data";
    public static final String API_KEY_POINT_VAL = "value";
    public static final String API_KEY_POINT_TIMESTAMP = "datetime";
    public static final String API_KEY_POINT_TIMESTAMP_FORMAT = "datetime_format";
    public static final String API_KEY_POINT_HIGH = "high";
    public static final String API_KEY_POINT_LOW = "low";
    public static final String API_KEY_UNITS = "units";
    public static final String API_KEY_UNIT = "unit";
    
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(MOSJTimeSeries.class);
    
    public MOSJTimeSeries(JSONObject o, Locale displayLocale) throws InstantiationException {
        apiStructure = o;
        this.displayLocale = displayLocale;
        dataPoints = new ArrayList<MOSJDataPoint>();
        try {
            id = apiStructure.getString(API_KEY_ID);
            try { dateTimeAccuracy = apiStructure.getString(API_KEY_POINT_TIMESTAMP_FORMAT); } catch (Exception ee) {}
            // Update settings
            setUnit().setTimestampFormat();
            // Set data points
            initDataPoints();
        } catch (Exception e) {
            throw new InstantiationException("Error attempting to create MOSJ timeseries instance from JSON object: " + e.getMessage());
        }
    }
    /**
     * @return This time series, after having set the unit.
     */
    private MOSJTimeSeries setUnit() {
        try {
            this.unit = new MOSJDataUnit(apiStructure.getString(API_KEY_UNIT), APIUtil.getStringByLocale(apiStructure.getJSONArray(API_KEY_UNITS), API_KEY_UNIT, displayLocale));
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error setting unit for time series '" + this.getID() + "'.", e);
            }
        }
        return this;
    }
    
    /**
     * @param loc Should identify the preferred language.
     * @return The title for this time series, preferably in the language identified by the given locale
     * @throws JSONException If the title cannot be read from the backing JSON fetched from the API.
     */
    public String getTitle(Locale loc) throws JSONException {
        return APIUtil.getStringByLocale(apiStructure.getJSONArray(API_KEY_TITLES), API_KEY_TITLE, loc);
    }
    
    /**
     * @return The unit used in this time series.
     */
    public MOSJDataUnit getUnit() {
        return this.unit;
    }
    
    /**
     * @return The long (verbose) form of the unit used in this time series.
     * @see MOSJDataUnit#getLongForm() 
     */
    public String getUnitVerbose() {
        return this.unit.getLongForm();
    }
    /*
    public String getUnit() throws JSONException {
        return apiStructure.getString(API_KEY_UNIT);
    }
    public String getUnitVerbose() throws JSONException {
        return getUnitVerbose(displayLocale);
    }
    public String getUnitVerbose(Locale loc) throws JSONException {
        return APIUtil.getStringByLocale(apiStructure.getJSONArray(API_KEY_UNITS), API_KEY_UNIT, loc);
    }
    */
    private MOSJTimeSeries initDataPoints() throws JSONException {
        JSONArray dps = apiStructure.getJSONArray(API_KEY_DATA_POINTS);
        if (dps.length() > 0) {
            dataPoints = new ArrayList<MOSJDataPoint>();
            for (int i = 0; i < dps.length(); i++) {
                try {
                    JSONObject dpObj = dps.getJSONObject(i);
                    Double value = null;
                    //String timestampFormat = null;
                    String timestamp = null;
                    try { value = dpObj.getDouble(API_KEY_POINT_VAL); } catch (Exception ee) {  }
                    try { timestamp = dpObj.getString(API_KEY_POINT_TIMESTAMP); } catch (Exception ee) {}
                    //try { timestampFormat = apiStructure.getString(API_KEY_POINT_TIMESTAMP_FORMAT); } catch (Exception ee) {}
                    MOSJDataPoint dp = new MOSJDataPoint(
                                        value, 
                                        timestamp, 
                                        this.getTimestampFormat(),
                                        this.getDateTimeAccuracy(),  // ToDo: This time series should keep track of the format, not the data point
                                        this.displayLocale);
                    
                    // ToDo: This time series should keep track of whether high/low/max/min values exist, not the data point
                    // (is this already handled by the isErrorBarSeries() method???)
                    if (dpObj.has(API_KEY_POINT_HIGH)) {
                        dp.setHi(dpObj.getDouble(API_KEY_POINT_HIGH));
                        this.hasHi = true;
                    } else if (this.hasHi) {
                        // Log this
                    }
                    if (dpObj.has(API_KEY_POINT_LOW)) {
                        dp.setLo(dpObj.getDouble(API_KEY_POINT_LOW));
                        this.hasLo = true;
                    } else if (this.hasLo) {
                        // Log this
                    }
                    dataPoints.add(dp);
                } catch (Exception e) {
                    // ToDo: ?
                }
            }
        }
        return this;
    }
    /**
     * @return The list of data points in this time series.
     */
    public List<MOSJDataPoint> getDataPoints() /*throws JSONException*/ { return dataPoints; }
    /**
     * @deprecated use {@link MOSJParameter#getAsTable() } or {@link MOSJDataSet#getTableRows() }
     */
    public String getAsTable() throws JSONException {
        String s = "<table id=\"" + this.getID() + "-data\" class=\"wcag-off-screen\">\n";
        s += "<caption>" + this.getTitle(displayLocale) + "</caption>\n";
        
        //s += "<tr><th></th><th>" + getUnitVerbose(displayLocale) + "</th></tr>\n";
        s += "<tr><th></th><th>" + getUnitVerbose() + "</th></tr>\n";
        
        JSONArray dps = apiStructure.getJSONArray("points");
        if (dps.length() > 0) {
            for (int i = 0; i < dps.length(); i++) {
                MOSJDataPoint dp = new MOSJDataPoint(
                                        dps.getJSONObject(i).getDouble("value"), 
                                        dps.getJSONObject(i).getString("datetime"), 
                                        this.getTimestampFormat(),
                                        this.getDateTimeAccuracy(),  //the timeseries should keep track of the format, not the data point
                                        displayLocale);
                
                s += "<tr><td>" + dp.getTimestamp(this.getTimestampFormat()) + "</td><td>" + dp.getVal() + "</td></tr>\n";
            }
        }
        
        s += "</table>";
        return s;
    }
    
    public String getID() { return id; }
    public JSONObject getAPIStructure() { return apiStructure; }
    public String getDateTimeAccuracy() { return dateTimeAccuracy; }
    public boolean isErrorBarSeries() { return this.hasHi && this.hasLo; }
    
    /**
     * Gets the timestamp format for this time series, according to its own
     * defined accuracy level.
     * 
     * @return The timestamp format for this time series, formatted according to its own defined accuracy level.
     */
    public SimpleDateFormat getTimestampFormat() { return this.timestampFormat; }
    
    
    /**
     * Sets the timestamp format for this time series, according to its own
     * defined accuracy level.
     */
    private MOSJTimeSeries setTimestampFormat() {
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
        this.timestampFormat = new SimpleDateFormat(f, displayLocale);
        return this;
    }
}
