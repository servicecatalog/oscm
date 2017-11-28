/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.junit.Test;

import org.oscm.auditlog.util.AuditLogSerializer;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogOperation.Operation;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogOperation.PriceModelType;

public class PriceModelAuditLogOperationTest {
    @Test
    public void testOperationIds_existsInMessagePropertiesFile() {
        // given
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                AuditLogSerializer.AUDITLOG_MESSAGE_RESOURCE_NAME,
                Locale.ENGLISH);

        // when
        for (PriceModelAuditLogOperation operation : PriceModelAuditLogOperation
                .values()) {
            resourceBundle.getString(operation.getOperationId());
        }

        // then no exception
    }

    @Test
    public void testOperationIds_isUnique() {
        Set<String> operationIds = new HashSet<String>();
        for (PriceModelAuditLogOperation operation : PriceModelAuditLogOperation
                .values()) {
            assertTrue(operationIds.add(operation.getOperationId()));
        }
    }

    @Test
    public void testOperationIds_isWithin30000And39999() {
        for (PriceModelAuditLogOperation operation : PriceModelAuditLogOperation
                .values()) {
            int operationId = Integer.parseInt(operation.getOperationId());
            assertTrue(operationId >= 30000 && operationId <= 39999);
        }
    }

    @Test
    public void testOperation() {
        for (Operation o : Operation.values()) {
            for (PriceModelType pm : PriceModelType.values()) {
                PriceModelAuditLogOperation op = PriceModelAuditLogOperation
                        .getOperation(o, pm);

                if (o == Operation.EDIT_ONETIME_FEE
                        && pm == PriceModelType.SUBSCRIPTION) {
                    continue;
                }

                if (o == Operation.DELETE_PRICE_MODEL
                        && (pm == PriceModelType.SERVICE || pm == PriceModelType.SUBSCRIPTION)) {
                    continue;
                }
                if ((o == Operation.LOCALIZE_PRICE_MODEL
                        || o == Operation.EDIT_CHARGEABLE_PRICE_MODEL || o == Operation.EDIT_FREE_PRICE_MODEL)
                        && (pm == PriceModelType.SUBSCRIPTION)) {
                    continue;
                }
                assertEquals(o.name() + "_FOR_" + pm.name(), op.name());
                assertEquals(o, op.getPriceModelOperation());
                assertEquals(pm, op.getPriceModelType());
            }
        }
    }
}
