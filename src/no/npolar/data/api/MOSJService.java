package no.npolar.data.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
//import no.npolar.data.api.APIService;
//import no.npolar.data.api.Labels;
//import no.npolar.data.api.TimeSeries;
import no.npolar.data.api.mosj.MOSJParameter;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
//import org.opencms.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencms.json.JSONObject;

/**
 * Provides an interface to read MOSJ-specific data from the Norwegian Polar 
 * Institute Data Centre.
 * <p>
 * ToDo: In addition to this MOSJ-specific service, there should be an agnostic, 
 *  generic class, e.g. "MonitoringService", which this class could then extend.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class MOSJService extends APIService {
    
    /** The URL path to use when accessing the service. */
    public static final String SERVICE_PATH = "indicator/";
    
    /** The URL path add-on for time series entries. */
    public static final String SERVICE_PATH_TIMESERIES = "timeseries/";
    
    /** The URL path add-on for time series entries. */
    public static final String SERVICE_PATH_PARAMETER = "parameter/";
    
    /**
     * The pre-defined parameter values used by this service.
     */
    public class ParamVal extends APIService.ParamVal {
        /** The value used in the APIs {@link APIEntry.Key#SYSTEMS} field, for entries that belong to the MOSJ system */
        public static final String SYSTEM_MOSJ = "mosj.no";
    }
    
    /** Translations. */
    protected ResourceBundle labels = null;
    
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(MOSJService.class);
    
    /** The configured protocol used by this service (http / https). */
    protected String serviceProtocol = null;
    
    /**
     * Creates a new service instance, configured with the given locale.
     * 
     * @param loc The locale to use when generating strings for screen view. If null, the default locale is used.
     * @param secure Set to true to use https, false to use http. 
     */
    public MOSJService(Locale loc, boolean secure) {
        this.serviceProtocol = secure ? "https" : "http";
        
        displayLocale = loc;
        if (displayLocale == null)
            displayLocale = new Locale(DEFAULT_LOCALE_NAME);
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        
        initPresetParameters();
    }
    
    /*
    public List<JSONObject> getParameter(String id) {
        
    }
    //*/
    
    /**
     * Queries the service using the current parameters and returns all (if any)
     * MOSJ parameters, generated from the service response.
     * 
     * @param params The query parameters to use in the service request.
     * @return A list of all MOSJ parameters, generated from the service response, or an empty list (= no matches).
     * @see APIService#doQuery(java.util.Map) 
     * @see APIService#getEntries() 
     * @throws UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException
     * @deprecated Use {@link #getMOSJParameters()} instead.
     */
    public List<MOSJParameter> getMOSJParameters(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        /*doQuery(params);
        JSONArray returnedObjects = getEntries();
        
        List<MOSJParameter> list = new ArrayList<MOSJParameter>();
        
        if (returnedObjects != null) {
            for (int i = 0; i < returnedObjects.length(); i++) {
                try {
                    list.add(new MOSJParameter(returnedObjects.getJSONObject(i)));
                } catch (Exception e) {
                    throw new InstantiationException("Error: " + e.getMessage());
                }
            }
        }
        return list;*/
        addParameters(params);
        return getMOSJParameters();
    }
    
    /**
     * Queries the service using the current parameters and returns all (if any)
     * MOSJ parameters, generated from the service response.
     * 
     * @return A list of all MOSJ parameters, generated from the service response, or an empty list (= no matches).
     * @see APIService#doQuery(java.util.Map) 
     * @see APIService#getEntries() 
     * @throws UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException
     */
    public List<MOSJParameter> getMOSJParameters() 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        //doQuery(params);
        JSONArray returnedObjects = doQuery(getParameters()).getEntries();
        
        List<MOSJParameter> list = new ArrayList<MOSJParameter>();
        
        if (returnedObjects != null) {
            for (int i = 0; i < returnedObjects.length(); i++) {
                try {
                    list.add(new MOSJParameter(returnedObjects.getJSONObject(i)));
                } catch (Exception e) {
                    throw new InstantiationException("Error: " + e.getMessage());
                }
            }
        }
        return list;
    }
    
    /**
     * Gets a single {@link MOSJParameter}, identified by the given ID.
     * <p>
     * ToDo: Create a MOSJParameter OR a TimeSeries instance. doQuery returns a 
     * JSONObject with a "collection" property at the root level; its value will 
     * be "parameter" or "timeseries", depending on the type of entry. This also 
     * means introducing a base class / interface (e.g. MonitoringData), which 
     * should then be the type returned by this method.
     * 
     * @param id The parameter ID.
     * @return The {@link MOSJParameter} identified by the given ID, or null if no such entry exists.
     * @see APIService#doRead(java.lang.String, java.lang.String) 
     */
    @Override
    public MOSJParameter get(String id) {
        try  {
            return new MOSJParameter(this.doRead(id, this.getParameterBaseURL()), displayLocale);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read MOSJ parameter with ID "+id, e);
            }
            return null;
        }
    }
    
    /**
     * Gets a single MOSJ parameter that comprises any time series returned by
     * the service as a result of a time series query using the given keywords.
     * 
     * @param keywords The keywords to use when querying for time series.
     * @param title The parameter title.
     * @param id The parameter ID.
     * @see MOSJParameter#MOSJParameter(java.lang.String, java.lang.String, org.opencms.json.JSONObject, java.util.Locale) 
     * @return  A MOSJ parameter that comprises any time series identified by the given keywords.
     */
    public MOSJParameter get(String keywords, String title, String id) {
        JSONObject tsQueryResult = APIUtil.queryService(
                this.getTimeSeriesBaseURL() 
                + "?" + modFilter("keywords.@value") + "=" + keywords 
                + "&" + Param.FORMAT + "=" + ParamVal.FORMAT_JSON
        );
        
        try {
            return new MOSJParameter(title, id, tsQueryResult.getJSONObject(Key.FEED), displayLocale);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create MOSJ parameter with ID '" + id + "' based on keywords string '" + keywords + "'.", e);
            }
            return null;
        }
    }
    
    /**
     * Creates a collection of time series, based on the given details, and with
     * no collection URL.
     * 
     * @param timeSeriesIds A list of {@link TimeSeries time series} IDs.
     * @param title The collection title. This is the chart / MOSJ parameter title.
     * @return A collection of time series.
     */
    public TimeSeriesCollection createTimeSeriesCollection(List<String> timeSeriesIds, String title) {
        return createTimeSeriesCollection(timeSeriesIds, title, null);
    }
    
    /**
     * Creates a collection of time series, based on the given details.
     * 
     * @param timeSeriesIds A list of {@link TimeSeries time series} IDs.
     * @param title The collection title. This is the chart / MOSJ parameter title.
     * @param url The URL for the collection, typically a Data Centre query-for-time-series URI.
     * @return A collection of time series.
     */
    public TimeSeriesCollection createTimeSeriesCollection(List<String> timeSeriesIds, String title, String url) {
        if (timeSeriesIds == null) {
            return null;
        }
        
        List<TimeSeries> tss = new ArrayList<TimeSeries>(timeSeriesIds.size());
        try {
            for (String id : timeSeriesIds) {
                try {
                    tss.add(this.getTimeSeries(id));
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Unable to add time series with ID '" + id + "' to collection.", e);
                    }
                }
            }
            
            if (url == null || url.isEmpty()) {
                url = getTimeSeriesBaseURL() 
                        + "?q=" 
                        + "&" + Param.FORMAT + "=" + ParamVal.FORMAT_JSON
                        + "&" + Param.RESULTS_LIMIT + "=" + timeSeriesIds.size()
                        + "&" + Param.FACETS + "=" + ParamVal.FACETS_NONE
                        + "&" + modFilter(TimeSeries.Key.SYSTEMS) + "=" + TimeSeries.Val.ORG_MOSJ_GENERIC
                        + "&" + modFilter(TimeSeries.Key.ID) + "=" + combine(Delimiter.OR, timeSeriesIds.toArray(new String[timeSeriesIds.size()]))
                        ;
            }

            return new TimeSeriesCollection(displayLocale, tss, title, url);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error creating time series collection defined at '" + url + "'.", e);
            }
            return null;
        }
    }
    
    /**
     * Gets a single {@link TimeSeries}, identified by the given ID.
     * 
     * @param id The time series' ID.
     * @return The {@link TimeSeries} identified by the given ID, or null if no such entry exists.
     * @see APIService#doRead(java.lang.String, java.lang.String) 
     */
    public TimeSeries getMOSJTimeSeries(String id) {
        try  {
            return new TimeSeries(doRead(id, this.getTimeSeriesBaseURL()), displayLocale);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read MOSJ time series with ID "+id, e);
            }
            return null;
        }
    }
    
    /**
     * @deprecated Use {@link #get(java.lang.String)} instead.
     */
    public MOSJParameter getMOSJParameter(String id) {
        return get(id);
    }
    /**
     * @see APIService#initDefaultParameters() 
     */
    private void initPresetParameters() {
        initUnmodifiableParameters();
        initDefaultParameters();
    }
    /**
     * @see APIService#initUnmodifiableParameters() 
     */
    private void initUnmodifiableParameters() {
        unmodifiableParams.put(
                APIService.modFilter(APIEntry.Key.SYSTEMS),
                toParamVal(MOSJService.ParamVal.SYSTEM_MOSJ)
        );
    }
    /**
     * @see APIService#initDefaultParameters() 
     */
    private void initDefaultParameters() {
        // Nothing here yet
    }
    
    /**
     * Gets a single {@link TimeSeries}, identified by the given ID.
     * 
     * @param id The ID.
     * @return The {@link TimeSeries} identified by the given ID, or null if no such entry exists.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     * @see APIService#doRead(java.lang.String, java.lang.String) 
     */
    public TimeSeries getTimeSeries(String id) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        return new TimeSeries( this.doRead(id, this.getTimeSeriesBaseURL()), displayLocale);
        
    }
    /*
    public List<TimeSeries> queryTimeSeries(String queryUrl) {
        
    }
    */
    /**
     * @see APIService#getDefaultParameters()
     */
    
    /*@Override
    public Map<String, String[]> getDefaultParameters() {
        if (defaultParams == null || defaultParams.isEmpty()) {
            defaultParams = new HashMap<String, String[]>();
            //defaultParams.put("not-draft", new String[]{ "yes" });
        }
        defaultParams.putAll(getUnmodifiableParameters());
        return defaultParams;
    }*/
    
    /*@Override
    protected void initDefaultParameters() {  
        super.initDefaultParameters();
    }*/
    
    /**
     * @see APIService#getUnmodifiableParameters() 
     */
    /*@Override
    public Map<String, String[]> getUnmodifiableParameters() {
        //Map<String, String[]> unmodParams = new HashMap<String, String[]>();
        //unmodParams.put("format", new String[]{ "json" });
        //unmodParams.put("filter-systems", new String[] { "mosj.no" });
        if (unmodifiableParams == null) {
            initUnmodifiableParameters();
        }
        return super.getUnmodifiableParameters();
    }*/
    
    /**
     * @see APIServiceInterface#getServiceBaseURL() 
     */
    @Override
    public String getServiceBaseURL() { return getServiceProtocol() + "://" + SERVICE_DOMAIN_NAME + "/" + SERVICE_PATH; }
    
    /**
     * @see APIServiceInterface#getServicePath() 
     */
    @Override
    public String getServicePath() { return SERVICE_PATH; }
    
    /**
     * Gets the configured protocol used by this service instance.
     * 
     * @return The service protocol (e.g. "http" or "https").
     */
    public String getServiceProtocol() { return this.serviceProtocol; }
    
    /**
     * Gets the URL path addon for time series entries.
     * 
     * @return The URL path addon for time series entries.
     */
    public String getTimeSeriesServicePath() { return SERVICE_PATH_TIMESERIES; }
    
    /**
     * Gets the URL path addon for parameter entries.
     * 
     * @return The URL path addon for parameter entries.
     */
    public String getParameterServicePath() { return SERVICE_PATH_PARAMETER; }
    
    /**
     * Gets the base URL for MOSJ time series entries.
     * <p>
     * Append an ID to get the URL of a single time series entry.
     * 
     * @return The base URL for MOSJ time series entries.
     */
    public String getTimeSeriesBaseURL() { return getServiceBaseURL() + SERVICE_PATH_TIMESERIES; }
    
    /**
     * Gets the base URL for MOSJ parameter entries.
     * <p>
     * Append an ID to get the URL of a single parameter entry.
     * 
     * @return The base URL for MOSJ parameter entries.
     */
    public String getParameterBaseURL() { return getServiceBaseURL() + SERVICE_PATH_PARAMETER; }
    
    //##########################################################################
    //
    //                  Horrible stuff below, must fix later!!!
    //      (Commented out everything and hoping none of these are in use :)
    //
    //##########################################################################
    /*
    public MOSJParameter getMOSJParameter(String id) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        String queryUri = getServiceBaseURL() + SERVICE_PATH_PARAMETER + id;
        
        MOSJParameter mp = null;
        
        try {
            JSONObject mosjParameterJson = new JSONObject(httpResponseAsString(queryUri));
            mp = new MOSJParameter(mosjParameterJson, displayLocale);
        } catch (Exception e) { 
            if (LOG.isErrorEnabled()) {
                LOG.error("Error creating MOSJ parameter using URL " + queryUri + ".", e);
            }
        }
        
        return mp;
    }
    //*/
    /*
    public TimeSeries getTimeSeries(String timeSeriesId) {      
        if (timeSeriesId == null) 
            return null;
        
        
        
        String timeSeriesUrl = null;
        if (!timeSeriesId.startsWith(SERVICE_PROTOCOL)) {
            // Not full URL, assume it's an ID
            timeSeriesUrl = getServiceBaseURL() + SERVICE_PATH_TIMESERIES + timeSeriesId;
            //timeSeriesUrl = SERVICE_BASE_URL + SERVICE_PATH_TIMESERIES + timeSeriesId;
        } else {
            timeSeriesUrl = timeSeriesId;
        }
            
        try {
            JSONObject timeSeriesJson = new JSONObject(httpResponseAsString(timeSeriesUrl)).getJSONObject(API_KEY_FEED);
            return new TimeSeries(timeSeriesJson, displayLocale);
        } catch (Exception e) {
            //System.out.println("Fatal error getting time series for parameter '" + parameterID + "':" + e.getMessage());
            //e.printStackTrace();
            // Log this
            if (LOG.isErrorEnabled()) {
                LOG.error("Fatal error getting time series identified as '" + timeSeriesId + "'.", e);
            }
            return null;
        }
    }
    //*/
    
    /**
    public List<TimeSeries> getTimeSeriesForMOSJParameter(String mosjParameterId) {
        List<TimeSeries> relatedTimeseries = new ArrayList<TimeSeries>();
        // This is so not right ... need to tidy up later tho, no time now before deadline
        String queryUri = SERVICE_BASE_URL + SERVICE_PATH_TIMESERIES + "?"
                // DidDo: Remove TEMPORARY path SERVICE_PATH_PARAMETER_AS_QUERY with real path SERVICE_PATH_PARAMETER
                //+ "&filter-links.href=" + SERVICE_PATH_PARAMETER_AS_QUERY + mosjParameterId 
                + "&filter-links.href=" + SERVICE_BASE_URL + SERVICE_PATH_PARAMETER + mosjParameterId 
                + "&filter-rel=parameter"
                + "&filter-systems=mosj.no"
                + "&q=&format=json&facets=false&limit=all";
        
        //System.out.println("Querying for related time series using URL: " + queryUri);
        
        try {
            JSONObject json = new JSONObject(httpResponseAsString(queryUri)).getJSONObject(API_KEY_FEED);
            
            JSONObject opensearch = json.getJSONObject(API_KEY_OPENSEARCH);
            
            int numHits = opensearch.getInt(API_KEY_OPENSEARCH_TOTAL_RESULTS);
            //System.out.println("Found " + numHits + " related time series.");
            if (numHits > 0) {
                JSONArray tsEntries = json.getJSONArray(API_KEY_ENTRIES); // Time series entries
                for (int i = 0; i < tsEntries.length(); i++) {
                    TimeSeries ts = new TimeSeries(tsEntries.getJSONObject(i), displayLocale);
                    //System.out.println(" - '" + ts.getTitle(displayLocale) + "'");
                    relatedTimeseries.add(ts);
                }
            }
        } catch (Exception e) {
            //System.out.println("Fatal error getting time series for parameter '" + parameterID + "':" + e.getMessage());
            //e.printStackTrace();
            // Log this
            if (LOG.isErrorEnabled()) {
                LOG.error("Fatal error getting time series for MOSJ parameter '" + mosjParameterId + "'.", e);
            }
            return null;
        }
        return relatedTimeseries;
    }//*/
}
