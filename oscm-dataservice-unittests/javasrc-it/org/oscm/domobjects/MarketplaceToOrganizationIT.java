/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 12.09.2011                                                      
 *                                                                              
 *  Completion Time: 12.09.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;

/**
 * @author weiser
 * 
 */
public class MarketplaceToOrganizationIT extends DomainObjectTestBase {

    private Organization org;
    private Marketplace mp;
    private MarketplaceToOrganization mpToOrg;

    @Override
    protected void dataSetup() throws Exception {
        org = Organizations.createOrganization(mgr);
        mp = Marketplaces.createGlobalMarketplace(org, "mpid", mgr);
    }

    @Test
    public void testAdd() throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAddCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testAdd_Duplicate() throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testModify() throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            final Organization newOrg = runTX(new Callable<Organization>() {
                @Override
                public Organization call() throws Exception {
                    MarketplaceToOrganization ref = mgr.getReference(
                            MarketplaceToOrganization.class, mpToOrg.getKey());
                    Organization org = Organizations.createOrganization(mgr);
                    ref.setOrganization(org);
                    ref.setPublishingAccess(PublishingAccess.PUBLISHING_ACCESS_GRANTED);
                    return org;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyCheck(newOrg);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testDelete() throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    MarketplaceToOrganization ref = mgr.getReference(
                            MarketplaceToOrganization.class, mpToOrg.getKey());
                    mgr.remove(ref);
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    protected void doTestDeleteCheck() {
        Assert.assertNull(mgr.find(MarketplaceToOrganization.class,
                mpToOrg.getKey()));

    }

    protected void doTestAdd() throws Exception {
        org = mgr.getReference(Organization.class, org.getKey());
        mp = mgr.getReference(Marketplace.class, mp.getKey());
        mpToOrg = new MarketplaceToOrganization(mp, org);
        mpToOrg.setPublishingAccess(PublishingAccess.PUBLISHING_ACCESS_DENIED);
        mgr.persist(mpToOrg);
    }

    protected void doTestAddCheck() throws Exception {
        MarketplaceToOrganization ref = mgr.getReference(
                MarketplaceToOrganization.class, mpToOrg.getKey());
        Assert.assertEquals(org.getKey(), ref.getOrganization().getKey());
        Assert.assertEquals(mp.getKey(), ref.getMarketplace().getKey());
        Assert.assertEquals(0, ref.getVersion());
        Assert.assertEquals(PublishingAccess.PUBLISHING_ACCESS_DENIED,
                ref.getPublishingAccess());

        Assert.assertEquals(PublishingAccess.PUBLISHING_ACCESS_DENIED,
                ref.getPublishingAccess());
    }

    protected void doTestModifyCheck(Organization newOrg) throws Exception {
        MarketplaceToOrganization ref = mgr.getReference(
                MarketplaceToOrganization.class, mpToOrg.getKey());
        Assert.assertEquals(newOrg.getKey(), ref.getOrganization().getKey());
        Assert.assertEquals(mp.getKey(), ref.getMarketplace().getKey());
        Assert.assertEquals(1, ref.getVersion());
        Assert.assertEquals(PublishingAccess.PUBLISHING_ACCESS_GRANTED,
                ref.getPublishingAccess());

        Assert.assertEquals(PublishingAccess.PUBLISHING_ACCESS_GRANTED,
                ref.getPublishingAccess());
    }

}
