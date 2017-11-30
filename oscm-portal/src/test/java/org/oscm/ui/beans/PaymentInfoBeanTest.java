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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage.Severity;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PaymentDeregistrationException;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.test.stubs.AccountServiceStub;

/**
 * Unit tests for PaymentInfoBean
 * 
 */
public class PaymentInfoBeanTest {

    @Ignore
    @Test
    public void dummyTest() {

    }

    private String addedMessageKey;
    private PaymentInfoBean bean;
    private PaymentInfoEditBean beanEdit;
    private AccountServiceStub accountServiceStub;
    private boolean concurrentDeletion, deregisterFailure = false;
    private List<VOPaymentInfo> storedPaymentInfos;
    private String pspResult = PaymentInfoBean.OUTCOME_PSP_SUCCESS;

    public PaymentInfoBeanTest() {
    }

    @Before
    public void setup() {

        storedPaymentInfos = new ArrayList<>();
        accountServiceStub = new AccountServiceStub() {

            @Override
            public void deletePaymentInfo(VOPaymentInfo paymentInfo) throws ObjectNotFoundException,
                    OperationNotPermittedException, ConcurrentModificationException, PaymentDeregistrationException {
                if (concurrentDeletion) {
                    // throw exception to simulate concurrent deletion
                    throw new ObjectNotFoundException();
                } else if (deregisterFailure) {
                    throw new PaymentDeregistrationException();
                }
            }

            @Override
            public List<VOPaymentInfo> getPaymentInfos() {
                return storedPaymentInfos;
            }
        };

        bean = spy(new PaymentInfoBean() {

            private static final long serialVersionUID = 2837407376320759286L;

            @Override
            protected void addMessage(String clientId, Severity severity, String key, Object[] params) {
                addedMessageKey = key;
            }
        });

        bean = spy(new PaymentInfoBean() {

            private static final long serialVersionUID = 2837407376320759286L;

            @Override
            protected void addMessage(String clientId, Severity severity, String key, Object[] params) {
                addedMessageKey = key;
            }
        });

        doReturn(pspResult).when(bean).getPSPResult();

        doReturn(accountServiceStub).when(bean).getAccountingService();

        beanEdit = new PaymentInfoEditBean() {
            @Override
            public void addMessage(String key) {
                addedMessageKey = key;
            }
        };
        beanEdit.setPaymentInfoBean(bean);
        beanEdit.setAccountService(accountServiceStub);
    }

    private void addSomePaymentInfos(String[] ids) {
        long key = 0L;
        for (String id : ids) {
            VOPaymentInfo pi = new VOPaymentInfo();
            pi.setId(id);
            pi.setKey(key++);
            storedPaymentInfos.add(pi);
        }
    }

    private VOPaymentInfo getStoredPaymentInfo(String id) {
        for (VOPaymentInfo storedPI : storedPaymentInfos) {
            if (storedPI.getId().equals(id))
                return storedPI;
        }
        return null;
    }

    @Test
    public void testDeletePaymentInfo() throws Exception {
        assertNull(addedMessageKey);
        concurrentDeletion = false;
        beanEdit.deletePaymentInfo();
        assertEquals(addedMessageKey, BaseBean.INFO_PAYMENT_INFO_DELETED);
    }

    @Test
    public void testDeletePaymentInfoConcurrrency() throws Exception {
        assertNull(addedMessageKey);
        concurrentDeletion = true;
        beanEdit.deletePaymentInfo();
        assertEquals(addedMessageKey, BaseBean.INFO_PAYMENT_INFO_DELETED_CONCURRENTLY);
    }

    @Test
    public void testSelectCreatedPaymentInfo_Bug8655() throws Exception {
        addSomePaymentInfos(new String[] { "SimplePay", "Test_Pay", "Test_Pay0", "Test_Pay_1", "Test_Pay_2",
                "Test_Pay_3" });
        pspResult = PaymentInfoBean.OUTCOME_PSP_SUCCESS;

        bean.getPaymentInfo().setId("SimplePay");
        bean.handlePspResult();
        VOPaymentInfo selectedPI = bean.getSelectedPaymentInfoForSubscription();
        assertEquals("SimplePay", selectedPI.getId());

        bean.getPaymentInfo().setId("Test_Pay0");
        bean.handlePspResult();
        selectedPI = bean.getSelectedPaymentInfoForSubscription();
        assertEquals("Test_Pay0", selectedPI.getId());

        // For existing ID, PI with highest key must be taken
        bean.getPaymentInfo().setId("Test_Pay");
        bean.handlePspResult();
        selectedPI = bean.getSelectedPaymentInfoForSubscription();
        assertEquals("Test_Pay_3", selectedPI.getId());

        // Change payment info highest key
        getStoredPaymentInfo("Test_Pay_2").setKey(2000L);

        // For existing ID, PI with highest key must be taken
        bean.getPaymentInfo().setId("Test_Pay");
        bean.handlePspResult();
        selectedPI = bean.getSelectedPaymentInfoForSubscription();
        assertEquals("Test_Pay_2", selectedPI.getId());

        // But, only version 0 considered!
        getStoredPaymentInfo("Test_Pay_2").setVersion(1);
        bean.getPaymentInfo().setId("Test_Pay");
        bean.handlePspResult();
        selectedPI = bean.getSelectedPaymentInfoForSubscription();
        assertEquals("Test_Pay_3", selectedPI.getId());

        // Test non existing ID (not saved, should not occur in practice)
        bean.getPaymentInfo().setId("Test");
        bean.handlePspResult();
        selectedPI = bean.getSelectedPaymentInfoForSubscription();
        assertNull(selectedPI);
    }

    @Test
    public void testInvalidateCacheOnDeletion_Bug8621() throws Exception {
        deregisterFailure = false;
        beanEdit.deletePaymentInfo();
        assertEquals(addedMessageKey, BaseBean.INFO_PAYMENT_INFO_DELETED);
        Mockito.verify(bean, Mockito.times(1)).resetCachedPaymentInfo();
    }

    @Test
    public void testInvalidateCacheOnDeletionFailure_Bug8621() throws Exception {
        deregisterFailure = true;
        try {
            beanEdit.deletePaymentInfo();
        } catch (PaymentDeregistrationException ex) {
            Mockito.verify(bean, Mockito.times(1)).resetCachedPaymentInfo();
        }
    }
}
