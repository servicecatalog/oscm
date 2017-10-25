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

import org.oscm.intf.BillingService;
import org.oscm.types.enumtypes.BillingSharesResultType;

/**
 * This is a stub implementation of the {@link BillingService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Aleh Khomich
 */
@WebService(serviceName = "BillingService", targetNamespace = "http://oscm.org/xsd", portName = "BillingServicePort", endpointInterface = "org.oscm.intf.BillingService")
public class BillingServiceImpl implements BillingService {

    @Override
    public byte[] getCustomerBillingData(Long from, Long to,
            List<String> customerIdList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getRevenueShareData(Long from, Long to,
            BillingSharesResultType resultType) {
        throw new UnsupportedOperationException();
    }

}
