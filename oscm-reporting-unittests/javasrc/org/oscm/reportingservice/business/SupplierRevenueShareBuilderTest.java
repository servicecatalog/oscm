/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Sep 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDORevenueShareDetail;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDORevenueShareSummary;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareCurrency;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReport;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReports;
import org.oscm.reportingservice.dao.PartnerRevenueDao;
import org.oscm.reportingservice.dao.PartnerRevenueDao.ReportData;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;
import org.oscm.test.DateTimeHandling;

/**
 * @author tokoda
 * 
 */
public class SupplierRevenueShareBuilderTest {

    private DataService ds;
    private HashMap<String, String> serviceIdMap;
    private HashMap<String, String> marketplaceNameMap;
    private HashMap<String, String> serviceNameMap;

    private static final String TEMPLATE_SERVICE_KEY_1 = "11111";
    private static final String TEMPLATE_SERVICE_ID_1 = "templateService1";
    private static final String TEMPLATE_SERVICE_KEY_2 = "22222";
    private static final String TEMPLATE_SERVICE_ID_2 = "templateService2";
    private static final String TEMPLATE_SERVICE_KEY_3 = "33333";
    private static final String TEMPLATE_SERVICE_ID_3 = "templateService3";
    private static final String TEMPLATE_SERVICE_KEY_4 = "44444";
    private static final String TEMPLATE_SERVICE_ID_4 = "templateService4";
    private static final String TEMPLATE_SERVICE_KEY_5 = "55555";
    private static final String TEMPLATE_SERVICE_ID_5 = "templateService5";
    private static final String TEMPLATE_SERVICE_KEY_6 = "66666";
    private static final String TEMPLATE_SERVICE_ID_6 = "templateService6";

    @Before
    public void setup() {
        ds = mock(DataService.class);

        serviceIdMap = new HashMap<String, String>();
        serviceIdMap.put(TEMPLATE_SERVICE_KEY_1, TEMPLATE_SERVICE_ID_1);
        serviceIdMap.put(TEMPLATE_SERVICE_KEY_2, TEMPLATE_SERVICE_ID_2);
        serviceIdMap.put(TEMPLATE_SERVICE_KEY_3, TEMPLATE_SERVICE_ID_3);
        serviceIdMap.put(TEMPLATE_SERVICE_KEY_4, TEMPLATE_SERVICE_ID_4);
        serviceIdMap.put(TEMPLATE_SERVICE_KEY_5, TEMPLATE_SERVICE_ID_5);
        serviceIdMap.put(TEMPLATE_SERVICE_KEY_6, TEMPLATE_SERVICE_ID_6);

        marketplaceNameMap = new HashMap<String, String>();
        marketplaceNameMap.put("mp1", "marketplaceName1");
        marketplaceNameMap.put("mp2", "marketplaceName2");
        marketplaceNameMap.put("mp3", "MarktplatzName3");

        serviceNameMap = new HashMap<String, String>();

        serviceNameMap.put(TEMPLATE_SERVICE_ID_1, "service name 1");
        serviceNameMap.put(TEMPLATE_SERVICE_ID_2, "service name 2");
        serviceNameMap.put(TEMPLATE_SERVICE_ID_3, "service name 3");
        serviceNameMap.put(TEMPLATE_SERVICE_ID_4, "service name 4");
    }

    private PartnerRevenueDao givenReportData(String address, String name,
            String id, long start, long end, String xmlFilePath)
            throws Exception {
        ReportData rd = new PartnerRevenueDao(null).new ReportData();
        rd.setAddress(address);
        rd.setName(name);
        rd.setOrganizationId(id);
        rd.setPeriodEnd(end);
        rd.setPeriodStart(start);
        rd.setResultXml(readXmlFromFile(new File(xmlFilePath)));

        PartnerRevenueDao sqlResult = new PartnerRevenueDao(ds);
        sqlResult.getReportData().add(rd);
        return sqlResult;
    }

    private PartnerRevenueDao givenReportData() throws Exception {
        return givenReportData("address", "name", "id",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"),
                "javares/SupplierRevenueShare.xml");
    }

    private PartnerRevenueDao givenReportData3() throws Exception {
        return givenReportData("address", "name", "id",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"),
                "javares/SupplierRevenueShare3.xml");
    }

    private PartnerRevenueDao givenReportData5() throws Exception {
        return givenReportData("address", "name", "id",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"),
                "javares/SupplierRevenueShare5.xml");
    }

    private PartnerRevenueDao givenMultipleReportData() throws Exception {

        ReportData rd1 = new PartnerRevenueDao(null).new ReportData();
        rd1.setAddress("address1");
        rd1.setName("name1");
        rd1.setOrganizationId("id1");
        rd1.setPeriodStart(DateTimeHandling
                .calculateMillis("2012-07-01 00:00:00"));
        rd1.setPeriodEnd(DateTimeHandling
                .calculateMillis("2012-06-01 00:00:00"));
        rd1.setResultXml(readXmlFromFile(new File(
                "javares/SupplierRevenueShare.xml")));

        ReportData rd2 = new PartnerRevenueDao(null).new ReportData();
        rd2.setAddress("address2");
        rd2.setName("name2");
        rd2.setOrganizationId("id2");
        rd2.setPeriodStart(DateTimeHandling
                .calculateMillis("2012-07-01 00:00:00"));
        rd2.setPeriodEnd(DateTimeHandling
                .calculateMillis("2012-06-01 00:00:00"));
        rd2.setResultXml(readXmlFromFile(new File(
                "javares/SupplierRevenueShare2.xml")));

        ReportData rd3 = new PartnerRevenueDao(null).new ReportData();
        rd3.setAddress("address3");
        rd3.setName("name3");
        rd3.setOrganizationId("id3");
        rd3.setPeriodStart(DateTimeHandling
                .calculateMillis("2012-07-01 00:00:00"));
        rd3.setPeriodEnd(DateTimeHandling
                .calculateMillis("2012-06-01 00:00:00"));
        rd3.setResultXml(readXmlFromFile(new File(
                "javares/SupplierRevenueShare3.xml")));

        PartnerRevenueDao sqlResult = new PartnerRevenueDao(ds);
        sqlResult.getReportData().add(rd1);
        sqlResult.getReportData().add(rd2);
        sqlResult.getReportData().add(rd3);
        return sqlResult;
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
    public void buildRreports_nullReportDataList() throws Exception {
        // given
        List<ReportData> reportData = null;

        // when
        RDOSupplierRevenueShareReports supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, reportData, null, null, null).buildReports();

        // then
        assertNotNull(supplierReport);
        assertTrue(supplierReport.getReports().isEmpty());
    }

    @Test
    public void buildReports_emptyReportDataList() throws Exception {
        // given
        List<ReportData> reportData = new ArrayList<PartnerRevenueDao.ReportData>();

        // when
        RDOSupplierRevenueShareReports supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, reportData, null, null, null).buildReports();

        // then
        assertNotNull(supplierReport);
        assertTrue(supplierReport.getReports().isEmpty());
    }

    @Test
    public void buildReports() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenMultipleReportData();

        // when
        RDOSupplierRevenueShareReports supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildReports();

        // then
        assertEquals(3, supplierReport.getReports().size());
        assertTrue(supplierReport.getServerTimeZone().startsWith("UTC"));
        assertEquals("name1 (id1)", supplierReport.getReports().get(0)
                .getSupplier());
        assertEquals(2, supplierReport.getReports().get(0).getCurrencies()
                .size());
        assertEquals("name2 (id2)", supplierReport.getReports().get(1)
                .getSupplier());
        assertEquals("name3 (id3)", supplierReport.getReports().get(2)
                .getSupplier());
        assertEquals(1, supplierReport.getReports().get(1).getCurrencies()
                .size());
    }

    @Test
    public void buildSingleReport_nullReportDataList() throws Exception {
        // given
        List<ReportData> reportData = null;

        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, reportData, null, null, null)
                .buildSingleReport();

        // then
        assertNotNull(supplierReport);
        assertTrue(supplierReport.getCurrencies().isEmpty());
    }

    @Test
    public void buildSingleReport_emptyReportDataList() throws Exception {
        // given
        List<ReportData> reportData = new ArrayList<PartnerRevenueDao.ReportData>();

        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, reportData, null, null, null)
                .buildSingleReport();

        // then
        assertNotNull(supplierReport);
        assertTrue(supplierReport.getCurrencies().isEmpty());
    }

    @Test
    public void buildSingleReport_header() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData("address", "name",
                "cb1a8642",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"),
                "javares/SupplierRevenueShare.xml");

        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        assertEquals("name (cb1a8642)", supplierReport.getSupplier());
        assertEquals("2012-06-01 00:00:00", supplierReport.getPeriodStart());
        assertEquals("2012-07-01 00:00:00", supplierReport.getPeriodEnd());
        assertEquals("address", supplierReport.getAddress());
        assertTrue(supplierReport.getServerTimeZone().startsWith("UTC"));
    }

    @Test
    public void buildSingleReport3_header() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData("address", "name",
                "cb1a8642",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"),
                "javares/SupplierRevenueShare3.xml");

        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        assertEquals("name (cb1a8642)", supplierReport.getSupplier());
        assertEquals("2012-06-01 00:00:00", supplierReport.getPeriodStart());
        assertEquals("2012-07-01 00:00:00", supplierReport.getPeriodEnd());
        assertEquals("address", supplierReport.getAddress());
    }

    @Test
    public void buildSingleReport_directRevenueShareSummary_opShare()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> eurDirectSummaries = report
                .getCurrencies().get(0).getDirectRevenueSummaries();
        assertEquals(
                "5.00%",
                getRevenueShareSummaries(eurDirectSummaries,
                        "direct_mode_service").getOperatorRevenuePercentage());
        assertEquals(
                "30.00",
                getRevenueShareSummaries(eurDirectSummaries,
                        "direct_mode_service").getOperatorRevenue());
        assertEquals(
                "8.00%",
                getRevenueShareSummaries(eurDirectSummaries,
                        "direct_mode_service2").getOperatorRevenuePercentage());
        assertEquals(
                "4.80",
                getRevenueShareSummaries(eurDirectSummaries,
                        "direct_mode_service2").getOperatorRevenue());

        List<RDORevenueShareSummary> usdDirectSummaries = report
                .getCurrencies().get(1).getDirectRevenueSummaries();
        assertEquals(
                "10.00%",
                getRevenueShareSummaries(usdDirectSummaries, "templateService4")
                        .getOperatorRevenuePercentage());
        assertEquals(
                "0.00",
                getRevenueShareSummaries(usdDirectSummaries, "templateService4")
                        .getOperatorRevenue());
    }

    private RDORevenueShareSummary getRevenueShareSummaries(
            List<RDORevenueShareSummary> details, String serviceId) {
        for (RDORevenueShareSummary d : details) {
            if (d.getService().contains(serviceId)) {
                return d;
            }
        }
        return null;
    }

    private RDORevenueShareSummary getRevenueShareSummariesByMarketplaceId(
            List<RDORevenueShareSummary> details, String marketplace) {
        marketplace = marketplaceNameMap.get(marketplace);
        for (RDORevenueShareSummary d : details) {
            if (d.getMarketplace().contains(marketplace)) {
                return d;
            }
        }
        return null;
    }

    @Test
    public void buildSingleReport_directRevenueShareDetails_opShare()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareDetail> eurDirectDetails = report.getCurrencies()
                .get(0).getDirectRevenueShareDetails();
        assertEquals(4, eurDirectDetails.size());
        RDORevenueShareDetail detail = getRevenueShareDetail(eurDirectDetails,
                "f278913568", "direct_mode_service");
        assertEquals("20.00", detail.getOperatorRevenue());
        assertEquals("5.00%", detail.getOperatorRevenuePercentage());
        detail = getRevenueShareDetail(eurDirectDetails, "h285673122",
                "direct_mode_service");
        assertEquals("10.00", detail.getOperatorRevenue());
        assertEquals("5.00%", detail.getOperatorRevenuePercentage());

        detail = getRevenueShareDetail(eurDirectDetails, "f278913568",
                "direct_mode_service2");
        assertEquals("3.20", detail.getOperatorRevenue());
        assertEquals("8.00%", detail.getOperatorRevenuePercentage());
        detail = getRevenueShareDetail(eurDirectDetails, "h285673122",
                "direct_mode_service2");
        assertEquals("1.60", detail.getOperatorRevenue());
        assertEquals("8.00%", detail.getOperatorRevenuePercentage());
    }

    private RDORevenueShareDetail getRevenueShareDetail(
            List<RDORevenueShareDetail> details, String customerId,
            String serviceId) {
        for (RDORevenueShareDetail d : details) {
            if (d.getCustomer().contains(customerId)
                    && d.getService().equals(serviceId)) {
                return d;
            }
        }
        return null;
    }

    @Test
    public void buildSingleReport_brokerRevenueShareSummary_opShare()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> eurBrokerSummaries = report
                .getCurrencies().get(0).getBrokerRevenueSummaries();
        RDORevenueShareSummary rss = getRevenueShareSummariesByMarketplaceId(
                eurBrokerSummaries, "mp1");
        assertEquals("5.00%", rss.getOperatorRevenuePercentage());
        assertEquals("35.00", rss.getOperatorRevenue());
        rss = getRevenueShareSummariesByMarketplaceId(eurBrokerSummaries, "mp2");
        assertEquals("10.00%", rss.getOperatorRevenuePercentage());
        assertEquals("7.00", rss.getOperatorRevenue());

        List<RDORevenueShareSummary> usdDirectSummaries = report
                .getCurrencies().get(1).getBrokerRevenueSummaries();
        rss = getRevenueShareSummariesByMarketplaceId(usdDirectSummaries, "mp3");
        assertEquals("10.00%", rss.getOperatorRevenuePercentage());
        assertEquals("0.20", rss.getOperatorRevenue());
    }

    @Test
    public void brokerRevenueShareSummary_sumUpServicesOfOneBroker()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData();
        ReportData rd = sqlResult.getReportData().get(0);
        rd.setResultXml(rd.getResultXml().replaceAll("id=\"broker2\"",
                "id=\"broker1\""));

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> eurBrokerSummaries = report
                .getCurrencies().get(0).getBrokerRevenueSummaries();
        RDORevenueShareSummary rss = getRevenueShareSummariesByMarketplaceId(
                eurBrokerSummaries, "mp1");
        assertEquals("NameBroker1 (broker1)", rss.getPartner());
        assertEquals("0.00%", rss.getOperatorRevenuePercentage());
        assertEquals("0.00", rss.getOperatorRevenue());
        assertEquals("20.00%", rss.getMarketplaceRevenuePercentage());
        assertEquals("260.00", rss.getMarketplaceRevenue());
        assertEquals("10.00", rss.getPartnerProvisionPercentage());
        assertEquals("280.00", rss.getPartnerProvision());
        assertEquals("760.00", rss.getRevenueMinusShares());
        assertEquals("1300.00", rss.getRevenue());

        rss = eurBrokerSummaries.get(1);
        assertEquals("NameBroker3 (broker3)", rss.getPartner());
        assertEquals("0.00%", rss.getOperatorRevenuePercentage());
        assertEquals("0.00", rss.getOperatorRevenue());
        assertEquals("10.00%", rss.getMarketplaceRevenuePercentage());
        assertEquals("9.00", rss.getMarketplaceRevenue());
        assertEquals("80.00", rss.getPartnerProvisionPercentage());
        assertEquals("72.00", rss.getPartnerProvision());
        assertEquals("9.00", rss.getRevenueMinusShares());
        assertEquals("90.00", rss.getRevenue());

        rss = getRevenueShareSummariesByMarketplaceId(eurBrokerSummaries, "mp2");
        assertEquals("NameBroker1 (broker1)", rss.getPartner());
        assertEquals("0.00%", rss.getOperatorRevenuePercentage());
        assertEquals("0.00", rss.getOperatorRevenue());
        assertEquals("20.00%", rss.getMarketplaceRevenuePercentage());
        assertEquals("80.00", rss.getMarketplaceRevenue());
        assertEquals("60.00", rss.getPartnerProvisionPercentage());
        assertEquals("240.00", rss.getPartnerProvision());
        assertEquals("80.00", rss.getRevenueMinusShares());
        assertEquals("400.00", rss.getRevenue());

        List<RDORevenueShareSummary> usdDirectSummaries = report
                .getCurrencies().get(1).getBrokerRevenueSummaries();
        rss = getRevenueShareSummariesByMarketplaceId(usdDirectSummaries, "mp3");
        assertEquals("NameBroker4 (broker4)", rss.getPartner());
        assertEquals("0.00%", rss.getOperatorRevenuePercentage());
        assertEquals("0.00", rss.getOperatorRevenue());
        assertEquals("50.00%", rss.getMarketplaceRevenuePercentage());
        assertEquals("1.00", rss.getMarketplaceRevenue());
        assertEquals("60.00", rss.getPartnerProvisionPercentage());
        assertEquals("1.20", rss.getPartnerProvision());
        assertEquals("-0.20", rss.getRevenueMinusShares());
        assertEquals("2.00", rss.getRevenue());
    }

    @Test
    public void brokerRevenueShareSummary_sumUpServicesOfOneBroker2()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData();
        ReportData rd = sqlResult.getReportData().get(0);
        rd.setResultXml(rd.getResultXml().replaceAll("id=\"broker3\"",
                "id=\"broker2\""));

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> eurBrokerSummaries = report
                .getCurrencies().get(0).getBrokerRevenueSummaries();
        RDORevenueShareSummary rss = getRevenueShareSummariesByMarketplaceId(
                eurBrokerSummaries, "mp1");
        assertEquals("NameBroker1 (broker1)", rss.getPartner());
        assertEquals("0.00%", rss.getOperatorRevenuePercentage());
        assertEquals("0.00", rss.getOperatorRevenue());
        assertEquals("20.00%", rss.getMarketplaceRevenuePercentage());
        assertEquals("100.00", rss.getMarketplaceRevenue());
        assertEquals("40.00", rss.getPartnerProvisionPercentage());
        assertEquals("200.00", rss.getPartnerProvision());
        assertEquals("200.00", rss.getRevenueMinusShares());
        assertEquals("500.00", rss.getRevenue());

        rss = eurBrokerSummaries.get(1);
        assertEquals("NameBroker2 (broker2)", rss.getPartner());
        assertEquals("0.00%", rss.getOperatorRevenuePercentage());
        assertEquals("0.00", rss.getOperatorRevenue());
        assertEquals("10.00%", rss.getMarketplaceRevenuePercentage());
        assertEquals("169.00", rss.getMarketplaceRevenue());
        assertEquals("80.00", rss.getPartnerProvisionPercentage());
        assertEquals("152.00", rss.getPartnerProvision());
        assertEquals("569.00", rss.getRevenueMinusShares());
        assertEquals("890.00", rss.getRevenue());

        rss = getRevenueShareSummariesByMarketplaceId(eurBrokerSummaries, "mp2");
        assertEquals("NameBroker1 (broker1)", rss.getPartner());
        assertEquals("0.00%", rss.getOperatorRevenuePercentage());
        assertEquals("0.00", rss.getOperatorRevenue());
        assertEquals("20.00%", rss.getMarketplaceRevenuePercentage());
        assertEquals("80.00", rss.getMarketplaceRevenue());
        assertEquals("60.00", rss.getPartnerProvisionPercentage());
        assertEquals("240.00", rss.getPartnerProvision());
        assertEquals("80.00", rss.getRevenueMinusShares());
        assertEquals("400.00", rss.getRevenue());

        List<RDORevenueShareSummary> usdDirectSummaries = report
                .getCurrencies().get(1).getBrokerRevenueSummaries();
        rss = getRevenueShareSummariesByMarketplaceId(usdDirectSummaries, "mp3");
        assertEquals("NameBroker4 (broker4)", rss.getPartner());
        assertEquals("0.00%", rss.getOperatorRevenuePercentage());
        assertEquals("0.00", rss.getOperatorRevenue());
        assertEquals("50.00%", rss.getMarketplaceRevenuePercentage());
        assertEquals("1.00", rss.getMarketplaceRevenue());
        assertEquals("60.00", rss.getPartnerProvisionPercentage());
        assertEquals("1.20", rss.getPartnerProvision());
        assertEquals("-0.20", rss.getRevenueMinusShares());
        assertEquals("2.00", rss.getRevenue());
    }

    @Test
    public void buildSingleReport_brokerRevenueShareDetails_opShare()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareDetail> eurBrokerDetails = report.getCurrencies()
                .get(0).getBrokerRevenueShareDetails();
        RDORevenueShareDetail detail = getRevenueShareDetail(eurBrokerDetails,
                "a92537743", "broker_mode_service");
        assertEquals("35.00", detail.getOperatorRevenue());
        assertEquals("5.00%", detail.getOperatorRevenuePercentage());

        detail = getRevenueShareDetail(eurBrokerDetails, "a92537743",
                "broker_mode_service2");
        assertEquals("7.00", detail.getOperatorRevenue());
        assertEquals("10.00%", detail.getOperatorRevenuePercentage());
    }

    @Test
    public void buildSingleReport_resellerRevenueShareSummary_opShare()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> eurResellerSummaries = report
                .getCurrencies().get(0).getResellerRevenueSummaries();
        RDORevenueShareSummary rss = getRevenueShareSummariesByMarketplaceId(
                eurResellerSummaries, "mp1");
        assertEquals("10.00%", rss.getOperatorRevenuePercentage());
        assertEquals("80.00", rss.getOperatorRevenue());
        rss = getRevenueShareSummariesByMarketplaceId(eurResellerSummaries,
                "mp2");
        assertEquals("10.00%", rss.getOperatorRevenuePercentage());
        assertEquals("8.00", rss.getOperatorRevenue());

        List<RDORevenueShareSummary> usdResellerSummaries = report
                .getCurrencies().get(1).getResellerRevenueSummaries();
        rss = getRevenueShareSummariesByMarketplaceId(usdResellerSummaries,
                "mp3");
        assertEquals("10.00%", rss.getOperatorRevenuePercentage());
        assertEquals("1.30", rss.getOperatorRevenue());
    }

    @Test
    public void buildSingleReport_resellerRevenueShareDetails_opShare()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareDetail> eurResellerDetails = report.getCurrencies()
                .get(0).getResellerRevenueShareDetails();
        RDORevenueShareDetail detail = getRevenueShareDetail(
                eurResellerDetails, "r12957322", "reseller_mode_service");
        assertEquals("80.00", detail.getOperatorRevenue());
        assertEquals("10.00%", detail.getOperatorRevenuePercentage());

        detail = getRevenueShareDetail(eurResellerDetails, "r12957322",
                "reseller_mode_service2");
        assertEquals("8.00", detail.getOperatorRevenue());
        assertEquals("10.00%", detail.getOperatorRevenuePercentage());
    }

    @Test
    public void buildSingleReport_directRevenueShareSummary_revShareAmount()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> eurDirectSummaries = report
                .getCurrencies().get(0).getDirectRevenueSummaries();
        assertEquals(2, eurDirectSummaries.size());
        RDORevenueShareSummary summary = getRevenueShareSummaries(
                eurDirectSummaries, "direct_mode_service");
        assertEquals("480.00", summary.getRevenueMinusShares());
        summary = getRevenueShareSummaries(eurDirectSummaries,
                "direct_mode_service2");
        assertEquals("46.20", summary.getRevenueMinusShares());
    }

    @Test
    public void buildSingleReport_directRevenueShareSummary_revShareAmount_oldKeyName()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData5();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> usdDirectSummaries = report
                .getCurrencies().get(1).getDirectRevenueSummaries();
        assertEquals(2, usdDirectSummaries.size());
        RDORevenueShareSummary summary = getRevenueShareSummaries(
                usdDirectSummaries, "direct_mode_service");
        assertEquals("480.00", summary.getRevenueMinusShares());
        summary = getRevenueShareSummaries(usdDirectSummaries,
                "direct_mode_service2");
        assertEquals("46.20", summary.getRevenueMinusShares());
    }

    @Test
    public void buildSingleReport_directRevenueShareDetails_revShareAmount()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareDetail> eurDirectDetails = report.getCurrencies()
                .get(0).getDirectRevenueShareDetails();
        RDORevenueShareDetail detail = getRevenueShareDetail(eurDirectDetails,
                "f278913568", "direct_mode_service");
        assertEquals("320.00", detail.getRevenueMinusShares());
        detail = getRevenueShareDetail(eurDirectDetails, "h285673122",
                "direct_mode_service");
        assertEquals("175.00", detail.getRevenueMinusShares());
    }

    @Test
    public void buildSingleReport_brokerRevenueShareSummary_revShareAmount()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> eurBrokerSummaries = report
                .getCurrencies().get(0).getBrokerRevenueSummaries();
        assertEquals(2, eurBrokerSummaries.size());
        RDORevenueShareSummary summary = getRevenueShareSummariesByMarketplaceId(
                eurBrokerSummaries, "mp1");
        assertEquals("525.00", summary.getRevenueMinusShares());
        summary = getRevenueShareSummariesByMarketplaceId(eurBrokerSummaries,
                "mp2");
        assertEquals("49.00", summary.getRevenueMinusShares());
    }

    @Test
    public void buildSingleReport_brokerRevenueShareSummary_revShareAmount_oldKeyName()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData5();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> usdBrokerSummaries = report
                .getCurrencies().get(1).getBrokerRevenueSummaries();
        assertEquals(2, usdBrokerSummaries.size());
        RDORevenueShareSummary summary = getRevenueShareSummariesByMarketplaceId(
                usdBrokerSummaries, "mp1");
        assertEquals("525.00", summary.getRevenueMinusShares());
        summary = getRevenueShareSummariesByMarketplaceId(usdBrokerSummaries,
                "mp2");
        assertEquals("49.00", summary.getRevenueMinusShares());
    }

    @Test
    public void buildSingleReport_brokerRevenueShareDetails_revShareAmount()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareDetail> eurBrokerDetails = report.getCurrencies()
                .get(0).getBrokerRevenueShareDetails();
        RDORevenueShareDetail detail = getRevenueShareDetail(eurBrokerDetails,
                "a92537743", "broker_mode_service");
        assertEquals("525.00", detail.getRevenueMinusShares());
        detail = getRevenueShareDetail(eurBrokerDetails, "a92537743",
                "broker_mode_service2");
        assertEquals("56.00", detail.getRevenueMinusShares());
    }

    @Test
    public void buildSingleReport_resellerRevenueShareSummary_revShareAmount()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> eurResellerSummaries = report
                .getCurrencies().get(0).getResellerRevenueSummaries();
        assertEquals(2, eurResellerSummaries.size());
        RDORevenueShareSummary summary = getRevenueShareSummariesByMarketplaceId(
                eurResellerSummaries, "mp1");
        assertEquals("520.00", summary.getRevenueMinusShares());
        summary = getRevenueShareSummariesByMarketplaceId(eurResellerSummaries,
                "mp2");
        assertEquals("59.00", summary.getRevenueMinusShares());
    }

    @Test
    public void buildSingleReport_resellerRevenueShareSummary_revShareAmount_oldKeyName()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData5();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareSummary> usdResellerSummaries = report
                .getCurrencies().get(1).getResellerRevenueSummaries();
        assertEquals(2, usdResellerSummaries.size());
        RDORevenueShareSummary summary = getRevenueShareSummariesByMarketplaceId(
                usdResellerSummaries, "mp1");
        assertEquals("520.00", summary.getRevenueMinusShares());
        summary = getRevenueShareSummariesByMarketplaceId(usdResellerSummaries,
                "mp2");
        assertEquals("52.00", summary.getRevenueMinusShares());
    }

    @Test
    public void buildSingleReport_resellerRevenueShareDetails_revShareAmount()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        List<RDORevenueShareDetail> eurResellerDetails = report.getCurrencies()
                .get(0).getResellerRevenueShareDetails();
        RDORevenueShareDetail detail = getRevenueShareDetail(
                eurResellerDetails, "r12957322", "reseller_mode_service");
        assertEquals("600.00", detail.getRevenueMinusShares());
        detail = getRevenueShareDetail(eurResellerDetails, "r12957322",
                "reseller_mode_service2");
        assertEquals("60.00", detail.getRevenueMinusShares());
    }

    @Test
    public void buildSingleReport_overview_direct() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        RDOSupplierRevenueShareCurrency eur = report.getCurrencies().get(0);
        assertEquals("34.80", eur.getDirectProvisionToOperator());
        assertEquals("542.70", eur.getDirectTotalRemainingRevenue());

        RDOSupplierRevenueShareCurrency usd = report.getCurrencies().get(1);
        assertEquals("0.00", usd.getDirectProvisionToOperator());
        assertEquals("0.99", usd.getDirectTotalRemainingRevenue());
    }

    @Test
    public void buildSingleReport_overview_broker() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        RDOSupplierRevenueShareCurrency eur = report.getCurrencies().get(0);
        assertEquals("42.00", eur.getBrokerProvisionToOperator());
        assertEquals("618.00", eur.getBrokerTotalRemainingRevenue());

        RDOSupplierRevenueShareCurrency usd = report.getCurrencies().get(1);
        assertEquals("0.20", usd.getBrokerProvisionToOperator());
        assertEquals("-0.40", usd.getBrokerTotalRemainingRevenue());
    }

    @Test
    public void buildSingleReport_overview_reseller() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport report = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        RDOSupplierRevenueShareCurrency eur = report.getCurrencies().get(0);
        assertEquals("88.00", eur.getResellerProvisionToOperator());
        assertEquals("689.50", eur.getResellerTotalRemainingRevenue());

        RDOSupplierRevenueShareCurrency usd = report.getCurrencies().get(1);
        assertEquals("1.30", usd.getResellerProvisionToOperator());
        assertEquals("-3.40", usd.getResellerTotalRemainingRevenue());
    }

    @Test
    public void buildSingleReport_overview() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData();

        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then
        assertEquals(2, supplierReport.getCurrencies().size());
        RDOSupplierRevenueShareCurrency currencyEUR = supplierReport
                .getCurrencies().get(0);
        assertEquals("EUR", currencyEUR.getCurrency());
        assertEquals("4091.00", currencyEUR.getTotalRevenue());
        assertEquals("1790.00", currencyEUR.getBrokerTotalRevenue());
        assertEquals("43.75%", currencyEUR.getBrokerPercentageRevenue());
        assertEquals("1700.00", currencyEUR.getResellerTotalRevenue());
        assertEquals("41.55%", currencyEUR.getResellerPercentageRevenue());
        assertEquals("349.00",
                currencyEUR.getBrokerProvisionToMarketplaceOwner());
        assertEquals("1010.00",
                currencyEUR.getResellerProvisionToMarketplaceOwner());
        assertEquals("120.01",
                currencyEUR.getDirectProvisionToMarketplaceOwner());

        RDOSupplierRevenueShareCurrency currencyUSD = supplierReport
                .getCurrencies().get(1);
        assertEquals("USD", currencyUSD.getCurrency());
        assertEquals("16.00", currencyUSD.getTotalRevenue());
        assertEquals("2.00", currencyUSD.getBrokerTotalRevenue());
        assertEquals("12.50%", currencyUSD.getBrokerPercentageRevenue());
        assertEquals("13.00", currencyUSD.getResellerTotalRevenue());
        assertEquals("81.25%", currencyUSD.getResellerPercentageRevenue());
        assertEquals("349.00",
                currencyEUR.getBrokerProvisionToMarketplaceOwner());
        assertEquals("1010.00",
                currencyEUR.getResellerProvisionToMarketplaceOwner());
        assertEquals("120.01",
                currencyEUR.getDirectProvisionToMarketplaceOwner());

    }

    @Test
    public void buildSingleReport_overview3() throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();
        // then
        assertEquals(2, supplierReport.getCurrencies().size());
        RDOSupplierRevenueShareCurrency currencyEUR = supplierReport
                .getCurrencies().get(0);
        assertEquals("EUR", currencyEUR.getCurrency());
        assertEquals("1850.20", currencyEUR.getTotalRemainingRevenue());
        assertEquals("2310.00", currencyEUR.getTotalRevenue());
        assertEquals("770.00", currencyEUR.getBrokerTotalRevenue());
        assertEquals("33.33%", currencyEUR.getBrokerPercentageRevenue());
        assertEquals("880.00", currencyEUR.getResellerTotalRevenue());
        assertEquals("38.10%", currencyEUR.getResellerPercentageRevenue());
        assertEquals("99.00",
                currencyEUR.getDirectProvisionToMarketplaceOwner());
        assertEquals("115.50",
                currencyEUR.getBrokerProvisionToMarketplaceOwner());
        assertEquals("132.00",
                currencyEUR.getResellerProvisionToMarketplaceOwner());

        RDOSupplierRevenueShareCurrency currencyUSD = supplierReport
                .getCurrencies().get(1);
        assertEquals("USD", currencyUSD.getCurrency());
        assertEquals("-2.81", currencyUSD.getTotalRemainingRevenue());
        assertEquals("16.00", currencyUSD.getTotalRevenue());
        assertEquals("2.00", currencyUSD.getBrokerTotalRevenue());
        assertEquals("12.50%", currencyUSD.getBrokerPercentageRevenue());
        assertEquals("13.00", currencyUSD.getResellerTotalRevenue());
        assertEquals("81.25%", currencyUSD.getResellerPercentageRevenue());
        assertEquals("1.00", currencyUSD.getBrokerProvisionToMarketplaceOwner());
    }

    @Test
    public void buildSingleReport_buildRevenueSummaries() throws Exception { // given
        PartnerRevenueDao sqlResult = givenReportData3();

        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        // then

        // currency: EUR, 2 marketplaces with 2 direct/broker/reseller services
        // each
        RDOSupplierRevenueShareCurrency currencyEUR = supplierReport
                .getCurrencies().get(0);
        assertEquals(2, currencyEUR.getDirectRevenueSummaries().size());
        assertEquals(2, currencyEUR.getBrokerRevenueSummaries().size());
        assertEquals(2, currencyEUR.getResellerRevenueSummaries().size());

        RDORevenueShareSummary eu_drs1 = currencyEUR
                .getDirectRevenueSummaries().get(0);
        assertEquals("EUR", eu_drs1.getCurrency());
        assertEquals("marketplaceName1", eu_drs1.getMarketplace());
        assertEquals("90.00", eu_drs1.getMarketplaceRevenue());
        assertEquals("15.00%", eu_drs1.getMarketplaceRevenuePercentage());
        assertEquals("600.00", eu_drs1.getRevenue());

        RDORevenueShareSummary us_drs2 = currencyEUR
                .getDirectRevenueSummaries().get(1);
        assertEquals("EUR", us_drs2.getCurrency());
        assertEquals("marketplaceName2", us_drs2.getMarketplace());
        assertEquals("9.00", us_drs2.getMarketplaceRevenue());
        assertEquals("15.00%", us_drs2.getMarketplaceRevenuePercentage());
        assertEquals("60.00", us_drs2.getRevenue());

        // currency: USD, 1 marketplace
        RDOSupplierRevenueShareCurrency currencyUSD = supplierReport
                .getCurrencies().get(1);
        assertEquals(1, currencyUSD.getDirectRevenueSummaries().size());
        assertEquals(1, currencyUSD.getBrokerRevenueSummaries().size());
        assertEquals(1, currencyUSD.getResellerRevenueSummaries().size());

        RDORevenueShareSummary brs1 = currencyUSD.getBrokerRevenueSummaries()
                .get(0);
        assertEquals("USD", brs1.getCurrency());

        assertEquals("MarktplatzName3", brs1.getMarketplace());
        assertEquals("1.00", brs1.getMarketplaceRevenue());
        assertEquals("50.00%", brs1.getMarketplaceRevenuePercentage());
    }

    @Test
    public void buildRevenueShareDetail_directNewXML() throws Exception {
        PartnerRevenueDao sqlResult = givenReportData3();
        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        RDOSupplierRevenueShareCurrency currencyEUR = supplierReport
                .getCurrencies().get(0);

        List<RDORevenueShareDetail> directDetails = currencyEUR
                .getDirectRevenueShareDetails();

        assertEquals(4, directDetails.size());

        RDORevenueShareDetail revenueDetail = directDetails.get(0);

        assertEquals("EUR", revenueDetail.getCurrency());
        assertEquals("customerA (f278913568)", revenueDetail.getCustomer());
        assertEquals("marketplaceName1", revenueDetail.getMarketplace());
        assertEquals("60.00", revenueDetail.getMarketplaceRevenue());
        assertEquals("15.00%", revenueDetail.getMarketplaceSharePercentage());
        assertEquals("", revenueDetail.getPartnerRevenue());
        assertEquals("", revenueDetail.getPartnerSharePercentage());
        assertEquals("400.00", revenueDetail.getRevenue());
        assertEquals("direct_mode_service", revenueDetail.getService());
    }

    @Test
    public void buildRevenueShareDetail_brokerNewXML() throws Exception {
        PartnerRevenueDao sqlResult = givenReportData3();
        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        RDOSupplierRevenueShareCurrency currencyEUR = supplierReport
                .getCurrencies().get(0);

        List<RDORevenueShareDetail> brokerDetails = currencyEUR
                .getBrokerRevenueShareDetails();

        assertEquals(2, brokerDetails.size());

        RDORevenueShareDetail revenueDetail = brokerDetails.get(0);

        assertEquals("EUR", revenueDetail.getCurrency());
        assertEquals("customerC (a92537743)", revenueDetail.getCustomer());
        assertEquals("marketplaceName1", revenueDetail.getMarketplace());
        assertEquals("105.00", revenueDetail.getMarketplaceRevenue());
        assertEquals("15.00%", revenueDetail.getMarketplaceSharePercentage());
        assertEquals("35.00", revenueDetail.getPartnerRevenue());
        assertEquals("5.00%", revenueDetail.getPartnerSharePercentage());
        assertEquals("700.00", revenueDetail.getRevenue());
        assertEquals("broker_mode_service", revenueDetail.getService());
    }

    @Test
    public void buildRevenueShareDetail_resellerNewXML() throws Exception {
        PartnerRevenueDao sqlResult = givenReportData3();
        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        RDOSupplierRevenueShareCurrency currencyEUR = supplierReport
                .getCurrencies().get(0);

        List<RDORevenueShareDetail> resellerDetails = currencyEUR
                .getResellerRevenueShareDetails();

        assertEquals(2, resellerDetails.size());

        RDORevenueShareDetail revenueDetail = resellerDetails.get(0);

        assertEquals("EUR", revenueDetail.getCurrency());
        assertEquals("customerD (r12957322)", revenueDetail.getCustomer());
        assertEquals("marketplaceName1", revenueDetail.getMarketplace());
        assertEquals("120.00", revenueDetail.getMarketplaceRevenue());
        assertEquals("15.00%", revenueDetail.getMarketplaceSharePercentage());
        assertEquals("80.00", revenueDetail.getPartnerRevenue());
        assertEquals("10.00%", revenueDetail.getPartnerSharePercentage());
        assertEquals("800.00", revenueDetail.getRevenue());
        assertEquals("reseller_mode_service", revenueDetail.getService());
    }

    // no RevenueShareDetail included in the xml
    @Test
    public void buildRevenueShareDetail_resellerOldXML() throws Exception {
        PartnerRevenueDao sqlResult = givenReportData3();
        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        RDOSupplierRevenueShareCurrency currencyUSD = supplierReport
                .getCurrencies().get(1);

        List<RDORevenueShareDetail> resellerDetails = currencyUSD
                .getResellerRevenueShareDetails();

        assertEquals(0, resellerDetails.size());

    }

    @Test
    public void createOrganizationDisplayName_OrganizationNameNotExisting() {
        // given
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                null, null, null, null, null);

        // when
        String displayName = builder.createOrganizationDisplayName("id");

        // then
        assertEquals("id", displayName);
    }

    @Test
    public void createOrganizationDisplayName_OrganizationNameExisting() {
        // given
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                null, null, null, null, null);
        builder.organizationNameMap.put("id", "name");

        // when
        String displayName = builder.createOrganizationDisplayName("id");

        // then
        assertEquals("name (id)", displayName);
    }

    @Test
    public void computePercentage() throws Exception {
        // given
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                null, null, null, null, null);

        // when
        String percentage = builder.computePercentage("100.00", "10.00");

        // then
        assertEquals("10.00%", percentage);
    }

    @Test
    public void computePercentage_zeroTotalValue() throws Exception {
        // given
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                null, null, null, null, null);

        // when
        String percentage = builder.computePercentage("0.00", "10.00");

        // then
        assertEquals("0.00%", percentage);
    }

    @Test
    public void computePercentage_zeroValue() throws Exception {
        // given
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                null, null, null, null, null);

        // when
        String percentage = builder.computePercentage("100.00", "0.00");

        // then
        assertEquals("0.00%", percentage);
    }

    @Test
    public void computePercentage_valueGreater() throws Exception {
        // given
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                null, null, null, null, null);

        // when
        String percentage = builder.computePercentage("10.00", "100.00");

        // then
        assertEquals("1000.00%", percentage);
    }

    @Test(expected = NullPointerException.class)
    public void isServiceRevenueValid_serviceRevenueIsNull() throws Exception {
        // given
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                null, null, null, null, null);
        String serviceRevenue = null;

        // when
        builder.isServiceRevenueValid(serviceRevenue);
    }

    @Test
    public void isServiceRevenueValid_serviceRevenueIsZero() throws Exception {
        // given
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                null, null, null, null, null);
        String serviceRevenue = "0.00";

        // when
        boolean revenueValid = builder.isServiceRevenueValid(serviceRevenue);

        // then
        assertFalse(revenueValid);
    }

    @Test
    public void isServiceRevenueValid_serviceRevenueIsGreaterThanZero()
            throws Exception {
        // given
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                null, null, null, null, null);
        String serviceRevenue = "0.01";

        // when
        boolean revenueValid = builder.isServiceRevenueValid(serviceRevenue);

        // then
        assertTrue(revenueValid);
    }

    @Test
    public void buildReports_summary_serviceWithZeroAmount() throws Exception {
        // given
        PartnerRevenueDao reportData = givenReportData("address", "name", "id",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"),
                "javares/SupplierRevenueShare4.xml");
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, reportData.getReportData(), null,
                marketplaceNameMap, serviceNameMap);

        // when
        RDOSupplierRevenueShareReports report = builder.buildReports();

        // then
        List<RDORevenueShareSummary> services = report.getReports().get(0)
                .getCurrencies().get(0).getDirectRevenueSummaries();
        assertEquals(1, services.size());
        assertEquals("600.00", services.get(0).getRevenue());
    }

    @Test
    public void buildReports_shareDetails_serviceWithZeroAmount()
            throws Exception {
        // given
        PartnerRevenueDao reportData = givenReportData("address", "name", "id",
                DateTimeHandling.calculateMillis("2012-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-07-01 00:00:00"),
                "javares/SupplierRevenueShare4.xml");
        SupplierRevenueShareBuilder builder = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, reportData.getReportData(), null,
                marketplaceNameMap, serviceNameMap);

        // when
        RDOSupplierRevenueShareReports report = builder.buildReports();

        // then
        List<RDORevenueShareDetail> shareDetails = report.getReports().get(0)
                .getCurrencies().get(0).getDirectRevenueShareDetails();
        assertEquals(2, shareDetails.size());
        assertEquals("400.00", shareDetails.get(0).getRevenue());
        assertEquals("200.00", shareDetails.get(1).getRevenue());
    }

    @Test
    public void buildSingleReport_computeRemainingRevenue_newXml()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData5();
        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        RDOSupplierRevenueShareCurrency currencyEUR = supplierReport
                .getCurrencies().get(0);

        // then
        assertEquals("EUR", currencyEUR.getCurrency());
        assertEquals("526.20", currencyEUR.getDirectTotalRemainingRevenue());
        assertEquals("574.00", currencyEUR.getBrokerTotalRemainingRevenue());
        assertEquals("572.00", currencyEUR.getResellerTotalRemainingRevenue());
        assertEquals("1672.20", currencyEUR.getTotalRemainingRevenue());
    }

    @Test
    public void buildSingleReport_computeRemainingRevenue_oldXml()
            throws Exception {
        // given
        PartnerRevenueDao sqlResult = givenReportData5();

        // when
        RDOSupplierRevenueShareReport supplierReport = new SupplierRevenueShareBuilder(
                Locale.ENGLISH, sqlResult.getReportData(), serviceIdMap,
                marketplaceNameMap, serviceNameMap).buildSingleReport();

        RDOSupplierRevenueShareCurrency currencyUSD = supplierReport
                .getCurrencies().get(1);

        // then
        assertEquals("USD", currencyUSD.getCurrency());
        assertEquals("526.20", currencyUSD.getDirectTotalRemainingRevenue());
        assertEquals("574.00", currencyUSD.getBrokerTotalRemainingRevenue());
        assertEquals("572.00", currencyUSD.getResellerTotalRemainingRevenue());
        assertEquals("1672.20", currencyUSD.getTotalRemainingRevenue());
    }
}
