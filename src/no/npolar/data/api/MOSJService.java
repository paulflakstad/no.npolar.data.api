package no.npolar.data.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
//import no.npolar.data.api.APIService;
//import no.npolar.data.api.Labels;
//import no.npolar.data.api.TimeSeries;
import no.npolar.data.api.mosj.MOSJParameter;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * ToDo: In addition to this MOSJ-specific service, there should be an agnostic, 
 *          generic class TimeSeriesService / MonitoringService. This class could 
 *          then probably extend that one.
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class MOSJService extends APIService {
    
    /** The URL path to use when accessing the service. */
    public static final String SERVICE_PATH = "indicator/";
    //protected static final String SERVICE_PATH = "/monitoring/parameter/";
    
    /** The base URL (that is, the complete URL before adding parameters) to use when accessing the service. */
    //protected static final String SERVICE_BASE_URL = SERVICE_PROTOCOL + "://" + SERVICE_DOMAIN_NAME + "/" + SERVICE_PATH;
    //protected static final String SERVICE_BASE_URL = SERVICE_PROTOCOL + "://" + "apptest.data.npolar.no" + ":" + 9000 + SERVICE_PATH;
    //protected static final String SERVICE_BASE_URL = SERVICE_PROTOCOL + "://" + SERVICE_DOMAIN_NAME + ":" + SERVICE_PORT + SERVICE_PATH;
    
    public static final String SERVICE_PATH_TIMESERIES = "timeseries/";
    //protected static final String SERVICE_PATH_TIMESERIES = "http://apptest.data.npolar.no:9000/monitoring/timeseries/";
    
    // DidDo: Remove TEMPORARY path "XXX_AS_QUERY" with real one below  ...
    //protected static final String SERVICE_PATH_PARAMETER_AS_QUERY = "http://localhost:9393/monitoring/parameter/";
    public static final String SERVICE_PATH_PARAMETER = "parameter/";
    //protected static final String SERVICE_PATH_PARAMETER = "http://apptest.data.npolar.no:9000/monitoring/parameter/";
    
    /** Translations. */
    protected ResourceBundle labels = null;
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(MOSJService.class);
    
    protected String serviceProtocol = null;
    //protected String serviceBaseUrl = null;
    
    /**
     * Creates a new service instance, configured with the given locale.
     * @param loc The locale to use when generating strings for screen view. If null, the default locale is used.
     * @param secure Set to true to use https, false to use http. 
     */
    public MOSJService(Locale loc, boolean secure) {
        this.serviceProtocol = secure ? "https" : "http";
        
        displayLocale = loc;
        if (displayLocale == null)
            displayLocale = new Locale(DEFAULT_LOCALE_NAME);
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
    }
    
    /*
    public List<JSONObject> getParameter(String id) {
        
    }
    //*/
    
    /**
     * Queries the service using the given parameters and returns all (if any)
     * MOSJ parameters.
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
     */
    public List<MOSJParameter> getMOSJParameters(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        doQuery(params);
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
        return list;
    }
    
    /**
     * @see APIService#getDefaultParameters()
     */
    @Override
    public Map<String, String[]> getDefaultParameters() {
        if (defaultParams == null || defaultParams.isEmpty()) {
            defaultParams = new HashMap<String, String[]>();
            //defaultParams.put("not-draft", new String[]{ "yes" });
        }
        defaultParams.putAll(getUnmodifiableParameters());
        return defaultParams;
    }
    
    /**
     * @see APIService#getUnmodifiableParameters() 
     */
    @Override
    public Map<String, String[]> getUnmodifiableParameters() {
        Map<String, String[]> unmodParams = new HashMap<String, String[]>();
        unmodParams.put("format", new String[]{ "json" });
        unmodParams.put("filter-systems", new String[] { "mosj.no" });
        return unmodParams;
    }
    
    //##########################################################################
    //
    //                  Horrible stuff below, must fix later!!!
    //
    //##########################################################################
    
    public MOSJParameter getMOSJParameter(String id) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        //List<TimeSeries> relatedTimeSeries = getTimeSeriesForMOSJParameter(id);
        
        // This is so not right ... need to tidy up later tho, no time now before deadline
        String queryUri = getServiceBaseURL() + SERVICE_PATH_PARAMETER + id;
        
        MOSJParameter mp = null;
        JSONObject mosjParameterJson = null;
        try {
            mosjParameterJson = new JSONObject(httpResponseAsString(queryUri));
            mp = new MOSJParameter(mosjParameterJson, displayLocale);
            //mp.addAllTimeSeries(relatedTimeSeries);
        } catch (Exception e) { 
            // ToDo: Log this
            LOG.error("Error creating MOSJ parameter using API URI " + queryUri + ".", e);
            return null;
        }
        // Code below moved to MOSJParameter
        /*
        try {
            mosjParameterJson.has(MOSJParameter.API_KEY_RELATED_TIME_SERIES);
            ArrayList<TimeSeries> relatedTimeSeriesList = new ArrayList<TimeSeries>();
            JSONArray relatedTimeSeriesArr = mosjParameterJson.getJSONArray(MOSJParameter.API_KEY_RELATED_TIME_SERIES); // Each array entry is a URLs to a related time series
            for (int i = 0; i < relatedTimeSeriesArr.length(); i++) {
                try {
                    String relatedTimeSeriesUrl = relatedTimeSeriesArr.getString(i);
                    relatedTimeSeriesList.add(getTimeSeries(relatedTimeSeriesUrl));
                } catch (Exception ee) {
                    // Log this?
                }
            }
            mp.addAllTimeSeries(relatedTimeSeriesList);
        } catch (Exception e) {
            // Parameter has no related time series
        }
        */
        return mp;
    }
    
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
    
    public String getServiceProtocol() { return this.serviceProtocol; }
    public String getTimeSeriesServicePath() { return SERVICE_PATH_TIMESERIES; }
    public String getParameterServicePath() { return SERVICE_PATH_PARAMETER; }
    public String getTimeSeriesBaseURL() { return getServiceBaseURL() + SERVICE_PATH_TIMESERIES; }
    public String getParameterBaseURL() { return getServiceBaseURL() + SERVICE_PATH_PARAMETER; }
}
