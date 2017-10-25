/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-10-22                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * When install SAML_SP mode those information are required.
 * 
 */
public enum MandatoryAttributesInSamlSP {

    ADMIN_USER_ID,
    
    SSO_IDP_AUTHENTICATION_REQUEST_HTTP_METHOD,

    SSO_IDP_TRUSTSTORE,

    SSO_IDP_TRUSTSTORE_PASSWORD,

    SSO_IDP_URL,

    SSO_ISSUER_ID,

    SSO_STS_ENCKEY_LEN,

    SSO_STS_METADATA_URL,

    SSO_STS_URL;

}
