/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Sep 18, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.billingservice.service.model.BillingRun;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentResult;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.reportingservice.business.model.billing.RDOCustomerPaymentPreview;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.reportingservice.dao.BillingDao.ReportData;
import org.oscm.reportingservice.dao.UnitDao;
import org.oscm.stream.Streams;
import org.w3c.dom.Document;

/**
 * @author kulle
 * 
 */
public class CustomerPaymentPreviewReportTest {

    private static long INVOCATION_TIME = 1325376000000L;

    private CustomerPaymentPreviewReport reporting;
    private BillingDao billingDao;
    private UnitDao unitDao;
    private BillingServiceLocal billingService;
    private static final File XML_FILE_UPGRADE = new File("javares/Upgrade.xml");
    private static final List<Long> UNIT_KEYS = Arrays.asList(
            Long.valueOf(100L), Long.valueOf(200L));

    @Before
    public void setup() {
        billingDao = mock(BillingDao.class);
        unitDao = mock(UnitDao.class);
        doReturn(UNIT_KEYS).when(unitDao).retrieveUnitKeysForUnitAdmin(
                anyLong());
        billingService = mock(BillingServiceLocal.class);
        reporting = new CustomerPaymentPreviewReport(billingDao, unitDao,
                billingService, null);
    }

    private PlatformUser givenUser(boolean unitAdmin, boolean orgAdmin,
            OrganizationRoleType... roles) {
        Organization o = new Organization();
        o.setOrganizationId("OrganizationId");
        o.setKey(1L);
        Set<OrganizationToRole> grantedRoles = new HashSet<OrganizationToRole>();
        for (OrganizationRoleType roleType : roles) {
            OrganizationToRole otr = new OrganizationToRole();
            otr.setOrganizationRole(new OrganizationRole(roleType));
            otr.setOrganization(o);
            grantedRoles.add(otr);
        }
        o.setGrantedRoles(grantedRoles);

        PlatformUser user = new PlatformUser();
        user.setKey(10L);
        user.setOrganization(o);
        user.setLocale("en");

        if (orgAdmin) {
            RoleAssignment roleAssignment = new RoleAssignment();
            roleAssignment
                    .setRole(new UserRole(UserRoleType.ORGANIZATION_ADMIN));
            roleAssignment.setUser(user);
            user.getAssignedRoles().add(roleAssignment);
        }

        if (unitAdmin) {
            RoleAssignment roleAssignment = new RoleAssignment();
            roleAssignment
                    .setRole(new UserRole(UserRoleType.UNIT_ADMINISTRATOR));
            roleAssignment.setUser(user);
            user.getAssignedRoles().add(roleAssignment);
        }

        return user;
    }

    @Test
    public void build_accessAsSupplier() throws Exception {
        // given
        PlatformUser user = givenUser(false, true,
                OrganizationRoleType.SUPPLIER, OrganizationRoleType.CUSTOMER);
        mockCalculateBillingResultsForPaymentPreview(null, user
                .getOrganization().getKey());

        // when
        RDOCustomerPaymentPreview result = reporting.buildReport(user);

        // then
        assertNotNull(result);
        assertNotNull(result.getSummaries());
    }

    @Test
    public void build_accessAsProvider() throws Exception {
        // given
        PlatformUser user = givenUser(false, true,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.CUSTOMER);
        mockCalculateBillingResultsForPaymentPreview(null, user
                .getOrganization().getKey());

        // when
        RDOCustomerPaymentPreview result = reporting.buildReport(user);

        // then
        assertNotNull(result);
        assertNotNull(result.getSummaries());
        verify(billingService).generatePaymentPreviewReport(
                eq(user.getOrganization().getKey()));
    }

    @Test
    public void build_accessAsProvider_Unit() throws Exception {
        // given
        PlatformUser user = givenUser(true, false,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.CUSTOMER);
        mockCalculateBillingResultsForPaymentPreview(null, user
                .getOrganization().getKey());

        // when
        RDOCustomerPaymentPreview result = reporting.buildReport(user);

        // then
        assertNotNull(result);
        assertNotNull(result.getSummaries());
        verify(billingService).generatePaymentPreviewReport(
                eq(user.getOrganization().getKey()), eq(UNIT_KEYS));
    }

    @Test
    public void build_upgrade() throws Exception {
        // given
        PlatformUser user = givenUser(false, true,
                OrganizationRoleType.CUSTOMER);
        mockCalculateBillingResultsForPaymentPreview(
                Arrays.asList(XML_FILE_UPGRADE), user.getOrganization()
                        .getKey());
        mockReportDaoData();

        // when
        RDOCustomerPaymentPreview result = reporting.buildReport(user);

        // then one RDOSummary for each price model
        assertNotNull(result);
        assertEquals("Result must contains 1 subscription summary on upgrade",
                1, result.getSummaries().size());
        assertNotNull("Subscription must contain 1 price model on upgrade",
                result.getSummaries().get(0).getPriceModels().get(0));
        assertNotNull("Subscription must contain 1 price model on upgrade",
                result.getSummaries().get(0).getPriceModels().get(1));
        assertNotNull("Purchase order number expect not null", result
                .getSubscriptions().get(0).getPurchaseOrderNumber());
    }

    @SuppressWarnings("unchecked")
    private void mockCalculateBillingResultsForPaymentPreview(List<File> files,
            long organzationKey) throws Exception {
        BillingRun billingRun = new BillingRun(1, 10);

        List<BillingResult> billingResults = new ArrayList<BillingResult>();
        if (files != null && !files.isEmpty()) {
            for (File brFile : files) {
                BillingResult billingResult = new BillingResult();
                billingResult.setCreationTime(123L);
                billingResult.setOrganizationTKey(organzationKey);
                billingResult.setPeriodStartTime(234L);
                billingResult.setPeriodEndTime(345L);
                billingResult.setResultXML(getTestFileAsString(brFile));
                PaymentResult pr = new PaymentResult();
                pr.setBillingResult(billingResult);
                billingResult.setPaymentResult(pr);
                billingResults.add(billingResult);
            }
        }
        billingRun.addBillingResult(billingResults
                .toArray(new BillingResult[billingResults.size()]));

        doReturn(billingRun).when(billingService).generatePaymentPreviewReport(
                anyLong());
        doReturn(billingRun).when(billingService).generatePaymentPreviewReport(
                anyLong(), any(List.class));
    }

    private static String getTestFileAsString(File file) {
        FileInputStream inputStream = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            inputStream = new FileInputStream(file);
            Document doc = builder.parse(inputStream);
            return XMLConverter.convertToString(doc, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Streams.close(inputStream);
        }
    }

    private void mockReportDaoData() {
        mockReportDaoData("name", "address", "productId");
    }

    private void mockReportDaoData(String name, String address, String productId) {
        ReportData reportData = new BillingDao(null).new ReportData();
        reportData.setAddress(address);
        reportData.setName(name);
        reportData.setProductId(productId);
        doReturn(reportData).when(billingDao).getReportData();
    }

    @Test
    public void build_localizedServiceName() throws Exception {
        // given
        PlatformUser user = givenUser(false, true,
                OrganizationRoleType.CUSTOMER);
        mockCalculateBillingResultsForPaymentPreview(
                Arrays.asList(XML_FILE_UPGRADE), user.getOrganization()
                        .getKey());
        mockReportDaoData("LocalizedServiceName", "address", "productId");

        // when
        RDOCustomerPaymentPreview result = reporting.buildReport(user);

        // then
        assertEquals("LocalizedServiceName", result.getSummaries().get(0)
                .getPriceModels().get(0).getServiceName());
    }

    @Test
    public void build_supplierNameAndAddress() throws Exception {
        // given
        PlatformUser user = givenUser(false, true,
                OrganizationRoleType.CUSTOMER);
        mockCalculateBillingResultsForPaymentPreview(
                Arrays.asList(XML_FILE_UPGRADE), user.getOrganization()
                        .getKey());
        mockReportDaoData("MyOrganization", "Main Street Munich", "productId");

        // when
        RDOCustomerPaymentPreview result = reporting.buildReport(user);

        // then
        assertEquals("MyOrganization", result.getSummaries().get(0)
                .getSupplierName());
        assertEquals("Main Street Munich", result.getSummaries().get(0)
                .getSupplierAddress());
    }

    @Test
    public void buildReport_CallParserForPaymentPreview() throws Exception {
        // given
        PlatformUser user = givenUser(false, true,
                OrganizationRoleType.CUSTOMER);
        mockReportDaoData("LocalizedServiceName", "address", "productId");

        reporting.brParser = mock(BillingResultParser.class);
        mockCalculateBillingResultsForPaymentPreview(user.getOrganization()
                .getKey());

        // when
        reporting.buildReport(user);

        // then
        verify(reporting.brParser, times(1))
                .evaluateBillingResultForPaymentPreview(any(RDOSummary.class),
                        any(Document.class), eq(user),
                        any(PriceConverter.class),
                        eq(Long.valueOf(INVOCATION_TIME)));
    }
    
    private void mockCalculateBillingResultsForPaymentPreview(
            long organizationKey) throws Exception {
        BillingResult billingResult = new BillingResult();
        billingResult.setOrganizationTKey(organizationKey);
        billingResult.setResultXML(null);

        BillingRun billingRun = new BillingRun(1, INVOCATION_TIME);
        billingRun.addBillingResult(billingResult);

        doReturn(billingRun).when(billingService).generatePaymentPreviewReport(
                anyLong());
    }

}
