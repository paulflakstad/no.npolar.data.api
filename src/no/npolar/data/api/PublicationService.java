package no.npolar.data.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
//import no.npolar.util.CmsAgent;
import java.util.Map;
import java.util.ResourceBundle;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;

/**
 *
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class PublicationService extends APIService {
    
    /** The URL path to use when accessing the service. */
    protected static final String SERVICE_PATH = "/publication/";
    /** The base URL (that is, the complete URL before adding parameters) to use when accessing the service. */
    protected static final String SERVICE_BASE_URL = SERVICE_PROTOCOL + "://" + SERVICE_DOMAIN_NAME + ":" + SERVICE_PORT + SERVICE_PATH;
    /** Translations. */
    protected ResourceBundle labels = null;
    
    /**
     * Creates a new service instance, configured with the given locale.
     * @param loc The locale to use when generating strings for screen view. If null, the default locale is used.
     */
    public PublicationService(Locale loc) {
        displayLocale = loc;
        if (displayLocale == null)
            displayLocale = new Locale(DEFAULT_LOCALE_NAME);
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
    }
    
    /**
     * Queries the service using the given parameters and returns all (if any)
     * publications, generated from the service response.
     * @param params The parameters to use in the service request.
     * @return A list of all publications, generated from the service response, or an empty list if no publications matched.
     * @see APIService#doQuery(java.util.Map) 
     * @see APIService#getEntries() 
     */
    public GroupedCollection<Publication> getPublications(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        // Define the order of the publications grouping
        String[] order = { 
            Publication.TYPE_PEER_REVIEWED,
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
            Publication.TYPE_OTHER
        };
        
        GroupedCollection<Publication> gc = new GroupedCollection<Publication>();
        gc.setOrder(order);
        
        JSONArray publicationObjects = doQuery(params).getEntries();
        
        if (publicationObjects != null) {
            for (int i = 0; i < publicationObjects.length(); i++) {
                Publication p = new Publication(publicationObjects.getJSONObject(i), displayLocale);
                // Set specific sub-type if possible
                if (p.isType(Publication.TYPE_BOOK) && !(p.hasParent() || p.isPartContribution())) {
                    p = new Book(publicationObjects.getJSONObject(i), displayLocale);
                    //System.out.println("Book: " + p.getTitle() + " " + p.getId());
                } else if (p.isPartContribution()) {
                    p = new Chapter(publicationObjects.getJSONObject(i), displayLocale);
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
     * Queries the service using the given parameters and returns all (if any)
     * publications, generated from the service response.
     * @param params The parameters to use in the service request.
     * @return A list of all publications, generated from the service response, or an empty list if no publications matched.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     */
    public List<Publication> getPublicationList(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        List<Publication> list = new ArrayList<Publication>();
        
        JSONArray publicationObjects = doQuery(params).getEntries();
        
        if (publicationObjects != null) {
            for (int i = 0; i < publicationObjects.length(); i++) {
                /*try {*/
                Publication p = new Publication(publicationObjects.getJSONObject(i), displayLocale);
                // Set specific sub-type if possible
                if (p.isType(Publication.TYPE_BOOK) && !(p.hasParent() || p.isPartContribution())) {
                    p = new Book(publicationObjects.getJSONObject(i), displayLocale);
                } else if (p.isPartContribution()) {
                    p = new Chapter(publicationObjects.getJSONObject(i), displayLocale);
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
     * Creates a new publication object based on the given ID.
     * @param id The publication ID.
     * @return The publication object, or null if no such publication could be created.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     */
    public Publication getPublication(String id)
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
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
            //throw new NullPointerException("Cannot create Publication instance: Querying service with ID " + id + " returned " + publicationObjects.length() + " entries.");
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
     * <li>not-draft : yes</li>
     * <li>facets : topics, category, publication_type</li>
     * <li>sort : -published-sort</li>
     * </ul>
     * @see APIService#getDefaultParameters()
     */
    @Override
    public Map<String, String[]> getDefaultParameters() {
        if (defaultParams == null || defaultParams.isEmpty()) {
            defaultParams = new HashMap<String, String[]>();
            //defaultParams.put("filter-draft", new String[]{ "no" });
            defaultParams.put("not-draft", new String[]{ "yes" });
            defaultParams.put("facets", new String[]{ "topics,category,publication_type" });
            defaultParams.put("sort", new String[]{ "-published_sort" });
            //defaultParams.put("sort", new String[]{ "-published-year" });
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
        return unmodParams;
    }
    
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
}
