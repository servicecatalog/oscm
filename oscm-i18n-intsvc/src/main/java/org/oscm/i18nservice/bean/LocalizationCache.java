/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                    
 *                                                                              
 *  Creation Date: 06.10.2011                                                      
 *                                                                              
 *  Completion Time: 06.10.2011                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.i18nservice.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizedDomainObject;

/**
 * Cache for localization resources. The cache is used to store prefetch
 * localization data in order to avoid many small SQL requests.
 * 
 * @author cheld
 * 
 */
class LocalizationCache {

    private Map<String, String> storage = new HashMap<String, String>();

    /**
     * Returns the localization for the
     */
    public String getText(long objectKey, LocalizedObjectTypes objectType) {
        return storage.get(createInternalKey(objectKey, objectType));
    }

    private String createInternalKey(long objectKey,
            LocalizedObjectTypes objectType) {
        return objectKey + " " + objectType;
    }

    /**
     * Ads the localizations for the given list of domain objects to this cache.
     */
    void put(List<LocalizedDomainObject> localizedDomainObjects) {
        for (LocalizedDomainObject localizedDomainObject : localizedDomainObjects) {
            put(localizedDomainObject.getObjKey(),
                    localizedDomainObject.getLocalizedResources());
        }
    }

    /**
     * Adds the localizations for the given domain object to this cache.
     */
    void put(long objKey, Map<LocalizedObjectTypes, String> localizedText) {
        for (Iterator<Entry<LocalizedObjectTypes, String>> iterator = localizedText
                .entrySet().iterator(); iterator.hasNext();) {
            Entry<LocalizedObjectTypes, String> localizedEntry = iterator
                    .next();
            String cacheKey = createInternalKey(objKey, localizedEntry.getKey());
            storage.put(cacheKey, localizedEntry.getValue());
        }
    }

    /**
     * Add the localization for the domain object and type.
     */
    void put(long objectKey, LocalizedObjectTypes objectType,
            String localizedValue) {
        String cacheKey = createInternalKey(objectKey, objectType);
        storage.put(cacheKey, localizedValue);
    }

}
