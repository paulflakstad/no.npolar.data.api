package no.npolar.data.api.util;

import org.opencms.jsp.CmsJspActionElement;

/**
 * An OptLink (optional link) instance is either just plain text, or a link.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class OptLink {
    private String text = null;
    private String uri = null;

    /**
     * Default constructor.
     */
    public OptLink() {}
    
    /**
     * Constructs a new instance, consisting of the given text (no link).
     * 
     * @param text The text.
     */
    public OptLink(String text) {
        this.text = text;
    }    
    
    /**
     * Constructs a new instance, consisting of the given text as a link to the
     * given URI.
     * 
     * @param text The link text.
     * @param uri The link target URI.
     */
    public OptLink(String text, String uri) {
        this.text = text;
        this.uri = uri;
    }

    /**
     * Sets the link target URI.
     * 
     * @param uri The link target URI.
     */
    public void setUri(String uri) { this.uri = uri; }
    /**
     * Gets the link target URI.
     * 
     * @return The link target URI.
     */
    public String getUri() { return uri; }
    /**
     * Gets the (link) text.
     * 
     * @return The (link) text.
     */
    public String getText() { return text; }
    /**
     * Gets a string representation suitable for HTML output.
     * <p>
     * Should be used only when the link URI is in fact an absolute URL 
     * (i.e. not an internal URI).
     * 
     * @return A string representation suitable for HTML output.
     * @see #toString(no.npolar.util.CmsAgent) 
     */
    @Override
    public String toString() {
        String s = text;
        if (uri != null && !uri.isEmpty())
            s = "<a href=\"" + uri + "\">" + s + "</a>";
        return s;
    }
    /**
     * Gets a string representation suitable for HTML output.
     * <p>
     * Should be used if this instance has a link is internal (i.e. not an 
     * absolute URL).
     * 
     * @param cms Needed to produce valid internal links.
     * @return A string representation suitable for HTML output.
     * @see #toString() 
     */
    public String toString(CmsJspActionElement cms) {
        String s = text;
        if (uri != null && !uri.isEmpty())
            s = "<a href=\"" + cms.link(uri) + "\">" + s + "</a>";
        return s;
    }
}
