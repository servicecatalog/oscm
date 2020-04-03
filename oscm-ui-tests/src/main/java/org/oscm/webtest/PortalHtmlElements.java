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
  public static final String DEFINE_MARKETSERVICE_BUTTON_SAVE = "editForm:saveButton";

  public static final String DEFINE_PRICEMODEL_DROPDOWN_SERVICENAME =
      "serviceSelectForm:serviceSelectionInner";
  public static final String DEFINE_PRICEMODEL_BUTTON_SAVE = "editForm:saveButton";
  public static final String DEFINE_PRICEMODEL_CHECKBOX_FREE_OF_CHARGE = "editForm:isfree:0";
  public static final String DEFINE_PRICEMODEL_CHECKBOX_PRO_RATE = "editForm:isfree:1";
  public static final String DEFINE_PRICEMODEL_CHECKBOX_PER_UNITE = "editForm:isfree:2";

  public static final String DEFINE_PUBLISH_OPTION_DROPDOWN_SERVICENAME =
      "editForm:selectedServiceIdInner";
  public static final String DEFINE_PUBLISH_OPTION_DROPDOWN_MARKETPLACE =
      "editForm:marketplaceInner";
  public static final String DEFINE_PUBLISH_OPTION_BUTTON_SAVE = "editForm:saveButton";

  public static final String DEACTIVATION_SERVICE_FORM = "serviceDeActivationForm";
  public static final String DEACTIVATION_SERVICE_TABLE = "serviceDeActivationForm:j_idt491";
  public static final String DEACTIVATION_SERVICE_BUTTON_SAVE =
      "serviceDeActivationForm:deActivateButtonLink";

  public static final String REGISTER_CUSTOMER_INPUT_EMAIL = "editForm:email";
  public static final String REGISTER_CUSTOMER_INPUT_USERID = "editForm:userId";
  public static final String REGISTER_CUSTOMER_DROPDOWN_COUNTRY = "editForm:country";
  public static final String REGISTER_CUSTOMER_DROPDOWN_MARKETPLACE = "editForm:marketplace";
  public static final String REGISTER_CUSTOMER_BUTTONLINK_SAVE = "editForm:saveButtonLink";

  public static final String MARKETPLACE_PASSWORD_INPUT_CURRENT = "passwordForm:currentPassword";
  public static final String MARKETPLACE_PASSWORD_INPUT_CHANGE = "passwordForm:password";
  public static final String MARKETPLACE_PASSWORD_INPUT_REPEAT = "passwordForm:password2";
  public static final String MARKETPLACE_PASSWORD_BUTTONLINK_SAVE = "passwordForm:changeButtonLink";
  public static final String MARKETPLACE_SPAN_WELCOME = "formLogout:welcome";
  public static final String GOTO_MARKETPLACE_DROPDOWN_MARKETPLACE =
      "gotoMarketplaceForm:marketplaceInner";
  public static final String GOTO_MARKETPLACE_BUTTONLINK_GOTO = "goForm:gotoBtnLink";
  public static final String MARKETPLACE_INPUT_USERID = "loginForm:loginUserId";
  public static final String MARKETPLACE_INPUT_PASSWORD = "loginForm:loginPassword";
  public static final String MARKETPLACE_BUTTON_LOGIN = "loginForm:loginButtonLink";
  public static final String MARKETPLACE_LINK_LOGOUT = "formLogout:logout";
  public static final String MARKETPLACE_LINKTEXT_LOGIN = "Login";

  public static final String MARKETPLACE_LINK_SERVICE_NAME = "showDetails0";
  public static final String MARKETPLACE_SUBSCRIPTION_BUTTON_GETITNOW = "//a[@id='buyme']/span";
  public static final String MARKETPLACE_SUBSCRIPTION_INPUT_SUBNAME =
      "configurationForm:subscriptionIdText";
  public static final String MARKETPLACE_SUBSCRIPTION_INPUT_REFNUMBER =
      "configurationForm:purchaseOrderNumberText";
  public static final String MARKETPLACE_SUBSCRIPTION_BUTTONLINK_NEXT =
      "configurationForm:nextLink";
  public static final String MARKETPLACE_SUBSCRIPTION_CHECKBOX_LICENSEAGREE =
      "confirmForm:agreeCheckbox";
  public static final String MARKETPLACE_SUBSCRIPTION_BUTTONLINK_CONFIRM =
      "confirmForm:confirmLink";

  public static final String MARKETPLACE_ACCOUNT_BUTTON = "navigation3";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON =
      "//a[@id='editProfileButton']/span";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_TITLE_DROPDOWN = "userForm:salutation";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_FIRST_NAME_FIELD = "userForm:firstName";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_LAST_NAME_FIELD = "userForm:lastName";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_EMAIL_FIELD = "userForm:email";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_LANGUAGE_DROPDOWN = "userForm:locale";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_BUTTON =
      "//span[contains(.,'Organization Data')]";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_FIELD =
      "orgForm:organizationName";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_EMAIL_FIELD =
      "orgForm:organizationEmail";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_ADDRESS_FIELD =
      "orgForm:organizationAddress";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_COUNTRY_DROPDOWN = "orgForm:country";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_SAVE_BUTTON =
      "//a[@id='orgForm:saveOrgButtonLink']/span";

  public static final String MARKETPLACE_ACCOUNT_PAYMENT_BUTTON = "menu2";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_ADD_BILLING_ADDRESS_BUTTON =
      "//span[contains(.,'Add Billing Address')]";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_DISPLAY_FIELD =
      "billingContactPanelForm:displayName";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_NAME_FIELD =
      "billingContactPanelForm:companyName";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_EMAIL_FIELD =
      "billingContactPanelForm:email";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_ADDRESS_FIELD =
      "billingContactPanelForm:address";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_SAVE_BUTTON =
      "billingContactPanelForm:saveBillingContactButton";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_MANAGE_PAYMENT_TABLE =
      "paymentOptionListForm:paymentOptionListTable:0:j_idt372";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_PAYMENT_NAME_FIELD =
      "paymentOptionEditPanelForm:displayName";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_PAYMENT_SAVE_BUTTON =
      "paymentOptionEditPanelForm:hiddenSetDisplayName";

  public static final String MARKETPLACE_ACCOUNT_USERS_SHOW_BUTTON =
      "//a[@id='showUsersButton']/span";
  public static final String MARKETPLACE_ACCOUNT_USERS_ADD_USER_BUTTON =
      "//a[@id='userListForm:addUserButtonLink']/span";
  public static final String MARKETPLACE_ACCOUNT_USERS_USERID_FIELD = "userForm:userId";
  public static final String MARKETPLACE_ACCOUNT_USERS_CREATE_BUTTON = "userForm:j_idt246";
  public static final String MARKETPLACE_ACCOUNT_USERS_ADD_ROLE_CHECKBOX =
      "//input[@id='editUserForm:userRolesTable:0:rolesCheckbox']";
  public static final String MARKETPLACE_ACCOUNT_USERS_ROLE_ASSIGN_BUTTON = "editUserForm:j_idt340";

  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_UNIT_BUTTON =
      "//span[contains(.,'Organizational units')]";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_ADD_UNIT_BUTTON =
      "//span[contains(.,'Add Unit')]";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_ORG_NAME_FIELD = "groupForm:groupId";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_ORG_ID_FIELD =
      "groupForm:groupReferenceId";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_ORG_DESCRIPTION_FIELD =
      "groupForm:groupDescription";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_CREATE_BUTTON =
      "//a[@id='groupForm:createButtonLink']/span";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_ADD_ADMINISTRATOR_CHECKBOX =
      "editGroupForm:usersInGroup:administrator:assignCheckbox";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_ADD_USER_CHECKBOX =
      "editGroupForm:usersInGroup:isabelSmith:assignCheckbox";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_ASSIGN_ROLE_DROPDOWN =
      "editGroupForm:usersInGroup:administrator:unitRole";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_ASSIGN_ROLE_BUTTON =
      "//a[@id='editGroupForm:createButtonLink']/span";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_DELETE_INPUT =
      "//input[@id='groupsListForm:groupListTable:0:deleteBt']";
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_DELETE_CONFIRM_BUTTON =
      "//a[@id='deleteGroupForm:deleteConfirmPlOkButton']/span";

  public static final String MARKETPLACE_ACCOUNT_PROCESSES_SHOW_BUTTON =
      "//a[@id='showProcessesButton']/span";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_ADD_PROCESS_BUTTON =
      "//a[@id='triggerDefinitionListForm:addTriggerDefinition']/span";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_NAME_FIELD =
      "triggerDefinitionPanelForm:triggerName";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_TYPE_DROPDOWN =
      "triggerDefinitionPanelForm:triggerType";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TARGET_TYPE_DROPDOWN =
      "triggerDefinitionPanelForm:targetType";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TARGET_URL_FIELD =
      "triggerDefinitionPanelForm:targetURL";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TARGET_SAVE_BUTTON =
      "//a[@id='triggerDefinitionPanelForm:triggerDefinitionPanelSave']/span";
}
