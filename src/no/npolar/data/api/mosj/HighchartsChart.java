package no.npolar.data.api.mosj;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import no.npolar.data.api.MOSJService;
import no.npolar.data.api.TimeSeriesDataUnit;
import no.npolar.data.api.TimeSeries;
import no.npolar.data.api.TimeSeriesCollection;
import no.npolar.data.api.TimeSeriesDataPoint;
import org.opencms.json.JSONObject;
import org.opencms.json.JSONException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencms.json.JSONArray;
import org.opencms.util.CmsStringUtil;

/**
 * Adds support for generating Highcharts {@link http://highcharts.com} charts.
 * <p>
 * The chart is generated from a MOSJ parameter (which has related time series), 
 * possibly with override settings. Overrides may be global or specific to 
 * individual time series.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class HighchartsChart {
    /** The MOSJ parameter that is the basis for this chart. */
    private MOSJParameter mosjParameter = null;
    /** Override settings - global and/or specific to individual time series. */
    private JSONObject overrides = null;
    /** The preferred language. */
    private Locale displayLocale = null;
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(HighchartsChart.class);
    
    /** Override key: Time series ID (when overriding individual time series). */
    public static final String OVERRIDE_KEY_SERIES_ID = "series";
    /** Override key: Chart / series type. {@link http://api.highcharts.com/highcharts#series.type } */
    public static final String OVERRIDE_KEY_TYPE_STRING = "type";
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
    /** Override key: Manual y-axis placement. */
    //public static final String OVERRIDE_KEY_Y_AXIS = "yAxis";
    /** The default series type name ("line"). */
    public static final String DEFAULT_SERIES_TYPE = "line";
    /** The default series stacking setting ("normal"). */
    public static final String DEFAULT_STACKING = "normal";
    /** The series type name for box plot charts. */
    public static final String SERIES_TYPE_BOX_PLOT = "boxplot";
    /** The number formatting pattern to use. */
    public static final String NUMBER_FORMAT = TimeSeriesDataPoint.DEFAULT_NUMBER_FORMAT;
    /** The number formatting locale (English, because we need 3.14, not 3,14). */
    public static final Locale NUMBER_FORMAT_LOCALE = Locale.forLanguageTag("en");
    
    /**
     * Creates a new instance based on the given MOSJ parameter and overrides.
     * 
     * @param mp The MOSJ parameter. Mandatory (not <code>null</code>).
     * @param overrides The overrides. Can be <code>null</code>.
     */
    public HighchartsChart(MOSJParameter mp, JSONObject overrides) {
        this.mosjParameter = mp;
        this.overrides = overrides;
        this.displayLocale = mp.getDisplayLocale();
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
                try { ts.setOrderIndex(tsOverrides.getInt(OVERRIDE_KEY_ORDER_INDEX)); manuallyOrdered = true; } catch (Exception e) {}
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
     * @param timeSeriesCollection
     * @return The given time series collection, updated.
     */
    protected TimeSeriesCollection fillTimeMarkerGaps(TimeSeriesCollection timeSeriesCollection) {
        try {
            int year = Integer.MIN_VALUE;
            int prevYear = Integer.MAX_VALUE;
            
            // First, find the smallest interval between two points
            int smallestInterval = Integer.MAX_VALUE;
            Iterator<String> itr = timeSeriesCollection.getTimeMarkerIterator();
            while (itr.hasNext()) {
                year = Integer.valueOf(itr.next());
                if (prevYear != Integer.MAX_VALUE) {
                    if (year - prevYear < smallestInterval) {
                        smallestInterval = year - prevYear;
                    }
                }
                prevYear = year;
            }
            
            year = Integer.MIN_VALUE;
            prevYear = Integer.MAX_VALUE;
            Map<String, TimeSeriesDataPoint[]> dataToAdd = new HashMap<String, TimeSeriesDataPoint[]>();
            
            itr = timeSeriesCollection.getTimeMarkerIterator();
            while (itr.hasNext()) {
                String yearStr = itr.next();
                System.out.println("Evaluating '" + yearStr + "'");
                year = Integer.valueOf(yearStr);
                if (prevYear != Integer.MAX_VALUE) {
                    while (year - prevYear > smallestInterval) {
                        prevYear = prevYear + smallestInterval;
                        System.out.println("Adding missing year: " + prevYear);
                        dataToAdd.put(String.valueOf(prevYear), null);
                    }
                }
                prevYear = year;
            }
            timeSeriesCollection.addDataPoints(dataToAdd);
        } catch (Exception e) {
            // Non-yearly time marker format
            System.out.println("CRASH! Error was: " + e.getMessage());
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
            TimeSeriesCollection timeSeriesCollection = resolveTimeSeriesOrdering(mosjParameter.getTimeSeriesCollection());
            
            
            List<TimeSeriesDataUnit> units = timeSeriesCollection.getUnits();
            //System.out.println("units: " + units);
            Iterator<TimeSeriesDataUnit> iUnits = units.iterator();
            
            // Enforce equal steps?
            boolean xAxisEnforceEqualSteps = false;
            try { xAxisEnforceEqualSteps = Boolean.valueOf(overrides.getString(OVERRIDE_KEY_X_AXIS_ENFORCE_EQUAL_STEPS)); } catch(Exception ee) {}
            if (xAxisEnforceEqualSteps) {
                timeSeriesCollection = fillTimeMarkerGaps(timeSeriesCollection);
            }
            
            String type = "zoomType: 'x'";
            int step = timeSeriesCollection.getTimeMarkersCount() / 8;
            int maxStaggerLines = -1;
            int xLabelRotation = -1;
            boolean hideMarkers = false;
            String stacking = null;
            //boolean errorBarsAlwaysOn = false;
            boolean xAxisOnTop = false;
            
            try { type = "type: '" + overrides.getString(OVERRIDE_KEY_TYPE_STRING) + "'"; } catch(Exception ee) {}
            try { step = Integer.valueOf(overrides.getString(OVERRIDE_KEY_X_AXIS_LABEL_STEP_INT)); } catch(Exception ee) {}
            try { maxStaggerLines = Integer.valueOf(overrides.getString(OVERRIDE_KEY_MAX_STAGGER_LINES_INT)); } catch(Exception ee) {}
            try { xLabelRotation = Integer.valueOf(overrides.getString(OVERRIDE_KEY_X_AXIS_LABEL_ROTATION_INT)); } catch(Exception ee) {}
            try { hideMarkers = !Boolean.valueOf(overrides.getString(OVERRIDE_KEY_HIDE_MARKERS_BOOL)); } catch(Exception ee) {}
            try { stacking = overrides.getString(OVERRIDE_KEY_STACKING); } catch (Exception ee) {}
            //try { errorBarsAlwaysOn = Boolean.valueOf(overrides.getString(OVERRIDE_KEY_ERROR_TOGGLER)); } catch(Exception ee) {}
            try { xAxisOnTop = Boolean.valueOf(overrides.getString(OVERRIDE_KEY_X_AXIS_ON_TOP)); } catch(Exception ee) {}
            
            
            s += "{ ";
            // Chart type
            s += "\nchart: { ";
            s += type;
            s += "}, ";
            
            s += "\ntitle: { text: '" + mosjParameter.getTitle().replaceAll("'", "\\\\'") + "' }, ";
            s += "\nurl: '" + mosjParameter.getURL(new MOSJService(displayLocale, true)) + "', ";
            
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
                                if (timeSeriesCollection.hasErrorBarSeries()) {
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
                                else if (timeSeriesCollection.getTimeSeries().size() == 1) {
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
            s += "\ncredits: { ";
                s += "\nenabled: false";
            s += "\n}, ";
                    
            
            // The x axis
            s += "\nxAxis: [{ ";
                    // ToDo: Default should be datetime, not categories - .... or SHOULD IT???? http://stackoverflow.com/questions/23816474/highcharts-xaxis-yearly-data
                    s += "\ncategories: [" + makeCategoriesString(timeSeriesCollection) + "], ";
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
                        
                        List<TimeSeries> axisSeriesList = timeSeriesCollection.getTimeSeriesWithUnit(unit); // Get the time series for this axis
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
                s += "shared: true";
            s += "\n}, ";
            
            // The actual data
            s += "\nseries: [ ";
                s += getSeriesDetails(timeSeriesCollection, overrides);
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
                
                // Defaults
                String seriesType = timeSeries.getValuesPerDataPoint() >= 5 ? SERIES_TYPE_BOX_PLOT : DEFAULT_SERIES_TYPE;
                String seriesName = timeSeries.getLabel();// timeSeriesCollection.getTitleForTimeSeries(timeSeries);
                boolean hideMarkers = false;
                boolean errorBarsAlwaysOn = false;
                //boolean isTrendLine = false;
                //boolean connectNulls = false;
                
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
                    if (tsCustomization.has(OVERRIDE_KEY_TREND_LINE)) {
                        try { 
                            if (Boolean.valueOf(tsCustomization.getString(OVERRIDE_KEY_TREND_LINE))) {
                                timeSeries.flagAsTrendLine();
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
                    //int yAxis = units.indexOf( new MOSJDataUnit(timeSeries.getUnit(), timeSeries.getUnitVerbose(displayLocale)) );
                    int yAxis = timeSeriesCollection.getUnits().indexOf(timeSeries.getUnit());
                    
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
                    s += "\ndata: [ " + getValuesForTimeSeries(timeSeriesCollection, timeSeriesIndex, false) + " ], ";
                    if (timeSeries.getChartLineThickness() != null) {
                        s += "\nlineWidth: " + timeSeries.getChartLineThickness() + ",";
                    }
                    if (timeSeries.getValuesPerDataPoint() == 5) {
                        s += "\ntooltip: { "
                                + "\npointFormat: '" 
                                                + labels.getString(Labels.CHART_MAX_0) + ": {point.high} " + timeSeries.getUnit().getShortForm() + "<br/>"
                                                + labels.getString(Labels.CHART_HIGH_0) + ": {point.q3} " + timeSeries.getUnit().getShortForm() + "<br/>"
                                                + "<span style=\"font-weight:bold;\">"+ labels.getString(Labels.CHART_MEDIAN_0) + ": {point.median} " + timeSeries.getUnit().getShortForm() + "</span><br/>"
                                                + labels.getString(Labels.CHART_LOW_0) + ": {point.q1} " + timeSeries.getUnit().getShortForm() + "<br/>"
                                                + labels.getString(Labels.CHART_LOW_0) + ": {point.low} " + timeSeries.getUnit().getShortForm() + "<br/>"
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
                        String errorBarSeriesName = CmsStringUtil.escapeJavaScript(seriesName) + " " + labels.getString(Labels.CHART_ERROR_0).toLowerCase();
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
                        s += "\ndata: [" + getValuesForTimeSeries(timeSeriesCollection, timeSeriesIndex, true) + "],";
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
     * Gets the values for a single time series, identified by the given time 
     * series index. 
     * <p>
     * The index corresponds to its placement in the 
     * {@link TimeSeriesCollection#timeSeriesList }.)
     * <p>
     * The returned values string is "aware" of other time series in the given 
     * collection, and will contain <code>null</code> values for any time 
     * markers where the specified time series lacks a value.
     * 
     * @param timeSeriesIndex The index identifying the time series. (Corresponds to its placement in {@link TimeSeriesCollection#timeSeriesList}.)
     * @return The values for the single time series, with <code>null</code> values where necessary.
     */
    private String getValuesForTimeSeries(TimeSeriesCollection timeSeriesCollection, int timeSeriesIndex, boolean errorBarValues) {
        String s = "";
        // We must loop all time markers to ensure we get proper null values
        // at time markers where the time series is missing a value
        Iterator<String> iTimeMark = timeSeriesCollection.getTimeMarkerIterator();//= getDataSet().keySet().iterator();
        
        // Loop all time markers of the given time series collection
        while (iTimeMark.hasNext()) {
            // Get the time marker
            String timeMark = iTimeMark.next();
            // Extract the data points for ALL time series for this time marker (each cell in the array represents one time series - so e.g. 3 cells = 3 time series)
            TimeSeriesDataPoint[] timeMarkData = timeSeriesCollection.getDataPointsForTimeMarker(timeMark); 

            try {
                // Get the data point for the particular time series that we're interested in
                TimeSeriesDataPoint dataPoint = timeMarkData[timeSeriesIndex];
                
                if (!errorBarValues) {
                    if (dataPoint.getPointCount() == 5) {
                        s += "[" + dataPoint.getAllValues("#.#####################", NUMBER_FORMAT_LOCALE) + "]";
                    } else {
                        s += dataPoint.getVal("#.#####################", NUMBER_FORMAT_LOCALE);
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
        return s;
    }
    
    
    
    /**
     * Get a Highcharts-munchable HTML table with all time series data.
     * 
     * @return A Highcharts-munchable HTML table.
     */
    public String getHtmlTable() {
        String s = "";
        if (!mosjParameter.hasAccuracyCompatibleTimeSeries()) {
            s += "\n<!-- Error: Parameter has multiple time series, with incompatible datetime accuracy levels. Table will probably not be Highcharts-munchable. -->\n";
        }
        
        s += "<table id=\"" + mosjParameter.getId() + "-data\" class=\"wcag-off-screen\">\n";
        s += "<caption>" + mosjParameter.getTitle() + "</caption>\n";
        
        try {
            s += getHtmlTableRows( this.mosjParameter.getTimeSeriesCollection() );
        } catch (Exception e) {
            //e.printStackTrace();
            if (LOG.isErrorEnabled()) {
                LOG.error("Creating Highcharts-munchable table from MOSJ parameter '" + mosjParameter.getId() + "' failed.", e);
            }
        }
        
        s += "</table>";
        return s;
    }
    
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
                
                Iterator<String> iTimeMark = tsc.getTimeMarkerIterator();

                while (iTimeMark.hasNext()) {
                    s += "<tr>";
                    String timeMarker = iTimeMark.next();
                    s += "<th><span class=\"hs-time-marker\">" + timeMarker + "</span></th>"; // The span is vital for Highslide (but not the span's class)
                    TimeSeriesDataPoint[] timeMarkerData = tsc.getDataPointsForTimeMarker(timeMarker); // Get the array of data points for this time marker

                    for (int i = 0; i < timeMarkerData.length; i++) {
                        s += "<td>";
                        try {
                            TimeSeriesDataPoint dp = timeMarkerData[i];
                            s += dp.getVal("#.#####################");
                        } catch (Exception ee) {
                            //s += "null"; // No, just leave empty
                            // ToDo: Log this
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
            // ToDo: Log this
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
            Iterator<String> itr = tsc.getTimeMarkerIterator();
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
