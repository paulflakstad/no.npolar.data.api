package no.npolar.data.api;

//import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
//import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
//import java.util.Map;
//import java.util.TreeMap;
import java.util.TreeSet;
//import java.util.Map;
import no.npolar.data.api.mosj.MOSJParameter;
//import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
//import org.opencms.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A time series collection is basically a container for one or more time 
 * series.
 * <p>
 * It also maps data to time markers, and keeps track of units.
 * <p>
 * When a {@link MOSJParameter} comprises multiple time series, we typically 
 * need these time series to be "aware" of each other, due to the fact that they
 * will be visualized in a single chart, or presented in a single table.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class TimeSeriesCollection {
    /** Holds all the time series in this collection. */
    private List<TimeSeries> timeSeriesList = null;
    //** Holds all data points. The keys are the time markers (timestamps, e.g. years or dates). The TreeMap natively orders by keys. */
    //private TreeMap<TimeSeriesTimestamp, TimeSeriesDataPoint[]> dataSet = null;
    //private TreeMap<String, TimeSeriesDataPoint[]> dataSet = null;
    //private TreeMap<TimeSeriesTimestamp, TimeSeries[]> allSeriesByTime = null;
    /**
     * Holds all time markers (timestamps).
     * <p>
     * The TreeSet type ensures natural ordering and no duplicates.
     */
    private TreeSet<TimeSeriesTimestamp> allTimestamps = null;
    //public static final Comparator<TimeSeries> SORT_BIGGEST_FIRST = null;
    
    /** Preferred locale to use when getting language-specific data. */
    private Locale displayLocale = null;
    /**
     * The title/name of this collection.
     * <p>
     * For MOSJ, this would be the title of the chart / "MOSJ parameter". 
     */
    private String title = null;
    /** 
     * The URL that points to this collection's Data Centre entry/entries.
     * <p>
     * For MOSJ, typically a query-for-time-series URL, or a URL to a parameter
     * entry, e.g.: .../indicator/parameter/PARAM_ENTRY_ID
     */
    private String url = null;
    /** Holds all various units used in the data set within this time series collection. */
    private List<TimeSeriesDataUnit> units = null;
    //** Holds the number of time markers. */
    //protected int numTimeMarkers = 0;
    /** Flag indicating whether this collections contains any error bar series or not. */
    private boolean hasErrorBarSeries = false;
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(TimeSeriesCollection.class);
    /** Localized strings. */
    protected ResourceBundle labels = null;
                
    /**
     * Creates a new time series collection, which holds the given time series.
     * 
     * @param displayLocale The preferred locale for language-specific data.
     * @param tss The list of time series.
     * @throws JSONException 
     */
    /*public TimeSeriesCollection(Locale displayLocale, List<TimeSeries> tss) throws JSONException {
        this.displayLocale = displayLocale;
        this.setTimeSeries(tss);
    }*/
    
    /**
     * Creates a new time series collection, which holds the given time series.
     * 
     * @param displayLocale The preferred locale for language-specific data.
     * @param tss The list of time series.
     * @param title The title for this collection. (For MOSJ, this would be the MOSJ parameter's title.)
     * @throws JSONException 
     */
    /*public TimeSeriesCollection(Locale displayLocale, List<TimeSeries> tss, String title) throws JSONException {
        this(displayLocale, tss);
        this.title = title;
    }*/
    
    /**
     * Creates a new time series collection, holding the given time series.
     * 
     * @param displayLocale The preferred locale for language-specific data.
     * @param tss The list of time series.
     * @param title The title for this collection. (In MOSJ, this would be the chart/"parameter" title.)
     * @param url The URL corresponding to the this collection. (Typically points to a time series query, or a MOSJ Parameter entry.)
     * @throws JSONException 
     */
    public TimeSeriesCollection(Locale displayLocale, List<TimeSeries> tss, String title, String url) throws JSONException {
        //this(displayLocale, tss);
        this.displayLocale = displayLocale;
        this.setTimeSeries(tss);
        this.title = title;
        this.url = url;
        this.labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
    }
    
    /**
     * Clears all existing time series in this collection, then adds all the
     * given ones.
     * 
     * @param tss The time series that this collection should contain.
     * @return This instance, updated.
     */
    private TimeSeriesCollection setTimeSeries(List<TimeSeries> tss) {
        // Clear all lists
        allTimestamps = new TreeSet<TimeSeriesTimestamp>();
        timeSeriesList = new ArrayList<TimeSeries>();
        units = new ArrayList<TimeSeriesDataUnit>();
        //units = new TreeSet<TimeSeriesDataUnit>();
        
        // Add the given time series
        timeSeriesList.addAll(tss);
        
        //System.out.println("Collating time series: ");
        
        // Loop all time series
        Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
        while (iTimeSeries.hasNext()) {
            TimeSeries timeSeries = iTimeSeries.next();
            //System.out.println("\t" + timeSeries.getTitle());
            // Add timestamps from this time series
            allTimestamps.addAll(timeSeries.getTimestamps());
            
            // Add the unit to the list of units, if not already added
            addUnit(timeSeries.getUnit());
            
            if (timeSeries.isErrorBarSeries()) {
                this.hasErrorBarSeries = true;
            }
        }
        //System.out.println("Collated " 
        // + allTimestamps.size() + "*" + timeSeriesList.size() + "=" 
        // + (allTimestamps.size() * timeSeriesList.size()) 
        // + " data points total.");
        
        return this;
    }
    
    /**
     * Adds a single time series to this collection.
     * <p>
     * If it already exists in the collection, it is not added.
     * 
     * @param timeSeries The time series to add.
     * @return This instance, updated.
     */
    public TimeSeriesCollection addTimeSeries(TimeSeries timeSeries) {
        if (!timeSeriesList.contains(timeSeries)) {
            timeSeriesList.add(timeSeries);
            allTimestamps.addAll(timeSeries.getTimestamps());
            addUnit(timeSeries.getUnit());
            if (timeSeries.isErrorBarSeries()) {
                hasErrorBarSeries = true;
            }
        }
        return this;
    }
    
    /**
     * Adds the given unit to the list of (unique) units used by the time series 
     * in this collection, if necessary.
     * <p>
     * If the given unit already exists in the list of unique units, nothing is
     * done.
     * 
     * @param unit The unit to add.
     * @return This instance, updated.
     */
    private TimeSeriesCollection addUnit(TimeSeriesDataUnit unit) {
        if (!units.contains(unit)) {
            units.add(unit);
        }
        return this;
    }
    
    /**
     * Sorts / re-orders the series in this collection.
     * 
     * @param comparator The comparator to use in the re-ordering / sorting.
     */
    public void sortTimeSeries(Comparator<TimeSeries> comparator) {
        // Make a copy of the current time series list
        List<TimeSeries> reOrderedTimeSeriesList = new ArrayList<TimeSeries>();
        reOrderedTimeSeriesList.addAll(timeSeriesList);
        // Order the copy based on the order index
        Collections.sort(reOrderedTimeSeriesList, comparator);
        // Update the time series list
        this.setTimeSeries(reOrderedTimeSeriesList);
    }
    /**
     * Gets the units applicable for the data in this set.
     * 
     * @return The units applicable for the data in this set.
     */
    public List<TimeSeriesDataUnit> getUnits() {
        return units;
    }
    /**
     * Gets the title of this collection. 
     * <p>
     * For MOSJ, this would normally be the title of the MOSJ parameter.
     * 
     * @return The title of this collection.
     */
    public String getTitle() { return this.title; }
    
    /**
     * Gets the URL for this collection.
     * <p>
     * In MOSJ, this would normally be a Data Centre URL pointing either to 
     * <p>
     * <ol>
     * <li>a time series query, that returns all the time series in this 
     * collection</li>
     * <li>a single MOSJ parameter entry, with the time series in this 
     * collection defined as its "related time series"</li>
     * </ol>
     * 
     * @return The URL related to this collection.
     */
    public String getURL() { return this.url; }
    
    /**
     * Gets the configured preferred locale.
     * 
     * @return The preferred locale.
     */
    public Locale getDisplayLocale() { return this.displayLocale; }
    
    /**
     * Exports all time series data in this collection to a comma-separated 
     * format, which can then be used to form the content of a (download) file.
     * 
     * @return All time series data as CSV content.
     */
    public String getAsCSV() {
        String s = "";
        try {
            s += getCSVRows();
        } catch (Exception e) {
            //e.printStackTrace();
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to create CSV content from time series collection at " + getURL(), e);
            }
        }
        return s;
    }
    
    /**
     * Translates the given time series collection to CSV rows containing the
     * data.
     * 
     * @return CSV rows containing the data in the given time series collection.
     */
    protected String getCSVRows() {
        String s = "";
        try {
            if (timeSeriesList != null && !timeSeriesList.isEmpty()) {           
                // heading
                s += labels.getString(Labels.TIME_SERIES_TITLE_0) + ";" + labels.getString(Labels.TIME_SERIES_UNIT_0) + ";";
                
                // The columns, based on timestamps (i.e. years)
                Iterator<TimeSeriesTimestamp> iTimeMarkers = getTimeMarkerIterator();
                while (iTimeMarkers.hasNext()) {
                    s += "" + iTimeMarkers.next();
                    s += iTimeMarkers.hasNext() ? ";" : "\n";
                }
                
                
                if (!timeSeriesList.isEmpty()) {
                    Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
                    while (iTimeSeries.hasNext()) {
                        TimeSeries ts = iTimeSeries.next();
                        //s += "<!-- time series: " + getTitle() + " - " + getId() + " -->\n";
                        s += ts.getDataPointsAsCSVRow(this);
                    }
                } else {
                    // No time series data
                }
            } else {
                // No time series data
            }
        } catch (Exception e) {
            //s += "<!-- Error: " + e.getMessage() + " -->\n";
            //e.printStackTrace();
            if (LOG.isErrorEnabled()) {
                LOG.error("Error creating html table for time series collection '" + this.getTitle() + "'.", e);
            }
        }
        return s;
    }
    
    /**
     * Gets an HTML table with all time series data.
     * 
     * @return An HTML table with all time series data.
     * @see #getAsTable(java.lang.String) 
     */
    public String getAsTable() {
        return getAsTable("", null);
    }
    
    /**
     * Gets an HTML table with all time series data.
     * 
     * @param tableClass A class name to append to the table.
     * @param tableId A table ID to append to the table.
     * @return An HTML table with all time series data.
     */
    public String getAsTable(String tableId, String tableClass) {
        String s = "";
        if (!this.hasAccuracyCompatibleTimeSeries()) {
            s += "\n<!-- Warning: Multiple time series with differences in units and/or timestamp accuracies. Table will probably not be Highcharts-munchable. -->\n";
        }
        
        s += "<table"
                + (tableId != null && !tableId.isEmpty() ? " id=\"" + tableId + "\"" : "")
                + " class=\"parameter-data-table" + (tableClass != null && !tableClass.isEmpty() ? " ".concat(tableClass) : "") + "\""
                + ">";
        s += "\n<caption>" + getTitle() + "</caption>\n";
        
        try {
            s += getTableRows();
        } catch (Exception e) {
            s += "\n<!-- Error creating table: " + e.getMessage() + " -->\n";
            //e.printStackTrace();
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to create table from time series collection at " + getURL() + ".", e);
            }
        }
        
        s += "</table>";
        return s;
    }
    
    /**
     * Translates the given time series collection to table rows containing the
     * data.
     * 
     * @return HTML table rows containing the data in the given time series collection.
     */
    protected String getTableRows() {
        String s = "";
        try {
            if (timeSeriesList != null && !timeSeriesList.isEmpty()) {
                s += "<thead>\n<tr>"
                        + "<th scope=\"col\">&nbsp;</th>"
                        + "<th scope=\"col\">" 
                            + labels.getString(Labels.TIME_SERIES_UNIT_0) 
                        + "</th>";
                
                // The columns, based on timestamps (i.e. years)
                Iterator<TimeSeriesTimestamp> iTimeMarkers = getTimeMarkerIterator();
                while (iTimeMarkers.hasNext()) {
                    // The span is vital for Highcharts (but the class name is 
                    // arbitrary), if it is to use this table as a base for 
                    // building a chart
                    s += "<th scope=\"col\">"
                            + "<span class=\"hs-time-marker\">" 
                                + iTimeMarkers.next() 
                            + "</span>"
                        + "</th>";
                }
                s += "</tr>\n</thead>\n";
                
                s += "<tbody>\n";
                
                if (!timeSeriesList.isEmpty()) {
                    Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
                    while (iTimeSeries.hasNext()) {
                        TimeSeries ts = iTimeSeries.next();
                        //s += "<!-- time series: " + ts.getTitle() + " - " + ts.getId() + " -->\n";
                        s += ts.getDataPointsAsTableRow(this);
                    }
                } else {
                    s += "<!-- No time series data! -->\n";
                }
                s += "</tbody>\n";
            } else {
                s += "<!-- No time series data! -->";
            }
        } catch (Exception e) {
            s += "<!-- Error: " + e.getMessage() + " -->\n";
            if (LOG.isErrorEnabled()) {
                LOG.error("Error creating html table for time series collection described at " + getURL() + ".", e);
            }
        }
        return s;
    }
    
    /**
     * Gets the title of a time series in this collection, with the collection 
     * title stripped.
     * <p>
     * This method is needed because some / many / all time series are stored 
     * with a title that is combined with the collection title. (Separated by a
     * slash.)
     * 
     * @deprecated This method should be redundant in the production-ready API.
     * @param ts The time series to get the title of.
     * @return The title of the given time series, with the collection part stripped.
     */
    public String getTitleForTimeSeries(TimeSeries ts) {
        String tsTitle = null;
        try {
            tsTitle = ts.getTitle(displayLocale);
            if (title != null) {
                tsTitle = tsTitle.replace(" / " + title, "");
            }
        } catch (Exception e) {
            // Log this
            if (LOG.isErrorEnabled()) {
                LOG.error("Error getting title for time series '" + ts.getId() + "'. Possibly missing title?", e);
            }
        }
        return tsTitle;
    }
    
    /**
     * Gets the number of time markers in this collection.
     * 
     * @return The number of time markers in this collection.
     */
    public int getTimeMarkersCount() {
        return allTimestamps.size();
        //return numTimeMarkers;
    }
    
    /**
     * Gets all time series in this collection.
     * 
     * @return All time series in this collection.
     */
    public List<TimeSeries> getTimeSeries() { 
        return timeSeriesList;
    }
    
    /**
     * Gets all time series of the given unit in this collection.
     * 
     * @param unit The unit to evaluate against.
     * @return All time series of the given unit.
     */
    public List<TimeSeries> getTimeSeriesWithUnit(TimeSeriesDataUnit unit) { 
        List<TimeSeries> matchingSeries = new ArrayList<TimeSeries>();
        
        Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
        while (iTimeSeries.hasNext()) {
            TimeSeries timeSeries = iTimeSeries.next();
            if (timeSeries.getUnit().equals(unit)) {
                matchingSeries.add(timeSeries);
            }
        }
        return matchingSeries;
    }
    
    /**
     * Gets a flag indicating whether or not this collections contains any 
     * error bar series.
     * 
     * @return True if this collections contains one or more error bar series, false if not.
     */
    public boolean hasErrorBarSeries() {
        return this.hasErrorBarSeries;
    }
    
    /**
     * Checks if all the time series in this collection are "accuracy 
     * compatible", that is, if they use the same timestamp format.
     * 
     * @return <code>true</code> if all the time series in this collection are "accuracy compatible", <code>false</code> otherwise.
     */
    public boolean hasAccuracyCompatibleTimeSeries() {
        if (timeSeriesList == null || timeSeriesList.isEmpty()) {
            return true;
        }
        
        int testAccuracy = timeSeriesList.get(0).getDateTimeAccuracy();
        Iterator<TimeSeries> i = timeSeriesList.iterator();
        while (i.hasNext()) {
            TimeSeries ts = i.next();
            if (ts.getDateTimeAccuracy() != testAccuracy) {
                return false;
            }
            //testAccuracy = ts.getDateTimeAccuracy();
        }
        return true;
    }
    
    /**
     * Gets the "raw" data set underlying the time series in this collection.
     * <p>
     * Each key string in the returned map is a time marker (e.g. a year) and 
     * the corresponding value is an array of data points, where one cell 
     * equals the data for one time series. (The index is determined from that 
     * time series position in {@link TimeSeriesCollection#timeSeriesList }.)
     * 
     * @return The "raw" data set underlying the time series in this collection.
     */
    /*public TreeMap<TimeSeriesTimestamp, TimeSeriesDataPoint[]> getDataSet() {
    //public TreeMap<String, TimeSeriesDataPoint[]> getDataSet() {
        return this.dataSet;
    }*/
    /*
    public TreeMap<TimeSeriesTimestamp, TimeSeries[]> getTimestampedTimeSeries() {
        return this.allSeriesByTime;
    }*/
    
    /**
     * Adds data points to this time series collection.
     * <p>
     * Useful in particular on irregular time series, if we want to show time
     * markers that have no associated data.
     * 
     * @param dataPoints
     * @return This collection, updated.
     */
    /*public TimeSeriesCollection addDataPoints(Map<TimeSeriesTimestamp, TimeSeriesDataPoint[]> dataPoints) {
    //public TimeSeriesCollection addDataPoints(Map<String, TimeSeriesDataPoint[]> dataPoints) {
        try {
            this.dataSet.putAll(dataPoints);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to add data points to collection '" + this.getTitle() + "'.", e);
            }
        }
        return this;
    }*/
    
    /**
     * Adds a timestamp with empty value to this collection.
     * 
     * @param timestamp The timestamp.
     * @return This instance, updated.
     */
    public TimeSeriesCollection setEmptyOnTimestamp(TimeSeriesTimestamp timestamp) {
        try {
            //this.allSeriesByTime.put(timestamp, null);
            allTimestamps.add(timestamp);
            //System.out.println("Added " + timestamp + " to time series collection (" + allTimestamps.size() + " timestamps total now)");
        }catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Failed to add empty data point at timestamp '" + timestamp + "' in collection '" + getTitle() + "'.", e);
            }
        }
        return this;
    }
    
    /**
     * Gets an iterator for the time markers in this collection.
     * 
     * @return An iterator for the time markers in this collection.
     */
    public Iterator<TimeSeriesTimestamp> getTimeMarkerIterator() {
    //public Iterator<String> getTimeMarkerIterator() {
        try {
            return allTimestamps.iterator();
            //return this.getDataSet().keySet().iterator();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Attempting to get time marker iterator for collection '" + getTitle() + "' failed.", e);
            }
            return null;
        }
    }
    
    /**
     * Gets all data points (that is, the data points for all time series in 
     * this collection) for the given time marker.
     * <p>
     * The data points from different time series are placed in different cells 
     * in the returned array (i.e. one cell = one time series), at the index 
     * determined by that particular time series' placement in 
     * {@link TimeSeriesCollection#timeSeriesList }. 
     * <p>
     * For example, the first time series in the list will be at index 0 in the 
     * returned array. The second time series in the list will be at index 1, 
     * and so on.
     * 
     * @param timeMarker The time marker to get data points for.
     * @return Every data point (from all time series) for the given time marker.
     */
    public TimeSeriesDataPoint[] getDataPointsForTimeMarker(TimeSeriesTimestamp timeMarker) {
        //return this.getDataSet().get(timeMarker);
        TimeSeriesDataPoint[] dataPoints = new TimeSeriesDataPoint[timeSeriesList.size()];
        int i = 0;
        Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
        while (iTimeSeries.hasNext()) {
            dataPoints[i] = iTimeSeries.next().getDataPointForTimeMarker(timeMarker);
        }
        return dataPoints;
    }
    
    /**
     * Gets the author string, compiled from the time series in this collection. 
     * 
     * @return The author string.
     * @see #getAuthors() 
     */
    public String getAuthorsStr() {
        String s = "";
        List<String> a = getAuthors();
        for (int i = 0; i < a.size(); i++) {
            if (i > 0) {
                // Not first author: add delimiter char
                if (i+2 == a.size()) {
                    s += " & ";
                } else {
                    s += ", ";
                }
            }
            // add name
            s += a.get(i);
        }
        return s;
    }
    
    /**
     * Gets a list of authors, compiled from the time series in this collection. 
     * 
     * @return A list of authors (without duplicates).
     * @see #getAuthorsStr() 
     */
    public List<String> getAuthors() {
        List<String> a = new ArrayList<String>(1);
        for (TimeSeries ts : getTimeSeries()) {
            List<String> tsAuths = ts.getAuthors();
            for (String tsAuth : tsAuths) {
                if (!a.contains(tsAuth)) {
                    a.add(tsAuth);
                }
            }
        }
        return a;
    }
    
    /**
     * Sets the time series and makes sure all changes are propagated.
     * <p>
     * This method is called by the constructor and by any method(s) that 
     * causes any (re)ordering of the time series in this collection.
     * 
     * @param tss The time series to add.
     * @return The updated instance.
     */
    /*private TimeSeriesCollection setTimeSeriesOld(List<TimeSeries> tss) {
        timeSeriesList.clear();
        units.clear();
        dataSet.clear();
        allSeriesByTime.clear();
        
        TreeSet<TimeSeriesTimestamp> allTimestamps = new TreeSet<TimeSeriesTimestamp>();
        
        timeSeriesList.addAll(tss);
        
        Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
        
        int timeSeriesIndex = 0; // Keep track of this time series' own index (or, "column" in a table)
        while (iTimeSeries.hasNext()) {
            TimeSeries timeSeries = iTimeSeries.next();
            
            allTimestamps.addAll(timeSeries.getTimestamps());
            
            // Add the unit to the list of units, if not already added
            //TimeSeriesDataUnit tsDataUnit = new TimeSeriesDataUnit(ts.getUnit(), ts.getUnitVerbose(displayLocale));
            
            if (!units.contains(timeSeries.getUnit()))
                units.add(timeSeries.getUnit());
            
            // Get all data points in the time series
            TreeSet<TimeSeriesDataPoint> dataPoints = timeSeries.getDataPoints();
            
            // Update variable holding the number of time markers
            if (dataPoints.size() > numTimeMarkers) {
                numTimeMarkers = dataPoints.size();
            }
            
            // Loop data points
            Iterator<TimeSeriesDataPoint> i = dataPoints.iterator();
            while (i.hasNext()) {
                TimeSeriesDataPoint dataPoint = i.next();
                
                // Use the associated timestamp object as the key
                TimeSeriesTimestamp dataPointKey = dataPoint.getTimestamp();
                //String dataPointKey = dataPoint.getTimestamp().format(new SimpleDateFormat(TimeSeriesTimestamp.PATTERN_TIME_STANDARD));
                //String dataPointKey = dataPoint.getTimestampFormatted();
                
                // If there is no data on that timestamp, add it, and map it to 
                // an (empty) array of data points. The array's capacity (size) 
                // is set to the total number of time series in this collection
                if (!dataSet.containsKey(dataPointKey)) {
                    dataSet.put(dataPointKey, new TimeSeriesDataPoint[timeSeriesList.size()] );
                    //System.out.println("TS collection: added data at " + dataPointKey);
                } 
                dataSet.get(dataPointKey)[timeSeriesIndex] = dataPoint;
            }
            
            if (timeSeries.isErrorBarSeries())
                this.hasErrorBarSeries = true;
            
            timeSeriesIndex++;
        }
        System.out.println("Collating " + dataSet.size() + "*" + dataSet.firstEntry().getValue().length 
                                + "=" + (dataSet.size() * dataSet.firstEntry().getValue().length) 
                                + " data points total.");
        
        return this;
    }*/
    
    /**
     * @param ts
     * @param overrides
     * @return 
     *
    protected JSONObject getTimeSeriesOverrides(TimeSeries ts, JSONObject overrides) {
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
    }*/
    /**
     * Gets this data set as "Highcharts-ready" javascript code.
     * 
     * @param overrides
     * @return
     *
    public String getAsHighchartsSeries(JSONObject overrides) {
        
        try { if (overrides == null) overrides = new JSONObject(); } catch (Exception e) {} // 
        
        String s = "";
        if (!timeSeriesList.isEmpty()) {
            Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
            int timeSeriesIndex = 0;
            while (iTimeSeries.hasNext()) {
                TimeSeries ts = iTimeSeries.next();
                
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
                    //int yAxis = units.indexOf( new TimeSeriesDataUnit(ts.getUnit(), ts.getUnitVerbose(displayLocale)) );
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
    }*/
    
    /**
     * Gets the values for the time series identified by the given time series
     * index.
     * <p>
     * The returned values string is "aware" of any other time series in this 
     * data set, and will contain a <code>null</code> value for any time markers 
     * where the specified time series lacks a value.
     * 
     * @param timeSeriesIndex The index identifying the time series. (This index is its location in {@link TimeSeriesCollection#timeSeriesList}.)
     * @return
     *
    private String getValuesForTimeSeries(int timeSeriesIndex, boolean highLowValues) {
        String s = "";
        // We must loop all time markers to ensure we get proper null values
        // at time markers where the time series is missing a value
        Iterator<String> iTimeMark = this.dataSet.keySet().iterator();
        
        while (iTimeMark.hasNext()) {
            String timeMark = iTimeMark.next();
            TimeSeriesDataPoint[] timeMarkData = this.dataSet.get(timeMark); // Get the array of data points for this time marker

            try {
                TimeSeriesDataPoint dp = timeMarkData[timeSeriesIndex];
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
    }*/
    
    /**
     * Gets this data set as table rows.
     * @return This data set as table rows.
     *
    public String getTableRows() {
        String s = "";
        try {
            if (!timeSeriesList.isEmpty()) {
                Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();

                s += "<thead>\n<tr><th></th>";
                while (iTimeSeries.hasNext()) {
                    TimeSeries ts = iTimeSeries.next();
                    String tsTitle = ts.getTitle(displayLocale);
                    if (title != null) {
                        tsTitle = tsTitle.replace(" / " + title, "");
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
                    TimeSeriesDataPoint[] timeMarkData = this.dataSet.get(timeMark); // Get the array of data points for this time marker

                    for (int i = 0; i < timeMarkData.length; i++) {
                        s += "<td>";
                        try {
                            TimeSeriesDataPoint dp = timeMarkData[i];
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
                LOG.error("Error creating Highcharts-munchable table for time series collection '" + this.title + "'.", e);
            }
            s += "<!-- Error: " + e.getMessage() + " -->";
        }
        return s;
    }*/
    
    /**
     * Gets the keys (time markers) in this data set, comma-separated.
     * 
     * @return The keys in this data set, comma-separated.
    public String getKeysCommaSeparated() {
        String s = "";
        try {
            Iterator<String> itr = this.dataSet.keySet().iterator();
            while (itr.hasNext()) {
                s += "'" + itr.next() + "'";
                if (itr.hasNext()) s += ", ";
            }
        } catch (Exception e) {
            LOG.error("Error creating Highcharts-munchable data set for time series collection '" + this.title + "'.", e);
        }
        return s;
    }
     */
}
