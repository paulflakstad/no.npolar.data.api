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
 * Represents a single contributor (author, editor, translator ...) to a 
 * publication. 
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
     * 
     * @param contributor The JSON object to use when constructing this instance.
     */
    public PublicationContributor(JSONObject contributor, final Locale loc) {
        this.displayLocale = loc;
        if (displayLocale == null)
            displayLocale = new Locale(APIService.DEFAULT_LOCALE_NAME);
        
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        
        try {
            try { id = contributor.getString(Publication.Key.ID); } catch (Exception e) { }
            try { organisation = contributor.getString(Publication.Key.ORG); } catch (Exception e) { }
            try { fName = contributor.getString(Publication.Key.FNAME); } catch (Exception e) { fName = ""; }
            try { lName = contributor.getString(Publication.Key.LNAME); } catch (Exception e) { lName = ""; }

            // Evaluate the person's role(s)
            JSONArray rolesArr = null;
            try {
                rolesArr = contributor.getJSONArray(Publication.Key.ROLES);
            } catch (Exception e) {
                // No role defined, assume role=author
                addRole(Publication.Val.ROLE_AUTHOR);
            }
            if (rolesArr != null) {
                for (int j = 0; j < rolesArr.length(); j++) {
                    String role = rolesArr.getString(j);
                    addRole(role);
                }
            }

            // NPI affiliate?
            try { 
                isNPIContributor = contributor.getString(Publication.Key.ORG).equalsIgnoreCase(Publication.Val.ORG_NPI);
            } catch (Exception e) {}
            
            
            // ToDo: Remove this hack
            // BEGIN HACK
            // This hack is here to amend erroneous entries.
            //
            // Amend entries that should have organisation = "npolar.no" but 
            // istead have organisation = "NPI", or similar.
            try {
                if (!isNPIContributor) {
                    if (organisation != null && !organisation.trim().isEmpty()) {
                        if (organisation.matches("^(?i)(NP(I)?|Norsk Polarinstitutt|Norwegian Polar Institute)")) {
                            isNPIContributor = true;
                        }
                    }
                }
            } catch (Exception e) {}
            // Try to amend missing ID on NPI contributors
            try {
                if (isNPIContributor) { 
                    if (id == null || id.isEmpty()) {
                        // No guarantees - just a best guess.
                        id = APIUtil.toURLFriendlyForm(fName + " " + lName + "@npolar.no");
                    }
                }
            } catch (Exception e) {}
            // END HACK
        } catch (Exception e) { }
    }

    /**
     * Gets the ID for this contributor.
     * 
     * @return The ID for this contributor.
     */
    public String getID() { return id; }

    /**
     * Gets the first name for this contributor.
     * 
     * @return The first name for this contributor.
     */
    public String getFirstName() { return fName; }

    /**
     * Gets the last name for this contributor.
     * 
     * @return The last name for this contributor.
     */
    public String getLastName() { return lName; }

    /**
     * Determines whether or not this contribution was made on behalf of the NPI.
     * 
     * @return True if the contribution was made on behalf of the NPI, false if not.
     */
    public boolean isNPIContributor() {
        return isNPIContributor;
    }

    /**
     * Checks if this contributor is assigned the given role.
     * 
     * @param role The role to check for.
     * @return True if this contributor is assigned the given role, false if not.
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Checks if this contributor is assigned only the given role, and no other 
     * role.
     * 
     * @param role The role to check for.
     * @return True if this contributor is assigned only the given role, false if not.
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
        if (roles == null)
            roles = new ArrayList<String>(1);
        if (!roles.contains(role))
            roles.add(role);
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
        while (i.hasNext()) 
            this.addRole(i.next());
        return roles;
    }

    /**
     * Gets all roles for this contributor.
     * 
     * @return The list of roles for this contributor.
     */
    public List<String> getRoles() { return roles; }

    /**
     * Gets a string representation of this contributor.
     * 
     * @return The string representation of this contributor.
     */
    @Override
    public String toString() {
        return fName + " " + lName + (hasRole(Publication.Val.ROLE_EDITOR) ? " (" + labels.getString(Labels.PUB_REF_EDITOR_0) + ")" : "");
    }

    /**
     * Gets an HTML representation of this contributor.
     * 
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

        if (this.hasRole(Publication.Val.ROLE_EDITOR) && specifyEditorRole)
            s += " (" + labels.getString(Labels.PUB_REF_EDITOR_0) + ")";

        if (isNPIContributor)
            s += "</span>";

        return s;
    }

    /**
     * Gets an HTML representation of this contributor.
     * 
     * @return The string representation of this contributor.
     */
    public String toHtml() {
        return toHtml(true, false);
    }
}
