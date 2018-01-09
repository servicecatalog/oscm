/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 10.05.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Test of the technical product domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class TechnicalProductIT extends DomainObjectTestBase {

    private List<TechnicalProduct> technicalProducts = new ArrayList<TechnicalProduct>();

    private String organizationId;

    @Test
    public void testAdd() throws Exception {
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
                doTestCheckCreation();
                return null;
            }
        });
    }

    @Test
    public void testModify() throws Exception {
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
                doTestModify();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestCheckModification();
                return null;
            }
        });
    }

    @Test
    public void testDelete() throws Exception {
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
                doTestRemoval();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doVerifyRemoval();
                return null;
            }
        });
    }

    @Test(expected = Exception.class)
    public void testAddDuplicateBusinessKey() throws Exception {
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
    }

    @Test
    public void testFindByBusinessKey() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });

        // now find by business key
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doFindByBusinessKey();
                return null;
            }
        });
    }

    private void doFindByBusinessKey() {
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        organization = (Organization) mgr.find(organization);

        TechnicalProduct product = TechnicalProducts.findTechnicalProduct(mgr,
                organization, "defaultID");
        Assert.assertNotNull("An object reference must be found!", product);
    }

    private void doTestModify() throws Exception {
        TechnicalProduct prod = mgr.getReference(TechnicalProduct.class,
                technicalProducts.get(0).getKey());
        // force loading of lists
        prod.getEvents().size();
        prod.getParameterDefinitions().size();
        prod.setBaseURL("newBaseURL");
        technicalProducts.remove(0);
        technicalProducts
                .add(0, (TechnicalProduct) ReflectiveClone.clone(prod));
    }

    private void doTestCheckModification() throws Exception {
        for (TechnicalProduct product : technicalProducts) {
            // check if new value for base URL field is okay
            TechnicalProduct savedEvent = mgr.getReference(
                    TechnicalProduct.class, product.getKey());
            Assert.assertEquals("Modification has not been stored",
                    "newBaseURL", savedEvent.getBaseURL());
            Assert.assertTrue(ReflectiveCompare.showDiffs(product, savedEvent),
                    ReflectiveCompare.compare(product, savedEvent));

            // now check the history entries to ensure that they reflect the
            // changes
            List<DomainHistoryObject<?>> history = mgr.findHistory(savedEvent);
            Assert.assertEquals("Wrong number of history entries", 2,
                    history.size());
            // exactly one entry must show the modification type update, check
            // for this and ensure that the base URL is correct
            boolean historyEntryFound = false;
            for (DomainHistoryObject<?> hist : history) {
                TechnicalProductHistory pHist = (TechnicalProductHistory) hist;
                if (pHist.getModtype() == ModificationType.MODIFY) {
                    Assert.assertEquals("Wrong baseURL", "newBaseURL",
                            pHist.getBaseURL());
                    historyEntryFound = true;
                }
            }
            Assert.assertTrue("Modification has not been tracked in history",
                    historyEntryFound);
        }
    }

    private void doTestRemoval() throws ObjectNotFoundException {
        TechnicalProduct prod = mgr.getReference(TechnicalProduct.class,
                technicalProducts.get(0).getKey());
        mgr.remove(prod);
    }

    private void doVerifyRemoval() {
        try {
            mgr.getReference(TechnicalProduct.class, technicalProducts.get(0)
                    .getKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        technicalProducts.remove(0);
    }

    private void doTestAdd() throws Exception {
        Organization organization;
        if (organizationId == null) {
            organization = Organizations.createOrganization(mgr);
            organizationId = organization.getOrganizationId();
        } else {
            organization = Organizations.findOrganization(mgr, organizationId);
        }
        TechnicalProduct prod = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "defaultID", false, ServiceAccessType.LOGIN);
        technicalProducts.add((TechnicalProduct) ReflectiveClone.clone(prod));
    }

    private void doTestCheckCreation() throws Exception {
        for (TechnicalProduct product : technicalProducts) {
            DomainObject<?> savedEvent = mgr.getReference(
                    TechnicalProduct.class, product.getKey());
            List<DomainHistoryObject<?>> historizedEvents = mgr
                    .findHistory(savedEvent);
            Assert.assertEquals("Wrong number of history objects found",
                    technicalProducts.size(), historizedEvents.size());
            if (historizedEvents.size() > 0) {
                DomainHistoryObject<?> hist = historizedEvents.get(0);
                Assert.assertEquals(ModificationType.ADD, hist.getModtype());
                Assert.assertEquals("modUser", "guest", hist.getModuser());
                Assert.assertTrue(
                        ReflectiveCompare.showDiffs(savedEvent, hist),
                        ReflectiveCompare.compare(savedEvent, hist));
                Assert.assertEquals("OBJID in history different",
                        savedEvent.getKey(), hist.getObjKey());
            }
            // now compare the objects themselves
            Assert.assertTrue(ReflectiveCompare.showDiffs(product, savedEvent),
                    ReflectiveCompare.compare(product, savedEvent));
        }
    }
}
