/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 06.06.2018                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.usage;

import org.oscm.app.v2_0.data.PasswordAuthentication;

/**
 * Object representing OSCM user credentials.
 */
class Credentials {

    private boolean isSSO;
    private long userKey;
    private String userId;
    private String password;
    private String orgId;

     Credentials(boolean isSSO) {
        this.isSSO = isSSO;
    }

    Credentials(boolean isSSO, String userId, String password) {
        this.isSSO = isSSO;
        this.userId = userId;
        this.password = password;
    }

    Credentials(boolean isSSO, long userKey, String password) {
        this.isSSO = isSSO;
        this.userKey = userKey;
        this.password = password;
    }

    long getUserKey() {
        return userKey;
    }

    void setUserKey(long userKey) {
        this.userKey = userKey;
    }

    String getUserId() {
        return userId;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    String getOrgId() {
        return orgId;
    }

    void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    PasswordAuthentication toPasswordAuthentication() {
        PasswordAuthentication pa = (isSSO) ? new PasswordAuthentication(
                userId, password) : new PasswordAuthentication(
                Long.toString(userKey), password);
        return pa;
    }

}
