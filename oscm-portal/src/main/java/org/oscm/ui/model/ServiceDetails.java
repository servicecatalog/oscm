/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import java.util.List;

import org.oscm.ui.common.DisplayData;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * Wrapper for <code>VOServiceDetails</code>.
 * 
 * @author Sven Kulle
 */
public class ServiceDetails {

    private VOServiceDetails voDetails;
    private boolean visible;

    /**
     * Specifies whether the service is visible to non-registered customers on a
     * marketplace.
     */
    private boolean publicService = true;

    /**
     * Default constructor.
     * 
     * @param voDetails
     */
    public ServiceDetails(VOServiceDetails voDetails) {
        this.voDetails = voDetails;
    }

    public VOServiceDetails getVoServiceDetails() {
        return voDetails;
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOServiceDetails#getTechnicalService()
     */
    public VOTechnicalService getTechnicalService() {
        return voDetails.getTechnicalService();
    }

    /**
     * @return
     * @see org.oscm.internal.vo.BaseVO#getKey()
     */
    public long getKey() {
        return voDetails.getKey();
    }

    /**
     * @param technicalService
     * @see org.oscm.internal.vo.VOServiceDetails#setTechnicalService(org.oscm.internal.vo.VOTechnicalService)
     */
    public void setTechnicalService(VOTechnicalService technicalService) {
        voDetails.setTechnicalService(technicalService);
    }

    /**
     * @return
     * @see org.oscm.internal.vo.BaseVO#getVersion()
     */
    public int getVersion() {
        return voDetails.getVersion();
    }

    /**
     * @param key
     * @see org.oscm.internal.vo.BaseVO#setKey(long)
     */
    public void setKey(long key) {
        voDetails.setKey(key);
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOServiceDetails#isImageDefined()
     */
    public boolean isImageDefined() {
        return voDetails.isImageDefined();
    }

    /**
     * @param version
     * @see org.oscm.internal.vo.BaseVO#setVersion(int)
     */
    public void setVersion(int version) {
        voDetails.setVersion(version);
    }

    /**
     * @param imageDefined
     * @see org.oscm.internal.vo.VOServiceDetails#setImageDefined(boolean)
     */
    public void setImageDefined(boolean imageDefined) {
        voDetails.setImageDefined(imageDefined);
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getDescription()
     */
    public String getDescription() {
        return voDetails.getDescription();
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getName()
     */
    public String getName() {
        if (voDetails.getName() != null) {
            return voDetails.getName();
        }
        return voDetails.getTechnicalId();
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getNameToDisplay()
     */
    public String getNameToDisplay() {
        if (voDetails == null) {
            return null;
        }
        return DisplayData.getServiceName(voDetails.getNameToDisplay());
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getParameters()
     */
    public List<VOParameter> getParameters() {
        return voDetails.getParameters();
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getPriceModel()
     */
    public VOPriceModel getPriceModel() {
        return voDetails.getPriceModel();
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getAccessType()
     */
    public ServiceAccessType getAccessType() {
        return voDetails.getAccessType();
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getServiceId()
     */
    public String getServiceId() {
        return voDetails.getServiceId();
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getFeatureURL()
     */
    public String getFeatureURL() {
        return voDetails.getFeatureURL();
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getTechnicalId()
     */
    public String getTechnicalId() {
        return voDetails.getTechnicalId();
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#isVisible()
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param marketingName
     * @see org.oscm.internal.vo.VOService#setDescription(java.lang.String)
     */
    public void setDescription(String marketingName) {
        if (marketingName == null) {
            voDetails.setDescription("");
        } else {
            voDetails.setDescription(marketingName);
        }
    }

    /**
     * @param name
     * @see org.oscm.internal.vo.VOService#setName(java.lang.String)
     */
    public void setName(String name) {
        voDetails.setName(name);
    }

    /**
     * @param parameters
     * @see org.oscm.internal.vo.VOService#setParameters(java.util.List)
     */
    public void setParameters(List<VOParameter> parameters) {
        voDetails.setParameters(parameters);
    }

    /**
     * @param priceModel
     * @see org.oscm.internal.vo.VOService#setPriceModel(org.oscm.internal.vo.VOPriceModel)
     */
    public void setPriceModel(VOPriceModel priceModel) {
        voDetails.setPriceModel(priceModel);
    }

    /**
     * @param accessType
     * @see org.oscm.internal.vo.VOService#setAccessType(org.oscm.internal.types.enumtypes.ServiceAccessType)
     */
    public void setAccessType(ServiceAccessType accessType) {
        voDetails.setAccessType(accessType);
    }

    /**
     * @param serviceId
     * @see org.oscm.internal.vo.VOService#setServiceId(java.lang.String)
     */
    public void setServiceId(String serviceId) {
        voDetails.setServiceId(serviceId);
    }

    /**
     * @param technicalURL
     * @see org.oscm.internal.vo.VOService#setFeatureURL(java.lang.String)
     */
    public void setFeatureURL(String technicalURL) {
        voDetails.setFeatureURL(technicalURL);
    }

    /**
     * @param technicalId
     * @see org.oscm.internal.vo.VOService#setTechnicalId(java.lang.String)
     */
    public void setTechnicalId(String technicalId) {
        voDetails.setTechnicalId(technicalId);
    }

    /**
     * @param visible
     * @see org.oscm.internal.vo.VOService#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @param status
     * @see org.oscm.internal.vo.VOService#setStatus(org.oscm.internal.types.enumtypes.ServiceStatus)
     */
    public void setStatus(ServiceStatus status) {
        voDetails.setStatus(status);
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getStatus()
     */
    public ServiceStatus getStatus() {
        return voDetails.getStatus();
    }

    /**
     * @param publicService
     * @see org.oscm.internal.vo.VOService#setPublicService(boolean)
     */
    public void setPublicService(boolean publicService) {
        this.publicService = publicService;
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#isPublicService()
     */
    public boolean isPublicService() {
        return publicService;
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getSellerId()
     */
    public String getSupplierId() {
        return voDetails.getSellerId();
    }

    /**
     * @param supplierId
     * @see org.oscm.internal.vo.VOService#setSellerId(java.lang.String)
     */
    public void setSupplierId(String supplierId) {
        voDetails.setSellerId(supplierId);
    }

    /**
     * @param supplierName
     * @see org.oscm.internal.vo.VOService#setSellerName(java.lang.String)
     */
    public void setSupplierName(String supplierName) {
        voDetails.setSellerName(supplierName);
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getSellerName()
     */
    public String getSupplierName() {
        return voDetails.getSellerName();
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getTags()
     */
    public List<String> getTags() {
        return voDetails.getTags();
    }

    /**
     * @param serviceTags
     * @see org.oscm.internal.vo.VOService#setTags(java.util.List)
     */
    public void setTags(List<String> serviceTags) {
        voDetails.setTags(serviceTags);
    }

    /**
     * @return
     * @see org.oscm.internal.vo.VOService#getShortDescription()
     */
    public String getShortDescription() {
        return voDetails.getShortDescription();
    }

    /**
     * @param description
     * @see org.oscm.internal.vo.VOService#setShortDescription(java.lang.String)
     */
    public void setShortDescription(String description) {
        voDetails.setShortDescription(description);
    }


    /**
     *
     * @return the name of the custom tab of the service
     */
    public String getCustomTabName() {
        return voDetails.getCustomTabName();
    }

    /**
     * Sets the name of the custom tab of the service
     * @param customTabName
     */
    public void setCustomTabName(String customTabName) {
        voDetails.setCustomTabName(customTabName);
    }

    /**
     *
     * @return URL of the custom tab of the service
     */
    public String getCustomTabUrl() {
        return voDetails.getCustomTabUrl();
    }

    /**
     * Sets the URL of the custom tab of the service
     *
     * @param customTabUrl
     */
    public void setCustomTabUrl(String customTabUrl) {
        voDetails.setCustomTabUrl(customTabUrl);
    }

    public boolean isReseller() {
        return voDetails.getOfferingType() == OfferingType.RESELLER;
    }

    public boolean isBroker() {
        return voDetails.getOfferingType() == OfferingType.BROKER;
    }

    public boolean isSupplier() {
        return voDetails.getOfferingType() == OfferingType.DIRECT;
    }

    public boolean isAutoAssignUserEnabled() {
        return voDetails.isAutoAssignUserEnabled().booleanValue();
    }

    public void setAutoAssignUserEnabled(boolean autoAssignUserEnabled) {
        voDetails.setAutoAssignUserEnabled(Boolean
                .valueOf(autoAssignUserEnabled));
    }

    public String getConfiguratorUrl() {
        return voDetails.getConfiguratorUrl();
    }

    public void setConfiguratorUrl(String configuratorUrl) {
        voDetails.setConfiguratorUrl(configuratorUrl);
    }

}
