/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.upgrade.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author qiuw.fnst
 * 
 */
public class VORecords {
    private Map<LocationInfo, List<ModificationDetail>> recordsMap;

    public Map<LocationInfo, List<ModificationDetail>> getRecordsMap() {
        return recordsMap;
    }

    public void setRecordsMap(
            Map<LocationInfo, List<ModificationDetail>> recordsMap) {
        this.recordsMap = recordsMap;
    }

    public List<ModificationDetail> getModificationDetailsForVO(Class<?> valueObject) {
        List<ModificationDetail> details = new ArrayList<ModificationDetail>();
        for (LocationInfo locationInfo : recordsMap.keySet()) {
            if (locationInfo.getClassName().equals(valueObject.getName())) {
                details = recordsMap.get(locationInfo);
            }
        }
        return details;
    }
}
