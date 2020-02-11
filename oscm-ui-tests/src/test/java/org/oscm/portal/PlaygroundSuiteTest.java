/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: Feb 7, 2017
 *
 * <p>*****************************************************************************
 */
package org.oscm.portal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.oscm.app.*;
import org.oscm.webtest.WebTester;

/**
 * Test suite for integration web tests for the OSCM portal
 *
 * @author miethaner
 */
@RunWith(Suite.class)
@SuiteClasses({
  PortalOrganizationWT.class,
  PortalMarketplaceWT.class,
  AppConfigurationWT.class,
  AppControllerAWS.class,
  AppControllerAzure.class,
  AppControllerOpenstack.class,
  AppControllerVCenter.class,
  PortalTechServiceWT.class,
  PortalMarketServiceWT.class,
  MarketplaceSubscriptionWT.class
})
public class PlaygroundSuiteTest {

  public static String supplierOrgId = "PLATFORM_OPERATOR";
  public static String supplierOrgName = "";
  public static String supplierOrgAdminId = "";
  public static String supplierOrgAdminPwd = "";
  public static String supplierOrgAdminMail = "";
  public static String supplierOrgAdminUserkey = "";
  public static String marketPlaceId = "";

  public static String controllerId = "";
  public static String techServiceName = "";
  public static String marketServiceName = "";
  public static String techServiceUserId = "";
  public static String techServiceUserPwd = "";
  public static String currentTimestampe = WebTester.getCurrentTime();
}
