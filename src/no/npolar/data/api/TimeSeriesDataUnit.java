package no.npolar.data.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a single data unit, for example "Number of polar bears" or 
 * "Degrees Celcius".
 * <p>
 * A data unit may have two separate forms, one long and one short, e.g. 
 * "Degrees Celcius" (long) and "°C" (short).
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class TimeSeriesDataUnit {
    /** The short form. */
    private String shortForm = null;
    /** The long form. */
    private String longForm = null;
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(TimeSeriesDataUnit.class);
    
    /**
     * Creates a new data unit instance.
     * 
     * @param shortForm The short form.
     * @param longForm The long form.
     */
    public TimeSeriesDataUnit(String shortForm, String longForm) {
        this.shortForm = shortForm;
        this.longForm = longForm;
    }
    
    /**
     * Gets the short form.
     * 
     * @return The short form.
     */
    public String getShortForm() { return this.shortForm; }
    /**
     * Gets the long form.
     * 
     * @return The long form.
     */
    public String getLongForm() { return this.longForm; }
    
    /**
     * @see Object#hashCode() 
     */
    @Override
    public int hashCode() {
        /*int hashCode = 1;
        hashCode = 31 * shortForm.hashCode();*/
        int hashCode = 31 * longForm.hashCode();
        return hashCode;
    }

    /**
     * @see Object#equals(java.lang.Object) 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TimeSeriesDataUnit))
            return false;
        
        if (obj == this)
            return true;

        TimeSeriesDataUnit that = (TimeSeriesDataUnit) obj;
        return this.longForm.equals(that.longForm) && this.shortForm.equals(that.shortForm);
    }
    
    /**
     * @see Object#toString() 
     */
    @Override
    public String toString() {
        return getShortForm();
    }
}