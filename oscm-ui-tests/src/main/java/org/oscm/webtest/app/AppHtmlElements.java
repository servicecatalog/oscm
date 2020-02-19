/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 20 6, 2018
 *
 * <p>*****************************************************************************
 */
package org.oscm.webtest.app;

public class AppHtmlElements {
  public static final String APP_CONFIG_DIV_CLASS_STATUS_MSG = "statusPanel";
  public static final String APP_CONFIG_LICLASS_STATUS_MSG_OK = "statusGreen";
  public static final String APP_CONFIG_LICLASS_STATUS_MSG_ERROR = "//span[2]/span";

  public static final String APP_CONFIG_LICLASS_STATUS_MSG_AT_CONTROLLER = "//body/span[2]";
  public static final String APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER_FIRST = "//body/span";
  public static final String APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER_SECOND =
      "//span[@id='status']";

  public static final String TEST_CONTROLLER_ID = "a.ess.sample";

  public static final String APP_CONFIG_FORM1 = "configurationSettings";
  public static final String APP_CONFIG_FORM1_INPUT_END_NEWCONTROLLERID = ":newControllerId";
  public static final String APP_CONFIG_FORM1_INPUT_END_NEWORGID = ":newOrgnizationId";
  public static final String APP_CONFIG_FORM_BUTTON_CLASS = "oscm_app_button";

  public static final String APP_CONTROLLER_TABLE_FIELD = "configurationSettings:j_idt52:0:j_idt53";

  public static final String APP_CONFIG_FORM2 = "appSettings";

  public static final String APP_ACCORDION_AREA = "accordion_area1";
  public static final String APP_SERVICEINSTANCE_TABLE_ID = "serviceForm:serviceTable";

  public static final String APP_SAMPLECONTROLLER_FORM_ID = "configurationSettings";
  public static final String APP_VSPHERE_API_SETTINGS = "balancer_form";
}
