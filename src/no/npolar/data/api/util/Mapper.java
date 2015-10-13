package no.npolar.data.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Mapper is intended populated with string-to-string mappings, and is 
 * typically used to translate the pre-defined strings used by the service into 
 * other, more "desirable" strings.
 * <p>
 * Such translations are necessary both for localization purposes and in order 
 * to avoid sub-optimal (or in some cases illegal) use of abbreviation and alike.
 * <p>
 * For more info on the global mappings, see {@link Labels the Labels} and its 
 * associated .properties files.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Mapper {
    /** Holds the mappings. */
    protected Map<String, String> m = null;

    /**
     * Creates a new mapper, using the locale contained in the given CmsAgent.
     */
    public Mapper() {
        m = new HashMap<String, String>();
    }
    
    /**
     * Adds all key-value mappings in the given string to this mapper.
     * <p>
     * All keys should be unique, and the format should be (e.g. for mapping country codes to country names):
     * SE:Sweden|NO:Norway|DK:Denmark|SF:Finland|US:United States of America
     * 
     * @param pipeSeparatedKeyValuePairs The mappings to add, as pipe-separated key-value pairs
     */
    public void addAllPipeSeparated(String pipeSeparatedKeyValuePairs) {
        List keyValPairs = new ArrayList(Arrays.asList(pipeSeparatedKeyValuePairs.split("\\|")));
        Map map = new HashMap<String, String>();
        Iterator iKeyValPairs = keyValPairs.iterator();
        while (iKeyValPairs.hasNext()) {
            String keyValPair = (String)iKeyValPairs.next();
            try {
                String[] keyValPairSplit = keyValPair.split(":");
                String key = keyValPairSplit[0];
                String val = keyValPairSplit[1];
                map.put(key, val);
            } catch (Exception e) {
                // Ignore ...
            }
        }
        m.putAll(map);
    }

    /**
     * Adds a singe mapping.
     * 
     * @param key The mapping key.
     * @param val The mapping value.
     */
    public void addMapping(String key, String val) {
        m.put(key, val);
    }

    /**
     * Gets the mapping identified by the given key.
     * 
     * @param key The mapping key.
     * @return The value mapped to the given key. If no such value exists, the given key is returned.
     */
    public String getMapping(String key) {
        String s = (String)m.get(key);
        if (s != null)
            return s;
        return key;
    }

    /**
     * Gets the map containing all mappings.
     * 
     * @return The map containing all mappings.
     */
    public Map<String, String> get() { return m; }

    /**
     * Gets the size of this mapper.
     * 
     * @return The size of this mapper.
     */
    public int size() { return m.size(); }

    /**
     * Flags whether or not this mapper has a value mapped to the given key.
     * 
     * @param key The key to check for.
     * @return True if this mapper has a value mapped to the given key, false if not.
     */
    public boolean hasMappingFor(String key) { return m.containsKey(key) && m.get(key) != null && !m.get(key).isEmpty(); }
}