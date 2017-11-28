/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.junit.Assert.assertEquals;
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
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceType;

public class PriceModelAuditLogCollector_EditServiceRolePriceTest {

    private static DataService dsMock;
    private static BigDecimal COPIED_VALUE = BigDecimal.TEN;

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
    public void editServiceRolePrice_auditLogActionUpdate() {
        // given
        long voPriceModelKey = 1;
        PriceModel priceModel = givenPriceModel(ServiceType.CUSTOMER_SUBSCRIPTION);
        PricedProductRole pricedProductRole = givenPricedProductRole();
        pricedProductRole.setPricePerUser(BigDecimal.valueOf(5));
        Organization targetCustomer = givenOrganization();
        Subscription subscription = givenSubscription();

        // when
        logCollector.editServiceRolePrice(dsMock, voPriceModelKey, priceModel,
                pricedProductRole, BigDecimal.valueOf(4.3), targetCustomer,
                subscription);

        // then
        verifyLogEntries(priceModel, pricedProductRole, targetCustomer,
                subscription);
    }

    @Test
    public void editServiceRolePrice_ServiceRolePriceCreatedInitially() {
        // given
        long voPriceModelKey = 0;
        PriceModel priceModel = givenPriceModel(ServiceType.CUSTOMER_SUBSCRIPTION);
        PricedProductRole pricedProductRole = givenPricedProductRole();
        pricedProductRole.setPricePerUser(BigDecimal.valueOf(5));
        Organization targetCustomer = givenOrganization();
        Subscription subscription = givenSubscription();

        // when
        logCollector.editServiceRolePrice(dsMock, voPriceModelKey, priceModel,
                pricedProductRole, COPIED_VALUE, targetCustomer, subscription);

        // then
        verifyLogEntries(priceModel, pricedProductRole, targetCustomer,
                subscription);
    }

    private PriceModel givenPriceModel(ServiceType serviceType) {
        PriceModel priceModel = new PriceModel();

        Product product = new Product();
        product.setKey(1000);
        product.setProductId("productId");
        product.setType(serviceType);
        priceModel.setProduct(product);
        priceModel.setCurrency(new SupportedCurrency("EUR"));
        priceModel.setPeriod(PricingPeriod.MONTH);
        return priceModel;
    }

    private Organization givenOrganization() {
        Organization organization = new Organization();
        organization.setOrganizationId("organizationId");
        organization.setName("organizationName");

        return organization;
    }

    private Subscription givenSubscription() {
        Subscription subscription = new Subscription();
        subscription.setKey(123L);
        subscription.setSubscriptionId("subscriptionId");

        return subscription;
    }

    private PricedProductRole givenPricedProductRole() {
        PricedProductRole ppr = new PricedProductRole();
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("roleId");
        ppr.setRoleDefinition(rd);
        return ppr;
    }

    private void verifyLogEntries(PriceModel priceModel,
            PricedProductRole pricedProductRole, Organization targetCustomer,
            Subscription subscription) {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry auditLogEntry = (BESAuditLogEntry) logEntries.get(0);
        Map<AuditLogParameter, String> parameters = auditLogEntry
                .getLogParameters();
        assertEquals(9, parameters.size());

        assertEquals(priceModel.getProduct().getProductId(),
                parameters.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                parameters.get(AuditLogParameter.SERVICE_NAME));

        assertEquals(priceModel.getCurrency().getCurrencyISOCode(),
                parameters.get(AuditLogParameter.CURRENCY_CODE));
        assertEquals(priceModel.getPeriod().name(),
                parameters.get(AuditLogParameter.TIMEUNIT));
        assertEquals(pricedProductRole.getRoleDefinition().getRoleId(),
                parameters.get(AuditLogParameter.USER_ROLE));
        assertEquals(pricedProductRole.getPricePerUser().toString(),
                parameters.get(AuditLogParameter.PRICE));
        assertEquals(targetCustomer.getOrganizationId(),
                parameters.get(AuditLogParameter.CUSTOMER_ID));
        assertEquals(targetCustomer.getName(),
                parameters.get(AuditLogParameter.CUSTOMER_NAME));
        assertEquals(subscription.getSubscriptionId(),
                parameters.get(AuditLogParameter.SUBSCRIPTION_NAME));
    }

}
