/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 06.05.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.L123;
import static org.oscm.test.Numbers.TIMESTAMP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.types.enumtypes.PlatformEventIdentifier;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Test of the price event domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PricedEventIT extends DomainObjectTestBase {

    private List<PricedEvent> createdEvents = new ArrayList<PricedEvent>();

    private PriceModel priceModel;
    private Event event;

    @Override
    protected void dataSetup() throws Exception {
        // create a price model

        Organization organization = Organizations.createOrganization(mgr);
        // create technical product
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID", false, ServiceAccessType.LOGIN);

        // create two products for it
        Product prod1 = new Product();
        prod1.setVendor(organization);
        prod1.setProductId("Product1");
        prod1.setTechnicalProduct(tProd);
        prod1.setProvisioningDate(TIMESTAMP);
        prod1.setStatus(ServiceStatus.ACTIVE);
        prod1.setType(ServiceType.TEMPLATE);
        ParameterSet emptyPS1 = new ParameterSet();
        prod1.setParameterSet(emptyPS1);

        // create a price model
        priceModel = new PriceModel();
        prod1.setPriceModel(priceModel);

        // and create an event as well
        Event evt = new Event();
        evt.setEventIdentifier(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        evt.setEventType(EventType.PLATFORM_EVENT);
        mgr.persist(evt);
        mgr.persist(prod1);
        event = evt;
    }

    @Test
    public void testAdd() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestCheckCreation();
                return null;
            }
        });
    }

    @Test
    public void testModify() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestModify();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestCheckModification();
                return null;
            }
        });
    }

    @Test
    public void testDelete() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestRemoval();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doVerifyRemoval();
                return null;
            }
        });
    }

    private void doTestModify() throws Exception {
        PricedEvent evt = mgr.getReference(PricedEvent.class, createdEvents
                .get(0).getKey());
        evt.setEventPrice(new BigDecimal(60));
        createdEvents.remove(0);
        createdEvents.add(0, (PricedEvent) ReflectiveClone.clone(evt));
    }

    private void doTestCheckModification() throws Exception {
        for (PricedEvent event : createdEvents) {
            // check if new value for price field is okay
            PricedEvent savedEvent = mgr.getReference(PricedEvent.class,
                    event.getKey());
            Assert.assertEquals("Modification has not been stored",
                    new BigDecimal(60), savedEvent.getEventPrice());
            Assert.assertTrue(ReflectiveCompare.showDiffs(event, savedEvent),
                    ReflectiveCompare.compare(event, savedEvent));

            // now check the history entries to ensure that they reflect the
            // changes
            List<DomainHistoryObject<?>> history = mgr.findHistory(savedEvent);
            Assert.assertEquals("Wrong number of history entries", 2,
                    history.size());
            // exactly one entry must show the modification type update, check
            // for this and ensure that the event price is correct
            boolean historyEntryFound = false;
            for (DomainHistoryObject<?> hist : history) {
                PricedEventHistory pHist = (PricedEventHistory) hist;
                if (pHist.getModtype() == ModificationType.MODIFY) {
                    Assert.assertEquals("Wrong pricing information",
                            new BigDecimal(60), pHist.getEventPrice());
                    historyEntryFound = true;
                }
            }
            Assert.assertTrue("Modification has not been tracked in history",
                    historyEntryFound);
        }
    }

    private void doTestRemoval() throws ObjectNotFoundException {
        PricedEvent evt = mgr.getReference(PricedEvent.class, createdEvents
                .get(0).getKey());
        mgr.remove(evt);
    }

    private void doVerifyRemoval() {
        try {
            mgr.getReference(PricedEvent.class, createdEvents.get(0).getKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        createdEvents.remove(0);
    }

    private void doTestAdd() throws Exception {
        PricedEvent evt = new PricedEvent();
        evt.setEventPrice(new BigDecimal(50));
        evt.setPriceModel(priceModel);
        evt.setEvent(event);
        mgr.persist(evt);
        createdEvents.add((PricedEvent) ReflectiveClone.clone(evt));
    }

    private void doTestCheckCreation() throws Exception {
        for (PricedEvent event : createdEvents) {
            DomainObject<?> savedEvent = mgr.getReference(PricedEvent.class,
                    event.getKey());
            List<DomainHistoryObject<?>> historizedEvents = mgr
                    .findHistory(savedEvent);
            Assert.assertEquals("Wrong number of history objects found",
                    createdEvents.size(), historizedEvents.size());
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
            Assert.assertTrue(ReflectiveCompare.showDiffs(event, savedEvent),
                    ReflectiveCompare.compare(event, savedEvent));
        }
    }

    @Test
    public void testAddWithSteppedPrice() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        addSteppedPrice();
        List<SteppedPrice> steps = runTX(new Callable<List<SteppedPrice>>() {
            public List<SteppedPrice> call() throws Exception {
                PricedEvent pe = mgr.getReference(PricedEvent.class,
                        createdEvents.get(0).getKey());
                List<SteppedPrice> steppedPrices = pe.getSteppedPrices();
                steppedPrices.size();
                return steppedPrices;
            }
        });
        Assert.assertNotNull(steps);
        Assert.assertEquals(1, steps.size());
        SteppedPrice sp = steps.get(0);
        Assert.assertEquals(new BigDecimal(123), sp.getAdditionalPrice());
        Assert.assertEquals(123, sp.getFreeEntityCount());
        Assert.assertEquals(Long.valueOf(123), sp.getLimit());
        Assert.assertEquals(new BigDecimal(123), sp.getPrice());
        Assert.assertNotNull(sp.getPricedEvent());
    }

    private SteppedPrice addSteppedPrice() throws Exception {
        return runTX(new Callable<SteppedPrice>() {
            public SteppedPrice call() throws Exception {
                PricedEvent pe = mgr.getReference(PricedEvent.class,
                        createdEvents.get(0).getKey());
                SteppedPrice sp = new SteppedPrice();
                sp.setAdditionalPrice(new BigDecimal(123));
                sp.setFreeEntityCount(123);
                sp.setLimit(L123);
                sp.setPrice(new BigDecimal(123));
                sp.setPricedEvent(pe);
                pe.setSteppedPrices(Collections.singletonList(sp));
                return sp;
            }
        });
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeleteWithSteppedPrice() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        final SteppedPrice sp = addSteppedPrice();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                PricedEvent pe = mgr.getReference(PricedEvent.class,
                        createdEvents.get(0).getKey());
                mgr.remove(pe);
                return null;
            }
        });
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    mgr.getReference(SteppedPrice.class, sp.getKey());
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testCopyWithSteppedPrice() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        addSteppedPrice();
        List<SteppedPrice> steps = runTX(new Callable<List<SteppedPrice>>() {
            public List<SteppedPrice> call() throws Exception {
                PricedEvent pe = mgr.getReference(PricedEvent.class,
                        createdEvents.get(0).getKey());
                List<SteppedPrice> steppedPrices = pe.getSteppedPrices();
                steppedPrices.size();
                return steppedPrices;
            }
        });
        PricedEvent copy = runTX(new Callable<PricedEvent>() {
            public PricedEvent call() throws Exception {
                PricedEvent pe = mgr.getReference(PricedEvent.class,
                        createdEvents.get(0).getKey());
                PricedEvent copy = pe.copy(pe.getPriceModel());
                return copy;
            }
        });
        Assert.assertEquals(steps.size(), copy.getSteppedPrices().size());
        SteppedPrice spCopy = copy.getSteppedPrices().get(0);
        Assert.assertEquals(copy, spCopy.getPricedEvent());
    }

}
