/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.11.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.operatorservice;

import java.util.Map;
import java.util.Properties;

import org.oscm.types.enumtypes.LocalizedDataType;
import org.oscm.internal.base.BasePO;

/**
 * Presentation object for localized data.
 * 
 * @author goebel
 */
public class POLocalizedData extends BasePO {

    private static final long serialVersionUID = 6523257913467122062L;

    private Map<String, Properties> propertiesMap;

    private LocalizedDataType type;

    public void setPropertiesMap(Map<String, Properties> map) {
        this.propertiesMap = map;
    }

    public Map<String, Properties> getPropertiesMap() {
        return this.propertiesMap;
    }

    public LocalizedDataType getType() {
        return type;
    }

    public void setType(LocalizedDataType type) {
        this.type = type;
    }

}
