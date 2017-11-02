/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-5-26                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.authorization;

import static org.oscm.ui.authorization.Conditions.and;
import static org.oscm.ui.authorization.Conditions.not;
import static org.oscm.ui.authorization.Conditions.or;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oscm.types.constants.Configuration;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.common.ServiceAccess;
import org.oscm.ui.model.User;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.internal.types.enumtypes.AuthenticationMode;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOConfigurationSetting;

/**
 * Builder for page authorization
 * 
 * @author gaowenxin
 * 
 */
public class PageAuthorizationBuilder {

    private final List<PageAuthorization> pageAuthorizationList;
    private final ServiceAccess serviceAccess;

    public PageAuthorizationBuilder(ServiceAccess serviceAccess) {
        this.serviceAccess = serviceAccess;
        pageAuthorizationList = new ArrayList<PageAuthorization>();
    }

    private void addItem(final String id, final String link,
            final Condition... visibility) {
        PageAuthorization pageAuthorization = new PageAuthorization(link,
                and(visibility), id, this);
        pageAuthorizationList.add(pageAuthorization);
    }

    private TriggerService getTriggerService() {
        TriggerService triggerService = serviceAccess
                .getService(TriggerService.class);
        return triggerService;

    }

    private ConfigurationService getConfigurationService() {
        ConfigurationService configurationService = serviceAccess
                .getService(ConfigurationService.class);
        return configurationService;

    }

    public Map<String, Boolean> getHiddenUIElements() {
        Map<String, Boolean> hiddenUIElements = new HashMap<String, Boolean>();
        VOConfigurationSetting hiddenUIElementsConf = getConfigurationService()
                .getVOConfigurationSetting(ConfigurationKey.HIDDEN_UI_ELEMENTS,
                        Configuration.GLOBAL_CONTEXT);
        if (hiddenUIElementsConf != null) {
            String strHiddenUIElementsConf = hiddenUIElementsConf.getValue();
            if (strHiddenUIElementsConf != null) {
                String[] results = strHiddenUIElementsConf.split(",");
                for (String str : results) {
                    String trimmedStr = str.trim();
                    if (!trimmedStr.equals("")) {
                        hiddenUIElements.put(trimmedStr, Boolean.FALSE);
                    }
                }
            }
        }
        return hiddenUIElements;
    }

    public List<PageAuthorization> buildPageAuthorizationList(
            final User currentUser) {
        final Condition ADMIN = new Condition() {
            @Override
            public boolean eval() {
                return currentUser.isOrganizationAdmin();
            }
        };

        final Condition REPORT_AVAILABLE = new Condition() {
            @Override
            public boolean eval() {
                VOConfigurationSetting reportEngineUrl = getConfigurationService()
                        .getVOConfigurationSetting(
                                ConfigurationKey.REPORT_ENGINEURL,
                                Configuration.GLOBAL_CONTEXT);
                return reportEngineUrl != null
                        && reportEngineUrl.getValue() != null
                        && reportEngineUrl.getValue().trim().length() > 0;
            }
        };

        final Condition INTERNAL_AUTH_MODE = new Condition() {
            @Override
            public boolean eval() {
                VOConfigurationSetting authMode = getConfigurationService()
                        .getVOConfigurationSetting(ConfigurationKey.AUTH_MODE,
                                Configuration.GLOBAL_CONTEXT);
                return authMode.getValue().equals(
                        AuthenticationMode.INTERNAL.name());
            }
        };

        final Condition CALLED_BY_KEY_1000 = new Condition() {
            @Override
            public boolean eval() {
                User u = currentUser;
                return (u.getKey() == 1000L);
            }
        };

        final Condition CUSTOMER = new Condition() {
            @Override
            public boolean eval() {
                return currentUser.isCustomer();
            }
        };

        final Condition SERVICE_MANAGER = new Condition() {
            @Override
            public boolean eval() {
                return currentUser.isServiceManager();
            }
        };

        final Condition BROKER_MANAGER = new Condition() {
            @Override
            public boolean eval() {
                return currentUser.isBrokerManager();
            }
        };

        final Condition RESELLER_MANAGER = new Condition() {
            @Override
            public boolean eval() {
                return currentUser.isResellerManager();
            }
        };

        final Condition MARKETPLACE_OWNER = new Condition() {
            @Override
            public boolean eval() {
                return currentUser.getUserRoles().contains(
                        UserRoleType.MARKETPLACE_OWNER);

            }
        };

        final Condition TECHNOLOGY_MANAGER = new Condition() {
            @Override
            public boolean eval() {
                return currentUser.isTechnologyManager();
            }
        };

        final Condition LDAP = new Condition() {
            @Override
            public boolean eval() {
                return currentUser.isRemoteLdapActive();
            }
        };

        final Condition TRIGGERS = new Condition() {

            @Override
            public boolean eval() {
                final TriggerService service = getTriggerService();
                return !service.getAllDefinitions().isEmpty();
            }
        };

        final Condition OPERATOR = new Condition() {
            @Override
            public boolean eval() {
                return currentUser.isPlatformOperator();
            }
        };

        final Condition GOTO_MARKETPLACE = Conditions.or(BROKER_MANAGER,
                RESELLER_MANAGER, MARKETPLACE_OWNER, SERVICE_MANAGER);

        // === TOP-LEVEL LINKS =================================================
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_LINK,
                MenuBean.LINK_GOTO_MARKETPLACE, GOTO_MARKETPLACE);

        // === ACCOUNT =========================================================
        this.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_EDIT,
                MenuBean.LINK_PROFILE_EDIT);
        this.addItem(HiddenUIConstants.MENU_ITEM_USER_PWD,
                MenuBean.LINK_USER_PWD, not(LDAP),
                or(INTERNAL_AUTH_MODE, CALLED_BY_KEY_1000));
        this.addItem(HiddenUIConstants.MENU_ITEM_USER_ADD,
                MenuBean.LINK_USER_ADD, ADMIN, not(LDAP));
        this.addItem(HiddenUIConstants.MENU_ITEM_USER_IMPORT,
                MenuBean.LINK_USER_IMPORT, ADMIN, LDAP);
        this.addItem(HiddenUIConstants.MENU_ITEM_USER_LIST,
                MenuBean.LINK_USER_LIST, ADMIN);
        this.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_LDAP,
                MenuBean.LINK_ORGANIZATION_MANAGE_LDAP_SETTINGS, ADMIN, LDAP);
        this.addItem(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_REPORT,
                MenuBean.LINK_ORGANIZATION_REPORTING,
                ADMIN,
                and(REPORT_AVAILABLE,
                        or(OPERATOR, CUSTOMER, SERVICE_MANAGER,
                                TECHNOLOGY_MANAGER)));
        this.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_TRIGGERS,
                MenuBean.LINK_ORGANIZATION_MANAGE_TRIGGERS, ADMIN);
        this.addItem(HiddenUIConstants.MENU_ITEM_TRIGGER_PROCESS_LIST,
                MenuBean.LINK_TRIGGERPROCESS_LIST, TRIGGERS);
        this.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_SUPPLIERS,
                MenuBean.LINK_ORGANIZATION_MANAGE_SUPPLIERS, TECHNOLOGY_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_UDAS,
                MenuBean.LINK_ORGANIZATION_MANAGE_UDAS, SERVICE_MANAGER);
        this.addItem(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_EXPORT_BILLING_DATA,
                MenuBean.LINK_USER_EXPORT_BILLING_DATA,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER,
                        MARKETPLACE_OWNER, OPERATOR));
        this.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_BILLING,
                MenuBean.LINK_USER_MANAGE_BILLING,
                or(SERVICE_MANAGER, RESELLER_MANAGER));

        // === OPERATOR ========================================================
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_USERS,
                MenuBean.LINK_OPERATOR_MANAGE_USERS, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_CREATE_ORGANIZATION,
                MenuBean.LINK_OPERATOR_CREATE_ORGANIZATION, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_CREATE_PSP,
                MenuBean.LINK_OPERATOR_CREATE_PSP, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_ORGANIZATIONS,
                MenuBean.LINK_OPERATOR_MANAGE_ORGANIZATIONS, OPERATOR);
        this.addItem(
                HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_OPERATOR_REVENUE_SHARE,
                MenuBean.LINK_OPERATOR_MANAGE_OPERATOR_REVENUE_SHARE, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_TIMERS,
                MenuBean.LINK_OPERATOR_MANAGE_TIMERS, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_PSPS,
                MenuBean.LINK_OPERATOR_MANAGE_PSPS, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_CURRENCIES,
                MenuBean.LINK_OPERATOR_MANAGE_CURRENCIES, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_LDAP,
                MenuBean.LINK_OPERATOR_MANAGE_LDAP_SETTINGS, OPERATOR,
                INTERNAL_AUTH_MODE);
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_CONFIGURATION,
                MenuBean.LINK_MANAGE_CONFIGURATION, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_EXPORT_BILLING_DATA,
                MenuBean.LINK_OPERATOR_EXPORT_BILLING_DATA, OPERATOR);
        this.addItem(
                HiddenUIConstants.MENU_ITEM_OPERATOR_EXECUTE_BILLING_TASKS,
                MenuBean.LINK_OPERATOR_EXECUTE_BILLING_TASKS, OPERATOR);
        this.addItem(
                HiddenUIConstants.MENU_ITEM_OPERATOR_EXPORT_AUDIT_LOG_DATA,
                MenuBean.LINK_OPERATOR_EXPORT_AUDIT_LOG_DATA, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_LANGUAGES,
                MenuBean.LINK_OPERATOR_MANAGE_LANGUAGES, OPERATOR);

        // === CUSTOMER ========================================================
        this.addItem(HiddenUIConstants.MENU_ITEM_ORGANIZATION_ADD_CUSTOMER,
                MenuBean.LINK_ORGANIZATION_ADD_CUSTOMER,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER));
        this.addItem(HiddenUIConstants.MENU_ITEM_EDIT_CUSTOMER,
                MenuBean.LINK_ORGANIZATION_EDIT_CUSTOMER, SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_VIEW_CUSTOMER,
                MenuBean.LINK_ORGANIZATION_VIEW_CUSTOMER,
                or(RESELLER_MANAGER, BROKER_MANAGER));
        this.addItem(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_PAYMENT_ENABLEMENT,
                MenuBean.LINK_ORGANIZATION_MANAGE_PAYMENT_ENABLEMENT,
                or(SERVICE_MANAGER, RESELLER_MANAGER));
        this.addItem(HiddenUIConstants.MENU_ITEM_MANAGE_VAT,
                MenuBean.LINK_ORGANIZATION_MANAGE_VATS, SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_SUBSCRIPTION_VIEW,
                MenuBean.LINK_SUBSCRIPTION_VIEW,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER));
        this.addItem(HiddenUIConstants.MENU_ITEM_SUBSCRIPTION_EDIT_UDAS,
                MenuBean.LINK_SUBSCRIPTION_EDIT_UDAS, SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_SUBSCRIPTION_TERMINATE,
                MenuBean.LINK_SUBSCRIPTION_TERMINATE,
                or(SERVICE_MANAGER, RESELLER_MANAGER));

        // === TECHNICAL SERVICE ===============================================
        this.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_ADD,
                MenuBean.LINK_TECHSERVICE_ADD, TECHNOLOGY_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_IMPORT,
                MenuBean.LINK_TECHSERVICE_IMPORT, TECHNOLOGY_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_EDIT,
                MenuBean.LINK_TECHSERVICE_EDIT, TECHNOLOGY_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_EXPORT,
                MenuBean.LINK_TECHSERVICE_EXPORT, TECHNOLOGY_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_DELETE,
                MenuBean.LINK_TECHSERVICE_DELETE, TECHNOLOGY_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_TECHSERVICE_VIEW_ADAPTERS,
                MenuBean.LINK_TECHSERVICE_VIEW_ADAPTERS, TECHNOLOGY_MANAGER);
        // === SERVICE =========================================================
        this.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_ADD,
                MenuBean.LINK_SERVICE_ADD, SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_EDIT,
                MenuBean.LINK_SERVICE_EDIT, SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_COPY,
                MenuBean.LINK_SERVICE_COPY, SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_DELETE,
                MenuBean.LINK_SERVICE_DELETE, SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_UPGRADE_OPTIONS,
                MenuBean.LINK_SERVICE_UPGRADEOPTIONS, SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_VIEW,
                MenuBean.LINK_SERVICE_VIEW, BROKER_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_MANAGE,
                MenuBean.LINK_SERVICE_MANAGE, RESELLER_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_PUBLISH,
                MenuBean.LINK_SERVICE_PUBLISH,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER));
        this.addItem(HiddenUIConstants.MENU_ITEM_SERVICE_ACTIVATION,
                MenuBean.LINK_SERVICE_ACTIVATION,
                or(SERVICE_MANAGER, BROKER_MANAGER, RESELLER_MANAGER));

        // === PRICE MODEL =====================================================
        this.addItem(HiddenUIConstants.MENU_ITEM_PRICE_MODEL_SERVICE,
                MenuBean.LINK_PRICE_MODEL_SERVICE_PRICE_MODEL, SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_PRICE_MODEL_CUSTOMER,
                MenuBean.LINK_PRICE_MODEL_CUSTOMER_PRICE_MODEL, SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_PRICE_MODEL_DELETE,
                MenuBean.LINK_PRICE_MODEL_CUSTOMER_PRICE_MODEL_DELETE,
                SERVICE_MANAGER);
        this.addItem(HiddenUIConstants.MENU_ITEM_PRICE_MODEL_SUBSCRIPTION,
                MenuBean.LINK_PRICE_MODEL_SUBSCRIPTION_PRICE_MODEL,
                SERVICE_MANAGER);

        // === MARKETPLACE =====================================================
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_CATEGORIES,
                MenuBean.LINK_SHOP_MANAGE_CATEGORIES, MARKETPLACE_OWNER);
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_ACCESS,
                MenuBean.LINK_SHOP_MANAGE_ACCESS, MARKETPLACE_OWNER);
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_SUPPLIERS,
                MenuBean.LINK_SHOP_MANAGE_SUPPLIERS, MARKETPLACE_OWNER);
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_CREATE,
                MenuBean.LINK_SHOP_CREATE_MARKETPLACE, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_UPDATE,
                MenuBean.LINK_SHOP_UPDATE_MARKETPLACE,
                or(MARKETPLACE_OWNER, OPERATOR));
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_DELETE,
                MenuBean.LINK_SHOP_DELETE_MARKETPLACE, OPERATOR);
        this.addItem(
                HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_BROKER_REVENUE_SHARE,
                MenuBean.LINK_SHOP_MANAGE_BROKER_REVENUE_SHARE, OPERATOR);
        this.addItem(
                HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_RESELLER_REVENUE_SHARE,
                MenuBean.LINK_SHOP_MANAGE_RESELLER_REVENUE_SHARE, OPERATOR);
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_TRACKINGCODE,
                MenuBean.LINK_SHOP_TRACKING_CODE, MARKETPLACE_OWNER);
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_LANDINGPAGE,
                MenuBean.LINK_SHOP_CUSTOMIZE_LANDINGPAGE, MARKETPLACE_OWNER);
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_EDIT_STAGE,
                MenuBean.LINK_SHOP_EDIT_STAGE, MARKETPLACE_OWNER);
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_TRANSLATION,
                MenuBean.LINK_SHOP_TRANSLATIONS, MARKETPLACE_OWNER);
        this.addItem(HiddenUIConstants.MENU_ITEM_MARKETPLACE_CUSTOMIZE_BRAND,
                MenuBean.LINK_SHOP_CUSTOMIZE_BRAND, MARKETPLACE_OWNER);

        return pageAuthorizationList;
    }
}
