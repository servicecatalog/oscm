/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Aug 26, 2011                                                      
 *                                                                              
 *  Completion Time: Aug 26, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.Set;

import sun.misc.BASE64Encoder;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.psp.data.RegistrationData;
import org.oscm.psp.intf.PaymentRegistrationService;

/**
 * Custom ANT task creating payment information using the WS-API.
 * 
 * @author Dirk Bernsau
 * 
 */
public class PaymentInfoDefineTask extends WebtestTask {

    private String truststorePwd = "changeit";
    private String keystorePwd = "changeit";
    private String paymentInfoId;
    private String paymentType = PaymentInfoType.INVOICE.toString();
    private String trustStorePath;
    private String keyStorePath;

    public void setPaymentType(String value) {
        paymentType = value;
    }

    public void setId(String value) {
        paymentInfoId = value;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public void setTruststorePwd(String truststorePwd) {
        this.truststorePwd = truststorePwd;
    }

    public void setKeystorePwd(String keystorePwd) {
        this.keystorePwd = keystorePwd;
    }

    @Override
    public void executeInternal() throws Exception {
        if (paymentInfoId == null || paymentInfoId.trim().length() == 0) {
            paymentInfoId = "PaymentInfo_" + System.currentTimeMillis();
        }

        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", truststorePwd);
        System.setProperty("javax.net.ssl.keyStore", keyStorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", keystorePwd);
        AccountService accSvc = getServiceInterface(AccountService.class);
        Set<VOPaymentType> pts = accSvc.getAvailablePaymentTypes();
        long ptKey = 0;
        for (VOPaymentType pt : pts) {
            if (paymentType.equals(pt.getPaymentTypeId())) {
                ptKey = pt.getKey();
                break;
            }
        }
        if (ptKey == 0) {
            throw new IllegalArgumentException("Payment type " + paymentType
                    + " was provided for " + getClass().getName()
                    + ". This is not a valid payment type.");
        }
        final RegistrationData pt = new RegistrationData();
        pt.setPaymentTypeKey(ptKey);
        pt.setIdentification("myExternalIdentifier");
        pt.setPaymentInfoId(new BASE64Encoder().encode(paymentInfoId
                .getBytes("UTF-8")));
        pt.setOrganizationKey(accSvc.getOrganizationData().getKey());

        final PaymentRegistrationService paymentService = getServiceInterface(PaymentRegistrationService.class);
        paymentService.register(pt);
    }
}
