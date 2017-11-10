/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Node;

import org.oscm.dataservice.local.DataService;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReport;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReports;
import org.oscm.reportingservice.business.model.partnerrevenue.RDORevenueDetail;
import org.oscm.reportingservice.business.model.partnerrevenue.RDORevenueDetailService;
import org.oscm.reportingservice.dao.PartnerRevenueDao;
import org.oscm.reportingservice.dao.PartnerRevenueDao.ReportData;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;
import org.oscm.test.DateTimeHandling;

/**
 * @author kulle
 * 
 */
public class PartnerRevenueBuilderTest {

    DataService ds;

    @Before
    public void setup() {
        ds = mock(DataService.class);
    }

    PartnerRevenueDao givenBrokerSqlData(String address, String name,
            String id, long start, long end) throws Exception {
        PartnerRevenueDao dao = new PartnerRevenueDao(ds);
        dao.getReportData()
                .add(newReportData("broker", "broker", address, name, id,
                        start, end));
        return dao;
    }

    private ReportData newReportData(String fileName, String type,
            String address, String name, String id, long start, long end)
            throws Exception {
        ReportData data = new PartnerRevenueDao(null).new ReportData();
        data.setResulttype(type);
        data.setAddress(address);
        data.setName(name);
        data.setOrganizationId(id);
        data.setPeriodEnd(end);
        data.setPeriodStart(start);
        File file = new File(this.getClass().getResource("/" + fileName.toLowerCase() + ".xml").getFile());
        data.setResultXml(readXmlFromFile(file));
        System.out.println(data.getResultXml());
        return data;
    }

    PartnerRevenueDao givenResellerSqlData(String address, String name,
            String id, long start, long end) throws Exception {
        PartnerRevenueDao dao = new PartnerRevenueDao(ds);
        dao.getReportData().add(
                newReportData("reseller", "reseller", address, name, id, start,
                        end));
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
                .add(newReportData(
                        "broker",
                        "broker",
                        "address",
                        "name",
                        "id",
                        DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                        DateTimeHandling.calculateMillis("2012-07-01 00:00:00")));
        dao.getReportData()
                .add(newReportData(
                        "reseller",
                        "reseller",
                        "address",
                        "name",
                        "id",
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
        assertEquals("address", partnerReport.getAddress());
        assertEquals("broker", partnerReport.getVendorType());
        assertTrue(partnerReport.getServerTimeZone().startsWith("UTC"));
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
        assertEquals("200.00", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getAmount());
        assertEquals("15.23", partnerReport.getCurrencies().get(0)
                .getRevenueDetails().get(0).getRevenue());

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
        assertTrue(partnerReport.getServerTimeZone().startsWith("UTC"));
    }

    @Test
    public void build_overview_reseller() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenResellerSqlData();

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

        // euro - service 2
        assertEquals("another_service (11007)", euroDetails.getServices()
                .get(1).getService());
        assertEquals("250,00", euroDetails.getServices().get(1).getAmount());
        assertEquals("37,50", euroDetails.getServices().get(1).getRevenue());
        assertEquals("15,00%", euroDetails.getServices().get(1)
                .getRevenueShare());
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
        assertTrue(partnerReport.getServerTimeZone().startsWith("UTC"));
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

    @Test(expected = NullPointerException.class)
    public void isServiceRevenueValid_serviceRevenueIsNull() throws Exception {
        // given
        PartnerRevenueBuilder builder = new PartnerRevenueBuilder(
                Locale.ENGLISH, null);

        // when
        builder.isServiceRevenueValid(null);
    }

    @Test
    public void isServiceRevenueValid_serviceRevenueIsZero() throws Exception {
        // given
        PartnerRevenueBuilder builder = new PartnerRevenueBuilder(
                Locale.ENGLISH, null);

        // when
        boolean revenueValid = builder.isServiceRevenueValid("0.00");

        // then
        assertFalse(revenueValid);
    }

//    @Test
//    public void isServiceRevenueValid_serviceRevenueIsGreaterThanZero()
//            throws Exception {
//        // given
//        PartnerRevenueBuilder builder = new PartnerRevenueBuilder(
//                Locale.ENGLISH, null);
//
//        // when
//        boolean revenueValid = builder.isServiceRevenueValid("0.01");
//
//        // then
//        assertTrue(revenueValid);
//    }

    @Test
    public void processService_createRevenueDetailService() throws Exception {
        // given
        PartnerRevenueBuilder builder = spy(new PartnerRevenueBuilder(
                Locale.ENGLISH, null));

        doReturn("1").when(builder).readServiceRevenue(anyString(),
                anyString(), anyString());
        doReturn("423").when(builder).getServiceKey(any(Node.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        }).when(builder).createRevenueDetailService(anyInt(), anyString(),
                anyString(), anyString());

        List<RDORevenueDetailService> result = new LinkedList<RDORevenueDetailService>();
        Node dummyService = null;

        // when
        builder.processService(result, 0, dummyService, "dummy", "dummy");

        // then
        assertEquals(1, result.size());
        verify(builder, times(1)).createRevenueDetailService(anyInt(),
                anyString(), anyString(), anyString());
    }

    @Test
    public void processService_doNotCreateRevenueShareSummary()
            throws Exception {
        // given
        PartnerRevenueBuilder builder = spy(new PartnerRevenueBuilder(
                Locale.ENGLISH, null));
        doReturn("0").when(builder).readServiceRevenue(anyString(),
                anyString(), anyString());
        doReturn("423").when(builder).getServiceKey(any(Node.class));
        List<RDORevenueDetailService> result = new LinkedList<RDORevenueDetailService>();
        Node dummyService = null;

        // when
        builder.processService(result, 0, dummyService, "dummy", "dummy");

        // then
        assertEquals(0, result.size());
        verify(builder, times(0)).createRevenueDetailService(anyInt(),
                anyString(), anyString(), anyString());
    }

    @Test
    public void buildReports_broker_serviceWithZeroAmount() throws Exception {
        // given
        PartnerRevenueDao dao = new PartnerRevenueDao(ds);
        dao.getReportData().add(
                newReportData("broker3", "broker", "", "", "", 0, 0));
        PartnerRevenueBuilder builder = new PartnerRevenueBuilder(
                Locale.ENGLISH, dao.getReportData());

        // when
        RDOPartnerReports report = builder.buildReports();

        // then
        List<RDORevenueDetailService> services = report.getReports().get(0)
                .getCurrencies().get(0).getRevenueDetails().get(0)
                .getServices();
        assertEquals(1, services.size());
        assertEquals("40.00", services.get(0).getAmount());
    }

    @Test
    public void buildReports_reseller_serviceWithZeroAmount() throws Exception {
        // given
        PartnerRevenueDao dao = new PartnerRevenueDao(ds);
        dao.getReportData().add(
                newReportData("reseller3", "reseller", "", "", "", 0, 0));
        PartnerRevenueBuilder builder = new PartnerRevenueBuilder(
                Locale.ENGLISH, dao.getReportData());

        // when
        RDOPartnerReports report = builder.buildReports();

        // then
        List<RDORevenueDetailService> services = report.getReports().get(0)
                .getCurrencies().get(0).getRevenueDetails().get(0)
                .getServices();
        assertEquals(1, services.size());
        assertEquals("250.00", services.get(0).getAmount());
    }
}
