/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 24.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean.DEFAULT_PRICE_VALUE;
import static org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean.DEFAULT_STEPPED_PRICE_LIMIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.auditlog.AuditLogData;
import org.oscm.auditlog.AuditLogParameter;
import org.oscm.auditlog.BESAuditLogEntry;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogOperation.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceType;

/**
 * @author Stavreva
 * 
 */
public class PriceModelAuditLogCollector_EditEventPricesTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";
    private final static String EVENT_ID = "event_id";
    private final static BigDecimal EVENT_PRICE = BigDecimal.TEN;
    private final static Long EVENT_LIMIT = Long.valueOf(20);
    private final static String EVENT_RANGE_ANY = "1-ANY";
    private final static String CURRENCY = "EUR";
    private final static long CUSTOMER_KEY = 10;
    private final static String CUSTOMER_ID = "customer_id";
    private final static String CUSTOMER_NAME = "customer_name";
    private static String ACTION_UPDATE = "UPDATE";
    private static String ACTION_DELETE = "DELETE";
    private static String ACTION_INSERT = "INSERT";

    private static DataService dsMock;
    private static PriceModelAuditLogCollector logCollector = new PriceModelAuditLogCollector();
    private static LocalizerServiceLocal localizerMock;
    private final static String LOCALIZED_RESOURCE = "TEST";

    @BeforeClass
    public static void setup() {
        dsMock = mock(DataService.class);
        when(dsMock.getCurrentUser()).thenReturn(givenUser());

        localizerMock = mock(LocalizerServiceLocal.class);
        when(
                localizerMock.getLocalizedTextFromDatabase(Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.any(LocalizedObjectTypes.class))).thenReturn(
                                LOCALIZED_RESOURCE);
        logCollector.localizer = localizerMock;
    }

    @Before
    public void cleadAuditLogData() {
        AuditLogData.clear();
    }

    @Test
    public void editSubscriptionEventPrice() {

        // given
        PricedEvent pricedEvent = givenPricedEvent(PriceModelType.SUBSCRIPTION);
        pricedEvent.setEventPrice(BigDecimal.TEN);

        // when
        logCollector.editEventPrice(dsMock, pricedEvent,
                BigDecimal.valueOf(10.1));

        // then
        verifyPricedEvent(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editSubscriptionSteppedEventPrice_WithLimit() {

        // given
        SteppedPrice sp = givenSteppedPricedEvent(PriceModelType.SUBSCRIPTION,
                true);

        // when
        logCollector.editEventSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedPricedEvent(PriceModelType.SUBSCRIPTION, true,
                ACTION_UPDATE);
    }

    @Test
    public void editSubscriptionSteppedEventPrice_NoLimit() {

        // given
        SteppedPrice sp = givenSteppedPricedEvent(PriceModelType.SUBSCRIPTION,
                false);

        // when
        logCollector.editEventSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedPricedEvent(PriceModelType.SUBSCRIPTION, false,
                ACTION_UPDATE);
    }

    @Test
    public void editCustomerEventPrice() {

        // given
        PricedEvent pricedEvent = givenPricedEvent(PriceModelType.CUSTOMER_SERVICE);
        pricedEvent.setEventPrice(BigDecimal.TEN);

        // when
        logCollector.editEventPrice(dsMock, pricedEvent,
                BigDecimal.valueOf(10.1));

        // then
        verifyPricedEvent(PriceModelType.CUSTOMER_SERVICE);
    }

    @Test
    public void editCustomerSteppedEventPrice_WithLimit() {

        // given
        SteppedPrice sp = givenSteppedPricedEvent(
                PriceModelType.CUSTOMER_SERVICE, true);

        // when
        logCollector.editEventSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedPricedEvent(PriceModelType.CUSTOMER_SERVICE, true,
                ACTION_UPDATE);
    }

    @Test
    public void editCustomerSteppedEventPrice_NoLimit() {

        // given
        SteppedPrice sp = givenSteppedPricedEvent(
                PriceModelType.CUSTOMER_SERVICE, false);

        // when
        logCollector.editEventSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedPricedEvent(PriceModelType.CUSTOMER_SERVICE, false,
                ACTION_UPDATE);
    }

    @Test
    public void editProductEventPrice() {

        // given
        PricedEvent pricedEvent = givenPricedEvent(PriceModelType.SERVICE);
        pricedEvent.setEventPrice(BigDecimal.TEN);

        // when
        logCollector.editEventPrice(dsMock, pricedEvent,
                BigDecimal.valueOf(10.1));

        // then
        verifyPricedEvent(PriceModelType.SERVICE);
    }

    @Test
    public void editProductSteppedEventPrice_WithLimit() {

        // given
        SteppedPrice sp = givenSteppedPricedEvent(PriceModelType.SERVICE, true);

        // when
        logCollector.editEventSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedPricedEvent(PriceModelType.SERVICE, true, ACTION_UPDATE);
    }

    @Test
    public void editProductSteppedEventPrice_NoLimit() {

        // given
        SteppedPrice sp = givenSteppedPricedEvent(PriceModelType.SERVICE, false);

        // when
        logCollector.editEventSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedPricedEvent(PriceModelType.SERVICE, false, ACTION_UPDATE);
    }

    private void verifyPricedEvent(PriceModelType type) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifyPricedEvent(logParams);
        verifyAction(logParams, null);
    }

    private void verifySteppedPricedEvent(PriceModelType type,
            boolean withLimit, String action) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifyAction(logParams, action);
        verifySteppedPricedEvent(logParams, withLimit);
    }

    private Map<AuditLogParameter, String> verifyLogEntry(PriceModelType type) {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        verifyProduct(logParams);
        if (type != PriceModelType.SERVICE) {
            verifyCustomer(logParams);
        } else {
            verifyNullCustomer(logParams);
        }
        if (type == PriceModelType.SUBSCRIPTION) {
            verifySubscription(logParams);
        } else {
            verifyNullSubscription(logParams);
        }
        return logParams;
    }

    private void verifyCustomer(Map<AuditLogParameter, String> logParams) {
        assertEquals(CUSTOMER_ID, logParams.get(AuditLogParameter.CUSTOMER_ID));
        assertEquals(CUSTOMER_NAME,
                logParams.get(AuditLogParameter.CUSTOMER_NAME));
    }

    private void verifyProduct(Map<AuditLogParameter, String> logParams) {
        assertEquals(PRODUCT_ID, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
    }

    private void verifySubscription(Map<AuditLogParameter, String> logParams) {
        assertEquals(SUBSCRIPTION_ID,
                logParams.get(AuditLogParameter.SUBSCRIPTION_NAME));
    }

    private void verifyNullSubscription(Map<AuditLogParameter, String> logParams) {
        assertNull(logParams.get(AuditLogParameter.SUBSCRIPTION_NAME));
    }

    private void verifyNullCustomer(Map<AuditLogParameter, String> logParams) {
        assertNull(logParams.get(AuditLogParameter.CUSTOMER_ID));
        assertNull(logParams.get(AuditLogParameter.CUSTOMER_NAME));
    }

    private void verifyAction(Map<AuditLogParameter, String> logParams,
            String action) {
        if (action == null) {
            assertNull(logParams.get(AuditLogParameter.ACTION_NAME));
        } else {
            assertEquals(action, logParams.get(AuditLogParameter.ACTION_NAME));
        }
    }

    private void verifyPricedEvent(Map<AuditLogParameter, String> logParams) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(EVENT_ID, logParams.get(AuditLogParameter.EVENT_NAME));
        assertEquals(EVENT_PRICE.toString(),
                logParams.get(AuditLogParameter.PRICE));
        assertEquals(EVENT_RANGE_ANY, logParams.get(AuditLogParameter.RANGE));
    }

    private void verifySteppedPricedEvent(
            Map<AuditLogParameter, String> logParams, boolean withLimit) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(EVENT_ID, logParams.get(AuditLogParameter.EVENT_NAME));
        assertEquals(EVENT_PRICE.toString(),
                logParams.get(AuditLogParameter.PRICE));
        if (withLimit) {
            assertEquals(EVENT_LIMIT.toString(),
                    logParams.get(AuditLogParameter.RANGE));
        } else {
            assertEquals("ANY ABOVE", logParams.get(AuditLogParameter.RANGE));
        }
    }

    private static PlatformUser givenUser() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID);
        user.setOrganization(org);
        return user;
    }

    private SteppedPrice givenSteppedPricedEvent(PriceModelType type,
            boolean withLimit) {
        PriceModel pm = new PriceModel();
        if (type == PriceModelType.SERVICE) {
            pm.setProduct(createTemplateProduct());
        } else if (type == PriceModelType.CUSTOMER_SERVICE) {
            pm.setProduct(createCustomerProduct());
        } else if (type == PriceModelType.SUBSCRIPTION) {
            pm.setProduct(createSubscriptionProduct());
        }
        pm.setCurrency(new SupportedCurrency(CURRENCY));
        Event event = new Event();
        event.setEventIdentifier(EVENT_ID);
        PricedEvent pe = new PricedEvent();
        pe.setPriceModel(pm);
        pe.setEvent(event);
        pe.setEventPrice(BigDecimal.ZERO);
        SteppedPrice sp = new SteppedPrice();
        sp.setPricedEvent(pe);
        if (withLimit) {
            sp.setLimit(EVENT_LIMIT);
        }
        sp.setPrice(EVENT_PRICE);
        return sp;
    }

    private PricedEvent givenPricedEvent(PriceModelType type) {
        PriceModel pm = new PriceModel();
        if (type == PriceModelType.SERVICE) {
            pm.setProduct(createTemplateProduct());
        } else if (type == PriceModelType.CUSTOMER_SERVICE) {
            pm.setProduct(createCustomerProduct());
        } else if (type == PriceModelType.SUBSCRIPTION) {
            pm.setProduct(createSubscriptionProduct());
        }
        pm.setCurrency(new SupportedCurrency(CURRENCY));
        Event event = new Event();
        event.setEventIdentifier(EVENT_ID);
        PricedEvent pe = new PricedEvent();
        pe.setPriceModel(pm);
        pe.setEvent(event);
        pe.setEventPrice(EVENT_PRICE);
        return pe;
    }

    private Product createSubscriptionProduct() {
        Organization customer = createCustomer();
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setOrganization(customer);
        sub.setOrganizationKey(CUSTOMER_KEY);
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setType(ServiceType.SUBSCRIPTION);
        prod.setTemplate(prod);
        prod.setOwningSubscription(sub);
        sub.setProduct(prod);
        return prod;
    }

    private Organization createCustomer() {
        Organization customer = new Organization();
        customer.setKey(CUSTOMER_KEY);
        customer.setOrganizationId(CUSTOMER_ID);
        customer.setName(CUSTOMER_NAME);
        return customer;
    }

    private Product createCustomerProduct() {
        Organization customer = createCustomer();
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setType(ServiceType.CUSTOMER_TEMPLATE);
        prod.setTargetCustomer(customer);
        prod.setTemplate(prod);
        return prod;
    }

    private Product createTemplateProduct() {
        Organization customer = createCustomer();
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setType(ServiceType.TEMPLATE);
        prod.setTargetCustomer(customer);
        prod.setTemplate(prod);
        return prod;
    }

    @Test
    public void insertEventSteppedPrice() {
        // given
        SteppedPrice steppedPrice = givenSteppedPricedEvent(
                PriceModelType.SUBSCRIPTION, true);

        // when
        logCollector.insertEventSteppedPrice(dsMock, steppedPrice);

        // then
        verifySteppedPricedEvent(PriceModelType.SUBSCRIPTION, true,
                ACTION_INSERT);
    }

    @Test
    public void removeEventSteppedPrice() {
        // given
        SteppedPrice steppedPrice = givenSteppedPricedEvent(
                PriceModelType.SUBSCRIPTION, true);

        // when
        logCollector.removeEventSteppedPrice(dsMock, steppedPrice);

        // then
        verifySteppedPricedEvent(PriceModelType.SUBSCRIPTION, true,
                ACTION_DELETE);
    }

    @Test
    public void removeEventPrice_eventCreatedInitially() {
        // given
        long voPriceModelKey = 0;
        PricedEvent pricedEvent = new PricedEvent();

        // when
        logCollector.removeEventPrice(dsMock, voPriceModelKey, pricedEvent);

        // then do not log
        assertTrue(auditLogDataIsEmpty());
    }

    private boolean auditLogDataIsEmpty() {
        return AuditLogData.get() == null || AuditLogData.get().isEmpty();
    }

    @Test
    public void removeEventPrice_eventAlreadyExisting() {
        // given
        long voPriceModelKey = 1;
        PricedEvent pricedEvent = givenPricedEvent(PriceModelType.SERVICE);

        // when
        logCollector.removeEventPrice(dsMock, voPriceModelKey, pricedEvent);

        // then
        Map<AuditLogParameter, String> logParams = verifyLogEntry(PriceModelType.SERVICE);
        assertEquals(BigDecimal.ZERO.toString(),
                logParams.get(AuditLogParameter.PRICE));
    }

    @Test
    public void editEventPrice_samePrice() {
        // given
        PricedEvent pricedEvent = new PricedEvent();
        pricedEvent.setEventPrice(BigDecimal.TEN);

        // when
        logCollector.editEventPrice(dsMock, pricedEvent, BigDecimal.TEN);

        // then
        assertTrue(auditLogDataIsEmpty());
    }
}
