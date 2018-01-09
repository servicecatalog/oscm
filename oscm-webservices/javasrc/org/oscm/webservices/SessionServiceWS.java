/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.webservices;

import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.intf.SessionService;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.ServiceParameterException;
import org.oscm.types.exceptions.ValidationException;

/**
 * End point facade for WS.
 * 
 * @author Aleh Khomich.
 * 
 */
@WebService(name = "SessionService", serviceName = "SessionService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.SessionService")
public class SessionServiceWS implements SessionService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(SessionServiceWS.class));

    org.oscm.internal.intf.SessionService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public void createPlatformSession(String sessionId)
            throws ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.createPlatformSession(sessionId);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void createServiceSession(long subscriptionKey, String sessionId,
            String userToken) throws ObjectNotFoundException,
            ServiceParameterException, OperationNotPermittedException,
            ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.createServiceSession(subscriptionKey, sessionId, userToken);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceParameterException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public int deletePlatformSession(String sessionId) {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.deletePlatformSession(sessionId);
    }

    @Override
    public String deleteServiceSession(long subscriptionKey, String sessionId) {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.deleteServiceSession(subscriptionKey, sessionId);
    }

    @Override
    public void deleteSessionsForSessionId(String sessionId) {
        WS_LOGGER.logAccess(wsContext, ds);
        delegate.deleteSessionsForSessionId(sessionId);
    }

    @Override
    public List<Long> getSubscriptionKeysForSessionId(String sessionId) {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.getSubscriptionKeysForSessionId(sessionId);
    }

    @Override
    public String resolveUserToken(long subscriptionKey, String sessionId,
            String userToken) {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.resolveUserToken(subscriptionKey, sessionId, userToken);
    }

    @Override
    public void deleteServiceSessionsForSubscription(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deleteServiceSessionsForSubscription(subscriptionKey);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public int getNumberOfServiceSessions(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.getNumberOfServiceSessions(subscriptionKey);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

}
