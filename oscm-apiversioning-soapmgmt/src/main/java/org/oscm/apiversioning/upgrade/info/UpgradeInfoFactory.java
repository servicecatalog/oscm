/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.upgrade.info;

import org.oscm.apiversioning.enums.ApiVersion;

/**
 * @author qiu
 * 
 */
public class UpgradeInfoFactory {

    public static UpgradeInfoGenerator getGenerator(ApiVersion version) {
        if (ApiVersion.VERSION_1_9.equals(version)) {
            return new UpgradeInfoVersion1();
        } else {
            throw new RuntimeException("No generator is found");
        }
    }
}
