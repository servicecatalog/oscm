/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-19                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.techserviceoperationmgmt;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.techproductoperation.bean.OperationRecordServiceLocalBean;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author maoq
 * 
 */
public class OperationRecordServiceBeanTest {
    private OperationRecordServiceBean operationRecordService;
    private OperationRecordServiceLocalBean operationRecordServiceLocal;
    private DataService dataService;

    @Before
    public void setup() {
        operationRecordService = spy(new OperationRecordServiceBean());
        operationRecordServiceLocal = mock(OperationRecordServiceLocalBean.class);
        dataService = mock(DataService.class);
        operationRecordService.operationRecordServiceLocalBean = operationRecordServiceLocal;
        operationRecordService.dm = dataService;
    }

    @Test
    public void getOperationRecords_myOperationsOnly() throws Exception {
        // when
        operationRecordService.getOperationRecords(Boolean.TRUE,
                Locale.ENGLISH.getLanguage());
        // then
        verify(operationRecordServiceLocal, times(1)).getOperationRecords(
                eq(true));

    }

    @Test
    public void getOperationRecords() throws Exception {
        // when
        operationRecordService.getOperationRecords(Boolean.FALSE,
                Locale.ENGLISH.getLanguage());
        // then
        verify(operationRecordServiceLocal, times(1)).getOperationRecords(
                eq(false));

    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteOperationRecords_IllegalArgumentException()
            throws Exception {
        // given
        Subscription sub = new Subscription();
        sub.setKey(1000L);
        PlatformUser user = new PlatformUser();
        user.setKey(1001L);

        when(
                operationRecordService.dm.getReference(Subscription.class,
                        sub.getKey())).thenReturn(sub);
        when(
                operationRecordService.dm.getReference(PlatformUser.class,
                        user.getKey())).thenReturn(user);

        // when
        operationRecordService.deleteOperationRecords(null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteOperationRecords() throws Exception {
        // given
        doThrow(new ObjectNotFoundException())
                .when(operationRecordServiceLocal).deleteOperationRecords(
                        anyListOf(Long.class));

        List<POOperationRecord> operationRecords = new ArrayList<POOperationRecord>();
        POSubscription poSubscription = new POSubscription();
        poSubscription.setKey(1000L);
        Subscription sub = new Subscription();
        sub.setKey(1000L);

        POUser poUser = new POUser();
        poUser.setKey(1001L);
        PlatformUser user = new PlatformUser();
        user.setKey(1001L);

        POOperationRecord record = new POOperationRecord();
        record.setKey(1002L);
        record.setSubscription(poSubscription);
        record.setUser(poUser);
        operationRecords.add(record);

        when(
                operationRecordService.dm.getReference(Subscription.class,
                        record.getSubscription().getKey())).thenReturn(sub);

        when(
                operationRecordService.dm.getReference(PlatformUser.class,
                        record.getUser().getKey())).thenReturn(user);

        // when
        operationRecordService.deleteOperationRecords(operationRecords);

        // then
        verify(operationRecordServiceLocal, times(1)).deleteOperationRecords(
                anyListOf(Long.class));
    }
}
