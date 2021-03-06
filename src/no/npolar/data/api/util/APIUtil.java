package no.npolar.data.api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import no.npolar.data.api.APIEntry;
import no.npolar.data.api.APIEntry.TimestampPattern;
import no.npolar.data.api.APIService;
import org.opencms.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.markdown4j.Markdown4jProcessor;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import static no.npolar.data.api.APIEntry.Key.LANG_GENERIC;
import no.npolar.data.api.Labels;

/**
 * Norwegian Polar Institute Data Centre API utilities.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class APIUtil {
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(no.npolar.data.api.util.APIUtil.class);
    
    /** 
     * Regular expression pattern for matching anything that strongly resembles 
     * anything that should be interpreted as "Norwegian Polar Institute", 
     * somewhere in the test string.
     * <p>
     * Intended usage: <code>yourString.matches(REGEX_PATTERN_NPI)</code>
     * <p>
     * For example, the following test strings will match:
     * <ul>
     *  <li>NPI</li>
     *  <li>NP</li>
     *  <li>AWI, NPI</li>
     *  <li>AWI;NPI;NASA</li>
     *  <li>Norwegian Polar Institute</li>
     *  <li>Norwegian Polar Institute and NASA</li>
     * </ul>
     * 
     * @see #isNPIContributor() 
     */
    public static final String REGEX_PATTERN_NPI =
            // Start at the beginning, and use case-insensitive matching
            "^(?i)"
            // Require that the "interesting bit" is preceded by a delimiter 
            // character, if it's not at the start of the string
            + "(.*(,|;|\\s))?"
            // The interesting bit
            + "(NP(I)?|Norsk Polar(\\s)?institutt|Norwegian Polar Institute)"
            // Require that the "interesting bit" is at the end, or directly 
            // followed by a delimiter character
            + "($|(,|;|\\s).*)";
    
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
     * Creates a OOCSS-style class set consisting of a base class and modifier
     * classes of that base class.
     * <p>
     * Examples of invocations and their return strings:
     * <ul>
     * <li>("pagination__page", "next") : "pagination__page pagination__page--next"</li>
     * <li>("pagination__page", "prev", "inactive") : "pagination__page pagination__page--prev pagination__page--inactive"</li>
     * </ul>
     * @param baseClass The base class.
     * @param modifier The modifier.
     * @param modifiers Optional additional modifiers.
     * @return The ready-to-use class set.
     */
    public static String classMod(String baseClass, String modifier, String ... modifiers) {
        String s =  baseClass + " " + baseClass + "--" + modifier;
        for (String m : modifiers) {
            s += " " + baseClass + "--" + m;
        }
        return s;
    }
    
    /**
     * Tests the availability of the given URL by issuing a HTTP GET request and 
     * sniffing the response code.
     * 
     * @param testUrl The URL to test.
     * @param validResponseCodes A set of valid response codes. (Typically just [200]).
     * @param timeout The timeout, in milliseconds, for each GET request.
     * @param attempts The number of connection attempts (each one lasting the length of timeout).
     * @return true if the URL is available, false if not.
     * @throws MalformedURLException If it's not possible to construct a {@link URL} from the given testUrl.
     */
    public static boolean testAvailability(String testUrl, int[] validResponseCodes, int timeout, int attempts) throws MalformedURLException {
        int responseCode = 0;        
        URL url = new URL(testUrl);
        
        for (int i = 0; i < attempts; i++) {
            try {
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(timeout);
                connection.connect();
                responseCode = connection.getResponseCode();
                break;
            } catch (Exception e) {
            }
        }        
        
        for (int i = 0; i < validResponseCodes.length; i++) {
            if (responseCode == validResponseCodes[i]) {
                return true;
            }
        }
        return false;
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
            //System.out.println("Querying API @ " + url);
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
     * Takes a service URL in common form, and tries to "normalize" it to a 
     * machine-readable api.npolar.no URL.
     * <p>
     * Protocols of type "http(s)://" are preserved, and if no protocol was 
     * specified – e.g. the given serviceUrl starts with "//" – then the 
     * returned URL will also use this form. 
     * <p>
     * Examples (input / output):
     * <dl>
     * <dt>//data.npolar.no/indicator/timeseries/1fcec195-ce45-4257-976f-3064ecb7a287/edit</dt>
     * <dd>//api.npolar.no/indicator/timeseries/1fcec195-ce45-4257-976f-3064ecb7a287</dd>
     * <dt>//data.npolar.no/indicator/timeseries/1fcec195-ce45-4257-976f-3064ecb7a287</dt>
     * <dd>//api.npolar.no/indicator/timeseries/1fcec195-ce45-4257-976f-3064ecb7a287</dd>
     * <dt>http://data.npolar.no/indicator/timeseries/1fcec195-ce45-4257-976f-3064ecb7a287/edit</dt>
     * <dd>http://api.npolar.no/indicator/timeseries/1fcec195-ce45-4257-976f-3064ecb7a287</dd>
     * <dt>https://data.npolar.no/indicator/timeseries/1fcec195-ce45-4257-976f-3064ecb7a287/edit</dt>
     * <dd>https://api.npolar.no/indicator/timeseries/1fcec195-ce45-4257-976f-3064ecb7a287</dd>
     * <dt>https://api.npolar.no/indicator/timeseries/1fcec195-ce45-4257-976f-3064ecb7a287</dt>
     * <dd>https://api.npolar.no/indicator/timeseries/1fcec195-ce45-4257-976f-3064ecb7a287 (unchanged)</dd>
     * </dl>
     * 
     * @param serviceUrl
     * @return 
     */
    public static String toApiUrl(String serviceUrl) {
        if (serviceUrl == null) {
            return null;
        }
        String protocol = "";
        try {
            if (!serviceUrl.startsWith("//")) {
                protocol = serviceUrl.substring(0, serviceUrl.indexOf("//"));
                if (!protocol.matches("^(http|https):$")) {
                    protocol = "";
                }
            }
            serviceUrl = serviceUrl.substring(serviceUrl.indexOf("//"));
        } catch (Exception e) {
            // This is an illegal format
            if (LOG.isErrorEnabled()) {
                LOG.error("Error: Unable to normalize the given service URL '" + serviceUrl + "'. Misspelled/missing URL?");
            }
            return null;
        }
        if (serviceUrl.endsWith("/edit")) {
            try {
                serviceUrl = serviceUrl.substring(0, serviceUrl.length() - "/edit".length());
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Error stripping trailing '/edit' from the given service URL '" + serviceUrl + "'.");
                }
            }
        }
        if (serviceUrl.startsWith("//" + APIService.SERVICE_DOMAIN_NAME_HUMAN + "/")) {
            serviceUrl = serviceUrl.replace(APIService.SERVICE_DOMAIN_NAME_HUMAN, APIService.SERVICE_DOMAIN_NAME);
        }
        return (protocol.isEmpty() ? "" : protocol) + serviceUrl;
        //return "http:" + serviceUrl;
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
     * Gets the query string from the given URI.
     * <p>
     * If no query string is present, an empty string is returned.
     * 
     * @param theUri The URI - may or may not contain a query string.
     * @return The query string, if any, or an empty string if none.
     */
    public static String getQueryString(String theUri) {
        try {
            return theUri.substring(theUri.indexOf("?")).substring(1);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Pulls parameters from the given URI string and returns them neatly in a 
     * map.
     * <p>
     * If no parameters were present, an empty map is returned.
     * <p>
     * Note that each value in the returned map may contain multiple values 
     * split by e.g. the "AND" or "OR" delimiter (see {@link APIService.Delimiter#AND}).
     * 
     * @param uri The URI string to extract parameters from.
     * @return The parameters pulled from the given URI string, or an empty map if none.
     */
    public static Map<String, String> getParametersInQueryString(String uri) {
    //public static Map<String, List<String>> getParametersInQueryString(String uri) {
        // The parameter map
        Map<String, String> m = new HashMap<String, String>();
        //Map<String, List<String>> m = new HashMap<String, List<String>>();
        //System.out.println("#getParametersInQueryString invoked with '" + uri + "'");
        // Require that there was a URI string, and that it did contain a "?"
        if (uri == null || uri.isEmpty() || !uri.contains("?")) {
            //System.out.println("NULL or no query string: " + uri);
            return m; // Nope => return empty map
        }
        
        // Require that there was in fact something AFTER the "?" as well.
        String queryString = getQueryString(uri);
        if (queryString.isEmpty()) {
            return m; // Nope => return empty map
        }
            
        /*
        uri = uri.substring(uri.indexOf("?"));
        if (uri.length() <= 1) {
            return m; // No? Then don't continue
        } else {
            uri = uri.substring(1); // OK! Now let's remove the trailing "?", so we're left with only the parameters
        }*/
        
        // Split on "&" to separate each parameter's key-value pair, then loop
        String[] keyValPairs = queryString.split("\\&");
        for (int i = 0; i < keyValPairs.length; i++) {
            // Separate key and value, and inject into the map
            String[] keyVal = keyValPairs[i].split("=");
            String key = keyVal[0];
            
            // Handle cases of HTML-escaped ampersand
            if (key.startsWith("amp;")) {
                key = key.substring("amp;".length());
            }
            m.put(key, "");
            //System.out.print("#getParametersInQueryString MAPPING " + key);
            //m.put(key, new ArrayList<String>());
            // The try/catch is vital to handles cases of "key exists but not value" properly
            try {
                m.put(key, keyVal[1]);
                //System.out.println("=" + keyVal[1]);
                /*if (keyVal[1].contains(",")) {
                
                    //
                    // The routine below is BAD because the delimiter may 
                    // alo be "|", ".." (and possibly other ones)
                    //
                
                    String[] vals = keyVal[1].split(","); // Split the value on "," to separate multiple values, then loop
                    for (int j = 0; j < vals.length; j++) {
                        String val = vals[j]; // Get the single value string and ... 
                        m.get(key).add(val); // ... add it to the list
                    }
                } else {
                    m.get(key).add(keyVal[1]); // Add the single value string
                }*/
                
            } catch (Exception e) {
                //System.out.println("=[EMPTY]");
            }
        }
        //System.out.println("Done, returning " + m.size() + " mapped parameters");
        return m;
    }
    
    /**
     * Gets a list of all the parameter keys present in the given URI, if any.
     * 
     * @param uri The URI, which may or may not include a query string.
     * @return All the parameter keys present in the given URI, or empty list if none.
     */
    public static List<String> getParametersKeysInQueryString(String uri) {
        List<String> keys = new ArrayList<String>();
        
        // Require that there was a URI string, and that it did contain a "?"
        if (uri == null || uri.isEmpty() || !uri.contains("?"))
            return keys; // Nope => return empty list
        
        // Require that there was in fact something AFTER the "?" as well.
        String queryString = getQueryString(uri);
        if (queryString.isEmpty())
            return keys; // Nope => return empty list
        
        // Extract and store each key
        String[] keyValPairs = queryString.split("\\&");
        for (String keyValPair : keyValPairs) {
            try {
                keys.add(keyValPair.split("=")[0]);
            } catch (Exception e) {}
        }
        
        return keys;
    }
    
    /**
     * Converts the given map of parameter names and values to a string.
     * <p>
     * Any map key should be the parameter name, and its associated values. 
     * (Possibly multiple values delimited by a delimiter in 
     * {@link APIService.Delimiter}.)
     * 
     * @param keyValuePairs The parameter keys and associated values.
     * @return A string representation of the the given parameter map, ready to use in a URI.
     */
    public static String getParameterString(Map<String, String> keyValuePairs) {
    //public static String getParameterString(Map<String, List<String>> m) {
        String s = "";
        Iterator<String> iKeys = keyValuePairs.keySet().iterator();
        while (iKeys.hasNext()) {
            String key = iKeys.next();
            s += key + "=" + keyValuePairs.get(key) + (iKeys.hasNext() ? "&" : "");
            /*
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
            */
        }
        return s;
    }
    
    /**
     * Converts the given map of parameter names and values to a string.
     * <p>
     * Any map key should be the parameter name, and its associated values. 
     * (Possibly multiple values delimited by a delimiter in 
     * {@link APIService.Delimiter}.)
     * 
     * @param keyValuePairs The parameter keys and associated values.
     * @return A string representation of the the given parameter map, ready to use in a URI.
     */
    public static String toQueryString(Map<String, String> keyValuePairs) {
        String s = "";
        Iterator<String> iKeys = keyValuePairs.keySet().iterator();
        while (iKeys.hasNext()) {
            String key = iKeys.next();
            s += key + "=" + keyValuePairs.get(key) + (iKeys.hasNext() ? "&" : "");
        }
        return s;
    }
    
    /**
     * Converts the given parameter map to a parameter string, stripped of any 
     * parameters given as exclusions.
     * 
     * @param params The parameter map - will not be modified in any way (= safe to pass request.getParameterMap()).
     * @param exclusions (Optional) parameters to strip from the return string.
     * @return A parameter string, stripped of any parameters given as exclusions.
     */
    public static String toParameterString(Map<String, String[]> params, String ... exclusions) {
        if (params.isEmpty()) {
            return "";
        }

        List<String> e = new ArrayList<String>(0);
        for (String exclusion : exclusions) {
            e.add(exclusion);
        }

        String s = "";
        Iterator<String> i = params.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next(); // e.g. "facets" (parameter name)
            if (e.contains(key)) {
                continue;
            }
            String[] values = params.get(key); // e.g. get the parameter value(s) for "facets"
            s += (s.isEmpty() ? "" : "&") + key + "=" + APIService.combine(APIService.Delimiter.AND, values);
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
     * @param s The (possibly) markdown-formatted string.
     * @return The given string with markdown translated to HTML.
     * @see #markdownToHtml(java.lang.String, boolean) 
     */
    public static String markdownToHtml(String s) {
        try {         
            return markdownToHtml(s, true);
        } catch (Exception e) { 
            return s + "\n<!-- Could not process this as markdown: " + e.getMessage() + " -->"; 
        }
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
     * @param matchLocale The language it must match to evaluate to <code>true</code>.
     * @return <code>true</code> if the given language string matches the language of the given locale, <code>false</code> if not.
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
     * Determines if the given locale represents some kind of Norwegian.
     * 
     * @param locale the locale to evaluate.
     * @return <code>true</code> if the given locale represents some kind of Norwegian, <code>false</code> if not.
     */
    public static boolean localeIsNorwegian(Locale locale) {
        try {
            return locale.toLanguageTag().startsWith("no") 
                    || locale.toLanguageTag().startsWith("nb") 
                    || locale.toLanguageTag().startsWith("nn");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Determines if the given locale represents some kind of English.
     * 
     * @param locale the locale to evaluate.
     * @return <code>true</code> if the given locale represents some kind of English, <code>false</code> if not.
     */
    public static boolean localeIsEnglish(Locale locale) {
        try {
            return locale.toLanguageTag().startsWith("en");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Tries to extract a localized string from the given array of localization 
     * objects, by matching an object's defined language with the given locale.
     * <p>
     * Starting at the first object in the array, the language code defined in 
     * the object's {@link LANG_GENERIC} property is evaluated against the 
     * language code of the given locale. If they match, the string value of the 
     * property identified by the given name is returned.
     * <p>
     * If the given array is <code>null</code> or empty, or no match is found, 
     * <code>null</code> is returned.
     * <p>
     * Example array (of localized objects):
     * [
     *  { "title" : "My title", "lang" : "en" }, 
     *  { "title" : "En tittel", "lang" : "nb" }
     * ]
     * 
     * @param jarr An array of all the "localization objects" to evaluate. Each object must be a {@link JSONObject}.
     * @param name The name of the object's (string) property that we want the value of, e.g. "title".
     * @param locale Identifies the language to match against, e.g. "en".
     * @return The string value read from the named property of the first object that matched the given language, or <code>null</code> if none.
     * @throws JSONException In case anything goes wrong in the JSON parsing.
     * @see no.npolar.data.api.APIEntry.Key#LANG_GENERIC
     * @see #matchLanguage(java.lang.String, java.util.Locale) 
     */
    public static String getStringByLocale(JSONArray jarr, String name, Locale locale) throws JSONException {
        if (jarr == null || jarr.length() < 1)
            return null;
        
        for (int i = 0; i < jarr.length(); i++) {
            JSONObject o = jarr.getJSONObject(i);
            if (!o.has(LANG_GENERIC) || !o.has(name))
                continue; // Missing either "lang" or <name> property, nothing to do ...
            String lang = o.getString(LANG_GENERIC);
            if (matchLanguage(lang, locale)) { 
                //System.out.println("TS object: " + o.toString());
                return o.getString(name); // We have a match
            }
        }
        
        return null; // No match
    }
    
    /**
     * Tries to extract a localized string from the given localizations object, 
     * by matching the defined language with the given locale.
     * <p>
     * Starting at the object's first property, its name (= language code) is 
     * evaluated against the language code of the given locale. If they match, 
     * the string value of that property is returned.
     * <p>
     * If the given object has nothing that matches that language, the object's 
     * first localized string is returned.
     * <p>
     * If the object is empty, <code>null</code> is returned.
     * <p>
     * Example object: <code>{ "en" : "In English", "no" : "På norsk" }</code>
     * 
     * @param localizations An object containing 1-n localized strings, each one using the language code as the key.
     * @param locale Identifies the language to match against.
     * @return A closest-match localized string, as fetched from the given object, by matching object keys with the given language.
     * @throws JSONException In case anything goes wrong in the JSON parsing.
     * @see #matchLanguage(java.lang.String, java.util.Locale) 
     * @see no.npolar.data.api.APIEntry.Key#LANG_GENERIC
     */
    public static String getStringByLocale(JSONObject localizations, Locale locale) throws JSONException {
        if (localizations == null || localizations.length() == 0) {
            return null;
        }
        Iterator<String> langs = localizations.keys();
        while (langs.hasNext()) {
            String lang = langs.next();
            if (matchLanguage(lang, locale)) {
                return localizations.getString(lang);
            }
        }
        // fallback: return first localized string in object
        return localizations.getString(localizations.keys().next());
    }
    
    /**
     * Gets the string values of the named property, as read from each object in 
     * the given array that also has a matching {@link LANG_GENERIC} property.
     * <p>
     * Array iteration starts at index 0.
     * 
     * @param jarr An array of all objects to evaluate. Each object must be a {@link JSONObject}.
     * @param name The name of the object's string property that we want the value of.
     * @param locale Identifies the language to match against.
     * @return A list of strings, read from the named property of every object that matched the given language, or an empty list if none.
     * @throws JSONException In case anything goes wrong in the JSON parsing.
     * @see APIEntry.Key#LANG_GENERIC
     */
    public static List<String> getStringsByLocale(JSONArray jarr, String name, Locale locale) throws JSONException {
        
        List<String> matches = new ArrayList<String>(2);
        
        if (jarr != null) {
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject o = jarr.getJSONObject(i);
                if (!o.has(LANG_GENERIC) || !o.has(name)) {
                    continue; // Missing either "lang" or <name> property
                }
                String lang = o.getString(LANG_GENERIC);
                if (matchLanguage(lang, locale)) {
                    matches.add(o.getString(name)); // We have a match - add it
                }
            }
        }
        return matches; // No match
    }
    
    /**
     * Gets the date format for the given timestamp, if the timestamp follows a
     * pattern defined in {@link TimestampPattern}.
     * 
     * @param timestamp The timestamp, e.g. "2016-01-01" or "2015".
     * @return A date format that fits the given timestamp, and can be used to format/parse it.
     * @see TimestampPattern
     */
    public static SimpleDateFormat getTimestampFormat(String timestamp) {
        for (TimestampPattern t : TimestampPattern.values()) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(t.toString());
                f.parse(timestamp);
                return f;
            } catch (Exception e) {}
        }
        // Return default
        return new SimpleDateFormat(TimestampPattern.TIME.toString());
    }
    
    /**
     * Converts the given string to an "URL-friendly" string consisting only 
     * of lowercase letters in the a-z range, numbers, and periods (dots).
     * 
     * @param s The string to make "URL-friendly"
     * @return the given string, converted to an "URL-friendly" form.
     * @see http://spanish.typeit.org/
     */
    public static String toURLFriendlyForm(String s) {
        s = s.toLowerCase();
        
        String regex = "(æ|å|à|á|ä|â|ā|ã|ă|ą)";
        s = s.replaceAll(regex, "a");
        
        regex = "(ç|č)";
        s = s.replaceAll(regex, "c");
        
        regex = "(ď)";
        s = s.replaceAll(regex, "d");
        
        regex = "(è|é|ê|ě|ë|ē|ę)";
        s = s.replaceAll(regex, "e");
        
        regex = "(ğ)";
        s = s.replaceAll(regex, "g");
        
        regex = "(ì|ı|í|î|ï|ī)";
        s = s.replaceAll(regex, "i");
        
        regex = "(ɫ|ł)";
        s = s.replaceAll(regex, "l");
        
        regex = "(ñ|ƞ|ň|ń)";
        s = s.replaceAll(regex, "n");
        
        regex = "(ø|ö|ó|ò|ő|õ|ô|ō)";
        s = s.replaceAll(regex, "o");
        
        regex = "(ř)";
        s = s.replaceAll(regex, "r");
        
        regex = "(š|ş|ș|ś)";
        s = s.replaceAll(regex, "s");
        
        regex = "(ť|ţ|ț)";
        s = s.replaceAll(regex, "t");
        
        regex = "(ù|ú|û|ü|ů|ű|ū)";
        s = s.replaceAll(regex, "u");
        
        regex = "(ŵ|ẅ|ẃ|ẁ)";
        s = s.replaceAll(regex, "w");
        
        regex = "(ÿ|ý|ỳ|ÿ)";
        s = s.replaceAll(regex, "y");
        
        regex = "(ž|ź|ż)";
        s = s.replaceAll(regex, "z");
        
        regex = "(ß)";
        s = s.replaceAll(regex, "ss");
        
        regex = "(þ|ð)";
        s = s.replaceAll(regex, "th");
        
        // Remove all periods followed by a space, to avoid two consecutive 
        // periods (e.g. if the given string was "Kit M. Kovacs")
        s = s.replaceAll("\\.\\s", " ");
        // Remove trailing period (e.g. if the given string was "Kit M.")
        s = s.replaceAll("\\.$", "");        
        
        // Replace spaces, commas and various dashes with a dot.
        regex = "(\\s|-|–|—|,)";
        s = s.replaceAll(regex, ".");
        
        //regex = "('|`|’|´)";
        //s = s.replaceAll(regex, "");
        
        // Clean up consecutive dots
        s = s.replaceAll("\\.\\.", ".");
        
        // Finally, strip anything that's NOT a-z, 0-9 or a dot
        s = s.replaceAll("[^a-z0-9\\.]", "");
        
        return s;
    }
}
