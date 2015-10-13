package no.npolar.data.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;

/**
 * Provides an interface to read projects from the Norwegian Polar Institute 
 * Data Centre.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class ProjectService extends APIService {
    
    /** The URL path to use when accessing the service. */
    protected static final String SERVICE_PATH = "project/";
    /** The base URL (that is, the complete URL before adding parameters) to use when accessing the service. */
    protected static final String SERVICE_BASE_URL = SERVICE_PROTOCOL + "://" + SERVICE_DOMAIN_NAME + ":" + SERVICE_PORT + "/" + SERVICE_PATH;
    
    /**
     * Creates a new project service instance.
     * 
     * @param loc The locale to use when generating strings for screen view. If null, the {@link APIService#DEFAULT_LOCALE_NAME default locale} is used.
     */
    public ProjectService(Locale loc) {
        this.displayLocale = loc;
        if (displayLocale == null)
            this.displayLocale = new Locale(APIService.DEFAULT_LOCALE_NAME);
    }
    
    /**
     * Creates a new project, based on the given ID.
     * <p>
     * The ID is used to construct the URL that is uniquely identifies the entry 
     * within the Data Centre.
     * 
     * @param id The project ID.
     * @return the project object, or null if no such project could be created.
     */
    public Project getProject(String id) {
        try {
            return new Project(this.doRead(id), displayLocale);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Queries the service using the given parameters and returns all (if any)
     * projects, generated from the service response.
     * 
     * @param params The parameters to use in the service request.
     * @return A list of all projects, generated from the service response, or an empty list if no projects matched.
     * @see APIService#doQuery(java.util.Map) 
     * @see APIService#getEntries() 
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException
     */
    public GroupedCollection<Project> getProjects(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        // Define the order of the grouping
        String[] order = { 
            Project.STATE_NAME_ACTIVE,
            Project.STATE_NAME_PLANNED,
            Project.STATE_NAME_COMPLETED,
            Project.STATE_NAME_UNDEFINED
        };
        
        GroupedCollection<Project> gc = new GroupedCollection<Project>();
        gc.setOrder(order);
        
        doQuery(params);
        JSONArray entries = getEntries();
        
        if (entries != null) {
            for (int i = 0; i < entries.length(); i++) {
                try {
                    gc.add(new Project(entries.getJSONObject(i), displayLocale));
                } catch (Exception e) {
                    throw new InstantiationException("Error when trying to create projects list: " + e.getMessage());
                }
            }
        }
        return gc;
    }
    
    /**
     * Queries the service using the given parameters and returns all (if any)
     * projects, generated from the service response.
     * 
     * @param params The parameters to use in the service request.
     * @return A list of all projects, generated from the service response, or an empty list if no projects matched.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     */
    public List<Project> getProjectList(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        List<Project> list = new ArrayList<Project> ();
        
        JSONArray publicationObjects = doQuery(params).getEntries();
        
        if (publicationObjects != null) {
            for (int i = 0; i < publicationObjects.length(); i++) {
                /*try {*/
                    list.add(new Project(publicationObjects.getJSONObject(i), displayLocale));
                /*} catch (Exception e) {
                    throw new InstantiationException("Error when trying to create publications list: " + e.getMessage());
                }*/
            }
        }
        return list;
    }
    
    /**
     * Gets the default parameters (if any).
     * <p>
     * If no default parameters have been manually defined using 
     * {@link APIService#setDefaultParameters(java.util.Map)} or 
     * {@link APIService#addDefaultParameter(java.lang.String, java.lang.String[]) }, 
     * a pre-defined list of default parameters are used.
     * 
     * @see APIService#getDefaultParameters()
     * @return the default parameters (if any).
     */
    @Override
    public Map<String, String[]> getDefaultParameters() {
        if (defaultParams == null || defaultParams.isEmpty()) {
            defaultParams = new HashMap<String, String[]>();
            // Add any default ("invisible") parameters here
            //defaultParams.put("filter-draft", new String[]{ "no" });
        
        }
        defaultParams.putAll(getUnmodifiableParameters());
        return defaultParams;
    }
    
    /**
     * Gets a list of unmodifiable parameters.
     * <p>
     * These are always used when accessing the service, and cannot be overridden.
     * 
     * @return A list of unmodifiable parameters, or an empty list if none. 
     */
    @Override
    public Map<String, String[]> getUnmodifiableParameters() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("format", new String[]{ "json" });
        return params;
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

