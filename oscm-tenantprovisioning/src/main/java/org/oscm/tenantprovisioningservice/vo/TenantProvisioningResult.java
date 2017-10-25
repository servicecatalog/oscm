/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Ronny Weiser                                                      
 *                                                                              
 *  Creation Date: 12.08.2009                                                      
 *                                                                              
 *  Completion Time: 12.08.2009
 *                                                                              
 *******************************************************************************/
package org.oscm.tenantprovisioningservice.vo;

import java.io.Serializable;

public class TenantProvisioningResult implements Serializable {

    private static final long serialVersionUID = -2349183800432878186L;

    private String productInstanceId;

    private boolean asyncProvisioning;

    private String accessInfo;

    private String baseUrl;

    private String loginPath;
    
    private String resultMesage;

    public String getProductInstanceId() {
        return productInstanceId;
    }

    public void setProductInstanceId(String productInstanceId) {
        this.productInstanceId = productInstanceId;
    }

    public boolean isAsyncProvisioning() {
        return asyncProvisioning;
    }

    public void setAsyncProvisioning(boolean asyncProvisioning) {
        this.asyncProvisioning = asyncProvisioning;
    }

    public String getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(String accessInfo) {
        this.accessInfo = accessInfo;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    public String getResultMesage() {
        return resultMesage;
    }

    public void setResultMesage(String resultMesage) {
        this.resultMesage = resultMesage;
    }

}
