/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Nov 14, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 14, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOBillingContact;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.faces.application.FacesMessage.Severity;

import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentInfoBean
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class BillingContactBeanTest {

    private BillingContactEditBean bean;
    private AccountServiceStub accountServiceStub;
    private boolean concurrentDeletion = false;

    @Mock
    private BillingContactBean billingBeanMock;

    @Mock
    private BaseBean baseBean;

    @Before
    public void setup() {

        accountServiceStub = new AccountServiceStub() {

            @Override
            public void deleteBillingContact(VOBillingContact billingContact)
                    throws ObjectNotFoundException,
                    ConcurrentModificationException,
                    OperationNotPermittedException {
                if (concurrentDeletion) {
                    // throw exception to simulate concurrent deletion
                    throw new ObjectNotFoundException();
                }
            }
        };
        bean = new BillingContactEditBean();
        bean.setAccountService(accountServiceStub);
        bean.setBillingContactBean(billingBeanMock);
        bean.setBaseBean(baseBean);

        doNothing().when(billingBeanMock).setBillingContacts((Set<VOBillingContact>)anySet());
        doNothing().when(baseBean).addMessage(anyString(), any(Severity.class), anyString());
    }

    @Test
    public void testDeletePaymentInfo() throws Exception {
        concurrentDeletion = false;
        bean.deleteBillingContact();
        verify(baseBean, times(1)).addMessage(anyString(), any(Severity.class), anyString());
    }

    @Test
    public void testDeletePaymentInfoConcurrrency() throws Exception {
        concurrentDeletion = true;
        bean.deleteBillingContact();
        verify(baseBean, times(1)).addMessage(anyString(), any(Severity.class), anyString());
    }
}
