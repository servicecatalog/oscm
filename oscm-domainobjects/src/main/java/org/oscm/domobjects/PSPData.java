/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 06.10.2011                                                      
 *                                                                              
 *  Completion Time: 06.10.2011                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The data container for the PSP domain objects.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class PSPData extends DomainDataContainer {

    private static final long serialVersionUID = 8839620149580058044L;

    /**
     * The identifier of the payment service provider, what is a BES internal
     * information.
     */
    private String identifier;

    /**
     * The URL at which to find the WSDL for the PSP specific implementation.
     */
    private String wsdlUrl;

    /**
     * The distinguished name for certificate based web service security.
     */
    @Column(nullable = true)
    private String distinguishedName;

    public String getIdentifier() {
        return identifier;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

}
