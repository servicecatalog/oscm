/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2012-09-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.data;

/**
 * Specifies the keys of the available controller configuration settings.
 */
public enum ControllerConfigurationKey {

    /**
     * The identifier of the organization registered in the platform which is
     * responsible for the controller.
     */
    BSS_ORGANIZATION_ID(true),

    /**
     * The numeric key of the user to be used for accessing the platform. The
     * user must belong to the organization which is responsible for the
     * controller and have the administrator role.
     */
    BSS_USER_KEY(false),

    /**
     * The identifier of the user to be used for accessing the platform. The
     * user must belong to the organization which is responsible for the
     * controller and have the administrator role.
     */
    BSS_USER_ID(false),

    /**
     * The password of the user to be used for accessing the platform.
     */
    BSS_USER_PWD(false);

    private boolean isMandatory;

    private ControllerConfigurationKey() {
        this(true);
    }

    private ControllerConfigurationKey(boolean mandatory) {
        isMandatory = mandatory;
    }

    /**
     * Indicates whether the configuration setting is mandatory.
     * 
     * @return <code>true</code> if the configuration of the controller is
     *         incomplete without the configuration setting, <code>false</code>
     *         otherwise
     */
    public boolean isMandatory() {
        return isMandatory;
    }
}
