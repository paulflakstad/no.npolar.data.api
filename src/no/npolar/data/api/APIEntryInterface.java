package no.npolar.data.api;

import org.opencms.json.JSONObject;

/**
 * Common interface for all entries read via the API.
 * <p>
 * An entry may be for example
 * <ul>
 * <li>a publication</li>
 * <li>a time series</li>
 * <li>a project</li>
 * <li>a dataset</li>
 * <li>a person</li>
 * <li>...</li>
 * </ul>
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public interface APIEntryInterface {
    
    /**
     * Gets the name of the group in which the entry belongs. 
     * <p>
     * Many entry types have a natural group (e.g. a publication is of a 
     * specific <em>type</em>, and that type is then its group), but contrary 
     * cases may exist. 
     * <p>
     * If there is no natural group, then it is unlikely this method is used for
     * anything. Thus, the return value is arbitrary, but the recommended return 
     * values are return null or an empty string.
     * 
     * @return The entry's group name, if any.
     */
    public String getGroupName();
    
    /**
     * Gets the title for the entry.
     * <p> For localized entries, this method should return the title in an 
     * already defined (e.g. by the service, upon creating the instance) 
     * language variable.
     * 
     * @return The entry's title.
     */
    public String getTitle();
    
    /**
     * Gets the full URL for the entry, within the context of the given service. 
     * <p>
     * This service should always "fit" the entry whose URL is being fetched. 
     * <p>
     * E.g.: When getting a {@link Publication} URL, a service instance of type 
     * {@link PublicationService} should be provided.
     * 
     * @param service The API service instance.
     * @return The entry's full URL.
     */
    public String getURL(APIServiceInterface service);
    
    /**
     * Gets the ID for the entry.
     * <p>
     * The ID that uniquely identifies the entry within the API.
     * 
     * @return The entry's ID.
     */
    public String getId();
    
    /**
     * Gets the backing JSON object that forms the basis of this entry, as read 
     * from the Data Centre.
     * 
     * @return The JSON object forming the basis of this entry.
     */
    public JSONObject getJSON();
}
