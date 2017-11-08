/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationSetting;
import org.oscm.domobjects.PlatformSetting;
import org.oscm.domobjects.PlatformUser;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.UserRoles;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.LdapProperties;

public class LdapSettingsMangementServiceBeanIT extends EJBTestBase {

    DataService ds;
    LdapSettingsManagementServiceLocal ldapSettingsMgmtSvc;

    private static final String customerOrgId = "custOrg";
    private static final String customerOrgId2 = "custOrg2";
    Organization customerOrg, customerOrg2;
    PlatformUser customerOrgAdmin, customerOrgAdmin2, platformOrgAdmin;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LdapSettingsManagementServiceBean());

        ds = container.get(DataService.class);
        ldapSettingsMgmtSvc = container
                .get(LdapSettingsManagementServiceLocal.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCountries.setupSomeCountries(ds);
                UserRoles.createSetupRoles(ds);
                EJBTestBase.createOrganizationRoles(ds);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization platformOrg = new Organization();
                platformOrg
                        .setOrganizationId(OrganizationRoleType.PLATFORM_OPERATOR
                                .name());
                platformOrg = (Organization) ds
                        .getReferenceByBusinessKey(platformOrg);
                Organizations.grantOrganizationRoles(ds, platformOrg,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                platformOrgAdmin = Organizations.createUserForOrg(ds,
                        platformOrg, true, "platformOrgAdmin");
                PlatformUsers.grantRoles(ds, platformOrgAdmin,
                        UserRoleType.PLATFORM_OPERATOR);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                customerOrg = Organizations.createOrganization(ds,
                        customerOrgId, OrganizationRoleType.CUSTOMER);
                customerOrgAdmin = Organizations.createUserForOrg(ds,
                        customerOrg, true, "custOrgAdmin");

                customerOrg2 = Organizations.createOrganization(ds,
                        customerOrgId2, OrganizationRoleType.CUSTOMER);
                customerOrgAdmin2 = Organizations.createUserForOrg(ds,
                        customerOrg2, true, "custOrgAdmin2");

                return null;
            }
        });

        // create one platform setting
        PlatformSetting pfSetting = new PlatformSetting();
        pfSetting.setSettingType(SettingType.LDAP_CONTEXT_FACTORY);
        pfSetting.setSettingValue("myLdapContextFactory");
        ds.persist(pfSetting);

        // create one organization setting for customerOrg
        OrganizationSetting orgSetting = new OrganizationSetting();
        orgSetting.setOrganization(customerOrg);
        orgSetting.setSettingType(SettingType.LDAP_ATTR_LOCALE);
        orgSetting.setSettingValue("en");
        ds.persist(orgSetting);
        customerOrg.setOrganizationSettings(Arrays.asList(orgSetting));

        container.login(customerOrgAdmin.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
    }

    @Test
    public void getPlatformSettings_default() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties platformSettings = ldapSettingsMgmtSvc
                        .getPlatformSettings();
                assertEquals("Only defined platform setting must be returned",
                        1, platformSettings.size());
                assertEquals(
                        "Returned value must correspond to value set during setup",
                        "myLdapContextFactory", platformSettings
                                .get(SettingType.LDAP_CONTEXT_FACTORY.name()));
                return null;
            }
        });
    }

    @Test
    public void getPlatformSettings_noPropertiesDefined() throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties platformSettings = ldapSettingsMgmtSvc
                        .getPlatformSettings();
                assertTrue("Empty properties (not null) expected",
                        platformSettings.keySet().isEmpty());
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOrganizationSettings_nullOrgId() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc.getOrganizationSettings(null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOrganizationSettings_emptyOrgId() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc.getOrganizationSettings("");
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getOrganizationSettings_invalidOrgId() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.getOrganizationSettings("myFantasyOrgId");
                return null;
            }
        });
    }

    @Test
    public void getOrganizationSettings_default() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties organizationSettings = ldapSettingsMgmtSvc
                        .getOrganizationSettings(customerOrgId);
                assertEquals(
                        "Only defined organization setting must be returned",
                        1, organizationSettings.size());
                assertEquals(
                        "Returned value must correspond to value set during setup",
                        "en", organizationSettings
                                .get(SettingType.LDAP_ATTR_LOCALE.name()));
                return null;
            }
        });
    }

    @Test
    public void getOrganizationSettings_noPropertiesDefined() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(customerOrgAdmin2.getKey(),
                        UserRoleType.ORGANIZATION_ADMIN.toString());
                Properties organizationSettings = ldapSettingsMgmtSvc
                        .getOrganizationSettings(customerOrgId2);
                assertTrue("Empty properties (not null) expected",
                        organizationSettings.keySet().isEmpty());
                return null;
            }
        });
    }

    @Test
    public void setPlatformSettings_nullProperties() throws Throwable {
        container.login(platformOrgAdmin.getKey(),
                UserRoleType.PLATFORM_OPERATOR.toString());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.setPlatformSettings(null);
                return null;
            }
        });
        assertEquals(
                "Existing platform settings must have been deleted and no new platform settings must have been created",
                0, getPlatformSettings(null).size());
        assertEquals(
                "Existing organization setting for customerOrg must still exist",
                1,
                getOrganizationSettings(customerOrg,
                        SettingType.LDAP_ATTR_LOCALE).size());
    }

    @Test
    public void setPlatformSettings_emptyProperties() throws Throwable {
        container.login(platformOrgAdmin.getKey(),
                UserRoleType.PLATFORM_OPERATOR.toString());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.setPlatformSettings(new Properties());
                return null;
            }
        });

        assertEquals(
                "Existing platform settings must have been deleted and no new platform settings must have been created",
                0, getPlatformSettings(null).size());
        assertEquals(
                "Existing organization setting for customerOrg must still exist",
                1,
                getOrganizationSettings(customerOrg,
                        SettingType.LDAP_ATTR_LOCALE).size());
    }

    @Test
    public void setPlatformSettings_supportedProperties() throws Throwable {
        container.login(platformOrgAdmin.getKey(),
                UserRoleType.PLATFORM_OPERATOR.toString());

        final String myLdapUrl = "someUrl";
        final String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LdapProperties props = new LdapProperties();
                props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
                props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);

                ldapSettingsMgmtSvc.setPlatformSettings(props.asProperties());
                return null;
            }
        });

        assertEquals(
                "Existing platform settings must have been deleted and new platform settings must have been created",
                2, getPlatformSettings(null).size());
        assertEquals("LDAP URL must have been stored as given", myLdapUrl,
                getPlatformSettings(SettingType.LDAP_URL).get(0)
                        .getSettingValue());
        assertEquals("LDAP BASE DN must have been stored as given", myBaseDn,
                getPlatformSettings(SettingType.LDAP_BASE_DN).get(0)
                        .getSettingValue());
        assertEquals(
                "Existing organization setting for customerOrg must still exist",
                1,
                getOrganizationSettings(customerOrg,
                        SettingType.LDAP_ATTR_LOCALE).size());
    }

    @Test
    public void setPlatformSettings_unsupportedPropertyKey() throws Throwable {
        container.login(platformOrgAdmin.getKey(),
                UserRoleType.PLATFORM_OPERATOR.toString());
        final String myLdapUrl = "someUrl";
        final String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        final String myFantasyPropKey = "myFantasyPropKey";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LdapProperties props = new LdapProperties();
                props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
                props.setProperty(myFantasyPropKey, "myFantasyPropValue");
                props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);
                ldapSettingsMgmtSvc.setPlatformSettings(props.asProperties());
                return null;
            }
        });

        assertEquals(
                "Invalid settings key must be ignored and platform settings must have been created for valid keys",
                2, getPlatformSettings(null).size());
        assertEquals("LDAP URL must have been stored as given", myLdapUrl,
                getPlatformSettings(SettingType.LDAP_URL).get(0)
                        .getSettingValue());
        assertEquals("LDAP BASE DN must have been stored as given", myBaseDn,
                getPlatformSettings(SettingType.LDAP_BASE_DN).get(0)
                        .getSettingValue());
        assertEquals(
                "Existing organization setting for customerOrg must still exist",
                1,
                getOrganizationSettings(customerOrg,
                        SettingType.LDAP_ATTR_LOCALE).size());
    }

    @Test
    public void setPlatformSettings_supportedPropertyKeyButNoValue()
            throws Throwable {
        container.login(platformOrgAdmin.getKey(),
                UserRoleType.PLATFORM_OPERATOR.toString());
        final String myLdapUrl = "someUrl";
        final String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    LdapProperties props = new LdapProperties();
                    props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
                    props.setProperty(SettingType.LDAP_ATTR_FIRST_NAME.name(),
                            "");
                    props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);

                    ldapSettingsMgmtSvc.setPlatformSettings(props
                            .asProperties());
                    fail();
                    return null;
                }
            });
        } catch (ValidationException e) {
            assertEquals(
                    ValidationException.ReasonEnum.LDAP_INVALID_PLATFORM_PROPERTY,
                    e.getReason());
            assertEquals(
                    "Existing platform setting must still exist (and only this)",
                    1, getPlatformSettings(null).size());
            assertEquals(
                    "Existing value for platform setting must still exist (as specified in setup)",
                    "myLdapContextFactory",
                    getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY)
                            .get(0).getSettingValue());
            assertEquals(
                    "Existing organization setting for customerOrg must still exist",
                    1,
                    getOrganizationSettings(customerOrg,
                            SettingType.LDAP_ATTR_LOCALE).size());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOrganizationSettings_nullOrgId() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc.setOrganizationSettings(null, null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOrganizationSettings_emptyOrgId() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc.setOrganizationSettings("", null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void setOrganizationSettings_invalidOrganizationId()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.setOrganizationSettings("myFantasyOrgId",
                        null);
                return null;
            }
        });
    }

    @Test
    public void setOrganizationSettings_validOrganization_nullProperties_initallyEmpty()
            throws Throwable {
        container.login(customerOrgAdmin2.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.setOrganizationSettings(customerOrgId2,
                        null);
                return null;
            }
        });
        assertEquals("Existing platform setting must still exist", 1,
                getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY).size());
        assertEquals(
                "Existing organization setting for customerOrg must still exist",
                1,
                getOrganizationSettings(customerOrg,
                        SettingType.LDAP_ATTR_LOCALE).size());
        assertEquals(
                "No new organization settings for this organization must have been created",
                0, getOrganizationSettings(customerOrg2, null).size());

    }

    @Test
    public void setOrganizationSettings_validOrganization_nullProperties()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc
                        .setOrganizationSettings(customerOrgId, null);
                return null;
            }
        });
        assertEquals("Existing platform setting must still exist", 1,
                getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY).size());
        assertEquals(
                "Existing organization setting for this organization must have been deleted and no new organization settings must have been created",
                0, getOrganizationSettings(customerOrg, null).size());
    }

    @Test
    public void setOrganizationSettings_validOrganization_emptyProperties()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.setOrganizationSettings(customerOrgId,
                        new Properties());
                return null;
            }
        });
        assertEquals("Existing platform setting must still exist", 1,
                getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY).size());
        assertEquals(
                "Existing organization setting for this organization must have been deleted and no new organization settings must have been created",
                0, getOrganizationSettings(customerOrg, null).size());
    }

    @Test
    public void setOrganizationSettings_validOrganization_supportedProperties()
            throws Throwable {
        final String myLdapUrl = "someUrl";
        final String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LdapProperties props = new LdapProperties();
                props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
                props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);
                ldapSettingsMgmtSvc.setOrganizationSettings(customerOrgId,
                        props.asProperties());
                return null;
            }
        });

        assertEquals(
                "Existing organization settings must have been deleted and new organization settings must have been created",
                2, getOrganizationSettings(customerOrg, null).size());
        assertEquals(
                "LDAP URL must have been stored as given",
                myLdapUrl,
                getOrganizationSettings(customerOrg, SettingType.LDAP_URL).get(
                        0).getSettingValue());
        assertEquals("LDAP BASE DN must have been stored as given", myBaseDn,
                getOrganizationSettings(customerOrg, SettingType.LDAP_BASE_DN)
                        .get(0).getSettingValue());
        assertEquals("Existing platform setting must still exist", 1,
                getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY).size());
    }

    @Test
    public void setOrganizationSettings_validOrganization_unsupportedPropertyKey()
            throws Throwable {

        final String myLdapUrl = "someUrl";
        final String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        final String myFantasyPropKey = "myFantasyPropKey";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LdapProperties props = new LdapProperties();
                props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
                props.setProperty(myFantasyPropKey, "myFantasyPropValue");
                props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);
                ldapSettingsMgmtSvc.setOrganizationSettings(customerOrgId,
                        props.asProperties());
                return null;
            }
        });

        assertEquals(
                "Existing organization settings must have been deleted and new organization settings must have been created",
                2, getOrganizationSettings(customerOrg, null).size());
        assertEquals(
                "LDAP URL must have been stored as given",
                myLdapUrl,
                getOrganizationSettings(customerOrg, SettingType.LDAP_URL).get(
                        0).getSettingValue());
        assertEquals("LDAP BASE DN must have been stored as given", myBaseDn,
                getOrganizationSettings(customerOrg, SettingType.LDAP_BASE_DN)
                        .get(0).getSettingValue());
        assertEquals("Existing platform setting must still exist", 1,
                getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY).size());
    }

    @Test
    public void setOrganizationSettings_validOrganization_supportedPropertyKeyButNoValue()
            throws Throwable {
        final String myLdapUrl = "someUrl";
        final String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LdapProperties props = new LdapProperties();
                props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
                props.setProperty(SettingType.LDAP_ATTR_FIRST_NAME.name(), "");
                props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);
                ldapSettingsMgmtSvc.setOrganizationSettings(customerOrgId,
                        props.asProperties());
                return null;
            }
        });

        assertEquals(
                "Existing organization settings must have been deleted and new organization settings must have been created",
                3, getOrganizationSettings(customerOrg, null).size());
        assertEquals(
                "LDAP URL must have been stored as given",
                myLdapUrl,
                getOrganizationSettings(customerOrg, SettingType.LDAP_URL).get(
                        0).getSettingValue());
        assertEquals("LDAP BASE DN must have been stored as given", myBaseDn,
                getOrganizationSettings(customerOrg, SettingType.LDAP_BASE_DN)
                        .get(0).getSettingValue());
        assertEquals(
                "LDAP FIRST NAME must have been stored empty as given so that platform value is used later on",
                "",
                getOrganizationSettings(customerOrg,
                        SettingType.LDAP_ATTR_FIRST_NAME).get(0)
                        .getSettingValue());
        assertEquals("Existing platform setting must still exist", 1,
                getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void resetOrganizationSettings_nullOrgId() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc.resetOrganizationSettings(null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void resetOrganizationSettings_emptyOrgId() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc.resetOrganizationSettings("");
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void resetOrganizationSettings_invalidOrgId() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.resetOrganizationSettings("myFantasyOrgId");
                return null;
            }
        });
    }

    @Test
    public void resetOrganizationSettings_noOrgPropertiesDefined_noPlatformPropertiesDefined()
            throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();
        assertEquals("No platform properties must be defined", 0,
                getPlatformSettings(null).size());

        assertEquals("No organization-specific properties must be defined", 0,
                getOrganizationSettings(customerOrg2, null).size());

        container.login(customerOrgAdmin2.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.resetOrganizationSettings(customerOrgId2);
                return null;
            }
        });

        assertEquals(
                "Still no organization-specific properties must be defined", 0,
                getOrganizationSettings(customerOrg2, null).size());
        // platform settings must be left unchanged
        assertEquals("Still no platform properties must be defined", 0,
                getPlatformSettings(null).size());
    }

    @Test
    public void resetOrganizationSettings_noOrgPropertiesDefined_platformPropertiesDefined()
            throws Throwable {
        assertEquals("No organization-specific properties must be defined", 0,
                getOrganizationSettings(customerOrg2, null).size());

        container.login(customerOrgAdmin2.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.resetOrganizationSettings(customerOrgId2);
                return null;
            }
        });

        assertEquals(
                "Exactly one organization-specific properties must be defined",
                1, getOrganizationSettings(customerOrg2, null).size());
        assertEquals(
                "Returned value must correspond to platform settings value",
                "",
                getOrganizationSettings(customerOrg2,
                        SettingType.LDAP_CONTEXT_FACTORY).get(0)
                        .getSettingValue());
        // platform settings must be left unchanged
        assertEquals("Exactly one platform property must be defined", 1,
                getPlatformSettings(null).size());
        assertEquals(
                "Returned value must correspond to value set during setup",
                "myLdapContextFactory",
                getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY).get(0)
                        .getSettingValue());
    }

    @Test
    public void resetOrganizationSettings_orgPropertiesDefined_noPlatformPropertiesDefined()
            throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();
        assertEquals("No platform properties must be defined", 0,
                getPlatformSettings(null).size());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.resetOrganizationSettings(customerOrgId);
                return null;
            }
        });

        assertEquals("No organization-specific properties must be defined", 0,
                getOrganizationSettings(customerOrg, null).size());
        // platform settings must be left unchanged
        assertEquals("Still no platform properties must be defined", 0,
                getPlatformSettings(null).size());
    }

    @Test
    public void resetOrganizationSettings_orgPropertiesDefined_platformPropertiesDefined()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.resetOrganizationSettings(customerOrgId);
                return null;
            }
        });

        assertEquals(
                "Exactly one organization-specific properties must be defined",
                1, getOrganizationSettings(customerOrg, null).size());
        assertEquals(
                "Returned value must be empty (to bind it to platform property)",
                "",
                getOrganizationSettings(customerOrg,
                        SettingType.LDAP_CONTEXT_FACTORY).get(0)
                        .getSettingValue());
        // platform settings must be left unchanged
        assertEquals("Exactly one platform property must be defined", 1,
                getPlatformSettings(null).size());
        assertEquals(
                "Returned value must correspond to value set during setup",
                "myLdapContextFactory",
                getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY).get(0)
                        .getSettingValue());
    }

    @Test
    public void clearPlatformSettings_noPlatformPropertiesDefined()
            throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();
        assertEquals("No platform properties must be defined", 0,
                getPlatformSettings(null).size());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.clearPlatformSettings();
                return null;
            }
        });

        // platform settings must be left unchanged
        assertEquals("Still no platform properties must be defined", 0,
                getPlatformSettings(null).size());
    }

    @Test
    public void clearPlatformSettings_platformPropertiesDefined()
            throws Throwable {
        assertEquals(
                "Returned value must correspond to value set during setup",
                "myLdapContextFactory",
                getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY).get(0)
                        .getSettingValue());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc.clearPlatformSettings();
                return null;
            }
        });

        // platform settings must be cleared
        assertEquals("Still no platform properties must be defined", 0,
                getPlatformSettings(null).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOrganizationSettingsResolved_nullOrgId() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc.getOrganizationSettingsResolved(null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOrganizationSettingsResolved_emptyOrgId() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc.getOrganizationSettingsResolved("");
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getOrganizationSettingsResolved_invalidOrgId() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ldapSettingsMgmtSvc
                        .getOrganizationSettingsResolved("myFantasyOrgId");
                return null;
            }
        });
    }

    @Test
    public void getOrganizationSettingsResolved_noOrgPropertiesDefined_noPlatformPropertiesDefined()
            throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();
        assertEquals("No platform properties must be defined", 0,
                getPlatformSettings(null).size());

        assertEquals("No organization-specific properties must be defined", 0,
                getOrganizationSettings(customerOrg2, null).size());

        container.login(customerOrgAdmin2.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties propsResolved = ldapSettingsMgmtSvc
                        .getOrganizationSettingsResolved(customerOrgId2);

                assertEquals("No properties must be returned", 0,
                        propsResolved.size());
                return null;
            }
        });
    }

    @Test
    public void getOrganizationSettingsResolved_noOrgPropertiesDefined_platformPropertiesDefined()
            throws Throwable {
        assertEquals("No organization-specific properties must be defined", 0,
                getOrganizationSettings(customerOrg2, null).size());

        container.login(customerOrgAdmin2.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties propsResolved = ldapSettingsMgmtSvc
                        .getOrganizationSettingsResolved(customerOrgId2);

                assertEquals(
                        "Return list must be empty (because no organization property was defined)",
                        0, propsResolved.size());
                return null;
            }
        });
    }

    @Test
    public void getOrganizationSettingsResolved_orgPropertiesDefined_noPlatformPropertiesDefined()
            throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();
        assertEquals("No platform properties must be defined", 0,
                getPlatformSettings(null).size());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties propsResolved = ldapSettingsMgmtSvc
                        .getOrganizationSettingsResolved(customerOrgId);

                assertEquals(
                        "Return list must consist of the defined organization property",
                        1, propsResolved.size());
                assertEquals(
                        "Returned value for setting must correspond to organization setting value set during setup",
                        "en",
                        propsResolved.get(SettingType.LDAP_ATTR_LOCALE.name()));
                return null;
            }
        });
    }

    @Test
    public void getOrganizationSettingsResolved_orgPropertiesDefined_platformPropertiesDefined_distinct()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties propsResolved = ldapSettingsMgmtSvc
                        .getOrganizationSettingsResolved(customerOrgId);

                assertEquals(
                        "Return list must consist of the defined organization properties",
                        1, propsResolved.size());
                assertEquals(
                        "Returned value for setting must correspond to organization setting value set during setup",
                        "en",
                        propsResolved.get(SettingType.LDAP_ATTR_LOCALE.name()));
                assertNull(
                        "Setting must not be included because no organization setting with corresponding key was defined",
                        propsResolved.get(SettingType.LDAP_CONTEXT_FACTORY
                                .name()));
                return null;
            }
        });
    }

    @Test
    public void getOrganizationSettingsResolved_orgPropertyDefinedWithKeyAndValue_platformPropertyDefinedWithSameKey()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // define additional platform and org setting with same key but
                // different values
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_UID, "uid");
                createAndStoreOrganizationSettingForCustomerOrg(
                        SettingType.LDAP_ATTR_UID, "email");
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties propsResolved = ldapSettingsMgmtSvc
                        .getOrganizationSettingsResolved(customerOrgId);

                assertEquals(
                        "Return list must consist of the defined organization properties which are all given by key and value",
                        2, propsResolved.size());
                assertEquals(
                        "Returned value for setting must correspond to organization setting value set during setup",
                        "en",
                        propsResolved.get(SettingType.LDAP_ATTR_LOCALE.name()));
                assertEquals(
                        "Returned value for setting must be the one defined for the organization (and thus override the one defined for the platform)",
                        "email",
                        propsResolved.get(SettingType.LDAP_ATTR_UID.name()));
                assertNull(
                        "Setting must not be included because no organization setting with corresponding key was defined",
                        propsResolved.get(SettingType.LDAP_CONTEXT_FACTORY
                                .name()));
                return null;
            }
        });
    }

    @Test
    public void getOrganizationSettingsResolved_orgPropertyDefinedWithKeyButNoValue_platformPropertyDefinedWithSameKey()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // define additional platform and org setting with same key but
                // only value for platform property
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_UID, "uid");
                createAndStoreOrganizationSettingForCustomerOrg(
                        SettingType.LDAP_ATTR_UID, "");
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties propsResolved = ldapSettingsMgmtSvc
                        .getOrganizationSettingsResolved(customerOrgId);

                assertEquals(
                        "Return list must consist of combination of defined organization and platform properties",
                        2, propsResolved.size());
                assertEquals(
                        "Returned value for setting must correspond to organization setting value set during setup",
                        "en",
                        propsResolved.get(SettingType.LDAP_ATTR_LOCALE.name()));
                assertEquals(
                        "Returned value for setting must be the one defined for the platform (because setting with key but no value is defined for the organization)",
                        "uid",
                        propsResolved.get(SettingType.LDAP_ATTR_UID.name()));
                assertNull(
                        "Setting must not be included because no organization setting with corresponding key was defined",
                        propsResolved.get(SettingType.LDAP_CONTEXT_FACTORY
                                .name()));
                return null;
            }
        });
    }

    @Test
    public void getOrganizationSettingsResolved_orgPropertyDefinedWithKeyButNoValue_noPlatformPropertyDefinedWithSameKey()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStoreOrganizationSettingForCustomerOrg(
                        SettingType.LDAP_ATTR_UID, "");
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties propsResolved = ldapSettingsMgmtSvc
                        .getOrganizationSettingsResolved(customerOrgId);

                assertEquals(
                        "Return list must consist of defined organization properties",
                        1, propsResolved.size());
                assertEquals(
                        "Returned value for setting must correspond to organization setting value set during setup",
                        "en",
                        propsResolved.get(SettingType.LDAP_ATTR_LOCALE.name()));
                assertNull(
                        "No value for setting must returned (because no value is defined for the organization and no corresponding platform property exists)",
                        propsResolved.get(SettingType.LDAP_ATTR_UID.name()));
                assertNull(
                        "Setting must not be included because no organization setting with corresponding key was defined",
                        propsResolved.get(SettingType.LDAP_CONTEXT_FACTORY
                                .name()));
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSettingsResolved_nullProperties() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc.getSettingsResolved(null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void getSettingsResolved_noOrgPropertiesDefined_noPlatformPropertiesDefined()
            throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties settingsResolved = ldapSettingsMgmtSvc
                        .getSettingsResolved(new Properties());

                assertEquals("No settings expected", 0, settingsResolved.size());
                return null;
            }
        });

    }

    @Test
    public void getSettingsResolved_noOrgPropertiesDefined_platformPropertiesDefined()
            throws Throwable {
        assertEquals("One platform property must be defined", 1,
                getPlatformSettings(SettingType.LDAP_CONTEXT_FACTORY).size());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties settingsResolved = ldapSettingsMgmtSvc
                        .getSettingsResolved(new Properties());

                assertEquals("No settings expected", 0, settingsResolved.size());
                return null;
            }
        });
    }

    @Test
    public void getSettingsResolved_orgPropertiesDefined_noPlatformPropertiesDefined()
            throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();
        final String myLdapUrl = "someUrl";
        final String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        final LdapProperties props = new LdapProperties();
        props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
        props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties settingsResolved = ldapSettingsMgmtSvc
                        .getSettingsResolved(props.asProperties());

                assertEquals("Only given properties must be returned", 2,
                        settingsResolved.size());
                assertEquals("LDAP URL must have been returned as given",
                        myLdapUrl,
                        settingsResolved.getProperty(SettingType.LDAP_URL
                                .name()));
                assertEquals("LDAP BASE DN must have been returned as given",
                        myBaseDn,
                        settingsResolved.getProperty(SettingType.LDAP_BASE_DN
                                .name()));
                return null;
            }
        });
    }

    @Test
    public void getSettingsResolved_orgPropertiesDefined_platformPropertiesDefined_distinct()
            throws Throwable {
        final String myLdapUrl = "someUrl";
        final String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        final LdapProperties props = new LdapProperties();
        props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
        props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties settingsResolved = ldapSettingsMgmtSvc
                        .getSettingsResolved(props.asProperties());

                assertEquals("Only given properties must be returned", 2,
                        settingsResolved.size());
                assertEquals("LDAP URL must have been returned as given",
                        myLdapUrl,
                        settingsResolved.getProperty(SettingType.LDAP_URL
                                .name()));
                assertEquals("LDAP BASE DN must have been returned as given",
                        myBaseDn,
                        settingsResolved.getProperty(SettingType.LDAP_BASE_DN
                                .name()));
                assertNull("Platform setting must not be returned",
                        settingsResolved
                                .getProperty(SettingType.LDAP_CONTEXT_FACTORY
                                        .name()));
                return null;
            }
        });
    }

    @Test
    public void getSettingsResolved_orgPropertyDefinedWithKeyAndValue_platformPropertyDefinedWithSameKey()
            throws Throwable {

        final String myLdapUrl = "someUrl";
        final String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        final String myCtxFct = "myFactory";
        final LdapProperties props = new LdapProperties();
        props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
        props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);
        props.setProperty(SettingType.LDAP_CONTEXT_FACTORY.name(), myCtxFct);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties settingsResolved = ldapSettingsMgmtSvc
                        .getSettingsResolved(props.asProperties());

                assertEquals("Only given properties must be returned", 3,
                        settingsResolved.size());
                assertEquals("LDAP URL must have been returned as given",
                        myLdapUrl,
                        settingsResolved.getProperty(SettingType.LDAP_URL
                                .name()));
                assertEquals("LDAP BASE DN must have been returned as given",
                        myBaseDn,
                        settingsResolved.getProperty(SettingType.LDAP_BASE_DN
                                .name()));
                assertEquals(
                        "LDAP CONTEXT FACTORY must have been returned as given",
                        myCtxFct, settingsResolved
                                .getProperty(SettingType.LDAP_CONTEXT_FACTORY
                                        .name()));
                return null;
            }
        });
    }

    @Test
    public void getSettingsResolved_orgPropertyDefinedWithKeyButNoValue_platformPropertyDefinedWithSameKey()
            throws Throwable {
        final String myLdapUrl = "someUrl";
        final String myBaseDn = "ou=people,dc=est,dc=fujitsu,dc=de";
        final LdapProperties props = new LdapProperties();
        props.setProperty(SettingType.LDAP_URL.name(), myLdapUrl);
        props.setProperty(SettingType.LDAP_BASE_DN.name(), myBaseDn);
        props.setProperty(SettingType.LDAP_CONTEXT_FACTORY.name(), "");

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Properties settingsResolved = ldapSettingsMgmtSvc
                        .getSettingsResolved(props.asProperties());

                assertEquals("Only given properties must be returned", 3,
                        settingsResolved.size());
                assertEquals("LDAP URL must have been returned as given",
                        myLdapUrl,
                        settingsResolved.getProperty(SettingType.LDAP_URL
                                .name()));
                assertEquals("LDAP BASE DN must have been returned as given",
                        myBaseDn,
                        settingsResolved.getProperty(SettingType.LDAP_BASE_DN
                                .name()));
                assertEquals(
                        "LDAP CONTEXT FACTORY must have been returned as specified in setup (from platform)",
                        "myLdapContextFactory", settingsResolved
                                .getProperty(SettingType.LDAP_CONTEXT_FACTORY
                                        .name()));
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDefaultValueForSetting_nullSetting() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc.getDefaultValueForSetting(null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDefaultValueForSetting_noPlatformPropertyForKeyDefined_platformPropertyHasNoDefault()
            throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ldapSettingsMgmtSvc
                            .getDefaultValueForSetting(SettingType.LDAP_ATTR_FIRST_NAME);
                    return null;
                }
            });
        } catch (EJBException ex) {
            throw ex.getCausedByException();
        }
    }

    @Test
    public void getDefaultValueForSetting_noPlatformPropertyForKeyDefined_propertyHasDefault()
            throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                String value = ldapSettingsMgmtSvc
                        .getDefaultValueForSetting(SettingType.LDAP_CONTEXT_FACTORY);
                assertEquals("Returned value must correspond to default value",
                        SettingType.LDAP_CONTEXT_FACTORY.getDefaultValue(),
                        value);
                return null;
            }
        });
    }

    @Test
    public void getDefaultValueForSetting_platformPropertyForKeyDefined_propertyHasNoDefault()
            throws Throwable {
        // create another platform setting
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_LAST_NAME,
                        "Smith");
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                String value = ldapSettingsMgmtSvc
                        .getDefaultValueForSetting(SettingType.LDAP_ATTR_LAST_NAME);
                assertEquals(
                        "Returned value must be empty String so that property is linked to platform setting",
                        "", value);
                return null;
            }
        });
    }

    @Test
    public void getDefaultValueForSetting_platformPropertyForKeyDefined_propertyHasDefault()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                String value = ldapSettingsMgmtSvc
                        .getDefaultValueForSetting(SettingType.LDAP_CONTEXT_FACTORY);
                assertEquals(
                        "Returned value must be empty String so that property is linked to platform setting",
                        "", value);
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_noOrgPropertiesDefined_noPlatformPropertiesDefined()
            throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();
        assertEquals("No platform platform settings must be defined", 0,
                getPlatformSettings(null).size());
        assertEquals("No organization-specific settings must be defined", 0,
                getOrganizationSettings(customerOrg2, null).size());

        container.login(customerOrgAdmin2.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();
                assertEquals("No attributes must be returned", 0,
                        mappedAttributes.size());
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_noOrgPropertiesDefined_noPlatformPropertiesDefined_platformOp()
            throws Throwable {
        // remove all existing platform settings
        clearAllPlatformSettings();
        assertEquals("No platform settings must be defined", 0,
                getPlatformSettings(null).size());
        assertEquals("No organization-specific settings must be defined", 0,
                getOrganizationSettings(customerOrg2, null).size());

        container.login(platformOrgAdmin.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();

                assertEquals("No attributes must be returned", 0,
                        mappedAttributes.size());
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_noOrgPropertiesDefined_platformPropertiesDefined()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_UID, "uid");
                return null;
            }
        });

        assertEquals("No organization-specific properties must be defined", 0,
                getOrganizationSettings(customerOrg2, null).size());

        assertEquals(
                "Two platform properties must be defined, one in set-up, one in test",
                2, getPlatformSettings(null).size());

        container.login(customerOrgAdmin2.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();

                assertEquals(
                        "Return list must be empty (because no organization property was defined)",
                        0, mappedAttributes.size());
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_noOrgPropertiesDefined_platformPropertiesDefined_platformOp()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_UID, "uid");
                return null;
            }
        });

        assertEquals("No organization-specific properties must be defined", 0,
                getOrganizationSettings(customerOrg2, null).size());

        assertEquals(
                "Two platform properties must be defined, one in set-up, one in test",
                2, getPlatformSettings(null).size());

        container.login(platformOrgAdmin.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();

                assertEquals(
                        "Only platform setting defined in test must be returned",
                        1, mappedAttributes.size());
                assertTrue(
                        "Returned value must correspond to value set in test",
                        mappedAttributes.contains(SettingType.LDAP_ATTR_UID));
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_orgPropertiesDefined_platformPropertiesDefined_distinct()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_UID, "uid");
                createAndStoreOrganizationSettingForCustomerOrg(
                        SettingType.LDAP_BASE_DN, "baseDN");
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();

                assertEquals(
                        "Return list must consist of one BES-specific organization setting defined during setup, but base DN should be ignored",
                        1, mappedAttributes.size());
                assertTrue(
                        "Returned value for setting must correspond to organization setting value set during setup",
                        mappedAttributes.contains(SettingType.LDAP_ATTR_LOCALE));
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_orgPropertiesDefined_platformPropertiesDefined_distinct_platformOp()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_UID, "uid");
                createAndStoreOrganizationSettingForCustomerOrg(
                        SettingType.LDAP_BASE_DN, "baseDN");
                return null;
            }
        });

        container.login(platformOrgAdmin.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();
                assertEquals(
                        "Return list must consist of the defined platform setting during test",
                        1, mappedAttributes.size());
                assertTrue(
                        "Returned value for setting must correspond to organization setting value set during setup",
                        mappedAttributes.contains(SettingType.LDAP_ATTR_UID));
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_orgPropertyDefinedWithKeyAndValue_platformPropertyDefinedWithSameKey()
            throws Throwable {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_LOCALE,
                        "de");
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();

                assertEquals(
                        "Return list must consist of one BES-specific organization setting defined during setup",
                        1, mappedAttributes.size());
                assertTrue(
                        "Returned value for setting must correspond to organization setting value set during setup",
                        mappedAttributes.contains(SettingType.LDAP_ATTR_LOCALE));
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_orgPropertyDefinedWithKeyAndValue_platformPropertyDefinedWithSameKey_platformOp()
            throws Throwable {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_LOCALE,
                        "de");
                return null;
            }
        });

        container.login(platformOrgAdmin.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();
                assertEquals(
                        "Return list must consist of the defined platform setting during test",
                        1, mappedAttributes.size());
                assertTrue(
                        "Returned value for setting must correspond to organization setting value set during setup",
                        mappedAttributes.contains(SettingType.LDAP_ATTR_LOCALE));
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_orgPropertyDefinedWithKeyButNoValue_platformPropertyDefinedWithSameKey()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_UID, "uid");
                createAndStoreOrganizationSettingForCustomerOrg(
                        SettingType.LDAP_ATTR_UID, "");
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();

                assertEquals(
                        "Return list must consist of BES-specific organization settings defined during test and setup",
                        2, mappedAttributes.size());
                assertTrue(
                        "Returned value for setting must correspond to organization setting value set during test",
                        mappedAttributes.contains(SettingType.LDAP_ATTR_UID));
                assertTrue(
                        "Returned value for setting must correspond to organization setting value set during test",
                        mappedAttributes.contains(SettingType.LDAP_ATTR_LOCALE));
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_orgPropertyDefinedWithKeyButNoValue_platformPropertyDefinedWithSameKey_platformOp()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStorePlatformSetting(SettingType.LDAP_ATTR_UID, "uid");
                createAndStoreOrganizationSettingForCustomerOrg(
                        SettingType.LDAP_ATTR_UID, "");
                return null;
            }
        });

        container.login(platformOrgAdmin.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();
                assertEquals(
                        "Return list must consist of the defined platform setting during test",
                        1, mappedAttributes.size());
                assertTrue(
                        "Returned value for setting must correspond to organization setting value set during test",
                        mappedAttributes.contains(SettingType.LDAP_ATTR_UID));
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_orgPropertyDefinedWithKeyButNoValue_noPlatformPropertyDefinedWithSameKey()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStoreOrganizationSettingForCustomerOrg(
                        SettingType.LDAP_ATTR_UID, "");
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();

                assertEquals(
                        "Return list must contain only of setting defined during setup because linked org setting defined during test has no corresponding platform setting",
                        1, mappedAttributes.size());
                assertTrue(
                        "Return list must be empty because linked org setting has no corresponding platform setting",
                        !mappedAttributes.contains(SettingType.LDAP_ATTR_UID));
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_orgPropertyDefinedWithKeyButNoValue_noPlatformPropertyDefinedWithSameKey_platformOp()
            throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createAndStoreOrganizationSettingForCustomerOrg(
                        SettingType.LDAP_ATTR_UID, "");
                return null;
            }
        });

        container.login(platformOrgAdmin.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();

                assertEquals(
                        "Return list must be empty because platform setting defined in setup is not bes-specific",
                        0, mappedAttributes.size());
                return null;
            }
        });
    }

    @Test
    public void getMappedAttributes_additionalNonBesAttributes()
            throws Throwable {
        container.login(customerOrgAdmin2.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<OrganizationSetting> types = new ArrayList<OrganizationSetting>();
                for (SettingType settings : SettingType.values()) {
                    OrganizationSetting newOrgSetting = new OrganizationSetting();
                    newOrgSetting.setOrganization(customerOrg2);
                    newOrgSetting.setSettingType(settings);
                    newOrgSetting.setSettingValue("bla");
                    ds.persist(newOrgSetting);
                    types.add(newOrgSetting);
                }
                try {
                    customerOrg2.setOrganizationSettings(types);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Set<SettingType> mappedAttributes = ldapSettingsMgmtSvc
                        .getMappedAttributes();
                assertEquals(
                        "Returned list must only contain bes-specific attributes",
                        SettingType.LDAP_ATTRIBUTES.size(),
                        mappedAttributes.size());
                for (SettingType setting : mappedAttributes) {
                    Assert.assertTrue(SettingType.LDAP_ATTRIBUTES
                            .contains(setting));
                }
                return null;
            }
        });
    }

    // get all or a specific organisationSetting for a given organization
    private List<OrganizationSetting> getOrganizationSettings(
            final Organization org, final SettingType type) throws Throwable {
        try {
            return runTX(new Callable<List<OrganizationSetting>>() {
                @Override
                @SuppressWarnings("unchecked")
                public List<OrganizationSetting> call() throws Exception {
                    Query query = ds
                            .createQuery("SELECT obj FROM OrganizationSetting obj WHERE "
                                    + (type != null ? "obj.dataContainer.settingType = :settingType and "
                                            : "")
                                    + "obj.organization = :organization");
                    query.setParameter("organization", org);
                    if (type != null) {
                        query.setParameter("settingType", type);
                    }
                    return query.getResultList();
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }

    private List<PlatformSetting> getPlatformSettings(final SettingType type)
            throws Throwable {
        try {
            return runTX(new Callable<List<PlatformSetting>>() {
                @Override
                @SuppressWarnings("unchecked")
                public List<PlatformSetting> call() throws Exception {
                    Query query = ds
                            .createQuery("SELECT obj FROM PlatformSetting obj"
                                    + (type != null ? " WHERE obj.dataContainer.settingType = :settingType"
                                            : ""));
                    if (type != null) {
                        query.setParameter("settingType", type);
                    }
                    return query.getResultList();
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }

    private void clearAllPlatformSettings() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = ds.createNamedQuery("PlatformSetting.removeAll");
                query.executeUpdate();
                return null;
            }
        });
    }

    private void createAndStorePlatformSetting(SettingType type, String value)
            throws NonUniqueBusinessKeyException {
        PlatformSetting pfSetting = new PlatformSetting();
        pfSetting.setSettingType(type);
        pfSetting.setSettingValue(value);
        ds.persist(pfSetting);
    }

    private void createAndStoreOrganizationSettingForCustomerOrg(
            SettingType type, String value)
            throws NonUniqueBusinessKeyException {
        OrganizationSetting newOrgSetting = new OrganizationSetting();
        newOrgSetting.setOrganization(customerOrg);
        newOrgSetting.setSettingType(type);
        newOrgSetting.setSettingValue(value);
        ds.persist(newOrgSetting);
        try {
            customerOrg
                    .setOrganizationSettings(Arrays.asList(
                            getOrganizationSettings(customerOrg,
                                    SettingType.LDAP_ATTR_LOCALE).get(0),
                            newOrgSetting));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getLdapManagedOrganization_NoneExisting() throws Exception {
        Set<Organization> result = runTX(new Callable<Set<Organization>>() {
            @Override
            public Set<Organization> call() throws Exception {
                return ldapSettingsMgmtSvc.getLdapManagedOrganizations();
            }
        });
        assertTrue(result.isEmpty());
    }

    @Test
    public void getLdapManagedOrganization_OneExisting() throws Exception {
        Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                org.setRemoteLdapActive(true);
                return org;
            }
        });
        Set<Organization> result = runTX(new Callable<Set<Organization>>() {
            @Override
            public Set<Organization> call() throws Exception {
                return ldapSettingsMgmtSvc.getLdapManagedOrganizations();
            }
        });
        assertEquals(1, result.size());
        Organization entry = result.iterator().next();
        assertEquals(org.getKey(), entry.getKey());
        assertEquals(org.getVersion(), entry.getVersion());
        assertEquals(org.getName(), entry.getName());
        assertEquals(org.getOrganizationId(), entry.getOrganizationId());
    }

}
