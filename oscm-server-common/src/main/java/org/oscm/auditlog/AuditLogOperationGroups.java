/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mao                                   
 *                                                                              
 *  Creation Date: 04.05.2013                                                     
 *                                                                              
 *******************************************************************************/
package org.oscm.auditlog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Class for handling available log operations.
 * 
 * @author Mao
 */
public class AuditLogOperationGroups {
    static final String AUDITLOG_MESSAGE_RESOURCE_NAME = "AuditLogMessages";
    private static final String GROUP_PREFIX = "OPERATIONS_";
    private static final String OPERATION_PREFIX = "30";
    private static AuditLogOperationGroups instance;
    private Map<String, String> operationGroups = new HashMap<String, String>();
    private Map<String, String> operations = new HashMap<String, String>();
    ResourceBundle resourceBundle;
    
    private AuditLogOperationGroups() {
        resourceBundle = ResourceBundle.getBundle(
                AUDITLOG_MESSAGE_RESOURCE_NAME, Locale.ENGLISH);
        Iterator<String> iter = resourceBundle.keySet().iterator();
        while (iter.hasNext()) {
            String entry = iter.next();
            if (entry.startsWith(GROUP_PREFIX)) {
                operationGroups.put(entry, resourceBundle.getString(entry));
            } else if (entry.startsWith(OPERATION_PREFIX)) {
                operations.put(entry, resourceBundle.getString(entry));
            }
        }
    }

    public static AuditLogOperationGroups getInstance() {
        if (instance == null) {
            instance = new AuditLogOperationGroups();
        }
        return instance;
    }

    public Map<String, String> getAvailableOperationGroups() {
        return operationGroups;
    }

    public Map<String, String> getAvailableOperations() {
        return operations;
    }

}
