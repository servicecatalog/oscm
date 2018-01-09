/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.webservices;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.intf.DiscountService;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.VODiscount;

/**
 * End point facade for WS.
 * 
 * @author farmaki
 * 
 */
@WebService(name = "DiscountService", serviceName = "DiscountService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.DiscountService")
public class DiscountServiceWS implements DiscountService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(DiscountServiceWS.class));

    org.oscm.internal.intf.DiscountService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public VODiscount getDiscountForService(long serviceKey)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter
                    .convertToApi(delegate.getDiscountForService(serviceKey));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }

    }

    @Override
    public VODiscount getDiscountForCustomer(String customerId)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter
                    .convertToApi(delegate.getDiscountForCustomer(customerId));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

}
