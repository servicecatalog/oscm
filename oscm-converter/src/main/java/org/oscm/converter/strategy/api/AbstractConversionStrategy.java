/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.converter.strategy.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Query;

import org.oscm.converter.api.DataServiceHolder;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.enums.LocalizedObjectTypes;

public abstract class AbstractConversionStrategy implements DataServiceHolder {

    private DataService dataService;

    @Override
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public DataService getDataService() {
        return dataService;
    }

    protected List<LocalizedResource> getLocalizedResource(Collection<LocalizedObjectTypes> objTypes, Long key, String locale) {

        String queryString =
                "SELECT * FROM LocalizedResource WHERE objectKey = :objectKey AND objectType IN (:objectType) AND locale=:locale";
        final Collection<String> convertedList = convertObjectTypeToString(objTypes);
        final Query query = getDataService().createNativeQuery(queryString, LocalizedResource.class);
        query.setParameter("locale", locale);
        query.setParameter("objectKey", key);
        query.setParameter("objectType", convertedList);
        List<LocalizedResource> result = query.getResultList();
        return result;
    }

    private Collection<String> convertObjectTypeToString(Collection<LocalizedObjectTypes> objTypes){
        Collection<String> convertedList = new ArrayList<>();
        for (LocalizedObjectTypes lot : objTypes) {
            convertedList.add(lot.name());
        }
        return convertedList;
    }
}
