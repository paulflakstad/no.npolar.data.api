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
 * Base class for providing access to the NPI's web services (API).
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public abstract class APIService implements APIServiceInterface {
    
    public static final String API_KEY_FEED = "feed";
    public static final String API_KEY_OPENSEARCH = "opensearch";
    public static final String API_KEY_OPENSEARCH_TOTAL_RESULTS = "totalResults";
    public static final String API_KEY_OPENSEARCH_ITEMS_PER_PAGE = "itemsPerPage";
    public static final String API_KEY_OPENSEARCH_START_INDEX = "startIndex";
    public static final String API_KEY_LIST = "list";
    public static final String API_KEY_LIST_SELF = "self";
    public static final String API_KEY_LIST_FIRST = "first";
    public static final String API_KEY_LIST_LAST = "last";
    public static final String API_KEY_LIST_NEXT = "next";
    public static final String API_KEY_LIST_PREVIOUS = "previous";
    public static final String API_KEY_SEARCH = "search";
    public static final String API_KEY_SEARCH_QUERY_TIME = "qtime";
    public static final String API_KEY_SEARCH_QUERY = "q";
    public static final String API_KEY_ENTRIES = "entries";
    public static final String API_KEY_FACETS = "facets";
    
    
    /** The protocol to use when accessing the service. */
    protected static final String SERVICE_PROTOCOL = "http";
    /** The domain name to use when accessing the service. */
    protected static final String SERVICE_DOMAIN_NAME = "api.npolar.no";
    /** The port to use when accessing the service. */
    protected static final String SERVICE_PORT = "80";
    
    /* The charset used by the service. */
    public static final String SERVICE_CHARSET = "UTF-8";
    
    /** The full URL to the service. Updated on every service request. */
    protected String serviceUrl = null;
    /** The parameters to use when accessing the service. */
    protected Map<String, String[]> serviceParams = new HashMap<String, String[]>();
    /** 
     * The default / hidden parameters to use when accessing the service. 
     * <p>
     * These are parameters that the user shouldn't see, nor be able to override 
     * by modifying/adding parameters in the URL manually.
     * <p>
     * Typical examples of a default parameter are 
     * <ul>
     * <li><code>not-draft=yes</code></li>
     * <li><code>facets=topic,type</code></li>
     * </ul>
     * <p>
     * Not to be confused with <strong>unmodifiable parameters</strong>. They are 
     * different: Default parameters <em>could</em> potentially be modified, while 
     * umodifiable parameters are those that must <strong>never</strong> be 
     * modified (typically because that would cause the entire client to break 
     * - for example the <code>format=json</code> parameter).
     */
    protected Map<String, String[]> defaultParams = new HashMap<String, String[]>();
    /** The total number of entries in the last fetch. */
    protected int totalResults = -1;
    /** The "Next page" URI, as provided by the service. */
    protected String pageUriNext = null;
    /** The "Previous page" URI, as provided by the service. */
    protected String pageUriPrev = null;
    /** The first page no, as provided by the service. */
    protected int indexNoFirstPageItem = -1;
    /** The last page, as provided by the service. */
    protected int indexNoLastPageItem = -1;
    /** The number of items per page, as provided by the service. */
    protected int itemsPerPage = -1;
    /** The index of the first item, as provided by the service. */
    protected int startIndex = -1;
    /** The "self" URI, as provided by the service. */
    protected String self = null;
    
    /** The (last) query, as provided by the service. */
    protected String query = null;
    /** The (last) query's search time, as provided by the service. */
    protected int querySearchTime = -1;
    
    /** Container for JSON entries. */
    protected JSONArray entries = null;
    
    /** Flag indicating whether or not any user-activated filters are applied. */
    protected boolean isUserFiltered = false;
    
    
    /** The locale to use when generating strings meant for viewing. */
    protected Locale displayLocale = null;
    
    /** The default locale to use when generating strings meant for viewing. */
    public static final String DEFAULT_LOCALE_NAME = "en";
    
    /** The prefix for filters. */
    public static final String FILTER_PREFIX = "filter-";
    
    /** Container for filter sets. */
    protected SearchFilterSets filterSets = null;
    
    /**
     * @see APIServiceInterface#doQuery(java.util.Map) 
     */
    @Override
    public APIServiceInterface doQuery(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        // Make sure default parameters are set (like "format=json")
        //params = addDefaultParameters(params);
        serviceUrl = getServiceBaseURL() + "?" + setParameters(params);
        // We're expecting a response in JSON format
        String jsonFeed = httpResponseAsString(serviceUrl);
        JSONObject json = new JSONObject(jsonFeed).getJSONObject(API_KEY_FEED);
        
        try { 
            JSONObject opensearch = json.getJSONObject(API_KEY_OPENSEARCH);
            try { totalResults = opensearch.getInt(API_KEY_OPENSEARCH_TOTAL_RESULTS); } catch (Exception innerE) { totalResults = -1; }
            try { itemsPerPage = opensearch.getInt(API_KEY_OPENSEARCH_ITEMS_PER_PAGE); } catch (Exception innerE) { itemsPerPage = -1; }
            try { startIndex = opensearch.getInt(API_KEY_OPENSEARCH_START_INDEX); } catch (Exception innerE) { startIndex = -1; }
        } catch (Exception e) { }
        
        try {
            JSONObject list = json.getJSONObject(API_KEY_LIST);
            try { self = list.getString(API_KEY_LIST_SELF); } catch (Exception innerE) { self = null; }
            try { indexNoFirstPageItem = list.getInt(API_KEY_LIST_FIRST); } catch (Exception innerE) { indexNoFirstPageItem = -1; }
            try { indexNoLastPageItem = list.getInt(API_KEY_LIST_LAST); } catch (Exception innerE) { indexNoLastPageItem = -1; }
            try { pageUriNext = list.getString(API_KEY_LIST_NEXT); } catch (Exception innerE) { pageUriNext = null; }
            try { pageUriPrev = list.getString(API_KEY_LIST_PREVIOUS); } catch (Exception innerE) { pageUriPrev = null; }
        } catch (Exception e) { }
        
        try { 
            JSONObject search = json.getJSONObject(API_KEY_SEARCH);
            try { querySearchTime = search.getInt(API_KEY_SEARCH_QUERY_TIME); } catch (Exception innerE) { querySearchTime = -1; }
            try { query = search.getString(API_KEY_SEARCH_QUERY); } catch (Exception innerE) { query = null; }
        } catch (Exception e) { }
        
        try { entries = json.getJSONArray(API_KEY_ENTRIES); } catch (Exception e) { entries = null; }
        
        //
        // Facets
        //
        filterSets = new SearchFilterSets();
        try {
            JSONArray facets = json.getJSONArray(API_KEY_FACETS);
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
                                Iterator<String> iDefaultParamKeys = this.getDefaultParameters().keySet().iterator();
                                while (iDefaultParamKeys.hasNext()) {
                                    f.removeParam(iDefaultParamKeys.next());
                                }
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
     * Gets all currently available filter sets.
     * 
     * @return The currently available filter sets for the current entry set.
     * @see SearchFilterSet
     * @see SearchFilter
     */
    public SearchFilterSets getFilterSets() { return this.filterSets; }
    
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
     * @return The query string of the given URI, clean of any default parameters, or null if none.
     */
    public String stripDefaultParameters(String uri) {
        Iterator<String> iDefaultParamKeys = this.getDefaultParameters().keySet().iterator();
        while (iDefaultParamKeys.hasNext()) {
            String paramName = iDefaultParamKeys.next();
            String[] uriAndParams = uri.split("\\?");
            Map<String, List<String>> params = APIUtil.getQueryParametersFromString(uri);
            params.remove(paramName);
            uri = uriAndParams[0] + "?" + APIUtil.getParameterString(params);
        }
        try {
            return uri.split("\\?")[1];
        } catch (Exception e) {
            return null;
        }
    }
    
    
    /**
     * Adds the given and the default parameters to the set of parameters and
     * converts the String representation of those parameters, ready to append
     * to the service URL.
     * 
     * @return The complete set of parameters, consisting of the given + default parameters.
     */
    protected String setParameters(Map<String, String[]> params) {
        serviceParams.clear();
        addParameters(params);
        addParameters(getDefaultParameters());
        return getParameterString(serviceParams);
    }
    
    /**
     * Sets the default parameters to use when querying the service.
     * 
     * @param defaults The default parameters to use when querying the service.
     * @see APIService#defaultParams
     */
    public APIServiceInterface setDefaultParameters(Map<String, String[]> defaults) {
        defaultParams = defaults;
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
     */
    public APIServiceInterface addDefaultParameter(String key, String[] values) {
        defaultParams.put(key, values);
        return this;
    }
    
    /**
     * Injects the default parameters (if any) into the set of parameters.
     * 
     * @return The complete set of parameters, including the default parameters.
     */
    protected Map<String, String[]> injectDefaultParameters() {
    //protected Map<String, String[]> addDefaultParameters() {
        addParameters(getDefaultParameters());
        return serviceParams;
    }
    
    /**
     * Adds the given parameters to the set of parameters to use when accessing 
     * the service.
     * 
     * @param params The parameters to add.
     * @return The complete set of parameters, including the given parameters.
     */
    protected Map<String, String[]> addParameters(Map<String, String[]> params) {
        serviceParams.putAll(params);
        return serviceParams;
    }
    
    /**
     * @see APIServiceInterface#getParameters() 
     */
    @Override
    public Map<String, String[]> getParameters() {
        return serviceParams;
    }
    
    /**
     * Gets the parameters that are visible to the end user.
     * 
     * @return The parameters that are visible to the end user.
     */
    public Map<String, String[]> getVisibleParameters(){
        Map<String, String[]> temp = new HashMap<String, String[]>();
        temp.putAll(serviceParams);
        Iterator<String> iDefaultKeys = getDefaultParameters().keySet().iterator();
        while (iDefaultKeys.hasNext()) {
            temp.remove(iDefaultKeys.next());
        }
        return temp;
    }
    
    /**
     * Gets a ResourceBundle that is based on the {@link Labels} class and 
     * the Labels.properties files in this package.
     * 
     * @param locale The locale to use.
     * @return A localized resource bundle.
     */
    public ResourceBundle getBundle(Locale locale) { return ResourceBundle.getBundle(Labels.getBundleName(), locale); }
    
    /**
     * Gets the default / hidden parameters (if any).
     * <p>
     * Included in the default parameters should also be those returned by 
     * {@link APIService#getUnmodifiableParameters() }.
     * 
     * @return The default parameters (including unmodifiable parameters).
     * @see APIService#defaultParams
     */
    protected abstract Map<String, String[]> getDefaultParameters();
    
    /**
     * Gets a list of unmodifiable parameters. These are _always_ used when 
     * accessing the service, and cannot be overridden.
     * <p>
     * An unmodifiable parameter is one that if changed would cause the client
     * to break down, for example <code>format=json</code>, and must not be 
     * confused with a default parameter. (<i>The latter is a parameter that is 
     * open for modifications, but we don't want to provide that freedom and/or 
     * expose these parameters to the end user.</i>)
     * 
     * @return A list of unmodifiable parameters, or an empty list if none. 
     */
    protected abstract Map<String, String[]> getUnmodifiableParameters();
    
    /**
     * Builds a query string based on the given parameters.
     * 
     * @param params The parameters to build the query string from.
     * @return The query string, containing the given parameters.
     */
    protected String getParameterString(Map<String, String[]> params) 
            //throws java.io.UnsupportedEncodingException 
    {
        
        if (params.isEmpty())
            return "";
        String s = "";
        Iterator<String> i = params.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            String[] values = params.get(key);
            for (int j = 0; j < values.length;) {
                s += key + "=" + values[j];
                if (++j == values.length)
                    break;
                else
                    s += "&";
            }
            if (i.hasNext())
                s += "&";
        }
        //return URLEncoder.encode(s, "utf-8");
        return s;
    }
    
    /**
     * Requests the given URL and returns the response as a String.
     * 
     * @param url The URL to request.
     * @return The response, as a string.
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
}
