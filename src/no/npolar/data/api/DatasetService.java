package no.npolar.data.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static no.npolar.data.api.APIService.DEFAULT_LOCALE_NAME;
import static no.npolar.data.api.APIService.SERVICE_DOMAIN_NAME;
import static no.npolar.data.api.APIService.SERVICE_PORT;
import static no.npolar.data.api.APIService.SERVICE_PROTOCOL;
import static no.npolar.data.api.APIService.modNot;
import static no.npolar.data.api.APIService.toParamVal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;

/**
 * Service for accessing datasets in the Data Centre.
 * <p>
 * ToDo: Improve this very crude version.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class DatasetService extends APIService {
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(DatasetService.class);
    
    /** The URL path to use when accessing the service. */
    protected static final String SERVICE_PATH = "dataset/";
    /** The base URL (that is, the complete URL before adding parameters) to use when accessing the service. */
    protected static final String SERVICE_BASE_URL = SERVICE_PROTOCOL + "://" + SERVICE_DOMAIN_NAME + ":" + SERVICE_PORT + "/" + SERVICE_PATH;
    
    /**
     * Creates a new dataset service instance.
     * 
     * @param loc The locale to use when generating strings for screen view. If <code>null</code>, the {@link APIService#DEFAULT_LOCALE_NAME default locale} is used.
     */
    public DatasetService(Locale loc) {
        this.displayLocale = loc;
        if (displayLocale == null)
            this.displayLocale = new Locale(DEFAULT_LOCALE_NAME);
        
        initPresetParameters();
    }
    
    /**
     * Creates a new dataset, based on the given ID.
     * <p>
     * The ID is used to construct the URL that is uniquely identifies the entry 
     * within the Data Centre.
     * 
     * @param id The dataset ID.
     * @return The dataset object, or <code>null</code> if no such dataset could be created.
     */
    public Dataset get(String id) {
        try {
            return new Dataset(doRead(id), displayLocale);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read dataset with ID "+id, e);
            }
            return null;
        }
    }
    
    /**
     * Queries the service using already set parameters and returns all (if any)
     * datasets, generated from the service response.
     * 
     * @return A list of all datasets, generated from the service response, or an empty list if no datasets matched.
     * @see APIService#doQuery(java.util.Map) 
     * @see APIService#getEntries() 
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException
     */
    public GroupedCollection<Dataset> getDatasets() 
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException {
        
        // Define the order of the grouping
        //String[] order = {};
        
        GroupedCollection<Dataset> gc = new GroupedCollection<Dataset>();
        //gc.setOrder(order);
        
        doQuery();
        
        if (entries != null) {
            for (int i = 0; i < entries.length(); i++) {
                try {
                    gc.add(new Dataset(entries.getJSONObject(i), displayLocale));
                } catch (Exception e) {
                    throw new InstantiationException("Error when trying to create datasets list: " + e.getMessage());
                }
            }
        }
        return gc;
    }
    
    /**
     * Queries the service using the given parameters and returns all (if any)
     * datasets, generated from the service response.
     * 
     * @param params The parameters to use in the service request.
     * @return A list of all datasets, generated from the service response, or an empty list if no datasets matched.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     */
    public List<Dataset> getDatasetList(Map<String, String[]> params) 
            throws java.io.UnsupportedEncodingException, 
            MalformedURLException, 
            IOException, 
            JSONException, 
            InstantiationException {
        
        List<Dataset> list = new ArrayList<Dataset> ();
        
        addParameters(params);
        
        doQuery();
        
        if (entries != null) {
            for (int i = 0; i < entries.length(); i++) {
                /*try {*/
                    list.add(new Dataset(entries.getJSONObject(i), displayLocale));
                /*} catch (Exception e) {
                    throw new InstantiationException("Error when trying to create datasets list: " + e.getMessage());
                }*/
            }
        }
        return list;
    }
    
    /**
     * Gets a list of datasets, using the current settings.
     * <p>
     * Intended used <strong>after</strong> having set parameters using 
     * {@link #addDefaultParameter(java.lang.String, java.lang.String)}, 
     * {@link #addParameter(java.lang.String, java.lang.String)}, 
     * {@link #addFilter(java.lang.String, java.lang.String)}, 
     * {@link #setQueryString(java.lang.String)}, etc.
     * 
     * @return A list of datasets matching the current set of parameters, or an empty list if none matched.
     * @throws java.io.UnsupportedEncodingException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws InstantiationException 
     */
    public List<Dataset> getDatasetList() 
            throws java.io.UnsupportedEncodingException, 
            MalformedURLException, 
            IOException, 
            JSONException, 
            InstantiationException {
        
        return getDatasetList(null);
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
                modNot(Dataset.Key.DRAFT),
                toParamVal(Dataset.Val.DRAFT_TRUE)
        );
    }
    
    /**
     * Adjust setting for whether or not to include entries flagged as drafts.
     * <p>
     * This setting will apply appropriate adjustment to the current set of 
     * default parameters. Any later overriding of the default parameters may
     * overwrite the setting done here.
     * 
     * @param allow Provide <code>true</code> to allow drafts, <code>false</code> to disallow.
     * @return This service instance, updated with the new value.
     */
    public DatasetService setAllowDrafts(boolean allow) {
        Object currentSetting = getPresetParameters().get(modNot(Dataset.Key.DRAFT)); // get("not-draft");
        
        if (!allow) {
                makeDefaultParameter(
                        modNot(Dataset.Key.DRAFT),
                        toParamVal(Dataset.Val.DRAFT_TRUE)
                );
        } else {
            if (currentSetting != null) {
                defaultParams.remove(modNot(Dataset.Key.DRAFT));
            }
        }
        return this; 
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
