package no.npolar.data.api;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Represents a topic.
 * <p>
 * Each topic consist of an ID (its name in the Data Centre) and a label (the 
 * local translation made via the ID).
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Topic {
    private String id = null;
    private String label = null;
    
    /**
     * Creates a new topic, identified by the given ID.
     * 
     * @param id The topic ID.
     */
    public Topic(String id) {
        this.id = id;
        this.label = getLabel(new Locale(APIService.DEFAULT_LOCALE_NAME));
    }
    
    /** @return the ID of this topic. */
    public final String getId() { return id; }
    /** @return the label for this topic. */
    public final String getLabel() { return label; }
    /** @return the localized label for this topic (if any). */
    public final String getLabel(Locale locale) { 
        try {
            return ResourceBundle.getBundle(Labels.getBundleName(), locale).getString(Labels.TOPIC_PREFIX_0.concat(id)); 
        } catch (Exception e) {
            // No translation available (!)
            return id; 
        }
    }
    /**
     * Gets a string representation, consisting of the ID.
     * 
     * @return a string representation of this topic.
     */
    @Override
    public String toString() { return id; }
    
    /**
     * Gets this topic as a topic filter link, wrapped in a 
     * <code>span class="tag"</code> element.
     * 
     * @param filterUrl The base URL.
     * @param locale The preferred language.
     * @return This topic as a topic filter.
     */
    public String toHtml(String filterUrl, Locale locale) { 
        String html = "<span class=\"tag\">" 
                        + (filterUrl != null && !filterUrl.isEmpty() 
                            ? 
                                "<a href=\"" + filterUrl.concat("&amp;").concat(APIService.FILTER_PREFIX).concat(Publication.JSON_KEY_TOPICS).concat("=").concat(id) + "\">"
                            : 
                                "")
                        + getLabel(locale)
                        + (filterUrl != null && !filterUrl.isEmpty() ? "</a>" : "")
                        + "</span>"; 
        return html;
    }
}
