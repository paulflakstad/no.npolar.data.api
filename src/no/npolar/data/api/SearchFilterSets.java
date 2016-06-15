package no.npolar.data.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;

/**
 * Container for a set of 0-N search filter sets.
 * <p>
 * More or less just a wrapper for the ArrayList that holds the SearchFilterSet 
 * instances, created to make sorting and getting individual filter sets by name 
 * easier.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class SearchFilterSets /*extends ArrayList<SearchFilterSet>*/ {
    /** Sort order: By title. */
    public static final int SORT_ORDER_TITLE = 1;
    /** Sort order: By relevancy. */
    public static final int SORT_ORDER_RELEVANCY = 2;
    
    private List<SearchFilterSet> sets = null;
    
    public SearchFilterSets() {
        sets = new ArrayList<SearchFilterSet>();
    }
    
    public SearchFilterSets add(SearchFilterSet set) {
        try { sets.add(set); } catch (Exception e) {}
        return this;
    }
    
    public SearchFilterSets remove(SearchFilterSet set) {
        try { sets.remove(set); } catch (Exception e) {}
        return this;
    }
    
    public SearchFilterSet get(int index) {
        return sets.get(index);
    }
    
    public int size() {
        return sets.size();
    }
    
    public List<SearchFilterSet> get() {
        return sets;
    }
    
    public Iterator<SearchFilterSet> iterator() {
        return sets.iterator();
    }
    
    public boolean isEmpty() {
        return sets.isEmpty();
    }
    
    /**
     * Removes a specific filter set, identified by the given name, from this list.
     * <p>
     * It is possible (though against reason) for names to be non-unique within
     * the list of filter sets. (I.e. two filter sets could have the same name.)
     * In that case, the first encountered filter set with the given name is 
     * removed.
     * 
     * @param name The name of the filter set.
     * @return The removed filter set, or null of nothing was removed.
     */
    public SearchFilterSet removeByName(String name) {
        Iterator<SearchFilterSet> i = sets.iterator();
        while (i.hasNext()) {
            try {
                SearchFilterSet sfs = i.next();
                if (sfs == null)
                    continue;
                
                if (sfs.getName().equals(name)) {
                    i.remove();
                    return sfs;
                }
            } catch (Exception e) {
                // Should NEVER happen
                return null;
            }
        }
        return null;
    }
    
    /**
     * Gets a specific filter set, identified by the given name, from this list.
     * <p>
     * It is possible (though against reason) for names to be non-unique within
     * the list of filter sets. (I.e. two filter sets could have the same name.)
     * In that case, the first encountered filter set with the given name is 
     * returned.
     * 
     * @param name The name of the filter set.
     * @return The filter set identified by the given name, or null if none.
     */
    public SearchFilterSet getByName(String name) {
        Iterator i = sets.iterator();
        while (i.hasNext()) {
            SearchFilterSet sfs = null;
            try {
                Object o = i.next();
                if (o == null)
                    continue;
                sfs = (SearchFilterSet)o;
                if (sfs.getName().equals(name)) {
                    return sfs;
                }
            } catch (Exception e) {
                // Should NEVER happen
                return null;
            }
        }
        return null;
    }
    
    /**
     * Orders this list of filter sets according to the given array which 
     * describes the order.
     * <p>
     * The (facet) name at index 0 in the array is given the highest 
     * relevancy, the name at index 1 the second highest, and so on.
     * <p>
     * Any names not mentioned in the given array will retain their existing 
     * relevancy.
     * 
     * @param namesInOrder The order description.
     * @return The list of filter sets, after the ordering modification.
     */
    public SearchFilterSets order(String ... namesInOrder) {
        int rel = 99;
        try {
            for (String name : namesInOrder) {
            //for (int i = 0; i < namesInOrder.length; i++) {
                try { 
                    //System.out.println("Trying to set relevancy=" + rel + " for filter set with name '" + namesInOrder[i] + "' ...");
                    getByName(name).setRelevancy(rel--);
                    //getByName(namesInOrder[i]).setRelevancy(rel--);
                } catch (Exception e) {} // Exception = no match on that name
            }
        } catch (Exception e) {
            // ???
        }
        sort(SORT_ORDER_RELEVANCY);
        return this;
    }
    
    /**
     * Sorts the list of filter sets according to the given sort order, which 
     * should be one of the SORT_ORDER_XXX constants of this class.
     * 
     * @param sortOrder The sort order.
     * @return This instance, updated.
     */
    public SearchFilterSets sort(int sortOrder) {
        if (sortOrder == SORT_ORDER_RELEVANCY) {
            Collections.sort(sets, SearchFilterSet.COMPARATOR_RELEVANCY);
        } else if (sortOrder == SORT_ORDER_TITLE) {
            Collections.sort(sets, SearchFilterSet.COMPARATOR_TITLE);
        }
        return this;
    }
    
    /**
     * Sorts the list of filter sets using the given comparator.
     * 
     * @param comp The comparator to use in the sort operation.
     */
    public void sort(Comparator comp) {
        Collections.sort(sets, comp);
    }
    
    public String toHtml(String togglerText, CmsJspActionElement cms, ResourceBundle labels) {
        String s = "";
        try {
            s += "<div class=\"search-widget search-widget--filters\">" 
                + "<a class=\"cta cta--filters-toggle\" tabindex=\"0\">" + togglerText + "</a>"
                + "<div class=\"filters-wrapper\">";
                    
                    if (!this.isEmpty()) {
                        Iterator<SearchFilterSet> iFilterSets = this.iterator();
                        s += "<div class=\"layout-group quadruple layout-group--quadruple\">";
                        //s += "<div class=\"boxes\">";
                        while (iFilterSets.hasNext()) {
                            SearchFilterSet filterSet = iFilterSets.next();
                            List<SearchFilter> filters = filterSet.getFilters();
                            
                            if (filters != null) {
                                s += "<div class=\"layout-box filter-set\">";
                                s += "<h3 class=\"filters-heading filter-set__heading\">";
                                s += filterSet.getTitle(cms.getRequestContext().getLocale());
                                s += "<span class=\"filter__num-matches\"> (" + filterSet.size() + ")</span>";
                                s += "</h3>";
                                s += "<ul class=\"filter-set__filters\">";
                                try {
                                    // Iterate through the filters in this set
                                    Iterator<SearchFilter> iFilters = filters.iterator();
                                    while (iFilters.hasNext()) {
                                        SearchFilter filter = iFilters.next();
                                        // The visible filter text (initialize this as the term)
                                        String filterText = filter.getTerm();
                                        
                                        // Try to fetch a better (and localized) text for the filter
                                        try {
                                            filterText = labels.getString( filterSet.labelKeyFor(filter) );
                                        } catch (Exception skip) {}
                                        
                                        // The filter
                                        s += "<li><a href=\"" + cms.link(cms.getRequestContext().getUri() + "?" + CmsStringUtil.escapeHtml(filter.getUrlPartParameters())) + "\""
                                                            + " class=\"filter" + (filter.isActive() ? " filter--active" : "") + "\""
                                                            + ">" 
                                                            //+ (filter.isActive() ? "<span style=\"background:red; border-radius:3px; color:white; padding:0 0.3em;\" class=\"remove-filter\">X</span> " : "")
                                                            + filterText
                                                            + "<span class=\"filter__num-matches\"> (" + filter.getCount() + ")</span>"
                                                        + "</a></li>";
                                    }
                                } catch (Exception filterE) {
                                    s += "<!-- " + filterE.getMessage() + " -->";
                                }
                                s += "</ul>";
                                s += "</div>";
                            }
                        }
                        //s += "</div>";
                        s += "</div>";
                    }
                    
                s += "</div>";
            s += "</div>";
        } catch (Exception e) {
            s += "<!-- Error constructing filters: " + e.getMessage() + " -->";
        }
        return s;
    }
}
