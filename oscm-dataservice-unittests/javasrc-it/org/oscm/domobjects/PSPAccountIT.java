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
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;

/**
 * Tests for the PSP account domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PSPAccountIT extends DomainObjectTestBase {

    private PSP psp;
    private Organization org;
    private PSPAccount account;

    @Before
    public void setUp() throws Exception {
        psp = createPSPAndOrg();
        account = createPSPAccount();
    }

    @After
    public void tearDown() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                List<DomainHistoryObject<?>> history = mgr.findHistory(account);
                assertTrue(!history.isEmpty());
                PSPAccountHistory ah = (PSPAccountHistory) history.get(history
                        .size() - 1);
                assertTrue(ah.getPspObjKey() > 0);
                assertTrue(ah.getOrganizationObjKey() > 0);
                return null;
            }
        });
    }

    @Test
    public void add() throws Exception {
        verify(ModificationType.ADD, Collections.singletonList(account),
                PSPAccount.class);
    }

    @Test
    public void modify() throws Exception {
        PSPAccount modifiedAccount = runTX(new Callable<PSPAccount>() {
            public PSPAccount call() throws Exception {
                PSPAccount lAccount = mgr.getReference(PSPAccount.class,
                        account.getKey());
                lAccount.setPspIdentifier("pspId2");
                return lAccount;
            }
        });
        verify(ModificationType.MODIFY,
                Collections.singletonList(modifiedAccount), PSPAccount.class);
    }

    @Test
    public void delete() throws Exception {
        PSPAccount deletedAccount = runTX(new Callable<PSPAccount>() {
            public PSPAccount call() throws Exception {
                PSPAccount lAccount = mgr.getReference(PSPAccount.class,
                        account.getKey());
                mgr.remove(lAccount);
                return lAccount;
            }
        });
        verify(ModificationType.DELETE,
                Collections.singletonList(deletedAccount), PSPAccount.class);
    }

    private PSP createPSPAndOrg() throws Exception {
        PSP psp = runTX(new Callable<PSP>() {
            public PSP call() throws Exception {
                PSP psp = new PSP();
                psp.setIdentifier("identifierForPSP");
                psp.setWsdlUrl("wsdlUrl");
                mgr.persist(psp);
                org = new Organization();
                org.setOrganizationId("testOrg");
                org.setCutOffDay(1);
                mgr.persist(org);
                return psp;
            }
        });
        return psp;
    }

    private PSPAccount createPSPAccount() throws Exception {
        return runTX(new Callable<PSPAccount>() {
            public PSPAccount call() throws Exception {
                PSPAccount account = new PSPAccount();
                account.setPspIdentifier("pspIdentifier");
                account.setPsp(psp);
                account.setOrganization(org);
                mgr.persist(account);
                return account;
            }
        });
    }

}
