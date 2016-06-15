package no.npolar.data.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.Locale;
//import no.npolar.util.CmsAgent;
import java.util.Map;
import java.util.ResourceBundle;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;

/**
 * Provides an interface to read publications from the Norwegian Polar Institute 
 * Data Centre.
 * <p>
 * Publications can be fetched 
 * <ul>
 * <li>as a list</li>
 * <li>as a collection, grouped by publication type</li>
 * <li>as single publication</li>
 * </ul>
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class PublicationService extends APIService {
    
    /** The URL path to use when accessing the service. */
    protected static final String SERVICE_PATH = "publication/";
    /** The base URL (that is, the complete URL before adding parameters) to use when accessing the service. */
    protected static final String SERVICE_BASE_URL = SERVICE_PROTOCOL + "://" + SERVICE_DOMAIN_NAME + ":" + SERVICE_PORT + "/" + SERVICE_PATH;
    /** Translations. */
    protected ResourceBundle labels = null;
    /** Holds the "current" (modifiable) query parameters. */
    //protected Map<String, String[]> queryParams = null;
    
    /**
     * Creates a new service instance, configured with the given locale.
     * 
     * @param loc The locale to use when generating strings for screen view. If null, the default locale is used.
     */
    public PublicationService(Locale loc) {
        //super(); redundant -> this is equivalent to not calling superclass
        displayLocale = loc;
        if (displayLocale == null)
            displayLocale = new Locale(DEFAULT_LOCALE_NAME);
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        
        initPresetParameters();
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
    }
    
    /**
     * Sets the initial default parameters (if any).
     * <p>
     * The following default parameter(s) are set:
     * <ul>
     * <li>not-draft: yes</li>
     * <li>facets: topics,publication_type</li>
     * <li>sort: -published</li>
     * </ul>
     * <p>
     * These initial settings can be expanded or modified with  
     * {@link #makeDefaultParameter(java.lang.String, java.lang.String)} or
     * {@link #makeDefaultParameter(java.lang.String, no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)}.
     * 
     * @see APIService#getDefaultParameters()
     */
    private void initDefaultParameters() {
        makeDefaultParameter(
                modNot(Publication.Key.DRAFT), 
                toParamVal(Publication.Val.DRAFT_TRUE)
        );
        makeDefaultParameter(
                Param.FACETS,
                toParamVal(Delimiter.AND, Publication.Key.TOPICS, Publication.Key.TYPE)
        );
        makeDefaultParameter(
                Param.SORT_BY, 
                toParamVal(modReverse(Publication.Key.PUB_TIME))
        );
    }
    
    /**
     * Queries the service using the given parameters and returns all (if any)
     * publications, generated from the service response.
     * 
     * @param params The parameters to use in the service request.
     * @return A list of all publications, generated from the service response, or an empty list if no publications matched.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     * @deprecated Use {@link #getPublications()} instead.
     */
    public GroupedCollection<Publication> getPublications(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        addParameters(params);
        return getPublications();
    }
    
    /**
     * Queries the service using the current parameters and returns all (if any)
     * publications, generated from the service response.
     * 
     * @return A list of all publications, generated from the service response, or an empty list if no publications matched.
     * @see APIService#doQuery(java.util.Map) 
     * @see APIService#getEntries() 
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException
     */
    public GroupedCollection<Publication> getPublications() 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        //ensureInstantiatedQueryParameters();
        //queryParams.putAll(params);
        //apiParams.putAll(params);
        
        // Define the order of the publications grouping
        String[] order = { 
            Publication.Type.PEER_REVIEWED.toString(),
            Publication.Type.BOOK.toString(),
            Publication.Type.EDITORIAL.toString(),
            Publication.Type.REPORT.toString(),
            Publication.Type.MAP.toString(),
            Publication.Type.REVIEW.toString(),
            Publication.Type.PROCEEDINGS.toString(),
            Publication.Type.ABSTRACT.toString(),
            Publication.Type.CORRECTION.toString(),
            Publication.Type.PHD.toString(),
            Publication.Type.MASTER.toString(),
            Publication.Type.POSTER.toString(),
            Publication.Type.POPULAR.toString(),
            Publication.Type.OTHER.toString(),
            Publication.Type.UNDEFINED.toString()
            /*Publication.TYPE_PEER_REVIEWED,
            Publication.TYPE_BOOK,
            Publication.TYPE_EDITORIAL,
            Publication.TYPE_REPORT,
            Publication.TYPE_MAP,
            Publication.TYPE_REVIEW,
            Publication.TYPE_PROCEEDINGS,
            Publication.TYPE_ABSTRACT,
            Publication.TYPE_CORRECTION,
            Publication.TYPE_PHD,
            Publication.TYPE_MASTER,
            Publication.TYPE_POSTER,
            Publication.TYPE_POPULAR,
            Publication.TYPE_OTHER*/
        };
        
        GroupedCollection<Publication> gc = new GroupedCollection<Publication>();
        gc.setOrder(order);
        
        JSONArray publicationEntries = doQuery(getParameters()).getEntries();
        
        if (publicationEntries != null) {
            for (int i = 0; i < publicationEntries.length(); i++) {
                Publication p = new Publication(publicationEntries.getJSONObject(i), displayLocale);
                // Set specific sub-type if possible
                if (p.isType(Publication.Type.BOOK) && !(p.hasParent() || p.isPartContribution())) {
                    p = new Book(publicationEntries.getJSONObject(i), displayLocale);
                    //System.out.println("Book: " + p.getTitle() + " " + p.getId());
                } else if (p.isPartContribution()) {
                    p = new Chapter(publicationEntries.getJSONObject(i), displayLocale);
                    //System.out.println("Chapter: " + p.getTitle() + " " + p.getId());
                }
                /*try {*/
                    gc.add(p);
                /*} catch (Exception e) {
                    throw new InstantiationException("Error when trying to create publications list: " + e.getMessage());
                }*/
            }
        }
        return gc;
        
        //return new PublicationCollection(doQuery(params), cms);
    }
    
    /**
     * Gets a list of publications, using the current settings.
     * <p>
     * Intended used <strong>after</strong> having set parameters using 
     * {@link #addDefaultParameter(java.lang.String, java.lang.String)}, 
     * {@link #addParameter(java.lang.String, java.lang.String)}, 
     * {@link #addFilter(java.lang.String, java.lang.String)}, 
     * {@link #setQueryString(java.lang.String)}, 
     * {@link #setAllowDrafts(boolean)} etc.
     * 
     * @return A list of publications matching the current set of parameters, or an empty list if none matched.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     */
    public List<Publication> getPublicationList() 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        return getPublicationList(null);
        //ensureInstantiatedQueryParameters();
        //return getPublicationList(queryParams);
    }
    
    /**
     * Queries the service using the given parameters and returns all (if any)
     * publications, generated from the service response.
     * 
     * @param params The parameters to use in the service request.
     * @return A list of all publications matching the given parameters, or an empty list if no publications matched.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     */
    public List<Publication> getPublicationList(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        List<Publication> list = new ArrayList<Publication>();
        
        // If params==NULL, that's OK too :)
        addParameters(params);
        //apiParams.putAll(params);
        //ensureInstantiatedQueryParameters();
        //queryParams.putAll(params);
        
        JSONArray publicationEntries = doQuery(getParameters()).getEntries();
        
        if (publicationEntries != null) {
            for (int i = 0; i < publicationEntries.length(); i++) {
                /*try {*/
                Publication p = new Publication(publicationEntries.getJSONObject(i), displayLocale);
                // Set specific sub-type if possible
                if (p.isType(Publication.Type.BOOK) && !(p.hasParent() || p.isPartContribution())) {
                    p = new Book(publicationEntries.getJSONObject(i), displayLocale);
                } else if (p.isPartContribution()) {
                    p = new Chapter(publicationEntries.getJSONObject(i), displayLocale);
                }
                
                list.add(p);
                /*} catch (Exception e) {
                    throw new InstantiationException("Error when trying to create publications list: " + e.getMessage());
                }*/
            }
        }
        return list;
    }
    
    /**
     * Gets the parameters currently set either by setter methods or by passing 
     * a parameter map to {@link #getPublicationList(java.util.Map)}, 
     * {@link #getPublications(java.util.Map)}, etc.
     * 
     * @return the parameters currently set.
     */
    /*public Map<String, String[]> getParameters() {
        return queryParams;
    }*/
    
    /**
     * Creates a new publication object, based on the given ID.
     * <p>
     * The ID is used to construct the URL that is uniquely identifies the entry 
     * within the Data Centre.
     * 
     * @param id The publication ID.
     * @return the publication object, or null if no such publication could be created.
     */
    @Override
    public Publication get(String id) {
        /*    throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        HashMap<String, String[]> params = new HashMap<String, String[]>();
        
        params.put("filter-".concat(Publication.JSON_KEY_ID), new String[] { id }); // Filter on the given ID
        params.put("q", new String[]{ "" }); // Catch-all query
        params.put("limit", new String[]{ "1" }); // Limit the results to a single entry
        
        JSONArray publicationObjects = doQuery(params).getEntries();
        
        //if (true) throw new NullPointerException(this.getLastServiceURL());
        
        if (publicationObjects.length() == 1) {
            Publication p = new Publication(publicationObjects.getJSONObject(0), displayLocale);
            // Set specific sub-type if possible
            if (p.isType(Publication.TYPE_BOOK) && !(p.hasParent() || p.isPartContribution())) {
                p = new Book(publicationObjects.getJSONObject(0), displayLocale);
            } else if (p.isPartContribution()) {
                p = new Chapter(publicationObjects.getJSONObject(0), displayLocale);
            }
            return p;
        } else {
            // LOG "Cannot create Publication instance: Querying service with ID " + id + " returned " + publicationObjects.length() + " entries."
            //throw new NullPointerException("Cannot create Publication instance: Querying service with ID " + id + " returned " + publicationObjects.length() + " entries. " +this.getLastServiceURL());
            return null;
        }*/
        try {
            
            Publication p = new Publication(this.doRead(id), displayLocale);
            // Set specific sub-type if possible
            if (p.isType(Publication.Type.BOOK) && !(p.hasParent() || p.isPartContribution())) {
                p = new Book(p.getJSON(), displayLocale);
            } else if (p.isPartContribution()) {
                p = new Chapter(p.getJSON(), displayLocale);
            }
            return p;
        } catch (Exception e) {
            // LOG "Cannot create Publication instance: Querying service with ID " + id + " returned " + publicationObjects.length() + " entries."
            //throw new NullPointerException("Cannot create Publication instance: Querying service with ID " + id + " returned " + publicationObjects.length() + " entries. " +this.getLastServiceURL());
            return null;
        }
    }
    
    /**
     * Gets the default parameters (if any).
     * <p>
     * If no default parameters have been manually defined using 
     * {@link APIService#setDefaultParameters(java.util.Map)} or 
     * {@link APIService#addDefaultParameter(java.lang.String, java.lang.String[]) }, 
     * a pre-defined list of default parameters are used:
     * <ul>
     * <li>not-draft: yes</li>
     * <li>facets: topics,publication_type</li>
     * <li>sort: -published</li>
     * </ul>
     * 
     * @see APIService#getDefaultParameters()
     */
    /*@Override
    public Map<String, String[]> getDefaultParameters() {
        if (defaultParams == null || defaultParams.isEmpty()) {
            defaultParams = new HashMap<String, String[]>();
            defaultParams.put(
                    modNot(Publication.JSON_KEY_DRAFT), 
                    toParamVal(Publication.JSON_VAL_DRAFT_TRUE)
            );
            defaultParams.put(
                    PARAM_FACETS,
                    toParamVal(Delimiter.AND, Publication.JSON_KEY_TOPICS, Publication.JSON_KEY_TYPE)
            );
            defaultParams.put(
                    PARAM_SORT_BY, 
                    toParamVal(modReverse(Publication.JSON_KEY_PUB_TIME))
            );
        }
        defaultParams.putAll(getUnmodifiableParameters());
        return defaultParams;
    }*/
    
    /**
     * Adjust setting for whether or not to include entries flagged as drafts.
     * <p>
     * This setting will apply appropriate adjustment to the current set of 
     * default parameters. Any later overriding of the default parameters may
     * overwrite the setting done here.
     * 
     * @param allow Provide true to allow drafts, false to disallow.
     * @return this service instance, updated with the new value.
     */
    public PublicationService setAllowDrafts(boolean allow) {
        //getDefaultParameters(); // Make sure we have defaults
        Object currentSetting = getPresetParameters().get(modNot(Publication.Key.DRAFT)); // get("not-draft");
        
        if (!allow) {
                makeDefaultParameter(
                        modNot(Publication.Key.DRAFT), 
                        toParamVal(Publication.Val.DRAFT_TRUE)
                );
                //defaultParams.put("not-draft", new String[] { "yes" });
        } else {
            if (currentSetting != null) {
                defaultParams.remove(modNot(Publication.Key.DRAFT));
                //defaultParams.remove("not-draft");
            }
        }
        return this;
    }
    
    /**
     * Ensures the query parameters map is instantiated.
     */
    /*private void ensureInstantiatedQueryParameters() {
        if (queryParams == null) {
            queryParams = new HashMap<String, String[]>();
        }
    }*/
    
    /**
     * Adds a key-value-paired query parameter. 
     * <p>
     * If a parameter with such a key exists already, it will be overwritten.
     * 
     * @param key The parameter key.
     * @param val The parameter value.
     * @return This instance, updated.
     */
    /*public PublicationService addQueryParam(String key, String val) {
        ensureInstantiatedQueryParameters();
        queryParams.put(key, toParamVal(val));
        return this;
    }*/
    
    /**
     * Shortcut for setting the query string parameter.
     * 
     * @param queryString The query string.
     * @return This instance, updated.
     * @see #addQueryParam(java.lang.String, java.lang.String) 
     */
    /*public PublicationService setQueryString(String queryString) {
        return addQueryParam(APIService.API_KEY_SEARCH_QUERY, queryString);
    }*/
    
    /**
     * Adds a filter to instruct the service that we require the given value on 
     * the specific given field.
     * 
     * @param fieldName The field name e.g. "people.roles".
     * @param val The field value, e.g. "author|editor".
     * @return This instance, updated.
     * @see APIService#combine(no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)
     * @see #addQueryParam(java.lang.String, java.lang.String) 
     */
    /*public PublicationService addFilterFor(String fieldName, String val) {
        return addQueryParam(modFilter(fieldName), val);
    }*/
    
    /**
     * @see APIService#getUnmodifiableParameters() 
     */
    /*@Override
    public Map<String, String[]> getUnmodifiableParameters() {
        Map<String, String[]> unmodParams = new HashMap<String, String[]>();
        unmodParams.put(
                PARAM_FORMAT, 
                toParamVal(PARAM_VAL_FORMAT_JSON)
                //new String[]{ PARAM_VAL_FORMAT_JSON }
        );
        return unmodParams;
    }*/
    
    /**
     * @see APIServiceInterface#getServiceBaseURL() 
     */
    @Override
    public String getServiceBaseURL() { return SERVICE_BASE_URL; }
    
    /**
     * @see APIServiceInterface#getServicePath() 
     */
    @Override
    public String getServicePath() { return SERVICE_PATH; }
    
    /**
     * @deprecated Use {@link #get(java.lang.String)} instead.
     */
    public Publication getPublication(String id) {
        return get(id);
    }
}
