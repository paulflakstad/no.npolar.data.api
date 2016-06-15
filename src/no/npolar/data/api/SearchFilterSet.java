package no.npolar.data.api;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Wrapper class for a search filter set.
 * <p>
 * Search filter sets are client representations of facets. Each search filter 
 * set corresponds to one entry in the field "facets" in service API responses.
 * <p>
 * A search filter set is basically a named (e.g. "category") list of filters, 
 * with additional features like: 
 * <ul>
 * <li>relevancy weight (used for ordering multiple filter sets),</li> 
 * <li>comparators (used for sorting)</li>
 * <li>localization properties and methods</li>
 * </ul>
 * 
 * Some additional
 * @see SearchFilter
 * @see SearchFilterSets
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class SearchFilterSet {
    protected List<SearchFilter> filters = null;
    protected Locale locale = null;
    private String name = null;
    private String title = null;
    private int relevancyWeight = 1;
    
    /** Comparator to use for sorting search filter sets by title. */
    public static final Comparator<SearchFilterSet> COMPARATOR_TITLE =
            new Comparator<SearchFilterSet>() {
                @Override
                public int compare(SearchFilterSet obj1, SearchFilterSet obj2) {
                    return obj1.getTitle().compareTo(obj2.getTitle());
                }
            };
    /** Comparator to use for sorting search filter sets by relevancy. */
    public static final Comparator<SearchFilterSet> COMPARATOR_RELEVANCY =
            new Comparator<SearchFilterSet>() {
                @Override
                public int compare(SearchFilterSet obj1, SearchFilterSet obj2) {
                    try {
                        // Integer.compare() throws NoSuchMethodException (at least on some Java versions)
                        //return Integer.compare(obj2.getRelevancy(), obj1.getRelevancy());
                        if (obj2.getRelevancy() > obj1.getRelevancy()) {
                            return 1;
                        } else if (obj2.getRelevancy() < obj1.getRelevancy()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    } catch (Exception e) { 
                        return 0;
                    }
                    /*if (obj1.getRelevancy() > obj2.getRelevancy())
                        return -1;
                    else if (obj1.getRelevancy() < obj2.getRelevancy())
                        return 1;
                    return 0;*/
                }
            };
    
    /**
     * Constructs a new filter set with the given name.
     * <p>
     * The expected name is the facet identifier, as returned from the service. 
     * (E.g.: "topics" or "published-year_sort")
     * 
     * @param name The name; should be the facet identifier.
     */
    public SearchFilterSet(String name) {
        this.name = name;
        this.filters = new ArrayList<SearchFilter>();
    }
    
    /**
     * Constructs a new filter set with the given name and locale.
     * 
     * @param name The name; typically the facet name, as returned from the service.
     * @param locale The locale to use in translations.
     */
    public SearchFilterSet(String name, Locale locale) {
        this(name);
        this.locale = locale;
    }
    
    /**
     * Gets the filters in this filter set.
     * 
     * @return The filters in this filter set.
     */
    public List<SearchFilter> getFilters() {
        return this.filters;
    }
    
    /**
     * Gets the label key for the given filter.
     * <p>
     * E.g. If this filter set is for topics ("topic") and the given filter is 
     * for atmosphere ("atmosphere"), then the resulting key would be 
     * "topic.atmosphere".
     * <p>
     * The returned key can then be used to retrieve a human-readable, localized 
     * string (via a resource bundle). See the {@link Labels} class and the
     * .properties files in this package for more info.
     * <p>
     * Example usage:
     * <pre>
     *  String labelKey = mySearchFilterSet.labelKeyFor( mySearchFilter );
     *  String niceFilterText = myLabels.getString( labelKey ); // myLabels is a ResourceBundle
     * </pre>
     * 
     * @param filter The filter to create a label key for.
     * @return The label key for the given filter.
     * @see Labels
     */
    public String labelKeyFor(SearchFilter filter) {
        String s = this.getName().concat(".").concat(filter.getTerm());
        try {
            s = Labels.normalizeServiceString(this.getName()).concat(".").concat(Labels.normalizeServiceString(filter.getTerm()));
        } catch (Exception e) {
            // uh-oh
        }
        return s;
    }
    
    /**
     * Gets the name of this filter set.
     * <p>
     * The name should be identical to the facet identifier provided by the API. 
     * (E.g.: "topics" or "year-published_sort").
     * 
     * @return The filter set's name.
     */
    public String getName() {
        return this.name;
    }
    
    
    /**
     * Gets the name of this filter set, "normalized".
     * 
     * @see SearchFilterSet#getName(boolean) 
     */
    /*public String getName() {
        return this.getName(true);
    }*/
    
    /**
     * Gets the name of this filter set. The name should be identical to the 
     * facet identifier provided by the API. (E.g.: "topics" or 
     * "year-published_sort").
     * 
     * @param normalize Whether or not to "normalize" the name.
     * @return The filter set's name, possibly "normalized".
     * @see Labels#normalizeServiceString(java.lang.String) 
     */
    /*public String getName(boolean normalize) {
        String s = this.name;
        
        if (normalize) {
            try {
                ResourceBundle labels = ResourceBundle.getBundle(Labels.getBundleName(), this.getLocale());
                s = labels.getString(Labels.normalizeServiceString(this.name));
            } catch (Exception e) {
                // whut?
            }
        }
        return s;
    }*/
    
    /**
     * Gets the title of this filter set, overriding any already set locale.
     * 
     * @param locale The preferred locale.
     * @return The filter set's title.
     * @see SearchFilterSet#getTitle() 
     */
    public String getTitle(Locale locale) {
        try {
            if (title == null) { // Don't re-evaluate the title
                ResourceBundle labels = ResourceBundle.getBundle(Labels.getBundleName(), locale);
                try {
                    title = labels.getString(Labels.labelFacetField(name));
                    return title;
                } catch (Exception e) {
                    // No match, is there a label defined without the "facet." prefix?
                    try { 
                        title = labels.getString(Labels.normalizeServiceString(name)); 
                        return title;
                    } catch (Exception ee) {}
                }
            }
            return title;
        } catch (Exception e) {
            //System.out.println("Error translating facet name '" + name + "': " + e.getMessage());
        }
        // Fallback to name
        return this.name;
    }
    /**
     * Gets the title using the set locale.
     * <p>
     * If no locale is set, falls back to the 
     * {@link APIService#DEFAULT_LOCALE_NAME default locale}. For details, see 
     * {@link SearchFilterSet#getTitle(java.util.Locale)}.
     * <p>
     * The returned string is the "translation" of the (facet) name, if such a 
     * translation exists in the {@link Labels} of this package. If not, the 
     * (facet) name is returned.
     * 
     * @return The filter set's title.
     */
    public String getTitle() {
        return getTitle(getLocale());
    }
    
    /**
     * Gets the locale this filter set uses for translations.
     * <p>
     * If no locale has been explicitly set, the filter set will fallback to the 
     * {@link APIService#DEFAULT_LOCALE_NAME default locale}.
     * 
     * @return The locale this filter set uses for translations.
     */
    public Locale getLocale() {
        return this.locale == null ? this.locale = new Locale(APIService.DEFAULT_LOCALE_NAME) : this.locale;
    }
    
    /**
     * Gets the relevancy weight.
     * <p>
     * The relevancy weight is typically used for ordering filter sets, when 
     * many sets are to appear together.
     * <p>
     * If not modified, the filter set has relevancy weight = 1 (default).
     * 
     * @return The filter set's relevancy weight.
     */
    public int getRelevancy() {
        return this.relevancyWeight;
    }
    
    /**
     * Sets the relevancy weight.
     * <p>
     * The relevancy weight is typically used for ordering filter sets when 
     * many sets are to appear together.
     * <p>
     * If not modified, the filter set has relevancy weight = 1 (default).
     * 
     * @param weight The new relevancy weight.
     * @return The filter set, updated.
     */
    public SearchFilterSet setRelevancy(int weight) {
        this.relevancyWeight = weight;
        return this;
    }
    
    /**
     * Adds a filter to this set.
     * 
     * @param filter The filter to add.
     * @return The filter set, updated.
     * @see List#add(java.lang.Object) 
     */
    public SearchFilterSet add(SearchFilter filter) {
        this.filters.add(filter);
        return this;
    }
    
    /**
     * Adds a filter to this set, at the given index.
     * 
     * @param index The index at which to add the given filter.
     * @param filter The filter to add.
     * @return The filter set, updated.
     * @see List#add(int, java.lang.Object)
     */
    public SearchFilterSet add(int index, SearchFilter filter) {
        this.filters.add(index, filter);
        return this;
    }
    
    /**
     * Gets an iterator for the filters in this filter set.
     * 
     * @return an iterator for the filters in this filter set.
     * @see List#iterator() 
     */
    public Iterator<SearchFilter> iterator() {
        return this.filters.iterator();
    }
    
    /**
     * Gets the number of filters in this filter set.
     * 
     * @return the number of filters in this filter set.
     * @see List#size() 
     */
    public int size() {
        return this.filters.size();
    }
    
    /**
     * Adds all the given filters to this set.
     * 
     * @param filters The filters to add.
     * @return The filter set, updated.
     * @see List#addAll(java.util.Collection)  
     */
    public SearchFilterSet addAll(List<SearchFilter> filters) {
        this.filters.addAll(filters);
        return this;
    }
    
    /**
     * Removes the given filter from this filter set.
     * 
     * @param filter The filter to remove.
     * @return The filter set, updated.
     * @see List#remove(java.lang.Object) 
     */
    public SearchFilterSet remove(SearchFilter filter) {
        this.filters.remove(filter);
        return this;
    }
    
    /**
     * Removes all the given filters from this filter set.
     * 
     * @param filters The filters to remove.
     * @return The filter set, updated.
     * @see List#removeAll(java.util.Collection) 
     */
    public SearchFilterSet removeAll(List<SearchFilter> filters) {
        this.filters.removeAll(filters);
        return this;
    }
    
    /**
     * @see List#contains(java.lang.Object) 
     */
    public boolean contains(SearchFilter filter) {
        return this.filters.contains(filter);
    }
    
    /**
     * @see List#containsAll(java.util.Collection) 
     */
    public boolean containsAll(List<SearchFilter> filters) {
        return this.filters.containsAll(filters);
    }
    
    /**
     * @see List#isEmpty() 
     */
    public boolean isEmpty() {
        return this.filters.isEmpty();
    }
    
    /**
     * @see List#indexOf(java.lang.Object) 
     */
    public int indexOf(SearchFilter filter) {
        return this.filters.indexOf(filter);
    }
    
    /**
     * Clears this filter set.
     * 
     * @return The filter set, updated.
     * @see List#clear() 
     */
    public SearchFilterSet clear() {
        this.filters.clear();
        return this;
    }
    
    /**
     * Gets the filter at the given index of this filter set.
     * 
     * @param index The index to lookup.
     * @return the filter at the given index of this filter set.
     * @see List#get(int) 
     */
    public SearchFilter get(int index) {
        return this.filters.get(index);
    }
    
    /**
     * Gets a filter set that contains a sub-list of the filters in this filter 
     * set.
     * 
     * @param startIndex The start index.
     * @param endIndex The end index.
     * @return a filter set that contains a sub-list of the filters in this filter set.
     * @see List#subList(int, int) 
     */
    public SearchFilterSet subSet(int startIndex, int endIndex) {
        SearchFilterSet set = new SearchFilterSet(this.title);
        set.addAll(filters.subList(startIndex, endIndex));
        return set;
    }
    
    /**
     * Gets the filters in this filter set, as an array.
     * 
     * @return the filters in this filter set, as an array.
     * @see List#toArray() 
     */
    public SearchFilter[] toArray() {
        return (SearchFilter[])filters.toArray();
    }
}
