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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
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
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
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
@SuppressWarnings("boxing")
public class PriceModelAuditLogCollector_EditParameterPricesTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";
    private final static String PARAMETER_NAME = "param_name";
    private final static String OPTION_NAME = "option_name";
    private final static BigDecimal PRICE_PER_SUBSCRIPTION = BigDecimal.TEN;
    private final static BigDecimal PRICE_PER_USER = BigDecimal.ONE;
    private final static Long PARAMETER_LIMIT = Long.valueOf(20);
    private final static String CURRENCY = "EUR";
    private final static long CUSTOMER_KEY = 10;
    private final static String CUSTOMER_ID = "customer_id";
    private final static String CUSTOMER_NAME = "customer_name";
    private final static PricingPeriod TIMEUNIT = PricingPeriod.MONTH;
    private final static String USER_ROLE_ID = "user_role_id";
    private static String ACTION_UPDATE = "UPDATE";
    private static String ACTION_DELETE = "DELETE";
    private static String ACTION_INSERT = "INSERT";

    private static DataService dsMock;
    private static LocalizerServiceLocal localizerMock;
    private static PriceModelAuditLogCollector logCollector = new PriceModelAuditLogCollector();
    private static BigDecimal COPIED_VALUE = BigDecimal.TEN;

    @BeforeClass
    public static void setup() {
        dsMock = mock(DataService.class);
        when(dsMock.getCurrentUser()).thenReturn(givenUser());
        localizerMock = mock(LocalizerServiceLocal.class);
        when(
                localizerMock.getLocalizedTextFromDatabase(Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.any(LocalizedObjectTypes.class))).thenReturn(
                OPTION_NAME);
        logCollector.localizer = localizerMock;
    }

    @Before
    public void cleadAuditLogData() {
        AuditLogData.clear();
    }

    @Test
    public void editParameterSubscriptionPrice_Subscription() {

        // given
        PricedParameter pp = givenPricedParameter(PriceModelType.SUBSCRIPTION);

        // when
        logCollector.editParameterSubscriptionPrice(dsMock, pp,
                DEFAULT_PRICE_VALUE);

        // then
        verifyParameterSubscriptionPrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editParameterUserPrice_Subscription() {

        // given
        PricedParameter pp = givenPricedParameter(PriceModelType.SUBSCRIPTION);

        // when
        logCollector.editParameterUserPrice(dsMock, pp, DEFAULT_PRICE_VALUE);

        // then
        verifyParameterUserPrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editParameterSubscriptionPrice_CustomerProduct() {

        // given
        PricedParameter pp = givenPricedParameter(PriceModelType.CUSTOMER_SERVICE);

        // when
        logCollector.editParameterSubscriptionPrice(dsMock, pp,
                DEFAULT_PRICE_VALUE);

        // then
        verifyParameterSubscriptionPrice(PriceModelType.CUSTOMER_SERVICE);
    }

    @Test
    public void editParameterUserPrice_CustomerProduct() {

        // given
        PricedParameter pp = givenPricedParameter(PriceModelType.CUSTOMER_SERVICE);

        // when
        logCollector.editParameterUserPrice(dsMock, pp, DEFAULT_PRICE_VALUE);

        // then
        verifyParameterUserPrice(PriceModelType.CUSTOMER_SERVICE);
    }

    @Test
    public void editParameterSubscriptionPrice_Product() {

        // given
        PricedParameter pp = givenPricedParameter(PriceModelType.SERVICE);

        // when
        logCollector.editParameterSubscriptionPrice(dsMock, pp,
                DEFAULT_PRICE_VALUE);

        // then
        verifyParameterSubscriptionPrice(PriceModelType.SERVICE);
    }

    @Test
    public void editParameterUserPrice_Product() {

        // given
        PricedParameter pp = givenPricedParameter(PriceModelType.SERVICE);

        // when
        logCollector.editParameterUserPrice(dsMock, pp, DEFAULT_PRICE_VALUE);

        // then
        verifyParameterUserPrice(PriceModelType.SERVICE);
    }

    @Test
    public void editParameterOptionSubscriptionPrice_Subscription() {

        // given
        PricedOption po = givenParameterPricedOption(PriceModelType.SUBSCRIPTION);

        // when
        logCollector.editParameterOptionSubscriptionPrice(dsMock, po,
                BigDecimal.ZERO);

        // then
        verifyParameterOptionSubscriptionPrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editParameterOptionSubscriptionPrice_CustomerProduct() {

        // given
        PricedOption po = givenParameterPricedOption(PriceModelType.CUSTOMER_SERVICE);

        // when
        logCollector.editParameterOptionSubscriptionPrice(dsMock, po,
                BigDecimal.ZERO);

        // then
        verifyParameterOptionSubscriptionPrice(PriceModelType.CUSTOMER_SERVICE);
    }

    @Test
    public void editParameterOptionSubscriptionPrice_Product() {

        // given
        PricedOption po = givenParameterPricedOption(PriceModelType.SERVICE);

        // when
        logCollector.editParameterOptionSubscriptionPrice(dsMock, po,
                BigDecimal.ZERO);

        // then
        verifyParameterOptionSubscriptionPrice(PriceModelType.SERVICE);
    }

    @Test
    public void editParameterOptionUserPrice_Subscription() {

        // given
        PricedOption po = givenParameterPricedOption(PriceModelType.SUBSCRIPTION);

        // when
        logCollector.editParameterOptionUserPrice(dsMock, po, BigDecimal.ZERO);

        // then
        verifyParameterOptionUserPrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editParameterOptionUserPrice_CustomerProduct() {

        // given
        PricedOption po = givenParameterPricedOption(PriceModelType.CUSTOMER_SERVICE);

        // when
        logCollector.editParameterOptionUserPrice(dsMock, po, BigDecimal.ZERO);

        // then
        verifyParameterOptionUserPrice(PriceModelType.CUSTOMER_SERVICE);
    }

    @Test
    public void editParameterOptionUserPrice_Product() {

        // given
        PricedOption po = givenParameterPricedOption(PriceModelType.SERVICE);

        // when
        logCollector.editParameterOptionUserPrice(dsMock, po, BigDecimal.ZERO);

        // then
        verifyParameterOptionUserPrice(PriceModelType.SERVICE);
    }

    @Test
    public void editParameterSteppedPrice_Subscription_NoLimit() {

        // given
        SteppedPrice sp = givenParameterSteppedPrice(
                PriceModelType.SUBSCRIPTION, false);

        // when
        logCollector.editParameterSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifyParameterSteppedPrice(PriceModelType.SUBSCRIPTION, false,
                ACTION_UPDATE);
    }

    @Test
    public void editParameterSteppedPrice_Subscription_WithLimit() {

        // given
        SteppedPrice sp = givenParameterSteppedPrice(
                PriceModelType.SUBSCRIPTION, true);

        // when
        logCollector.editParameterSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifyParameterSteppedPrice(PriceModelType.SUBSCRIPTION, true,
                ACTION_UPDATE);
    }

    @Test
    public void editParameterSteppedPrice_CustomerProduct_NoLimit() {

        // given
        SteppedPrice sp = givenParameterSteppedPrice(
                PriceModelType.CUSTOMER_SERVICE, false);

        // when
        logCollector.editParameterSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifyParameterSteppedPrice(PriceModelType.CUSTOMER_SERVICE, false,
                ACTION_UPDATE);
    }

    @Test
    public void editParameterSteppedPrice_CustomerProduct_WithLimit() {

        // given
        SteppedPrice sp = givenParameterSteppedPrice(
                PriceModelType.CUSTOMER_SERVICE, true);

        // when
        logCollector.editParameterSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifyParameterSteppedPrice(PriceModelType.CUSTOMER_SERVICE, true,
                ACTION_UPDATE);
    }

    @Test
    public void editParameterSteppedPrice_Product_NoLimit() {

        // given
        SteppedPrice sp = givenParameterSteppedPrice(PriceModelType.SERVICE,
                false);

        // when
        logCollector.editParameterSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifyParameterSteppedPrice(PriceModelType.SERVICE, false,
                ACTION_UPDATE);
    }

    @Test
    public void editParameterSteppedPrice_Product_WithLimit() {

        // given
        SteppedPrice sp = givenParameterSteppedPrice(PriceModelType.SERVICE,
                true);

        // when
        logCollector.editParameterSteppedPrice(dsMock, sp, DEFAULT_PRICE_VALUE,
                DEFAULT_STEPPED_PRICE_LIMIT);

        // then
        verifyParameterSteppedPrice(PriceModelType.SERVICE, true, ACTION_UPDATE);
    }

    @Test
    public void editParameterUserRolePrice_Subscription() {

        // given
        long voPriceModelKey = 1;
        PricedParameter pp = givenPricedParameter(PriceModelType.SUBSCRIPTION);

        // when
        logCollector.editParameterUserRolePrice(dsMock, voPriceModelKey, pp
                .getRoleSpecificUserPrices().get(0), DEFAULT_PRICE_VALUE);

        // then
        verifyParameterUserRolePrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editParameterUserRolePrice_CustomerProduct() {

        // given
        long voPriceModelKey = 1;
        PricedParameter pp = givenPricedParameter(PriceModelType.CUSTOMER_SERVICE);

        // when
        logCollector.editParameterUserRolePrice(dsMock, voPriceModelKey, pp
                .getRoleSpecificUserPrices().get(0), DEFAULT_PRICE_VALUE);

        // then
        verifyParameterUserRolePrice(PriceModelType.CUSTOMER_SERVICE);
    }

    @Test
    public void editParameterUserRolePrice_Product() {

        // given
        long voPriceModelKey = 1;
        PricedParameter pp = givenPricedParameter(PriceModelType.SERVICE);

        // when
        logCollector.editParameterUserRolePrice(dsMock, voPriceModelKey, pp
                .getRoleSpecificUserPrices().get(0), DEFAULT_PRICE_VALUE);

        // then
        verifyParameterUserRolePrice(PriceModelType.SERVICE);
    }

    @Test
    public void editParameterOptionUserRolePrice_Subscription() {

        // given
        PricedOption po = givenParameterPricedOption(PriceModelType.SUBSCRIPTION);

        // when
        logCollector.editParameterOptionUserRolePrice(dsMock, po
                .getRoleSpecificUserPrices().get(0), DEFAULT_PRICE_VALUE);

        // then
        verifyParameterOptionUserRolePrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editParameterOptionUserRolePrice_CustomerProduct() {

        // given
        PricedOption po = givenParameterPricedOption(PriceModelType.CUSTOMER_SERVICE);

        // when
        logCollector.editParameterOptionUserRolePrice(dsMock, po
                .getRoleSpecificUserPrices().get(0), DEFAULT_PRICE_VALUE);

        // then
        verifyParameterOptionUserRolePrice(PriceModelType.CUSTOMER_SERVICE);
    }

    @Test
    public void editParameterOptionUserRolePrice_Product() {

        // given
        PricedOption po = givenParameterPricedOption(PriceModelType.SERVICE);

        // when
        logCollector.editParameterOptionUserRolePrice(dsMock, po
                .getRoleSpecificUserPrices().get(0), DEFAULT_PRICE_VALUE);

        // then
        verifyParameterOptionUserRolePrice(PriceModelType.SERVICE);
    }

    @Test
    public void editParameterOptionUserRolePrice_pricedRoleCreatedInitially() {
        // given
        PricedOption po = givenParameterPricedOption(PriceModelType.SUBSCRIPTION);

        // when
        logCollector.editParameterOptionUserRolePrice(dsMock, po
                .getRoleSpecificUserPrices().get(0), COPIED_VALUE);

        // then
        verifyParameterOptionUserRolePrice(PriceModelType.SUBSCRIPTION);
    }

    private Map<AuditLogParameter, String> verifyLogEntry(PriceModelType type) {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        verifyProduct(logParams);
        if (type == PriceModelType.SUBSCRIPTION) {
            verifySubscription(logParams);
        } else {
            verifyNullSubscription(logParams);
        }
        if (type == PriceModelType.SERVICE) {
            verifyNullCustomer(logParams);
        } else {
            verifyCustomer(logParams);
        }
        return logParams;
    }

    private void verifyParameterSubscriptionPrice(PriceModelType type) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifyParameterSubscriptionPrice(logParams);
        verifyAction(logParams, null);
    }

    private void verifyParameterUserPrice(PriceModelType type) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifyParameterUserPrice(logParams);
    }

    private void verifyParameterUserRolePrice(PriceModelType type) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifyParameterUserRolePrice(logParams);
    }

    private void verifyParameterOptionSubscriptionPrice(PriceModelType type) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifyParameterOptionSubscriptionPrice(logParams);
    }

    private void verifyParameterOptionUserPrice(PriceModelType type) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifyParameterOptionUserPrice(logParams);
        verifyAction(logParams, null);
    }

    private void verifyParameterOptionUserRolePrice(PriceModelType type) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifyParameterOptionUserRolePrice(logParams);
    }

    private void verifyParameterSteppedPrice(PriceModelType type,
            boolean withLimit, String action) {
        Map<AuditLogParameter, String> logParams = verifyLogEntry(type);
        verifyParameterSteppedPrice(logParams, withLimit);
        verifyAction(logParams, action);
    }

    private void verifyCustomer(Map<AuditLogParameter, String> logParams) {
        assertEquals(CUSTOMER_ID, logParams.get(AuditLogParameter.CUSTOMER_ID));
        assertEquals(CUSTOMER_NAME,
                logParams.get(AuditLogParameter.CUSTOMER_NAME));
    }

    private void verifyProduct(Map<AuditLogParameter, String> logParams) {
        assertEquals(PRODUCT_ID, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(OPTION_NAME, logParams.get(AuditLogParameter.SERVICE_NAME));
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

    private void verifyParameterSubscriptionPrice(
            Map<AuditLogParameter, String> logParams) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(TIMEUNIT.name(), logParams.get(AuditLogParameter.TIMEUNIT));
        assertEquals(PARAMETER_NAME,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(PRICE_PER_SUBSCRIPTION.toString(),
                logParams.get(AuditLogParameter.PRICE));
    }

    private void verifyParameterUserPrice(
            Map<AuditLogParameter, String> logParams) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(TIMEUNIT.name(), logParams.get(AuditLogParameter.TIMEUNIT));
        assertEquals(PARAMETER_NAME,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(PRICE_PER_USER.toString(),
                logParams.get(AuditLogParameter.PRICE));
    }

    private void verifyParameterUserRolePrice(
            Map<AuditLogParameter, String> logParams) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(TIMEUNIT.name(), logParams.get(AuditLogParameter.TIMEUNIT));
        assertEquals(USER_ROLE_ID, logParams.get(AuditLogParameter.USER_ROLE));
        assertEquals(PARAMETER_NAME,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(PRICE_PER_USER.toString(),
                logParams.get(AuditLogParameter.PRICE));
    }

    private void verifyParameterOptionSubscriptionPrice(
            Map<AuditLogParameter, String> logParams) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(PARAMETER_NAME,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(OPTION_NAME, logParams.get(AuditLogParameter.OPTION_NAME));
        assertEquals(PRICE_PER_SUBSCRIPTION.toString(),
                logParams.get(AuditLogParameter.PRICE));
    }

    private void verifyParameterOptionUserPrice(
            Map<AuditLogParameter, String> logParams) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(TIMEUNIT.name(), logParams.get(AuditLogParameter.TIMEUNIT));
        assertEquals(PARAMETER_NAME,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(OPTION_NAME, logParams.get(AuditLogParameter.OPTION_NAME));
        assertEquals(PRICE_PER_USER.toString(),
                logParams.get(AuditLogParameter.PRICE));
    }

    private void verifyParameterOptionUserRolePrice(
            Map<AuditLogParameter, String> logParams) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(TIMEUNIT.name(), logParams.get(AuditLogParameter.TIMEUNIT));
        assertEquals(PARAMETER_NAME,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(OPTION_NAME, logParams.get(AuditLogParameter.OPTION_NAME));
        assertEquals(PRICE_PER_USER.toString(),
                logParams.get(AuditLogParameter.PRICE));
    }

    private void verifyAction(Map<AuditLogParameter, String> logParams,
            String action) {
        if (action == null) {
            assertNull(logParams.get(AuditLogParameter.ACTION_NAME));
        } else {
            assertEquals(action, logParams.get(AuditLogParameter.ACTION_NAME));
        }
    }

    private void verifyParameterSteppedPrice(
            Map<AuditLogParameter, String> logParams, boolean withLimit) {
        assertEquals(CURRENCY, logParams.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(TIMEUNIT.name(), logParams.get(AuditLogParameter.TIMEUNIT));
        assertEquals(PARAMETER_NAME,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(PRICE_PER_SUBSCRIPTION.toString(),
                logParams.get(AuditLogParameter.PRICE));
        if (withLimit) {
            assertEquals(PARAMETER_LIMIT.toString(),
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

    private Parameter createParameter() {
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setParameterId(PARAMETER_NAME);
        Parameter param = new Parameter();
        param.setParameterDefinition(paramDef);
        return param;
    }

    private PriceModel createPriceModel(PriceModelType type) {
        PriceModel pm = new PriceModel();
        if (type == PriceModelType.SUBSCRIPTION) {
            pm.setProduct(createSubscriptionProduct());
        } else if (type == PriceModelType.SERVICE) {
            pm.setProduct(createTemplateProduct());
        } else if (type == PriceModelType.CUSTOMER_SERVICE) {
            pm.setProduct(createCustomerProduct());
        }
        pm.setCurrency(new SupportedCurrency(CURRENCY));
        pm.setPeriod(TIMEUNIT);
        return pm;
    }

    private SteppedPrice givenParameterSteppedPrice(PriceModelType type,
            boolean withLimit) {
        PriceModel pm = createPriceModel(type);
        PricedParameter pp = new PricedParameter();
        pp.setParameter(createParameter());
        pp.setPricePerSubscription(BigDecimal.ZERO);
        pp.setPricePerUser(PRICE_PER_USER);
        pp.setPriceModel(pm);
        SteppedPrice sp = new SteppedPrice();
        sp.setPricedParameter(pp);
        if (withLimit) {
            sp.setLimit(PARAMETER_LIMIT);
        }
        sp.setPrice(PRICE_PER_SUBSCRIPTION);
        return sp;
    }

    private PricedParameter givenPricedParameter(PriceModelType type) {
        PriceModel pm = createPriceModel(type);
        PricedParameter pp = new PricedParameter();
        pp.setPricePerSubscription(PRICE_PER_SUBSCRIPTION);
        pp.setPricePerUser(PRICE_PER_USER);
        pp.setParameter(createParameter());
        pp.setPriceModel(pm);
        PricedProductRole ppr = createPricedRole();
        ppr.setPricedParameter(pp);
        pp.setRoleSpecificUserPrices(Arrays.asList(ppr));
        return pp;
    }

    /**
     * @return
     */
    private PricedProductRole createPricedRole() {
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId(USER_ROLE_ID);
        PricedProductRole ppr = new PricedProductRole();
        ppr.setRoleDefinition(rd);
        ppr.setPricePerUser(PRICE_PER_USER);
        return ppr;
    }

    private PricedOption givenParameterPricedOption(PriceModelType type) {
        PriceModel pm = createPriceModel(type);
        PricedParameter pp = new PricedParameter();
        pp.setParameter(createParameter());
        pp.setPricePerSubscription(PRICE_PER_SUBSCRIPTION);
        pp.setPricePerUser(PRICE_PER_USER);
        pp.setPriceModel(pm);
        PricedOption po = new PricedOption();
        po.setPricePerSubscription(PRICE_PER_SUBSCRIPTION);
        po.setPricePerUser(PRICE_PER_USER);
        po.setPricedParameter(pp);
        PricedProductRole ppr = createPricedRole();
        ppr.setPricedOption(po);
        po.setRoleSpecificUserPrices(Arrays.asList(ppr));
        pp.setPricedOptionList(Arrays.asList(po));
        return po;
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
    public void editParameterOptionUserRolePrice_samePrice() {
        // given
        PricedProductRole pricedProductRole = new PricedProductRole();
        pricedProductRole.setPricePerUser(BigDecimal.valueOf(34));

        // when
        logCollector.editParameterOptionUserRolePrice(dsMock,
                pricedProductRole, BigDecimal.valueOf(34));

        // then
        assertTrue(auditLogDataIsEmpty());
    }

    private boolean auditLogDataIsEmpty() {
        return AuditLogData.get() == null || AuditLogData.get().isEmpty();
    }

    @Test
    public void editParameterOptionUserRolePrice_changedPrice() {
        // given
        PricedOption pricedParameter = givenParameterPricedOption(PriceModelType.SUBSCRIPTION);
        PricedProductRole pricedProductRole = pricedParameter
                .getRoleSpecificUserPrices().get(0);
        pricedProductRole.setPricePerUser(BigDecimal.valueOf(1));

        // when
        logCollector.editParameterOptionUserRolePrice(dsMock,
                pricedProductRole, BigDecimal.valueOf(1.01));

        // then
        verifyParameterUserRolePrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editParameterUserRolePrice_samePrice() {
        // given
        long voPriceModelKey = 1;
        PricedProductRole pricedProductRole = new PricedProductRole();
        pricedProductRole.setPricePerUser(BigDecimal.valueOf(34));

        // when
        logCollector.editParameterUserRolePrice(dsMock, voPriceModelKey,
                pricedProductRole, BigDecimal.valueOf(34));

        // then
        assertTrue(auditLogDataIsEmpty());
    }

    @Test
    public void editParameterUserRolePrice_changedPrice() {
        // given
        long voPriceModelKey = 1;
        PricedParameter pricedParameter = givenPricedParameter(PriceModelType.SUBSCRIPTION);
        PricedProductRole pricedProductRole = pricedParameter
                .getRoleSpecificUserPrices().get(0);
        pricedProductRole.setPricePerUser(BigDecimal.valueOf(1));

        // when
        logCollector.editParameterUserRolePrice(dsMock, voPriceModelKey,
                pricedProductRole, BigDecimal.valueOf(1.01));

        // then
        verifyParameterUserRolePrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void editParameterUserRolePrice_parameterUserRoleCreatedInitially() {
        // given
        long voPriceModelKey = 0;
        PricedParameter pp = givenPricedParameter(PriceModelType.SUBSCRIPTION);

        // when
        logCollector.editParameterUserRolePrice(dsMock, voPriceModelKey, pp
                .getRoleSpecificUserPrices().get(0), COPIED_VALUE);

        // then
        verifyParameterUserRolePrice(PriceModelType.SUBSCRIPTION);
    }

    @Test
    public void steppedPriceDataChanged_samePriceAndLimit() {
        // given
        SteppedPrice steppedPrice = new SteppedPrice();
        steppedPrice.setPrice(BigDecimal.valueOf(34));
        steppedPrice.setLimit(5L);

        BigDecimal oldPrice = BigDecimal.valueOf(34);
        Long oldLimit = 5L;

        // when
        boolean dataChanged = logCollector.steppedPriceDataChanged(
                steppedPrice, oldPrice, oldLimit);

        // then
        assertFalse(dataChanged);
    }

    @Test
    public void steppedPriceDataChanged_priceChanged() {
        // given
        SteppedPrice steppedPrice = new SteppedPrice();
        steppedPrice.setPrice(BigDecimal.valueOf(34));
        steppedPrice.setLimit(5L);

        BigDecimal oldPrice = BigDecimal.valueOf(34.1);
        Long oldLimit = 5L;

        // when
        boolean dataChanged = logCollector.steppedPriceDataChanged(
                steppedPrice, oldPrice, oldLimit);

        // then
        assertTrue(dataChanged);
    }

    @Test
    public void steppedPriceDataChanged_limitChanged() {
        // given
        SteppedPrice steppedPrice = new SteppedPrice();
        steppedPrice.setPrice(BigDecimal.valueOf(34));
        steppedPrice.setLimit(5L);

        BigDecimal oldPrice = BigDecimal.valueOf(34);
        Long oldLimit = 6L;

        // when
        boolean dataChanged = logCollector.steppedPriceDataChanged(
                steppedPrice, oldPrice, oldLimit);

        // then
        assertTrue(dataChanged);
    }

    @Test
    public void insertParameterSteppedPrice() {

        // given
        SteppedPrice steppedPrice = givenParameterSteppedPrice(
                PriceModelType.SUBSCRIPTION, true);

        // when
        logCollector.insertParameterSteppedPrice(dsMock, steppedPrice);

        // then
        verifyParameterSteppedPrice(PriceModelType.SUBSCRIPTION, true,
                ACTION_INSERT);
    }

    @Test
    public void removeParameterSteppedPrice() {

        // given
        SteppedPrice steppedPrice = givenParameterSteppedPrice(
                PriceModelType.SUBSCRIPTION, true);

        // when
        logCollector.removeParameterSteppedPrice(dsMock, steppedPrice);

        // then
        verifyParameterSteppedPrice(PriceModelType.SUBSCRIPTION, true,
                ACTION_DELETE);
    }

    @Test
    public void removePricedParameters_parameterCreatedInitially() {
        // given
        long voPriceModelKey = 0;
        PricedParameter pricedParameter = new PricedParameter();

        // when
        logCollector.removeParameterSubscriptionPrice(dsMock, voPriceModelKey,
                pricedParameter);

        // then
        assertTrue(auditLogDataIsEmpty());
    }

    @Test
    public void removeEventPrice_eventAlreadyExisting() {
        // given
        long voPriceModelKey = 1;
        PricedParameter pricedParameter = givenPricedParameter(PriceModelType.SUBSCRIPTION);

        // when
        logCollector.removeParameterSubscriptionPrice(dsMock, voPriceModelKey,
                pricedParameter);

        // then
        Map<AuditLogParameter, String> logParams = verifyLogEntry(PriceModelType.SUBSCRIPTION);
        assertEquals(BigDecimal.ZERO.toString(),
                logParams.get(AuditLogParameter.PRICE));
    }

    @Test
    public void editParameterUserPrice_samePrice() {

        // given
        PricedParameter pp = givenPricedParameter(PriceModelType.CUSTOMER_SERVICE);
        pp.setPricePerUser(BigDecimal.TEN);

        // when
        logCollector.editParameterUserPrice(dsMock, pp, BigDecimal.TEN);

        // then
        assertTrue(auditLogDataIsEmpty());
    }

    @Test
    public void editParameterSubscriptionPrice_samePrice() {
        // given
        PricedParameter pricedParameter = new PricedParameter();
        pricedParameter.setPricePerSubscription(BigDecimal.TEN);

        // when
        logCollector.editParameterSubscriptionPrice(dsMock, pricedParameter,
                BigDecimal.TEN);

        // then
        assertTrue(auditLogDataIsEmpty());
    }

    @Test
    public void editParameterOptionSubscriptionPrice_samePrice() {
        // given
        PricedOption pricedOption = new PricedOption();
        pricedOption.setPricePerSubscription(BigDecimal.TEN);

        // when
        logCollector.editParameterOptionSubscriptionPrice(dsMock, pricedOption,
                BigDecimal.TEN);

        // then
        assertTrue(auditLogDataIsEmpty());
    }

    @Test
    public void editParameterOptionUserPrice_samePrice() {
        // given
        PricedOption pricedOption = new PricedOption();
        pricedOption.setPricePerUser(BigDecimal.TEN);

        // when
        logCollector.editParameterOptionUserPrice(dsMock, pricedOption,
                BigDecimal.TEN);

        // then
        assertTrue(auditLogDataIsEmpty());
    }
}
