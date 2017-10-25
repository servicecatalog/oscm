/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 07.10.2011                                                      
 *                                                                              
 *  Completion Time: 07.10.2011                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.i18nservice.local;

import java.util.Map;

import org.oscm.domobjects.enums.LocalizedObjectTypes;

/**
 * Container for all localized resources for one domain object.
 * 
 * @author cheld
 * 
 */
public class LocalizedDomainObject {

    long objKey;

    Map<LocalizedObjectTypes, String> localizedResources;

    public LocalizedDomainObject(long objKey,
            Map<LocalizedObjectTypes, String> localizedResources) {
        this.objKey = objKey;
        this.localizedResources = localizedResources;
    }

    public long getObjKey() {
        return objKey;
    }

    public void setObjKey(long objKey) {
        this.objKey = objKey;
    }

    public Map<LocalizedObjectTypes, String> getLocalizedResources() {
        return localizedResources;
    }

    public void setLocalizedResources(
            Map<LocalizedObjectTypes, String> localizedResources) {
        this.localizedResources = localizedResources;
    }

}
