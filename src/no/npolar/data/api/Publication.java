package no.npolar.data.api;

import com.google.gwt.aria.client.Roles;
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
public class Publication extends APIEntry implements APIEntryInterface {

    /** The logger. */
    private static final Log LOG = LogFactory.getLog(Publication.class);
    /** The JSON object that this instance is built from. */
    private JSONObject o = null;
    
    /**
     * JSON key: Publication title.
     * @deprecated Use {@link Key#TITLE} instead.
     */
    public static final String JSON_KEY_TITLE           = Key.TITLE;
    /**
     * JSON key: Links.
     * @deprecated Use {@link Key#LINKS} instead.
     */
    public static final String JSON_KEY_LINKS           = Key.LINKS;
    /**
     * JSON key: The rel property of a link.
     * @deprecated Use {@link Key#LINK_REL} instead.
     */
    public static final String JSON_KEY_LINK_REL        = Key.LINK_REL;
    /**
     * JSON key: The href property of a link.
     * @deprecated Use {@link Key#LINK_HREF} instead.
     */
    public static final String JSON_KEY_LINK_HREF       = Key.LINK_HREF;
    /**
     * JSON key: The hreflang property of a link.
     * @deprecated Use {@link Key#LINK_HREFLANG} instead.
     */
    public static final String JSON_KEY_LINK_HREFLANG   = Key.LINK_HREFLANG;
    /**
     * JSON key: Type of publication.
     * @deprecated Use {@link Key#LINK_TYPE} instead.
     */
    public static final String JSON_KEY_LINK_TYPE       = Key.LINK_TYPE;
    /**
     * JSON key: Published timestamp.
     * @deprecated Use {@link Key#PUB_TIME} instead.
     */
    public static final String JSON_KEY_PUB_TIME        = Key.PUB_TIME;
    /**
     * JSON key: Published timestamp accuracy.
     * @deprecated This field has been removed. (Implicitly defined by {@link Key#PUB_TIME}.)
     */
    public static final String JSON_KEY_PUB_ACCURACY    = "published_helper";
    /** 
     * JSON key: Publish year. 
     * @deprecated Use {@link Key#PUB_TIME} instead.
     */
    public static final String JSON_KEY_PUBYEAR         = "published-year";
    /**
     * JSON key: Publish date. 
     * @deprecated Use {@link Key#PUB_TIME} instead.
     */
    public static final String JSON_KEY_PUBDATE         = "published-date";
    /** 
     * JSON key: ID. 
     * @deprecated Use {@link Key#ID} instead.
     */
    public static final String JSON_KEY_ID              = Key.ID;
    
    public class Key extends APIEntry.Key {
        /** Publication title. */
        public static final String TITLE = "title";
        /** Publication type. */
        public static final String TYPE = "publication_type";
        /** Publication language. */
        public static final String LANGUAGE = "publication_lang";
        /** Publication time. */
        public static final String PUB_TIME = "published";
        /** DOI. */
        public static final String DOI = "doi";
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
        /** Publication state. */
        public static final String STATE = "state";
        /** Comment. */
        public static final String COMMENT = "comment";
        /** Volume. */        
        public static final String VOLUME = "volume";
        /** Issue. */
        public static final String ISSUE = "issue";
        /** Journal / periodical. */
        public static final String JOURNAL = "journal";
        /** Topics. */
        public static final String TOPICS = "topics";
        /** Name - generic, used for journal name, person name, etc. */
        public static final String NAME = "name";
        /** NPI-specific series. */
        public static final String NPI_SERIES = "np_series";
        /** Series. */
        public static final String SERIES = "series";
        /** Series number. */
        public static final String SERIES_NO = "series_no";
        /** Pages - page interval, from page to page. */
        public static final String PAGES = "pages";
        /** Page count - total number of pages. */
        public static final String PAGE_COUNT = "page_count";
        /** People – authors, editors, translators, etc. */
        public static final String PEOPLE = "people";
        /** First name. */
        public static final String FNAME = "first_name";
        /** Last name. */
        public static final String LNAME = "last_name";
        /** Roles. */
        public static final String ROLES = "roles";
        /** Email. */
        public static final String EMAIL = "email";
        /** Contributor ID (currently the email). */
        public static final String CONTRIB_ID = "people.email";
        /** Contributor role. */
        public static final String CONTRIB_ROLE = "people.roles";
        /** Organization. */
        public static final String ORG = "organisation";
        /** Organizations. */
        public static final String ORGS = "organisations";
        /** Organizations ID. */
        public static final String ORGS_ID = "organisations.id";
        /** Research stations associated with the publication. */
        public static final String STATIONS = "research_stations";
        /** Programmes associated with the publication. */
        public static final String PROGRAMMES = "programme";
        /** Location. */
        public static final String LOCATION = "location";
        /** Conference. */
        public static final String CONF = "conference";
        /** Conference name. */
        public static final String CONF_NAME = "name";
        /** Conference place. */
        public static final String CONF_PLACE = "place";
        /** Conference country. */
        public static final String CONF_COUNTRY = "country";
        /** Conference dates. */
        public static final String CONF_DATES = "dates";
        /** ISBN identifier. */
        public static final String ISBN = "isbn";
        /** ISSN identifier. */
        public static final String ISSN = "issn";
        /** Supplement (?). */
        public static final String SUPPLEMENT = "suppl";
        /** Article number (?). */
        public static final String ARTICLE_NUMBER = "art_no";
        /** Draft state. (Applies to the entry, not the publication.) */
        public static final String DRAFT = "draft";
    }
    
    /**
     * JSON key: DOI.
     * @deprecated Use {@link Key#DOI} instead.
     * 
     */
    public static final String JSON_KEY_DOI             = Key.DOI;
    /** 
     * JSON key: Publication type. 
     * @deprecated Use {@link Key#TYPE} instead.
     */
    public static final String JSON_KEY_TYPE            = Key.TYPE;
    /** 
     * JSON key: Publication language. 
     * @deprecated Use {@link Key#LANGUAGE} instead.
     */
    public static final String JSON_KEY_LANGUAGE        = Key.LANGUAGE;
    /**
     * JSON key: Publication state.
     * @deprecated Use {@link Key#STATE} instead.
     */
    public static final String JSON_KEY_STATE           = Key.STATE;
    /**
     * JSON key: Comment.
     * @deprecated Use {@link Key#COMMENT} instead.
     */
    public static final String JSON_KEY_COMMENT         = Key.COMMENT;
    /**
     * JSON key: Volume.
     * @deprecated Use {@link Key#VOLUME} instead.
     */
    public static final String JSON_KEY_VOLUME          = Key.VOLUME;
    /**
     * JSON key: Issue.
     * @deprecated Use {@link Key#ISSUE} instead.
     */
    public static final String JSON_KEY_ISSUE           = Key.ISSUE;
    /**
     * JSON key: Journal in which the publication appeared.
     * @deprecated Use {@link Key#JOURNAL} instead.
     */
    public static final String JSON_KEY_JOURNAL         = Key.JOURNAL;
    /**
     * JSON key: Topics that apply to the publications.
     * @deprecated Use {@link Key#TOPICS} instead.
     */
    public static final String JSON_KEY_TOPICS          = Key.TOPICS;
    /**
     * JSON key: Name (generic: journal name, person name etc.).
     * @deprecated Use {@link Key#NAME} instead.
     */
    public static final String JSON_KEY_NAME            = Key.NAME;
    /**
     * JSON key: NPI series.
     * @deprecated Use {@link Key#NPI_SERIES} instead.
     */
    public static final String JSON_KEY_NPI_SERIES      = Key.NPI_SERIES;
    /**
     * JSON key: Series.
     * @deprecated Use {@link Key#SERIES} instead.
     */
    public static final String JSON_KEY_SERIES          = Key.SERIES;
    /**
     * JSON key: Series no.
     * @deprecated Use {@link Key#SERIES_NO} instead.
     */
    public static final String JSON_KEY_SERIES_NO       = Key.SERIES_NO;
    /**
     * JSON key: Pages.
     * @deprecated Use {@link Key#PAGES} instead.
     */
    public static final String JSON_KEY_PAGES           = Key.PAGES;
    /** 
     * JSON key: Page count. 
     * @deprecated Use {@link Key#PAGE_COUNT} instead.
     */
    public static final String JSON_KEY_PAGE_COUNT      = Key.PAGE_COUNT;
    /** 
     * JSON key: People. 
     * @deprecated Use {@link Key#PEOPLE} instead.
     */
    public static final String JSON_KEY_PEOPLE          = Key.PEOPLE;
    /** 
     * JSON key: First name. 
     * @deprecated Use {@link Key#FNAME} instead.
     */
    public static final String JSON_KEY_FNAME           = Key.FNAME;
    /** 
     * JSON key: Last name. 
     * @deprecated Use {@link Key#LNAME} instead.
     */
    public static final String JSON_KEY_LNAME           = Key.LNAME;
    /** 
     * JSON key: Roles. 
     * @deprecated Use {@link Key#ROLES} instead.
     */
    public static final String JSON_KEY_ROLES           = Key.ROLES;
    /** 
     * JSON key: Email. 
     * @deprecated Use {@link Key#EMAIL} instead.
     */
    public static final String JSON_KEY_EMAIL           = Key.EMAIL;
    /**
     * JSON key: Contributor ID (currently the email).
     * @deprecated Use {@link Key#CONTRIB_ID} instead.
     * 
     */
    public static final String JSON_KEY_CONTRIB_ID      = Key.CONTRIB_ID;
    /**
     * JSON key: Contributor role.
     * @deprecated Use {@link Key#CONTRIB_ROLE} instead.
     */
    public static final String JSON_KEY_CONTRIB_ROLE    = Key.CONTRIB_ROLE;
    /**
     * JSON key: Organization.
     * @deprecated Use {@link Key#ORG} instead.
     */
    public static final String JSON_KEY_ORG             = Key.ORG;
    /**
     * JSON key: Organizations.
     * @deprecated Use {@link Key#ORGS} instead.
     */
    public static final String JSON_KEY_ORGS            = Key.ORGS;
    /**
     * JSON key: Organizations ID.
     * @deprecated Use {@link Key#ORGS_ID} instead.
     */
    public static final String JSON_KEY_ORGS_ID         = Key.ORGS_ID;
    /**
     * JSON key: Research stations associated with the publication.
     * @deprecated Use {@link Key#STATIONS} instead.
     */
    public static final String JSON_KEY_STATIONS        = Key.STATIONS;
    /**
     * JSON key: Programmes associated with the publication.
     * @deprecated Use {@link Key#PROGRAMMES} instead.
     */
    public static final String JSON_KEY_PROGRAMMES      = Key.PROGRAMMES;
    /**
     * JSON key: Location.
     * @deprecated Use {@link Key#LOCATION} instead.
     */
    public static final String JSON_KEY_LOCATION        = Key.LOCATION;
    /**
     * JSON key: Conference.
     * @deprecated Use {@link Key#CONF} instead.
     */
    public static final String JSON_KEY_CONF            = Key.CONF;
    /**
     * JSON key: Conference name.
     * @deprecated Use {@link Key#CONF_NAME} instead.
     */
    public static final String JSON_KEY_CONF_NAME       = Key.CONF_NAME;
    /**
     * JSON key: Conference place.
     * @deprecated Use {@link Key#CONF_PLACE} instead.
     */
    public static final String JSON_KEY_CONF_PLACE      = Key.CONF_PLACE;
    /**
     * JSON key: Conference country.
     * @deprecated Use {@link Key#CONF_COUNTRY} instead.
     */
    public static final String JSON_KEY_CONF_COUNTRY    = Key.CONF_COUNTRY;
    /**
     * JSON key: Conference dates.
     * @deprecated Use {@link Key#CONF_DATES} instead.
     */
    public static final String JSON_KEY_CONF_DATES      = Key.CONF_DATES;
    /**
     * JSON key: ISBN identifier.
     * @deprecated Use {@link Key#ISBN} instead.
     */
    public static final String JSON_KEY_ISBN            = Key.ISBN;
    /**
     * JSON key: ISSN identifier.
     * @deprecated Use {@link Key#ISSN} instead.
     */
    public static final String JSON_KEY_ISSN            = Key.ISSN;
    /**
     * JSON key: Supplement (?).
     * @deprecated Use {@link Key#SUPPLEMENT} instead.
     */
    public static final String JSON_KEY_SUPPLEMENT      = Key.SUPPLEMENT;
    /**
     * JSON key: Article number (?).
     * @deprecated Use {@link Key#ARTICLE_NUMBER} instead.
     */
    public static final String JSON_KEY_ARICLE_NUMBER   = Key.ARTICLE_NUMBER;
    /**
     * JSON key: Draft state. (Applies to the entry, not the publication.)
     * @deprecated Use {@link Key#DRAFT} instead.
     */
    public static final String JSON_KEY_DRAFT           = Key.DRAFT;
    
    public class Val extends APIEntry.Val {
        /** Pre-defined JSON value: Used on entries that are NOT flagged as drafts. */
        public static final String DRAFT_FALSE = "no";
        /** Pre-defined JSON value: Used on entries that are flagged as drafts. */
        public static final String DRAFT_TRUE = "yes";
        /** Pre-defined JSON value: link rel "DOI". */
        public static final String LINK_DOI = "doi";
        /** Pre-defined JSON value: link rel "parent". */
        public static final String LINK_PARENT = "parent";
        /** Pre-defined JSON value: "related". */
        public static final String LINK_RELATED = "related";
        /** Pre-defined JSON value: link rel "XREF_DOI". */
        public static final String LINK_XREF_DOI = "xref_doi";
        /** Pre-defined JSON value: NPI organizational id. */
        public static final String ORG_NPI = "npolar.no";
        /** Pre-defined JSON value: role "author". */
        public static final String ROLE_AUTHOR = "author";
        /** Pre-defined JSON value: role "co-author". */
        public static final String ROLE_COAUTHOR = "co-author";
        /** Pre-defined JSON value: role "editor". */
        public static final String ROLE_EDITOR = "editor";
        /** Pre-defined JSON value: role "publisher". */
        public static final String ROLE_ORIGINATOR = "originator";
        /** Pre-defined JSON value: role "publisher". */
        public static final String ROLE_PUBLISHER = "publisher";
        /** Pre-defined JSON value: role "publisher". */
        public static final String ROLE_RESOURCE_PROVIDER = "resourceProvider";
        /** Pre-defined JSON value: role "publisher". */
        public static final String ROLE_FUNDER = "funder";
        /** Pre-defined JSON value: role "translator". */
        public static final String ROLE_TRANSLATOR = "translator";
        /** Pre-defined JSON value: role "advisor". */
        public static final String ROLE_ADVISOR = "advisor";
        /** Pre-defined JSON value: role "correspondent". */
        public static final String ROLE_CORRESPONDENT = "correspondent";
        /** Pre-defined JSON value: state "accepted". */
        public static final String STATE_ACCEPTED = "accepted";
        /** Pre-defined JSON value: state "published". */
        public static final String STATE_PUBLISHED = "published";
        /** Pre-defined JSON value: state "submitted". */
        public static final String STATE_SUBMITTED = "submitted";
    }

    /** 
     * Pre-defined JSON value: state "submitted". 
     * @deprecated Use {@link Val#STATE_SUBMITTED} instead.
     */
    public static final String JSON_VAL_STATE_SUBMITTED = Val.STATE_SUBMITTED;
    /** 
     * Pre-defined JSON value: state "accepted". 
     * @deprecated Use {@link Val#STATE_ACCEPTED} instead.
     */
    public static final String JSON_VAL_STATE_ACCEPTED  = Val.STATE_ACCEPTED;
    /** 
     * Pre-defined JSON value: state "published". 
     * @deprecated Use {@link Val#STATE_PUBLISHED} instead.
     */
    public static final String JSON_VAL_STATE_PUBLISHED = Val.STATE_PUBLISHED;
    /** 
     * Pre-defined JSON value: "related". 
     * @deprecated Use {@link Val#LINK_RELATED} instead.
     */
    public static final String JSON_VAL_LINK_RELATED    = Val.LINK_RELATED;
    /** 
     * Pre-defined JSON value: link rel "DOI". 
     * @deprecated Use {@link Val#LINK_DOI} instead.
     */
    public static final String JSON_VAL_LINK_DOI        = Val.LINK_DOI;
    /** 
     * Pre-defined JSON value: link rel "XREF_DOI". 
     * @deprecated Use {@link Val#LINK_XREF_DOI} instead.
     */
    public static final String JSON_VAL_LINK_XREF_DOI   = Val.LINK_XREF_DOI;
    /** 
     * Pre-defined JSON value: link rel "parent". 
     * @deprecated Use {@link Val#LINK_PARENT} instead.
     */
    public static final String JSON_VAL_LINK_PARENT     = Val.LINK_PARENT;
    /**
     * Pre-defined JSON value: role "author". 
     * @deprecated Use {@link Val#ROLE_AUTHOR} instead.
     */
    public static final String JSON_VAL_ROLE_AUTHOR     = Val.ROLE_AUTHOR;
    /**
     * Pre-defined JSON value: role "co-author". 
     * @deprecated Use {@link Val#ROLE_COAUTHOR} instead.
     */
    public static final String JSON_VAL_ROLE_COAUTHOR   = Val.ROLE_COAUTHOR;
    /**
     * Pre-defined JSON value: role "editor". 
     * @deprecated Use {@link Val#ROLE_EDITOR} instead.
     */
    public static final String JSON_VAL_ROLE_EDITOR     = Val.ROLE_EDITOR;
    /**
     * Pre-defined JSON value: role "translator". 
     * @deprecated Use {@link Val#ROLE_TRANSLATOR} instead.
     */
    public static final String JSON_VAL_ROLE_TRANSLATOR = Val.ROLE_TRANSLATOR;
    /**
     * Pre-defined JSON value: role "publisher". 
     * @deprecated Use {@link Val#ROLE_PUBLISHER} instead.
     */
    public static final String JSON_VAL_ROLE_PUBLISHER  = Val.ROLE_PUBLISHER;
    /**
     * Pre-defined JSON value: NPI organizational id. 
     * @deprecated Use {@link Val#ORG_NPI} instead.
     */
    public static final String JSON_VAL_ORG_NPI         = Val.ORG_NPI;
    /** 
     * Pre-defined JSON value: Used on entries that are flagged as drafts. 
     * @deprecated Use {@link Val#DRAFT_TRUE} instead.
     */
    public static final String JSON_VAL_DRAFT_TRUE      = Val.DRAFT_TRUE;
    /** 
     * Pre-defined JSON value: Used on entries that are NOT flagged as drafts. 
     * @deprecated Use {@link Val#DRAFT_FALSE} instead.
     */
    public static final String JSON_VAL_DRAFT_FALSE     = Val.DRAFT_FALSE;
    
    /**
     * The date format pattern used in the JSON.
     * @deprecated The pattern will vary, and must be determined by evaluating {@link #JSON_KEY_PUB_TIME}.
     */
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
    
    /**
     * Valid publication types.
     */
    public enum Type {
        PEER_REVIEWED("peer-reviewed")
        ,EDITORIAL("editorial")
        ,REVIEW("review") 
        ,CORRECTION("correction")
        ,BOOK("book")
        ,MAP("map")
        ,POSTER("poster")
        ,REPORT("report")
        ,ABSTRACT("abstract")
        ,PHD("phd")
        ,MASTER("master")
        ,PROCEEDINGS("proceedings")
        ,POPULAR("popular")
        ,IN_BOOK("in-book")
        ,IN_REPORT("in-report")
        ,OTHER("other")
        ,UNDEFINED("undefined")
        ;
        
        private String typeString = "other";
        
        Type(String typeString) {
            this.typeString = typeString;
        }
        
        /**
         * Gets the Type variant that is associated with the given type string.
         * 
         * @param typeStr The type string.
         * @return The Type variant that is associated with the given pattern string, or {@link Type#UNDEFINED} if none.
         */
        public static Type forString(String typeStr) {
            for (Type t : values()) {
                if (typeStr.toLowerCase().equals(t.toString()))
                    return t;
            }
            return Type.UNDEFINED;
        }
        
        @Override
        public String toString() {
            return this.typeString;
        }
        /*
        @Override
        public final boolean equals (Object obj) {
            if (!(obj instanceof Type))
                return false;
            if (obj == this)
                return true;
            Type rhs = (Type) obj;
            return rhs.typeString.equals(this.typeString);
        }
        //*/
    };
    
    /** 
     * The pre-defined keyword for identifying peer-reviewed publications. 
     * @deprecated Use {@link Type#PEER_REVIEWED} instead.
     */
    public static final String TYPE_PEER_REVIEWED = Type.PEER_REVIEWED.toString();
    /** 
     * The pre-defined keyword for identifying editorials. 
     * @deprecated Use {@link Type#EDITORIAL} instead.
     */
    public static final String TYPE_EDITORIAL = Type.EDITORIAL.toString();
    /** 
     * The pre-defined keyword for identifying reviews. 
     * @deprecated Use {@link Type#REVIEW} instead.
     */
    public static final String TYPE_REVIEW = Type.REVIEW.toString();
    /** 
     * The pre-defined keyword for identifying corrections. 
     * @deprecated Use {@link Type#CORRECTION} instead.
     */
    public static final String TYPE_CORRECTION = Type.CORRECTION.toString();
    /** 
     * The pre-defined keyword for identifying books. 
     * @deprecated Use {@link Type#BOOK} instead.
     */
    public static final String TYPE_BOOK = Type.BOOK.toString();
    /** The pre-defined keyword for identifying book chapters. */
    //public static final String TYPE_BOOK_CHAPTER = "book-chapter";
    /** 
     * The pre-defined keyword for identifying maps. 
     * @deprecated Use {@link Type#MAP} instead.
     */
    public static final String TYPE_MAP = Type.MAP.toString();
    /** 
     * The pre-defined keyword for identifying posters. 
     * @deprecated Use {@link Type#POSTER} instead.
     */
    public static final String TYPE_POSTER = Type.POSTER.toString();
    /** 
     * The pre-defined keyword for identifying reports. 
     * @deprecated Use {@link Type#REPORT} instead.
     */
    public static final String TYPE_REPORT = Type.REPORT.toString();
    /**The pre-defined keyword for identifying report series contributions. */
    //public static final String TYPE_REPORT_SERIES_CONTRIBUTION = "report-series-contrib";
    /** 
     * The pre-defined keyword for identifying abstracts. 
     * @deprecated Use {@link Type#ABSTRACT} instead.
     */
    public static final String TYPE_ABSTRACT = Type.ABSTRACT.toString();
    /** 
     * The pre-defined keyword for identifying PhD theses. 
     * @deprecated Use {@link Type#PHD} instead.
     */
    public static final String TYPE_PHD = Type.PHD.toString();
    /** 
     * The pre-defined keyword for identifying Master theses. 
     * @deprecated Use {@link Type#MASTER} instead.
     */
    public static final String TYPE_MASTER = Type.MASTER.toString();
    /** 
     * The pre-defined keyword for identifying proceedings. 
     * @deprecated Use {@link Type#PROCEEDINGS} instead.
     */
    public static final String TYPE_PROCEEDINGS = Type.PROCEEDINGS.toString();
    /** 
     * The pre-defined keyword for identifying popular science publications. 
     * @deprecated Use {@link Type#POPULAR} instead.
     */
    public static final String TYPE_POPULAR = Type.POPULAR.toString();
    /** 
     * The pre-defined keyword for identifying other publications. 
     * @deprecated Use {@link Type#OTHER} instead.
     */
    public static final String TYPE_OTHER = Type.OTHER.toString();
    
    /** Identifier constant for reference string partial: author(s). */
    public static final int CITE_PART_AUTHORS = 0;
    /** Identifier constant for reference string partial: editor(s). */
    public static final int CITE_PART_EDITORS = 1;
    /** Identifier constant for reference string partial: translator(s). */
    public static final int CITE_PART_TRANSLATORS = 2;
    
    /** The supported timestamp patterns for {@link #JSON_KEY_PUB_TIME}. Higher index in this array = higher timestamp accuracy. */
    public static final String[] PATTERNS_PUB_TIME = { "yyyy", "yyyy-MM", "yyyy-MM-dd" };
    
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
    protected String articleNo = "";
    //protected String pages = "";
    protected String doi = "";
    protected String id = "";
    protected String link = "";
    protected Type type = Type.UNDEFINED;
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
        // If the JSON object is null or the ID is missing, this is no good
        // (Added this check in attempt to avoid logging of errors suspected
        // to be rooted in API downtime/error, which does occur now and then.)
        if (this.o != null && this.o.has(Key.ID)) {
            init();
        }
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
            // Translate 2-letter country, e.g. "NO" => "Norway"
            try { mappings.addAllPipeSeparated(labels.getString(Labels.DATA_COUNTRIES_0)); } catch (Exception e) { }
            // Translate general strings used by the service, e.g. "NP Report Series" => "Norwegian Polar Institute Report series"
            try { mappings.addAllPipeSeparated(labels.getString(Labels.DATA_DB_VALUES_0)); } catch (Exception e) { } 
            //try { mappings.addMapping("Temakart nr.", "Thematic map no."); } catch (Exception e) { }
        } catch (Exception e) { }
        
        
        ////////////////////////////////////////////////////////////////////////
        // All the non-complex stuff
        try { title     = o.getString(Key.TITLE).trim(); } catch (Exception e) { title = labels.getString(Labels.LABEL_DEFAULT_TITLE_0); }
        //try { pubYear   = o.getString(JSON_KEY_PUBYEAR); if (pubYear.equalsIgnoreCase("0")) pubYear = ""; } catch (Exception e) { }
        //try { pubDate   = new SimpleDateFormat(DATE_FORMAT_JSON).parse(o.getString(JSON_KEY_PUBDATE)); } catch (Exception e) { }
        try { id        = o.getString(Key.ID); } catch (Exception e) { }//o.getString(JSON_KEY_ID); } catch (Exception e) { }
        try { type      = Type.forString(o.getString(Key.TYPE)); } catch (Exception e) { }
        //try { type      = o.getString(Key.TYPE); } catch (Exception e) { }
        try { language  = o.getString(Key.LANGUAGE); } catch (Exception e) { }
        try { state     = o.getString(Key.STATE); } catch (Exception e) { } 
        try { volume    = o.getString(Key.VOLUME); } catch (Exception e) { }
        try { issue     = o.getString(Key.ISSUE); } catch (Exception e) { }
        try { articleNo = o.getString(Key.ARTICLE_NUMBER); } catch (Exception e) { }
        try { pageCount = o.getString(Key.PAGE_COUNT); } catch (Exception e) { }
        try { comment   = o.getString(Key.COMMENT); } catch (Exception e) { }
        try { links     = o.getJSONArray(Key.LINKS); } catch (Exception e) { }
        try { topics    = o.getJSONArray(Key.TOPICS); } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Publish time: Should be year OR month of year OR full date
        //String publishTimePattern = PATTERNS_PUB_TIME[0]; // Initially, use year
        try { 
            /*
            String publishTimeFormatStr = o.getString(JSON_KEY_PUB_ACCURACY);
            if (publishTimeFormatStr.contains("d")) {
                publishTimeFormat = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_DATE_0), displayLocale);
            } else if (publishTimeFormatStr.contains("m")) {
                publishTimeFormat = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_MONTH_0), displayLocale);
            } else {
                publishTimeFormat = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_YEAR_0), displayLocale);
            }
            */
            
            publishTimeFormat = APIUtil.getTimestampFormat(o.getString(Key.PUB_TIME));
            //publishTimePattern = publishTimeFormat.toPattern();
            /*
            String publishTimeString = o.getString(Key.PUB_TIME);
            int publishTimeStringLength = publishTimeString.length();
            if (publishTimeStringLength == 10) { // yyyy-MM-dd
                publishTimePattern = PATTERNS_PUB_TIME[2];
                publishTimeFormat = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_DATE_0), displayLocale);
            } else if (publishTimeStringLength == 7) { // yyyy-MM
                publishTimePattern = PATTERNS_PUB_TIME[1];
                publishTimeFormat = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_MONTH_0), displayLocale);
            } else if (publishTimeStringLength == 4) { // yyyy
                publishTimeFormat = new SimpleDateFormat(labels.getString(Labels.PUB_REF_DATE_FORMAT_YEAR_0), displayLocale);
            }
            //*/
        } catch (Exception e) { 
            publishTimeFormat = new SimpleDateFormat(PATTERNS_PUB_TIME[0], displayLocale);
        }
        String publishTimeRaw = null;
        try { publishTimeRaw = o.getString(Key.PUB_TIME); } catch (Exception ignore) {}
        if (publishTimeRaw != null) {
            try { 

                publishTime = publishTimeFormat.parse(publishTimeRaw);
            } catch (Exception e) {
                //System.out.println("Unexpected format on publish time, no suitable parser available. Publication ID was " + this.id);
                if (LOG.isErrorEnabled()) {
                    LOG.error("Unexpected format on publish time, no suitable parser available. Publication ID was " + this.id);
                }
            }
            try {
                pubYear = new SimpleDateFormat(PATTERNS_PUB_TIME[0], displayLocale).format(publishTime);
            } catch (Exception e) {
                //System.out.println("Unable to determine publish year. Bad publish time format? Publication ID was " + this.id);
                if (LOG.isErrorEnabled()) {
                    LOG.error("Unable to determine publish year. Bad publish time format '" + publishTimeRaw  + "'? Publication ID was " + this.id);
                }
            } finally {
                if (pubYear == null || pubYear.isEmpty()) {
                    try {
                        pubYear = publishTimeRaw.substring(0, 4);
                    } catch (Exception e) {
                        //System.out.println("Fallback routine for determining publish year failed. Publication ID was " + this.id);
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Fallback routine for determining publish year failed. Publication ID was " + this.id);
                        }
                    }
                }
            }
        } else {
            if (LOG.isErrorEnabled()) {
                LOG.error("Publication with ID " + this.id + " is missing required publish time.");
            }
        }
        
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
                                    /*
                                    publisher += (publisher.isEmpty() ? "" : ", ") + org.getString(Key.NAME).trim(); // Add the publisher name, if any
                                    publisherLocation = org.getString(Key.LOCATION).trim(); // Get the publisher location, if any
                                    publisher += (publisher.isEmpty() ? "" : ", ") + publisherLocation; // Add the publisher location
                                    */
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
        
        ////////////////////////////////////////////////////////////////////////
        // People
        if (o.has(Key.PEOPLE)) {
            try {
                JSONArray persons = o.getJSONArray(Key.PEOPLE);
                authorsAndEditors = new PersonCollection(persons, displayLocale);
                // The list above may now contain translators and/or co-authors. 
                // If so, split those out into separate lists:
                translators = authorsAndEditors.getByRoleOnly(Val.ROLE_TRANSLATOR);
                coAuthors = authorsAndEditors.getByRoleOnly(Val.ROLE_COAUTHOR);
                authorsAndEditors.removeAll(translators);
                authorsAndEditors.removeAll(coAuthors);
            } catch (Exception e) { }
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Pages
        JSONArray pagesArr = null;
        try {
            pagesArr = o.getJSONArray(Key.PAGES);
            if (pagesArr.length() == 2) {
                pageStart = pagesArr.getString(0).trim();
                pageEnd = pagesArr.getString(1).trim();
            }
        } catch (Exception e) { }
        
        ////////////////////////////////////////////////////////////////////////
        // Journal
        JSONObject journalObj = null;
        try {
            journalObj = o.getJSONObject(Key.JOURNAL);
            try { journalName = journalObj.getString(Key.NAME).trim(); } catch (Exception e) { }
            if (journalObj.has(Key.NPI_SERIES) || journalObj.has(Key.SERIES)) {
                try { 
                    journalSeries = journalObj.getString(Key.SERIES).trim(); // If there is a "normal" series, use that.
                } catch (Exception e) {
                    try {
                        journalSeries = journalObj.getString(Key.NPI_SERIES).trim(); // If not, use the NPI series.
                    } catch (Exception ee) {
                    }
                }
                if (journalObj.has(Key.SERIES_NO)) {
                    journalSeriesNo = journalObj.getString(Key.SERIES_NO).trim();
                }
            }
            /*if (journalObj.has(Key.NPI_SERIES)) {
                journalSeries = journalObj.getString(Key.NPI_SERIES).trim();
                if (journalObj.has(Key.SERIES_NO)) {
                    journalSeriesNo = journalObj.getString(Key.SERIES_NO).trim();
                }
            }*/
        } catch (Exception e) { 
            
        }
        
        ////////////////////////////////////////////////////////////////////////
        // DOI
        // New routine - should work for all that have DOI
        try {
            //doi = o.getString(Key.DOI); 
            doi = o.getString(Key.DOI);
            //System.out.println("Got DOI " + doi);
        } catch (Exception e) {}
        // Old routine - just in case
        if (getDOI().isEmpty()) {
            try {
                for (int i = 0; i < links.length(); i++) {
                    JSONObject linkObj = links.getJSONObject(i);
                    try {
                        if (linkObj.getString(Key.LINK_REL).equalsIgnoreCase(Val.LINK_DOI)) {
                            doi = extractDoi(linkObj.getString(Key.LINK_HREF));//doi = linkObj.getString(Key.LINK_HREF).replace(URL_DOI_BASE, "");
                            //break;
                        }
                        else if (linkObj.getString(Key.LINK_REL).equalsIgnoreCase(Val.LINK_XREF_DOI)) {
                            if (doi == null || doi.isEmpty())
                                doi = extractDoi(linkObj.getString(Key.LINK_HREF));//doi = linkObj.getString(Key.LINK_HREF).replace(URL_DOI_BASE, "");
                        }                      
                    } catch (Exception doie) { }
                }
            } catch (Exception e) { }
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Parent publication
        try {
            if (links != null) {
                for (int i = 0; i < links.length(); i++) {
                    JSONObject linkObj = links.getJSONObject(i);
                    try {
                        if (Val.LINK_PARENT.equalsIgnoreCase(linkObj.getString(Key.LINK_REL))) { // if (this link's "rel" says "parent")
                            parentUrl = linkObj.getString(Key.LINK_HREF);
                            parentId = parentUrl.substring(parentUrl.lastIndexOf("/")+1);
                            
                            if (parentId.contains("%")) {
                                parentId = parentId.substring(0, parentId.indexOf("%"));
                            }

                            //System.out.println(this.getTitle() + " - found parent link: " + parentUrl + " - ID: " + parentId);

                            parent = new PublicationService(displayLocale).get(parentId);
                            
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
            conference = o.getJSONObject(Key.CONF);
            try { confName = conference.getString(Key.CONF_NAME).trim(); } catch (Exception e) { }
            try { confPlace = conference.getString(Key.CONF_PLACE).trim(); } catch (Exception e) { }
            //try { confCountry = getMappedString(conference.getString(Key.CONF_COUNTRY).trim()); } catch (Exception e) { }
            try { confCountry = mappings.getMapping(conference.getString(Key.CONF_COUNTRY).trim()); } catch (Exception e) { }
            
            if (conference.has(Key.CONF_DATES)) {
                try {
                    JSONArray dates = conference.getJSONArray(Key.CONF_DATES);
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
                    && getType().equals(Type.PROCEEDINGS) // AND type is "proceedings"
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
     * @see Type
     */
    public Type getType() { return type; }
    
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
    public String getGroupName() { return getType().toString(); }
    
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
     * Gets the article number for this publication, if any.
     * 
     * @return The article number for this publication, or an empty string if none.
     */
    public String getArticleNumber() { return articleNo; }
    
    /**
     * Gets the language for this publication, if any.
     * 
     * @return The language for this publication, or an empty string if none.
     */
    public String getLanguage() { return language; }
    
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
     * 
     * @param baseUrl The base URL, that is, the URL of the service.
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
     * 
     * @param type The type to test against, e.g. {@link Type#REPORT}.
     * 
     * @return True if this publication is of the given type, false if not.
     * @see #isType(java.lang.String) 
     */
    public boolean isType(Type type) {
        return this.type.equals(type);
        //return this.type != null && this.type.toString().equalsIgnoreCase(type); 
        //return !this.type.isEmpty() && this.type.equalsIgnoreCase(type); 
    }
    
    /**
     * Gets a flag indicating whether or not this publication is of the given type. 
     * <p>
     * The type match is not case sensitive.
     * 
     * @param typeString The type to test against, e.g. "report".
     * 
     * @return True if this publication is of the given type, false if not.
     * @see #isType(no.npolar.data.api.Publication.Type) 
     */
    public boolean isType(String typeString) {
        return this.type.equals(Type.forString(typeString));
    }
    
    /**
     * Gets a flag indicating whether or not this publication's state matches the 
     * given state.
     * <p>
 The forString is not case sensitive.
     * 
     * @param state The state string to test against, e.g. "published".
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
        if (hasParent() || type == Type.IN_BOOK || type == Type.IN_REPORT)
            return true;
        
        //return type.equals(TYPE_BOOK_CHAPTER) || type.equals(TYPE_REPORT_SERIES_CONTRIBUTION);
        if (type == Type.REPORT || type == Type.BOOK) {  // Do this only if the type is currently "report" or "book"
        //if (type.equalsIgnoreCase(TYPE_REPORT) || type.equalsIgnoreCase(TYPE_BOOK)) {  // Do this only if the type is currently "report" or "book"
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
        return getPeopleStringByRole(Val.ROLE_AUTHOR);
    }
    
    /**
     * Gets the complete editors string for this publication.
     * 
     * @return The complete editors string for this publication, or an empty string if none.
     * @see #getPeopleStringByRole(java.lang.String) 
     */
    public String getEditors() {
        return getPeopleStringByRole(Val.ROLE_EDITOR);
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
     * @param role The role to forString against, for example {@link #Val.ROLE_AUTHOR}.
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
     * @param role The role to forString against, for example {@link #Val.ROLE_AUTHOR}.
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
                s += getEditors() + " (" + labels.getString(getPeopleByRole(Val.ROLE_EDITOR).size() > 1 ? Labels.PUB_REF_EDITORS_0 : Labels.PUB_REF_EDITOR_0).toLowerCase() + ")";
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
     * Gets HTML for this publication, intended for listings etc.
     * 
     * @param attrClass Extra classes to apply. Provide empty string or <code>null</code> if no additional class should be applied.
     * @param asListItem If <code>true</code>, a <code>li</code> element is used as wrapper. Otherwise, a <code>span</code> is used.
     * @param includeTypeTag If <code>true</code>, the first element will be a <code>span</code> defining the publication type.
     * @return 
     */
    public String toHtml(String attrClass, boolean asListItem, boolean includeTypeTag) {
        String type = getType().toString();
        String s = "";
        s += "<" + (asListItem ? "li" : "span")
                + " class=\"publication publication--" + type
                + (attrClass != null && !attrClass.isEmpty() ? " ".concat(attrClass) : "")
                + "\">"
                + (includeTypeTag ? "<span class=\"publication__type publication__type--" + type+" tag\">" 
                    + labels.getString(Labels.PUB_TYPE_PREFIX_0.concat(type)) + "</span>" : "")
                + "<a"
                    + " class=\"publication__title\""
                    + " href=\"" + URL_PUBLINK_BASE.replaceAll("^http(s)?:", "") + id + "\""
                    + " lang=\"" + getLanguage() + "\""
                + ">"
                + getTitleClosed()
                + "</a>"
                + "<span class=\"publication__details\">"
                + " ("
                ;
        
        int i = 0;
        List<PublicationContributor> contributors = getPeople();
        Iterator<PublicationContributor> iContributors = contributors.iterator();
        while (iContributors.hasNext()) {
            PublicationContributor contributor = iContributors.next();
            i++;
            if (contributors.size() == 2) { 
                if (i == 2) {
                    s += " &amp; ";
                }
            } else if (contributors.size() == 3) {
                if (i == 2) {
                    s += ", ";
                } else if (i == 3) {
                    s += " &amp; ";
                }   
            } else if (contributors.size() > 3) {
                if (i == 2) {
                    s += " et al.";
                    break;
                }
            }
            s += contributor.getLastName();
        }
        
        if (i > 0) {
            s += " ";
        }
        
        s +=  getPubYear();
        
        s += ")";
        
        s += "</span>";
        s += "</" + (asListItem ? "li" : "span") + ">";
        
        
        return s;
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
                if (isType(Publication.Type.POPULAR) || isType(Publication.Type.OTHER))
                    s += " " + (!getPubDate().isEmpty() ? getPubDate() : pubYear) + "."; // Date takes precedence over year
                else 
                    s += " " + pubYear + "."; // Ignore date, use year
            }
            
            if (!this.isState(Val.STATE_PUBLISHED)) {
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
                        s += editorsStr + " (" + labels.getString(getPeopleByRole(Publication.Val.ROLE_EDITOR).size() > 1 ? Labels.PUB_REF_EDITORS_0 : Labels.PUB_REF_EDITOR_0).toLowerCase() + "): ";
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
            
            if (!hasParent() && (isType(Publication.Type.BOOK) || isType(Publication.Type.REPORT))) {
                if (s.endsWith(".."))
                    s = s.substring(0, s.length()-1);
                
                if (!getPublisher().isEmpty()) {
                    //s += (s.trim().endsWith(".") ? " " : ". ") + mappings.getMapping(getPublisher()) + ".";
                    s += mappings.getMapping(getPublisher()) + ". ";
                }
            }
            
            // Page / page span (where this publication appears in the journal/periodical/...)
            if (!(hasParent() || isPartContribution())) {
                if (!getPages().isEmpty() || !getArticleNumber().isEmpty()) {
                    // Remove trailing "." and possibly also whitespace
                    s = s.trim();
                    if (s.endsWith("."))
                        s = s.substring(0, s.length()-1);
                    
                    s += ":&nbsp;" + (getPages().isEmpty() ? getArticleNumber() : getPages()) + ".";
                }
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
