/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年1月30日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.upgrade.info;

import java.util.List;
import java.util.Map;

/**
 * @author qiu
 * 
 */
public class ServiceRecords {

    private Map<ServiceInfo, List<ModificationDetail>> recordsMap;

    public Map<ServiceInfo, List<ModificationDetail>> getRecordsMap() {
        return recordsMap;
    }

    public void setRecordsMap(
            Map<ServiceInfo, List<ModificationDetail>> recordsMap) {
        this.recordsMap = recordsMap;
    }

}
