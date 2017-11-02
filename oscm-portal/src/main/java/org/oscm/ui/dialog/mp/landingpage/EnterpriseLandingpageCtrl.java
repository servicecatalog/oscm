/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.landingpage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.landingpage.EnterpriseLandingpageData;
import org.oscm.internal.landingpage.EnterpriseLandingpageService;
import org.oscm.internal.landingpage.POLandingpageEntry;
import org.oscm.internal.types.enumtypes.ServiceAccessType;

/**
 * Controller for the enterprise landingpage
 * 
 * @author zankov
 * 
 */
@ViewScoped
@ManagedBean(name = "enterpriseLandingpageCtrl")
public class EnterpriseLandingpageCtrl {
    UiDelegate ui = new UiDelegate();

    /**
     * Contains the services which should be listed on the enterprise landing
     * page.
     */
    @ManagedProperty(value = "#{enterpriseLandingpageModel}")
    EnterpriseLandingpageModel model;

    /**
     * References the landingpageService which is responsible for listing the
     * landingpage entries.
     */
    EnterpriseLandingpageService landingpageService;

    /**
     * References the categorizationService which is responsible for listing the
     * categories.
     */
    CategorizationService categorizationService;

    ApplicationBean applicationBean;

    /**
     * initializer method called by <adm:initialize />
     * 
     * @return empty string (due to value jsf binding )
     * 
     */
    public String getInitializeLandingpage() {
        initializeModel();
        return "";
    }

    private void initializeModel() {
        if (model.isInitialized() == false) {
            loadData();
            model.setInitialized(true);
        }
    }

    private EnterpriseLandingpageService getLandingpageService() {
        if (landingpageService == null) {
            landingpageService = ui
                    .findService(EnterpriseLandingpageService.class);
        }
        return landingpageService;
    }

    EnterpriseLandingpageModel loadData() {
        Response response = getLandingpageService().loadLandingpageEntries(
                ui.getMarketplaceId(), ui.getViewLocale().getLanguage());
        EnterpriseLandingpageData responseData = response
                .getResult(EnterpriseLandingpageData.class);

        model.addLandingpageData(responseData);

        addModelEntries(responseData);

        return model;
    }

    public String getNameFirstCategoryShow() {
        if (isNameFirstCategoryShowNull()) {
            return model.getIdFirstCategory();
        } else
            return model.getNameFirstCategory();
    }

    public String getNameSecondCategoryShow() {
        if (isNameSecondCategoryShowNull()) {
            return model.getIdSecondCategory();
        } else
            return model.getNameSecondCategory();
    }

    public String getNameThirdCategoryShow() {
        if (isNameThirdCategoryShowNull()) {
            return model.getIdThirdCategory();
        } else
            return model.getNameThirdCategory();
    }

    public boolean isNameFirstCategoryShowNull() {
        return (model.getNameFirstCategory() == null || model
                .getNameFirstCategory().isEmpty());
    }

    public boolean isNameSecondCategoryShowNull() {
        return (model.getNameSecondCategory() == null || model
                .getNameSecondCategory().isEmpty());
    }

    public boolean isNameThirdCategoryShowNull() {
        return (model.getNameThirdCategory() == null || model
                .getNameThirdCategory().isEmpty());
    }

    void addModelEntries(EnterpriseLandingpageData responseData) {
        if (responseData.numberOfColumns() >= 1) {
            model.addEntries(0,
                    convertEntriesForModel(responseData.getEntries(0)));
        }

        if (responseData.numberOfColumns() >= 2) {
            model.addEntries(1,
                    convertEntriesForModel(responseData.getEntries(1)));
        }

        if (responseData.numberOfColumns() == 3) {
            model.addEntries(2,
                    convertEntriesForModel(responseData.getEntries(2)));
        }
    }

    List<LandingpageEntryModel> convertEntriesForModel(
            List<POLandingpageEntry> entries) {

        ArrayList<LandingpageEntryModel> result = new ArrayList<LandingpageEntryModel>();
        for (POLandingpageEntry entry : entries) {
            LandingpageEntryModel entryModel = new LandingpageEntryModel(entry);
            entryModel.setAccessLink(getAccessUrl(entry));
            entryModel.setRedirectUrl(composeRedirectUrl(entryModel));
            entryModel.setTarget(isOpenNewTab(entryModel) ? "_blank" : "");
            entryModel.setShowSubscribeButton(!entryModel.isSubscribed());
            result.add(entryModel);
        }
        return result;
    }

    String getAccessUrl(POLandingpageEntry entry) {
        if (entry.getServiceAccessType() == ServiceAccessType.USER
                || entry.getServiceAccessType() == ServiceAccessType.DIRECT) {
            return entry.getServiceAccessURL();
        } else {
            String serviceBaseUrl = entry.getServiceAccessURL();
            String serverBaseUrl;
            if (ADMValidator.isHttpsScheme(serviceBaseUrl)) {
                serverBaseUrl = getApplicationBean().getServerBaseUrlHttps();
            } else {
                serverBaseUrl = getApplicationBean().getServerBaseUrl();
            }

            return ADMStringUtils.removeEndingSlash(serverBaseUrl)
                    + Constants.SERVICE_BASE_URI + "/"
                    + Long.toHexString(entry.getSubscriptionKey()) + "/";
        }
    }

    private ApplicationBean getApplicationBean() {
        if (applicationBean == null) {
            applicationBean = ui.findBean("appBean");
        }
        return applicationBean;
    }

    public boolean isShowFirstCategory() {
        return model.getNumberOfColumns() >= 1;
    }

    public boolean isShowSecondCategory() {
        return model.getNumberOfColumns() >= 2;
    }

    public boolean isShowThirdCategory() {
        return model.getNumberOfColumns() == 3;
    }

    public EnterpriseLandingpageModel getModel() {
        return model;
    }

    public void setModel(EnterpriseLandingpageModel model) {
        this.model = model;
    }

    public void entrySelected() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContext = context.getExternalContext();
        LandingpageEntryModel selectedEntry = findSelectedEntry(model
                .getSelectedEntryKey());
        try {
            JSFUtils.redirect(extContext, selectedEntry.getRedirectUrl());
        } catch (Exception e) {
            extContext.log(getClass().getName() + ".startService()", e);
        } finally {
            // reset requested key;
            model.setSelectedEntryKey(null);
            model.setSelectedCategory(0);
        }
    }

    String composeRedirectUrl(LandingpageEntryModel selectedEntry) {

        if (selectedEntry.isSubscribed()) {
            if (selectedEntry.getServiceAccessURL() == null) {
                return composeMySubscriptionsUrl();
            } else {
                return composeServiceUrl(selectedEntry);
            }
        } else {
            return composeServiceDetailsUrl(selectedEntry);
        }
    }

    String composeServiceUrl(LandingpageEntryModel selectedEntry) {
        return selectedEntry.getAccessLink();
    }

    public String composeMySubscriptionsUrl() {
        String viewId = Marketplace.MARKETPLACE_ROOT
                + "/subscriptions/index.jsf";

        viewId = ui.getExternalContext().getRequestContextPath() + viewId;
        return ui.getExternalContext().encodeActionURL(viewId);
    }

    public String composeServiceDetailsUrl(LandingpageEntryModel selectedEntry) {
        String viewId = Marketplace.MARKETPLACE_ROOT + "/serviceDetails.jsf";

        viewId = ui.getExternalContext().getRequestContextPath()
                + viewId
                + ui.getSelectedServiceKeyQueryPart(String
                        .valueOf(selectedEntry.getKey()))
                + ui.getMarketplaceIdQueryPart();

        return ui.getExternalContext().encodeActionURL(viewId);
    }

    LandingpageEntryModel findSelectedEntry(String selectedElementKey) {
        ArrayList<LandingpageEntryModel> allEntries = mergeAllEntries();
        return getEntry(allEntries, selectedElementKey);
    }

    ArrayList<LandingpageEntryModel> mergeAllEntries() {
        ArrayList<LandingpageEntryModel> result = new ArrayList<LandingpageEntryModel>();
        if (model.entriesOfCateogry0 != null) {
            result.addAll(model.entriesOfCateogry0);
        }

        if (model.entriesOfCateogry1 != null) {
            result.addAll(model.entriesOfCateogry1);
        }

        if (model.entriesOfCateogry2 != null) {
            result.addAll(model.entriesOfCateogry2);
        }
        return result;
    }

    LandingpageEntryModel getEntry(List<LandingpageEntryModel> entries,
            String selectedEntryKey) {
        for (LandingpageEntryModel entry : entries) {
            if (entry.getKey() == Long.parseLong(selectedEntryKey)) {
                return entry;
            }
        }

        throw new IllegalArgumentException("Entry not found: " + "Entry Key: "
                + selectedEntryKey);
    }

    boolean isOpenNewTab(LandingpageEntryModel selectedEntry) {
        if (selectedEntry.isSubscribed()) {
            if (selectedEntry.getServiceAccessURL() != null) {
                if (!isInternalURL(selectedEntry.getServiceAccessURL())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the given URL is external URL. External URL's are all URL which
     * do not contain the BES-Base-URL
     * 
     * @param serviceAccessURL
     * @return true if URL doesn't contain the BES-Base-URL
     */
    boolean isInternalURL(String serviceAccessURL) {
        if (serviceAccessURL == null || serviceAccessURL.length() == 0) {
            return false;
        }

        return isMatch(serviceAccessURL, getApplicationBean()
                .getServerBaseUrl())
                || isMatch(serviceAccessURL, getApplicationBean()
                        .getServerBaseUrlHttps());
    }

    private static boolean isMatch(String s, String pattern) {
        Pattern patt = Pattern.compile("\\b" + pattern + ".*");
        Matcher matcher = patt.matcher(s);
        return matcher.matches();
    }
}
