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

package org.oscm.webservices;

import java.security.Principal;

import javax.xml.ws.WebServiceContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PSP;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.psp.data.RegistrationData;
import org.oscm.psp.intf.PaymentRegistrationService;
import org.oscm.types.exceptions.OperationNotPermittedException;

public class PaymentRegistrationServiceBeanTest {

    private PaymentRegistrationServiceWS prs;
    private RegistrationData data;
    private String certDn = "dn=est";
    private PSP psp;

    @Before
    public void setup() throws Exception {
        prs = new PaymentRegistrationServiceWS();
        prs.wsContext = Mockito.mock(WebServiceContext.class);
        prs.delegate = Mockito.mock(PaymentRegistrationService.class);
        prs.ds = Mockito.mock(DataService.class);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(certDn);
        Mockito.when(prs.wsContext.getUserPrincipal()).thenReturn(principal);

        PaymentInfo pi = new PaymentInfo();
        PaymentType pt = new PaymentType();
        psp = new PSP();
        pt.setPsp(psp);
        pi.setPaymentType(pt);
        Mockito.when(
                prs.ds.getReference(Matchers.eq(PaymentType.class),
                        Matchers.eq(1L))).thenReturn(pi.getPaymentType());
        Mockito.doThrow(new ObjectNotFoundException()).when(prs.ds)
                .getReference(Matchers.eq(PaymentType.class), Matchers.eq(2L));

        data = new RegistrationData();
        data.setAccountNumber("12345");
        data.setIdentification("personalId");
        data.setProvider("provider");
        data.setPaymentInfoKey(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void register_NullInput() throws Exception {
        prs.register(null);
    }

    @Test(expected = org.oscm.types.exceptions.ObjectNotFoundException.class)
    public void register_NonExistingPaymentInfo() throws Exception {
        data.setPaymentTypeKey(2);
        prs.register(data);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void register_Unauthorized() throws Exception {
        data.setPaymentTypeKey(1);
        prs.register(data);
    }

    @Test
    public void register_Success() throws Exception {
        data.setPaymentTypeKey(1);
        psp.setDistinguishedName(certDn);
        prs.register(data);
    }

}
