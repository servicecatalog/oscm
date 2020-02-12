/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 16.05.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.oscm.internal.types.enumtypes.ConfigurationKey.SSO_SIGNING_KEYSTORE;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.types.enumtypes.AuthenticationMode;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.NotExistentTenantException;
import org.oscm.internal.types.exception.WrongTenantConfigurationException;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOTenant;
import org.oscm.tenant.bean.TenantServiceBean;
import org.oscm.types.constants.Configuration;

/** @author stavreva */
public class AuthenticationSettingsTest {

  private static final String ISSUER = "OSCM";
  private static final String IDP = "http://idp.de:9080/openam/SSORedirect/metaAlias/idp";
  private static final String IDP_UPPERCASE = IDP.toUpperCase();
  private static final String IDP_CONTEXT_ROOT = "http://idp.de:9080/openam";
  private static final String IDP_HTTP_METHOD = "POST";
  private static final String IDP_KEYSTORE_PASS = "changeit";
  private static final String BASE_URL = "http://www.example.de";

  private AuthenticationSettings authSettings;
  private ConfigurationService cfgMock;
  private TenantService tenantService;
  private VOTenant tenant;

  @Before
  public void setup() throws Exception {
    tenantService = spy(new TenantServiceBean() {});
    tenant = new VOTenant();
    tenant.setTenantId("tenantID");
    doReturn(tenant).when(tenantService).getTenantByTenantId("tenantID");

    cfgMock = mock(ConfigurationService.class);
  }

  private void givenMock(AuthenticationMode authMode, String idpUrl)
      throws NotExistentTenantException, WrongTenantConfigurationException {
    doReturn(
            new VOConfigurationSetting(
                ConfigurationKey.AUTH_MODE, Configuration.GLOBAL_CONTEXT, authMode.name()))
        .when(cfgMock)
        .getVOConfigurationSetting(ConfigurationKey.AUTH_MODE, Configuration.GLOBAL_CONTEXT);
    doReturn(
            new VOConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT, idpUrl))
        .when(cfgMock)
        .getVOConfigurationSetting(ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT);
    doReturn(
            new VOConfigurationSetting(
                ConfigurationKey.SSO_SIGNING_KEY_ALIAS,
                Configuration.GLOBAL_CONTEXT,
                IDP_KEYSTORE_PASS))
        .when(cfgMock)
        .getVOConfigurationSetting(
            ConfigurationKey.SSO_SIGNING_KEY_ALIAS, Configuration.GLOBAL_CONTEXT);
    doReturn(
            new VOConfigurationSetting(
                ConfigurationKey.SSO_SIGNING_KEYSTORE_PASS,
                Configuration.GLOBAL_CONTEXT,
                IDP_KEYSTORE_PASS))
        .when(cfgMock)
        .getVOConfigurationSetting(
            ConfigurationKey.SSO_SIGNING_KEYSTORE_PASS, Configuration.GLOBAL_CONTEXT);
    doReturn(
            new VOConfigurationSetting(
                SSO_SIGNING_KEYSTORE, Configuration.GLOBAL_CONTEXT, IDP_KEYSTORE_PASS))
        .when(cfgMock)
        .getVOConfigurationSetting(SSO_SIGNING_KEYSTORE, Configuration.GLOBAL_CONTEXT);
    authSettings = new AuthenticationSettings(tenantService, cfgMock);
    authSettings.init("tenantID");
  }

  private void givenMockWithoutSettings()
      throws NotExistentTenantException, WrongTenantConfigurationException {
    authSettings = new AuthenticationSettings(tenantService, cfgMock);
    authSettings.init(null);
  }

  @Test
  public void constructor() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);

    // then
    verify(cfgMock, times(1))
        .getVOConfigurationSetting(ConfigurationKey.AUTH_MODE, Configuration.GLOBAL_CONTEXT);
  }

  @Test
  public void isServiceProvider() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);

    // then
    assertTrue(authSettings.isServiceProvider());
  }

  @Test
  public void isInternal() throws Exception {

    // given
    givenMock(AuthenticationMode.INTERNAL, IDP);

    // then
    assertTrue(authSettings.isInternal());
  }

  @Test
  public void getConfigurationSetting_null() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);
    doReturn(null)
        .when(cfgMock)
        .getVOConfigurationSetting(ConfigurationKey.LOG_LEVEL, Configuration.GLOBAL_CONTEXT);

    // then
    assertNull(authSettings.getConfigurationSetting(cfgMock, ConfigurationKey.LOG_LEVEL));
  }

  @Test
  public void getConfigurationSetting() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);

    // then
    assertEquals("OIDC", authSettings.getConfigurationSetting(cfgMock, ConfigurationKey.AUTH_MODE));
  }

  @Test
  public void getContextRoot_null() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);

    // then
    assertNull(authSettings.getContextRoot(null));
  }

  @Test
  public void getContextRoot_empty() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);

    // then
    assertNull(authSettings.getContextRoot(""));
  }

  @Test
  public void getContextRoot_lessTokens() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);

    // then
    assertNull(authSettings.getContextRoot("http://www.idp.de/"));
  }

  @Test
  public void getContextRoot() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);

    // then
    assertEquals(IDP_CONTEXT_ROOT, authSettings.getContextRoot(IDP));
  }

  @Test
  public void getSigningKeystorePass() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);

    // then
    assertEquals(IDP_KEYSTORE_PASS, authSettings.getSigningKeystorePass());
  }

  @Test
  public void getSigningKeystore() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);

    // then
    assertEquals(IDP_KEYSTORE_PASS, authSettings.getSigningKeystore());
  }

  @Test
  public void getSigningKeyAlias() throws Exception {

    // given
    givenMock(AuthenticationMode.OIDC, IDP);

    // then
    assertEquals(IDP_KEYSTORE_PASS, authSettings.getSigningKeyAlias());
  }

  @Ignore // (expected = WrongTenantConfigurationException.class)
  public void givenMockWithoutSettingsTest() throws Exception {
    givenMockWithoutSettings();
    fail();
  }

  @Test
  public void getTenantBlank() throws Exception {
    // given
    givenMock(AuthenticationMode.OIDC, IDP);
    authSettings = spy(authSettings);
    doReturn(IDP_KEYSTORE_PASS)
        .when(authSettings)
        .getConfigurationSetting(cfgMock, ConfigurationKey.SSO_SIGNING_KEYSTORE_PASS);

    // then
    assertEquals(IDP_KEYSTORE_PASS, authSettings.getSigningKeystorePass());
  }
}
