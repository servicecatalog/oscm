/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 03-04-2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.v2_0.service;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.oscm.app.remote.APPCredentialsUpdateService;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Remote(APPCredentialsUpdateService.class)
@Stateless
public class APPCredentialsUpdateServiceBean implements APPCredentialsUpdateService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(APPCredentialsUpdateServiceBean.class);

  private static final String PROXY_CONTROLLER_ID = "PROXY";

  @EJB protected APPConfigurationServiceBean configService;

  @Override
  public String updateUserCredentials(long userId, String username, String password) {

    List<String> controllers = configService.getUserConfiguredControllers(username);

    if (controllers.contains(PROXY_CONTROLLER_ID)) {
      HashMap<String, String> settings = new HashMap<>();
      settings.put("BSS_USER_PWD", password);
      try {
        configService.storeAppConfigurationSettings(settings);
        logSuccessInfo(username);
      } catch (ConfigurationException | GeneralSecurityException e) {
        LOGGER.error("Unable to store proxy settings " + settings, e.getMessage(), e);
      }
    }

    controllers.stream()
        .filter(id -> !PROXY_CONTROLLER_ID.equals(id))
        .forEach(
            id -> {
              HashMap<String, Setting> settings = new HashMap<>();
              settings.put("BSS_USER_PWD", new Setting("BSS_USER_PWD", password));
              try {
                configService.storeControllerConfigurationSettings(id, settings);
                logSuccessInfo(username);
              } catch (ConfigurationException e) {
                LOGGER.error("Unable to store controller settings " + settings, e.getMessage(), e);
              }
            });

    return userId + ", " + username + ", " + password;
  }

  private void logSuccessInfo(String username) {
    LOGGER.info("User [" + username + "] password updated");
  }
}
