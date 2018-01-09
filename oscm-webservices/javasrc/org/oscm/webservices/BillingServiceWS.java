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
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.intf.BillingService;
import org.oscm.types.enumtypes.BillingSharesResultType;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.ValidationException;

/**
 * End point facade for WS.
 * 
 * @author Aleh Khomich.
 * 
 */
@WebService(name = "BillingService", serviceName = "BillingService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.BillingService")
public class BillingServiceWS implements BillingService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(BillingServiceWS.class));

    DataService ds;
    org.oscm.internal.intf.BillingService delegate;
    WebServiceContext wsContext;

    @Override
    public byte[] getCustomerBillingData(Long from, Long to,
            List<String> customerIdList)
            throws OrganizationAuthoritiesException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.getCustomerBillingData(from, to, customerIdList);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public byte[] getRevenueShareData(Long from, Long to,
            BillingSharesResultType roleType)
            throws OrganizationAuthoritiesException,
            OperationNotPermittedException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.getRevenueShareData(from, to, EnumConverter.convert(
                    roleType,
                    org.oscm.internal.types.enumtypes.BillingSharesResultType.class));
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

}
