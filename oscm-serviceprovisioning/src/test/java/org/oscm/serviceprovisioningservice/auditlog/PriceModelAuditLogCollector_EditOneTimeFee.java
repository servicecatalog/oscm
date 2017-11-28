/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Oct 15, 2013                                                      
 *                                                                              
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
import org.oscm.domobjects.Product;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogOperation.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceType;

/**
 * @author Zhou
 * 
 *         Unit test for {@link PriceModelAuditLogCollector#editOneTimeFee}
 */
public class PriceModelAuditLogCollector_EditOneTimeFee {
    private final static BigDecimal OLD_ONETIME_FEE = BigDecimal.ZERO;
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";
    // private final static BigDecimal USER_PRICE = BigDecimal.TEN;
    private final static String CURRENCY = "EUR";
    private final static PricingPeriod TIMEUNIT = PricingPeriod.MONTH;
    private final static BigDecimal ONETIME_FEE = BigDecimal.TEN;
    private final static long CUSTOMER_KEY = 10;
    private final static String CUSTOMER_ID = "customer_id";
    private final static String CUSTOMER_NAME = "customer_name";

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
    public void editOneTimeFee_CreateForService() {
        // given
        PriceModel pm = givenPriceModel(PriceModelType.SERVICE);

        // when
        logCollector.editOneTimeFee(dsMock, pm, null, true);

        // then
        verifyLogEntries(PriceModelType.SERVICE);
    }

    @Test
    public void editOneTimeFee_CreateForCustormer() {
        // given
        PriceModel pm = givenPriceModel(PriceModelType.CUSTOMER_SERVICE);

        // when
        logCollector.editOneTimeFee(dsMock, pm, null, true);

        // then
        verifyLogEntries(PriceModelType.CUSTOMER_SERVICE);
    }

    @Test
    public void editOneTimeFee_ChangeForService() {
        // given
        PriceModel pm = givenPriceModel(PriceModelType.SERVICE);

        // when
        logCollector.editOneTimeFee(dsMock, pm, OLD_ONETIME_FEE, false);

        // then
        verifyLogEntries(PriceModelType.SERVICE);
    }

    @Test
    public void editOneTimeFee_ChangeForCustormer() {
        // given
        PriceModel pm = givenPriceModel(PriceModelType.CUSTOMER_SERVICE);

        // when
        logCollector.editOneTimeFee(dsMock, pm, OLD_ONETIME_FEE, false);

        // then
        verifyLogEntries(PriceModelType.CUSTOMER_SERVICE);
    }

    @Test
    public void editOneTimeFee_NoChange() {
        // given
        PriceModel pm = givenPriceModel(PriceModelType.SERVICE);

        // when
        logCollector.editOneTimeFee(dsMock, pm, ONETIME_FEE, false);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(auditLogDataIsEmpty()));
    }

    private PriceModel givenPriceModel(PriceModelType type) {
        PriceModel pm = new PriceModel();
        if (type == PriceModelType.SERVICE) {
            pm.setProduct(createTemplateProduct());
        } else if (type == PriceModelType.CUSTOMER_SERVICE) {
            pm.setProduct(createCustomerProduct());
        }
        pm.setCurrency(new SupportedCurrency(CURRENCY));
        pm.setOneTimeFee(ONETIME_FEE);
        pm.setPeriod(PricingPeriod.MONTH);
        return pm;
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

    private void verifyLogEntries(PriceModelType type) {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry auditLogEntry = (BESAuditLogEntry) logEntries.get(0);
        Map<AuditLogParameter, String> parameters = auditLogEntry
                .getLogParameters();

        switch (type) {
        case CUSTOMER_SERVICE:
            assertEquals(CUSTOMER_ID,
                    parameters.get(AuditLogParameter.CUSTOMER_ID));
            assertEquals(CUSTOMER_NAME,
                    parameters.get(AuditLogParameter.CUSTOMER_NAME));
        case SERVICE:
            assertEquals(PRODUCT_ID,
                    parameters.get(AuditLogParameter.SERVICE_ID));
            assertEquals(LOCALIZED_RESOURCE,
                    parameters.get(AuditLogParameter.SERVICE_NAME));
        default:
            assertEquals(CURRENCY,
                    parameters.get(AuditLogParameter.CURRENCY_CODE));
            assertEquals(TIMEUNIT.name(),
                    parameters.get(AuditLogParameter.TIMEUNIT));
            assertEquals(ONETIME_FEE.toString(),
                    parameters.get(AuditLogParameter.ONE_TIME_FEE));
        }
    }

    private boolean auditLogDataIsEmpty() {
        return AuditLogData.get() == null || AuditLogData.get().isEmpty();
    }
}
