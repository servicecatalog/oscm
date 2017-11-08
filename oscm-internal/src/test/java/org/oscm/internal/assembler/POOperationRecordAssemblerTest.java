/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-18                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.OperationRecord;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.techserviceoperationmgmt.POOperationRecord;
import org.oscm.internal.types.enumtypes.OperationStatus;

/**
 * Unit test of POOperationRecordAssemblerTest
 * 
 * @author sun
 * 
 */
public class POOperationRecordAssemblerTest {

    private LocalizerFacade facade;

    @Before
    public void setup() {
        facade = mock(LocalizerFacade.class);
    }

    @Test
    public void toPOOperationRecord() throws Exception {
        // given
        OperationRecord operationRecord = prepareOperationRecord(123L,
                "transactionId");

        // when
        POOperationRecord poOperationRecord = POOperationRecordAssembler
                .toPOOperationRecord(operationRecord, facade);

        // then
        verifyPOWithDO(poOperationRecord, operationRecord);
    }

    @Test
    public void toPOOperationRecords() throws Exception {
        // given
        List<OperationRecord> operationRecords = prepareOperationRecords(2);

        // when
        List<POOperationRecord> poOperationRecords = POOperationRecordAssembler
                .toPOOperationRecords(operationRecords, facade);

        // then
        assertEquals(2, poOperationRecords.size());
        verifyPOWithDO(poOperationRecords.get(0), operationRecords.get(0));
        verifyPOWithDO(poOperationRecords.get(1), operationRecords.get(1));
    }

    private OperationRecord prepareOperationRecord(long key,
            String transactionId) {
        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setKey(key);
        operationRecord.setTransactionid(transactionId);
        operationRecord.setExecutiondate(new Date(1L));
        operationRecord.setStatus(OperationStatus.RUNNING);
        operationRecord
                .setTechnicalProductOperation(prepareTechnicalProductOperation(transactionId));
        operationRecord.setUser(preparePlatformUser(transactionId));
        operationRecord.setSubscription(prepareSubscription(transactionId));
        return operationRecord;
    }

    private List<OperationRecord> prepareOperationRecords(int OperationRecordNum) {
        List<OperationRecord> operationRecords = new ArrayList<OperationRecord>();
        for (int i = 0; i < OperationRecordNum; i++) {
            operationRecords.add(prepareOperationRecord(i, String.valueOf(i)));
        }
        return operationRecords;
    }

    private TechnicalProductOperation prepareTechnicalProductOperation(
            String operationId) {
        TechnicalProductOperation operation = new TechnicalProductOperation();
        operation.setOperationId(operationId);
        return operation;
    }

    private Subscription prepareSubscription(String subId) {
        Subscription sub = new Subscription();
        sub.setSubscriptionId(subId);
        return sub;
    }

    private PlatformUser preparePlatformUser(String userId) {
        PlatformUser user = new PlatformUser();
        user.setUserId(userId);
        return user;
    }

    private void verifyPOWithDO(POOperationRecord poOperationRecord,
            OperationRecord operationRecord) {
        assertEquals(poOperationRecord.getKey(), operationRecord.getKey());
        assertEquals(poOperationRecord.getExecutionDate(), operationRecord
                .getExecutiondate().getTime());
        assertEquals(poOperationRecord.getStatus(), operationRecord.getStatus());
        assertEquals(poOperationRecord.getTransactionId(),
                operationRecord.getTransactionid());
        assertEquals(poOperationRecord.getSubscriptionId(), operationRecord
                .getSubscription().getSubscriptionId());
        assertEquals(poOperationRecord.getUserId(), operationRecord.getUser()
                .getUserId());
    }
}
