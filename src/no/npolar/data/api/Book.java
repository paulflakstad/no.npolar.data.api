package no.npolar.data.api;

import java.util.Locale;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONObject;

/**
 * Custom implementation for books and any book-like publications, due to 
 * special cite string requirements.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Book extends Publication {
    /**
     * @see Publication#Publication(org.opencms.json.JSONObject, java.util.Locale) 
     */
    public Book(JSONObject pubObject, Locale loc) {
        super(pubObject, loc);
    }

    /**
     * @see Publication#toString() 
     */
    @Override
    public String toString() {
        String s = "";
        
        String authorsStr = getAuthors();
        String editorsStr = getEditors();
        
        // 1: Author(s), if any
        if (!authorsStr.isEmpty()) {
            s += authorsStr;
            //s += "."; // Add this only when using full names
        } else {
            // No authors, print editor(s), if any
            if (!editorsStr.isEmpty()) {
                s += editorsStr + " (" + labels.getString(getPeopleByRole(Publication.JSON_VAL_ROLE_EDITOR).size() > 1 ? Labels.PUB_REF_EDITORS_0 : Labels.PUB_REF_EDITOR_0).toLowerCase() + ")";
            }
        }
        
        // 2: Published year (and/or state, e.g. "in press", if not published)
        if (!pubYear.isEmpty() || !getPubDate().isEmpty()) {
            s += " " + (!getPubDate().isEmpty() ? getPubDateAsYear() : pubYear) + "."; // Date takes precedence over year
        }
        if (!this.isState(JSON_VAL_STATE_PUBLISHED)) {
            s += " <em>(" + labels.getString(Labels.PUB_STATE_PREFIX_0.concat(getState())) + ")</em>";
        }
        
        // 3: Title
        s += " <a href=\"" + URL_PUBLINK_BASE + id + "\">";
        s += title; 
        if (!getVolume().isEmpty()) {
            s += " (Vol. " + getVolume() + ")";
        }
        s += "</a>" + (endsWithStopChar(title) ? "" : ".") + " ";
        
        // 4: Editor(s)
        if (!authorsStr.isEmpty() && !editorsStr.isEmpty()) { // Require both not-empty author(s) AND not-empty editor(s). If not, editor(s) has already been printed in step 1.
            s += editorsStr + " (" + labels.getString(getPeopleByRole(Publication.JSON_VAL_ROLE_EDITOR).size() > 1 ? Labels.PUB_REF_EDITORS_0 : Labels.PUB_REF_EDITOR_0).toLowerCase() + ").";
        }
        
        // 5: Translator(s)
        if (!translators.isEmpty()) {
            s+= " (";
            try {
                String publicationDisplayLanguage = APIUtil.getDisplayLanguage(new Locale(language), displayLocale);
                s += labels.getString(Labels.LABEL_TRANSLATED_TO_0) + " " + publicationDisplayLanguage + " " + labels.getString(Labels.LABEL_BY_0).toLowerCase();
            } catch (Exception e) {
                s += labels.getString(Labels.LABEL_TRANSLATED_BY_0);
            };
            s += " " + getNamesOfTranslators() + ")"; // No trailing "." because the transator name(s) string will end with a "."
        }
        
        // Journal
        if (!getJournalName().isEmpty() 
                || !getVolume().isEmpty() 
                || !getJournalSeries().isEmpty()) {
            s += " ";
            if (!getJournalName().isEmpty()) {
                s += getJournalName();
            }

            // Volume / series
            if (!getVolume().isEmpty() || !getJournalSeries().isEmpty()) {
                if (!getJournalSeries().isEmpty()) {
                    s += (getJournalName().isEmpty() ? "" : ". ") + mappings.getMapping(getJournalSeries());
                    if (!getJournalSeriesNo().isEmpty())
                        s += NBSP + getJournalSeriesNo();
                    else if (!getVolume().isEmpty())
                        s += NBSP + getVolume();
                }
                else if (!getVolume().isEmpty()) {
                    s += NBSP + getVolume();
                    if (!getIssue().isEmpty()) {
                        s += "(" + getIssue() + ")";
                    }
                }
            }
            
            s += ".";
        }
        
        // 6: Publisher
        if (!getPublisher().isEmpty()) {
            s += (s.trim().endsWith(".") ? " " : ". ") + getPublisher() + ".";
        }
        
        // 7: DOI
        s += (doi.isEmpty() ? "" : "<br />DOI:<a href=\"" + URL_DOI_BASE + doi + "\">" + doi + "</a>");

        return s;
    }

}