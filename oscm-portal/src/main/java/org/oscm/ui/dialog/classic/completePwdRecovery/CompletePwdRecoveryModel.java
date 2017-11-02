/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2013-2-25
 *
 *******************************************************************************/

package org.oscm.ui.dialog.classic.completePwdRecovery;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * @author Qiu
 * @author tomasz.trebski
 */
@ViewScoped
@ManagedBean(name = "completePwdRecoveryModel")
public class CompletePwdRecoveryModel {
    private String userId;
    // It is safer to store password as char[]
    private char[] newPassword;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNewPassword() {
        return this.newPassword == null ? null : new String(this.newPassword);
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword.toCharArray();
    }

}
