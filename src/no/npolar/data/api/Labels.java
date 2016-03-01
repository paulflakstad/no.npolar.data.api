package no.npolar.data.api;

import java.util.Map;
import java.util.HashMap;
//import java.util.Locale;
//import java.util.ResourceBundle;

/**
 * Provides access to human-readable, localized translations of service identifier
 * string.
 * <p>
 * Also, provides access to localized date formats, various labels, etc.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 * @see Labels.properties
 */
public class Labels {
    public static Map<String, String> norm = new HashMap<String, String>();
    
    public static final String TIME_SERIES_PREFIX_0 = "timeseries.";
    public static final String TIME_SERIES_UNIT_0 = TIME_SERIES_PREFIX_0 + "unit";
    public static final String TIME_SERIES_TITLE_0 = TIME_SERIES_PREFIX_0 + "title";
    
    public static final String TIME_SERIES_POINT_PREFIX_0 = TIME_SERIES_PREFIX_0 + "point.";
    public static final String TIME_SERIES_POINT_MEDIAN_0 = TIME_SERIES_POINT_PREFIX_0 + "median";
    public static final String TIME_SERIES_POINT_ERROR_0 = TIME_SERIES_POINT_PREFIX_0 + "error";
    
    public static final String TIME_SERIES_POINT_VALUE_PREFIX_0 = TIME_SERIES_POINT_PREFIX_0 + "value.";
    public static final String TIME_SERIES_POINT_VALUE_LOW_0 = TIME_SERIES_POINT_VALUE_PREFIX_0 + "low";
    public static final String TIME_SERIES_POINT_VALUE_HIGH_0 = TIME_SERIES_POINT_VALUE_PREFIX_0 + "high";
    public static final String TIME_SERIES_POINT_VALUE_MIN_0 = TIME_SERIES_POINT_VALUE_PREFIX_0 + "min";
    public static final String TIME_SERIES_POINT_VALUE_MAX_0 = TIME_SERIES_POINT_VALUE_PREFIX_0 + "max";
    public static final String TIME_SERIES_POINT_VALUE_MEDIAN_0 = TIME_SERIES_POINT_VALUE_PREFIX_0 + "max";
    public static final String TIME_SERIES_POINT_VALUE_ERROR_0 = TIME_SERIES_POINT_VALUE_PREFIX_0 + "max";
    
    public static final String PUB_REF_EDITOR_0 = "publication.reference.editor";
    public static final String PUB_REF_EDITORS_0 = "publication.reference.editors";
    public static final String PUB_REF_PAGE_0 = "publication.reference.page";
    public static final String PUB_REF_PAGES_0 = "publication.reference.pages";
    public static final String PUB_REF_PAGESPAN_0 = "publication.reference.pagespan";
    public static final String PUB_REF_IN_0 = "publication.reference.in";
    public static final String PUB_REF_DATE_FORMAT_0 = "publication.reference.dateformat";
    public static final String PUB_REF_DATE_FORMAT_YEARONLY_0 = "publication.reference.dateformat.yearonly"; // yyyy
    public static final String PUB_REF_DATE_FORMAT_YEAR_0 = "publication.reference.dateformat.year";    // yyyy
    public static final String PUB_REF_DATE_FORMAT_MONTH_0 = "publication.reference.dateformat.month";  // MMM yyyy
    public static final String PUB_REF_DATE_FORMAT_DATE_0 = "publication.reference.dateformat.date";    // d MMM yyyy

    /** The key prefix for publication types. */
    public static final String PUB_TYPE_PREFIX_0 = "publication.type.";
    /** The key for publication type "peer-reviewed". */
    public static final String PUB_TYPE_PEER_REVIEWED_0 = PUB_TYPE_PREFIX_0 + "peer-reviewed";
    /** The key for publication type "editorial". */
    public static final String PUB_TYPE_EDITORIAL_0 = PUB_TYPE_PREFIX_0 + "editorial";
    /** The key for publication type "review". */
    public static final String PUB_TYPE_REVIEW_0 = PUB_TYPE_PREFIX_0 + "review";
    /** The key for publication type "correction". */
    public static final String PUB_TYPE_CORRECTION_0 = PUB_TYPE_PREFIX_0 + "correction";
    /** The key for publication type "book". */
    public static final String PUB_TYPE_BOOK_0 = PUB_TYPE_PREFIX_0 + "book";
    /** The key for publication type "poster". */
    public static final String PUB_TYPE_POSTER_0 = PUB_TYPE_PREFIX_0 + "poster";
    /** The key for publication type "report". */
    public static final String PUB_TYPE_REPORT_0 = PUB_TYPE_PREFIX_0 + "report";
    /** The key for publication type "abstract". */
    public static final String PUB_TYPE_ABSTRACT_0 = PUB_TYPE_PREFIX_0 + "abstract";
    /** The key for publication type "PhD". */
    public static final String PUB_TYPE_PHD_0 = PUB_TYPE_PREFIX_0 + "phd";
    /** The key for publication type "master thesis". */
    public static final String PUB_TYPE_MASTER_0 = PUB_TYPE_PREFIX_0 + "master";
    /** The key for publication type "proceedings". */
    public static final String PUB_TYPE_PROCEEDINGS_0 = PUB_TYPE_PREFIX_0 + "proceedings";
    /** The key for publication type "popular". */
    public static final String PUB_TYPE_POPULAR_0 = PUB_TYPE_PREFIX_0 + "popular";
    /** The key for publication type "map". */
    public static final String PUB_TYPE_MAP_0 = PUB_TYPE_PREFIX_0 + "map";
    /** The key for publication type "other". */
    public static final String PUB_TYPE_OTHER_0 = PUB_TYPE_PREFIX_0 + "other";
    
    /*
    // Publications categories
    public static final String PUB_CAT_PREFIX_0 = "publication.category"; // Category
    public static final String PUB_CAT_PEER_REVIEWED_0 = "publication.category.peer-reviewed"; // Peer reviewed
    public static final String PUB_CAT_ASSESSMENT_0 = "publication.category.assessment"; // Assessment
    public static final String PUB_CAT_NON_PEER_REVIEWED_0 = "publication.category.non-peer-reviewed"; // Not peer reviewed
    public static final String PUB_CAT_POPULAR_SCIENCE_0 = "publication.category.popular-science"; // Popularised
    public static final String PUB_CAT_FICTION_0 = "publication.category.fiction"; // Fiction
    
    // Publication groups (general types)
    public static final String PUB_GROUP_PREFIX_0 = "publication.group"; // General type
    public static final String PUB_GROUP_JOURNAL_0 = "publication.group.journal"; // Journal contributions
    public static final String PUB_GROUP_CONFERENCE_0 = "publication.group.conference"; // Conference material
    public static final String PUB_GROUP_BOOK_0 = "publication.group.book"; // Book
    public static final String PUB_GROUP_REPORT_THESIS_0 = "publication.group.report-thesis"; // Report / Thesis
    public static final String PUB_GROUP_PART_OF_BOOK_OR_REPORT_0 = "publication.group.part-of-book-or-report"; // Part of book or report
    public static final String PUB_GROUP_OTHER_0 = "publication.group.other"; // Other
    
    // Publication types (specific types)
    public static final String PUB_TYPE_PREFIX_0 = "publication.type"; // Specific type
    public static final String PUB_TYPE_JOURNAL_PUBLICATION_0 = "publication.type.journal-publication"; // Journal publication
    public static final String PUB_TYPE_REVIEW_ARTICLE_0 = "publication.type.review-article"; // Review article
    public static final String PUB_TYPE_ABSTRACT_0 = "publication.type.abstract"; // Abstract
    public static final String PUB_TYPE_REPORT_0 = "publication.type.report"; // Report
    public static final String PUB_TYPE_FEATURE_ARTICLE_0 = "publication.type.feature-article"; // Feature article
    public static final String PUB_TYPE_EDITORIAL_0 = "publication.type.editorial"; // Editorial
    public static final String PUB_TYPE_BOOK_REVIEW_0 = "publication.type.book-review"; // Book review
    public static final String PUB_TYPE_SHORT_COMMUNICATION_0 = "publication.type.short-communication"; // Short communication
    public static final String PUB_TYPE_READER_OPINION_PIECE_0 = "publication.type.reader-opinion-piece"; // Reader opinion piece
    public static final String PUB_TYPE_LETTER_TO_THE_EDITOR_0 = "publication.type.letter-to-the-editor"; // Letter to the editor
    public static final String PUB_TYPE_ERRATA_0 = "publication.type.errata"; // Errata
    public static final String PUB_TYPE_INTERVIEW_0 = "publication.type.interview"; // Interview
    public static final String PUB_TYPE_ARTICLE_IN_BUSINESS_TRADE_INDUSTRY_JOURNAL_0 = "publication.type.article-in-business-trade-industry-journal"; // Article in business/trade/industry journal
    // Publication types in group: Conference material
    public static final String PUB_TYPE_ORAL_PRESENTATION_0 = "publication.type.oral-presentation"; // Oral presentation
    public static final String PUB_TYPE_POSTER_0 = "publication.type.poster"; // Poster
    // Publication types in group: Book
    public static final String PUB_TYPE_ANTHOLOGY_0 = "publication.type.anthology"; // Anthology
    public static final String PUB_TYPE_MONOGRAPH_0 = "publication.type.monograph"; // Monograph
    public static final String PUB_TYPE_ENCYCLOPEDIA_0 = "publication.type.encyclopedia"; // Encyclopedia
    public static final String PUB_TYPE_REFERENCE_MATERIAL_0 = "publication.type.reference-material"; // Reference material
    public static final String PUB_TYPE_NON_FICTION_BOOK_0 = "publication.type.non-fiction-book"; // Non-fictional book
    public static final String PUB_TYPE_FICTION_BOOK_0 = "publication.type.fiction-book"; // Fiction book
    public static final String PUB_TYPE_TEXTBOOK_0 = "publication.type.textbook"; // Textbook
    public static final String PUB_TYPE_EXHIBITION_CATALOGUE_0 = "publication.type.exhibition-catalogue"; // Exhibition catalogue
    // Publication types in group: Report / Thesis
    public static final String PUB_TYPE_REPORT_0 = "publication.type.report"; // Report
    public static final String PUB_TYPE_COMPENDIUM_0 = "publication.type.compendium"; // Compendium
    public static final String PUB_TYPE_PHD_THESIS_0 = "publication.type.phd-thesis"; // PhD thesis
    public static final String PUB_TYPE_MASTER_THESIS_0 = "publication.type.master-thesis"; // Master thesis
    // Publication types in group: Part of book or report
    public static final String PUB_TYPE_CHAPTER_OR_ARTICLE_IN_BOOK_OR_REPORT_0 = "publication.type.chapter-or-article-in-book-or-report"; // Chapter or article in book or report
    public static final String PUB_TYPE_OTHER_PART_OF_BOOK_OR_REPORT_0 = "publication.type.other-part-of-book-or-report"; // Other part of book or report
    // Publication types in group: Other
    public static final String PUB_TYPE_OTHER_0 = "publication.type.other"; // Other
    //*/
    
    /** The key prefix for publication states. */
    public static final String PUB_STATE_PREFIX_0 = "publication.state.";
    /** The key for publication state "submitted". */
    public static final String PUB_STATE_SUBMITTED_0 = PUB_STATE_PREFIX_0 + "submitted";
    /** The key for publication state "accepted". */
    public static final String PUB_STATE_ACCEPTED_0 = PUB_STATE_PREFIX_0 + "accepted";
    /** The key for publication state "published". */
    public static final String PUB_STATE_PUBLISHED_0 = PUB_STATE_PREFIX_0 + "published";

    /** The key for publications. */
    public static final String PUB_0 = "publications";
    
    /** The key prefix for topics. */
    public static final String TOPIC_PREFIX_0 = "topic.";
    /** The key for topic "atmosphere". */
    public static final String TOPIC_ATMOSPHERE_0 = TOPIC_PREFIX_0 + "atmosphere";
    /** The key for topic "biodiversity". */
    public static final String TOPIC_BIODIVERSITY_0 = TOPIC_PREFIX_0 + "biodiversity";
    /** The key for topic "biology". */
    public static final String TOPIC_BIOLOGI_0 = TOPIC_PREFIX_0 + "biology";
    /** The key for topic "biogeochemistry". */
    public static final String TOPIC_BIOGEOCHEMISTRY_0 = TOPIC_PREFIX_0 + "biogeochemistry";
    /** The key for topic "chemistry". */
    public static final String TOPIC_CHEMISTRY_0 = TOPIC_PREFIX_0 + "chemistry";
    /** The key for topic "climate". */
    public static final String TOPIC_CLIMATE_0 = TOPIC_PREFIX_0 + "climate";
    /** The key for topic "conservation". */
    public static final String TOPIC_CONSERVATION_0 = TOPIC_PREFIX_0 + "conservation";
    /** The key for topic "ecology". */
    public static final String TOPIC_ECOLOGY_0 = TOPIC_PREFIX_0 + "ecology";
    /** The key for topic "ecotoxicology". */
    public static final String TOPIC_ECOTOXICOLOGY_0 = TOPIC_PREFIX_0 + "ecotoxicology";
    /** The key for topic "environment". */
    public static final String TOPIC_ENVIRONMENT_0 = TOPIC_PREFIX_0 + "environment";
    /** The key for topic "geophysics". */
    public static final String TOPIC_GEOPHYSICS_0 = TOPIC_PREFIX_0 + "geophysics";
    /** The key for topic "geology". */
    public static final String TOPIC_GEOLOGY_0 = TOPIC_PREFIX_0 + "geology";
    /** The key for topic "glaciology". */
    public static final String TOPIC_GLACIOLOGY_0 = TOPIC_PREFIX_0 + "glaciology";
    /** The key for topic "history". */
    public static final String TOPIC_HISTORY_0 = TOPIC_PREFIX_0 + "history";
    /** The key for topic "the human dimension". */
    public static final String TOPIC_HUMAN_DIMENSION_0 = TOPIC_PREFIX_0 + "human-dimension";
    /** The key for topic "management". */
    public static final String TOPIC_MANAGEMENT_0 = TOPIC_PREFIX_0 + "management";
    /** The key for topic "marine". */
    public static final String TOPIC_MARINE_0 = TOPIC_PREFIX_0 + "marine";
    /** The key for topic "maps". */
    public static final String TOPIC_MAPS_0 = TOPIC_PREFIX_0 + "maps";
    /** The key for topic "oceanography". */
    public static final String TOPIC_OCEANOGRAPHY_0 = TOPIC_PREFIX_0 + "oceanography";
    /** The key for topic "other". */
    public static final String TOPIC_OTHER_0 = TOPIC_PREFIX_0 + "other";
    /** The key for topic "paleoclimate". */
    public static final String TOPIC_PALEOCLIMATE_0 = TOPIC_PREFIX_0 + "paleoclimate";
    /** The key for topic "remote sensing". */
    public static final String TOPIC_REMOTE_SENSING_0 = TOPIC_PREFIX_0 + "remote-sensing";
    /** The key for topic "sea ice". */
    public static final String TOPIC_SEA_ICE_0 = TOPIC_PREFIX_0 + "seaice";
    /** The key for topic "snow". */
    public static final String TOPIC_SNOW_0 = TOPIC_PREFIX_0 + "snow";
    /** The key for topic "terrestrial". */
    public static final String TOPIC_TERRESTRIAL_0 = TOPIC_PREFIX_0 + "terrestrial";
    /** The key for topic "topography". */
    public static final String TOPIC_TOPOGRAPHY_0 = TOPIC_PREFIX_0 + "topography";
    /** The key for topic "vegetation". */
    public static final String TOPIC_VEGETATION_0 = TOPIC_PREFIX_0 + "vegetation";
    
    /** The key prefix for programmes. */
    public static final String PROGRAMME_PREFIX_0 = "programme.";
    
    /** The key prefix for areas. */
    public static final String AREA_PREFIX_0 = "area.";
    /** The key for area "Svalbard". */
    public static final String AREA_SVALBARD_0 = AREA_PREFIX_0 + "svalbard";
    /** The key for area "The Antarctic". */
    public static final String AREA_ANTARCTIC_0 = AREA_PREFIX_0 + "antarctic";
    /** The key for area "The Fram Strait". */
    public static final String AREA_FRAM_STRAIT_0 = AREA_PREFIX_0 + "framstredet";
    /** The key for area "The Arctic". */
    public static final String AREA_ARCTIC_0 = AREA_PREFIX_0 + "arctic";

    /** The key prefix for (project) states. */
    public static final String STATE_PREFIX_0 = "state.";
    /** The key for (project) state "completed". */
    public static final String STATE_COMPLETED_0 = STATE_PREFIX_0 + "completed";
    /** The key for (project) state "ongoing". */
    public static final String STATE_ONGOING_0 = STATE_PREFIX_0 + "ongoing";
    /** The key for (project) state "cancelled". */
    public static final String STATE_CANCELLED_0 = STATE_PREFIX_0 + "cancelled";
    /** The key for (project) state "planned". */
    public static final String STATE_PLANNED_0 = STATE_PREFIX_0 + "planned";

    /** The key prefix for (project) types. */
    public static final String TYPE_PREFIX_0 = "type.";
    /** The key for (project) type "research". */
    public static final String TYPE_RESEARCH_0 = TYPE_PREFIX_0 + "research";
    /** The key for (project) type "education". */
    public static final String TYPE_EDUCATION_0 = TYPE_PREFIX_0 + "education";
    /** The key for (project) type "monitoring". */
    public static final String TYPE_MONITORING_0 = TYPE_PREFIX_0 + "monitoring";
    /** The key for (project) type "modelling". */
    public static final String TYPE_MODELLING_0 = TYPE_PREFIX_0 + "modeling";
    /** The key for (project) type "mapping". */
    public static final String TYPE_MAPPING_0 = TYPE_PREFIX_0 + "mapping";
    
    /**  */
    public static final String DATA_COUNTRIES_0 = "DATA_COUNTRIES_0";
    /**  */
    public static final String DATA_DB_VALUES_0 = "DATA_DB_VALUES_0";
    
    public static final String LABEL_DEFAULT_PROCEEDINGS_JOURNAL_0 = "LABEL_DEFAULT_PROCEEDINGS_JOURNAL_0";
    public static final String LABEL_DEFAULT_TITLE_0 = "LABEL_DEFAULT_TITLE_0";
    public static final String LABEL_DEFAULT_NAME_0 = "LABEL_DEFAULT_NAME_0";
    
    public static final String LABEL_TRANSLATED_BY_0 = "publication.label.translatedby";
    public static final String LABEL_TRANSLATED_TO_0 = "publication.label.translatedto";
    public static final String LABEL_BY_0 = "publication.label.by";
    
    /** The key prefix for organizational units. */
    public static final String ORG_PREFIX_0 = "org.";
    public static final String ORG_COMM_0 = "ORG_COMM_0";
    public static final String ORG_COMM_INFO_0 = "ORG_COMM_INFO_0";
    public static final String ORG_ADM_0 = "ORG_ADM_0";
    public static final String ORG_ADM_ECONOMICS_0 = "ORG_ADM_ECONOMICS_0";
    public static final String ORG_ADM_HR_0 = "ORG_ADM_HR_0";
    public static final String ORG_ADM_SENIOR_0 = "ORG_ADM_SENIOR_0";
    public static final String ORG_ADM_ICT_0 = "ORG_ADM_ICT_0";
    public static final String ORG_LEADER_0 = "ORG_LEADER_0";
    public static final String ORG_RESEARCH_0 = "ORG_RESEARCH_0";
    public static final String ORG_RESEARCH_BIODIV_0 = "ORG_RESEARCH_BIODIV_0";
    public static final String ORG_RESEARCH_GEO_0 = "ORG_RESEARCH_GEO_0";
    public static final String ORG_RESEARCH_MARINE_CRYO_0 = "ORG_RESEARCH_MARINE_CRYO_0";
    public static final String ORG_RESEARCH_ICE_0 = "ORG_RESEARCH_ICE_0";
    public static final String ORG_RESEARCH_ICE_N_ICE_0 = "ORG_RESEARCH_ICE_N_ICE_0";
    public static final String ORG_RESEARCH_ICE_ANTARCTICA_0 = "ORG_RESEARCH_ICE_ANTARCTICA_0";
    public static final String ORG_RESEARCH_ICE_FIMBUL_0 = "ORG_RESEARCH_ICE_FIMBUL_0";
    public static final String ORG_RESEARCH_ICE_ECOSYSTEMS_0 = "ORG_RESEARCH_ICE_ECOSYSTEMS_0";
    public static final String ORG_RESEARCH_ICE_SEA_ICE_0 = "ORG_RESEARCH_ICE_SEA_ICE_0";
    public static final String ORG_RESEARCH_ECOTOX_0 = "ORG_RESEARCH_ECOTOX_0";
    public static final String ORG_RESEARCH_SUPPORT_0 = "ORG_RESEARCH_SUPPORT_0";
    public static final String ORG_ENVMAP_0 = "ORG_ENVMAP_0";
    public static final String ORG_ENVMAP_DATA_0 = "ORG_ENVMAP_DATA_0";
    public static final String ORG_ENVMAP_MANAGEMENT_0 = "ORG_ENVMAP_MANAGEMENT_0";
    public static final String ORG_ENVMAP_MAP_0 = "ORG_ENVMAP_MAP_0";
    public static final String ORG_OL_0 = "ORG_OL_0";
    public static final String ORG_OL_ANTARCTIC_0 = "ORG_OL_ANTARCTIC_0";
    public static final String ORG_OL_ARCTIC_0 = "ORG_OL_ARCTIC_0";
    public static final String ORG_OL_LYR_0 = "ORG_OL_LYR_0";
    public static final String ORG_OTHER_0 = "ORG_OTHER_0";
    public static final String ORG_OTHER_AC_0 = "ORG_OTHER_AC_0";
    public static final String ORG_OTHER_CLIC_0 = "ORG_OTHER_CLIC_0";
    public static final String ORG_OTHER_NA2011_0 = "ORG_OTHER_NA2011_0";
    public static final String ORG_OTHER_NYSMAC_0 = "ORG_OTHER_NYSMAC_0";
    public static final String ORG_OTHER_SSF_0 = "ORG_OTHER_SSF_0";
    
    /** The key prefix for facets. */
    public static final String FACET_PREFIX_0 = "facet.";
    
    /**
     * Default constructor. Does nothing.
     */
    public Labels() {}
    
    /**
     * Gets the label key for a facet field identifier.
     * <p>
     * The service API will provide facet field identifiers like e.g. "programme",
     * "category", etc. This method takes that identifier and returns the corresponding 
     * key, e.g. "facet.programme", so a human-readable, localized translation 
     * can be determined.
     * 
     * @param facetField The facet field identifier, as returned by the service.
     * @return The facet field name's corresponding key.
     */
    public static String labelFacetField(String facetField) {
        return FACET_PREFIX_0.concat(facetField);
    }
    /**
     * Gets the label key for an organizational unit identifier.
     * <p>
     * The service API will provide org. unit identifiers like e.g. "mika",
     * "geo", etc. This method takes that identifier and returns the corresponding 
     * key, e.g. "org.mika", so a human-readable, localized translation can be 
     * determined.
     * 
     * @param orgName The organizational unit identifier, as returned by the service.
     * @return The organizational unit identifier's corresponding key.
     */
    public static String labelOrgName(String orgName) {
        return ORG_PREFIX_0.concat(orgName);
    }
    /**
     * Gets the label key for a topic identifier.
     * <p>
     * The service API will provide topic identifiers like e.g. "geology",
     * "seaice", etc. This method takes that identifier and returns the corresponding 
     * key, e.g. "topic.geology", so a human-readable, localized translation 
     * can be determined.
     * 
     * @param topic The topic identifier, as returned by the service.
     * @return The topic identifier's corresponding key.
     */
    public static String labelTopic(String topic) {
        return TOPIC_PREFIX_0.concat(topic);
    }
    
    /**
     * Gets the label key for a publication type identifier.
     * <p>
     * The service API will provide publication type identifiers like e.g. 
     * "book", "report", etc. This method takes that identifier and returns the 
     * corresponding key, e.g. "publication.type.book", so a human-readable, 
     * localized translation can be determined.
     * 
     * @param pubType The publication type identifier, as returned by the service.
     * @return The publication type identifier's corresponding key.
     */
    public static String labelPubType(String pubType) {
        return PUB_TYPE_PREFIX_0.concat(pubType);
    }
    
    /**
     * Gets the label key for a programme name.
     * <p>
     * The API service will provide (in facets etc.) programme names like e.g.
     * "Oceans and sea ice", "Biodiversity" or "ICE Fluxes". This method takes 
     * that name and returns the corresponding key, e.g. 
     * "programme.oceans-and-sea-ice", so a human-readable, localized 
     * translation can be determined.
     * 
     * @param programmeName The programme name, as returned by the service.
     * @return The programme name's corresponding key.
     */
    public static String labelProgramme(String programmeName) {
        return PROGRAMME_PREFIX_0.concat(normalizeServiceString(programmeName));
    }
    
    /**
     * Gets the bundle name.
     * 
     * @return The bundle name.
     */
    public static String getBundleName() { return Labels.class.getCanonicalName(); }
    
    /** 
     * Swap the given value (retrieved from the service) with a "normalized" value.
     * Gets a "normalized" (ready-to-use by this class) string, based on the 
     * given string, which should be as it was returned by the service.
     * <p>
     * This method is here to keep strings (especially keys) used in the client 
     * consistent, while the strings used by the service remains inconsistent. 
     * <p>
     * Any given string will be converted to lower-case, trimmed, and all spaces 
     * will be replaced with hyphens (-); e.g.: "My string" -> "my-string".
     * <p>
     * Currently, these service strings (left) will also be replaced (right):
     * <ul>
     * <li>publication_type - publication.type</li>
     * <li>topics - topic</li>
     * <li>research_stations - research.station</li>
     * </ul>
     */
    public static String normalizeServiceString(String serviceString) {
        if (serviceString == null || serviceString.isEmpty())
            return serviceString;
        
        if (norm.isEmpty()) {
            norm.put("publication_type", "publication.type");
            norm.put("topics", "topic");
            norm.put("research_stations", "research.station");
        }
        String s = norm.get(serviceString);
        if (s == null || s.isEmpty())
            return serviceString.toLowerCase().replaceAll("\\s", "-").trim();
        return s;
    }
    
    /*public static String get(String str, Locale locale) {
        String s = str;
        
            try {
                ResourceBundle labels = ResourceBundle.getBundle(getBundleName(), locale);
                s = labels.getString(Labels.normalizeServiceString(s));
            } catch (Exception e) {
                // whut?
            }
        return s;
    }*/
}
