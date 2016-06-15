package no.npolar.data.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONObject;

/**
 * Represents a facet filter.
 * <p>
 * This implementation's counterpart can be found in the "facets" field in 
 * service's JSON responses. A search filter is basically a 
 * <strong>term</strong>, a <strong>URI</strong> and a 
 * <strong>hit counter</strong>, with some additional features, like:
 * <ul>
 * <li>Methods to evaluate if a filter is currently active or not</li>
 * <li>...</li>
 * </ul>
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class SearchFilter {
    protected String term = null;
    protected int count = -1;
    protected String uri = null;
    protected String filterField = null;
    protected boolean isActive = false;
    protected String serviceUri = null;
    
    private Map<String, String> params = null;
    
    /** JSON key: Term. */
    public final String JSON_KEY_TERM = "term";
    /** JSON key: Count. */
    public final String JSON_KEY_COUNT = "count";
    /** JSON key: URI. */
    public final String JSON_KEY_URI = "uri";
    
    /** The prefix used on names of parameters that are used for filtering. */
    public static final String PARAM_NAME_PREFIX = "filter-";
    
    /** Pattern used to create/normalize timestamps, in order to use them as filter values. */
    public static final String PATTERN_DATETIME_FILTER = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // e.g. 1871-06-01T12:00:00Z
    
    /**
     * Creates a new filter for the given field, based on the given details.
     * 
     * @param filterField The field this filter is filtering on (e.g. "category") - normally the facet name.
     * @param term The term for this filter.
     * @param count The number of matches for this filter.
     * @param uri The URI for this filter.
     */
    public SearchFilter(String filterField, String term, int count, String uri) {
        if (filterField == null || term == null || count < 0 || uri == null) {
            throw new NullPointerException("Invalid constructor argument(s). Term and URI must be not null, and count must be non-negative.");
        }
        this.filterField = filterField;
        this.term = term;
        this.count = count;
        this.uri = uri;
        
        init();
    }
    
    /**
     * Creates a new filter for the given field, based on the given filter object.
     * 
     * @param filterField The field this filter is filtering on (e.g. "category") - normally the facet name.
     * @param filterObject A JSON representation of the filter.
     */
    public SearchFilter(String filterField, JSONObject filterObject) {
        try {
            this.filterField = filterField;
            if (filterField == null)
                throw new NullPointerException("A filter field is required when creating filters.");
            
            this.term = filterObject.getString(JSON_KEY_TERM);
            this.count = filterObject.getInt(JSON_KEY_COUNT);
            this.uri = filterObject.getString(JSON_KEY_URI);
        } catch (Exception e) {
            throw new NullPointerException("Invalid JSON object. Term and URI must be not null, and count must be non-negative.");
        }
        
        init();
    }
    
    /**
     * Creates a new filter for the given field, based on the given filter object
     * and with a state evaluated against the given service URI.
     * 
     * @param filterField The field this filter is filtering on (e.g. "category") - normally the facet name.
     * @param filterObject A JSON representation of the filter.
     * @param serviceUri The URI to the service API that corresponds to the currently displayed client page.
     */
    public SearchFilter(String filterField, JSONObject filterObject, String serviceUri) {
        try {
            this.filterField = filterField;
            if (filterField == null)
                throw new NullPointerException("A filter field is required when creating filters.");
            
            this.term = filterObject.getString(JSON_KEY_TERM);
            this.count = filterObject.getInt(JSON_KEY_COUNT);
            
            //
            // NOTE: examples in the next two comments (about serviceUri & uri)
            // are based on 2 topic filters being active on the current page: 
            // "marine" and "biology"
            //
            
            // The URI of the current page (or: the currently active filtering)
            // Will contain "filter-topics=marine,biology"
            this.serviceUri = serviceUri;
            
            // The URI for this filter, which when requested will either 
            //  - enable (if currently inactive) this filter, OR
            //  - disable (if currently active) this filter.
            //
            // The (non-active) filter for enabling "ecology" will contain "filter-topics=marine,biology,ecology" 
            // The (active) filter for disabling "marine" will contain "filter-topics=biology"
            // The (active) filter for disabling "biology" will contain "filter-topics=marine"
            this.uri = filterObject.getString(JSON_KEY_URI); 
            
            
        } catch (Exception e) {
            throw new NullPointerException("Invalid JSON object. Term and URI must be not null, and count must be non-negative.");
        }
        
        //System.out.println("Created filter: " + filterField + "." + term + (isActive ? " (ACTIVE)" : ""));
        //System.out.println("  URL is " + uri);
        init();
    }
    
    /**
     * Performs additional initialization on the filter, like setting start=0
     * and evaluating the state (on/off).
     */
    private void init() {
        this.params = APIUtil.getParametersInQueryString(uri);
        // ALL filters should have start=0, force this
        //removeParam("start");
        addParam("start", "0");
        //uri = uri + "&start=0";
        
        // Evaluate state: is this filter currently active?
        if (serviceUri != null) {
            try {
                // We can base the "active" state simply on the size of the 
                // filter's value(s), because at any given time, the following
                // will be true: 
                //
                // Comparing with this filter's value(s) in the current URI:
                //  1.) Any NON-ACTIVE filter will have 1 more value
                //  2.) Any ACTIVE filter will have 1 less value (possibly none)
                //
                // As such, a filter is active ONLY when its number of values is 
                // less than the number of values reflected by the current URI.
                
                // e.g. (filter-topic=)"biology,marine"
                String currentValue = APIUtil.getParametersInQueryString(serviceUri).get(APIService.modFilter(filterField));
                // e.g. (filter-topic=)"biology,marine,ecology" (currently non-active)
                // or   (filter-topic=)"biology" (currently active)
                String thisFiltersValue = params.get(APIService.modFilter(filterField));
                        
                        
                        
                // Handle multiple comma-separated values
                List<String> currentValues = new ArrayList<String>();
                try { currentValues = Arrays.asList(currentValue.split(APIService.Delimiter.AND.toString())); } catch (Exception ignore) {}
                
                List<String> thisFiltersValues = new ArrayList<String>(); 
                try { thisFiltersValues = Arrays.asList(thisFiltersValue.split(APIService.Delimiter.AND.toString())); } catch (Exception ignore) {}
                
                // Determine the active state from on the number of values in 
                // this filter vs. the number of values currently in the URI
                if (currentValues.size() > thisFiltersValues.size()) {
                    this.isActive = true;
                }
                /*
                // Old version, from when getParametersInQueryString returned Map<String, List<String>>
                List<String> currentValues = APIUtil.getParametersInQueryString(serviceUri).get(PARAM_NAME_PREFIX + filterField);
                if (currentValues == null)
                    currentValues = new ArrayList<String>();
                
                List<String> thisFiltersValues = APIUtil.getParametersInQueryString(uri).get(PARAM_NAME_PREFIX + filterField);
                if (thisFiltersValues == null)
                    thisFiltersValues = new ArrayList<String>();

                if (currentValues.size() > thisFiltersValues.size())
                    this.isActive = true;
                */
            } catch (Exception e) {
                throw new NullPointerException("Unable to evaluate state: " + e.getMessage());
            }
        }
    }
    
    /**
     * Removes a given parameter from the filter's URI.
     * 
     * @param paramName The parameter name.
     * @return The filter URI, with the parameter identified by the given name removed.
     */
    public SearchFilter removeParam(String paramName) {
        //String[] uriAndParams = uri.split("\\?");
        //Map<String, List<String>> params = APIUtil.getParametersInQueryString(uri);
        //System.out.println("ABOUT to remove '" + paramName + "' from '" + uri + "'");
        //Map<String, String> params = APIUtil.getParametersInQueryString(uri);
        params.remove(paramName);
        updateUri();
        //uri = getUrlPartBase() + (!params.isEmpty() ? ("?" + APIUtil.getParameterString(params)) : "");
        //System.out.println("REMOVED " + paramName + ". Result: " + uri);
        return this;
    }
    /**
     * Updates the URI, based on the parameters currently set.
     * <p>
     * This method must be invoked to reflect any changes in the parameters.
     * 
     */
    protected void updateUri() {
        uri = getUrlPartBase() + (!params.isEmpty() ? ("?" + APIUtil.getParameterString(params)) : "");
    }
    
    /**
     * Adds a parameter (key-value pair).
     * <p>
     * Note: Overwrites any preexisting parameter using the same key.
     * 
     * @param key The parameter key
     * @param value The parameter value
     * @return This instance, updated.
     */
    public SearchFilter addParam(String key, String value) {
        params.put(key, value);
        updateUri();
        return this;
    }
    
    /**
     * Sets the "base URL" (everything preceding the query string) for this 
     * filter.
     * <p>
     * For example, calling setBaseUrl("http://bar.org/kek") will change this 
     * filter's base URL from the preexisting 
     * http://foo.com/lol?x=y to 
     * http://bar.org/kek?x=y
     * 
     * @param baseUrl The new base URL.
     * @return This instance, updated.
     */
    public SearchFilter setBaseUrl(String baseUrl) {
        String queryString = APIUtil.getQueryString(uri);
        uri = baseUrl + (!queryString.isEmpty() ? "?".concat(queryString) : "");
        /*String[] uriAndParams = uri.split("\\?");
        uri = baseUrl + "?" + uriAndParams[1];*/
        return this;
    }
    /**
     * Gets the URL for this filter.
     * 
     * @return The URL for this filter.
     */
    public String getUrl() {
        return uri;
    }
    
    /**
     * Gets the parameter string from the filter's URI, that is, everything
     * after the first ? in the URI.
     * 
     * @return The parameter string from this filter's URI.
     */
    public String getUrlPartParameters() {
        return APIUtil.getQueryString(uri);
        //return uri.split("\\?")[1];
    }
    
    /**
     * Gets the base part of this filter's URI, that is, everything before the
     * first ? in the URI.
     * 
     * @return The base part of this filter's URI.
     */
    public String getUrlPartBase() {
        return uri.split("\\?")[0];
    }
    
    /**
     * Gets the filter's term.
     * 
     * @return The filter's term.
     */
    public String getTerm() {
        return term;
    }
    
    /**
     * Gets the number of matches for this filter.
     * 
     * @return The number of matches for this filter.
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Gets a flag indicating whether or not this filter is active.
     * 
     * @return True if this filter is active, false if not.
     */
    public boolean isActive() {
        return this.isActive;
    }
    
    /**
     * Gets an HTML representation of this filter.
     * 
     * @return An HTML representation of this filter.
     */
    public String toHtml() {
        // ToDo: Make this an add / remove filter, based on the current uri
        String s = "";
        s += "<a" 
                + (isActive() ? " class=\"filter--active\"" : "")
                + " href=\"" + uri + "\">"
                    + term
                + "</a>";
        return s;
    }
}
