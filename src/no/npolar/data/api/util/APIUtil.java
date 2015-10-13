package no.npolar.data.api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import no.npolar.data.api.APIService;
import org.opencms.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.markdown4j.Markdown4jProcessor;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;

/**
 * Norwegian Polar Institute Data Centre API utilities.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class APIUtil {
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(no.npolar.data.api.util.APIUtil.class);
    
    /**
     * Requests the given URL and returns the response as a String.
     * 
     * @param url The URL to request.
     * @return The response, as a string.
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String httpResponseAsString(String url) 
            throws MalformedURLException, IOException {
        
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream(), APIService.SERVICE_CHARSET));
        StringBuilder s = new StringBuilder();
        String oneLine;
        while ((oneLine = in.readLine()) != null) {
            s.append(oneLine);
        }
        in.close();

        return s.toString();
    }
    
    /**
     * Queries the service at the given URL, and tries to return the service 
     * response as a JSON object.
     * 
     * @param url The URL to use for querying the service.
     * @return The service response as a JSON object, or null if anything goes wrong.
     */
    public static JSONObject queryService(String url) {
        try {
            JSONObject serviceResponseObject = new JSONObject(httpResponseAsString(url));
            return serviceResponseObject;
        } catch (java.io.FileNotFoundException missingFileException) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("The API service says there is no entry at '" + url + "'.");
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to get a valid JSON object from API service at '" + url + "'.", e); 
            }
        }
        return null;
    }
    
    /**
     * Converts a JSON array to a String array, swapping DB values with "nice" 
     * values if a mapper is supplied.
     * 
     * @param a The JSON array to convert.
     * @param m A mapper for replacing strings in the JSON array. If null, no replacements are performed.
     * @return The given JSON array, converted to a string array.
     */
    public static String[] jsonArrayToStringArray(JSONArray a, Mapper m) {
        if (m == null)
            m = new Mapper();
        String[] sa = new String[a.length()];
        for (int i = 0; i < a.length(); i++) {
            try { sa[i] = m.getMapping(a.getString(i)); } catch (Exception e) { sa[i] = ""; }
        }
        return sa;
    }

    /**
     * Converts a list to a comma-separated string.
     * <p>
     * If needed, a mapper can be supplied, in which case the strings in the 
     * given list are attempted replaced by the mapped value.
     * 
     * @param list The list to convert to a comma-separated string.
     * @param m A mapper for replacing the strings in the list. If null, no replacements are performed.
     * @return The given list, converted to a string.
     */
    public static String listToString(List list, Mapper m) {
        return listToString(list, m, ", ");
    }

    /**
     * Converts a list to a string, where each item in the list separated by the 
     * given separator.
     * <p>
     * If needed, a mapper can be supplied, in which case the strings in the 
     * given list are attempted replaced by the mapped value.
     * 
     * @param list The list to convert to a comma-separated string.
     * @param m A mapper for replacing the strings in the list. If null, no replacements are performed.
     * @param separator The string to use for separating each item.
     * @return The given list, converted to a string.
     */
    public static String listToString(List list, Mapper m, String separator) {
        Iterator i = list.iterator();
        String s = "";
        while (i.hasNext()) {
            s += m != null ? m.getMapping((String)i.next()) : i.next();
            if (i.hasNext()) 
                s += separator;
        }
        return s;
    }
    
    /**
     * Capitalizes the first letter of the given string.
     * 
     * @param s The string to capitalize.
     * @return The given string, with the first letter capitalized.
     */
    public static String capitalizeFirstLetter(String s) {
        if (s == null)
            return null;
        if (s.length() < 1)
            return s;
        String firstChar = s.substring(0, 1);
        s = s.replaceFirst(firstChar, firstChar.toUpperCase());
        return s;
    }
    
    /**
     * Converts the given name to initials.
     * <p>
     * E.g.: "John Clayton M.", will convert to "J.C.M.".
     * 
     * @param name The name (any form) to convert to initials.
     * @return The initials of the given name.
     */
    public static String getInitials(String name) {
        String s = "";
        if (name == null || name.isEmpty())
            return s;
        
        String[] parts = name.trim().split("\\s"); // Split the given name on spaces
        for (int i = 0; i < parts.length; i++) {
            try {
                String part = parts[i].trim().replaceAll("\\.", ""); // Remove already present punctuations
                if (StringUtils.isAllUpperCase(part)) { // Assume a short form, e.g. WK or just W
                    for (int j = 0; j < part.length(); j++) {
                        s += part.charAt(j) + ".";
                    }
                } else { // Assume this is a regular name, e.g. Winfried
                    if (part.contains("-")) { // E.g. "Jan-Gunnar"
                        String[] hyphenatedParts = part.split("-"); // [Jan][Gunnar]
                        for (int k = 0; k < hyphenatedParts.length; k++) {
                            s += hyphenatedParts[k].charAt(0) + "." + ((k+1) < hyphenatedParts.length ? "-" : ""); // First iteration "J.-", next iteration "G.". End result: "J.-G."
                        }
                    } else {
                        s += part.charAt(0) + ".";
                    }
                }
            } catch (Exception e) {
                //
            }
        }
        
        return s;
    }
    
    /**
     * CSV-escapes double quotes and semicolon from the given string.
     * 
     * @param unescaped The string to escape.
     * @return The given string, safe to use in CSV.
     */
    public static String escapeCSV(String unescaped) {
        String s = unescaped;
        if (unescaped != null && (unescaped.contains("\"") || unescaped.contains(";"))) {
            s = "\"" + unescaped.replaceAll("\"", "\"\"") + "\"";
        }        
        return s;
    }
    
    /**
     * Gets the displayable name of the given language, ready for display in the
     * other given language. 
     * <p>
     * This method exists because of a shortcoming in 
     * java.util.Locale#getDisplayLanguage(java.util.Locale), which cannot 
     * display all relevant languages correctly.
     * 
     * @param lang The language to get the name for.
     * @param inLang The language to display in.
     * @return The lang language name, ready for display in the inLang language.
     * @see java.util.Locale#getDisplayLanguage(java.util.Locale) 
     */
    public static String getDisplayLanguage(Locale lang, Locale inLang) {
        try {
            if (inLang.toString().substring(0, 2).equals("no")) {
                String langStr = lang.toString().substring(0, 2);
                if (langStr.equalsIgnoreCase("en")) {
                    return "engelsk";
                } else if (langStr.equalsIgnoreCase("de")) {
                    return "tysk";
                } else if (langStr.equalsIgnoreCase("no")) {
                    return "norsk";
                } else if (langStr.equalsIgnoreCase("fr")) {
                    return "fransk";
                } else if (langStr.equalsIgnoreCase("es")) {
                    return "spansk";
                } else if (langStr.equalsIgnoreCase("it")) {
                    return "italiensk";
                } else {
                    throw new NullPointerException("Language not supported in this helper method, fallback to default.");
                }
            }
        } catch (Exception e) {
            // Do nothing, fallback to java.util.Locale#getDisplayLanguage(java.util.Locale)
        }
        return lang.getDisplayLanguage(inLang);
    }
    
    /**
     * Pulls parameters from the given URI string and returns them neatly in a 
     * map.
     * <p>
     * If no parameters were present, an empty map is returned.
     * 
     * @param uri The URI string to pull parameters from
     * @return The parameters pulled from the given URI string, or an empty map if none.
     */
    public static Map<String, List<String>> getQueryParametersFromString(String uri) {
        // The parameter map
        Map<String, List<String>> m = new HashMap<String, List<String>>();
        
        // Require that there was a URI string, and that it did contain a "?"
        if (uri == null || uri.isEmpty() || uri.indexOf("?") == -1)
            return m; // No? Then don't continue
        
        // Require that there was in fact something AFTER the "?" as well.
        uri = uri.substring(uri.indexOf("?"));
        if (uri.length() <= 1) {
            return m; // No? Then don't continue
        } else {
            uri = uri.substring(1); // OK! Now let's remove the trailing "?", so we're left with only the parameters
        }
        
        // Split on "&" to separate each parameter's key-value pair, then loop
        String[] params = uri.split("\\&");
        for (int i = 0; i < params.length; i++) {
            String[] keyVal = params[i].split("="); // Split on "=" to separate key and value
            String key = keyVal[0]; // Get the key
            m.put(key, new ArrayList<String>()); // Put the key in the map
            try {
                if (keyVal[1].contains(",")) {
                    String[] vals = keyVal[1].split(","); // Split the value on "," to separate multiple values, then loop
                    for (int j = 0; j < vals.length; j++) {
                        String val = vals[j]; // Get the single value string and ... 
                        m.get(key).add(val); // ... add it to the list
                    }
                } else {
                    m.get(key).add(keyVal[1]); // Add the single value string
                }
                
            } catch (Exception e) {}
        }
        
        return m;
    }
    /**
     * Converts the given map of parameter names and values to a string.
     * <p>
     * Any map key should be the parameter name, and its associated list should 
     * hold any and all parameter value(s).
     * <p>
     * Multiple parameter values will be comma-separated in the returned string.
     * 
     * @param m The parameter map.
     * @return A string representation of the the given parameter map.
     */
    public static String getParameterString(Map<String, List<String>> m) {
        String s = "";
        Iterator<String> iKeys = m.keySet().iterator();
        while (iKeys.hasNext()) {
            String key = iKeys.next();
            s += key + "=";
            List<String> vals = m.get(key);
            Iterator<String> iVals = vals.iterator();
            while (iVals.hasNext()) {
                s += iVals.next();
                if (iVals.hasNext()) 
                    s+= ",";
            }
            
            if (iKeys.hasNext())
                s += "&";
        }
        return s;
    }
    
    /**
     * Translates the given markdown-formatted string to HTML format.
     * <p>
     * If no markdown is found, the given string is returned unmodified. By 
     * default, the returned string will be wrapped in a paragraph tag.
     * 
     * @param s The (possibly) markdown-formatted string.
     * @param pWrapper Flag indicating if the return string should be wrapped in a paragraph.
     * @return The given string with markdown translated to HTML.
     */
    public static String markdownToHtml(String s, boolean pWrapper) {
        try { 
            Markdown4jProcessor md = new Markdown4jProcessor();
            String html = md.process(s);
            if (!pWrapper) { 
                // Remove all paragraph tags
                html = html.replaceAll("<p>", "").replaceAll("<\\/p>", "");
                // Also remove the trailing newline character
                if (html.endsWith("\n"))
                    html = html.substring(0, html.length()-1);
            }
            
            return html;
        } catch (Exception e) { 
            return s + "\n<!-- Exception while attempting to process markdown: " + e.getMessage() + " -->"; 
        }
    }
    
    /**
     * Translates the given markdown-formatted string to HTML format.
     * <p>
     * If no markdown is found, the given string is returned unmodified. The 
     * returned string will be wrapped in a paragraph tag.
     * 
     * @see #markdownToHtml(java.lang.String, boolean) 
     */
    public static String markdownToHtml(String s) {
        return markdownToHtml(s, true);
    }

    /** 
     * Swap the DB value with the "nice" value.
     */
    /*public static String swapJsonValueWithNiceValue(String dbValue) {
        String s = valueMappings.get(dbValue);
        if (s != null && !s.isEmpty())
            return s;
        return dbValue;
    }*/
    
    /**
     * Tests if the given language string matches the language of the given 
     * locale.
     * 
     * @param s The language string to test (e.g. "no").
     * @param matchLocale The language it must match to evaluate to true.
     * @return True if the given language string matches the language of the given locale, false if not.
     */
    public static boolean matchLanguage(String s, Locale matchLocale) {
        if (s == null || s.isEmpty())
            return false; 
        
        String matchLang = matchLocale.toString();
        
        if (matchLang.equalsIgnoreCase("no")) {
            return s.equalsIgnoreCase("no") || s.equalsIgnoreCase("nb");
        }
        
        return s.equalsIgnoreCase(matchLang);    
    }
    
    /**
     * Simplified filter for returning the property identified by the given
     * name, as read from the first object in the given array that also has a 
     * matching "lang" property.
     * <p>
     * The array is processed in incremental steps of 1, starting at index 0.
     * 
     * @param jarr The array that contains the objects to evaluate.
     * @param name The object property to evaluate.
     * @param locale Identifies the language to match against.
     * @return The named property of the first encountered object that matched the given language.
     * @throws JSONException In case anything goes wrong in the JSON parsing.
     */
    public static String getStringByLocale(JSONArray jarr, String name, Locale locale) throws JSONException {
        if (jarr == null || jarr.length() < 1)
            return null;
        
        for (int i = 0; i < jarr.length(); i++) {
            JSONObject o = jarr.getJSONObject(i);
            if (!o.has("lang") || !o.has(name))
                continue; // Missing either "lang" or <name> property, nothing to do ...
            String lang = o.getString("lang");
            if (matchLanguage(lang, locale)) { 
                //System.out.println("TS object: " + o.toString());
                return o.getString(name); // We have a match
            }
        }
        
        return null; // No match
    }
}
