package no.npolar.data.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Class for storing various types of Data Centre entries, grouped by type.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class GroupedCollection<T extends APIEntryInterface> {
    private T t;
    /** The list container, where all objects in this collection are stored. */
    private LinkedHashMap<String, ArrayList<T>> objects = null;
    
    /**
     * Creates a new, empty collection.
     */
    public GroupedCollection() throws InstantiationException {
        objects = new LinkedHashMap<String, ArrayList<T>>();
    }
    
    /**
     * Merges any and all (grouped) objects in this collection in a single list.
     * <p>
     * No ordering is applied, objects will appear firstly in the order of group 
     * and secondly in the in-group order.
     * 
     * @return A list containing all objects in this collection.
     * @see #merge(java.util.Comparator) 
     */
    public List<T> merge() {
        return merge(null);
    }
    
    /**
     * Merges any and all (grouped) objects in this collection in a single list,
     * ordered as defined by the given comparator.
     * 
     * @param c The comparator to use when ordering the list.
     * @return A list containing all objects in this collection, ordered by the given comparator.
     */
    public List<T> merge(Comparator<T> c) {
        List<T> merged = new ArrayList<T>();
        Iterator<String> i = getTypesContained().iterator();
        while (i.hasNext()) {
            try {
                String groupName = i.next();
                ArrayList<T> groupObjects = getListGroup(groupName);
                merged.addAll(groupObjects);
            } catch (Exception e) {
                // Ignore
            }
        }
        if (c != null) {
            try {
                Collections.sort(merged, c);
            } catch (Exception e) {
                // Ignore
            }
        }
        return merged;
    }
    
    /**
     * Sets the group order, as defined by the order in the given array.
     * <p> 
     * Each array value should be a valid group name.
     * 
     * @param order The group order.
     * @see APIObjectInterface#getGroupName() 
     */
    public void setOrder(String[] order) {
        for (int i = 0; i < order.length; i++) {
            objects.put(order[i], new ArrayList<T>());
        }
    }
    
    /**
     * Gets a sub-list of this collection, which will contain only objects 
     * assigned the group with the given name, or an empty list if no objects are 
     * currently assigned that group.
     * 
     * @param type The object group name (see {@link APIObjectInterface#getGroupName()} and "TYPE_" prefixed constants of implementing classes).
     * @return All objects assigned the group with the given name, or an empty list if none.
     */
    public ArrayList<T> getListGroup(String groupName) {
        return this.objects.get(groupName);
    }
    
    /**
     * Adds an object to this collection.
     * 
     * @param o The object to add.
     */
    public final void add(T t) {
        if (objects.get(t.getGroupName()) == null) // Should never happen, but anyway ...
            objects.put(t.getGroupName(), new ArrayList<T>());
        
        objects.get(t.getGroupName()).add(t);
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
     * Gets the object types contained in this collection.
     * 
     * @return The object types contained in this collection.
     */
    public Set<String> getTypesContained() {
        return objects.keySet();
    }
    
    /**
     * Gets the total number of objects in this collection.
     * 
     * @return The total number of objects in this collection.
     */
    public int size() {
        int size = 0;
        Iterator<String> i = objects.keySet().iterator();
        while (i.hasNext()) {
            size += objects.get(i.next()).size();
        }
        return size;
    }
}
