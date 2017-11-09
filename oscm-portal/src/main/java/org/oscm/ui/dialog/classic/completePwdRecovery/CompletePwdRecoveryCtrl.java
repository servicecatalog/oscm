/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2013-2-25
 *
 *******************************************************************************/

package org.oscm.ui.dialog.classic.completePwdRecovery;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;

import org.oscm.ui.beans.BaseBean;

/**
 * @author Qiu
 * @author tomasz.trebski
 */
@ViewScoped
@ManagedBean(name = "completePwdRecoveryCtrl")
public class CompletePwdRecoveryCtrl extends BaseBean {
    @ManagedProperty(value = "#{completePwdRecoveryModel}")
    protected CompletePwdRecoveryModel model;
    protected boolean showError = false;
    protected String token;

    /**
     * Adds a error message to the page and sets showError=true
     *
     * @param errorKey
     *            The error key.
     *
     */
    private void addErrorMessage(String errorKey) {
        addMessage(null, FacesMessage.SEVERITY_ERROR, errorKey);
        setShowError(true);
    }

    /**
     * Complete password recovery procedure:Change user password
     *
     * @return String <br>
     *         OUTCOME_SUCCESS | OUTCOME_ERROR
     */
    public String completePasswordRecovery() {

        final String userId = this.model.getUserId();
        final String newPassword = this.model.getNewPassword();

        final boolean changePassword = this.getPasswordRecoveryService().completePasswordRecovery(userId, newPassword);

        if (changePassword) {
            this.addMessage(null, FacesMessage.SEVERITY_INFO, INFO_RECOVERPASSWORD_SUCCESS);
            return OUTCOME_SUCCESS;
        }

        this.addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_USER_PWD_RESET);
        return OUTCOME_ERROR;
    }

    public CompletePwdRecoveryModel getModel() {
        return this.model;
    }

    public void setModel(CompletePwdRecoveryModel model) {
        this.model = model;
    }

    public boolean isShowError() {
        return showError;
    }

    public void setShowError(boolean showError) {
        this.showError = showError;
    }

    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public void setToken(final String token) {

        if (this.isTokenMissing(token)) {
            this.addErrorMessage(ERROR_RECOVERPASSWORD_INVALID_LINK);
            return;
        }

        final String userId;

        if ((userId = this.getUserId(token)) == null) {
            this.addErrorMessage(ERROR_RECOVERPASSWORD_INVALID_LINK);
            return;
        } else {
            this.getModel().setUserId(userId);
        }

        this.token = token;
    }

    protected boolean isTokenMissing(final String token) {
        return StringUtils.isEmpty(token);
    }

    protected String getUserId(final String token) {
        return getPasswordRecoveryService().confirmPasswordRecoveryLink(token,
                isMarketplaceSet(getRequest()) ? this.ui.getMarketplaceId() : null);
    }
}
