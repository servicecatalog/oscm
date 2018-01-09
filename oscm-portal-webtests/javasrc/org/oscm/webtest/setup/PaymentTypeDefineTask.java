/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-5-15                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPSetting;
import org.oscm.internal.vo.VOPaymentType;

/**
 * @author qiu
 * 
 */
public class PaymentTypeDefineTask extends WebtestTask {

    @Override
    public void executeInternal() throws Exception {

        OperatorService op = getServiceInterface(OperatorService.class);
        List<VOPSP> psps = op.getPSPs();
        for (VOPSP psp : psps) {
            if ("heidelpay".equals(psp.getId())) {
                return;
            }
        }
        VOPSP psp = preparePSP();

        VOPSP psp_saved = op.savePSP(psp);
        op.savePaymentType(psp_saved, preparePaymentType("CREDIT_CARD"));
        op.savePaymentType(psp_saved, preparePaymentType("DIRECT_DEBIT"));
    }

    private VOPSP preparePSP() {
        VOPSP psp = new VOPSP();
        psp.setId("heidelpay");
        psp.setWsdlUrl("http://www.yoursite.com");
        psp.setPspSettings(preparePSPSettings());
        return psp;
    }

    private VOPaymentType preparePaymentType(String paymentTypeId) {
        VOPaymentType paymentType = new VOPaymentType();
        paymentType
                .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        paymentType.setPaymentTypeId(paymentTypeId);
        paymentType.setName(paymentTypeId);
        return paymentType;
    }

    private List<VOPSPSetting> preparePSPSettings() {
        List<VOPSPSetting> pspSettings = new ArrayList<VOPSPSetting>();
        pspSettings.add(preparePSPSetting("PSP_POST_URL"));
        pspSettings.add(preparePSPSetting("PSP_RESPONSE_SERVLET_URL"));
        pspSettings.add(preparePSPSetting("PSP_USER_PWD"));
        pspSettings.add(preparePSPSetting("PSP_USER_LOGIN"));
        pspSettings.add(preparePSPSetting("PSP_TRANSACTION_CHANNEL"));
        pspSettings.add(preparePSPSetting("PSP_SECURITY_SENDER"));
        pspSettings.add(preparePSPSetting("PSP_XML_URL"));
        pspSettings.add(preparePSPSetting("PSP_TXN_MODE"));
        pspSettings.add(preparePSPSetting("BASE_URL"));
        pspSettings.add(preparePSPSetting("PSP_SUPPORTED_DD_COUNTRIES"));
        pspSettings.add(preparePSPSetting("PSP_SUPPORTED_CC_BRANDS"));
        pspSettings.add(preparePSPSetting("PSP_PAYMENT_REGISTRATION_WSDL"));
        pspSettings.add(preparePSPSetting("PSP_FRONTEND_JS_PATH"));
        return pspSettings;
    }

    private VOPSPSetting preparePSPSetting(String settingKey) {
        VOPSPSetting pspSetting = new VOPSPSetting();
        pspSetting.setSettingKey(settingKey);
        pspSetting.setSettingValue("");
        return pspSetting;
    }
}
