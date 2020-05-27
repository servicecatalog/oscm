/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: Feb 8, 2017
 *
 * <p>*****************************************************************************
 */
package org.oscm.portal;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.support.ui.Select;
import org.oscm.webtest.PortalHtmlElements;
import org.oscm.webtest.PortalPathSegments;
import org.oscm.webtest.PortalTester;

import static org.junit.Assert.assertTrue;

/**
 * Integration web test to create a marketable service.
 *
 * @author miethaner
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PortalMarketServiceWT {

  private static final String TECHSERVICE_IAAS_USER_ID = "DummyUser";
  private static final String TECHSERVICE_IAAS_USER_PWD = "DummyPwd123";
  private static final String marketServiceName = "ms_" + PlaygroundSuiteTest.currentTimestampe;
  private static final String IMPORT_TECHSERV_NAME = "technicalservice.name";
  private static PortalTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    tester = new PortalTester();
    String userid = PlaygroundSuiteTest.supplierOrgAdminId;
    String userpassword = PlaygroundSuiteTest.supplierOrgAdminPwd;
    tester.loginPortal(userid, userpassword);
  }

  @AfterClass
  public static void cleanUp() {
    tester.logoutPortal();
    tester.close();
  }

  @Test
  public void test01createService() throws Exception {

    tester.visitPortal(PortalPathSegments.DEFINE_MARKETSERVICE);
    tester.waitForElement(By.id(PortalHtmlElements.DEFINE_MARKETSERVICE_DROPDOWN_SERVICENAME), 5);
    tester.selectDropdown(PortalHtmlElements.DEFINE_MARKETSERVICE_DROPDOWN_SERVICENAME, "10000");

    tester.waitForElementVisible(By.id(PortalHtmlElements.DEFINE_MARKETSERVICE_BUTTONLINK_SAVE), 5);
    tester.writeValue(PortalHtmlElements.DEFINE_MARKETSERVICE_INPUT_SERVICEID, marketServiceName);
    tester
        .getDriver()
        .findElement(By.id(PortalHtmlElements.DEFINE_MARKETSERVICE_INPUT_SERVICENAME))
        .clear();
    tester.writeValue(PortalHtmlElements.DEFINE_MARKETSERVICE_INPUT_SERVICENAME, marketServiceName);
    setDescriptionValue(
        PortalTester.TECHSERVICE_PARAM_EMAIL, PlaygroundSuiteTest.supplierOrgAdminMail);
    setDescriptionValue(PortalTester.TECHSERVICE_PARAM_MESSAGETEXT, "You are welcome!");
    setDescriptionValue(PortalTester.TECHSERVICE_PARAM_USER, TECHSERVICE_IAAS_USER_ID);
    setDescriptionValue(PortalTester.TECHSERVICE_PARAM_PWD, TECHSERVICE_IAAS_USER_PWD);
    checkCheckBox(PortalTester.TECHSERVICE_PARAM_EMAIL);
    checkCheckBox(PortalTester.TECHSERVICE_PARAM_MESSAGETEXT);
    checkCheckBox(PortalTester.TECHSERVICE_PARAM_USER);
    checkCheckBox(PortalTester.TECHSERVICE_PARAM_PWD);

    tester.waitForElementVisible(
        By.id(PortalHtmlElements.DEFINE_MARKETSERVICE_BUTTONLINK_SAVE), 10);
    tester.clickElement(PortalHtmlElements.DEFINE_MARKETSERVICE_BUTTONLINK_SAVE);
    tester.log("Params: marketServiceName:=" + marketServiceName + " ");
    assertTrue(tester.getExecutionResult());
    PlaygroundSuiteTest.marketServiceName = marketServiceName;
    PlaygroundSuiteTest.techServiceUserId = TECHSERVICE_IAAS_USER_ID;
    PlaygroundSuiteTest.techServiceUserPwd = TECHSERVICE_IAAS_USER_PWD;
  }

  @Test
  public void test02definePriceModel() throws Exception {

    tester.visitPortal(PortalPathSegments.DEFINE_PREICEMODEL);
    Select dropdownServiceName =
        new Select(
            tester
                .getDriver()
                .findElement(By.id(PortalHtmlElements.DEFINE_PRICEMODEL_DROPDOWN_SERVICENAME)));
    dropdownServiceName.selectByVisibleText(PlaygroundSuiteTest.marketServiceName);
    tester.waitForElementVisible(By.id(PortalHtmlElements.DEFINE_PRICEMODEL_BUTTON_SAVE), 10);
    if (!tester
        .getDriver()
        .findElement(By.id(PortalHtmlElements.DEFINE_PRICEMODEL_CHECKBOX_TIMEUNIT_CALC))
        .isSelected()) {
      tester.clickElement(PortalHtmlElements.DEFINE_PRICEMODEL_CHECKBOX_TIMEUNIT_CALC);
    }

    tester.writeValue(PortalHtmlElements.DEFINE_PRICEMODEL_RECURRING_PRICE_INPUT, "5.00");

    tester.waitForElementVisible(By.id(PortalHtmlElements.DEFINE_PRICEMODEL_BUTTON_SAVE), 10);
    tester.clickElement(PortalHtmlElements.DEFINE_PRICEMODEL_BUTTON_SAVE);
    tester.waitForElement(By.id(PortalHtmlElements.PORTAL_SPAN_INFOS), 10);

    assertTrue(tester.getExecutionResult());
  }

  @Test
  public void test03definePublishOption() throws Exception {

    tester.visitPortal(PortalPathSegments.DEFINE_PUBLISHOPTION);
    tester.waitForElement(By.id(PortalHtmlElements.DEFINE_PUBLISH_OPTION_DROPDOWN_SERVICENAME), 10);
    Select dropdownServiceName =
        new Select(
            tester
                .getDriver()
                .findElement(By.id(PortalHtmlElements.DEFINE_PUBLISH_OPTION_DROPDOWN_SERVICENAME)));
    dropdownServiceName.selectByVisibleText(PlaygroundSuiteTest.marketServiceName);

    tester.selectDropdown(
        PortalHtmlElements.DEFINE_PUBLISH_OPTION_DROPDOWN_MARKETPLACE,
        PlaygroundSuiteTest.marketPlaceId);

    tester.waitForElementVisible(By.id(PortalHtmlElements.DEFINE_PUBLISH_OPTION_BUTTON_SAVE), 10);
    tester.clickElement(PortalHtmlElements.DEFINE_PUBLISH_OPTION_BUTTON_SAVE);

    assertTrue(tester.getExecutionResult());
  }

  @Test
  public void test04activeService() throws Exception {

    tester.visitPortal(PortalPathSegments.ACTIVE_MARKETSERVICE);
    tester.waitForElement(By.id(PortalHtmlElements.DEACTIVATION_SERVICE_TABLE), 5);

    String serviceXpath = "//input[@id='input_serviceDeActivationForm:j_idt491:0:active']";
    if (tester.getDriver().findElements(By.xpath(serviceXpath)).size() != 0) {
      if (!tester.getDriver().findElement(By.xpath(serviceXpath)).isSelected()) {
        tester.getDriver().findElement(By.xpath(serviceXpath)).click();
      }
    } else {
      String serviceOIDCXpath = "//input[@id='input_serviceDeActivationForm:j_idt492:0:active']";
      if (!tester.getDriver().findElement(By.xpath(serviceOIDCXpath)).isSelected()) {
        tester.getDriver().findElement(By.xpath(serviceOIDCXpath)).click();
      }
    }
    tester.clickElement(PortalHtmlElements.DEACTIVATION_SERVICE_BUTTON_SAVE);
    tester.waitForElement(By.id(PortalHtmlElements.PORTAL_SPAN_INFOS), 10);

    assertTrue(tester.getExecutionResult());
  }

  @Test
  public void test05setPaymentType() throws Exception {
    tester.visitPortal(PortalPathSegments.MANAGE_PAYMENT);
    tester.waitForElement(By.id(PortalHtmlElements.MANAGE_PAYMENT_FORM), 5);

    tester.clickElement(PortalHtmlElements.MANAGE_PAYMENT_NEW_SERVICES);
    tester.clickElement(PortalHtmlElements.MANAGE_PAYMENT_EXISTING_SERVICES);
    tester.clickElement(PortalHtmlElements.MANAGE_PAYMENT_NEW_USERS);
    tester.clickElement(PortalHtmlElements.MANAGE_PAYMENT_EXISTING_USERS);
    tester.clickElement(PortalHtmlElements.MANAGE_PAYMENT_SAVE_BUTTON);

    assertTrue(tester.getExecutionResult());
  }

  @Test
  public void test06defineCustomAttributes() throws Exception {
    tester.visitPortal(PortalPathSegments.MANAGE_CUSTOM_ATTRIBUTES);
    tester.waitForElement(By.xpath(PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_ADD_BUTTON), 5);

    tester.clickElementXPath(PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_ADD_BUTTON);
    Thread.sleep(1000);
    tester.writeValue(PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_ATTRIBUTE_FIELD, "AttributeID");
    tester.selectDropdown(PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_LANGUAGE_SELECT, "en");
    tester.writeValue(PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_NAME_FIELD, "AttributeName");
    tester.writeValue(PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_VALUE_FIELD, "AttributeValue");
    tester.writeValue(
        PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_CONTROLLER_FIELD, "AttributeController");
    tester.clickElement(PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_USER_CHECKBOX);
    Thread.sleep(2000);
    tester.clickElement(PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_MANDATORY_CHECKBOX);
    tester.clickElement(PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_ENCRYPTED_CHECKBOX);
    tester.clickElement(PortalHtmlElements.MANAGE_ATTRIBUTES_CUSTOMER_SAVE_BUTTON);
    Thread.sleep(2000);

    assertTrue(
        tester
            .readInfoMessage()
            .contains("The custom attribute definitions have been successfully saved"));
  }

  private void setDescriptionValue(String description, String value) {
    String descriptionXpath =
        "//table[@id='"
            + PortalHtmlElements.DEFINE_MARKETSERVICE_PARAM_TABLE
            + "']//span[.= '"
            + description
            + "']/../../../td[3]/div/input";
    tester.getDriver().findElement(By.xpath(descriptionXpath)).clear();
    tester.getDriver().findElement(By.xpath(descriptionXpath)).sendKeys(value);
  }

  private void checkCheckBox(String label) {
    if (!tester
        .getDriver()
        .findElement(By.xpath("//*[span='" + label + "']/../../td[2]//input"))
        .isSelected())
      try {
        tester
            .getDriver()
            .findElement(By.xpath("//*[span='" + label + "']/../../td[2]//input"))
            .click();
      } catch (ElementClickInterceptedException e) {
        tester
            .getDriver()
            .findElement(By.xpath("//*[span='" + label + "']/../../td[2]//input"))
            .click();
      }
  }
}
