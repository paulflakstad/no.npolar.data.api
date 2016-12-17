package no.npolar.data.api;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
//import static no.npolar.data.api.Publication.DATE_FORMAT_JSON;
//import static no.npolar.data.api.Publication.PATTERNS_PUB_TIME;
import no.npolar.data.api.util.APIUtil;
import no.npolar.data.api.util.Mapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONObject;

/**
 * Represents a dataset entry in the Data Centre.
 * <p>
 * ToDo: Improve this very crude version.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Dataset extends APIEntry {
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(Dataset.class);
    
    // Class members
    protected String title = "";
    protected String authors = "";
    protected String publisher = "";
    protected String publisherLocation = "";
    protected Date publishTime = null;
    protected SimpleDateFormat publishTimeFormat = null;
    protected String pubYear = "";
    protected Date pubDate = null;
    protected String articleNo = "";
    protected String doi = "";
    protected String link = "";
    protected String language = "";
    protected String state = "";
    protected String comment = "";
    protected List<String> sets = null;
    protected JSONArray links = null;
    protected JSONArray topics = null;
    //** The locale to use when generating strings meant for viewing. (Important especially for date formats etc.) */
    //protected Locale displayLocale = null;
    /** Collection to hold all contributors (persons). */
    protected PersonCollection allContribPersons = null;
    /** A mapper used for translating "code words" from the service into human-friendly content. */
    protected Mapper mappings = null;
    /** Localization resource bundle. */
    protected ResourceBundle labels = null;
    
    public class Key extends APIEntry.Key {
        /** Progress status. */
        public static final String PROGRESS = "progress";
        public static final String TOPICS_ISO = "iso_topics";
        public static final String TOPICS_NPI = "topics";
        public static final String SETS = "sets"; // e.g. "N-ICE2015"
        /** Tags. */
        public static final String TAGS = "tags";
        public static final String LICENSES = "licences";
        public static final String RIGHTS = "rights";
        /** People – authors, contacts, etc. */
        public static final String PEOPLE = "people";
        /** Title. */
        public static final String TITLE = "title";
        /** Version. */
        public static final String VERSION = "version";
        /** Language. */
        public static final String LANGUAGE = "lang";
        /** DOI. */
        public static final String DOI = "doi";
        /** Citation. */
        public static final String CITATION = "citation";
        /** Summary. */
        public static final String SUMMARY = "summary";
        /** Draft state. */
        public static final String DRAFT = "draft";
        
        
        
        
        /** Publish time: When the dataset was released. */
        public static final String PUB_TIME = "released";
        /** Links. */
        public static final String LINKS = "links";
        /** Link relation. */
        public static final String LINK_REL = "rel";
        /** Link target address. */
        public static final String LINK_HREF = "href";
        /** Language of the link target resource. */
        public static final String LINK_HREFLANG = "hreflang";
        /** Link type. */
        public static final String LINK_TYPE = "type";
        /** Dataset state. */
        public static final String STATE = "state";
        /** Comment. */
        public static final String COMMENT = "comment";
        /** Name - generic, used for journal name, person name, etc. */
        public static final String NAME = "name";
        /** First name (generic). */
        public static final String FNAME = "first_name";
        /** Last name (generic). */
        public static final String LNAME = "last_name";
        /** Home page (generic). */
        public static final String URI_HOME_PAGE = "homepage";
        /** Roles (generic). */
        public static final String ROLES = "roles";
        /** Email (generic). */
        public static final String EMAIL = "email";
        /** Contributor ID (currently the person's email). */
        public static final String CONTRIB_ID = "people.email";
        /** Contributor role. */
        public static final String CONTRIB_ROLE = "people.roles";
        /** Organization. */
        public static final String ORG = "organisation";
        /** Organizations. */
        public static final String ORGS = "organisations";
        /** Organizations ID. */
        public static final String ORGS_ID = "organisations.id";
        /** Research stations associated with the dataset. */
        public static final String STATIONS = "research_stations";
        /** Programmes associated with the dataset. */
        public static final String PROGRAMMES = "programme";
        /** Location. */
        public static final String LOCATION = "location";
    }
    
    public class Val extends APIEntry.Val {
        public static final String PROGRESS_ONGOING = "ongoing";
        public static final String PROGRESS_COMPLETE = "complete";
        public static final String PROGRESS_PLANNED = "planned";
        
        public static final String SET_NICE2015 = "N-ICE2015";
        public static final String SET_ARCTIC = "arctic";
        public static final String SET_GCMD_NASA_GOV = "gcmd.nasa.gov";
        public static final String SET_MARINE = "marine";
        
        /** Pre-defined draft value, used on entries that are NOT flagged as drafts. */
        public static final String DRAFT_FALSE = "no";
        /** Pre-defined draft value, used on entries that are flagged as drafts. */
        public static final String DRAFT_TRUE = "yes";
        /** Pre-defined value for link relation "DOI". */
        public static final String LINK_DOI = "doi";
        /** Pre-defined value for link relation "parent". */
        public static final String LINK_PARENT = "parent";
        /** Pre-defined value for link relation "related". */
        public static final String LINK_RELATED = "related";
        /** Pre-defined value for "Norwegian Polar Institute" as organization (an ID). */
        public static final String ORG_NPI = "npolar.no";
        /** Pre-defined JSON value: role "Author". */
        public static final String ROLE_AUTHOR = "author";
        /** Pre-defined value for role: "Principal Investigator". */
        public static final String ROLE_PRINCIPAL_INVESTIGATOR = "principalInvestigator";
        /** Pre-defined value for role: "Processor". */
        public static final String ROLE_PROCESSOR = "processor";
        /** Pre-defined value for role: "Point of Contact". */
        public static final String ROLE_POINT_OF_CONTACT = "pointOfContact";
        /** Pre-defined value for role: "Editor" (of Metadata). */
        public static final String ROLE_EDITOR = "editor";
        /** Pre-defined value for role: "Publisher". */
        public static final String ROLE_PUBLISHER = "originator";
        /** Pre-defined value for role: "Originator". */
        public static final String ROLE_ORIGINATOR = "originator";
        /** Pre-defined value for role: "Owner". */
        public static final String ROLE_OWNER = "owner";
        /** Pre-defined value for role: "Resource provider". */
        public static final String ROLE_RESOURCE_PROVIDER = "resourceProvider";
    }
    
    /**
     * Creates a new instance from the given JSON object.
     * 
     * @param pubObject The JSON object to use when constructing this instance.
     * @param loc The locale to use when generating strings for screen view. If <code>null</code>, the default locale (English) is used.
     */
    public Dataset(JSONObject pubObject, Locale loc) {
        super(pubObject, loc);
        if (this.o != null && this.o.has(Key.ID)) {
            init();
        }
    }
    
    /**
     * Builds this instance from the JSON.
     */
    protected final void init() {
        // Initialize the bundle (for localized labels)
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        // Create the mapper
        try { 
            mappings = new Mapper();
            // Translate 2-letter country, e.g. "NO" => "Norway"
            try { mappings.addAllPipeSeparated(labels.getString(Labels.DATA_COUNTRIES_0)); } catch (Exception e) { }
            // Translate general strings used by the service, e.g. "NP Report Series" => "Norwegian Polar Institute Report series"
            try { mappings.addAllPipeSeparated(labels.getString(Labels.DATA_DB_VALUES_0)); } catch (Exception e) { }
        } catch (Exception e) { }
        
        
        ////////////////////////////////////////////////////////////////////////
        // All the non-complex stuff
        try { title     = o.getString(Key.TITLE).trim(); } catch (Exception e) { title = labels.getString(Labels.LABEL_DEFAULT_TITLE_0); }
        try { language  = o.getString(Key.LANGUAGE); } catch (Exception e) { }
        try { state     = o.getString(Key.PROGRESS); } catch (Exception e) { }
        try { comment   = o.getString(Key.COMMENT); } catch (Exception e) { }
        try { links     = o.getJSONArray(Key.LINKS); } catch (Exception e) { }
        try { topics    = o.getJSONArray(Key.TOPICS_NPI); } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Publish time: Should be year OR month of year OR full date
        try { 
            
            publishTimeFormat = APIUtil.getTimestampFormat(o.getString(Key.PUB_TIME));
        } catch (Exception e) { 
            publishTimeFormat = new SimpleDateFormat(APIEntry.TimestampPattern.YEAR.toString(), displayLocale);
        }
        String publishTimeRaw = null;
        try { publishTimeRaw = o.getString(Key.PUB_TIME); } catch (Exception ignore) {}
        if (publishTimeRaw != null) {
            try { 

                publishTime = publishTimeFormat.parse(publishTimeRaw);
            } catch (Exception e) {
                //System.out.println("Unexpected format on publish time, no suitable parser available. Dataset ID was " + this.id);
                if (LOG.isErrorEnabled()) {
                    LOG.error("Unexpected format on publish time '" + publishTimeRaw  + "', no suitable parser available. Dataset ID was " + this.id);
                }
            }
            try {
                pubYear = new SimpleDateFormat(APIEntry.TimestampPattern.YEAR.toString(), displayLocale).format(publishTime);
            } catch (Exception e) {
                //System.out.println("Unable to determine publish year. Bad publish time format? Dataset ID was " + this.id);
                if (LOG.isErrorEnabled()) {
                    LOG.error("Unable to determine publish year for '" + publishTimeRaw  + "'. Bad publish time format? Dataset ID was " + this.id);
                }
            } finally {
                if (pubYear == null || pubYear.isEmpty()) {
                    try {
                        pubYear = publishTimeRaw.substring(0, 4);
                    } catch (Exception e) {
                        //System.out.println("Fallback routine for determining publish year failed. Dataset ID was " + this.id);
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Fallback routine for determining publish year failed. Dataset ID was " + this.id);
                        }
                    }
                }
            }
        } else {
            if (LOG.isErrorEnabled()) {
                LOG.error("Dataset with ID " + this.id + " is missing publish time.");
            }
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Sets (e.g. "N-ICE2015")
        if (o.has(Key.SETS)) {
        try {
            sets = Arrays.asList( APIUtil.jsonArrayToStringArray(o.getJSONArray(Key.SETS), mappings) );
        } catch (Exception e) {
            // log this?
        }
        }
        /*
        ////////////////////////////////////////////////////////////////////////
        // Publisher
        if (o.has(Key.ORGS)) {
            try {
                JSONArray orgs = o.getJSONArray(Key.ORGS);
                for (int i = 0; i < orgs.length(); i++) { // For each organisation in organisations
                    JSONObject org = orgs.getJSONObject(i);
                    if (org.has(Key.ROLES)) {
                        JSONArray roles = org.getJSONArray(Key.ROLES);
                        for (int j = 0; j < roles.length(); j++) {
                            String role = roles.getString(j);
                            if (role.equals(Val.ROLE_PUBLISHER)) { // This is a publisher
                                try {
                                    publisher += (publisher.isEmpty() ? "" : ", ");
                                    try {
                                        publisherLocation = org.getString(Key.LOCATION).trim(); // Get the publisher location, if any
                                        if (!publisherLocation.isEmpty())
                                            publisher += mappings.getMapping(publisherLocation); // Add the publisher location
                                    } catch (Exception pe) {
                                        publisherLocation = "";
                                    }
                                    
                                    try {
                                        String publisherName = org.getString(Key.NAME).trim(); // Add the publisher name, if any
                                        if (!publisherName.isEmpty())
                                            publisher += (publisherLocation.isEmpty() ? "" : ": ") + mappings.getMapping(publisherName); // Add the publisher name
                                    } catch (Exception pe) {
                                        // Ignore
                                    }
                                } catch (Exception e) {}
                            }
                        }
                    }
                }
            } catch (Exception e) { }
        }
        //*/
        
        /*
        ////////////////////////////////////////////////////////////////////////
        // People
        if (o.has(Key.PEOPLE)) {
            try {
                JSONArray persons = o.getJSONArray(Key.PEOPLE);
                allContribPersons = new PersonCollection(persons, displayLocale);
            } catch (Exception e) { }
        }
        //*/
        
        ////////////////////////////////////////////////////////////////////////
        // DOI
        // New routine - should work for all that have DOI
        try {
            //doi = o.getString(Key.DOI); 
            doi = o.getString(Key.DOI);
            //System.out.println("Got DOI " + doi);
        } catch (Exception e) {}
        /*
        // Old routine - just in case
        if (getDOI().isEmpty()) {
            try {
                for (int i = 0; i < links.length(); i++) {
                    JSONObject linkObj = links.getJSONObject(i);
                    try {
                        if (linkObj.getString(Key.LINK_REL).equalsIgnoreCase(Val.LINK_DOI)) {
                            doi = extractDoi(linkObj.getString(Key.LINK_HREF));
                        }                    
                    } catch (Exception doie) { }
                }
            } catch (Exception e) { }
        }
        //*/
        
        ////////////////////////////////////////////////////////////////////////
        // Translate markdown to HTML
        title = APIUtil.markdownToHtml(title, false);
        comment = APIUtil.markdownToHtml(comment);
    }
    
    /**
     * Extracts the DOI from the given full DOI URL.
     * <p>
     * E.g. extracts "10.1111/conl.12009" from "http://dx.doi.org/10.1111/conl.12009".
     * <p>
     * This is a String modifier. The given URL must be on the doi.org domain.
     * 
     * @param doiUrl The full DOI URL.
     * @return The DOI.
     */
    protected String extractDoi(String doiUrl) {
        final String DOI_DOMAIN = "doi.org/";
        if (doiUrl.contains(DOI_DOMAIN))
            return doiUrl.substring(doiUrl.indexOf(DOI_DOMAIN) + DOI_DOMAIN.length());
        return doiUrl;
    }
    
    /**
     * Gets the URL for this dataset, within the context of the given 
     * service.
     * <p>
     * The service should be of type {@link DatasetService}.
     * 
     * @param service The API service. Should be of type {@link DatasetService}.
     * @return The URL for this dataset, or: where it resides within the given service.
     */
    @Override
    public String getURL(APIServiceInterface service) {
        if (!(service instanceof DatasetService)) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Retrieving dataset URL using a service not of type " + DatasetService.class.getName() + " may produce unexpected results.");
            }
        }
        return service.getServiceBaseURL() + this.getId();
    }
    
    /**
     * Gets the URL to the entry in the human-friendly part of the Data Centre.
     * 
     * @param service The API service. Should be of type {@link DatasetService}.
     * @return The URL for this dataset, or: where it resides within the given service.
     * @see #getURL(no.npolar.data.api.APIServiceInterface) 
     * @see APIService#SERVICE_DOMAIN_NAME_HUMAN
     */
    public String getHumanURL(APIServiceInterface service) {
        return getURL(service).replace("://".concat(APIService.SERVICE_DOMAIN_NAME), "://".concat(APIService.SERVICE_DOMAIN_NAME_HUMAN));
    }
    
    /**
     * Gets the title for this dataset.
     * 
     * @return The title for this dataset.
     */
    @Override
    public String getTitle() { return title.trim(); }
    
    /**
     * Gets the "closed" title, that is, appended a period at the end - but only
     * if necessary.
     * <p>
     * If the title already ends with a one of the characters [?.!], it is 
     * considered already closed and no period will be appended.
     * 
     * @return The "closed" title.
     */
    public String getTitleClosed() { 
        String t = getTitle();
        return t.concat(t.matches(".*(\\?|\\.|\\!)$") ? "" : "."); 
    }
    
    /**
     * Gets the DOI for this dataset.
     * 
     * @return The DOI for this dataset, or an empty string if none.
     */
    public String getDOI() { return doi; }
    
    /**
     * Gets the group name for this dataset.
     * <p>
     * The group name is the same as the state.
     * 
     * @return The group name for this dataset.
     * @see #getState() 
     */
    @Override
    public String getGroupName() {
        return getState();
    }
    
    /**
     * Gets the state for this dataset.
     * 
     * @return The state for this dataset.
     */
    public String getState() {
        return this.state;
    }
    
    /**
     * Gets the publish year for this dataset.
     * 
     * @return The publish year for this dataset, or an empty string if none.
     */
    public String getPubYear() { return pubYear; }
    
    /**
     * Gets the publish timestamp for this dataset.
     * <p>
     * The formatting is based on the accuracy level of the publish time. See 
     * the {@link #init()} method.
     * 
     * @return The publish timestamp for this dataset, formatted based on the accuracy level of the publish time.
     */
    public String getPubTime() { return publishTimeFormat.format(publishTime); }
}
