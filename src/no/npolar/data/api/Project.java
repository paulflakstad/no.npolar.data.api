package no.npolar.data.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import no.npolar.data.api.util.Mapper;
import no.npolar.data.api.util.OptLink;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.poi.hpsf.Thumbnail;

/**
 * Represents a single project entry, as read from the Norwegian Polar 
 * Institute Data Centre.
 * <p>
 * Projects are typically created by a {@link ProjectService} instance 
 * acting as an interface to the Data Centre.
 * 
 * @see https://github.com/npolar/api.npolar.no/blob/master/schema/project.json
 * @see https://data.npolar.no/projects/
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Project extends APIEntry /*implements APIEntryInterface*/ {

    /** The logger. */
    private static final Log LOG = LogFactory.getLog(Project.class);
    
    /**
     * System keys / field names.
     */
    public static class Key extends APIEntry.Key {
        public static final String WORKSPACE     = "workspace";
        public static final String WEBSITE_FLAG  = "website";
        public static final String TITLE         = "title";
        public static final String ABBREV_TITLE  = "acronym";
        public static final String DESCRIPTION   = "summary";
        public static final String ABSTRACT      = "abstract";
        public static final String STATE         = "translations";
        public static final String NPI_ID        = "np_project_number";
        public static final String RIS_ID        = "ris_id";
        public static final String KEYWORDS      = "keywords";
        public static final String LOGO          = "logo_image_url";
        public static final String FEATURED_IMAGE= "featured_image_url";
        public static final String BEGIN         = "start_date";
        public static final String END           = "end_date";
        public static final String TYPE          = "type";
        public static final String DRAFT         = "draft";
        public static final String GEO_AREA      = "geo_area";
        public static final String PLACENAMES    = "placenames";
        public static final String PLACENAME     = "placename";
        public static final String AREA          = "area";
        public static final String WEBSITE       = "project_url";
        public static final String PEOPLE        = "people";
        public static final String LEADERS       = "project_leaders";
        public static final String PERSON_FNAME  = "first_name";
        public static final String PERSON_LNAME  = "last_name";
        public static final String PERSON_URI    = "url";
        public static final String PERSON_EMAIL  = "email";
        public static final String PERSON_AFFIL  = "affiliation";
        public static final String ROLE          = "role";
        public static final String ORG           = "org";
        public static final String NAME          = "name";
        public static final String ORGANISATION  = "organisation";
        public static final String ORGANISATIONS = "organisations";
        public static final String PERSON_INST   = "institution";
        public static final String PARTICIPANTS  = "project_participants";
        public static final String PARTNER       = "contract_partners";
        public static final String TOPICS        = "topics";
        public static final String AFFIL_ICE     = "affiliated_ICE";
        public static final String RES_PROG      = "research_programs";
        public static final String RES_PROG_TITLE= "title";
        public static final String RES_PROG_URI  = "href";
        public static final String TRANSLATIONS  = "translations";
    }
    
    /**
     * Pre-defined system values.
     */
    public static class Val extends APIEntry.Val {
        public static final String PROJECT_LEADER = "projectLeader";
        public static final String PROJECT_PARTICIPANT = "projectParticipant";

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
        public static final String ROLE_LEADER = "projectLeader";
        public static final String ROLE_PARTICIPANT = "projectParticipant";
    }
    
    
    /** 
     * The date format used in the JSON. 
     * @deprecated There is no longer a single timestamp pattern, see {@link APIEntry.TimestampPattern}.
     */
    public static final String DATE_FORMAT_PATTERN_JSON = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    //** The base URL for publication links. */
    //public static final String URL_PUBLINK_BASE         = "http://data.npolar.no/publication/";
    /** The default title, used if title is missing. */
    public static final String DEFAULT_TITLE            = "Unknown title";
    /** The default locale to use when generating strings meant for viewing. */
    public static final String DEFAULT_LOCALE_NAME      = "en";
    
    /** Project state: Undefined. */
    public static final int STATE_UNDEFINED = -1;
    /** Project state: Planned. */
    public static final int STATE_PLANNED = 0;
    /** Project state: Active. */
    public static final int STATE_ACTIVE = 1;
    /** Project state: Completed. */
    public static final int STATE_COMPLETED = 2;
    
    /** Project state name: Undefined. */
    public static final String STATE_NAME_UNDEFINED = "project_state_undefined";
    /** Project state name: Planned. */
    public static final String STATE_NAME_PLANNED = "project_state_planned";
    /** Project state name: Active. */
    public static final String STATE_NAME_ACTIVE = "project_state_active";
    /** Project state name: Completed. */
    public static final String STATE_NAME_COMPLETED = "project_state_completed";
    
    //** The backing JSON that describes the project. */
    //private JSONObject o = null;
    //** The locale to use when generating strings meant for viewing. (Important especially for date formats etc.) */
    //private Locale displayLocale = null;
    /** Mapper for string translations. */
    private Mapper mappings = null;
    
    // Class members
    //private String id = "";
    private String title = "";
    private String titleAbbrev = "";
    private String npiId = "";
    private String risId = "";
    private Date dateStart = null;
    private Date dateEnd = null;
    
    private boolean featured        = false;
    private String keywords         = "";
    private String websiteTitle     = "";
    private String websiteUri       = "";
    private String timeDisplay      = "";
    private String programmeTitle   = "";
    private String programmeUri     = "";
    private String status           = "";
    private String personTitle      = "";
    private String personUri        = "";
    private String personFirstName  = "";
    private String personLastName   = "";
    private String participantTitle = "";
    private String participantUri   = "";
    private String logoUri          = "";
    private String imageUri         = "";
    private String imageAlt         = "";
    private String imageCaption     = "";
    private String imageSource      = "";
    private String imageType        = "";
    private String imageSize        = "";
    private String imageFloat       = "";
    private String description      = "";
    private String abstr            = "";
    private String type             = "";
    //private String topics           = "";
    
    protected JSONArray topics = null;
    
    //private final SimpleDateFormat DATE_FORMAT_JSON = new SimpleDateFormat(DATE_FORMAT_PATTERN_JSON);
    private SimpleDateFormat dfOutput = null;
    
    private List<ProjectParticipant> leaders = new ArrayList<ProjectParticipant>();
    private List<ProjectParticipant> participants = new ArrayList<ProjectParticipant>();
    private List<OptLink> programmes = new ArrayList<OptLink>();
    private List<OptLink> placenames = new ArrayList<OptLink>();
    private List<OptLink> partners  = new ArrayList<OptLink>();
    
    /** Keeps track of generated symbols. */
    private HashMap<String, String> symbolMappings = new HashMap<String, String>();
    
    /** Keeps track of the number of generated symbols. */
    private int symbolsGenerated = 0;
    
    /** Labels / translations. */
    protected ResourceBundle labels = null;
    
    /**
     * Creates a new instance from the given JSONObject and using the given 
     * CmsAgent to create valid links and determine localization.
     * 
     * @param projectObject The JSON to create this instance from.
     * @param loc The locale to use. If null, the {@link APIService#DEFAULT_LOCALE_NAME default locale} is used.
     */
    public Project(JSONObject projectObject, Locale loc) {
        super(projectObject, loc);
        /*
        this.o = projectObject;
        this.displayLocale = loc;
        if (displayLocale == null)
            this.displayLocale = new Locale(APIService.DEFAULT_LOCALE_NAME);
        //*/
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        
        this.dfOutput = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_0), displayLocale);
        init();
    }
    
    /**
     * Builds this instance by interpreting the JSON source.
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
        //try { id = o.getString(Key.ID); } catch (Exception e) { id = null; }
        try { title = o.getString(Key.TITLE); } catch (Exception e) { title = DEFAULT_TITLE; }
        try { titleAbbrev = o.getString(Key.ABBREV_TITLE); } catch (Exception e) { }
        try { npiId = o.getString(Key.NPI_ID); } catch (Exception e) { }
        try { risId = o.getString(Key.RIS_ID); } catch (Exception e) { }
        try { description = o.getString(Key.DESCRIPTION); } catch (Exception e) { }
        try { abstr = o.getString(Key.ABSTRACT); } catch (Exception e) { }
        try { type = o.getString(Key.TYPE); } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Topics
        try { topics    = o.getJSONArray(Key.TOPICS); } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Simple stuff that needs string substitution
        //try { area = listToString(Arrays.asList(jsonArrayToStringArray(p.getJSONArray(Key.GEO_AREA)))); } catch (Exception e) { }
        try { keywords  = APIUtil.listToString(Arrays.asList(APIUtil.jsonArrayToStringArray(o.getJSONArray(Key.KEYWORDS), null)), null); } catch (Exception e) { }
        //try { type      = APIUtil.listToString(Arrays.asList(APIUtil.jsonArrayToStringArray(o.getJSONArray(Key.TYPE), null)), mappings); } catch (Exception e) { }
        //try { topics    = APIUtil.listToString(Arrays.asList(APIUtil.jsonArrayToStringArray(o.getJSONArray(Key.TOPICS), null)), mappings); } catch (Exception e) { }


        ////////////////////////////////////////////////////////////////////////
        // Start & end dates
        try {
            dateStart = getDate(o.getString(Key.BEGIN));// DATE_FORMAT_JSON.parse(o.getString(Key.BEGIN));
        } catch (Exception e) {}

        try {
            dateEnd = getDate(o.getString(Key.END));//DATE_FORMAT_JSON.parse(o.getString(Key.END));
        } catch (Exception e) {}

        ////////////////////////////////////////////////////////////////////////
        // Placenames
        try {
            JSONArray placenamesArr = o.getJSONArray(Key.PLACENAMES);
            for (int i = 0; i < placenamesArr.length(); i++) {
                JSONObject placenameObj = placenamesArr.getJSONObject(i);
                String placename = "";
                String area = "";
                try { placename = placenameObj.getString(Key.PLACENAME); } catch (Exception e) {  }
                try { area = placenameObj.getString(Key.AREA); } catch (Exception e) {  }
                placenames.add(new OptLink("" + placename + (placename.isEmpty() ? "" : ", ") + area));
            }
        } catch (Exception e) {

        }

        ////////////////////////////////////////////////////////////////////////
        // People
        try {
            JSONArray peopleArr = o.getJSONArray(Key.PEOPLE);
            for (int i = 0; i < peopleArr.length(); i++) {
                JSONObject personObj = peopleArr.getJSONObject(i);
                ProjectParticipant participant = new ProjectParticipant(personObj.getString(Key.PERSON_FNAME), personObj.getString(Key.PERSON_LNAME));
                try { 
                    // Organization (per person)
                    //String orgName = personObj.getJSONObject(Key.PERSON_AFFIL).getString(Key.ORG);
                    String orgName = personObj.getString(Key.ORGANISATION);
                    if (!orgName.isEmpty()) {
                        String symbol = "";
                        if (!symbolMappings.containsValue(orgName)) {
                            symbol = getNextOrgSymbol();
                            symbolMappings.put(symbol, orgName);
                        } else {
                            symbol = getCurrentOrgSymbol();
                        }
                        participant.setInstitutionSymbol(symbol);
                        participant.setOrganization(orgName);
                        /*
                        person.setOrganization(orgName);
                        if (symbolMappings.get(orgName) == null) {
                            symbolMappings.put(orgName, getNextOrgSymbol());
                        }
                        */
                    }
                } catch (Exception e) {  }
                try { participant.setUri(personObj.getString(Key.PERSON_URI)); } catch (Exception e) {  }
                String role = "";
                try { role = personObj.getString(Key.ROLE); } catch (Exception e) {}
                if (role.equals(Val.PROJECT_LEADER))
                    leaders.add(participant);
                else 
                    participants.add(participant);
            }
        } catch (Exception e) {}

        ////////////////////////////////////////////////////////////////////////
        // Organizations (per project) - aka "partners"
        try {
            JSONArray organisationsArr = o.getJSONArray(Key.ORGANISATIONS);
            for (int i = 0; i < organisationsArr.length(); i++) {
                JSONObject organisationObj = organisationsArr.getJSONObject(i);
                String orgName = "";
                String orgRole = "";
                try { orgName = organisationObj.getString(Key.NAME); } catch (Exception e) {  }
                try { orgRole = organisationObj.getString(Key.ROLE); } catch (Exception e) {  }

                partners.add(new OptLink("" + orgName));// + (orgName.isEmpty() ? "" : ", ") + orgRole));
            }
        } catch (Exception e) {}

        ////////////////////////////////////////////////////////////////////////
        // translations
        try {
            JSONObject translationsObj = o.getJSONObject(Key.TRANSLATIONS).getJSONObject(mappings.getMapping(displayLocale.toString()));
            try {
                description = translationsObj.getString(Key.DESCRIPTION);
            } catch (Exception e) {}
            try {
                abstr = translationsObj.getString(Key.ABSTRACT);
            } catch (Exception e) {}
        } catch (Exception e) {}


        /*
        try {
            JSONArray leadersArr = p.getJSONArray(Key.LEADERS);
            for (int i = 0; i < leadersArr.length(); i++) {
                JSONObject leader = leadersArr.getJSONObject(i);
                Person person = new Person(leader.getString(Key.PERSON_FNAME), leader.getString(Key.PERSON_LNAME));
                try { 
                    String inst = leader.getString(Key.PERSON_INST);
                    person.setInstitution(inst);
                    if (symbolMappings.get(inst) == null) {
                        symbolMappings.put(inst, getNextOrgSymbol());
                    }
                } catch (Exception e) {  }
                try { person.setUri(leader.getString(Key.PERSON_URI)); } catch (Exception e) {  }
                leaders.add(person);
            }
        } catch (Exception e) {}

        try {
            JSONArray participantsArr = p.getJSONArray(Key.PARTICIPANTS);
            for (int i = 0; i < participantsArr.length(); i++) {
                JSONObject participant = participantsArr.getJSONObject(i);
                Person person = new Person(participant.getString(Key.PERSON_FNAME), participant.getString(Key.PERSON_LNAME));
                try { 
                    String inst = participant.getString(Key.PERSON_INST);
                    person.setInstitution(inst);
                    if (symbolMappings.get(inst) == null) {
                        symbolMappings.put(inst, getNextOrgSymbol());
                    }
                } catch (Exception e) {  }
                try { person.setUri(participant.getString(Key.PERSON_URI)); } catch (Exception e) {  }
                participants.add(person);
            }
        } catch (Exception e) {}
        */
        ////////////////////////////////////////////////////////////////////////
        // Programmes
        try {
            JSONArray programmesArr = o.getJSONArray(Key.RES_PROG);
            for (int i = 0; i < programmesArr.length(); i++) {
                JSONObject programme = programmesArr.getJSONObject(i);
                OptLink optLink = new OptLink(mappings.getMapping(programme.getString(Key.RES_PROG_TITLE)));
                try { optLink.setUri(programme.getString(Key.RES_PROG_URI)); } catch (Exception e) {  }
                programmes.add(optLink);
            }
        } catch (Exception e) {}



        ////////////////////////////////////////////////////////////////////////
        //
        // Some extra processing on organizations (merge 2 sets into 1) ...
        //
        // Remove duplicate organizations (both in "partners" and "affiliated 
        // institutions")
        Iterator<String> iAffOrg = symbolMappings.keySet().iterator();
        while (iAffOrg.hasNext()) {
            String _sym = iAffOrg.next();
            String _org = symbolMappings.get(_sym);

            Iterator<OptLink> iPartners = partners.iterator();
            while (iPartners.hasNext()) {
                OptLink _partner = iPartners.next();
                //out.println("<!-- duplicate evaluation: '" + _partner.getText() + "' vs. '" + _org.split(",")[0] + "' (" + (_partner.getText().startsWith(_org.split(",")[0])) + ") -->");
                if (_partner.getText().startsWith(_org.split(",")[0])) {
                    iPartners.remove();
                }
            }
        }
        // Merge affiliate organizations with "partners" list
        if (!symbolMappings.isEmpty()) {
            SortedSet<String> keys = new TreeSet<String>(symbolMappings.keySet());
            Iterator<String> iSymbolMappings = keys.iterator();
            int addAt = 0;
            while (iSymbolMappings.hasNext()) {
               String symbol = iSymbolMappings.next();
               String inst = symbolMappings.get(symbol);
                partners.add(addAt++, new OptLink("<strong>" + symbol + "</strong> " + inst));
            }
        }
    }
    
    /**
     * Gets the current symbol (used in affiliation lists).
     * 
     * @return The current symbol.
     */
    private String getCurrentOrgSymbol() {
        String symbol = Integer.toString(symbolsGenerated);
        /*
        String symbol = "";
        try {
            int symbolIndex = (symbolsGenerated + symbols.length) % symbols.length;
            int repeat = 1 + symbolsGenerated / symbols.length;
            for (int j = 0; j < repeat; j++)
                symbol += symbols[symbolIndex];
            symbolsGenerated++;
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            // whut WHUT???
        }
        */
        return symbol;
    }

    /**
     * Gets the next symbol (used in affiliation lists).
     * 
     * @return The next symbol.
     */
    private String getNextOrgSymbol() {
        String symbol = Integer.toString(++symbolsGenerated);
        /*
        String symbol = "";
        try {
            int symbolIndex = (symbolsGenerated + symbols.length) % symbols.length;
            int repeat = 1 + symbolsGenerated / symbols.length;
            for (int j = 0; j < repeat; j++)
                symbol += symbols[symbolIndex];
            symbolsGenerated++;
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            // whut WHUT???
        }
        */
        return symbol;
    }
    
    /**
     * Gets the project's state, which is determined by investigating the start 
     * and possibly end date.
     * <p>
     * If no start date is set, {@link #STATE_UNDEFINED) is returned.
     * 
     * @return The project's state.
     * @see #STATE_UNDEFINED
     * @see #STATE_ACTIVE
     * @see #STATE_PLANNED
     * @see #STATE_COMPLETED
     */
    public int getState() {
        if (getDateStart() != null) {
            Date now = new Date();
            int state = STATE_ACTIVE;
            if (getDateStart().after(now))
                state = STATE_PLANNED;
            if (getDateEnd() != null && getDateEnd().before(now))
                state = STATE_COMPLETED;
            return state;
        }
        return STATE_UNDEFINED;
    }
    
    /**
     * Gets the duration as a localized string, either as HTML or plain text.
     * 
     * @param html Provide true to get as HTML, or false to get as plain text.
     * @return The duration as a localized string, either as HTML or plain text.
     */
    public String getDuration(boolean html) {
        if (getDateStart() == null)
            return "";
        String s = dfOutput.format(getDateStart());
        if (getDateEnd() != null)
            s += (html ? "&nbsp;&ndash;&nbsp;" : " - ") + dfOutput.format(getDateEnd());
        return s;
    }
    
    private Date getDate(String timestamp) {
        try { 
            return APIUtil.getTimestampFormat(timestamp).parse(timestamp);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to determine start for project with ID " + this.id);
            }
            return new Date();
        }
    }
    
    /**
     * Gets the state "name" (the state in human readable text) for this project.
     * 
     * @return This project's state name.
     */
    public String getStateName() {
        if (getState() == STATE_PLANNED)
            return STATE_NAME_PLANNED;
        else if (getState() == STATE_ACTIVE)
            return STATE_NAME_ACTIVE;
        else if (getState() == STATE_COMPLETED)
            return STATE_NAME_COMPLETED;
        
        return STATE_NAME_UNDEFINED;
    }
    /**
     * Gets the project participant(s) as a string.
     * 
     * @param html Provide true to get as HTML, or false to get as plain text.
     * @return The project participant(s) as a string.
     */
    public String getParticipantsStr(boolean html) {
        return participantListToString(participants, html);
    }
    /**
     * Gets the project leader(s) as a string.
     * 
     * @param html Provide true to get as HTML, or false to get as plain text.
     * @return The project leader(s) as a string.
     */
    public String getLeadersStr(boolean html) {
        return participantListToString(leaders, html);
    }
    /**
     * Translates the given list of project participants to a string.
     * 
     * @param participants The project participants.
     * @param html Provide true to get as HTML, or false to get as plain text.
     * @return The project participants, as a string.
     */
    protected String participantListToString(List<ProjectParticipant> participants, boolean html) {
        String s = "";
        if (!participants.isEmpty()) {
            Iterator<ProjectParticipant> i = participants.iterator();
            while (i.hasNext()) {
                ProjectParticipant p = i.next();
                s += p.getFirstName() + " " + p.getLastName() 
                        //+ (p.getInstitution() != null ? " (" + p.getInstitution() + ")" : "")
                        ;
                //s += participant.toString(new Mapper());
                if (i.hasNext())
                    s += ", ";
            }
        }
        return s;
    }
    
    /**
     * @see APIEntryInterface#getGroupName()
     */
    @Override
    public String getGroupName() {
        return getStateName();
    }
    
    /**
     * @return the title.
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * @return the title abbreviation, or an empty string if none.
     */
    public String getTitleAbbrev() {
        return titleAbbrev;
    }

    /**
     * @return the NPI project ID.
     */
    public String getNpiId() {
        return npiId;
    }

    /**
     * @return the RIS project ID.
     */
    public String getRisId() {
        return risId;
    }

    /**
     * @return the start date.
     */
    public Date getDateStart() {
        return dateStart;
    }

    /**
     * @return the end date.
     */
    public Date getDateEnd() {
        return dateEnd;
    }

    /**
     * @return true if the project is featured, false if not.
     */
    public boolean isFeatured() {
        return featured;
    }

    /**
     * @return the keywords.
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the abstract.
     */
    public String getAbstr() {
        return abstr;
    }

    /**
     * @return the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @return the topics.
     */
    /*public String getTopics() {
        return topics;
    }*/
    
    
    
    /**
     * Gets the topics for this project, if any.
     * 
     * @return The topics for this project, or an empty list if none.
     */
    public List<Topic> getTopics() { 
        List<Topic> t = new ArrayList<Topic>();
        if (topics != null) {
            for (int i = 0; i < topics.length(); i++ ) {
                try {
                    t.add(new Topic(topics.getString(i)));
                } catch (JSONException e) {
                    // Whut whut???
                }
            }
        }
        return t;
    }
    
    /**
     * Gets the topics for this project, if any, as HTML code.
     * 
     * @param filterUrl The current URL, which (perhaps) includes filtering parameter(s).
     * @param separator The character to use for separate the topics. Use null for none.
     * @param locale The preferred language.
     * @return The topics for this project, or an empty string if none.
     */
    public String getTopicsHtml(String filterUrl, String separator, Locale locale) {
        String s = "";
        Iterator<Topic> i = getTopics().iterator();
        while (i.hasNext()) {
            Topic t = i.next();
            s += t.toHtml(filterUrl, locale);
            if (i.hasNext() && separator != null)
                s += separator + " ";
        }
        return s;
    }

    /**
     * @return the leaders.
     */
    public List<ProjectParticipant> getLeaders() {
        return leaders;
    }

    /**
     * @return the participants.
     */
    public List<ProjectParticipant> getParticipants() {
        return participants;
    }

    /**
     * @return the programmes.
     */
    public List<OptLink> getProgrammes() {
        return programmes;
    }

    /**
     * @return the placenames.
     */
    public List<OptLink> getPlacenames() {
        return placenames;
    }

    /**
     * @return the partners.
     */
    public List<OptLink> getPartners() {
        return partners;
    }
    /*
    public static SimpleDateFormat getTimestampFormat(String timestamp) {
        for (TimestampPattern t : TimestampPattern.values()) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(t.toString());
                f.parse(timestamp);
                return f;
                //pattern = f.toPattern();
                //break;
            } catch (Exception e) {
                //System.out.println("Timestamp [" + ts + "] did not fit pattern [" + t.toString() + "]");
            }
        }
        // Return default
        return new SimpleDateFormat(TimestampPattern.TIME.toString());
    }*/
    
    /**
     * Gets a crude string representation of this project, consisting of the
     * title, abbreviation (if any) and abstract (if any).
     * 
     * @return a crude string representation of this project.
     */
    @Override
    public String toString() {
        return "<h3>" + getTitle() + (getTitleAbbrev().isEmpty() ? "" : "(" + getTitleAbbrev() + ")") + "</h3>" 
                + (getAbstr().isEmpty() ? "" : "<p>" + getAbstr() + "</p>");
    }
    /**
     * Gets the URL for this project, within the context of the given service.
     * <p>
     * The service should be of type {@link ProjectService}.
     * 
     * @param service The API service. Should be of type {@link ProjectService}.
     * @return The URL for this project, or: where it resides within the given service.
     */
    @Override
    public String getURL(APIServiceInterface service) {
        if (!(service instanceof ProjectService)) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Retrieving project URL using a service not of type " + ProjectService.class.getName() + " may produce unexpected results.");
            }
        }
        return service.getServiceBaseURL() + this.getId();
    }
    
    
    
    
    // JSON keys 
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_ID            = Key.ID;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_WORKSPACE     = Key.WORKSPACE;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_WEBSITE_FLAG  = Key.WEBSITE_FLAG;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_TITLE         = Key.TITLE;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_ABBREV_TITLE  = Key.ABBREV_TITLE;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_DESCRIPTION   = Key.DESCRIPTION;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_ABSTRACT      = Key.ABSTRACT;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_NPI_ID        = Key.NPI_ID;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_RIS_ID        = Key.RIS_ID;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_KEYWORDS      = Key.KEYWORDS;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_LOGO          = Key.LOGO;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_FEATURED_IMAGE= Key.FEATURED_IMAGE;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_BEGIN         = Key.BEGIN;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_END           = Key.END;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_TYPE          = Key.TYPE;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_GEO_AREA      = Key.GEO_AREA;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PLACENAMES    = Key.PLACENAMES;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PLACENAME     = Key.PLACENAME;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_AREA          = Key.AREA;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_WEBSITE       = Key.WEBSITE;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PEOPLE        = Key.PEOPLE;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_LEADERS       = Key.LEADERS;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PERSON_FNAME  = Key.PERSON_FNAME;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PERSON_LNAME  = Key.PERSON_LNAME;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PERSON_URI    = Key.PERSON_URI;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PERSON_EMAIL  = Key.PERSON_EMAIL;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PERSON_AFFIL  = Key.PERSON_AFFIL;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_ROLE          = Key.ROLE;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_ORG           = Key.ORG;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_NAME          = Key.NAME;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_ORGANISATION  = Key.ORGANISATION;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_ORGANISATIONS = Key.ORGANISATIONS;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PERSON_INST   = Key.PERSON_INST;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PARTICIPANTS  = Key.PARTICIPANTS;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_PARTNER       = Key.PARTNER;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_TOPICS        = Key.TOPICS;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_AFFIL_ICE     = Key.AFFIL_ICE;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_RES_PROG      = Key.RES_PROG;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_RES_PROG_TITLE= Key.RES_PROG_TITLE;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_RES_PROG_URI  = Key.RES_PROG_URI;
    /** @deprecated Use {@link Key} instead. */
    public static final String JSON_KEY_TRANSLATIONS  = Key.TRANSLATIONS;
    
    // JSON pre-defined values
    /** @deprecated Use {@link Val} instead. */
    public static final String JSON_VAL_PROJECT_LEADER = Val.PROJECT_LEADER;
    /** @deprecated Use {@link Val} instead. */
    public static final String JSON_VAL_PROJECT_PARTICIPANT = Val.PROJECT_PARTICIPANT;
    
    /** @deprecated Use {@link Val} instead. */
    public static final String JSON_VAL_STATE_PLANNED = Val.STATE_PLANNED;
    /** @deprecated Use {@link Val} instead. */
    public static final String JSON_VAL_STATE_ONGOING = Val.STATE_ONGOING;
    /** @deprecated Use {@link Val} instead. */
    public static final String JSON_VAL_STATE_COMPLETED = Val.STATE_COMPLETED;
    /** @deprecated Use {@link Val} instead. */
    public static final String JSON_VAL_STATE_CANCELLED = Val.STATE_CANCELLED;
    
    public static final String JSON_VAL_TYPE_RESEARCH = Val.TYPE_RESEARCH;
    /** @deprecated Use {@link Val} instead. */
    public static final String JSON_VAL_TYPE_MONITORING = Val.TYPE_MONITORING;
    /** @deprecated Use {@link Val} instead. */
    public static final String JSON_VAL_TYPE_MODELLING = Val.TYPE_MODELLING;
    /** @deprecated Use {@link Val} instead. */
    public static final String JSON_VAL_TYPE_MAPPING = Val.TYPE_MAPPING;
    /** @deprecated Use {@link Val} instead. */
    public static final String JSON_VAL_TYPE_EDUCATION = Val.TYPE_EDUCATION;
}
