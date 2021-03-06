/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 20 6, 2018
 *
 * <p>*****************************************************************************
 */
package org.oscm.webtest;

public class PortalHtmlElements {

  public static final String PORTAL_TITLE = "Service Catalog Manager";
  public static final String PORTAL_INPUT_USERID = "userId";
  public static final String PORTAL_INPUT_PASSWORD = "password";
  public static final String PORTAL_BUTTON_LOGIN = "loginButton";
  public static final String PORTAL_LINK_LOGOUT = "formLogout:logout";
  public static final String PORTAL_DIV_LOGIN_FAILED = "login_warning";

  public static final String PORTAL_SPAN_ERRORS = "errorMessages:";
  public static final String PORTAL_ERRORCLASS = "rf-msgs-sum";
  public static final String PORTAL_SPAN_INFOS = "infoMessages:";
  public static final String PORTAL_INFOCLASS = "rf-msgs-sum";

  public static final String PORTAL_DIV_SHOWMESSAGE = "mainTmplMessagesPanel";
  public static final String MARKETPLACE_SPAN_SHOWMESSAGE = "globalMessagesHolder";

  public static final String PORTAL_PASSWORD_INPUT_CURRENT = "passwordform:currentPassword";
  public static final String PORTAL_PASSWORD_INPUT_CHANGE = "passwordform:password";
  public static final String PORTAL_PASSWORD_INPUT_REPEAT = "passwordform:password2";
  public static final String PORTAL_PASSWORD_BUTTON_SAVE = "passwordform:changeButtonLink";

  public static final String CREATE_MARKETPLACE_INPUT_NAME =
      "createMarketplaceForm:marketplaceName";
  public static final String CREATE_MARKETPLACE_INPUT_ID = "createMarketplaceForm:marketplaceId";
  public static final String CREATE_MARKETPLACE_INPUT_ORG_ID =
      "createMarketplaceForm:organizationIdInner";
  public static final String CREATE_MARKETPLACE__BUTTON_SAVE =
      "createMarketplaceForm:saveButtonLink";

  public static final String DELETE_MARKETPLACE_DROPDOWN_IDLIST =
      "marketplaceSelectForm:selectMarketplaceIdInner";
  public static final String DELETE_MARKETPLACE_BUTTON_DELETE =
      "deleteMarketplaceForm:deleteButton";
  public static final String DELETE_MARKETPLACE_BUTTON_CONFIRM = "confirmForm:okButton";

  public static final String CREATE_ORGANIZATION_INPUT_ADMINEMAIL = "editForm:administratorEmail";
  public static final String CREATE_ORGANIZATION_INPUT_DESIRED_USERID =
      "editForm:administratorUserId";
  public static final String CREATE_ORGANIZATION_DROPDOWN_LANGUAGE = "editForm:administratorLocale";

  public static final String CREATE_ORGANIZATION_CHECKBOX_TPROVIDER =
      "editForm:checkboxRoleTechnologyProvider";
  public static final String CREATE_ORGANIZATION_FORM_UPLOADIMAGE = "editForm:image";
  public static final String CREATE_ORGANIZATION_CHECKBOX_SUPPLIER =
      "editForm:checkboxRoleSupplier";
  public static final String CREATE_ORGANIZATION_INPUT_REVENUESHARE =
      "editForm:operatorRevenueShare";

  public static final String CREATE_ORGANIZATION_INPUT_ORGNAME = "editForm:organizationName";
  public static final String CREATE_ORGANIZATION_INPUT_ORGEMAIL = "editForm:organizationEmail";
  public static final String CREATE_ORGANIZATION_DROPDOWN_ORGLOCALE = "editForm:organizationLocale";
  public static final String CREATE_ORGANIZATION_INPUT_ORGPHONE = "editForm:organizationPhone";
  public static final String CREATE_ORGANIZATION_INPUT_ORGURL = "editForm:organizationUrl";
  public static final String CREATE_ORGANIZATION_INPUT_ORGADDRESS = "editForm:organizationAddress";
  public static final String CREATE_ORGANIZATION_DROPDOWN_ORGCOUNTRY =
      "editForm:organizationCountry";
  public static final String CREATE_ORGANIZATION_BUTTON_SAVE = "editForm:saveButtonLink";

  public static final String IMPORT_TECHSERVICE_UPLOAD_INPUT = "importForm:file";
  public static final String IMPORT_TECHSERVICE_UPLOAD_BUTTON = "importForm:importButtonLink";

  public static final String UPDATE_TECHSERVICE_PARAM_TABLE = "editForm:parameterTable";
  public static final String UPDATE_TECHSERVICE_DROPDOWN_SERVICENAME =
      "techServiceForm:techServiceIdInner";
  public static final String UPDATE_TECHSERVICE_BUTTONLINK_SAVE = "editForm:saveButtonLink";
  public static final String UPDATE_TECHSERVICE_TAG_CLOUD_INPUT = "editForm:tagListInput";

  public static final String DEFINE_MARKETSERVICE_DROPDOWN_SERVICENAME =
      "editForm:techServiceIdInner";
  public static final String DEFINE_MARKETSERVICE_INPUT_SERVICENAME = "editForm:serviceName";
  public static final String DEFINE_MARKETSERVICE_INPUT_SERVICEID = "editForm:serviceId";
  public static final String DEFINE_MARKETSERVICE_PARAM_TABLE = "editForm:parameterTable";
  public static final String DEFINE_MARKETSERVICE_BUTTONLINK_SAVE = "editForm:saveButtonLink";

  public static final String DEFINE_PRICEMODEL_DROPDOWN_SERVICENAME =
      "serviceSelectForm:serviceSelectionInner";
  public static final String DEFINE_PRICEMODEL_BUTTON_SAVE = "editForm:saveButton";
  public static final String DEFINE_PRICEMODEL_CHECKBOX_FREE_OF_CHARGE = "editForm:isfree:0";
  public static final String DEFINE_PRICEMODEL_CHECKBOX_TIMEUNIT_CALC = "editForm:isfree:2";
  public static final String DEFINE_PRICEMODEL_RECURRING_PRICE_INPUT = "editForm:recChargePerSubs";

  public static final String DEFINE_PUBLISH_OPTION_DROPDOWN_SERVICENAME =
      "editForm:selectedServiceIdInner";
  public static final String DEFINE_PUBLISH_OPTION_DROPDOWN_MARKETPLACE =
      "editForm:marketplaceInner";
  public static final String DEFINE_PUBLISH_OPTION_BUTTON_SAVE = "editForm:saveButton";

  public static final String DEACTIVATION_SERVICE_TABLE = "serviceDeActivationForm:j_idt491";
  public static final String DEACTIVATION_SERVICE_BUTTON_SAVE =
      "serviceDeActivationForm:deActivateButtonLink";

  public static final String MANAGE_CATEGORIES_DROPDOWN =
      "marketplaceSelectForm:selectMarketplaceIdInner";
  public static final String MANAGE_CATEGORIES_ADD_BUTTON = "categoriesForm:addNewCategoryLink";
  public static final String MANAGE_CATEGORIES_CATEGORY_ID_INPUT =
      "categoriesForm:categoriesTable:0:categoryId";
  public static final String MANAGE_CATEGORIES_CATEGORY_NAME_INPUT =
      "categoriesForm:categoriesTable:0:localizedValue";
  public static final String MANAGE_CATEGORIES_SAVE_BUTTON = "categoriesForm:saveButton";

  public static final String MANAGE_PAYMENT_FORM = "managePayment";
  public static final String MANAGE_PAYMENT_NEW_SERVICES = "managePayment:serviceDefault0";
  public static final String MANAGE_PAYMENT_EXISTING_SERVICES =
      "managePayment:serviceTable:0:service0";
  public static final String MANAGE_PAYMENT_NEW_USERS = "managePayment:default0";
  public static final String MANAGE_PAYMENT_EXISTING_USERS = "managePayment:customerTable:0:cust0";
  public static final String MANAGE_PAYMENT_SAVE_BUTTON =
      "managePayment:modifyPaymentEnablementButtonLink";

  public static final String MANAGE_CURRENCY_INPUT = "currencies:currencyToManage";
  public static final String MANAGE_CURRENCY_ADD_BUTTON = "currencies:addButtonLink";

  public static final String MANAGE_ATTRIBUTES_CUSTOMER_ADD_BUTTON =
      "//a[@id='udaDefinitionsCustomerForm:createDefinition']/span";
  public static final String MANAGE_ATTRIBUTES_CUSTOMER_ATTRIBUTE_FIELD =
      "createForm:createUdaIdInput";
  public static final String MANAGE_ATTRIBUTES_CUSTOMER_LANGUAGE_SELECT = "createForm:language";
  public static final String MANAGE_ATTRIBUTES_CUSTOMER_NAME_FIELD =
      "createForm:createUdaNameInput";
  public static final String MANAGE_ATTRIBUTES_CUSTOMER_VALUE_FIELD =
      "createForm:createUdaDefaultValueInput";
  public static final String MANAGE_ATTRIBUTES_CUSTOMER_CONTROLLER_FIELD =
      "createForm:createUdaControllerIdInput";
  public static final String MANAGE_ATTRIBUTES_CUSTOMER_USER_CHECKBOX =
      "createForm:createUdaUserOptionCheckBox";
  public static final String MANAGE_ATTRIBUTES_CUSTOMER_MANDATORY_CHECKBOX =
      "createForm:createUdaMandatoryCheckBox";
  public static final String MANAGE_ATTRIBUTES_CUSTOMER_ENCRYPTED_CHECKBOX =
      "createForm:createUdaEncryptedCheckBox";
  public static final String MANAGE_ATTRIBUTES_CUSTOMER_SAVE_BUTTON =
      "createForm:createPanelSaveButton";
}
