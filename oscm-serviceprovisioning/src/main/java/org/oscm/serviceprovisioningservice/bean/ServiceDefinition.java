/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 15.02.2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.serviceprovisioningservice.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * This local class holds the model as needed for importing a marketable service
 * definition.
 * 
 * @author goebel
 */
class ServiceDefinition {
    private VOServiceDetails service = new VOServiceDetails();
    private List<VOLocalizedText> serviceShortDescriptionLocalizations = new ArrayList<VOLocalizedText>();
    private List<VOLocalizedText> serviceDescriptionLocalizations = new ArrayList<VOLocalizedText>();
    private List<VOLocalizedText> serviceNameForCustomerLocalizations = new ArrayList<VOLocalizedText>();
    private List<VOLocalizedText> priceModelDescriptionLocalizations = new ArrayList<VOLocalizedText>();
    private List<VOLocalizedText> serviceLicenseLocalizations = new ArrayList<VOLocalizedText>();
    private List<VOLocalizedText> priceModelShortDescriptionLocalizations = new ArrayList<VOLocalizedText>();
    private VOMarketplace marketplace;
    private List<String> failurePaths = new ArrayList<String>();

    protected ServiceDefinition() {
    }

    protected VOServiceDetails getService() {
        return service;
    }

    protected void setService(VOServiceDetails service) {
        this.service = service;
    }

    protected VOMarketplace getMarketplace() {
        return marketplace;
    }

    protected void setMarketplace(VOMarketplace marketplace) {
        this.marketplace = marketplace;
    }

    protected List<String> getFailurePaths() {
        return failurePaths;
    }

    protected void setFailurePaths(List<String> failurePaths) {
        this.failurePaths = failurePaths;
    }

    protected void setServiceNameForCustomer(VOLocalizedText localizedText) {
        setOrAdd(serviceNameForCustomerLocalizations, localizedText);
    }

    /**
     * Return the list of localized service names for customer.
     */
    protected List<VOLocalizedText> getServiceNameForCustomer() {
        return serviceNameForCustomerLocalizations;
    }

    /**
     * Return the list of localized licenses.
     */
    protected List<VOLocalizedText> getServiceLicense() {
        return serviceLicenseLocalizations;
    }

    protected void setServiceShortDescription(VOLocalizedText localizedText) {
        setOrAdd(serviceShortDescriptionLocalizations, localizedText);
    }

    /**
     * Return the localized service short description for the given locale or
     * <code>null</code> if not defined.
     */
    protected List<VOLocalizedText> getServiceShortDescription() {
        return serviceShortDescriptionLocalizations;
    }

    protected void setServiceDescription(VOLocalizedText localizedText) {
        setOrAdd(serviceDescriptionLocalizations, localizedText);
    }

    protected List<VOLocalizedText> getServiceDescription() {
        return serviceDescriptionLocalizations;
    }

    protected void setPriceModelDescription(VOLocalizedText localizedText) {
        setOrAdd(priceModelDescriptionLocalizations, localizedText);
    }

    protected List<VOLocalizedText> getPriceModelDescription() {
        return priceModelDescriptionLocalizations;
    }

    /**
     * Return the list of localized licenses.
     */
    protected List<VOLocalizedText> getPriceModelShortDescription() {
        return priceModelShortDescriptionLocalizations;
    }

    /**
     * Helper for collecting localizations
     */
    private void setOrAdd(Collection<VOLocalizedText> collection,
            VOLocalizedText localizedText) {
        for (VOLocalizedText voLt : collection) {
            if (voLt.getLocale().equals(localizedText.getLocale())) {
                voLt.setText(localizedText.getText());
                return;
            }
        }
        collection.add(localizedText);
    }
}
