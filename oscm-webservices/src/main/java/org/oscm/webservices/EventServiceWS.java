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
package org.oscm.webservices;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.intf.EventService;
import org.oscm.types.exceptions.DuplicateEventException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOGatheredEvent;

/**
 * End point facade for WS.
 * 
 * @author Aleh Khomich.
 * 
 */
@WebService(name = "EventService", serviceName = "EventService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.EventService")
public class EventServiceWS implements EventService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(EventServiceWS.class));

    org.oscm.internal.intf.EventService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public void recordEventForInstance(String technicalProductId,
            String instanceId, VOGatheredEvent event)
            throws OrganizationAuthoritiesException, DuplicateEventException,
            ObjectNotFoundException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.recordEventForInstance(technicalProductId, instanceId,
                    VOConverter.convertToUp(event));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.DuplicateEventException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void recordEventForSubscription(long subscriptionKey,
            VOGatheredEvent event)
            throws DuplicateEventException, OrganizationAuthoritiesException,
            ObjectNotFoundException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.recordEventForSubscription(subscriptionKey,
                    VOConverter.convertToUp(event));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.DuplicateEventException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

}
