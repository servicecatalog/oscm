/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.oscm.internal.accountmgmt.AccountServiceManagement;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.BillingService;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.DiscountService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.MarketplaceServiceInternal;
import org.oscm.internal.intf.PaymentService;
import org.oscm.internal.intf.ReportingService;
import org.oscm.internal.intf.SearchService;
import org.oscm.internal.intf.SearchServiceInternal;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.ServiceProvisioningServiceInternal;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.intf.SubscriptionServiceInternal;
import org.oscm.internal.intf.TagService;
import org.oscm.internal.intf.TriggerDefinitionService;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.landingpageconfiguration.LandingpageConfigurationService;
import org.oscm.internal.operatorservice.LocalizedDataService;
import org.oscm.internal.operatorservice.ManageLanguageService;
import org.oscm.internal.passwordrecovery.PasswordRecoveryService;
import org.oscm.internal.portallandingpage.LandingpageService;
import org.oscm.internal.pricemodel.external.ExternalPriceModelService;
import org.oscm.internal.review.ReviewInternalService;
import org.oscm.internal.subscriptiondetails.SubscriptionDetailsService;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.techserviceoperationmgmt.OperationRecordService;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.usermanagement.UserService;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocalizationLocal;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.ui.model.User;
import org.oscm.ui.resources.DbMessages;

/**
 * Base class for all backing beans which provides the getters for the services
 * and some helper methods.
 * 
 */
public class BaseBean {
    public UiDelegate ui = new UiDelegate();

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(BaseBean.class);

    /**
     * Helper class to map a list with value objects to a list with model
     * objects
     * 
     */
    public abstract static class Vo2ModelMapper<V, M> {

        /**
         * Perform the mapping of the value object list to the model list.
         * 
         * @param voList
         *            the value object list
         * @return the model list
         */
        public List<M> map(final List<V> voList) {

            List<M> list = new ArrayList<>();
            if (voList != null) {
                for (V vo : voList) {
                    M model = createModel(vo);
                    if (model != null) {
                        list.add(model);
                    }
                }
            }
            return list;
        }

        /**
         * Create a new model object for the given value object.
         * 
         * @param vo
         *            the value object
         * @return the model object
         */
        public abstract M createModel(V vo);

    }

    public static final String CUSTOMER = "CUSTOMER";
    public static final String CUSTOMER_SUBSCRIPTION = "CUSTOMER_SUBSCRIPTION";
    public static final String ERROR_COMPLETE_REGISTRATION = "error.complete.registration";
    public static final String ERROR_CONVERTER_BIG_DECIMAL = "error.converter.bigDecimal";
    public static final String ERROR_DATABASE_NOT_AVAILABLE = "error.database.notAvailable";
    public static final String ERROR_GROUP_DELETED = "error.group.concurrentDelete";
    public static final String ERROR_GROUP_EXIST = "error.group.exist";
    public static final String ERROR_INVALID_MARKETPLACE_URL = "error.invalidMarketplaceUrl";
    public static final String ERROR_INVALID_SERVICE_URL = "error.invalidServiceUrl";
    public static final String ERROR_LOGIN = "error.login";
    public static final String ERROR_LOGIN_TO_CLOSED_MARKETPLACE = "error.loginToClosedMarketplace";
    public static final String ERROR_ACCESS_TO_CLOSED_MARKETPLACE = "error.accessToClosedMarketplace";
    public static final String ERROR_LOGIN_SAML_SP = "error.login.samlsp";
    public static final String ERROR_LOGIN_IMPOSSIBLE = "error.login.impossible";
    public static final String ERROR_USER_ALREADY_CONFIRMED = "error.user.alreadyConfirmed";
    public static final String ERROR_USER_ALREADY_EXIST = "error.user.alreadyExist";
    public static final String ERROR_USER_CREATE_MAIL = "error.user.create.mail";
    public static final String ERROR_USER_CREATE_MAIL_NOT_INTERNAL = "error.user.create.mail.notInternal";
    public static final String ERROR_USER_CREATE_INSUFFICIENT_ROLES = "error.user.create.insufficientRoles";
    public static final String ERROR_USER_LDAP_SEARCH_LIMIT_EXCEEDED = "error.user.ldapSearchLimitExceeded";
    public static final String ERROR_USER_LOCKED = "error.user.locked";
    public static final String ERROR_USER_LOCKED_NOT_CONFIRMED = "error.user.lockedNotConfirmed";
    public static final String ERROR_USER_CONFIRMED_LOGIN_FAIL = "error.user.confirmed.loginfail";
    public static final String ERROR_USER_PWD_MATCH = "error.user.pwdMatch";
    public static final String ERROR_USER_PWD_LENGTH = "error.user.pwdLength";
    public static final String ERROR_USER_PWD_RESET = "error.user.pwdReset";
    public static final String ERROR_USER_SECURITY_INFO_MISSING = "error.user.sequrityInfo.missing";
    public static final String ERROR_CONFIRMATION_INVALID_LINK = "error.confirmation.invalid.link";
    public static final String ERROR_REGISTRATION_ACKNOWLEDGE_MAIL = "error.registration.acknowledge.mail";
    public static final String ERROR_REGISTRATION_TERMS = "error.registration.terms";
    public static final String ERROR_REGISTRATION_CAPTCHA = "error.registration.captcha";
    public static final String ERROR_CAPTCHA = "error.captcha";
    public static final String ERROR_PARAMETER_VALUE_OUT_OF_RANGE = "error.parameter.value.outOfRange";
    public static final String ERROR_LONG_VALUE_OUT_OF_RANGE = "error.long.value.outOfRange";
    public static final String ERROR_PARAMETER_VALUE_MANDATORY = "error.parameter.value.mandatory";
    public static final String ERROR_PARAMETER_VALUE_TO_LONG = "javax.faces.validator.LengthValidator.MAXIMUM";
    public static final String ERROR_PRICEMODEL_INPUT = "error.priceModel.input";
    public static final String ERROR_PRICEMODEL_INVALID_DECIMAL_SEPRATOR = "error.priceModel.invalid.decimal.input";
    public static final String ERROR_PRICEMODEL_INVALID_FRACTIONAL_PART = "error.priceModel.invalid.fractional.part";
    public static final String ERROR_SERVICE_SAVED_MARKETPLACE_DELETED = "error.service.savedButNotPublished_marketplaceDeleted";
    public static final String ERROR_SERVICE_SAVED_PUBLISHING_NOT_PERMITTED = "error.service.savedButNotPublished_publishingNotPermitted";
    public static final String ERROR_SERVICE_NOT_AVAILABLE = "error.service.notAvailable";
    public static final String ERROR_SERVICE_NOT_AVAILABLE_LOGGED_IN = "error.service.notAvailableLoggedIn";
    public static final String ERROR_SERVICE_NOT_AVAILABLE_ANYMORE = "error.service.notAvailableAnymore";
    public static final String ERROR_SERVICE_INVALID_KEY = "error.service.invalidKey";
    public static final String ERROR_SHOP_TRANSLATIONS_FILEFORMAT = "error.shop.translations.fileformat";
    public static final String ERROR_SHOP_TRANSLATIONS_FILENAME_PROPERTIES = "error.shop.translations.filename.properties";
    public static final String ERROR_SUBSCRIPTION_LICENSE = "error.subscription.license";
    public static final String ERROR_SUBSCRIPTION_KEY = "error.subscription.parseKey";
    public static final String ERROR_TEXT_FIELDS = "error.text.fields";
    public static final String ERROR_TRANSLATIONS_FILEFORMAT = "error.translations.fileformat";
    public static final String ERROR_TRANSLATIONS_ONELANGUAGE = "error.translations.onelanguageallowed";
    public static final String ERROR_UPLOAD = "error.upload";
    public static final String ERROR_CONNECT_TO_OIDC = "error.connect.to.oidc";
    public static final String ERROR_DELETE_USER_FROM_EXPIRED_SUBSCRIPTION = "error.deleteUser.subscriptionExpired";
    public static final String ERROR_UPLOAD_SIZE_LIMIT_EXCEEDED = "error.upload.sizeLimitExceeded";
    public static final String ERROR_UPLOAD_SIZE_LIMIT_EXCEEDED_KNOWNMAX = "error.upload.sizeLimitExceeded.konwnMax";
    public static final String ERROR_FROM_DATE_AFTER_TO_DATE = "error.date.fromAfterTo";
    public static final String ERROR_TO_DATE_BEFORE_FROM_DATE = "error.date.toBeforeFrom";
    public static final String ERROR_DISCOUNT_VALUE = "error.discount.value";
    public static final String ERROR_DISCOUNT_DATE = "error.discount.date";
    public static final String ERROR_DISCOUNT_DATE_BEFORE = "error.discount.date.before";
    public static final String ERROR_DISCOUNT_DATE_FUTURE = "error.discount.date.future";
    public static final String ERROR_DISCOUNT_INVALID_FRACTIONAL_PART = "error.discount.invalid.fractional.part";
    public static final String ERROR_DISCOUNT_VALUE_IS_REQUIRED = "error.discount.value.required";
    public static final String ERROR_SEARCH_TEXT_EMPTY = "error.search.emptyInput";
    public static final String ERROR_SUBSCRIPTION_NOT_ACCESSIBLE = "error.subscription.notAccessible";
    public static final String ERROR_SUBSCRIPTION_NOT_SET = "error.subscription.subscriptionNotSet";
    public static final String ERROR_SUBSCRIPTION_NOT_FOUND = "error.subscription.subscriptionNotFound";
    public static final String ERROR_USER_NOT_FOUND = "ex.ObjectNotFoundException.USER";
    public static final String ERROR_USERGROUP_NOT_FOUND = "ex.ObjectNotFoundException.USER_GROUP";
    public static final String ERROR_USER_GROUP_TO_USER_NOT_FOUND = "ex.ObjectNotFoundException.USER_GROUP_TO_USER";
    public static final String ERROR_USERGROUP_NOT_FOUND_EXCEPTION = "errorUserGroupNotFound";
    public static final String ERROR_USER_GROUP_TO_USER_NOT_FOUND_EXCEPTION = "errorUserGroupToUserNotFound";
    public static final String ERROR_USERGROUP_NOT_FOUND_EXCEPTION_UNIT_ADMIN = "errorUserGroupNotFoundUnitAdmin";
    public static final String ERROR_NO_CURRENCIES = "error.no.currencies";
    public static final String ERROR_NOT_AVALIABLE_SERVICE = "ex.OperationNotPermittedException.NOT_AVALIABLE_SERVICE";
    public static final String ERROR_FILE_IMPORT_FAILED = "ex.ValidationException.FILE_IMPORT_FAILED";
    public static final String ERROR_FILE_IMPORT_FAILED_ISOCODE_INVALID = "ex.ValidationException.FILE_IMPORT_FAILED_ISOCODE_INVALID";
    public static final String ERROR_FILE_IMPORT_FAILED_ISOCODE_NOT_SUPPORTED = "ex.ValidationException.LANGUAGE_ISOCODE_NOT_SUPPORTED";
    public static final String ERROR_FILE_IMPORT_NO_lANGUAGE = "ex.PropertiesImportException.NONE_LANGUAGE_CODE";
    public static final String ERROR_FILE_IMPORT_LANGUAGE_NOT_SUPPORTED = "ex.PropertiesImportException.LANGUAGE_NOT_SUPPORTED";
    public static final String ERROR_FILE_IMPORT_TRANSLATIONS_MISSING = "ex.PropertiesImportException.TRANSLATIONS_MISSING";
    public static final String ERROR_FILE_IMPORT_TRANSLATIONS_MULTIPLEKEY_EXISTING = "error.translations.multiplekey.existing";
    public static final String ERROR_BILLING_CONTACT_MODIFIED_OR_DELETED_CONCURRENTLY = "error.paymentInfo.modifiedConcurrently";
    public static final String ERROR_ACOUNT_MODIFIED_OR_DELETED_CONCURRENTLY = "error.editUser.modifiedConcurrently";
    public static final String ERROR_UNIT_MODIFIED_OR_DELETED_CONCURRENTLY = "error.editUnit.modifiedConcurrently";
    public static final String ERROR_SUBSCRIPTION_MODIFIED_OR_DELETED_CONCURRENTLY = "error.editSubscription.modifiedConcurrently";
    public static final String ERROR_REVENUESHARE_INVALID_FRACTIONAL_PART = "error.revenueshare.invalid.fractional.part";
    public static final String ERROR_REVENUESHARE_VALUE = "error.revenueshare.value";
    public static final String ERROR_RECOVERPASSWORD_INVALID_LINK = "error.recoverpassword.invalid.link";
    public static final String ERROR_GENERATE_AUTHNREQUEST = "error.generating.authnrequest";
    public static final String ERROR_TOKEN_VALIDATION_FAILED = "error.auth.tokenvalidation";
    public static final String ERROR_INVALID_SAML_RESPONSE = "error.invalid.samlResponse";
    public static final String ERROR_INVALID_SAML_RESPONSE_STATUS_CODE = "error.invalid.samlResponseStatusCode";
    public static final String ERROR_SAML_TIMEOUT = "error.timeout.during.saml";
    public static final String ERROR_INVALID_IDP_URL = "error.invalid.idpUrl";
    public static final String ERROR_CSS_CONNECTION = "error.cssConnection";
    public static final String ERROR_ISOCODE_ISEMPTY = "error.isocode.empty";
    public static final String ERROR_ISOCODE_INVALID = "error.isocode.invalid";
    public static final String ERROR_ISOCODE_NOTSUPPORTED = "error.isocode.notsupported";
    public static final String ERROR_LOCALE_INVALID = "error.locale.invalid";
    public static final String ERROR_LDAPUSER_RESETPASSWORD = "error.ldapuser.resetpassword";
    public static final String ERROR_INVALID_GROUP = "error.group.invalid";
    public static final String ERROR_TO_PROCEED_SELECT_UNIT = "error.subscription.unitHasToBeSelected";
    public static final String ERROR_EXTERNAL_PRICEMODEL_NOT_AVAILABLE = "error.externalPricemodel.notavailable";
    public static final String ERROR_BILLING_ID_ALREADY_EXISTS = "ex.NonUniqueBusinessKeyException.BILLING_ADAPTER";
    public static final String ERROR_NO_FILE_WITH_IDP_SETTINGS = "error.tenant.idpsettings.nofile";
    public static final String ERROR_MISSING_TENANTID = "error.missingtenant";
    public static final String ERROR_TENANT_SETTINGS_MISSING = "error.missingtenantsettings";
    public static final String ERROR_TENANT_NO_LONGER_EXISTS = "error.tenant.noLongerExists";
    public static final String ERROR_MARKETPLACE_REMOVED = "error.tenant.marketplaceRemoved";
    public static final String ERROR_EMPTY_FILE = "error.upload.fileNotNullNorEmpty";

    public static final String WARNING_SUBSCRIBE_ONLY_ONCE = "warning.subscription.onlyOne";
    public static final String WARNING_SUBSCRIBE_ONLY_BY_ADMIN = "warning.subscription.onlyByAdmin";
    public static final String WARNING_SUPPORTEDLANGUAGE_LOCALE_INVALID = "warning.supportedlanguage.locale.invalid";
    public static final String WARNING_SUPPORTEDLANGUAGE_LOCALE_DEACTIVATED = "warning.supportedlanguage.locale.deactivated";
    public static final String WARNING_OWNER_NOT_A_UNIT_ADMIN = "warning.editSubscription.subscriptionOwner.administrator";
    public static final String WARNING_OWNER_IS_SUB_MAN = "warning.editSubscription.subscriptionOwner.subscriptionManager";
    public static final String WARNING_UNIT_NOT_SELECTED_UNIT_ADMIN = "warning.editSubscription.subscriptionUnitNotSelected";
    public static final String WARNING_PAYMENT_TYPES_NOT_USED = "warning.paymentTypesAreNotUsed";
    public static final String WARNING_NO_CUSTOMER_ACCESS_TO_RESTRICTED_MPL = "warning.noCustomerAccessToRestrictedMpl";
    public static final String WARNING_TENANT_DEF_NOT_COMPLETE = "warning.tenantDefinitionNotComplete";

    public static final String INFO_BILLING_CONTACT_DELETED = "info.billingContact.deleted";
    public static final String INFO_BILLING_CONTACT_DELETED_CONCURRENTLY = "info.billingContact.deletedConcurrently";
    public static final String INFO_BILLING_CONTACT_SAVED = "info.billingContact.saved";
    public static final String INFO_GROUP_CREATED = "info.group.created";
    public static final String INFO_GROUP_SAVED = "info.group.saved";
    public static final String INFO_GROUP_DELETED = "info.group.deleted";
    public static final String INFO_GROUP_DELETE_MSG_KEY = "info.group.deleted.message";
    public static final String INFO_GROUP_USERASSIGNED = "info.group.userAssigned";
    public static final String INFO_GROUP_USERDEASSIGNED = "info.group.userDeassigned";
    public static final String INFO_MANAGE_BILLING_SAVED = "info.manageBilling.saved";
    public static final String INFO_MARKETPLACE_SAVED = "info.marketplace.saved";
    public static final String INFO_OPERATION_EXECUTED = "info.operation.executed";
    public static final String INFO_OPERATION_DELETED = "info.operation.deleted";
    public static final String INFO_ORGANIZATION_SAVED = "info.organization.saved";
    public static final String INFO_ORGANIZATION_SUPPORTMAIL_SENT = "info.supportemail.sent";
    public static final String INFO_ORGANIZATION_CREATED = "info.organization.created";
    public static final String INFO_ORGANIZATION_UPDATED = "info.organization.saved";
    public static final String INFO_PASSWORD_CHANGED = "info.password.changed";
    public static final String INFO_PAYMENT_INFO_DELETED = "info.paymentInfo.deleted";
    public static final String INFO_PAYMENT_INFO_DELETED_CONCURRENTLY = "info.paymentInfo.deletedConcurrently";
    public static final String INFO_PAYMENT_INFO_SAVED = "info.paymentInfo.saved";
    public static final String INFO_PAYMENT_TYPE_SAVED = "info.paymentType.saved";
    public static final String INFO_PSP_SAVED = "info.psp.saved";
    public static final String INFO_PSP_CREATED = "info.psp.created";
    public static final String INFO_SERVICE_CREATED = "info.service.created";
    public static final String INFO_SERVICE_DELETED = "info.service.deleted";
    public static final String INFO_SERVICE_IMPORTED = "info.service.imported";
    public static final String INFO_SERVICE_SAVED = "info.service.saved";
    public static final String INFO_SERVICE_STATE_SAVED = "info.service.statesaved";
    public static final String INFO_SERVICE_UPGRADEOPTIONS_SAVED = "info.service.upgradeOptionsSaved";
    public static final String INFO_SERVICE_CATALOGDETAILS_SAVED = "info.service.catalogDetailsSaved";
    public static final String INFO_SERVICE_SUSPENDED = "info.service.suspended";
    public static final String INFO_SERVICE_RESUMED = "info.service.resumed";
    public static final String INFO_SUBSCRIPTION_CREATED = "info.subscription.created";
    public static final String INFO_DIRECT_SUBSCRIPTION_CREATED_DISABLE_INFO = "info.direct.subscription.created.disable.info";
    public static final String INFO_SUBSCRIPTION_DELETED = "info.subscription.deleted";
    public static final String INFO_SUBSCRIPTION_UPGRADED = "info.subscription.upgraded";
    public static final String INFO_SUBSCRIPTION_SAVED = "info.subscription.saved";
    public static final String INFO_SUBSCRIPTION_USER_SAVED = "info.subscription.userSaved";
    public static final String INFO_SUBSCRIPTION_TERMINATED = "info.subscription.terminated";
    public static final String INFO_PRICEMODEL_SAVED = "info.pricemodel.saved";
    public static final String INFO_PRICEMODEL_FOR_CUSTOMER_SAVED = "info.pricemodel.customer.saved";
    public static final String INFO_PRICEMODEL_FOR_SUBSCRIPTION_SAVED = "info.pricemodel.subscription.saved";
    public static final String INFO_PRICEMODEL_FOR_CUSTOMER_DELETED = "info.pricemodel.customer.deleted";
    public static final String INFO_PRICEMODEL_FOR_CUSTOMER_DELETED_CONCURRENTLY = "info.pricemodel.customer.deletedConcurrently";
    public static final String INFO_TECH_SERVICE_CREATED = "info.techService.created";
    public static final String INFO_TECH_SERVICE_DELETED = "info.techService.deleted";
    public static final String INFO_TECH_SERVICE_IMPORTED = "info.techService.imported";
    public static final String INFO_TECH_SERVICE_SAVED = "info.techService.saved";
    public static final String INFO_TRIGGER_DEFINITION_CREATED = "info.triggerDefinition.created";
    public static final String INFO_TRIGGER_DEFINITION_SAVED = "info.triggerDefinition.saved";
    public static final String INFO_TRIGGER_DEFINITION_DELETED = "info.triggerDefinition.deleted";
    public static final String INFO_USER_ACCOUNTS_SENT = "info.user.accounts.sent";
    public static final String INFO_USER_CREATED = "info.user.created";
    public static final String INFO_USER_DELETED = "info.user.deleted";
    public static final String INFO_USER_PROFILE_SAVED = "info.user.profileSaved";
    public static final String INFO_USER_PWD_RESET = "info.user.pwdReset";
    public static final String INFO_USER_IMPORTED = "info.user.imported";
    public static final String INFO_USER_SAVED = "info.user.saved";
    public static final String INFO_USER_SAVED_ITSELF = "info.user.saved.itself";
    public static final String INFO_USER_SECURITY_INFO_SAVED = "info.user.securityInfoSaved";
    public static final String INFO_USER_UNLOCKED = "info.user.unlocked";
    public static final String INFO_SHOP_TRANSLATIONS_DELETED = "info.shop.translations.deleted";
    public static final String INFO_TRANSLATIONS_SAVED = "info.shop.translations.saved";
    public static final String INFO_MARKETPLACE_STAGE_SAVED = "info.marketplace.stage.saved";
    public static final String INFO_MARKETPLACE_CREATED = "info.marketplace.created";
    public static final String INFO_MARKETPLACE_DELETED = "info.marketplace.deleted";
    public static final String INFO_MARKETPLACE_ACCESS_SAVED = "marketplace.manageAccess.saved";
    public static final String INFO_SUPPLIER_ADDED = "info.supplier.added";
    public static final String INFO_SUPPLIER_REMOVED = "info.supplier.removed";
    public static final String INFO_SUPPLIER_BANNED = "info.supplier.banned";
    public static final String INFO_SUPPLIER_BANLIFTED = "info.supplier.banlifted";
    public static final String INFO_PAYMENT_ENABLEMENT_SAVED = "info.paymentEnablment.saved";
    public static final String INFO_RECREATE_INDEXES = "info.operator.indexes";
    public static final String TRIGGER_PROCESS_DELETED = "info.triggerProcess.deleted";
    public static final String TRIGGER_PROCESS_CANCELED = "info.triggerProcess.canceled";
    public static final String INFO_UDADEFINITIONS_DELETED = "info.udaDefinitions.deleted";
    public static final String INFO_UDADEFINITIONS_SAVED = "info.udaDefinitions.saved";
    public static final String INFO_UDA_SAVED = "info.uda.saved";
    public static final String INFO_BRANDING_URL_SET = "info.brandingUrl.set";
    public static final String INFO_CATEGORIES_SAVED = "info.categories.saved";
    public static final String INFO_VAT_SAVED = "info.vat.saved";
    public static final String INFO_COUNTRIES_SAVED = "info.countries.saved";
    public static final String INFO_CONFIGURATION_SAVED = "info.configuration.saved";
    public static final String INFO_CURRENCIES_ADDED = "info.currencies.added";
    public static final String INFO_WHITE_LABEL_BRANDING_URL_SET = "info.whiteLabelBrandingUrl.set";
    public static final String INFO_RECOVERPASSWORD_START = "info.recoverpassword.start";
    public static final String INFO_RECOVERPASSWORD_SUCCESS = "info.recoverpassword.success";
    public static final String INFO_CSS_CONNECTION_SUCCESS = "info.cssConnection.success";
    public static final String INFO_SUPPORTEDLANGUAGE_SAVED = "info.supportedlanguage.saved";
    public static final String INFO_SUPPORTEDLANGUAGE_ADDED = "info.supportedlanguage.added";
    public static final String INFO_NO_MORE_USERS = "info.subscriptions.noMoreUsersForAssignment";
    public static final String INFO_EXTERNAL_PRICE_UPLOADED = "info.externalPriceModel.upload";
    public static final String INFO_TENANT_SAVED = "info.tenant.saved";
    public static final String INFO_TENANT_ADDED = "info.tenant.added";
    public static final String INFO_TENANT_DELETED = "info.tenant.deleted";
    
    public static final String LABEL_USERINTERFACE_TRANSLARIONS = "label.userinterface.title";
    public static final String LABEL_MAIL_TRANSLARIONS = "label.mail.title";
    public static final String LABEL_PLATFORM_TRANSLARIONS = "label.platform.title";

    public static final String LABEL_SHOP_TRANSLARIONS = "shop.translations.title";
    public static final String LABEL_SHOP_TRANSLARIONS_KEY = "shop.translations.key";

    public static final String LABEL_PRICE_MODEL_FREE = "priceModel.text.free";
    public static final String LABEL_PRICE_MODEL_PRICE = "priceModel.text.price";
    public static final String LABEL_PRICE_MODEL_PER_SUB = "priceModel.text.perSubscription";
    public static final String LABEL_PRICE_MODEL_PER_USER = "priceModel.text.perUser";
    public static final String LABEL_PRICE_MODEL_SEE_DETAILS = "priceModel.text.seeDetails";
    public static final String LABEL_PRICE_MODEL_PRICE_AND_UNIT = "priceModel.text.combinePriceAndUnit";
    public static final String LABEL_PRICE_MODEL_EXTERNAL = "priceModel.text.external";

    public static final String INFO_ADAPTER_SAVED = "info.billingAdapter.saved";
    public static final String INFO_ADAPTER_DELETED = "info.billingAdapter.deleted";
    public static final String INFO_BILLINGSYSTEM_CONNECTION_SUCCESS = "info.billingSystem.connection.success";

    public static final String MARKETPLACE_START_SITE = Marketplace.MARKETPLACE_ROOT
            + "/index.jsf";
    public static final String MARKETPLACE_LOGIN_PAGE = Marketplace.MARKETPLACE_ROOT
            + "/loginPage.jsf";

    public static final String MARKETPLACE_LOGIN = Marketplace.MARKETPLACE_ROOT
            + "/login.jsf";

    public static final String MARKETPLACE_ERROR_PAGE = Marketplace.MARKETPLACE_ROOT
            + "/errorPage.jsf";
    public static final String MARKETPLACE_USERS_PAGE = Marketplace.MARKETPLACE_ROOT
            + "/account/users.jsf";
    public static final String MARKETPLACE_UNITS_PAGE = Marketplace.MARKETPLACE_ROOT
            + "/account/users.jsf";
    public static final String MARKETPLACE_REDIRECT = Marketplace.MARKETPLACE_ROOT
            + "/redirect.jsf";
    public static final String MARKETPLACE_ACCESS_DENY_PAGE = Marketplace.MARKETPLACE_ROOT
            + "/accessDeniedPage.jsf";

    public static final String SAML_SP_LOGIN_AUTOSUBMIT_PAGE = Marketplace.MARKETPLACE_ROOT
            + "/serviceProvider.jsf";
    public static final String MANAGE_PAYMENT_TYPES_PAGE = "/organization/managePaymentEnablement.jsf";
    public static final String MANAGE_PAYMENT_TYPES_PAGE_XHTML = "/organization/managePaymentEnablement.xhtml";
    public static final String ACCOUNT_PAYMENT_PAGE = "/marketplace/account/payments.jsf";
    public static final String ACCOUNT_PAYMENT_PAGE_XHTML = "/marketplace/account/payments.xhtml";

    public static final String ERROR_PAGE = "/public/error.jsf";

    public static final String OUTCOME_LOGIN = "login";
    public static final String OUTCOME_MARKETPLACE_REDIRECT = "marketplace/redirect";
    public static final String OUTCOME_MARKETPLACE_LOGOUT = "marketplace/logout";
    public static final String OUTCOME_ERROR = "error";
    public static final String OUTCOME_CANCEL = "cancel";
    public static final String OUTCOME_NEWGROUP = "addNewGroup";
    public static final String OUTCOME_SUCCESS = "success";
    public static final String OUTCOME_SUCCESS_UNIT_ADMIN = "successUnitAdmin";
    public static final String OUTCOME_PENDING = "pending";
    public static final String OUTCOME_PSP_ERROR = "psp_error";
    public static final String OUTCOME_PSP_SUCCESS = "psp_success";
    public static final String OUTCOME_NEXT = "next";
    public static final String OUTCOME_SERVICE_EDIT = "editService";
    public static final String OUTCOME_SERVICE_SUBSCRIBE = "serviceSubscribe";
    public static final String OUTCOME_SERVICE_UNSUBSCRIBE = "serviceUnsubscribe";
    public static final String OUTCOME_SERVICE_UPGRADE = "serviceUpgrade";
    public static final String OUTCOME_SERVICE_LOCALIZE = "serviceLocalize";
    public static final String OUTCOME_PRICE_MODEL_LOCALIZE = "priceModelLocalize";
    public static final String OUTCOME_ENTER_PAYMENT = "enterPayment";
    public static final String OUTCOME_RESET_PWD = "resetPwd";
    public static final String OUTCOME_PREVIOUS = "previous";
    public static final String OUTCOME_SHOW_DETAILS = "showDetails";
    public static final String OUTCOME_REVIEW_ENABLEMENT_CHANGED = "reviewEnablementChanged";
    public static final String OUTCOME_SHOW_REGISTRATION = "showRegistration";
    public static final String OUTCOME_SHOW_SERVICE_LIST = "showServiceList";
    public static final String OUTCOME_MARKETPLACE_CONFIRMSTARTPWDRECOVERY = "marketplace/confirmStartPwdRecovery";
    public static final String OUTCOME_SUBSCRIPTION_LIST = "showSubscription";
    public static final String OUTCOME_USER_LIST = "showUsers";
    public static final String OUTCOME_REFRESH = "refresh";
    public static final String OUTCOME_UNIT_ADMIN_ROLE_REMOVED = "unitAdminRoleRemoved";

    public static final String PROGRESS_DEFAULT = "progress.default";
    public static final String PROGRESS_CREATE_USER = "progress.createUser";
    public static final String PROGRESS_PANEL = "progressPanel";
    public static final String OUTCOME_MODIFICATION_ERROR = "concurrentModificationError";
    public static final String OUTCOME_PUBLIC_ERROR_PAGE = "publicErrorPage";
    public static final String OUTCOME_MARKETPLACE_ERROR_PAGE = "marketplaceErrorPage";
    public static final String OUTCOME_EDIT_GROUP = "editGroup";
    public static final String OUTCOME_RELOAD = "reloadGroup";

    public static final String OUTCOME_STAY_ON_PAGE = null;
    public static final String HIDDEN_PWD = "*****";

    // Without the @EJB annotation we can run the GUI without an EJB container
    IdentityService idService;
    UserGroupService userGroupService;
    OperationRecordService operationRecordService;
    UserService userService;
    SubscriptionService subscriptionService;
    SubscriptionsService subscriptionsService;
    SubscriptionServiceInternal subscriptionServiceInternal;
    ServiceProvisioningService provisioningService;
    ServiceProvisioningServiceInternal provisioningServiceInternal;
    SearchService searchService;
    SearchServiceInternal searchServiceInternal;
    TagService tagService;
    AccountService accountingService;
    SessionService sessionService;
    ReportingService reportingService;
    PaymentService paymentProcessingService;
    BillingService billingService;
    MarketplaceService marketplaceService;
    MarketplaceServiceInternal marketplaceServiceInternal;
    ReviewInternalService reviewInternalService;
    DiscountService discountService;
    TriggerDefinitionService triggerDefinitionService;
    TriggerService triggerService;
    CategorizationService categorizationService;
    LandingpageConfigurationService landingpageService;
    LandingpageService showLandingpageService;
    SubscriptionDetailsService subscriptionDetailsService;
    ServiceProvisioningServiceLocalizationLocal serviceProvisioningServiceLocalizationLocal;
    AccountServiceManagement accountServiceManagement;
    PasswordRecoveryService passwordRecoveryService;
    ConfigurationService configurationService;
    ManageLanguageService manageLanguageService;
    LocalizedDataService localizedDataService;
    ExternalPriceModelService externalPriceModelService;

    private String token;
    private String tokenIntern;
    protected ServiceLocator sl = new ServiceLocator();

    protected BaseBean() {
        tokenIntern = String.valueOf(Math.random());
    }

    public void setServiceLocator(ServiceLocator sl) {
        this.sl = sl;
    }

    /**
     * 
     */
    public String getValueOf(String page) {
        try {
            Field field = BaseBean.class.getDeclaredField(page);
            if (String.class.getClass().isInstance(field.getType())) {
                return (String) field.get(null);
            }
        } catch (Exception e) {
            logger.logError(LogMessageIdentifier.WARN_NO_VALUE_FOUND, page);
        }
        return null;
    }

    /**
     * @return the Marketplace service
     */
    protected MarketplaceService getMarketplaceService() {
        marketplaceService = getService(MarketplaceService.class,
                marketplaceService);
        return marketplaceService;
    }

    /**
     * Returns the marketplace management service that contains performance
     * optimized methods.
     */
    protected MarketplaceServiceInternal getMarketplaceServiceInternal() {
        marketplaceServiceInternal = getService(
                MarketplaceServiceInternal.class, marketplaceServiceInternal);
        return marketplaceServiceInternal;
    }

    /**
     * @return the billing service
     */
    protected BillingService getBillingService() {
        billingService = getService(BillingService.class, billingService);
        return billingService;
    }

    /**
     * Get the id management service
     * 
     * @return the id management service
     */
    protected IdentityService getIdService() {
        idService = getService(IdentityService.class, idService);
        return idService;
    }

    protected UserGroupService getUserGroupService() {
        userGroupService = getService(UserGroupService.class, userGroupService);
        return userGroupService;
    }

    protected OperationRecordService getOperationRecordService() {
        operationRecordService = getService(OperationRecordService.class,
                operationRecordService);
        return operationRecordService;
    }

    /**
     * Get the id management service
     * 
     * @return the id management service
     */
    protected UserService getUserService() {
        userService = getService(UserService.class, userService);
        return userService;
    }

    /**
     * Get the subscription management service
     * 
     * @return the subscription management service
     */
    protected SubscriptionService getSubscriptionService() {
        subscriptionService = getService(SubscriptionService.class,
                subscriptionService);
        return subscriptionService;
    }

    /**
     * Get the subscriptions management service
     * 
     * @return the subscription management service
     */
    protected SubscriptionsService getSubscriptionsService() {
        subscriptionsService = getService(SubscriptionsService.class,
                subscriptionsService);
        return subscriptionsService;
    }

    /**
     * Returns the subscription management service that contains performance
     * optimized methods.
     */
    protected SubscriptionServiceInternal getSubscriptionServiceInternal() {
        subscriptionServiceInternal = getService(
                SubscriptionServiceInternal.class, subscriptionServiceInternal);
        return subscriptionServiceInternal;
    }

    /**
     * Get the service provisioning service
     * 
     * @return the service provisioning service
     */
    protected ServiceProvisioningService getProvisioningService() {
        provisioningService = getService(ServiceProvisioningService.class,
                provisioningService);
        return provisioningService;
    }

    /**
     * Returns the service provisioning service that contains performance
     * optimized methods.
     */
    protected ServiceProvisioningServiceInternal getProvisioningServiceInternal() {
        provisioningServiceInternal = getService(
                ServiceProvisioningServiceInternal.class,
                provisioningServiceInternal);
        return provisioningServiceInternal;
    }

    public ExternalPriceModelService getExternalPriceModelService() {
        externalPriceModelService = getService(ExternalPriceModelService.class,
                externalPriceModelService);
        return externalPriceModelService;
    }

    /**
     * Get the Search service
     * 
     * @return the Search service
     */
    protected SearchService getSearchService() {
        searchService = getService(SearchService.class, searchService);
        return searchService;
    }

    /**
     * Returns the search service that contains performance optimized methods.
     */
    protected SearchServiceInternal getSearchServiceInternal() {
        searchServiceInternal = getService(SearchServiceInternal.class,
                searchServiceInternal);
        return searchServiceInternal;
    }

    /**
     * Get the tag service.
     * 
     * @return the tag service
     */
    protected TagService getTagService() {
        tagService = getService(TagService.class, tagService);
        return tagService;
    }

    /**
     * Get the configuration service
     * 
     * @return the configuration service
     */
    protected ConfigurationService getConfigurationService() {
        configurationService = getService(ConfigurationService.class,
                configurationService);
        return configurationService;
    }

    /**
     * Get the discount service
     * 
     * @return the discount service
     */
    protected DiscountService getDiscountService() {
        discountService = getService(DiscountService.class, discountService);
        return discountService;
    }

    /**
     * Get the trigger definition service
     * 
     * @return the trigger definition service
     */
    protected TriggerDefinitionService getTriggerDefinitionService() {
        triggerDefinitionService = getService(TriggerDefinitionService.class,
                triggerDefinitionService);
        return triggerDefinitionService;
    }

    /**
     * Get the trigger service
     * 
     * @return the trigger service
     */
    protected TriggerService getTriggerService() {
        triggerService = getService(TriggerService.class, triggerService);
        return triggerService;
    }

    /**
     * Gets the payment processing service
     * 
     * @return the payment processing service
     */
    protected PaymentService getPaymentProcessingService() {
        paymentProcessingService = getService(PaymentService.class,
                paymentProcessingService);
        return paymentProcessingService;
    }

    /**
     * Gets the review service
     * 
     * @return the review service
     */
    protected ReviewInternalService getReviewService() {
        reviewInternalService = getService(ReviewInternalService.class,
                reviewInternalService);
        return reviewInternalService;
    }

    /**
     * Get the accounting management service
     * 
     * @return the accounting management service
     */
    protected AccountService getAccountingService() {
        accountingService = getService(AccountService.class, accountingService);
        return accountingService;
    }

    /**
     * Returns the password recovery service.
     */
    protected PasswordRecoveryService getPasswordRecoveryService() {
        passwordRecoveryService = getService(PasswordRecoveryService.class,
                passwordRecoveryService);
        return passwordRecoveryService;
    }

    /**
     * Returns the account service that contains performance optimized methods.
     */
    protected ServiceProvisioningServiceLocalizationLocal getServiceProvisioningServiceLocalizationLocal() {
        serviceProvisioningServiceLocalizationLocal = getService(
                ServiceProvisioningServiceLocalizationLocal.class,
                serviceProvisioningServiceLocalizationLocal);
        return serviceProvisioningServiceLocalizationLocal;
    }

    /**
     * Get the reporting service
     * 
     * @return the reporting service
     */
    protected ReportingService getReportingService() {
        reportingService = getService(ReportingService.class, reportingService);
        return reportingService;
    }

    /**
     * Get the session management service
     * 
     * @return the session management service
     */
    protected SessionService getSessionService() {
        sessionService = getService(SessionService.class, sessionService);
        return sessionService;
    }

    /**
     * Get the categorization service
     * 
     * @return the categorization service
     */
    protected CategorizationService getCategorizationService() {
        categorizationService = getService(CategorizationService.class,
                categorizationService);
        return categorizationService;
    }

    protected LandingpageConfigurationService getLandingpageService() {
        landingpageService = getService(LandingpageConfigurationService.class,
                landingpageService);
        return landingpageService;
    }

    protected SubscriptionDetailsService getSubscriptionDetailsService() {
        subscriptionDetailsService = getService(
                SubscriptionDetailsService.class, subscriptionDetailsService);
        return subscriptionDetailsService;
    }

    protected LandingpageService getShowLandingpage() {
        showLandingpageService = getService(LandingpageService.class,
                showLandingpageService);
        return showLandingpageService;
    }

    protected AccountServiceManagement getAccountServiceManagement() {
        accountServiceManagement = getService(AccountServiceManagement.class,
                accountServiceManagement);
        return accountServiceManagement;
    }

    protected ManageLanguageService getManageLanguageService() {
        manageLanguageService = getService(ManageLanguageService.class,
                manageLanguageService);
        return manageLanguageService;
    }

    protected LocalizedDataService getLocalizedDataService() {
        localizedDataService = getService(LocalizedDataService.class,
                localizedDataService);
        return localizedDataService;
    }

    /**
     * Return the given service object if it is not null. Otherwise a new
     * service object is looked up and returned. If the local variable
     * useMockService is set to true the MockService will be returned.
     * 
     * @param <T>
     *            the service interface
     * @param clazz
     *            the class of the service interface
     * @param service
     *            if not null this service is returned
     * @return the given service object if it is not null. Otherwise a new
     *         service object is looked up and returned
     */
    protected <T> T getService(final Class<T> clazz, Object service) {
        if (service == null) {
            service = sl.findService(clazz);
        }
        return clazz.cast(service);
    }

    /**
     * Gets the current HTTP servlet request from the faces context. This method
     * may be stubbed.
     * 
     * @return the current HTTP servlet request
     */
    protected HttpServletRequest getRequest() {
        return getRequestStatic();
    }

    /**
     * Gets the current HTTP servlet request from the faces context.
     * 
     * @return the current HTTP servlet request
     */
    protected static HttpServletRequest getRequestStatic() {
        return (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
    }

    /**
     * Gets the current HTTP servlet response from the faces context.
     * 
     * @return the current HTTP servlet response
     */
    protected HttpServletResponse getResponse() {
        return (HttpServletResponse) getFacesContext().getExternalContext()
                .getResponse();
    }

    protected HttpSession getSession() {
        return (HttpSession) getFacesContext().getExternalContext().getSession(
                false);
    }

    /**
     * Retrieves the attribute with the given key.
     * 
     * @param key
     *            lookup key
     * @return the attribute value
     */
    protected Object getSessionAttribute(String key) {
        HttpSession session = getSession();
        if (session != null) {
            return session.getAttribute(key);
        }
        return null;
    }

    /**
     * Adds the given attribute with the given key to the session.
     * 
     * @param key
     *            lookup key
     * @param value
     *            attribute value to store
     */
    protected void setSessionAttribute(String key, Object value) {
        getSession().setAttribute(key, value);
    }

    /**
     * Returns the request parameter for the given key.
     * 
     * @param key
     *            parameter name
     * @return the request parameter value
     */
    protected String getRequestParameter(String key) {
        return getRequest().getParameter(key);
    }

    /**
     * Get the current user from the session.
     * 
     * @return the current user from the session.
     */
    public User getUserFromSession() {
        return getUserFromSession(getFacesContext());
    }

    /**
     * Get the current user from the session of the provided FacesContext.
     * 
     * @param facesContext
     *            the FacesContext to get the session from
     * @return the user details
     */
    public static User getUserFromSession(FacesContext facesContext) {
        VOUserDetails voUserDetails = getUserFromSessionWithoutException(facesContext);
        if (voUserDetails == null) {
            HttpServletRequest request = (HttpServletRequest) facesContext
                    .getExternalContext().getRequest();
            request.getSession().invalidate();
            SaaSSystemException se = new SaaSSystemException("Invalid session!");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    se,
                    LogMessageIdentifier.ERROR_USER_VALUE_OBJECT_MISSING_IN_SESSION);
            throw se;
        }
        return new User(voUserDetails);
    }

    /**
     * Get the current user from the session without throwing an exception if
     * the user is not found in the session.
     * 
     * @return the current user from the session, or <code>null</code> if no
     *         user is found in the session.
     */
    public VOUserDetails getUserFromSessionWithoutException() {
        return getUserFromSessionWithoutException(FacesContext
                .getCurrentInstance());
    }

    public static VOUserDetails getUserFromSessionWithoutException(
            FacesContext facesContext) {
        HttpServletRequest request = (HttpServletRequest) facesContext
                .getExternalContext().getRequest();
        return (VOUserDetails) request.getSession().getAttribute(
                Constants.SESS_ATTR_USER);
    }

    /**
     * Store a new user value object in the session (e.g. after an update of the
     * current user).
     * 
     * @param voUserDetails
     *            User details.
     */
    protected void setUserInSession(final VOUserDetails voUserDetails) {
        if (voUserDetails == null) {
            throw new SaaSSystemException("voUSerDetails must not be null!");
        }
        HttpServletRequest request = getRequest();
        request.getSession().setAttribute(Constants.SESS_ATTR_USER,
                voUserDetails);
    }

    /**
     * Check whether current userRoles in the session and in EJB session context
     * are the same or not?
     * 
     * @return true if the current user role is changed
     */
    public boolean isCurrentUserRolesChanged() {
        Set<UserRoleType> userRoles = getIdService().getCurrentUserDetails()
                .getUserRoles();
        Set<UserRoleType> currentUserRoles = getUserFromSessionWithoutException()
                .getUserRoles();
        if (userRoles.size() != currentUserRoles.size()) {
            return true;
        } else {
            userRoles.removeAll(currentUserRoles);
            return !userRoles.isEmpty();
        }
    }

    /**
     * Set the user details in the session that belongs to the currently logged
     * in user.
     */
    protected void updateUserInSession() {
        setUserInSession(getIdService().getCurrentUserDetails());
    }

    /**
     * Check if the current user is a OrganizationAdmin
     * 
     * @return true if the current user is a OrganizationAdmin
     */
    protected boolean getIsOrganizationAdmin() {
        User user = getUserFromSession();
        return user != null && user.isOrganizationAdmin();
    }

    /**
     * Append a FacesMessage to the current faces context. The FacesMessage is
     * added to the set of messages associated with the specified client
     * identifier, if clientId is not null. If clientId is null, this
     * FacesMessage is assumed to not be associated with any specific component
     * instance.
     * 
     * @param clientId
     *            - the id of the client component to which the message is
     *            associated. If null the message is not associated with any
     *            specific component.
     * 
     * @param severity
     *            - the message severity.
     * 
     * @param key
     *            - the key of the message in the resource bundle.
     * 
     * @param params
     *            - option parameters of the message.
     */
    protected void addMessage(final String clientId,
            final FacesMessage.Severity severity, final String key,
            final Object[] params) {
        JSFUtils.addMessage(clientId, severity, key, params);
    }

    /**
     * Append a FacesMessage with one parameter to the current faces context.
     * The FacesMessage is added to the set of messages associated with the
     * specified client identifier, if clientId is not null. If clientId is
     * null, this FacesMessage is assumed to not be associated with any specific
     * component instance.
     * 
     * @param clientId
     *            - the id of the client component to which the message is
     *            associated. If null the message is not associated with any
     *            specific component.
     * 
     * @param severity
     *            - the message severity.
     * 
     * @param key
     *            - the key of the message in the resource bundle.
     * 
     * @param param
     *            - parameter of the message.
     */
    protected void addMessage(final String clientId,
            final FacesMessage.Severity severity, final String key,
            final String param) {
        addMessage(clientId, severity, key, new Object[] { param });
    }

    /**
     * Append a FacesMessage without parameter to the current faces context. The
     * FacesMessage is added to the set of messages associated with the
     * specified client identifier, if clientId is not null. If clientId is
     * null, this FacesMessage is assumed to not be associated with any specific
     * component instance.
     * 
     * @param clientId
     *            - the id of the client component to which the message is
     *            associated. If null the message is not associated with any
     *            specific component.
     * 
     * @param severity
     *            - the message severity.
     * 
     * @param key
     *            - the key of the message in the resource bundle.
     */
    protected void addMessage(final String clientId,
            final FacesMessage.Severity severity, final String key) {
        addMessage(clientId, severity, key, (Object[]) null);
    }

    protected void addInfoOrProgressMessage(boolean info, String infoKey,
            String infoParam) {
        if (info) {
            addMessage(null, FacesMessage.SEVERITY_INFO, infoKey, infoParam);
        } else {
            addMessage(PROGRESS_PANEL, FacesMessage.SEVERITY_INFO,
                    PROGRESS_DEFAULT, (String) null);
        }
    }

    /**
     * Checks if a String contains only whitespace is empty ("") or null.
     * 
     * @param str
     *            the String to check, may be null
     * @return true if the String is null, empty or whitespace
     */
    protected static boolean isBlank(final String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * Invoke the BeanUtils.copyProperties() method and convert the possible
     * exceptions into SaaSSystemExceptions
     * 
     * @param dest
     *            the destination object
     * @param orig
     *            the source object
     */
    public void copyProperties(final Object dest, final Object orig) {
        try {
            BeanUtils.copyProperties(dest, orig);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new SaaSSystemException(e);
        }
    }

    /**
     * Delegates to
     * {@link JSFUtils#writeContentToResponse(byte[], String, String)}
     */
    protected void writeContentToResponse(byte[] content, String filename,
            String contentType) throws IOException {
        JSFUtils.writeContentToResponse(content, filename, contentType);
    }

    /**
     * Delegates to
     * {@link JSFUtils#writeContentToResponse(byte[], String, String, FacesContext)}
     */
    protected void writeContentToResponse(byte[] content, String filename,
            String contentType, FacesContext fc) throws IOException {
        JSFUtils.writeContentToResponse(content, filename, contentType, fc);
    }

    FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    protected void setMarketplaceId(String marketplaceId) {
        setMarketplaceIdStatic(marketplaceId);
    }

    public static void setMarketplaceIdStatic(String marketplaceId) {
        HttpServletRequest request = getRequestStatic();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(Constants.REQ_PARAM_MARKETPLACE_ID,
                    marketplaceId);
        }
        request.setAttribute(Constants.REQ_PARAM_MARKETPLACE_ID, marketplaceId);
    }

    /**
     * Determines the currently used marketplace ID.
     * 
     * @return the currently used marketplace ID.
     */
    protected String getMarketplaceId() {
        return getMarketplaceIdStatic();
    }

    public static String getMarketplaceIdStatic() {
        HttpServletRequest request = getRequestStatic();
        String marketplaceId = (String) request
                .getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID);
        if (isBlank(marketplaceId)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                marketplaceId = (String) session
                        .getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID);
            }
        }
        return marketplaceId;
    }

    protected boolean isMarketplaceSet() {
        String marketplaceId = getMarketplaceId();
        return marketplaceId != null && marketplaceId.trim().length() > 0;
    }

    protected boolean isMarketplaceSet(HttpServletRequest httpRequest) {
        return httpRequest.getServletPath().startsWith(
                Marketplace.MARKETPLACE_ROOT);
    }

    public boolean isLoggedInAndAdmin() {
        VOUserDetails user = getUserFromSessionWithoutException(FacesContext
                .getCurrentInstance());
        return user != null && user.hasAdminRole();
    }

    protected boolean isLoggedInAndMarketplaceOwner() {
        VOUserDetails user = getUserFromSessionWithoutException(FacesContext
                .getCurrentInstance());
        return user != null
                && user.getUserRoles().contains(UserRoleType.MARKETPLACE_OWNER);
    }

    protected boolean isLoggedInAndVendorManager() {
        VOUserDetails user = getUserFromSessionWithoutException(getFacesContext());
        return user != null
                && (user.getUserRoles().contains(UserRoleType.SERVICE_MANAGER)
                        || user.getUserRoles().contains(
                                UserRoleType.BROKER_MANAGER) || user
                        .getUserRoles().contains(UserRoleType.RESELLER_MANAGER));
    }

    protected boolean isLoggedInAndPlatformOperator() {
        VOUserDetails user = ui.getUserFromSessionWithoutException();
        return user != null
                && user.getUserRoles().contains(UserRoleType.PLATFORM_OPERATOR);
    }

    /**
     * Returns true if current user is logged in as Unit Administrator, but not
     * as a Organization Administrator.
     * 
     * @return true if current user is logged in as Unit Administrator, but not
     *         as a Organization Administrator
     */
    public boolean isLoggedInAndUnitAdmin() {
        VOUserDetails user = getUserFromSessionWithoutException();
        return user != null
                && user.getUserRoles()
                        .contains(UserRoleType.UNIT_ADMINISTRATOR)
                && !user.getUserRoles().contains(
                        UserRoleType.ORGANIZATION_ADMIN);
    }

    protected boolean isLoggedIn() {
        return getUserFromSessionWithoutException(FacesContext
                .getCurrentInstance()) != null;
    }

    /**
     * Return the current user language or browser language if user is not
     * logged in.
     */
    protected String getUserLanguage() {
        FacesContext fc = getFacesContext();
        if (fc == null) {
            return "en";
        }
        VOUserDetails voUserDetails = getUserFromSessionWithoutException(fc);
        if (voUserDetails == null) {
            return fc.getViewRoot().getLocale().getLanguage();
        }
        return voUserDetails.getLocale();
    }

    protected String getServiceDetailsQueryPart(HttpServletRequest request,
            SessionBean sessionBean) {
        String queryPart = "";
        if (request.getServletPath().startsWith(
                Marketplace.MARKETPLACE_ROOT + "/serviceDetails.jsf")) {
            queryPart = getSelectedServiceQueryPart(sessionBean)
                    + getMarketplaceIdQueryPart();
        }
        return queryPart;
    }

    protected String getSelectedServiceQueryPart(SessionBean sessionBean) {
        String selectedService = "";
        ExternalContext extContext = getFacesContext().getExternalContext();
        String charEncoding = extContext.getRequestCharacterEncoding();

        try {
            String name = URLEncoder.encode("selectedServiceKey", charEncoding);
            String value = URLEncoder.encode(String.valueOf(sessionBean
                    .getSelectedServiceKeyForCustomer()), charEncoding);
            selectedService = '?' + name + '=' + value;
        } catch (UnsupportedEncodingException e) {
            extContext.log(getClass().getName()
                    + ".getSelectedServiceQueryPart()", e);

        }
        return selectedService;
    }

    protected String getMarketplaceIdQueryPart() {
        String marketplaceId = "";
        if (!isBlank(getMarketplaceId())) {
            marketplaceId = "&mId=" + getMarketplaceId();
        }
        return marketplaceId;
    }

    /**
     * Tells JSF that it should reset the values of all UIInput children. Which
     * makes JSF to reload the values from the backing beans.<br>
     * <br>
     * Use case: When a validation fails, JSF will not reload the values from
     * the backing beans, to prevent loss of user input. If the reload is
     * wanted, then this method must be explicitly called.
     * 
     * @see JSFUtils#resetUIInputChildren(javax.faces.component.UIComponent)
     */
    protected void resetUIInputChildren() {
        final FacesContext fc = getFacesContext();
        if (fc != null) {
            // JUnit tests
            JSFUtils.resetUIInputChildren(fc.getViewRoot());
        }
    }

    /**
     * Removes the specified components, searching down the UI tree beginning
     * with the view root, from the children lists of their parents, so they are
     * rebuilt and rendered again. This might be necessary after changing the UI
     * model without navigating away from the page.
     * 
     * @param componentIds
     *            the IDs of the components to be reseted
     * @see http://wiki.apache.org/myfaces/ClearInputComponents
     */
    protected void resetUIComponents(Set<String> componentIds) {
        JSFUtils.resetUIComponents(getFacesContext().getViewRoot(),
                componentIds);
    }

    /**
     * Checks is the current user is the marketplace administrator.
     * 
     * @return true is the user is marketplace administrator, otherwise false.
     */
    public boolean isMarketplaceOwner() {
        // Check if a marketplaceOwner and logged in
        if (!isLoggedInAndMarketplaceOwner()) {
            return false;
        }

        // Get all marketplaces of the currently logged in user
        List<VOMarketplace> marketplaces = getMarketplaceService()
                .getMarketplacesOwned();
        if (marketplaces.isEmpty()) {
            return false;
        }

        // Check if the organization of the logged in user is the owner of the
        // current marketplace
        String currentMplId = getMarketplaceId();
        for (VOMarketplace mpl : marketplaces) {
            if (mpl.getMarketplaceId().equals(currentMplId)) {
                return true;
            }
        }
        return false;
    }

    public String getToken() {
        return tokenIntern;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Checks if the internal token matches the one submitted from the form. If
     * the tokens don't match, the action should not be processed.
     * 
     * @return <code>true</code> if internal and submitted token match otherwise
     *         <code>false</code>
     */
    protected boolean isTokenValid() {
        return tokenIntern.equals(token);
    }

    /**
     * Fill the internal token with a new random value after the successful
     * action execution.
     */
    protected void resetToken() {
        tokenIntern = String.valueOf(Math.random());
    }

    /**
     * Returns text translated from resource bundle with given parameters. This
     * non-static variant may be mocked for testing purpose.
     */
    protected String getText(String key, Object[] params) {
        return JSFUtils.getText(key, params);
    }

    /**
     * This method is used as generic binding for all input fields of the
     * billing contact dialog. The binding was introduced in the context of
     * fixing bug #7843. The value of the passed component is will be cleared to
     * make sure that the value-binding (happens in a later phase) can set the
     * values which are eventually shown in the UI.
     * 
     * @param input
     *            the UIinput of the webpage.
     */
    public void setGenericInput(UIInput input) {
        input.setValue(null);
    }

    /**
     * Introduced in the context of fixing bug #7843 The getter is required for
     * formal reasons. It's not necessary to pass back a component, also it's
     * not possible to pass back the component passed into the setter, because
     * the bean has request scope (all private members would be clears the time
     * the getter is called)
     * 
     * @return null
     */
    public UIInput getGenericInput() {
        return null;
    }

    /**
     * Checks if current user is logged in and has Subscription Manager role
     * assigned.
     * 
     * @return true if current user is logged in and has Subscription Manager
     *         role assigned
     */
    public boolean isLoggedInAndSubscriptionManager() {
        VOUserDetails user = this.getUserFromSessionWithoutException();
        return user != null
                && user.getUserRoles().contains(
                        UserRoleType.SUBSCRIPTION_MANAGER);
    }

    /**
     * Checks if current user is logged in and allowed to subscribe
     * 
     * @return true if user is organization admin or subscription manager or
     *         unit administrator, otherwise false.
     */
    public boolean isLoggedInAndAllowedToSubscribe() {
        VOUserDetails user = this.getUserFromSessionWithoutException();
        return user != null
                && (user.getUserRoles().contains(
                        UserRoleType.SUBSCRIPTION_MANAGER)
                        || user.getUserRoles().contains(
                                UserRoleType.ORGANIZATION_ADMIN) || user
                        .getUserRoles().contains(
                                UserRoleType.UNIT_ADMINISTRATOR));
    }

    /**
     * Checks if current user has access to admin portal
     * 
     * @return true if user is organization admin or subscription manager or
     *         technology manager, otherwise false.
     */
    public boolean isAdministrationAccess() {
        VOUserDetails user = this.getUserFromSessionWithoutException();
        return user != null
                && (user.getUserRoles().contains(UserRoleType.SERVICE_MANAGER)
                        || user.getUserRoles().contains(
                                UserRoleType.TECHNOLOGY_MANAGER) || user
                        .getUserRoles()
                        .contains(UserRoleType.MARKETPLACE_OWNER))
                || user.getUserRoles().contains(UserRoleType.BROKER_MANAGER)
                || user.getUserRoles().contains(UserRoleType.RESELLER_MANAGER)
                || user.getUserRoles().contains(UserRoleType.PLATFORM_OPERATOR);
    }

    /**
     * Returns the resource bundle for the marketplace the user is logged in.
     * 
     * @return the bundle according to the given locale
     */
    public ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle(DbMessages.class.getName(), locale,
                Thread.currentThread().getContextClassLoader());
    }

    /**
     * Reset all resource bundles
     */
    public void resetBundles() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Iterator<Locale> it = fc.getApplication().getSupportedLocales();
        while (it.hasNext()) {
            Locale locale = it.next();
            ResourceBundle bundle = getResourceBundle(locale);
            if (bundle instanceof DbMessages) {
                ((DbMessages) bundle).resetProperties();
            }
        }
    }

}
