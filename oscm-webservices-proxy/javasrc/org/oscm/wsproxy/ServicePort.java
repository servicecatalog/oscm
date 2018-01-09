/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.wsproxy;

/**
 * Enumeration class for the different web service ports of OSCM.
 * 
 * - BASIC: service port for basic authentication using user key and password;
 * 
 * - CLIENTCERT: service port for authentication using certificates;
 * 
 * - STS: service port for authentication via SAML token using WS-Security
 * 
 */
public enum ServicePort {
    CLIENTCERT, BASIC, STS
}
