package org.oscm.app.v2_0.service;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.oscm.app.remote.APPCredentialsUpdateService;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class APPCredentialsUpdateServiceBean implements APPCredentialsUpdateService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(APPCredentialsUpdateServiceBean.class);

  @EJB protected APPConfigurationServiceBean configService;

  @Override
  public String updateUserCredentials(long userId, String username, String password) {

    LOGGER.info(
        "From logger: Update user credentials: " + userId + ", " + username + ", " + password);
    System.out.println("Update user credentials: " + userId + ", " + username + ", " + password);

    try {
      configService.getAllProxyConfigurationSettings().values().stream()
          .forEach(
              (s) ->
                  LOGGER.info(s.getControllerId() + " -> " + s.getKey() + " -> " + s.getValue()));
    } catch (ConfigurationException e) {
      System.out.println("ERROR");
      e.printStackTrace();
    }

    return userId + ", " + username + ", " + password;
  }
}
package org.oscm.app.remote;

import javax.ejb.Remote;

@Remote
public interface APPCredentialsUpdateService {

  String updateUserCredentials(long userId, String username, String password);
}
