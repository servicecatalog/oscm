/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.intf.AccountService;
import org.oscm.intf.ReportingService;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.enumtypes.ReportType;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOReport;
import org.oscm.vo.VOUserDetails;
@Ignore
public class ReportingServiceWSTest {

    private static String supplierUserKey;
    private static String technologyProviderKey;
    private static String customerKey;
    private static VOFactory factory = new VOFactory();
    private static ReportingService reportingService_Operator;
    private static ReportingService reportingService_Supplier;
    private static ReportingService reportingService_TechnologyProvider;
    private static ReportingService reportingService_Customer;
    private static AccountService accountService_Supplier;

    private static String[] reportsForCustomer = { "Customer_PaymentPreview",
            "Customer_BillingDetails", "Event", "Subscription" };

    private static String[] reportsForOperator = { "ExternalServices",
            "Supplier_Revenue", "Partner_RevenueShare",
            "Suppliers_RevenueShare", "Supplier_BillingOfASupplier",
            "Supplier_BillingDetailsOfASupplier",
            "Supplier_CustomerOfASupplier", "Supplier_ProductOfASupplier" };

    private static String[] reportsForSupplier = { "Supplier_BillingDetails",
            "Supplier_PaymentResultStatus", "Supplier_Billing",
            "Supplier_Customer", "Supplier_Product", "Supplier_RevenueShare" };

    private static String[] reportsForTechnologyProvider = {
            "Provider_Instance", "Provider_Subscription", "Provider_Supplier",
            "Provider_Event" };

    @BeforeClass
    public static void setup() throws Exception {
        init();
    }

    private static void init() throws Exception {
        // new reporting service for operator
        reportingService_Operator = ServiceFactory.getDefault()
                .getReportingService(
                        WebserviceTestBase.getPlatformOperatorKey(),
                        WebserviceTestBase.getPlatformOperatorPassword());

        // new reporting service for supplier
        String userId = (String) createOrganizationAndReturnUser(OrganizationRoleType.SUPPLIER)[1];
        supplierUserKey = WebserviceTestBase.readLastMailAndSetCommonPassword(userId);
        reportingService_Supplier = ServiceFactory.getDefault()
                .getReportingService(supplierUserKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);

        accountService_Supplier = ServiceFactory.getDefault()
                .getAccountService(supplierUserKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);

        // new reporting service for technology provider
        userId = (String) createOrganizationAndReturnUser(OrganizationRoleType.TECHNOLOGY_PROVIDER)[1];
        technologyProviderKey = WebserviceTestBase
                .readLastMailAndSetCommonPassword(userId);

        reportingService_TechnologyProvider = ServiceFactory.getDefault()
                .getReportingService(technologyProviderKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);

        // supplier registers a new customer
        VOOrganization newCustomer = factory.createOrganizationVO();
        VOUserDetails newCustomerUser = factory.createUserVO("Mr.Bean_"
                + WebserviceTestBase.createUniqueKey());

        accountService_Supplier.registerKnownCustomer(newCustomer,
                newCustomerUser, null,
                WebserviceTestBase.getGlobalMarketplaceId());
        customerKey = WebserviceTestBase.readLastMailAndSetCommonPassword(newCustomerUser.getUserId());

        reportingService_Customer = ServiceFactory.getDefault()
                .getReportingService(customerKey,
                        WebserviceTestBase.DEFAULT_PASSWORD);
    }

    @Test
    public void testGetReportsForOperator() {
        assertReports(
                getExpectedReportsForRole(OrganizationRoleType.PLATFORM_OPERATOR),
                getAvailableReportsFromWebservice(reportingService_Operator));
    }

    @Test
    public void testGetReportsForSupplier() {
        assertReports(getExpectedReportsForRole(OrganizationRoleType.SUPPLIER),
                getAvailableReportsFromWebservice(reportingService_Supplier));
    }

    @Test
    public void testGetReportsForTechnologyProvider() {
        assertReports(
                getExpectedReportsForRole(OrganizationRoleType.TECHNOLOGY_PROVIDER),
                getAvailableReportsFromWebservice(reportingService_TechnologyProvider));
    }

    @Test
    public void testGetReportsForCustomer() {
        assertReports(getExpectedReportsForRole(OrganizationRoleType.CUSTOMER),
                getAvailableReportsFromWebservice(reportingService_Customer));
    }

    private Map<String, VOReport> getAvailableReportsFromWebservice(
            ReportingService service) {
        assertNotNull(service);

        Map<String, VOReport> result = new HashMap<String, VOReport>();
        List<VOReport> reports = service.getAvailableReports(ReportType.ALL);
        assertNotNull(reports);

        for (VOReport voReport : reports) {
            result.put(voReport.getReportName(), voReport);
        }

        return result;
    }

    private void assertReports(List<String> expectedReports,
            Map<String, VOReport> actualReportsFromWebservice) {
        assertNotNull(expectedReports);
        assertNotNull(actualReportsFromWebservice);
        assertEquals(expectedReports.size(), actualReportsFromWebservice.size());

        for (String reportName : expectedReports) {
            VOReport report = actualReportsFromWebservice.get(reportName);

            assertNotNull(report);
            assertNotNull(report.getReportName());
            assertNotNull(report.getLocalizedReportName());
            assertTrue(report.getLocalizedReportName().length() > 0);
        }
    }

    private static VOOrganization createOrganization(OrganizationRoleType role)
            throws Exception {
        return WebserviceTestBase.createOrganization(role.name() + "_"
                + WebserviceTestBase.createUniqueKey(), role.name(), role);
    }

    private static Object[] createOrganizationAndReturnUser(OrganizationRoleType role)
            throws Exception {
        return WebserviceTestBase.createOrganizationAndReturnUser(role.name() + "_"
                + WebserviceTestBase.createUniqueKey(), role.name(), role);
    }

    private List<String> getExpectedReportsForRole(OrganizationRoleType role) {
        List<String> result = new LinkedList<String>();

        if (role == OrganizationRoleType.PLATFORM_OPERATOR) {
            result.addAll(Arrays.asList(reportsForOperator));
            result.addAll(Arrays.asList(reportsForCustomer));

        } else if (role == OrganizationRoleType.SUPPLIER) {
            result.addAll(Arrays.asList(reportsForSupplier));
            result.addAll(Arrays.asList(reportsForCustomer));

        } else if (role == OrganizationRoleType.CUSTOMER) {
            result.addAll(Arrays.asList(reportsForCustomer));

        } else if (role == OrganizationRoleType.TECHNOLOGY_PROVIDER) {
            result.addAll(Arrays.asList(reportsForTechnologyProvider));
            result.addAll(Arrays.asList(reportsForCustomer));
        }

        return result;
    }
}
