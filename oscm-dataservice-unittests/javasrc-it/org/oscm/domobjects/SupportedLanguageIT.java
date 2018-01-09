/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-11-7                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Test;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Unit test for SupportedLanguage
 * 
 * @author Chen
 * 
 */
public class SupportedLanguageIT extends DomainObjectTestBase {

    private final List<DomainObjectWithVersioning<?>> domObjects = new ArrayList<DomainObjectWithVersioning<?>>();

    private final String ISOCODE_DE = "de";

    private final String ISOCODE_EN = "en";

    /**
     * Search for a SupportedLanguage. All language objects are created during
     * DB setup.
     * 
     * @throws Exception
     */
    @Test
    public void testFind() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doSetup();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doFindByStatus();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doFindByBK();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doFindForDefault();
                return null;
            }
        });

    }

    @Test(expected = ObjectNotFoundException.class)
    public void testFind_Error() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doSetup();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doFindByBK_Wrong();
                return null;
            }
        });

    }

    private void doSetup() throws NonUniqueBusinessKeyException {
        SupportedLanguage sc = new SupportedLanguage();
        sc.setLanguageISOCode(ISOCODE_EN);
        sc.setActiveStatus(true);
        sc.setDefaultStatus(true);
        mgr.persist(sc);

        sc = new SupportedLanguage();
        sc.setLanguageISOCode(ISOCODE_DE);
        sc.setActiveStatus(true);
        sc.setDefaultStatus(false);
        mgr.persist(sc);

        sc = new SupportedLanguage();
        sc.setLanguageISOCode("te");
        sc.setActiveStatus(false);
        sc.setDefaultStatus(false);
        mgr.persist(sc);
    }

    @Test
    public void testModify() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doSetup();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModify();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doFindByBK() throws ObjectNotFoundException {
        SupportedLanguage l = new SupportedLanguage();
        l.setLanguageISOCode(ISOCODE_EN);
        l = (SupportedLanguage) mgr.getReferenceByBusinessKey(l);

        assertNotNull("Object not found", l);
        assertEquals("Wrong country code", ISOCODE_EN, l.getLanguageISOCode());
    }

    private void doFindByBK_Wrong() throws ObjectNotFoundException {
        SupportedLanguage l = new SupportedLanguage();
        l.setLanguageISOCode("ee");
        l = (SupportedLanguage) mgr.getReferenceByBusinessKey(l);
    }

    private void doFindByStatus() {
        List<SupportedLanguage> ls = findAll(mgr);

        assertNotNull("Object not found", ls);
        assertEquals(3, ls.size());
        assertEquals(Boolean.valueOf(true),
                Boolean.valueOf(ls.get(0).getDefaultStatus()));
        assertEquals(ISOCODE_EN, ls.get(0).getLanguageISOCode());
        assertEquals(Boolean.valueOf(true),
                Boolean.valueOf(ls.get(1).getActiveStatus()));
        assertEquals(ISOCODE_DE, ls.get(1).getLanguageISOCode());
        assertEquals(Boolean.valueOf(false),
                Boolean.valueOf(ls.get(2).getActiveStatus()));
        assertEquals("te", ls.get(2).getLanguageISOCode());

        ls = findAllActive(mgr);

        assertNotNull("Object not found", ls);
        assertEquals(2, ls.size());
        assertEquals(Boolean.valueOf(true),
                Boolean.valueOf(ls.get(0).getDefaultStatus()));
        assertEquals(ISOCODE_EN, ls.get(0).getLanguageISOCode());
        assertEquals(Boolean.valueOf(true),
                Boolean.valueOf(ls.get(1).getActiveStatus()));
        assertEquals(ISOCODE_DE, ls.get(1).getLanguageISOCode());
    }

    private void doFindForDefault() throws ObjectNotFoundException {
        String isocode = findDefault(mgr);

        assertNotNull("Object not found", isocode);
        assertEquals("Wrong country code", ISOCODE_EN, isocode);
    }

    private void doTestModify() {
        domObjects.clear();
        SupportedLanguage saved = new SupportedLanguage();
        saved.setLanguageISOCode(ISOCODE_DE);
        saved = (SupportedLanguage) mgr.find(saved);
        saved.setActiveStatus(false);
        domObjects.add(saved);
    }

    protected void doTestModifyCheck() {
        SupportedLanguage saved = new SupportedLanguage();
        saved.setLanguageISOCode(ISOCODE_DE);
        saved = (SupportedLanguage) mgr.find(saved);

        assertEquals(Boolean.valueOf(false),
                Boolean.valueOf(saved.getActiveStatus()));

    }

    /**
     * get language by status
     * 
     * @param ds
     * @param supportstatus
     * @param activestatus
     * @return
     */
    private static List<SupportedLanguage> findAll(DataService ds) {

        Query query = ds.createNamedQuery("SupportedLanguage.findAll");

        List<SupportedLanguage> result = ParameterizedTypes.list(
                query.getResultList(), SupportedLanguage.class);

        return result;
    }

    /**
     * get language by status
     * 
     * @param ds
     * @param supportstatus
     * @param activestatus
     * @return
     */
    private static List<SupportedLanguage> findAllActive(DataService ds) {

        Query query = ds.createNamedQuery("SupportedLanguage.findAllActive");

        List<SupportedLanguage> result = ParameterizedTypes.list(
                query.getResultList(), SupportedLanguage.class);

        return result;
    }

    /**
     * 
     * @param ds
     * @return
     * @throws NonUniqueBusinessKeyException
     */
    private String findDefault(DataService ds) throws ObjectNotFoundException {
        Query query = ds.createNamedQuery("SupportedLanguage.findDefault");

        List<String> result = ParameterizedTypes.list(query.getResultList(),
                String.class);
        if (result == null || result.size() != 1) {
            throw new ObjectNotFoundException();
        }
        return result.get(0);
    }

}
