/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 06.05.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.L10;

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
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Test of the price event domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class EventIT extends DomainObjectTestBase {

    private List<Event> createdEvents = new ArrayList<Event>();

    @Test
    public void testAdd() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd(null);
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
    public void testDelete() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd(null);
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

    private void doTestRemoval() throws ObjectNotFoundException {
        Event evt = mgr
                .getReference(Event.class, createdEvents.get(0).getKey());
        mgr.remove(evt);
    }

    private void doVerifyRemoval() {
        try {
            mgr.getReference(Event.class, createdEvents.get(0).getKey());
            Assert.fail("Event not removed!");
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        createdEvents.remove(0);
    }

    private Event doTestAdd(Long historyModDate) throws Exception {
        Event evt = new Event();
        evt.setEventIdentifier("Some event identifier");
        evt.setEventType(EventType.SERVICE_EVENT);
        evt.setHistoryModificationTime(historyModDate);
        mgr.persist(evt);
        mgr.flush();
        createdEvents.add((Event) ReflectiveClone.clone(evt));
        return evt;
    }

    private void doTestCheckCreation() throws Exception {
        for (Event event : createdEvents) {
            DomainObject<?> savedEvent = mgr.getReference(Event.class,
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

    // //////////////////////////////////////////////////////////////////////
    // now test relation to the technical product

    // first create one event in relation to a technical product, check
    // references in both...
    @Test
    public void testCreateEventAndProduct() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAddEventAndTProd();
                return null;
            }
        });
    }

    private void doTestAddEventAndTProd() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        TechnicalProduct prod = TechnicalProducts.createTechnicalProduct(mgr,
                Organizations.createOrganization(mgr), "TP_ID", false,
                ServiceAccessType.LOGIN);

        Event evt = new Event();
        evt.setEventIdentifier("Some event identifier");
        evt.setEventType(EventType.SERVICE_EVENT);
        evt.setTechnicalProduct(prod);
        mgr.persist(evt);
    }

    @Test
    public void testHistoryModDateHandlingCreation() throws Exception {
        final long startTime = System.currentTimeMillis();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Event obj1 = doTestAdd(null);
                Event obj2 = doTestAdd(L10);
                List<DomainHistoryObject<?>> history = mgr.findHistory(obj1);
                Assert.assertEquals(1, history.size());
                Assert.assertTrue(history.get(0).getModdate().getTime() >= startTime);
                Assert.assertEquals(ModificationType.ADD, history.get(0)
                        .getModtype());

                history = mgr.findHistory(obj2);
                Assert.assertEquals(1, history.size());
                Assert.assertEquals(10, history.get(0).getModdate().getTime());
                Assert.assertEquals(ModificationType.ADD, history.get(0)
                        .getModtype());
                return null;
            }
        });
    }

    @Test
    public void testHistoryModDateHandlingUpdate() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd(null);
                doTestAdd(null);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final Event obj1 = mgr.find(Event.class,
                        Long.valueOf(createdEvents.get(0).getKey()));
                final Event obj2 = mgr.find(Event.class,
                        Long.valueOf(createdEvents.get(1).getKey()));
                // Must not lead to a new version as event is unmodifiable:
                obj1.setEventIdentifier("brandnewId");
                obj2.setEventIdentifier("brandnewId");
                obj2.setHistoryModificationTime(L10);
                mgr.flush();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final Event obj1 = mgr.find(Event.class,
                        Long.valueOf(createdEvents.get(0).getKey()));
                final Event obj2 = mgr.find(Event.class,
                        Long.valueOf(createdEvents.get(1).getKey()));

                // Objects must not have been modified:
                Assert.assertEquals("Some event identifier",
                        obj1.getEventIdentifier());
                Assert.assertEquals("Some event identifier",
                        obj2.getEventIdentifier());

                List<DomainHistoryObject<?>> history = mgr.findHistory(obj1);
                Assert.assertEquals(1, history.size());

                history = mgr.findHistory(obj2);
                Assert.assertEquals(1, history.size());
                return null;
            }
        });
    }

    @Test
    public void testHistoryModDateHandlingDelete() throws Exception {
        final List<Event> events = runTX(new Callable<List<Event>>() {
            public List<Event> call() throws Exception {
                List<Event> events = new ArrayList<Event>();
                events.add(doTestAdd(null));
                events.add(doTestAdd(null));
                return events;
            }
        });
        final long startTime = System.currentTimeMillis();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {

                final Event obj1 = mgr.find(Event.class,
                        Long.valueOf(events.get(0).getKey()));
                final Event obj2 = mgr.find(Event.class,
                        Long.valueOf(events.get(1).getKey()));
                obj2.setHistoryModificationTime(L10);
                mgr.remove(obj1);
                mgr.remove(obj2);
                mgr.flush();
                List<DomainHistoryObject<?>> history = mgr.findHistory(obj1);
                Assert.assertEquals(2, history.size());
                Assert.assertTrue(history.get(1).getModdate().getTime() >= startTime);
                Assert.assertEquals(ModificationType.DELETE, history.get(1)
                        .getModtype());

                history = mgr.findHistory(obj2);
                Assert.assertEquals(2, history.size());
                Assert.assertEquals(10, history.get(1).getModdate().getTime());
                Assert.assertEquals(ModificationType.DELETE, history.get(1)
                        .getModtype());
                return null;
            }
        });
    }

}
