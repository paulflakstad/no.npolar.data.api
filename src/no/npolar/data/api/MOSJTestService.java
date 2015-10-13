package no.npolar.data.api;

import java.util.Locale;

/**
 * Service that communicates with the test service rather than the production 
 * service.
 * 
 * @author Paul-Inge Flakstad
 */
public class MOSJTestService extends MOSJService {    
    /**
     * Creates a new MOSJ test service instance, localized according to the 
     * given locale.
     * 
     * @param loc The preferred language.
     */
    public MOSJTestService(Locale loc, boolean secure) {
        super(loc, secure);
    }

    /**
     * @see APIService#getServiceBaseURL()
     */
    @Override
    public String getServiceBaseURL() {
        return getServiceProtocol() + "://" + "apptest.data.npolar.no:9000/" + SERVICE_PATH; 
    }
}
