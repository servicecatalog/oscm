/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.util.Calendar;

/**
 * @author kulle
 * 
 */
class VerifierConfiguration {

    private final String tenantID;
    private String requestId;
    private String acsUrl;
    private String acsUrlHttps;
    private Calendar referenceTime;

    public VerifierConfiguration(String requestId, String acsUrl,
                                 String acsUrlHttps, Calendar referenceTime, String tenantID) {
        this.requestId = requestId;
        this.acsUrl = acsUrl;
        this.acsUrlHttps = acsUrlHttps;
        this.referenceTime = referenceTime;
        this.tenantID = tenantID;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getAcsUrl() {
        return acsUrl;
    }

    public String getAcsUrlHttps() {
        return acsUrlHttps;
    }

    public Calendar getReferenceTime() {
        return referenceTime;
    }

    public String getTenantID() {
        return tenantID;
    }
}
