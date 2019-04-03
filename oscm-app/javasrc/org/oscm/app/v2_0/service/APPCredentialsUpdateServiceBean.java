package org.oscm.app.v2_0.service;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.oscm.app.remote.APPCredentialsUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Remote(APPCredentialsUpdateService.class)
@Stateless
public class APPCredentialsUpdateServiceBean implements APPCredentialsUpdateService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(APPCredentialsUpdateServiceBean.class);

  @EJB protected APPConfigurationServiceBean configService;

  @Override
  public String updateUserCredentials(long userId, String username, String password) {

    LOGGER.info(
        "From logger: Update user credentials: " + userId + ", " + username + ", " + password);
    configService.getUserConfiguredControllers(username);

    return userId + ", " + username + ", " + password;
  }
}
