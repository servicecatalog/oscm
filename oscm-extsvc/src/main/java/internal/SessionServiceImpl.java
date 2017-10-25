/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package internal;

import java.util.List;

import javax.jws.WebService;

import org.oscm.intf.SessionService;

/**
 * This is a stub implementation of the {@link SessionService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Aleh Khomich
 */
@WebService(serviceName = "SessionService", targetNamespace = "http://oscm.org/xsd", portName = "SessionServicePort", endpointInterface = "org.oscm.intf.SessionService")
public class SessionServiceImpl implements SessionService {

    @Override
    public void createPlatformSession(String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createServiceSession(long subscriptionKey, String sessionId,
            String userToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int deletePlatformSession(String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String deleteServiceSession(long subscriptionTKey, String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteSessionsForSessionId(String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getSubscriptionKeysForSessionId(String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String resolveUserToken(long subscriptionKey, String sessionId,
            String userToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteServiceSessionsForSubscription(long subscriptionKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNumberOfServiceSessions(long subscriptionKey) {
        throw new UnsupportedOperationException();
    }

}
