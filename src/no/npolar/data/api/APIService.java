package no.npolar.data.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
//import no.npolar.util.CmsAgent;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;


/**
 * Base class for accessing the Norwegian Polar Institute Data Centre web services.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public abstract class APIService implements APIServiceInterface {
    
    public class Key {
        public static final String FEED = "feed";
        public static final String OPENSEARCH = "opensearch";
        public static final String OPENSEARCH_TOTAL_RESULTS = "totalResults";
        public static final String OPENSEARCH_ITEMS_PER_PAGE = "itemsPerPage";
        public static final String OPENSEARCH_START_INDEX = "startIndex";
        public static final String LIST = "list";
        public static final String LIST_SELF = "self";
        public static final String LIST_FIRST = "first";
        public static final String LIST_LAST = "last";
        public static final String LIST_NEXT = "next";
        public static final String LIST_PREVIOUS = "previous";
        public static final String SEARCH = "search";
        public static final String SEARCH_QUERY_TIME = "qtime";
        public static final String SEARCH_QUERY = "q";
        public static final String ENTRIES = "entries";
        public static final String FACETS = "facets";
    }
    
    public class Param {
        /** Request parameter: The query string. */
        public static final String QUERY = "q";
        /** Request parameter: How many results to fetch (max). */
        public static final String RESULTS_LIMIT = "limit";
        /** Request parameter: What fields to fetch. Define as a comma-separated string. */
        public static final String FIELDS = "fields";
        /** Request parameter: What facets to fetch. Defined as a comma-separated string. Use "false" for none. See {@link https://github.com/npolar/icelastic#faceting}. */
        public static final String FACETS = "facets";
        /** Request parameter: How many items of each facet to fetch. See {@link https://github.com/npolar/icelastic#faceting}. */
        public static final String FACETS_SIZE = "size-facet";
        /** Request parameter: What field to sort by. (Prefix the field name with {@link #PARAM_VAL_PREFIX_REVERSE} for reversed order.) */
        public static final String SORT_BY = "sort";
        /** Request parameter: The result number to start at. See also {@link #API_KEY_OPENSEARCH_START_INDEX}. */
        public static final String START_AT = "start";
        /** Request parameter: The desired format of the response. Usually {@link #PARAM_VAL_FORMAT_JSON}. */
        public static final String FORMAT = "format";
        /** Request parameter: NOT modifier. (Used to construct negating parameters, e.g. "not-draft=yes" - which loosely translates to: IF_NOT[draft=yes].) */
        public static final String MOD_NOT = "not-";
        /** Request parameter: FILTER modifier. (Used to construct filter parameters, e.g. "filter-state=accepted|published".) */
        public static final String MOD_FILTER = "filter-";
    }
    
    public class ParamVal {
        /** Request parameter value: JSON format. */
        public static final String FORMAT_JSON = "json";
        /** Request parameter value: JSON format. */
        public static final String FORMAT_GEOJSON = "geojson";
        /** Request parameter value: No facets. */
        public static final String FACETS_NONE = "false";
        /** Request parameter value: All items of each facet – or, more precisely; just a very large number of items. */
        public static final String FACETS_SIZE_MAX = "9999";
        /** Request parameter value: Limitless number of results. */
        public static final String RESULTS_LIMIT_NO_LIMIT = "all";
        /** Request parameter value: Prefix for reversing the order. (Use as prefix to construct e.g. "sort=-published".) */
        public static final String PREFIX_REVERSE = "-";
    }
    /**
     * @deprecated Use {@link Key#FEED} instead.
     */
    public static final String API_KEY_FEED = Key.FEED;
    /**
     * @deprecated Use {@link Key#OPENSEARCH} instead.
     */
    public static final String API_KEY_OPENSEARCH = Key.OPENSEARCH;
    /**
     * @deprecated Use {@link Key#OPENSEARCH_TOTAL_RESULTS} instead.
     */
    public static final String API_KEY_OPENSEARCH_TOTAL_RESULTS = Key.OPENSEARCH_TOTAL_RESULTS;
    /**
     * @deprecated Use {@link Key#OPENSEARCH_ITEMS_PER_PAGE} instead.
     */
    public static final String API_KEY_OPENSEARCH_ITEMS_PER_PAGE = Key.OPENSEARCH_ITEMS_PER_PAGE;
    /**
     * @deprecated Use {@link Key#OPENSEARCH_START_INDEX} instead.
     */
    public static final String API_KEY_OPENSEARCH_START_INDEX = Key.OPENSEARCH_START_INDEX;
    /**
     * @deprecated Use {@link Key#LIST} instead.
     */
    public static final String API_KEY_LIST = Key.LIST;
    /**
     * @deprecated Use {@link Key#LIST_SELF} instead.
     */
    public static final String API_KEY_LIST_SELF = Key.LIST_SELF;
    /**
     * @deprecated Use {@link Key#LIST_FIRST} instead.
     */
    public static final String API_KEY_LIST_FIRST = Key.LIST_FIRST;
    /**
     * @deprecated Use {@link Key#LIST_LAST} instead.
     */
    public static final String API_KEY_LIST_LAST = Key.LIST_LAST;
    /**
     * @deprecated Use {@link Key#LIST_NEXT} instead.
     */
    public static final String API_KEY_LIST_NEXT = Key.LIST_NEXT;
    /**
     * @deprecated Use {@link Key#LIST_PREVIOUS} instead.
     */
    public static final String API_KEY_LIST_PREVIOUS = Key.LIST_PREVIOUS;
    /**
     * @deprecated Use {@link Key#SEARCH} instead.
     */
    public static final String API_KEY_SEARCH = Key.SEARCH;
    /**
     * @deprecated Use {@link Key#SEARCH_QUERY_TIME} instead.
     */
    public static final String API_KEY_SEARCH_QUERY_TIME = Key.SEARCH_QUERY_TIME;
    /**
     * @deprecated Use {@link Key#SEARCH_QUERY} instead.
     */
    public static final String API_KEY_SEARCH_QUERY = Key.SEARCH_QUERY;
    /**
     * @deprecated Use {@link Key#ENTRIES} instead.
     */
    public static final String API_KEY_ENTRIES = Key.ENTRIES;
    /**
     * @deprecated Use {@link Key#FACETS} instead.
     */
    public static final String API_KEY_FACETS = Key.FACETS;
    
    /** 
     * Request parameter: The query string. 
     * @deprecated Use {@link Param#QUERY} instead.
     */
    public static final String PARAM_QUERY = Param.QUERY;
    /** 
     * Request parameter: How many results to fetch (max). 
     * @deprecated Use {@link Param#RESULTS_LIMIT} instead.
     */
    public static final String PARAM_RESULTS_LIMIT = Param.RESULTS_LIMIT;
    /** 
     * Request parameter: How many results to fetch (max). 
     * @deprecated Use {@link #PARAM_RESULTS_LIMIT} instead.
     */
    public static final String PARAM_RESULTS_COUNT = PARAM_RESULTS_LIMIT;
    /** 
     * Request parameter: What fields to fetch (defined in a comma-separated string).
     * @deprecated Use {@link Param#FIELDS} instead. 
     */
    public static final String PARAM_FIELDS = Param.FIELDS;
    /** 
     * Request parameter: What facets to fetch. Defined as a comma-separated string. Use "false" for none. 
     * @deprecated Use {@link Param#FACETS} instead.
     */
    public static final String PARAM_FACETS = Param.FACETS;
    /** 
     * Request parameter: What field to sort by. (Prefix the field name with {@link #PARAM_VAL_PREFIX_REVERSE} for reversed order.) 
     * @deprecated Use {@link Param#SORT_BY} instead.
     */
    public static final String PARAM_SORT_BY = Param.SORT_BY;
    /** 
     * Request parameter: The result number to start at. 
     * @see #API_KEY_OPENSEARCH_START_INDEX
     * @deprecated Use {@link Param#START_AT} instead.
     */
    public static final String PARAM_START_AT = Param.START_AT;
    /** 
     * Request parameter: The desired format of the response - typically {@link #PARAM_VAL_FORMAT_JSON}. 
     * @deprecated Use {@link Param#FORMAT} instead.
     */
    public static final String PARAM_FORMAT = Param.FORMAT;
    /** 
     * Request parameter: NOT modifier. (Used to construct negating parameters, e.g. "not-draft=yes" - which loosely translates to: IF_NOT[draft=yes].) 
     * @deprecated Use {@link Param#MOD_NOT} instead.
     */
    public static final String PARAM_MODIFIER_NOT = Param.MOD_NOT;
    /** 
     * Request parameter: FILTER modifier. (Used to construct filter parameters, e.g. "filter-state=accepted|published".) 
     * @deprecated Use {@link Param#MOD_FILTER} instead.
     */
    public static final String PARAM_MODIFIER_FILTER = Param.MOD_FILTER;
    
    /** 
     * Request parameter value: JSON format. 
     * @deprecated Use {@link ParamVal#FORMAT_JSON} instead.
     */
    public static final String PARAM_VAL_FORMAT_JSON = ParamVal.FORMAT_JSON;
    /** 
     * Request parameter value: No facets. 
     * @deprecated Use {@link ParamVal#FACETS_NONE} instead.
     */
    public static final String PARAM_VAL_FACETS_NONE = ParamVal.FACETS_NONE;
    /** 
     * Request parameter value: Limitless number of results. 
     * @deprecated Use {@link ParamVal#RESULTS_LIMIT_NO_LIMIT} instead.
     */
    public static final String PARAM_VAL_RESULTS_LIMIT_NO_LIMIT = ParamVal.RESULTS_LIMIT_NO_LIMIT;
    /**
     * Request parameter value: Limitless number of results. 
     * @deprecated Use {@link #PARAM_VAL_RESULTS_LIMIT_NO_LIMIT} instead.
     */
    public static final String PARAM_VAL_RESULTS_COUNT_LIMITLESS = PARAM_VAL_RESULTS_LIMIT_NO_LIMIT;
    /** 
     * Request parameter value: Prefix for reversing the order. (Use as prefix to construct e.g. "sort=-published".) 
     * @deprecated Use {@link ParamVal#PREFIX_REVERSE} instead.
     */
    public static final String PARAM_VAL_PREFIX_REVERSE = ParamVal.PREFIX_REVERSE;
    
    /**
     * Valid delimiters for combining multiple fields or multiple field values.
     */
    public enum Delimiter { 
        AND(",")
        , OR("|")
        , RANGE("..")
        , CHILD(".");
        
        private String s;
        
        Delimiter(String s) {
            this.s = s;
        }
        @Override
        public String toString() {
            return this.s;
        };
    };
    
    //public static final String[] DELIMITERS = { ",", "|", ".." };
    //public static final int DELIM_OR = 0;
    //public static final int DELIM_AND = 1;
    //public static final int DELIM_INTERVAL = 2;
    
    /** The protocol to use when accessing the service. */
    public static final String SERVICE_PROTOCOL = "http";
    /** The domain name to use when accessing the service (programmatically). */
    public static final String SERVICE_DOMAIN_NAME = "api.npolar.no";
    /** The domain name to use when accessing the service (as a human). */
    public static final String SERVICE_DOMAIN_NAME_HUMAN = "data.npolar.no";
    /** The port to use when accessing the service. */
    public static final String SERVICE_PORT = "80";
    
    /** The character set used by the service. */
    public static final String SERVICE_CHARSET = "UTF-8";
    
    /** The full URL to the service, updated on every service request. */
    protected String serviceUrl = null;
    
    /** The variable/modifiable parameters to use when accessing the service. */
    protected Map<String, String[]> apiParams = new HashMap<String, String[]>();
    
    /** 
     * The default / hidden parameters to use when accessing the service. 
     * <p>
     * These are parameters that the user shouldn't see, nor be able to override 
     * by modifying or adding parameters in the URL manually.
     * <p>
     * Typical examples of default parameters:
     * <ul>
     * <li><code>not-draft=yes</code></li>
     * <li><code>facets=topic,type</code></li>
     * </ul>
     * <p>
     * Not to be confused with <strong>unmodifiable parameters</strong>. They are 
     * different: Default parameters <em>could</em> potentially be modified, while 
     * unmodifiable parameters are those that must <strong>never</strong> be 
     * modified (typically because that would cause the entire client to break 
     * - for example the <code>format=json</code> parameter).
     * <p>
     * <strong>Default parameters</strong> are set for each use-case (e.g. a 
     * publication list on a web page), and <strong>Unmodifiable 
     * parameters</strong> are set for each service (e.g. the publication 
     * service).
     */
    protected Map<String, String[]> defaultParams = null;//new HashMap<String, String[]>();
    
    /** The unmodifiable parameters, required for the service to behave predictably and without fundamental errors. */
    protected Map<String, String[]> unmodifiableParams = null;
    
    /** The total number of entries in the last fetch. */
    protected int totalResults = -1;
    /** The "Next page" URI, as provided by the service. */
    protected String pageUriNext = null;
    /** The "Previous page" URI, as provided by the service. */
    protected String pageUriPrev = null;
    /** The first page no, as provided by the service. */
    protected int indexNoFirstPageItem = -1;
    /** The last page no, as provided by the service. */
    protected int indexNoLastPageItem = -1;
    /** The number of items per page, as provided by the service. */
    protected int itemsPerPage = -1;
    /** The index of the first item, as provided by the service. */
    protected int startIndex = -1;
    /** The "self" URI, as provided by the service. */
    protected String self = null;
    
    /** The (last) query, as provided by the service. */
    protected String query = null;
    /** The (last) query search time, as provided by the service. */
    protected int querySearchTime = -1;
    
    /** Container for entries. */
    protected JSONArray entries = null;
    
    /** Flag indicating whether or not any user-activated filters are applied. */
    protected boolean isUserFiltered = false;
    
    /** The locale to use when generating language-specific content. */
    protected Locale displayLocale = null;
    
    /** The default locale to use when generating language-specific content. */
    public static final String DEFAULT_LOCALE_NAME = "en";
    
    /**
     * The (parameter) prefix for filters. 
     * @deprecated Use {@link Param#MOD_FILTER} instead.
     */
    public static final String FILTER_PREFIX = Param.MOD_FILTER;
    
    /** Container for filter sets. */
    protected SearchFilterSets filterSets = null;
    /**
     * Base constructor, initializes the list of preset (unmodifiable / default)
     * parameters shared across all services.
     * <p>
     * The child service class is responsible for adding its own (additional) 
     * preset parameters.
     * 
     * @see #initUnmodifiableParameters() 
     * @see #initDefaultParameters() 
     */
    public APIService() {
        initUnmodifiableParameters();
        initDefaultParameters();
    }
    
    /**
     * Unmodifiable parameter(s) shared across all services.
     */
    private void initUnmodifiableParameters() {
        if (unmodifiableParams == null) {
            unmodifiableParams = new HashMap<String, String[]>(1);
        }
        unmodifiableParams.put(
                Param.FORMAT, 
                toParamVal(ParamVal.FORMAT_JSON)
        );
    }
    
    /**
     * Initial default parameters, shared across all services.
     */
    private void initDefaultParameters() {
        if (defaultParams == null) {
            defaultParams = new HashMap<String, String[]>(0);
        }
    }
    
    public APIServiceInterface doQuery() 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        return doQuery(apiParams);
    }
    
    /**
     * @see APIServiceInterface#doQuery(java.util.Map) 
     */
    @Override
    public APIServiceInterface doQuery(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        // Make sure default parameters are set
        //params = addDefaultParameters(params);
        
        //System.out.println("doQuery using " + getParameterString(params) );
        
        addParameters(params);
        
        serviceUrl = getServiceBaseURL().concat("?").concat( prepareParameters(params) );
        
        //System.out.println("doQuery using " + getParameterString(params) );
        // We're expecting a response in JSON format
        String jsonFeed = httpResponseAsString(serviceUrl);
        JSONObject json = new JSONObject(jsonFeed).getJSONObject(Key.FEED);
        
        try { 
            JSONObject opensearch = json.getJSONObject(Key.OPENSEARCH);
            try { totalResults = opensearch.getInt(Key.OPENSEARCH_TOTAL_RESULTS); } catch (Exception innerE) { totalResults = -1; }
            try { itemsPerPage = opensearch.getInt(Key.OPENSEARCH_ITEMS_PER_PAGE); } catch (Exception innerE) { itemsPerPage = -1; }
            try { startIndex = opensearch.getInt(Key.OPENSEARCH_START_INDEX); } catch (Exception innerE) { startIndex = -1; }
        } catch (Exception e) { }
        
        try {
            JSONObject list = json.getJSONObject(Key.LIST);
            try { self = list.getString(Key.LIST_SELF); } catch (Exception innerE) { self = null; }
            try { indexNoFirstPageItem = list.getInt(Key.LIST_FIRST); } catch (Exception innerE) { indexNoFirstPageItem = -1; }
            try { indexNoLastPageItem = list.getInt(Key.LIST_LAST); } catch (Exception innerE) { indexNoLastPageItem = -1; }
            try { pageUriNext = list.getString(Key.LIST_NEXT); } catch (Exception innerE) { pageUriNext = null; }
            try { pageUriPrev = list.getString(Key.LIST_PREVIOUS); } catch (Exception innerE) { pageUriPrev = null; }
        } catch (Exception e) { }
        
        try { 
            JSONObject search = json.getJSONObject(Key.SEARCH);
            try { querySearchTime = search.getInt(Key.SEARCH_QUERY_TIME); } catch (Exception innerE) { querySearchTime = -1; }
            try { query = search.getString(Key.SEARCH_QUERY); } catch (Exception innerE) { query = null; }
        } catch (Exception e) { }
        
        try { entries = json.getJSONArray(Key.ENTRIES); } catch (Exception e) { entries = null; }
        
        //
        // Facets
        //
        filterSets = new SearchFilterSets();
        try {
            JSONArray facets = json.getJSONArray(Key.FACETS);
            //System.out.println("Found " + facets.length() + " facets.");
            for (int i = 0; i < facets.length(); i++) {
                try { 
                    JSONObject facet = facets.getJSONObject(i);
                    String facetName = facet.keys().next();
                    //System.out.println("\nFacet field is '" + facetName + "'");
                    JSONArray filters = facet.getJSONArray(facetName);
                    if (filters.length() > 0) {
                        SearchFilterSet filterSet = new SearchFilterSet(facetName);
                        //System.out.println("[" + facetName + "]filters.length() = " + filters.length());
                        for (int j = 0; j < filters.length(); j++) {
                            JSONObject filter = filters.getJSONObject(j);
                            try {
                                SearchFilter f = new SearchFilter(facetName, filter, self);
                                Iterator<String> iPresetParamKeys = this.getPresetParameters().keySet().iterator();
                                while (iPresetParamKeys.hasNext()) {
                                    String presetParamKey = iPresetParamKeys.next();
                                    f.removeParam(presetParamKey); // this is a default/unmodifiable parameter: remove it
                                    //System.out.println("\nRemoved filter parameter " + presetParamKey);
                                }
                                //System.out.println("Adding filter: " + f.getUrl());
                                filterSet.add(f);
                                if (f.isActive)
                                    this.isUserFiltered = true;
                            } catch (NullPointerException npe) { 
                                //System.out.println("EXCEPTION npe: " + npe.getMessage()); 
                            }
                        }
                        filterSets.add(filterSet);
                    }
                } catch (Exception innerE) { 
                    //System.out.println("EXCEPTION innerE: " + innerE.getMessage()); 
                }
            }
        } catch (Exception e) { }
        
        /*
        // Facets
        filterSets = new HashMap<String, List<SearchFilter>>();
        try {
            JSONArray facets = json.getJSONArray("facets");
            //System.out.println("Found " + facets.length() + " facets.");
            for (int i = 0; i < facets.length(); i++) {
                try { 
                    JSONObject facet = facets.getJSONObject(i);
                    String facetName = facet.keys().next();
                    //System.out.println("\nFacet field is '" + facetName + "'");
                    JSONArray filters = facet.getJSONArray(facetName);
                    if (filters.length() > 0) {
                        filterSets.put(facetName, new ArrayList<SearchFilter>());
                        //System.out.println("[" + facetName + "]filters.length() = " + filters.length());
                        for (int j = 0; j < filters.length(); j++) {
                            JSONObject filter = filters.getJSONObject(j);
                            try {
                                SearchFilter f = new SearchFilter(facetName, filter, self);
                                Iterator<String> iDefaultParamKeys = this.getDefaultParameters().keySet().iterator();
                                while (iDefaultParamKeys.hasNext()) {
                                    f.removeParam(iDefaultParamKeys.next());
                                }
                                filterSets.get(facetName).add(f);
                                if (f.isActive)
                                    this.isUserFiltered = true;
                            } catch (NullPointerException npe) { 
                                //System.out.println("EXCEPTION npe: " + npe.getMessage()); 
                            }
                        }
                    }
                } catch (Exception innerE) { 
                    //System.out.println("EXCEPTION innerE: " + innerE.getMessage()); 
                }
            }
        } catch (Exception e) { }
        //*/
        return this;
    }
    
    /**
     * @see APIServiceInterface#doRead(java.lang.String) 
     */
    @Override
    public JSONObject doRead(String id) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        serviceUrl = getServiceBaseURL().concat(id);
        // We're expecting a response in JSON format
        String jsonFeed = httpResponseAsString(serviceUrl);
        try {
            return new JSONObject(jsonFeed);
        } catch (Exception e) {
            // No such ID?
            return null;
        }
    }
    
    /**
     * Reads a single entry from the service using the given ID and base URL.
     * <p>
     * This allows a service to read any kind of entry, not just entries that 
     * "reside" on the service's own base URL. This is sometimes necessary, 
     * e.g. the MOSJ service needs to read both time series and parameter 
     * entries.
     * 
     * @param id The ID that uniquely identifies the single entry.
     * @param baseUrl The base URL at which the entry "resides" (the part that combines with the ID to create the entry's absolute URL).
     * @return The JSON object describing the single entry, or null if no such entry could be found.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     * @see APIServiceInterface#doRead(java.lang.String) 
     */
    public JSONObject doRead(String id, String baseUrl)
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        serviceUrl = baseUrl.concat(id);
        String jsonFeed = httpResponseAsString(serviceUrl);
        try {
            return new JSONObject(jsonFeed);
        } catch (Exception e) {
            // No such ID?
            return null;
        }
    }
    
    /**
     * Gets all currently available filter sets.
     * 
     * @return The currently available filter sets for the current entry set.
     * @see SearchFilterSet
     * @see SearchFilter
     */
    public SearchFilterSets getFilterSets() {
        return this.filterSets;
    }
    
    /**
     * Gets a flag indicating whether or not the current entry set is filtered 
     * by the end user.
     * 
     * @return True if the current entry set is filtered by the end user, false if not.
     */
    public boolean isUserFiltered() { return isUserFiltered; }
    
    /*private void isActiveFilter(SearchFilter f) {
        
    }*/
    
    /**
     * @see APIServiceInterface#getEntries() 
     */
    @Override
    public JSONArray getEntries() { return entries; }
    
    /**
     * @see APIServiceInterface#getTotalResults() 
     */
    @Override
    public int getTotalResults() { return totalResults; }
    
    /**
     * @see APIObjectInterface#getItemsPerPage()
     */
    @Override
    public int getItemsPerPage() { return itemsPerPage; }
    
    /**
     * @see APIObjectInterface#getStartIndex()
     */
    @Override
    public int getStartIndex() { return startIndex; }
    
    /**
     * Gets the full (service) URL to the next page, or null if none.
     * 
     * @return The full (service) URL to the next page, or null if none.
     */
    public String getNextPageFullUrl() { return pageUriNext.equalsIgnoreCase(String.valueOf(Boolean.FALSE)) ? null : pageUriNext; }
    
    /**
     * Gets the full (service) URL to the next page, or null if none.
     * 
     * @return The full (service) URL to the next page, or null if none.
     */
    public String getPrevPageFullUrl() { return pageUriPrev.equalsIgnoreCase(String.valueOf(Boolean.FALSE)) ? null : pageUriPrev; }
    
    /**
     * Gets the index of the first item on the current page.
     * 
     * @return The index of the first item on the current page.
     */
    public int getIndexNoFirst() { return indexNoFirstPageItem; }
    
    /**
     * Gets the index of the last item on the current page.
     * 
     * @return The index of the last item on the current page.
     */
    public int getIndexNoLast() { return indexNoLastPageItem; }
    
    /**
     * Gets the query time for the most recent (current) query.
     * 
     * @return The query time for the most recent (current) query.
     */
    public int getSearchTime() { return querySearchTime; }
    
    /**
     * Gets the search phrase used in the most recent (current) query.
     * 
     * @return The search phrase used in the most recent (current) query.
     */
    public String getLastSearchPhrase() { return query; }
    
    /**
     * Gets the query string for the current next page.
     * 
     * @return The query string for the current next page, or null if none.
     */
    public String getNextPageParameters() {
        return stripDefaultParameters(String.valueOf(pageUriNext));
    }
    
    /**
     * Gets the query string for the current previous page.
     * 
     * @return The query string for the current previous page, or null if none.
     */
    public String getPrevPageParameters() {
        return stripDefaultParameters(String.valueOf(pageUriPrev));
    }
    
    /**
     * Strips any default parameters (which should typically not be exposed to 
     * the end user) from the given URI, and returns the query string only.
     * 
     * @param uri The URI to modify.
     * @return The query string of the given URI, clean of any default parameters, or empty string.
     */
    public String stripDefaultParameters(String uri) {
        
        Map<String, String> params = APIUtil.getParametersInQueryString(uri);
        if (params.isEmpty()) {
            return "";
        }
        Iterator<String> iPresetParamKeys = getPresetParameters().keySet().iterator(); // get keys for defaults and unmodifiables
        while (iPresetParamKeys.hasNext()) {
            params.remove(iPresetParamKeys.next());
        }
        try {
            return APIUtil.getParameterString(params);
        } catch (Exception e) {
            return "";
        }
        /*
        String queryString = APIUtil.getQueryString(uri);
        if (queryString.isEmpty()) {
            return "";
        }
        
        Iterator<String> iPresetParamKeys = getPresetParameters().keySet().iterator(); // get keys for defaults and unmodifiables
        while (iPresetParamKeys.hasNext()) {
            String presetParamName = iPresetParamKeys.next();
            String[] uriAndParams = uri.split("\\?");
            Map<String, String> params = APIUtil.getParametersInQueryString(uri);
            //Map<String, List<String>> params = APIUtil.getParametersInQueryString(uri);
            params.remove(presetParamName);
            uri = uriAndParams[0] + "?" + APIUtil.getParameterString(params);
        }
        try {
            return uri.split("\\?")[1];
        } catch (Exception e) {
            return "";
        }
        */
    }
    
    
    /**
     * Adds the given and the default parameters to the set of parameters and
     * converts the String representation of those parameters, ready to append
     * to the service URL.
     * <p>
     * Any existing parameters are deleted.
     * 
     * @return The complete set of parameters, consisting of the given + default parameters.
     */
    protected String prepareParameters(Map<String, String[]> params) {
        //apiParams.clear();
        return getParameterString(getMasterParameterMap());
    }
    
    /**
     * Gets a single map that contains ALL parameters, including (ordered by
     * priority):
     * <ol>
     * <li>Unmodifiable parameters</li>
     * <li>Default parameters</li>
     * <li>Variable / user-defined parameters</li>
     * </ol>
     * @return 
     */
    protected Map<String, String[]> getMasterParameterMap() {
        // order is important here! (we don't want to overwrite the defaults / unmodifiables)
        Map<String, String[]> tmp = new HashMap<String, String[]>();
        tmp.putAll(getParameters());
        tmp.putAll(getPresetParameters());
        return tmp;
    }
    
    /**
     * Removes all parameters currently set.
     * <p>
     * Default and unmodifiable parameters are not cleared.
     * 
     * @return This instance, updated.
     */
    @Override 
    public APIServiceInterface clearParameters() {
        apiParams.clear();
        return this;
    }
    
    /**
     * @see #addParameter(java.lang.String, no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...) 
     */
    public APIServiceInterface addParameter(String key, String val) {
        return addParameter(key, null, val);
    }
    
    /**
     * Safely adds a key-value-paired query parameter to the list of freely 
     * modifiable parameters.
     * <p>
     * The given parameter will not be added if it already exists as a default 
     * or unmodifiable parameter.
     * 
     * @param key The parameter key.
     * @param value The parameter value, see {@link APIService#toParamVal(no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)} for complex variants.
     * @param del The delimiter to use, see {@link Delimiter}, when combining multiple values.
     * @param moreValues Optional additional values.
     * @return This instance, updated.
     * @see #addParameter(java.lang.String, java.lang.String) 
     */
    public APIServiceInterface addParameter(String key, Delimiter del, String value, String ... moreValues) {
        makeParameter(key, toParamVal(del, value, moreValues));
        //apiParams.put(key, toParamVal(del, value, moreValues));
        //System.out.println("Added '" + key + "=" + toParamVal(del, value, moreValues)[0] + "'");
        return this;
    }
    
    /**
     * Safely adds a key-value-paired query parameter to the list of freely 
     * modifiable parameters.
     * <p>
     * The given parameter will not be added if it already exists as a default 
     * or unmodifiable parameter.
     * 
     * @param key The parameter key.
     * @param val The parameter value, see {@link APIService#toParamVal(no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)} for complex variants.
     * @return This instance, updated.
     * @see #addParameter(java.lang.String, no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...) 
     * @deprecated Probably more convenient to use one of the other addParameter(...) methods instead.
     */
    public APIServiceInterface addParameter(String key, String[] val) {
        makeParameter(key, val);
        //apiParams.put(key, val);
        return this;
    }
    
    /**
     * Shortcut for setting the query string parameter.
     * 
     * @param freetextQuery The query string.
     * @return This instance, updated.
     * @see #addParameter(java.lang.String, java.lang.String) 
     * @see #API_KEY_SEARCH_QUERY
     */
    public APIServiceInterface setFreetextQuery(String freetextQuery) {
        return addParameter(Key.SEARCH_QUERY, freetextQuery);
    }
    
    /**
     * Adds a filter parameter, that instructs the service to require the given 
     * value on the given field.
     * <p>
     * Any preexisting default or unmodifiable filter parameters will not be
     * overridden.
     * 
     * @param fieldName The field name e.g. "people.roles".
     * @param val The field value, e.g. "author|editor".
     * @return This instance, updated.
     * @see APIService#combine(no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)
     * @see #addParameter(java.lang.String, java.lang.String) 
     */
    @Override
    public APIServiceInterface addFilter(String fieldName, String val) {
        return addParameter(modFilter(fieldName), val);
    }
    
     /**
     * Adds a filter to instruct the service that we require the given value on 
     * the specific given field.
     * <p>
     * Any preexisting default or unmodifiable filter parameters will not be
     * overridden.
     * 
     * @param key The field name e.g. "people.roles".
     * @param del The delimiter to use, see {@link Delimiter}, when combining multiple values.
     * @param value The field value, e.g. "author|editor".
     * @param moreValues Optional additional field value(s).
     * @return This instance, updated.
     * @see APIService#combine(no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)
     * @see #addParameter(java.lang.String, java.lang.String) 
     */
    @Override
    public APIServiceInterface addFilter(String key, Delimiter del, String value, String ... moreValues) {
        //varParams.put(modFilter(key), toParamVal(del, value, moreValues));
        return addParameter(modFilter(key), del, value, moreValues);
        //return this;
    }
    
    


    //
    // ToDo: add addDefaultFilter() methods, equivalent to as addDefaultParameter()
    //
    
    
    
    
    
    /**
     * Sets the default parameters to use when querying the service.
     * 
     * @param defaults The default parameters to use when querying the service.
     * @return This instance, updated.
     * @see #defaultParams
     * @deprecated Probably more convenient to use one of the addDefaultParameter(...) methods instead.
     */
    public APIServiceInterface setDefaultParameters(Map<String, String[]> defaults) {
        defaultParams.putAll(defaults);
        
        return this;
    }
    
    /**
     * Adds a single default parameter to use when querying the service.
     * 
     * @param key The parameter key.
     * @param values The parameter value(s).
     * @return This interface, updated with the given default parameter.
     * @see APIService#setDefaultParameters(java.util.Map) 
     * @see APIService#defaultParams
     * @deprecated Probably more convenient to use one of the other addDefaultParameter(...) methods instead.
     */
    public APIServiceInterface addDefaultParameter(String key, String[] values) {
        makeDefaultParameter(key, values);
        //defaultParams.put(key, values);
        return this;
    }
    
    /**
     * Safely adds the given parameter to the list of <strong>default</strong> 
     * parameters.
     * <p>
     * The given parameter will not be added if it already exists as an 
     * unmodifiable parameter.
     * <p>
     * If added safely, any preexisting duplicate key in the list of freely 
     * modifiable parameters will be removed.
     * 
     * @param key The parameter key.
     * @param value The parameter value, see {@link APIService#toParamVal(no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)} for complex variants.
     * @param del The delimiter to use, see {@link Delimiter}, when combining multiple values.
     * @param moreValues Optional additional values.
     * @return This instance, updated.
     * @see #addDefaultParameter(java.lang.String, java.lang.String) 
     */
    @Override
    public APIServiceInterface addDefaultParameter(String key, Delimiter del, String value, String ... moreValues) {
        makeDefaultParameter(key, toParamVal(del, value, moreValues));
        //defaultParams.put(key, toParamVal(del, value, moreValues));
        //System.out.println("Added default '" + key + "=" + toParamVal(del, value, moreValues)[0] + "'");
        return this;
    }
    
    /**
     * @see #addDefaultParameter(java.lang.String, no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...) 
     */
    public APIServiceInterface addDefaultParameter(String key, String value) {
        //defaultParams.put(key, toParamVal(value));
        return addDefaultParameter(key, null, value);
    }
    
    /**
     * Safely adds the given parameter to the list of <strong>default</strong> 
     * parameters.
     * <p>
     * The given parameter will not be added if it already exists as an 
     * unmodifiable parameter, and in case of such an override attempt, 
     * <code>false</code> is returned.
     * <p>
     * If added safely, any preexisting duplicate key in the list of freely 
     * modifiable parameters will be removed.
     * 
     * @param k the parameter key.
     * @param v the parameter value, see {@link APIService#toParamVal(no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)} for complex variants.
     * @return true if the parameter was added, false if not.
     */
    protected synchronized boolean makeDefaultParameter(String k, String[] v) {
        if (getDefaultParameters() == null) {
            initDefaultParameters();
        }
        try {
            if (getUnmodifiableParameters().containsKey(k)) {
                return false;
            }
        } catch (Exception e) {
        }
        defaultParams.put(k, v);
        apiParams.remove(k);
        return true;
    }
    
    /**
     * Safely adds the given parameter to the list of (modifiable) parameters.
     * <p>
     * The given parameter will not be added if it already exists as a default 
     * or unmodifiable parameter, and in case of such an override attempt, 
     * <code>false</code> is returned.
     * 
     * @param k the parameter key.
     * @param v the parameter value, see {@link APIService#toParamVal(no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)} for complex variants.
     * @return true if the parameter was added, false if not.
     */
    protected synchronized boolean makeParameter(String k, String[] v) {
        try {
            if (getUnmodifiableParameters().containsKey(k)) {
                return false;
            }
        } catch (Exception e) {
        }
        try {
            if (getDefaultParameters().containsKey(k)) {
                return false;
            }
        } catch (Exception e) {}
        
        apiParams.put(k, v);
        return true;
    }
    
    /**
     * Injects the default parameters (if any) into the set of parameters.
     * 
     * @return The complete set of parameters, including the default parameters.
     */
    /*protected Map<String, String[]> injectDefaultParameters() {
    //protected Map<String, String[]> addDefaultParameters() {
        addParameters(getDefaultParameters());
        return apiParams;
    }*/
    
    /**
     * Adds the given parameters to the set of parameters to use when accessing 
     * the service.
     * <p>
     * The given parameters will not override any default or unmodifiable 
     * parameters.
     * 
     * @param params The parameters to add.
     * @return The complete set of parameters, including the given parameters.
     * 
     * @see #addDefaultParameter(java.lang.String, java.lang.String) 
     * @see #addDefaultParameter(java.lang.String, no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...) 
     */
    public Map<String, String[]> addParameters(Map<String, String[]> params) {
        if (params != null && !params.isEmpty()) {
            Iterator<String> i = params.keySet().iterator();
            while (i.hasNext()) {
                String key = i.next();
                makeParameter(key, params.get(key));
            }
        }
        return apiParams;
    }
    
    /**
     * Gets the freely modifiable parameters currently set.
     * <p>
     * Will normally 
     * @see APIServiceInterface#getParameters() 
     * @see getVisibleParameters()
     */
    @Override
    public Map<String, String[]> getParameters() {
        return apiParams;
    }
    
    /**
     * Gets the parameters that are "visible" to the end user.
     * 
     * @return The parameters that are "visible" to the end user.
     * @see #getDefaultParameters() 
     * @see #getUnmodifiableParameters() 
     */
    public Map<String, String[]> getVisibleParameters(){
        Map<String, String[]> temp = new HashMap<String, String[]>();
        temp.putAll(apiParams);
        
        // Remove any default/unmodifiable parameters present
        Iterator<String> iPresetKeys = getPresetParameters().keySet().iterator(); // Get default + unmodifiable keys
        while (iPresetKeys.hasNext()) {
            temp.remove(iPresetKeys.next()); // This was a default/unmodifiable: remove it
        }
        
        return temp;
    }
    
    /**
     * Gets a resource bundle that is based on the {@link Labels} class and 
     * the <code>Labels.properties</code> files in this package.
     * 
     * @param locale The locale to use.
     * @return A localized resource bundle.
     */
    public ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(Labels.getBundleName(), locale);
    }
    
    /**
     * Gets the default ("hidden") parameters, if any.
     * <p>
     * Default parameters are open for modifications via the service instance, 
     * but we don't want to provide that freedom and/or expose these parameters 
     * to the end user.
     * 
     * @return The default parameters.
     * @see #getUnmodifiableParameters() 
     */
    //protected abstract Map<String, String[]> getDefaultParameters();
    protected Map<String, String[]> getDefaultParameters() {
        if (defaultParams == null) {
            initDefaultParameters();
        }
        return defaultParams;
    }
    
    /**
     * Gets a list of unmodifiable parameters, if any. 
     * <p>
     * Unmodifiable parameters are <strong>always</strong> used when accessing 
     * the service, and cannot be overridden.
     * <p>
     * These are the parameters which, if changed, would cause the client to
     * break - for example <code>format=json</code>. These should not be 
     * confused with the <em>default</em> parameters. (<i>Default parameters 
     * are open for modifications via the service instance, but we don't want to 
     * provide that freedom and/or expose these parameters to the end user.</i>)
     * 
     * @return A list of unmodifiable parameters, or an empty list if none. 
     * @see #getDefaultParameters() 
     */
    protected Map<String, String[]> getUnmodifiableParameters() {
        if (unmodifiableParams == null) {
            initUnmodifiableParameters();
        }
        return unmodifiableParams;
    }
    
    /**
     * Gets the default and unmodifiable parameters.
     * <p>
     * These parameters are the ones that are not freely modifiable.
     * 
     * @return the default and unmodifiable parameters.
     * @see #addDefaultParameter(java.lang.String, java.lang.String) 
     * @see #addDefaultParameter(java.lang.String, no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...) 
     */
    public Map<String, String[]> getPresetParameters() {
        Map<String, String[]> tmp = new HashMap<String, String[]>();
        tmp.putAll(getDefaultParameters());
        tmp.putAll(getUnmodifiableParameters());
        return tmp;
    }
    
    /**
     * Builds a URL query string based on the given parameters.
     * <p>
     * <strong>Note: Each array in the given map should typically contain only 
     * 1 string entry.</strong> Arrays with multiple entries are treated as 
     * AND-combination values, and converted to a single, comma-separated value 
     * in the returned string.
     * <p>
     * The API does not use repeating keys in the query string, so the first 
     * cell of each key's associated array should hold the value. If there are 
     * in fact <em>multiple values</em> associated with a key, these values 
     * should be converted to a single, properly delimited string 
     * <em>before</em> invoking this method.
     * <p>
     * 
     * @param params The parameters to build the query string from.
     * @return The query string, containing the given parameters.
     */
    protected String getParameterString(Map<String, String[]> params) {
        if (params.isEmpty()) {
            return "";
        }
        
        String s = "";
        Iterator<String> i = params.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next(); // e.g. "facets" (parameter name)
            String[] values = params.get(key); // e.g. get the parameter value(s) for "facets"
            s += key + "=" + combine(Delimiter.AND, values) + (i.hasNext() ? "&" : "");
            /*
            for (int j = 0; j < values.length;) { // loop all values
                s += (j == 0 ? (key+"=") : Delimiter.AND.toString()) + values[j]; // e.g. "facets=type"
                if (++j == values.length)
                    break;
                //else
                //    s += "&";
            }
            if (i.hasNext()) {
                s += "&";
            }
            */
        }
        //return URLEncoder.encode(s, "utf-8");
        return s;
    }
    
    /**
     * Requests the given URL and returns the response as a String.
     * 
     * @param url The URL to request.
     * @return The response, as a string.
     * @throws MalformedURLException
     * @throws IOException
     */
    protected String httpResponseAsString(String url) 
            throws MalformedURLException, IOException {
        
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream(), SERVICE_CHARSET));
        StringBuilder s = new StringBuilder();
        String oneLine;
        while ((oneLine = in.readLine()) != null) {
            s.append(oneLine);
        }
        in.close();

        return s.toString();
    }
    
    /**
     * @see APIServiceInterface#getLastServiceURL() 
     */
    @Override
    public String getLastServiceURL() { return serviceUrl; }
    
    /**
     * Prefixes the given field name with the "reverse" modifier.
     * <p>
     * A typical use-case is reverse sort order. Calling modReverse("published")
     * will return "-published", which can then be passed to order entries "most 
     * recent first".
     * 
     * @param fieldName The field name, e.g. "published".
     * @return The given field name, prefixed with the "reverse" modifier, e.g. "-published".
     */
    public static String modReverse(String fieldName) {
        return ParamVal.PREFIX_REVERSE.concat(fieldName);
    }
    
    /**
     * Prefixes the given field name with the "not" modifier.
     * <p>
     * A typical use-case is filtering out drafts. Calling modNot("draft" will 
     * will return "not-draft", which can then be used as a parameter. Combined 
     * with "yes" as value ("not-draft=yes"), only entries which are flagged as 
     * non-drafts will be collected.
     * 
     * @param paramOrFieldName The parameter or field name, e.g. "draft".
     * @return The given parameter or field name, prefixed with the "not" modifier, e.g. "not-draft".
     */
    public static String modNot(String paramOrFieldName) {
        return Param.MOD_NOT.concat(paramOrFieldName);
    }
    
    /**
     * Prefixes the given field name with the "filter" modifier.
     * <p>
     * A typical use-case is limiting to state. Calling modFilter("state")
     * will return "filter-state", which can then be used to fetch only entries 
     * with specific states, e.g. "published".
     * 
     * @param fieldName The field name, e.g. "state".
     * @return The given field name, prefixed with the "filter" modifier, e.g. "filter-state".
     */
    public static String modFilter(String fieldName) {
        return Param.MOD_FILTER.concat(fieldName);
    }
    
    /**
     * Puts all the given 1-N strings in an array of 1 string - delimited by the
     * given delimiter if multiple strings were given - ready to pass to the API.
     * 
     * @param delimiter The delimiter to use, see {@link Delimiter}, when combining multiple values.
     * @param value The first parameter value.
     * @param moreValues Optional additional parameter values.
     * @return An array containing 1 string, which holds the given parameter value(s), separated by the given delimiter (if multiple).
     */
    public static String[] toParamVal(Delimiter delimiter, String value, String ... moreValues) {
        // NOTE: we *always* return an array (for reasons that escape me) with just 1 value
        /*String val = s1;
        for (String s : strings) {
            val += delimiter + s;
        }*/
        return new String[] { combine(delimiter, value, moreValues) };
        /*String[] array = new String[strings.length + 1];
        array[0] = s1;
        int i = 1;
        for (String s : strings) {
            array[i] = s;
        }
        return array;*/
    }
    
    /**
     * Puts all the given 1-N strings in an array of 1 string - delimited by the
     * given delimiter if multiple strings were given - ready to pass to the API.
     * 
     * @param value The parameter value.
     * @return An array containing 1 string, which holds the given parameter value.
     * @see #toParamVal(java.lang.String, java.lang.String, java.lang.String...) 
     */
    public static String[] toParamVal(String value) {
        return toParamVal(Delimiter.AND, value);
    }
    
    /**
     * Combines the given strings into one, separated by the given delimiter.
     * 
     * @param delimiter The delimiter to use, see {@link Delimiter}, when combining multiple values.
     * @param string The first parameter value.
     * @param moreStrings Optional additional parameter values.
     * @return The given string(s) combined into one.
     */
    public static String combine(Delimiter delimiter, String string, String ... moreStrings) {
        String combined = string;
        for (String s : moreStrings) {
            combined += delimiter + s;
        }
        return combined;
    }
    
    /**
     * Combines the given strings into one, separated by the given delimiter.
     * 
     * @param delimiter The delimiter to use, see {@link Delimiter}, when combining multiple values.
     * @param strings One or more strings.
     * @return All given strings combined into one.
     */
    public static String combine(Delimiter delimiter, String[] strings) {
        String combined = "";
        for (String s : strings) {
            combined += (!combined.isEmpty() ? delimiter : "") + s;
        }
        return combined;
    }
}
