/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                                
 *                                                                              
 *  Creation Date: 12.10.2011                                                      
 *                                                                              
 *  Completion Time: 12.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentResult;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Report;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.reportingservice.business.model.RDO;
import org.oscm.reportingservice.stubs.ConnectionStub;
import org.oscm.reportingservice.stubs.DataSourceStub;
import org.oscm.reportingservice.stubs.QueryStub;
import org.oscm.reportingservice.stubs.ResultSetMetaDataStub;
import org.oscm.reportingservice.stubs.ResultSetStub;
import org.oscm.reportingservice.stubs.StatementStub;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;
import org.oscm.test.stubs.BillingServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.types.constants.Configuration;
import org.w3c.dom.Document;

/**
 * @author kulle
 */
public abstract class BaseBillingReport {

    protected static final File XML_FILE_1 = new File(
            "javares/BillingResult.xml");
    protected static final File XML_FILE_2 = new File(
            "javares/BillingResult2.xml");
    protected static final File XML_FILE_3 = new File(
            "javares/BillingResult3.xml");
    protected static final File XML_FILE_4 = new File(
            "javares/BillingResult4.xml");
    protected static final File XML_FILE_6 = new File(
            "javares/BillingResult6.xml");
    protected static final File XML_FILE_STEPPEDPRICES = new File(
            "javares/SteppedPrices.xml");
    protected static final File XML_FILE_OPTIONS = new File(
            "javares/Options.xml");
    protected static final File XML_FILE_UPGRADE = new File(
            "javares/Upgrade.xml");
    protected static final File XML_FILE_ZEROPRICES = new File(
            "javares/ZeroPrices.xml");
    protected static final File XML_PER_TIMEUNIT = new File(
            "javares/BillingResultPerTimeUnit.xml");
    protected static final File XML_NO_CALCULATIONMODE = new File(
            "javares/BillingResultWithoutCalculationMode.xml");

    protected static final long INVALID_USER_ID = 2L;
    protected static final String REPORT_WSDLURL = "http://localhost:8081/Report/ReportingServiceBean?wsdl";
    protected static final String REPORT_SOAP_ENDPOINT = "http://localhost:8081/Report/ReportingServiceBean";
    protected static final String REPORT_ENGINEURL = "http://localhost:8080/birt/frameset?__report=${reportname}.rptdesign&SessionId=${sessionid}&__locale=${locale}&WSDLURL=${wsdlurl}&SOAPEndPoint=${soapendpoint}";
    protected static final String LOCALIZED_REPORT_DESCRIPTION = "LocalizedReportDescription";

    protected static final String VALID_SESSION_ID = "valid_session";
    protected static final String VALID_SESSION_ID2 = "valid_session2";
    protected static final String VALID_SESSION_ID3 = "valid_session3";
    
    protected static final String EMPTY = "";

    protected ReportingServiceBean reporting;
    protected ReportingServiceBeanLocal reportingLocal;
    protected SessionServiceStub prodMgmt;
    protected Session session;
    protected DataService dm;
    protected QueryStub query;
    protected DataSourceStub dataSource;
    protected ConnectionStub conn;

    protected StatementStub stmt;
    protected ResultSetStub rs;
    protected ResultSetMetaDataStub rsmd;
    protected LocalizerServiceStub localizerStub;
    protected Set<OrganizationToRole> roles = new HashSet<>();
    protected Organization organization;
    protected Map<OrganizationRoleType, List<Report>> roleToReports;
    protected BillingServiceStub billing;
    protected List<File> filesToUse = new ArrayList<>();
    protected List<BillingResult> usedBillingResults = new ArrayList<>();

    protected static String userLocale = "en";
    protected static Locale defaultLocale = Locale.ENGLISH;
    
    protected ConfigurationServiceLocal cnfgServLocal;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        filesToUse.add(XML_FILE_1);
        roleToReports = new HashMap<>();
        roleToReports.put(OrganizationRoleType.CUSTOMER,
                getReportList("Event", "Subscription"));
        roleToReports.put(
                OrganizationRoleType.SUPPLIER,
                getReportList("Supplier_Product", "Supplier_Customer",
                        "Supplier_Billing", "Supplier_PaymentResultStatus"));
        roleToReports.put(
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                getReportList("Provider_Event", "Provider_Supplier",
                        "Provider_Subscription", "Provider_Instance"));

        localizerStub = new LocalizerServiceStub() {

            @Override
            public java.util.Locale getDefaultLocale() {
                return defaultLocale;
            }

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
        
        doReturn(true).when(cnfgServLocal).isPaymentInfoAvailable();

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

        dm = mock(DataService.class);
        Organization supplier = new Organization();
        Set<OrganizationToRole> list = new HashSet<>();
        list.add(addOrgToRole(supplier, OrganizationRoleType.SUPPLIER));
        supplier.setGrantedRoles(list);
        PlatformUser platformUser = new PlatformUser();
        organization = new Organization();
        organization.setGrantedRoles(roles);
        OrganizationReference ref = new OrganizationReference(supplier,
                organization, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        organization.getSources().add(ref);
        supplier.getTargets().add(ref);
        platformUser.setOrganization(organization);
        platformUser.setLocale(userLocale);

        doReturn(platformUser).when(dm).find(eq(PlatformUser.class), anyLong());

        billing = new BillingServiceStub() {

            @Override
            public List<BillingResult> generateBillingForAnyPeriod(
                    long startOfPeriod, long endOfPeriod, long organizationKey) {

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
        query = new QueryStub();
        dataSource = new DataSourceStub();
        conn = new ConnectionStub();
        stmt = new StatementStub();
        rs = new ResultSetStub(null);
        rsmd = new ResultSetMetaDataStub();

        session = new Session();
        session.setPlatformUserKey(1L);
        dataSource.setConnection(conn);
        conn.setStatement(stmt);
        stmt.setResultSet(rs);
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
        doNothing().when(reportingLocal)
                .putToCache(anyString(), any(RDO.class));
    }

    private List<Report> getReportList(String... reportNames) {
        List<Report> result = new ArrayList<>();
        for (String string : reportNames) {
            Report report = new Report();
            report.setReportName(string);
            result.add(report);
        }
        return result;
    }

    private static String getTestFileAsString(File file) {
        FileInputStream fis = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            fis = new FileInputStream(file);
            Document doc = builder.parse(new FileInputStream(file));
            return XMLConverter.convertToString(doc, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Streams.close(fis);
        }
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

    @After
    public void resetCommonData() {
        userLocale = "en";
        defaultLocale = Locale.ENGLISH;
    }

    void mockResultData(File f) throws
            InterruptedException, IOException {
        mockResultData(f, "", "");
    }

    void mockResultData(File f, String name, String localizedName)
            throws InterruptedException, IOException {
        FileInputStream inputStream = null;
        try {
            DataSet br = new DataSet();
            inputStream = new FileInputStream(f);
            br.addRow(Arrays.asList(new Object[] { Long.valueOf(123L),
                    Long.valueOf(234L), Long.valueOf(345L),
                    Strings.toString(Streams.readFrom(inputStream)),
                    "Supplier", "SupAddress", null, "TheService" }));
            DataSet srv = new DataSet();
            srv.addRow(Arrays.asList(new Object[] { name, localizedName }));
            doReturn(br).doReturn(srv).when(dm)
                    .executeQueryForRawData(any(SqlQuery.class));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

}
