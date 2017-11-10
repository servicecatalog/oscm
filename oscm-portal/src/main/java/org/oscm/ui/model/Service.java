/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                                
 *                                                                              
 *  Creation Date: 18.02.2009                                                   
 *                                                                              
 *  Completion Time: 18.05.2011                                                 
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.oscm.converter.PriceConverter;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.DisplayData;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.RatingCssMapper;
import org.oscm.internal.pricemodel.external.ExternalPriceModelException;
import org.oscm.internal.pricemodel.external.ExternalPriceModelService;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceEntry;

/**
 * Wrapper Class for VOService which holds additional view attributes.
 * 
 */
public class Service extends BaseBean implements Serializable {

    private final static long serialVersionUID = 1L;
    private final static int MAX_LEN_LIMITED_SHORT_DESCRIPTION = 120;
    private final static int INDEX_LIMIT_SHORT_DESCRIPTION = 100;

    private final VOService vo;

    private PriceModel pm;
    private boolean selected;
    private String priceText = null;
    private String priceUnitText = null;
    private List<VOCatalogEntry> catalogEntries;
    private boolean oldVisibility;
    private boolean subscribable;
    private String marketplaceName;

    public Service(VOService vo) {
        this.vo = vo;
        if (vo.getPriceModel() != null) {
            pm = new PriceModel(vo.getPriceModel());
        }

        // we don't know it from the basic VOService
        setSubscribable(true);
    }

    public Service(VOServiceEntry vo) {
        this.vo = vo;
        if (vo.getPriceModel() != null) {
            pm = new PriceModel(vo.getPriceModel());
        }
        setSubscribable(!vo.isSubscriptionLimitReached());
    }

    /**
     * @return true, if the configurator URL is not null and not empty,
     *         otherwise false.
     */
    public boolean useExternalConfigurator() {
        return vo.getConfiguratorUrl() != null
                && !vo.getConfiguratorUrl().trim().isEmpty();
    }

    public String getConfiguratorUrl() {
        return vo.getConfiguratorUrl();
    }

    public VOService getVO() {
        return vo;
    }

    public boolean isSelected() {
        return selected;
    }

    public int getReviewCount() {
        if (vo != null) {
            return vo.getNumberOfReviews();
        }
        return 0;
    }

    public String getRating() {
        if (vo != null) {
            return RatingCssMapper.getRatingClass(vo.getAverageRating());
        }
        return RatingCssMapper.getRatingClass(BigDecimal.ZERO);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Calculate "visibleInCatalogFlag" from catalog entries
     */
    public boolean isVisibleInCatalog() {
        // right now every service can be visible in only one catalog
        // (default: false)
        if (catalogEntries != null && !catalogEntries.isEmpty())
            return catalogEntries.get(0).isVisibleInCatalog();
        return false;
    }

    /**
     * Delegate "visibleInCatalogFlag" to catalog entries
     */
    public void setVisibleInCatalog(boolean visibleInCatalog) {
        // propagate flag to all stored catalog entries
        if (catalogEntries != null) {
            Iterator<VOCatalogEntry> iter = catalogEntries.iterator();
            while (iter.hasNext()) {
                iter.next().setVisibleInCatalog(visibleInCatalog);
            }
        }
    }

    /**
     * Value for helper checkbox on page.
     * <p>
     * The real value will be exchanged within a hidden field for supporting a
     * dynamically disabling.
     */
    public boolean isVisibleInCatalogChecked() {
        return isVisibleInCatalog();
    }

    /**
     * Value for helper checkbox on page.
     * <p>
     * The real value will be exchanged within a hidden field for supporting a
     * dynamically disabling. Therefore this "set" is ignored.
     * 
     * @param checked
     */
    public void setVisibleInCatalogChecked(boolean checked) {
    }

    public List<VOCatalogEntry> getCatalogEntries() {
        return catalogEntries;
    }

    public void setCatalogEntries(List<VOCatalogEntry> catalogEntries) {
        this.catalogEntries = catalogEntries;
        // remember currently defined visibility
        this.oldVisibility = isVisibleInCatalog();
    }

    /**
     * Returns boolean which informs the UI whether the flag "visibleInCatalog"
     * can be adjusted (only for global -not customer specific- services).
     */
    public boolean isVisibleInCatalogSupported() {
        boolean rc = (getOrganizationId() == null); // not customer specific
        if (rc && catalogEntries != null && !catalogEntries.isEmpty()) {
            // Only for marketplaces
            if (isNoMarketplaceAssigned()) {
                rc = false;
            }
        }
        return rc;
    }

    /**
     * Returns whether the data of this service has been modified and therefore
     * needs to be updated. This affects only the activation and visibility
     * state.
     */
    public boolean isModified() {
        boolean modified = ((isSelected() && vo.getStatus() == ServiceStatus.INACTIVE) || (!isSelected() && vo
                .getStatus() == ServiceStatus.ACTIVE));
        if (!modified && isVisibleInCatalogSupported()
                && isVisibleInCatalog() != oldVisibility)
            modified = true;
        return modified;
    }

    /*
     * Delegate Methods
     */

    public long getKey() {
        return vo.getKey();
    }

    public String getLicense() {
        if (vo.getPriceModel() != null) {
            return vo.getPriceModel().getLicense();
        }
        return null;
    }

    public String getDescription() {
        return vo.getDescription();
    }

    public String getName() {
        return vo.getName();
    }

    public String getNameToDisplay() {
        if (vo == null) {
            return null;
        }
        return DisplayData.getServiceName(vo.getName());
    }

    public String getPriceToDisplay() {
        if (vo == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(getPriceText());
        String priceUnit = getPriceUnitText();
        if (priceUnit != null && priceUnit.length() > 0) {
            buf.append(" ");
            buf.append(priceUnit);
        }
        return buf.toString();
    }

    public PriceModel getPriceModel() {
        return pm;
    }

    public String getServiceId() {
        return vo.getServiceId();
    }

    public String getServiceIdToDisplay() {
        if (vo == null) {
            return null;
        }
        return vo.getServiceIdToDisplay();
    }

    public String getFeatureURL() {
        return vo.getFeatureURL();
    }

    public String getBaseURL() {
        return vo.getBaseURL();
    }

    public String getTechnicalId() {
        return vo.getTechnicalId();
    }

    public int getVersion() {
        return vo.getVersion();
    }

    public void setLicenseDescription(String licenseDescription) {
        if (vo.getPriceModel() != null) {
            vo.getPriceModel().setLicense(licenseDescription);
        } else {
            VOPriceModel priceModel = new VOPriceModel();
            priceModel.setLicense(licenseDescription);
            vo.setPriceModel(priceModel);
        }
    }

    public void setMarketingDescription(String marketingName) {
        vo.setDescription(marketingName);
    }

    public void setName(String name) {
        vo.setName(name);
    }

    public void setPriceModel(PriceModel priceModel) {
        this.pm = priceModel;
    }

    public void setServiceId(String serviceId) {
        vo.setServiceId(serviceId);
    }

    public void setFeatureURL(String featureURL) {
        vo.setFeatureURL(featureURL);
    }

    public void setTechnicalId(String technicalId) {
        vo.setTechnicalId(technicalId);
    }

    @Override
    public String toString() {
        return vo.toString();
    }

    public String getPriceModelTextKey() {
        PriceModel priceModel = getPriceModel();
        if (priceModel == null || priceModel.getVo() == null
                || priceModel.getKey() == 0) {
            return "priceModel.text.undefined";
        }
        if (priceModel.isChargeable()) {
            return "priceModel.text.defined";
        }
        return "priceModel.text.free";
    }

    public String getOrganizationDisplayName() {
        return null;
    }

    public String getOrganizationName() {
        return null;
    }

    public String getOrganizationId() {
        return null;
    }

    public Long getOrganizationKey() {
        return null;
    }

    public String getSellerName() {
        if (vo != null) {
            return vo.getSellerName();
        }
        return "";
    }

    public long getSupplierKey() {
        if (vo != null) {
            return vo.getSellerKey();
        }
        return -1L;
    }

    /**
     * Return the list of defined localized tags as string array
     */
    public List<String> getTags() {
        return vo.getTags();
    }

    public String getPriceText() {
        if (priceText == null) {
            initPriceText();
        }
        return priceText;
    }

    public String getPriceUnitText() {
        if (priceUnitText == null) {
            initPriceText();
        }
        return priceUnitText;
    }

    private void initPriceText() {
        if (vo.getAccessType() == ServiceAccessType.EXTERNAL) {
            priceText = JSFUtils.getText(BaseBean.LABEL_PRICE_MODEL_EXTERNAL, null);
            return;
        }
        String[] text = getPriceText(getPriceModel().getVo());
        priceText = text[0];
        if (text.length > 1) {
            priceUnitText = text[1];
        }
    }

    /**
     * Determines the short pricing information to display
     * 
     * @param priceModel
     *            the price model to get the price
     * @return the price text
     */
    String[] getPriceText(VOPriceModel priceModel) {
        if (priceModel.isExternal()) {
            return getPriceTextExternal(priceModel);
        }
        return getPriceTextNative(priceModel);
    }

    String[] getPriceTextExternal(VOPriceModel priceModel) {
        String[] result = new String[1];
        ExternalPriceModelService service = getExternalPriceModelService();
        Locale locale = getLocale();
        try {
            result[0] = service.getCachedPriceModelTag(locale, priceModel.getUuid());
        } catch (ExternalPriceModelException e) {
            result[0] = "";
        }
        return result;
    }

    Locale getLocale() {
        return FacesContext.getCurrentInstance().getViewRoot().getLocale();
    }

    String[] getPriceTextNative(VOPriceModel priceModel) {
        String[] result = new String[] {
                JSFUtils.getText(BaseBean.LABEL_PRICE_MODEL_FREE, null), "", "" };

        if (priceModel.isFree()) {
            result[2] = result[0];
            return result;
        }

        if (priceModel.isPricePerPeriodSet()) {
            result[0] = JSFUtils.getText(BaseBean.LABEL_PRICE_MODEL_PRICE,
                    new Object[] { getCurrencySymbol(priceModel),
                            getDisplayPrice(priceModel.getPricePerPeriod()) });
            result[1] = JSFUtils.getText(BaseBean.LABEL_PRICE_MODEL_PER_SUB,
                    new Object[] { getPeriodText(priceModel.getPeriod()) });
        } else if (priceModel.isPricePerUserAssignmentSet()) {
            result[0] = JSFUtils.getText(
                    BaseBean.LABEL_PRICE_MODEL_PRICE,
                    new Object[] {
                            getCurrencySymbol(priceModel),
                            getDisplayPrice(priceModel
                                    .getPricePerUserAssignment()) });
            result[1] = JSFUtils.getText(BaseBean.LABEL_PRICE_MODEL_PER_USER,
                    new Object[] { getPeriodText(priceModel.getPeriod()) });
        } else if (priceModel.isOneTimeFeeSet()) {
            result[0] = JSFUtils.getText(BaseBean.LABEL_PRICE_MODEL_PRICE,
                    new Object[] { getCurrencySymbol(priceModel),
                            getDisplayPrice(priceModel.getOneTimeFee()) });
        } else {
            result[0] = JSFUtils.getText(
                    BaseBean.LABEL_PRICE_MODEL_SEE_DETAILS, new Object[0]);
        }
        if (result[1].trim().length() > 0) {
            // when both price and unit are set, provide combined string as well
            result[2] = JSFUtils.getText(
                    BaseBean.LABEL_PRICE_MODEL_PRICE_AND_UNIT, new Object[] {
                            result[0], result[1] });
        } else {
            result[2] = result[0];
        }
        return result;
    }

    private static String getCurrencySymbol(VOPriceModel priceModel) {
        return Currency.getInstance(priceModel.getCurrencyISOCode()).getSymbol(
                JSFUtils.getViewLocale());
    }

    private static final String getDisplayPrice(BigDecimal price) {
        PriceConverter converter = new PriceConverter(JSFUtils.getViewLocale());
        return converter.getValueToDisplay(price, true);
    }

    static final String getPeriodText(PricingPeriod period) {
        return JSFUtils
                .getText("PricingPeriod." + period.name(), new Object[0]);
    }

    /**
     * Returns the short description of this service
     */
    public String getShortDescription() {
        return vo.getShortDescription();
    }

    /**
     * Returns the short description of this service with a max. length of 120
     * characters. If the description is longer it will be cut accordingly.
     */
    public String getShortDescriptionLimited() {
        StringBuffer sd = new StringBuffer(getShortDescription());
        if (sd.length() > MAX_LEN_LIMITED_SHORT_DESCRIPTION) {
            // Shorten as follows:
            // Find the last blank before the 100th character, use the part
            // before the blank and add "...".
            String shortString = sd.substring(0, INDEX_LIMIT_SHORT_DESCRIPTION);
            int blankIdx = shortString.lastIndexOf(' ');
            if (blankIdx > 0) {
                sd.setLength(blankIdx);
            } else {
                // No blank found => simply cut and add "..."
                sd.setLength(INDEX_LIMIT_SHORT_DESCRIPTION);

            }
            // Append ...
            sd.append("...");
        }
        return sd.toString();
    }

    public boolean isNoMarketplaceAssigned() {
        return catalogEntries == null || catalogEntries.isEmpty()
                || catalogEntries.get(0).getMarketplace() == null;
    }

    /**
     * Returns name of marketplace
     */
    public String getMarketplace() {
        if (catalogEntries != null && !catalogEntries.isEmpty()) {
            // right now only one marketplace is supported
            if (isNoMarketplaceAssigned()) {
                return JSFUtils.getText("marketplace.name.undefined", null);
            }
            return catalogEntries.get(0).getMarketplace().getName();
        }
        return "";
    }

    public void setSubscribable(boolean subscribable) {
        this.subscribable = subscribable;
    }

    public boolean isSubscribable() {
        return subscribable;
    }

    /**
     * Returns if the service is suspended or not.
     * 
     * @return Returns true, if the service is suspended, otherwise false.
     */
    public boolean isServiceSuspended() {
        return getVO().getStatus().equals(ServiceStatus.SUSPENDED);
    }

    /**
     * Returns if the service is active or not.
     * 
     * @return Returns true, if the service is active, otherwise false.
     */
    public boolean isServiceActive() {
        return getVO().getStatus().equals(ServiceStatus.ACTIVE);
    }

    public boolean isExternal() {
        return vo.getAccessType() == ServiceAccessType.EXTERNAL;
    }

    public void setMarketplaceName(String marketplaceName) {
        this.marketplaceName = marketplaceName;
    }

    public String getMarketplaceName() {
        return marketplaceName;
    }

    public boolean isReseller() {
        return vo.getOfferingType() == OfferingType.RESELLER;
    }

    public boolean isBroker() {
        return vo.getOfferingType() == OfferingType.BROKER;
    }

    public boolean isSupplier() {
        return vo.getOfferingType() == OfferingType.DIRECT;
    }

    public boolean isSupplierOrBroker() {
        OfferingType offeringType = vo.getOfferingType();
        return offeringType == OfferingType.DIRECT || offeringType == OfferingType.BROKER;
    }

}
