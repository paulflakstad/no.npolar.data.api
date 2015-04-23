package no.npolar.data.api.mosj;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import no.npolar.data.api.TimeSeriesDataUnit;
import no.npolar.data.api.TimeSeries;
import no.npolar.data.api.TimeSeriesCollection;
import no.npolar.data.api.TimeSeriesDataPoint;
import org.opencms.json.JSONObject;
import org.opencms.json.JSONException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencms.json.JSONArray;

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
    /** Override key: Number of lines to spread labels over (applies to horizontal lines). {@link http://api.highcharts.com/highcharts#xAxis.labels.staggerLines } */
    public static final String OVERRIDE_KEY_MAX_STAGGER_LINES_INT = "maxStaggerLines";
    /** Override key: Hide point markers in the series? {@link http://api.highcharts.com/highcharts#plotOptions.series.marker.enabled } */
    public static final String OVERRIDE_KEY_HIDE_MARKERS_BOOL = "dots";
    /** The default series type ("line"). */
    public static final String DEFAULT_SERIES_TYPE = "line";
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
            
            TimeSeriesCollection timeSeriesCollection = mosjParameter.getTimeSeriesCollection();
            List<TimeSeriesDataUnit> units = timeSeriesCollection.getUnits();
            Iterator<TimeSeriesDataUnit> iUnits = units.iterator();
            
            String type = "zoomType: 'x'";
            int step = timeSeriesCollection.getTimeMarkersCount() / 8;
            int maxStaggerLines = -1;
            int xLabelRotation = -1;
            boolean hideMarkers = false;
            
            try { type = "type: '" + overrides.getString(OVERRIDE_KEY_TYPE_STRING) + "'"; } catch(Exception ee) {}
            try { step = Integer.valueOf(overrides.getString(OVERRIDE_KEY_X_AXIS_LABEL_STEP_INT)); } catch(Exception ee) {}
            try { maxStaggerLines = Integer.valueOf(overrides.getString(OVERRIDE_KEY_MAX_STAGGER_LINES_INT)); } catch(Exception ee) {}
            try { xLabelRotation = Integer.valueOf(overrides.getString(OVERRIDE_KEY_X_AXIS_LABEL_ROTATION_INT)); } catch(Exception ee) {}
            try { hideMarkers = !Boolean.valueOf(overrides.getString(OVERRIDE_KEY_HIDE_MARKERS_BOOL)); } catch(Exception ee) {}
            
            s += "{ ";
            // Chart type
            s += "\nchart: { ";
            s += type;
            s += "}, ";
            
            s += "\ntitle: { text: '" + mosjParameter.getTitle() + "' }, ";
            
            if (hideMarkers) {
                s += "\nplotOptions: { ";
                    s += "\nseries: { ";
                        s += "\nmarker: { enabled: false }";
                        // If there is only 1 time series, disable "click to hide"
                        if (timeSeriesCollection.getTimeSeries().size() == 1) {
                            s += ",\npoint: {";
                                s += "\nevents: {";
                                    s += "\nlegendItemClick: function() { return false; }";
                                s += "\n}";
                            s += "\n}";
                        }
                    s += "\n}";
                s += "\n}, ";
            }
            
            // The x axis
            s += "\nxAxis: [{ ";
                    // ToDo: Default should be datetime, not categories - .... or SHOULD IT???? http://stackoverflow.com/questions/23816474/highcharts-xaxis-yearly-data
                    s += "\ncategories: [" + makeCategoriesString(timeSeriesCollection) + "], ";
                    s += "\nlabels: {";
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
            s += "\nyAxis: [";
                    int i = 0;
                    while (iUnits.hasNext() && i < 2) {
                        TimeSeriesDataUnit unit = iUnits.next();
                        s += "\n{ ";
                            s += "\nlabels: {"; 
                                    s += "\nformat: '{value} " + unit.getShortForm() + "', ";
                                    s += "\nstyle: { ";
                                        s += "\ncolor: Highcharts.getOptions().colors[" + i + "] ";
                                    s += "\n}";
                            s += "\n}, ";
                            s += "\ntitle: {";
                                    s += "\ntext: '" + unit.getLongForm() + "',";
                                    s += "\nstyle: { ";
                                        s += "color: Highcharts.getOptions().colors[" + i + "] ";
                                    s += "}";
                            s += "}";
                            
                            if (i == 1) {
                                s += ", \nopposite: true";
                            }
                        s += "\n}";
                        
                        if (++i < 2 && iUnits.hasNext()) {
                            s += ",";
                        }
                    }                    
            s += "], ";
            
            s += "\ntooltip: { ";
                s += "shared: true";
            s += "}, ";
            
            // The actual data
            s += "\nseries: [";
                s += getSeriesDetails(timeSeriesCollection, overrides);
            s += "]";
            
            s += "}";
            
            
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
        
        String s = "";
        List<TimeSeries> timeSeriesList = timeSeriesCollection.getTimeSeries();
        if (!timeSeriesList.isEmpty()) {
            Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
            int timeSeriesIndex = 0;
            while (iTimeSeries.hasNext()) {
                TimeSeries timeSeries = iTimeSeries.next();
                
                // Defaults
                String seriesType = DEFAULT_SERIES_TYPE;
                String seriesName = timeSeries.getLabel();// timeSeriesCollection.getTitleForTimeSeries(timeSeries);
                boolean hideMarkers = false;
                // Customization
                JSONObject tsCustomization = getTimeSeriesOverrides(timeSeries, overrides);
                
                // Handle case: General overrides (e.g. a general "type" override)
                if (overrides != null) { 
                    try { seriesType = overrides.getString(OVERRIDE_KEY_TYPE_STRING); } catch (Exception e) { }
                } 
                // Handle case: Specific overrides for this individual time series
                if (tsCustomization != null) {
                //else {
                    try { seriesType = tsCustomization.getString(OVERRIDE_KEY_TYPE_STRING); } catch (Exception e) { }
                    try { seriesName = tsCustomization.getString(OVERRIDE_KEY_NAME_STRING); } catch (Exception e) { }
                    try { hideMarkers = !Boolean.valueOf(overrides.getString(OVERRIDE_KEY_HIDE_MARKERS_BOOL)); } catch(Exception ee) {}
                }
                
                
                s += "\n{";
                try {
                    //int yAxis = units.indexOf( new MOSJDataUnit(timeSeries.getUnit(), timeSeries.getUnitVerbose(displayLocale)) );
                    int yAxis = timeSeriesCollection.getUnits().indexOf(timeSeries.getUnit());
                            
                    s += "\nname: '" + seriesName + "',";
                    s += "\ntype: '" + seriesType + "',";
                    s += "\nyAxis: " + yAxis + ",";
                    if (hideMarkers) {
                        s += "\nmarker: { enabled: false },";
                    }
                    s += "\ndata: [" + getValuesForTimeSeries(timeSeriesCollection, timeSeriesIndex, false) + "],";
                    s += "\ntooltip: {"
                                + "\npointFormat: '<span style=\"font-weight: bold; color: {series.color}\">{series.name}</span>: <b>{point.y} " 
                                        + timeSeries.getUnit().getShortForm() + "</b>" + (timeSeries.isErrorBarSeries() ? " " : "<br/>") + "'"
                            + "\n}";
                    
                    if (timeSeries.isErrorBarSeries()) {
                        s += "},\n{";
                        //s += "\nname: '" + timeSeriesCollection.getTitleForTimeSeries(timeSeries) + " error',";
                        s += "\nname: '" + timeSeries.getLabel() + " error',";
                        s += "\ntype: '" + "errorbar" + "',";
                    s += "\nyAxis: " + yAxis + ",";
                        s += "\ndata: [" + getValuesForTimeSeries(timeSeriesCollection, timeSeriesIndex, true) + "],";
                        s += "\ntooltip: {"
                                    + "\npointFormat: '(error range: {point.low}-{point.high} " + timeSeries.getUnit().getShortForm() + ")<br/>'"
                                + "\n}";
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
    private String getValuesForTimeSeries(TimeSeriesCollection timeSeriesCollection, int timeSeriesIndex, boolean highLowValues) {
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
                
                if (!highLowValues) {
                    s += dataPoint.getVal("#.#####################", NUMBER_FORMAT_LOCALE);
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
                s += "[null,null]";
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
