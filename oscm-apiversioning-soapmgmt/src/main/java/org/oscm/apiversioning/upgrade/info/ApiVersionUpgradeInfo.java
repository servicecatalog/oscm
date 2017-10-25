/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 22, 2015                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.upgrade.info;

import java.util.HashMap;
import java.util.Map;

import org.oscm.apiversioning.enums.ApiVersion;

/**
 * @author qiu
 * 
 */
public class ApiVersionUpgradeInfo {

    public static final Map<ApiVersion, VORecords> VO_INFO = new HashMap<ApiVersion, VORecords>();
    public static final Map<ApiVersion, ServiceRecords> REQUEST_INFO = new HashMap<ApiVersion, ServiceRecords>();
    public static final Map<ApiVersion, ServiceRecords> RESPONSE_INFO = new HashMap<ApiVersion, ServiceRecords>();
    public static final Map<ApiVersion, ServiceRecords> EXCEPTION_INFO = new HashMap<ApiVersion, ServiceRecords>();

    static {
        registerUpgradeInfo(ApiVersion.VERSION_1_9);

    }

    private static void registerUpgradeInfo(ApiVersion v) {
        UpgradeInfoGenerator generator = UpgradeInfoFactory.getGenerator(v);
        VO_INFO.put(v, generator.generateVORecords());
        REQUEST_INFO.put(v, generator.generateRequestRecords());
        RESPONSE_INFO.put(v, generator.generateResponseRecords());
        EXCEPTION_INFO.put(v, generator.generateExceptionRecords());
    }

}
