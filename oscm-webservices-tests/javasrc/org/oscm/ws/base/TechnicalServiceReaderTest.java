/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 04.02.2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ws.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

/** @author kulle */
public class TechnicalServiceReaderTest {

  private TechnicalServiceReader reader;
  private Properties configSettings;

  @Before
  public void setup() throws Exception {
    assumeFalse(ServiceFactory.getDefault().isSSOMode());
    configSettings = WebserviceTestBase.getConfigSetting();
    configSettings.put("APP_BASE_URL", "http://estkulle:8880/oscm-app");
    configSettings.put("DEFAULT_USER", "1000");
    reader = new TechnicalServiceReader(configSettings);
  }

  @Test
  public void replaceProperties() throws Exception {
    // given
    String line = "provisioningUrl=\"^APP_BASE_URL^\" provisioningVersion=\"1.0\">";

    // when
    String result = reader.replaceProperties(line);

    // then
    assertEquals(
        "provisioningUrl=\"http://estkulle:8880/oscm-app\" provisioningVersion=\"1.0\">", result);
  }

  @Test
  public void replaceProperties_twoReplacements() throws Exception {
    // given
    String line = "provisioningUrl=\"^APP_BASE_URL^\" provisioningVersion=\"^DEFAULT_USER^\">";

    // when
    String result = reader.replaceProperties(line);

    // then
    assertEquals(
        "provisioningUrl=\"http://estkulle:8880/oscm-app\" provisioningVersion=\"1000\">", result);
  }

  @Test
  public void replaceProperties_noReplacement() throws Exception {
    // given
    String line = "provisioningUrl=\"http://localhost:8080\" provisioningVersion=\"1\">";

    // when
    String result = reader.replaceProperties(line);

    // then
    assertEquals("provisioningUrl=\"http://localhost:8080\" provisioningVersion=\"1\">", result);
  }
}
