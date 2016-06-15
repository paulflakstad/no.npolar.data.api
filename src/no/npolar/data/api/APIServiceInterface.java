package no.npolar.data.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;

/**
 * Methods required by all implementing classes. 
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public interface APIServiceInterface {
    /**
     * Gets the service path, like e.g.: "/publication/", "/project/".
     * 
     * @return The service path.
     */
    public String getServicePath();
    
    /** 
     * Gets the base URL (that is, the complete URL, without parameters) to use when accessing the service. 
     * 
     * @return The base URL.
     */
    public String getServiceBaseURL();
    
    /**
     * Gets the total number of entries found in the response from the last service request.
     * 
     * @return The total number of entries found in the response from the last service request, or -1 if no request has yet been made.
     */
    public int getTotalResults();
    
    /**
     * Gets the number of items per page.
     * 
     * @return The number of items per page.
     */
    public int getItemsPerPage();
    
    /**
     * The start index, that is, the index of the first item on the current page.
     * 
     * @return The start index.
     */
    public int getStartIndex();
    
    /**
     * Queries the service using the given parameters.
     * <p>
     * The query results should be stored by the implementing class, with the 
     * resulting entries available via {@link #getEntries()}.
     * 
     * @param params The parameters to use in the query.
     * @return APIServiceInterface The updated instance, holding the query results. (Entries available via {@link #getEntries()}.)
     */
    public APIServiceInterface doQuery(Map<String, String[]> params)
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException;
    
    /**
     * Reads a single entry from the service using the given ID.
     * 
     * @param id The ID that uniquely identifies the single entry.
     * @return The JSON object describing the single entry, or null if no such entry could be found.
     */
    public JSONObject doRead(String id)
            throws java.io.UnsupportedEncodingException, MalformedURLException, IOException, JSONException, InstantiationException;
    
    /**
     * Returns all (if any) entries retrieved in the last executed query.
     * 
     * @see #doQuery(java.util.Map) 
     * @return A JSONArray containing all entries retrieved in the last query, or null if there were no entries.
     */
    public JSONArray getEntries();
    
    /**
     * Gets the complete set of parameters currently set for this object.
     * 
     * @return The complete set of parameters currently set for this object.
     */
    public Map<String, String[]> getParameters();
    
    /**
     * Gets the full URL used in the last service request.
     * 
     * @return The full URL used in the last service request, or null if no request has yet been issued.
     */
    public String getLastServiceURL();
    
    /**
     * Gets a single entry, identified by the given ID.
     * 
     * @param id The ID.
     * @return A single entry. 
     */
    public <T extends APIEntry> T get(String id);
    
    // Filtering narrows results by excluding stuff
    public APIServiceInterface addFilter(String fieldName, String val);
    public APIServiceInterface addFilter(String key, APIService.Delimiter del, String value, String ... moreValues);
    
    // Generic parameters, use for anything
    public APIServiceInterface addParameter(String key, String val);
    public APIServiceInterface addParameter(String key, APIService.Delimiter del, String value, String ... moreValues);
    
    // Default parameters, should be set by each service type (or instance)
    public APIServiceInterface addDefaultParameter(String key, APIService.Delimiter del, String value, String ... moreValues);
    public APIServiceInterface addDefaultParameter(String key, String value);
    
    public APIServiceInterface clearParameters();
}
