/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 26.05.2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.data.User;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.intf.APPlatformService;

/** Mockup for the APP platform service */
public class APPlatformServiceMockup implements APPlatformService {
  public APPlatformException exceptionOnGetControllerSettings;
  public APPlatformException exceptionOnStoreControllerSettings;
  private HashMap<String, Setting> ctrl_settings;

  public APPlatformServiceMockup() {
    // Set default controller settings
    ctrl_settings = new HashMap<>();
    ctrl_settings.put("BSS_ORGANIZATION_ID", new Setting("BSS_ORGANIZATION_ID", "orgId"));
    ctrl_settings.put("BSS_USER_KEY", new Setting("BSS_USER_KEY", "12345"));
    ctrl_settings.put("BSS_USER_ID", new Setting("BSS_USER_ID", "userId"));
    ctrl_settings.put("BSS_USER_PWD", new Setting("BSS_USER_PWD", "secret1"));
    ctrl_settings.put("TEST1", new Setting("TEST1", "secret2"));
    ctrl_settings.put("TEST2", new Setting("TEST2", "secret3"));
  }

  @Override
  public void sendMail(List<String> mailAddresses, String subject, String text)
      throws APPlatformException {}

  @Override
  public String getEventServiceUrl() throws ConfigurationException {
    return null;
  }

  @Override
  public boolean exists(String controllerId, String instanceId) {
    return false;
  }

  @Override
  public String getBSSWebServiceUrl() throws ConfigurationException {
    return null;
  }

  @Override
  public HashMap<String, Setting> getControllerSettings(String arg0, PasswordAuthentication arg1)
      throws AuthenticationException, ConfigurationException, APPlatformException {
    if (exceptionOnGetControllerSettings != null) {
      throw exceptionOnGetControllerSettings;
    }
    return ctrl_settings;
  }

  @Override
  public boolean lockServiceInstance(String arg0, String arg1, PasswordAuthentication arg2)
      throws AuthenticationException, APPlatformException {
    return false;
  }

  @Override
  public void storeControllerSettings(
      String arg0, HashMap<String, Setting> controllerSettings, PasswordAuthentication arg2)
      throws AuthenticationException, ConfigurationException, APPlatformException {
    if (exceptionOnStoreControllerSettings != null) {
      throw exceptionOnStoreControllerSettings;
    }
    ctrl_settings = controllerSettings;
  }

  @Override
  public void unlockServiceInstance(String arg0, String arg1, PasswordAuthentication arg2)
      throws AuthenticationException, APPlatformException {}

  @Override
  public User authenticate(String controllerId, PasswordAuthentication authentication)
      throws AuthenticationException, ConfigurationException, APPlatformException {
    return null;
  }

  @Override
  public void requestControllerSettings(String controllerId) throws ConfigurationException {}

  @Override
  public Collection<String> listServiceInstances(
      String controllerId, PasswordAuthentication authentication)
      throws AuthenticationException, APPlatformException {
    return new ArrayList<>();
  }

  @Override
  public ProvisioningSettings getServiceInstanceDetails(
      String controllerId, String instanceId, PasswordAuthentication authentication)
      throws AuthenticationException, APPlatformException {
    return null;
  }

  @Override
  public String getBSSWebServiceWSDLUrl() throws ConfigurationException {
    return null;
  }

  @Override
  public void storeServiceInstanceDetails(
      String controllerId,
      String instanceId,
      ProvisioningSettings settings,
      PasswordAuthentication authentication)
      throws AuthenticationException, ConfigurationException, APPlatformException {
    // store service settings not supported by mock
  }

  @Override
  public ProvisioningSettings getServiceInstanceDetails(
      String controllerId, String instanceId, String subscriptionId, String organizationId)
      throws APPlatformException {
    return null;
  }

  @Override
  public boolean checkToken(String token, String signature) {
    return false;
  }

  @Override
  public void updateUserCredentials(long useKey, String username, String password) {}
}
