/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package internal;

import javax.jws.WebService;

import org.oscm.intf.DiscountService;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.VODiscount;

/**
 * This is a stub implementation of the {@link DiscountService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author farmaki
 */

@WebService(serviceName = "DiscountService", targetNamespace = "http://oscm.org/xsd", portName = "DiscountServicePort", endpointInterface = "org.oscm.intf.DiscountService")
public class DiscountServiceImpl implements DiscountService {

    @Override
    public VODiscount getDiscountForService(long serviceKey)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VODiscount getDiscountForCustomer(String customerId)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

}
