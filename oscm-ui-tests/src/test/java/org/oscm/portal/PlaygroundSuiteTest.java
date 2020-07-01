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
import org.oscm.marketplace.*;
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
  PortalTechServiceWT.class,
  AppAzureControllerWT.class,
  AppConfigurationWT.class,
  AppAWSControllerWT.class,
  AppOpenstackControllerWT.class,
  AppVCenterControllerWT.class,
  PortalMarketServiceWT.class,
  MarketplaceSubscriptionWT.class,
  MarketplaceWT.class,
  MarketplaceAccountPersonalWT.class,
  MarketplaceAccountPaymentWT.class,
  MarketplaceSubscriptionDataWT.class,
  MarketplaceSubscriptionTableWT.class,
  MarketplaceServiceDetailsWT.class,
})
public class PlaygroundSuiteTest {

  public static String supplierOrgId = "959c9bf7";
  public static String supplierOrgName = "supplierorg";
  public static String supplierOrgAdminId = "supplier@adfs.com";
  public static String supplierOrgAdminPwd = "qwerty12";
  public static String supplierOrgAdminMail = "";
  public static String supplierOrgAdminUserkey = "";
  public static String marketPlaceName = "";
  public static String marketPlaceId = "8cfccf38";

  public static String controllerId = "";
  public static String techServiceName = "";
  public static String marketServiceName = "";
  public static String techServiceUserId = "";
  public static String techServiceUserPwd = "";
  public static String currentTimestampe = WebTester.getCurrentTime();
}
