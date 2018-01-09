/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 24, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.trigger.config;

/**
 * @author miethaner
 *
 */
public class TriggerCommonParams {

    // trigger path names
    public static final String PATH_DEFINITIONS = "/triggers";
    public static final String PATH_PROCESSES = "/processes";
    public static final String PATH_TRIGGER_APPROVE = "/approve";
    public static final String PATH_TRIGGER_REJECT = "/reject";
    public static final String PATH_ACTIONS = "/actions";

    // properties
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_SUSPEND = "suspend";
    public static final String PROPERTY_TARGET_URL = "target_url";
    public static final String PROPERTY_ACTION = "action";
    public static final String PROPERTY_TARGET_TYPE = "target_type";
    public static final String PROPERTY_COMMENT = "comment";

    private TriggerCommonParams() {
    }
}
