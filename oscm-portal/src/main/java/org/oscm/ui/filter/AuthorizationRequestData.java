/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 05.07.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.j2ep.AdmRule;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;

/**
 * @author kulle
 */
public class AuthorizationRequestData {

    private boolean isRequestedToChangePwd;
    private boolean isMarketplace;
    private boolean isMarketplaceLoginPage;
    private boolean isMarketplaceErrorPage;
    private boolean isAccessToServiceURL;
    private boolean isLandingPage;

    private String marketplaceId;
    private String relativePath;
    private String password;
    private String newPassword;
    private String newPassword2;
    private String userId;
    private String subscriptionKey;
    private String contextPath;
    private String tenantID;

    private VOUserDetails userDetails;

    /**
     * Default constructor
     */
    AuthorizationRequestData() {
        isRequestedToChangePwd = false;
        subscriptionKey = "";
    }

    void refreshData(HttpServletRequest httpRequest) {
        if (password == null) {
            password = httpRequest
                    .getParameter(BesServletRequestReader.REQ_PARAM_PASSWORD);
        }

        if (userDetails != null) {
            userId = userDetails.getUserId();
        }

        if (userId == null) {
            userId = httpRequest.getParameter(Constants.REQ_PARAM_USER_ID);
        }

        if (ADMStringUtils.isBlank(subscriptionKey)) {
            subscriptionKey = httpRequest
                    .getParameter(Constants.REQ_PARAM_SUB_KEY);
        }

    }

    public boolean isRequiredToChangePwd() {
        return !isMarketplaceLoginPage()
                && !getRelativePath().startsWith("/public/")
                && !getRelativePath().startsWith("/javax.faces.resource/")
                && userDetails != null
                && userDetails.getStatus() == UserAccountStatus.PASSWORD_MUST_BE_CHANGED;
    }

    /**
     * @return the isRequestedToChangePwd
     */
    public boolean isRequestedToChangePwd() {
        return isRequestedToChangePwd;
    }

    /**
     * @param isRequestedToChangePwd
     *            the isRequestedToChangePwd to set
     */
    void setRequestedToChangePwd(boolean isRequestedToChangePwd) {
        this.isRequestedToChangePwd = isRequestedToChangePwd;
    }

    /**
     * @return the isMarketplace
     */
    public boolean isMarketplace() {
        return isMarketplace;
    }

    /**
     * @param isMarketplace
     *            the isMarketplace to set
     */
    void setMarketplace(boolean isMarketplace) {
        this.isMarketplace = isMarketplace;
    }

    /**
     * @return the isMarketplaceLoginPage
     */
    public boolean isMarketplaceLoginPage() {
        return isMarketplaceLoginPage;
    }

    /**
     * @param isMarketplaceLoginPage
     *            the isMarketplaceLoginPage to set
     */
    void setMarketplaceLoginPage(boolean isMarketplaceLoginPage) {
        this.isMarketplaceLoginPage = isMarketplaceLoginPage;
    }

    /**
     * @return the isMarketplaceErrorPage
     */
    public boolean isMarketplaceErrorPage() {
        return isMarketplaceErrorPage;
    }

    /**
     * @param isMarketplaceErrorPage
     *            the isMarketplaceErrorPage to set
     */
    void setMarketplaceErrorPage(boolean isMarketplaceErrorPage) {
        this.isMarketplaceErrorPage = isMarketplaceErrorPage;
    }

    /**
     * @return the marketplaceId
     */
    public String getMarketplaceId() {
        return marketplaceId;
    }

    /**
     * @param marketplaceId
     *            the marketplaceId to set
     */
    void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    /**
     * @return the relativePath
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * @param relativePath
     *            the relativePath to set
     */
    void setRelativePath(String relativePath) {
        this.relativePath = relativePath == null ? "" : relativePath;
        Matcher matcher = AdmRule.getMatchPattern().matcher(getRelativePath());
        isAccessToServiceURL = false;
        if (matcher.matches()) {
            subscriptionKey = matcher.group(1).substring(1);
            contextPath = matcher.group(2);
            isAccessToServiceURL = true;
        }
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the newPassword
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * @param newPassword
     *            the newPassword to set
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * @return the newPassword2
     */
    public String getNewPassword2() {
        return newPassword2;
    }

    /**
     * @param newPassword2
     *            the newPassword2 to set
     */
    public void setNewPassword2(String newPassword2) {
        this.newPassword2 = newPassword2;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the subscriptionKey
     */
    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    /**
     * @return the voUser
     */
    public VOUserDetails getUserDetails() {
        return userDetails;
    }

    /**
     * @param voUser
     *            the voUser to set
     */
    void setUserDetails(VOUserDetails voUser) {
        this.userDetails = voUser;
    }

    /**
     * @return the contextPath
     */
    public String getContextPath() {
        return contextPath;
    }

    public boolean isPublicURL(String publicUrlPattern) {
        return !isRequestedToChangePwd() && publicUrlPattern != null
                && getRelativePath().matches(publicUrlPattern);
    }

    public boolean isPasswordSet() {
        final String pwd = getPassword();
        return !ADMStringUtils.isBlankNoTrim(pwd);
    }

    public boolean isAccessToServiceUrl() {
        return isAccessToServiceURL;
    }

    public boolean isLandingPage() {
        return isLandingPage;
    }

    public void setLandingPage(boolean isLandingPage) {
        this.isLandingPage = isLandingPage;
    }

    public String getTenantID() {
        return tenantID;
    }

    public void setTenantID(String tenantID) {
        this.tenantID = tenantID;
    }
}
