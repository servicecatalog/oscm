/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 31.08.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Tests for the domain object representing supported country. Only a find by
 * business-key is tested, because all country objects are created during setup.
 * 
 * @author Held
 * 
 */
public class SupportedCountryIT extends DomainObjectTestBase {

    /**
     * Search for a SupportedCountry. All country objects are created during DB
     * setup.
     * 
     * @throws Exception
     */
    @Test
    public void testFind() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doSetup();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doFindByBK();
                return null;
            }
        });

    }

    private void doSetup() throws NonUniqueBusinessKeyException {
        SupportedCountry sc = new SupportedCountry();
        sc.setCountryISOCode("DE");
        mgr.persist(sc);
    }

    private void doFindByBK() {
        SupportedCountry sc = new SupportedCountry();
        sc.setCountryISOCode("DE");
        sc = (SupportedCountry) mgr.find(sc);

        Assert.assertNotNull("Object not found", sc);
        Assert.assertEquals("Wrong country code", "DE", sc.getCountryISOCode());
    }

}
