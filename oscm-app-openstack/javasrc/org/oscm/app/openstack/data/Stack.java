/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-12-03                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class Stack {

    private String id;
    private String status;
    private Map<String, String> outputs;
    private String statusReason;

    /**
     * @param name
     *            the name of the stack
     */
    public Stack() {
        outputs = new HashMap<String, String>();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    public void addOutput(String key, String value) {
        if (key != null) {
            outputs.put(key, value);
        }
    }

    public String getOutput(String key) {
        return outputs.get(key);
    }

    public Map<String, String> getOutput() {
        return Collections.unmodifiableMap(outputs);
    }

    public void setStatusReason(String reason) {
        this.statusReason = reason;
    }

    public String getStatusReason() {
        return statusReason;
    }
}
