package no.npolar.data.api.mosj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import no.npolar.data.api.APIEntryInterface;
import no.npolar.data.api.APIServiceInterface;
import no.npolar.data.api.MOSJService;
import no.npolar.data.api.TimeSeries;
import no.npolar.data.api.TimeSeriesCollection;
import no.npolar.data.api.TimeSeriesTimestamp;
import no.npolar.data.api.util.APIUtil;
import no.npolar.data.api.Labels;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a MOSJ parameter, which is basically a wrapper for time series 
 * data.
 * <p>
 * This is the main class for interacting with the MOSJ API.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class MOSJParameter implements APIEntryInterface {
    /** This parameter's ID, as read from the API. */
    private String id = null;
    /** The complete parameter data, as read from the API. */
    private JSONObject apiStructure = null;
    /** List of related time series. */
    protected List<TimeSeries> relatedTimeSeries = null;
    //protected List<MOSJTimeSeries> relatedTimeSeries = null;
    
    /** All related time series. */
    protected TimeSeriesCollection tsc = null;
    
    /** Preferred locale to use when getting language-specific data. */
    protected Locale displayLocale = null;
    /** Localized strings. */
    protected ResourceBundle labels = null;
    /** Default locale string. */
    public static final String DEFAULT_LOCALE = "en";
    
    
    // Data Centre keywords
    public static final String API_KEY_TITLES = "titles";
    //public static final String API_KEY_TITLE = "text";
    public static final String API_KEY_TITLE = "title";
    public static final String API_KEY_ID = "id";
    public static final String API_KEY_RELATED_TIME_SERIES = "timeseries";
    
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(MOSJParameter.class);
    
    /**
     * Constructs a new parameter instance, based on the given JSON object, which
     * is typically read from the Data Centre API.
     * 
     * @param o The JSON object to base this parameter instance on.
     * @param displayLocale The preferred language.
     * @throws InstantiationException If anything goes wrong when reading the 'id' property from the JSON object.
     */
    public MOSJParameter(JSONObject o, Locale displayLocale) throws InstantiationException {
        this.displayLocale = displayLocale;
        this.relatedTimeSeries = new ArrayList<TimeSeries>();
        apiStructure = o;
        try {
            id = o.getString(API_KEY_ID);
        } catch (Exception e) {
            throw new InstantiationException("Error attempting to create MOSJ parameter instance from JSON object: " + e.getMessage());
        }
        this.labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        
        resolveTimeSeries();
    }
    
    /**
     * Constructs a new parameter instance, based on the given JSON object, 
     * which is typically read from the Data Centre API.
     * <p>
     * The preferred language for the new parameter will be set to 
     * {@link #DEFAULT_LOCALE}.
     * 
     * @see #MOSJParameter(org.opencms.json.JSONObject, java.util.Locale) 
     * @param o The JSON object to base this parameter instance on.
     * @throws InstantiationException If anything goes wrong when reading the 'id' property from the JSON object.
     */
    public MOSJParameter(JSONObject o) throws InstantiationException {
        this(o, new Locale(DEFAULT_LOCALE) );
    }
    
    /**
     * Populates or re-populates the list of related {@link TimeSeries}.
     * <p>
     * This method evaluates the backing JSON object, and looks for related time
     * series. A {@link TimeSeries} instance is created for each related time 
     * series that is referenced.
     * <p>
     * Note that any existing list of related time series is cleared before it 
     * is (potentially again) populated.
     * 
     * @return The updated parameter instance.
     */
    private MOSJParameter resolveTimeSeries() {
        this.relatedTimeSeries.clear();
        if (apiStructure.has(API_KEY_RELATED_TIME_SERIES)) {
            try {   
                JSONArray relatedTimeSeriesArr = this.apiStructure.getJSONArray(API_KEY_RELATED_TIME_SERIES); // Each array entry is a URLs to a related time series
                for (int i = 0; i < relatedTimeSeriesArr.length(); i++) {
                    try {
                        String relatedTimeSeriesUrl = relatedTimeSeriesArr.getString(i);
                        JSONObject timeSeriesJSON = APIUtil.queryService(relatedTimeSeriesUrl);
                        if (timeSeriesJSON == null) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("MOSJ parameter " + id + " includes a problem time series at " + relatedTimeSeriesUrl);
                            }
                        } else {
                            long a = System.currentTimeMillis();
                            TimeSeries ts = new TimeSeries(timeSeriesJSON, displayLocale);
                            long b = System.currentTimeMillis();
                            //System.out.println("Created time series (" + (b-a) +"ms).");
                            this.addTimeSeries(ts);
                        }
                    } catch (Exception e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Error resolving related time series for MOSJ parameter " + id + ".", e);
                        }
                    }
                }
            } catch (Exception e) {
                // Parameter has no related time series
            }
        }
        return this;
    }
    
    /**
     * Sets the preferred locale.
     * <p>
     * This is a costly method, as all related time series have to be resolved 
     * over again (some of their properties are language-dependent).
     * 
     * @param displayLocale The preferred locale to use when fetching language-specific data.
     * @return The updated parameter instance.
     */
    public MOSJParameter setDisplayLocale(Locale displayLocale) {
        this.displayLocale = displayLocale;
        resolveTimeSeries();
        return this;
    }
    
    /**
     * Gets the parameter's title, localized (if possible) according to the 
     * given locale.
     * 
     * @param locale The preferred locale to use when fetching the title.
     * @return The parameter's title, localized (if possible) according to the given locale.
     */
    public String getTitle(Locale locale) {
        String title = null;
        
        try { 
            JSONArray titles = apiStructure.getJSONArray(API_KEY_TITLES);
            title = APIUtil.getStringByLocale(titles, API_KEY_TITLE, locale); // Get title in preferred language
            /*
            for (int i = 0; i < titles.length(); i++) {
                JSONObject titleObj = titles.getJSONObject(i);
                if (APIUtil.matchLanguage(titleObj.getString("lang"), locale))
                    title = titleObj.getString("title");
            } 
            //*/
            if (title == null)  { // No title in that language
                title = APIUtil.getStringByLocale(titles, API_KEY_TITLE, new Locale(DEFAULT_LOCALE)); // Get title in default language
            }
            if (title == null) {
                title = titles.getJSONObject(0).getString(API_KEY_TITLE); // Get ANY title (if there are multiple, we select the first one)
            }
        } catch (Exception e) { 
            if (LOG.isErrorEnabled()) {
                LOG.error("Failed to read title using the configured locale (" + this.displayLocale.getLanguage() + ").", e);
            }
        }
        return title;
    }
    
    /**
     * Gets the title for this time series, in the preferred language.
     * 
     * @return The title for this time series, in the preferred language.
     */
    @Override
    public String getTitle() {
        return this.getTitle(this.displayLocale);
    }
    
    /**
     * Gets the group name.
     * <p>
     * For MOSJ parameters, there is no natural group name currently available, 
     * so this method will just return null.
     * <p>
     * <strong>ToDo</strong>: Return name/ID of parent indicator.
     * 
     * @see TimeSeriesDataUnit#getShortForm()
     * @return The group name.
     */
    @Override
    public String getGroupName() {
        // ToDo: return name/ID of parent indicator.
        return null;
        //return this.unit.getShortForm();
    }
    
    /**
     * Gets the URL for this MOSJ parameter, within the context of the given 
     * service.
     * <p>
     * The service must be of type {@link MOSJService}.
     * 
     * @param service The API service. Must be of type {@link MOSJService}.
     * @return The URL for this MOSJ parameter, or: where it resides within the given service.
     */
    @Override
    public String getURL(APIServiceInterface service) {
        if (!(service instanceof MOSJService)) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Cannot retrieve MOSJ parameter URL using a service not of type " + MOSJService.class.getName() + ".");
            }
        }
        
        return ((MOSJService)service).getParameterBaseURL() + this.getId();
    }
    
    
    
    /**
     * Adds a single related time series to this parameter.
     * 
     * @param relatedTimeSeries The single time series to add as related to this parameter.
     * @return The updated parameter instance.
     */
    private MOSJParameter addTimeSeries(TimeSeries relatedTimeSeries) {
        if (this.relatedTimeSeries == null)
            this.relatedTimeSeries = new ArrayList<TimeSeries>();
        
        this.relatedTimeSeries.add(relatedTimeSeries);
        
        return this;
    }
    
    /**
     * Adds a list of related time series to this parameter.
     * 
     * @param relatedTimeSeries The list of time series to add as related to this parameter.
     * @return The updated parameter instance.
     */
    public MOSJParameter addAllTimeSeries(List<TimeSeries> relatedTimeSeries) {
        if (this.relatedTimeSeries == null)
            this.relatedTimeSeries = new ArrayList<TimeSeries>();
        
        this.relatedTimeSeries.addAll(relatedTimeSeries);
        
        return this;
    }
    
    /**
     * Checks if all the time series related to this parameter are "accuracy 
     * compatible", that is, if they use the same timestamp format.
     * 
     * @return True if all the time series related to this parameter are "accuracy compatible" (use the same timestamp format), false if not.
     */
    public boolean hasAccuracyCompatibleTimeSeries() {
        if (this.relatedTimeSeries == null)
            return true;
        
        if (this.relatedTimeSeries.isEmpty()) {
            return true;
        }
        
        int testAccuracy = relatedTimeSeries.get(0).getDateTimeAccuracy();
        Iterator<TimeSeries> i = relatedTimeSeries.iterator();
        while (i.hasNext()) {
            TimeSeries ts = i.next();
            if (ts.getDateTimeAccuracy() != testAccuracy) {
                return false;
            }
            testAccuracy = ts.getDateTimeAccuracy();
        }
        
        /*String testAccuracy = relatedTimeSeries.get(0).getDateTimeAccuracy();
        Iterator<TimeSeries> i = relatedTimeSeries.iterator();
        while (i.hasNext()) {
            TimeSeries ts = i.next();
            if (!ts.getDateTimeAccuracy().equals(testAccuracy)) {
                return false;
            }
            testAccuracy = ts.getDateTimeAccuracy();
        }*/
        return true;
    }
    
    /**
     * Gets the chart instance.
     * <p>
     * Custom settings may be passed via a JSON object. Such settings will add 
     * to or override the default chart settings.
     * 
     * @param overrides Overrides object, as passed from the client controller (i.e. the JSP).
     * @return The chart instance.
     */
    public HighchartsChart getChart(JSONObject overrides) {
        return new HighchartsChart(this, overrides);
    }
    
    /**
     * @return All time series data as CSV content.
     */
    public String getAsCSV() {
        String s = "";
        try {
            s += getCSVRows();
        } catch (Exception e) {
            //e.printStackTrace();
            if (LOG.isErrorEnabled()) {
                LOG.error("Creating CSV content from MOSJ parameter '" + this.getId() + "' failed.", e);
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
            TimeSeriesCollection tsc = this.getTimeSeriesCollection();
            if (tsc == null)
                tsc = new TimeSeriesCollection(displayLocale, relatedTimeSeries);
            
            List<TimeSeries> timeSeriesList = tsc.getTimeSeries();
            if (timeSeriesList != null && !timeSeriesList.isEmpty()) {
                
                // heading
                s += labels.getString(Labels.TIME_SERIES_TITLE_0) + ";" + labels.getString(Labels.TIME_SERIES_UNIT_0) + ";";
                
                // The columns, based on timestamps (i.e. years)
                Iterator<TimeSeriesTimestamp> iTimeMarkers = tsc.getTimeMarkerIterator();
                while (iTimeMarkers.hasNext()) {
                    s += "" + iTimeMarkers.next();
                    s += iTimeMarkers.hasNext() ? ";" : "\n";
                }
                
                
                if (!timeSeriesList.isEmpty()) {
                    Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
                    while (iTimeSeries.hasNext()) {
                        TimeSeries ts = iTimeSeries.next();
                        //s += "<!-- time series: " + ts.getTitle() + " - " + ts.getId() + " -->\n";
                        s += ts.getDataPointsAsCSVRow(tsc);
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
            // ToDo: Log this
            if (LOG.isErrorEnabled()) {
                LOG.error("Error creating html table for time series collection '" + this.getTimeSeriesCollection().getTitle() + "'.", e);
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
        return getAsTable(null);
    }
    
    /**
     * Gets an HTML table with all time series data.
     * 
     * @param tableClass A class name to append to the table.
     * @return An HTML table with all time series data.
     */
    public String getAsTable(String tableClass) {
        String s = "";
        if (!this.hasAccuracyCompatibleTimeSeries()) {
            s += "\n<!-- Warning: Parameter has multiple time series, with differences in units and/or timestamp accuracies. Table will probably not be Highcharts-munchable. -->\n";
        }
        
        s += "<table id=\"" + this.getId() + "-data\" class=\"parameter-data-table" + (tableClass != null && !tableClass.isEmpty() ? " ".concat(tableClass) : "") + "\">\n";
        s += "<caption>" + this.getTitle() + "</caption>\n";
        
        try {
            s += getTableRows();
        } catch (Exception e) {
            s += "\n<!-- Error creating table: " + e.getMessage() + " -->\n";
            //e.printStackTrace();
            if (LOG.isErrorEnabled()) {
                LOG.error("Creating Highcharts-munchable table from MOSJ parameter '" + this.getId() + "' failed.", e);
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
            TimeSeriesCollection tsc = this.getTimeSeriesCollection();
            if (tsc == null)
                tsc = new TimeSeriesCollection(displayLocale, relatedTimeSeries);
            
            List<TimeSeries> timeSeriesList = tsc.getTimeSeries();
            if (timeSeriesList != null && !timeSeriesList.isEmpty()) {
                

                s += "<thead>\n<tr><th scope=\"col\">&nbsp;</th><th scope=\"col\">" + labels.getString(Labels.TIME_SERIES_UNIT_0) + "</th>";
                
                // The columns, based on timestamps (i.e. years)
                Iterator<TimeSeriesTimestamp> iTimeMarkers = tsc.getTimeMarkerIterator();
                while (iTimeMarkers.hasNext()) {
                    // The span is vital for Highcharts (but the class name is arbitrary), if the chart is to be generated based on the table
                    s += "<th scope=\"col\"><span class=\"hs-time-marker\">" + iTimeMarkers.next() + "</span></th>";
                }
                s += "</tr>\n</thead>\n";
                
                s += "<tbody>\n";
                if (!timeSeriesList.isEmpty()) {
                    Iterator<TimeSeries> iTimeSeries = timeSeriesList.iterator();
                    while (iTimeSeries.hasNext()) {
                        TimeSeries ts = iTimeSeries.next();
                        //s += "<!-- time series: " + ts.getTitle() + " - " + ts.getId() + " -->\n";
                        s += ts.getDataPointsAsTableRow(tsc);
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
            //e.printStackTrace();
            // ToDo: Log this
            if (LOG.isErrorEnabled()) {
                LOG.error("Error creating html table for time series collection '" + this.getTimeSeriesCollection().getTitle() + "'.", e);
            }
        }
        return s;
    }
    
    /**
     * Use {@link MOSJParameter#getChartConfigurationString(org.opencms.json.JSONObject) }
     * @param overrides
     * @return 
     *
    public String getHighchartsConfig(JSONObject overrides) {
        String s = "";
        try {
            // Prevent NPE
            if (overrides == null) overrides = new JSONObject(); // Empty json object
            
            TimeSeriesCollection timeSeriesCollection = getTimeSeriesCollection();
            List<TimeSeriesDataUnit> units = timeSeriesCollection.getUnits();
            Iterator<TimeSeriesDataUnit> iUnits = units.iterator();
            
            String type = "zoomType: 'x'";
            int step = timeSeriesCollection.getTimeMarkersCount() / 8;
            int maxStaggerLines = -1;
            int xLabelRotation = -1;
            boolean hideMarkers = false;
            
            try { type = "type: '" + overrides.getString("type") + "'"; } catch(Exception ee) {}
            try { step = Integer.valueOf(overrides.getString("step")); } catch(Exception ee) {}
            try { maxStaggerLines = Integer.valueOf(overrides.getString("maxStaggerLines")); } catch(Exception ee) {}
            try { xLabelRotation = Integer.valueOf(overrides.getString("xLabelRotation")); } catch(Exception ee) {}
            try { hideMarkers = !Boolean.valueOf(overrides.getString("dots")); } catch(Exception ee) {}
            
            s += "{ ";
            // Chart type
            s += "\nchart: { ";
            s += type;
            s += "}, ";
            
            
            
            s += "\ntitle: { text: '" + getTitle() + "' }, ";
            
            if (hideMarkers) {
                s += "\nplotOptions: { ";
                    s += "\nseries: { ";
                        s += "\nmarker: { enabled: false }";
                    s += "\n}";
                s += "\n}, ";
            }
            
            // The x axis
            s += "\nxAxis: [{ ";
                    s += "\ncategories: [" + timeSeriesCollection.getKeysCommaSeparated() + "], "; // ToDo: Default should be datetime
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
                s += timeSeriesCollection.getAsHighchartsSeries(overrides);
            s += "]";
            
            s += "}";
            
            return s;
            //return new JSONObject(s);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Fatal error creating Highcharts-munchable javascript snippet.", e);
            }
        }       
        
        return null;
    }*/
    
    
    /**
     * Gets the data table, which contains the data from all the related time 
     * series. The table can (in some cases) be used as a data source for a 
     * Highcharts graph.
     * 
     * @return A data table containing the data from all the related time series.
     * @throws JSONException 
     *
    public String getAsTable() throws JSONException {
        if (!this.hasAccuracyCompatibleTimeSeries()) {
            return "<!-- Error: Parameter has multiple time series, with incompatible datetime accuracy levels. Unable to render table. -->";
        }
        String pTitle = this.getTitle();
        String s = "<table id=\"" + this.getID() + "-data\" class=\"wcag-off-screen\">\n";
        s += "<caption>" + pTitle + "</caption>\n";
        
        //MOSJDataSet timeSeriesCollection = new MOSJDataSet(displayLocale, relatedTimeSeries, pTitle);
        s += this.getTimeSeriesCollection().getTableRows();
        
        s += "</table>";
        return s;
    }*/
    
    /**
     * @return The collection of time series related to this parameter.
     */
    public TimeSeriesCollection getTimeSeriesCollection() {
        if (this.tsc == null) {
            try {
                long a = System.currentTimeMillis();
                tsc = new TimeSeriesCollection(displayLocale, relatedTimeSeries, this.getTitle());
                long b = System.currentTimeMillis();
                //System.out.println("Creating time series collection ... done (" + (b-a) + "ms).");
                //return tsc;
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Error constructing time series collection for MOSJ parameter " + this.getId() + ": ", e);
                }
                return null;
            }
        }
        return tsc;
    }
    
    /**
     * Gets related time series.
     * <p>
     * The related time series have to be manually added first. This is done by the MOSJService.
     * @return 
     */
    public List<TimeSeries> getTimeSeries() {
        return this.relatedTimeSeries;
    }
    
    /**
     * @return The ID, as read from the API.
     */
    @Override
    public String getId() { return this.id; }
    
    /**
     * @see APIEntryInterface#getJSON() 
     */
    @Override
    public JSONObject getJSON() { return apiStructure; }
    
    /**
     * @return The configured preferred locale to use when getting language-specific data.
     */
    public Locale getDisplayLocale() { return this.displayLocale; }
}
