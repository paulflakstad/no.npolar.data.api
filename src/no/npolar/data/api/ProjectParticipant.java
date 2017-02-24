package no.npolar.data.api;

import no.npolar.data.api.util.Mapper;
import org.opencms.jsp.CmsJspActionElement;

/**
 * Represents a single project participant (either "regular" or leader).
 * <p>
 * ToDo: Check if this class is able to extend {@link Contributor}.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class ProjectParticipant {
    private String firstName = null;
    private String lastName = null;
    private String organisation = null;
    private String uri = null;
    private String orgSymbol = null;

    /**
     * Creates a new, empty instance.
     */
    public ProjectParticipant() {  }

    /**
     * Creates a new instance with the given name.
     * 
     * @param firstName The first (given) name.
     * @param lastName The last (family) name.
     */
    public ProjectParticipant(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    /**
     * Sets the organization.
     * 
     * @param organisation The organization.
     */
    public void setOrganization(String organisation) { this.organisation = organisation; }
    
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
    public String getName() { return firstName + " " + lastName; }
    
    /**
     * @return the first (given) name.
     */
    public String getFirstName() { return firstName; }
    
    /**
     * @return the last (family) name
     */
    public String getLastName() { return lastName; }
    
    /**
     * @return the organization.
     */
    public String getInstitution() { return organisation; }
    
    /**
     * @return the URI.
     */
    public String getUri() { return uri; }
    
    /**
     * Returns a string representation of this instance, without using any 
     * mapper.
     * 
     * @see #toString(no.npolar.data.api.util.Mapper) 
     * @return This instance, as a string in HTML format.
     */
    @Override
    public String toString() {
        return toString(new Mapper());
    }

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
        if (m == null) {
            m = new Mapper();
        }
        String s = getName();
        //if (inst != null && !inst.isEmpty())
        //    s += " [" + inst + "]";
        if (organisation != null && !organisation.isEmpty()) {
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
     * @param cms An initialized CMS action element.
     * @return This instance, as a string in HTML format.
     * @deprecated Use {@link #toString(no.npolar.data.api.util.Mapper)} instead.
     */
    public String toString(CmsJspActionElement cms) {
        String s = getName();
        if (organisation != null && !organisation.isEmpty())
            s += " [" + organisation + "]";
        if (uri != null && !uri.isEmpty())
            s = "<a href=\"" + (cms != null ? cms.link(uri) : uri) + "\">" + s + "</a>";

        return s;
    }
}
