/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import java.security.Key;
import java.security.PublicKey;

import javax.xml.crypto.KeySelectorResult;

/**
 * @author kulle
 * 
 */
class SimpleKeySelectorResult implements KeySelectorResult {

    private PublicKey publicKey;

    public SimpleKeySelectorResult(PublicKey pk) {
        this.publicKey = pk;
    }

    @Override
    public Key getKey() {
        return publicKey;
    }

}
