/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019
 *                                                                                                                                 
 *  Creation Date: 03-04-2019                                                     
 *                                                                              
 *******************************************************************************/
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

@Stateless
public class APPCredentialsUpdateServiceBean implements APPCredentialsUpdateService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(APPCredentialsUpdateServiceBean.class);

  private static final String PROXY_CONTROLLER_ID = "PROXY";
  private static final String PWD_KEY = "BSS_USER_PWD";

  @EJB protected APPConfigurationServiceBean configService;

  @Override
  public void updateUserCredentials(long userId, String username, String password) {

    List<String> controllers = configService.getUserConfiguredControllers(username);

    if (controllers.contains(PROXY_CONTROLLER_ID)) {
      HashMap<String, String> settings = new HashMap<>();
      settings.put(PWD_KEY, password);
      try {
        configService.storeAppConfigurationSettings(settings);
        logSuccessInfo(username, PROXY_CONTROLLER_ID);
      } catch (ConfigurationException | GeneralSecurityException e) {
        logErrorMsg(PROXY_CONTROLLER_ID, PWD_KEY, e);
      }
    }

    controllers.stream()
        .filter(id -> !PROXY_CONTROLLER_ID.equals(id))
        .forEach(
            id -> {
              HashMap<String, Setting> settings = new HashMap<>();
              settings.put(PWD_KEY, new Setting(PWD_KEY, password));
              try {
                configService.storeControllerConfigurationSettings(id, settings);
                logSuccessInfo(username, id);
              } catch (ConfigurationException e) {
                logErrorMsg(id, PWD_KEY, e);
              }
            });
  }

  private void logSuccessInfo(String username, String id) {
    LOGGER.info("User [" + username + "] password for controller [" + id + "] updated");
  }

  private void logErrorMsg(String controllerId, String settingKey, Exception excp) {
    LOGGER.error(
        "Unable to store controller [" + controllerId + "] setting [" + settingKey + "]", excp);
  }
}
