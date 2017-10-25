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

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PaymentType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.psp.data.RegistrationData;
import org.oscm.psp.intf.PaymentRegistrationService;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.PaymentDataException;

/**
 * End point facade for WS.
 * 
 * @author Mike J&auml;ger
 * 
 */
@WebService(name = "PaymentRegistrationService", serviceName = "PaymentRegistrationService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.psp.intf.PaymentRegistrationService")
public class PaymentRegistrationServiceWS
        implements PaymentRegistrationService {

    Log4jLogger logger = LoggerFactory
            .getLogger(PaymentRegistrationServiceWS.class);

    DataService ds;
    PaymentRegistrationService delegate;
    WebServiceContext wsContext;

    @Override
    public String register(RegistrationData result)
            throws ObjectNotFoundException, PaymentDataException,
            OperationNotPermittedException {
        ArgumentValidator.notNull("result", result);

        // authorize caller
        String certDN = wsContext.getUserPrincipal().getName();
        long paymentTypeKey = result.getPaymentTypeKey();
        PaymentType paymentType;
        try {
            paymentType = ds.getReference(PaymentType.class, paymentTypeKey);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
        if (!certDN.equals(paymentType.getPsp().getDistinguishedName())) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    String.format(
                            "Caller with dn '%s' is not permitted to save an external identifier for payment type with key '%s'.",
                            certDN, Long.valueOf(paymentTypeKey)));
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.ACCESS_LOG, onp,
                    LogMessageIdentifier.WARN_SET_PSP_ID_UNAUTHORIZED, certDN,
                    String.valueOf(paymentTypeKey));
            throw onp;
        }
        return delegate.register(result);
    }
}
