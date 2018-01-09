/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 21.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

public class PriceModelAuditLogCollector_EditPriceModelTypeTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static long PRICEMODEL_KEY_2 = 20L;
    private final static String TRIAL_MODE_OFF = "OFF";
    private final static String TRIAL_MODE_ON = "ON";
    private final static PriceModelType PRICEMODEL_TYPE_1 = PriceModelType.PRO_RATA;
    private final static PriceModelType PRICEMODEL_TYPE_2 = PriceModelType.PER_UNIT;
    private final static int FREEPERIOD_1 = 100;
    private final static int FREEPERIOD_2 = 0;
    private final static PricingPeriod PRICINGPERIOD_1 = PricingPeriod.DAY;
    private static final PricingPeriod PRICINGPERIOD_2 = PricingPeriod.MONTH;
    private static final String CURRENCY_CODE_1 = "USD";
    private static final String CURRENCY_CODE_2 = "JPY";
    private static final String CUSTOMER_NAME = "customer";
    private static final String CUSTOMER_ID = "customerId";
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";
    private static long VO_PRICEMODEL_KEY_IS_ZERO = 0;

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
    public void editPriceModelOperation_CustomerTemplate() {

        // given
        PriceModel pmNew = givenPriceModel2(ServiceType.CUSTOMER_TEMPLATE);

        // when
        logCollector.editPriceModelTypeToChargeable(dsMock, pmNew,
                VO_PRICEMODEL_KEY_IS_ZERO, new SupportedCurrency(
                        CURRENCY_CODE_2), PRICEMODEL_TYPE_2, FREEPERIOD_1,
                PRICINGPERIOD_1);

        // then
        verifyLogEntries(ServiceType.CUSTOMER_TEMPLATE, false, TRIAL_MODE_OFF,
                FREEPERIOD_2, PRICINGPERIOD_2);
    }

    @Test
    public void editPriceModelOperation_Template() {

        // given
        PriceModel pmNew = givenPriceModel2(ServiceType.TEMPLATE);

        // when
        logCollector.editPriceModelTypeToChargeable(dsMock, pmNew,
                VO_PRICEMODEL_KEY_IS_ZERO, new SupportedCurrency(
                        CURRENCY_CODE_2), PRICEMODEL_TYPE_2, FREEPERIOD_1,
                PRICINGPERIOD_1);

        // then
        verifyLogEntries(ServiceType.TEMPLATE, false, TRIAL_MODE_OFF,
                FREEPERIOD_2, PRICINGPERIOD_2);
    }

    @Test
    public void editPriceModelFreeOperation_CustomerTemplate() {

        // given
        PriceModel pmNew = givenPriceModel2(ServiceType.CUSTOMER_TEMPLATE);

        // when
        logCollector.editPriceModelTypeToChargeable(dsMock, pmNew,
                VO_PRICEMODEL_KEY_IS_ZERO, new SupportedCurrency(
                        CURRENCY_CODE_2), PRICEMODEL_TYPE_2, FREEPERIOD_1,
                PRICINGPERIOD_1);

        // then
        verifyLogEntries(ServiceType.CUSTOMER_TEMPLATE, true, TRIAL_MODE_OFF,
                FREEPERIOD_2, PRICINGPERIOD_2);
    }

    @Test
    public void editPriceModelFreeOperation_Template() {

        // given
        PriceModel pmNew = givenPriceModel2(ServiceType.TEMPLATE);

        // when
        logCollector.editPriceModelTypeToChargeable(dsMock, pmNew,
                VO_PRICEMODEL_KEY_IS_ZERO, new SupportedCurrency(
                        CURRENCY_CODE_2), PRICEMODEL_TYPE_2, FREEPERIOD_1,
                PRICINGPERIOD_1);

        // then
        verifyLogEntries(ServiceType.TEMPLATE, true, TRIAL_MODE_OFF,
                FREEPERIOD_2, PRICINGPERIOD_2);
    }

    @Test
    public void deletePriceModel_CustomerTemplate() {

        // given
        PriceModel pm = givenPriceModel2(ServiceType.CUSTOMER_TEMPLATE);

        // when
        logCollector.deletePriceModel(dsMock, pm);

        // then
        verifyLogEntries(ServiceType.CUSTOMER_TEMPLATE, true, TRIAL_MODE_OFF,
                FREEPERIOD_2, PRICINGPERIOD_2);
    }

    private void verifyLogEntries(ServiceType type, boolean freeOfCharge,
            String trialMode, int freePeriod, PricingPeriod timeUnit) {
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
        } else if (ServiceType.isTemplateOrPartnerTemplate(type)) {
            verifyNullSubscription(logParams);
            verifyNullCustomer(logParams);
        }
        if (!freeOfCharge) {
            assertEquals(CURRENCY_CODE_2,
                    logParams.get(AuditLogParameter.CURRENCY_CODE));
            assertEquals(timeUnit.name(),
                    logParams.get(AuditLogParameter.TIMEUNIT));
            assertEquals(PRICEMODEL_TYPE_2.name(),
                    logParams.get(AuditLogParameter.CALCULATION_MODE));
            assertEquals(trialMode,
                    logParams.get(AuditLogParameter.TRIAL_PERIOD));
            assertEquals(String.valueOf(freePeriod),
                    logParams.get(AuditLogParameter.DAYS_OF_TRIAL));
        }
    }

    private PriceModel generatePriceModel(long priceModelKey,
            String currencyCode, PricingPeriod pricingPeriod,
            PriceModelType priceModelType, int trialDays) {
        PriceModel priceModel = new PriceModel();
        priceModel.setKey(priceModelKey);
        priceModel.setCurrency(new SupportedCurrency(currencyCode));
        priceModel.setPeriod(pricingPeriod);
        priceModel.setType(priceModelType);
        priceModel.setFreePeriod(trialDays);
        return priceModel;
    }

    private PriceModel givenPriceModel2(ServiceType type) {
        PriceModel pm = this.generatePriceModel(PRICEMODEL_KEY_2,
                CURRENCY_CODE_2, PRICINGPERIOD_2, PRICEMODEL_TYPE_2,
                FREEPERIOD_2);
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
    public void editPriceModelTypeToChargeable_transientPriceModel() {
        // given transient vo price model
        long voPriceModelKey = 0;

        // price model type and currency same
        PriceModel priceModel = givenPriceModel2(ServiceType.TEMPLATE);
        SupportedCurrency currency = new SupportedCurrency(CURRENCY_CODE_2);
        priceModel.setCurrency(currency);
        SupportedCurrency oldCurrency = new SupportedCurrency(CURRENCY_CODE_2);
        priceModel.setType(PRICEMODEL_TYPE_2);
        PriceModelType oldPriceModelType = PRICEMODEL_TYPE_2;

        // when
        logCollector.editPriceModelTypeToChargeable(dsMock, priceModel,
                voPriceModelKey, oldCurrency, oldPriceModelType, FREEPERIOD_1,
                PRICINGPERIOD_1);

        // then log, because VOPriceModel key is 0
        verifyLogEntries(ServiceType.TEMPLATE, false, TRIAL_MODE_OFF,
                FREEPERIOD_2, PRICINGPERIOD_2);
    }

    @Test
    public void editPriceModelTypeToChargeable_nothingChanged() {
        // given already persisted pricemodel, VOPriceModel.key is greater than
        // 0
        long voPriceModelKey = 543;

        // price model type same, same currency
        PriceModel priceModel = givenPriceModel2(ServiceType.TEMPLATE);
        priceModel.setCurrency(new SupportedCurrency(CURRENCY_CODE_2));
        SupportedCurrency oldCurrency = new SupportedCurrency(CURRENCY_CODE_2);
        priceModel.setType(PRICEMODEL_TYPE_2);
        PriceModelType oldPriceModelType = PRICEMODEL_TYPE_2;
        priceModel.setFreePeriod(FREEPERIOD_1);
        priceModel.setPeriod(PRICINGPERIOD_1);

        // when
        logCollector.editPriceModelTypeToChargeable(dsMock, priceModel,
                voPriceModelKey, oldCurrency, oldPriceModelType, FREEPERIOD_1,
                PRICINGPERIOD_1);

        // then
        assertTrue(auditLogDataIsEmpty());
    }

    private boolean auditLogDataIsEmpty() {
        return AuditLogData.get() == null || AuditLogData.get().isEmpty();
    }

    @Test
    public void editPriceModelTypeToChargeable_currencyDifferent() {
        // given already persisted pricemodel, vo is greater than 0
        long voPriceModelKey = 543;

        // price model type same, different currency
        PriceModel priceModel = givenPriceModel2(ServiceType.TEMPLATE);
        priceModel.setCurrency(new SupportedCurrency(CURRENCY_CODE_2));
        SupportedCurrency oldCurrency = new SupportedCurrency(CURRENCY_CODE_1);
        priceModel.setType(PRICEMODEL_TYPE_2);
        PriceModelType oldPriceModelType = PRICEMODEL_TYPE_2;

        // when
        logCollector.editPriceModelTypeToChargeable(dsMock, priceModel,
                voPriceModelKey, oldCurrency, oldPriceModelType, FREEPERIOD_1,
                PRICINGPERIOD_1);

        // then log, because currency is different
        verifyLogEntries(ServiceType.TEMPLATE, false, TRIAL_MODE_OFF,
                FREEPERIOD_2, PRICINGPERIOD_2);
    }

    @Test
    public void editPriceModelTypeToChargeable_freePeriodDifferent() {
        // given
        long voPriceModelKey = 543;
        PriceModel priceModel = givenPriceModel2(ServiceType.CUSTOMER_TEMPLATE);
        priceModel.setCurrency(new SupportedCurrency(CURRENCY_CODE_2));
        SupportedCurrency oldCurrency = new SupportedCurrency(CURRENCY_CODE_2);
        priceModel.setType(PRICEMODEL_TYPE_2);
        priceModel.setFreePeriod(FREEPERIOD_1);
        PriceModelType oldPriceModelType = PRICEMODEL_TYPE_2;

        // when
        logCollector.editPriceModelTypeToChargeable(dsMock, priceModel,
                voPriceModelKey, oldCurrency, oldPriceModelType, FREEPERIOD_2,
                PRICINGPERIOD_1);

        // then
        verifyLogEntries(ServiceType.CUSTOMER_TEMPLATE, false, TRIAL_MODE_ON,
                FREEPERIOD_1, PRICINGPERIOD_2);
    }

    @Test
    public void editPriceModelTypeToChargeable_pricingPeriodDifferent() {
        // given
        long voPriceModelKey = 543;
        PriceModel priceModel = givenPriceModel2(ServiceType.CUSTOMER_TEMPLATE);
        priceModel.setCurrency(new SupportedCurrency(CURRENCY_CODE_2));
        SupportedCurrency oldCurrency = new SupportedCurrency(CURRENCY_CODE_2);
        priceModel.setType(PRICEMODEL_TYPE_2);
        priceModel.setPeriod(PRICINGPERIOD_1);
        PriceModelType oldPriceModelType = PRICEMODEL_TYPE_2;

        // when
        logCollector.editPriceModelTypeToChargeable(dsMock, priceModel,
                voPriceModelKey, oldCurrency, oldPriceModelType, FREEPERIOD_1,
                PRICINGPERIOD_2);

        // then
        verifyLogEntries(ServiceType.CUSTOMER_TEMPLATE, false, TRIAL_MODE_OFF,
                FREEPERIOD_2, PRICINGPERIOD_1);
    }

    @Test
    public void editPriceModelTypeToChargeable_currencyNotSetInPriceModel() {
        // given already persisted pricemodel, vo is greater than 0
        long voPriceModelKey = 543;

        // price model type same, different currency
        PriceModel priceModel = givenPriceModel2(ServiceType.TEMPLATE);
        priceModel.setCurrency(new SupportedCurrency(CURRENCY_CODE_2));
        SupportedCurrency oldCurrency = null;

        priceModel.setType(PRICEMODEL_TYPE_2);
        PriceModelType oldPriceModelType = PRICEMODEL_TYPE_2;

        // when
        logCollector.editPriceModelTypeToChargeable(dsMock, priceModel,
                voPriceModelKey, oldCurrency, oldPriceModelType, FREEPERIOD_1,
                PRICINGPERIOD_1);

        // then log, because currency is different
        verifyLogEntries(ServiceType.TEMPLATE, false, TRIAL_MODE_OFF,
                FREEPERIOD_2, PRICINGPERIOD_2);
    }

    @Test
    public void editPriceModelTypeToChargeable_priceModelTypeDifferent() {
        // given already persisted pricemodel, vo is greater than 0
        long voPriceModelKey = 543;

        // price model type different, same currency
        PriceModel priceModel = givenPriceModel2(ServiceType.TEMPLATE);
        priceModel.setCurrency(new SupportedCurrency(CURRENCY_CODE_2));
        SupportedCurrency oldCurrency = new SupportedCurrency(CURRENCY_CODE_2);
        priceModel.setType(PRICEMODEL_TYPE_2);
        PriceModelType oldPriceModelType = PRICEMODEL_TYPE_1;

        // when if (oldCurrencyCode == nul
        logCollector.editPriceModelTypeToChargeable(dsMock, priceModel,
                voPriceModelKey, oldCurrency, oldPriceModelType, FREEPERIOD_1,
                PRICINGPERIOD_1);

        // then log, because price model type is different
        verifyLogEntries(ServiceType.TEMPLATE, false, TRIAL_MODE_OFF,
                FREEPERIOD_2, PRICINGPERIOD_2);
    }
}
