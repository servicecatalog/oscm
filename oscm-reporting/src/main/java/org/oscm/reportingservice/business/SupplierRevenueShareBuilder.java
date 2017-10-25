/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oscm.converter.DateConverter;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.reportingservice.business.model.RdoIdGenerator;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDORevenueShareDetail;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDORevenueShareSummary;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareCurrency;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReport;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReports;
import org.oscm.reportingservice.dao.PartnerRevenueDao.ReportData;
import org.oscm.types.constants.BillingShareResultXmlTags;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author tokoda
 * 
 */
class SupplierRevenueShareBuilder {

    final static String PERCENTAGE_VARIOUS = "various";
    final static String PERCENTAGE_SIGN = "%";

    private final TimeZone timeZoneServer = TimeZone.getDefault();
    private final List<ReportData> sqlData;
    private Document xmlDocument;
    private final PriceConverter priceConverter;
    private final RdoIdGenerator idGen;

    private BigDecimal totalPartnerProvision = BigDecimal.ZERO;

    /* Map<Service Key, Service Id> */
    private final Map<String, String> serviceIdMap;

    /* Map<Service Id, Service Name> */
    private final Map<String, String> serviceNameMap;

    /* Map<Organization Id, Organization Name> */
    final Map<String, String> organizationNameMap;

    private final MarketplaceMap marketplaceNameMap;

    private class MarketplaceMap {
        private final Map<String, String> marketplaceNameMap;

        public MarketplaceMap(Map<String, String> marketplaceNameMap) {
            this.marketplaceNameMap = marketplaceNameMap;
        }

        private String get(String marketplaceId) {
            // Deleted marketplaces do not have the localized name anymore, so
            // return the marketplaceId
            return marketplaceNameMap.containsKey(marketplaceId)
                    ? marketplaceNameMap.get(marketplaceId) : marketplaceId;
        }
    }

    SupplierRevenueShareBuilder(Locale locale, List<ReportData> sqlData,
            Map<String, String> serviceIdMap,
            Map<String, String> marketplaceNameMap,
            Map<String, String> serviceNameMap) {
        super();
        this.sqlData = sqlData;
        priceConverter = new PriceConverter(locale);
        idGen = new RdoIdGenerator();

        if (null == serviceIdMap) {
            this.serviceIdMap = serviceIdMap;
        } else {
            this.serviceIdMap = new HashMap<>();
        }

        this.marketplaceNameMap = new MarketplaceMap(marketplaceNameMap);

        if (null == serviceNameMap) {
            this.serviceNameMap = serviceNameMap;
        } else {
            this.serviceNameMap = new HashMap<>();
        }

        organizationNameMap = new HashMap<>();
    }

    public RDOSupplierRevenueShareReports buildReports()
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException, ParseException {
        if (sqlData == null || sqlData.isEmpty()) {
            return new RDOSupplierRevenueShareReports();
        }

        RDOSupplierRevenueShareReports result = new RDOSupplierRevenueShareReports();
        result.setEntryNr(idGen.nextValue());
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());

        for (ReportData data : sqlData) {
            xmlDocument = XMLConverter.convertToDocument(data.getResultXml(),
                    false);
            result.getReports().add(build(data, result.getEntryNr()));
        }
        return result;
    }

    public RDOSupplierRevenueShareReport buildSingleReport()
            throws XPathExpressionException, ParserConfigurationException,
            SAXException, IOException, ParseException {

        if (sqlData == null || sqlData.isEmpty()) {
            return new RDOSupplierRevenueShareReport();
        }

        xmlDocument = XMLConverter
                .convertToDocument(sqlData.get(0).getResultXml(), false);
        return build(sqlData.get(0), 0);
    }

    private RDOSupplierRevenueShareReport build(ReportData reportData,
            int parentEntryNr) throws XPathExpressionException, ParseException {
        RDOSupplierRevenueShareReport report = new RDOSupplierRevenueShareReport(
                parentEntryNr, idGen.nextValue());

        report.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());
        report.setAddress(reportData.getAddress());
        report.setPeriodEnd(DateConverter.convertLongToDateTimeFormat(
                reportData.getPeriodEnd(), timeZoneServer,
                DateConverter.DTP_WITHOUT_MILLIS));
        report.setPeriodStart(DateConverter.convertLongToDateTimeFormat(
                reportData.getPeriodStart(), timeZoneServer,
                DateConverter.DTP_WITHOUT_MILLIS));
        report.setSupplier(Formatting.nameAndId(reportData.getName(),
                reportData.getOrganizationId()));

        report.setCountry(reportData.getCountryIsoCode());

        createOrganizationNameMap();

        report.setCurrencies(buildCurrencies(report.getEntryNr()));
        return report;
    }

    private void createOrganizationNameMap() throws XPathExpressionException {
        for (Node organizationNode : organizationNodes()) {
            String organizationId = XMLConverter.getStringAttValue(
                    organizationNode,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_ID);
            if (!organizationNameMap.containsKey(organizationId)) {
                Node nameNode = XMLConverter.getLastChildNode(organizationNode,
                        BillingShareResultXmlTags.NODE_NAME_NAME);
                if (nameNode != null) {
                    String organizationName = nameNode.getTextContent();
                    organizationNameMap.put(organizationId, organizationName);
                }
            }
        }
    }

    private List<RDOSupplierRevenueShareCurrency> buildCurrencies(
            int parentEntryNr) throws XPathExpressionException, ParseException {
        List<RDOSupplierRevenueShareCurrency> result = new ArrayList<>();
        for (Node nodeCurrency : currencyNodes()) {
            RDOSupplierRevenueShareCurrency currency = new RDOSupplierRevenueShareCurrency(
                    parentEntryNr, idGen.nextValue());
            result.add(currency);
            String currencyId = XMLConverter.getStringAttValue(nodeCurrency,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_ID);
            currency.setCurrency(currencyId);
            currency.setTotalRevenue(computeRevenuePerMarketplaceSum(currencyId,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE));
            createSupplierRelatedReportEntries(currency, currencyId);
            createBrokerRelatedReportEntries(currency, currencyId);
            createResellerRelatedReportEntries(currency, currencyId);
            currency.setTotalRemainingRevenue(
                    computeTotalRemainingRevenue(currency));
        }
        return result;
    }

    private List<Node> currencyNodes() throws XPathExpressionException {
        NodeList currencyNodes = XMLConverter.getNodeListByXPath(xmlDocument,
                "/SupplierRevenueShareResult/Currency");
        return XMLConverter.getNodeList(currencyNodes,
                BillingShareResultXmlTags.NODE_NAME_CURRENCY);
    }

    private String computeRevenuePerMarketplaceSum(String currencyId,
            String targetAttribute) throws XPathExpressionException {
        String path = "//Currency[@id='" + currencyId
                + "']/Marketplace/RevenuePerMarketplace/@" + targetAttribute;
        return computeSum(path);
    }

    private String computeTotalRemainingRevenue(
            RDOSupplierRevenueShareCurrency currency) throws ParseException {
        boolean allowNegativePrices = true;
        BigDecimal directTotalRemaining = priceConverter.parse(
                currency.getDirectTotalRemainingRevenue(), allowNegativePrices);
        BigDecimal brokerTotalRemaining = priceConverter.parse(
                currency.getBrokerTotalRemainingRevenue(), allowNegativePrices);
        BigDecimal resellerTotalRemaining = priceConverter.parse(
                currency.getResellerTotalRemainingRevenue(),
                allowNegativePrices);
        BigDecimal sum = directTotalRemaining.add(brokerTotalRemaining)
                .add(resellerTotalRemaining);
        return priceConverter.getValueToDisplay(sum, false);
    }

    BigDecimal computeRemainingRevenue(String currencyId,
            OfferingType offerType) throws XPathExpressionException {
        BigDecimal sum = BigDecimal.ZERO;
        for (Node marketplace : marketplaceNodes(currencyId)) {
            String marketplaceId = XMLConverter.getStringAttValue(marketplace,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_ID);
            for (Node service : serviceNodes(currencyId, marketplaceId,
                    offerType)) {
                Node revenueShareDetails = XMLConverter.getLastChildNode(
                        service,
                        BillingShareResultXmlTags.NODE_NAME_REVENUE_SHARE_DETAILS);
                BigDecimal amountForSupplier = XMLConverter
                        .getBigDecimalAttValue(revenueShareDetails,
                                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT_FOR_SUPPLIER);
                if (amountForSupplier == null) {
                    amountForSupplier = XMLConverter.getBigDecimalAttValue(
                            revenueShareDetails,
                            BillingShareResultXmlTags.ATTRIBUTE_NAME_NETAMOUNT_FOR_SUPPLIER);
                }
                if (amountForSupplier != null) {
                    sum = sum.add(amountForSupplier);
                }
            }
        }
        return sum;
    }

    private void createSupplierRelatedReportEntries(
            RDOSupplierRevenueShareCurrency currency, String currencyId)
            throws XPathExpressionException, ParseException {
        currency.setDirectTotalRevenue(
                computeServiceRevenueSum(currencyId, OfferingType.DIRECT));
        currency.setDirectProvisionToMarketplaceOwner(
                computeTotalRevenueToMarketplace(currencyId,
                        OfferingType.DIRECT,
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE));
        currency.setDirectRevenueSummaries(buildRevenueSummaries(
                currency.getEntryNr(), currencyId, OfferingType.DIRECT));
        currency.setDirectRevenueShareDetails(buildRevenueShareDetails(
                currency.getEntryNr(), currencyId, OfferingType.DIRECT));
        currency.setDirectProvisionToOperator(
                readDirectTotalOperatorRevenue(currencyId));
        currency.setDirectTotalRemainingRevenue(
                computeDirectTotalRemainingRevenue(currencyId));
    }

    private void createBrokerRelatedReportEntries(
            RDOSupplierRevenueShareCurrency currency, String currencyId)
            throws XPathExpressionException, ParseException {
        currency.setBrokerTotalRevenue(
                computeServiceRevenueSum(currencyId, OfferingType.BROKER));
        currency.setBrokerPercentageRevenue(computePercentage(
                currency.getTotalRevenue(), currency.getBrokerTotalRevenue()));
        currency.setBrokerProvisionToMarketplaceOwner(
                computeTotalRevenueToMarketplace(currencyId,
                        OfferingType.BROKER,
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE));
        currency.setBrokerRevenueSummaries(buildRevenueSummariesForPartner(
                currency.getEntryNr(), currencyId, OfferingType.BROKER));
        currency.setBrokerRevenueShareDetails(buildRevenueShareDetails(
                currency.getEntryNr(), currencyId, OfferingType.BROKER));
        currency.setBrokerProvisionToOperator(
                readBrokerTotalOperatorRevenue(currencyId));
        currency.setBrokerTotalRemainingRevenue(
                computeBrokerTotalRemainingRevenue(currencyId));
        currency.setBrokerTotalShareAmount(
                priceConverter.getValueToDisplay(totalPartnerProvision, false));
    }

    private void createResellerRelatedReportEntries(
            RDOSupplierRevenueShareCurrency currency, String currencyId)
            throws XPathExpressionException, ParseException {
        currency.setResellerTotalRevenue(
                computeServiceRevenueSum(currencyId, OfferingType.RESELLER));
        currency.setResellerPercentageRevenue(
                computePercentage(currency.getTotalRevenue(),
                        currency.getResellerTotalRevenue()));
        currency.setResellerProvisionToMarketplaceOwner(
                computeTotalRevenueToMarketplace(currencyId,
                        OfferingType.RESELLER,
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE));
        currency.setResellerRevenueSummaries(buildRevenueSummariesForPartner(
                currency.getEntryNr(), currencyId, OfferingType.RESELLER));
        currency.setResellerRevenueShareDetails(buildRevenueShareDetails(
                currency.getEntryNr(), currencyId, OfferingType.RESELLER));
        currency.setResellerProvisionToOperator(
                readResellerTotalOperatorRevenue(currencyId));
        currency.setResellerTotalRemainingRevenue(
                computeResellerTotalRemainingRevenue(currencyId));
        currency.setResellerTotalShareAmount(
                priceConverter.getValueToDisplay(totalPartnerProvision, false));
    }

    private String computeDirectTotalRemainingRevenue(String currencyId)
            throws XPathExpressionException {
        Node nodeDirectRevenue = XMLConverter.getNodeByXPath(xmlDocument,
                "//Currency[@id='" + currencyId
                        + "']/SupplierRevenue/DirectRevenue");

        // the node is defined as optional by the SupplierRevenueShareResult.xsd
        if (nodeDirectRevenue == null) {
            // bug 10506, adapt old existing database
            return priceConverter.getValueToDisplay(
                    computeRemainingRevenue(currencyId, OfferingType.DIRECT),
                    false);
        }

        BigDecimal serviceRevenue = XMLConverter.getBigDecimalAttValue(
                nodeDirectRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE);
        BigDecimal marketplaceRevenue = XMLConverter.getBigDecimalAttValue(
                nodeDirectRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE);
        BigDecimal operatorRevenue = XMLConverter.getBigDecimalAttValue(
                nodeDirectRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE);
        return priceConverter.getValueToDisplay(serviceRevenue
                .subtract(marketplaceRevenue).subtract(operatorRevenue), false);
    }

    private String readDirectTotalOperatorRevenue(String currencyId)
            throws XPathExpressionException {
        String revenue = XMLConverter.getNodeTextContentByXPath(xmlDocument,
                "//Currency[@id='" + currencyId
                        + "']/SupplierRevenue/DirectRevenue/@operatorRevenue");
        return revenue != null ? revenue : "0.00";
    }

    private String computeBrokerTotalRemainingRevenue(String currencyId)
            throws XPathExpressionException {
        Node nodeBrokerRevenue = XMLConverter.getNodeByXPath(xmlDocument,
                "//Currency[@id='" + currencyId
                        + "']/SupplierRevenue/BrokerRevenue");

        // the node is defined as optional by the SupplierRevenueShareResult.xsd
        if (nodeBrokerRevenue == null) {
            // bug 10506, adapt old existing database
            return priceConverter.getValueToDisplay(
                    computeRemainingRevenue(currencyId, OfferingType.BROKER),
                    false);
        }

        BigDecimal serviceRevenue = XMLConverter.getBigDecimalAttValue(
                nodeBrokerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE);
        BigDecimal marketplaceRevenue = XMLConverter.getBigDecimalAttValue(
                nodeBrokerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE);
        BigDecimal operatorRevenue = XMLConverter.getBigDecimalAttValue(
                nodeBrokerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE);
        BigDecimal brokerRevenue = XMLConverter.getBigDecimalAttValue(
                nodeBrokerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE);
        return priceConverter.getValueToDisplay(
                serviceRevenue.subtract(marketplaceRevenue)
                        .subtract(operatorRevenue).subtract(brokerRevenue),
                false);
    }

    private String readBrokerTotalOperatorRevenue(String currencyId)
            throws XPathExpressionException {
        String revenue = XMLConverter.getNodeTextContentByXPath(xmlDocument,
                "//Currency[@id='" + currencyId
                        + "']/SupplierRevenue/BrokerRevenue/@operatorRevenue");
        return revenue != null ? revenue : "0.00";
    }

    private String computeResellerTotalRemainingRevenue(String currencyId)
            throws XPathExpressionException {
        Node nodeResellerRevenue = XMLConverter.getNodeByXPath(xmlDocument,
                "//Currency[@id='" + currencyId
                        + "']/SupplierRevenue/ResellerRevenue");

        // the node is defined as optional by the SupplierRevenueShareResult.xsd
        if (nodeResellerRevenue == null) {
            // bug 10506, adapt old existing database
            return priceConverter.getValueToDisplay(
                    computeRemainingRevenue(currencyId, OfferingType.RESELLER),
                    false);
        }

        BigDecimal serviceRevenue = XMLConverter.getBigDecimalAttValue(
                nodeResellerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE);
        BigDecimal marketplaceRevenue = XMLConverter.getBigDecimalAttValue(
                nodeResellerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE);
        BigDecimal operatorRevenue = XMLConverter.getBigDecimalAttValue(
                nodeResellerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE);
        BigDecimal resellerRevenue = XMLConverter.getBigDecimalAttValue(
                nodeResellerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE);
        return priceConverter.getValueToDisplay(
                serviceRevenue.subtract(marketplaceRevenue)
                        .subtract(operatorRevenue).subtract(resellerRevenue),
                false);
    }

    private String readResellerTotalOperatorRevenue(String currencyId)
            throws XPathExpressionException {
        String revenue = XMLConverter.getNodeTextContentByXPath(xmlDocument,
                "//Currency[@id='" + currencyId
                        + "']/SupplierRevenue/ResellerRevenue/@operatorRevenue");
        return revenue != null ? revenue : "0.00";
    }

    List<RDORevenueShareSummary> buildRevenueSummaries(int parentEntryNr,
            String currencyId, OfferingType offeringType)
            throws XPathExpressionException, ParseException {
        totalPartnerProvision = BigDecimal.ZERO;
        List<RDORevenueShareSummary> result = new ArrayList<>();
        for (Node marketplace : marketplaceNodes(currencyId)) {
            String marketplaceId = XMLConverter.getStringAttValue(marketplace,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_ID);
            String marketplaceName = marketplaceNameMap.get(marketplaceId);

            for (Node service : serviceNodes(currencyId, marketplaceId,
                    offeringType)) {

                String serviceRevenue = getServiceRevenue(service);
                if (isServiceRevenueValid(serviceRevenue)) {
                    RDORevenueShareSummary rss = createRevenueShareSummary(
                            parentEntryNr, currencyId, offeringType,
                            marketplaceName, service, serviceRevenue);
                    result.add(rss);
                }
            }
        }
        return result;
    }

    private RDORevenueShareSummary createRevenueShareSummary(int parentEntryNr,
            String currencyId, OfferingType offeringType,
            String marketplaceName, Node service, String serviceRevenue) {
        RDORevenueShareSummary rss = new RDORevenueShareSummary(parentEntryNr,
                idGen.nextValue());
        rss.setRevenue(serviceRevenue);
        rss.setCurrency(currencyId);
        rss.setMarketplace(marketplaceName);
        rss.setPartner(getOrganizationDataId(service, offeringType));
        rss.setService(createServiceDisplayName(XMLConverter.getStringAttValue(
                service, BillingShareResultXmlTags.ATTRIBUTE_NAME_ID)));

        Node nodeRevenueShareDetails = XMLConverter.getLastChildNode(service,
                BillingShareResultXmlTags.NODE_NAME_REVENUE_SHARE_DETAILS);
        rss.setMarketplaceRevenue(XMLConverter.getStringAttValue(
                nodeRevenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE));
        rss.setMarketplaceRevenuePercentage(
                XMLConverter.getStringAttValue(nodeRevenueShareDetails,
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE_SHARE_PERCENTAGE)
                        + PERCENTAGE_SIGN);
        rss.setOperatorRevenue(XMLConverter.getStringAttValue(
                nodeRevenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE));
        rss.setOperatorRevenuePercentage(
                XMLConverter.getStringAttValue(nodeRevenueShareDetails,
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE_SHARE_PERCENTAGE)
                        + PERCENTAGE_SIGN);
        String amountForSupplier = XMLConverter.getStringAttValue(
                nodeRevenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT_FOR_SUPPLIER);
        if (amountForSupplier == null) {
            amountForSupplier = XMLConverter.getStringAttValue(
                    nodeRevenueShareDetails,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_NETAMOUNT_FOR_SUPPLIER);
        }
        if (amountForSupplier != null) {
            rss.setRevenueMinusShares(amountForSupplier);
        }
        return rss;
    }

    List<RDORevenueShareSummary> buildRevenueSummariesForPartner(
            int parentEntryNr, String currencyId, OfferingType offeringType)
            throws XPathExpressionException, ParseException {

        totalPartnerProvision = BigDecimal.ZERO;
        List<RDORevenueShareSummary> result = new ArrayList<>();
        for (Node marketplace : marketplaceNodes(currencyId)) {
            addMarketplaceSummary(parentEntryNr, currencyId, offeringType,
                    result, marketplace);
        }
        return result;
    }

    private void addMarketplaceSummary(int parentEntryNr, String currencyId,
            OfferingType offeringType, List<RDORevenueShareSummary> result,
            Node marketplace) throws XPathExpressionException, ParseException {
        final String marketplaceId = XMLConverter.getStringAttValue(marketplace,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_ID);
        final String marketplaceName = marketplaceNameMap.get(marketplaceId);
        String partner = null;

        BigDecimal svcRev = BigDecimal.ZERO, mpRev = BigDecimal.ZERO,
                opRev = BigDecimal.ZERO, partnerProvision = BigDecimal.ZERO,
                revMinusShares = BigDecimal.ZERO;
        final List<Node> services = serviceNodes(currencyId, marketplaceId,
                offeringType);
        Node lastNode = null;
        for (int i = 0; i < services.size(); i++) {
            final Node service = services.get(i);
            String serviceRevenue = getServiceRevenue(service);
            if (isServiceRevenueValid(serviceRevenue)) {
                final Node n = XMLConverter.getLastChildNode(service,
                        BillingShareResultXmlTags.NODE_NAME_REVENUE_SHARE_DETAILS);
                String currentPartner = getOrganizationDataId(service,
                        offeringType);

                if (svcRev.doubleValue() > 0
                        && hasPartnerChanged(partner, currentPartner)) {
                    addRevenueShareSummary(result, lastNode, offeringType,
                            partner, svcRev.toString(), mpRev.toString(),
                            opRev.toString(), revMinusShares.toString(),
                            partnerProvision.toString(), parentEntryNr,
                            currencyId, marketplaceName);

                    // reset variables for new partner
                    svcRev = BigDecimal.ZERO;
                    mpRev = BigDecimal.ZERO;
                    opRev = BigDecimal.ZERO;
                    partnerProvision = BigDecimal.ZERO;
                    revMinusShares = BigDecimal.ZERO;
                }
                svcRev = addRevenueShare(
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE,
                        n, svcRev);
                mpRev = addRevenueShare(
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE,
                        n, mpRev);
                opRev = addRevenueShare(
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE,
                        n, opRev);
                BigDecimal amountForSupplier = XMLConverter
                        .getBigDecimalAttValue(n,
                                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT_FOR_SUPPLIER);
                if (amountForSupplier == null) {
                    amountForSupplier = XMLConverter.getBigDecimalAttValue(n,
                            BillingShareResultXmlTags.ATTRIBUTE_NAME_NETAMOUNT_FOR_SUPPLIER);
                }
                if (amountForSupplier != null) {
                    revMinusShares = revMinusShares.add(amountForSupplier);
                }
                final BigDecimal d = new BigDecimal(
                        totalPartnerProvision.toString());
                totalPartnerProvision = addRevenueShare(
                        OfferingType.BROKER == offeringType
                                ? BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE
                                : BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE,
                        n, totalPartnerProvision);
                partnerProvision = partnerProvision
                        .add(totalPartnerProvision.subtract(d));
                partner = currentPartner;
                lastNode = n;
                if (i == services.size() - 1) {
                    addRevenueShareSummary(result, lastNode, offeringType,
                            partner, svcRev.toString(), mpRev.toString(),
                            opRev.toString(), revMinusShares.toString(),
                            partnerProvision.toString(), parentEntryNr,
                            currencyId, marketplaceName);
                }
            }
        }
    }

    private boolean hasPartnerChanged(String partner, String currentPartner) {
        // add to result if last element or partner changes
        return partner != null && !partner.equals(currentPartner);
    }

    private BigDecimal addRevenueShare(String tag, Node nodeRevenueShareDetails,
            BigDecimal sum) {
        final BigDecimal d = XMLConverter
                .getBigDecimalAttValue(nodeRevenueShareDetails, tag);
        if (d != null) {
            return sum.add(d);
        }
        return sum;
    }

    private void addRevenueShareSummary(List<RDORevenueShareSummary> result,
            Node n, OfferingType offeringType, String partner,
            String serviceRevenue, String marketplaceRevenue,
            String operatorRevenue, String revenueMinusShares,
            String partnerProvision, int parentEntryNr, String currencyId,
            String marketplaceName) {
        RDORevenueShareSummary rss = new RDORevenueShareSummary(parentEntryNr,
                idGen.nextValue());
        rss.setRevenue(serviceRevenue);
        rss.setCurrency(currencyId);
        rss.setMarketplace(marketplaceName);
        rss.setPartner(partner);

        rss.setMarketplaceRevenue(marketplaceRevenue);
        rss.setMarketplaceRevenuePercentage(XMLConverter.getStringAttValue(n,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE_SHARE_PERCENTAGE)
                + PERCENTAGE_SIGN);
        rss.setOperatorRevenue(operatorRevenue);
        rss.setOperatorRevenuePercentage(XMLConverter.getStringAttValue(n,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE_SHARE_PERCENTAGE)
                + PERCENTAGE_SIGN);
        rss.setRevenueMinusShares(revenueMinusShares);

        rss.setPartnerProvision(partnerProvision);
        rss.setPartnerProvisionPercentage(XMLConverter.getStringAttValue(n,
                OfferingType.BROKER == offeringType
                        ? BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE_SHARE_PERCENTAGE
                        : BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE_SHARE_PERCENTAGE));
        result.add(rss);
    }

    private List<Node> marketplaceNodes(String currencyId)
            throws XPathExpressionException {
        NodeList marketplaceNodes = XMLConverter.getNodeListByXPath(xmlDocument,
                "//Currency[@id='" + currencyId + "']/Marketplace");
        return XMLConverter.getNodeList(marketplaceNodes,
                BillingShareResultXmlTags.NODE_NAME_MARKETPLACE);
    }

    boolean isServiceRevenueValid(String serviceRevenue) throws ParseException {
        NumberFormat nf = NumberFormat.getInstance();
        BigDecimal nServiceRevenue = new BigDecimal(
                nf.parse(serviceRevenue).toString());
        return nServiceRevenue.doubleValue() > 0;
    }

    private String getServiceRevenue(Node service) {
        Node revenueShareDetails = XMLConverter.getLastChildNode(service,
                BillingShareResultXmlTags.NODE_NAME_REVENUE_SHARE_DETAILS);
        return XMLConverter.getStringAttValue(revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE);
    }

    private List<RDORevenueShareDetail> buildRevenueShareDetails(
            int parentEntryNr, String currencyId, OfferingType type)
            throws XPathExpressionException, ParseException {

        ArrayList<RDORevenueShareDetail> result = new ArrayList<>();
        for (Node marketplace : marketplaceNodes(currencyId)) {
            String marketplaceId = XMLConverter.getStringAttValue(marketplace,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_ID);
            String marketplaceName = marketplaceNameMap.get(marketplaceId);
            for (Node nodeService : serviceNodes(currencyId, marketplaceId,
                    type)) {
                String serviceId = XMLConverter.getStringAttValue(nodeService,
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_ID);
                String serviceKey = XMLConverter.getStringAttValue(nodeService,
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_KEY);
                for (Node details : customerRevenueShareDetailNodes(currencyId,
                        serviceKey, type)) {
                    addRevenueShareDetail(parentEntryNr, currencyId, type,
                            result, marketplaceName, nodeService, serviceId,
                            details);
                }
            }
        }
        return result;
    }

    private void addRevenueShareDetail(int parentEntryNr, String currencyId,
            OfferingType type, ArrayList<RDORevenueShareDetail> result,
            String marketplaceName, Node nodeService, String serviceId,
            Node details) throws ParseException {
        RDORevenueShareDetail revenueShareDetail = createRDORevenueShareDetail(
                details, nodeService, parentEntryNr, type, currencyId,
                marketplaceName, serviceId);
        if (revenueShareDetail != null) {
            result.add(revenueShareDetail);
        }
    }

    RDORevenueShareDetail createRDORevenueShareDetail(Node nodeCustDetails,
            Node nodeService, int parentEntryNr, OfferingType offeringType,
            String currencyId, String marketplaceName, String serviceId)
            throws ParseException {

        String serviceRevenue = getServiceRevenueByCustomerDetails(
                nodeCustDetails);
        if (!isServiceRevenueValid(serviceRevenue)) {
            return null;
        }

        String customerId = XMLConverter.getStringAttValue(nodeCustDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_CUSTOMER_ID);

        Node nodeRevenueShareDetails = XMLConverter.getLastChildNode(
                nodeService,
                BillingShareResultXmlTags.NODE_NAME_REVENUE_SHARE_DETAILS);

        RDORevenueShareDetail rsd = new RDORevenueShareDetail(parentEntryNr,
                idGen.nextValue());
        rsd.setCurrency(currencyId);
        String cumstomerName = XMLConverter.getStringAttValue(nodeCustDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_CUSTOMER_NAME);
        rsd.setCustomer(Formatting.nameAndId(cumstomerName, customerId));
        rsd.setRevenue(XMLConverter.getStringAttValue(nodeCustDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE));
        rsd.setMarketplace(marketplaceName);
        rsd.setService(createServiceDisplayName(serviceId));
        rsd.setMarketplaceRevenue(XMLConverter.getStringAttValue(
                nodeCustDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE));
        rsd.setMarketplaceSharePercentage(
                XMLConverter.getStringAttValue(nodeRevenueShareDetails,
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE_SHARE_PERCENTAGE)
                        + PERCENTAGE_SIGN);
        rsd.setPartner(getOrganizationDataId(nodeService, offeringType));
        if (offeringType.equals(OfferingType.BROKER)) {
            rsd.setPartnerRevenue(XMLConverter.getStringAttValue(
                    nodeCustDetails,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE));
            rsd.setPartnerSharePercentage(
                    XMLConverter.getStringAttValue(nodeRevenueShareDetails,
                            BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE_SHARE_PERCENTAGE)
                            + PERCENTAGE_SIGN);
        } else if (offeringType.equals(OfferingType.RESELLER)) {
            rsd.setPartnerRevenue(XMLConverter.getStringAttValue(
                    nodeCustDetails,
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE));
            rsd.setPartnerSharePercentage(
                    XMLConverter.getStringAttValue(nodeRevenueShareDetails,
                            BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE_SHARE_PERCENTAGE)
                            + PERCENTAGE_SIGN);
        }
        rsd.setOperatorRevenue(XMLConverter.getStringAttValue(nodeCustDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE));
        rsd.setOperatorRevenuePercentage(
                XMLConverter.getStringAttValue(nodeRevenueShareDetails,
                        BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE_SHARE_PERCENTAGE)
                        + PERCENTAGE_SIGN);
        rsd.setRevenueMinusShares(XMLConverter.getStringAttValue(
                nodeCustDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT_FOR_SUPPLIER));
        return rsd;
    }

    private String getServiceRevenueByCustomerDetails(Node nodeDetails) {
        return XMLConverter.getStringAttValue(nodeDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE);
    }

    private List<Node> serviceNodes(String currencyId, String marketplaceId,
            OfferingType partnerType) throws XPathExpressionException {
        NodeList serviceNodes = XMLConverter.getNodeListByXPath(xmlDocument,
                "//Currency[@id='" + currencyId + "']/Marketplace[@id='"
                        + marketplaceId + "']/Service[@model='"
                        + partnerType.name() + "']");
        return XMLConverter.getNodeList(serviceNodes,
                BillingShareResultXmlTags.NODE_NAME_SERVICE);
    }

    private List<Node> customerRevenueShareDetailNodes(String currencyId,
            String serviceKey, OfferingType partnerType)
            throws XPathExpressionException {
        NodeList serviceNodes = XMLConverter.getNodeListByXPath(xmlDocument,
                "//Currency[@id='" + currencyId
                        + "']/Marketplace/Service[@key='" + serviceKey
                        + "' and @model='" + partnerType.name()
                        + "' ]/RevenueShareDetails/CustomerRevenueShareDetails");
        return XMLConverter.getNodeList(serviceNodes,
                BillingShareResultXmlTags.NODE_NAME_CUSTOMER_REVENUE_SHARE_DETAILS);
    }

    private List<Node> organizationNodes() throws XPathExpressionException {
        NodeList organizationNodes = XMLConverter
                .getNodeListByXPath(xmlDocument, "//OrganizationData");
        return XMLConverter.getNodeList(organizationNodes,
                BillingShareResultXmlTags.NODE_NAME_ORGANIZATIONDATA);
    }

    private String computeServiceRevenueSum(String currencyId,
            OfferingType type) throws XPathExpressionException {
        String path = "//Currency[@id='" + currencyId
                + "']/Marketplace/Service[@model='" + type.name()
                + "']/RevenueShareDetails/@serviceRevenue";
        return computeSum(path);
    }

    private String computeTotalRevenueToMarketplace(String currencyId,
            OfferingType partnerType, String attribute)
            throws XPathExpressionException {
        String path = "//Currency[@id='" + currencyId
                + "']/Marketplace/Service[@model='" + partnerType.name()
                + "']/RevenueShareDetails/@" + attribute;
        return computeSum(path);
    }

    private String computeSum(String path) throws XPathExpressionException {
        return computeSum(Collections.singletonList(path));
    }

    private String computeSum(List<String> paths)
            throws XPathExpressionException {
        double sum = 0;
        for (String path : paths) {
            sum += XMLConverter.sumup(xmlDocument, path).doubleValue();
        }
        BigDecimal result = BigDecimal.valueOf(sum).setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        return result.toString();
    }

    String computePercentage(String totalValue, String value)
            throws ParseException {
        BigDecimal v = priceConverter.parse(value, true);
        BigDecimal tv = priceConverter.parse(totalValue, true);
        if (BigDecimal.ZERO.compareTo(tv) == 0) {
            return percentageToDisplay(BigDecimal.ZERO);
        } else {
            return percentageToDisplay(v.multiply(BigDecimal.valueOf(100))
                    .divide(tv, PriceConverter.ROUNDING_MODE));
        }
    }

    private String getOrganizationDataId(Node node, OfferingType type) {

        String NodeName = "";
        if (OfferingType.BROKER.equals(type)) {
            NodeName = BillingShareResultXmlTags.NODE_NAME_BROKER;
        } else if (OfferingType.RESELLER.equals(type)) {
            NodeName = BillingShareResultXmlTags.NODE_NAME_RESELLER;
        } else {
            return "";
        }

        Node partnerNode = XMLConverter.getLastChildNode(node, NodeName);
        Node orgData = XMLConverter.getLastChildNode(partnerNode,
                BillingShareResultXmlTags.NODE_NAME_ORGANIZATIONDATA);
        String id = XMLConverter.getStringAttValue(orgData,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_ID);
        return createOrganizationDisplayName(id);
    }

    private String percentageToDisplay(BigDecimal revenueShare) {
        return priceConverter.getValueToDisplay(revenueShare, false)
                + PERCENTAGE_SIGN;
    }

    String createOrganizationDisplayName(String orgId) {
        String orgName = organizationNameMap.get(orgId);
        return Formatting.nameAndId(orgName, orgId);
    }

    private String createServiceDisplayName(String serviceId) {
        String serviceName = serviceNameMap.get(serviceId);
        return Formatting.nameAndId(serviceName, serviceId);
    }

}
