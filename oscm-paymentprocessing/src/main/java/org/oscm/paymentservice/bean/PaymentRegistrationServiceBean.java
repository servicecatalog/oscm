/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 13.10.2011                                                      
 *                                                                              
 *  Completion Time: 13.10.2011                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.bean;

import java.io.IOException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.apache.commons.codec.binary.Base64;

import org.oscm.dataservice.local.DataService;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.validation.ArgumentValidator;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOPaymentData;
import org.oscm.psp.data.RegistrationData;
import org.oscm.psp.data.RegistrationData.Status;
import org.oscm.psp.intf.PaymentRegistrationService;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.PaymentDataException;

/**
 * Service bean handling the callbacks from the PSP registration operations,
 * storing the PSP identification data along with the payment information
 * objects in BES.
 * 
 * @author Mike J&auml;ger
 */
@Stateless
@Remote(PaymentRegistrationService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class PaymentRegistrationServiceBean implements
        PaymentRegistrationService {

    @EJB(beanInterface = PaymentServiceLocal.class)
    protected PaymentServiceLocal payServLocal;

    @EJB(beanInterface = DataService.class)
    protected DataService ds;

    @Resource
    protected SessionContext sessionCtx;

    @Override
    public String register(RegistrationData result)
            throws ObjectNotFoundException, PaymentDataException,
            OperationNotPermittedException {
        ArgumentValidator.notNull("result", result);

        // call account service to store the information
        VOPaymentData paymentData = convertRegistrationData(result);
        if (result.getStatus() == Status.Success) {
            try {
                payServLocal
                        .savePaymentIdentificationForOrganization(paymentData);
            } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
                throw ExceptionConverter.convertToApi(e);
            } catch (org.oscm.internal.types.exception.PaymentDataException e) {
                throw ExceptionConverter.convertToApi(e);
            }
        }

        return "/public/pspregistrationresult.jsf?success="
                + result.getStatus().name();
    }

    private VOPaymentData convertRegistrationData(RegistrationData result) {
        VOPaymentData paymentData = new VOPaymentData();
        paymentData.setAccountNumber(result.getAccountNumber());
        paymentData.setIdentification(result.getIdentification());
        paymentData.setPaymentInfoKey(result.getPaymentInfoKey());
        if (result.getPaymentInfoId() != null) {
            try {
                paymentData.setPaymentInfoId(new String(Base64
                        .decodeBase64(result.getPaymentInfoId()), "UTF-8"));
            } catch (IOException e) {
                throw new SaaSSystemException(
                        "Unexpected base64 decode exception for paymentInfoId "
                                + result.getPaymentInfoId(), e);
            }
        }
        paymentData.setProvider(result.getProvider());
        paymentData.setOrganizationKey(result.getOrganizationKey());
        paymentData.setPaymentTypeKey(result.getPaymentTypeKey());
        return paymentData;
    }
}
