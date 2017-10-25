/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 06.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

/**
 * Wrapper object to store the SOAP 1.1 port description for one WSDL.
 * 
 * @author Mike J&auml;ger
 */
public class WSPortDescription {

    private String targetNamespace;

    private String endpointURL;

    private String version;

    public WSPortDescription() {
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
