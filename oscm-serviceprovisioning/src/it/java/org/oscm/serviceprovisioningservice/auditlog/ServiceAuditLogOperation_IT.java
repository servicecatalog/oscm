/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.junit.Test;

import org.oscm.auditlog.util.AuditLogSerializer;

/**
 * 
 * @author Min CHEN
 * 
 */
public class ServiceAuditLogOperation_IT {
    @Test
    public void testOperationIds_existsInMessagePropertiesFile() {
        // given
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                AuditLogSerializer.AUDITLOG_MESSAGE_RESOURCE_NAME,
                Locale.ENGLISH);

        // when
        for (ServiceAuditLogOperation operation : ServiceAuditLogOperation
                .values()) {
            resourceBundle.getString(operation.getOperationId());
        }

        // then no exception
    }

    @Test
    public void testOperationIds_isUnique() {
        Set<String> operationIds = new HashSet<String>();
        for (ServiceAuditLogOperation operation : ServiceAuditLogOperation
                .values()) {
            assertTrue(operationIds.add(operation.getOperationId()));
        }
    }

    @Test
    public void testOperationIds_isWithin30000And39999() {
        for (ServiceAuditLogOperation operation : ServiceAuditLogOperation
                .values()) {
            int operationId = Integer.parseInt(operation.getOperationId());
            assertTrue(operationId >= 30000 && operationId <= 39999);
        }
    }
}
