/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 26.02.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.serviceDetails;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.oscm.ui.model.Discount;
import org.oscm.ui.model.Organization;
import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.ServiceReview;
import org.oscm.internal.review.POServiceFeedback;

public class POServiceDetail implements Serializable {
    private static final long serialVersionUID = 4875317891929775320L;

    private Service selectedService;
    private List<Service> relatedServices;
    private POServiceFeedback selectedServiceFeedback;
    private List<ServiceReview> selectedServiceReviews;
    private Discount discount;
    private List<PricedEventRow> serviceEvents;
    private List<PricedParameterRow> serviceParameters;

    private Map<String, Organization> vendorAddresses;

    private Organization servicePartner;

    private Organization serviceSupplier;

    public Organization getServicePartner() {
        return servicePartner;
    }

    public void setServicePartner(Organization servicePartner) {
        this.servicePartner = servicePartner;
    }

    public Organization getServiceSupplier() {
        return serviceSupplier;
    }

    public void setServiceSupplier(Organization serviceSupplier) {
        this.serviceSupplier = serviceSupplier;
    }

    public POServiceDetail(List<PricedEventRow> serviceEvents) {
        this.serviceEvents = serviceEvents;
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(Service selectedService) {
        this.selectedService = selectedService;
    }

    public List<Service> getRelatedServices() {
        return relatedServices;
    }

    public void setRelatedServices(List<Service> relatedServices) {
        this.relatedServices = relatedServices;
    }

    public POServiceFeedback getSelectedServiceFeedback() {
        return selectedServiceFeedback;
    }

    public void setSelectedServiceFeedback(
            POServiceFeedback selectedServiceFeedback) {
        this.selectedServiceFeedback = selectedServiceFeedback;
    }

    public List<ServiceReview> getSelectedServiceReviews() {
        return selectedServiceReviews;
    }

    public void setSelectedServiceReviews(
            List<ServiceReview> selectedServiceReviews) {
        this.selectedServiceReviews = selectedServiceReviews;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public List<PricedEventRow> getServiceEvents() {
        return serviceEvents;
    }

    public void setServiceEvents(List<PricedEventRow> serviceEvents) {
        this.serviceEvents = serviceEvents;
    }

    public List<PricedParameterRow> getServiceParameters() {
        return serviceParameters;
    }

    public void setServiceParameters(List<PricedParameterRow> serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    public Map<String, Organization> getVendorAddresses() {
        return vendorAddresses;
    }

    public void setVendorAddresses(Map<String, Organization> vendorAddresses) {
        this.vendorAddresses = vendorAddresses;
    }
}
