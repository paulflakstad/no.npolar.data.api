package no.npolar.data.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONObject;

/**
 *
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class PublicationContributor {
    private String id = null;
    private String organisation = "";
    private String fName = "";
    private String lName = "";
    private boolean isNPIContributor = false;
    private List<String> roles = null;

    protected ResourceBundle labels = null;
    protected Locale displayLocale = null;
    /**
     * Constructs a new instance, based on the given JSON object.
     * @param person The JSON object to use when constructing this instance.
     */
    public PublicationContributor(JSONObject person, final Locale loc) {
        this.displayLocale = loc;
        if (displayLocale == null)
            displayLocale = new Locale(APIService.DEFAULT_LOCALE_NAME);
        
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        
        try {
            try { id = person.getString(Publication.JSON_KEY_ID); } catch (Exception e) { }
            try { organisation = person.getString(Publication.JSON_KEY_ORG); } catch (Exception e) { }
            try { fName = person.getString(Publication.JSON_KEY_FNAME); } catch (Exception e) { fName = ""; }
            try { lName = person.getString(Publication.JSON_KEY_LNAME); } catch (Exception e) { lName = ""; }

            // Evaluate the person's role(s)
            JSONArray rolesArr = null;
            try {
                rolesArr = person.getJSONArray(Publication.JSON_KEY_ROLES);
            } catch (Exception e) {
                // No role defined, assume role=author
                addRole(Publication.JSON_VAL_ROLE_AUTHOR);
            }
            if (rolesArr != null) {
                for (int j = 0; j < rolesArr.length(); j++) {
                    String role = rolesArr.getString(j);
                    addRole(role);
                }
            }

            // NPI affiliate?
            try { if (person.getString(Publication.JSON_KEY_ORG).equalsIgnoreCase(Publication.JSON_VAL_ORG_NPI)) isNPIContributor = true; } catch (Exception e) {}
        } catch (Exception e) { }
    }

    /**
     * Gets the ID for this person.
     * @return The ID for this person.
     */
    public String getID() { return id; }

    /**
     * Gets the first name for this person.
     * @return The first name for this person.
     */
    public String getFirstName() { return fName; }

    /**
     * Gets the last name for this person.
     * @return The last name for this person.
     */
    public String getLastName() { return lName; }

    /**
     * Determines whether or not this person contributed on behalf of the NPI.
     * @return True if the person contributed on behalf of the NPI, false if not.
     */
    public boolean isNPIContributor() {
        return isNPIContributor;
    }

    /**
     * Checks if this person is assigned the given role.
     * @param role The role to check for.
     * @return True if this person is assigned the given role, false if not.
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Checks if this person is assigned only the given role, and no other role.
     * @param role The role to check for.
     * @return True if this person is assigned only the given role, false if not.
     */
    public boolean hasRoleOnly(String role) {
        return roles.size() == 1 && roles.contains(role);
    }

    /**
     * Adds the given role for this person. Every role is assigned only once. 
     * If the person was already assigned the given role, no change is made.
     * @param role The role to add.
     * @return The list of roles for this person, including the given role.
     */
    protected final List<String> addRole(String role) {
        if (roles == null)
            roles = new ArrayList<String>(1);
        if (!roles.contains(role))
            roles.add(role);
        return roles;
    }

    /**
     * Adds the given roles for this person.
     * @see #addRole(String)
     * @param roles A list containing all roles to add.
     * @return The list of roles for this person, after this method has finished modifying it.
     */
    public List<String> addRoles(List<String> roles) {
        Iterator<String> i = roles.iterator();
        while (i.hasNext()) 
            this.addRole(i.next());
        return roles;
    }

    /**
     * Gets all roles for this person.
     * @return The list of roles for this person.
     */
    public List<String> getRoles() { return roles; }

    /**
     * Gets a string representation of this person.
     * @return The string representation of this person.
     */
    @Override
    public String toString() {
        return fName + " " + lName + (hasRole(Publication.JSON_VAL_ROLE_EDITOR) ? " (" + labels.getString(Labels.PUB_REF_EDITOR_0) + ")" : "");
    }

    /**
     * Gets an HTML representation of this person.
     * @param specifyEditorRole Whether or not to specify editors with " (ed.)" after the name.
     * @param fullName Whether or not to get the full name (John Clayton Mayer) or the short form (Mayer, J.C.).
     * @return The string representation of this contributor.
     */
    public String toHtml(boolean specifyEditorRole, boolean fullName) {
        String s = "";

        if (isNPIContributor)
            s += "<span class=\"pub-contributor-npi\">";
        
        if (fullName) {
            if (!fName.isEmpty() && !lName.isEmpty())
                s += fName + " " + lName;
            else {
                s += fName + lName; // One of the two, or both, is empty
            }
        }
        else {
            if (!fName.isEmpty() && !lName.isEmpty())
                s += lName + ",&nbsp;" + APIUtil.getInitials(fName);
            else
                s += lName + APIUtil.getInitials(fName);
        }

        if (this.hasRole(Publication.JSON_VAL_ROLE_EDITOR) && specifyEditorRole)
            s += " (" + labels.getString(Labels.PUB_REF_EDITOR_0) + ")";

        if (isNPIContributor)
            s += "</span>";

        return s;
    }

    /**
     * Gets an HTML representation of this person.
     * @return The string representation of this person.
     */
    public String toHtml() {
        return toHtml(true, false);
    }
}
