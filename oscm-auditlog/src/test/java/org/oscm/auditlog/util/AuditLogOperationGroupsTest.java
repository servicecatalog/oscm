/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-5-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.auditlog.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * @author Mao
 * 
 */
public class AuditLogOperationGroupsTest {

    @Test
    public void getAvailableOrganizationGroups() {
        // when
        Map<String, String> groups = new HashMap<String, String>();
        groups = AuditLogOperationGroups.getInstance()
                .getAvailableOperationGroups();

        // then
        assertAvailableOrganizationGroups(groups);
    }

    @Test
    public void getAvailableOperations() {
        // when
        Map<String, String> operations = new HashMap<String, String>();

        operations.putAll(AuditLogOperationGroups.getInstance()
                .getAvailableOperations());

        // then
        assertAvailableOperations(operations);
    }

    private void assertAvailableOrganizationGroups(Map<String, String> groups) {
        assertEquals(3, groups.size());

        assertEquals(
                Boolean.TRUE,
                new Boolean(groups.keySet().contains(
                        "OPERATIONS_ORGANIZATION_ADMIN")));
        assertEquals(
                Boolean.TRUE,
                new Boolean(groups.keySet().contains(
                        "OPERATIONS_ORGANIZATION_USER")));
        assertEquals(
                Boolean.TRUE,
                new Boolean(groups.keySet().contains(
                        "OPERATIONS_SERVICE_MANAGER")));
    }

    private void assertAvailableOperations(Map<String, String> operations) {
        for (String key : operations.keySet()) {
            assertEquals(Boolean.TRUE, Boolean.valueOf(key.startsWith("30")));
            assertNotNull(operations.get(key));
            assertEquals(Boolean.TRUE,
                    Boolean.valueOf(!operations.get(key).isEmpty()));
        }
        assertNotDuplicate(operations.values());
    }

    private void assertNotDuplicate(Collection<String> values) {
        Collection<String> valuesToCheck = new ArrayList<String>();
        valuesToCheck.addAll(values);
        for (String value : values) {
            boolean oneTimeContained = valuesToCheck.remove(value);
            assertTrue(oneTimeContained);
            boolean twoTimesContained = valuesToCheck.remove(value);
            assertEquals(String.format("Found duplicate value '%s' in %s!",
                    value,
                    AuditLogOperationGroups.AUDITLOG_MESSAGE_RESOURCE_NAME),
                    Boolean.FALSE, Boolean.valueOf(twoTimesContained));
        }

    }

    @Test
    public void noUnexpectedKeys() {
        for (String key : AuditLogOperationGroups.getInstance().resourceBundle
                .keySet()) {
            assertIfUnexpected(key);
        }
    }

    @Test
    public void subscribeToService_InAdminGroup_B10531() {
        final List<String> userOps = operationList("OPERATIONS_ORGANIZATION_USER");
        assertEquals(Boolean.FALSE, Boolean.valueOf(userOps.contains("30000")));

        final List<String> adminOps = operationList("OPERATIONS_ORGANIZATION_ADMIN");
        assertEquals(Boolean.TRUE, Boolean.valueOf(adminOps.contains("30000")));
    }

    private void assertIfUnexpected(String key) {
        assertTrue("Enexpected key " + key + " found in "
                + AuditLogOperationGroups.AUDITLOG_MESSAGE_RESOURCE_NAME
                + ".properties", isExpectedKey(key));
    }

    private List<String> operationList(String group) {
        final Map<String, String> groups = AuditLogOperationGroups
                .getInstance().getAvailableOperationGroups();
        return Arrays.asList(groups.get(group).split(","));
    }

    private boolean isExpectedKey(String key) {
        if (key.startsWith("30"))
            return true;

        if (key.startsWith("OPERATIONS_"))
            return true;
        // Check if it's a valid parameter
        try {
            Enum.valueOf(AuditLogParameter.class, key);
        } catch (IllegalArgumentException iallArg) {
            return false;
        }
        return true;
    }
}
