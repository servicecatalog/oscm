/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

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

public class PlatformSettingIT extends DomainObjectTestBase {

    private static final String MAIL = "mail";
    private List<PlatformSetting> createdSettings = new ArrayList<PlatformSetting>();

    @Test
    public void testUniqueConstrainViolation() throws Exception {
        final PlatformSetting pfSetting1 = new PlatformSetting();
        pfSetting1.setSettingType(SettingType.LDAP_ATTR_EMAIL);
        pfSetting1.setSettingValue("value1");
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                mgr.persist(pfSetting1);
                return null;
            }
        });
        ;

        final PlatformSetting pfSetting2 = new PlatformSetting();
        pfSetting2.setSettingType(SettingType.LDAP_ATTR_EMAIL);
        pfSetting2.setSettingValue("value2");
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                try {
                    mgr.persist(pfSetting2);
                    fail();
                } catch (NonUniqueBusinessKeyException ex) {

                }
                return null;
            }
        });
    }

    @Test
    public void testAdd() throws Exception {
        final PlatformSetting platformSetting = new PlatformSetting();
        platformSetting.setSettingType(SettingType.LDAP_ATTR_EMAIL);
        assertEquals(SettingType.LDAP_ATTR_EMAIL,
                platformSetting.getSettingType());
        String testValue = "test";
        platformSetting.setSettingValue(testValue);
        assertEquals(testValue, platformSetting.getSettingValue());
        platformSetting.setSettingValue(MAIL);
        assertEquals(MAIL, platformSetting.getSettingValue());
        final long key = runTX(new Callable<Long>() {
            public Long call() throws Exception {
                mgr.persist(platformSetting);
                createdSettings.add(platformSetting);
                return Long.valueOf(platformSetting.getKey());
            }
        }).longValue();
        assertTrue(key > 0);
        PlatformSetting loadedSetting = runTX(new Callable<PlatformSetting>() {
            public PlatformSetting call() throws Exception {
                PlatformSetting setting = mgr.getReference(
                        PlatformSetting.class, key);
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
                PlatformSetting orgSetting = mgr.getReference(
                        PlatformSetting.class, createdSettings.get(0).getKey());
                mgr.remove(orgSetting);
                return null;
            }
        });
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    mgr.getReference(PlatformSetting.class, createdSettings
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
    public void testGetAllQuery() throws Throwable {
        testAdd();
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    Query query = mgr
                            .createNamedQuery("PlatformSetting.getAll");
                    List<?> list = query.getResultList();
                    assertEquals(1, list.size());
                    assertTrue(list.get(0) instanceof PlatformSetting);
                    PlatformSetting setting = (PlatformSetting) list.get(0);
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
    public void testFindByBusinessKeyQuery() throws Throwable {
        testAdd();
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    Query query = mgr
                            .createNamedQuery("PlatformSetting.findByBusinessKey");
                    query.setParameter("settingType",
                            SettingType.LDAP_ATTR_EMAIL);
                    List<?> list = query.getResultList();
                    assertEquals(1, list.size());
                    assertTrue(list.get(0) instanceof PlatformSetting);
                    PlatformSetting setting = (PlatformSetting) list.get(0);
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
    public void testRemoveAllQuery() throws Throwable {
        testAdd();
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    Query query = mgr
                            .createNamedQuery("PlatformSetting.removeAll");
                    query.executeUpdate();

                    // now execute already tested query to verify DB is empty
                    query = mgr.createNamedQuery("PlatformSetting.getAll");
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
