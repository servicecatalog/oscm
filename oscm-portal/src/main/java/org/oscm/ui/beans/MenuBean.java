/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: hoffmann                                                      
 *                                                                              
 *  Creation Date: 21.10.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans;

import static org.oscm.ui.authorization.Conditions.and;
import static org.oscm.ui.authorization.Conditions.not;
import static org.oscm.ui.authorization.Conditions.or;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.ui.authorization.Condition;
import org.oscm.ui.authorization.Conditions;
import org.oscm.ui.authorization.Conditions.Cache;
import org.oscm.ui.authorization.UIStatus;
import org.oscm.ui.menu.MenuBuilder;
import org.oscm.ui.menu.MenuGroup;
import org.oscm.ui.menu.MenuGroupBuilder;
import org.oscm.ui.model.User;

/**
 * Bean keeping and managing the main menu.
 * 
 * @author hoffmann
 */
@SessionScoped
@ManagedBean(name = "menuBean")
public class MenuBean extends BaseBean implements UIStatus, Serializable {

    private static final long serialVersionUID = 4827014270214092749L;

    public static final String LINK_SUBSCRIPTION_PAYMENT = "/subscription/payment.jsf";
    public final static String LINK_PROFILE_EDIT = "/organization/edit.jsf";
    public static final String LINK_SERVICE_EDIT = "/service/edit.jsf";
    public final static String LINK_SERVICE_ADD = "/service/add.jsf";
    public final static String LINK_SERVICE_VIEW = "/service/view.jsf";
    public final static String LINK_SERVICE_MANAGE = "/service/manage.jsf";
    public static final String LINK_SUBSCRIPTION_UPGRADE = "/subscription/upgrade.jsf";
    public static final String LINK_SUBSCRIPTION_USERS = "/subscription/users.jsf";
    public static final String LINK_TECHSERVICE_EDIT = "/techservice/edit.jsf";
    public final static String LINK_TECHSERVICE_IMPORT = "/techservice/import.jsf";
    public final static String LINK_MANAGE_CONFIGURATION = "/operator/manageConfiguration.jsf";
    public final static String LINK_MARKETPLACE = "/marketplace/index.jsf";
    public final static String LINK_GOTO_MARKETPLACE = "/shop/gotoMarketplace.jsf";
    public static final String LINK_USER_IMPORT = "/user/import.jsf";
    public static final String LINK_USER_MANAGE_BILLING = "/user/manageBilling.jsf";
    public static final String LINK_OPERATOR_MANAGE_USERS = "/operator/manageUsers.jsf";
    public static final String LINK_OPERATOR_CREATE_ORGANIZATION = "/operator/createOrganization.jsf";
    public static final String LINK_OPERATOR_CREATE_PSP = "/operator/createPSP.jsf";
    public static final String LINK_OPERATOR_MANAGE_ORGANIZATIONS = "/operator/manageOrganizations.jsf";
    public static final String LINK_OPERATOR_MANAGE_OPERATOR_REVENUE_SHARE = "/operator/manageOperatorRevenueShare.jsf";
    public static final String LINK_OPERATOR_MANAGE_TIMERS = "/operator/manageTimers.jsf";
    public static final String LINK_OPERATOR_MANAGE_PSPS = "/operator/managePSPs.jsf";
    public static final String LINK_OPERATOR_MANAGE_CURRENCIES = "/operator/manageCurrencies.jsf";
    public static final String LINK_OPERATOR_MANAGE_LDAP_SETTINGS = "/operator/manageLdapSettings.jsf";
    public static final String LINK_OPERATOR_MANAGE_BILLING_ADAPTERS = "/operator/manageBillingAdapters.jsf";
    public static final String LINK_OPERATOR_EXPORT_BILLING_DATA = "/operator/exportBillingData.jsf";
    public static final String LINK_OPERATOR_EXECUTE_BILLING_TASKS = "/operator/executeBillingTasks.jsf";
    public static final String LINK_OPERATOR_EXPORT_AUDIT_LOG_DATA = "/operator/exportAuditLogData.jsf";
    public static final String LINK_OPERATOR_MANAGE_LANGUAGES = "/operator/manageLanguages.jsf";
    public static final String LINK_OPERATOR_MANAGE_TENANTS = "/operator/manageTenants.jsf";
    public static final String LINK_OPERATOR_MANAGE_INDEXES = "/operator/manageIndexes.jsf";
    public static final String LINK_ORGANIZATION_ADD_CUSTOMER = "/organization/addCustomer.jsf";
    public static final String LINK_ORGANIZATION_EDIT_CUSTOMER = "/organization/editCustomer.jsf";
    public static final String LINK_ORGANIZATION_VIEW_CUSTOMER = "/organization/viewCustomer.jsf";
    public static final String LINK_ORGANIZATION_MANAGE_PAYMENT_ENABLEMENT = "/organization/managePaymentEnablement.jsf";
    public static final String LINK_ORGANIZATION_MANAGE_VATS = "/organization/manageVats.jsf";
    public static final String LINK_SUBSCRIPTION_VIEW = "/subscription/view.jsf";
    public static final String LINK_SUBSCRIPTION_EDIT_UDAS = "/subscription/editUdas.jsf";
    public static final String LINK_SUBSCRIPTION_TERMINATE = "/subscription/terminate.jsf";
    public static final String LINK_TECHSERVICE_ADD = "/techservice/add.jsf";
    public static final String LINK_TECHSERVICE_EXPORT = "/techservice/export.jsf";
    public static final String LINK_TECHSERVICE_DELETE = "/techservice/delete.jsf";
    public static final String LINK_TECHSERVICE_VIEW_ADAPTERS = "/techservice/viewBillingAdapters.jsf";
    public static final String LINK_SERVICE_COPY = "/service/copy.jsf";
    public static final String LINK_SERVICE_DELETE = "/service/delete.jsf";
    public static final String LINK_SERVICE_UPGRADEOPTIONS = "/service/upgradeoptions.jsf";
    public static final String LINK_SERVICE_PUBLISH = "/service/publish.jsf";
    public static final String LINK_SERVICE_ACTIVATION = "/service/activation.jsf";
    public static final String LINK_PRICE_MODEL_SERVICE_PRICE_MODEL = "/priceModel/servicePriceModel.jsf";
    public static final String LINK_PRICE_MODEL_CUSTOMER_PRICE_MODEL = "/priceModel/customerPriceModel.jsf";
    public static final String LINK_PRICE_MODEL_CUSTOMER_PRICE_MODEL_DELETE = "/priceModel/customerPriceModelDelete.jsf";
    public static final String LINK_PRICE_MODEL_SUBSCRIPTION_PRICE_MODEL = "/priceModel/subscriptionPriceModel.jsf";
    public static final String LINK_SHOP_MANAGE_SUPPLIERS = "/shop/manageSuppliers.jsf";
    public static final String LINK_SHOP_MANAGE_CATEGORIES = "/shop/manageCategories.jsf";
    public static final String LINK_SHOP_MANAGE_ACCESS = "/shop/manageAccess.jsf";
    public static final String LINK_SHOP_CREATE_MARKETPLACE = "/shop/createMarketplace.jsf";
    public static final String LINK_SHOP_UPDATE_MARKETPLACE = "/shop/updateMarketplace.jsf";
    public static final String LINK_SHOP_DELETE_MARKETPLACE = "/shop/deleteMarketplace.jsf";
    public static final String LINK_SHOP_MANAGE_BROKER_REVENUE_SHARE = "/shop/manageBrokerRevenueShare.jsf";
    public static final String LINK_SHOP_MANAGE_RESELLER_REVENUE_SHARE = "/shop/manageResellerRevenueShare.jsf";
    public static final String LINK_SHOP_TRACKING_CODE = "/shop/trackingCode.jsf";
    public static final String LINK_SHOP_CUSTOMIZE_LANDINGPAGE = "/shop/customizeLandingpage.jsf";
    public static final String LINK_SHOP_EDIT_STAGE = "/shop/editStage.jsf";
    public static final String LINK_SHOP_TRANSLATIONS = "/shop/translations.jsf";
    public static final String LINK_SHOP_CUSTOMIZE_BRAND = "/shop/customizeBrand.jsf";
    public static final String LINK_USER_EXPORT_BILLING_DATA = "/user/exportBillingData.jsf";
    public static final String LINK_ORGANIZATION_MANAGE_UDAS = "/organization/manageUdas.jsf";
    public static final String LINK_ORGANIZATION_MANAGE_SUPPLIERS = "/organization/manageSuppliers.jsf";
    public static final String LINK_TRIGGERPROCESS_LIST = "/triggerprocess/list.jsf";
    public static final String LINK_ORGANIZATION_MANAGE_TRIGGERS = "/organization/manageTriggers.jsf";
    public static final String LINK_ORGANIZATION_REPORTING = "/organization/reporting.jsf";
    public static final String LINK_ORGANIZATION_MANAGE_LDAP_SETTINGS = "/organization/manageLdapSettings.jsf";
    public static final String LINK_USER_LIST = "/user/list.jsf";
    public static final String LINK_USER_ADD = "/user/add.jsf";
    public static final String LINK_USER_PWD = "/user/pwd.jsf";
    /**
     * Link to the empty default page - the initial page. When this page is
     * called, the default page should be retrieved.
     */
    public static final String LINK_DEFAULT = "/default.jsf";

    @ManagedProperty(value = "#{appBean}")
    private ApplicationBean applicationBean;

    private final Cache visibilityCache = new Cache();

    private String currentLink;

    private MenuBuilder mainMenu;

    /**
     * @return main menu structure
     */
    public MenuGroup getMainMenu() {
        if (mainMenu == null) {
            mainMenu = createMainMenu();
        }
        return mainMenu;
    }

    public void setCurrentPageLink(String link) {
        currentLink = link;
    }

    @Override
    public String getCurrentPageLink() {
        if (currentLink == null) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestServletPath();
        }
        return currentLink;
    }

    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    @Override
    public boolean isHidden(String id) {
        return applicationBean.isUIElementHidden(id);
    }

    /**
     * Called from the navigation facelet
     * 
     * @return null
     */
    public String getResetCurrentPageLink() {
        currentLink = null;
        return null;
    }

    /**
     * Clears the cache for pre-calculated menu visibility. Must be explicitly
     * called when a condition has changed.
     */
    public void resetMenuVisibility() {
        visibilityCache.reset();
    }

    /**
     * Toggles the expanded state of the menu group with the given id.
     * 
     * @param id
     *            menu group id
     */
    public void setToggleGroupExpanded(String id) {
        if (mainMenu != null) {
            mainMenu.toggleGroupExpanded(id);
        }
    }

    private MenuBuilder createMainMenu() {
        MenuBuilder main = new MenuBuilder(this);

        // Predefined Visibility Checks:

        final Condition ADMIN = visibilityCache.get(new Condition() {
            @Override
            public boolean eval() {
                return getUserFromSession().isOrganizationAdmin();
            }
        });

        final Condition REPORT_AVAILABLE = new Condition() {
            @Override
            public boolean eval() {
                return getApplicationBean().isReportingAvailable();
            }
        };

        final Condition PAYMENTINFO_AVAILABLE = new Condition() {
            @Override
            public boolean eval() {
                return getApplicationBean().isPaymentInfoAvailable();
            }
        };

        final Condition INTERNAL_AUTH_MODE = new Condition() {
            @Override
            public boolean eval() {
                return getApplicationBean().isInternalAuthMode();
            }
        };

        final Condition SAML_SP_AUTH_MODE = new Condition() {
            @Override
            public boolean eval() {
                return !getApplicationBean().isInternalAuthMode();
            }
        };

        final Condition CALLED_BY_KEY_1000 = new Condition() {
            @Override
            public boolean eval() {
                User u = getUserFromSession();
                return (u.getKey() == 1000L);
            }
        };

        final Condition CUSTOMER = visibilityCache.get(new Condition() {
            @Override
            public boolean eval() {
                return getUserFromSession().isCustomer();
            }
        });

        final Condition SERVICE_MANAGER = visibilityCache.get(new Condition() {
            @Override
            public boolean eval() {
                return getUserFromSession().isServiceManager();
            }
        });

        final Condition BROKER_MANAGER = visibilityCache.get(new Condition() {
            @Override
            public boolean eval() {
                return getUserFromSession().isBrokerManager();
            }
        });

        final Condition RESELLER_MANAGER = visibilityCache.get(new Condition() {
            @Override
            public boolean eval() {
                return getUserFromSession().isResellerManager();
            }
        });

        final Condition MARKETPLACE_OWNER = visibilityCache
                .get(new Condition() {
                    @Override
                    public boolean eval() {
                        return getUserFromSession().isMarketplaceOwner();
                    }
                });

        final Condition TECHNOLOGY_MANAGER = visibilityCache
                .get(new Condition() {
                    @Override
                    public boolean eval() {
                        return getUserFromSession().isTechnologyManager();
                    }
                });

        final Condition LDAP = visibilityCache.get(new Condition() {
            @Override
            public boolean eval() {
                return getUserFromSession().isRemoteLdapActive();
            }
        });

        final Condition TRIGGERS = visibilityCache.get(new Condition() {

            @Override
            public boolean eval() {
                final TriggerService service = getTriggerService();
                return !service.getAllDefinitions().isEmpty();
            }
        });

        final Condition OPERATOR = new Condition() {
            @Override
            public boolean eval() {
                return getUserFromSession().isPlatformOperator();
            }
        };

        final Condition GOTO_MARKETPLACE = visibilityCache
                .get(Conditions.or(BROKER_MANAGER, RESELLER_MANAGER,
                        MARKETPLACE_OWNER, SERVICE_MANAGER));

        // === TOP-LEVEL LINKS =================================================
        main.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_LINK,
                LINK_GOTO_MARKETPLACE, GOTO_MARKETPLACE);

        // === ACCOUNT =========================================================
        MenuGroupBuilder group = main
                .addGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT);
        group.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_EDIT,
                LINK_PROFILE_EDIT);
        group.addItem(HiddenUIConstants.MENU_ITEM_USER_PWD, LINK_USER_PWD,
                not(LDAP), or(INTERNAL_AUTH_MODE,
                        and(SAML_SP_AUTH_MODE, CALLED_BY_KEY_1000)));
        group.addItem(HiddenUIConstants.MENU_ITEM_USER_ADD, LINK_USER_ADD,
                ADMIN, not(LDAP));
        group.addItem(HiddenUIConstants.MENU_ITEM_USER_IMPORT, LINK_USER_IMPORT,
                ADMIN, LDAP);
        group.addItem(HiddenUIConstants.MENU_ITEM_USER_LIST, LINK_USER_LIST,
                ADMIN);
        group.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_LDAP,
                LINK_ORGANIZATION_MANAGE_LDAP_SETTINGS, ADMIN, LDAP);
        group.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_REPORT,
                LINK_ORGANIZATION_REPORTING, ADMIN,
                and(REPORT_AVAILABLE, or(OPERATOR, CUSTOMER, SERVICE_MANAGER,
                        TECHNOLOGY_MANAGER)));
        group.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_TRIGGERS,
                LINK_ORGANIZATION_MANAGE_TRIGGERS, ADMIN);
        group.addItem(HiddenUIConstants.MENU_ITEM_TRIGGER_PROCESS_LIST,
                LINK_TRIGGERPROCESS_LIST, TRIGGERS);
        group.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_SUPPLIERS,
                LINK_ORGANIZATION_MANAGE_SUPPLIERS, TECHNOLOGY_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_UDAS,
                LINK_ORGANIZATION_MANAGE_UDAS, SERVICE_MANAGER);
        group.addItem(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_EXPORT_BILLING_DATA,
                LINK_USER_EXPORT_BILLING_DATA,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER,
                        MARKETPLACE_OWNER, OPERATOR));
        group.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_BILLING,
                LINK_USER_MANAGE_BILLING,
                or(SERVICE_MANAGER, RESELLER_MANAGER));

        // === OPERATOR ========================================================
        group = main.addGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR,
                OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_USERS,
                LINK_OPERATOR_MANAGE_USERS);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_CREATE_ORGANIZATION,
                LINK_OPERATOR_CREATE_ORGANIZATION, OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_CREATE_PSP,
                LINK_OPERATOR_CREATE_PSP, OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_ORGANIZATIONS,
                LINK_OPERATOR_MANAGE_ORGANIZATIONS, OPERATOR);
        group.addItem(
                HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_OPERATOR_REVENUE_SHARE,
                LINK_OPERATOR_MANAGE_OPERATOR_REVENUE_SHARE, OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_TIMERS,
                LINK_OPERATOR_MANAGE_TIMERS);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_PSPS,
                LINK_OPERATOR_MANAGE_PSPS, OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_CURRENCIES,
                LINK_OPERATOR_MANAGE_CURRENCIES, OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_LDAP,
                LINK_OPERATOR_MANAGE_LDAP_SETTINGS, OPERATOR,
                INTERNAL_AUTH_MODE);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_CONFIGURATION,
                LINK_MANAGE_CONFIGURATION, OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_INDEXES,
                LINK_OPERATOR_MANAGE_INDEXES, OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_EXPORT_BILLING_DATA,
                LINK_OPERATOR_EXPORT_BILLING_DATA);
        group.addItem(
                HiddenUIConstants.MENU_ITEM_OPERATOR_EXECUTE_BILLING_TASKS,
                LINK_OPERATOR_EXECUTE_BILLING_TASKS);
        group.addItem(
                HiddenUIConstants.MENU_ITEM_OPERATOR_EXPORT_AUDIT_LOG_DATA,
                LINK_OPERATOR_EXPORT_AUDIT_LOG_DATA);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_LANGUAGES,
                LINK_OPERATOR_MANAGE_LANGUAGES, OPERATOR);
        group.addItem(
                HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_BILLING_ADAPTERS,
                LINK_OPERATOR_MANAGE_BILLING_ADAPTERS, OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_TENANTS,
                LINK_OPERATOR_MANAGE_TENANTS, OPERATOR, SAML_SP_AUTH_MODE);

        // === CUSTOMER ========================================================
        group = main.addGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER));
        group.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_ADD_CUSTOMER,
                LINK_ORGANIZATION_ADD_CUSTOMER,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER));
        group.addItem(HiddenUIConstants.MENU_ITEM_EDIT_CUSTOMER,
                LINK_ORGANIZATION_EDIT_CUSTOMER, SERVICE_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_VIEW_CUSTOMER,
                LINK_ORGANIZATION_VIEW_CUSTOMER,
                or(RESELLER_MANAGER, BROKER_MANAGER));
        group.addItem(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_PAYMENT_ENABLEMENT,
                LINK_ORGANIZATION_MANAGE_PAYMENT_ENABLEMENT,
                and(PAYMENTINFO_AVAILABLE,
                        or(SERVICE_MANAGER, RESELLER_MANAGER)));
        group.addItem(HiddenUIConstants.MENU_ITEM_MANAGE_VAT,
                LINK_ORGANIZATION_MANAGE_VATS, SERVICE_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_SUBSCRIPTION_VIEW,
                LINK_SUBSCRIPTION_VIEW,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER));
        group.addItem(HiddenUIConstants.MENU_ITEM_SUBSCRIPTION_EDIT_UDAS,
                LINK_SUBSCRIPTION_EDIT_UDAS, SERVICE_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_SUBSCRIPTION_TERMINATE,
                LINK_SUBSCRIPTION_TERMINATE,
                or(SERVICE_MANAGER, RESELLER_MANAGER));

        // === TECHNICAL SERVICE ===============================================
        group = main.addGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_TECHSERVICE,
                TECHNOLOGY_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_ADD,
                LINK_TECHSERVICE_ADD);
        group.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_IMPORT,
                LINK_TECHSERVICE_IMPORT);
        group.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_EDIT,
                LINK_TECHSERVICE_EDIT);
        group.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_EXPORT,
                LINK_TECHSERVICE_EXPORT);
        group.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_DELETE,
                LINK_TECHSERVICE_DELETE);
        group.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_VIEW_ADAPTERS,
                LINK_TECHSERVICE_VIEW_ADAPTERS);

        // === SERVICE =========================================================
        group = main.addGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_SERVICE,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER));
        group.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_ADD, LINK_SERVICE_ADD,
                SERVICE_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_EDIT,
                LINK_SERVICE_EDIT, SERVICE_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_COPY,
                LINK_SERVICE_COPY, SERVICE_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_DELETE,
                LINK_SERVICE_DELETE, SERVICE_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_UPGRADE_OPTIONS,
                LINK_SERVICE_UPGRADEOPTIONS, SERVICE_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_VIEW,
                LINK_SERVICE_VIEW, BROKER_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_MANAGE,
                LINK_SERVICE_MANAGE, RESELLER_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_PUBLISH,
                LINK_SERVICE_PUBLISH,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER));
        group.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_ACTIVATION,
                LINK_SERVICE_ACTIVATION,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER));

        // === PRICE MODEL =====================================================
        group = main.addGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_PRICE_MODEL,
                SERVICE_MANAGER);
        group.addItem(HiddenUIConstants.MENU_ITEM_PRICE_MODEL_SERVICE,
                LINK_PRICE_MODEL_SERVICE_PRICE_MODEL);
        group.addItem(HiddenUIConstants.MENU_ITEM_PRICE_MODEL_CUSTOMER,
                LINK_PRICE_MODEL_CUSTOMER_PRICE_MODEL);
        group.addItem(HiddenUIConstants.MENU_ITEM_PRICE_MODEL_DELETE,
                LINK_PRICE_MODEL_CUSTOMER_PRICE_MODEL_DELETE);
        group.addItem(HiddenUIConstants.MENU_ITEM_PRICE_MODEL_SUBSCRIPTION,
                LINK_PRICE_MODEL_SUBSCRIPTION_PRICE_MODEL);

        // === MARKETPLACE =====================================================
        group = main.addGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE,
                or(MARKETPLACE_OWNER, OPERATOR));
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_CATEGORIES,
                LINK_SHOP_MANAGE_CATEGORIES, MARKETPLACE_OWNER);
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_ACCESS,
                LINK_SHOP_MANAGE_ACCESS, MARKETPLACE_OWNER);
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_SUPPLIERS,
                LINK_SHOP_MANAGE_SUPPLIERS, MARKETPLACE_OWNER);
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_CREATE,
                LINK_SHOP_CREATE_MARKETPLACE, OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_UPDATE,
                LINK_SHOP_UPDATE_MARKETPLACE);
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_DELETE,
                LINK_SHOP_DELETE_MARKETPLACE, OPERATOR);
        group.addItem(
                HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_BROKER_REVENUE_SHARE,
                LINK_SHOP_MANAGE_BROKER_REVENUE_SHARE, OPERATOR);
        group.addItem(
                HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_RESELLER_REVENUE_SHARE,
                LINK_SHOP_MANAGE_RESELLER_REVENUE_SHARE, OPERATOR);
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_TRACKINGCODE,
                LINK_SHOP_TRACKING_CODE, MARKETPLACE_OWNER);
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_LANDINGPAGE,
                LINK_SHOP_CUSTOMIZE_LANDINGPAGE, MARKETPLACE_OWNER);
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_EDIT_STAGE,
                LINK_SHOP_EDIT_STAGE, MARKETPLACE_OWNER);
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_TRANSLATION,
                LINK_SHOP_TRANSLATIONS, MARKETPLACE_OWNER);
        group.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_CUSTOMIZE_BRAND,
                LINK_SHOP_CUSTOMIZE_BRAND, MARKETPLACE_OWNER);

        return main;
    }
}
