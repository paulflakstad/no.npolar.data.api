package no.npolar.data.api;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Topic {
    private String id = null;
    private String label = null;
    
    public Topic(String id) {
        this.id = id;
        this.label = getLabel(new Locale(APIService.DEFAULT_LOCALE_NAME));
    }
    
    public final String getId() { return id; }
    public final String getLabel() { return label; }
    public final String getLabel(Locale locale) { return ResourceBundle.getBundle(Labels.getBundleName(), locale).getString(Labels.TOPIC_PREFIX_0.concat(id)); }
    @Override
    public String toString() { return id; }
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
