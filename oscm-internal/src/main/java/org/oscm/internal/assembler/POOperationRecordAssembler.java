/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-18                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.OperationRecord;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.techserviceoperationmgmt.POOperationRecord;
import org.oscm.internal.techserviceoperationmgmt.POSubscription;
import org.oscm.internal.techserviceoperationmgmt.POUser;

/**
 * Assembler to convert the POOperationRecord to domain object and vice versa.
 * 
 * @author sun
 * 
 */
public class POOperationRecordAssembler extends BasePOAssembler {

    /**
     * Create a new POOperationRecord object and fill the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param operationRecord
     *            The domain object containing the values to be set.
     * @return The created po.
     */
    public static POOperationRecord toPOOperationRecord(
            OperationRecord operationRecord, LocalizerFacade facade) {

        POOperationRecord poOperationRecord = new POOperationRecord();
        updatePresentationObject(poOperationRecord, operationRecord);
        fillPOOperationRecord(poOperationRecord, operationRecord, facade);

        return poOperationRecord;
    }

    /**
     * Create new POOperationRecord objects and fill the fields with the
     * corresponding fields from the given domain objects.
     * 
     * @param operationRecords
     *            The domain object containing the values to be set.
     * @return The created po.
     */
    public static List<POOperationRecord> toPOOperationRecords(
            List<OperationRecord> operationRecords, LocalizerFacade facade) {

        List<POOperationRecord> poOperationRecords = new ArrayList<POOperationRecord>();
        for (OperationRecord operationRecord : operationRecords) {
            poOperationRecords
                    .add(toPOOperationRecord(operationRecord, facade));
        }
        return poOperationRecords;
    }

    private static void fillPOOperationRecord(
            POOperationRecord poOperationRecord,
            OperationRecord operationRecord, LocalizerFacade facade) {
        poOperationRecord.setTransactionId(operationRecord.getTransactionid());
        poOperationRecord.setStatus(operationRecord.getStatus());
        poOperationRecord.setExecutionDate(operationRecord.getExecutiondate()
                .getTime());
        poOperationRecord.setStatusDesc(facade.getText(
                operationRecord.getKey(),
                LocalizedObjectTypes.OPERATION_STATUS_DESCRIPTION));
        poOperationRecord.setSubscription(fillPOSubscription(operationRecord
                .getSubscription()));
        poOperationRecord.setUser(fillPOUser(operationRecord.getUser()));
        poOperationRecord.setOperationId(facade.getText(operationRecord
                .getTechnicalProductOperation().getKey(),
                LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME));
    }

    private static POSubscription fillPOSubscription(Subscription subscription) {
        POSubscription po = new POSubscription();
        po.setKey(subscription.getKey());
        po.setVersion(subscription.getVersion());
        po.setSubscriptionId(subscription.getSubscriptionId());
        return po;
    }

    private static POUser fillPOUser(PlatformUser platformUser) {
        POUser po = new POUser();
        po.setKey(platformUser.getKey());
        po.setVersion(platformUser.getVersion());
        po.setUserId(platformUser.getUserId());
        return po;
    }
}
