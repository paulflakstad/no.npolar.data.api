package no.npolar.data.api.mosj;

//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
//import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
//import java.util.Map;
import java.util.ResourceBundle;
//import java.util.TreeSet;
import no.npolar.data.api.MOSJService;
import no.npolar.data.api.TimeSeriesDataUnit;
import no.npolar.data.api.TimeSeries;
import no.npolar.data.api.TimeSeriesCollection;
import no.npolar.data.api.TimeSeriesDataPoint;
import no.npolar.data.api.TimeSeriesTimestamp;
import no.npolar.data.api.Labels;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONObject;
import org.opencms.json.JSONException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencms.json.JSONArray;
import org.opencms.util.CmsStringUtil;

/**
 * Adds support for generating {@link http://highcharts.com Highcharts charts}.
 * <p>
 * Each chart's job is to present 1-N time series, both as a visualization (a 
 * Highcharts chart), and as text (a table).
 * <p>
 * Custom chart settings may exist, both globally (i.e. affecting all time 
 * series and/or the chart itself) and/or specific to individual time series.
 * <p>
 * An earlier version relied heavily on the {@link MOSJParameter} class. This 
 * dependency was later relocated to the {@link TimeSeriesCollection} class.
 * <p>
 * For more info, please refer to the general  
 * {@link http://api.highcharts.com/highcharts Highcharts documentation}.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class HighchartsChart {
    /** The MOSJ parameter that is the basis for this chart. */
    //private MOSJParameter mosjParameter = null;
    /** The set of time series that go into this chart. */
    private TimeSeriesCollection timeSeriesColl = null;
    /** Override settings - global and/or specific to individual time series. */
    private JSONObject overrides = null;
    /** The preferred language. */
    private Locale displayLocale = null;
    //** The chart title, typically the time series collection's title, or a manually set one. */
    //private String title = null;
    /** The chart ID, typically the MOSJ parameter's ID, or a manually set one. */
    private String id = null;
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(HighchartsChart.class);
    
    /** Override key: Time series ID (when overriding individual time series). */
    public static final String OVERRIDE_KEY_SERIES_ID = "series";
    /** Override key: Chart / series type. {@link http://api.highcharts.com/highcharts#series.type } */
    public static final String OVERRIDE_KEY_TYPE_STRING = "type";
    /** Override key: Chart group inverting. {@link http://jsfiddle.net/dcus5fjs/1/ } */
    public static final String OVERRIDE_KEY_INVERT_GROUPING_BOOL = "invertGrouping";
    /** Override key: Series name. {@link http://api.highcharts.com/highcharts#series.name } */
    public static final String OVERRIDE_KEY_NAME_STRING = "name";
    /** Override key: Number of steps in-between labels on the x-axis. {@link http://api.highcharts.com/highcharts#xAxis.labels.step } */
    public static final String OVERRIDE_KEY_X_AXIS_LABEL_STEP_INT = "step";
    /** Override key: Number of degrees to rotate the x-axis labels. {@link http://api.highcharts.com/highcharts#xAxis.labels.rotation } */
    public static final String OVERRIDE_KEY_X_AXIS_LABEL_ROTATION_INT = "xLabelRotation";
    /** Override key: X-axis on top flag. {@link http://api.highcharts.com/highcharts#xAxis.reversed } and {@link http://api.highcharts.com/highcharts#xAxis.opposite } */
    public static final String OVERRIDE_KEY_X_AXIS_ON_TOP = "xAxisOnTop";
    /** Override key: X-axis "enforce equal steps" flag. Can be used on irregular time series data, e.g. to show in-between years with no data. */
    public static final String OVERRIDE_KEY_X_AXIS_ENFORCE_EQUAL_STEPS = "enforceEqualSteps";
    /** Override key: Number of lines to spread labels over (applies to horizontal lines). {@link http://api.highcharts.com/highcharts#xAxis.labels.staggerLines } */
    public static final String OVERRIDE_KEY_MAX_STAGGER_LINES_INT = "maxStaggerLines";
    /** Override key: Hide point markers in the series? {@link http://api.highcharts.com/highcharts#plotOptions.series.marker.enabled } */
    public static final String OVERRIDE_KEY_HIDE_MARKERS_BOOL = "dots";
    /** Override key: Series stacking setting. {@link http://api.highcharts.com/highcharts#plotOptions.series.stacking } */
    public static final String OVERRIDE_KEY_STACKING = "stacking";
    /** Override key: Series error setting. {@link http://highcharts.uservoice.com/forums/55896-highcharts-javascript-api/suggestions/4321475-add-an-ability-to-show-error-bar-in-legend } */
    public static final String OVERRIDE_KEY_ERROR_TOGGLER = "errorToggler";
    /** Override key: Series "trend line" flag. */
    public static final String OVERRIDE_KEY_TREND_LINE = "trendLine";
    /** Override key: The "dash style" value. {@link http://www.highcharts.com/docs/chart-concepts/series#12} */
    public static final String OVERRIDE_KEY_DASH_STYLE = "dashStyle";
    /** Override key: Series order index (a number). */
    public static final String OVERRIDE_KEY_ORDER_INDEX = "orderIndex";
    /** Override key: Series "connect nulls" (don't show as discontinuous) flag. {@link http://api.highcharts.com/highcharts#plotOptions.series.connectNulls } */
    public static final String OVERRIDE_KEY_CONNECT_NULLS = "connectNulls";
    /** Override key: Series color. {@link http://api.highcharts.com/highcharts#plotOptions.series.color } */
    public static final String OVERRIDE_KEY_COLOR = "color";
    /** Override key: Series line thickness. {@link http://api.highcharts.com/highcharts#plotOptions.series.lineWidth } */
    public static final String OVERRIDE_KEY_LINE_THICKNESS = "lineThickness";
    /** Override key: Series marker thickness. {@link http://api.highcharts.com/highcharts#plotOptions.series.marker.radius } */
    public static final String OVERRIDE_KEY_MARKER_THICKNESS = "dotThickness";
    /** Override key: Y-axis minimum value (a number). {@link http://api.highcharts.com/highcharts#yAxis.min } */
    public static final String OVERRIDE_KEY_Y_AXIS_MIN = "minValue";
    /** Override key: Y-axis "allow decimals" flag. {@link http://api.highcharts.com/highcharts#yAxis.allowDecimals } */
    public static final String OVERRIDE_KEY_Y_AXIS_INTEGERS_ONLY = "integerValues";
    /** Override key: The credit text. */
    public static final String OVERRIDE_KEY_CREDIT_TEXT = "creditText";
    /** Override key: The credit link URI. */
    public static final String OVERRIDE_KEY_CREDIT_URI = "creditUri";
    //** Override key: Manual y-axis placement. */
    //public static final String OVERRIDE_KEY_Y_AXIS = "yAxis";
    /** The default series type name ("line"). */
    public static final String DEFAULT_SERIES_TYPE = "line";
    /** The default series stacking setting ("normal"). */
    public static final String DEFAULT_STACKING = "normal";
    /** The series type name for box plot charts. */
    public static final String SERIES_TYPE_BOX_PLOT = "boxplot";
    /** The dash style keyword that indicates a short/dotted line. */
    public static final String DASH_STYLE_SHORT = "shortdot";
    /** The dash style keyword that indicates a long/dashed line. */
    public static final String DASH_STYLE_LONG = "longdash";
    
    /** Highcharts-specific timestamp formatting for timestamps with <strong>date</strong> accuracy. */
    public static final String DATE_FORMAT_DATE = "yyyy,M,d";
    
    /** The number formatting pattern to use. */
    public static final String NUMBER_FORMAT = TimeSeriesDataPoint.DEFAULT_NUMBER_FORMAT;
    /** The number formatting locale (English, because we need 3.14, not 3,14). */
    public static final Locale NUMBER_FORMAT_LOCALE = Locale.forLanguageTag("en");
    
    /**
     * Threshold value, indicates the maximum number of data points (per series) 
     * that will be added inline (as part of the <code>data</code> array). 
     * <p>
     * For any series containing more data points, the <code>data</code> array
     * will be <strong>empty</strong>, and must be populated by a javascript 
     * function that AJAX-loads the data. This approach is employed in order to 
     * avoid severe performance hits when dealing with large time series.
     * <p>
     * For more info, see <code>fillEmptyData(obj)</code> in commons.js and the 
     * {@link #getChartConfigurationString()}.
     */
    public static final int MAX_ALLOWED_INLINE_POINTS = 500;
    
    /**
     * Creates a new instance based on the given MOSJ parameter and overrides.
     * 
     * @param mp The MOSJ parameter. Mandatory (not <code>null</code>).
     * @param overrides The overrides. Can be <code>null</code>.
     * @deprecated The concept of MOSJ parameters is obsolete. Consider using one of the alternative constructors.
     */
    public HighchartsChart(MOSJParameter mp, JSONObject overrides) {
        //this.mosjParameter = mp;
        id = mp.getId();
        this.overrides = overrides;
        this.displayLocale = mp.getDisplayLocale();
        
        // Note: The overrides must be set prior to ordering time series
        this.timeSeriesColl = resolveTimeSeriesOrdering( mp.getTimeSeriesCollection() );
    }
    
    /**
     * Creates a new chart instance using the given details.
     * <p> 
     * If there is no need to set the chart ID explicitly, you can provide 
     * <code>null</code> to auto-generate an ID based on the title of the given 
     * time series collection.
     * 
     * @param id The chart ID. Provide <code>null</code> to generate an ID.
     * @param tsc The time series for this chart.
     * @param overrides The overrides / custom settings. Can be <code>null</code>.
     */
    public HighchartsChart(String id, TimeSeriesCollection tsc, JSONObject overrides) {
        this.id = id;
        this.overrides = overrides;
        displayLocale = tsc.getDisplayLocale();
        
        // Note: Ordering time series should always be done *after* having set
        // the overrides / custom settings.
        timeSeriesColl = resolveTimeSeriesOrdering(tsc);
        if (id == null || id.isEmpty()) {
            this.id = APIUtil.toURLFriendlyForm(tsc.getTitle()).replace(".", "-");
        }
    }
    
    /**
     * Creates a new chart instance with the given overrides / custom settings, 
     * and with an auto-generated ID, based on the title of the given time 
     * series collection.
     * 
     * @param tsc The time series for this chart.
     * @param overrides The overrides / custom settings. Can be <code>null</code>.
     * @see #HighchartsChart(java.lang.String, no.npolar.data.api.TimeSeriesCollection, org.opencms.json.JSONObject) 
     */
    public HighchartsChart(TimeSeriesCollection tsc, JSONObject overrides) {
        this(null, tsc, overrides);
    }
    
    /**
     * Creates a new chart instance without any overrides / custom settings, and
     * with an auto-generated ID, based on the title of the given time series 
     * collection.
     * 
     * @param tsc The time series for this chart.
     * @see #HighchartsChart(java.lang.String, no.npolar.data.api.TimeSeriesCollection, org.opencms.json.JSONObject) 
     */
    public HighchartsChart(TimeSeriesCollection tsc) {
        this(null, tsc, null);
    }
    
    /**
     * Convenience method: Converts the chart configuration string into a JSON 
     * object.
     * 
     * @see HighchartsChart#getChartConfigurationString() 
     * @return The chart configuration as a JSON object.
     * @throws JSONException If the chart configuration string cannot be parsed as a JSON object.
     */
    public JSONObject getChartConfiguration() throws JSONException {
        return new JSONObject(getChartConfigurationString());
    }
    
    /**
     * Resolves any manually defined ordering of the time series in the given 
     * time series collection.
     * <p>
     * Any manual ordering is defined in the override object.
     * 
     * @param timeSeriesCollection The collection to resolve ordering for.
     * @return The given collection, possibly with an updated time series order.
     */
    private TimeSeriesCollection resolveTimeSeriesOrdering(TimeSeriesCollection timeSeriesCollection) {
        if (overrides == null || overrides.equals(new JSONObject()))
            return timeSeriesCollection;
        
        // some override exist, check for manually defined ordering
        boolean manuallyOrdered = false;
        
        Iterator<TimeSeries> i = timeSeriesCollection.getTimeSeries().iterator();
        while (i.hasNext()) {
            TimeSeries ts = i.next();
            JSONObject tsOverrides = getTimeSeriesOverrides(ts, overrides);
            if (tsOverrides != null && tsOverrides.has(OVERRIDE_KEY_ORDER_INDEX)) {
                try { 
                    ts.setOrderIndex(tsOverrides.getInt(OVERRIDE_KEY_ORDER_INDEX));
                    manuallyOrdered = true; 
                } catch (Exception e) {}
            }
        }
        
        if (manuallyOrdered) {
            timeSeriesCollection.sortTimeSeries(TimeSeries.ORDER_INDEX_COMPARATOR);
        }
        
        return timeSeriesCollection;
    }
    
    /**
     * Ensures equal steps between all time markers.
     * <p>
     * Currently works only for yearly time markers, where "missing" years are 
     * added. E.g. in a collection that has time markers for 2010, 2012, 2013, 
     * and 2015, time markers for 2011 and 2014 will be added.
     * 
     * @param timeSeriesCollection The time series collection that is possibly missing time markers.
     * @return The given time series collection, updated.
     */
    protected TimeSeriesCollection fillTimeMarkerGaps(TimeSeriesCollection timeSeriesCollection) {
        try {
            int year = Integer.MIN_VALUE;
            int prevYear = Integer.MAX_VALUE;
            
            // First, find the smallest interval between two points
            int smallestInterval = Integer.MAX_VALUE;
            Iterator<TimeSeriesTimestamp> itr = timeSeriesCollection.getTimeMarkerIterator();
            while (itr.hasNext()) {
                year = Integer.valueOf(itr.next().toString());
                //System.out.println("Found time marker: " + year);
                if (prevYear != Integer.MAX_VALUE) {
                    if (year - prevYear < smallestInterval) {
                        smallestInterval = year - prevYear;
                    }
                }
                prevYear = year;
            }
            //System.out.println("Smallest interval: " + smallestInterval);
            
            year = Integer.MIN_VALUE;
            prevYear = Integer.MAX_VALUE;
            
            // Map out which time markers are "missing", and store these in a list
            List<TimeSeriesTimestamp> missing = new ArrayList();
            //Map<TimeSeriesTimestamp, TimeSeriesDataPoint[]> dataToAdd = new HashMap<TimeSeriesTimestamp, TimeSeriesDataPoint[]>();
            
            itr = timeSeriesCollection.getTimeMarkerIterator();
            while (itr.hasNext()) {
                String yearStr = itr.next().toString();
                //System.out.println("Evaluating '" + yearStr + "'");
                year = Integer.valueOf(yearStr);
                if (prevYear != Integer.MAX_VALUE) {
                    while (year - prevYear > smallestInterval) {
                        prevYear = prevYear + smallestInterval;
                        //dataToAdd.put(new TimeSeriesTimestamp(year), null);
                        missing.add(new TimeSeriesTimestamp(prevYear));
                    }
                }
                prevYear = year;
            }
            
            // Add the "missing" time markers
            //timeSeriesCollection.addDataPoints(dataToAdd);
            if (!missing.isEmpty()) {
                for (TimeSeriesTimestamp missingTimestamp : missing) {
                    timeSeriesCollection.setEmptyOnTimestamp(missingTimestamp);
                }
            }
        } catch (Exception e) {
            // Non-yearly time marker format
            //System.out.println("CRASH! Error was: " + e.getMessage());
            e.printStackTrace();
        }
        return timeSeriesCollection;
    }
    
    /**
     * Returns the chart configuration string.
     * <p>
     * It should be parseable as a JSON object.
     * 
     * @return The chart configuration string (a stringified JSON object).
     */
    public String getChartConfigurationString() {
        String s = "";
        try {
            // Prevent NPE
            if (overrides == null) overrides = new JSONObject(); // Empty json object
            
            // Get the time series collection via the MOSJ parameter, and make 
            // sure any custom defined order is applied.
            //TimeSeriesCollection timeSeriesCollection = resolveTimeSeriesOrdering(timeSeriesColl);
            
            
            List<TimeSeriesDataUnit> units = timeSeriesColl.getUnits();
            //System.out.println("units: " + units);
            Iterator<TimeSeriesDataUnit> iUnits = units.iterator();
            
            // Enforce equal steps?
            boolean xAxisEnforceEqualSteps = false;
            try { xAxisEnforceEqualSteps = Boolean.valueOf(overrides.getString(OVERRIDE_KEY_X_AXIS_ENFORCE_EQUAL_STEPS)); } catch(Exception ee) {}
            if (xAxisEnforceEqualSteps) {
                timeSeriesColl = fillTimeMarkerGaps(timeSeriesColl);
            }
            
            String type = "zoomType: 'x'";
            int step = timeSeriesColl.getTimeMarkersCount() / 8;
            if (this.containsDateSeries()) {
                step = 0;
            }
            int maxStaggerLines = -1;
            int xLabelRotation = -1;
            boolean hideMarkers = false;
            String stacking = null;
            //boolean errorBarsAlwaysOn = false;
            boolean xAxisOnTop = false;
            boolean invertGrouping = false;
            
            try { type = "type: '" + overrides.getString(OVERRIDE_KEY_TYPE_STRING) + "'"; } catch(Exception ee) {}
            try { step = Integer.valueOf(overrides.getString(OVERRIDE_KEY_X_AXIS_LABEL_STEP_INT)); } catch(Exception ee) {}
            try { maxStaggerLines = Integer.valueOf(overrides.getString(OVERRIDE_KEY_MAX_STAGGER_LINES_INT)); } catch(Exception ee) {}
            try { xLabelRotation = Integer.valueOf(overrides.getString(OVERRIDE_KEY_X_AXIS_LABEL_ROTATION_INT)); } catch(Exception ee) {}
            try { hideMarkers = !Boolean.valueOf(overrides.getString(OVERRIDE_KEY_HIDE_MARKERS_BOOL)); } catch(Exception ee) {}
            try { stacking = overrides.getString(OVERRIDE_KEY_STACKING); } catch (Exception ee) {}
            //try { errorBarsAlwaysOn = Boolean.valueOf(overrides.getString(OVERRIDE_KEY_ERROR_TOGGLER)); } catch(Exception ee) {}
            try { xAxisOnTop = Boolean.valueOf(overrides.getString(OVERRIDE_KEY_X_AXIS_ON_TOP)); } catch(Exception ee) {}
            try { invertGrouping = Boolean.valueOf(overrides.getString(OVERRIDE_KEY_INVERT_GROUPING_BOOL)); } catch(Exception ee) {}
            
            
            s += "{ ";
            // Chart type
            s += "\nchart: { ";
            if (overrides.has(OVERRIDE_KEY_CREDIT_TEXT)) {
                s += "\nspacingBottom: 50,"; // Allow some air between legend and credit
            }
            s += type;
            // Swap grouping and series names? 
            // Important: The js method toggleHighChartsGrouping(jQuery) MUST be available
            //if (invertGrouping || ) {
                s += ",";
                s += "\nevents: {";
                    s += "\nload: function() {";
                        if (invertGrouping) {
                            s += "\nvar customSettings = {};"; 
                            if (overrides != null) {                                
                                s += "\ncustomSettings = " + overrides.toString() + ";";
                            }
                            s += "\ntoggleHighChartsGrouping(this, customSettings);";
                        }
                        s += "\nfillEmptyData(this);"; // Enables AJAX-loading of data, see getValuesForTimeSeries() and fillEmptyData() in commons.js
                    s += "\n}";
                s += "\n}";
            //}
            s += "}, ";
            
            s += "\ntitle: { text: '" + timeSeriesColl.getTitle().replaceAll("'", "\\\\'") + "' }, ";
            s += "\nurl: '" + timeSeriesColl.getURL() + "', ";
            
            //if (hideMarkers || timeSeriesCollection.getTimeSeries().size() == 1 || stacking != null) {
                String plotOptionsSeries = "";
                if (hideMarkers) {
                    plotOptionsSeries += "\nmarker: { enabled: false }";
                }
                
                
                
                // If there is only 1 time series, disable "click to hide"
                //if (timeSeriesCollection.getTimeSeries().size() == 1) {
                    plotOptionsSeries += plotOptionsSeries.length() > 0 ? "," : "";
                    //plotOptionsSeries += "\npoint: { ";
                        plotOptionsSeries += "\nevents: { ";
                            plotOptionsSeries += "\nlegendItemClick: function(e) { ";
                                                    
                                // Make the function that shows/hides the error bars when clicking on the dummy series name in the legend
                                if (timeSeriesColl.hasErrorBarSeries()) {
                                    plotOptionsSeries += "e.preventDefault();";
                                    plotOptionsSeries += "\nif(this.options.connectTo) { // Error bar series"
                                                            + "\nvar parentSeries = this.chart.get(this.options.connectTo);"
                                                            + "\nif (this.chart.get(parentSeries.options.linkedTo).visible) {"
                                                                + "\nthis.chart.get(this.options.connectTo).setVisible(!this.visible);"
                                                                + "\nthis.setVisible(!this.visible);"
                                                            + "\n}"
                                                        + "\n}"
                                                        + "\nelse { // Regular series"
                                                            + "\nvar id = this.options.id;"
                                                            + "\nthis.setVisible(!this.visible);"
                                                            + "\nthis.chart.get(id + \"-error\").setVisible(false);"
                                                            + "\nthis.chart.get(id + \"-error-toggler\").setVisible(false);"
                                                        + "\n}";
                                }
                                // If there is only a single time series AND it's not an error bar series, make in unhideable
                                else if (timeSeriesColl.getTimeSeries().size() == 1) {
                                    plotOptionsSeries += "\nreturn false;";
                                }
                                                    
                            plotOptionsSeries += "\n}";
                        plotOptionsSeries += "\n}";
                    //plotOptionsSeries += "\n}";
                //}
                
                
                if (stacking != null) {
                    plotOptionsSeries += plotOptionsSeries.length() > 0 ? "," : "";
                    plotOptionsSeries += "\nstacking: '" + stacking + "'";
                }
                    
                    
                if (!plotOptionsSeries.isEmpty()) {
                    //plotOptionsSeries = plotOptionsSeries;
                    s += "\nplotOptions: { ";
                        s += "\nseries: { " + plotOptionsSeries + "\n}";
                    s += "\n}, ";
                }
            //}
            
            /*if (stacking != null) {
                s += "\nplotOptions: { ";
                    s += "\nseries: { ";
                        s += "\nstacking: '" + stacking + "'";
                    s += "\n}";
                s += "\n}, ";
            }*/
            
            // Credits
            // See http://jsfiddle.net/gh/get/jquery/3.1.1/highcharts/highcharts/tree/master/samples/highcharts/credits/href/
            s += "\ncredits: { ";
                if (overrides.has(OVERRIDE_KEY_CREDIT_TEXT) && overrides.has(OVERRIDE_KEY_CREDIT_URI)) {
                    s += "\ntext: '" + overrides.getString(OVERRIDE_KEY_CREDIT_TEXT) + "',";
                    s += "\nhref: '" + overrides.getString(OVERRIDE_KEY_CREDIT_URI) + "',";
                    s += "\nstyle : { 'color':'#555', 'fontSize':'0.6rem' }";
                } else {
                    s += "\nenabled: false";
                }
            s += "\n}, ";
                    
            
            // The x axis
            s += "\nxAxis: [{ ";
                    boolean useCategory = true;
                    try {
                        if (this.containsDateSeries()) {
                            useCategory = false;
                        }
                    } catch (Exception e) {}
                    if (useCategory) {
                        // ToDo: Default should be datetime, not categories
                        // .... or SHOULD IT..? See http://stackoverflow.com/questions/23816474/highcharts-xaxis-yearly-data
                        s += "\ncategories: [" + makeCategoriesString(timeSeriesColl) + "], ";
                    } else {
                        s += "\ntype: 'datetime', ";
                    }
                    if (xAxisOnTop) {
                        s += "\nopposite: true,";
                    }
                    s += "\nlabels: { ";
                        s += "\nstep: " + step + "";
                        if (maxStaggerLines > 0) {
                            s += ",\nmaxStaggerLines: " + maxStaggerLines;
                        }
                        if (xLabelRotation > 0) {
                            s += ",\nrotation: " + xLabelRotation;
                        }
                    s += "\n}";
            s += "\n}], ";
            
            // The y axis / axes
            s += "\nyAxis: [ ";
                    int i = 0;
                    while (iUnits.hasNext()/* && i < 4*/) {
                        TimeSeriesDataUnit unit = iUnits.next();
                        
                        // Resolve some info about the values of this time series: 
                        boolean integerValuesOnly = true; // Flag: Non-decimal values only? 
                        boolean positiveValuesOnly = true; // Flag: Non-negative values only?
                        double largestValue = Double.MAX_VALUE; // The maximum value
                        
                        List<TimeSeries> axisSeriesList = timeSeriesColl.getTimeSeriesWithUnit(unit); // Get the time series for this axis
                        Iterator<TimeSeries> iAxisSeries = axisSeriesList.iterator();
                        while (iAxisSeries.hasNext()) {
                            TimeSeries axisSeries = iAxisSeries.next();
                            
                            try {
                                if (axisSeries.getMaxValue() > largestValue) {
                                    largestValue = axisSeries.getMaxValue();
                                }
                            } catch (Exception whut) {}
                            
                            if (!axisSeries.isIntegerValuesOnlySeries()) {
                                integerValuesOnly = false;
                            }
                            if (!axisSeries.isPositiveValuesOnlySeries()) {
                                positiveValuesOnly = false;
                            }
                            if (!integerValuesOnly && !positiveValuesOnly)
                                break;
                        }
                        
                        // Should we define a minimum value for the y-axis?
                        Integer min = null; // null = don't set a minimum value for the y-axis (let Highcharts decide)
                        /*if (positiveValuesOnly && largestValue <= 100) { // 100 because 100 %
                            min = 0; // 0 = set the minimum value to zero
                        }*/
                        
                        try { 
                            if (overrides.has(OVERRIDE_KEY_Y_AXIS_INTEGERS_ONLY)) {
                                integerValuesOnly = Boolean.valueOf(overrides.getString(OVERRIDE_KEY_Y_AXIS_INTEGERS_ONLY));
                            }
                        } catch(Exception ee) {
                        }
                        try { 
                            if (overrides.has(OVERRIDE_KEY_Y_AXIS_MIN)) {
                                min = Integer.valueOf(overrides.getString(OVERRIDE_KEY_Y_AXIS_MIN)); // set the minimum value accordingly
                            }
                        } catch(Exception ee) {
                        }
                        
                        
                        s += "\n{ ";
                            if (integerValuesOnly) {
                                s += "\nallowDecimals: false,";
                            }
                            if (min != null) {
                                s += "\nmin: " + min + ",";
                            }
                            if (xAxisOnTop) {
                                s += "\nreversed: true,";
                            }
                            s += "\nlabels: { "; 
                                    //s += "\nformat: '{value} " + unit.getShortForm() + "', ";
                                    s += "\nformat: '{value}', ";
                                    s += "\nstyle: { ";
                                        s += "\ncolor: Highcharts.getOptions().colors[" + i + "] ";
                                    s += "\n}";
                            s += "\n}, ";
                            s += "\ntitle: { ";
                                    s += "\nuseHTML: true,";
                                    s += "\ntext: '" + CmsStringUtil.escapeJavaScript(unit.getLongForm() + (unit.hasShortForm() ? "  ( ".concat(unit.getShortForm()).concat(" )") : "")) + "', ";
                                    s += "\nstyle: { ";
                                        s += "color: Highcharts.getOptions().colors[" + i + "] ";
                                    s += "}";
                            s += "\n}";
                            
                            if (i >= 1) {
                                s += ", \nopposite: true";
                            }
                        s += "\n}";
                        i++;
                        if (/*i < 4 && */iUnits.hasNext()) {
                            s += ",";
                        }
                    }                    
            s += " ], ";
            
            s += "\ntooltip: { ";
                s += "\nshared: true";
                if (this.containsDateSeries()) {
                    s += ", ";
                    s += "\nxDateFormat: '%Y-%m-%d'";
                }
            s += "\n}, ";
            
            // The actual data
            s += "\nseries: [ ";
                s += getSeriesDetails(timeSeriesColl, overrides);
            s += " ]";
            
            s += "\n}";
            
            
            /*
            // Return both the container div and the javascript
            return "<div id=\"chart-" + this.mosjParameter.getID() + "\" class=\"time-series-chart highcharts-chart\"></div>\n"
                    + "<script type=\"text/javascript\">\n"
                        + "$(function () {\n"
                            + "$('#chart-" + this.mosjParameter.getID() + "').highcharts(\n" 
                                + s + "\n" 
                            + ")\n"
                        + "});\n"
                    + "</script>\n";
            */
            
            // Better to return just the JSON string:
            // This allows for easier modification by the client / renderer JSP (should the need arise).
            // It also allows for more flexible placement of the javascript bit.
            return s;
            
            //return new JSONObject(s); // Nah, this can be done by the client if needed ...
        } catch (Exception e) {
            //e.printStackTrace();
            if (LOG.isErrorEnabled()) {
                LOG.error("Fatal error creating Highcharts-munchable config string.", e);
            }
        }       
        
        return null;
    }
    
    /**
     * Checks if this chart has at least one time series that has an accuracy 
     * level equal to {@link TimeSeries#DATE_FORMAT_UNIX_DATE} - hinting that 
     * we should not use the 'category' approach, but rather the 'datetime'.
     * 
     * @return True if this chart has at least one time series with 'date' accuracy level.
     */
    public boolean containsDateSeries() {
        if (timeSeriesColl.hasAccuracyCompatibleTimeSeries()) {
            return timeSeriesColl.getTimeSeries().get(0).getDateTimeAccuracy() > TimeSeriesTimestamp.TYPE_YEAR;
        } else {
            Iterator<TimeSeries> i = timeSeriesColl.getTimeSeries().iterator();
            while (i.hasNext()) {
                if (i.next().getDateTimeAccuracy() > TimeSeriesTimestamp.TYPE_YEAR) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /**
     * Gets the configuration details for all time series in the given 
     * collection, applying overrides according to the given override object.
     * 
     * @param timeSeriesCollection The time series collection. Mandatory.
     * @param overrides The overrides. Can be <code>null</code>.
     * @return The configuration string part for the series in the given collection.
     */
    protected String getSeriesDetails(TimeSeriesCollection timeSeriesCollection, JSONObject overrides) {
        try { if (overrides == null) overrides = new JSONObject(); } catch (Exception e) {} // 
        
        ResourceBundle labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        
        String s = "";
        List<TimeSeries> timeSeriesList = timeSeriesCollection.getTimeSeries();
        if (!timeSeriesList.isEmpty()) {
            Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
            int timeSeriesIndex = 0;
            while (iTimeSeries.hasNext()) {
                TimeSeries timeSeries = iTimeSeries.next();
                //System.out.println("getSeriesDetails: handling " + timeSeries.getTitle());
                // Defaults
                String seriesType = timeSeries.getValuesPerDataPoint() >= 5 ? SERIES_TYPE_BOX_PLOT : DEFAULT_SERIES_TYPE;
                String seriesName = timeSeries.getLabel();// timeSeriesCollection.getTitleForTimeSeries(timeSeries);
                boolean hideMarkers = false;
                boolean errorBarsAlwaysOn = false;
                //boolean isTrendLine = false;
                //boolean connectNulls = false;
                String dashStyle = null;
                
                // Customization
                JSONObject tsCustomization = getTimeSeriesOverrides(timeSeries, overrides);
                //System.out.println(tsCustomization);
                
                // Handle case: General overrides (e.g. a general "type" override)
                if (overrides != null) {
                    try { seriesType = overrides.getString(OVERRIDE_KEY_TYPE_STRING); timeSeries.setChartSeriesType(seriesType); } catch (Exception e) { }
                    try { hideMarkers = !Boolean.valueOf(overrides.getString(OVERRIDE_KEY_HIDE_MARKERS_BOOL)); } catch(Exception ee) {}
                    try { errorBarsAlwaysOn = !Boolean.valueOf(overrides.getString(OVERRIDE_KEY_ERROR_TOGGLER)); } catch(Exception ee) {}
                    if (overrides.has(OVERRIDE_KEY_CONNECT_NULLS)) {
                        try { 
                            timeSeries.setChartConnectNulls(Boolean.valueOf(overrides.getString(OVERRIDE_KEY_CONNECT_NULLS)));
                        } catch(Exception ee) {}
                    }
                } 
                // Handle case: Specific overrides for this individual time series
                if (tsCustomization != null) {
                //else {
                    try { seriesType = tsCustomization.getString(OVERRIDE_KEY_TYPE_STRING); timeSeries.setChartSeriesType(seriesType); } catch (Exception e) { }
                    try { seriesName = tsCustomization.getString(OVERRIDE_KEY_NAME_STRING); } catch (Exception e) { }
                    if (tsCustomization.has(OVERRIDE_KEY_HIDE_MARKERS_BOOL)) {
                        try { hideMarkers = !Boolean.valueOf(tsCustomization.getString(OVERRIDE_KEY_HIDE_MARKERS_BOOL)); } catch(Exception ee) {}
                    }
                    if (tsCustomization.has(OVERRIDE_KEY_ERROR_TOGGLER)) { 
                        try { errorBarsAlwaysOn = Boolean.valueOf(tsCustomization.getString(OVERRIDE_KEY_ERROR_TOGGLER));} catch(Exception ee) {}
                    }
                    if (tsCustomization.has(OVERRIDE_KEY_DASH_STYLE)) {
                        try { dashStyle = tsCustomization.getString(OVERRIDE_KEY_DASH_STYLE); } catch (Exception ee) {}
                    }
                    if (tsCustomization.has(OVERRIDE_KEY_TREND_LINE)) {
                        try { 
                            if (Boolean.valueOf(tsCustomization.getString(OVERRIDE_KEY_TREND_LINE))) {
                                timeSeries.flagAsTrendLine();
                                if (dashStyle != null) {
                                    timeSeries.setChartDashStyle(dashStyle);
                                }
                            }
                        } catch(Exception ee) {}
                    }
                    if (tsCustomization.has(OVERRIDE_KEY_CONNECT_NULLS)) {
                        try { 
                            timeSeries.setChartConnectNulls(Boolean.valueOf(tsCustomization.getString(OVERRIDE_KEY_CONNECT_NULLS)));
                        } catch(Exception ee) {}
                    }
                    if (tsCustomization.has(OVERRIDE_KEY_COLOR)) {
                        try { 
                            timeSeries.setChartColor(tsCustomization.getString(OVERRIDE_KEY_COLOR));
                        } catch(Exception ee) {}
                    }
                    if (tsCustomization.has(OVERRIDE_KEY_LINE_THICKNESS)) {
                        try {
                            timeSeries.setChartLineThickness(Integer.valueOf(tsCustomization.getString(OVERRIDE_KEY_LINE_THICKNESS)));
                        } catch(Exception ee) {}
                    }
                    if (tsCustomization.has(OVERRIDE_KEY_MARKER_THICKNESS)) {
                        try {
                            timeSeries.setChartMarkersThickness(Integer.valueOf(tsCustomization.getString(OVERRIDE_KEY_MARKER_THICKNESS)));
                        } catch(Exception ee) {}
                    }
                    /*if (tsCustomization.has(OVERRIDE_KEY_Y_AXIS_MIN)) {
                        try {
                            timeSeries.set(Integer.valueOf(tsCustomization.getString(OVERRIDE_KEY_Y_AXIS_MIN)));
                        } catch(Exception ee) {}
                    }
                    if (tsCustomization.has(OVERRIDE_KEY_Y_AXIS_INTEGERS_ONLY)) {
                        try {
                            timeSeries.set(Boolean.valueOf(tsCustomization.getString(OVERRIDE_KEY_Y_AXIS_INTEGERS_ONLY)));
                        } catch(Exception ee) {}
                    }*/
                }
                
                
                s += "\n{ ";
                try {
                    List<TimeSeriesDataUnit> units = new ArrayList<TimeSeriesDataUnit>(timeSeriesCollection.getUnits());
                    //int yAxis = units.indexOf( new MOSJDataUnit(timeSeries.getUnit(), timeSeries.getUnitVerbose(displayLocale)) );
                    int yAxis = units.indexOf(timeSeries.getUnit());
                    
                    /*
                    // Handle case: same unit, but time series is manually placed on separate y-axis
                    try {
                        //if (timeSeriesCollection.getUnits().size() == 1 && tsCustomization.has(OVERRIDE_KEY_Y_AXIS)) {
                        if (tsCustomization.has(OVERRIDE_KEY_Y_AXIS)) {
                            try { yAxis = Integer.valueOf(tsCustomization.getString(OVERRIDE_KEY_Y_AXIS)); } catch (Exception e) {  }
                        }
                    } catch (NullPointerException npe) {
                        // ignore
                    }
                    */
                            
                    s += "\nname: '" + CmsStringUtil.escapeJavaScript(seriesName) + "',";
                    s += "\ntype: '" + seriesType + "',";
                    s += "\nid: '" + timeSeries.getId() + "',";
                    s += "\nurl: '" + timeSeries.getURL(new MOSJService(displayLocale, true)) + "',";
                    s += "\nyAxis: " + yAxis + ",";
                    if (timeSeries.isTrendLine() || timeSeries.isChartConnectNulls()) {
                        s += "\nconnectNulls: true,";
                    }
                    if (hideMarkers || timeSeries.isTrendLine() || timeSeries.getChartMarkersThickness() != null) {
                        //s += "\nmarker: { enabled: false },";
                        s += "\nmarker: { ";
                        if (hideMarkers || timeSeries.isTrendLine()) {
                            s += "\nenabled: false" + (timeSeries.getChartMarkersThickness() != null ? "," : "");
                        }
                        if (timeSeries.getChartMarkersThickness() != null) {
                            s += "\nradius: " + timeSeries.getChartMarkersThickness();
                        }
                        s += "\n}, ";
                    }
                    if (timeSeries.getChartColor() != null) {
                        s += "\ncolor: '" + timeSeries.getChartColor() + "',";
                    }
                    if (timeSeries.getChartDashStyle() != null) {
                        s += "\ndashStyle: '" + timeSeries.getChartDashStyle() + "',";
                    }
                    s += "\ndata: [ " + getValuesForTimeSeries(timeSeriesCollection, timeSeries, false) + " ], ";
                    if (timeSeries.getChartLineThickness() != null) {
                        s += "\nlineWidth: " + timeSeries.getChartLineThickness() + ",";
                    }
                    if (timeSeries.getValuesPerDataPoint() == 5) {
                        s += "\ntooltip: { "
                                + "\npointFormat: '" 
                                                + labels.getString(Labels.TIME_SERIES_POINT_VALUE_MAX_0) + ": {point.high} " + timeSeries.getUnit().getShortForm() + "<br/>"
                                                + labels.getString(Labels.TIME_SERIES_POINT_VALUE_HIGH_0) + ": {point.q3} " + timeSeries.getUnit().getShortForm() + "<br/>"
                                                + "<span style=\"font-weight:bold;\">"+ labels.getString(Labels.TIME_SERIES_POINT_MEDIAN_0) + ": {point.median} " + timeSeries.getUnit().getShortForm() + "</span><br/>"
                                                + labels.getString(Labels.TIME_SERIES_POINT_VALUE_LOW_0) + ": {point.q1} " + timeSeries.getUnit().getShortForm() + "<br/>"
                                                + labels.getString(Labels.TIME_SERIES_POINT_VALUE_MIN_0) + ": {point.low} " + timeSeries.getUnit().getShortForm() + "<br/>"
                                                + "'"
                            + "\n}";
                    } else {
                        s += "\ntooltip: {"
                                + "\npointFormat: '<span style=\"font-weight: bold; color: {series.color}\">{series.name}</span>: <b>{point.y} " 
                                        + timeSeries.getUnit().getShortForm() + "</b>" + (timeSeries.isErrorBarSeries() ? (errorBarsAlwaysOn ? " " : "<br/>") : "<br/>") + "'"
                            + "\n}";
                    }
                    
                    // is this a time series with error bars?
                    if (timeSeries.isErrorBarSeries()) {
                        s += "},\n{";
                        
                        // first we define the actual error bar series.
                        // its "parent" will be the series directly above here.
                        String errorBarSeriesName = CmsStringUtil.escapeJavaScript(seriesName) + " " + labels.getString(Labels.TIME_SERIES_POINT_ERROR_0).toLowerCase();
                        //s += "\nname: '" + timeSeriesCollection.getTitleForTimeSeries(timeSeries) + " error',";
                        s += "\nname: '" + errorBarSeriesName + "',";
                        s += "\nid: '" + timeSeries.getId() + "-error" + "',";
                        s += "\ntype: '" + "errorbar" + "',";
                        s += "\nyAxis: " + yAxis + ",";
                        s += "\nvisible: " + errorBarsAlwaysOn + ",";
                        s += "\nlinkedTo: '" + timeSeries.getId()  + "',";
                        //if (!errorBarsAlwaysOn) {
                        //    s += "\nlinkedTo: null,";
                        //    s += "\nvisible: false,";
                        //}
                        s += "\ndata: [" + getValuesForTimeSeries(timeSeriesCollection, timeSeries, true) + "],";
                        s += "\ntooltip: {"
                                    + "\npointFormat: '({point.low}-{point.high} " + timeSeries.getUnit().getShortForm() + ")<br/>'"
                                + "\n}";
                        
                        // second, we define the dummy series that will act as
                        // the visibility toggler in the chart legend.
                        if (!errorBarsAlwaysOn) {
                            s += "},\n{";
                            s += "\nname: '" + errorBarSeriesName + "',"; // Use the same name
                            s += "\nid: '" + timeSeries.getId() + "-error-toggler" + "',";
                            s += "\ntype: '" + "errorbar" + "',";
                            s += "\nlinkedTo: null,";
                            s += "\nvisible: " + errorBarsAlwaysOn + ",";
                            s += "\nconnectTo: '" + timeSeries.getId() + "-error" + "'";
                        }
                    }
                    
                } catch (Exception e) {
                    //e.printStackTrace();
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Error creating Highcharts-munchable config string for time series '" + timeSeries.getId() +"'.", e);
                    }
                }
                s += "\n}";
                if (iTimeSeries.hasNext()) {
                    s += ",";
                }
                timeSeriesIndex++; // Important!
            }
        }
        return s;
    }
    
    /**
     * Gets the overrides for a specific time series.
     * 
     * @param ts The time series to look for overrides for.
     * @param overrides All overrides.
     * @return The overrides specific to the given time series, or null if none.
     */
    protected JSONObject getTimeSeriesOverrides(TimeSeries ts, JSONObject overrides) {
        // The OVERRIDE_SERIES_ID key identifies a time series that's being overridden (via its ID).
        // (So if the "series" key is missing, then no time series is being overridden.)
        if (overrides == null || !overrides.has(OVERRIDE_KEY_SERIES_ID)) 
            return null;
        
        try {
            JSONArray series = overrides.getJSONArray(OVERRIDE_KEY_SERIES_ID);
            for (int i = 0; i < series.length(); i++) {
                JSONObject seriesObj = series.getJSONObject(i);
                try {
                    if (seriesObj.get(TimeSeries.API_KEY_ID).equals(ts.getId())) {
                        //System.out.println("Found override for " + ts.getID());
                        return seriesObj;
                    }
                } catch (Exception ee) {
                    continue; // Just for clarity ...
                }
                //System.out.println("No series overrides for " + ts.getID());
            }
        } catch (Exception e) {
            //e.printStackTrace();
            // Log this
            if (LOG.isErrorEnabled()) {
                LOG.error("Critical error during override processing for time series '" + ts.getId() + "'.", e);
            }
        }
        
        return null;
    }
    
    /**
     * Transforms a service-formatted date "YYYY-MM-dd" into UTC form, ready for 
     * javascript's <code>Date.UTC()</code> function.
     * 
     * @param dateFromService Date in form YYYY-MM-dd (january=1)
     * @return Date in form YYYY,M,d (january=0)
     */
    private String toUTCDate(String dateFromService) {
        String[] parts = dateFromService.split("-");

        String year = parts[0];
        int month = Integer.parseInt(parts[1]);
        int date = Integer.parseInt(parts[2]);
        
        return "" + year + "," + (month-1) + "," + date;
    }
    
    /**
     * Gets the values for a single time series, in the context of the given 
     * time series collection.
     * <p>
     * The returned values string is "aware" of other time series in the given 
     * collection, and will contain <code>null</code> values for any time 
     * markers where the specific time series lacks a value.
     * <p>
     * For charts of type <code>datetime</code>, when the time series contains 
     * a large number (500 or more) of data points, an empty string will be 
     * returned, meaning the values will have to be added manually at a later 
     * time. (Typically this is done in the chart's <code>load</code> event.)
     * 
     * @param timeSeries The time series.
     * @param timeSeriesCollection The time series collection to use as context.
     * @param errorBarValues Whether or not to produce error bar values.
     * @return The values for the single time series, with <code>null</code> values where necessary.
     */
    private String getValuesForTimeSeries(TimeSeriesCollection timeSeriesCollection, TimeSeries timeSeries, boolean errorBarValues) {
        //System.out.print("Generating series data ...");
        long a = System.currentTimeMillis();
        
        
        String s = "";
        
        TimeSeriesTimestamp firstTimestamp = timeSeries.getTimestamps().get(0);
        
        // If we're dealing with dated series / single values
        //*
        if (timeSeries.isSingleValueSeries() && firstTimestamp.getType() == TimeSeriesTimestamp.TYPE_DATE && timeSeries.size() >= MAX_ALLOWED_INLINE_POINTS) {
            // return empty string => values must be filled later (usually done 
            // in the chart's load event)
            return s; 
        }//*/
        /*
        if (firstTimestamp.isMorePreciseThan(TimeSeriesTimestamp.TYPE_YEAR) && !firstTimestamp.isLiteralType()) {
            // ... we can just output the time/value pair directly
            try {
                JSONArray dataArr = timeSeries.getJSON().getJSONArray(TimeSeries.API_KEY_DATA_POINTS);
                for (int i = 0; i < dataArr.length(); i++) {
                    if (i > 0) {
                        s += ",";
                    }
                    JSONObject dataObj = dataArr.getJSONObject(i);
                    String val = dataObj.getString("value");
                    String date = dataObj.getString("date");
                    s += "[Date.UTC("+toUTCDate(date)+"),"+val+"]";
                }
            } catch (Exception e) {
                s += "{ 'error' : '" + e.getMessage() + "' }";
            }
        }
        //*/ 
        else {

            // We must loop all time markers to ensure we get proper null values
            // at time markers where the time series is missing a value
            Iterator<TimeSeriesTimestamp> iTimeMark = timeSeriesCollection.getTimeMarkerIterator();//= getDataSet().keySet().iterator();

            // Loop all time markers of the given time series collection
            while (iTimeMark.hasNext()) {
                // Get the time marker
                TimeSeriesTimestamp timeMark = iTimeMark.next();

                // Extract the data points for ALL time series for this time marker (each cell in the array represents one time series - so e.g. 3 cells = 3 time series)
                //TimeSeriesDataPoint[] timeMarkData = timeSeriesCollection.getDataPointsForTimeMarker(timeMark); 

                try {
                    // Get the data point for the particular time series that we're interested in
                    //TimeSeriesDataPoint dataPoint = timeSeriesClone.removeDataPointForTimeMarker(timeMark);
                    TimeSeriesDataPoint dataPoint = timeSeries.getDataPointForTimeMarker(timeMark);

                    if (!errorBarValues) {
                        TimeSeriesTimestamp timestamp = dataPoint.getTimestamp();
                        if (timestamp.isMorePreciseThan(TimeSeriesTimestamp.TYPE_YEAR) && !timestamp.isLiteralType()) {
                            //s += "[Date.UTC(" + timestamp.getUTCDate() + "), " + dataPoint.getValue() + "]";
                            s += "[" + getDateUTC(timestamp) + ", " + dataPoint.getValue() + "]";
                            //s += "[" + getDateUTC(timestamp) + ", " + dataPoint.getValue("#.#####################", NUMBER_FORMAT_LOCALE) + "]";
                        } else {
                            if (dataPoint.getPointCount() == 5) {
                                s += "[" + dataPoint.getAllValues("#.#####################", NUMBER_FORMAT_LOCALE) + "]";
                            } else {
                                s += dataPoint.getValue();
                                //s += dataPoint.getValue("#.#####################", NUMBER_FORMAT_LOCALE);
                            }
                        }
                    } else {
                        String hiLoStr = dataPoint.getHighLow("#.######################", ",", NUMBER_FORMAT_LOCALE);
                        if (hiLoStr != null) {
                            s += "[" + hiLoStr + "]";
                        } else {
                            s += "[null,null]";
                        }
                    }
                    if (iTimeMark.hasNext()) {
                        s += ", ";
                    }
                } catch (Exception e) {
                    // No data for our particular time series at this time marker 
                    if (!this.containsDateSeries()) {
                        s += "null";
                        if (iTimeMark.hasNext()) {
                            s += ", ";
                        }
                    } else {
                        // xAxis has type:datetime => just skip this one
                    }
                }
            }
        }
        long b = System.currentTimeMillis();
        //System.out.println(" done (" + (b-a) + "ms).");
        
        return s;
    }
    
    // Old version
    /*private String getValuesForTimeSeries(TimeSeriesCollection timeSeriesCollection, int timeSeriesIndex, boolean errorBarValues) {
        System.out.print("Generating series data ...");
        long a = System.currentTimeMillis();
        
        String s = "";
        // We must loop all time markers to ensure we get proper null values
        // at time markers where the time series is missing a value
        Iterator<TimeSeriesTimestamp> iTimeMark = timeSeriesCollection.getTimeMarkerIterator();//= getDataSet().keySet().iterator();
        
        // Loop all time markers of the given time series collection
        while (iTimeMark.hasNext()) {
            // Get the time marker
            TimeSeriesTimestamp timeMark = iTimeMark.next();
            // Extract the data points for ALL time series for this time marker (each cell in the array represents one time series - so e.g. 3 cells = 3 time series)
            TimeSeriesDataPoint[] timeMarkData = timeSeriesCollection.getDataPointsForTimeMarker(timeMark); 

            try {
                // Get the data point for the particular time series that we're interested in
                TimeSeriesDataPoint dataPoint = timeMarkData[timeSeriesIndex];
                
                if (!errorBarValues) {
                    TimeSeriesTimestamp timestamp = dataPoint.getTimestamp();
                    if (timestamp.isMorePreciseThan(TimeSeriesTimestamp.TYPE_YEAR) && !timestamp.isLiteralType()) {
                        s += "[Date.UTC(" + timestamp.getUTCDate() + "), " + dataPoint.getValue() + "]";
                        //s += "[" + getDateUTC(timestamp) + ", " + dataPoint.getValue() + "]";
                        //s += "[" + getDateUTC(timestamp) + ", " + dataPoint.getValue("#.#####################", NUMBER_FORMAT_LOCALE) + "]";
                    } else {
                        if (dataPoint.getPointCount() == 5) {
                            s += "[" + dataPoint.getAllValues("#.#####################", NUMBER_FORMAT_LOCALE) + "]";
                        } else {
                            s += dataPoint.getValue();
                            //s += dataPoint.getValue("#.#####################", NUMBER_FORMAT_LOCALE);
                        }
                    }
                } 
                else {
                    String hiLoStr = dataPoint.getHighLow("#.######################", ",", NUMBER_FORMAT_LOCALE);
                    if (hiLoStr != null) {
                        s += "[" + hiLoStr + "]";
                    } else {
                        s += "[null,null]";
                    }
                }
            } catch (Exception e) {
                // No data for our particular time series at this time marker 
                s += "null";
            }
            
            if (iTimeMark.hasNext())
                s += ", ";
        }
        
        long b = System.currentTimeMillis();
        System.out.println(" done (" + (b-a) + "ms).");
        
        return s;
    }*/
    
    /**
     * Converts the given timestamp to a "Date.UTC(2009,2,18)" type string.
     * <p>
     * This method is costly, as a calendar instance is used as a foundation.
     * 
     * @param ts The timestamp to convert.
     * @return A "Date.UTC(2009,2,18)" type string that represents the given timestamp.
     */
    private String getDateUTC(TimeSeriesTimestamp ts) {
        Calendar tempCal = new GregorianCalendar();
        tempCal.setTime(ts.getTime());
        return "Date.UTC(" + tempCal.get(Calendar.YEAR) + "," + tempCal.get(Calendar.MONTH) + "," + tempCal.get(Calendar.DATE) + ")";
        
    }
    
    /**
     * Get a Highcharts-munchable HTML table with all time series data.
     * 
     * @return A Highcharts-munchable HTML table.
     */
    public String getHtmlTable() {
        String s = "";
        if (!timeSeriesColl.hasAccuracyCompatibleTimeSeries()) {
            s += "\n<!-- Error: Parameter has multiple time series, with incompatible datetime accuracy levels. Table will probably not be Highcharts-munchable. -->\n";
        }
        
        s += "<table id=\"" + getId() + "-data\" class=\"wcag-off-screen\">\n";
        s += "<caption>" + timeSeriesColl.getTitle() + "</caption>\n";
        
        try {
            s += getHtmlTableRows( timeSeriesColl );
        } catch (Exception e) {
            //e.printStackTrace();
            if (LOG.isErrorEnabled()) {
                LOG.error("Creating Highcharts-munchable table from time series collection '" + timeSeriesColl.getURL() + "' failed.", e);
            }
        }
        
        s += "</table>";
        return s;
    }
    
    /**
     * Gets the ID for this chart.
     * <p>
     * The ID was set during creation of this instance, and is either
     * <ol>
     * <li>a manually constructed one</li>
     * <li>a generated one (based on a time series collection's title)</li>
     * <li>a clone of the MOSJ parameter's ID</li>
     * </ol>
     * 
     * @return The ID for this chart.
     */
    public String getId() { return id; }
    
    /**
     * Translates the given time series collection to table rows containing the
     * data.
     * 
     * @param tsc The time series collection.
     * @return Html table rows containing the data in the given time series collection.
     */
    protected String getHtmlTableRows(TimeSeriesCollection tsc) {
        String s = "";
        try {
            List<TimeSeries> timeSeriesList = tsc.getTimeSeries();
            if (!timeSeriesList.isEmpty()) {
                Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();

                s += "<thead>\n<tr><th></th>";
                while (iTimeSeries.hasNext()) {
                    TimeSeries ts = iTimeSeries.next();
                    String tsTitle = ts.getLabel();
                    //String tsTitle = tsc.getTitleForTimeSeries(ts);
                    s += "<th>" + tsTitle + "</th>";
                }
                s += "</tr>\n</thead>\n";
                s += "<tbody>\n";
                
                Iterator<TimeSeriesTimestamp> iTimeMark = tsc.getTimeMarkerIterator();

                while (iTimeMark.hasNext()) {
                    s += "<tr>";
                    TimeSeriesTimestamp timeMarker = iTimeMark.next();
                    s += "<th><span class=\"hs-time-marker\">" + timeMarker + "</span></th>"; // The span is vital for Highslide (but not the span's class)
                    TimeSeriesDataPoint[] timeMarkerData = tsc.getDataPointsForTimeMarker(timeMarker); // Get the array of data points for this time marker

                    for (int i = 0; i < timeMarkerData.length; i++) {
                        s += "<td>";
                        try {
                            TimeSeriesDataPoint dp = timeMarkerData[i];
                            s += dp.getValue("#.#####################");
                        } catch (Exception ee) {
                            //s += "null"; // No, just leave empty
                            // ToDo: Log this ?
                            //s += "<!-- Error: " + ee.getMessage() + " -->";
                        }
                        s += "</td>";
                    }

                    s += "</tr>\n";
                }
                s += "</tbody>\n";
            }
        } catch (Exception e) {
            //e.printStackTrace();
            if (LOG.isErrorEnabled()) {
                LOG.error("Error creating html table for time series collection '" + tsc.getTitle() + "'.", e);
            }
            s += "\n<!-- Error: " + e.getMessage() + " -->\n";
        }
        return s;
    }
    
    /**
     * Gets the categories (time markers) for the time series collection, 
     * comma-separated.
     * 
     * @param tsc The time series collection.
     * @return The categories (time markers) for the time series collection, comma-separated.
     */
    protected String makeCategoriesString(TimeSeriesCollection tsc) {
        String s = "";
        try {
            Iterator<TimeSeriesTimestamp> itr = tsc.getTimeMarkerIterator();
            while (itr.hasNext()) {
                s += "'" + itr.next() + "'";
                if (itr.hasNext()) s += ", ";
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("Error creating Highcharts config part 'categories' for time series '" + tsc.getTitle() + "'.", e);
        }
        return s;
    }
}
