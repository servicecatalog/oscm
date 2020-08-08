/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 02-04-2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.webtest;

public class MarketplaceHtmlElements {
  public static final String MARKETPLACE_NAVBAR_HOME_LINK = "homeLink";
  public static final String MARKETPLACE_NAVBAR_LOGIN_LINK = "//*[@id=\"formLogin:loginLink\"]";
  public static final String MARKETPLACE_NAVBAR_LOGOUT_LINK = "formLogout:logout";
  public static final String MARKETPLACE_NAVBAR_BROWSE_SERVICES_LINK = "servicesLink";
  public static final String MARKETPLACE_NAVBAR_TOGGLE_BUTTON = "//*[@id=\"navbarToggleButton\"]";
  public static final String MARKETPLACE_NAVBAR_USER_TOGGLE_BUTTON = "navbarUserToggle";

  public static final String MARKETPLACE_HOME_LINKTEXT = "//a[contains(text(),'Home')]";
  public static final String GOTO_MARKETPLACE_DROPDOWN_MARKETPLACE =
      "gotoMarketplaceForm:marketplaceInner";
  public static final String GOTO_MARKETPLACE_BUTTONLINK_GOTO = "goForm:gotoBtnLink";
  public static final String MARKETPLACE_INPUT_USERID = "loginForm:loginUserId";
  public static final String MARKETPLACE_INPUT_PASSWORD = "loginForm:loginPassword";
  public static final String MARKETPLACE_BUTTON_LOGIN = "loginButtonLink";

  public static final String MARKETPLACE_LINK_SERVICE_NAME = "showDetails0";
  public static final String MARKETPLACE_SUBSCRIPTION_BUTTON_GETITNOW = "buyme";
  public static final String MARKETPLACE_SUBSCRIPTION_INPUT_SUBNAME =
      "configurationForm:subscriptionIdText";
  public static final String MARKETPLACE_SUBSCRIPTION_INPUT_REFNUMBER =
      "configurationForm:purchaseOrderNumberText";
  public static final String MARKETPLACE_SUBSCRIPTION_BUTTONLINK_NEXT =
      "configurationForm:nextLink";
  public static final String MARKETPLACE_SUBSCRIPTION_PAYMENT_BUTTONLINK_NEXT = "payment:nextLink";
  public static final String MARKETPLACE_SUBSCRIPTION_BUTTON_NEXT = "nextLink";
  public static final String MARKETPLACE_SUBSCRIPTION_CHECKBOX_LICENSEAGREE =
      "confirmForm:agreeCheckbox";
  public static final String MARKETPLACE_SUBSCRIPTION_BUTTONLINK_CONFIRM =
      "confirmForm:confirmLink";
  public static final String MARKETPLACE_SUBSCRIPTION_BUTTON_CONFIRM = "confirmLink";

  public static final String MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON =
      "//a[@id='editProfileButton']/span";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON_ID = "editProfileButton";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_PERSONAL_BUTTON =
      "//span[contains(.,'Personal Data')]";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_PERSONAL_NAV = "tabUser";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_TITLE_DROPDOWN = "userForm:salutation";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_FIRST_NAME_FIELD = "userForm:firstName";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_LAST_NAME_FIELD = "userForm:lastName";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_EMAIL_FIELD = "userForm:email";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_LANGUAGE_DROPDOWN = "userForm:locale";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_USER_SAVE_BUTTON =
      "userForm:saveUserButtonLink";

  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_BUTTON =
      "//span[contains(.,'Organization Data')]";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_NAV = "tabOrg";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_FIELD =
      "orgForm:organizationName";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_EMAIL_FIELD =
      "orgForm:organizationEmail";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_ADDRESS_FIELD =
      "orgForm:organizationAddress";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_COUNTRY_DROPDOWN = "orgForm:country";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_SAVE_BUTTON =
      "//a[@id='orgForm:saveOrgButtonLink']/span";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_SAVE_BUTTON_ID =
      "orgForm:saveOrgButtonLink";

  public static final String MARKETPLACE_ACCOUNT_PROFILE_ATTRIBUTE_NAV = "tabAttr";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ATTRIBUTE_VALUE_FIELD =
      "attrForm:attributesTable:0:udaValuePassword";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ATTRIBUTE_SAVE_BUTTON =
      "attrForm:saveAttrButtonLink";

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
      "paymentOptionListForm:paymentOptionListTable:0:displayCell";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_PAYMENT_NAME_FIELD =
      "paymentOptionEditPanelForm:displayName";
  public static final String MARKETPLACE_ACCOUNT_PAYMENT_PAYMENT_SAVE_BUTTON =
      "paymentOptionEditPanelForm:hiddenSetDisplayName";

  public static final String MARKETPLACE_ACCOUNT_USERS_SHOW_BUTTON =
      "//a[@id='showUsersButton']/span";
  public static final String MARKETPLACE_ACCOUNT_USERS_ADD_USER_BUTTON =
      "//a[@id='userListForm:addUserButtonLink']/span";
  public static final String MARKETPLACE_ACCOUNT_USERS_USERID_FIELD = "userForm:userId";
  public static final String MARKETPLACE_ACCOUNT_USERS_CREATE_BUTTON = "userForm:createButtonLink";
  public static final String MARKETPLACE_ACCOUNT_USERS_ADD_ROLE_CHECKBOX =
      "//input[@id='editUserForm:userRolesTable:0:rolesCheckbox']";
  public static final String MARKETPLACE_ACCOUNT_USERS_ROLE_ASSIGN_BUTTON =
      "editUserForm:saveButtonLink";

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
  public static final String MARKETPLACE_ACCOUNT_ORGANIZATION_ADD_USER_CHECKBOX =
      "editGroupForm:usersInGroup:isabelSmith:assignCheckbox";
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

  public static final String MARKETPLACE_LANDING_PAGE_CATEGORY_LINK =
      "categorySelectionForm:categoryLink0";

  public static final String MARKETPLACE_HEADER_HAMBURGER_MENU_BUTTON = "navbarToggleButton";
  public static final String MARKETPLACE_HEADER_SEARCH_SERVICE_FIELD = "Search";
  public static final String MARKETPLACE_HEADER_SEARCH_SERVICE_BUTTON = "searchForm:submit";

  public static final String MARKETPLACE_HEADER_HOME_LINK = "navigation1";
  public static final String MARKETPLACE_HEADER_HELP_LINK = "helpButton";
  public static final String MARKETPLACE_HEADER_USER_FUNCTION_LIST = "logoutDrop";
  public static final String MARKETPLACE_HEADER_USER_SUBSCRIPTIONS_LINK = "navigation2";
  public static final String MARKETPLACE_HEADER_USER_ACCOUNT_LINK = "navigation3";
  public static final String MARKETPLACE_HEADER_USER_LOGOUT_LINK = "formLogout:logout";
  public static final String MARKETPLACE_HEADER_USER_LOGIN_LINK = "//a[contains(text(),'Login')]";
  public static final String MARKETPLACE_FOOTER_IMPRINT_LINK = "imprintLink";
  public static final String MARKETPLACE_FOOTER_PRIVACY_LINK = "privacyPolicyLink";
  public static final String MARKETPLACE_FOOTER_TERMS_LINK =
      "//a[contains(text(),'Terms and Conditions')]";

  public static final String MARKETPLACE_BROWSE_ALL_SERVICES_BUTTON =
      "categorySelectionForm:browseAllBtn";
  public static final String MARKETPLACE_SPAN_WELCOME = "navbarToggleButton";
  public static final String MARKETPLACE_LINK_LOGOUT = "formLogout:logout";
  public static final String MARKETPLACE_LINKTEXT_LOGIN = "Login";

  public static final String MARKETPLACE_SERVICE_DETAILS_WRITE_REVIEW_BUTTON = "btnWriteFeedback";
  public static final String MARKETPLACE_SERVICE_DETAILS_DEACTIVATE_SERVICE_LINK =
      "deactivateServiceForm:deactivateService";
  public static final String MARKETPLACE_SERVICE_DETAILS_DEACTIVATE_SERVICE_REASON_INPUT =
      "deactivateServicePanelForm:reasonTextArea";
  public static final String MARKETPLACE_SERVICE_DETAILS_DEACTIVATE_SERVICE_REASON_BUTTON =
      "deactivateServicePanelForm:deactivateServicePanelDeactivate";
  public static final String MARKETPLACE_SERVICE_DETAILS_REACTIVATE_SERVICE_REASON_BUTTON =
      "reactivateServicePanelForm:reactivateServicePanelReactivate";
  public static final String MARKETPLACE_SERVICE_DETAILS_REACTIVATE_SERVICE_LINK =
      "reactivateSelectForm:reactivateService";
  public static final String MARKETPLACE_SERVICE_DETAILS_SERVICE_ACTIVATION_INDICATOR =
      "serviceStatusColor";

  public static final String MARKETPLACE_SHOW_SERVICE_DETAILS_BUTTON = "showServiceDetails0";

  public static final String MARKETPLACE_REVIEW_TITLE_INPUT = "commentForm:title";
  public static final String MARKETPLACE_REVIEW_COMMENT_INPUT = "commentForm:comment";
  public static final String MARKETPLACE_REVIEW_SAVE_BUTTON = "btnPublishLink";
  public static final String MARKETPLACE_REVIEW_REMOVE_BUTTON = "commentForm:btnRemoveLink";
  public static final String MARKETPLACE_REVIEW_STARS_5 = "star5";
  public static final String MARKETPLACE_REVIEW_BLOCK = "reviewBlock1";

  public static final String MARKETPLACE_SUBSCRIPTION_PAYMENT_DROPDOWN =
      "payment:selectPaymentInfo";
  public static final String MARKETPLACE_SUBSCRIPTION_BILLING_DROPDOWN =
      "payment:payment:selectBillingContact";
  public static final String MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_LINK =
      "payment:newBillingContact";
  public static final String MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_DISPLAY_NAME =
      "billingContactPanelForm:displayName";
  public static final String MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_ORG_NAME =
      "billingContactPanelForm:companyName";
  public static final String MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_EMAIL =
      "billingContactPanelForm:email";
  public static final String MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_ADDRESS =
      "billingContactPanelForm:address";
  public static final String MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_SAVE =
      "billingContactPanelForm:saveBillingContactButton";

  public static final String MARKETPLACE_ACCOUNT_SHOW_SUBSCRIPTIONS_BUTTON =
      "showSubscriptionsButton";
  public static final String MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_FILTER_FIELD =
      "//input[@id='subListForm:subscriptionsList:subscriptionId']";
  public static final String MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_FIRST_ROW =
      "//tr[@id='subListForm:subscriptionsList:0']/td[2]";
  public static final String MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_SECOND_PAGE =
      "//a[@id='subListForm:subscriptionsList:subListPager_ds_2']";
  public static final String MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_SEARCH_FIELD =
      "//input[@id='subListForm:searchField']";
  public static final String MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_SEARCH_BUTTON =
      "//a[@id='subListForm:submitLink']/i";
  public static final String MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_EXPAND = "//span/span/img";
  public static final String MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_MANAGE_SUBSCRIPTIONS =
      "//button[contains(.,'Manage subscription')]";
  public static final String MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_SERVICE_DETAILS =
      "//h2[@id='serviceDetailName']";

  public static final String MARKETPLACE_ACCOUNT_SHOW_PAYMENTS_BUTTON = "showPaymentsButton";
  public static final String MARKETPLACE_ACCOUNT_PAYMENTS_ADD_BILLING_ADDRESS =
      "billingContactTableForm:addBillingContact";
  public static final String MARKETPLACE_ACCOUNT_PAYMENTS_EDIT_BILLING_ADDRESS =
      "editBillingImage0";
  public static final String MARKETPLACE_ACCOUNT_PAYMENTS_DELETE_BILLING_ADDRESS =
      "billingContactEditPanelForm:deleteButton";
  public static final String MARKETPLACE_ACCOUNT_PAYMENTS_DELETE_BILLING_CONFIRM =
      "confirmPanelOkButton";
  public static final String MARKETPLACE_ACCOUNT_PAYMENTS_EDIT_PAYMENT = "editPaymentImage0";
  public static final String MARKETPLACE_ACCOUNT_PAYMENTS_BILLING_MODAL_ERROR =
      "billingContactPanelmodalErrorPanel";
  public static final String MARKETPLACE_ACCOUNT_PAYMENTS_ADD_PAYMENT_TYPE =
      "paymentOptionTableForm:addPaymentOption";
  public static final String MARKETPLACE_ACCOUNT_PAYMENTS_ADD_PAYMENT_TYPE_NEXT =
      "paymentOptionPanelForm:switchToDetailsAjax";
  public static final String MARKETPLACE_ACCOUNT_PAYMENTS_PAYMENT_MODAL_ERROR =
      "paymentOptionPanelmodalErrorPanel";
}
