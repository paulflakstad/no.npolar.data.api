package no.npolar.data.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Wrapper class for a list of search filter sets.
 * <p>
 * The wrapper was implemented to make sorting and getting individual filter 
 * sets by name easier.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class SearchFilterSets extends ArrayList<SearchFilterSet> {
    /** Sort order: By title. */
    public static final int SORT_ORDER_TITLE = 1;
    /** Sort order: By relevancy. */
    public static final int SORT_ORDER_RELEVANCY = 2;
    
    /**
     * Removes a specific filter set, identified by the given name, from this list.
     * <p>
     * It is *possible* (though against reason) for names to be non-unique within
     * the list of filter sets. (I.e. two filter sets *could* have the same name.)
     * In that case, the first encountered filter set with the given name is 
     * removed.
     * 
     * @param name The name of the filter set.
     * @return True if this method removed anything, false if not.
     */
    public boolean removeByName(String name) {
        Iterator i = iterator();
        while (i.hasNext()) {
            SearchFilterSet sfs = null;
            try {
                Object o = i.next();
                if (o == null)
                    continue;
                sfs = (SearchFilterSet)o;
                if (sfs.getName().equals(name)) {
                    i.remove();
                    return true;
                }
            } catch (Exception e) {
                // Should NEVER happen
                return false;
            }
        }
        return false;
    }
    
    /**
     * Gets a specific filter set, identified by the given name, from this list.
     * <p>
     * It is *possible* (though against reason) for names to be non-unique within
     * the list of filter sets. (I.e. two filter sets *could* have the same name.)
     * In that case, the first encountered filter set with the given name is 
     * returned.
     * 
     * @param name The name of the filter set.
     * @return The filter set identified by the given name, or null if none.
     */
    public SearchFilterSet getByName(String name) {
        Iterator i = iterator();
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
     * <p>The (facet) name at index 0 in the array is given the highest 
     * relevancy, the name at index 1 the second highest, and so on.
     * <p>
     * Any names not mentioned in the given array will retain their existing 
     * relevancy.
     * 
     * @param namesInOrder The order description.
     * @return The list of filter sets, after the ordering modification.
     */
    public SearchFilterSets order(String[] namesInOrder) {
        int rel = 99;
        try {
            for (int i = 0; i < namesInOrder.length; i++) {
                try { 
                    //System.out.println("Trying to set relevancy=" + rel + " for filter set with name '" + namesInOrder[i] + "' ...");
                    getByName(namesInOrder[i]).setRelevancy(rel--);
                } catch (Exception e) {} // Exception = no match on that name
            }
        } catch (Exception e) {
            // ???
        }
        this.sort(SORT_ORDER_RELEVANCY);
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
            Collections.sort(this, SearchFilterSet.COMPARATOR_RELEVANCY);
        } else if (sortOrder == SORT_ORDER_TITLE) {
            Collections.sort(this, SearchFilterSet.COMPARATOR_TITLE);
        }
        return this;
    }
    
    /**
     * Sorts the list of filter sets using the given comparator.
     * 
     * @param comp The comparator to use in the sort operation.
     */
    public void sort(Comparator comp) {
        Collections.sort(this, comp);
    }
}
