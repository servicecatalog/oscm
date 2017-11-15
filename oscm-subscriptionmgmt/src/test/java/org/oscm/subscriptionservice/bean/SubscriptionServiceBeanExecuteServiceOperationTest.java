/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-2-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.OperationRecord;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.techproductoperation.bean.OperationRecordServiceLocalBean;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.internal.types.enumtypes.OperationStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationStateException;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTechnicalServiceOperation;
import org.oscm.operation.data.OperationResult;

/**
 * @author Qiu
 * 
 */
@SuppressWarnings("unchecked")
public class SubscriptionServiceBeanExecuteServiceOperationTest {

    private static final String P1 = "P1";
    private static final String P2 = "P2";
    private static final String P3 = "P3";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String ORGANIZATION_ID = "organizationId";
    private final List<VOLocalizedText> progress = new ArrayList<VOLocalizedText>();
    private SubscriptionServiceBean bean;
    private final OperationResult result = new OperationResult();
    private final Subscription subscription = new Subscription();
    private final VOSubscription voSubscription = new VOSubscription();
    private final VOInstanceInfo voInstance = new VOInstanceInfo();
    private final TechnicalProductOperation operation = new TechnicalProductOperation();
    private final VOTechnicalServiceOperation voOperation = new VOTechnicalServiceOperation();

    @Before
    public void setup() throws Exception {

        bean = new SubscriptionServiceBean();
        bean.manageBean = mock(ManageSubscriptionBean.class);
        bean.dataManager = mock(DataService.class);
        bean.operationRecordBean = mock(OperationRecordServiceLocalBean.class);
        bean.stateValidator = new ValidateSubscriptionStateBean();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setSubscriptionId("subscriptionId");
        when(
                bean.manageBean.executeServiceOperation(eq(subscription),
                        eq(operation), anyMap(), anyString())).thenReturn(
                result);
        when(bean.manageBean.loadSubscription(anyString(), anyLong()))
                .thenReturn(subscription);

        when(
                bean.dataManager.getReference(
                        eq(TechnicalProductOperation.class), anyLong()))
                .thenReturn(operation);

    }

    @Test
    public void executeServiceOperation() throws Exception {
        // when
        bean.executeServiceOperation(voSubscription, voOperation);
        // then
        verify(bean.manageBean, times(1)).executeServiceOperation(
                eq(subscription), eq(operation), anyMap(), anyString());

    }

    @Test(expected = ConcurrentModificationException.class)
    public void executeServiceOperation_Bug10769_ModifyParam() throws Exception {
        // given
        prepareOperationParams();
        operation.getParameters().remove(1);
        OperationParameter param = createOperationParameter(2L, P2, true,
                OperationParameterType.INPUT_STRING);
        operation.getParameters().add(param);
        // when
        bean.executeServiceOperation(voSubscription, voOperation);

    }

    @Test(expected = ConcurrentModificationException.class)
    public void executeServiceOperation_Bug10769_AddParam() throws Exception {
        // given
        prepareOperationParams();
        operation.getParameters().add(
                createOperationParameter(3L, P3, false,
                        OperationParameterType.INPUT_STRING));
        // when
        bean.executeServiceOperation(voSubscription, voOperation);

    }

    @Test
    public void updateAsyncOperationProgress() throws Exception {
        // when
        bean.updateAsyncOperationProgress(TRANSACTION_ID,
                OperationStatus.COMPLETED, progress);
        // then
        verify(bean.operationRecordBean, times(1))
                .updateOperationStatus(eq(TRANSACTION_ID),
                        eq(OperationStatus.COMPLETED), eq(progress));

    }

    @Test
    public void givenOperationRecord_sync() throws Exception {
        // when
        OperationRecord result = bean.givenOperationRecord(subscription,
                operation, new Date(), "transactionid", false);
        // then
        assertEquals(OperationStatus.COMPLETED, result.getStatus());

    }

    @Test
    public void givenOperationRecord_async() throws Exception {
        // when
        OperationRecord result = bean.givenOperationRecord(subscription,
                operation, new Date(), "transactionid", true);
        // then
        assertEquals(OperationStatus.RUNNING, result.getStatus());

    }

    @Test(expected = OperationNotPermittedException.class)
    public void updateAsyncOperationProgress_OperationNotPermittedException()
            throws Exception {
        // given
        doThrow(new OperationNotPermittedException()).when(
                bean.operationRecordBean).updateOperationStatus(anyString(),
                any(OperationStatus.class), anyListOf(VOLocalizedText.class));
        // when
        bean.updateAsyncOperationProgress(TRANSACTION_ID,
                OperationStatus.COMPLETED, progress);
    }

    @Test(expected = OperationStateException.class)
    public void updateAsyncOperationProgress_OperationStateException()
            throws Exception {
        // given
        doThrow(new OperationStateException()).when(bean.operationRecordBean)
                .updateOperationStatus(anyString(), any(OperationStatus.class),
                        anyListOf(VOLocalizedText.class));

        // when
        bean.updateAsyncOperationProgress(TRANSACTION_ID,
                OperationStatus.COMPLETED, progress);
    }

    @Test
    public void updateAsyncSubscriptionStatus() throws Exception {
        when(bean.manageBean.findSubscription(anyString(), anyString()))
                .thenReturn(subscription);
        bean.updateAsyncSubscriptionStatus(subscription.getSubscriptionId(),
                ORGANIZATION_ID, voInstance);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void updateAsyncSubscriptionStatus_ObjectNotFoundException()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(bean.manageBean)
                .findSubscription(eq(subscription.getSubscriptionId()),
                        eq(ORGANIZATION_ID));
        // when
        bean.updateAsyncSubscriptionStatus(subscription.getSubscriptionId(),
                ORGANIZATION_ID, voInstance);
    }

    private void prepareOperationParams() {

        operation.setParameters(new ArrayList<OperationParameter>());
        voOperation
                .setOperationParameters(new ArrayList<VOServiceOperationParameter>());
        operation.getParameters().add(
                createOperationParameter(0L, P1, true,
                        OperationParameterType.REQUEST_SELECT));
        operation.getParameters().add(
                createOperationParameter(1L, P2, false,
                        OperationParameterType.INPUT_STRING));
        voOperation
                .getOperationParameters()
                .add(createVOOperationParameter(
                        0L,
                        P1,
                        true,
                        org.oscm.internal.types.enumtypes.OperationParameterType.REQUEST_SELECT));
        voOperation
                .getOperationParameters()
                .add(createVOOperationParameter(
                        1L,
                        P2,
                        false,
                        org.oscm.internal.types.enumtypes.OperationParameterType.INPUT_STRING));

    }

    private OperationParameter createOperationParameter(long key, String id,
            boolean mandatory, OperationParameterType type) {
        OperationParameter op = new OperationParameter();
        op.setKey(key);
        op.setId(id);
        op.setMandatory(mandatory);
        op.setType(type);
        return op;
    }

    private VOServiceOperationParameter createVOOperationParameter(long key,
            String id, boolean mandatory,
            org.oscm.internal.types.enumtypes.OperationParameterType type) {
        VOServiceOperationParameter op = new VOServiceOperationParameter();
        op.setKey(key);
        op.setParameterId(id);
        op.setMandatory(mandatory);
        op.setType(type);
        return op;
    }
}
