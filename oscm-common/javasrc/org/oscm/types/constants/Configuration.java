/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Author: Mike J&auml;ger                                                      
 *
 *  Creation Date: 09.02.2009                                                      
 *
 *  Completion Time: 17.06.2009                                             
 *
 *******************************************************************************/

package org.oscm.types.constants;

import java.util.Arrays;
import java.util.List;

/**
 * Provides constants used in the context of the configuration setting service.
 *
 * @author Mike J&auml;ger
 */
public interface Configuration {

        /**
         * Fix context id that has to be used whenever the setting is meant to be
         * valid for all nodes.
         */
        public static final String GLOBAL_CONTEXT = "global";

        /**
         * Controller Ids used in oscm-app
         */
        public static final String AWS_CONTROLLER_ID = "ess.aws";
        public static final String AZURE_CONTROLLER_ID = "ess.azureARM";
        public static final String OPENSTACK_CONTROLLER_ID = "ess.openstack";
        public static final String SHELL_CONTROLLER_ID = "ess.shell";
        public static final String VMWARE_CONTROLLER_ID = "ess.vmware";

        /**
         * List of controllers that have canPing/ping functionality implemented
         */
        public static final List<String> pingableControllersList = Arrays
                .asList(
                        VMWARE_CONTROLLER_ID
                );
}
