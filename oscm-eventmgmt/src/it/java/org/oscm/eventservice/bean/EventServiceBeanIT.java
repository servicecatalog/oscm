/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 08.06.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.eventservice.bean;

import static org.oscm.test.Numbers.TIMESTAMP;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.GatheredEvent;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.intf.EventService;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.DuplicateEventException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOGatheredEvent;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.enumtypes.PlatformEventIdentifier;

/**
 * @author Mike J&auml;ger
 * 
 */
public class EventServiceBeanIT extends EJBTestBase {

    private static final String USER_KEY_EXISTING = "1";

    private DataService mgr;
    private EventService evMgmt;

    private String productId;
    private String technicalProductId;
    private String instanceId;
    private VOGatheredEvent event;
    private String customerId;

    private String ACTOR = "anyUser";
    private long SUBSCRIPTION_KEY;
    private long MULTIPLIER = 2L;
    private String UNIQUEID = "id1";

    private Organization supplier;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login(USER_KEY_EXISTING);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new EventServiceBean());
        mgr = container.get(DataService.class);
        evMgmt = container.get(EventService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                createTestData();
                return null;
            }
        });
    }

    @Test
    public void testRecordEventForSubscription() throws Exception {

        final VOGatheredEvent evt = new VOGatheredEvent();
        evt.setActor(ACTOR);
        evt.setOccurrenceTime(TIMESTAMP);
        evt.setEventId(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        evt.setMultiplier(MULTIPLIER);
        evt.setUniqueId(UNIQUEID);
        evMgmt.recordEventForSubscription(SUBSCRIPTION_KEY, evt);
        GatheredEvent gatheredEvent = readEvent(ACTOR, TIMESTAMP,
                SUBSCRIPTION_KEY, EventType.SERVICE_EVENT);
        testSavedEvent(TIMESTAMP, MULTIPLIER, UNIQUEID, gatheredEvent,
                EventType.SERVICE_EVENT);
    }

    @Test(expected = ValidationException.class)
    public void testRecordEventForSubscription_ToLongActor() throws Exception {
        final VOGatheredEvent evt = new VOGatheredEvent();
        evt.setActor(TOO_LONG_DESCRIPTION);
        evt.setOccurrenceTime(TIMESTAMP);
        evt.setEventId(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        evt.setMultiplier(MULTIPLIER);
        evt.setUniqueId(UNIQUEID);
        evMgmt.recordEventForSubscription(SUBSCRIPTION_KEY, evt);
    }

    @Test(expected = ValidationException.class)
    public void testRecordEventForSubscription_ToLongUniqueId()
            throws Exception {
        final VOGatheredEvent evt = new VOGatheredEvent();
        evt.setActor(ACTOR);
        evt.setOccurrenceTime(TIMESTAMP);
        evt.setEventId(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        evt.setMultiplier(MULTIPLIER);
        evt.setUniqueId(TOO_LONG_DESCRIPTION);
        evMgmt.recordEventForSubscription(SUBSCRIPTION_KEY, evt);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testRecordEventForSubscriptionWrongEventId() throws Exception {

        final VOGatheredEvent evt = new VOGatheredEvent();
        evt.setActor(ACTOR);
        evt.setOccurrenceTime(TIMESTAMP);
        evt.setEventId(PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE);
        evt.setMultiplier(MULTIPLIER);
        evt.setUniqueId(UNIQUEID);
        evMgmt.recordEventForSubscription(SUBSCRIPTION_KEY, evt);
        GatheredEvent gatheredEvent = readEvent(ACTOR, TIMESTAMP,
                SUBSCRIPTION_KEY, EventType.SERVICE_EVENT);
        testSavedEvent(TIMESTAMP, MULTIPLIER, UNIQUEID, gatheredEvent,
                EventType.SERVICE_EVENT);
    }

    @Test(expected = DuplicateEventException.class)
    public void testRecordEventForSubscriptionDuplicateUniqueId()
            throws Exception {

        final VOGatheredEvent evt = new VOGatheredEvent();
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                evt.setActor(ACTOR);
                evt.setOccurrenceTime(TIMESTAMP);
                evt.setEventId(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
                evt.setMultiplier(MULTIPLIER);
                evt.setUniqueId(UNIQUEID);
                evMgmt.recordEventForSubscription(SUBSCRIPTION_KEY, evt);

                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                evt.setOccurrenceTime(TIMESTAMP + 100);
                evMgmt.recordEventForSubscription(SUBSCRIPTION_KEY, evt);
                return null;
            }
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testRecordEventForSubscriptionWrongOrganization()
            throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // login as another technology provider
                Organization provider = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        provider, true, "admin");
                container.login(user.getKey());
                return null;
            }
        });

        final VOGatheredEvent evt = new VOGatheredEvent();
        evt.setActor(ACTOR);
        evt.setOccurrenceTime(TIMESTAMP);
        evt.setEventId(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        evt.setMultiplier(MULTIPLIER);
        try {
            evMgmt.recordEventForSubscription(SUBSCRIPTION_KEY, evt);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testRecordEventForInstanceObjectNotFound() throws Exception {

        final String technicalProductIdLocal = "idTechProd";
        final String instanceIdLocal = "idInst";
        final VOGatheredEvent eventLocal = new VOGatheredEvent();

        evMgmt.recordEventForInstance(technicalProductIdLocal, instanceIdLocal,
                eventLocal);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testRecordEventForInstanceNoResult() throws Exception {

        instanceId = "123";
        evMgmt.recordEventForInstance(technicalProductId, instanceId,
                new VOGatheredEvent());
    }

    /*
     * Record events test.
     */
    @Test
    public void testRecordEventForInstance() throws Exception {

        event = new VOGatheredEvent();
        event.setActor(ACTOR);
        event.setOccurrenceTime(TIMESTAMP);
        event.setEventId(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        event.setMultiplier(MULTIPLIER);
        event.setUniqueId(UNIQUEID);

        evMgmt.recordEventForInstance(technicalProductId, instanceId, event);
        GatheredEvent savedEvent = readEvent(ACTOR, TIMESTAMP, SUBSCRIPTION_KEY,
                EventType.SERVICE_EVENT);
        testSavedEvent(TIMESTAMP, MULTIPLIER, UNIQUEID, savedEvent,
                EventType.SERVICE_EVENT);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Subscription subscription = Subscriptions.createSubscription(
                        mgr, customerId, productId, "SUBSCRIPTION_2", supplier);
                subscription.setProductInstanceId(instanceId);
                return null;
            }
        });
        try {
            evMgmt.recordEventForInstance(technicalProductId, instanceId,
                    event);
            Assert.fail("recordEvent() must faile!");
        } catch (EJBException e) {
            Assert.assertEquals(NonUniqueResultException.class,
                    e.getCause().getCause().getClass());
        }
    }

    @Test(expected = ValidationException.class)
    public void testRecordEventForInstance_ToLongActor() throws Exception {

        event = new VOGatheredEvent();
        event.setActor(TOO_LONG_DESCRIPTION);
        event.setOccurrenceTime(TIMESTAMP);
        event.setEventId(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        event.setMultiplier(MULTIPLIER);
        event.setUniqueId(UNIQUEID);
        evMgmt.recordEventForInstance(technicalProductId, instanceId, event);
    }

    @Test(expected = ValidationException.class)
    public void testRecordEventForInstance_ToLongUniqueId() throws Exception {

        event = new VOGatheredEvent();
        event.setActor(ACTOR);
        event.setOccurrenceTime(TIMESTAMP);
        event.setEventId(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        event.setMultiplier(MULTIPLIER);
        event.setUniqueId(TOO_LONG_DESCRIPTION);
        evMgmt.recordEventForInstance(technicalProductId, instanceId, event);
    }

    /**
     * Helper method for event reading.
     * 
     * @param actor
     *            User saving event.
     * @param occurrenceTime
     *            Occurance time.
     * @param subKey
     *            Key of subscription.
     * @param eventType
     *            The type of the event to read
     * @return Event.
     * @throws Exception
     */
    private GatheredEvent readEvent(final String actor,
            final long occurrenceTime, final long subKey,
            final EventType eventType) throws Exception {
        GatheredEvent eventLocal = runTX(new Callable<GatheredEvent>() {
            @Override
            public GatheredEvent call() throws Exception {
                long key = -1;
                Query query = mgr.createQuery(
                        "select c from GatheredEvent c where c.dataContainer.actor=:actor and "
                                + "c.dataContainer.occurrenceTime=:occurrencetime and "
                                + "c.dataContainer.subscriptionTKey=:subscriptionTKey and "
                                + "c.dataContainer.type=:type");
                query.setParameter("actor", actor);
                query.setParameter("occurrencetime",
                        Long.valueOf(occurrenceTime));
                query.setParameter("subscriptionTKey", Long.valueOf(subKey));
                query.setParameter("type", eventType);
                Iterator<GatheredEvent> gatheredEventIterator = ParameterizedTypes
                        .iterator(query.getResultList(), GatheredEvent.class);
                if (gatheredEventIterator.hasNext()) {
                    key = gatheredEventIterator.next().getKey();
                }
                return mgr.find(GatheredEvent.class, key);
            }
        });
        return eventLocal;
    }

    /**
     * Helper method for testing saved event.
     * 
     * @param occurrenceTime
     *            Occurance time.
     * @param multiplier
     *            Event multiplier.
     * @param pEvent
     *            Event for testing.
     * @param eventType
     *            expected event type
     */
    private void testSavedEvent(final long occurrenceTime,
            final long multiplier, final String uniqueId, GatheredEvent pEvent,
            EventType eventType) {
        Assert.assertNotNull(
                "Object was stored, so its reference must be found", pEvent);
        Assert.assertEquals("Stored information is wrong!", "anyUser",
                pEvent.getActor());
        Assert.assertEquals("Stored information is wrong!",
                "USER_LOGIN_TO_SERVICE", pEvent.getEventId());
        Assert.assertEquals("Stored information is wrong!", occurrenceTime,
                pEvent.getOccurrenceTime());
        Assert.assertEquals("Stored information is wrong!", SUBSCRIPTION_KEY,
                pEvent.getSubscriptionTKey());
        Assert.assertEquals("Stored information is wrong!", eventType,
                pEvent.getType());
        Assert.assertEquals("Stored information is wrong!",
                PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                pEvent.getEventId());
        Assert.assertEquals("Stored information is wrong!", multiplier,
                pEvent.getMultiplier());
        Assert.assertEquals("UniqueId is wrong.", uniqueId,
                pEvent.getUniqueId());
    }

    /**
     * Helper method for initialize data.
     * 
     * @throws Exception
     *             On initialization error.
     */
    private void createTestData() throws Exception {
        supplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);

        PlatformUser user = new PlatformUser();
        user.setCreationDate(new Date(1L));
        user.setFailedLoginCounter(100);
        user.setLocale("EN");

        user.setStatus(UserAccountStatus.ACTIVE);
        user.setUserId(USER_KEY_EXISTING);
        user.setOrganization(supplier);

        mgr.persist(user);

        long key = user.getKey();
        user.setUserId(String.valueOf(key));

        container.login(String.valueOf(key), ROLE_TECHNOLOGY_MANAGER);

        TechnicalProduct technicalProduct = TechnicalProducts
                .createTechnicalProduct(mgr, supplier, "testTechProd", false,
                        ServiceAccessType.LOGIN);
        technicalProductId = technicalProduct.getTechnicalProductId();
        Product product = Products.createProduct(supplier, technicalProduct,
                true, "PRODUCT_1", null, mgr);
        productId = product.getProductId();

        Organization customer = Organizations.createCustomer(mgr, supplier);
        customerId = customer.getOrganizationId();
        Subscription subscription = Subscriptions.createSubscription(mgr,
                customerId, productId, "SUBSCRIPTION_1", supplier);
        SUBSCRIPTION_KEY = subscription.getKey();
        instanceId = subscription.getProductInstanceId();

        List<TechnicalProduct> list = new ArrayList<>();
        list.add(technicalProduct);
        supplier.setTechnicalProducts(list);
        Event event = new Event();
        event.setEventIdentifier(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        event.setEventType(EventType.SERVICE_EVENT);
        event.setTechnicalProduct(technicalProduct);
        mgr.persist(event);

        List<Event> eventList = new ArrayList<>();
        eventList.add(event);
        technicalProduct.setEvents(eventList);
    }

}
