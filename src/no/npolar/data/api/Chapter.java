package no.npolar.data.api;

import java.util.Locale;
import no.npolar.data.api.util.APIUtil;
import org.opencms.json.JSONObject;
import org.opencms.util.CmsHtmlExtractor;

/**
 * Custom implementation for books and any book-like publications, due to 
 * special cite string requirements.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Chapter extends Publication {
    /**
     * @see Publication#Publication(org.opencms.json.JSONObject, java.util.Locale) 
     */
    public Chapter(JSONObject pubObject, Locale loc) {
        super(pubObject, loc);
    }
    
    /**
     * @see Publication#toString() 
     */
    @Override
    public String toString() {
        String s = "";
        
        String authorsStr = getAuthors();
        
        // 1: Author(s), if any
        if (!authorsStr.isEmpty()) {
            s += authorsStr;
            //s += "."; // Add this only when using full names
        }
        
        // 2: Published year (and/or state, e.g. "in press", if not published)
        if (!pubYear.isEmpty() || !getPubDate().isEmpty()) {
            s += " " + (!getPubDate().isEmpty() ? getPubDateAsYear() : pubYear) + "."; // Date takes precedence over year
        }
        if (!this.isState(JSON_VAL_STATE_PUBLISHED)) {
            s += " <em>(" + labels.getString(Labels.PUB_STATE_PREFIX_0.concat(getState())) + ")</em>";
        }
        
        // 3: Title
        s += " <a href=\"" + URL_PUBLINK_BASE + id + "\">" + title + "</a>" + (endsWithStopChar(title) ? "" : ".") + " ";
        
        
        
        
            
            
            
            
        

        
        //if (this.hasParent() || this.isPartContribution()) {
            // The "in" part
            if (!getPages().isEmpty()) {
                s += APIUtil.capitalizeFirstLetter(getPagesWithLabel()) + " " + labels.getString(Labels.PUB_REF_IN_0).toLowerCase() + ": ";
            } else {
                s += labels.getString(Labels.PUB_REF_IN_0) + ": ";
            }

            if (this.hasParent()) { // Parent exists
                // Parent editor(s)
                String parentEditors = parent.getEditors();
                if (!parentEditors.isEmpty()) {
                    s += parentEditors + " (" + labels.getString(parent.getPeopleByRole(JSON_VAL_ROLE_EDITOR).size() > 1 ? Labels.PUB_REF_EDITORS_0 : Labels.PUB_REF_EDITOR_0).toLowerCase() + ")";
                    s += ": ";
                }
                
                // Parent title and, for books, possibly also volume
                s += "<a href=\"" + URL_PUBLINK_BASE + parentId + "\">" + parent.getTitle();
                if (parent.getType().equals(TYPE_BOOK) && !parent.getVolume().isEmpty()) {
                    s += " (Vol." + NBSP + parent.getVolume() + ")";
                }
                s += ". "; // Don't end the parent link before we're done with the entire parent string (series etc.)
            }
            else { // No parent registered, but still "part-contribution" (book/report chapter)
                String editorsStr = getEditors();
                // Parent publication editors
                if (!editorsStr.isEmpty()) {
                    s += editorsStr + " (" + labels.getString(getPeopleByRole(Publication.JSON_VAL_ROLE_EDITOR).size() > 1 ? Labels.PUB_REF_EDITORS_0 : Labels.PUB_REF_EDITOR_0).toLowerCase() + "): ";
                }
            }
        //}
        
        
        
        
        

        // If a parent exists, override any journal existing on this publication with the parent's journal
        Publication journalObj = hasParent() ? parent : this;

        // Journal
        String journal = journalObj.getJournalName();

        if (!journal.isEmpty() 
                || !journalObj.getVolume().isEmpty() 
                || !journalObj.getJournalSeries().isEmpty()) {

            if (!journal.isEmpty()) {
                s += journal;
            }
            
            if (s.endsWith(".."))
                s = s.substring(0, s.length()-1);

            // Volume / series
            if (!journalObj.isType(Publication.TYPE_BOOK)) {
                if (!journalObj.getVolume().isEmpty() || !journalObj.getJournalSeries().isEmpty()) {
                    if (!journalObj.getJournalSeries().isEmpty()) {
                        s += (journal.isEmpty() ? "" : (journal.endsWith(".") ? "" : ".")) + " " + mappings.getMapping(journalObj.getJournalSeries());
                        if (!journalObj.getJournalSeriesNo().isEmpty())
                            s += NBSP + journalObj.getJournalSeriesNo();
                        else if (!journalObj.getVolume().isEmpty())
                            s += NBSP + journalObj.getVolume();
                    }
                    else if (!journalObj.getVolume().isEmpty()) {
                        s += NBSP + journalObj.getVolume();
                        if (!journalObj.getIssue().isEmpty()) {
                            s += "(" + journalObj.getIssue() + ")";
                        }
                    }
                }
            }
            /*s += ".";
            s = s.trim();
            if (s.endsWith(".."))
                s = s.substring(0, s.length()-1);*/
            
            //s = s.trim();
            /*if (s.endsWith("."))
                s = s.substring(0, s.length()-1);*/
            
            /*if (hasParent()) {
                s += "</a>";
            }*/
        }
        
        s = s.trim();
        if (s.endsWith("."))
            s = s.substring(0, s.length()-1);

        //if (hasParent() && !s.endsWith("</a>"))
        if (hasParent())
            s += "</a>";
        
        
        s += ".";

        // Translation credit
        if (!translators.isEmpty()) {
            s+= " (";
            //if (language != null && !language.isEmpty()) {
                try {
                    String publicationDisplayLanguage = APIUtil.getDisplayLanguage(new Locale(language), displayLocale);
                    //String publicationDisplayLanguage = new Locale(language).getDisplayLanguage(new Locale(displayLocale.getCountry()));
                    s += labels.getString(Labels.LABEL_TRANSLATED_TO_0) + " " + publicationDisplayLanguage + " " + labels.getString(Labels.LABEL_BY_0).toLowerCase();
                } catch (Exception e) {
                    s += labels.getString(Labels.LABEL_TRANSLATED_BY_0);
                } 
            //}
            s += " " + getNamesOfTranslators() + ")"; // No trailing "." because the transator name(s) string will end with a "."
        }

        //if (!hasParent()) {
            if (s.endsWith(".."))
                s = s.substring(0, s.length()-1);

            if (!journalObj.getPublisher().isEmpty()) {
                try {
                    s += (CmsHtmlExtractor.extractText(s.trim(), "utf-8").endsWith(".") ? " " : ". ") + mappings.getMapping(journalObj.getPublisher()) + ".";
                } catch (Exception e) {
                    s += (s.trim().endsWith(".") ? " " : ". ") + mappings.getMapping(journalObj.getPublisher()) + ".";
                }
            }
        //}

        if (s.endsWith(".."))
            s = s.substring(0, s.length()-1);

        if (s.endsWith(". ."))
            s = s.substring(0, s.length()-2);

        // Conference
        if (!journalObj.getConference().isEmpty()) {
            s += " <span class=\"pub-event\">" + journalObj.getConference() + "</span>.";
        }

        s += (doi.isEmpty() ? "" : "<br />DOI:<a href=\"" + URL_DOI_BASE + doi + "\">" + doi + "</a>");
            
        return s;
    }
}
