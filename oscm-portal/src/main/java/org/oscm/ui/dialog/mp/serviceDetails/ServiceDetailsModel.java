/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.03.2013                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.serviceDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.oscm.ui.common.RolePriceHandler;
import org.oscm.ui.common.SteppedPriceHandler;
import org.oscm.ui.model.Discount;
import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.RoleSpecificPrice;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.ServiceReview;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.internal.partnerservice.POVendorAddress;
import org.oscm.internal.review.POServiceFeedback;
import org.oscm.internal.types.enumtypes.ServiceAccessType;

/**
 * all service detail information
 * 
 */
@ManagedBean
@SessionScoped
public class ServiceDetailsModel implements Serializable {

    private static final long serialVersionUID = -3000938669923416418L;

    String selectedServiceKey;
    private POVendorAddress servicePartner;
    private POVendorAddress serviceSupplier;
    List<Long> invisibleProductKeys;
    private PriceModelContent priceModelContent;

    boolean parametersWithSteppedPrices = false;
    boolean pricedEventsWithSteppedPrices = false;

    POServiceDetail poServiceDetail = new POServiceDetail(
            new ArrayList<PricedEventRow>());

    public String getSelectedServiceKey() {
        return selectedServiceKey;
    }

    public void setSelectedServiceKey(String selectedServiceKey) {
        this.selectedServiceKey = selectedServiceKey;
    }

    public boolean getDirectAccess() {
        return poServiceDetail.getSelectedService().getVO().getAccessType() == ServiceAccessType.DIRECT;
    }

    public Service getSelectedService() {
        return poServiceDetail.getSelectedService();
    }

    public void setSelectedService(Service selectedService) {
        this.poServiceDetail.setSelectedService(selectedService);
    }

    public ServiceDetailsModel() {
        super();
    }

    public void setRelatedServices(List<Service> relatedServices) {
        this.poServiceDetail.setRelatedServices(relatedServices);
    }

    public List<Service> getRelatedServices() {
        return this.poServiceDetail.getRelatedServices();
    }

    public POServiceFeedback getSelectedServiceFeedback() {
        return poServiceDetail.getSelectedServiceFeedback();
    }

    public void setSelectedServiceFeedback(
            POServiceFeedback selectedServiceFeedback) {
        this.poServiceDetail
                .setSelectedServiceFeedback(selectedServiceFeedback);
    }

    public int getCountSelectedServiceReviews() {
        if (this.getSelectedService() == null
                || this.getSelectedServiceFeedback() == null
                || this.getSelectedServiceFeedback().getReviews() == null)
            return 0;
        return this.getSelectedServiceFeedback().getReviews().size();
    }

    public List<ServiceReview> getSelectedServiceReviews() {
        return poServiceDetail.getSelectedServiceReviews();
    }

    public void setSelectedServiceReviews(List<ServiceReview> reviews) {
        this.poServiceDetail.setSelectedServiceReviews(reviews);
    }

    public Service getService() {
        return this.poServiceDetail.getSelectedService();
    }

    public Discount getDiscount() {
        return this.poServiceDetail.getDiscount();

    }

    public void setDiscount(Discount discount) {
        this.poServiceDetail.setDiscount(discount);
    }

    public void setServiceEvents(List<PricedEventRow> serviceEvents) {
        this.poServiceDetail.setServiceEvents(serviceEvents);

    }

    public List<PricedEventRow> getServiceEvents() {
        return poServiceDetail.getServiceEvents();
    }

    public boolean isParametersWithSteppedPrices() {
        return SteppedPriceHandler
                .isParametersWithSteppedPrices(poServiceDetail
                        .getServiceParameters());
    }

    public void setParametersWithSteppedPrices(boolean b) {
        this.parametersWithSteppedPrices = b;
    }

    public boolean isPricedEventsWithSteppedPrices() {
        return SteppedPriceHandler
                .isPricedEventsWithSteppedPrices(poServiceDetail
                        .getServiceEvents());
    }

    public void setPricedEventsWithSteppedPrices(boolean b) {
        this.pricedEventsWithSteppedPrices = b;
    }

    public void setServiceParameters(List<PricedParameterRow> serviceParameters) {
        this.poServiceDetail.setServiceParameters(serviceParameters);

    }

    public List<PricedParameterRow> getServiceParameters() {
        return poServiceDetail.getServiceParameters();
    }

    public List<RoleSpecificPrice> getRoleSpecificPrices() {
        return RolePriceHandler
                .determineRolePricesForPriceModel(poServiceDetail
                        .getSelectedService().getVO().getPriceModel());
    }

    public POVendorAddress getServicePartner() {
        return servicePartner;
    }

    public void setServicePartner(POVendorAddress servicePartner) {
        this.servicePartner = servicePartner;
    }

    public POVendorAddress getServiceSupplier() {
        return serviceSupplier;
    }

    public void setServiceSupplier(POVendorAddress serviceSupplier) {
        this.serviceSupplier = serviceSupplier;
    }

    public List<Long> getInvisibleProductKeys() {
        return invisibleProductKeys;
    }

    public void setInvisibleProductKeys(List<Long> invisibleProductsKeys) {
        invisibleProductKeys = invisibleProductsKeys;
    }

    public PriceModelContent getPriceModelContent() {
        return priceModelContent;
    }

    public void setPriceModelContent(PriceModelContent priceModelContent) {
        this.priceModelContent = priceModelContent;
    }

}
