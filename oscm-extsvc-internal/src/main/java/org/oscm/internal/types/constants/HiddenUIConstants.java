/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                                      
 *                                                                              
 *  Creation Date: 25.10.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.types.constants;

/**
 * Constants for configurable UI.
 * 
 * @author khomich
 * 
 */
public interface HiddenUIConstants {

    String MENU_GROUP_NAVIGATION_OPERATOR = "navigation.operator";
    String MENU_ITEM_OPERATOR_CREATE_ORGANIZATION = "operator.createOrganization";
    String MENU_ITEM_OPERATOR_CREATE_PSP = "operator.createPSP";
    String MENU_ITEM_ORGANIZATION_MANAGE_TRIGGERS = "organization.manageTriggers";
    String MENU_ITEM_OPERATOR_MANAGE_TIMERS = "operator.manageTimers";
    String MENU_ITEM_OPERATOR_MANAGE_USERS = "operator.manageUsers";
    String MENU_ITEM_OPERATOR_MANAGE_TENANTS = "operator.manageTenants";
    String MENU_ITEM_OPERATOR_MANAGE_CURRENCIES = "operator.manageCurrencies";
    String MENU_ITEM_OPERATOR_MANAGE_LDAP = "operator.manageLdapSettings";
    String MENU_ITEM_OPERATOR_MANAGE_LANGUAGES = "operator.manageLanguages";
    String MENU_ITEM_OPERATOR_MANAGE_CONFIGURATION = "operator.manageConfiguration";
    String MENU_ITEM_OPERATOR_MANAGE_ORGANIZATIONS = "operator.manageOrganizations";
    String MENU_ITEM_OPERATOR_MANAGE_OPERATOR_REVENUE_SHARE = "operator.manageOperatorRevenueShare";
    String MENU_ITEM_OPERATOR_MANAGE_PSPS = "operator.managePSPs";
    String MENU_ITEM_OPERATOR_MANAGE_BILLING_ADAPTERS = "operator.manageBillingAdapters";
    String MENU_ITEM_OPERATOR_EXPORT_BILLING_DATA = "operator.exportBillingData";
    String MENU_ITEM_OPERATOR_EXECUTE_BILLING_TASKS = "operator.executeBillingTasks";
    String MENU_ITEM_OPERATOR_EXPORT_AUDIT_LOG_DATA = "operator.exportAuditLogData";
    String MENU_ITEM_OPERATOR_MANAGE_INDEXES = "operator.manageIndexes";
    String MENU_ITEM_MARKETPLACE_LINK = "marketplace.link";
    String MENU_GROUP_NAVIGATION_MYACCOUNT = "navigation.myAccount";
    String MENU_ITEM_ORGANIZATION_EDIT = "organization.edit";
    String MENU_ITEM_USER_PWD = "user.pwd";
    String MENU_ITEM_USER_ADD = "user.add";
    String MENU_ITEM_USER_IMPORT = "user.import";
    String MENU_ITEM_USER_LIST = "user.list";
    String MENU_ITEM_ORGANIZATION_MANAGE_LDAP = "organization.ldapSettings";
    String MENU_ITEM_ORGANIZATION_PAYMENT = "organization.payment";
    String MENU_ITEM_ORGANIZATION_REPORT = "organization.report";
    String MENU_ITEM_TRIGGER_PROCESS_LIST = "triggerProcess.list";
    String MENU_ITEM_ORGANIZATION_MANAGE_SUPPLIERS = "organization.manageSuppliers";
    String MENU_ITEM_ORGANIZATION_MANAGE_PAYMENTS = "organization.managePayments";
    String MENU_ITEM_ORGANIZATION_MANAGE_UDAS = "organization.manageUdas";
    String MENU_GROUP_NAVIGATION_MARKETPLACE = "navigation.marketplace";
    String MENU_ITEM_MARKETPLACE_MANAGE_CATEGORIES = "marketplace.manageCategories";
    String MENU_ITEM_MARKETPLACE_MANAGE_SUPPLIERS = "marketplace.manageSuppliers";
    String MENU_ITEM_MARKETPLACE_CREATE = "marketplace.create";
    String MENU_ITEM_MARKETPLACE_UPDATE = "marketplace.update";
    String MENU_ITEM_MARKETPLACE_DELETE = "marketplace.delete";
    String MENU_ITEM_MARKETPLACE_MANAGE_BROKER_REVENUE_SHARE = "marketplace.manageBrokerRevenueShare";
    String MENU_ITEM_MARKETPLACE_MANAGE_RESELLER_REVENUE_SHARE = "marketplace.manageResellerRevenueShare";
    String MENU_ITEM_MARKETPLACE_LANDINGPAGE = "marketplace.landingpage";
    String MENU_ITEM_MARKETPLACE_CUSTOMIZE_BRAND = "shop.customizeBrand";
    String MENU_ITEM_MARKETPLACE_EDIT_SKIN = "shop.editSkin";
    String MENU_ITEM_MARKETPLACE_TRACKINGCODE = "shop.trackingCode";
    String MENU_ITEM_MARKETPLACE_EDIT_STAGE = "shop.editStage";
    String MENU_ITEM_MARKETPLACE_TRANSLATION = "shop.translations";
    String MENU_ITEM_MARKETPLACE_MANAGE_ACCESS = "marketplace.manageAccess";
    String MENU_GROUP_NAVIGATION_CUSTOMER = "navigation.customer";
    String MENU_ITEM_ORGANIZATION_ADD_CUSTOMER = "organization.addCustomer";
    String MENU_ITEM_EDIT_CUSTOMER = "organization.editCustomer";
    String MENU_ITEM_VIEW_CUSTOMER = "organization.viewCustomer";
    String MENU_ITEM_MANAGE_COUNTRIES = "organization.manageCountries";
    String MENU_ITEM_ORGANIZATION_MANAGE_PAYMENT_ENABLEMENT = "organization.managePaymentEnablement";
    String MENU_ITEM_MANAGE_VAT = "organization.manageVats";
    String MENU_ITEM_ORGANIZATION_EXPORT_BILLING_DATA = "user.exportBillingData";
    String MENU_ITEM_SUBSCRIPTION_VIEW = "subscription.view";
    String MENU_ITEM_SUBSCRIPTION_EDIT_UDAS = "subscription.editUdas";
    String MENU_ITEM_SUBSCRIPTION_TERMINATE = "subscription.terminate";
    String MENU_GROUP_NAVIGATION_TECHSERVICE = "navigation.techService";
    String MENU_ITEM_TECHSERVICE_ADD = "techService.add";
    String MENU_ITEM_TECHSERVICE_IMPORT = "techService.import";
    String MENU_ITEM_TECHSERVICE_EDIT = "techService.edit";
    String MENU_ITEM_TECHSERVICE_EXPORT = "techService.export";
    String MENU_ITEM_TECHSERVICE_DELETE = "techService.delete";
    String MENU_GROUP_NAVIGATION_SERVICE = "navigation.service";
    String MENU_ITEM_TECHSERVICE_VIEW_ADAPTERS = "techService.viewBillingAdapters";
    String MENU_ITEM_SERVICE_ADD = "service.add";
    String MENU_ITEM_SERVICE_EDIT = "service.edit";
    String MENU_ITEM_SERVICE_COPY = "service.copy";
    String MENU_ITEM_SERVICE_DELETE = "service.delete";
    String MENU_ITEM_SERVICE_UPGRADE_OPTIONS = "service.upgradeOptions";
    String MENU_ITEM_SERVICE_VIEW = "service.view";
    String MENU_ITEM_SERVICE_MANAGE = "service.manage";
    String MENU_ITEM_SERVICE_ACTIVATION = "service.activation";
    String MENU_ITEM_SERVICE_PUBLISH = "service.publish";
    String MENU_GROUP_NAVIGATION_PRICE_MODEL = "navigation.priceModel";
    String MENU_ITEM_PRICE_MODEL_SERVICE = "priceModel.service";
    String MENU_ITEM_PRICE_MODEL_CUSTOMER = "priceModel.customer";
    String MENU_ITEM_PRICE_MODEL_DELETE = "priceModel.customer.delete";
    String MENU_ITEM_PRICE_MODEL_SUBSCRIPTION = "priceModel.subscription";
    String PANEL_ORGANIZATION_EDIT_USERPROFILE = "organization.edit.userProfile";
    String PANEL_ORGANIZATION_EDIT_ORGANIZATIONDATA = "organization.edit.organizationData";
    String PANEL_USER_LIST_SUBSCRIPTIONS = "user.list.subscriptions";
    String MENU_ITEM_ORGANIZATION_MANAGE_BILLING = "user.manageBilling";
    String MARKETPLACE_MENU_ITEM_ACCOUNT_PROFILE = "marketplace.navigation.Profile";
    String MARKETPLACE_MENU_ITEM_ACCOUNT_PAYMENT = "marketplace.navigation.Payment";
    String MARKETPLACE_MENU_ITEM_ACCOUNT_SUBSCRIPTIONS = "marketplace.navigation.Subscriptions";
    String MARKETPLACE_MENU_ITEM_ACCOUNT_USERS = "marketplace.navigation.Users";
    String MARKETPLACE_MENU_ITEM_ACCOUNT_UNITS = "marketplace.navigation.Units";
    String MARKETPLACE_MENU_ITEM_ACCOUNT_REPORTS = "marketplace.navigation.Reports";
    String MARKETPLACE_MENU_ITEM_ACCOUNT_PROCESSES = "marketplace.navigation.Processes";
    String MARKETPLACE_MENU_ITEM_ACCOUNT_OPERATIONS = "marketplace.navigation.Operations";
    String MARKETPLACE_MENU_ITEM_ACCOUNT_ADMINISTRATION = "marketplace.navigation.Administration";

}
