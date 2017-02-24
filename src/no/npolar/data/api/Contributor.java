package no.npolar.data.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import no.npolar.data.api.util.APIUtil;

/**
 * A contributor is typically an author, but can have other roles as well.
 * <p>
 * This class is also the base class for others, e.g. 
 * {@link PublicationContributor}.
 * <p>
 * Note that a contributor is not necessarily a person - organisations can also 
 * be contributors.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Contributor implements Comparable<Contributor> {
    
    /** The ID, like "john.doe" or "npolar.no". */
    protected String id = null;
    
    /** The (localized) name, like "John Doe" or "Norwegian Polar Institute". */
    protected String name = null;
    
    /** The last name, like "Doe". */
    protected String lastName = null;
    
    /** The first name, like "John". */
    protected String firstName = null;
    
    /** The organisation name(s), like "Norwegian Polar Institute". */
    protected String organisation = null;
    
    /** Holds the contributor's roles. */
    protected List<String> roles = null;
    
    /**
     * Creates a new, blank contributor.
     */
    public Contributor() {
        this(null);
    } 
    
    /**
     * Creates a new contributor by invoking the main constructor, passing the 
     * given info.
     * 
     * @param id The ID, like "npolar.no" or "john.doe".
     */
    public Contributor(String id) {
        this(id, null);
    }
    
    /**
     * Creates a new contributor by invoking the main constructor, passing the 
     * given info.
     * 
     * @param id The ID, like "npolar.no" or "john.doe".
     * @param name The name, like "Norwegian Polar Institute" or "John Doe".
     */
    public Contributor(String id, String name) {
        this(id, name, null, null, null);
    }
    
    /**
     * Creates a new contributor by invoking the main constructor, passing the 
     * given info.
     * 
     * @param id The ID, e.g. "npolar.no" or "john.doe".
     * @param firstName The (person's) first name, like "John".
     * @param lastName The (person's) last name, like "Doe".
     */
    public Contributor(String id, String firstName, String lastName) {
        this(id, null, firstName, lastName, null);
    }
    
    /**
     * Creates a new contributor by invoking the main constructor, passing the 
     * given info.
     * 
     * @param id The ID, e.g. "npolar.no" or "john.doe".
     * @param firstName The (person's) first name, like "John".
     * @param lastName The (person's) last name, like "Doe".
     * @param organisation The organisation name, like "Norwegian Polar Institute".
     * @see #Contributor(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public Contributor(String id, String firstName, String lastName, String organisation) {
        this(id, null, firstName, lastName, organisation);
    }
    
    /**
     * Main constructor. 
     * <p>
     * Only the ID is required. All other arguments that are not passed into
     * this method will will be set as best guesses, see {@link #init()}.
     * <p>
     * For example, if the first and last name is set, but not the name, then 
     * the name is set based on the first and last name.
     * 
     * @param id The ID, like "npolar.no" or "john.doe".
     * @param name The name, like "Norwegian Polar Institute" or "John Doe".
     * @param firstName The (person's) first name, like "John".
     * @param lastName The (person's) last name, like "Doe".
     * @param organisation The organisation name, like "Norwegian Polar Institute".
     */
    public Contributor(String id, String name, String firstName, String lastName, String organisation) {
        this.id = id;
        this.name = name;        
        this.firstName = firstName;
        this.lastName = lastName;        
        this.organisation = organisation;        
        init();
    }
    
    /**
     * Initializes the name and ID.
     */
    private void init() {
        initName();
        initId();
    }
    
    /**
     * Initializes the name, making sure it is non-null.
     * <p>
     * The name can take on many values. In order of priority:
     * <ul>
     * <li>The name</li>
     * <li>[first name] [last name] (or just one of the two)</li>
     * <li>The organisation</li>
     * <li>The ID</li>
     * </ul>
     */
    private void initName() {
        if (name == null) {
            if (firstName != null || lastName != null) {
                name = getFirstName();
                if (getLastName().isEmpty()) {
                    name += name.isEmpty() ? "" : " ";
                    name += getLastName();
                }
            }
            else if (organisation != null) {
                name = organisation;
            }
            else {
                // Final fallback
                name = id;
            }
        } else if (organisation == null) {
            // "name" was set ...
            if (firstName == null && lastName == null) {
                // ...but not personal names => assume "name" was name of org.
                organisation = name;
            }
        }
    }
    
    /**
     * Initializes the ID, making sure it is non-null.
     * <p>
     * The ID, if missing, will take on the value of the name. If that is also 
     * missing, the ID will take on a fallback "unknown" value.
     */
    private void initId() {
        if (id == null) {
            if (name != null) {
                id = name;
            }
        }
        if (id == null) {
            id = "[UNKNOWN ID]";
        }
    }

    /**
     * Gets the name.
     * <p>
     * The name can be many things; see {@link #initName()}.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the contributor's name, in an URL-friendly form.
     * <p>
     * The returned string will be in the form "[first name].[last name]".
     * 
     * @return the contributor's name, in an URL-friendly form.
     * @see APIUtil#toURLFriendlyForm(java.lang.String) 
     */
    public String getNameURLFriendly() {
        return APIUtil.toURLFriendlyForm(getName());
    }

    /**
     * Gets the ID, or an empty string if none.
     * 
     * @return The ID.
     */
    public String getId() {
        return id == null ? "" : id;
    }

    /**
     * Gets the last name, or an empty string if none.
     * 
     * @return The last name.
     */
    public String getLastName() {
        return lastName == null ? "" : lastName;
    }

    /**
     * Gets the first name, or an empty string if none.
     * 
     * @return The first name.
     */
    public String getFirstName() {
        return firstName == null ? "" : firstName;
    }

    /**
     * Gets the organisation, or an empty string if none.
     * 
     * @return The organisation.
     */
    public String getOrganisation() {
        return organisation == null ? "" : organisation;
    }

    /**
     * Sets the organisation.
     * 
     * @param organisation The organisation to set.
     * @return This instance, updated.
     */
    public Contributor setOrganization(String organisation) {
        this.organisation = organisation;
        return this;
    }
    
    
    
    /**
     * Checks if this contributor has an organisation set.
     * 
     * @return <code>true</code> if this contributor has an organisation set, <code>false</code> if not.
     */
    public boolean hasOrganisation() {
        return organisation != null && !getOrganisation().isEmpty();
    }
    
    /**
     * Checks if this contributor has an ID set.
     * 
     * @return <code>true</code> if this contributor has an ID set, <code>false</code> if not.
     */
    public boolean hasId() {
        return id != null && !id.isEmpty();
    }

    /**
     * Checks if this contributor is assigned the given role.
     * 
     * @param role The role to check for.
     * @return <code>true</code> if this contributor is assigned the given role, <code>false</code> if not.
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Checks if this contributor is assigned only the given role, and no other 
     * role.
     * 
     * @param role The role to check for.
     * @return <code>true</code> if this contributor is assigned only the given role, <code>false</code> if not.
     */
    public boolean hasRoleOnly(String role) {
        return roles.size() == 1 && roles.contains(role);
    }

    /**
     * Adds the given role for this contributor.
     * <p>
     * Every role is assigned only once. If the contributor was already assigned 
     * the given role, no change is made.
     * 
     * @param role The role to add.
     * @return The list of roles for this contributor, including the given role.
     */
    protected final List<String> addRole(String role) {
        if (roles == null) {
            roles = new ArrayList<String>(1);
        }
        if (!roles.contains(role)) {
            roles.add(role);
        }
        return roles;
    }

    /**
     * Adds the given roles for this contributor.
     * 
     * @see #addRole(String)
     * @param roles A list containing all roles to add.
     * @return The list of roles for this contributor, after this method has finished modifying it.
     */
    public List<String> addRoles(List<String> roles) {
        Iterator<String> i = roles.iterator();
        while (i.hasNext()) {
            this.addRole(i.next());
        }
        return roles;
    }

    /**
     * Gets all roles for this contributor.
     * 
     * @return The list of roles for this contributor.
     */
    public List<String> getRoles() { return roles; }
    
    /**
     * Returns this contributor's name. 
     * 
     * @return This contributor's name.
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Gets the hash code for this instance.
     * <p>
     * The return value is the hash code of a combination of the return values 
     * from {@link #getName()} and {@link #getId()}.
     * 
     * @return The hash code for this instance.
     */
    @Override
    public int hashCode() {
        return getId().concat(getName()).hashCode();
    }
    
    /**
     * Checks for equality on getName() and getId() return values.
     * 
     * @param that The Contributor to compare with.
     * @return <code>true</code> if both instances share the same name and ID, <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Contributor)) {
            return false;
        }
        return this.getName().equals(((Contributor)that).getName()) 
                && this.getId().equals(((Contributor)that).getId());
    }
    
    /**
     * Compares by comparing each instance's {@link #getName()} return value.
     * 
     * @param that The Contributor to compare with.
     * @return A comparison via the {@link #getName()} method.
     */
    @Override
    public int compareTo(Contributor that) {
        return this.getName().compareTo(
                that.getName()
        );
    }
}