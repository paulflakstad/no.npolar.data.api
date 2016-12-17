package no.npolar.data.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencms.json.JSONException;

import static no.npolar.data.api.Person.Key.*;
import static no.npolar.data.api.Person.Val.*;

/**
 *
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class PersonService extends APIService {
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(MOSJService.class);
    
    /** The URL path to use when accessing the service. */
    protected static final String SERVICE_PATH = "person/";
    /** The base URL (that is, the complete URL before adding parameters) to use when accessing the service. */
    protected static final String SERVICE_BASE_URL = SERVICE_PROTOCOL + "://" + SERVICE_DOMAIN_NAME + ":" + SERVICE_PORT + "/" + SERVICE_PATH;
    
    /**
     * Creates a new person service instance.
     * 
     * @param loc The locale to use when generating strings for screen view. If <code>null</code>, the {@link APIService#DEFAULT_LOCALE_NAME default locale} is used.
     * @see APIService#APIService(java.util.Locale) 
     */
    public PersonService(Locale loc) {
        super(loc);
        initPresetParameters();
    }
    
    /**
     * Creates a new person, based on the given ID.
     * <p>
     * The ID is used to construct the URL that is uniquely identifies the entry 
     * within the Data Centre.
     * 
     * @param id The person ID.
     * @return the person object, or <code>null</code> if no such project could be created.
     */
    public Person get(String id) {
        try {
            return new Person(doRead(id), displayLocale);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read Project with ID "+id, e);
            }
            return null;
        }
    }
    
    /**
     * Queries the storage using the given parameters and returns all (if any)
     * results.
     * <p>
     * Any existing default parameter will NOT be overridden by a same-name 
     * parameter in the given map.
     * 
     * @param params The parameters to use in the request. Can be <code>null</code>.
     * @return A list of entries, or an empty list if none, generated from the response.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     * @see APIService#addParameters(java.util.Map) 
     * @see APIService#addDefaultParameter(java.lang.String, no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)
     */
    public List<Person> getPersonList(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, 
            MalformedURLException, 
            IOException, 
            JSONException, 
            InstantiationException {
        
        List<Person> list = new ArrayList<Person> ();
        
        addParameters(params);
        doQuery();
        
        if (entries != null) {
            for (int i = 0; i < entries.length(); i++) {
                try {
                    list.add(new Person(entries.getJSONObject(i), displayLocale));
                } catch (Exception e) {
                    // Log this?
                }
            }
        }
        return list;
    }
    
    /**
     * Gets a list of persons, using the current settings.
     * <p>
     * Intended used <em>after</em> having set parameters using 
     * {@link #addDefaultParameter(java.lang.String, java.lang.String)}, 
     * {@link #addParameter(java.lang.String, java.lang.String)}, 
     * {@link #addFilter(java.lang.String, java.lang.String)}, 
     * {@link #setQueryString(java.lang.String)}, etc.
     * 
     * @return A list of persons matching the current set of parameters, or an empty list if none matched.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     */
    public List<Person> getPersonList() 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        return getPersonList(null);
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
     * <li>currently_employed: true</li>
     * </ul>
     * <p>
     * These initial settings can be expanded or modified with  
     * {@link #makeDefaultParameter(java.lang.String, java.lang.String)} or
     * {@link #makeDefaultParameter(java.lang.String, no.npolar.data.api.APIService.Delimiter, java.lang.String, java.lang.String...)}.
     * 
     * @see APIService#getDefaultParameters()
     */
    //@Override
    private void initDefaultParameters() {
        makeDefaultParameter(
                modFilter(CURR_EMPLOYED),
                toParamVal(TRUE_GENERIC)
        );
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
    
    /**
     * @see Person#getComparatorSortChar(java.util.Locale) 
     */
    public Comparator<Person> getComparatorSortChar() {
        return Person.getComparatorSortChar(displayLocale);
    }
}
