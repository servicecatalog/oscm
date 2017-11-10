/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Report;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ReportType;
import org.oscm.internal.vo.VOReport;

public class GetAvailableReportsTest {

    private static final String REPORTENGINE_URL = "http://estkulle:8180/birt/frameset?__report=${reportname}.rptdesign&SessionId=${sessionid}&__locale=${locale}&WSDLURL=${wsdlurl}&SOAPEndPoint=${soapendpoint}";

    ReportingServiceBean reportBean = new ReportingServiceBean();
    PlatformUser currentUser;
    Organization organization;

    Set<OrganizationToRole> supplierRoles;
    Set<OrganizationToRole> tpRoles;
    Set<OrganizationToRole> customerRoles;

    Query queryMock;

    @Before
    public void setup() {
        setupRolesForSupplier();
        setupRolesForTp();
        setupRolesForCustomer();

        DataService dataServiceMock = mock(DataService.class);
        reportBean.dataService = dataServiceMock;
        ConfigurationServiceLocal configMock = mock(ConfigurationServiceLocal.class);
        reportBean.configurationService = configMock;

        organization = new Organization();
        currentUser = new PlatformUser();
        currentUser.setOrganization(organization);
        currentUser.setLocale("en");
        when(dataServiceMock.getCurrentUser()).thenReturn(currentUser);

        queryMock = mock(Query.class);
        when(dataServiceMock.createNamedQuery(anyString())).thenReturn(
                queryMock);

        ConfigurationSetting setting = new ConfigurationSetting();
        setting.setValue(REPORTENGINE_URL);
        when(
                configMock.getConfigurationSetting(
                        eq(ConfigurationKey.REPORT_ENGINEURL),
                        eq(Configuration.GLOBAL_CONTEXT))).thenReturn(setting);
        ConfigurationSetting setting2 = new ConfigurationSetting();
        setting2.setValue("");
        when(
                configMock.getConfigurationSetting(
                        eq(ConfigurationKey.REPORT_SOAP_ENDPOINT),
                        eq(Configuration.GLOBAL_CONTEXT))).thenReturn(setting2);
        ConfigurationSetting setting3 = new ConfigurationSetting();
        setting3.setValue("");
        when(
                configMock.getConfigurationSetting(
                        eq(ConfigurationKey.REPORT_WSDLURL),
                        eq(Configuration.GLOBAL_CONTEXT))).thenReturn(setting3);

        LocalizerServiceLocal localizerMock = mock(LocalizerServiceLocal.class);
        reportBean.localizerService = localizerMock;
        when(
                localizerMock.getLocalizedTextFromDatabase(anyString(),
                        anyLong(), any(LocalizedObjectTypes.class)))
                .thenReturn("LOCALIZED TEXT");
    }

    private void setupRolesForSupplier() {
        supplierRoles = new HashSet<OrganizationToRole>();
        OrganizationToRole supplierRole = new OrganizationToRole();
        supplierRole.setOrganization(organization);
        OrganizationRole role1 = new OrganizationRole(
                OrganizationRoleType.SUPPLIER);
        role1.setKey(1L);
        supplierRole.setOrganizationRole(role1);
        supplierRole.setKey(1L);
        supplierRoles.add(supplierRole);

        OrganizationToRole customerRole = new OrganizationToRole();
        customerRole.setOrganization(organization);
        OrganizationRole role2 = new OrganizationRole(
                OrganizationRoleType.CUSTOMER);
        role2.setKey(2L);
        customerRole.setOrganizationRole(role2);
        customerRole.setKey(2L);
        supplierRoles.add(customerRole);
    }

    private void setupRolesForTp() {
        tpRoles = new HashSet<OrganizationToRole>();
        OrganizationToRole tpRole = new OrganizationToRole();
        tpRole.setOrganization(organization);
        OrganizationRole role1 = new OrganizationRole(
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        role1.setKey(3L);
        tpRole.setOrganizationRole(role1);
        tpRole.setKey(3L);
        tpRoles.add(tpRole);

        OrganizationToRole customerRole = new OrganizationToRole();
        customerRole.setOrganization(organization);
        OrganizationRole role2 = new OrganizationRole(
                OrganizationRoleType.CUSTOMER);
        role2.setKey(4L);
        customerRole.setOrganizationRole(role2);
        customerRole.setKey(4L);
        tpRoles.add(customerRole);
    }

    private void setupRolesForCustomer() {
        customerRoles = new HashSet<OrganizationToRole>();
        OrganizationToRole customerRole = new OrganizationToRole();
        customerRole.setOrganization(organization);
        OrganizationRole role = new OrganizationRole(
                OrganizationRoleType.CUSTOMER);
        role.setKey(5L);
        customerRole.setOrganizationRole(role);
        customerRole.setKey(5L);
        customerRoles.add(customerRole);
    }

    @Test
    public void orgRolesForReportType_All_Supplier() {
        organization.setGrantedRoles(supplierRoles);
        Set<OrganizationToRole> rolesForReportType = reportBean
                .mapReportTypeToRoles(currentUser.getOrganization(),
                        ReportType.ALL);
        assertEquals(2, rolesForReportType.size());
        for (OrganizationToRole otr : rolesForReportType) {
            assertTrue(supplierRoles.contains(otr));
        }
    }

    @Test
    public void orgRolesForReportType_NonCustomer_Supplier() {
        organization.setGrantedRoles(supplierRoles);
        Set<OrganizationToRole> rolesForReportType = reportBean
                .mapReportTypeToRoles(currentUser.getOrganization(),
                        ReportType.NON_CUSTOMER);
        assertEquals(1, rolesForReportType.size());
        for (OrganizationToRole otr : rolesForReportType) {
            assertTrue(supplierRoles.contains(otr));
        }
    }

    @Test
    public void orgRolesForReportType_All_TechnologyProvider() {
        organization.setGrantedRoles(tpRoles);
        Set<OrganizationToRole> rolesForReportType = reportBean
                .mapReportTypeToRoles(currentUser.getOrganization(),
                        ReportType.ALL);
        assertEquals(2, rolesForReportType.size());
        for (OrganizationToRole otr : rolesForReportType) {
            assertTrue(tpRoles.contains(otr));
        }
    }

    @Test
    public void orgRolesForReportType_NonCustomer_TechnologyProvider() {
        organization.setGrantedRoles(tpRoles);
        Set<OrganizationToRole> rolesForReportType = reportBean
                .mapReportTypeToRoles(currentUser.getOrganization(),
                        ReportType.NON_CUSTOMER);
        assertEquals(1, rolesForReportType.size());
        for (OrganizationToRole otr : rolesForReportType) {
            assertTrue(tpRoles.contains(otr));
        }
    }

    @Test
    public void orgRolesForReportType_All_Customer() {
        organization.setGrantedRoles(customerRoles);
        Set<OrganizationToRole> rolesForReportType = reportBean
                .mapReportTypeToRoles(currentUser.getOrganization(),
                        ReportType.ALL);
        assertEquals(1, rolesForReportType.size());
        for (OrganizationToRole otr : rolesForReportType) {
            assertTrue(customerRoles.contains(otr));
        }
    }

    @Test
    public void orgRolesForReportType_NonCustomer_Customer() {
        organization.setGrantedRoles(customerRoles);
        Set<OrganizationToRole> rolesForReportType = reportBean
                .mapReportTypeToRoles(currentUser.getOrganization(),
                        ReportType.NON_CUSTOMER);
        assertEquals(0, rolesForReportType.size());
    }

    @Test
    public void orgRolesForReportType_NonCustomer_SupplierAndTp() {
        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.addAll(supplierRoles);
        roles.addAll(tpRoles);
        organization.setGrantedRoles(roles);

        Set<OrganizationToRole> rolesForReportType = reportBean
                .mapReportTypeToRoles(currentUser.getOrganization(),
                        ReportType.NON_CUSTOMER);
        assertEquals(3, rolesForReportType.size());
        for (OrganizationToRole otr : rolesForReportType) {
            assertTrue(roles.contains(otr));
        }
    }

    @Test
    public void getReportsByRoles() {
        List<Object> roles = new ArrayList<Object>();
        Report report = new Report();
        report.setReportName("report");
        roles.add(report);

        when(queryMock.getResultList()).thenReturn(roles);

        List<Report> reportsByRoles = reportBean
                .getReportsByRoles(supplierRoles);
        assertEquals(2, reportsByRoles.size());
    }

    @Test
    public void convertToVOReports() {
        List<Report> reports = new ArrayList<Report>();
        Report report1 = new Report();
        report1.setReportName("a");
        reports.add(report1);
        Report report2 = new Report();
        report2.setReportName("b");
        reports.add(report2);

        List<VOReport> voReports = reportBean.convertToVOReports(currentUser,
                reports);
        assertEquals(2, voReports.size());
        assertEquals("a", voReports.get(0).getReportName());
        assertEquals("b", voReports.get(1).getReportName());
    }

    @Test
    public void supplier_ignoreCustomerReports() {
        organization.setGrantedRoles(supplierRoles);

        List<Object> supplierRoles = new ArrayList<Object>();
        Report supplierReport = new Report();
        supplierReport.setReportName("SUPPLIER");
        supplierRoles.add(supplierReport);
        when(queryMock.getResultList()).thenReturn(supplierRoles);

        List<VOReport> reports = reportBean
                .getAvailableReports(ReportType.NON_CUSTOMER);

        verify(queryMock, times(1)).getResultList();
        assertEquals(1, reports.size());
    }

    @Test
    public void mapReportTypeToRoles_bug10663() {
        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.addAll(supplierRoles);
        organization.setGrantedRoles(roles);

        Set<OrganizationToRole> rolesForReportType = reportBean
                .mapReportTypeToRoles(currentUser.getOrganization(),
                        ReportType.NON_CUSTOMER);
        assertEquals(1, rolesForReportType.size());
        assertEquals(2, currentUser.getOrganization().getGrantedRoles().size());
        for (OrganizationToRole otr : rolesForReportType) {
            assertTrue(roles.contains(otr));
        }

    }
}
