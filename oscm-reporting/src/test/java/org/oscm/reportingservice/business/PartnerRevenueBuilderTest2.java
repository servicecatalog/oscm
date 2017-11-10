/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReport;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReports;
import org.oscm.reportingservice.business.model.partnerrevenue.RDORevenueDetail;
import org.oscm.reportingservice.dao.PartnerRevenueDao;
import org.oscm.reportingservice.dao.PartnerRevenueDao.ReportData;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;
import org.oscm.test.DateTimeHandling;

/**
 * @author Mao
 * 
 */
public class PartnerRevenueBuilderTest2 {

    DataService ds;

    @Before
    public void setup() {
        ds = mock(DataService.class);
    }

    PartnerRevenueDao givenBrokerSqlData(String address, String name,
            String id, long start, long end) throws Exception {
        PartnerRevenueDao dao = new PartnerRevenueDao(ds);
        dao.getReportData().add(
                newReportDataForBroker("broker", address, name, id, "EN",
                        start, end));
        return dao;
    }

    private ReportData newReportDataForBroker(String type, String address,
            String name, String id, String countryIsoCode, long start, long end)
            throws Exception {
        ReportData data = new PartnerRevenueDao(null).new ReportData();
        data.setResulttype(type);
        data.setAddress(address);
        data.setName(name);
        data.setCountryIsoCode(countryIsoCode);
        data.setOrganizationId(id);
        data.setPeriodEnd(end);
        data.setPeriodStart(start);
        File file = new File(this.getClass().getResource("/broker2.xml").getFile());
        data.setResultXml(readXmlFromFile(file));
        return data;
    }

    private ReportData newReportDataForReseller(String type, String address,
            String name, String id, String countryIsoCode, long start, long end)
            throws Exception {
        ReportData data = new PartnerRevenueDao(null).new ReportData();
        data.setResulttype(type);
        data.setAddress(address);
        data.setName(name);
        data.setCountryIsoCode(countryIsoCode);
        data.setOrganizationId(id);
        data.setPeriodEnd(end);
        data.setPeriodStart(start);
        File file = new File(this.getClass().getResource("/reseller2.xml").getFile());
        data.setResultXml(readXmlFromFile(file));
        return data;
    }

    PartnerRevenueDao givenResellerSqlData(String address, String name,
            String id, long start, long end) throws Exception {
        PartnerRevenueDao dao = new PartnerRevenueDao(ds);
        dao.getReportData().add(
                newReportDataForReseller("reseller", address, name, id, "EN",
                        start, end));
        return dao;
    }

    PartnerRevenueDao givenBrokerSqlData() throws Exception {
        return givenBrokerSqlData("address", "name", "id",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"));
    }

    PartnerRevenueDao givenResellerSqlData() throws Exception {
        return givenResellerSqlData("address", "name", "id",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"));
    }

    PartnerRevenueDao givenBrokerAndResellerSqlData() throws Exception {
        PartnerRevenueDao dao = new PartnerRevenueDao(ds);
        dao.getReportData()
                .add(newReportDataForBroker(
                        "broker",
                        "address",
                        "name",
                        "id",
                        "EN",
                        DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                        DateTimeHandling.calculateMillis("2012-07-01 00:00:00")));
        dao.getReportData()
                .add(newReportDataForReseller(
                        "reseller",
                        "address",
                        "name",
                        "id",
                        "EN",
                        DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                        DateTimeHandling.calculateMillis("2012-07-01 00:00:00")));
        return dao;
    }

    private static String readXmlFromFile(File testFile)
            throws FileNotFoundException, InterruptedException, IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(testFile);
            String billingResult = Strings.toString(Streams.readFrom(is));
            return billingResult;
        } finally {
            Streams.close(is);
        }
    }

    @Test
    public void buildSingleReport_nullReportDataList() throws Exception {
        // given
        List<ReportData> reportData = null;

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, reportData).buildSingleReport();

        // then
        assertNotNull(partnerReport);
        assertTrue(partnerReport.getCurrencies().isEmpty());
    }

    @Test
    public void buildSingleReport_emptyReportDataList() throws Exception {
        // given
        List<ReportData> reportData = new ArrayList<PartnerRevenueDao.ReportData>();

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, reportData).buildSingleReport();

        // then
        assertNotNull(partnerReport);
        assertTrue(partnerReport.getCurrencies().isEmpty());
    }

    @Test
    public void buildRreports_nullReportDataList() throws Exception {
        // given
        List<ReportData> reportData = null;

        // when
        RDOPartnerReports partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, reportData).buildReports();

        // then
        assertNotNull(partnerReport);
        assertTrue(partnerReport.getReports().isEmpty());
    }

    @Test
    public void buildReports_emptyReportDataList() throws Exception {
        // given
        List<ReportData> reportData = new ArrayList<PartnerRevenueDao.ReportData>();

        // when
        RDOPartnerReports partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, reportData).buildReports();

        // then
        assertNotNull(partnerReport);
        assertTrue(partnerReport.getReports().isEmpty());
    }

    @Test
    public void build_header_broker() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenBrokerSqlData("address", "name",
                "cb1a8642",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"));

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlResult.getReportData()).buildSingleReport();

        // then
        assertEquals("name (cb1a8642)", partnerReport.getVendor());
        assertEquals("2012-06-01 00:00:00", partnerReport.getPeriodStart());
        assertEquals("2012-07-01 00:00:00", partnerReport.getPeriodEnd());
        assertEquals("EN", partnerReport.getCountryName());
        assertEquals("address", partnerReport.getAddress());
        assertEquals("broker", partnerReport.getVendorType());
    }

    @Test
    public void build_overview_broker() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenBrokerSqlData();

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlResult.getReportData()).buildSingleReport();

        // then
        assertEquals(2, partnerReport.getCurrencies().size());
        assertEquals("300.00", partnerReport.getCurrencies().get(0)
                .getTotalAmount());
        assertEquals("27.40", partnerReport.getCurrencies().get(0)
                .getTotalRevenue());
        assertEquals("27.40", partnerReport.getCurrencies().get(0)
                .getBrokerRevenue());
        assertEquals("", partnerReport.getCurrencies().get(0)
                .getRemainingAmount());
        assertEquals("EUR", partnerReport.getCurrencies().get(0).getCurrency());

        assertEquals("200.00", partnerReport.getCurrencies().get(1)
                .getTotalAmount());
        assertEquals("27.00", partnerReport.getCurrencies().get(1)
                .getTotalRevenue());
        assertEquals("27.00", partnerReport.getCurrencies().get(1)
                .getBrokerRevenue());
        assertEquals("", partnerReport.getCurrencies().get(1)
                .getRemainingAmount());
        assertEquals("USD", partnerReport.getCurrencies().get(1).getCurrency());
    }

    @Test
    public void build_overviewForVendor_broker() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenBrokerSqlData();

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlResult.getReportData()).buildSingleReport();

        // then
        assertEquals(2, partnerReport.getCurrencies().get(0)
                .getRevenueDetails().size());

        // EUR, supplier 1
        assertEquals("First supplier ever (supp1)", partnerReport
                .getCurrencies().get(0).getRevenueDetails().get(0).getVendor());
        assertEquals("EN", partnerReport.getCountryName());
        assertEquals("300.00", partnerReport.getCurrencies().get(0)
                .getTotalAmount());
        assertEquals("27.40", partnerReport.getCurrencies().get(0)
                .getBrokerRevenue());
        assertEquals("200.00", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getAmount());
        assertEquals("15.23", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getRevenue());
        assertEquals("100.00", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getServices().get(0).getAmount());
        assertEquals("10.00", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getServices().get(0).getRevenue());
        assertEquals("30.00", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getServices().get(0)
                .getServicesPerCustomer().get(0).getAmount());
        assertEquals("3.00", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getServices().get(0)
                .getServicesPerCustomer().get(0).getRevenue());
        assertEquals("10.00%", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getServices().get(0)
                .getServicesPerCustomer().get(0).getRevenueShare());

        // EUR, supplier 2
        assertEquals("Yet another supplier (supp2)", partnerReport
                .getCurrencies().get(0).getRevenueDetails().get(1).getVendor());
        assertEquals("100.00", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(1).getAmount());
        assertEquals("12.17", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(1).getRevenue());

        // USD, supplier 1
        assertEquals(1, partnerReport.getCurrencies().get(1)
                .getRevenueDetails().size());
        assertEquals("Yet another supplier (supp2)", partnerReport
                .getCurrencies().get(1).getRevenueDetails().get(0).getVendor());
        assertEquals("200.00", partnerReport.getCurrencies().get(1)
                .getRevenueDetails().get(0).getAmount());
        assertEquals("27.00", partnerReport.getCurrencies().get(1)
                .getRevenueDetails().get(0).getRevenue());
    }

    @Test
    public void build_overviewForVendor_broker_german() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenBrokerSqlData();

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.GERMAN, sqlResult.getReportData()).buildSingleReport();

        // then
        assertEquals(2, partnerReport.getCurrencies().get(0)
                .getRevenueDetails().size());

        // EUR, supplier 1
        assertEquals("First supplier ever (supp1)", partnerReport
                .getCurrencies().get(0).getRevenueDetails().get(0).getVendor());
        assertEquals("200,00", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getAmount());
        assertEquals("15,23", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getRevenue());

        // EUR, supplier 2
        assertEquals("Yet another supplier (supp2)", partnerReport
                .getCurrencies().get(0).getRevenueDetails().get(1).getVendor());
        assertEquals("100,00", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(1).getAmount());
        assertEquals("12,17", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(1).getRevenue());

        // USD, supplier 1
        assertEquals(1, partnerReport.getCurrencies().get(1)
                .getRevenueDetails().size());
        assertEquals("Yet another supplier (supp2)", partnerReport
                .getCurrencies().get(1).getRevenueDetails().get(0).getVendor());
        assertEquals("200,00", partnerReport.getCurrencies().get(1)
                .getRevenueDetails().get(0).getAmount());
        assertEquals("27,00", partnerReport.getCurrencies().get(1)
                .getRevenueDetails().get(0).getRevenue());
    }

    @Test
    public void build_services_broker() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenBrokerSqlData();

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlResult.getReportData()).buildSingleReport();

        // then
        List<RDORevenueDetail> euroDetails = partnerReport.getCurrencies()
                .get(0).getRevenueDetails();
        List<RDORevenueDetail> dollarDetails = partnerReport.getCurrencies()
                .get(1).getRevenueDetails();

        assertEquals(2, euroDetails.size());

        // euro - service 1
        assertEquals("firstService (10001)", euroDetails.get(0).getServices()
                .get(0).getService());
        assertEquals("100.00", euroDetails.get(0).getServices().get(0)
                .getAmount());
        assertEquals("10.00", euroDetails.get(0).getServices().get(0)
                .getRevenue());
        assertEquals("10.00%", euroDetails.get(0).getServices().get(0)
                .getRevenueShare());

        // euro - service 2
        assertEquals("secondService (10002)", euroDetails.get(0).getServices()
                .get(1).getService());
        assertEquals("100.00", euroDetails.get(0).getServices().get(1)
                .getAmount());
        assertEquals("5.23", euroDetails.get(0).getServices().get(1)
                .getRevenue());
        assertEquals("5.23%", euroDetails.get(0).getServices().get(1)
                .getRevenueShare());

        // euro - service 3
        assertEquals("foreignService (10003)", euroDetails.get(1).getServices()
                .get(0).getService());
        assertEquals("100.00", euroDetails.get(1).getServices().get(0)
                .getAmount());
        assertEquals("12.17", euroDetails.get(1).getServices().get(0)
                .getRevenue());
        assertEquals("12.17%", euroDetails.get(1).getServices().get(0)
                .getRevenueShare());

        // dollar - service 1
        assertEquals("service_using_dollars (10004)", dollarDetails.get(0)
                .getServices().get(0).getService());
        assertEquals("200.00", dollarDetails.get(0).getServices().get(0)
                .getAmount());
        assertEquals("27.00", dollarDetails.get(0).getServices().get(0)
                .getRevenue());
        assertEquals("13.50%", dollarDetails.get(0).getServices().get(0)
                .getRevenueShare());
    }

    @Test
    public void build_header_reseller() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenResellerSqlData("address", "name",
                "cb1a8642",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"));

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlResult.getReportData()).buildSingleReport();

        // then
        assertEquals("name (cb1a8642)", partnerReport.getVendor());
        assertEquals("2012-06-01 00:00:00", partnerReport.getPeriodStart());
        assertEquals("2012-07-01 00:00:00", partnerReport.getPeriodEnd());
        assertEquals("address", partnerReport.getAddress());
        assertEquals("reseller", partnerReport.getVendorType());
    }

    @Test
    public void build_overview_reseller() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenResellerSqlData();
        System.out.println(sqlResult.getReportData().get(0).getResultXml());
        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlResult.getReportData()).buildSingleReport();

        // then
        assertEquals(1, partnerReport.getCurrencies().size());
        assertEquals("418.60", partnerReport.getCurrencies().get(0)
                .getTotalAmount());
        assertEquals("53.99", partnerReport.getCurrencies().get(0)
                .getTotalRevenue());
        assertEquals("", partnerReport.getCurrencies().get(0)
                .getBrokerRevenue());
        assertEquals("364.61", partnerReport.getCurrencies().get(0)
                .getRemainingAmount());
        assertEquals("EUR", partnerReport.getCurrencies().get(0).getCurrency());
    }

    @Test
    public void build_overviewForVendor_reseller() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenResellerSqlData();

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlResult.getReportData()).buildSingleReport();

        // then
        assertEquals(1, partnerReport.getCurrencies().get(0)
                .getRevenueDetails().size());

        // EUR, supplier 1
        assertEquals("Supplier for Reseller (supp1)", partnerReport
                .getCurrencies().get(0).getRevenueDetails().get(0).getVendor());
        assertEquals("418.60", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getAmount());
        assertEquals("79.65", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getRevenue());
    }

    @Test
    public void build_services_reseller() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenResellerSqlData();

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlResult.getReportData()).buildSingleReport();

        // then
        RDORevenueDetail euroDetails = partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0);

        assertEquals(2, euroDetails.getServices().size());

        // euro - service 1
        assertEquals("reseller_mode_service (11001)", euroDetails.getServices()
                .get(0).getService());
        assertEquals("168.60", euroDetails.getServices().get(0).getAmount());
        assertEquals("42.15", euroDetails.getServices().get(0).getRevenue());
        assertEquals("25.00%", euroDetails.getServices().get(0)
                .getRevenueShare());

        // euro - service 2
        assertEquals("another_service (11007)", euroDetails.getServices()
                .get(1).getService());
        assertEquals("250.00", euroDetails.getServices().get(1).getAmount());
        assertEquals("37.50", euroDetails.getServices().get(1).getRevenue());
        assertEquals("15.00%", euroDetails.getServices().get(1)
                .getRevenueShare());
    }

    @Test
    public void build_services_reseller_german() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenResellerSqlData();

        // when
        RDOPartnerReport partnerReport = new PartnerRevenueBuilder(
                Locale.GERMAN, sqlResult.getReportData()).buildSingleReport();

        // then
        RDORevenueDetail euroDetails = partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0);

        assertEquals(2, euroDetails.getServices().size());

        // euro - service 1
        assertEquals("reseller_mode_service (11001)", euroDetails.getServices()
                .get(0).getService());
        assertEquals("168,60", euroDetails.getServices().get(0).getAmount());
        assertEquals("42,15", euroDetails.getServices().get(0).getRevenue());
        assertEquals("25,00%", euroDetails.getServices().get(0)
                .getRevenueShare());
        // euro - service 1 - customer X
        assertEquals("customerX (d23823782)", euroDetails.getServices().get(0)
                .getServicesPerCustomer().get(0).getCustomer());
        assertEquals("98,40", euroDetails.getServices().get(0)
                .getServicesPerCustomer().get(0).getAmount());
        assertEquals("88,56", euroDetails.getServices().get(0)
                .getServicesPerCustomer().get(0).getPurchasePrice());
        assertEquals("9,84", euroDetails.getServices().get(0)
                .getServicesPerCustomer().get(0).getRevenue());
        assertEquals("10,00%", euroDetails.getServices().get(0)
                .getServicesPerCustomer().get(0).getRevenueShare());

        // euro - service 1 - customer Y
        assertEquals("customerY (g26754723)", euroDetails.getServices().get(0)
                .getServicesPerCustomer().get(1).getCustomer());
        assertEquals("70,20", euroDetails.getServices().get(0)
                .getServicesPerCustomer().get(1).getAmount());
        assertEquals("63,18", euroDetails.getServices().get(0)
                .getServicesPerCustomer().get(1).getPurchasePrice());
        assertEquals("7,02", euroDetails.getServices().get(0)
                .getServicesPerCustomer().get(1).getRevenue());
        assertEquals("10,00%", euroDetails.getServices().get(0)
                .getServicesPerCustomer().get(1).getRevenueShare());

        // euro - service 2
        assertEquals("another_service (11007)", euroDetails.getServices()
                .get(1).getService());
        assertEquals("250,00", euroDetails.getServices().get(1).getAmount());
        assertEquals("37,50", euroDetails.getServices().get(1).getRevenue());
        assertEquals("15,00%", euroDetails.getServices().get(1)
                .getRevenueShare());

        // euro - service 2 - customer Z
        assertEquals("customerZ (a26768672)", euroDetails.getServices().get(1)
                .getServicesPerCustomer().get(0).getCustomer());
        assertEquals("250,00", euroDetails.getServices().get(1)
                .getServicesPerCustomer().get(0).getAmount());
        assertEquals("212,50", euroDetails.getServices().get(1)
                .getServicesPerCustomer().get(0).getPurchasePrice());
        assertEquals("37,50", euroDetails.getServices().get(1)
                .getServicesPerCustomer().get(0).getRevenue());
        assertEquals("15,00%", euroDetails.getServices().get(1)
                .getServicesPerCustomer().get(0).getRevenueShare());
    }

    @Test
    public void build_partnerReports_header() throws Exception {
        // given
        PartnerRevenueDao sqlData = givenBrokerAndResellerSqlData();

        // when
        RDOPartnerReports partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlData.getReportData()).buildReports();

        // then
        // BROKER
        assertEquals(2, partnerReport.getReports().size());
        RDOPartnerReport brokerReport = partnerReport.getReports().get(0);
        assertEquals("name (id)", brokerReport.getVendor());
        assertEquals("2012-06-01 00:00:00", brokerReport.getPeriodStart());
        assertEquals("2012-07-01 00:00:00", brokerReport.getPeriodEnd());
        assertEquals("address", brokerReport.getAddress());

        // RESELLER
        RDOPartnerReport resellerReport = partnerReport.getReports().get(1);
        assertEquals("name (id)", resellerReport.getVendor());
        assertEquals("2012-06-01 00:00:00", resellerReport.getPeriodStart());
        assertEquals("2012-07-01 00:00:00", resellerReport.getPeriodEnd());
        assertEquals("address", resellerReport.getAddress());
    }

    @Test
    public void build_partnerReports_overview() throws Exception {
        // given
        PartnerRevenueDao sqlData = givenBrokerAndResellerSqlData();

        // when
        RDOPartnerReports partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlData.getReportData()).buildReports();

        // then
        // BROKER
        RDOPartnerReport brokerReport = partnerReport.getReports().get(0);
        assertEquals(2, brokerReport.getCurrencies().size());
        assertEquals("300.00", brokerReport.getCurrencies().get(0)
                .getTotalAmount());
        assertEquals("27.40", brokerReport.getCurrencies().get(0)
                .getTotalRevenue());
        assertEquals("27.40", brokerReport.getCurrencies().get(0)
                .getBrokerRevenue());
        assertEquals("", brokerReport.getCurrencies().get(0)
                .getRemainingAmount());
        assertEquals("EUR", brokerReport.getCurrencies().get(0).getCurrency());
        assertEquals("200.00", brokerReport.getCurrencies().get(1)
                .getTotalAmount());
        assertEquals("27.00", brokerReport.getCurrencies().get(1)
                .getTotalRevenue());
        assertEquals("27.00", brokerReport.getCurrencies().get(1)
                .getBrokerRevenue());
        assertEquals("", brokerReport.getCurrencies().get(1)
                .getRemainingAmount());
        assertEquals("USD", brokerReport.getCurrencies().get(1).getCurrency());

        // RESELLER
        RDOPartnerReport resellerReport = partnerReport.getReports().get(1);
        assertEquals(1, resellerReport.getCurrencies().size());
        assertEquals("418.60", resellerReport.getCurrencies().get(0)
                .getTotalAmount());
        assertEquals("53.99", resellerReport.getCurrencies().get(0)
                .getTotalRevenue());
        assertEquals("", resellerReport.getCurrencies().get(0)
                .getBrokerRevenue());
        assertEquals("364.61", resellerReport.getCurrencies().get(0)
                .getRemainingAmount());
        assertEquals("EUR", brokerReport.getCurrencies().get(0).getCurrency());
    }

    @Test
    public void build_partnerReports_overviewForVendor() throws Exception {
        // given
        PartnerRevenueDao sqlData = givenBrokerAndResellerSqlData();

        // when
        RDOPartnerReports partnerReport = new PartnerRevenueBuilder(
                Locale.ENGLISH, sqlData.getReportData()).buildReports();

        // then
        // BROKER
        RDOPartnerReport brokerReport = partnerReport.getReports().get(0);
        // EUR, supplier 1
        assertEquals("First supplier ever (supp1)", brokerReport
                .getCurrencies().get(0).getRevenueDetails().get(0).getVendor());
        assertEquals("200.00", brokerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getAmount());
        assertEquals("15.23", brokerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getRevenue());
        // EUR, supplier 2
        assertEquals("Yet another supplier (supp2)", brokerReport
                .getCurrencies().get(0).getRevenueDetails().get(1).getVendor());
        assertEquals("100.00", brokerReport.getCurrencies().get(0)
                .getRevenueDetails().get(1).getAmount());
        assertEquals("12.17", brokerReport.getCurrencies().get(0)
                .getRevenueDetails().get(1).getRevenue());
        // USD, supplier 1
        assertEquals(1, brokerReport.getCurrencies().get(1).getRevenueDetails()
                .size());
        assertEquals("Yet another supplier (supp2)", brokerReport
                .getCurrencies().get(1).getRevenueDetails().get(0).getVendor());
        assertEquals("200.00", brokerReport.getCurrencies().get(1)
                .getRevenueDetails().get(0).getAmount());
        assertEquals("27.00", brokerReport.getCurrencies().get(1)
                .getRevenueDetails().get(0).getRevenue());

        // RESELLER
        RDOPartnerReport resellerReport = partnerReport.getReports().get(1);
        assertEquals("Supplier for Reseller (supp1)", resellerReport
                .getCurrencies().get(0).getRevenueDetails().get(0).getVendor());
        assertEquals("418.60", resellerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getAmount());
        assertEquals("79.65", resellerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getRevenue());
    }
}
