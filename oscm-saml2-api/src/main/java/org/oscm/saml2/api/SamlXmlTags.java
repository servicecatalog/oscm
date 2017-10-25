/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

/**
 * @author kulle
 * 
 */
interface SamlXmlTags {

    String ATTRIBUTE_RECIPIENT = "Recipient";
    String ATTRIBUTE_ID = "ID";
    String ATTRIBUTE_NAME = "Name";
    String ATTRIBUTE_NOT_ON_OR_AFTER = "NotOnOrAfter";
    String ATTRIBUTE_IN_RESPONSE_TO = "InResponseTo";

    String NODE_SIGNATURE = "Signature";
    String NODE_ASSERTION = "Assertion";
    String NODE_KEY_INFO = "KeyInfo";
    String NODE_KEY_VALUE = "KeyValue";
    String NODE_X509DATA = "X509Data";
}
