/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-08-29                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the ways available to access the application underlying to a
 * service and subscription.
 */
public enum ServiceAccessType {

    /**
     * The platform is responsible for the user login but not for any subsequent
     * requests.
     */
    LOGIN,

    /**
     * The platform is not involved in any way in the communication between the
     * user and the application, but only handles the subscriptions and billing.
     */
    DIRECT,

    /**
     * The platform is responsible for user authentication, where it acts as a
     * SAML identity provider. Any subsequent requests are handled by the
     * application without involving the platform in any way.
     */
    USER,

    /**
     * The platform is not involved in any way in the communication between the
     * user and the application or in the interaction between the customer and
     * the service provider organizations. It only lists the service for the
     * application. Subscriptions and billing are handled externally.
     */
    EXTERNAL;

}
