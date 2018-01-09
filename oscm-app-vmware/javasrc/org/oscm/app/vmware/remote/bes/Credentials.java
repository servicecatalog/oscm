/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.remote.bes;

import org.oscm.app.v2_0.data.PasswordAuthentication;

/**
 * Object representing OSCM user credentials.
 */
public class Credentials {

    private boolean isSSO;
    private long userKey;
    private String userId;
    private String password;
    private String orgId;

    public Credentials(boolean isSSO) {
        this.isSSO = isSSO;
    }

    public Credentials(boolean isSSO, String userId, String password) {
        this.isSSO = isSSO;
        this.userId = userId;
        this.password = password;
    }

    public Credentials(boolean isSSO, long userKey, String password) {
        this.isSSO = isSSO;
        this.userKey = userKey;
        this.password = password;
    }

    public long getUserKey() {
        return userKey;
    }

    public void setUserKey(long userKey) {
        this.userKey = userKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public PasswordAuthentication toPasswordAuthentication() {
        PasswordAuthentication pa = (isSSO) ? new PasswordAuthentication(
                userId, password) : new PasswordAuthentication(
                Long.toString(userKey), password);
        return pa;
    }

}
