/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2014-9-18
 *
 *******************************************************************************/

package org.oscm.ui.dialog.mp.operations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.techserviceoperationmgmt.OperationRecordService;
import org.oscm.internal.techserviceoperationmgmt.POOperationRecord;
import org.oscm.internal.techserviceoperationmgmt.POSubscription;
import org.oscm.internal.techserviceoperationmgmt.POUser;
import org.oscm.internal.types.enumtypes.OperationStatus;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author maoq
 *
 */
public class OperationRecordCtrlTest {
    private OperationRecordCtrl ctrl;
    private OperationRecordModel model;
    private OperationRecordService operationRecordService;
    private SessionBean sessionBean;
    private UiDelegate ui;
    private UserBean userBean;

    @Before
    public void setup() throws Exception {

        this.operationRecordService = mock(OperationRecordService.class);
        this.sessionBean = mock(SessionBean.class);
        this.userBean = mock(UserBean.class);

        this.model = spy(new OperationRecordModel());
        this.ctrl = spy(new OperationRecordCtrl() {

            @Override
            public OperationRecordService getOperationRecordService() {
                return OperationRecordCtrlTest.this.operationRecordService;
            }

            @Override
            public OperationRecordModel getModel() {
                return OperationRecordCtrlTest.this.model;
            }

            @Override
            public boolean isFilterRenderRequired() {
                return true;
            }

            @Override
            protected void addDeletedMessage(){
            }

        });
        this.ui = spy(new UiDelegate() {

            @Override
            public void handleException(final SaaSApplicationException ex) {

            }

            @Override
            public Locale getViewLocale() {
                return Locale.ENGLISH;
            }

        });

        this.ctrl.setModel(this.model);
        this.ctrl.setUiDelegate(this.ui);
        this.ctrl.setSessionBean(this.sessionBean);
        this.ctrl.setUserBean(this.userBean);
    }

    @Test
    public void getInitialize() {
        this.ctrl.postConstruct();
        verify(this.ctrl, times(1)).loadOperationRecords();
    }

    @Test
    public void loadOperationRecords_myOperationsOnly_orgAdminOrSubOwner()
            throws Exception {
        // given
        when(this.ctrl.isMyOperationsOnly()).thenReturn(Boolean.TRUE);

        // when
        this.ctrl.loadOperationRecords();

        // then
        verify(this.operationRecordService, times(1)).getOperationRecords(
                eq(Boolean.TRUE), anyString());
    }

    @Test
    public void loadOperationRecords_myOperationsOnly_user() throws Exception {
        // given
        when(this.ctrl.isMyOperationsOnly()).thenReturn(Boolean.TRUE);

        // when
        this.ctrl.loadOperationRecords();

        // then
        verify(this.operationRecordService, times(1)).getOperationRecords(
                eq(Boolean.TRUE), anyString());
    }

    @Test
    public void loadOperationRecords_allOperations_orgAdminOrSubOwner()
            throws Exception {
        // given
        when(this.ctrl.isMyOperationsOnly()).thenReturn(Boolean.FALSE);

        // when
        this.ctrl.loadOperationRecords();

        // then
        verify(this.operationRecordService, times(1)).getOperationRecords(
                eq(Boolean.FALSE), anyString());

    }

    @Test
    public void loadOperationRecords_allOperations_user() throws Exception {
        // given
        when(this.ctrl.isMyOperationsOnly()).thenReturn(Boolean.FALSE);

        // when
        this.ctrl.loadOperationRecords();

        // then
        verify(this.operationRecordService, times(1)).getOperationRecords(
                eq(Boolean.FALSE), anyString());
    }

    @Test
    public void deleteOperations_noRecords() throws Exception {
        // given
        this.model.getOperationRecords().clear();

        // when
        this.ctrl.deleteOperations();

        // then
        verify(this.operationRecordService, never()).deleteOperationRecords(
                anyListOf(POOperationRecord.class));
    }

    @Test
    public void deleteOperations() throws Exception {
        // given
        this.model.setOperationRecords(this.prepareOperationRecords());

        // when
        this.ctrl.deleteOperations();

        // then
        verify(this.operationRecordService, times(1)).deleteOperationRecords(
                anyListOf(POOperationRecord.class));
    }

    @Test
    public void initOperationRecords() throws Exception {
        // given
        this.model.setOperationRecords(this.prepareOperationRecords());

        final POOperationRecord poRecord1 = new POOperationRecord();
        poRecord1.setTransactionId("TransactionId");

        final POOperationRecord poRecord2 = new POOperationRecord();
        poRecord2.setTransactionId("NewTransactionId");

        // when
        final List<OperationRecord> operationRecords = this.ctrl
                .initOperationRecords(Arrays.asList(poRecord1, poRecord2));

        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(operationRecords.get(0).isSelected()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(operationRecords.get(1).isSelected()));
    }

    private Collection<OperationRecord> prepareOperationRecords() {
        final List<OperationRecord> operationRecords = new ArrayList<OperationRecord>();
        final POOperationRecord poRecord = new POOperationRecord();
        poRecord.setKey(1000);
        poRecord.setOperationId("OperationID");
        poRecord.setStatus(OperationStatus.RUNNING);

        final POSubscription sub = new POSubscription();
        sub.setSubscriptionId("SubscriptionID");
        poRecord.setSubscription(sub);
        poRecord.setExecutionDate(new Date().getTime());

        final POUser user = new POUser();
        user.setUserId("Administrator");
        poRecord.setUser(user);
        poRecord.setStatusDesc("Status Description");
        poRecord.setTransactionId("TransactionId");
        final OperationRecord record = new OperationRecord(poRecord);
        record.setSelected(true);
        operationRecords.add(record);
        return operationRecords;
    }

}
