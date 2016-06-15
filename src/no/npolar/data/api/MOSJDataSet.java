package no.npolar.data.api;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Map;
import no.npolar.data.api.mosj.MOSJDataPoint;
import no.npolar.data.api.mosj.MOSJParameter;
import no.npolar.data.api.mosj.MOSJTimeSeries;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A "MOSJ data set" is basically a collection of MOSJ time series.
 * <p>
 * When a {@link MOSJParameter} comprises multiple time series, we typically 
 * need these time series to be "aware" of each other, due to the fact that they
 * will be visualized in a single chart.
 * <p>
 * This is especially true if the basis for the chart should be a table.
 * 
 * @deprecated Use {@link no.npolar.data.api.TimeSeriesCollection} instead.
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class MOSJDataSet {
    private List<MOSJTimeSeries> timeSeriesList = null;
    private Map<String, MOSJDataPoint[]> dataSet = null;
    public static final Comparator<MOSJTimeSeries> SORT_BIGGEST_FIRST = null;
    private Locale displayLocale = null;
    private String parameterTitle = null;
    private List<MOSJDataUnit> units = null;
    public static final String DEFAULT_HC_SERIES_TYPE = "line";
    protected int numTimeMarkers = 0;
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(MOSJDataSet.class);
            
    /**
     * Creates a new MOSJ data set.
     * 
     * @param displayLocale
     * @param tss
     * @throws JSONException 
     */
    public MOSJDataSet(Locale displayLocale, List<MOSJTimeSeries> tss) throws JSONException {
        timeSeriesList = new ArrayList<MOSJTimeSeries>();
        dataSet = new TreeMap<String, MOSJDataPoint[]>(); // TreeMap => natively orders by keys
        units = new ArrayList<MOSJDataUnit>();
        this.displayLocale = displayLocale;
        this.setTimeSeries(tss);
    }
    /**
     * Creates a new MOSJ data set.
     * 
     * @param displayLocale
     * @param tss
     * @param parameterTitle
     * @throws JSONException 
     */
    public MOSJDataSet(Locale displayLocale, List<MOSJTimeSeries> tss, String parameterTitle) throws JSONException {
        this(displayLocale, tss);
        this.parameterTitle = parameterTitle;
    }
    
    /**
     * Gets the units applicable for the data in this set.
     * 
     * @return The units applicable for the data in this set.
     */
    public List<MOSJDataUnit> getUnits() {
        return units;
    }
    
    /**
     * Gets the title of a time series, without the parameter title part.
     * 
     * @param ts The time series to get the title of.
     * @return The title of the given time series, without the parameter title part.
     */
    public String getTitleForTimeSeries(MOSJTimeSeries ts) {
        String tsTitle = null;
        try {
            tsTitle = ts.getTitle(displayLocale);
            if (parameterTitle != null) {
                tsTitle = tsTitle.replace(" / " + parameterTitle, "");
            }
        } catch (Exception e) {
            // Log this
            if (LOG.isErrorEnabled()) {
                LOG.error("Error getting title for time series '" + ts.getID() + "'. Possibly missing title?", e);
            }
        }
        return tsTitle;
    }
    
    protected JSONObject getTimeSeriesOverrides(MOSJTimeSeries ts, JSONObject overrides) {
        if (overrides == null || !overrides.has("series")) // "series" identifies a time series that's being overridden
            return null;
        
        try {
            JSONArray series = overrides.getJSONArray("series");
            for (int i = 0; i < series.length(); i++) {
                JSONObject seriesObj = series.getJSONObject(i);
                try {
                    if (seriesObj.get("id").equals(ts.getID()))
                        return seriesObj;
                } catch (Exception ee) {
                    continue;
                }
            }
        } catch (Exception e) {
            // Log this
            if (LOG.isErrorEnabled()) {
                LOG.error("Critical error during override processing for time series '" + ts.getID() + "'.", e);
            }
        }
        
        return null;
    }
    
    /**
     * Gets this data set as "Highcharts-ready" javascript code.
     * 
     * @param overrides
     * @return 
     */
    public String getAsHighchartsSeries(JSONObject overrides) {
        
        try { if (overrides == null) overrides = new JSONObject(); } catch (Exception e) {} // 
        
        String s = "";
        if (!timeSeriesList.isEmpty()) {
            Iterator<MOSJTimeSeries> iTimeSeries = timeSeriesList.iterator();
            int timeSeriesIndex = 0;
            while (iTimeSeries.hasNext()) {
                MOSJTimeSeries ts = iTimeSeries.next();
                
                // Defaults
                String seriesType = DEFAULT_HC_SERIES_TYPE;
                String seriesName = getTitleForTimeSeries(ts);
                boolean hideMarkers = false;
                // Customization
                JSONObject tsCustomization = getTimeSeriesOverrides(ts, overrides);
                
                // Handle case: General overrides (e.g. a general "type" override)
                if (overrides != null) { 
                    try { seriesType = overrides.getString("type"); } catch (Exception e) { }
                } 
                // Handle case: Specific overrides for this individual time series
                else {
                    try { seriesType = tsCustomization.getString("type"); } catch (Exception e) { }
                    try { seriesName = tsCustomization.getString("name"); } catch (Exception e) { }
                    try { hideMarkers = !Boolean.valueOf(overrides.getString("dots")); } catch(Exception ee) {}
                }
                
                
                s += "\n{";
                try {
                    //int yAxis = units.indexOf( new MOSJDataUnit(ts.getUnit(), ts.getUnitVerbose(displayLocale)) );
                    int yAxis = units.indexOf(ts.getUnit());
                            
                    s += "\nname: '" + seriesName + "',";
                    s += "\ntype: '" + seriesType + "',";
                    s += "\nyAxis: " + yAxis + ",";
                    if (hideMarkers) {
                        s += "\nmarker: { enabled: false },";
                    }
                    s += "\ndata: [" + getValuesForTimeSeries(timeSeriesIndex, false) + "],";
                    s += "\ntooltip: {"
                                + "\npointFormat: '<span style=\"font-weight: bold; color: {series.color}\">{series.name}</span>: <b>{point.y} " 
                                        + ts.getUnit().getShortForm() + "</b>" + (ts.isErrorBarSeries() ? " " : "<br/>") + "'"
                            + "\n}";
                    
                    if (ts.isErrorBarSeries()) {
                        s += "},\n{";
                        s += "\nname: '" + getTitleForTimeSeries(ts) + " error',";
                        s += "\ntype: '" + "errorbar" + "',";
                    s += "\nyAxis: " + yAxis + ",";
                        s += "\ndata: [" + getValuesForTimeSeries(timeSeriesIndex, true) + "],";
                        s += "\ntooltip: {"
                                    + "\npointFormat: '(error range: {point.low}-{point.high} " + ts.getUnit().getShortForm() + ")<br/>'"
                                + "\n}";
                    }
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Error creating Highcharts-munchable javascript snippet for time series '" + ts.getID() +"'.", e);
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
     * Gets the values for the time series identified by the given time series
     * index.
     * <p>
     * The returned values string is "aware" of any other time series in this 
     * data set, and will contain a <code>null</code> value for any time markers 
     * where the specified time series lacks a value.
     * 
     * @param timeSeriesIndex The index identifying the time series. (This index is its location in {@link MOSJDataSet#timeSeriesList}.)
     * @return 
     */
    private String getValuesForTimeSeries(int timeSeriesIndex, boolean highLowValues) {
        String s = "";
        // We must loop all time markers to ensure we get proper null values
        // at time markers where the time series is missing a value
        Iterator<String> iTimeMark = this.dataSet.keySet().iterator();
        
        while (iTimeMark.hasNext()) {
            String timeMark = iTimeMark.next();
            MOSJDataPoint[] timeMarkData = this.dataSet.get(timeMark); // Get the array of data points for this time marker

            try {
                MOSJDataPoint dp = timeMarkData[timeSeriesIndex];
                if (!highLowValues) {
                    s += dp.getVal("#.#####################");
                } else {
                    String hiLoStr = dp.getHighLow("#.######################", ",");
                    if (hiLoStr != null) {
                        s += "[" + hiLoStr + "]";
                    } else {
                        s += "[null,null]";
                    }
                }
            } catch (Exception e) {
                s += "[null,null]";
            }
            
            if (iTimeMark.hasNext())
                s += ", ";
        }
        return s;
    }
    
    /**
     * Gets this data set as table rows.
     * @return This data set as table rows.
     */
    public String getTableRows() {
        String s = "";
        try {
            if (!timeSeriesList.isEmpty()) {
                Iterator<MOSJTimeSeries> iTimeSeries = timeSeriesList.iterator();

                s += "<thead>\n<tr><th></th>";
                while (iTimeSeries.hasNext()) {
                    MOSJTimeSeries ts = iTimeSeries.next();
                    String tsTitle = ts.getTitle(displayLocale);
                    if (parameterTitle != null) {
                        tsTitle = tsTitle.replace(" / " + parameterTitle, "");
                    }
                    s += "<th>" + tsTitle + "</th>";
                }
                s += "</tr>\n</thead>\n";
                s += "<tbody>\n";
                
                Iterator<String> iTimeMark = this.dataSet.keySet().iterator();

                while (iTimeMark.hasNext()) {
                    s += "<tr>";
                    String timeMark = iTimeMark.next();
                    s += "<th><span class=\"hs-time-marker\">" + timeMark + "</span></th>"; // The span is vital for Highslide (but not the span's class)
                    MOSJDataPoint[] timeMarkData = this.dataSet.get(timeMark); // Get the array of data points for this time marker

                    for (int i = 0; i < timeMarkData.length; i++) {
                        s += "<td>";
                        try {
                            MOSJDataPoint dp = timeMarkData[i];
                            s += dp.getVal("#.#####################");
                        } catch (Exception ee) {
                            //s += "null"; // No, just leave empty
                            // ToDo: Log this?
                            //s += "<!-- Error: " + ee.getMessage() + " -->";
                        }
                        s += "</td>";
                    }

                    s += "</tr>\n";
                }
                s += "</tbody>\n";
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error creating Highcharts-munchable table for parameter '" + this.parameterTitle + "'.", e);
            }
            s += "<!-- Error: " + e.getMessage() + " -->";
        }
        return s;
    }
    
    /**
     * Gets the keys (time markers) in this data set, comma-separated.
     * 
     * @return The keys in this data set, comma-separated.
     */
    public String getKeysCommaSeparated() {
        String s = "";
        try {
            Iterator<String> itr = this.dataSet.keySet().iterator();
            while (itr.hasNext()) {
                s += "'" + itr.next() + "'";
                if (itr.hasNext()) s += ", ";
            }
        } catch (Exception e) {
            LOG.error("Error creating Highcharts-munchable data set for parameter '" + this.parameterTitle + "'.", e);
        }
        return s;
    }
    
    /**
     * Gets the number of time markers in this data set.
     * 
     * @return The number of time markers in this data set.
     */
    public int getTimeMarkersCount() {
        return numTimeMarkers;
    }
    
    /**
     * Sets the time series. (Called by the constructor.)
     * 
     * @param tss
     * @return
     * @throws JSONException 
     */
    private void setTimeSeries(List<MOSJTimeSeries> tss) throws JSONException {
        timeSeriesList.clear();
        timeSeriesList.addAll(tss);
        
        Iterator<MOSJTimeSeries> iTimeSeries = timeSeriesList.iterator();
        
        int timeSeriesIndex = 0; // Keep track of this time series' own index (or, "column" in a table)
        while (iTimeSeries.hasNext()) {
            MOSJTimeSeries ts = iTimeSeries.next();
            
            // Add the unit to the list of units, if not already added
            //MOSJDataUnit tsDataUnit = new MOSJDataUnit(ts.getUnit(), ts.getUnitVerbose(displayLocale));
            
            if (!units.contains(ts.getUnit()))
                units.add(ts.getUnit());
            
            // Get all data points in the time series
            List<MOSJDataPoint> dps = ts.getDataPoints();
            
            // Update variable holding the number of time markers in this data set
            if (dps.size() > numTimeMarkers) {
                numTimeMarkers = dps.size();
            }
            
            // Loop data points
            Iterator<MOSJDataPoint> i = dps.iterator();
            while (i.hasNext()) {
                MOSJDataPoint dp = i.next();
                String dpKey = dp.getTimestampFormatted();
                if (!dataSet.containsKey(dpKey)) {
                    dataSet.put(dpKey, new MOSJDataPoint[timeSeriesList.size()]);
                } 
                dataSet.get(dpKey)[timeSeriesIndex] = dp;
            }
            timeSeriesIndex++;
        }
    }
}
