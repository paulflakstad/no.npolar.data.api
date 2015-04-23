package no.npolar.data.api;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a single data unit. Can be e.g. "Number of polar bears" or 
 * "Degrees Celcius". 
 * 
 * A data unit may have two separate forms, one long and one short, e.g. 
 * "Degrees Celcius" (long) and "°C" (short).
 * 
 * @deprecated Use {@link no.npolar.data.api.TimeSeriesDataUnit} instead.
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class MOSJDataUnit {
    private String shortForm = null;
    private String longForm = null;
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(MOSJDataUnit.class);
    
    public MOSJDataUnit(String shortForm, String longForm) {
        this.shortForm = shortForm;
        this.longForm = longForm;
    }
    
    public String getShortForm() { return this.shortForm; }
    public String getLongForm() { return this.longForm; }
    
    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode += 31 * shortForm.hashCode();
        hashCode += 31 * longForm.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MOSJDataUnit))
            return false;
        
        if (obj == this)
            return true;

        MOSJDataUnit that = (MOSJDataUnit) obj;
        return this.longForm.equals(that.longForm) && this.shortForm.equals(that.shortForm);
    }
    
    @Override
    public String toString() {
        return getShortForm();
    }
}

