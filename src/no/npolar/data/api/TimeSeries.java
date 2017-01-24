package no.npolar.data.api;

//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
//import java.util.Arrays;
//import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;
//import java.util.TreeMap;
//import java.util.TreeSet;
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
 * accuracy</strong> (for the data points), and various other meta data.
 * <p>
 * Example time series (randomly chosen): 
 * http://apptest.data.npolar.no:9000/monitoring/timeseries/e5b14b14-2143-539a-a03d-cd5c10fb80a3
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class TimeSeries extends APIEntry implements Comparable<TimeSeries> /*APIEntryInterface*/ {
    // Date format parts
    //public static final String DATE_FORMAT_UNIX_YEAR = "%Y";
    //public static final String DATE_FORMAT_UNIX_MONTH = "%m";
    //public static final String DATE_FORMAT_UNIX_DATE = "%d";
    //public static final String DATE_FORMAT_UNIX_HOUR = "%H";
    //public static final String DATE_FORMAT_UNIX_MINUTE = "%M";
    //public static final String DATE_FORMAT_UNIX_SECOND = "%S";
        
    /** Pattern that fits the API timestamps. Used to parse timestamps read from the API. */
    public static final String PATTERN_DATE_API = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // 1871-06-01T12:00:00Z
    ///** Pattern that matches "ISO date" dates. Used by the API. */
    //public static final String PATTERN_ISODATE_API = "yyyy-MM-dd";
    
    //** The ID for this time series, as read from the API. */
    //private String id = null;
    //** The complete time series data, as read from the API. */
    //private JSONObject apiStructure = null;
    ///** A string representing the timestamp accuracy, one of DATE_FORMAT_UNIX_XXXXX constants - or, in exceptional cases, the literal value as read from the API (e.g. '2007/2008'). */
    //private String dateTimeAccuracy = null;
    ///** Preferred locale to use when getting language-specific data. */
    //private Locale displayLocale = null;
    /** The unit for the data in this time series. */
    private TimeSeriesDataUnit unit = null;
    ///** The data points in this time series. */
    //private List<TimeSeriesDataPoint> dataPoints = null;
    //private TreeSet<TimeSeriesDataPoint> dataPoints = null;
    
    /** The timestamps in this series. */
    private List<TimeSeriesTimestamp> timestamps = null;
    //private TreeSet<TimeSeriesTimestamp> timestamps = null;
        
    /** The data points in this series. */
    private List<TimeSeriesDataPoint> dataPoints = null;
    //private TreeMap<TimeSeriesTimestamp, TimeSeriesDataPoint> timeSeriesData = null;
    //private Map<TimeSeriesTimestamp, TimeSeriesDataPoint> timeSeriesData = null;
    
    /** The type of the timestamps in this series. */
    private int timestampsType = TimeSeriesTimestamp.TYPE_UNKNOWN;
    
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

    /** Chart setting: Enabled/disabled markers. */
    protected boolean chartMarkersEnabled = true;
    /** Chart setting: Markers thickness. */
    protected Integer chartMarkersThickness = null;
    /** Chart setting: Line thickness. */
    protected Integer chartLineThickness = null;
    /** Chart setting: Dash style. */
    protected String chartDashStyle = null;
    /** Chart setting: Connect nulls or not? */
    protected boolean chartConnectNulls = false;
    /** Chart setting: Markers enabled? */
    protected String chartColor = null;
    /** Chart setting: Series type. */
    protected String chartSeriesType = null;
    
    /** The number of actual data points in this series. */
    private int numDataPoints = 0;
    
    
    //** The format to use when rendering timestamps. */
    //private SimpleDateFormat timestampFormat = null;
    
    /**
     * Keys (names) for entry values (fields).
     */
    public static class Key extends APIEntry.Key {
        /** API key: Titles */
        public static final String TITLES = "titles";
        /** API key: Title. */
        public static final String TITLE = "title";
        //public static final String TITLE = "text";
        /** API key: Title -> Label. */
        public static final String TITLE_LABEL = "label";
        /** API key: ID. */
        public static final String ID = "id";
        //public static final String DATA_POINTS = "points";
        /** API key: Data points. */
        public static final String DATA_POINTS = "data";
        /** API key: Data point -> Timestamp. */
        public static final String POINT_WHEN = "when";
        /** API key: Data point -> Main value. */
        public static final String POINT_VAL = "value";
        /** API key: Data point -> High value. */
        public static final String POINT_HIGH = "high";
        /** API key: Data point -> Low value. */
        public static final String POINT_LOW = "low";
        /** API key: Data point -> Maximum value. */
        public static final String POINT_MAX = "max";
        /** API key: Data point -> Minimum value. */
        public static final String POINT_MIN = "min";
        /** API key: Units. */
        public static final String UNITS = "units";
        /** API key: Unit. */
        public static final String UNIT = "unit";
        /** API key: Unit -> Symbol. */
        public static final String UNIT_SYMBOL = "symbol";
        /** API key: Labels. */
        public static final String LABELS = "labels";
        /** API key: Labels -> Label. */
        public static final String LABELS_LABEL = "label";
        /** API key: Labels -> Label for */
        public static final String LABELS_LABEL_FOR = "variable";
        /** API key: Labels -> Label language. */
        public static final String LABELS_LABEL_LANGUAGE = "lang";
        /** API key: Variables. */
        public static final String VARIABLES = "variables";
        /** API key: Variables -> Name. */
        public static final String VARIABLES_NAME = "name";
        /** API key: Variables -> Labels. */
        public static final String VARIABLES_LABELS = "labels";
        /** API key: Variables -> Label. */
        public static final String VARIABLES_LABEL = "label";
        /** API key: Variables -> Units. */
        public static final String VARIABLES_UNITS = "units";
        /** API key: Array of localized names (generic). */
        public static final String NAMES_LOCALIZED = "names";
        /** API key: A localized value. */
        public static final String VALUE_LOCALIZED = "@value";
        /** API key: A localized value's language code. */
        public static final String VALUE_LOCALIZED_LANG = "@language";
        /** API key: Authors (array of objects). */
        public static final String AUTHORS = "authors";
        /** API key: Authors -> Names (array of localized names). */
        public static final String AUTHORS_NAMES = NAMES_LOCALIZED;
        /** API key: Authors -> Names -> Name (localized). */
        public static final String AUTHORS_NAMES_NAME = VALUE_LOCALIZED;
        /** API key: Authors -> Names -> Language of the localized name. */
        public static final String AUTHORS_NAMES_LANGUAGE = VALUE_LOCALIZED_LANG;
    }
    
    // Data access keys (used to extract stuff from the JSON object returned by the API)
    /** API key: Titles */
    //public static final String API_KEY_TITLES = "titles";
    /** API key: Title. */
    //public static final String API_KEY_TITLE = "title";
    //public static final String API_KEY_TITLE = "text";
    /** API key: Title -> Label. */
    //public static final String API_KEY_TITLE_LABEL = "label";
    /**
     * API key: ID.
     * @deprecated Use {@link Key#ID} instead.
     */
    public static final String API_KEY_ID = Key.ID;
    /**
     * API key: Data points.
     * @deprecated Use {@link Key#DATA_POINTS} instead.
     */
    public static final String API_KEY_DATA_POINTS = Key.DATA_POINTS;
    /**
     * API key: Data point -> Timestamp.
     * @deprecated Use {@link Key#POINT_WHEN} instead.
     */
    public static final String API_KEY_POINT_WHEN = Key.POINT_WHEN;
    /**
     * API key: Data point -> Timestamp.
     * @deprecated Use {@link Key#POINT_WHEN} instead.
     */
    public static final String API_KEY_POINT_TIMESTAMP = "datetime";
    /**
     * API key: Data point -> timestamp format.
     * @deprecated Not used by the API anymore. No replacement.
     */
    public static final String API_KEY_POINT_TIMESTAMP_FORMAT = "datetime_format";
    /**
     * API key: Data point -> Timestamp.
     * @deprecated Use {@link Key#POINT_WHEN} instead.
     */
    public static final String API_KEY_POINT_DATE = "date";
    /**
     * API key: Data point -> Year stamp.
     * @deprecated Use {@link Key#POINT_WHEN} instead.
     */
    public static final String API_KEY_POINT_YEAR = "year";
    /**
     * API key: Data point -> Main value.
     * @deprecated Use {@link Key#POINT_VAL} instead.
     */
    public static final String API_KEY_POINT_VAL = Key.POINT_VAL;
    /**
     * API key: Data point -> High value.
     * @deprecated Use {@link Key#POINT_HIGH} instead.
     */
    public static final String API_KEY_POINT_HIGH = Key.POINT_HIGH;
    /**
     * API key: Data point -> Low value.
     * @deprecated Use {@link Key#POINT_LOW} instead.
     */
    public static final String API_KEY_POINT_LOW = Key.POINT_LOW;
    /**
     * API key: Data point -> Maximum value.
     * @deprecated Use {@link Key#POINT_MAX} instead.
     */
    public static final String API_KEY_POINT_MAX = Key.POINT_MAX;
    /**
     * API key: Data point -> Minimum value.
     * @deprecated Use {@link Key#POINT_MIN} instead.
     */
    public static final String API_KEY_POINT_MIN = Key.POINT_MIN;
    /**
     * API key: Units.
     * @deprecated Use {@link Key#UNITS} instead.
     */
    public static final String API_KEY_UNITS = Key.UNITS;
    /**
     * API key: Unit.
     * @deprecated Use {@link Key#UNIT} instead.
     */
    public static final String API_KEY_UNIT = Key.UNIT;
    /**
     * API key: Unit -> Symbol.
     * @deprecated Use {@link Key#UNIT_SYMBOL} instead.
     */
    public static final String API_KEY_UNIT_SYMBOL = Key.UNIT_SYMBOL;
    /**
     * API key: Labels.
     * @deprecated Use {@link Key#LABELS} instead.
     */
    public static final String API_KEY_LABELS = Key.LABELS;
    /**
     * API key: Labels -> Label.
     * @deprecated Use {@link Key#LABELS_LABEL} instead.
     */
    public static final String API_KEY_LABELS_LABEL = Key.LABELS_LABEL;
    /**
     * API key: Labels -> Label for
     * @deprecated Use {@link Key#LABELS_LABEL_FOR} instead.
     */
    public static final String API_KEY_LABELS_LABEL_FOR = Key.LABELS_LABEL_FOR;
    /**
     * API key: Labels -> Label language.
     * @deprecated Use {@link Key#LABELS_LABEL_LANGUAGE} instead.
     */
    public static final String API_KEY_LABELS_LABEL_LANGUAGE = Key.LABELS_LABEL_LANGUAGE;
    /**
     * API key: Variables.
     * @deprecated Use {@link Key#VARIABLES} instead.
     */
    public static final String API_KEY_VARIABLES = Key.VARIABLES;
    /**
     * API key: Variables -> Name.
     * @deprecated Use {@link Key#VARIABLES_NAME} instead.
     */
    public static final String API_KEY_VARIABLES_NAME = Key.VARIABLES_NAME;
    /**
     * API key: Variables -> Labels.
     * @deprecated Use {@link Key#VARIABLES_LABELS} instead.
     */
    public static final String API_KEY_VARIABLES_LABELS = Key.VARIABLES_LABELS;
    /**
     * API key: Variables -> Label.
     * @deprecated Use {@link Key#VARIABLES_LABEL} instead. 
     */    
    public static final String API_KEY_VARIABLES_LABEL = Key.VARIABLES_LABEL;
    /** 
     * API key: Variables -> Units.
     * @deprecated Use {@link Key#VARIABLES_UNITS} instead.
     */
    public static final String API_KEY_VARIABLES_UNITS = Key.VARIABLES_UNITS;
    
    public static final String DEFAULT_AUTHOR_NAME = "[Unknown author]".toUpperCase();
    
    /**
     * Holds "authors" - needed for citation.
     */
    private List<String> authors = new ArrayList(1);
    
    /** Mapping of partial keys (like "low") to their complete counterparts, used to identify default labels.  */
    public static final Map<String, String> DEFAULT_LABEL_KEYS = new HashMap<String, String>()
        {{
            put(Key.POINT_HIGH, Labels.TIME_SERIES_POINT_VALUE_HIGH_0);
            put(Key.POINT_LOW, Labels.TIME_SERIES_POINT_VALUE_LOW_0);
            put(Key.POINT_MAX, Labels.TIME_SERIES_POINT_VALUE_MAX_0);
            put(Key.POINT_MIN, Labels.TIME_SERIES_POINT_VALUE_MIN_0); 
        }};
    
    /** Compares time series by their order index, intended for sorting by order index ascending. */
    public static final Comparator<TimeSeries> ORDER_INDEX_COMPARATOR = new Comparator<TimeSeries>() {
        @Override
        public int compare(TimeSeries o1, TimeSeries o2) {
            return Integer.compare(o1.getOrderIndex(), o2.getOrderIndex());
        }
    };
    
    /** Translations. */
    protected ResourceBundle labels = null;
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(TimeSeries.class);
    
    /**
     * Creates a time series from the given JSON object, and localized according
     * to the given locale.
     * 
     * @param o A JSON object that describes the time series.
     * @param displayLocale The preferred locale for language-specific stuff.
     * @throws InstantiationException 
     */
    public TimeSeries(JSONObject o, Locale displayLocale) throws InstantiationException {
        super(o, displayLocale);
        //apiStructure = o;
        //this.displayLocale = displayLocale;
        //labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        //dataPoints = new ArrayList<TimeSeriesDataPoint>();
        //dataPoints = new TreeSet<TimeSeriesDataPoint>(TimeSeriesDataPoint.COMPARE_TIMESTAMP);
        if (this.id == null) {
            throw new InstantiationException("Error attempting to create timeseries instance from JSON object: ID was null.");
        }
        try {
            //id = apiStructure.getString(Key.ID);
            //try { dateTimeAccuracy = apiStructure.getString(Key.POINT_TIMESTAMP_FORMAT); } catch (Exception ee) {}
            // Update settings
            setUnit();
            //setTimestampFormat();
            // Set data points
            initDataPoints();
            setAuthors();
        } catch (Exception e) {
            throw new InstantiationException("Error attempting to create timeseries instance from JSON object: " + e.getMessage());
        }
    }
    
    /**
     * A copy constructor that is not really a copy constructor. 
     * <p>
     * The "copy" is created using the backing JSON, fetched from the given 
     * original time series. This means that <strong>the copy might differ from 
     * the original</strong> – in particular, this may occur if the original has 
     * been manually updated after instantiation.
     * <p>
     * In other words, this constructor will re-create a "clean" version of the 
     * given original time series.
     * 
     * @param other The time series holding the JSON and locale on which to base the "copy" on.
     * @throws InstantiationException 
     */
    public TimeSeries(TimeSeries other) throws InstantiationException {
        this(other.getJSON(), other.getDisplayLocale());
    }
    
    /**
     * Sets the unit for this time series.
     * <p>
     * The unit should always be set explicitly, but if that's not the case then
     * an empty string is used as unit.
     * 
     * @return This time series, updated.
     */
    private TimeSeries setUnit() {
        try {
            String unitShort = "";
            String unitLong = "";
            
            // Value unit/symbol (e.g. "mm") should be allowed missing (= indicates absolute number of something)
            try { unitShort = o.getJSONObject(Key.UNIT).getString(Key.UNIT_SYMBOL); } catch (Exception e) {}
            // Value description/label (e.g. "Precipitation") should always be present ...
            try { unitLong = getLabelFor(Key.POINT_VAL); } catch (Exception e) { }
                        
            // ... so log a warning if it's missing
            if (unitLong == null || unitLong.equals(Key.POINT_VAL)) {
                unitLong = "";
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Value label missing on time series " + this.getId() + ".");
                }
            }
            
            this.unit = new TimeSeriesDataUnit(unitShort, unitLong);
            
            
            
            //JSONObject unitVariableObj = getVariablesFor(Key.POINT_VAL);
            
            //String valueUnit = unitVariableObj.getString(Key.UNITS);
            //String valueLabel = APIUtil.getStringByLocale(unitVariableObj.getJSONArray(Key.VARIABLES_LABELS), Key.VARIABLES_LABEL, displayLocale);
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
            this.unit = new TimeSeriesDataUnit(apiStructure.getString(Key.UNIT), APIUtil.getStringByLocale(apiStructure.getJSONArray(Key.UNITS), Key.UNIT, displayLocale));
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error setting unit for time series '" + this.getID() + "'.", e);
            }
        }
        return this;
        */
    }
    
    /**
     * Sets the author(s) of this time series.
     * <p>
     * Author names are extracted from the JSON fetched from the Data Centre. 
     * They are localized to currently configured preferred locale, if possible.
     */
    private void setAuthors() {
        try {
            if (o.has(Key.AUTHORS)) {
                JSONArray authArr = o.getJSONArray(Key.AUTHORS);
                for (int i = 0; i < authArr.length(); i++) {
                    try {
                        JSONObject authObj = authArr.getJSONObject(i);
                        String authName = getLocalizedValue(
                                authObj.getJSONArray(Key.AUTHORS_NAMES)
                        );
                        if (authName == null) {
                            authName = DEFAULT_AUTHOR_NAME;
                        }
                        addAuthor(authName);
                    } catch (Exception e) {
                        // Author object in array, but something went wrong
                        addAuthor(DEFAULT_AUTHOR_NAME);
                    }
                }
            }
        } catch (Exception e) {
           // Freak error, log it
           if (LOG.isErrorEnabled()) {
               LOG.error("Error extracting author(s) for time series with ID " + getId() + ".", e);
           }
        }
    }
    
    /**
     * Tries to get a localized value that matches the currently set preferred 
     * locale.
     * <p>
     * If there is no direct match, the first encountered value is returned. If 
     * there is nothing in the array, <code>null</code> is returned.
     * 
     * @param localizedNames The array of localized values and their language codes.
     * @return The localized value that matches the current locale, or the closest alternative.
     */
    private String getLocalizedValue(JSONArray localizedValues) {
        String authName = null;
        try {
            // First, set a fallback value (the first name in the array)
            authName = localizedValues.getJSONObject(0).getString(Key.VALUE_LOCALIZED);
            for (int i = 0; i < localizedValues.length(); i++) {
                String langCode = localizedValues.getJSONObject(i).getString(Key.VALUE_LOCALIZED_LANG);
                if (APIUtil.matchLanguage(langCode, displayLocale)) {
                    authName = localizedValues.getJSONObject(i).getString(Key.VALUE_LOCALIZED);
                    break;
                }
            }
        } catch (Exception e) {}
        return authName;
    }
    
    /**
     * Adds the given author to the list of authors, if necessary.
     * <p>
     * If the author already exists in the list, nothing is done.
     * 
     * @param author The author to add.
     */
    private void addAuthor(String author) {
        if (!authors.contains(author)) {
            authors.add(author);
        }
    }
    
    /**
     * Gets the authors, comma-separated.
     * 
     * @return The authors, comma-separated.
     */
    public String getAuthorsString() {
        String s = "";
        
        for (int i = 0; i < authors.size(); i++) {
            // Alt 1: Comma
            if (i > 0) {
                s += ", ";
            }
            /*
            // Alt. 2: Ampersand before last author, comma elsewhere
            if (i > 0) {
                if (i+1 == authors.size()) {
                    // current author is last
                    s += " & ";
                } else {
                    // at least 1 more author after this one
                    s += ", ";
                }
            }
            //*/
            s += authors.get(i);
        }
        return s;
    }
    
    /**
     * Gets the list of authors. 
     * <p>
     * If possible, each author's name will be localized according to the 
     * currently set preferred language. (An "author" of a time series is 
     * typically the name of the institution that owns the data.)
     * 
     * @return The list of authors.
     */
    public List<String> getAuthors() {
        return authors;
    }
    
    /**
     * Sets the chart line thickness.
     * 
     * @param lineThickness The line thickness, in pixels. 0 (zero) indicates "no line".
     * @return This time series, updated.
     * @see HighchartsChart#OVERRIDE_KEY_LINE_THICKNESS
     * @see {http://api.highcharts.com/highcharts#plotOptions.series.lineWidth}
     */
    public TimeSeries setChartLineThickness(int lineThickness) {
        this.chartLineThickness = lineThickness;
        return this;
    }
    
    /**
     * Sets the chart dash style for this series.
     * 
     * @param dashStyle The dash style, either "longdash" or "shortdot", or null to unset.
     * @return This time series, updated.
     * @see HighchartsChart#OVERRIDE_KEY_DASH_STYLE
     * @see {http://www.highcharts.com/docs/chart-concepts/series#12}
     */
    public TimeSeries setChartDashStyle(String dashStyle) {
        if (dashStyle == null || dashStyle.equalsIgnoreCase("null")) // OR statement prevents NPE
            this.chartDashStyle = null;
        this.chartDashStyle = dashStyle;
        return this;
    }
    
    /**
     * Sets the chart marker thickness for this series.
     * 
     * @param markersThickness The marker thickness, in pixels.
     * @return This time series, updated.
     * @see HighchartsChart#OVERRIDE_KEY_MARKER_THICKNESS
     * @see {http://api.highcharts.com/highcharts#plotOptions.series.marker.radius}
     */
    public TimeSeries setChartMarkersThickness(int markersThickness) {
        this.chartMarkersThickness = markersThickness;
        return this;
    }
    
    /**
     * Enables or disables chart markers for this series.
     * 
     * @param markersEnabled Provide true to enable markers, or false to disable.
     * @return This time series, updated.
     * @see HighchartsChart#OVERRIDE_KEY_HIDE_MARKERS_BOOL
     * @see {http://api.highcharts.com/highcharts#plotOptions.series.marker.enabled}
     */
    public TimeSeries setChartMarkersEnabled(boolean markersEnabled) {
        this.chartMarkersEnabled = markersEnabled;
        return this;
    }
    
    /**
     * Enables or disables "connected null values" - that is, sets whether or 
     * not to draw series with null values as continuous (connected) or 
     * discontinuous (non-connected).
     * 
     * @param connectNulls Provide true to connect nulls, or false not to.
     * @return This time series, updated.
     * @see HighchartsChart#OVERRIDE_KEY_CONNECT_NULLS
     * @see {http://api.highcharts.com/highcharts#plotOptions.series.connectNulls}
     */
    public TimeSeries setChartConnectNulls(boolean connectNulls) {
        this.chartConnectNulls = connectNulls;
        return this;
    }
    
    /**    
     * Sets the color for this time series.
     * 
     * @param color The color, as a CSS-style hex value, with or without a leading '#'.
     * @return The updated time series.
     * @see HighchartsChart#OVERRIDE_KEY_COLOR
     * @see {http://api.highcharts.com/highcharts#plotOptions.series.color}
     */
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
    
    /**
     * Sets the chart series type for this time series.
     * 
     * @param seriesType The series type.
     * @return This time series, updated.
     * @see HighchartsChart#OVERRIDE_KEY_TYPE_STRING
     * @see {http://api.highcharts.com/highcharts#series.type}
     */
    public TimeSeries setChartSeriesType(String seriesType) {
        this.chartSeriesType = seriesType;
        return this;
    }
    
    /**
     * Sets the order index for this time series.
     * 
     * @param orderIndex The desired order index.
     * @return This time series, updated.
     * @see HighchartsChart#OVERRIDE_KEY_ORDER_INDEX
     */
    public TimeSeries setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
        return this;
    }
    
    /**
     * @return The chart line thickness for this time series.
     */
    public Integer getChartLineThickness() { return this.chartLineThickness; }
    
    /**
     * @return The chart dash style for this time series.
     */
    public String getChartDashStyle() { return this.chartDashStyle; }
    
    /**
     * @return The chart markers thickness for this time series.
     */
    public Integer getChartMarkersThickness() { return this.chartMarkersThickness; }
    
    /**
     * @return The "show markers" chart setting for this time series.
     */
    public boolean isChartMarkersEnabled() { return this.chartMarkersEnabled; }
    
    /**
     * @return The "connect nulls" chart setting for this time series.
     */
    public boolean isChartConnectNulls() { return this.chartConnectNulls; }
    
    /**
     * @return The chart series color for this time series.
     */
    public String getChartColor() { return this.chartColor; }
    
    /**
     * @return The chart series type for this time series.
     */
    public String getChartSeriesType() { return this.chartSeriesType; }
    
    /**
     * @return The order index for this time series in the chart.
     */
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
    
    /*private JSONObject getVariablesFor(String variableName) {
        if (!apiStructure.has(Key.VARIABLES) || variableName == null)
            return null;
        try {
            JSONArray variablesArr = apiStructure.getJSONArray(Key.VARIABLES);
            for (int i = 0; i < variablesArr.length(); i++) {
                JSONObject variablesObj = variablesArr.getJSONObject(i);
                if (variablesObj.getString(Key.VARIABLES_NAME).equals(variableName)) {
                    return variablesObj;
                }
            }
        } catch (Exception e) {
            // WTF
        }
        return null;
    }*/
    
    /**
     * Gets a localized label for a given variable.
     * <p>
     * All relevant labels are defined in the data centre, and included in the 
     * JSON feed for this time series.
     * 
     * @param variableName The name of the variable to get the label for.
     * @return the label for the given variable, or itself, if no label could be resolved.
     */
    private String getLabelFor(String variableName) {
        if (variableName == null || !(o.has(Key.LABELS) || DEFAULT_LABEL_KEYS.containsKey(variableName))) {
            return variableName;
        }
        try {
            JSONArray labelsArr = o.getJSONArray(Key.LABELS);
            for (int i = 0; i < labelsArr.length(); i++) {
                JSONObject labelObj = labelsArr.getJSONObject(i);
                if (labelObj.getString(Key.LABELS_LABEL_FOR).equals(variableName)) {
                    if (APIUtil.matchLanguage(labelObj.getString(Key.LABELS_LABEL_LANGUAGE), displayLocale))
                        return labelObj.getString(Key.LABELS_LABEL);
                }
            }
        } catch (Exception e) {
            // No label for that variable, or no labels array at all
        }
        
        // No label explicitly defined - fallback to default..?
        if (DEFAULT_LABEL_KEYS.containsKey(variableName)) {
            if (this.labels == null) {
                this.labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
            }
            try {
                return labels.getString(DEFAULT_LABEL_KEYS.get(variableName));
            } catch (Exception e) {}
        }
        
        // No label found anywhere
        return variableName;
    }
    
    /**
     * Gets the title for this time series, in the language defined by the given
     * locale.
     * 
     * @param loc Should identify the preferred language.
     * @return The title for this time series, preferably in the language identified by the given locale.
     */
    public String getTitle(Locale loc) {
        
        try {
            return APIUtil.getStringByLocale(o.getJSONArray(Key.TITLES), Key.TITLE, loc);
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
            //System.out.println("TS label is " + APIUtil.getStringByLocale(apiStructure.getJSONArray(Key.TITLES), Key.TITLE_LABEL, loc));
            return APIUtil.getStringByLocale(o.getJSONArray(Key.TITLES), Key.TITLE_LABEL, loc);
        } catch (Exception e) {
            return getTitle(loc);
        }
    }
    
    /**
     * @return The label for this time series, in the preferred language.
     * @see #getLabel(java.util.Locale) 
     */
    public String getLabel() {
        return getLabel(this.displayLocale);
    }
    
    /**
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
     * <p>
     * For time series, this is the standardized unit. (Because that's what 
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
     * The service must be of type {@link MOSJService}.
     * 
     * @param service The API service. Must be of type {@link MOSJService}.
     * @return The URL for this time series, or: where it resides within the given service.
     */
    @Override
    public String getURL(APIServiceInterface service) {
        if (!(service instanceof MOSJService)) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Cannot retrieve MOSJ time series URL using a service not of type " + MOSJService.class.getName() + ".");
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
        return apiStructure.getString(Key.UNIT);
    }
    public String getUnitVerbose() throws JSONException {
        return getUnitVerbose(displayLocale);
    }
    public String getUnitVerbose(Locale loc) throws JSONException {
        return APIUtil.getStringByLocale(apiStructure.getJSONArray(Key.UNITS), Key.UNIT, loc);
    }
    */
    
    /**
     * Initializes the data points, based on the contents of the backing JSON's 
     * {@link Key#DATA_POINTS} array.
     * <p>
     * This method is called by the constructor.
     * 
     * @return The updated instance.
     * @throws JSONException 
     */
    private TimeSeries initDataPoints() throws JSONException {
        JSONArray dataPointsJSONArr = o.getJSONArray(Key.DATA_POINTS);
        //if (dataPointsJSONArr.length() > 0) {
            //dataPoints = new ArrayList<TimeSeriesDataPoint>();
            //dataPoints = new TreeSet<TimeSeriesDataPoint>(TimeSeriesDataPoint.COMPARE_TIMESTAMP);
            
            numDataPoints = dataPointsJSONArr.length();
            
            dataPoints = new ArrayList<TimeSeriesDataPoint>(numDataPoints); // new
            
            timestamps = new ArrayList<TimeSeriesTimestamp>(numDataPoints);
            //timestamps = new TreeSet<TimeSeriesTimestamp>(TimeSeriesTimestamp.CHRONOLOGICAL);
            
            //timeSeriesData = new TreeMap<TimeSeriesTimestamp, TimeSeriesDataPoint>(TimeSeriesTimestamp.CHRONOLOGICAL);
            
            for (int i = 0; i < numDataPoints; i++) {
                try {
                    JSONObject dataPointJSON = dataPointsJSONArr.getJSONObject(i);
                    Double value = null;
                    //String timestamp = null;
                    //int year = Integer.MIN_VALUE;
                    try { value = dataPointJSON.getDouble(Key.POINT_VAL); } catch (Exception ee) {  }
                    
                    // Data point had no value -> disregard, continue to next
                    if (value == null) {
                        continue;
                    }
                    
                    TimeSeriesTimestamp timestamp = null;
                    // The timestamp is either 
                    //  - a full timestamp (string)
                    //  - a date timestamp (string)
                    //  - just the year (int)
                    try {
                        if (dataPointJSON.has(Key.POINT_WHEN)) {
                            //System.out.println("Found time marker " + Key.POINT_WHEN);
                            if (this.timestampsType == TimeSeriesTimestamp.TYPE_UNKNOWN || this.timestampsType == TimeSeriesTimestamp.TYPE_LITERAL) {
                                // Timestamp type unknown: Rely on sniffing the type
                                //System.out.println("Time marker type unknown, sniffing...");
                                timestamp = new TimeSeriesTimestamp(dataPointJSON.getString(Key.POINT_WHEN));
                            } else {
                                // Timestamp type known: Create it specifically
                                timestamp = new TimeSeriesTimestamp(dataPointJSON.getString(Key.POINT_WHEN), this.timestampsType);
                            }
                        } else {
                            throw new InstantiationException("Missing required field '" + Key.POINT_WHEN + "'.");
                        }
                        
                        /*else if (dataPointJSON.has(Key.POINT_YEAR)) {
                            timestamp = new TimeSeriesTimestamp(dataPointJSON.getInt(Key.POINT_YEAR));
                        }*/
                        
                        /*if (dataPointJSON.has(Key.POINT_TIMESTAMP)) {
                            timestamp = new TimeSeriesTimestamp(dataPointJSON.getString(Key.POINT_TIMESTAMP));
                        } else if (dataPointJSON.has(Key.POINT_DATE)) {
                            //System.out.println("Found " + Key.POINT_DATE);
                            if (this.timestampsType == TimeSeriesTimestamp.TYPE_UNKNOWN) {
                                // Timestamp type unknown: Rely on sniffing the type
                                timestamp = new TimeSeriesTimestamp(dataPointJSON.getString(Key.POINT_DATE));
                            } else {
                                // Timestamp type known: Create it specifically
                                timestamp = new TimeSeriesTimestamp(dataPointJSON.getString(Key.POINT_DATE), this.timestampsType);
                            }
                            //this.dateTimeAccuracy = DATE_FORMAT_UNIX_DATE;
                            //this.setTimestampFormat(); // Because the datetime accuracy changed
                            //System.out.println("Crated timestamp: " + timestamp);
                        } else {
                            timestamp = new TimeSeriesTimestamp(dataPointJSON.getInt(Key.POINT_YEAR));
                        }*/
                    } catch (Exception e) {
                        LOG.error("Cannot create timestamp for data point in time series " + this.getId() + ": " + e.getMessage());
                    }
                    
                    if (this.timestampsType == TimeSeriesTimestamp.TYPE_UNKNOWN || this.timestampsType == TimeSeriesTimestamp.TYPE_LITERAL) {
                        // Set timestamps type (common for this series)
                        this.timestampsType = timestamp.getType();
                    } else {
                        if (this.timestampsType != timestamp.getType() // Means this series contains more than 1 type of timestamps
                                && !(this.timestampsType == TimeSeriesTimestamp.TYPE_LITERAL || timestamp.getType() == TimeSeriesTimestamp.TYPE_LITERAL)) {
                            LOG.error("Mixing timestamp is not recommended, but was found in time series " + this.getId() + ".");
                        }
                    }
                    /*try { 
                        if (dataPointJSON.has(Key.POINT_TIMESTAMP)) {
                            timestamp = dataPointJSON.getString(Key.POINT_TIMESTAMP);
                        } else if (dataPointJSON.has(Key.POINT_DATE)) {
                            //System.out.println("Found " + Key.POINT_DATE);
                            timestamp = dataPointJSON.getString(Key.POINT_DATE) + "T12:00:00Z";
                            this.dateTimeAccuracy = DATE_FORMAT_UNIX_DATE;
                            this.setTimestampFormat(); // Because the datetime accuracy changed
                            //System.out.println("Crated timestamp: " + timestamp);
                        }
                    } catch (Exception ee) {
                        try {
                            // Probably temporary
                            int year = dataPointJSON.getInt(Key.POINT_YEAR); 
                            timestamp = "" + year + "-01-01T12:00:00Z";
                            this.dateTimeAccuracy = DATE_FORMAT_UNIX_YEAR;
                            this.setTimestampFormat(); // Because the datetime accuracy changed
                        } catch (Exception eee) {
                        } 
                    }
                    */
                    
                    //try { timestampFormat = apiStructure.getString(Key.POINT_TIMESTAMP_FORMAT); } catch (Exception ee) {}
                    TimeSeriesDataPoint dp = new TimeSeriesDataPoint(
                                        value, 
                                        timestamp, 
                                        //this.getTimestampFormat(),
                                        //this.getDateTimeAccuracy(),  // ToDo: This time series should keep track of the format, not the data point
                                        this.displayLocale);
                    
                    //addDataPoint(dp);
                    //*
                    //updateExtremeValues(value);
                    // Add the timestamp
                    //timestamps.add(timestamp);
                    
                    // ToDo: This time series should keep track of whether high/low/max/min values exist, not the data point
                    // (is this already handled by the isErrorBarSeries() method???)
                    if (dataPointJSON.has(Key.POINT_HIGH)) {
                        dp.setHigh(dataPointJSON.getDouble(Key.POINT_HIGH));
                        //this.hasHigh = true;
                        //updateExtremeValues(dp.get(TimeSeriesDataPoint.VALUE_HIGH));
                    } else if (this.hasHigh) {
                        // Log this
                    }
                    if (dataPointJSON.has(Key.POINT_LOW)) {
                        dp.setLow(dataPointJSON.getDouble(Key.POINT_LOW));
                        //this.hasLow = true;
                        //updateExtremeValues(dp.get(TimeSeriesDataPoint.VALUE_LOW));
                    } else if (this.hasLow) {
                        // Log this
                    }
                    if (dataPointJSON.has(Key.POINT_MAX)) {
                        dp.setMax(dataPointJSON.getDouble(Key.POINT_MAX));
                        //this.hasMax = true;
                        //updateExtremeValues(dp.get(TimeSeriesDataPoint.VALUE_MAX));
                    } else if (this.hasMax) {
                        // Log this
                    }
                    if (dataPointJSON.has(Key.POINT_MIN)) {
                        dp.setMin(dataPointJSON.getDouble(Key.POINT_MIN));
                        //this.hasMin = true;
                        //updateExtremeValues(dp.get(TimeSeriesDataPoint.VALUE_MIN));
                    } else if (this.hasMin) {
                        // Log this
                    }
                    
                    //dataPoints.add(dp);
                    //updateExtremeValues(dp);
                    // Store the data point, mapped to its timestamp
                    //timeSeriesData.put(timestamp, dp);
                    //*/
                    addDataPoint(dp);
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Undefined error adding data point for time series " + this.getId(), e);
                    }
                } 
                //*
                if (this.isSingleValueSeries() && this.timestampsType == TimeSeriesTimestamp.TYPE_DATE && numDataPoints >= 500) {
                    // This is a long, high-resolution series that needs performance optimization.
                    // No table will be output (link to table instead), and we'll load the JSON data async'ly into the chart.
                    //System.out.println("Breaking out.");
                    //break; // Break the for-loop: 
                }else {
                    //System.out.println("NOT breaking out.");
                }//*/
            } // for-loop
        //}
        return this;
    }
    
    /**
     * Adds a data point to this series.
     * 
     * @param dp The data point to add.
     * @return This instance, updated.
     */
    private synchronized TimeSeries addDataPoint(TimeSeriesDataPoint dp) {
        // Update flags to "true" if necessary - but never to false
        if (dp.hasHigh()) {
            this.hasHigh = true;
        }
        if (dp.hasLow()) {
            this.hasLow = true;
        }
        if (dp.hasMax()) {
            this.hasMax = true;
        }
        if (dp.hasMin()) {
            this.hasMin = true;
        }
        
        // Add the data point
        dataPoints.add(dp);
        // Add the timestamp
        timestamps.add(dp.getTimestamp());
        // Update extreme values of this series
        updateExtremeValues(dp);
        return this;
    }
    
    /**
     * Adds a data point to this series, and optionally sorts the list of all
     * data points (chronologically) afterwards.
     * 
     * @param dp The data point to add.
     * @param sortAfter If <code>true</code>, the data points list is sorted after insertion.
     * @return This instance, updated.
     */
    public synchronized TimeSeries addDataPoint(TimeSeriesDataPoint dp, boolean sortAfter) {
        addDataPoint(dp);
        if (sortAfter) {
            Collections.sort(dataPoints, TimeSeriesDataPoint.COMPARE_TIMESTAMP);
        }
        return this;
    }
    
    /**
     * Updates the extreme values of this series, based on the given data point.
     * 
     * @see #updateExtremeValues(double) 
     * @param dp The data point holding the value(s) that are potentially new extreme values.
     */
    public synchronized void updateExtremeValues(TimeSeriesDataPoint dp) {
        updateExtremeValues(dp.getAbsMax());
        updateExtremeValues(dp.getAbsMin());
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
     * Returns the number of data points in this series.
     * <p>
     * Normally, this is the size of the {@link Key#DATA_POINTS} array of 
     * the backing JSON.
     * 
     * @return The number of data points in this series.
     */
    public int size() {
        return numDataPoints;
    }
    
    /**
     * Determines whether or not this series contains a single value per 
     * timestamp.
     * 
     * @return True if this series contains a single value per timestamp, false if not.
     */
    public boolean isSingleValueSeries() {
        return !(this.hasHigh || this.hasLow || this.hasMax || this.hasMin);
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
     * @return The maximum value in this time series.
     */
    public double getMaxValue() {
        return this.maxValue;
    }
    
    /**
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
    public List<TimeSeriesTimestamp> getTimestamps() {
    //public TreeSet<TimeSeriesTimestamp> getTimestamps() {
    //public TreeSet<TimeSeriesDataPoint> getDataPoints() /*throws JSONException*/ { return dataPoints; }
    //public List<TimeSeriesDataPoint> getDataPoints() /*throws JSONException*/ { return dataPoints; }
        return timestamps;
    }
    
    /**
     * Gets the data point for a specific time marker. 
     * <p>
     * If no data exists for the given time marker, null is returned.
     * 
     * @param timeMarker The time marker to get the data point for.
     * @return The data point for the given time marker, or null if there is no data for that time marker.
     */
    public TimeSeriesDataPoint getDataPointForTimeMarker(TimeSeriesTimestamp timeMarker) {
        //return timeSeriesData.get(timeMarker);
        //*
        Iterator<TimeSeriesDataPoint> iDataPoints = getDataPoints().iterator();
        while (iDataPoints.hasNext()) {
            TimeSeriesDataPoint dataPoint = iDataPoints.next();
            if (dataPoint.getTimestamp().equals(timeMarker)) {
                return dataPoint;
            }
        }
        return null;
        //*/
    }
    
    /**
     * Gets all the data points in this time series.
     * 
     * @return All the data points in this time series, or an empty list if none.
     */
    public List<TimeSeriesDataPoint> getDataPoints() {
        return dataPoints;
    }
    
    //public TimeSeriesDataPoint removeDataPointForTimeMarker(TimeSeriesTimestamp timeMarker) {
    //    return timeSeriesData.remove(timeMarker);
    //}
    
    /**
     * Gets the API key for a value field, based on the given value identifier.
     * <p>
     * If there is no specific match for the value identifier, the default 
     * {@link Key#POINT_VAL} is returned.
     * 
     * @param valueIdentifier The value identifier, one of the VALUE_XXX constants of {@link TimeSeriesDataPoint}.
     * @return The API key that corresponds to the given value identifier.
     */
    public String getValueAPIKey(int valueIdentifier) {
        switch (valueIdentifier) {
            case TimeSeriesDataPoint.VALUE_MAIN:
                return Key.POINT_VAL;
            case TimeSeriesDataPoint.VALUE_LOW:
                return Key.POINT_LOW;
            case TimeSeriesDataPoint.VALUE_HIGH:
                return Key.POINT_HIGH;
            case TimeSeriesDataPoint.VALUE_MIN:
                return Key.POINT_MIN;
            case TimeSeriesDataPoint.VALUE_MAX:
                return Key.POINT_MAX;
            default:
                return Key.POINT_VAL;
        }
    }
    
    /**
     * Gets the data points in this time series as an HTML table row string.
     * <p>
     * The time series collection is required to know which time markers are 
     * used in the table.
     * 
     * @param tsc The time series collection comprised in the table.
     * @return This time series, as an HTML table row string.
     */
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

            Iterator<TimeSeriesTimestamp> iTimeMarker = tsc.getTimeMarkerIterator();

            while (iTimeMarker.hasNext()) {
                TimeSeriesTimestamp timeMarker = iTimeMarker.next();
                
                //s += "<!-- getting data for " + timeMarker + " ... -->\n";
                
                TimeSeriesDataPoint dataPoint = getDataPointForTimeMarker(timeMarker);
                
                for (int i = 0; i < getValuesPerDataPoint(); i++) { // Must use getValuesPerDataPoint because dataPoint might be null
                    if (rows.get(i).isEmpty()) {
                        //s += "<!-- label appendix is " + getLabelFor(getValueAPIKey(i)) + " -->\n";
                        String rowStart = "<tr>"
                                + "<th scope=\"row\">"
                                + "<span class=\"tr-time-series-title\">" 
                                    + getLabel() 
                                    + (i > 0 ? " (".concat(getLabelFor(getValueAPIKey(i))).concat(")") : "") 
                                + "</span>"
                                + "</th>";
                        rowStart += "<td>"
                                + "<span class=\"tr-time-series-unit\">" 
                                    + getUnit().getShortForm() 
                                + "</span>"
                                + "</td>";
                        rowStart += "<td>"
                                + "<span class=\"tr-time-series-data-supplier\">" 
                                    + getAuthorsString()
                                + "</span>"
                                + "</td>";
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
    
    /**
     * Gets the data points in this time series as a CSV row string.
     * <p>
     * The time series collection is required to know which time markers are 
     * used in the table.
     * 
     * @param tsc The time series collection comprised in the table.
     * @return This time series, as a CSV row string.
     */
    public String getDataPointsAsCSVRow(TimeSeriesCollection tsc) {
        String s = "";
        
        if (this.isTrendLine())
            return s;
        
        try {
            int numRows = getValuesPerDataPoint();
            
            ArrayList<String> rows = new ArrayList<String>(numRows);
            for (int i = 0; i < numRows; i++) {
                rows.add("");
            }
            
            //s += "<!-- Total rows: " + rows.size() + ", this.getUnit().getShortForm()=" + this.getUnit().getShortForm() + " -->\n";

            // New, tweaked routine: 
            // Instead of calling getDataPointForTimeMarker(...), we traverse
            // the time markers AND the data points in this time series,
            // and spit out empty values for any time marker that is not used in 
            // this series.
            // This tweak should mean an improvement from O(n^2) to O(n)
            // (The biggest collection tested went from ~2300ms to ~1800ms)
            // 
            // NOTE: This routine does require that all the time markers in this 
            // series are already sorted in chronologial order! (They should 
            // always be.)
            
            Iterator<TimeSeriesTimestamp> iTimeMarker = tsc.getTimeMarkerIterator();
            List<TimeSeriesDataPoint> dps = getDataPoints();
            
            // Get a reference to the first data point in this series
            int dataPointIndex = 0;
            TimeSeriesDataPoint dataPoint = dps.get(dataPointIndex);
            
            // Use the time markers in the COLLECTION as the "outer steps" 
            // (This particular series' time markers may be a subset of those)
            while (iTimeMarker.hasNext()) {
                TimeSeriesTimestamp timeMarker = iTimeMarker.next();
                boolean timeMarkerMatchesDataPoint = false;
                
                // Process the 1-5 values in the data point. Each value will
                // have 1 row.
                // For example:
                // Value (high);mg;2;3;5;4
                //        Value;mg;1;2;4;3
                //  Value (low);mg;0;1;3;2
                for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
                    String rowContent = rows.get(rowIndex);
                    // If this row is empty, we add the first "cells", that is: 
                    //  - the short-form label
                    //  - the unit
                    if (rowContent.isEmpty()) {
                        //s += "<!-- label appendix is " + getLabelFor(getValueAPIKey(i)) + " -->\n";
                        rowContent += APIUtil.escapeCSV(
                                this.getLabel(displayLocale) 
                                + (rowIndex > 0 
                                        ? 
                                        " (".concat(getLabelFor(getValueAPIKey(rowIndex))).concat(")") 
                                        : 
                                        "")
                        ) + ";";
                        rowContent += APIUtil.escapeCSV(this.getUnit().getShortForm()) + ";";
                        rowContent += APIUtil.escapeCSV(this.getAuthorsString()) + ";";
                    }
                    
                    if (dataPoint == null || dataPoint.getTimestamp() == null) {
                        rowContent += "";
                    } else {
                        if (dataPoint.getTimestamp().equals(timeMarker)) {
                            rowContent += dataPoint.get(rowIndex, "#.####");
                            timeMarkerMatchesDataPoint = true;
                        } else {
                            rowContent += "";
                        }
                    }
                    
                    rows.set(rowIndex, rowContent.concat(iTimeMarker.hasNext() ? ";" : "\n"));
                }
                
                if (timeMarkerMatchesDataPoint) {
                    try {
                        // Move on to the next data point
                        dataPoint = dps.get(++dataPointIndex);
                    } catch (Exception e) {
                        // = Index out of bounds = No more data points
                    }
                }
            }
            
            /*
            // Original, sub-optimal routine
            Iterator<TimeSeriesTimestamp> iTimeMarker = tsc.getTimeMarkerIterator();
            
            while (iTimeMarker.hasNext()) {
                TimeSeriesTimestamp timeMarker = iTimeMarker.next();
                
                //s += "<!-- getting data for " + timeMarker + " ... -->\n";
                
                TimeSeriesDataPoint dataPoint = getDataPointForTimeMarker(timeMarker);
                
                for (int i = 0; i < numRows; i++) { // Must use getValuesPerDataPoint because dataPoint might be null
                    if (rows.get(i).isEmpty()) {
                        //s += "<!-- label appendix is " + getLabelFor(getValueAPIKey(i)) + " -->\n";
                        String rowStart = APIUtil.escapeCSV(this.getLabel(displayLocale) + (i > 0 ? " (".concat(getLabelFor(getValueAPIKey(i))).concat(")") : "")) + ";";
                        rowStart += APIUtil.escapeCSV(this.getUnit().getShortForm()) + ";";
                        rows.set(i, rowStart);
                    }
                    rows.set(i, rows.get(i) + (dataPoint == null ? "" : dataPoint.get(i, "#.####")) + (iTimeMarker.hasNext() ? ";" : "\n"));
                }
                
            }
            //*/
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
    
    /**
     * @return The JSON object that reflects how this time series is represented by the service. 
     * @deprecated Replaced by the interface-defined {@link #getJSON()} method (which is identical).
     */
    public JSONObject getAPIStructure() { return o; }
    
    /**
     * Gets the accuracy level for the timestamps in this time series.
     * <p>
     * The accuracy levels are defined in {@link TimeSeriesTimestamp}, in its
     * TYPE_XXX members.
     * 
     * @return The timestamp accuracy level, e.g. {@link TimeSeriesTimestamp#TYPE_YEAR}.
     * @see TimeSeriesTimestamp#TYPE_TIME
     * @see TimeSeriesTimestamp#TYPE_DATE
     * @see TimeSeriesTimestamp#TYPE_MONTH
     * @see TimeSeriesTimestamp#TYPE_YEAR
     * @see TimeSeriesTimestamp#TYPE_LITERAL
     */
    public int getDateTimeAccuracy() { return timestampsType; }
    //public String getDateTimeAccuracy() { return dateTimeAccuracy; }
        
    /**
     * @return A flag indicating whether or not this series has error bars. 
     */
    public boolean isErrorBarSeries() { return this.hasHigh && this.hasLow && (!this.hasMax && !this.hasMin); }
    
    /**
     * Gets the number of values contained in each data point of this series.
     * <p>
     * This method should always return 1, 3 or 5:
     * <ul>
     * <li>1: only main value</li>
     * <li>3: additional high and low values (typically "error bar" series)</li>
     * <li>5: additional minimum and maximum values (typically "boxplot" series)</li>
     * </ul>
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
     * Compares this time series to the given one.
     * <p> 
     * If they are not equal, a test is done to compare the first timestamp of 
     * each time series. If this test is not zero, we return that value. 
     * Otherwise, we return the result of comparing (as strings) the hash codes.
     * 
     * @param that The time series to compare to this instance.
     * @return Anything other than zero indicates a difference. 
     */
    @Override 
    public int compareTo(TimeSeries that) {
        if (this.equals(that)) {
            return 0;
        }
        
        try {
            int tsRes = this.getTimestamps().get(0).compareTo(
                    that.getTimestamps().get(0)
            );
            if (tsRes != 0) {
                return tsRes;
            }
            return String.valueOf(this.hashCode()).compareTo(
                    String.valueOf(that.hashCode())
            );
            
        } catch (Exception e) {}
        
        return 1; // Just something non-zero
    }
    
    /**
     * Gets the hash code.
     * <p>
     * The hash code is based only on the ID string, because it should be unique
     * for all time series entries.
     * 
     * @return The hash code for this time series.
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    
    /**
     * If the given object is a time series and not this instance, the return 
     * value is the result of an ID comparison.
     * 
     * @param obj The (time series) object to compare with.
     * @return <code>true</code> if the given object is this instance or equivalent to it, <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeSeries)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        
        return this.hashCode() == ((TimeSeries)obj).hashCode();
    }
    
    /**
     * Gets the timestamp format for this time series, according to its own
     * defined accuracy level.
     * 
     * @return The timestamp format for this time series, formatted according to its own defined accuracy level.
     */
    //public SimpleDateFormat getTimestampFormat() { return this.timestampFormat; }
    
    /**
     * Resolves a date format pattern by evaluating a given timestamp (which is 
     * typically read from the API service).
     * 
     * @param apiTimestamp The timestamp to evaluate.
     * @return A date format pattern that can be used when parsing the timestamp string.
     */
    /*public String resolveTimestampPattern(String apiTimestamp) {
        String[] variants = new String[] { "yyyy-mm-dd", "yyyy-mm", "yyyy" };
        int i = 0;
        for (; i < variants.length; i++) {
            try {
                new SimpleDateFormat(variants[i]).parse(apiTimestamp);
                break;
            } catch (Exception e) {
                
            }
        }
        
        return variants[i];
    }*/
    
    
    
    /**
     * Resolves a date format pattern by evaluating a given timestamp (which is 
     * typically read from the API service).
     * 
     * @param apiTimestamp The timestamp to evaluate.
     * @return A date format pattern that can be used when parsing the timestamp string.
     */
    /*public String normalizeTimestamp(String apiTimestamp) {
        String[] variants = new String[] { PATTERN_DATE_API, PATTERN_DATE_API, "yyyy-MM", "yyyy" };
        int i = 0;
        for (; i < variants.length; i++) {
            try {
                new SimpleDateFormat(variants[i]).parse(apiTimestamp);
                break;
            } catch (Exception e) {
                // Just continue
            }
        }
        try {
            return new SimpleDateFormat(PATTERN_DATE_API, displayLocale).format(new SimpleDateFormat(variants[i], displayLocale).parse(apiTimestamp));
        } catch (Exception e ) {
            return null;
        }
    }*/
    
    /**
     * Sets the timestamp format for this time series, according to its own
     * defined accuracy level.
     * 
     * @return This time series, updated.
     */
    /*private TimeSeries setTimestampFormat() {
        String f = null;
        
        if (timestampsType == TimeSeriesTimestamp.TYPE_UNKNOWN) {
            f = TimeSeriesTimestamp.PATTERN_TIME_STANDARD;
        } else if (timestampsType == TimeSeriesTimestamp.TYPE_TIME) {
            f = "yyyy-MM-dd HH:mm:ss";
        } else if (timestampsType == TimeSeriesTimestamp.TYPE_DATE) {
            f = "yyyy-MM-dd HH:mm:00";
        } else if (timestampsType == TimeSeriesTimestamp.TYPE_MONTH) {
            f = "yyyy-MM-dd HH:00:00";
        } else if (timestampsType == TimeSeriesTimestamp.TYPE_YEAR) {
            f = "yyyy";
        }
        
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
        
        this.timestampFormat = new SimpleDateFormat(TimeSeriesTimestamp.PATTERN_TIME_STANDARD, displayLocale);
        return this;
    }*/
}
