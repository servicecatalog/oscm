/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 10.01.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.webtest.authentication;

import javax.security.auth.login.LoginException;

/** Definition of tasks which are related to OSCM authentication */
public interface AuthenticationContext {

  /**
   * Attempts to login to OSCM portal with given credentials. In case of failure it throws {@link
   * LoginException}
   *
   * @param userId user's id
   * @param userPwd user's password
   * @throws LoginException
   */
  void loginPortal(String userId, String userPwd) throws LoginException, InterruptedException;
  void loginMarketplace(String userId, String userPwd) throws LoginException, InterruptedException;
}
