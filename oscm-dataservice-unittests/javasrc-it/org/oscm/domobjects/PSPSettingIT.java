/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 06.10.2011                                                      
 *                                                                              
 *  Completion Time: 06.10.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;

/**
 * Tests for the PSP setting domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PSPSettingIT extends DomainObjectTestBase {

    private PSP psp;

    @Before
    public void setUp() throws Exception {
        psp = createPSP();
    }

    @After
    public void tearDown() throws Exception {
        runTX(new Callable<PSP>() {
            public PSP call() throws Exception {
                // ensure that the history object's reference key to the PSP
                // element is correct
                Query query = mgr
                        .createQuery("SELECT count(psp) FROM PSPHistory psp, PSPSettingHistory setting WHERE psp.objKey = setting.pspObjKey");
                Long count = (Long) query.getSingleResult();
                assertTrue(count.longValue() > 0);
                return null;
            }
        });
    }

    @Test
    public void add() throws Exception {
        PSPSetting setting = createPSPSetting();
        verify(ModificationType.ADD, Collections.singletonList(setting),
                PSPSetting.class);
    }

    @Test
    public void modify() throws Exception {
        final PSPSetting setting = createPSPSetting();
        PSPSetting modifiedSetting = runTX(new Callable<PSPSetting>() {
            public PSPSetting call() throws Exception {
                PSPSetting lSetting = mgr.getReference(PSPSetting.class,
                        setting.getKey());
                lSetting.setSettingKey("settingKey2");
                lSetting.setSettingValue("settingValue2");
                lSetting.setPsp(psp);
                return lSetting;
            }
        });
        verify(ModificationType.MODIFY,
                Collections.singletonList(modifiedSetting), PSPSetting.class);
    }

    @Test
    public void delete() throws Exception {
        final PSPSetting setting = createPSPSetting();
        PSPSetting deletedSetting = runTX(new Callable<PSPSetting>() {
            public PSPSetting call() throws Exception {
                PSPSetting lSetting = mgr.getReference(PSPSetting.class,
                        setting.getKey());
                mgr.remove(lSetting);
                return lSetting;
            }
        });
        verify(ModificationType.DELETE,
                Collections.singletonList(deletedSetting), PSPSetting.class);
    }

    private PSP createPSP() throws Exception {
        PSP psp = runTX(new Callable<PSP>() {
            public PSP call() throws Exception {
                PSP psp = new PSP();
                psp.setIdentifier("identifierForPSP");
                psp.setWsdlUrl("wsdlUrl");
                mgr.persist(psp);
                return psp;
            }
        });
        return psp;
    }

    private PSPSetting createPSPSetting() throws Exception {
        PSPSetting setting = runTX(new Callable<PSPSetting>() {
            public PSPSetting call() throws Exception {
                PSPSetting setting = new PSPSetting();
                setting.setSettingKey("settingKey");
                setting.setSettingValue("settingValue");
                setting.setPsp(psp);
                mgr.persist(setting);
                return setting;
            }
        });
        return setting;
    }

}
