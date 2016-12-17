package no.npolar.data.api;

import java.text.Collator;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import no.npolar.data.api.util.Mapper;
import no.npolar.data.api.util.OptLink;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static no.npolar.data.api.ProjectService.*;
import org.apache.commons.lang.StringUtils;
import org.opencms.util.CmsStringUtil;

/**
 *
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Person extends APIEntry/* implements APIEntryInterface*/ {
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(Person.class);
    
    /**
     * System keys / field names.
     */
    public static class Key extends APIEntry.Key {
        public static final String UUID          = "uuid";
        public static final String JOB_TITLE     = "jobtitle";
        public static final String JOB_TITLES    = "job_titles";
        public static final String EMPLOYMENT    = "employment";
        public static final String ON_LEAVE      = "on_leave";
        public static final String CURR_EMPLOYED = "currently_employed";
        public static final String WORKPLACE     = "workplace";
        public static final String ORGANIZATION  = "organisation";
        public static final String PHONE         = "phone";
        public static final String MOBILE        = "mobile";
        public static final String EMAIL         = "email";
        public static final String UPDATED       = "updated";
        public static final String HON_PREFIX    = "honorific_prefix";
        public static final String FNAME         = "first_name";
        public static final String LNAME         = "last_name";
        public static final String TITLE         = "title";
        public static final String ORGTREE       = "orgtree";
        public static final String LINKS         = "links";
        public static final String LINK_HREF     = "href";
        public static final String LINK_HREFLANG = "hreflang";
        public static final String LINK_REL      = "rel";
        public static final String LINK_TYPE     = "type";
        public static final String EVENTS        = "events";
        public static final String EVENT_TYPE    = "type";
        public static final String EVENT_DATE    = "date";
        public static final String LANGUAGE      = "lang";
    }
    
    /**
     * Pre-defined system values.
     */
    public static class Val extends APIEntry.Val {
        public static final String EVENT_TYPE_QUIT = "quit";
        public static final String LANG_EN = "en";
        public static final String LANG_NO = "no";
        
        public static final String CURR_EMPLOYED_TRUE = "true";
        public static final String CURR_EMPLOYED_FALSE = "false";
        
        public static final String ORG_ID_NPI = "npolar.no";

        public static final String STATE_PLANNED = "planned";
        public static final String STATE_ONGOING = "ongoing";
        public static final String STATE_COMPLETED = "completed";
        public static final String STATE_CANCELLED = "cancelled";
        
        public static final String TYPE_RESEARCH = "Research";
        public static final String TYPE_MONITORING = "Monitoring";
        public static final String TYPE_MODELLING = "Modeling";
        public static final String TYPE_MAPPING = "Mapping";
        public static final String TYPE_EDUCATION = "Education";
        
        public static final String DRAFT_TRUE = "yes";
        public static final String DRAFT_FALSE = "no";
        
        public static final String ROLE_OWNER = "owner";
        public static final String ROLE_PARTNER = "partner";
        public static final String ROLE_PROJ_LEADER = "projectLeader";
        public static final String ROLE_PROJ_PARTICIPANT = "projectParticipant";
    }
    
    public class PersonEvent {
        private String what;
        private String when;
        
        public PersonEvent(String what, String when) {
            this.what = what;
            this.when = when;
        }
        
        public String what() { return what; }
        public String when() { return when; }
    }
    
    /** The base URL for a person entry link. */
    public static final String URL_PERSON_LINK_BASE     = "http://data.npolar.no/person/";
    /** The default title, used if title is missing. */
    public static final String DEFAULT_TITLE            = "Unknown name";
    /** The default locale to use when generating strings meant for viewing. */
    public static final String DEFAULT_LOCALE_NAME      = "en";
    
    
    
    /**
     * Comparator for sorting people by their standard sort character, specialized for Norwegian.
     * <p>
     * Use this to make Æ, Ø and Å appear at the end (standard in Norwegian).
     * 
     * @see #getSortChar() 
     */
    public static final Comparator<Person> COMP_SORT_CHAR_NO = 
        new Comparator<Person>() {
            @Override
            public int compare(Person thisOne, Person thatOne) {
                java.text.Collator c = Collator.getInstance(new Locale("no")); // Use Norwegian to force Æ Ø Å to appear at the end
                return c.compare(thisOne.getSortChar(), thatOne.getSortChar());
            }
        };
    /**
     * Default comparator for sorting people by their standard sort character.
     * <p>
     * Use this to make Æ, Ø and Å appear along with A and O.
     * 
     * @see #getSortChar() 
     */
    public static final Comparator<Person> COMP_SORT_CHAR_DEFAULT = 
        new Comparator<Person>() {
            @Override
            public int compare(Person thisOne, Person thatOne) {
                java.text.Collator c = Collator.getInstance(new Locale(APIService.DEFAULT_LOCALE_NAME)); // Use English - Æ Ø Å will appear along with "A" and "O"
                return c.compare(thisOne.getSortChar(), thatOne.getSortChar());
            }
        };
    
    /** Mapper for string translations. */
    private Mapper mappings = null;
    
    // Class members
    //private String id = "";
    private String uuid = "";
    private String name = "";
    private String fName = "";
    private String lName = "";
    private String sortChar = "";
    private String phone = "";
    private String mobile = "";
    private String email = "";
    private String workplace = "";
    private String organization = "";    
    private Boolean currentlyEmployed = false;
    private List<String> positions;
    private List<String> internalOrg;
    private List<PersonEvent> events;
    private List<OptLink> links;
        
    /** Labels / translations. */
    protected ResourceBundle labels = null;
    
    /**
     * Creates a new instance from the given JSON object and locale.
     * 
     * @param rawObject The JSON object to create this instance from, typically read from the Data Centre.
     * @param loc The locale to use. If <code>null</code>, {@link DEFAULT_LOCALE_NAME default locale} is used.
     */
    public Person(JSONObject rawObject, Locale loc) {
        super(rawObject, loc);
        init();
    }
    
    /**
     * Builds this instance from the "raw" JSON object.
     */
    private void init() {
        // Create the mapper and populate it with initial mappings
        //try { mappings = new Mapper(); } catch (Exception e) { }
        
        // Initialize the bundle (for localized labels)
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        // Create the mapper
        try { 
            mappings = new Mapper();
            try { mappings.addAllPipeSeparated(labels.getString(Labels.DATA_COUNTRIES_0)); } catch (Exception e) { }
            try { mappings.addAllPipeSeparated(labels.getString(Labels.DATA_DB_VALUES_0)); } catch (Exception e) { }
        } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Simple stuff
        try { uuid = o.getString(Key.UUID); } catch (Exception e) { id = null; }
        try { fName = o.getString(Key.FNAME); } catch (Exception e) { }
        try { lName = o.getString(Key.LNAME); } catch (Exception e) { }
        try { name = fName.concat(" ").concat(lName); } catch (Exception e) { }
        try { currentlyEmployed = o.getBoolean(Key.CURR_EMPLOYED); } catch (Exception e) { }
        try { phone = o.getString(Key.PHONE); } catch (Exception e) { }
        try { mobile = o.getString(Key.MOBILE); } catch (Exception e) { }
        try { email = o.getString(Key.EMAIL); } catch (Exception e) { }
        try { workplace = o.getString(Key.WORKPLACE); } catch (Exception e) { }
        try { organization = o.getString(Key.ORGANIZATION); } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Position(s)
        if (true) {
            try {
                // Current version:
                // "jobtitle" : {
                //      "en" : "Web developer",
                //      "no" : "Webutvikler"
                // }
                positions.add(o.getJSONObject(Key.JOB_TITLE).getString(displayLocale.toString())); 
            } catch (Exception e) {}
        } else {
            try {
                // IMAGINED Future version:
                // "job_titles" : [
                //      { "lang" : "en", "title" : "Research scientist" },
                //      { "lang" : "en", "title" : "Leader, ..." },
                //      { "lang" : "no", "title" : "Forsker" },
                //      { "lang" : "no", "title" : "Leder, ..." }
                // ]
                positions.addAll(APIUtil.getStringsByLocale(
                        o.getJSONArray(Key.JOB_TITLES), 
                        Key.TITLE, 
                        displayLocale
                ));
            } catch (Exception e) {} // Not array
        }
        ////////////////////////////////////////////////////////////////////////
        // Internal organizational affiliation
        try { 
            JSONArray intOrgArr = o.getJSONArray(Key.ORGTREE);
            for (int i = 0; i < intOrgArr.length(); i++) {
                try { internalOrg.add(intOrgArr.getString(i)); } catch (Exception ee) {}
            }
        } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Events
         try {
            JSONArray eventsArr = o.getJSONArray(Key.EVENTS);
            events = new ArrayList<PersonEvent>(eventsArr.length());
        } catch (Exception e) { }
        
        
        ////////////////////////////////////////////////////////////////////////
        // Links
        try {
            JSONArray linksArr = o.getJSONArray(Key.LINKS);
            links = new ArrayList<OptLink>(linksArr.length());
        } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Set the standard sort character
        setSortChar();
    }
    
    /**
     * Sets the standard sort character (last name mode).
     */
    private void setSortChar() {
        if (lName == null || CmsStringUtil.isEmptyOrWhitespaceOnly(lName)) {
            sortChar = "";
        }

        int i = 0;
        while (true) {
            try {
                String letter = String.valueOf(lName.charAt(i));
                if (i == 0) {
                    try {
                        String secondLetter = String.valueOf(lName.charAt(i+1));
                        if (letter.equalsIgnoreCase("a") && secondLetter.equalsIgnoreCase("a")) { // "Aa" = "Å"
                            sortChar = "Å";
                            break;
                        }
                    } catch (Exception e) {
                        // Never mind that then
                    }
                }
                if (StringUtils.isAllUpperCase(letter)) {
                    sortChar = letter;
                    break;
                }
            } catch (Exception e) {
                break;
            }
            i++;
        }
    }
    
    public String getUuid() {
        return uuid;
    }
    public String getName() {
        return name;
    }
    public String getFirstName() {
        return fName;
    }
    public String getLastName() {
        return lName;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
    public String getMobile() {
        return mobile;
    }
    public String getOrganization() {
        return organization;
    }
    public String getWorkplace() {
        return workplace;
    }
    public boolean isCurrentlyEmployed() {
        return currentlyEmployed;
    }
    public List<String> getInternalOrgAffil() {
        return internalOrg;
    }
    public List<String> getPositions() {
        return positions;
    }
    
    /**
     * @return Always returns an empty string.
     * @see APIEntryInterface#getGroupName()
     */
    @Override
    public String getGroupName() {
        return "";
    }
    
    /**
     * @return The person's name.
     */
    @Override
    public String getTitle() {
        return getName();
    }

    /**
     * @return The links.
     */
    public List<OptLink> getLinks() {
        return links;
    }

    /**
     * @return The events.
     */
    public List<PersonEvent> getEvents() {
        return events;
    }
    
    /**
     * Gets the standard sort character (last name mode).
     * 
     * @return The standard sort character (last name mode).
     */
    public String getSortChar() {
        return sortChar;
    }
    
    /** 
     * @return A crude string representation of this person.
     */
    @Override
    public String toString() {
        return getTitle();
    }
    
    /**
     * Gets the URL for this person, within the context of the given service.
     * <p>
     * The service should be of type {@link PersonService}.
     * 
     * @param service The API service. Should be of type {@link PersonService}.
     * @return The URL for this person, or: the location within the given service.
     */
    @Override
    public String getURL(APIServiceInterface service) {
        if (!(service instanceof PersonService)) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Retrieving person URL using a service not of type " + PersonService.class.getName() + " may produce unexpected results.");
            }
        }
        return service.getServiceBaseURL() + this.getId();
    }
    
    /**
     * Gets a comparator that can be used for sorting on the standard sort 
     * character (last name mode).
     * <p>
     * The comparator will be configured to fit the language of the given locale.
     * 
     * @param locale The preferred locale.
     * @return The ready-to use comparator for sorting on standard sort character, adapted to the given locale.
     */
    public static Comparator<Person> getComparatorSortChar(Locale locale) {
        if (APIUtil.localeIsNorwegian(locale)) {
            return COMP_SORT_CHAR_NO;
        }
        return COMP_SORT_CHAR_DEFAULT;
    }
}
