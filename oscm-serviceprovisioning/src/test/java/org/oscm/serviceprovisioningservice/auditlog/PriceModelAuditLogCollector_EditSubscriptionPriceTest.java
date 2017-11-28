/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean.DEFAULT_PRICE_VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

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
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceType;

public class PriceModelAuditLogCollector_EditSubscriptionPriceTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static long PRICEMODEL_KEY_2 = 20L;
    private final static int PRICEMODEL_FREE_PERIOD_2 = 0;
    private final static BigDecimal ONE_TIME_FEE_2 = new BigDecimal(20);
    private final static BigDecimal RECURRING_CHARGE_2 = new BigDecimal(10);
    private final static PriceModelType PRICEMODEL_TYPE_2 = PriceModelType.PER_UNIT;
    private static final String CURRENCY_CODE_2 = "JPY";
    private static final PricingPeriod PRICING_PERIOD_2 = PricingPeriod.MONTH;
    private static final String CUSTOMER_NAME = "customer";
    private static final String CUSTOMER_ID = "customerId";
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";

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

    private static PlatformUser givenUser() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID);
        user.setOrganization(org);
        return user;
    }

    @Test
    public void editSubscriptionPriceOperation_Subscription() {

        // given
        PriceModel pmNew = givenPriceModel2(ServiceType.SUBSCRIPTION);

        // when
        logCollector.editSubscriptionPrice(dsMock, pmNew, DEFAULT_PRICE_VALUE,
                true);
        // then
        verifyLogEntries(ServiceType.SUBSCRIPTION);
    }

    @Test
    public void editSubscriptionPriceOperation_CustomerTemplate() {

        // given
        PriceModel pmNew = givenPriceModel2(ServiceType.CUSTOMER_TEMPLATE);

        // when
        logCollector.editSubscriptionPrice(dsMock, pmNew, DEFAULT_PRICE_VALUE,
                true);

        // then
        verifyLogEntries(ServiceType.CUSTOMER_TEMPLATE);
    }

    @Test
    public void editSubscriptionPriceOperation_Template() {

        // given
        PriceModel pmNew = givenPriceModel2(ServiceType.TEMPLATE);

        // when
        logCollector.editSubscriptionPrice(dsMock, pmNew, DEFAULT_PRICE_VALUE,
                true);

        // then
        verifyLogEntries(ServiceType.TEMPLATE);
    }

    private void verifyLogEntries(ServiceType type) {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        verifyProduct(logParams);
        if (ServiceType.isSubscription(type)) {
            verifySubscription(logParams);
            verifyCustomer(logParams);
        } else if (ServiceType.isCustomerTemplate(type)) {
            verifyNullSubscription(logParams);
            verifyCustomer(logParams);
        } else if (ServiceType.isTemplate(type)) {
            verifyNullSubscription(logParams);
            verifyNullCustomer(logParams);
        } else {
            Assert.fail("Service type not correct! " + type.name());
        }
        assertEquals(PRICING_PERIOD_2.name(),
                logParams.get(AuditLogParameter.TIMEUNIT));
        assertEquals(ONE_TIME_FEE_2.toString(),
                logParams.get(AuditLogParameter.ONE_TIME_FEE));
        assertEquals(RECURRING_CHARGE_2.toString(),
                logParams.get(AuditLogParameter.RECURRING_CHARGE));
    }

    private PriceModel generatePriceModel(long priceModelKey,
            String currencyCode, PricingPeriod pricingPeriod,
            PriceModelType priceModelType, int trialDays,
            BigDecimal oneTimeFee, BigDecimal recurringCharge) {
        PriceModel priceModel = new PriceModel();
        priceModel.setKey(priceModelKey);
        priceModel.setCurrency(new SupportedCurrency(currencyCode));
        priceModel.setPeriod(pricingPeriod);
        priceModel.setType(priceModelType);
        priceModel.setFreePeriod(trialDays);
        priceModel.setOneTimeFee(oneTimeFee);
        priceModel.setPricePerPeriod(recurringCharge);
        return priceModel;
    }

    private PriceModel givenPriceModel2(ServiceType type) {
        PriceModel pm = this.generatePriceModel(PRICEMODEL_KEY_2,
                CURRENCY_CODE_2, PRICING_PERIOD_2, PRICEMODEL_TYPE_2,
                PRICEMODEL_FREE_PERIOD_2, ONE_TIME_FEE_2, RECURRING_CHARGE_2);
        Subscription sub = givenSubscriptionWithPriceModelNull();
        Product prod = createProduct(type);
        if (ServiceType.isSubscription(type)) {
            sub.setProduct(prod);
            prod.setOwningSubscription(sub);
        }
        prod.setPriceModel(pm);
        pm.setProduct(prod);
        return pm;
    }

    private Subscription givenSubscriptionWithPriceModelNull() {
        Subscription sub = new Subscription();
        Organization customerOrg = new Organization();
        customerOrg.setOrganizationId(CUSTOMER_ID);
        customerOrg.setName(CUSTOMER_NAME);
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setOrganization(customerOrg);
        return sub;
    }

    private Product createProduct(ServiceType type) {
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setType(type);
        if (ServiceType.TEMPLATE != type) {
            prod.setTemplate(prod);
            Organization org = new Organization();
            org.setOrganizationId(CUSTOMER_ID);
            org.setName(CUSTOMER_NAME);
            prod.setTargetCustomer(org);
        }
        return prod;
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

    @Test
    public void editSubscriptionPrice_transientPriceModel() {
        // given
        PriceModel priceModel = givenPriceModel2(ServiceType.TEMPLATE);
        // predefined value, but not default value!
        priceModel.setPricePerPeriod(BigDecimal.valueOf(10));

        // when
        logCollector.editSubscriptionPrice(dsMock, priceModel,
                BigDecimal.valueOf(10), true);

        // then
        verifyLogEntries(ServiceType.TEMPLATE);
    }

    @Test
    public void editSubscriptionPrice_samePrice() {
        // given
        PriceModel priceModel = new PriceModel();
        priceModel.setPricePerPeriod(BigDecimal.valueOf(34));

        // when
        logCollector.editSubscriptionPrice(dsMock, priceModel,
                BigDecimal.valueOf(34), false);

        // then
        assertTrue(auditLogDataIsEmpty());
    }

    private boolean auditLogDataIsEmpty() {
        return AuditLogData.get() == null || AuditLogData.get().isEmpty();
    }

    @Test
    public void editSubscriptionPrice_priceChanged() {
        // given
        PriceModel priceModel = givenPriceModel2(ServiceType.TEMPLATE);
        priceModel.setPricePerPeriod(BigDecimal.valueOf(10));

        // when
        logCollector.editSubscriptionPrice(dsMock, priceModel,
                BigDecimal.valueOf(10.1), false);

        // then
        verifyLogEntries(ServiceType.TEMPLATE);
    }
}
