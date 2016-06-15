package no.npolar.data.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;

/**
 * Provides an interface to read projects from the Norwegian Polar Institute 
 * Data Centre.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class ProjectService extends APIService {
    
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(MOSJService.class);
    
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
        
        initPresetParameters();
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
    public Project get(String id) {
        try {
            return new Project(doRead(id), displayLocale);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read Project with ID "+id, e);
            }
            return null;
        }
    }
    
    /**
     * @deprecated Use {@link #get(java.lang.String)} instead.
     */
    public Project getProject(String id) { return get(id); }
    
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
    public GroupedCollection<Project> getProjects() 
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
        
        JSONArray projectEntries = doQuery(getParameters()).getEntries();
        
        if (projectEntries != null) {
            for (int i = 0; i < projectEntries.length(); i++) {
                try {
                    gc.add(new Project(projectEntries.getJSONObject(i), displayLocale));
                } catch (Exception e) {
                    throw new InstantiationException("Error when trying to create projects list: " + e.getMessage());
                }
            }
        }
        return gc;
    }
    
    /**
     * @deprecated Probably more  {@link #getProjects()} instead.
     */
    public GroupedCollection<Project> getProjects(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        addParameters(params);
        return getProjects();
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
        
        addParameters(params);
        
        JSONArray projectEntries = doQuery(apiParams).getEntries();
        
        if (projectEntries != null) {
            for (int i = 0; i < projectEntries.length(); i++) {
                /*try {*/
                    list.add(new Project(projectEntries.getJSONObject(i), displayLocale));
                /*} catch (Exception e) {
                    throw new InstantiationException("Error when trying to create publications list: " + e.getMessage());
                }*/
            }
        }
        return list;
    }
    
    /**
     * Gets a list of projects, using the current settings.
     * <p>
     * Intended used <strong>after</strong> having set parameters using 
     * {@link #addDefaultParameter(java.lang.String, java.lang.String)}, 
     * {@link #addParameter(java.lang.String, java.lang.String)}, 
     * {@link #addFilter(java.lang.String, java.lang.String)}, 
     * {@link #setQueryString(java.lang.String)}, etc.
     * 
     * @return A list of projects matching the current set of parameters, or an empty list if none matched.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     */
    public List<Project> getProjectList() 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        return getProjectList(null);
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
    /*@Override
    public Map<String, String[]> getDefaultParameters() {
        if (defaultParams == null || defaultParams.isEmpty()) {
            defaultParams = new HashMap<String, String[]>();
            // Add any default ("invisible") parameters here
            //defaultParams.put("filter-draft", new String[]{ "no" });
        
        }
        defaultParams.putAll(getUnmodifiableParameters());
        return defaultParams;
    }*/
    
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
                modNot(Project.Key.DRAFT),
                toParamVal(Project.Val.DRAFT_TRUE)
        );
    }
    
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
    public ProjectService setAllowDrafts(boolean allow) {
        Object currentSetting = getPresetParameters().get(modNot(Publication.Key.DRAFT)); // get("not-draft");
        
        if (!allow) {
                makeDefaultParameter(
                        modNot(Publication.Key.DRAFT),
                        toParamVal(Publication.Val.DRAFT_TRUE)
                );
        } else {
            if (currentSetting != null) {
                defaultParams.remove(modNot(Publication.Key.DRAFT));
            }
        }
        return this;
    }
    
    /**
     * Gets a list of unmodifiable parameters.
     * <p>
     * These are always used when accessing the service, and cannot be overridden.
     * 
     * @return A list of unmodifiable parameters, or an empty list if none. 
     */
    /*@Override
    public Map<String, String[]> getUnmodifiableParameters() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(Param.FORMAT, new String[] {ParamVal.FORMAT_JSON});
        return params;
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
}

