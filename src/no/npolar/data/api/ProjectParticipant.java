package no.npolar.data.api;

import no.npolar.data.api.util.Mapper;
import org.opencms.jsp.CmsJspActionElement;

/**
 * Represents a single project participant (either regular or leader).
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class ProjectParticipant {
    private String fName = null;
    private String lName = null;
    private String org = null;
    private String uri = null;
    private String orgSymbol = null;

    /**
     * Creates a new, empty instance.
     */
    public ProjectParticipant() {  }

    /**
     * Creates a new instance with the given name.
     * 
     * @param fName The first (given) name.
     * @param lName The last (family) name.
     */
    public ProjectParticipant(String fName, String lName) {
        this.fName = fName;
        this.lName = lName;
    }
    /**
     * Sets the organization.
     * 
     * @param inst The organization.
     */
    public void setOrganization(String org) { this.org = org; }
    /**
     * Sets the institution symbol.
     * 
     * @param instSymbol The institution symbol.
     */
    public void setInstitutionSymbol(String instSymbol) { this.orgSymbol = instSymbol; }
    /**
     * Sets the URI.
     * 
     * @param uri The URI.
     */
    public void setUri(String uri) { this.uri = uri; }
    /**
     * @return the name.
     */
    public String getName() { return fName + " " + lName; }
    /**
     * @return the first (given) name.
     */
    public String getFirstName() { return fName; }
    /**
     * @return the last (family) name
     */
    public String getLastName() { return lName; }
    /**
     * @return the organization.
     */
    public String getInstitution() { return org; }
    /**
     * @return the URI.
     */
    public String getUri() { return uri; }

    /**
     * Returns a string representation of this instance.
     * <p>
     * The returned string is HTML code. It contains the name, and the 
     * organization info. If a URI has been set, the outer wrapper is a link.
     * 
     * @param m The mapper.
     * @return This instance, as a string in HTML format.
     */
    public String toString(Mapper m) {
        String s = getName();
        //if (inst != null && !inst.isEmpty())
        //    s += " [" + inst + "]";
        if (org != null && !org.isEmpty()) {
            //s += "<sup style=\"vertical-align:top; position:relative; top:-3px;\">" + symbolMappings.get(inst) + "</sup>";
            //s += "<sup style=\"vertical-align:top; position:relative; top:2px; cursor:pointer;\">" 
            s += "<sup style=\"position:relative; top:2px; cursor:pointer;\" class=\"project-participant-note\">" 
                    + "<span" 
                        + (m != null ? " data-tooltip=\"" + m.getMapping(orgSymbol) + "\"" : "") // Prevent NPE
                    + ">"
                        + " [" + orgSymbol + "]"
                    +"</span>"
                + "</sup>";
        }
        if (uri != null && !uri.isEmpty())
            s = "<a href=\"" + uri + "\">" + s + "</a>";

        return s;
    }

    /**
     * Returns a string representation of this instance.
     * <p>
     * The returned string is HTML code. It contains the name, and the 
     * organization info. If a URI has been set, the outer wrapper is a link.
     * 
     * @param m The mapper.
     * @return This instance, as a string in HTML format.
     * @deprecated Use {@link #toString(no.npolar.data.api.util.Mapper)} instead.
     */
    public String toString(CmsJspActionElement cms) {
        String s = getName();
        if (org != null && !org.isEmpty())
            s += " [" + org + "]";
        if (uri != null && !uri.isEmpty())
            s = "<a href=\"" + (cms != null ? cms.link(uri) : uri) + "\">" + s + "</a>";

        return s;
    }
}
