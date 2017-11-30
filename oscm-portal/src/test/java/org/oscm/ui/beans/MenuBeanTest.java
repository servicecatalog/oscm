/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Lorenz Goebel                                                   
 *                                                                              
 *  Creation Date: 28.06.2011                                                      
 *                                                                              
 *  Completion Time: 28.06.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.ui.menu.MenuGroup;
import org.oscm.ui.menu.MenuItem;
import org.oscm.ui.model.User;

/**
 * Tests the menu bean and menu group visibility.
 * 
 * @author Lorenz Goebel
 */
public class MenuBeanTest {
    private MenuBean menuBean;
    private ApplicationBean applicationBean;
    private TriggerService triggerServiceBean;
    private User SERVICE_MANAGER;
    private User TECHNOLOGY_MANAGER;
    private User OPERATOR_ADMIN;
    private User OPERATOR;
    private User MARKETPLACE_OWNER;
    private User RESELLER;
    private User BROKER;
    private User NO_ROLES_USER;
    private User currentUser;
    private User DEFAULT_PLATFORM_OPERATOR_KEY_1000;

    @SuppressWarnings("serial")
    @Before
    public void before() {
        currentUser = null;
        applicationBean = mock(ApplicationBean.class);
        triggerServiceBean = mock(TriggerService.class);
        doReturn(new ArrayList<VOTriggerDefinition>()).when(triggerServiceBean)
                .getAllDefinitions();

        DEFAULT_PLATFORM_OPERATOR_KEY_1000 = new User(new VOUserDetails() {
            private Set<UserRoleType> roles;

            @Override
            public Set<UserRoleType> getUserRoles() {
                if (roles == null) {
                    roles = new HashSet<>();
                    roles.add(UserRoleType.PLATFORM_OPERATOR);
                    roles.add(UserRoleType.ORGANIZATION_ADMIN);
                }
                return roles;
            };

            @Override
            public long getKey() {
                return 1000L;
            };
        });

        SERVICE_MANAGER = new User(new VOUserDetails() {
            private Set<UserRoleType> roles;

            @Override
            public Set<UserRoleType> getUserRoles() {
                if (roles == null) {
                    roles = new HashSet<>();
                    roles.add(UserRoleType.SERVICE_MANAGER);
                }
                return roles;
            };
        });

        TECHNOLOGY_MANAGER = new User(new VOUserDetails() {
            private Set<UserRoleType> roles;

            @Override
            public Set<UserRoleType> getUserRoles() {
                if (roles == null) {
                    roles = new HashSet<>();
                    roles.add(UserRoleType.TECHNOLOGY_MANAGER);
                }
                return roles;
            };
        });

        RESELLER = new User(new VOUserDetails() {
            private Set<UserRoleType> roles;

            @Override
            public Set<UserRoleType> getUserRoles() {
                if (roles == null) {
                    roles = new HashSet<>();
                    roles.add(UserRoleType.RESELLER_MANAGER);
                }
                return roles;
            };
        });

        BROKER = new User(new VOUserDetails() {
            private Set<UserRoleType> roles;

            @Override
            public Set<UserRoleType> getUserRoles() {
                if (roles == null) {
                    roles = new HashSet<>();
                    roles.add(UserRoleType.BROKER_MANAGER);
                }
                return roles;
            };
        });

        OPERATOR_ADMIN = new User(new VOUserDetails() {
            private Set<UserRoleType> roles;

            @Override
            public Set<UserRoleType> getUserRoles() {
                if (roles == null) {
                    roles = new HashSet<>();
                    roles.add(UserRoleType.PLATFORM_OPERATOR);
                    roles.add(UserRoleType.ORGANIZATION_ADMIN);
                }
                return roles;
            };
        });

        NO_ROLES_USER = new User(new VOUserDetails() {
            @Override
            public Set<UserRoleType> getUserRoles() {
                return new HashSet<>();
            };
        });

        OPERATOR = new User(new VOUserDetails() {
            private Set<UserRoleType> roles;

            @Override
            public Set<UserRoleType> getUserRoles() {
                if (roles == null) {
                    roles = new HashSet<>();
                    roles.add(UserRoleType.PLATFORM_OPERATOR);
                }
                return roles;
            };
        });

        MARKETPLACE_OWNER = new User(new VOUserDetails() {
            private Set<UserRoleType> roles;

            @Override
            public Set<UserRoleType> getUserRoles() {
                if (roles == null) {
                    roles = new HashSet<>();
                    roles.add(UserRoleType.MARKETPLACE_OWNER);
                }
                return roles;
            };
        });
    }

    private MenuBean createMenuBean() {
        return new MenuBean() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isHidden(String id) {
                return false;
            }

            @Override
            public String getCurrentPageLink() {
                return null;
            }

            /**
             * Get the current user from the session.
             * 
             * @return the current user from the session.
             */
            @Override
            public User getUserFromSession() {
                return currentUser;
            }

            /**
             * @return the Marketplace service
             */
            @Override
            protected MarketplaceService getMarketplaceService() {
                return new MarketplaceServiceStub() {
                    @Override
                    public List<VOMarketplace> getMarketplacesOwned() {
                        VOMarketplace mp = new VOMarketplace();
                        mp.setMarketplaceId(
                                BaseAdmUmTest.GLOBAL_MARKETPLACE_NAME);
                        return Arrays.asList(new VOMarketplace[] { mp });
                    }
                };
            }

            /**
             * @return the trigger service
             */
            @Override
            protected TriggerService getTriggerService() {
                return triggerServiceBean;
            }

            /**
             * @return the ApplicationBean
             */
            @Override
            public ApplicationBean getApplicationBean() {
                return applicationBean;
            }
        };
    }

    /**
     * Get Menu group with given ID.
     */
    private MenuGroup getGroup(String id) {
        List<MenuGroup> groups = menuBean.getMainMenu().getGroups();
        for (MenuGroup group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }
        fail("No menu group found with ID='" + id + "'.");
        return null;
    }

    private void setUser(User user) {
        currentUser = user;
        menuBean = createMenuBean();
    }

    /**
     * Re-test Bug 7727: Customization and Operator group must be invisible for
     * user without roles
     */
    @Test
    public void testGroupVisibility_NoRolesUser() {
        setUser(NO_ROLES_USER);

        // -- VISIBLE GROUPS -- //
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT)
                .isVisible());

        // -- INVISIBLE GROUPS -- //
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE)
                        .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_TECHSERVICE)
                        .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_SERVICE)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_PRICE_MODEL)
                        .isVisible());
    }

    @Test
    public void testGroupVisibility_GlobalMarketPlaceOwner() {
        setUser(OPERATOR_ADMIN);

        // -- VISIBLE GROUPS -- //
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE)
                .isVisible());

        // -- INVISIBLE GROUPS -- //
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_TECHSERVICE)
                        .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_SERVICE)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_PRICE_MODEL)
                        .isVisible());
    }

    @Test
    public void testGroupVisibility_GlobalMarketPlaceOwner_DifferentMarketplace() {
        setUser(OPERATOR_ADMIN);

        // -- VISIBLE GROUPS -- //
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE)
                .isVisible());

        // -- INVISIBLE GROUPS -- //
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_TECHSERVICE)
                        .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_SERVICE)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_PRICE_MODEL)
                        .isVisible());
    }

    @Test
    public void testGroupVisibility_ServiceManager() {
        setUser(SERVICE_MANAGER);

        // -- VISIBLE GROUPS -- //
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_SERVICE)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_PRICE_MODEL)
                .isVisible());

        // -- INVISIBLE GROUPS -- //
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_TECHSERVICE)
                        .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE)
                        .isVisible());

    }

    @Test
    public void testGroupVisibility_Reseller() {
        // given
        setUser(RESELLER);

        // -- VISIBLE GROUPS -- //
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_SERVICE)
                .isVisible());

        // -- INVISIBLE GROUPS -- //
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_TECHSERVICE)
                        .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE)
                        .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_PRICE_MODEL)
                        .isVisible());

    }

    @Test
    public void testGroupVisibility_Broker() {
        // given
        setUser(BROKER);

        // -- VISIBLE GROUPS -- //
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_SERVICE)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_PRICE_MODEL)
                        .isVisible());

        // -- INVISIBLE GROUPS -- //
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_TECHSERVICE)
                        .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE)
                        .isVisible());

    }

    @Test
    public void testGroupVisibility_TechnologyManager() {
        setUser(TECHNOLOGY_MANAGER);

        // -- VISIBLE GROUPS -- //
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_TECHSERVICE)
                .isVisible());

        // -- INVISIBLE GROUPS -- //
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE)
                        .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER)
                .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_SERVICE)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_PRICE_MODEL)
                        .isVisible());
    }

    @Test
    public void testGroupVisibility_Operator() {
        setUser(OPERATOR);

        // -- VISIBLE GROUPS -- //
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR)
                .isVisible());
        assertTrue(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE)
                .isVisible());

        // -- INVISIBLE GROUPS -- //
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_TECHSERVICE)
                        .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER)
                .isVisible());
        assertFalse(getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_SERVICE)
                .isVisible());
        assertFalse(
                getGroup(HiddenUIConstants.MENU_GROUP_NAVIGATION_PRICE_MODEL)
                        .isVisible());
    }

    @Test
    public void testMenuItemVisibility_Operator_GroupOperation() {
        // given
        when(Boolean.valueOf(applicationBean.isInternalAuthMode()))
                .thenReturn(Boolean.TRUE);

        // when
        setUser(OPERATOR);

        // then
        MenuGroup manageMpGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR);
        assertTrue(manageMpGroup.isVisible());

        List<MenuItem> visibleMenuItems = getVisibleMenuItems(manageMpGroup);
        assertEquals(16, visibleMenuItems.size());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_USERS,
                visibleMenuItems.get(0).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_CREATE_ORGANIZATION,
                visibleMenuItems.get(1).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_CREATE_PSP,
                visibleMenuItems.get(2).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_ORGANIZATIONS,
                visibleMenuItems.get(3).getId());

        assertEquals(
                HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_OPERATOR_REVENUE_SHARE,
                visibleMenuItems.get(4).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_TIMERS,
                visibleMenuItems.get(5).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_PSPS,
                visibleMenuItems.get(6).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_CURRENCIES,
                visibleMenuItems.get(7).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_LDAP,
                visibleMenuItems.get(8).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_CONFIGURATION,
                visibleMenuItems.get(9).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_INDEXES,
                visibleMenuItems.get(10).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_EXPORT_BILLING_DATA,
                visibleMenuItems.get(11).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_EXECUTE_BILLING_TASKS,
                visibleMenuItems.get(12).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_EXPORT_AUDIT_LOG_DATA,
                visibleMenuItems.get(13).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_LANGUAGES,
                visibleMenuItems.get(14).getId());

        assertEquals(
                HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_BILLING_ADAPTERS,
                visibleMenuItems.get(15).getId());
    }

    @Test
    public void testMenuItemVisibility_Operator_NoManageLdapSettingsMenu() {
        // given
        when(Boolean.valueOf(applicationBean.isInternalAuthMode()))
                .thenReturn(Boolean.FALSE);

        // when
        setUser(OPERATOR);

        // then
        MenuGroup operatorGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR);
        assertTrue(operatorGroup.isVisible());

        List<MenuItem> visibleMenuItems = getVisibleMenuItems(operatorGroup);
        assertEquals(16, visibleMenuItems.size());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_USERS,
                visibleMenuItems.get(0).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_CREATE_ORGANIZATION,
                visibleMenuItems.get(1).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_CREATE_PSP,
                visibleMenuItems.get(2).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_ORGANIZATIONS,
                visibleMenuItems.get(3).getId());

        assertEquals(
                HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_OPERATOR_REVENUE_SHARE,
                visibleMenuItems.get(4).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_TIMERS,
                visibleMenuItems.get(5).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_PSPS,
                visibleMenuItems.get(6).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_CURRENCIES,
                visibleMenuItems.get(7).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_CONFIGURATION,
                visibleMenuItems.get(8).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_INDEXES,
                visibleMenuItems.get(9).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_EXPORT_BILLING_DATA,
                visibleMenuItems.get(10).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_EXECUTE_BILLING_TASKS,
                visibleMenuItems.get(11).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_EXPORT_AUDIT_LOG_DATA,
                visibleMenuItems.get(12).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_LANGUAGES,
                visibleMenuItems.get(13).getId());

        assertEquals(
                HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_BILLING_ADAPTERS,
                visibleMenuItems.get(14).getId());
    }

    /**
     * Test Bug 9419: Broker and reseller revenue share in marketplace group
     * visible for operator role
     */
    @Test
    public void testMenuItemVisibility_Operator_GroupMarketplace() {
        setUser(OPERATOR);

        MenuGroup manageMpGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE);
        assertTrue(manageMpGroup.isVisible());

        List<MenuItem> visibleMenuItems = getVisibleMenuItems(manageMpGroup);
        assertEquals(5, visibleMenuItems.size());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_CREATE,
                visibleMenuItems.get(0).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_UPDATE,
                visibleMenuItems.get(1).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_DELETE,
                visibleMenuItems.get(2).getId());

        assertEquals(
                HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_BROKER_REVENUE_SHARE,
                visibleMenuItems.get(3).getId());

        assertEquals(
                HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_RESELLER_REVENUE_SHARE,
                visibleMenuItems.get(4).getId());
    }

    @Test
    public void testMenuItemVisibility_MarketplaceOwner() {
        setUser(MARKETPLACE_OWNER);

        MenuGroup manageMpGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE);
        assertTrue(manageMpGroup.isVisible());

        List<MenuItem> visibleMenuItems = getVisibleMenuItems(manageMpGroup);
        assertEquals(9, visibleMenuItems.size());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_CATEGORIES,
                visibleMenuItems.get(0).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_ACCESS,
                visibleMenuItems.get(1).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_SUPPLIERS,
                visibleMenuItems.get(2).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_UPDATE,
                visibleMenuItems.get(3).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_TRACKINGCODE,
                visibleMenuItems.get(4).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_LANDINGPAGE,
                visibleMenuItems.get(5).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_EDIT_STAGE,
                visibleMenuItems.get(6).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_TRANSLATION,
                visibleMenuItems.get(7).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_MARKETPLACE_CUSTOMIZE_BRAND,
                visibleMenuItems.get(8).getId());

    }

    @Test
    public void testMenuItemVisibility_HaveChangePasswordMenu_Internal() {
        // given
        when(Boolean.valueOf(applicationBean.isInternalAuthMode()))
                .thenReturn(Boolean.TRUE);

        // when
        setUser(SERVICE_MANAGER);

        // then
        MenuGroup accountGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT);
        assertTrue(accountGroup.isVisible());

        List<MenuItem> visibleMenuItems = getVisibleMenuItems(accountGroup);
        assertEquals(5, visibleMenuItems.size());

        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_EDIT,
                visibleMenuItems.get(0).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_USER_PWD,
                visibleMenuItems.get(1).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_UDAS,
                visibleMenuItems.get(2).getId());
        assertEquals(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_EXPORT_BILLING_DATA,
                visibleMenuItems.get(3).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_BILLING,
                visibleMenuItems.get(4).getId());
    }

    @Test
    public void testMenuItemVisibility_NoChangePasswordMenu_SAMLSP_ServiceManager() {
        // given
        when(Boolean.valueOf(applicationBean.isInternalAuthMode()))
                .thenReturn(Boolean.FALSE);

        // when
        setUser(SERVICE_MANAGER);

        // then
        MenuGroup accountGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT);
        assertTrue(accountGroup.isVisible());

        List<MenuItem> visibleMenuItems = getVisibleMenuItems(accountGroup);
        assertEquals(4, visibleMenuItems.size());

        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_EDIT,
                visibleMenuItems.get(0).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_UDAS,
                visibleMenuItems.get(1).getId());
        assertEquals(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_EXPORT_BILLING_DATA,
                visibleMenuItems.get(2).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_BILLING,
                visibleMenuItems.get(3).getId());
    }

    @Test
    public void testMenuItemVisibility_HaveChangePasswordMenu_SAMLSP_ADMIN() {
        // given
        when(Boolean.valueOf(applicationBean.isInternalAuthMode()))
                .thenReturn(Boolean.FALSE);
        when(Boolean.valueOf(applicationBean.isReportingAvailable()))
                .thenReturn(Boolean.TRUE);

        // when
        setUser(OPERATOR_ADMIN);

        // then
        MenuGroup accountGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT);
        assertTrue(accountGroup.isVisible());

        List<MenuItem> visibleMenuItems = getVisibleMenuItems(accountGroup);
        assertEquals(6, visibleMenuItems.size());

        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_EDIT,
                visibleMenuItems.get(0).getId());

        assertEquals(HiddenUIConstants.MENU_ITEM_USER_ADD,
                visibleMenuItems.get(1).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_USER_LIST,
                visibleMenuItems.get(2).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_REPORT,
                visibleMenuItems.get(3).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_TRIGGERS,
                visibleMenuItems.get(4).getId());
        assertEquals(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_EXPORT_BILLING_DATA,
                visibleMenuItems.get(5).getId());
    }

    @Test
    public void testMenuItemVisibility_HaveChangePasswordMenu_SAMLSP_CALLED_BY_KEY_1000() {
        // given
        when(Boolean.valueOf(applicationBean.isInternalAuthMode()))
                .thenReturn(Boolean.FALSE);
        when(Boolean.valueOf(applicationBean.isReportingAvailable()))
                .thenReturn(Boolean.TRUE);

        // when
        setUser(DEFAULT_PLATFORM_OPERATOR_KEY_1000);

        // then
        MenuGroup accountGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT);
        assertTrue(accountGroup.isVisible());

        List<MenuItem> visibleMenuItems = getVisibleMenuItems(accountGroup);
        assertEquals(7, visibleMenuItems.size());

        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_EDIT,
                visibleMenuItems.get(0).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_USER_PWD,
                visibleMenuItems.get(1).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_USER_ADD,
                visibleMenuItems.get(2).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_USER_LIST,
                visibleMenuItems.get(3).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_REPORT,
                visibleMenuItems.get(4).getId());
        assertEquals(HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_TRIGGERS,
                visibleMenuItems.get(5).getId());
        assertEquals(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_EXPORT_BILLING_DATA,
                visibleMenuItems.get(6).getId());
    }

    @Test
    public void testMenuItemVisibility_ExportBillingDataVisible_B10271() {
        setUser(OPERATOR);

        MenuGroup accountGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT);

        boolean isExportBillingDataVisible = isVisible(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_EXPORT_BILLING_DATA,
                accountGroup);

        assertTrue(isExportBillingDataVisible);
    }

    @Test
    public void testMenuItemVisibility_ManageLanguages() {
        setUser(OPERATOR);

        MenuGroup accountGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_OPERATOR);

        boolean isManageLanguagesVisible = isVisible(
                HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_LANGUAGES,
                accountGroup);

        assertTrue(isManageLanguagesVisible);
    }

    private List<MenuItem> getVisibleMenuItems(MenuGroup menuGroup) {
        List<MenuItem> result = new LinkedList<>();
        for (MenuItem mi : menuGroup.getItems()) {
            if (mi.isVisible()) {
                result.add(mi);
            }
        }
        return result;
    }

    private boolean isVisible(String id, MenuGroup group) {
        for (MenuItem item : getVisibleMenuItems(group)) {
            if (item.getId().equals(id))
                return true;

        }
        return false;
    }

    @Test
    public void testMenuItemVisibility_ManagePaymentType() {
        // given
        when(Boolean.valueOf(applicationBean.isPaymentInfoAvailable()))
                .thenReturn(Boolean.TRUE);
        // when
        setUser(SERVICE_MANAGER);

        // then
        MenuGroup operatorGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER);
        boolean isManagePaymentTypeVisible = isVisible(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_PAYMENT_ENABLEMENT,
                operatorGroup);
        assertTrue(isManagePaymentTypeVisible);
    }

    @Test
    public void testMenuItemVisibility_NoManagePaymentType() {
        // given
        when(Boolean.valueOf(applicationBean.isPaymentInfoAvailable()))
                .thenReturn(Boolean.FALSE);
        // when
        setUser(SERVICE_MANAGER);

        // then
        MenuGroup operatorGroup = getGroup(
                HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER);
        boolean isManagePaymentTypeVisible = isVisible(
                HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_PAYMENT_ENABLEMENT,
                operatorGroup);
        assertFalse(isManagePaymentTypeVisible);
    }

}
