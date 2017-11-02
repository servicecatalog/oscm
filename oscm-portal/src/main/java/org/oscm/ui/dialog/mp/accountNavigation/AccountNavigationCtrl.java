/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-8-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.accountNavigation;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;

/**
 * @author Yuyin
 * 
 */
@ManagedBean(name = "accountNavigationBean")
@SessionScoped
public class AccountNavigationCtrl extends BaseBean implements Serializable {

    private static final long serialVersionUID = -6631452652280374841L;

    @ManagedProperty(value = "#{appBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{accountNavigationModel}")
    private AccountNavigationModel model;

    @PostConstruct
    public void getInitialize() {
        String url = getRequest().getRequestURL().toString();
        computeBaseUrl(url);
    }

    protected void computeBaseUrl(String url) {
        String baseUrl = url.replaceFirst("/marketplace/.*", "");
        getModel().setBaseUrl(baseUrl);
        getModel().initLink();
    }

    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public AccountNavigationModel getModel() {
        return model;
    }

    public List<String> getLink() {
        return getModel().getLink();
    }

    public List<String> getTitle() {
        return getModel().getTitle();
    }

    public List<String> getHiddenElement() {
        return getModel().getHiddenElement();
    }

    /**
     * Check if reporting box should be displayed - reporting must be available
     * and user must be logged in and have the administrator role.
     * 
     * @return <code>true</code> if report box is available, <code>false</code>
     *         otherwise.
     */
    public boolean isReportingAvailable() {
        return (applicationBean.isReportingAvailable()
                && (isLoggedInAndAdmin() || isLoggedInAndUnitAdmin())
                && !applicationBean.isUIElementHidden(
                        HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_REPORTS));
    }

    public boolean isPaymentAvailable() {

        return (isLoggedInAndAdmin()
                && !applicationBean.isUIElementHidden(
                        HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_PAYMENT)
                && getConfigurationService().isPaymentInfoAvailable());
    }

    public void setModel(AccountNavigationModel model) {
        this.model = model;
    }

    /**
     * @param index
     *            the index of model.link or model.title
     * @return <code>true</code> if the index-1 of getHiddenElement() is
     *         available, <code>false</code> otherwise.
     */
    public boolean isLinkVisible(int index) {
        if (index < 1 || index > getLink().size()) {
            return false;
        }
        String hiddenElement = getHiddenElement().get(index - 1);
        if (HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_REPORTS
                .equals(hiddenElement)) {
            return isReportingAvailable();
        } else if (HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_USERS
                .equals(hiddenElement)) {
            return isLoggedInAndAdmin()
                    && !applicationBean.isUIElementHidden(hiddenElement);
        } else if (HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_PAYMENT
                .equals(hiddenElement)) {
            return isPaymentAvailable();
        } else if (HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_SUBSCRIPTIONS
                .equals(hiddenElement)) {
            return (isLoggedInAndAdmin() || isLoggedInAndSubscriptionManager()
                    || isLoggedInAndUnitAdmin())
                    && !applicationBean.isUIElementHidden(hiddenElement);
        } else if (HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_UNITS
                .equals(hiddenElement)) {
            return isLoggedInAndUnitAdmin()
                    && !applicationBean.isUIElementHidden(hiddenElement);
        } else if (HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_ADMINISTRATION
                .equals(hiddenElement)) {
            return isAdministrationAccess()
                    && !applicationBean.isUIElementHidden(hiddenElement);
        }
        return !applicationBean.isUIElementHidden(hiddenElement);
    }
}
