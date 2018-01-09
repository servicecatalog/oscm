/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.TIMESTAMP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

public class OrganisationSettingIT extends DomainObjectTestBase {

    private static final String MAIL = "mail";
    private List<OrganizationSetting> createdSettings = new ArrayList<OrganizationSetting>();
    private Organization createdOrganization;

    @Override
    protected void dataSetup() throws Exception {
        createdOrganization = new Organization();
        createdOrganization.setOrganizationId("OrganisationSettingTest");
        createdOrganization.setRegistrationDate(TIMESTAMP);
        createdOrganization.setCutOffDay(1);
        mgr.persist(createdOrganization);
    }

    @Test
    public void testUniqueConstrainViolation() throws Exception {
        final OrganizationSetting orgSetting1 = new OrganizationSetting();
        orgSetting1.setSettingType(SettingType.LDAP_ATTR_EMAIL);
        orgSetting1.setSettingValue("value1");
        runTX(new Callable<Long>() {
            public Long call() throws Exception {
                createdOrganization = mgr.getReference(Organization.class,
                        createdOrganization.getKey());
                orgSetting1.setOrganization(createdOrganization);
                mgr.persist(orgSetting1);
                return Long.valueOf(orgSetting1.getKey());
            }
        }).longValue();

        final OrganizationSetting orgSetting2 = new OrganizationSetting();
        orgSetting2.setSettingType(SettingType.LDAP_ATTR_EMAIL);
        orgSetting2.setSettingValue("value2");
        runTX(new Callable<Long>() {
            public Long call() throws Exception {
                createdOrganization = mgr.getReference(Organization.class,
                        createdOrganization.getKey());
                orgSetting2.setOrganization(createdOrganization);
                try {
                    mgr.persist(orgSetting2);
                    mgr.flush();
                    fail();
                } catch (NonUniqueBusinessKeyException e) {

                }
                return Long.valueOf(orgSetting2.getKey());
            }
        }).longValue();
    }

    @Test
    public void testReferenceByBusinessKey() throws Exception {
        // first create mail setting
        final OrganizationSetting orgSetting1 = new OrganizationSetting();
        orgSetting1.setSettingType(SettingType.LDAP_ATTR_EMAIL);
        orgSetting1.setSettingValue("myEmail");
        runTX(new Callable<Long>() {
            public Long call() throws Exception {
                createdOrganization = mgr.getReference(Organization.class,
                        createdOrganization.getKey());
                orgSetting1.setOrganization(createdOrganization);
                mgr.persist(orgSetting1);
                return Long.valueOf(orgSetting1.getKey());
            }
        }).longValue();

        // now try to retrieve it
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                OrganizationSetting setting = new OrganizationSetting();
                setting.setOrganization(createdOrganization);
                setting.setSettingType(SettingType.LDAP_ATTR_EMAIL);
                setting = (OrganizationSetting) mgr
                        .getReferenceByBusinessKey(setting);

                assertEquals(SettingType.LDAP_ATTR_EMAIL,
                        setting.getSettingType());
                assertEquals("myEmail", setting.getSettingValue());
                return null;
            }
        });
    }

    @Test
    public void testAdd() throws Exception {
        final OrganizationSetting organisationSetting = new OrganizationSetting();
        organisationSetting.setSettingType(SettingType.LDAP_ATTR_EMAIL);
        assertEquals(SettingType.LDAP_ATTR_EMAIL,
                organisationSetting.getSettingType());
        String testValue = "test";
        organisationSetting.setSettingValue(testValue);
        assertEquals(testValue, organisationSetting.getSettingValue());
        organisationSetting.setSettingValue(MAIL);
        assertEquals(MAIL, organisationSetting.getSettingValue());
        final long key = runTX(new Callable<Long>() {
            public Long call() throws Exception {
                createdOrganization = mgr.getReference(Organization.class,
                        createdOrganization.getKey());
                organisationSetting.setOrganization(createdOrganization);
                mgr.persist(organisationSetting);
                createdSettings.add(organisationSetting);
                return Long.valueOf(organisationSetting.getKey());
            }
        }).longValue();
        assertTrue(key > 0);
        OrganizationSetting loadedSetting = runTX(new Callable<OrganizationSetting>() {
            public OrganizationSetting call() throws Exception {
                OrganizationSetting setting = mgr.getReference(
                        OrganizationSetting.class, key);
                setting.getOrganization();
                setting.getSettingType();
                return setting;
            }
        });
        assertEquals(SettingType.LDAP_ATTR_EMAIL,
                loadedSetting.getSettingType());
        assertEquals(MAIL, loadedSetting.getSettingValue());

    }

    @Test(expected = ObjectNotFoundException.class)
    public void testRemove() throws Throwable {
        testAdd();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                OrganizationSetting orgSetting = mgr.getReference(
                        OrganizationSetting.class, createdSettings.get(0)
                                .getKey());
                mgr.remove(orgSetting);
                return null;
            }
        });
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    mgr.getReference(OrganizationSetting.class, createdSettings
                            .get(0).getKey());
                    return null;
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

    @Test
    public void testFindByBusinessKeyQuery() throws Throwable {
        testAdd();
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    Query query = mgr
                            .createNamedQuery("OrganizationSetting.findByBusinessKey");
                    query.setParameter("organization", createdOrganization);
                    query.setParameter("settingType",
                            SettingType.LDAP_ATTR_EMAIL);
                    List<?> list = query.getResultList();
                    assertEquals(1, list.size());
                    assertTrue(list.get(0) instanceof OrganizationSetting);
                    OrganizationSetting setting = (OrganizationSetting) list
                            .get(0);
                    assertEquals(createdOrganization, setting.getOrganization());
                    assertEquals(SettingType.LDAP_ATTR_EMAIL,
                            setting.getSettingType());
                    assertEquals(MAIL, setting.getSettingValue());
                    return null;
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

    @Test
    public void testRemoveAllForOrganizationQuery() throws Throwable {
        testAdd();
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    Query query = mgr
                            .createNamedQuery("OrganizationSetting.removeAllForOrganization");
                    query.setParameter("organization", createdOrganization);

                    query.executeUpdate();

                    // now execute already tested query to verify DB is empty
                    query = mgr
                            .createNamedQuery("OrganizationSetting.findByBusinessKey");
                    query.setParameter("organization", createdOrganization);
                    query.setParameter("settingType",
                            SettingType.LDAP_ATTR_EMAIL);
                    List<?> list = query.getResultList();

                    assertEquals(0, list.size());
                    return null;
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
}
