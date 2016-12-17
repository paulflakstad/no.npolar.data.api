package no.npolar.data.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONObject;

/**
 * Helper class for {@link Publication} contributors.
 * <p>
 * Typically, a contributor is a person, but may also be an institution or 
 * organization.
 * <p>
 * ToDo: Rename class to PublicationContributorCollection
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public class PersonCollection {
        
    /** The locale to use when generating strings meant for viewing. (Important especially for date formats etc.) */
    protected Locale displayLocale = null;
    
    /** Contains all contributors currently in this collection. */
    private List<PublicationContributor> contributors = null;

    /**
     * Creates a new, empty collection.
     * 
     * @param displayLocale The preferred language.
     */
    public PersonCollection(Locale displayLocale) {
        this.displayLocale = displayLocale;
        contributors = new ArrayList<PublicationContributor>(0);
    }

    /**
     * Creates a new collection from the given JSON array.
     * 
     * @param contributorsArr A JSON array of JSON objects describing contributors.
     * @param displayLocale The preferred language.
     */
    public PersonCollection(JSONArray contributorsArr, Locale displayLocale) {
        this.displayLocale = displayLocale;
        contributors = new ArrayList<PublicationContributor>(0);
        for (int i = 0; i < contributorsArr.length(); i++) {
            try {
                JSONObject contributor = contributorsArr.getJSONObject(i);
                add(contributor);
            } catch (Exception e) {
                continue; // For clarity
            }
        }
    }

    /**
     * Adds a contributor to this collection of contributors.
     * 
     * @param contributorObj The JSON object that describes the contributor.
     * @return The list of contributors in this collection, after the given contributor has been added.
     */
    public final List<PublicationContributor> add(JSONObject contributorObj) {
        PublicationContributor contributor = new PublicationContributor(contributorObj, displayLocale);
        List<String> roles = contributor.getRoles();
        PublicationContributor existing = getByName(contributor.getFirstName(), contributor.getLastName());
        if (existing != null) {
            existing.addRoles(roles);
        } else {
            contributors.add(contributor);
        }
        return contributors;
    }

    /**
     * Removes a contributor from this collection.
     * @param contributor The contributor to remove.
     * @see List#remove(java.lang.Object) 
     * @return true if the collection did contain the given contributor, false if not.
     */
    public boolean remove(PublicationContributor contributor) {
        return contributors.remove(contributor);
    }

    /**
     * Removes a subset from this collection.
     * @param peopleToRemove The subset to remove.
     * @see List#removeAll(java.util.Collection) 
     * @return true if this collection changed as a result of this call, false if not.
     */
    public boolean removeAll(List<PublicationContributor> peopleToRemove) {
        return contributors.removeAll(peopleToRemove);
    }

    /**
     * Removes a subset from this collection.
     * @param collectionToRemove The subset to remove.
     * @see #removeAll(java.util.List) 
     * @return true if this collection changed as a result of this call, false if not.
     */
    public boolean removeAll(PersonCollection collectionToRemove) {
        return removeAll(collectionToRemove.get());
    }

    /**
     * Checks if this collection contains the given publication contributor.
     * @param person The publication contributor to look for.
     * @see List#contains(java.lang.Object) 
     * @return true if this collection contains the given publication contributor, false if not. 
     */
    public boolean contains(PublicationContributor person) {
        return contributors.contains(person);
    }

    /**
     * Gets all contributors with the given role currently in this collection.
     * 
     * @param role The role to match against.
     * @return All contributors with the given role currently in this collection, or an empty list if none.
     */
    public List<PublicationContributor> getByRole(String role) {
        List<PublicationContributor> temp = new ArrayList<PublicationContributor>(0);
        Iterator<PublicationContributor> i = contributors.iterator();
        while (i.hasNext()) {
            PublicationContributor contributor = i.next();
            if (contributor.hasRole(role))
                temp.add(contributor);
        }
        return temp;
    }

    /**
     * Gets all contributors with <strong>only</strong> the given role currently 
     * in this collection.
     * 
     * @param role The role to match against.
     * @return All contributors with only the given role currently in this collection, or an empty list if none.
     */
    public List<PublicationContributor> getByRoleOnly(String role) {
        List<PublicationContributor> temp = new ArrayList<PublicationContributor>(0);
        Iterator<PublicationContributor> i = contributors.iterator();
        while (i.hasNext()) {
            PublicationContributor contributor = i.next();
            if (contributor.hasRoleOnly(role))
                temp.add(contributor);
        }
        return temp;
    }

    /**
     * Gets a contributor identified by the given name from this collection, if 
     * any.
     * 
     * @param fName The first name (given name) of the contributor to get.
     * @param lName The last name (family name) of the contributor to get.
     * @return The contributor identified by the given name, or null if none.
     */
    public PublicationContributor getByName(String fName, String lName) {
        if (fName == null || lName == null || fName.isEmpty() || lName.isEmpty())
            return null;

        Iterator<PublicationContributor> i = contributors.iterator();
        while (i.hasNext()) {
            PublicationContributor contributor = i.next();
            if (contributor.getFirstName().equals(fName) && contributor.getLastName().equals(lName))
                return contributor;
        }
        return null;
    }

    /**
     * Gets a contributor identified by the given ID from this collection, if any.
     * 
     * @param id The ID used to identify the contributor to get.
     * @return The contributor identified by the given ID, or null if none.
     */
    public PublicationContributor getByID(String id) {
        if (id == null || id.isEmpty())
            return null;

        Iterator<PublicationContributor> i = contributors.iterator();
        while (i.hasNext()) {
            PublicationContributor contributor = i.next();
            if (contributor.getID().equals(id))
                return contributor;
        }
        return null;
    }

    /**
     * Gets the list of contributors currently in this collection.
     * 
     * @return The list of contributors currently in this collection.
     */
    public List<PublicationContributor> get() { return contributors; }

    /**
     * Checks if all the contributors in this collection are editors (and 
     * editors only).
     * 
     * @return True if all the contributors in this collection are editors, false if not.
     * @see #containsRoleOnly(java.lang.String)
     */
    public boolean containsEditorsOnly() {
        return containsRoleOnly(Publication.Val.ROLE_EDITOR);
    }

    /**
     * Checks if all the contributors in this collection are translators (and 
     * translators only).
     * 
     * @return True if all the contributors in this collection are translators, false if not.
     * @see #containsRoleOnly(java.lang.String)
     */
    public boolean containsTranslatorsOnly() {
        return containsRoleOnly(Publication.Val.ROLE_TRANSLATOR);
    }

    /**
     * Checks if all the contributors in this collection are assigned only the 
     * given role, and no other role.
     * 
     * @param role The role to check for.
     * @return True if all contributors are assigned only the given role, false if not.
     * @see #JSON_VAL_ROLE_EDITOR and alike.
     */
    public boolean containsRoleOnly(String role) {
        Iterator<PublicationContributor> i = contributors.iterator();
        while (i.hasNext()) {
            PublicationContributor p = i.next();
            if (!p.hasRoleOnly(role))
                return false;
        }
        return true;
    }
}
