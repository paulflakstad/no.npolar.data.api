package no.npolar.data.api;

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
public class PublicationContributor extends Contributor {    
    /** Flag indicating if this contributor is affiliated with the Polar Institute. */
    private boolean isNPIContributor = false;
    /** Translations etc. */
    protected ResourceBundle labels = null;
    /** Holds the currently preferred display locale. */
    protected Locale displayLocale = null;
    
    /** 
     * @deprecated Use {@link APIUtil#REGEX_PATTERN_NPI} instead.
     */
    public static final String REGEX_PATTERN_NPI = APIUtil.REGEX_PATTERN_NPI;
    
    /**
     * Constructs a new instance, based on the given JSON object.
     * 
     * @param contributor The JSON object to use when constructing this instance.
     * @param loc The preferred locale to use for any output.
     */
    public PublicationContributor(JSONObject contributor, final Locale loc) {
        this.displayLocale = loc;
        if (displayLocale == null) {
            displayLocale = new Locale(APIService.DEFAULT_LOCALE_NAME);
        }
        
        labels = ResourceBundle.getBundle(Labels.getBundleName(), displayLocale);
        
        try {
            try { id = contributor.getString(Publication.Key.ID).trim(); } catch (Exception e) { }
            try { organisation = contributor.getString(Publication.Key.ORG).trim(); } catch (Exception e) { }
            try { firstName = contributor.getString(Publication.Key.FNAME).trim(); } catch (Exception e) { firstName = ""; }
            try { lastName = contributor.getString(Publication.Key.LNAME).trim(); } catch (Exception e) { lastName = ""; }

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
            
            amendFlaws();
        } catch (Exception e) { }
    }
    
    /**
     * Tries to amend flaws on contributor affiliated with the Polar Institute.
     * <p>
     * This is a hack that aims to amend bad user input in two key fields:
     * <ul>
     * <li>if "organisation" is something we can assume should be interpreted as 
     * "Norwegian Polar Institute", but not the expected "npolar.no", we set the 
     * {@link #isNPIContributor} flag to <code>true</code>.</li>
     * <li>if the ID is missing, AND this is an NPI-affiliated contributor, we
     * make a best guess, based on the name, and set it as ID.</li>
     * </ul>
     * 
     * @see APIUtil#REGEX_PATTERN_NPI
     */
    private void amendFlaws() {
        // This hack was implemented in response to the discovery of an unknown
        // number of publications containing bad data, input by users who did
        // not know how to fill out necessary fields.
        //
        // First, check for organisation = "npolar.no" errors (these will 
        // istead have organisation = "NPI", or similar - see the regex pattern)
        try {
            if (!isNPIContributor() && hasOrganisation()) {
                if (getOrganisation().matches(APIUtil.REGEX_PATTERN_NPI)) {
                    isNPIContributor = true;
                }
            }
        } catch (Exception ignore) {}
        
        // Next, try to amend missing ID
        // (But only if this is an NPI contributor)
        try {
            if (isNPIContributor() && !hasID()) {
                // No guarantee this ID will be correct - just a best guess
                id = getNameURLFriendly().concat("@" + "npolar.no");
            }
        } catch (Exception ignore) {}
    }

    /**
     * Gets the ID for this contributor.
     * 
     * @return The ID for this contributor.
     * @deprecated Use {@link #getId()} instead.
     */
    public String getID() {
        return getId();
    }
    
    /**
     * Determines whether or not this contribution was made on behalf of the NPI.
     * 
     * @return True if the contribution was made on behalf of the NPI, false if not.
     */
    public boolean isNPIContributor() {
        return isNPIContributor;
    }
    
    /**
     * Checks if this contributor has an ID set.
     * 
     * @return <code>true</code> if this contributor has an ID set, <code>false</code> if not.
     * @deprecated Use {@link #hasId()} instead.
     */
    public boolean hasID() {
        return hasId();
    }

    /**
     * Gets a string representation of this contributor.
     * <p>
     * The returned string will be of the form: 
     * <ul>
     * <li>"[first name] [last name]", OR</li>
     * <li>"[first name] [last name] (editor)" (if this contributor is an editor)</li>
     * </ul>
     * 
     * @return The string representation of this contributor.
     */
    @Override
    public String toString() {
        return getName() + (hasRole(Publication.Val.ROLE_EDITOR) ? " (" + labels.getString(Labels.PUB_REF_EDITOR_0) + ")" : "");
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

        if (isNPIContributor) {
            s += "<span class=\"pub-contributor-npi\">";
        }
        
        if (fullName) {
            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                s += firstName + " " + lastName;
            } else {
                s += firstName + lastName; // One of the two, or both, is empty
            }
        }
        else {
            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                s += lastName + ",&nbsp;" + APIUtil.getInitials(firstName);
            } else {
                s += lastName + APIUtil.getInitials(firstName);
            }
        }

        if (this.hasRole(Publication.Val.ROLE_EDITOR) && specifyEditorRole) {
            s += " (" + labels.getString(Labels.PUB_REF_EDITOR_0) + ")";
        }

        if (isNPIContributor) {
            s += "</span>";
        }

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
