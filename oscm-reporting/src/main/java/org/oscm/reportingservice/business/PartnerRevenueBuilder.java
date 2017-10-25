/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 7, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.oscm.converter.DateConverter;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.reportingservice.business.model.RdoIdGenerator;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOCurrency;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReport;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReports;
import org.oscm.reportingservice.business.model.partnerrevenue.RDORevenueDetail;
import org.oscm.reportingservice.business.model.partnerrevenue.RDORevenueDetailService;
import org.oscm.reportingservice.business.model.partnerrevenue.RDORevenueDetailServiceCustomer;
import org.oscm.reportingservice.dao.PartnerRevenueDao.ReportData;
import org.oscm.string.Strings;
import org.oscm.types.constants.BillingShareResultXmlTags;

/**
 * @author kulle
 * 
 */
class PartnerRevenueBuilder {

    private final TimeZone timeZoneServer = TimeZone.getDefault();
    private final List<ReportData> sqlData;
    private Document xmlDocument;
    private String partnerType;
    private final PriceConverter priceConverter;
    private final RdoIdGenerator idGen;

    PartnerRevenueBuilder(Locale locale, List<ReportData> sqlData) {
        super();
        this.sqlData = sqlData;
        priceConverter = new PriceConverter(locale);
        idGen = new RdoIdGenerator();
    }

    public RDOPartnerReports buildReports()
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException, ParseException {
        if (sqlData == null || sqlData.isEmpty()) {
            return new RDOPartnerReports();
        }

        RDOPartnerReports result = new RDOPartnerReports();
        result.setEntryNr(idGen.nextValue());
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());
        for (ReportData data : sqlData) {
            xmlDocument = XMLConverter.convertToDocument(data.getResultXml(),
                    false);
            partnerType = Strings.firstCharToUppercase(data.getResulttype());
            result.getReports().add(build(data, result.getEntryNr()));
        }
        return result;
    }

    public RDOPartnerReport buildSingleReport()
            throws XPathExpressionException, ParserConfigurationException,
            SAXException, IOException, ParseException {
        if (sqlData == null || sqlData.isEmpty()) {
            return new RDOPartnerReport();
        }
        xmlDocument = XMLConverter.convertToDocument(
                sqlData.get(0).getResultXml(), false);
        partnerType = Strings.firstCharToUppercase(sqlData.get(0)
                .getResulttype());
        return build(sqlData.get(0), 0);
    }

    private RDOPartnerReport build(ReportData reportData, int parentEntryNr)
            throws XPathExpressionException, ParseException {
        RDOPartnerReport report = new RDOPartnerReport(parentEntryNr,
                idGen.nextValue());
        report.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());
        report.setVendorType(partnerType.toLowerCase());
        report.setAddress(reportData.getAddress());
        report.setCountryName(reportData.getCountryIsoCode());
        report.setPeriodEnd(DateConverter.convertLongToDateTimeFormat(
                reportData.getPeriodEnd(), timeZoneServer,
                DateConverter.DTP_WITHOUT_MILLIS));
        report.setPeriodStart(DateConverter.convertLongToDateTimeFormat(
                reportData.getPeriodStart(), timeZoneServer,
                DateConverter.DTP_WITHOUT_MILLIS));
        report.setVendor(Formatting.nameAndId(reportData.getName(),
                reportData.getOrganizationId()));
        report.setCurrencies(buildCurrencies(report.getEntryNr()));
        return report;
    }

    private List<RDOCurrency> buildCurrencies(int parentEntryNr)
            throws XPathExpressionException, ParseException {
        List<RDOCurrency> result = new ArrayList<RDOCurrency>();
        for (Node currencyNode : currencyNodes()) {
            RDOCurrency currency = new RDOCurrency(parentEntryNr,
                    idGen.nextValue());
            result.add(currency);
            String currencyId = XMLConverter.getStringAttValue(currencyNode,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_ID);
            currency.setCurrency(currencyId);
            currency.setTotalAmount(getTotalAmount(currencyId));
            currency.setTotalRevenue(readTotalRevenue(currencyId));
            currency.setRemainingAmount(computeRemainingAmount(
                    currency.getTotalAmount(), currency.getTotalRevenue()));
            currency.setBrokerRevenue(readBrokerRevenue(currencyId));
            currency.setRevenueDetails(buildRevenueDetails(currencyId,
                    currency.getEntryNr()));
        }

        return result;
    }

    private List<Node> currencyNodes() throws XPathExpressionException {
        NodeList currencyNodes = XMLConverter.getNodeListByXPath(xmlDocument,
                '/' + partnerType + "RevenueShareResult/Currency");
        return XMLConverter.getNodeList(currencyNodes,
                BillingShareResultXmlTags.NODE_NAME_CURRENCY);
    }

    private String getTotalAmount(String currencyId)
            throws XPathExpressionException {
        if (readTotalAmount(currencyId).isEmpty())
            return computeServiceSum(currencyId);
        else
            return readTotalAmount(currencyId);
    }

    private String readTotalAmount(String currencyId)
            throws XPathExpressionException {
        return priceToDisplay(XMLConverter.getNodeTextContentByXPath(
                xmlDocument, "//Currency[@id='" + currencyId + "']/"
                        + partnerType + "Revenue/@totalAmount"));
    }

    private String computeServiceSum(String currencyId)
            throws XPathExpressionException {
        String path = "//Currency[@id='" + currencyId
                + "']/Supplier/Service/ServiceRevenue/@totalAmount";
        return computeSum(path);
    }

    private String computeSum(String path) throws XPathExpressionException {
        Double sum = XMLConverter.sumup(xmlDocument, path);
        BigDecimal result = BigDecimal.valueOf(sum.doubleValue()).setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        return revenueValueToDisplay(result);
    }

    private String readTotalRevenue(String currencyId)
            throws XPathExpressionException {
        return priceToDisplay(XMLConverter.getNodeTextContentByXPath(
                xmlDocument, "//Currency[@id='" + currencyId + "']/"
                        + partnerType + "Revenue/@amount"));
    }

    private String computeRemainingAmount(String totalAmount,
            String totalRevenue) throws ParseException {
        if ("Reseller".equals(partnerType)) {
            BigDecimal a = priceConverter.parse(totalAmount);
            BigDecimal r = priceConverter.parse(totalRevenue);
            return priceToDisplay(a.subtract(r).toString());
        }
        return "";
    }

    private String readBrokerRevenue(String currencyId)
            throws XPathExpressionException {
        if ("Broker".equals(partnerType)) {
            return priceToDisplay(XMLConverter.getNodeTextContentByXPath(
                    xmlDocument, "//Currency[@id='" + currencyId + "']/"
                            + partnerType + "Revenue/@amount"));
        }
        return "";
    }

    private List<RDORevenueDetail> buildRevenueDetails(String currencyId,
            int parentEntryNr) throws XPathExpressionException, ParseException {
        List<RDORevenueDetail> result = new ArrayList<RDORevenueDetail>();
        for (Node orgDataNode : organizationDataNodes(currencyId)) {
            RDORevenueDetail details = new RDORevenueDetail(parentEntryNr,
                    idGen.nextValue());
            String supplierId = XMLConverter.getStringAttValue(orgDataNode,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_ID);
            details.setAmount(getRevenueDetailsAmount(currencyId, supplierId));
            details.setRevenue(readRevenue(currencyId, supplierId));
            details.setPurchasePrice(getRemainingAmount(currencyId, supplierId));
            details.setServices(buildServices(currencyId, supplierId,
                    details.getEntryNr()));
            details.setVendor(readVendorName(currencyId, supplierId));
            details.setCurrency(currencyId);
            result.add(details);
        }
        return result;
    }

    private List<Node> organizationDataNodes(String currencyId)
            throws XPathExpressionException {
        NodeList supplierNodes = XMLConverter.getNodeListByXPath(xmlDocument,
                "//Currency[@id='" + currencyId
                        + "']/Supplier/OrganizationData");
        return XMLConverter.getNodeList(supplierNodes,
                BillingShareResultXmlTags.NODE_NAME_ORGANIZATIONDATA);
    }

    private String getRevenueDetailsAmount(String currencyId, String supplierId)
            throws XPathExpressionException {
        String result = readAmount(currencyId, supplierId);
        if (result.equals(""))
            return computeServiceSumForSupplier(currencyId, supplierId);
        return result;
    }

    private String computeServiceSumForSupplier(String currencyId,
            String supplierId) throws XPathExpressionException {
        String path = "//Currency[@id='" + currencyId
                + "']/Supplier[OrganizationData[@id='" + supplierId
                + "']]/Service/ServiceRevenue/@totalAmount";
        return computeSum(path);
    }

    private String readAmount(String currencyId, String supplierId)
            throws XPathExpressionException {
        return priceToDisplay(XMLConverter.getNodeTextContentByXPath(
                xmlDocument, "//Currency[@id='" + currencyId
                        + "']/Supplier[OrganizationData[@id='" + supplierId
                        + "']]/" + partnerType
                        + "RevenuePerSupplier/@totalAmount"));
    }

    private String readRevenue(String currencyId, String supplierId)
            throws XPathExpressionException {
        return priceToDisplay(XMLConverter.getNodeTextContentByXPath(
                xmlDocument, "//Currency[@id='" + currencyId
                        + "']/Supplier[OrganizationData[@id='" + supplierId
                        + "']]/" + partnerType + "RevenuePerSupplier/@amount"));
    }

    private String getRemainingAmount(String currencyId, String supplierId)
            throws XPathExpressionException {
        return priceToDisplay(XMLConverter.getNodeTextContentByXPath(
                xmlDocument, "//Currency[@id='" + currencyId
                        + "']/Supplier[OrganizationData[@id='" + supplierId
                        + "']]/" + partnerType
                        + "RevenuePerSupplier/@purchasePrice"));
    }

    private String readVendorName(String currencyId, String supplierId)
            throws XPathExpressionException {
        return Formatting.nameAndId(XMLConverter
                .getNodeTextContentByXPath(xmlDocument, "//Currency[@id='"
                        + currencyId + "']/Supplier/OrganizationData[@id='"
                        + supplierId + "']/Name"), supplierId);
    }

    private List<RDORevenueDetailService> buildServices(String currencyId,
            String supplierId, int parentEntryNr)
            throws XPathExpressionException, ParseException {

        List<RDORevenueDetailService> result = new ArrayList<RDORevenueDetailService>();
        for (Node service : serviceNodes(currencyId, supplierId)) {
            processService(result, parentEntryNr, service, currencyId,
                    supplierId);
        }
        return result;
    }

    void processService(List<RDORevenueDetailService> result,
            int parentEntryNr, Node service, String currencyId,
            String supplierId) throws XPathExpressionException, ParseException {
        String serviceKey = getServiceKey(service);
        String serviceRevenue = readServiceRevenue(currencyId, supplierId,
                serviceKey);
        if (isServiceRevenueValid(serviceRevenue)) {
            RDORevenueDetailService revenueDetailService = createRevenueDetailService(
                    parentEntryNr, serviceKey, currencyId, supplierId);
            result.add(revenueDetailService);
        }
    }

    String getServiceKey(Node service) {
        return XMLConverter.getStringAttValue(service,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_KEY);
    }

    boolean isServiceRevenueValid(String serviceRevenue) throws ParseException {
        NumberFormat nf = NumberFormat.getInstance();
        BigDecimal nServiceRevenue = new BigDecimal(nf.parse(serviceRevenue)
                .toString());
        return nServiceRevenue.doubleValue() > 0;
    }

    RDORevenueDetailService createRevenueDetailService(int parentEntryNr,
            String serviceKey, String currencyId, String supplierId)
            throws XPathExpressionException, ParseException {
        RDORevenueDetailService rds = new RDORevenueDetailService(
                parentEntryNr, idGen.nextValue());

        rds.setCurrency(currencyId);
        rds.setVendor(readVendorName(currencyId, supplierId));
        rds.setAmount(readServiceAmount(currencyId, supplierId, serviceKey));
        rds.setRevenue(readServiceRevenue(currencyId, supplierId, serviceKey));
        rds.setRevenueShare(readRevenueShare(currencyId, supplierId, serviceKey));
        rds.setService(readServiceName(currencyId, supplierId, serviceKey));
        rds.setServicesPerCustomer(buildServicesPerCustomer(currencyId,
                supplierId, serviceKey, rds.getEntryNr()));
        rds.setPurchasePrice(computeRemainingAmount(rds.getAmount(),
                rds.getRevenue()));
        return rds;
    }

    private List<RDORevenueDetailServiceCustomer> buildServicesPerCustomer(
            String currencyId, String supplierId, String serviceKey,
            int parentEntryNr) throws XPathExpressionException {
        List<RDORevenueDetailServiceCustomer> result = new ArrayList<RDORevenueDetailServiceCustomer>();
        for (Node serviceCustomerNode : serviceCustomerNodes(currencyId,
                supplierId, serviceKey)) {
            RDORevenueDetailServiceCustomer servicesPerCustomer = new RDORevenueDetailServiceCustomer(
                    parentEntryNr, idGen.nextValue());
            String customerId = XMLConverter.getStringAttValue(
                    serviceCustomerNode,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_CUSTOMER_ID);
            servicesPerCustomer.setCustomer(readCustomerName(currencyId,
                    supplierId, serviceKey, customerId));
            servicesPerCustomer.setAmount(readServicesPerCustomerAmount(
                    currencyId, supplierId, serviceKey, customerId));
            servicesPerCustomer.setRevenue(readServicesPerCustomerRevenue(
                    currencyId, supplierId, serviceKey, customerId));
            servicesPerCustomer
                    .setRevenueShare(readServicesPerCustomerRevenueShare(
                            currencyId, supplierId, serviceKey, customerId));
            servicesPerCustomer
                    .setPurchasePrice(readServicesPerCustomerPurchasePrice(
                            currencyId, supplierId, serviceKey, customerId));
            servicesPerCustomer.setCurrency(currencyId);
            servicesPerCustomer
                    .setVendor(readVendorName(currencyId, supplierId));
            servicesPerCustomer.setService(readServiceName(currencyId,
                    supplierId, serviceKey));

            result.add(servicesPerCustomer);
        }
        return result;
    }

    private String readCustomerName(String currencyId, String supplierId,
            String serviceKey, String customerId)
            throws XPathExpressionException {
        return Formatting
                .nameAndId(
                        XMLConverter
                                .getNodeTextContentByXPath(
                                        xmlDocument,
                                        "//Currency[@id='"
                                                + currencyId
                                                + "']/Supplier[OrganizationData[@id='"
                                                + supplierId
                                                + "']]/Service[@key='"
                                                + serviceKey
                                                + "']/ServiceRevenue/ServiceCustomerRevenue[@customerId='"
                                                + customerId
                                                + "']/@customerName"),
                        customerId);
    }

    private String readServicesPerCustomerRevenueShare(String currencyId,
            String supplierId, String serviceKey, String customerId)
            throws XPathExpressionException {
        return percentageToDisplay(new BigDecimal(
                XMLConverter
                        .getNodeTextContentByXPath(
                                xmlDocument,
                                "//Currency[@id='"
                                        + currencyId
                                        + "']/Supplier[OrganizationData[@id='"
                                        + supplierId
                                        + "']]/Service[@key='"
                                        + serviceKey
                                        + "']/ServiceRevenue/ServiceCustomerRevenue[@customerId='"
                                        + customerId + "']/@"
                                        + partnerType.toLowerCase()
                                        + "RevenueSharePercentage")));
    }

    private String readServicesPerCustomerPurchasePrice(String currencyId,
            String supplierId, String serviceKey, String customerId)
            throws XPathExpressionException {
        if ("Reseller".equals(partnerType)) {
            return priceToDisplay(XMLConverter
                    .getNodeTextContentByXPath(
                            xmlDocument,
                            "//Currency[@id='"
                                    + currencyId
                                    + "']/Supplier[OrganizationData[@id='"
                                    + supplierId
                                    + "']]/Service[@key='"
                                    + serviceKey
                                    + "']/ServiceRevenue/ServiceCustomerRevenue[@customerId='"
                                    + customerId + "']/@purchasePrice"));
        }
        return "";
    }

    private String readServicesPerCustomerRevenue(String currencyId,
            String supplierId, String serviceKey, String customerId)
            throws XPathExpressionException {
        return priceToDisplay(XMLConverter
                .getNodeTextContentByXPath(
                        xmlDocument,
                        "//Currency[@id='"
                                + currencyId
                                + "']/Supplier[OrganizationData[@id='"
                                + supplierId
                                + "']]/Service[@key='"
                                + serviceKey
                                + "']/ServiceRevenue/ServiceCustomerRevenue[@customerId='"
                                + customerId + "']/@"
                                + partnerType.toLowerCase() + "Revenue"));
    }

    private String readServicesPerCustomerAmount(String currencyId,
            String supplierId, String serviceKey, String customerId)
            throws XPathExpressionException {
        return priceToDisplay(XMLConverter
                .getNodeTextContentByXPath(
                        xmlDocument,
                        "//Currency[@id='"
                                + currencyId
                                + "']/Supplier[OrganizationData[@id='"
                                + supplierId
                                + "']]/Service[@key='"
                                + serviceKey
                                + "']/ServiceRevenue/ServiceCustomerRevenue[@customerId='"
                                + customerId + "']/@totalAmount"));
    }

    private String readServiceAmount(String currencyId, String supplierId,
            String serviceKey) throws XPathExpressionException {
        return priceToDisplay(XMLConverter.getNodeTextContentByXPath(
                xmlDocument, "//Currency[@id='" + currencyId
                        + "']/Supplier[OrganizationData[@id='" + supplierId
                        + "']]/Service[@key='" + serviceKey
                        + "']/ServiceRevenue/@totalAmount"));
    }

    String readServiceRevenue(String currencyId, String supplierId,
            String serviceKey) throws XPathExpressionException {
        return priceToDisplay(XMLConverter.getNodeTextContentByXPath(
                xmlDocument, "//Currency[@id='" + currencyId
                        + "']/Supplier[OrganizationData[@id='" + supplierId
                        + "']]/Service[@key='" + serviceKey
                        + "']/ServiceRevenue/@" + partnerType.toLowerCase()
                        + "Revenue"));
    }

    private String readRevenueShare(String currencyId, String supplierId,
            String serviceKey) throws XPathExpressionException {
        return percentageToDisplay(new BigDecimal(
                XMLConverter.getNodeTextContentByXPath(xmlDocument,
                        "//Currency[@id='" + currencyId
                                + "']/Supplier[OrganizationData[@id='"
                                + supplierId + "']]/Service[@key='"
                                + serviceKey + "']/ServiceRevenue/@"
                                + partnerType.toLowerCase()
                                + "RevenueSharePercentage")));
    }

    private String readServiceName(String currencyId, String supplierId,
            String serviceKey) throws XPathExpressionException {
        return Formatting.nameAndId(XMLConverter
                .getNodeTextContentByXPath(xmlDocument, "//Currency[@id='"
                        + currencyId + "']/Supplier[OrganizationData[@id='"
                        + supplierId + "']]/Service[@key='" + serviceKey
                        + "']/@id"), serviceKey);
    }

    private List<Node> serviceNodes(String currencyId, String supplierId)
            throws XPathExpressionException {
        NodeList serviceNodes = XMLConverter.getNodeListByXPath(xmlDocument,
                "//Currency[@id='" + currencyId
                        + "']/Supplier[OrganizationData[@id='" + supplierId
                        + "']]/Service");
        return XMLConverter.getNodeList(serviceNodes,
                BillingShareResultXmlTags.NODE_NAME_SERVICE);
    }

    private List<Node> serviceCustomerNodes(String currencyId,
            String supplierId, String serviceKey)
            throws XPathExpressionException {
        NodeList serviceCustomerNodes = XMLConverter.getNodeListByXPath(
                xmlDocument, "//Currency[@id='" + currencyId
                        + "']/Supplier[OrganizationData[@id='" + supplierId
                        + "']]/Service[@key='" + serviceKey
                        + "']/ServiceRevenue/ServiceCustomerRevenue");
        if (serviceCustomerNodes == null) {
            return new ArrayList<Node>();
        }
        return XMLConverter.getNodeList(serviceCustomerNodes,
                BillingShareResultXmlTags.NODE_NAME_SERVICE_CUSTOMER_REVENUE);
    }

    private String revenueValueToDisplay(BigDecimal value) {
        return priceConverter.getValueToDisplay(value, false);
    }

    private String priceToDisplay(String xmlprice) {
        if (null == xmlprice) {
            return "";
        }
        return priceConverter
                .getValueToDisplay(new BigDecimal(xmlprice), false);
    }

    private String percentageToDisplay(BigDecimal revenueShare) {
        return priceConverter.getValueToDisplay(revenueShare, false) + "%";
    }

}
