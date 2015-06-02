package no.npolar.data.api.mosj;

import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides access to human-readable, localized translations of service identifier
 * string. Also, provides access to localized date formats, various labels, etc.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 * @see Labels.properties
 */
public class Labels {

    /**  */
    public static final String CHART_MEDIAN_0 = "label.chart.median";
    /**  */
    public static final String CHART_MIN_0 = "label.chart.min";
    /**  */
    public static final String CHART_MAX_0 = "label.chart.max";
    /**  */
    public static final String CHART_HIGH_0 = "label.chart.high";
    /**  */
    public static final String CHART_LOW_0 = "label.chart.low";
    /**  */
    public static final String CHART_ERROR_0 = "label.chart.error";
    
    
    /**
     * Default constructor. Does nothing.
     */
    public Labels() {}
    
    /**
     * Gets the bundle name.
     * 
     * @return The bundle name.
     */
    public static String getBundleName() { return Labels.class.getCanonicalName(); }
}
