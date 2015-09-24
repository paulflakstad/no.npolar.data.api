package no.npolar.data.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import no.npolar.data.api.mosj.HighchartsChart;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a time series, which is in essence a collection (list) of data 
 * points.
 * <p>
 * Data points are stored internally as {@link TimeSeriesDataPoint} instances. 
 * <p>
 * The time series also contains a <strong>title</strong>, <strong>datetime 
 * accuracy</strong> (for the data points) and various other meta data.
 * <p>
 * Example time series (randomly chosen): 
 * http://apptest.data.npolar.no:9000/monitoring/timeseries/e5b14b14-2143-539a-a03d-cd5c10fb80a3
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class TimeSeries implements APIEntryInterface {
    
    public static final String DATE_FORMAT_UNIX_YEAR = "%Y";
    public static final String DATE_FORMAT_UNIX_MONTH = "%m";
    public static final String DATE_FORMAT_UNIX_DATE = "%d";
    public static final String DATE_FORMAT_UNIX_HOUR = "%H";
    public static final String DATE_FORMAT_UNIX_MINUTE = "%M";
    public static final String DATE_FORMAT_UNIX_SECOND = "%S";
        
    /** Pattern that fits the API timestamps. Used to parse timestamps read from the API. */
    public static final String PATTERN_DATE_API = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // 1871-06-01T12:00:00Z
    
    /** The ID for this time series, as read from the API. */
    private String id = null;
    /** The complete time series data, as read from the API. */
    private JSONObject apiStructure = null;
    /** A string representing the timestamp accuracy, one of DATE_FORMAT_UNIX_XXXXX constants - or, in exceptional cases, the literal value as read from the API (e.g. '2007/2008'). */
    private String dateTimeAccuracy = null;
    /** Preferred locale to use when getting language-specific data. */
    private Locale displayLocale = null;
    /** The unit for the data in this time series. */
    private TimeSeriesDataUnit unit = null;
    /** The data points in this time series. */
    private List<TimeSeriesDataPoint> dataPoints = null;
    
    /** Flag indicating if any data point has a "high" value. */
    protected boolean hasHigh = false;
    /** Flag indicating if any data point has a "low" value. */
    protected boolean hasLow = false;
    /** Flag indicating if any data point has a "max" value. */
    protected boolean hasMax = false;
    /** Flag indicating if any data point has a "min" value. */
    protected boolean hasMin = false;
    
    /** Flag indicating if this time series is a trend line. */
    protected boolean isTrendLine = false;
    /** Flag indicating if this time series contains decimal number values. */
    protected boolean isDecimalValueSeries = false; 
    /** The lowest value in this time series. Set by {@link #initDataPoints()}. */
    protected Double minValue = Double.MAX_VALUE;
    /** The highest value in this time series. Set by {@link #initDataPoints()}. */
    protected Double maxValue = Double.MIN_VALUE;
    
    /** A number indicating this time series' order position in a set of time series. */
    protected int orderIndex = Integer.MIN_VALUE;
    
    protected boolean chartMarkersEnabled = true;
    protected Integer chartMarkersThickness = null;
    protected Integer chartLineThickness = null;
    protected String chartDashStyle = null;
    protected boolean chartConnectNulls = false;
    protected String chartColor = null;
    protected String chartSeriesType = null;
    
    
    /** The format to use when rendering timestamps. */
    private SimpleDateFormat timestampFormat = null;
    
    // Data access keys (used to extract stuff from the JSON object returned by the API)
    public static final String API_KEY_TITLES = "titles";
    public static final String API_KEY_TITLE = "title";
    //public static final String API_KEY_TITLE = "text";
    public static final String API_KEY_TITLE_LABEL = "label";
    public static final String API_KEY_ID = "id";
    //public static final String API_KEY_DATA_POINTS = "points";
    public static final String API_KEY_DATA_POINTS = "data";
    public static final String API_KEY_POINT_VAL = "value";
    public static final String API_KEY_POINT_TIMESTAMP = "datetime";
    public static final String API_KEY_POINT_YEAR = "year";
    public static final String API_KEY_POINT_TIMESTAMP_FORMAT = "datetime_format";
    public static final String API_KEY_POINT_HIGH = "high";
    public static final String API_KEY_POINT_LOW = "low";
    public static final String API_KEY_POINT_MAX = "max";
    public static final String API_KEY_POINT_MIN = "min";
    public static final String API_KEY_UNITS = "units";
    public static final String API_KEY_UNIT = "unit";
    public static final String API_KEY_UNIT_SYMBOL = "symbol";
    public static final String API_KEY_LABELS = "labels";
    public static final String API_KEY_LABELS_LABEL = "label";
    public static final String API_KEY_LABELS_LABEL_FOR = "variable";
    public static final String API_KEY_LABELS_LABEL_LANGUAGE = "lang";
    public static final String API_KEY_VARIABLES = "variables";
    public static final String API_KEY_VARIABLES_NAME = "name";
    public static final String API_KEY_VARIABLES_LABELS = "labels";
    public static final String API_KEY_VARIABLES_LABEL = "label";
    public static final String API_KEY_VARIABLES_UNITS = "units";
    
    public static final Comparator<TimeSeries> ORDER_INDEX_COMPARATOR = new Comparator<TimeSeries>() {
        @Override
        public int compare(TimeSeries o1, TimeSeries o2) {
            return Integer.compare(o1.getOrderIndex(), o2.getOrderIndex());
        }
    };
    
    
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(TimeSeries.class);
    
    //protected ResourceBundle labels = null;
    
    /**
     * Creates a time series from the given JSON object, and localized according
     * to the given locale.
     * 
     * @param o A JSON object that describes the time series.
     * @param displayLocale The preferred locale for language-specific stuff.
     * @throws InstantiationException 
     */
    public TimeSeries(JSONObject o, Locale displayLocale) throws InstantiationException {
        apiStructure = o;
        this.displayLocale = displayLocale;
        //labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        dataPoints = new ArrayList<TimeSeriesDataPoint>();
        try {
            id = apiStructure.getString(API_KEY_ID);
            try { dateTimeAccuracy = apiStructure.getString(API_KEY_POINT_TIMESTAMP_FORMAT); } catch (Exception ee) {}
            // Update settings
            setUnit().setTimestampFormat();
            // Set data points
            initDataPoints();
        } catch (Exception e) {
            throw new InstantiationException("Error attempting to create timeseries instance from JSON object: " + e.getMessage());
        }
    }
    
    /**
     * Sets the unit for this time series.
     * <p>
     * The unit should always be set explicitly, but if that's not the case then
     * an empty string is used as unit.
     * 
     * @return This time series, after having set the unit.
     */
    private TimeSeries setUnit() {
        try {
            String unitShort = "";
            String unitLong = "";
            
            // Value unit/symbol (e.g. "mm") should be allowed missing (= indicates absolute number of something)
            try { unitShort = apiStructure.getJSONObject(API_KEY_UNIT).getString(API_KEY_UNIT_SYMBOL); } catch (Exception e) {}
            // Value description/label (e.g. "Precipitation") should always be present ...
            try { unitLong = getLabelFor(API_KEY_POINT_VAL); } catch (Exception e) { }
            
            // ... so log a warning if it's missing
            if (unitLong == null || unitLong.equals(API_KEY_POINT_VAL)) {
                unitLong = "";
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Value label missing on time series " + this.getId() + ".");
                }
            }
            
            this.unit = new TimeSeriesDataUnit(unitShort, unitLong);
            
            
            
            //JSONObject unitVariableObj = getVariablesFor(API_KEY_POINT_VAL);
            
            //String valueUnit = unitVariableObj.getString(API_KEY_UNITS);
            //String valueLabel = APIUtil.getStringByLocale(unitVariableObj.getJSONArray(API_KEY_VARIABLES_LABELS), API_KEY_VARIABLES_LABEL, displayLocale);
            //System.out.println("Setting unit (locale is " + displayLocale.getLanguage() + "): " + valueUnit + " / " + valueLabel);
            //this.unit = new TimeSeriesDataUnit(valueUnit, valueLabel);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error setting unit for time series '" + this.getId() + "'.", e);
            }
        }
        return this;
        
        // Old version:
        /*
        try {
            this.unit = new TimeSeriesDataUnit(apiStructure.getString(API_KEY_UNIT), APIUtil.getStringByLocale(apiStructure.getJSONArray(API_KEY_UNITS), API_KEY_UNIT, displayLocale));
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error setting unit for time series '" + this.getID() + "'.", e);
            }
        }
        return this;
        */
    }
    
    public TimeSeries setChartLineThickness(int lineThickness) {
        this.chartLineThickness = lineThickness;
        return this;
    }
    /**
     * Sets the chart dash style for this series.
     * 
     * @param dashStyle The dash style, either "longdash" or "shortdot", or null to unset.
     * @return The updated time series.
     * @see {http://www.highcharts.com/docs/chart-concepts/series#12}
     */
    public TimeSeries setChartDashStyle(String dashStyle) {
        if (dashStyle == null || dashStyle.equalsIgnoreCase("null")) // OR statement prevents NPE
            this.chartDashStyle = null;
        this.chartDashStyle = dashStyle;
        return this;
    }
    public TimeSeries setChartMarkersThickness(int markersThickness) {
        this.chartMarkersThickness = markersThickness;
        return this;
    }
    
    public TimeSeries setChartMarkersEnabled(boolean markersEnabled) {
        this.chartMarkersEnabled = markersEnabled;
        return this;
    }
    public TimeSeries setChartConnectNulls(boolean connectNulls) {
        this.chartConnectNulls = connectNulls;
        return this;
    }
    public TimeSeries setChartColor(String color) {
        if (color == null || color.isEmpty() || color.length() < 3) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Attemting to set non-valid chart color '" + color + "' on time series " + this.getId() + " / " + this.getTitle() + ".");
            }
            return this;
        }
        
        color = color.trim();
        
        if (!color.startsWith("#")) {
            color = "#" + color;
        }
        
        this.chartColor = color;
        
        //if (color != null) {
        if (!this.chartColor.substring(1).matches("[0-9a-fA-F]{3}|[0-9a-fA-F]{6}")) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Attemting to set non-valid chart color '" + color + "' on time series " + this.getId() + " / " + this.getTitle() + ".");
            }
            this.chartColor = null; // Illegal value
        }
        //}
        
        return this;
    }
    /**
     * Unsets the chart color (if any).
     * 
     * @return This time series, updated.
     */
    public TimeSeries unsetChartColor() {
        this.chartColor = null;
        return this;
    }
    public TimeSeries setChartSeriesType(String seriesType) {
        this.chartSeriesType = seriesType;
        return this;
    }
    public TimeSeries setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
        return this;
    }
    
    public Integer getChartLineThickness() { return this.chartLineThickness; }
    public String getChartDashStyle() { return this.chartDashStyle; }
    public Integer getChartMarkersThickness() { return this.chartMarkersThickness; }
    public boolean isChartMarkersEnabled() { return this.chartMarkersEnabled; }
    public boolean isChartConnectNulls() { return this.chartConnectNulls; }
    public String getChartColor() { return this.chartColor; }
    public String getChartSeriesType() { return this.chartSeriesType; }
    public Integer getOrderIndex() { return this.orderIndex; }
    
    /**
     * Flags this time series as being a trend line.
     * 
     * @return The updated time series instance.
     */
    public TimeSeries flagAsTrendLine() {
        this.isTrendLine = true;
        this.setChartColor("#c00");
        this.setChartConnectNulls(true);
        this.setChartMarkersEnabled(false);
        this.setChartDashStyle(HighchartsChart.DASH_STYLE_SHORT);
        return this;
    }
    
    /**
     * Determines whether or not this time series is flagged as being a trend 
     * line.
     * 
     * @return True if this time series is flagged as being a trend line, false if not.
     */
    public boolean isTrendLine() {
        return this.isTrendLine;
    }
    
    private JSONObject getVariablesFor(String variableName) {
        if (!apiStructure.has(API_KEY_VARIABLES) || variableName == null)
            return null;
        try {
            JSONArray variablesArr = apiStructure.getJSONArray(API_KEY_VARIABLES);
            for (int i = 0; i < variablesArr.length(); i++) {
                JSONObject variablesObj = variablesArr.getJSONObject(i);
                if (variablesObj.getString(API_KEY_VARIABLES_NAME).equals(variableName)) {
                    return variablesObj;
                }
            }
        } catch (Exception e) {
            // WTF
        }
        return null;
    }
    
    private String getLabelFor(String variableName) {
        if (!apiStructure.has(API_KEY_LABELS) || variableName == null)
            return variableName;
        try {
            JSONArray labelsArr = apiStructure.getJSONArray(API_KEY_LABELS);
            for (int i = 0; i < labelsArr.length(); i++) {
                JSONObject labelObj = labelsArr.getJSONObject(i);
                if (labelObj.getString(API_KEY_LABELS_LABEL_FOR).equals(variableName)) {
                    if (APIUtil.matchLanguage(labelObj.getString(API_KEY_LABELS_LABEL_LANGUAGE), displayLocale))
                        return labelObj.getString(API_KEY_LABELS_LABEL);
                }
            }
        } catch (Exception e) {
            // WTF
        }
        return variableName;
    }
    
    /**
     * Gets the title for this time series, in the language defined by the given
     * locale.
     * 
     * @param loc Should identify the preferred language.
     * @return The title for this time series, preferably in the language identified by the given locale
     */
    public String getTitle(Locale loc) {
        
        try {
            return APIUtil.getStringByLocale(apiStructure.getJSONArray(API_KEY_TITLES), API_KEY_TITLE, loc);
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error reading title of time series '" + this.getId() + "'.", e);
            }
            return "[No title]";
        }
    }
    
    /**
     * Gets the label for this time series, in the language defined by the given
     * locale. 
     * <p>
     * The label is the "short version" of the title, e.g. if the full title is
     * "PCB-level in polar bear blood", the label might be "PCB".
     * <p>The labels are typically used to differentiate and identify different 
     * time series that appear within the same collection. (For example in a 
     * table or in a chart.)
     * <p>If no label is defined, this method will return the full title, by 
     * calling ({@link #getTitle(java.util.Locale)}).
     * 
     * @param loc Should identify the preferred language.
     * @return The label for this time series, preferably in the language identified by the given locale.
     */
    public String getLabel(Locale loc) {
        try {
            //System.out.println("TS label is " + APIUtil.getStringByLocale(apiStructure.getJSONArray(API_KEY_TITLES), API_KEY_TITLE_LABEL, loc));
            return APIUtil.getStringByLocale(apiStructure.getJSONArray(API_KEY_TITLES), API_KEY_TITLE_LABEL, loc);
        } catch (Exception e) {
            return getTitle(loc);
        }
    }
    /**
     * Gets the label for this time series, in the preferred language.
     * 
     * @see #getLabel(java.util.Locale) 
     * @return The label for this time series, in the preferred language.
     */
    public String getLabel() {
        return getLabel(this.displayLocale);
    }
    
    /**
     * Gets the title for this time series, in the preferred language.
     * 
     * @return The title for this time series, in the preferred language
     */
    @Override
    public String getTitle() {
        try {
            return this.getTitle(this.displayLocale);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Failed to read title using the configured locale (" + this.displayLocale.getLanguage() + ").", e);
            }
            return null;
        }
    }
    /**
     * Gets the group name.
     * <p>For time series, this is the standardized unit. (Because that's what 
     * time series are typically "grouped" by - think a chart with two y-axes.)
     * 
     * @see TimeSeriesDataUnit#getShortForm()
     * @return The group name.
     */
    @Override
    public String getGroupName() {
        return this.unit.getShortForm();
    }
    
    /**
     * Gets the URL for this time series, within the context of the given 
     * service.
     * <p>
     * The service must be of type {@link MOSJService}
     * @param service The API service. Must be of type {@link MOSJService}.
     * @return The URL for this time series, or: where it resides within the given service.
     */
    @Override
    public String getURL(APIServiceInterface service) {
        if (!(service instanceof MOSJService)) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Cannot retrieve MOSJ parameter URL using a service not of type " + MOSJService.class.getName() + ".");
            }
        }
        
        return ((MOSJService)service).getTimeSeriesBaseURL() + this.getId();
    }
    
    /**
     * @return The unit used in this time series.
     */
    public TimeSeriesDataUnit getUnit() {
        return this.unit;
    }
    
    /**
     * @return The long (verbose) form of the unit used in this time series.
     * @see TimeSeriesDataUnit#getLongForm() 
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
    /**
     * Initializes the data points. 
     * <p>
     * This method is called by the constructor.
     * 
     * @return The updated instance.
     * @throws JSONException 
     */
    private TimeSeries initDataPoints() throws JSONException {
        JSONArray dataPointsJSONArr = apiStructure.getJSONArray(API_KEY_DATA_POINTS);
        if (dataPointsJSONArr.length() > 0) {
            dataPoints = new ArrayList<TimeSeriesDataPoint>();
            for (int i = 0; i < dataPointsJSONArr.length(); i++) {
                try {
                    JSONObject dataPointJSON = dataPointsJSONArr.getJSONObject(i);
                    Double value = null;
                    //String timestampFormat = null;
                    String timestamp = null;
                    //int year = Integer.MIN_VALUE;
                    try { value = dataPointJSON.getDouble(API_KEY_POINT_VAL); } catch (Exception ee) {  }
                    // The timestamp is either a full timestamp (string) or just the year (int)
                    try { 
                        timestamp = dataPointJSON.getString(API_KEY_POINT_TIMESTAMP); 
                    } catch (Exception ee) {
                        try {
                            // Probably temporary
                            int year = dataPointJSON.getInt(API_KEY_POINT_YEAR); 
                            timestamp = "" + year + "-01-01T12:00:00Z";
                            this.dateTimeAccuracy = DATE_FORMAT_UNIX_YEAR;
                            this.setTimestampFormat(); // Because the datetime accuracy changed
                        } catch (Exception eee) {
                        } 
                    }
                    
                    // Data point had no value -> disregard, continue to next
                    if (value == null) {
                        continue;
                    }
                    
                    updateExtremeValues(value);
                    
                    //try { timestampFormat = apiStructure.getString(API_KEY_POINT_TIMESTAMP_FORMAT); } catch (Exception ee) {}
                    TimeSeriesDataPoint dp = new TimeSeriesDataPoint(
                                        value, 
                                        timestamp, 
                                        this.getTimestampFormat(),
                                        this.getDateTimeAccuracy(),  // ToDo: This time series should keep track of the format, not the data point
                                        this.displayLocale);
                    
                    // ToDo: This time series should keep track of whether high/low/max/min values exist, not the data point
                    // (is this already handled by the isErrorBarSeries() method???)
                    if (dataPointJSON.has(API_KEY_POINT_HIGH)) {
                        dp.setHigh(dataPointJSON.getDouble(API_KEY_POINT_HIGH));
                        this.hasHigh = true;
                        updateExtremeValues(dp.get(TimeSeriesDataPoint.VALUE_HIGH));
                    } else if (this.hasHigh) {
                        // Log this
                    }
                    if (dataPointJSON.has(API_KEY_POINT_LOW)) {
                        dp.setLow(dataPointJSON.getDouble(API_KEY_POINT_LOW));
                        this.hasLow = true;
                        updateExtremeValues(dp.get(TimeSeriesDataPoint.VALUE_LOW));
                    } else if (this.hasLow) {
                        // Log this
                    }
                    if (dataPointJSON.has(API_KEY_POINT_MAX)) {
                        dp.setMax(dataPointJSON.getDouble(API_KEY_POINT_MAX));
                        this.hasMax = true;
                        updateExtremeValues(dp.get(TimeSeriesDataPoint.VALUE_MAX));
                    } else if (this.hasMax) {
                        // Log this
                    }
                    if (dataPointJSON.has(API_KEY_POINT_MIN)) {
                        dp.setMin(dataPointJSON.getDouble(API_KEY_POINT_MIN));
                        this.hasMin = true;
                        updateExtremeValues(dp.get(TimeSeriesDataPoint.VALUE_MIN));
                    } else if (this.hasMin) {
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
     * Updates the minimum and maximum values of this time series, if the given 
     * value exceeds the current extreme values.
     * <p>
     * Also, updates the "decimal values" and "positive values only" flags.
     * <p>
     * Invoked by {@link #initDataPoints()}.
     * 
     * @param value The value to compare to the current extreme values.
     */
    private void updateExtremeValues(double value) {
        if (value > this.maxValue) {
            this.maxValue = value;
        }
        if (value < this.minValue) {
            this.minValue = value;
        }
        
        if (!isDecimalValueSeries) {
            // If the given value is not an integer, update the flag
            if (value % 1 != 0) {
                this.isDecimalValueSeries = true;
            } 
        }
    }
    /**
     * Determines whether or not this series contains no negative values.
     * 
     * @return True if this series contains no negative values, false if not.
     */
    public boolean isPositiveValuesOnlySeries() {
        return this.minValue >= 0;
    }
    /**
     * Gets the maximum value in this time series.
     * 
     * @return The maximum value in this time series.
     */
    public double getMaxValue() {
        return this.maxValue;
    }
    /**
     * Gets the minimum value in this time series.
     * 
     * @return The minimum value in this time series.
     */
    public double getMinValue() {
        return this.minValue;
    }
    /**
     * Determines whether or not this series contains only integer values.
     * 
     * @return True if this series contains only integer values, false if not.
     */
    public boolean isIntegerValuesOnlySeries() { return !this.isDecimalValueSeries; }
    /**
     * @return The list of data points in this time series.
     */
    public List<TimeSeriesDataPoint> getDataPoints() /*throws JSONException*/ { return dataPoints; }
    
    /**
     * Gets the data point for a specific time marker. 
     * <p>
     * If no data exists for the given time marker, null is returned.
     * 
     * @param timeMarker The time marker to get the data point for.
     * @return The data point for the given time marker, or null if there is no data for that time marker.
     */
    public TimeSeriesDataPoint getDataPointForTimeMarker(String timeMarker) {
        Iterator<TimeSeriesDataPoint> iDataPoints = getDataPoints().iterator();
        while (iDataPoints.hasNext()) {
            TimeSeriesDataPoint dataPoint = iDataPoints.next();
            if (dataPoint.getTimestamp(timestampFormat).equals(timeMarker)) {
                return dataPoint;
            }
        }
        return null;
    }
    
    public String getValueAPIKey(int valueIdentifier) {
        switch (valueIdentifier) {
            case TimeSeriesDataPoint.VALUE_MAIN:
                return API_KEY_POINT_VAL;
            case TimeSeriesDataPoint.VALUE_LOW:
                return API_KEY_POINT_LOW;
            case TimeSeriesDataPoint.VALUE_HIGH:
                return API_KEY_POINT_HIGH;
            case TimeSeriesDataPoint.VALUE_MIN:
                return API_KEY_POINT_MIN;
            case TimeSeriesDataPoint.VALUE_MAX:
                return API_KEY_POINT_MAX;
            default:
                return API_KEY_POINT_VAL;
        }
    }
    
    public String getDataPointsAsTableRow(TimeSeriesCollection tsc) {
        String s = "";
        
        if (this.isTrendLine())
            return s;
        
        try {
            ArrayList<String> rows = new ArrayList<String>();
            for (int i = 0; i < getValuesPerDataPoint(); i++) {
                rows.add("");
            }
            
            //s += "<!-- Total rows: " + rows.size() + ", this.getUnit().getShortForm()=" + this.getUnit().getShortForm() + " -->\n";

            Iterator<String> iTimeMarker = tsc.getTimeMarkerIterator();

            while (iTimeMarker.hasNext()) {
                String timeMarker = iTimeMarker.next();
                
                //s += "<!-- getting data for " + timeMarker + " ... -->\n";
                
                TimeSeriesDataPoint dataPoint = getDataPointForTimeMarker(timeMarker);
                
                for (int i = 0; i < getValuesPerDataPoint(); i++) { // Must use getValuesPerDataPoint because dataPoint might be null
                    if (rows.get(i).isEmpty()) {
                        //s += "<!-- label appendix is " + getLabelFor(getValueAPIKey(i)) + " -->\n";
                        String rowStart = "<tr><th scope=\"row\"><span class=\"tr-time-series-title\">" + this.getLabel(displayLocale) + (i > 0 ? " (".concat(getLabelFor(getValueAPIKey(i))).concat(")") : "") + "</span></th>";
                        rowStart += "<td><span class=\"tr-time-series-unit\">" + this.getUnit().getShortForm() + "</span></td>";
                        rows.set(i, rowStart);
                    }
                    rows.set(i, rows.get(i) + "<td>" + (dataPoint == null ? "" : dataPoint.get(i, "#.####")) + "</td>" + (iTimeMarker.hasNext() ? "" : "</tr>\n"));
                }
                
            }
            if (!rows.isEmpty()) {
                Iterator<String> iRows = rows.iterator();
                while (iRows.hasNext()) {
                    s += iRows.next();
                }
            } else {
                s += "<!-- No data points in time series " + this.getId() + " -->\n";
            }
        } catch (Exception e) {
            s += "<!-- Error creating table row(s) for time series " + this.getId() + ": " + e.getMessage() + " -->\n"; 
        }
        return s;
    }
    
    
    public String getDataPointsAsCSVRow(TimeSeriesCollection tsc) {
        String s = "";
        
        if (this.isTrendLine())
            return s;
        
        try {
            ArrayList<String> rows = new ArrayList<String>();
            for (int i = 0; i < getValuesPerDataPoint(); i++) {
                rows.add("");
            }
            
            //s += "<!-- Total rows: " + rows.size() + ", this.getUnit().getShortForm()=" + this.getUnit().getShortForm() + " -->\n";

            Iterator<String> iTimeMarker = tsc.getTimeMarkerIterator();

            while (iTimeMarker.hasNext()) {
                String timeMarker = iTimeMarker.next();
                
                //s += "<!-- getting data for " + timeMarker + " ... -->\n";
                
                TimeSeriesDataPoint dataPoint = getDataPointForTimeMarker(timeMarker);
                
                for (int i = 0; i < getValuesPerDataPoint(); i++) { // Must use getValuesPerDataPoint because dataPoint might be null
                    if (rows.get(i).isEmpty()) {
                        //s += "<!-- label appendix is " + getLabelFor(getValueAPIKey(i)) + " -->\n";
                        String rowStart = APIUtil.escapeCSV(this.getLabel(displayLocale) + (i > 0 ? " (".concat(getLabelFor(getValueAPIKey(i))).concat(")") : "")) + ";";
                        rowStart += APIUtil.escapeCSV(this.getUnit().getShortForm()) + ";";
                        rows.set(i, rowStart);
                    }
                    rows.set(i, rows.get(i) + (dataPoint == null ? "" : dataPoint.get(i, "#.####")) + (iTimeMarker.hasNext() ? ";" : "\n"));
                }
                
            }
            if (!rows.isEmpty()) {
                Iterator<String> iRows = rows.iterator();
                while (iRows.hasNext()) {
                    s += iRows.next();
                }
            } else {
                //s += "<!-- No data points in time series " + this.getId() + " -->\n";
            }
        } catch (Exception e) {
            //s += "<!-- Error creating CSV row(s) for time series " + this.getId() + ": " + e.getMessage() + " -->\n"; 
        }
        return s;
    }
    
    /**
     * Use {@link HighchartsChart#getHtmlTable() }
     *
    public String getAsTable() throws JSONException {
        String s = "<table id=\"" + this.getID() + "-data\" class=\"wcag-off-screen\">\n";
        s += "<caption>" + this.getTitle(displayLocale) + "</caption>\n";
        
        //s += "<tr><th></th><th>" + getUnitVerbose(displayLocale) + "</th></tr>\n";
        s += "<tr><th></th><th>" + getUnitVerbose() + "</th></tr>\n";
        
        JSONArray dps = apiStructure.getJSONArray("points");
        if (dps.length() > 0) {
            for (int i = 0; i < dps.length(); i++) {
                TimeSeriesDataPoint dp = new TimeSeriesDataPoint(
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
    */
    /**@return The ID of this time series. */
    @Override
    public String getId() { return id; }
    /**@return The JSON object that reflects how this time series is represented by the service. */
    public JSONObject getAPIStructure() { return apiStructure; }
    /**@return The timestamp accuracy level. */
    public String getDateTimeAccuracy() { return dateTimeAccuracy; }
    /**@return Flag indicating whether or not this series has error bars. */
    public boolean isErrorBarSeries() { return this.hasHigh && this.hasLow && (!this.hasMax && !this.hasMin); }
    
    /**
     * Gets the number of values contained in each data point of this series.
     * <p>
     * If high/low values exist, 3 is returned. Otherwise 1 is returned.
     * 
     * @return The number of values contained in this data point.
     */
    public int getValuesPerDataPoint() { 
        if (this.hasMax && this.hasMin)
            return 5;
        else if (this.hasHigh && this.hasLow)
            return 3;
        else
            return 1;
    }
    
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
     * 
     * @return The updated instance.
     */
    private TimeSeries setTimestampFormat() {
        String f = null;
        
        if (dateTimeAccuracy == null) {
            f = PATTERN_DATE_API;
        } else if (dateTimeAccuracy.indexOf(DATE_FORMAT_UNIX_SECOND) > -1) { // Second
            f = "yyyy-MM-dd HH:mm:ss";
        } else if (dateTimeAccuracy.indexOf(DATE_FORMAT_UNIX_MINUTE) > -1) { // Minute
            f = "yyyy-MM-dd HH:mm:00";
        } else if (dateTimeAccuracy.indexOf(DATE_FORMAT_UNIX_HOUR) > -1) {   // Hour
            f = "yyyy-MM-dd HH:00:00";
        } else if (dateTimeAccuracy.indexOf(DATE_FORMAT_UNIX_DATE) > -1) {   // Day of month (date)
            f = "d MMM yyyy";
        } else if (dateTimeAccuracy.indexOf(DATE_FORMAT_UNIX_MONTH) > -1) {  // Month
            f = "MMMM yyyy";
        } else if (dateTimeAccuracy.indexOf(DATE_FORMAT_UNIX_YEAR) > -1) {   // Year
            f = "yyyy";
        } else {                                                                            // Default
            f = "'" + dateTimeAccuracy + "'"; // No format, use the "format" as a literal, e.g. "2007/08" or "1990–2010"
        }
        this.timestampFormat = new SimpleDateFormat(f, displayLocale);
        return this;
    }
}
