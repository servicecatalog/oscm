/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-02-28                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.iaas;

import org.oscm.app.iaas.data.IaasContext;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.exceptions.ConfigurationException;

/**
 * Handles access to multi-dimensional service parameters.
 * <p>
 * Additional LServers can be provisioned and configured by using a certain
 * prefix for the property keys (e.g. LSERVER_#_).
 * <p>
 * For every such defined additional LServer a property handler of type
 * SubPropertyHandler is returned. It inherits all methods of the main property
 * handler and additionally provides an "activated" flag, which defines whether
 * this LServer has been enabled.
 */
public class SubPropertyHandler extends PropertyHandler {
    // Defines whether this sub entity bag has been activated
    private boolean enabled;

    /**
     * Internal constructor (for sub entities)
     * 
     * @throws ConfigurationException
     */
    SubPropertyHandler(ProvisioningSettings settings, String prefix,
            boolean enabled, IaasContext context) throws ConfigurationException {
        super(settings, new PropertyReader(settings.getParameters(), prefix));
        this.enabled = enabled;
        if (context != null) {
            getIaasContext().setVSystemStatus(context.getVSystemStatus());
            getIaasContext().add(context.getVSystemConfiguration());
        }
    }

    /**
     * Returns whether this sub-entity is enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }
}
