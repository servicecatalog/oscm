/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 19.02.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentResult;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Report;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ReportType;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOReport;
import org.oscm.reportingservice.business.model.RDO;
import org.oscm.reportingservice.business.model.billing.RDOCustomerPaymentPreview;
import org.oscm.reportingservice.business.model.billing.RDODetailedBilling;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.business.model.externalservices.RDOExternal;
import org.oscm.reportingservice.business.model.supplierrevenue.RDOPlatformRevenue;
import org.oscm.reportingservice.stubs.*;
import org.oscm.test.stubs.DataServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.types.constants.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The tests for the payment processing status report of the reporting service.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ReportingServiceBeanTest {

    private static final String TEST_XML_FILE = "/BillingResult.xml";
    private static final String TEST_XML_FILE_2 = "/BillingResult7.xml";
    private static final long INVALID_USER_ID = 2L;
    private static final String INVALID_REPORT_ID = "DUMMY";
    private static final String INVALID_SESSION_ID = "dkjf892hlkjsf";
    private static final String REPORT_WSDLURL = "http://localhost:8081/Report/ReportingServiceBean?wsdl";
    private static final String REPORT_SOAP_ENDPOINT = "http://localhost:8081/Report/ReportingServiceBean";
    private static final String REPORT_ENGINEURL = "http://localhost:8080/birt/frameset?__report=${reportname}.rptdesign&SessionId=${sessionid}&__locale=${locale}&WSDLURL=${wsdlurl}&SOAPEndPoint=${soapendpoint}";
    private static final String LOCALIZED_REPORT_DESCRIPTION = "LocalizedReportDescription";

    private static final String VALID_SESSION_ID = "valid_session";
    private static final String VALID_SESSION_ID2 = "valid_session2";
    private static final String VALID_SESSION_ID3 = "valid_session3";
    private static final long VALID_BILLING_KEY = 83476L;
    private static final long NON_EXISTING_BILLING_KEY = 0L;
    
    private static final String EMPTY = "";

    private ReportingServiceBean reporting;
    private ReportingServiceBeanLocal reportingLocal;
    private SessionServiceStub prodMgmt;
    private Session session;
    private DataServiceStub dm;
    private QueryStub namedQuery;
    private QueryStub nativeQuery;
    private DataSourceStub dataSource;
    private Connection conn;

    private static final String REPORT_ID = "Supplier_PaymentResultStatus";
    private PreparedStatement stmt;
    private ResultSetStub rs;
    private ResultSetMetaDataStub rsmd;
    private LocalizerServiceStub localizerStub;
    private final Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
    private Organization organization;
    private Map<OrganizationRoleType, List<Report>> roleToReports;
    private BillingServiceStub billing;
    private final List<File> filesToUse = new ArrayList<File>();
    private final List<BillingResult> usedBillingResults = new ArrayList<BillingResult>();
    private final DataSet dataSet = new DataSet();
    private String currentOrgId;
    private static String userLocale = "en";
    private static Locale defaultLocale = Locale.ENGLISH;
    private ConfigurationServiceLocal cnfgServLocal;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {

        File file = new File(this.getClass().getResource(TEST_XML_FILE).getFile());
        filesToUse.add(file);

        roleToReports = new HashMap<OrganizationRoleType, List<Report>>();
        roleToReports.put(OrganizationRoleType.CUSTOMER, getReportList(
                ReportName.EVENT.value(), ReportName.SUBSCRIPTION.value()));

        roleToReports.put(OrganizationRoleType.SUPPLIER,
                getReportList(ReportName.SUPPLIER_PRODUCT.value(),
                        ReportName.SUPPLIER_CUSTOMER.value(),
                        ReportName.SUPPLIER_BILLING.value(),
                        ReportName.SUPPLIER_PAYMENT_RESULT_STATUS.value()));

        roleToReports.put(OrganizationRoleType.TECHNOLOGY_PROVIDER,
                getReportList(ReportName.PROVIDER_EVENT.value(),
                        ReportName.PROVIDER_SUPPLIER.value(),
                        ReportName.PROVIDER_SUBSCRIPTION.value(),
                        ReportName.PROVIDER_INSTANCE.value()));

        roleToReports.put(OrganizationRoleType.PLATFORM_OPERATOR,
                getReportList(ReportName.SUPPLIER_BILLING_OF_SUPPLIER.value(),
                        ReportName.SUPPLIER_BILLING_DETAILS_OF_SUPPLIER.value(),
                        ReportName.SUPPLIER_CUSTOMER_OF_SUPPLIER.value(),
                        ReportName.SUPPLIER_PRODUCT_OF_SUPPLIER.value()));

        localizerStub = new LocalizerServiceStub() {

            @Override
            public java.util.Locale getDefaultLocale() {
                return defaultLocale;
            };

            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                if (objectType == LocalizedObjectTypes.REPORT_DESC) {
                    return LOCALIZED_REPORT_DESCRIPTION;
                }
                return super.getLocalizedTextFromDatabase(localeString,
                        objectKey, objectType);
            }
        };

        cnfgServLocal = mock(ConfigurationServiceLocal.class);
        doReturn(new ConfigurationSetting(ConfigurationKey.REPORT_ENGINEURL,
                Configuration.GLOBAL_CONTEXT, REPORT_ENGINEURL))
                        .when(cnfgServLocal).getConfigurationSetting(
                                ConfigurationKey.REPORT_ENGINEURL,
                                Configuration.GLOBAL_CONTEXT);
        doReturn(new ConfigurationSetting(ConfigurationKey.REPORT_SOAP_ENDPOINT,
                Configuration.GLOBAL_CONTEXT, REPORT_SOAP_ENDPOINT))
                        .when(cnfgServLocal).getConfigurationSetting(
                                ConfigurationKey.REPORT_SOAP_ENDPOINT,
                                Configuration.GLOBAL_CONTEXT);
        doReturn(new ConfigurationSetting(ConfigurationKey.REPORT_WSDLURL,
                Configuration.GLOBAL_CONTEXT, REPORT_WSDLURL))
                        .when(cnfgServLocal).getConfigurationSetting(
                                ConfigurationKey.REPORT_WSDLURL,
                                Configuration.GLOBAL_CONTEXT);

        reporting = spy(new ReportingServiceBean());
        reportingLocal = spy(new ReportingServiceBeanLocal());
        reporting.delegate = reportingLocal;

        prodMgmt = new SessionServiceStub() {
            @Override
            public Session getPlatformSessionForSessionId(String sessionId) {
                if (VALID_SESSION_ID.equals(sessionId)
                        || VALID_SESSION_ID2.equals(sessionId)
                        || VALID_SESSION_ID3.equals(sessionId)) {
                    return session;
                }
                throw new NoResultException("Invalid session id");
            }
        };

        dm = new DataServiceStub() {
            @Override
            public DomainObject<?> getReferenceByBusinessKey(
                    DomainObject<?> findTemplate)
                            throws ObjectNotFoundException {
                if (findTemplate instanceof Report) {
                    Report report = (Report) findTemplate;
                    if (INVALID_REPORT_ID.equals(report.getReportName())) {
                        throw new ObjectNotFoundException(ClassEnum.REPORT,
                                report.getReportName());
                    }
                    return report;
                }
                if (findTemplate instanceof Organization) {
                    return organization;
                }
                return super.getReferenceByBusinessKey(findTemplate);
            }

            @Override
            public Query createNamedQuery(String arg0) {
                return namedQuery;
            }

            @Override
            public Query createNativeQuery(String arg0) {
                return nativeQuery;
            }

            @Override
            public <T extends DomainObject<?>> T find(Class<T> objclazz,
                    long key) {
                if (objclazz == PlatformUser.class && key != INVALID_USER_ID) {
                    return objclazz.cast(dm.getCurrentUser());
                }
                return null;
            }

            @Override
            public PlatformUser getCurrentUser() {
                Organization supplier = new Organization();
                Set<OrganizationToRole> list = new HashSet<OrganizationToRole>();
                list.add(addOrgToRole(supplier, OrganizationRoleType.SUPPLIER));
                supplier.setGrantedRoles(list);
                PlatformUser platformUser = new PlatformUser();
                organization = new Organization();
                organization.setGrantedRoles(roles);
                organization
                        .setDomicileCountry(new SupportedCountry(userLocale));
                OrganizationReference ref = new OrganizationReference(supplier,
                        organization,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                organization.getSources().add(ref);
                organization.setOrganizationId("orgId");
                supplier.getTargets().add(ref);
                platformUser.setOrganization(organization);
                platformUser.setLocale(userLocale);
                return platformUser;
            }

            @Override
            public DataSet executeQueryForRawData(SqlQuery sqlQuery) {
                return dataSet;
            }
        };
        currentOrgId = dm.getCurrentUser().getOrganization()
                .getOrganizationId();

        billing = new BillingServiceStub() {

            @Override
            public List<BillingResult> generateBillingForAnyPeriod(
                    long startOfPeriod, long endOfPeriod,
                    long organizationKey) {

                usedBillingResults.clear();

                for (File brFile : filesToUse) {
                    BillingResult br = new BillingResult();
                    br.setCreationTime(123L);
                    br.setOrganizationTKey(organizationKey);
                    br.setPeriodStartTime(234L);
                    br.setPeriodEndTime(345L);
                    br.setResultXML(getTestFileAsString(brFile));
                    PaymentResult pr = new PaymentResult();
                    pr.setBillingResult(br);
                    br.setPaymentResult(pr);
                    usedBillingResults.add(br);
                }

                return usedBillingResults;
            }

        };
        namedQuery = new QueryStub();
        nativeQuery = new QueryStub();
        dataSource = new DataSourceStub();
        conn = mock(Connection.class);
        stmt = mock(PreparedStatement.class);
        rs = new ResultSetStub(null);
        rsmd = new ResultSetMetaDataStub();

        session = new Session();
        session.setPlatformUserKey(1L);
        dataSource.setConnection(conn);

        doAnswer(new Answer<ResultSet>() {
            @Override
            public ResultSet answer(InvocationOnMock invocation)
                    throws Throwable {
                rs.setPosition(-1);
                return rs;
            }
        }).when(stmt).executeQuery();
        doAnswer(new Answer<PreparedStatement>() {
            @Override
            public PreparedStatement answer(InvocationOnMock invocation)
                    throws Throwable {
                rs.setSQLColumns((String) invocation.getArguments()[0]);
                return stmt;
            }
        }).when(conn).prepareStatement(anyString());
        rs.setResultSetMetaData(rsmd);

        reporting.dataService = dm;
        reporting.localizerService = localizerStub;
        reporting.configurationService = cnfgServLocal;
        reportingLocal.sessionService = prodMgmt;
        reportingLocal.billingService = billing;
        reportingLocal.dataService = dm;
        reportingLocal.localizerService = localizerStub;
        reportingLocal.configurationService = cnfgServLocal;

        doReturn(null).when(reportingLocal).getFromCache(anyString(),
                any(Class.class));
        doNothing().when(reportingLocal).putToCache(anyString(),
                any(RDO.class));
    }

    @After
    public void resetCommonData() {
        userLocale = "en";
        defaultLocale = Locale.ENGLISH;
    }

    @Test(expected = NoResultException.class)
    public void testGetReportNoValidSession() {
        reporting.getReport(INVALID_SESSION_ID, "someId");
    }

    @Test
    public void testGetReportNonExistingUser() {
        session.setPlatformUserKey(INVALID_USER_ID);
        VOReportResult report = reporting.getReport(VALID_SESSION_ID,
                "someReportId");
        Assert.assertEquals("No object must be returned", 0,
                report.getData().size());
    }

    @Test
    public void getReportOfASupplier_nonExistingUser() {
        // given
        session.setPlatformUserKey(INVALID_USER_ID);

        // when
        VOReportResult report = reporting.getReportOfASupplier(VALID_SESSION_ID,
                "someReportId", "someReportId");

        // then
        assertEquals("No object must be returned", 0, report.getData().size());
    }

    @Test
    public void getReportOfASupplier_nonExistingSupplierOrgId() {
        // when
        VOReportResult report = reporting.getReportOfASupplier(VALID_SESSION_ID,
                null, "someReportId");

        // then
        assertEquals("No object must be returned", 0, report.getData().size());
    }

    @Test
    public void getReportOfASupplier_nonExistingUserNonExistingSupplierId() {
        // given
        session.setPlatformUserKey(INVALID_USER_ID);

        // when
        VOReportResult report = reporting.getReportOfASupplier(VALID_SESSION_ID,
                "someReportId", "someReportId");

        // then
        assertEquals("No object must be returned", 0, report.getData().size());
    }

    @Test
    public void getReportOfASupplier_nonExistingReportId() {
        // given
        givenOperatorReportData(OrganizationRoleType.SUPPLIER);

        // when
        VOReportResult report = reporting.getReportOfASupplier(VALID_SESSION_ID,
                currentOrgId, "someReportId");

        // then
        assertEquals("No object must be returned", 0, report.getData().size());
    }

    @Test
    public void getReportOfASupplier_product_resellerRole() {
        // given
        givenOperatorReportData(OrganizationRoleType.RESELLER);

        // when
        VOReportResult report = reporting.getReportOfASupplier(VALID_SESSION_ID,
                currentOrgId, ReportName.SUPPLIER_PRODUCT_OF_SUPPLIER.value());

        // then
        assertEquals("No object must be returned", 0, report.getData().size());
    }

    @Test
    public void getReportOfASupplier_product_brokerRole() {
        // given
        givenOperatorReportData(OrganizationRoleType.BROKER);

        // when
        VOReportResult report = reporting.getReportOfASupplier(VALID_SESSION_ID,
                currentOrgId, ReportName.SUPPLIER_PRODUCT_OF_SUPPLIER.value());

        // then
        assertEquals("No object must be returned", 0, report.getData().size());
    }

    @Test
    public void testGetReportNoResult() {
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        namedQuery.setReports(roleToReports.get(OrganizationRoleType.SUPPLIER));

        VOReportResult voReport = reporting.getReport(VALID_SESSION_ID,
                REPORT_ID);
        Assert.assertEquals("No object must be returned", 0,
                voReport.getData().size());
    }

    @Test
    public void getReport_StringColumn() throws Exception {
        // given
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        namedQuery.setReports(roleToReports.get(OrganizationRoleType.SUPPLIER));
        givenDataSetRow(Arrays.asList((Object) "Result"));
        givenDataSetMetaData(Arrays.asList("column_name"));

        // when
        VOReportResult voReport = reporting.getReport(VALID_SESSION_ID,
                REPORT_ID);

        // then
        assertEquals("One object must be returned", 1,
                voReport.getData().size());
        assertEquals("Wrong content in retrieved report data", "Result",
                XMLConverter.getNodeTextContentByXPath(
                        getFirstElement(voReport).getOwnerDocument(),
                        "/row/COLUMN_NAME/text()"));
    }

    @Test
    public void getReport_asSupplier_internalRenamedProduct() throws Exception {
        // given
        givenDataSetRow(Arrays.asList((Object) "test#12345"));
        givenDataSetMetaData(Arrays.asList("PRODUCTID"));
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        namedQuery.setReports(roleToReports.get(OrganizationRoleType.SUPPLIER));

        // when
        VOReportResult reportResult = reporting.getReport(VALID_SESSION_ID,
                roleToReports.get(OrganizationRoleType.SUPPLIER).get(0)
                        .getReportName());

        // then
        assertEquals(1, reportResult.getData().size());
        assertEquals("test",
                XMLConverter.getNodeTextContentByXPath(
                        getFirstElement(reportResult).getOwnerDocument(),
                        "/row/PRODUCTID/text()"));
    }

    @Test
    public void getReport_asSupplier() throws Exception {
        // given
        givenDataSetRow(Arrays.asList((Object) "test"));
        givenDataSetMetaData(Arrays.asList("PRODUCTID"));
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        namedQuery.setReports(roleToReports.get(OrganizationRoleType.SUPPLIER));

        // when
        VOReportResult reportResult = reporting.getReport(VALID_SESSION_ID,
                roleToReports.get(OrganizationRoleType.SUPPLIER).get(1)
                        .getReportName());

        // then
        assertEquals(1, reportResult.getData().size());
        assertEquals("test",
                XMLConverter.getNodeTextContentByXPath(
                        getFirstElement(reportResult).getOwnerDocument(),
                        "/row/PRODUCTID/text()"));
    }

    @Test
    public void getReport_asSupplier_nullValueInStringResult()
            throws Exception {
        // given
        dataSet.addRow(Arrays.asList((Object) null));
        dataSet.getMetaData().add(1, "DESCRIPTION", "VARCHAR", Types.VARCHAR);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        namedQuery.setReports(roleToReports.get(OrganizationRoleType.SUPPLIER));

        // when
        VOReportResult reportResult = reporting.getReport(VALID_SESSION_ID,
                roleToReports.get(OrganizationRoleType.SUPPLIER).get(2)
                        .getReportName());

        // then
        assertEquals(1, reportResult.getData().size());
        Element dataElement = getFirstElement(reportResult);
        String result = XMLConverter.getNodeTextContentByXPath(
                dataElement.getOwnerDocument(), "/row/DESCRIPTION/text()");
        assertEquals("", result);
    }

    @Test
    public void getReport_asSupplier_nullValueInNonStringResult()
            throws Exception {
        // given
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        namedQuery.setReports(roleToReports.get(OrganizationRoleType.SUPPLIER));
        dataSet.addRow(Arrays.asList((Object) null));
        dataSet.getMetaData().add(1, "CREATIONDATE", "DATE", Types.DATE);

        // when
        VOReportResult reportResult = reporting.getReport(VALID_SESSION_ID,
                roleToReports.get(OrganizationRoleType.SUPPLIER).get(3)
                        .getReportName());

        // then
        Assert.assertEquals(1, reportResult.getData().size());
        Element dataElement = getFirstElement(reportResult);
        String result = XMLConverter.getNodeTextContentByXPath(
                dataElement.getOwnerDocument(), "/row/CREATIONDATE/text()");
        Assert.assertEquals("null", result);
    }

    @Test
    public void testGetExternalReport() throws Exception {
        RDOExternal report = reporting
                .getExternalServicesReport(VALID_SESSION_ID);
        assertNotNull(report);
    }

    @Test
    public void testGetReportAsTechnologyProvider() throws Exception {
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.TECHNOLOGY_PROVIDER));
        namedQuery.setReports(
                roleToReports.get(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        VOReportResult reportResult = reporting.getReport(VALID_SESSION_ID,
                roleToReports.get(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                        .get(0).getReportName());
        Assert.assertNotNull(reportResult);
        Assert.assertTrue(reportResult.getData().isEmpty());
    }

    @Test
    public void getReport_asTechnologyProvider_internalRenamedProduct()
            throws Exception {
        // given
        givenDataSetRow(Arrays.asList((Object) "test#12345"));
        givenDataSetMetaData(Arrays.asList("PRODUCTID"));
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.TECHNOLOGY_PROVIDER));
        namedQuery.setReports(
                roleToReports.get(OrganizationRoleType.TECHNOLOGY_PROVIDER));

        // when
        VOReportResult reportResult = reporting.getReport(VALID_SESSION_ID,
                roleToReports.get(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                        .get(1).getReportName());

        // then
        assertEquals(1, reportResult.getData().size());
        assertEquals("test",
                XMLConverter.getNodeTextContentByXPath(
                        getFirstElement(reportResult).getOwnerDocument(),
                        "/row/PRODUCTID/text()"));
    }

    @Test
    public void getReport_asTechnologyProvider_withNullClob() throws Exception {
        // given
        Object o = null;
        givenDataSetRow(Arrays.asList(o));
        givenDataSetMetaData(Arrays.asList("XMLDATA"));
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.TECHNOLOGY_PROVIDER));
        namedQuery.setReports(
                roleToReports.get(OrganizationRoleType.TECHNOLOGY_PROVIDER));

        // when
        VOReportResult reportResult = reporting.getReport(VALID_SESSION_ID,
                roleToReports.get(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                        .get(3).getReportName());

        // then
        assertEquals(1, reportResult.getData().size());
        assertEquals("",
                XMLConverter.getNodeTextContentByXPath(
                        getFirstElement(reportResult).getOwnerDocument(),
                        "/row/XMLDATA/text()"));
    }

    @Test
    public void testGetReportAsTechnologyProviderAndSupplier()
            throws Exception {
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.TECHNOLOGY_PROVIDER));
        List<Report> newReports = roleToReports
                .get(OrganizationRoleType.SUPPLIER);
        newReports.addAll(
                roleToReports.get(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        namedQuery.setReports(newReports);
        VOReportResult reportResult = reporting.getReport(VALID_SESSION_ID,
                roleToReports.get(OrganizationRoleType.SUPPLIER).get(0)
                        .getReportName());
        Assert.assertNotNull(reportResult);
        Assert.assertTrue(reportResult.getData().isEmpty());
        reportResult = reporting.getReport(VALID_SESSION_ID,
                roleToReports.get(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                        .get(0).getReportName());
        Assert.assertNotNull(reportResult);
        Assert.assertTrue(reportResult.getData().isEmpty());
    }

    @Test
    public void getReportOfASupplier_billing() throws Exception {
        // given
        File file = new File(this.getClass().getResource(TEST_XML_FILE_2).getFile());
        givenDataSetRow(
                Arrays.asList((Object) getTestFileAsString(file)));
        givenDataSetMetaData(Arrays.asList("resultxml"));
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        namedQuery.setReports(
                roleToReports.get(OrganizationRoleType.PLATFORM_OPERATOR));

        // when
        VOReportResult reportResult = reporting.getReportOfASupplier(
                VALID_SESSION_ID, currentOrgId,
                ReportName.SUPPLIER_BILLING_OF_SUPPLIER.value());

        // then
        assertEquals(1, reportResult.getData().size());
        assertEquals("XXXX",
                XMLConverter.getNodeTextContentByXPath(
                        getFirstElement(reportResult)
                                .getOwnerDocument(),
                "/row/RESULTXML/BillingDetails/OrganizationDetails/Email/text()"));
        assertEquals("XXXX",
                XMLConverter.getNodeTextContentByXPath(
                        getFirstElement(reportResult)
                                .getOwnerDocument(),
                "/row/RESULTXML/BillingDetails/OrganizationDetails/Name/text()"));
        assertEquals("XXXX",
                XMLConverter.getNodeTextContentByXPath(
                        getFirstElement(reportResult)
                                .getOwnerDocument(),
                "/row/RESULTXML/BillingDetails/OrganizationDetails/Address/text()"));

    }

    private void givenOperatorReportData(OrganizationRoleType orgRole) {
        givenDataSetRow(Arrays.asList((Object) "test"));
        givenDataSetMetaData(Arrays.asList("PRODUCTID"));
        roles.add(addOrgToRole(organization, orgRole));
        namedQuery.setReports(
                roleToReports.get(OrganizationRoleType.PLATFORM_OPERATOR));
    }

    @Test
    public void getReportOfASupplier_customer() throws Exception {
        // given
        givenOperatorReportData(OrganizationRoleType.SUPPLIER);

        // when
        VOReportResult reportResult = reporting.getReportOfASupplier(
                VALID_SESSION_ID, currentOrgId,
                ReportName.SUPPLIER_CUSTOMER_OF_SUPPLIER.value());

        // then
        assertEquals(1, reportResult.getData().size());
        assertEquals("test",
                XMLConverter.getNodeTextContentByXPath(
                        getFirstElement(reportResult).getOwnerDocument(),
                        "/row/PRODUCTID/text()"));
    }

    @Test
    public void getReportOfASupplier_product() throws Exception {
        // given
        givenOperatorReportData(OrganizationRoleType.SUPPLIER);

        // when
        VOReportResult reportResult = reporting.getReportOfASupplier(
                VALID_SESSION_ID, currentOrgId,
                ReportName.SUPPLIER_PRODUCT_OF_SUPPLIER.value());

        // then
        assertEquals(1, reportResult.getData().size());
        assertEquals("test",
                XMLConverter.getNodeTextContentByXPath(
                        getFirstElement(reportResult).getOwnerDocument(),
                        "/row/PRODUCTID/text()"));
    }

    @Test
    public void testGetReportReportIdNull() throws Throwable {
        // when
        VOReportResult reportResult = reporting.getReport(VALID_SESSION_ID,
                null);

        // then
        assertTrue(reportResult.getData().isEmpty());
    }

    @Test
    public void testGetReportReportIdNotForRole() throws Throwable {
        // given
        roles.add(addOrgToRole(organization, OrganizationRoleType.CUSTOMER));
        namedQuery.setReports(roleToReports.get(OrganizationRoleType.CUSTOMER));

        // when
        VOReportResult reportResult = reporting.getReport(VALID_SESSION_ID,
                roleToReports.get(OrganizationRoleType.SUPPLIER).get(0)
                        .getReportName());

        // then
        assertTrue(reportResult.getData().isEmpty());
    }

    @Test(expected = NoResultException.class)
    public void testGetReportSessionIdNull() throws Throwable {
        reporting.getReport(null, null);
    }

    @Test(expected = NoResultException.class)
    public void testGetReportOfASupplierSessionIdNull() throws Throwable {
        reporting.getReportOfASupplier(null, null, null);
    }

    @Test
    public void testGetAvailableReportsAsCustomer() throws Exception {
        roles.add(addOrgToRole(organization, OrganizationRoleType.CUSTOMER));
        namedQuery.setReports(roleToReports.get(OrganizationRoleType.CUSTOMER));
        List<VOReport> reportList = reporting
                .getAvailableReports(ReportType.ALL);
        Assert.assertEquals(2, reportList.size());
        for (VOReport voReport : reportList) {
            Assert.assertNotNull(voReport.getReportName());
            Assert.assertNotNull(voReport.getReportUrlTemplate());
            Assert.assertEquals(LOCALIZED_REPORT_DESCRIPTION,
                    voReport.getLocalizedReportName());
        }
    }

    @Test
    public void testGetAvailableReportsAsSupplier() throws Exception {
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        namedQuery.setReports(roleToReports.get(OrganizationRoleType.SUPPLIER));
        List<VOReport> reportList = reporting
                .getAvailableReports(ReportType.ALL);
        Assert.assertEquals(4, reportList.size());
        for (VOReport voReport : reportList) {
            Assert.assertNotNull(voReport.getReportName());
            Assert.assertNotNull(voReport.getReportUrlTemplate());
            Assert.assertEquals(LOCALIZED_REPORT_DESCRIPTION,
                    voReport.getLocalizedReportName());
        }
    }

    @Test
    public void testGetAvailableReportsAsTechnologyProvider() throws Exception {
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.TECHNOLOGY_PROVIDER));
        namedQuery.setReports(
                roleToReports.get(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        List<VOReport> reportList = reporting
                .getAvailableReports(ReportType.ALL);
        Assert.assertEquals(4, reportList.size());
        for (VOReport voReport : reportList) {
            Assert.assertNotNull(voReport.getReportName());
            Assert.assertNotNull(voReport.getReportUrlTemplate());
            Assert.assertEquals(LOCALIZED_REPORT_DESCRIPTION,
                    voReport.getLocalizedReportName());
        }
    }

    @Test
    public void testGetAvailableReportsAsTechnologyProviderAndSupplier()
            throws Exception {
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.TECHNOLOGY_PROVIDER));
        namedQuery.setReports(roleToReports.get(OrganizationRoleType.SUPPLIER));
        List<VOReport> reportList = reporting
                .getAvailableReports(ReportType.ALL);
        Assert.assertEquals(8, reportList.size());
        for (VOReport voReport : reportList) {
            Assert.assertNotNull(voReport.getReportName());
            Assert.assertNotNull(voReport.getReportUrlTemplate());
            Assert.assertEquals(LOCALIZED_REPORT_DESCRIPTION,
                    voReport.getLocalizedReportName());
        }
    }

    @Test
    public void testGetAvailableReportsAsOperator() throws Exception {
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.PLATFORM_OPERATOR));
        namedQuery.setReports(
                roleToReports.get(OrganizationRoleType.PLATFORM_OPERATOR));
        List<VOReport> reportList = reporting
                .getAvailableReports(ReportType.ALL);
        Assert.assertEquals(4, reportList.size());
        for (VOReport voReport : reportList) {
            Assert.assertNotNull(voReport.getReportName());
            Assert.assertNotNull(voReport.getReportUrlTemplate());
            Assert.assertEquals(LOCALIZED_REPORT_DESCRIPTION,
                    voReport.getLocalizedReportName());
        }
    }

    @Test(expected = NoResultException.class)
    public void testGetBillingDetailsReportNoResult() throws Exception {
        reporting.getBillingDetailsReport(null, -1);
    }

    @Test(expected = NoResultException.class)
    public void testGetBillingDetailsOfASupplierReportNoResult()
            throws Exception {
        reporting.getBillingDetailsOfASupplierReport(null, -1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCustomerPaymentPreviewReportInvalidUser() throws Exception {
        session.setPlatformUserKey(INVALID_USER_ID);
        roles.add(addOrgToRole(organization, OrganizationRoleType.CUSTOMER));
        doReturn(null).when(reportingLocal).getFromCache(anyString(),
                any(Class.class));
        doNothing().when(reportingLocal).putToCache(anyString(),
                any(RDOCustomerPaymentPreview.class));

        RDOCustomerPaymentPreview result = reporting
                .getCustomerPaymentPreview(VALID_SESSION_ID);

        assertEquals("", result.getStartDate());
        assertEquals("", result.getEndDate());
        assertTrue(result.getSummaries().isEmpty());
    }

    protected OrganizationToRole addOrgToRole(Organization organization,
            OrganizationRoleType roleType) {
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganization(organization);
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        orgToRole.setOrganizationRole(role);
        return orgToRole;
    }

    private List<Report> getReportList(String... reportNames) {
        List<Report> result = new ArrayList<Report>();
        for (String string : reportNames) {
            Report report = new Report();
            report.setReportName(string);
            result.add(report);
        }
        return result;
    }

    private void givenDataSetRow(List<Object> row) {
        dataSet.addRow(row);
    }

    private void givenDataSetMetaData(List<String> columnNames) {
        for (int i = 1; i <= columnNames.size(); i++) {
            dataSet.getMetaData().add(i, columnNames.get(i - 1),
                    "columnTypeName", Types.VARCHAR);
        }
    }

    private static Element getFirstElement(VOReportResult voReport) {
        Element dataElement = (Element) voReport.getData().get(0);
        dataElement.getOwnerDocument().appendChild(
                dataElement.getOwnerDocument().adoptNode(dataElement));
        return dataElement;
    }

    private static String getTestFileAsString(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new FileInputStream(file));
            return XMLConverter.convertToString(doc, false)
                    .replace("<!-- Copyright FUJITSU LIMITED 2017-->", "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetSupplierRevenueAsSupplier() throws Exception {
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        RDOPlatformRevenue result = reporting
                .getPlatformRevenueReport(VALID_SESSION_ID, null, null);
        Assert.assertNotNull("Result must not be null", result);
        Assert.assertEquals("Result must not contain entries", 0,
                result.getSupplierDetails().size());
    }

    @Test
    public void testGetSupplierRevenueAsCustomer() throws Exception {
        roles.add(addOrgToRole(organization, OrganizationRoleType.CUSTOMER));
        RDOPlatformRevenue result = reporting
                .getPlatformRevenueReport(VALID_SESSION_ID, null, null);
        Assert.assertNotNull("Result must not be null", result);
        Assert.assertEquals("Result must not contain entries", 0,
                result.getSupplierDetails().size());
    }

    @Test
    public void testGetSupplierRevenueAsTechnologyProvider() throws Exception {
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.TECHNOLOGY_PROVIDER));
        RDOPlatformRevenue result = reporting
                .getPlatformRevenueReport(VALID_SESSION_ID, null, null);
        Assert.assertNotNull("Result must not be null", result);
        Assert.assertEquals("Result must not contain entries", 0,
                result.getSupplierDetails().size());
    }

    @Test
    public void getPlatformRevenueReport_asPlatformOperator_nullMp()
            throws Exception {
        // given
        File file = new File(this.getClass().getResource(TEST_XML_FILE).getFile());
        givenDataSetRow(Arrays.asList((Object) "supplierName", "supplierID",
                "country", null, "mp", getTestFileAsString(file)));
        givenDataSetMetaData(Arrays.asList("name", "organizationid",
                "countryisocode", "marketplaceid", "value", "resultxml"));
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.PLATFORM_OPERATOR));

        // when
        RDOPlatformRevenue result = reporting.getPlatformRevenueReport(
                VALID_SESSION_ID, new Date(0), new Date());

        // then
        assertEquals(1, result.getSupplierDetails().size());
        assertEquals("[NOT_LISTED_IN_MARKETPLACE]",
                result.getSummaryByMarketplace().get(0).getMarketplace());
    }

    @Test
    public void getPlatformRevenueReport_asPlatformOperator_noLocalization1()
            throws Exception {
        // given one billing result and proper role
        File file = new File(this.getClass().getResource(TEST_XML_FILE).getFile());
        givenDataSetRow(Arrays.asList((Object) "supplierName", "supplierID",
                "country", "mpid", "", getTestFileAsString(file)));
        givenDataSetMetaData(Arrays.asList("name", "organizationid",
                "countryisocode", "marketplaceid", "value", "resultxml"));
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.PLATFORM_OPERATOR));

        // execute
        RDOPlatformRevenue result = reporting.getPlatformRevenueReport(
                VALID_SESSION_ID, new Date(0), new Date());

        // assert
        assertEquals(1, result.getSupplierDetails().size());
        assertEquals("mpid",
                result.getSummaryByMarketplace().get(0).getMarketplace());
    }

    @Test
    public void getPlatformRevenueReport_asPlatformOperator_NoLocalization2()
            throws Exception {
        // given one billing result and proper role
        File file = new File(this.getClass().getResource(TEST_XML_FILE).getFile());
        givenDataSetRow(Arrays.asList((Object) "supplierName", "supplierID",
                "country", "mpid", null, getTestFileAsString(file)));
        givenDataSetMetaData(Arrays.asList("name", "organizationid",
                "countryisocode", "marketplaceid", "value", "resultxml"));
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.PLATFORM_OPERATOR));

        // execute
        RDOPlatformRevenue result = reporting.getPlatformRevenueReport(
                VALID_SESSION_ID, new Date(0), new Date());

        // assert
        assertEquals(1, result.getSupplierDetails().size());
        assertEquals("mpid",
                result.getSummaryByMarketplace().get(0).getMarketplace());
    }

    @Test
    public void getPlatformRevenueReport_asPlatformOperator() throws Exception {
        // given one billing result and proper role
        File file = new File(this.getClass().getResource(TEST_XML_FILE).getFile());
        givenDataSetRow(Arrays.asList((Object) "supplierName", "supplierID",
                "country", "mpid", "mp", getTestFileAsString(file)));
        givenDataSetMetaData(Arrays.asList("name", "organizationid",
                "countryisocode", "marketplaceid", "value", "resultxml"));
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.PLATFORM_OPERATOR));

        // execute
        RDOPlatformRevenue result = reporting.getPlatformRevenueReport(
                VALID_SESSION_ID, new Date(0), new Date());

        // assert
        assertEquals(1, result.getSupplierDetails().size());
        assertEquals("mp (mpid)",
                result.getSummaryByMarketplace().get(0).getMarketplace());
    }

    /**
     * Empty report expected if start date is after end date.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSupplierRevenue_invalidDateRange() throws Exception {
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.PLATFORM_OPERATOR));
        RDOPlatformRevenue result = reporting.getPlatformRevenueReport(
                VALID_SESSION_ID, new Date(), new Date(0));
        assertEquals(0, result.getSupplierDetails().size());
    }

    @Test(expected = NoResultException.class)
    public void getBillingDetailsOfASupplierReport_invalidSessionId() {
        // when
        reporting.getBillingDetailsOfASupplierReport(INVALID_SESSION_ID,
                VALID_BILLING_KEY);
    }

    @Test
    public void getBillingDetailsOfASupplierReport_nonExistingBillingKey() {
        // when
        RDODetailedBilling report = reporting
                .getBillingDetailsOfASupplierReport(VALID_SESSION_ID,
                        NON_EXISTING_BILLING_KEY);

        // then
        assertTrue(report.getSummaries().isEmpty());
    }

    @SuppressWarnings("boxing")
    @Test
    public void getBillingDetailsOfASupplierReport() {
        // given
        
        doReturn(true).when(cnfgServLocal).isPaymentInfoAvailable();

        File file = new File(this.getClass().getResource(TEST_XML_FILE).getFile());
        givenDataSetRow(Arrays.asList((Object) 1000L,
                getTestFileAsString(file), "supplierName",
                "supplerAddress"));

        givenDataSetMetaData(
                Arrays.asList("creationtime", "resultxml", "name", "address"));
        // when
        RDODetailedBilling report = reporting
                .getBillingDetailsOfASupplierReport(VALID_SESSION_ID,
                        VALID_BILLING_KEY);

        // then
        assertEquals(1, report.getSummaries().size());
        assertEquals("XXXX",
                report.getSummaries().get(0).getOrganizationName());
        assertEquals("XXXX",
                report.getSummaries().get(0).getOrganizationAddress());

    }

    @Test
    public void testCustomerPaymentPreviewReportWithHiddenPaymentInfo()
            throws Exception {
        
        //given
        doReturn(false).when(cnfgServLocal).isPaymentInfoAvailable();
        
        // when
        reporting.getCustomerPaymentPreview(VALID_SESSION_ID);

        // then
        verify(reportingLocal, times(1)).hidePaymentInfo(any(RDOCustomerPaymentPreview.class));

    }

    @Test
    public void testBillingDetailsReportWithHiddenPaymentInfo()
            throws Exception {
        
        //given
        doReturn(false).when(cnfgServLocal).isPaymentInfoAvailable();
        
        // when
        reporting.getBillingDetailsReport(VALID_SESSION_ID, VALID_BILLING_KEY);

        // then
        verify(reportingLocal, times(1)).hidePaymentInfo(any(RDODetailedBilling.class));
    }
    
    @Test
    public void testBillingDetailsOfASupplierReportWithHiddenPaymentInfo()
            throws Exception {
        
        //given
        doReturn(false).when(cnfgServLocal).isPaymentInfoAvailable();

        File file = new File(this.getClass().getResource(TEST_XML_FILE).getFile());
        givenDataSetRow(Arrays.asList((Object) 1000L,
                getTestFileAsString(file), "supplierName",
                "supplerAddress"));

        givenDataSetMetaData(
                Arrays.asList("creationtime", "resultxml", "name", "address"));
        
        // when
        RDODetailedBilling report = reporting.getBillingDetailsOfASupplierReport(VALID_SESSION_ID,VALID_BILLING_KEY);

        // then
        verify(reportingLocal, times(1)).hidePaymentInfo(any(RDODetailedBilling.class));
        
        RDOSummary summary = report.getSummaries().get(0);
        assertThat(summary.getPaymentType(), is(EMPTY));
        assertThat(summary.getOrganizationAddress(), is(EMPTY));
    }
}
