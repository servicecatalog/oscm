/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.03.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.landingpage;

import org.oscm.ui.common.JSFUtils;
import org.oscm.internal.landingpage.POLandingpageEntry;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * @author zankov
 * 
 */
public class LandingpageEntryModel {

    private final static int MAX_LEN_LIMITED_SHORT_DESCRIPTION = 120;

    private final static int INDEX_LIMIT_SHORT_DESCRIPTION = 100;

    private POLandingpageEntry entry;

    private String accessLink;

    private String redirectUrl;

    private String target;

    private boolean showSubscribeButton;

    public LandingpageEntryModel(POLandingpageEntry entry) {
        this.entry = entry;
    }

    public long getKey() {
        return entry.getServiceKey();
    }

    public void setKey(long key) {
        entry.setServiceKey(key);
    }

    public long getSubscriptionKey() {
        return entry.getSubscriptionKey();
    }

    public void setSubcriptionKey(long key) {
        entry.setSubscriptionKey(key);
    }

    public String getSubscriptionHexKey() {
        return Long.toHexString(getSubscriptionKey());
    }

    public int getVersion() {
        return entry.getVersion();
    }

    public void setVersion(int version) {
        entry.setVersion(version);
    }

    public String getServiceId() {
        return entry.getServiceId();
    }

    public void setServiceId(String serviceId) {
        entry.setServiceId(serviceId);
    }

    public void setServiceAccessURL(String url) {
        entry.setServiceAccessURL(url);
    }

    public String getServiceAccessURL() {
        return entry.getServiceAccessURL();
    }

    public void setShortDescription(String shortDescription) {
        entry.setShortDescription(shortDescription);
    }

    public String getShortDescription() {
        return entry.getShortDescription();
    }

    public void setName(String name) {
        entry.setName(name);
    }

    public String getName() {
        return entry.getName();
    }

    public void setServiceStatus(ServiceStatus status) {
        entry.setServiceStatus(status);
    }

    public ServiceStatus getServiceStatus() {
        return entry.getServiceStatus();
    }

    public boolean isSubscribed() {
        return entry.isSubscribed();
    }

    public void setSubscribed(boolean subscribed) {
        entry.setSubscribed(subscribed);
    }

    public String getSubscriptionId() {
        return entry.getSubscriptionId();
    }

    public void setSubscriptionId(String subscriptionId) {
        entry.setSubscriptionId(subscriptionId);
    }

    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        entry.setSubscriptionStatus(subscriptionStatus);

    }

    public SubscriptionStatus getSubscriptionStatus() {
        return entry.getSubscriptionStatus();
    }

    public String getNameToDisplay() {
        String name = entry.getName();
        if (name == null || name.trim().length() == 0) {
            return JSFUtils.getText("service.name.undefined", null);
        }

        return name;
    }

    public void setSellerName(String sellerName) {
        entry.setSellerName(sellerName);
    }

    public String getSellerName() {
        return entry.getSellerName();
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

    public void setAccessLink(String accessUrl) {
        this.accessLink = accessUrl;
    }

    public String getAccessLink() {
        return accessLink;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isShowSubscribeButton() {
        return showSubscribeButton;
    }

    public void setShowSubscribeButton(boolean showSubscribeButton) {
        this.showSubscribeButton = showSubscribeButton;
    }

}
