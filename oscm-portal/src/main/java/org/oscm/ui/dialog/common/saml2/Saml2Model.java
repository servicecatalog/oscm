/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.10.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.common.saml2;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;

/**
 * @author roderus
 * 
 */
@ManagedBean
@RequestScoped
public class Saml2Model implements Serializable {

    private static final long serialVersionUID = 1L;

    private String encodedAuthnRequest;
    private String relayState;
    private String acsUrl;

    public String getEncodedAuthnRequest() {
        return encodedAuthnRequest;
    }

    public void setEncodedAuthnRequest(String encodedAuthnRequest) {
        this.encodedAuthnRequest = encodedAuthnRequest;
    }

    public String getRelayState() {
        return relayState;
    }

    public void setRelayState(String relayState) {
        this.relayState = relayState;
    }

    public String getAcsUrl() {
        return acsUrl;
    }

    public void setAcsUrl(String acsUrl) {
        this.acsUrl = acsUrl;
    }

}
