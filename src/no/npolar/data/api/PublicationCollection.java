package no.npolar.data.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Class for storing Publication objects, grouped by type.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class PublicationCollection {
    /** The list container, where all publications in this collection is stored. */
    private LinkedHashMap<String, ArrayList<Publication>> pubs = null;
    
    /** The pre-defined keyword for identifying peer-reviewed publications. */
    //public static final String PEER_REVIEWED = "peer-reviewed";
    /** The pre-defined keyword for identifying editorials. */
    //public static final String EDITORIAL = "editorial";
    /** The pre-defined keyword for identifying reviews. */
    //public static final String REVIEW = "review";
    /** The pre-defined keyword for identifying corrections. */
    //public static final String CORRECTION = "correction";
    /** The pre-defined keyword for identifying books. */
    //public static final String BOOK = "book";
    /** The pre-defined keyword for identifying maps. */
    //public static final String MAP = "map";
    /** The pre-defined keyword for identifying posters. */
    //public static final String POSTER = "poster";
    /** The pre-defined keyword for identifying reports. */
    //public static final String REPORT = "report";
    /** The pre-defined keyword for identifying abstracts. */
    //public static final String ABSTRACT = "abstract";
    /** The pre-defined keyword for identifying PhD theses. */
    //public static final String PHD = "phd";
    /** The pre-defined keyword for identifying Master theses. */
    //public static final String MASTER = "master";
    /** The pre-defined keyword for identifying proceedings. */
    //public static final String PROCEEDINGS = "proceedings";
    /** The pre-defined keyword for identifying popular science publications. */
    //public static final String POPULAR = "popular";
    /** The pre-defined keyword for identifying other publications. */
    //public static final String OTHER = "other";
    
    /** Order definition – dictates in what order Publications are stored and printed. */
    String[] order = { 
        Publication.TYPE_PEER_REVIEWED,
        Publication.TYPE_BOOK,
        Publication.TYPE_EDITORIAL,
        Publication.TYPE_REPORT,
        Publication.TYPE_MAP,
        Publication.TYPE_REVIEW,
        Publication.TYPE_PROCEEDINGS,
        Publication.TYPE_ABSTRACT,
        Publication.TYPE_CORRECTION,
        Publication.TYPE_PHD,
        Publication.TYPE_MASTER,
        Publication.TYPE_POSTER,
        Publication.TYPE_POPULAR,
        Publication.TYPE_OTHER
    };
    
    /**
     * Creates a new, empty publication collection.
     */
    /*public PublicationCollection() {
        pubs = new LinkedHashMap<String, ArrayList<Publication>>();
        for (int i = 0; i < order.length; i++) {
            pubs.put(order[i], new ArrayList<Publication>());
        }
    }*/
    
    /**
     * Creates a new publication collection containing the publications defined in the given JSON array.
     * @param publicationObjects An array of JSON objects, each of which describe a publication.
     * @param cms A reference CmsAgent, containing among other things the locale to use when generating strings for screen view.
     */
    /*public PublicationCollection(JSONArray publicationObjects, CmsAgent cms) throws InstantiationException {
        this();
        for (int i = 0; i < publicationObjects.length(); i++) {
            try {
                this.add(new Publication(publicationObjects.getJSONObject(i), cms));
            } catch (Exception e) {
                throw new InstantiationException("Error when trying to create publications list: " + e.getMessage());
            }
        }
    }*/
    
    /**
     * Gets a sub-list of this collection, which will contain only publications 
     * of the given type, or an empty list if no publications of that type are 
     * currently contained in this collection.
     * 
     * @param pubType The publication type name (see constants of {@link no.npolar.api.Publication} ).
     * @return All publications of the given type currently in this collection, or an empty list if none.
     */
    public ArrayList<Publication> getListByType(String pubType) {
        return this.pubs.get(pubType);
    }
    
    /**
     * Adds a publication to this collection.
     * 
     * @param p The publication to add.
     */
    public final void add(Publication p) {
        if (pubs.get(p.getType()) == null) // Should never happen, but anyway ...
            pubs.put(p.getType(), new ArrayList<Publication>());
        
        pubs.get(p.getType()).add(p);
    }
    
    /**
     * Indicates whether this collection is empty or not.
     * 
     * @return True if this collection is empty, false if not.
     */
    public boolean isEmpty() {
        return this.size() <= 0;
    }
    
    /**
     * Gets the publication types contained in this collection.
     * 
     * @return The publication types contained in this collection.
     */
    public Set<String> getTypesContained() {
        return pubs.keySet();
    }
    
    /**
     * Gets the total number of publications in this collection.
     * 
     * @return The total number of publications in this collection.
     */
    public int size() {
        int size = 0;
        Iterator<String> i = pubs.keySet().iterator();
        while (i.hasNext()) {
            size += pubs.get(i.next()).size();
        }
        return size;
    }
}
