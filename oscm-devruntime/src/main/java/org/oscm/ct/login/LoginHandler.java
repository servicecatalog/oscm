/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.03.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ct.login;

/**
 * When working with enterprise beans we rely on the fact that the principal is
 * set. However, to do so it is necessary to provide the credentials to the ejb
 * container.
 * 
 * <p>
 * There are no standards for that handling. So there might be different
 * required approaches for the particular application servers. This interface is
 * meant to provide the login capability in a neutral form without dependencies
 * to any vendor specific libraries.
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
public interface LoginHandler {

    /**
     * Performs a login operation for the glassfish application server.
     * 
     * @param username
     *            The key of the user that should be logged in.
     * @param password
     *            The user's password.
     * @throws Exception
     */
    public void login(String username, String password) throws Exception;

    /**
     * Performs a logout operation for the glassfish application server.
     * 
     * @throws Exception
     */
    public void logout() throws Exception;

}
