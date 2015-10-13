package no.npolar.data.api;

import java.util.ArrayList;
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
    
    /** JSON key: Term. */
    public final String JSON_KEY_TERM = "term";
    /** JSON key: Count. */
    public final String JSON_KEY_COUNT = "count";
    /** JSON key: URI. */
    public final String JSON_KEY_URI = "uri";
    
    /** The prefix used on names of parameters that are used for filtering. */
    public static final String PARAM_NAME_PREFIX = "filter-";
    
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
            this.uri = filterObject.getString(JSON_KEY_URI);
            this.serviceUri = serviceUri;
        } catch (Exception e) {
            throw new NullPointerException("Invalid JSON object. Term and URI must be not null, and count must be non-negative.");
        }
        
        init();
        //System.out.println("Created filter: " + filterField + "." + term + (isActive ? " (ACTIVE)" : ""));
    }
    
    /**
     * Performs additional initialization on the filter, like setting start=0
     * and evaluating the state (on/off).
     */
    private void init() {
        // ALL filters should have start=0, fix this
        removeParam("start");
        uri += "&start=0";
        
        // Evaluate state: is this filter currently active?
        if (serviceUri != null) {
            try {
                List<String> currentValues = APIUtil.getQueryParametersFromString(serviceUri).get(PARAM_NAME_PREFIX + filterField);
                if (currentValues == null)
                    currentValues = new ArrayList<String>();

                List<String> thisFiltersValues = APIUtil.getQueryParametersFromString(uri).get(PARAM_NAME_PREFIX + filterField);
                if (thisFiltersValues == null)
                    thisFiltersValues = new ArrayList<String>();

                if (currentValues.size() > thisFiltersValues.size())
                    this.isActive = true;
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
        String[] uriAndParams = uri.split("\\?");
        Map<String, List<String>> params = APIUtil.getQueryParametersFromString(uri);
        params.remove(paramName);
        uri = uriAndParams[0] + "?" + APIUtil.getParameterString(params);
        return this;
    }
    
    /**
     * Sets the base URL for this filter.
     * 
     * @param baseUrl The new base URL.
     * @return This filter, after modification.
     */
    public SearchFilter setBaseUrl(String baseUrl) {
        String[] uriAndParams = uri.split("\\?");
        uri = baseUrl + "?" + uriAndParams[1];
        return this;
    }
    /**
     * Gets the URL for this filter.
     * 
     * @return The URL for this filter.
     */
    public String getUrl() { return uri; }
    /**
     * Gets the parameter string from the filter's URI, that is, everything
     * after the first ? in the URI.
     * 
     * @return The parameter string from this filter's URI.
     */
    public String getUrlPartParameters() { return uri.split("\\?")[1]; }
    /**
     * Gets the base part of the filter's URI, that is, everything before the
     * first ? in the URI.
     * 
     * @return The base part of the filter's URI.
     */
    public String getUrlPartBase() { return uri.split("\\?")[0]; }
    
    /**
     * Gets the filter term.
     * 
     * @return The filter term.
     */
    public String getTerm() { return term; }
    
    /**
     * Gets the number of matches for this filter.
     * @return The number of matches for this filter.
     */
    public int getCount() { return count; }
    
    /**
     * Gets a flag indicating whether or not this filter is active.
     * 
     * @return True if this filter is active, false if not.
     */
    public boolean isActive() { return this.isActive; }
    
    /**
     * Gets an HTML representation of this filter.
     * 
     * @return An HTML representation of this filter.
     */
    public String toHtml() {
        // ToDo: Make this an add / remove filter, based on the current uri
        String s = "";
        s += "<a href=\"" + uri + "\">" + term + "</a>";
        return s;
    }
}
