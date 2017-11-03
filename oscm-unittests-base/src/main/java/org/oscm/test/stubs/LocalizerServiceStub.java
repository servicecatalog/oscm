/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizedDomainObject;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.vo.VOLocalizedText;

public class LocalizerServiceStub implements LocalizerServiceLocal {

    @Override
    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkExistenceOfBundleFiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalizedTextFromBundle(LocalizedObjectTypes objectType,
            Marketplace shop, String localeString, String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalizedTextFromDatabase(String localeString,
            long objectKey, LocalizedObjectTypes objectType) {
        return null;
    }

    @Override
    public List<VOLocalizedText> getLocalizedValues(long objectKey,
            LocalizedObjectTypes objectType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties loadLocalizedPropertiesFromDatabase(long objectKey,
            LocalizedObjectTypes type, String localeString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties loadLocalizedPropertiesFromFile(String baseName,
            String localString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeLocalizedValues(long objectKey,
            LocalizedObjectTypes objectType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocalizedValues(long key, LocalizedObjectTypes type,
            List<VOLocalizedText> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean storeLocalizedResource(String localeString, long objectKey,
            LocalizedObjectTypes objectType, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void storeLocalizedResources(long objectKey,
            LocalizedObjectTypes objectType, List<VOLocalizedText> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<LocalizedObjectTypes, String> getLocalizedTextFromDatabase(
            String localeString, long objectKey,
            List<LocalizedObjectTypes> objectTypes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeLocalizedValue(long objectKey,
            LocalizedObjectTypes objectType, String localeString) {
    }

    @Override
    public List<LocalizedDomainObject> getLocalizedTextFromDatabase(
            String localeString, List<Long> objectKeys,
            List<LocalizedObjectTypes> objectTypes) {
        return Collections.emptyList();
    }
    
    @Override
    public LocalizedBillingResource getLocalizedBillingResource(
            String localeString, UUID objectID,
            LocalizedBillingResourceType resourceType) {
        return null;
    }

    @Override
    public LocalizedBillingResource getLocalizedPriceModelResource(
            String localeString, UUID objectId) {
        return null;
    }

}
