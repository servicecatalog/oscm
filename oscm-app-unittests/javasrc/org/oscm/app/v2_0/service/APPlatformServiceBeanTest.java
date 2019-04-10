/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019
 *                                                                                                                                 
 *  Creation Date: 09.04.2019                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class APPlatformServiceBeanTest {

  @Spy @InjectMocks private APPlatformServiceBean applatformService = new APPlatformServiceBean();

  @Mock private APPConfigurationServiceBean appConfigurationService;

  @SuppressWarnings("unchecked")
  @Test
  public void updateUserCredentials_isExecutedSuccessfully_ifAllControllersConfigured()
      throws Exception {

    // given
    List<String> controllers =
        Arrays.asList("PROXY", "ess.aws", "ess.openstack", "ess.azure", "ess.vmware");
    when(appConfigurationService.getUserConfiguredControllers(anyString())).thenReturn(controllers);

    // when
    applatformService.updateUserCredentials(1000, "test", "test");

    // then
    verify(appConfigurationService, times(1)).storeAppConfigurationSettings(any(HashMap.class));
    verify(appConfigurationService, times(4))
        .storeControllerConfigurationSettings(anyString(), any(HashMap.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void updateUserCredentials_hasNoInteractions_ifNoControllerIsConfigured()
      throws Exception {

    // given
    List<String> controllers = new ArrayList<>();
    when(appConfigurationService.getUserConfiguredControllers(anyString())).thenReturn(controllers);

    // when
    applatformService.updateUserCredentials(1000, "test", "test");

    // then
    verify(appConfigurationService, never()).storeAppConfigurationSettings(any(HashMap.class));
    verify(appConfigurationService, never())
        .storeControllerConfigurationSettings(anyString(), any(HashMap.class));
  }
}
