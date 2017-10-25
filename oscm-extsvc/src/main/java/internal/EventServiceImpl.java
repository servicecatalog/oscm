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

import javax.jws.WebService;

import org.oscm.intf.EventService;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.VOGatheredEvent;

/**
 * This is a stub implementation of the {@link EventService} as the Metro jax-ws
 * tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Aleh Khomich
 */
@WebService(serviceName = "EventService", targetNamespace = "http://oscm.org/xsd", portName = "EventServicePort", endpointInterface = "org.oscm.intf.EventService")
public class EventServiceImpl implements EventService {

    @Override
    public void recordEventForInstance(String technicalProductId,
            String instanceId, VOGatheredEvent event)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordEventForSubscription(long subscriptionKey,
            VOGatheredEvent event) throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

}
