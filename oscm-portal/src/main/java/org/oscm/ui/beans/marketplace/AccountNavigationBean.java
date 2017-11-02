/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: goebel                                                      
 *                                                                              
 *  Creation Date: 7.4.2011                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans.marketplace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.JSFUtils;

/**
 * A simple backing bean keeping and managing the navigation menu.
 * 
 * @author goebel
 * 
 */

public class AccountNavigationBean extends BaseBean implements Serializable {

    static final String MARKETPLACE_ACCOUNT_OPERATIONS_TITLE = "marketplace.account.operations.title";
    static final String MARKETPLACE_ACCOUNT_PROCESSES_TITLE = "marketplace.account.processes.title";
    static final String MARKETPLACE_ACCOUNT_REPORTS_TITLE = "marketplace.account.reports.title";
    static final String MARKETPLACE_ACCOUNT_USERS_TITLE = "marketplace.account.users.title";
    static final String MARKETPLACE_ACCOUNT_UNITS_TITLE = "marketplace.account.units.title";
    static final String MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_TITLE = "marketplace.account.subscriptions.title";
    static final String MARKETPLACE_ACCOUNT_PAYMENTS_TITLE = "marketplace.account.payments.title";
    static final String MARKETPLACE_ACCOUNT_PROFILE_TITLE = "marketplace.account.profile.title";
    static final String MARKETPLACE_ACCOUNT_TITLE = "marketplace.account.title";
    static final String MARKETPLACE_ACCOUNT_ADMINISTRATION_TITLE = "marketplace.account.administration";

    private static final String ACCOUNT_LINK = "account/index.jsf";
    private static final String PROFILE_LINK = "account/profile.jsf";
    private static final String PAYMENT_LINK = "account/payments.jsf";
    private static final String SUBSCRIPTIONS_LINK = "account/subscriptions.jsf";
    private static final String USERS_LINK = "account/users.jsf";
    private static final String UNITS_LINK = "account/units.jsf";
    private static final String REPORTS_LINK = "account/reports.jsf";
    private static final String PROCESSES_LINK = "account/processes.jsf";
    private static final String OPERATIONS_LINK = "account/operations.jsf";

    private static final long serialVersionUID = -8392197098976931792L;

    private Map<String, String> linkMap;

    private ApplicationBean applicationBean;
    private UserBean userBean;

    private void initLinks() {
        if (linkMap == null) {
            linkMap = new LinkedHashMap<>(7);
            linkMap.put(MARKETPLACE_ACCOUNT_TITLE, ACCOUNT_LINK);

            boolean isLoggedInAndAdmin = isLoggedInAndAdmin();
            addProfileLink();
            addPaymentLink(isLoggedInAndAdmin);
            addSubscriptionsLink(isLoggedInAndAdmin);
            addUsersAndUnitsLinks(isLoggedInAndAdmin);
            addReportsLink();
            addProcessesLink();
            addOperationsLink();
            addAdministrationLink();
        }
    }

    private void addProfileLink() {
        setLinkVisible(MARKETPLACE_ACCOUNT_PROFILE_TITLE, PROFILE_LINK,
                HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_PROFILE);
    }

    private void addPaymentLink(boolean isLoggedInAndAdmin) {
        if (isLoggedInAndAdmin) {
            setLinkVisible(MARKETPLACE_ACCOUNT_PAYMENTS_TITLE, PAYMENT_LINK,
                    HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_PAYMENT);
        }
    }

    private void addSubscriptionsLink(boolean isLoggedInAndAdmin) {
        if (isLoggedInAndAdmin || isLoggedInAndSubscriptionManager()) {
            setLinkVisible(
                    MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_TITLE,
                    SUBSCRIPTIONS_LINK,
                    HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_SUBSCRIPTIONS);
        }
    }

    private void addUsersAndUnitsLinks(boolean isLoggedInAndAdmin) {
        if (isLoggedInAndAdmin) {
            setLinkVisible(MARKETPLACE_ACCOUNT_USERS_TITLE, USERS_LINK,
                    HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_USERS);
        } else if (isLoggedInAndUnitAdmin()) {
            setLinkVisible(MARKETPLACE_ACCOUNT_UNITS_TITLE, UNITS_LINK,
                    HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_UNITS);
        }
    }

    private void addReportsLink() {
        if (isReportingAvailable()) {
            setLinkVisible(MARKETPLACE_ACCOUNT_REPORTS_TITLE, REPORTS_LINK,
                    HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_REPORTS);
        }
    }

    private void addProcessesLink() {
        setLinkVisible(MARKETPLACE_ACCOUNT_PROCESSES_TITLE, PROCESSES_LINK,
                HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_PROCESSES);
    }

    private void addOperationsLink() {
        setLinkVisible(MARKETPLACE_ACCOUNT_OPERATIONS_TITLE, OPERATIONS_LINK,
                HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_OPERATIONS);
    }

    private void addAdministrationLink() {
        if (isAdministrationAvailable()) {
            setLinkVisible(MARKETPLACE_ACCOUNT_ADMINISTRATION_TITLE,
                    userBean.getAdminPortalAddress(),
                    HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_ADMINISTRATION);
        }
    }

    private void setLinkVisible(String title, String link, String menuKey) {
        if (!applicationBean.isUIElementHidden(menuKey)) {
            linkMap.put(title, link);
        }
    }

    /**
     * Return the link map, for which the keys refer to the resource bundle keys
     * of the link names and values refer to the actual URLs.
     *
     * @return the described map
     */
    public Map<String, String> getLinkMap() {
        initLinks();
        return linkMap;
    }

    public List<String> getLinkKeys() {
        List<String> list = new ArrayList<>();
        list.addAll(getLinkMap().keySet());
        return list;
    }

    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    /**
     * Check if reporting box should be displayed - reporting must be available
     * and user must be logged in and have the administrator role.
     * 
     * @return <code>true</code> if report box is available, <code>false</code>
     *         otherwise.
     */
    public boolean isReportingAvailable() {
        return (getApplicationBean().isReportingAvailable() && isLoggedInAndAdmin());
    }

    public boolean isAdministrationAvailable() {
        return (getUserBean().isAdministrationAccess());
    }

    public UserBean getUserBean() {
        if (userBean != null) {
            return userBean;
        }
        return JSFUtils.findBean("userBean");
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }
}
