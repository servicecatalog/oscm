/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.auditlog.util.AuditLogData;import org.oscm.auditlog.util.AuditLogParameter;import org.oscm.auditlog.util.BESAuditLogEntry;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogOperation.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceType;

/**
 * @author Stavreva
 * 
 */
public class PriceModelAuditLogCollector_EditUserPriceTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";
    private final static BigDecimal USER_PRICE = BigDecimal.TEN;
    private final static PricingPeriod TIMEUNIT = PricingPeriod.MONTH;
    private final static Long LIMIT = Long.valueOf(20);
    private final static String RANGE_ANY = "1-ANY";
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
        dsMock = mockDataService();

        localizerMock = mock(LocalizerServiceLocal.class);
        when(
                localizerMock.getLocalizedTextFromDatabase(Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.any(LocalizedObjectTypes.class))).thenReturn(
                                LOCALIZED_RESOURCE);
        logCollector.localizer = localizerMock;
    }

    @Before
    public void before() {
        AuditLogData.clear();
    }

    private static DataService mockDataService() {
        DataService ds = mock(DataService.class);
        PlatformUser user = new PlatformUser();
        user.setUserId("userId");
        Organization organization = new Organization();
        organization.setOrganizationId("orgId");
        organization.setName("organizationName");
        user.setOrganization(organization);
        doReturn(user).when(ds).getCurrentUser();
        return ds;
    }

    @Test
    public void editUserPrice() {

        // given
        PriceModel pm = givenPriceModel(PriceModelType.SUBSCRIPTION);

        // when
        logCollector.editUserPrice(dsMock, pm, DEFAULT_PRICE_VALUE, true);

        // then
        verifyUserPrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editSubscriptionSteppedUserPrice_WithLimit() {

        // given
        SteppedPrice sp = givenSteppedUserPrice(PriceModelType.SUBSCRIPTION,
                true);

        // when
        logCollector.editUserSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedUserPrice(PriceModelType.SUBSCRIPTION, true, ACTION_UPDATE);
    }

    @Test
    public void editSubscriptionSteppedUserPrice_NoLimit() {

        // given
        SteppedPrice sp = givenSteppedUserPrice(PriceModelType.SUBSCRIPTION,
                false);

        // when
        logCollector.editUserSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedUserPrice(PriceModelType.SUBSCRIPTION, false,
                ACTION_UPDATE);
    }

    @Test
    public void editCustomerUserPrice() {

        // given
        PriceModel pm = givenPriceModel(PriceModelType.CUSTOMER_SERVICE);

        // when
        logCollector.editUserPrice(dsMock, pm, DEFAULT_PRICE_VALUE, true);

        // then
        verifyUserPrice(PriceModelType.CUSTOMER_SERVICE);
    }

    @Test
    public void editCustomerSteppedUserPrice_WithLimit() {

        // given
        SteppedPrice sp = givenSteppedUserPrice(
                PriceModelType.CUSTOMER_SERVICE, true);

        // when
        logCollector.editUserSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedUserPrice(PriceModelType.CUSTOMER_SERVICE, true,
                ACTION_UPDATE);
    }

    @Test
    public void editCustomerSteppedUserPrice_NoLimit() {

        // given
        SteppedPrice sp = givenSteppedUserPrice(
                PriceModelType.CUSTOMER_SERVICE, false);

        // when
        logCollector.editUserSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedUserPrice(PriceModelType.CUSTOMER_SERVICE, false,
                ACTION_UPDATE);
    }

    @Test
    public void editProductUserPrice() {

        // given
        PriceModel pm = givenPriceModel(PriceModelType.SERVICE);

        // when
        logCollector.editUserPrice(dsMock, pm, DEFAULT_PRICE_VALUE, true);

        // then
        verifyUserPrice(PriceModelType.SERVICE);
    }

    @Test
    public void editProductSteppedUserPrice_WithLimit() {

        // given
        SteppedPrice sp = givenSteppedUserPrice(PriceModelType.SERVICE, true);

        // when
        logCollector.editUserSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedUserPrice(PriceModelType.SERVICE, true, ACTION_UPDATE);
    }

    @Test
    public void editProductSteppedUserPrice_NoLimit() {

        // given
        SteppedPrice sp = givenSteppedUserPrice(PriceModelType.SERVICE, false);

        // when
        logCollector.editUserSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifySteppedUserPrice(PriceModelType.SERVICE, false, ACTION_UPDATE);
    }

    private void verifyUserPrice(PriceModelType type) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifyUserPrice(logParams);
        verifyAction(logParams, null);
    }

    private void verifySteppedUserPrice(PriceModelType type, boolean withLimit,
            String action) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifySteppedUserPrice(logParams, withLimit);
        verifyAction(logParams, action);
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

    private void verifyUserPrice(Map<AuditLogParameter, String> logParams) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(TIMEUNIT.name(), logParams.get(AuditLogParameter.TIMEUNIT));
        assertEquals(USER_PRICE.toString(),
                logParams.get(AuditLogParameter.RECURRING_CHARGE));
        assertEquals(RANGE_ANY, logParams.get(AuditLogParameter.RANGE));
    }

    private void verifySteppedUserPrice(
            Map<AuditLogParameter, String> logParams, boolean withLimit) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(TIMEUNIT.name(), logParams.get(AuditLogParameter.TIMEUNIT));
        assertEquals(USER_PRICE.toString(),
                logParams.get(AuditLogParameter.RECURRING_CHARGE));
        if (withLimit) {
            assertEquals(LIMIT.toString(),
                    logParams.get(AuditLogParameter.RANGE));
        } else {
            assertEquals("ANY ABOVE", logParams.get(AuditLogParameter.RANGE));
        }
    }

    private SteppedPrice givenSteppedUserPrice(PriceModelType type,
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
        pm.setPeriod(TIMEUNIT);
        SteppedPrice sp = new SteppedPrice();
        sp.setPriceModel(pm);
        if (withLimit) {
            sp.setLimit(LIMIT);
        }
        sp.setPrice(USER_PRICE);
        return sp;
    }

    private PriceModel givenPriceModel(PriceModelType type) {
        PriceModel pm = new PriceModel();
        if (type == PriceModelType.SERVICE) {
            pm.setProduct(createTemplateProduct());
        } else if (type == PriceModelType.CUSTOMER_SERVICE) {
            pm.setProduct(createCustomerProduct());
        } else if (type == PriceModelType.SUBSCRIPTION) {
            pm.setProduct(createSubscriptionProduct());
        }
        pm.setCurrency(new SupportedCurrency(CURRENCY));
        pm.setPricePerUserAssignment(USER_PRICE);
        pm.setPeriod(PricingPeriod.MONTH);
        return pm;
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
    public void editUserPrice_transientPriceModel() {
        // given
        PriceModel priceModel = givenPriceModel(PriceModelType.SUBSCRIPTION);
        // predefined value, but not default value!
        priceModel.setPricePerUserAssignment(BigDecimal.valueOf(10));

        // when
        logCollector.editUserPrice(dsMock, priceModel, BigDecimal.valueOf(10),
                true);

        // then
        verifyUserPrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editUserPrice_samePrice() {
        // given
        PriceModel priceModel = new PriceModel();
        priceModel.setPricePerUserAssignment(BigDecimal.valueOf(34));

        // when
        logCollector.editUserPrice(dsMock, priceModel, BigDecimal.valueOf(34),
                false);

        // then
        assertTrue(auditLogDataIsEmpty());
    }

    private boolean auditLogDataIsEmpty() {
        return AuditLogData.get() == null || AuditLogData.get().isEmpty();
    }

    @Test
    public void editSubscriptionPrice_priceChanged() {
        // given
        PriceModel priceModel = givenPriceModel(PriceModelType.SERVICE);
        priceModel.setPricePerPeriod(BigDecimal.valueOf(10));

        // when
        logCollector.editUserPrice(dsMock, priceModel,
                BigDecimal.valueOf(10.1), false);

        // then
        verifyUserPrice(PriceModelType.SERVICE);
    }

    @Test
    public void insertUserSteppedPrice() {
        // given
        SteppedPrice steppedPrice = givenSteppedUserPrice(
                PriceModelType.CUSTOMER_SERVICE, false);

        // when
        logCollector.insertUserSteppedPrice(dsMock, steppedPrice);

        // then
        verifySteppedUserPrice(PriceModelType.CUSTOMER_SERVICE, false,
                ACTION_INSERT);
    }

    @Test
    public void removeUserSteppedPrice() {
        // given
        SteppedPrice steppedPrice = givenSteppedUserPrice(
                PriceModelType.CUSTOMER_SERVICE, false);

        // when
        logCollector.removeUserSteppedPrice(dsMock, steppedPrice);

        // then
        verifySteppedUserPrice(PriceModelType.CUSTOMER_SERVICE, false,
                ACTION_DELETE);
    }
}
