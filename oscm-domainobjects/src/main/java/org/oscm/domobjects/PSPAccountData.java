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

import javax.persistence.Embeddable;

/**
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class PSPAccountData extends DomainDataContainer {

    private static final long serialVersionUID = 4605667171983257386L;

    private String pspIdentifier;

    public void setPspIdentifier(String pspIdentifier) {
        this.pspIdentifier = pspIdentifier;
    }

    public String getPspIdentifier() {
        return pspIdentifier;
    }

}
