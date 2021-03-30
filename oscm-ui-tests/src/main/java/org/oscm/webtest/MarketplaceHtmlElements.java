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
  public static final String MARKETPLACE_NAVBAR_LOGIN_LINK_TEXT = "Login";
  public static final String MARKETPLACE_NAVBAR_LOGOUT_LINK = "formLogout:logout";
  public static final String MARKETPLACE_NAVBAR_BROWSE_SERVICES_LINK = "servicesLink";
  public static final String MARKETPLACE_NAVBAR_TOGGLE_BUTTON = "navbarToggleButton";
  public static final String MARKETPLACE_NAVBAR_USER_TOGGLE_BUTTON = "navbarUserToggle";
  public static final String MARKETPLACE_NAVBAR_ACCOUNT_LINK = "//*[@id=\"userDrop\"]/a[1]";

  public static final String GOTO_MARKETPLACE_DROPDOWN_MARKETPLACE =
      "gotoMarketplaceForm:marketplaceInner";
  public static final String GOTO_MARKETPLACE_BUTTONLINK_GOTO = "goForm:gotoBtnLink";
  public static final String MARKETPLACE_INPUT_USERID = "loginForm:loginUserId";
  public static final String MARKETPLACE_INPUT_PASSWORD = "loginForm:loginPassword";
  public static final String MARKETPLACE_BUTTON_LOGIN = "loginButtonLink";

  public static final String MARKETPLACE_SUBSCRIPTION_BUTTON_GETITNOW = "buyme";
  public static final String MARKETPLACE_SUBSCRIPTION_INPUT_SUBNAME =
      "configurationForm:subscriptionIdText";
  public static final String MARKETPLACE_SUBSCRIPTION_INPUT_REFNUMBER =
      "configurationForm:purchaseOrderNumberText";
  public static final String MARKETPLACE_SUBSCRIPTION_BUTTON_NEXT = "nextLink";
  public static final String MARKETPLACE_SUBSCRIPTION_CHECKBOX_LICENSEAGREE =
      "confirmForm:agreeCheckbox";
  public static final String MARKETPLACE_SUBSCRIPTION_BUTTON_CONFIRM = "confirmLink";

  public static final String MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON_ID = "editProfileButton";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_PERSONAL_NAV = "tabUser";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_TITLE_DROPDOWN = "userForm:salutation";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_FIRST_NAME_FIELD = "userForm:firstName";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_LAST_NAME_FIELD = "userForm:lastName";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_EMAIL_FIELD = "userForm:email";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_LANGUAGE_DROPDOWN = "userForm:locale";

  public static final String MARKETPLACE_ACCOUNT_PROFILE_PRIMARY_COLOR_INPUT =
      "userForm:primaryColorInput";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_FONT_COLOR_INPUT =
      "userForm:fontColorInput";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_NAVBAR_COLOR_INPUT =
      "userForm:navbarColorInput";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_NAVBAR_LINK_COLOR_INPUT =
      "userForm:navbarLinkColorInput";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_INPUT_COLOR_INPUT = "userForm:inputColorInput";

  public static final String MARKETPLACE_ACCOUNT_PROFILE_USER_SAVE_BUTTON =
      "userForm:saveUserButtonLink";

  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_NAV = "tabOrg";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_FIELD =
      "orgForm:organizationName";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_EMAIL_FIELD =
      "orgForm:organizationEmail";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_ADDRESS_FIELD =
      "orgForm:organizationAddress";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_COUNTRY_DROPDOWN = "orgForm:country";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_SAVE_BUTTON_ID =
      "orgForm:saveOrgButtonLink";

  public static final String MARKETPLACE_ACCOUNT_PROFILE_ATTRIBUTE_NAV = "tabAttr";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ATTRIBUTE_VALUE_FIELD =
      "attrForm:attributesTable:0:udaValuePassword";
  public static final String MARKETPLACE_ACCOUNT_PROFILE_ATTRIBUTE_SAVE_BUTTON =
      "attrForm:saveAttrButtonLink";

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

  public static final String MARKETPLACE_LANDING_PAGE_CATEGORY_LINK =
      "categorySelectionForm:categoryLink0";

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

  public static final String MARKETPLACE_ACCOUNT_PROCESSES_NAV_LINK = "link7";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_ADD_TRIGGER =
      "triggerDefinitionListForm:addTriggerDefinition";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_NAME =
      "triggerDefinitionPanelForm:triggerName";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_TYPE =
      "triggerDefinitionPanelForm:triggerType";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_TARGET =
      "triggerDefinitionPanelForm:targetType";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_URL =
      "triggerDefinitionPanelForm:targetURL";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_SUSPEND =
      "triggerDefinitionPanelForm:suspend";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_SAVE =
      "triggerDefinitionPanelSave";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_EDIT = "editProcessTriggerImage";
  public static final String MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_DELETE =
      "triggerDefinitionPanelDelete";
}
