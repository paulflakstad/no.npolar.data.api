package no.npolar.data.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
//import no.npolar.data.api.util.APIUtil;
import no.npolar.data.api.util.APIUtil;
import no.npolar.data.api.util.Mapper;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.util.CmsHtmlExtractor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a single publication entry, as read from the Norwegian Polar 
 * Institute Data Centre.
 * <p>
 * Publications are typically created by a {@link PublicationService} instance 
 * acting as an interface to the Data Centre.
 * 
 * @see https://github.com/npolar/api.npolar.no/blob/master/schema/publication.json
 * @see https://data.npolar.no/publications/
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Publication implements APIEntryInterface {

    /** The logger. */
    private static final Log LOG = LogFactory.getLog(Publication.class);
    /** The JSON object that this instance is built from. */
    private JSONObject o = null;
    
    /** JSON key: Publication title. */
    public static final String JSON_KEY_TITLE           = "title";
    /** JSON key: Links. */
    public static final String JSON_KEY_LINKS           = "links";
    /** JSON key: Links -> rel. */
    public static final String JSON_KEY_LINK_REL        = "rel";
    /** JSON key: Links -> href. */
    public static final String JSON_KEY_LINK_HREF       = "href";
    /** JSON key: Links -> href language. */
    public static final String JSON_KEY_LINK_HREFLANG   = "hreflang";
    /** JSON key: Type. */
    public static final String JSON_KEY_LINK_TYPE       = "type";
    /** JSON key: Published timestamp. */
    public static final String JSON_KEY_PUB_TIME        = "published_sort";
    /** JSON key: Published timestamp accuracy. */
    public static final String JSON_KEY_PUB_ACCURACY    = "published_helper";
    /** JSON key: Publish year. */
    public static final String JSON_KEY_PUBYEAR         = "published-year";
    /** JSON key: Publish date. */
    public static final String JSON_KEY_PUBDATE         = "published-date";
    /** JSON key: ID. */
    public static final String JSON_KEY_ID              = "id";
    /** JSON key: Publication type. */
    public static final String JSON_KEY_TYPE            = "publication_type";
    /** JSON key: Publication language. */
    public static final String JSON_KEY_LANGUAGE        = "publication_lang";
    /** JSON key: Publication state. */
    public static final String JSON_KEY_STATE           = "state";
    /** JSON key: Comment. */
    public static final String JSON_KEY_COMMENT         = "comment";
    /** JSON key: Volume. */
    public static final String JSON_KEY_VOLUME          = "volume";
    /** JSON key: Issue. */
    public static final String JSON_KEY_ISSUE           = "issue";
    /** JSON key: Journal. */
    public static final String JSON_KEY_JOURNAL         = "journal";
    /** JSON key: Topics. */
    public static final String JSON_KEY_TOPICS          = "topics";
    /** JSON key: Name (generic: journal name, person name etc.). */
    public static final String JSON_KEY_NAME            = "name";
    /** JSON key: NPI series. */
    public static final String JSON_KEY_NPI_SERIES      = "np_series";
    /** JSON key: Series. */
    public static final String JSON_KEY_SERIES          = "series";
    /** JSON key: Series no. */
    public static final String JSON_KEY_SERIES_NO       = "series_no";
    /** JSON key: Pages. */
    public static final String JSON_KEY_PAGES           = "pages";
    /** JSON key: Page count. */
    public static final String JSON_KEY_PAGE_COUNT      = "page_count";
    /** JSON key: People. */
    public static final String JSON_KEY_PEOPLE          = "people";
    /** JSON key: First name. */
    public static final String JSON_KEY_FNAME           = "first_name";
    /** JSON key: Last name. */
    public static final String JSON_KEY_LNAME           = "last_name";
    /** JSON key: Roles. */
    public static final String JSON_KEY_ROLES           = "roles";
    /** JSON key: Organization. */
    public static final String JSON_KEY_ORG             = "organisation";
    /** JSON key: Organizations. */
    public static final String JSON_KEY_ORGS            = "organisations";
    /** JSON key: Location. */
    public static final String JSON_KEY_LOCATION        = "location";
    /** JSON key: Conference. */
    public static final String JSON_KEY_CONF            = "conference";
    /** JSON key: Conference name. */
    public static final String JSON_KEY_CONF_NAME       = "name";
    /** JSON key: Conference place. */
    public static final String JSON_KEY_CONF_PLACE      = "place";
    /** JSON key: Conference country. */
    public static final String JSON_KEY_CONF_COUNTRY    = "country";
    /** JSON key: Conference dates. */
    public static final String JSON_KEY_CONF_DATES      = "dates";
    //public static final String JSON_KEY_

    /** Pre-defined JSON value: state "submitted". */
    public static final String JSON_VAL_STATE_SUBMITTED = "submitted";
    /** Pre-defined JSON value: state "accepted". */
    public static final String JSON_VAL_STATE_ACCEPTED  = "accepted";
    /** Pre-defined JSON value: state "published". */
    public static final String JSON_VAL_STATE_PUBLISHED = "published";
    /** Pre-defined JSON value: "related". */
    public static final String JSON_VAL_LINK_RELATED    = "related";
    /** Pre-defined JSON value: link rel "DOI". */
    public static final String JSON_VAL_LINK_DOI        = "doi";
    /** Pre-defined JSON value: link rel "XREF_DOI". */
    public static final String JSON_VAL_LINK_XREF_DOI   = "xref_doi";
    /** Pre-defined JSON value: link rel "parent". */
    public static final String JSON_VAL_LINK_PARENT     = "parent";
    /** Pre-defined JSON value: role "author". */
    public static final String JSON_VAL_ROLE_AUTHOR     = "author";
    /** Pre-defined JSON value: role "co-author". */
    public static final String JSON_VAL_ROLE_COAUTHOR   = "co-author";
    /** Pre-defined JSON value: role "editor". */
    public static final String JSON_VAL_ROLE_EDITOR     = "editor";
    /** Pre-defined JSON value: role "translator". */
    public static final String JSON_VAL_ROLE_TRANSLATOR = "translator";
    /** Pre-defined JSON value: role "publisher". */
    public static final String JSON_VAL_ROLE_PUBLISHER  = "publisher";
    /** Pre-defined JSON value: NPI organizational id. */
    public static final String JSON_VAL_ORG_NPI         = "npolar.no";
    
    /** The date format used in the JSON. */
    public static final String DATE_FORMAT_JSON         = "yyyy-MM-dd";
    /** The base URL for publication links. */
    public static final String URL_PUBLINK_BASE         = "http://data.npolar.no/publication/";
    /** The base URL for DOI links. */
    public static final String URL_DOI_BASE             = "http://dx.doi.org/";
    /** Non-breaking space HTML. */
    public static final String NBSP                     = "&nbsp;";
    /** The default title, used if title is missing. */
    //public static final String DEFAULT_TITLE            = "Unknown title";
    
    //public static final String DEFAULT_PROCEEDINGS_JOURNAL = "Book of abstracts";
    
    
    
    /** The pre-defined keyword for identifying peer-reviewed publications. */
    public static final String TYPE_PEER_REVIEWED = "peer-reviewed";
    /** The pre-defined keyword for identifying editorials. */
    public static final String TYPE_EDITORIAL = "editorial";
    /** The pre-defined keyword for identifying reviews. */
    public static final String TYPE_REVIEW = "review";
    /** The pre-defined keyword for identifying corrections. */
    public static final String TYPE_CORRECTION = "correction";
    /** The pre-defined keyword for identifying books. */
    public static final String TYPE_BOOK = "book";
    /** The pre-defined keyword for identifying book chapters. */
    //public static final String TYPE_BOOK_CHAPTER = "book-chapter";
    /** The pre-defined keyword for identifying maps. */
    public static final String TYPE_MAP = "map";
    /** The pre-defined keyword for identifying posters. */
    public static final String TYPE_POSTER = "poster";
    /** The pre-defined keyword for identifying reports. */
    public static final String TYPE_REPORT = "report";
    /** The pre-defined keyword for identifying report series contributions. */
    //public static final String TYPE_REPORT_SERIES_CONTRIBUTION = "report-series-contrib";
    /** The pre-defined keyword for identifying abstracts. */
    public static final String TYPE_ABSTRACT = "abstract";
    /** The pre-defined keyword for identifying PhD theses. */
    public static final String TYPE_PHD = "phd";
    /** The pre-defined keyword for identifying Master theses. */
    public static final String TYPE_MASTER = "master";
    /** The pre-defined keyword for identifying proceedings. */
    public static final String TYPE_PROCEEDINGS = "proceedings";
    /** The pre-defined keyword for identifying popular science publications. */
    public static final String TYPE_POPULAR = "popular";
    /** The pre-defined keyword for identifying other publications. */
    public static final String TYPE_OTHER = "other";
    
    /** Identifier constant for reference string partial: author(s). */
    public static final int CITE_PART_AUTHORS = 0;
    /** Identifier constant for reference string partial: editor(s). */
    public static final int CITE_PART_EDITORS = 1;
    /** Identifier constant for reference string partial: translator(s). */
    public static final int CITE_PART_TRANSLATORS = 2;
    
    // Class members
    protected String title = "";
    protected String authors = "";
    protected String publisher = "";
    protected String publisherLocation = "";
    protected Date publishTime = null;
    protected SimpleDateFormat publishTimeFormat = null;
    protected String pubYear = "";
    protected Date pubDate = null;
    protected String parentUrl = "";
    protected String parentId = "";
    protected Publication parent = null;
    protected String journalName = "";
    protected String journalSeries = "";
    protected String journalSeriesNo = "";
    //protected String journal = "";
    protected String volume = "";
    protected String issue = "";
    protected String pageStart = "";
    protected String pageEnd = "";
    protected String pageCount = "";
    //protected String pages = "";
    protected String doi = "";
    protected String id = "";
    protected String link = "";
    protected String type = "";
    protected String language = "";
    protected String state = "";
    protected String comment = "";
    protected String confName = "";
    protected String confPlace = "";
    protected String confCountry = "";
    protected String confDates = "";
    protected Date confStart = null;
    protected Date confEnd = null;
    protected JSONObject conference = null;
    protected JSONArray links = null;
    protected JSONArray topics = null;
    /** The locale to use when generating strings meant for viewing. (Important especially for date formats etc.) */
    protected Locale displayLocale = null;
    /** Collection to hold authors and editors for this publication. */
    protected PersonCollection authorsAndEditors = null;
    /** List to hold translators of this publication. */
    protected List<PublicationContributor> translators = null;
    /** List to hold co-authors of this publication. */
    protected List<PublicationContributor> coAuthors = null;
    /** A mapper used for translating "code words" from the service into human-friendly content. */
    protected Mapper mappings = null;
    /** Localization resource bundle. */
    protected ResourceBundle labels = null;
    
    /** Comparator that can be used to order publications in a collection by publish date descending (newest first). */
    public static final Comparator<Publication> COMPARATOR_PUBLISHED_NEWEST_FIRST = 
            new Comparator<Publication>() {
                @Override
                public int compare(Publication o1, Publication o2) {
                    return o1.getPubYear().compareTo(o2.getPubYear());
                }
            };
    /** Comparator that can be used to order publications in a collection alphabetically (using the cite string). */
    public static final Comparator<Publication> COMPARATOR_CITESTRING =
            new Comparator<Publication>() {
                @Override
                public int compare(Publication o1, Publication o2) {
                    try {
                        return CmsHtmlExtractor.extractText(o1.toString(), "utf-8").compareTo(CmsHtmlExtractor.extractText(o2.toString(), "utf-8"));
                    } catch (Exception e) {
                        return o1.toString().compareTo(o2.toString());
                    }
                }
            };
    
    /**
     * Creates a new publication instance from the given JSON object.
     * 
     * @param pubObject The JSON object to use when constructing this instance.
     * @param loc The locale to use when generating strings for screen view. If null, the default locale (English) is used.
     */
    public Publication(JSONObject pubObject, Locale loc) {
        this.o = pubObject;
        this.displayLocale = loc;
        if (this.displayLocale == null)
            this.displayLocale = new Locale(APIService.DEFAULT_LOCALE_NAME);
        init();
    }
    
    /**
     * Builds this publication instance by interpreting the JSON source.
     */
    protected final void init() {
        // Initialize the bundle (for localized labels)
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        // Create the mapper
        try { 
            mappings = new Mapper();
            try { mappings.addAllPipeSeparated(labels.getString(Labels.DATA_COUNTRIES_0)); } catch (Exception e) { }
            try { mappings.addAllPipeSeparated(labels.getString(Labels.DATA_DB_VALUES_0)); } catch (Exception e) { }
            //try { mappings.addMapping("Temakart nr.", "Thematic map no."); } catch (Exception e) { }
        } catch (Exception e) { }
        
        
        ////////////////////////////////////////////////////////////////////////
        // All the non-complex stuff
        try { title     = o.getString(JSON_KEY_TITLE).trim(); } catch (Exception e) { title = labels.getString(Labels.LABEL_DEFAULT_TITLE_0); }
        //try { pubYear   = o.getString(JSON_KEY_PUBYEAR); if (pubYear.equalsIgnoreCase("0")) pubYear = ""; } catch (Exception e) { }
        try { pubDate   = new SimpleDateFormat(DATE_FORMAT_JSON).parse(o.getString(JSON_KEY_PUBDATE)); } catch (Exception e) { }
        try { id        = o.getString(JSON_KEY_ID); } catch (Exception e) { }
        try { type      = o.getString(JSON_KEY_TYPE); } catch (Exception e) { }
        try { language  = o.getString(JSON_KEY_LANGUAGE); } catch (Exception e) { }
        try { state     = o.getString(JSON_KEY_STATE); } catch (Exception e) { } 
        try { volume    = o.getString(JSON_KEY_VOLUME); } catch (Exception e) { }
        try { issue     = o.getString(JSON_KEY_ISSUE); } catch (Exception e) { }
        try { pageCount = o.getString(JSON_KEY_PAGE_COUNT); } catch (Exception e) { }
        try { comment   = o.getString(JSON_KEY_COMMENT); } catch (Exception e) { }
        try { links     = o.getJSONArray(JSON_KEY_LINKS); } catch (Exception e) { }
        try { topics    = o.getJSONArray(JSON_KEY_TOPICS); } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Publish time (year OR month of year OR full date)
        try { 
            String publishTimeFormatStr = o.getString(JSON_KEY_PUB_ACCURACY);
            if (publishTimeFormatStr.contains("d")) {
                publishTimeFormat = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_DATE_0), displayLocale);
            } else if (publishTimeFormatStr.contains("m")) {
                publishTimeFormat = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_MONTH_0), displayLocale);
            } else {
                publishTimeFormat = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_YEAR_0), displayLocale);
            }
        } catch (Exception e) { 
            publishTimeFormat = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_YEAR_0), displayLocale);
        }
        try { publishTime = new SimpleDateFormat(DATE_FORMAT_JSON).parse(o.getString(JSON_KEY_PUB_TIME)); } catch (Exception e) { }
        try { pubYear = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_YEAR_0), displayLocale).format(publishTime); } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Publisher
        if (o.has(JSON_KEY_ORGS)) {
            try {
                JSONArray orgs = o.getJSONArray(JSON_KEY_ORGS);
                for (int i = 0; i < orgs.length(); i++) { // For each organisation in organisations
                    JSONObject org = orgs.getJSONObject(i);
                    if (org.has(JSON_KEY_ROLES)) {
                        JSONArray roles = org.getJSONArray(JSON_KEY_ROLES);
                        for (int j = 0; j < roles.length(); j++) {
                            String role = roles.getString(j);
                            if (role.equals(JSON_VAL_ROLE_PUBLISHER)) { // This is a publisher
                                try {
                                    /*
                                    publisher += (publisher.isEmpty() ? "" : ", ") + org.getString(JSON_KEY_NAME).trim(); // Add the publisher name, if any
                                    publisherLocation = org.getString(JSON_KEY_LOCATION).trim(); // Get the publisher location, if any
                                    publisher += (publisher.isEmpty() ? "" : ", ") + publisherLocation; // Add the publisher location
                                    */
                                    publisher += (publisher.isEmpty() ? "" : ", ");
                                    try {
                                        publisherLocation = org.getString(JSON_KEY_LOCATION).trim(); // Get the publisher location, if any
                                        if (!publisherLocation.isEmpty())
                                            publisher += mappings.getMapping(publisherLocation); // Add the publisher location
                                    } catch (Exception pe) {
                                        publisherLocation = "";
                                    }
                                    
                                    try {
                                        String publisherName = org.getString(JSON_KEY_NAME).trim(); // Add the publisher name, if any
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
        
        ////////////////////////////////////////////////////////////////////////
        // People
        if (o.has(JSON_KEY_PEOPLE)) {
            try {
                JSONArray persons = o.getJSONArray(JSON_KEY_PEOPLE);
                authorsAndEditors = new PersonCollection(persons, displayLocale);
                // The list above may now contain translators and/or co-authors. 
                // If so, split those out into separate lists:
                translators = authorsAndEditors.getByRoleOnly(JSON_VAL_ROLE_TRANSLATOR);
                coAuthors = authorsAndEditors.getByRoleOnly(JSON_VAL_ROLE_COAUTHOR);
                authorsAndEditors.removeAll(translators);
                authorsAndEditors.removeAll(coAuthors);
            } catch (Exception e) { }
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Pages
        JSONArray pagesArr = null;
        try {
            pagesArr = o.getJSONArray(JSON_KEY_PAGES);
            if (pagesArr.length() == 2) {
                pageStart = pagesArr.getString(0).trim();
                pageEnd = pagesArr.getString(1).trim();
            }
        } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Journal
        JSONObject journalObj = null;
        try {
            journalObj = o.getJSONObject(JSON_KEY_JOURNAL);
            try { journalName = journalObj.getString(JSON_KEY_NAME).trim(); } catch (Exception e) { }
            if (journalObj.has(JSON_KEY_NPI_SERIES) || journalObj.has(JSON_KEY_SERIES)) {
                try { 
                    journalSeries = journalObj.getString(JSON_KEY_SERIES).trim(); // If there is a "normal" series, use that.
                } catch (Exception e) {
                    try {
                        journalSeries = journalObj.getString(JSON_KEY_NPI_SERIES).trim(); // If not, use the NPI series.
                    } catch (Exception ee) {
                        
                    }
                }
                if (journalObj.has(JSON_KEY_SERIES_NO)) {
                    journalSeriesNo = journalObj.getString(JSON_KEY_SERIES_NO).trim();
                }
            }
            /*if (journalObj.has(JSON_KEY_NPI_SERIES)) {
                journalSeries = journalObj.getString(JSON_KEY_NPI_SERIES).trim();
                if (journalObj.has(JSON_KEY_SERIES_NO)) {
                    journalSeriesNo = journalObj.getString(JSON_KEY_SERIES_NO).trim();
                }
            }*/
        } catch (Exception e) { 
            
        }
        
        ////////////////////////////////////////////////////////////////////////
        // DOI
        try {
            for (int i = 0; i < links.length(); i++) {
                JSONObject linkObj = links.getJSONObject(i);
                try {
                    if (linkObj.getString(JSON_KEY_LINK_REL).equalsIgnoreCase(JSON_VAL_LINK_DOI)) {
                        doi = extractDoi(linkObj.getString(JSON_KEY_LINK_HREF));//doi = linkObj.getString(JSON_KEY_LINK_HREF).replace(URL_DOI_BASE, "");
                        //break;
                    }
                    else if (linkObj.getString(JSON_KEY_LINK_REL).equalsIgnoreCase(JSON_VAL_LINK_XREF_DOI)) {
                        if (doi == null || doi.isEmpty())
                            doi = extractDoi(linkObj.getString(JSON_KEY_LINK_HREF));//doi = linkObj.getString(JSON_KEY_LINK_HREF).replace(URL_DOI_BASE, "");
                    }                      
                } catch (Exception doie) { }
            }
        } catch (Exception e) { }
        
        
        ////////////////////////////////////////////////////////////////////////
        // Parent publication
        try {
            if (links != null) {
                for (int i = 0; i < links.length(); i++) {
                    JSONObject linkObj = links.getJSONObject(i);
                    try {
                        if (JSON_VAL_LINK_PARENT.equalsIgnoreCase(linkObj.getString(JSON_KEY_LINK_REL))) { // if (this link's "rel" says "parent")
                            parentUrl = linkObj.getString(JSON_KEY_LINK_HREF);
                            parentId = parentUrl.substring(parentUrl.lastIndexOf("/")+1);
                            
                            if (parentId.contains("%")) {
                                parentId = parentId.substring(0, parentId.indexOf("%"));
                            }

                            //System.out.println(this.getTitle() + " - found parent link: " + parentUrl + " - ID: " + parentId);

                            parent = new PublicationService(displayLocale).getPublication(parentId);
                            
                            //System.out.println(this.getTitle() + " - added parent: " + parent.getTitle() + " - " + parent.getId());
                            break;
                        }                   
                    } catch (Exception parente) { 
                        //throw new NullPointerException("Error reading parent publication: " + parente.getMessage()); 
                    }
                }
            }
        } catch (Exception e) {
            //throw new NullPointerException("Error reading parent publication for id: " + this.getId() + ": " + e.getMessage()); 
        }
        
        
        ////////////////////////////////////////////////////////////////////////
        // Conference
        String s = "";
        try { 
            conference = o.getJSONObject(JSON_KEY_CONF);
            try { confName = conference.getString(JSON_KEY_CONF_NAME).trim(); } catch (Exception e) { }
            try { confPlace = conference.getString(JSON_KEY_CONF_PLACE).trim(); } catch (Exception e) { }
            //try { confCountry = getMappedString(conference.getString(JSON_KEY_CONF_COUNTRY).trim()); } catch (Exception e) { }
            try { confCountry = mappings.getMapping(conference.getString(JSON_KEY_CONF_COUNTRY).trim()); } catch (Exception e) { }
            
            if (conference.has(JSON_KEY_CONF_DATES)) {
                try {
                    JSONArray dates = conference.getJSONArray(JSON_KEY_CONF_DATES);
                    if (dates != null) {
                        try {
                            SimpleDateFormat dfSource = new SimpleDateFormat(DATE_FORMAT_JSON);
                            //SimpleDateFormat dfScreen = new SimpleDateFormat(displayLocale.toString().equalsIgnoreCase("no") ? DATE_FORMAT_SCREEN_NO : DATE_FORMAT_SCREEN_EN, displayLocale);
                            SimpleDateFormat dfScreen = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_0), displayLocale);
                            confStart = dfSource.parse(dates.getString(0));
                            confDates = dfScreen.format(confStart);
                            confEnd = dfSource.parse(dates.getString(1));
                            if (confEnd.after(confStart)) {
                                // Check if dates are in the same month, and if so, use a shorter format
                                GregorianCalendar calStart = new GregorianCalendar();
                                calStart.setTime(confStart);
                                GregorianCalendar calEnd = new GregorianCalendar();
                                calEnd.setTime(confEnd);
                                if (calStart.get(Calendar.MONTH) == calEnd.get(Calendar.MONTH) && calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR)) {
                                    // Same month: Use shorter format
                                    confDates = String.valueOf(calStart.get(Calendar.DATE)) + "&ndash;" + dfScreen.format(confEnd);
                                } else {
                                    // Not same month: Use long format
                                    confDates += "&nbsp;&ndash;&nbsp;" + dfScreen.format(confEnd);
                                }
                            }
                        } catch (Exception e) { }
                    }
                } catch (Exception e) { }
            }
        } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Journal modification needed?
        if (getJournal().trim().isEmpty()) { // No journal 
            if (!getConference().isEmpty() // conference info exists
                    && getType().equals(TYPE_PROCEEDINGS) // AND type is "proceedings"
                    && !getPages().isEmpty()) { // AND start/end pages exist
                // Assume the "journal" is the book of abstracts
                journalName = labels.getString(Labels.LABEL_DEFAULT_PROCEEDINGS_JOURNAL_0);// DEFAULT_PROCEEDINGS_JOURNAL;
            }
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Type modification needed?
        /*
        if (type.equalsIgnoreCase(TYPE_REPORT) || type.equalsIgnoreCase(TYPE_BOOK)) {  // Do this only if the type is currently "report" or "book"
            if (!getAuthors().isEmpty()) { // Require that there are authors
                if (!getJournal().isEmpty()) { // Require a "journal" (that is, a book or a report series title)
                    if (!getPageStart().isEmpty() && !getPageEnd().isEmpty()) { // Require a start page and an end page
                        // Conclude that this is not a standalone report/book, but rather a contribution to a book or a report series
                        type = type.equalsIgnoreCase(TYPE_REPORT) ? TYPE_REPORT_SERIES_CONTRIBUTION : TYPE_BOOK_CHAPTER;
                    }
                }
            }
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
     * Gets the title for this publication.
     * 
     * @return The title for this publication.
     */
    @Override
    public String getTitle() { return title; }
    
    /**
     * Gets the URL for this publication, within the context of the given 
     * service.
     * <p>
     * The service should be of type {@link PublicationService}.
     * 
     * @param service The API service. Should be of type {@link PublicationService}.
     * @return The URL for this publication, or: where it resides within the given service.
     */
    @Override
    public String getURL(APIServiceInterface service) {
        if (!(service instanceof PublicationService)) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Retrieving project URL using a service not of type " + PublicationService.class.getName() + " may produce unexpected results.");
            }
        }
        return service.getServiceBaseURL() + this.getId();
    }
    
    /**
     * Gets the publisher for this publication.
     * 
     * @return The publisher for this publication, or an empty string if none.
     */
    public String getPublisher() { return publisher; }
    
    /**
     * Gets the publish year for this publication.
     * 
     * @return The publish year for this publication, or an empty string if none.
     */
    public String getPubYear() { return pubYear; }
    
    /**
     * Gets the publish timestamp for this publication.
     * <p>
     * The formatting is based on the accuracy level of the publish time. See 
     * the {@link #init()} method.
     * 
     * @return The publish timestamp for this publication, formatted based on the accuracy level of the publish time.
     */
    public String getPubTime() { return publishTimeFormat.format(publishTime); }
    
    /**
     * Gets the publish date for this publication.
     * 
     * @return The publish date for this publication, or an empty string if none.
     */
    public String getPubDate() { try { return new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_0), displayLocale).format(pubDate); } catch (Exception e) { return ""; } }
    
    /**
     * Gets the year of the the publish date for this publication.
     * 
     * @return The year of the publish date for this publication, or an empty string if none.
     */
    public String getPubDateAsYear() { try { return new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_YEARONLY_0), displayLocale).format(pubDate); } catch (Exception e) { return ""; } }
    
    /**
     * Gets the unique ID for this publication.
     * 
     * @return The unique ID for this publication, or an empty string if none.
     */
    @Override
    public String getId() { return id; }
    
    /**
     * Gets the type for this publication.
     * 
     * @return The type for this publication, or an empty string if none.
     */
    public String getType() { return type; }
    
    /**
     * Gets the state for this publication.
     * 
     * @return The state for this publication, or an empty string if none.
     */
    public String getState() { return state; }
    
    /**
     * Gets the group name for this publication.
     * <p>
     * For publications, this is the type, so this method is identical to 
     * {@link #getType()}.
     * 
     * @return The group name (the type), or an empty string if none.
     * @see #getType() 
     */
    @Override
    public String getGroupName() { return getType(); }
    
    /**
     * Gets the volume for this publication, if any.
     * 
     * @return The volume for this publication, or an empty string if none.
     */
    public String getVolume() { return volume; }
    
    /**
     * Gets the issue for this publication, if any.
     * 
     * @return The issue for this publication, or an empty string if none.
     */
    public String getIssue() { return issue; }
    
    /**
     * Gets the links for this publication, if any.
     * 
     * @return The links for this publication, or null if none.
     */
    public JSONArray getLinks() { return links; }
    
    /**
     * Gets the topics for this publication, if any.
     * 
     * @return The topics for this publication, or an empty list if none;
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
     * Gets the topics for this publication, if any, as HTML code.
     * 
     * @param separator The character to use for separate the topics. Use null for none.
     * @return The topics for this publication, or an empty string if none;
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
     * Gets the default link for this publication.
     * @return The default link for this publication, or an empty string if none.
     */
    public String getPubLink(String baseUrl) {
        return getId().isEmpty() ? "" : baseUrl + getId();
    }
    
    /**
     * Gets the journal name for this publication.
     * 
     * @return The journal name for this publication, or an empty string if none.
     */
    public String getJournalName() { return journalName; }
    
    /**
     * Gets the series for this publication.
     * 
     * @return The series for this publication, or an empty string if none.
     */
    public String getJournalSeries() { return journalSeries; }
    
    /**
     * Gets the series no for this publication.
     * 
     * @return The series no for this publication, or an empty string if none.
     */
    public String getJournalSeriesNo() { return journalSeriesNo; }
    
    /**
     * Gets the complete journal string for this publication.
     * 
     * @return The complete journal string for this publication, or an empty string if none.
     */
    public String getJournal() {
        String s = "";
        try {
            s = journalName;
            if (isInSeries()) {
                s += ". " + journalSeries;
                if (hasSeriesNo()) {
                    s += " " + journalSeriesNo;
                }
            }
        } catch (Exception e) { }
        return s;
    }
    
    /**
     * Gets a flag indicating whether or not this publication is of the given type. 
     * <p>
     * The type match is not case sensitive.
     * 
     * @return True if this publication is of the given type, false if not.
     */
    public boolean isType(String type) { return !this.type.isEmpty() && this.type.equalsIgnoreCase(type); }
    
    /**
     * Gets a flag indicating whether or not this publication's state matches the 
     * given state.
     * <p>
     * The match is not case sensitive.
     * 
     * @return True if this publication's state matches the given state, false if not.
     */
    public boolean isState(String state) { return !this.state.isEmpty() && this.state.equalsIgnoreCase(state); }
    
    /**
     * Gets a flag indicating whether or not this publication starts and ends on
     * the same page, thereby making it a one-pager.
     * 
     * @return True if this publication is a one-pager, false if not.
     */
    public boolean isOnePage() { try { return this.pageStart.equals(this.pageEnd); } catch (Exception e) { return false; } }
    
    /**
     * Gets a flag indicating whether or not this publication is related to a conference.
     * 
     * @return True if this publication is related to a conference, false if not.
     */
    public boolean isConferenceRelated() { return !confName.isEmpty(); }
    
    /**
     * Gets a flag indicating whether or not this publication is part of a series.
     * 
     * @return True if this publication is part of a series, false if not.
     */
    public boolean isInSeries() { return !journalSeries.isEmpty(); }
    
    /**
     * Gets a flag indicating whether or not this publication is a 
     * part-contribution to another publication.
     * <p>
     * In practice, if this method method returns "true", that means this 
     * publication is a report in a report series or a chapter in a book.
     * 
     * @return True if this publication is a part-contribution to another publication, false if not.
     */
    public boolean isPartContribution() {
        if (hasParent())
            return true;
        //return type.equals(TYPE_BOOK_CHAPTER) || type.equals(TYPE_REPORT_SERIES_CONTRIBUTION);
        if (type.equalsIgnoreCase(TYPE_REPORT) || type.equalsIgnoreCase(TYPE_BOOK)) {  // Do this only if the type is currently "report" or "book"
            if (!getAuthors().isEmpty()) { // Require that there are authors
                if (!getJournal().isEmpty()) { // Require a "journal" (that is, a book or a report series title)
                    if (!getPageStart().isEmpty() && !getPageEnd().isEmpty()) { // Require a start page and an end page
                        // Conclude that this is not a standalone report/book, but rather a contribution to a book or a report series
                        return true;
                    }
                }
            }
        }
        return false;
    } 
    
    /**
     * Gets a flag indicating whether or not this publication has a series number.
     * 
     * @return True if this publication has a series number, false if not.
     */
    public boolean hasSeriesNo() { return !journalSeriesNo.isEmpty(); }
    
    /**
     * Gets a flag indicating whether or not this publication has editors only.
     * 
     * @return True if this publication has editors only, false if not.
     */
    public boolean hasEditorsOnly() { return authorsAndEditors == null ? false : authorsAndEditors.containsEditorsOnly(); }
    
    /**
     * Gets a flag indicating whether or not this publication has a parent publication.
     * 
     * @return True if this publication has a parent publication, false if not.
     */
    public boolean hasParent() { return parent != null; }

    /**
     * Gets the start page number for this publication.
     * 
     * @return The start page number for this publication, or an empty string if none.
     */
    public String getPageStart() { return pageStart; }
    
    /**
     * Gets the end page number for this publication.
     * 
     * @return The end page number for this publication, or an empty string if none.
     */
    public String getPageEnd() { return pageEnd; }
    
    /**
     * Gets the page number(s) string for this publication
     * <p>
     * E.g.: "18-21" or "18".
     * 
     * @return The page number(s) string for this publication, or an empty string if none.
     */
    public String getPages() {
        String s = "";
        if (!pageStart.isEmpty()) {
            s += pageStart + (!pageEnd.isEmpty() && !pageStart.equals(pageEnd) ? "&ndash;".concat(pageEnd) : "");
        }
        return s;
    }
    
    /**
     * Gets the complete page(s) string, with label.
     * <p>
     * E.g.: "Pp. 18-21" or "P. 18".
     * 
     * @return The complete page(s) string, with label, or an empty string if none.
     */
    public String getPagesWithLabel() {
        String s = "";
        boolean singlePage = true;
        if (!pageStart.isEmpty()) {
            s += pageStart;
            if (!pageEnd.isEmpty() && !pageStart.equals(pageEnd)) {
                s += "&ndash;".concat(pageEnd);
                singlePage = false;
            }
        }
        return labels.getString(singlePage ? Labels.PUB_REF_PAGE_0 : Labels.PUB_REF_PAGESPAN_0) + "&nbsp;" + s;
    }
    
    /**
     * Gets the page count (total number of pages) for this publication.
     * 
     * @return The page count (total number of pages) for this publication, or an empty string if none.
     */
    public String getPageCount() {
        return pageCount;
    }
    
    /**
     * Gets the DOI for this publication.
     * 
     * @return The DOI for this publication, or an empty string if none.
     */
    public String getDOI() { return doi; }
    
    /**
     * Gets the URL for this publication's parent.
     * 
     * @return The URL for this publication's parent, or an empty string if none.
     */
    public String getParentUrl() { return parentUrl; }
    
    /**
     * Gets the ID for this publication's parent.
     * 
     * @return The ID for this publication's parent, or an empty string if none.
     */
    public String getParentId() { return parentId; }
    
    /**
     * Gets the complete authors string for this publication.
     * 
     * @return The complete authors string for this publication, or an empty string if none.
     * @see #getPeopleStringByRole(java.lang.String) 
     */
    public String getAuthors() {
        return getPeopleStringByRole(JSON_VAL_ROLE_AUTHOR);
    }
    
    /**
     * Gets the complete editors string for this publication.
     * 
     * @return The complete editors string for this publication, or an empty string if none.
     * @see #getPeopleStringByRole(java.lang.String) 
     */
    public String getEditors() {
        return getPeopleStringByRole(JSON_VAL_ROLE_EDITOR);
    }
    
    /**
     * Gets the complete names (authors and editors) string for this publication.
     * 
     * @return The complete names (authors and editors) string for this publication, or an empty string if none.
     */
    public String getNames() {
        String s = "";
        boolean specifyEditors = !this.hasEditorsOnly();
        if (authorsAndEditors != null) {
            List<PublicationContributor> list = authorsAndEditors.get();
            Iterator<PublicationContributor> i = list.iterator();
            while (i.hasNext()) {
                s += i.next().toHtml(specifyEditors, false);
                if (i.hasNext())
                    s += ", ";
            }
        }
        return s;
    }
    
    /**
     * Gets the complete translator name(s) string for this publication.
     * 
     * @return The complete translator name(s) string for this publication, or an empty string if none.
     */
    public String getNamesOfTranslators() {
        /*String s = "";
        if (translators != null && !translators.isEmpty()) {
            Iterator<PublicationContributor> i = translators.iterator();
            while (i.hasNext()) {
                s += i.next().toHtml(false, false);
                if (i.hasNext())
                    s += ", ";
            }
        }
        return s;*/
        return getNamesInList(translators);
    }
    
    /**
     * Gets the complete co-author name(s) string for this publication.
     * 
     * @return The complete co-author name(s) string for this publication, or an empty string if none.
     */
    public String getNamesOfCoAuthors() {
        return getNamesInList(coAuthors);
    }
    
    /**
     * Gets the complete name(s) string for the contributors in the given list.
     * 
     * @param list The list containing the contributors.
     * @return The complete name(s) string for the given list.
     */
    protected String getNamesInList(List<PublicationContributor> list) {
        String s = "";
        if (list != null && !list.isEmpty()) {
            Iterator<PublicationContributor> i = list.iterator();
            while (i.hasNext()) {
                s += i.next().toHtml(false, false);
                if (i.hasNext())
                    s += ", ";
            }
        }
        return s;
    }
    
    /**
     * Gets the complete names string for all contributors to this publication that
     * are assigned the given role.
     * @param role The role to match against, for example {@link #JSON_VAL_ROLE_AUTHOR}.
     * @return The complete names string for all contributors to this publication that are assigned the given role, or an empty string if none.
     */
    public String getPeopleStringByRole(String role) {
        String s = "";
        if (authorsAndEditors != null) {
            List<PublicationContributor> list = authorsAndEditors.getByRole(role);
            Iterator<PublicationContributor> i = list.iterator();
            while (i.hasNext()) {
                s += i.next().toHtml(false, false);
                if (i.hasNext())
                    s += ", ";
            }
        }
        return s;
    }
    
    /**
     * Gets the complete list of contributors to this publication that are 
     * assigned the given role.
     * 
     * @param role The role to match against, for example {@link #JSON_VAL_ROLE_AUTHOR}.
     * @return The complete list of contributors to this publication that are assigned the given role, or an empty list if none.
     */
    public List<PublicationContributor> getPeopleByRole(String role) {
        List<PublicationContributor> list = new ArrayList<PublicationContributor>();
        if (authorsAndEditors != null) {
            list.addAll(authorsAndEditors.getByRole(role));
        }
        return list;
    }
    
    /**
     * Gets an identified part of the reference string - for example, the authors.
     * 
     * @param elementId The identification for which part to get. See this class' CITE_PART_XXXX constants.
     * @return The identified part of the reference string, or an empty string if that part does not exist for this publication.
     */
    public String getReferenceElement(int elementId) {
        String s = "";
        if (elementId == CITE_PART_EDITORS) {
            if (!getEditors().isEmpty())
                s += getEditors() + " (" + labels.getString(getPeopleByRole(JSON_VAL_ROLE_EDITOR).size() > 1 ? Labels.PUB_REF_EDITORS_0 : Labels.PUB_REF_EDITOR_0).toLowerCase() + ")";
        }
        else if (elementId == CITE_PART_AUTHORS) {
            if (!getAuthors().isEmpty()) 
                s += getAuthors();
        }
        else if (elementId == CITE_PART_TRANSLATORS) {
            if (!getNamesOfTranslators().isEmpty()) 
                s += getNamesOfTranslators();
        }
        return s;
    }
    
    /**
     * Gets the complete conference string for this publication.
     * 
     * @return The complete conference string for this publication, or an empty string if none.
     */
    public String getConference() {
        String s = "";
        if (this.isConferenceRelated()) {
            s += "" + confName + "";
            if (!confPlace.isEmpty())
                s += ", " + confPlace;
            if (!confCountry.isEmpty()) 
                s += ", " + confCountry;
            if (!confDates.isEmpty())
                s += ", " + confDates;
        }
        return s;
    }
    
    /**
     * Gets the complete list of people who have contributed to this publication.
     * 
     * @return The complete list of people who have contributed to this publication, or an empty list if none.
     */
    public List<PublicationContributor> getPeople() {
        return this.authorsAndEditors.get();
    }
    
    /**
     * Checks if the given string ends with a "stop character", that is, a 
     * character that ends a sentence (currently: "?", "." or "!").
     * <p>
     * Used to determine whether or not to add e.g. a punctuation mark at the 
     * end of a title or not.
     * 
     * @param s The string to check.
     * @return True if the given string ends with a "stop character", false if not.
     */
    protected boolean endsWithStopChar(String s) {
        if (s == null || s.length() < 1)
            return false;
        String str = s.trim();
        if (str.length() < 1)
            return false;
        List<String> stopChars = Arrays.asList(new String[] { "?", ".", "!" });
        String lastChar = str.substring(str.length() -1);
        if (stopChars.contains(lastChar))
            return true;
        return false;
    }
    
    /**
     * Gets the cite string / reference for this publication.
     * <p>
     * Equivalent to calling {@link #toString()}.
     * 
     * @return the cite string / reference for this publication.
     * @see #toString() 
     */
    public String cite() {
        return this.toString();
    }
    
    /**
     * Gets the string representation for this publication.
     * 
     * @return The string representation for this publication.
     */
    @Override
    public String toString() {
        String s = "";
        
        //if (!this.hasParent()) {
        if (!this.isPartContribution()) {
            String names = getNames();
            if (!names.isEmpty()) {
                s += names; 
                if (hasEditorsOnly()) {
                    s += " (" + labels.getString(authorsAndEditors.get().size() > 1 ? Labels.PUB_REF_EDITORS_0 : Labels.PUB_REF_EDITOR_0).toLowerCase() + ")" + ".";
                    //s += " (ed" + (authorsAndEditors.get().size() > 1 ? "s" : "") + ".)";
                }                
                //s += "."; // Add this only when using full names
            }
        } else {
            String authorsStr = getAuthors();
            if (!authorsStr.isEmpty()) {
                s += authorsStr;
                //s += "."; // Add this only when using full names
            }
        }
            
            
            
            
            if (!pubYear.isEmpty() || !getPubDate().isEmpty()) {
                if (isType(Publication.TYPE_POPULAR) || isType(Publication.TYPE_OTHER))
                    s += " " + (!getPubDate().isEmpty() ? getPubDate() : pubYear) + "."; // Date takes precedence over year
                else 
                    s += " " + pubYear + "."; // Ignore date, use year
            }
            
            if (!this.isState(JSON_VAL_STATE_PUBLISHED)) {
                s += " <em>(" + labels.getString(Labels.PUB_STATE_PREFIX_0.concat(getState())) + ")</em>";
            }
            
            s += " <a href=\"" + URL_PUBLINK_BASE + id + "\">" + title + "</a>" + (endsWithStopChar(title) ? "" : ".") + " ";
            
            // Special routine for "child" publications
            if (this.hasParent() || this.isPartContribution()) {
                
                if (!getPages().isEmpty()) {
                    s += APIUtil.capitalizeFirstLetter(getPagesWithLabel()) + " " + labels.getString(Labels.PUB_REF_IN_0).toLowerCase() + ": ";
                } else {
                    s += labels.getString(Labels.PUB_REF_IN_0) + ": ";
                }
                
                //s += "In: ";
                if (this.hasParent()) {
                    String parentNames = parent.getNames();
                    if (!parentNames.isEmpty()) {
                        s += parentNames;
                        if (parent.hasEditorsOnly()) {
                            s += " (" + labels.getString(parent.getPeople().size() > 1 ? Labels.PUB_REF_EDITORS_0 : Labels.PUB_REF_EDITOR_0).toLowerCase() + ")";
                            //s += " (ed" + (parent.getPeople().size() > 1 ? "s" : "") + ".)";
                        }
                        s += ": ";
                    }
                    //s += "<em>";
                    //s += "<a href=\"" + URL_PUBLINK_BASE + parentId + "\">" + parent.getTitle() + "</a>. ";
                    s += "<a href=\"" + URL_PUBLINK_BASE + parentId + "\">" + parent.getTitle() + ". "; // Start the link here, but don't end it before we're done with the entire parent string (series etc.)
                }
                else { // No parent registered, but still "part-contribution" (book/report chapter)
                    String editorsStr = getEditors();
                    // Parent publication editors
                    if (!editorsStr.isEmpty()) {
                        s += editorsStr + " (" + labels.getString(getPeopleByRole(Publication.JSON_VAL_ROLE_EDITOR).size() > 1 ? Labels.PUB_REF_EDITORS_0 : Labels.PUB_REF_EDITOR_0).toLowerCase() + "): ";
                    }
                }
                
                //if (!parent.getVolume().isEmpty() || !parent.getJournalSeries().isEmpty() || !parent.getPages().isEmpty())
            }
            
            // If a parent exists, override any journal existing on this publication with the parent's journal
            Publication journalObj = hasParent() ? parent : this;

            // Journal
            String journal = journalObj.getJournalName();
            
            if (!journal.isEmpty() 
                    || !journalObj.getVolume().isEmpty() 
                    || !journalObj.getJournalSeries().isEmpty()) {
                
                if (!journal.isEmpty()) { // Do this only for publications without parent.
                    s += journal;
                }
                
                // Volume / series
                if (!journalObj.getVolume().isEmpty() || !journalObj.getJournalSeries().isEmpty()) {
                    if (!journalObj.getJournalSeries().isEmpty()) {
                        s += (journal.isEmpty() ? "" : (journal.endsWith(".") ? "" : ".")) + " " + mappings.getMapping(journalObj.getJournalSeries());
                        if (!journalObj.getJournalSeriesNo().isEmpty())
                            s += "&nbsp;" + journalObj.getJournalSeriesNo();
                        else if (!journalObj.getVolume().isEmpty())
                            s += "&nbsp;" + journalObj.getVolume();
                    }
                    else if (!journalObj.getVolume().isEmpty()) {
                        s += "&nbsp;" + journalObj.getVolume();
                        if (!journalObj.getIssue().isEmpty()) {
                            s += "(" + journalObj.getIssue() + ")";
                        }
                    }
                    
                    s += ".";
                    
                    if (hasParent()) {
                        s += "</a>";
                    }
                } 
                else {
                    s += ".";
                    if (hasParent()) {
                        s += "</a>";
                    }
                }
                
                s += " ";
            }
            
            if (hasParent() && !s.endsWith("</a>"))
                s += "</a>";
            
            if (!hasParent() && (isType(Publication.TYPE_BOOK) || isType(Publication.TYPE_REPORT))) {
                if (s.endsWith(".."))
                    s = s.substring(0, s.length()-1);
                
                if (!getPublisher().isEmpty()) {
                    //s += (s.trim().endsWith(".") ? " " : ". ") + mappings.getMapping(getPublisher()) + ".";
                    s += mappings.getMapping(getPublisher()) + ". ";
                }
            }
            
            // Page / page span (where this publication appears in the journal/periodical/...)
            if (!getPages().isEmpty() && !(hasParent() || isPartContribution())) {
                // Remove trailing "." and possibly also whitespace
                s = s.trim();
                if (s.endsWith("."))
                    s = s.substring(0, s.length()-1);
                
                s += ":&nbsp;" + getPages() + ".";
            }
            
            //s += ".";

            if (s.endsWith(".."))
                s = s.substring(0, s.length()-1);

            if (s.endsWith(". ."))
                s = s.substring(0, s.length()-2);

            // Number of pages in publication
            if (getPages().isEmpty() && !getPageCount().isEmpty()) {
                //s += " " + getPageCount() + " pp.";
                s += " " + getPageCount() + "&nbsp;" + labels.getString(Labels.PUB_REF_PAGES_0);
            }

            if (s.endsWith(". ."))
                s = s.substring(0, s.length()-2);

            // Conference
            if (!getConference().isEmpty()) {
                s += " <span class=\"pub-event\">" + getConference() + "</span>.";
            }
            
            // Translation credit
            if (!translators.isEmpty()) {
                s+= " (";
                //if (language != null && !language.isEmpty()) {
                    try {
                        String publicationDisplayLanguage = APIUtil.getDisplayLanguage(new Locale(language), displayLocale);
                        //String publicationDisplayLanguage = new Locale(language).getDisplayLanguage(new Locale(displayLocale.getCountry()));
                        s += labels.getString(Labels.LABEL_TRANSLATED_TO_0) + " " + publicationDisplayLanguage + " " + labels.getString(Labels.LABEL_BY_0).toLowerCase();
                    } catch (Exception e) {
                        s += labels.getString(Labels.LABEL_TRANSLATED_BY_0);
                    }; 
                //}
                s += " " + getNamesOfTranslators() + ")"; // No trailing "." because the transator name(s) string will end with a "."
            }

            s += (doi.isEmpty() ? "" : "<br />DOI:<a href=\"" + URL_DOI_BASE + doi + "\">" + doi + "</a>");
        /*}
        // Publications that have a parent publication:
        else {
            String authorsStr = getAuthors();
            String editorsStr = getEditors();
            if (!authorsStr.isEmpty()) {
                s += authorsStr; 
                if (hasEditorsOnly()) {
                    s += " (ed" + (authorsAndEditors.get().size() > 1 ? "s" : "") + ".)";
                }                
                s += ".";
            }
            if (!pubYear.isEmpty())
                s += " " + pubYear + ".";
            s += " <a href=\"" + URL_PUBLINK_BASE + id + "\"><em>" + title + "</em></a>. ";

            s += "In: ";
            // Parent publication editors
            if (!editorsStr.isEmpty()) {
                s += editorsStr + " (eds.): ";
            }
            
            // IDENTICAL FROM HERE
            
            // Journal
            String journal = getJournalName();
            if (!journal.isEmpty()) {
                s += journal;
            }
            // Volume / series
            if (!volume.isEmpty() || !journalSeries.isEmpty() || !getPages().isEmpty()) {
                if (!journalSeries.isEmpty()) {
                    s += (journal.isEmpty() ? "" : ". ") + journalSeries;
                    if (!journalSeriesNo.isEmpty())
                        s += "&nbsp;" + journalSeriesNo;
                    else if (!volume.isEmpty())
                        s += "&nbsp;" + volume;
                }
                else if (!volume.isEmpty()) {
                    s += "&nbsp;" + volume;
                    if (!issue.isEmpty()) {
                        s += "(" + issue + ")";
                    }
                }
                // Pages
                if (!getPages().isEmpty()) {
                    s += ":&nbsp;" + getPages() + ".";
                }
            }

            // Conference
            if (!getConference().isEmpty()) {
                s += " " + getConference();
            }
            s += ".";

            if (s.endsWith(".."))
                s = s.substring(0, s.length()-1);

            if (s.endsWith(". ."))
                s = s.substring(0, s.length()-2);

            if (getPages().isEmpty() && !getPageCount().isEmpty()) {
                s += " " + getPageCount() + " pp.";
            }

            s += (doi.isEmpty() ? "" : "<br />DOI:<a href=\"" + URL_DOI_BASE + doi + "\">" + doi + "</a>");
        }*/
        return s;
    }
    
    
    /**
     * Gets the string representation for this publication.
     * @return The string representation for this publication.
     */
    /*@Override
    public String toString() {
        String s = "";
        
        if (!this.isPartContribution()) {
            String names = getNames();
            if (!names.isEmpty()) {
                s += names; 
                if (hasEditorsOnly()) {
                    s += " (ed" + (authorsAndEditors.get().size() > 1 ? "s" : "") + ".)";
                }                
                s += ".";
            }
            if (!pubYear.isEmpty())
                s += " " + pubYear + ".";
            s += " <a href=\"" + URL_PUBLINK_BASE + id + "\"><em>" + title + "</em></a>. ";

            // Journal
            String journal = getJournalName();
            if (!journal.isEmpty()) {
                s += journal;
            }
            // Volume / series
            if (!volume.isEmpty() || !journalSeries.isEmpty() || !getPages().isEmpty()) {
                if (!journalSeries.isEmpty()) {
                    s += (journal.isEmpty() ? "" : ". ") + journalSeries;
                    if (!journalSeriesNo.isEmpty())
                        s += "&nbsp;" + journalSeriesNo;
                    else if (!volume.isEmpty())
                        s += "&nbsp;" + volume;
                }
                else if (!volume.isEmpty()) {
                    s += "&nbsp;" + volume;
                    if (!issue.isEmpty()) {
                        s += "(" + issue + ")";
                    }
                }
                // Pages
                if (!getPages().isEmpty()) {
                    s += ":&nbsp;" + getPages() + ".";
                }
            }

            // Conference
            if (!getConference().isEmpty()) {
                s += " " + getConference();
            }
            s += ".";

            if (s.endsWith(".."))
                s = s.substring(0, s.length()-1);

            if (s.endsWith(". ."))
                s = s.substring(0, s.length()-2);

            if (getPages().isEmpty() && !getPageCount().isEmpty()) {
                s += " " + getPageCount() + " pp.";
            }

            s += (doi.isEmpty() ? "" : "<br />DOI:<a href=\"" + URL_DOI_BASE + doi + "\">" + doi + "</a>");
        }
        else {
            String authorsStr = getAuthors();
            String editorsStr = getEditors();
            if (!authorsStr.isEmpty()) {
                s += authorsStr; 
                if (hasEditorsOnly()) {
                    s += " (ed" + (authorsAndEditors.get().size() > 1 ? "s" : "") + ".)";
                }                
                s += ".";
            }
            if (!pubYear.isEmpty())
                s += " " + pubYear + ".";
            s += " <a href=\"" + URL_PUBLINK_BASE + id + "\"><em>" + title + "</em></a>. ";

            s += "In: ";
            // Parent publication editors
            if (!editorsStr.isEmpty()) {
                s += editorsStr + " (eds.): ";
            }
            
            // IDENTICAL FROM HERE
            
            // Journal
            String journal = getJournalName();
            if (!journal.isEmpty()) {
                s += journal;
            }
            // Volume / series
            if (!volume.isEmpty() || !journalSeries.isEmpty() || !getPages().isEmpty()) {
                if (!journalSeries.isEmpty()) {
                    s += (journal.isEmpty() ? "" : ". ") + journalSeries;
                    if (!journalSeriesNo.isEmpty())
                        s += "&nbsp;" + journalSeriesNo;
                    else if (!volume.isEmpty())
                        s += "&nbsp;" + volume;
                }
                else if (!volume.isEmpty()) {
                    s += "&nbsp;" + volume;
                    if (!issue.isEmpty()) {
                        s += "(" + issue + ")";
                    }
                }
                // Pages
                if (!getPages().isEmpty()) {
                    s += ":&nbsp;" + getPages() + ".";
                }
            }

            // Conference
            if (!getConference().isEmpty()) {
                s += " " + getConference();
            }
            s += ".";

            if (s.endsWith(".."))
                s = s.substring(0, s.length()-1);

            if (s.endsWith(". ."))
                s = s.substring(0, s.length()-2);

            if (getPages().isEmpty() && !getPageCount().isEmpty()) {
                s += " " + getPageCount() + " pp.";
            }

            s += (doi.isEmpty() ? "" : "<br />DOI:<a href=\"" + URL_DOI_BASE + doi + "\">" + doi + "</a>");
        }
        return s;
    }
    //*/
    
    /**
     * @see APIEntryInterface#getJSON()
     */
    @Override
    public JSONObject getJSON() { return this.o; }
}
