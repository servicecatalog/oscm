package org.oscm.app.v2_0.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  @EJB protected APPConfigurationServiceBean configService;

  @Override
  public String updateUserCredentials(long userId, String username, String password) {

    LOGGER.info(
        "From logger: Update user credentials: " + userId + ", " + username + ", " + password);
    List<String> controllers = configService.getUserConfiguredControllers(username);

    Map<String, Setting> settings =
        controllers
            .stream()
            .filter(controllerId -> controllerId.equals("ess.aws"))
            .collect(
                Collectors.toMap(
                    controllerId -> controllerId, x -> new Setting("BSS_USER_ID", "test123")));
    settings.values().stream().forEach(s -> LOGGER.info(s.getKey() + s.getValue()));
    try {
      configService.storeControllerConfigurationSettings(
          "ess.aws", (HashMap<String, Setting>) settings);
    } catch (ConfigurationException e) { // TODO Auto-generated catch block
      LOGGER.error(e.getMessage(),e);
    }
    return userId + ", " + username + ", " + password;
  }
}
