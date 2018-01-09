/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.auditlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

public class BESAuditLogEntryTest {
    private BESAuditLogEntry logEntry;

    public BESAuditLogEntryTest() {
        logEntry = spy(new BESAuditLogEntry(
                mockDataService("organizationName"), null, null,
                AuditLogParameter.values()));
    }

    @Test
    public void createBesAuditLogEntry() {
        // given
        DataService ds = mockDataService("orgName");
        AuditLogParameter parameters[] = new AuditLogParameter[] {
                AuditLogParameter.SERVICE_ID, AuditLogParameter.SERVICE_NAME };

        // when
        BESAuditLogEntry logEntry = spy(new BESAuditLogEntry(ds, "operationId",
                "operationName", parameters));

        // then
        assertEquals("operationId", logEntry.getOperationId());
        assertEquals("operationName", logEntry.getOperationName());
        assertEquals("userId", logEntry.getUserId());
        assertEquals("orgId", logEntry.getOrganizationId());
        assertEquals("orgName", logEntry.getOrganizationName());
        String expectedLog = "|"
                + getParameterName(AuditLogParameter.SERVICE_ID) + "|"
                + getParameterName(AuditLogParameter.SERVICE_NAME) + "|";
        assertEquals(expectedLog, logEntry.getLog());
    }

    private DataService mockDataService(String organizationName) {
        DataService ds = mock(DataService.class);
        PlatformUser user = new PlatformUser();
        user.setUserId("userId");
        Organization organization = new Organization();
        organization.setOrganizationId("orgId");
        organization.setName(organizationName);
        user.setOrganization(organization);
        doReturn(user).when(ds).getCurrentUser();
        return ds;
    }

    private String getParameterName(AuditLogParameter parameter) {
        return logEntry.resourceBundle.getString(parameter.name());
    }

    @Test
    public void addParameter() {
        // given
        logEntry = spy(new BESAuditLogEntry(
                mockDataService("organizationName"), null, null,
                new AuditLogParameter[] { AuditLogParameter.SERVICE_ID }));
        String parameterValue = "aServiceId";

        // when
        logEntry.addParameter(AuditLogParameter.SERVICE_ID, parameterValue);

        // then
        assertEquals("|" + getParameterName(AuditLogParameter.SERVICE_ID) + "=\""
                + parameterValue + "\"|", logEntry.getLog());
        assertTrue(logEntry.getLogParameters().containsKey(
                AuditLogParameter.SERVICE_ID));
        verify(logEntry, times(1)).escapeValueIfNeeded(eq(parameterValue));
    }

    @Test
    public void addParameter_UserRoleForService() {
        // given
        logEntry = spy(new BESAuditLogEntry(
                mockDataService("organizationName"), null, null,
                new AuditLogParameter[] { 
                    AuditLogParameter.TARGET_USER,
                    AuditLogParameter.USER_ROLE }));
        String targetUser = "fnst";
        String userRole = "administrator";

        // when
        logEntry.addParameter(AuditLogParameter.TARGET_USER, targetUser);
        logEntry.addParameter(AuditLogParameter.USER_ROLE, userRole);

        // then
        assertEquals("|" + getParameterName(AuditLogParameter.TARGET_USER) + "=\"" + targetUser + "\"|"
                + getParameterName(AuditLogParameter.USER_ROLE) + "=\"" + userRole + "\"|", 
                logEntry.getLog());
        assertTrue(logEntry.getLogParameters().containsKey(
                AuditLogParameter.TARGET_USER));
        assertTrue(logEntry.getLogParameters().containsKey(
                AuditLogParameter.USER_ROLE));
        verify(logEntry, times(1)).escapeValueIfNeeded(eq(targetUser));
        verify(logEntry, times(1)).escapeValueIfNeeded(eq(userRole));
    }

    @Test
    public void addParameter_nullValue() {
        // given
        logEntry = spy(new BESAuditLogEntry(
                mockDataService("organizationName"), null, null,
                new AuditLogParameter[] { AuditLogParameter.SERVICE_ID }));
        String parameterValue = null;

        // when
        logEntry.addParameter(AuditLogParameter.SERVICE_ID, parameterValue);

        // then
        assertEquals("|" + getParameter(AuditLogParameter.SERVICE_ID, "") + "|",
                logEntry.getLog());
        assertTrue(logEntry.getLogParameters().containsKey(
                AuditLogParameter.SERVICE_ID));
    }

    private String getParameter(AuditLogParameter key, String value) {
        return getParameterName(key) + "=\"" + value + "\"";
    }

    @Test
    public void escapeValueIfNeeded() {
        // given
        String parameterValue = "abc";

        // when
        String value = logEntry.escapeValueIfNeeded(parameterValue);

        // then
        assertEquals(parameterValue, value);
    }

    @Test
    public void escapeValueIfNeeded_withDoubleQuote() {
        // when
        String value = logEntry.escapeValueIfNeeded("a\"bc");

        // then value is same
        assertEquals("a\\\"bc", value);
    }

    @Test
    public void getOrganizationName() {
        // given
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.getOrganizationName();

        // then
        verify(logEntry, times(1)).escapeValueIfNeeded(eq("orgName"));
    }

    @Test
    public void addSubscription() {

        // given
        Subscription sub = givenSubscription();

        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addSubscription(sub);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.SUBSCRIPTION_NAME, sub.getSubscriptionId());
    }

    @Test
    public void addCustomer() {

        // given
        Organization org = givenCustomer();

        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addCustomer(org);

        // then
        verify(logEntry, times(1)).addParameter(AuditLogParameter.CUSTOMER_ID,
                org.getOrganizationId());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CUSTOMER_NAME, org.getName());
    }

    @Test
    public void addProduct() {

        // given
        Product prod = givenProduct();

        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        LocalizerServiceLocal localizer = mock(LocalizerServiceLocal.class);
        LocalizerFacade facade = mock(LocalizerFacade.class);
        doReturn(facade).when(logEntry).getLocalizerFacade(localizer);

        doReturn("productName").when(facade).getText(anyLong(),
                any(LocalizedObjectTypes.class));

        // when
        logEntry.addProduct(prod, localizer);

        // then
        verify(logEntry, times(1)).addParameter(AuditLogParameter.SERVICE_ID,
                prod.getProductId());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.SERVICE_NAME,
                "productName");
    }

    @Test
    public void addPricedEvent() {

        // given
        PricedEvent pe = givenPricedEvent();
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addPricedEvent(pe);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                pe.getPriceModel().getCurrency().getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.EVENT_NAME,
                pe.getEvent().getEventIdentifier());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.RANGE,
                "1-ANY");
        verify(logEntry, times(1)).addParameter(AuditLogParameter.PRICE,
                pe.getEventPrice().toString());
    }

    @Test
    public void addSteppedPricedEvent_WithLimit() {

        // given
        SteppedPrice sp = givenSteppedPriceEvent(true);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addSteppedPricedEvent(sp);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                sp.getPricedEvent().getPriceModel().getCurrency()
                        .getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.EVENT_NAME,
                sp.getPricedEvent().getEvent().getEventIdentifier());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.RANGE,
                sp.getLimit().toString());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.PRICE,
                sp.getPrice().toString());
    }

    @Test
    public void addSteppedPricedEvent_NoLimit() {

        // given
        SteppedPrice sp = givenSteppedPriceEvent(false);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addSteppedPricedEvent(sp);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                sp.getPricedEvent().getPriceModel().getCurrency()
                        .getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.EVENT_NAME,
                sp.getPricedEvent().getEvent().getEventIdentifier());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.RANGE,
                "ANY ABOVE");
        verify(logEntry, times(1)).addParameter(AuditLogParameter.PRICE,
                sp.getPrice().toString());
    }

    @Test
    public void addParameterSubscriptionPrice() {

        // given
        PricedParameter pp = givenPricedParameter();
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addParameterSubscriptionPrice(pp);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                pp.getPriceModel().getCurrency().getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                pp.getPriceModel().getPeriod().name());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.PARAMETER_NAME,
                pp.getParameter().getParameterDefinition().getParameterId());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.PRICE,
                pp.getPricePerSubscription().toString());
        verify(logEntry, times(1)).removeParameter(
                AuditLogParameter.OPTION_NAME);
    }

    @Test
    public void addParameterUserPrice() {

        // given
        PricedParameter pp = givenPricedParameter();
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addParameterUserPrice(pp);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                pp.getPriceModel().getCurrency().getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                pp.getPriceModel().getPeriod().name());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.PARAMETER_NAME,
                pp.getParameter().getParameterDefinition().getParameterId());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.PRICE,
                pp.getPricePerUser().toString());
        verify(logEntry, times(1)).removeParameter(
                AuditLogParameter.OPTION_NAME);
    }

    @Test
    public void addParameterUserRolePrice() {

        // given
        PricedParameter pp = givenPricedParameter();
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addParameterUserRolePrice(pp.getRoleSpecificUserPrices()
                .get(0));

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                pp.getPriceModel().getCurrency().getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                pp.getPriceModel().getPeriod().name());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.USER_ROLE,
                pp.getRoleSpecificUserPrices().get(0).getRoleDefinition()
                        .getRoleId());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.PARAMETER_NAME,
                pp.getParameter().getParameterDefinition().getParameterId());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.PRICE,
                pp.getRoleSpecificUserPrices().get(0).getPricePerUser()
                        .toString());
        verify(logEntry, times(1)).removeParameter(
                AuditLogParameter.OPTION_NAME);
    }

    @Test
    public void addParameterOptionUserRolePrice() {

        // given
        PricedOption po = givenPricedOption();
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addParameterOptionUserRolePrice(po.getRoleSpecificUserPrices()
                .get(0), "option_name");

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                po.getPricedParameter().getPriceModel().getCurrency()
                        .getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                po.getPricedParameter().getPriceModel().getPeriod().name());

        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.USER_ROLE,
                po.getRoleSpecificUserPrices().get(0).getRoleDefinition()
                        .getRoleId());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.PARAMETER_NAME,
                po.getPricedParameter().getParameter().getParameterDefinition()
                        .getParameterId());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.OPTION_NAME,
                "option_name");
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.PRICE,
                po.getRoleSpecificUserPrices().get(0).getPricePerUser()
                        .toString());
    }

    @Test
    public void addParameterOptionSubscriptionPrice() {

        // given
        PricedOption po = givenPricedOption();
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addParameterOptionSubscriptionPrice(po, "option_name");

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                po.getPricedParameter().getPriceModel().getCurrency()
                        .getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                po.getPricedParameter().getPriceModel().getPeriod().name());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.PARAMETER_NAME,
                po.getPricedParameter().getParameter().getParameterDefinition()
                        .getParameterId());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.OPTION_NAME,
                "option_name");
        verify(logEntry, times(1)).addParameter(AuditLogParameter.PRICE,
                po.getPricePerSubscription().toString());
    }

    @Test
    public void addParameterOptionUserPrice() {

        // given
        PricedOption po = givenPricedOption();
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addParameterOptionUserPrice(po, "option_name");

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                po.getPricedParameter().getPriceModel().getCurrency()
                        .getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                po.getPricedParameter().getPriceModel().getPeriod().name());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.PARAMETER_NAME,
                po.getPricedParameter().getParameter().getParameterDefinition()
                        .getParameterId());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.OPTION_NAME,
                "option_name");
        verify(logEntry, times(1)).addParameter(AuditLogParameter.PRICE,
                po.getPricePerUser().toString());
    }

    @Test
    public void addParameterSteppedPrice_WithLimit() {

        // given
        SteppedPrice sp = givenSteppedParameterPrice(true);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addParameterSteppedPrice(sp);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                sp.getPricedParameter().getPriceModel().getCurrency()
                        .getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.PARAMETER_NAME,
                sp.getPricedParameter().getParameter().getParameterDefinition()
                        .getParameterId());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.RANGE,
                sp.getLimit().toString());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.PRICE,
                sp.getPrice().toString());
        verify(logEntry, times(1)).removeParameter(
                AuditLogParameter.OPTION_NAME);
    }

    @Test
    public void addParameterSteppedPrice_NoLimit() {

        // given
        SteppedPrice sp = givenSteppedParameterPrice(false);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addParameterSteppedPrice(sp);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                sp.getPricedParameter().getPriceModel().getCurrency()
                        .getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.PARAMETER_NAME,
                sp.getPricedParameter().getParameter().getParameterDefinition()
                        .getParameterId());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.RANGE,
                "ANY ABOVE");
        verify(logEntry, times(1)).addParameter(AuditLogParameter.PRICE,
                sp.getPrice().toString());
        verify(logEntry, times(1)).removeParameter(
                AuditLogParameter.OPTION_NAME);
    }

    @Test
    public void addPriceModel_WithTrialPeriod() {

        // given
        PriceModel pm = givenPriceModel(true, PriceModelType.PRO_RATA);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addPriceModel(pm);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                pm.getCurrency().getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                pm.getPeriod().name());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CALCULATION_MODE, pm.getType().name());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.DAYS_OF_TRIAL,
                String.valueOf(pm.getDataContainer().getFreePeriod()));
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TRIAL_PERIOD,
                "ON");
    }

    @Test
    public void addPriceModel_FreeOfCharge() {

        // given
        PriceModel pm = givenPriceModel(true, PriceModelType.FREE_OF_CHARGE);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addPriceModel(pm);

        // then
        verify(logEntry, times(0)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                pm.getCurrency().getCurrencyISOCode());
        verify(logEntry, times(0)).addParameter(AuditLogParameter.TIMEUNIT,
                pm.getPeriod().name());
        verify(logEntry, times(0)).addParameter(
                AuditLogParameter.CALCULATION_MODE, pm.getType().name());
        verify(logEntry, times(0)).addParameter(
                AuditLogParameter.DAYS_OF_TRIAL,
                String.valueOf(pm.getDataContainer().getFreePeriod()));
        verify(logEntry, times(0)).addParameter(AuditLogParameter.TRIAL_PERIOD,
                "ON");
    }

    @Test
    public void addSubscriptionPrice() {

        // given
        PriceModel pm = givenPriceModel(true, PriceModelType.PRO_RATA);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addSubscriptionPrice(pm);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                pm.getCurrency().getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                pm.getPeriod().name());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.ONE_TIME_FEE,
                pm.getOneTimeFee().toString());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.RECURRING_CHARGE,
                pm.getPricePerPeriod().toString());
    }

    @Test
    public void addUserPrice() {

        // given
        PriceModel pm = givenPriceModel(true, PriceModelType.PRO_RATA);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addUserPrice(pm);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                pm.getCurrency().getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                pm.getPeriod().name());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.RANGE,
                "1-ANY");
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.RECURRING_CHARGE,
                pm.getPricePerUserAssignment().toString());
    }

    @Test
    public void addSteppedUserPrice_WithLimit() {

        // given
        SteppedPrice sp = givenSteppedUserPrice(true);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addSteppedUserPrice(sp);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                sp.getPriceModel().getCurrency().getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                sp.getPriceModel().getPeriod().name());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.RANGE,
                sp.getLimit().toString());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.RECURRING_CHARGE, sp.getPrice().toString());
    }

    @Test
    public void addSteppedUserPrice_NoLimit() {

        // given
        SteppedPrice sp = givenSteppedUserPrice(false);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addSteppedUserPrice(sp);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                sp.getPriceModel().getCurrency().getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                sp.getPriceModel().getPeriod().name());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.RANGE,
                "ANY ABOVE");
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.RECURRING_CHARGE, sp.getPrice().toString());
    }

    @Test
    public void addPriceModel_NoTrialPeriod() {

        // given
        PriceModel pm = givenPriceModel(false, PriceModelType.PRO_RATA);
        logEntry = spy(new BESAuditLogEntry(mockDataService("orgName"), null,
                null, AuditLogParameter.values()));

        // when
        logEntry.addPriceModel(pm);

        // then
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CURRENCY_CODE,
                pm.getCurrency().getCurrencyISOCode());
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TIMEUNIT,
                pm.getPeriod().name());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.CALCULATION_MODE, pm.getType().name());
        verify(logEntry, times(1)).addParameter(
                AuditLogParameter.DAYS_OF_TRIAL,
                String.valueOf(pm.getDataContainer().getFreePeriod()));
        verify(logEntry, times(1)).addParameter(AuditLogParameter.TRIAL_PERIOD,
                "OFF");
    }

    @Test
    public void getRootTemplateProduct_NoTemplate() {

        // given
        Product product = givenProduct();

        // when
        Product result = logEntry.getRootTemplateProduct(product);

        // then
        assertEquals(result, product);
    }

    @Test
    public void getRootTemplateProduct_HasTemplate() {

        // given
        Product product = givenProduct();
        Product template = new Product();
        template.setKey(2);
        template.setProductId("template_id");
        product.setTemplate(template);

        // when
        Product result = logEntry.getRootTemplateProduct(product);

        // then
        assertEquals(result, template);
    }

    private Subscription givenSubscription() {
        Subscription sub = new Subscription();
        sub.setKey(1);
        sub.setSubscriptionId("sub_id");
        return sub;
    }

    private Product givenProduct() {
        Product prod = new Product();
        prod.setKey(1);
        prod.setProductId("prod_id");
        return prod;
    }

    private Organization givenCustomer() {
        Organization org = new Organization();
        org.setOrganizationId("id");
        org.setName("name");
        return org;
    }

    private PriceModel givenPriceModel(boolean withFreePeriod,
            PriceModelType type) {
        PriceModel pm = new PriceModel();
        pm.setCurrency(new SupportedCurrency("EUR"));
        pm.setType(type);
        pm.setPeriod(PricingPeriod.MONTH);
        if (withFreePeriod) {
            pm.setFreePeriod(2);
        }
        return pm;
    }

    private PricedEvent givenPricedEvent() {
        Event e = new Event();
        e.setEventIdentifier("event_id");
        PricedEvent pe = new PricedEvent();
        pe.setEvent(e);
        pe.setPriceModel(givenPriceModel(false, PriceModelType.PRO_RATA));
        pe.setEventPrice(BigDecimal.TEN);
        return pe;
    }

    private SteppedPrice givenSteppedPriceEvent(boolean withLimit) {
        SteppedPrice sp = new SteppedPrice();
        sp.setPricedEvent(givenPricedEvent());
        sp.setPrice(BigDecimal.ONE);
        if (withLimit) {
            sp.setLimit(Long.valueOf(10));
        }
        return sp;
    }

    private SteppedPrice givenSteppedUserPrice(boolean withLimit) {
        SteppedPrice sp = new SteppedPrice();
        sp.setPriceModel(givenPriceModel(false, PriceModelType.PRO_RATA));
        sp.setPrice(BigDecimal.ONE);
        if (withLimit) {
            sp.setLimit(Long.valueOf(10));
        }
        return sp;
    }

    private PricedParameter givenPricedParameter() {
        ParameterDefinition pd = new ParameterDefinition();
        pd.setParameterId("param_id");
        Parameter p = new Parameter();
        p.setParameterDefinition(pd);
        PricedParameter pp = new PricedParameter();
        pp.setPriceModel(givenPriceModel(false, PriceModelType.PRO_RATA));
        pp.setParameter(p);
        pp.setPricePerSubscription(BigDecimal.TEN);
        pp.setPricePerUser(BigDecimal.ONE);
        PricedProductRole ppr = createPricedRole();
        ppr.setPricedParameter(pp);
        pp.setRoleSpecificUserPrices(Arrays.asList(ppr));
        return pp;
    }

    private PricedProductRole createPricedRole() {
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("role_id");
        PricedProductRole ppr = new PricedProductRole();
        ppr.setPricePerUser(BigDecimal.TEN);
        ppr.setRoleDefinition(rd);
        return ppr;
    }

    private PricedOption givenPricedOption() {
        PricedOption po = new PricedOption();
        po.setPricedParameter(givenPricedParameter());
        po.setPricePerSubscription(BigDecimal.TEN);
        po.setPricePerUser(BigDecimal.ONE);
        PricedProductRole ppr = createPricedRole();
        ppr.setPricedOption(po);
        po.setRoleSpecificUserPrices(Arrays.asList(ppr));
        return po;
    }

    private SteppedPrice givenSteppedParameterPrice(boolean withLimit) {
        SteppedPrice sp = new SteppedPrice();
        sp.setPricedParameter(givenPricedParameter());
        sp.setPrice(BigDecimal.ONE);
        if (withLimit) {
            sp.setLimit(Long.valueOf(10));
        }
        return sp;
    }
}
