/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                                
 *                                                                              
 *  Creation Date: 13.09.2011                                                      
 *                                                                              
 *  Completion Time: 13.09.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.billingservice.business.calculation.BigDecimals;
import org.oscm.converter.DateConverter;
import org.oscm.converter.LocaleHandler;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.RdoIdGenerator;
import org.oscm.reportingservice.business.model.billing.RDOBilling;
import org.oscm.reportingservice.business.model.billing.RDOEvent;
import org.oscm.reportingservice.business.model.billing.RDOEventFees;
import org.oscm.reportingservice.business.model.billing.RDOOption;
import org.oscm.reportingservice.business.model.billing.RDOParameter;
import org.oscm.reportingservice.business.model.billing.RDOPaymentPreviewSummary;
import org.oscm.reportingservice.business.model.billing.RDOPriceModel;
import org.oscm.reportingservice.business.model.billing.RDORole;
import org.oscm.reportingservice.business.model.billing.RDOSteppedPrice;
import org.oscm.reportingservice.business.model.billing.RDOSubscriptionFees;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.business.model.billing.RDOUserFees;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.types.constants.BillingResultXMLTags;
import org.oscm.internal.types.enumtypes.PriceModelType;

/**
 * Parses the billing result and creates report data objects.
 **/
public class BillingResultParser {

    private final BillingDao dao;

    /** the price converter to use for parsing prices */
    private PriceConverter parser = new PriceConverter(Locale.ENGLISH);

    private RdoIdGenerator sequence = new RdoIdGenerator();
    private PriceConverter formatter;
    private Document document;

    public BillingResultParser(BillingDao dao) {
        this.dao = dao;
    }

    private class ListPriceResult {
        private final List<? extends Object> list;
        private final BigDecimal total;

        public ListPriceResult(List<? extends Object> list, BigDecimal total) {
            this.list = list;
            this.total = total;
        }
    }

    /**
     * The zeroFactors flag indicates that not rounded zero factors are used in
     * price calculation.
     * 
     * @author kulle
     */
    private class BasicBillingInformation {
        private BigDecimal price;
        private boolean zeroFactors;

        public BasicBillingInformation(BigDecimal price, boolean zeroFactors) {
            this.price = price;
            this.zeroFactors = zeroFactors;
        }
    }

    private class ParameterOptionInformation {
        private BasicBillingInformation basicBillingInfo;
        private RDOOption rdoOption;

        public ParameterOptionInformation(RDOOption rdoOption) {
            this.rdoOption = rdoOption;
        }

        public boolean containsZeroFactors() {
            return basicBillingInfo.zeroFactors;
        }

        public BigDecimal getPrice() {
            return basicBillingInfo.price;
        }
    }

    /**
     * Retrieves from the history the localized service name as it was during
     * the billing period of the specified price model. If the corresponding
     * localized resource exists no more, the service ID is returned instead.
     * 
     * @param pricemodelKey
     *            the ID or the price model node, as it appears in the billing
     *            result XML
     * @return the service name as described
     * @throws SQLException
     *             if any error occurs on querying the database.
     */
    protected String getServiceName(String pricemodelKey) throws SQLException {
        String serviceName = null;
        String productId = null;

        dao.executeServiceNameQuery(Long.valueOf(pricemodelKey).longValue());
        if (dao.getReportData() != null) {
            productId = dao.getReportData().getProductId().split("#")[0];
            serviceName = dao.getReportData().getName();
        }

        if (serviceName != null) {
            return serviceName;
        }
        return productId;
    }

    protected BasicBillingInformation readBasicBillingInformation(Node node,
            RDOBilling rdo, PriceConverter formatter,
            BigDecimal... priceFactors) {

        BigDecimal price = BigDecimal.ZERO;
        boolean containsZeroPriceFactors = false;

        if (node != null && rdo != null) {

            // BASE PRICE
            BigDecimal basePrice = XMLConverter.getBigDecimalAttValue(node,
                    BillingResultXMLTags.BASE_PRICE_ATTRIBUTE_NAME);
            if (basePrice != null) {
                rdo.setBasePrice(formatter.getValueToDisplay(basePrice, true));
                if (basePrice.compareTo(BigDecimal.ZERO) == 0) {
                    containsZeroPriceFactors = true;
                }
            } else {
                rdo.setBasePrice("");
            }

            // FACTOR
            double factor = XMLConverter.getDoubleAttValue(node,
                    BillingResultXMLTags.FACTOR_ATTRIBUTE_NAME);
            // Data must be displayed if a factor is 0 after rounding!
            if (factor == 0) {
                containsZeroPriceFactors = true;
            }
            rdo.setFactor(ValueRounder.roundValue(BigDecimal.valueOf(factor),
                    formatter.getActiveLocale(), ValueRounder.SCALING_FACTORS));

            // PRICE
            List<BigDecimal> temp = new ArrayList<BigDecimal>(
                    Arrays.asList(priceFactors));
            temp.add(basePrice);
            temp.add(BigDecimal.valueOf(factor));
            BigDecimal[] f = new BigDecimal[0];

            price = parseBigDecimal(node,
                    BillingResultXMLTags.PRICE_ATTRIBUTE_NAME);
            rdo.setPrice(ValueRounder.roundValue(formatter, price,
                    temp.toArray(f)));
        }

        return new BasicBillingInformation(price, containsZeroPriceFactors);
    }

    /**
     * Evaluates a billing result and creates the corresponding data objects for
     * billing detail report
     * 
     * @param summaryTemplate
     *            the template for the summary data container, that is created
     *            for each price model
     * @param doc
     *            the document to get the billing data from
     * @param user
     *            the user to get the locale from
     * @param formatter
     *            the price converter to use for displaying prices.
     * @param reportEndTime
     *            the end time of the target period of the report
     * 
     * @throws XPathExpressionException
     *             in case of exceptions while accessing the document using
     *             xpath expressions
     */
    public List<RDOSummary> evaluateBillingResultForBillingDetails(
            RDOSummary summaryTemplate, Document doc, PlatformUser user,
            PriceConverter formatter) throws XPathExpressionException,
            SQLException {

        this.formatter = formatter;
        this.document = doc;
        return evaluateBillingResultForBilling(summaryTemplate, user, null);
    }

    /**
     * Evaluates a billing result and creates the corresponding data objects for
     * payment preview report
     * 
     * @param summaryTemplate
     *            the template for the summary data container, that is created
     *            for each price model
     * @param doc
     *            the document to get the billing data from
     * @param user
     *            the user to get the locale from
     * @param formatter
     *            the price converter to use for displaying prices.
     * @param reportEndTime
     *            the end time of the target period of the report
     * 
     * @throws XPathExpressionException
     *             in case of exceptions while accessing the document using
     *             xpath expressions
     */
    public List<RDOPaymentPreviewSummary> evaluateBillingResultForPaymentPreview(
            RDOSummary summaryTemplate, Document doc, PlatformUser user,
            PriceConverter formatter, Long paymentPreviewEndTime)
            throws XPathExpressionException, SQLException {

        this.formatter = formatter;
        this.document = doc;
        return evaluateBillingResult(summaryTemplate, user,
                paymentPreviewEndTime);
    }

    private List<RDOSummary> evaluateBillingResultForBilling(
            RDOSummary summaryTemplate, PlatformUser user,
            Long paymentPreviewEndTime) throws XPathExpressionException,
            SQLException {
        setOrganizationAndPaymentInfoForSummary(summaryTemplate);

        NodeList subscriptionNodes = XMLConverter.getNodeListByXPath(document,
                "/BillingDetails/Subscriptions/Subscription");
        List<RDOSummary> summaryList = new ArrayList<RDOSummary>();
        if (subscriptionNodes.getLength() > 0) {
            for (int index = subscriptionNodes.getLength() - 1; index >= 0; index--) {
                Node sub = subscriptionNodes.item(index);

                summaryTemplate.setSubscriptionId(XMLConverter
                        .getStringAttValue(sub,
                                BillingResultXMLTags.ID_ATTRIBUTE_NAME));
                summaryTemplate.setPurchaseOrderNumber(XMLConverter
                        .getStringAttValue(sub,
                                BillingResultXMLTags.PON_ATTRIBUTE_NAME));

                summaryList.addAll(evaluatePriceModels(summaryTemplate, sub,
                        user, paymentPreviewEndTime));
            }
            return summaryList;
        } else {
            return new ArrayList<RDOSummary>();
        }
    }

    private List<RDOPaymentPreviewSummary> evaluateBillingResult(
            RDOSummary summaryTemplate, PlatformUser user,
            Long paymentPreviewEndTime) throws XPathExpressionException,
            SQLException {
        setOrganizationAndPaymentInfoForSummary(summaryTemplate);

        NodeList subscriptionNodes = XMLConverter.getNodeListByXPath(document,
                "/BillingDetails/Subscriptions/Subscription");
        List<RDOPaymentPreviewSummary> summaryList = new ArrayList<RDOPaymentPreviewSummary>();
        if (subscriptionNodes.getLength() > 0) {
            for (int index = subscriptionNodes.getLength() - 1; index >= 0; index--) {
                Node sub = subscriptionNodes.item(index);

                summaryTemplate.setSubscriptionId(XMLConverter
                        .getStringAttValue(sub,
                                BillingResultXMLTags.ID_ATTRIBUTE_NAME));
                summaryTemplate.setPurchaseOrderNumber(XMLConverter
                        .getStringAttValue(sub,
                                BillingResultXMLTags.PON_ATTRIBUTE_NAME));

                summaryList
                        .add(convertToPaymentPreviewSummary(evaluatePriceModels(
                                summaryTemplate, sub, user,
                                paymentPreviewEndTime)));
            }
            return summaryList;
        } else {
            return new ArrayList<RDOPaymentPreviewSummary>();
        }
    }

    public RDOPaymentPreviewSummary convertToPaymentPreviewSummary(
            List<RDOSummary> summaries) {

        RDOPaymentPreviewSummary result = new RDOPaymentPreviewSummary();

        RDOSummary summary = summaries.get(0);

        result.setEntryNr(summary.getEntryNr());
        result.setSupplierName(summary.getSupplierName());
        result.setSupplierAddress(summary.getSupplierAddress());
        result.setPurchaseOrderNumber(summary.getPurchaseOrderNumber());
        result.setOrganizationName(summary.getOrganizationName());
        result.setOrganizationAddress(summary.getOrganizationAddress());
        result.setPaymentType(summary.getPaymentType());
        result.setCurrency(summary.getCurrency());
        result.setDiscount(summary.getDiscount());
        result.setDiscountAmount(summary.getDiscountAmount());
        result.setGrossAmount(summary.getGrossAmount());
        result.setAmount(summary.getAmount());
        result.setVat(summary.getVat());
        result.setVatAmount(summary.getVatAmount());
        result.setNetAmountBeforeDiscount(summary.getNetAmountBeforeDiscount());
        result.setSubscriptionId(summary.getSubscriptionId());

        List<RDOPriceModel> priceModels = new ArrayList<RDOPriceModel>();

        for (RDOSummary rDOSummary : summaries) {
            rDOSummary.getPriceModel().setStartDate(
                    rDOSummary.getPriceModelStartDate());
            rDOSummary.getPriceModel().setEndDate(
                    rDOSummary.getPriceModelEndDate());
            rDOSummary.getPriceModel().setParentEntryNr(summary.getEntryNr());

            priceModels.add(rDOSummary.getPriceModel());
        }
        result.setPriceModels(priceModels);

        return result;
    }

    private void setOrganizationAndPaymentInfoForSummary(
            RDOSummary summaryTemplate) throws XPathExpressionException {
        summaryTemplate.setOrganizationName(XMLConverter
                .getNodeTextContentByXPath(document,
                        "/BillingDetails/OrganizationDetails/Name"));
        summaryTemplate.setOrganizationAddress(XMLConverter
                .getNodeTextContentByXPath(document,
                        "/BillingDetails/OrganizationDetails/Address"));

        String paymentType = XMLConverter.getNodeTextContentByXPath(document,
                "/BillingDetails/OrganizationDetails/Paymenttype");
        summaryTemplate.setPaymentType(paymentType);
    }

    /**
     * Replaces the double decimal separator '.' with the one from the users
     * locale.
     * 
     * @param user
     *            the user to get the locale from
     * @param numberString
     *            the string representing a double value
     * @return the number string with the decimal separator of the users locale
     */
    private String replaceWithLocaleDecimalSeparator(PlatformUser user,
            String numberString) {
        Locale locale = LocaleHandler.getLocaleFromString(user.getLocale());
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
        char dfsChar = dfs.getDecimalSeparator();
        numberString = numberString.replace('.', dfsChar);
        return numberString;
    }

    /**
     * Creates and fills the price model related data objects for a
     * subscription.
     * 
     * @param summaryTemplate
     *            the template for the summary data container, that is created
     *            for each price model
     * @param subscriptionNode
     *            the subscription node to get the price models data from
     * @param user
     *            the user to get the locale from
     * @param formatter
     *            the price converter to use for displaying prices.
     * @param parser
     *            the price converter to use for parsing prices.
     * @return an array of <code>RDOPriceModelData</code> node.
     */
    @SuppressWarnings("unchecked")
    private List<RDOSummary> evaluatePriceModels(RDOSummary summaryTemplate,
            Node subscriptionNode, PlatformUser user, Long paymentPreviewEndTime)
            throws SQLException, XPathExpressionException {

        List<RDOSummary> summaryList = new ArrayList<RDOSummary>();
        TimeZone timeZoneServer = TimeZone.getDefault();

        // iterate over all price models and create for each rdo summary objects
        Node priceModelsNode = XMLConverter.getLastChildNode(subscriptionNode,
                BillingResultXMLTags.PRICE_MODELS_NODE_NAME);
        NodeList childNodes = priceModelsNode.getChildNodes();
        for (int index = 0; index < childNodes.getLength(); index++) {
            Node priceModelNode = childNodes.item(index);

            if (BillingResultXMLTags.PRICE_MODEL_NODE_NAME
                    .equals(priceModelNode.getNodeName())) {

                BigDecimal subscrFeesSubtotal = BigDecimal.ZERO;
                BigDecimal userFeesSubtotal = BigDecimal.ZERO;

                RDOSummary summary = createSummary(summaryTemplate);
                summaryList.add(summary);
                if (index == 0) {
                    summary.setEntryNr(sequence.nextValue());
                }
                RDOPriceModel priceModel = new RDOPriceModel();
                summary.setPriceModel(priceModel);
                priceModel.setParentEntryNr(summary.getEntryNr());
                priceModel.setEntryNr(sequence.nextValue());
                priceModel.setId(XMLConverter.getStringAttValue(priceModelNode,
                        BillingResultXMLTags.ID_ATTRIBUTE_NAME));

                // save service name at price model and summary due to simpler
                // report template creation
                String serviceName = getServiceName(priceModel.getId());
                priceModel.setServiceName(serviceName);
                summary.setServiceName(serviceName);

                RDOSubscriptionFees subscriptionFees = priceModel
                        .getSubscriptionFees();
                subscriptionFees.setParentEntryNr(priceModel.getEntryNr());
                subscriptionFees.setEntryNr(sequence.nextValue());
                subscriptionFees
                        .setServerTimeZone(DateConverter
                                .convertToUTCString(readTimeZoneFromBillingDetails(priceModelNode)));

                RDOUserFees userFees = priceModel.getUserFees();
                userFees.setParentEntryNr(priceModel.getEntryNr());
                userFees.setEntryNr(sequence.nextValue());

                // Calculation mode
                PriceModelType priceModelType = evaluateCalculationMode(priceModelNode);
                subscriptionFees.setCalculationMode(priceModelType.name());

                evaluateUsagePeriod(paymentPreviewEndTime, timeZoneServer,
                        priceModelNode, summary, priceModel);
                evaluateEventFees(priceModelNode, priceModel);
                subscrFeesSubtotal = evaluatePriceModelPeriodFee(
                        priceModelNode, subscriptionFees);
                userFeesSubtotal = evaluatePriceModelUserAssignmentCosts(
                        priceModelNode, userFees);
                evaluateOneTimeFee(priceModelNode, priceModel);

                // SUBSCRIPTION FEE PARAMETER
                final ListPriceResult parSubscrFees = getSubscriptionParameter(
                        priceModelNode, subscriptionFees.getEntryNr());
                subscriptionFees
                        .setParameters((List<RDOParameter>) parSubscrFees.list);
                subscrFeesSubtotal = subscrFeesSubtotal
                        .add(parSubscrFees.total);
                subscriptionFees.setSubtotalAmount(ValueRounder.roundSubtotal(
                        formatter, subscrFeesSubtotal));

                // USER FEE PARAMETER
                ListPriceResult parUserFees = getUserFeesParameter(
                        priceModelNode, formatter, userFees,
                        userFees.getEntryNr());
                userFees.setParameters((List<RDOParameter>) parUserFees.list);
                userFeesSubtotal = userFeesSubtotal.add(parUserFees.total);
                userFees.setSubtotalAmount(ValueRounder.roundSubtotal(
                        formatter, userFeesSubtotal));

                evaluatePriceModelCostsNode(priceModelNode, priceModel);

                evaluateOverallCosts(summary, user);

                // POST-CALCULATIONS
                if (userFees.isHideRecurringCharge()
                        && userFees.getRoles().isEmpty()
                        && userFees.getParameters().isEmpty()) {
                    userFees.setHideUserFees(true);
                }
                if (subscriptionFees.isHideRecurringCharge()
                        && subscriptionFees.getParameters().isEmpty()) {
                    subscriptionFees.setHideSubscriptionFees(true);
                }
            }
        }

        return summaryList;
    }

    private void evaluateUsagePeriod(Long paymentPreviewEndTime,
            TimeZone timeZoneServer, Node priceModelNode, RDOSummary summary,
            RDOPriceModel priceModel) {
        Node period = XMLConverter.getLastChildNode(priceModelNode,
                BillingResultXMLTags.USAGE_PERIOD_NODE_NAME);
        if (period != null) {
            // price model start date, redundant at summary
            long startmillis = XMLConverter.getLongAttValue(period,
                    BillingResultXMLTags.START_DATE_ATTRIBUTE_NAME);
            String startDate = DateConverter.convertLongToDateTimeFormat(
                    startmillis, timeZoneServer,
                    DateConverter.DTP_WITHOUT_MILLIS);
            priceModel.setStartDate(startDate);
            summary.setPriceModelStartDate(startDate);

            // price model end date, redundant at summary
            long endmillis = XMLConverter.getLongAttValue(period,
                    BillingResultXMLTags.END_DATE_ATTRIBUTE_NAME);
            if (paymentPreviewEndTime != null
                    && endmillis > paymentPreviewEndTime.longValue()) {
                endmillis = paymentPreviewEndTime.longValue();
            }
            String endDate = DateConverter
                    .convertLongToDateTimeFormat(endmillis, timeZoneServer,
                            DateConverter.DTP_WITHOUT_MILLIS);
            priceModel.setEndDate(endDate);
            summary.setPriceModelEndDate(endDate);

        }
    }

    private void evaluateOneTimeFee(Node priceModelNode,
            RDOPriceModel priceModel) {
        Node oneTime = XMLConverter.getLastChildNode(priceModelNode,
                BillingResultXMLTags.ONE_TIME_FEE_NODE_NAME);
        if (oneTime != null) {
            priceModel.setOneTimeFee(formatter.getValueToDisplay(
                    parseBigDecimal(oneTime,
                            BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME), true));
        }
    }

    private void evaluateOverallCosts(RDOSummary summary, PlatformUser user)
            throws XPathExpressionException {

        Node nodeOverallCosts = XMLConverter.getNodeByXPath(document,
                "//BillingDetails/OverallCosts");
        summary.setCurrency(XMLConverter.getStringAttValue(nodeOverallCosts,
                BillingResultXMLTags.CURRENCY_ATTRIBUTE_NAME));
        summary.setAmount(formatter.getValueToDisplay(
                parseBigDecimal(nodeOverallCosts,
                        BillingResultXMLTags.NET_AMOUNT_ATTRIBUTE_NAME), true));
        BigDecimal grossAmount = parseBigDecimal(nodeOverallCosts,
                BillingResultXMLTags.GROSS_AMOUNT_ATTRIBUTE_NAME);
        summary.setGrossAmount(formatter.getValueToDisplay(grossAmount, true));
        evaluateDiscount(summary, user);
        evaluateVat(summary, user);
    }

    private void evaluateDiscount(RDOSummary summary, PlatformUser user)
            throws XPathExpressionException {

        Node nodeDiscount = XMLConverter.getNodeByXPath(document,
                "//BillingDetails/OverallCosts/Discount");
        if (nodeDiscount != null) {
            summary.setDiscount(replaceWithLocaleDecimalSeparator(user,
                    XMLConverter.getStringAttValue(nodeDiscount,
                            BillingResultXMLTags.PERCENT_ATTRIBUTE_NAME)));
            BigDecimal discountValue = parseBigDecimal(nodeDiscount,
                    BillingResultXMLTags.DISCOUNT_NET_AMOUNT_ATTRIBUTE_NAME);
            summary.setDiscountAmount(formatter.getValueToDisplay(
                    discountValue, true));

            BigDecimal netAmountBeforeDiscount = parseBigDecimal(nodeDiscount,
                    BillingResultXMLTags.AMOUNT_BEFORE_DISCOUNT);

            summary.setNetAmountBeforeDiscount(formatter.getValueToDisplay(
                    netAmountBeforeDiscount, true));

        } else {
            summary.setDiscount(replaceWithLocaleDecimalSeparator(user, "0.0"));
            summary.setDiscountAmount(formatter.getValueToDisplay(
                    BigDecimal.ZERO, true));
            summary.setNetAmountBeforeDiscount(summary.getAmount());
        }
    }

    private void evaluateVat(RDOSummary summary, PlatformUser user)
            throws XPathExpressionException {

        Node nodeVat = XMLConverter.getNodeByXPath(document,
                "//BillingDetails/OverallCosts/VAT");
        if (nodeVat != null) {
            String vatPercent = replaceWithLocaleDecimalSeparator(user,
                    XMLConverter.getStringAttValue(nodeVat,
                            BillingResultXMLTags.PERCENT_ATTRIBUTE_NAME));
            summary.setVat(vatPercent);

            summary.setVatAmount(formatter.getValueToDisplay(
                    parseBigDecimal(nodeVat,
                            BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME), true));
        }
    }

    /**
     * Evaluate the calculationMode attribute of the PriceModel node
     * 
     * @param priceModelNode
     *            the priceModel node
     * @return the calculationMode as priceModelType enumeration
     */
    protected PriceModelType evaluateCalculationMode(Node priceModelNode) {
        String calculationMode = XMLConverter.getStringAttValue(priceModelNode,
                BillingResultXMLTags.CALCULATION_MODE_ATTRIBUTE_NAME);
        PriceModelType priceModelType = PriceModelType.PRO_RATA;
        if (calculationMode != null
                && calculationMode.equals(PriceModelType.PER_UNIT.name())) {
            priceModelType = PriceModelType.PER_UNIT;
        }

        return priceModelType;
    }

    int readTimeZoneFromBillingDetails(Node nodePriceModel)
            throws XPathExpressionException {
        String timezoneId = XMLConverter
                .getNodeTextContentByXPath(nodePriceModel.getOwnerDocument(),
                        "//BillingDetails/@timezone");
        return rawOffsetFromTimzoneId(timezoneId);
    }

    int rawOffsetFromTimzoneId(String timezoneId) {
        if (timezoneId != null && timezoneId.trim().length() > 0) {
            timezoneId = timezoneId.replace("UTC", "GMT");
            TimeZone timeZone = TimeZone.getTimeZone(timezoneId);
            return timeZone.getRawOffset();
        } else {
            return Calendar.getInstance().getTimeZone().getRawOffset();
        }
    }

    /**
     * Create a new RDOSummary using a summary template
     * 
     * @param summaryTemplate
     * @return the new RDOSummary
     */
    private RDOSummary createSummary(RDOSummary summaryTemplate) {
        RDOSummary summary = new RDOSummary();
        summary.setSubscriptionId(summaryTemplate.getSubscriptionId());
        summary.setOrganizationName(summaryTemplate.getOrganizationName());
        summary.setOrganizationAddress(
                summaryTemplate.getOrganizationAddress());
        summary.setPaymentType(summaryTemplate.getPaymentType());
        summary.setSupplierName(summaryTemplate.getSupplierName());
        summary.setSupplierAddress(summaryTemplate.getSupplierAddress());
        summary.setBillingDate(summaryTemplate.getBillingDate());
        summary.setPurchaseOrderNumber(
                summaryTemplate.getPurchaseOrderNumber());
        summary.setUserGroupName(summaryTemplate.getUserGroupName());
        summary.setUserGroupReferenceId(
                summaryTemplate.getUserGroupReferenceId());
        return summary;
    }

    /**
     * @return the price attribute from the PeriodFee node
     */
    private BigDecimal evaluatePriceModelPeriodFee(Node nodePriceModel,
            RDOSubscriptionFees subscriptionFees) {

        Node nodePeriodFee = XMLConverter.getLastChildNode(nodePriceModel,
                BillingResultXMLTags.PERIOD_FEE_NODE_NAME);
        if (nodePeriodFee == null) {
            return BigDecimal.ZERO;
        }

        subscriptionFees
                .setBasePeriod(XMLConverter.getStringAttValue(nodePeriodFee,
                        BillingResultXMLTags.BASE_PERIOD_ATTRIBUTE_NAME));

        BasicBillingInformation basicBillingInformation = readBasicBillingInformation(
                nodePeriodFee, subscriptionFees, formatter);

        // reports do not need to display empty recurring charges
        if (basicBillingInformation.zeroFactors) {
            subscriptionFees.setHideRecurringCharge(true);
        }

        return basicBillingInformation.price;
    }

    /**
     * @return the sum of the user assignment costs
     */
    @SuppressWarnings("unchecked")
    private BigDecimal evaluatePriceModelUserAssignmentCosts(
            Node priceModelNode, RDOUserFees userFees) {

        Node userAssignCostsNode = XMLConverter.getLastChildNode(
                priceModelNode,
                BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME);
        if (userAssignCostsNode == null) {
            return BigDecimal.ZERO;
        }

        // BASE PERIOD
        userFees.setBasePeriod(XMLConverter.getStringAttValue(
                userAssignCostsNode,
                BillingResultXMLTags.BASE_PERIOD_ATTRIBUTE_NAME));

        // NUMBER OF USERS TOTAL
        final BigDecimal numOfUsersTotal = parseBigDecimal(userAssignCostsNode,
                BillingResultXMLTags.NUMBER_OF_USERS_TOTAL_ATTRIBUTE_NAME);
        userFees.setNumberOfUsersTotal(formatter.getValueToDisplay(
                numOfUsersTotal, true));

        // BASE PRICE, FACTOR, PRICE
        BasicBillingInformation basicBillingInformation = readBasicBillingInformation(
                userAssignCostsNode, userFees, formatter, numOfUsersTotal);
        BigDecimal userFeesSubtotal = basicBillingInformation.price;

        // STEPPED PRICES
        ListPriceResult steppedPriceResult = getSteppedPrices(
                userAssignCostsNode, formatter, "", userFees.getEntryNr(),
                false);
        userFees.setSteppedPrices((List<RDOSteppedPrice>) steppedPriceResult.list);
        // remove price from user fees if stepped prices are set
        if (userFees.getSteppedPrices().size() > 0) {
            userFees.setPrice("");
        }

        // set flag to indicate if report should hide the recurring charge line
        if (basicBillingInformation.zeroFactors
                && userFees.getSteppedPrices().isEmpty()) {
            userFees.setHideRecurringCharge(true);
        }

        // ROLES
        ListPriceResult roles = getRoleData(userFees.getEntryNr(),
                userAssignCostsNode, formatter);
        userFeesSubtotal = userFeesSubtotal.add(roles.total);
        addRoleBasedRecuringCharges(formatter, userFees,
                (List<RDORole>) roles.list);

        return userFeesSubtotal;
    }

    private void evaluatePriceModelCostsNode(Node priceModelNode,
            RDOPriceModel priceModel) {

        Node priceModelCostsNode = XMLConverter.getLastChildNode(
                priceModelNode,
                BillingResultXMLTags.PRICE_MODEL_COSTS_NODE_NAME);
        String amount = formatter.getValueToDisplay(
                parseBigDecimal(priceModelCostsNode,
                        BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME), true);
        priceModel.setCosts(amount);
        priceModel.setNetAmountBeforeDiscount(amount);
        priceModel.setCurrency(XMLConverter.getStringAttValue(
                priceModelCostsNode,
                BillingResultXMLTags.CURRENCY_ATTRIBUTE_NAME));

    }

    /**
     * Merge the given RDORoles into the given RDOUserFees
     */
    private void addRoleBasedRecuringCharges(PriceConverter formatter,
            RDOUserFees userFee, List<RDORole> roles) {
        for (RDORole role : roles) {
            RDORole existingRole = userFee.getRole(role.getRoleId());
            if (existingRole == null) {
                userFee.getRoles().add(role);
            } else {
                existingRole.setBasePrice(role.getBasePrice());
                existingRole.setFactor(ValueRounder.roundValue(new BigDecimal(
                        role.getFactor()), formatter.getActiveLocale(),
                        ValueRounder.SCALING_FACTORS));
                existingRole.setPrice(role.getPrice());
            }

        }
    }

    /**
     * Creates and fills the role related data objects.
     * 
     * @param parentEntryNr
     *            id of the parent rdo object
     * @param parent
     *            the parent xml node
     * @param container
     *            the structure object that will contain the role specific data
     * @param formatter
     *            the price converter to use for displaying prices.
     * @return an array of {@link RDORole}
     */
    private ListPriceResult getRoleData(int parentEntryNr, Node parent,
            PriceConverter formatter) {

        List<RDORole> list = new ArrayList<RDORole>();
        BigDecimal subtotal = BigDecimal.ZERO;

        Node nodeRoleCosts = XMLConverter.getLastChildNode(parent,
                BillingResultXMLTags.ROLE_COSTS_NODE_NAME);
        if (nodeRoleCosts == null) {
            // RoleCosts maybe contained in a parameter or a parameter option
            Node parameter = parent;

            Node temp = getParameterOptionNode(parameter);
            if (temp != null) {
                temp = XMLConverter.getLastChildNode(temp,
                        BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME);
                nodeRoleCosts = XMLConverter.getLastChildNode(temp,
                        BillingResultXMLTags.ROLE_COSTS_NODE_NAME);
            } else {
                temp = XMLConverter.getLastChildNode(parameter,
                        BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME);
                if (temp != null) {
                    nodeRoleCosts = XMLConverter.getLastChildNode(temp,
                            BillingResultXMLTags.ROLE_COSTS_NODE_NAME);
                }
            }
        }

        if (nodeRoleCosts != null) {
            subtotal = XMLConverter.getBigDecimalAttValue(nodeRoleCosts,
                    BillingResultXMLTags.TOTAL_ATTRIBUTE_NAME);

            NodeList roles = nodeRoleCosts.getChildNodes();
            for (int index = 0; index < roles.getLength(); index++) {

                Node nodeRoleCost = roles.item(index);
                if (BillingResultXMLTags.ROLE_COST_NODE_NAME
                        .equals(nodeRoleCost.getNodeName())) {

                    // create RDO...
                    RDORole roleData = new RDORole();
                    roleData.setEntryNr(sequence.nextValue());
                    roleData.setParentEntryNr(parentEntryNr);
                    roleData.setRoleId(XMLConverter.getStringAttValue(
                            nodeRoleCost,
                            BillingResultXMLTags.ID_ATTRIBUTE_NAME));

                    if (!readBasicBillingInformation(nodeRoleCost, roleData,
                            formatter).zeroFactors) {
                        list.add(roleData);
                    }

                }
            }
        }

        return new ListPriceResult(list, subtotal);
    }

    /**
     * Returns the option node inside a parameter node.
     * 
     * @param pNode
     *            the parameter node
     * @return the option node or <code>null</code> if not existing
     */
    private Node getParameterOptionNode(Node pNode) {
        Node options = XMLConverter.getLastChildNode(pNode,
                BillingResultXMLTags.OPTIONS_NODE_NAME);
        if (options == null) {
            return null;
        }
        Node oNode = XMLConverter.getLastChildNode(options,
                BillingResultXMLTags.OPTION_NODE_NAME);
        return oNode;
    }

    /**
     * Evaluates the event data of a price model.
     * 
     * @param nodePriceModel
     *            the price model document node
     * @param priceModel
     *            the price model data object
     * @param formatter
     *            the price converter to use for displaying prices.
     * @param parser
     *            the price converter to use for parsing prices.
     * @return an array of <code>RDOEventData</code>
     */
    @SuppressWarnings("unchecked")
    private void evaluateEventFees(Node nodePriceModel, RDOPriceModel priceModel) {

        // create event fees rdo
        RDOEventFees eventFees = priceModel.getEventFees();
        eventFees.setParentEntryNr(priceModel.getEntryNr());
        eventFees.setEntryNr(sequence.nextValue());

        // parse events
        List<RDOEvent> eventList = new ArrayList<RDOEvent>();
        Node nodeGatheredEvents = XMLConverter.getLastChildNode(nodePriceModel,
                BillingResultXMLTags.GATHERED_EVENTS_NODE_NAME);
        if (nodeGatheredEvents != null) {
            NodeList events = nodeGatheredEvents.getChildNodes();
            for (int index = 0; index < events.getLength(); index++) {
                Node nodeEvent = events.item(index);
                if (BillingResultXMLTags.EVENT_NODE_NAME.equals(nodeEvent
                        .getNodeName())) {

                    // create and fill event rdo
                    RDOEvent event = new RDOEvent();
                    event.setParentEntryNr(eventFees.getEntryNr());
                    event.setEntryNr(sequence.nextValue());
                    event.setId(XMLConverter.getStringAttValue(nodeEvent,
                            BillingResultXMLTags.ID_ATTRIBUTE_NAME));

                    Node occurencesNode = XMLConverter
                            .getLastChildNode(
                                    nodeEvent,
                                    BillingResultXMLTags.NUMBER_OF_OCCURRENCE_NODE_NAME);

                    ListPriceResult result = getSteppedPrices(nodeEvent,
                            formatter, event.getId(), event.getEntryNr(), true);
                    event.setSteppedPrices((List<RDOSteppedPrice>) result.list);

                    if (event.getSteppedPrices().isEmpty()) {
                        event.setNumberOfOccurences(XMLConverter
                                .getStringAttValue(
                                        occurencesNode,
                                        BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME));
                        Node basePrice = XMLConverter.getLastChildNode(
                                nodeEvent,
                                BillingResultXMLTags.SINGLE_COST_NODE_NAME);
                        event.setBasePrice(formatter
                                .getValueToDisplay(
                                        parseBigDecimal(
                                                basePrice,
                                                BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME),
                                        true));
                        Node price = XMLConverter
                                .getLastChildNode(
                                        nodeEvent,
                                        BillingResultXMLTags.COST_FOR_EVENT_TYPE_NODE_NAME);
                        if (price != null) {
                            final BigDecimal amount = parseBigDecimal(price,
                                    BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME);
                            event.setPrice(ValueRounder.roundValue(formatter,
                                    amount, new BigDecimal[0]));
                        }

                    }

                    eventList.add(event);
                }
            }
        }
        eventFees.setEvents(eventList);

        if (nodeGatheredEvents != null) {// GatheredEvents may not be available

            // subtotal of events is already a scaled bigdecimal and converted
            // to string
            eventFees
                    .setSubtotalAmount(XMLConverter.getStringAttValue(
                            XMLConverter
                                    .getLastChildNode(
                                            nodeGatheredEvents,
                                            BillingResultXMLTags.GATHERED_EVENTS_COSTS_NODE_NAME),
                            "amount"));
        }

        // calculate hide event fees
        if (eventFees.getEvents().isEmpty()) {
            eventFees.setHideEventFees(true);
        }
    }

    /**
     * Creates and fill the parameter related data objects for a price model.
     * 
     * @param nodePriceModel
     *            the price model document node
     * @param formatter
     *            the price converter to use for displaying prices.
     * @param userFees
     *            the parent RDOUserFees object
     * @param parentEntryNr
     *            the entry number of the parent RDOUserFees object
     * @return a list of <code>RDOParameterData</code> objects
     */
    @SuppressWarnings("unchecked")
    private ListPriceResult getUserFeesParameter(Node nodePriceModel,
            PriceConverter formatter, RDOUserFees userFees, int parentEntryNr) {

        List<RDOParameter> list = new ArrayList<RDOParameter>();
        BigDecimal subtotal = BigDecimal.ZERO;

        Node nodeParameters = XMLConverter.getLastChildNode(nodePriceModel,
                BillingResultXMLTags.PARAMETERS_NODE_NAME);
        if (nodeParameters != null) {

            NodeList params = nodeParameters.getChildNodes();
            for (int index = 0; index < params.getLength(); index++) {

                Node nodeParameter = params.item(index);
                if (BillingResultXMLTags.PARAMETER_NODE_NAME
                        .equals(nodeParameter.getNodeName())) {

                    Node nodeUserFee = XMLConverter
                            .getLastChildNode(
                                    nodeParameter,
                                    BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME);
                    Node nodeOptions = XMLConverter.getLastChildNode(
                            nodeParameter,
                            BillingResultXMLTags.OPTIONS_NODE_NAME);

                    if (nodeUserFee != null || nodeOptions != null) {
                        RDOParameter parameter = null;
                        RDOOption parameterOption = null;

                        String parameterId = XMLConverter.getStringAttValue(
                                nodeParameter,
                                BillingResultXMLTags.ID_ATTRIBUTE_NAME);

                        if (nodeUserFee != null) {
                            parameter = new RDOParameter();
                            parameter.setParentEntryNr(parentEntryNr);
                            parameter.setEntryNr(sequence.nextValue());
                            parameter.setId(parameterId);

                            BasicBillingInformation basicBillingInfo = parseParameterUserFee(
                                    nodeParameter, nodeUserFee, parameter,
                                    formatter);

                            if (!basicBillingInfo.zeroFactors) {
                                list.add(parameter);
                                subtotal = subtotal.add(basicBillingInfo.price);
                            }
                        } else {
                            // parse option
                            ParameterOptionInformation pResult = getParameterOption(
                                    BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME,
                                    nodeOptions, formatter);

                            if (pResult != null) {
                                parameterOption = pResult.rdoOption;

                                if (!pResult.containsZeroFactors()) {
                                    // ENUM parameter -> The Parameter XML
                                    // element may exist several times
                                    // with different options!
                                    parameter = findOrCreateParameter(list,
                                            parameterId, parentEntryNr);

                                    parameterOption.setParentEntryNr(parameter
                                            .getEntryNr());
                                    parameter.getOptions().add(parameterOption);
                                    subtotal = subtotal.add(pResult.getPrice());
                                }
                            }
                        }

                        // ROLES
                        ListPriceResult roleData = getRoleData(parentEntryNr,
                                nodeParameter, formatter);

                        for (RDORole role : (List<RDORole>) roleData.list) {
                            RDORole roleOfUserFees = findOrCreateRole(userFees,
                                    role.getRoleId(), parentEntryNr);

                            if (parameterOption == null) {
                                if (parameter != null) {
                                    // Create a new RDOParameter with the role
                                    // data and link it to the RDORole
                                    RDOParameter roleBasedParameter = createRoleBasedParameter(
                                            parameter, role,
                                            roleOfUserFees.getEntryNr());

                                    roleOfUserFees.getParameters().add(
                                            roleBasedParameter);
                                }
                            } else {
                                // The role parameter may already exist because
                                // the parameter option may have changed
                                RDOParameter roleParameter = findOrCreateRoleParameter(
                                        roleOfUserFees, parameterId);

                                // Create a new RDOOption with the role data and
                                // link it to the role parameter
                                createRoleBasedOption(roleParameter,
                                        parameterOption.getValue(), role);
                            }
                        }

                        subtotal = subtotal.add(roleData.total);
                    }

                }
            }
        }

        return new ListPriceResult(list, subtotal);
    }

    /**
     * We do not have to look for stepped prices as with subscription fee
     * parameters! User fee parameter cannot have stepped prices, see
     * BillingResult.xsd.
     * <p>
     * Roles are parsed separately.
     * */
    private BasicBillingInformation parseParameterUserFee(Node nodeParameter,
            Node nodeUserFee, RDOParameter parameter, PriceConverter formatter) {

        // PARAMETERVALUE
        Node value = XMLConverter.getLastChildNode(nodeParameter,
                BillingResultXMLTags.PARAMETER_VALUE_NODE_NAME);
        if (value != null) {
            parameter.setValue(XMLConverter.getStringAttValue(value,
                    BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME));
        }

        // VALUEFACTOR
        BigDecimal valueFactor = XMLConverter.getBigDecimalAttValue(
                nodeUserFee, BillingResultXMLTags.VALUE_FACTOR_ATTRIBUTE_NAME);
        parameter
                .setValueFactor(formatter.getValueToDisplay(valueFactor, true));

        return readBasicBillingInformation(nodeUserFee, parameter, formatter);
    }

    /**
     * Find the role of the user fees. It might not yet exist, depending of the
     * xml structure of the billing. In this case a new one is created.
     */
    private RDORole findOrCreateRole(RDOUserFees userFees, String roleId,
            int parentEntryNr) {
        RDORole priceModelRole = userFees.getRole(roleId);
        if (priceModelRole == null) {
            priceModelRole = new RDORole();
            priceModelRole.setParentEntryNr(parentEntryNr);
            priceModelRole.setEntryNr(sequence.nextValue());
            priceModelRole.setRoleId(roleId);
            userFees.getRoles().add(priceModelRole);
        }
        return priceModelRole;
    }

    /**
     * Create a new role based parameter. The information from the role and the
     * parameter are merged.
     */
    private RDOParameter createRoleBasedParameter(RDOParameter parameterRdo,
            RDORole role, int parentEntryNr) {
        RDOParameter roleBasedParameter = new RDOParameter();
        roleBasedParameter.setParentEntryNr(parentEntryNr);
        roleBasedParameter.setEntryNr(sequence.nextValue());
        roleBasedParameter.setId(parameterRdo.getId());
        roleBasedParameter.setValue(parameterRdo.getValue());
        roleBasedParameter.setValueFactor(parameterRdo.getValueFactor());

        roleBasedParameter.setBasePrice(role.getBasePrice());
        roleBasedParameter.setPrice(role.getPrice());
        roleBasedParameter.setFactor(role.getFactor()); // already rounded

        return roleBasedParameter;
    }

    /**
     * Search a parameter in a RDORole and create the parameter if it doesn't
     * exist
     * 
     * @param roleRdo
     *            an RDORole
     * @param parameterId
     *            the id of the parameter to search for
     * @return the found or newly created RDOParameter
     */
    private RDOParameter findOrCreateRoleParameter(RDORole roleRdo,
            String parameterId) {
        RDOParameter roleParameter = roleRdo.getParameter(parameterId);
        if (roleParameter == null) {
            roleParameter = new RDOParameter();
            roleParameter.setParentEntryNr(roleRdo.getEntryNr());
            roleParameter.setEntryNr(sequence.nextValue());
            roleParameter.setId(parameterId);
            roleRdo.getParameters().add(roleParameter);
        }

        return roleParameter;
    }

    /**
     * Create a new role based option, fill it with the data of the given
     * RDORole and link it to the given RDOParameter
     * 
     * @param roleParameter
     *            a role parameter
     * @param optionId
     *            the option ID
     * @param role
     *            the RDORole that contains the role data
     */
    private void createRoleBasedOption(RDOParameter roleParameter,
            String optionId, RDORole role) {
        RDOOption roleBasedOption = new RDOOption();
        roleBasedOption.setParentEntryNr(roleParameter.getEntryNr());
        roleBasedOption.setEntryNr(sequence.nextValue());
        roleBasedOption.setValue(optionId);
        roleBasedOption.setBasePrice(role.getBasePrice());
        roleBasedOption.setPrice(role.getPrice());
        roleBasedOption.setFactor(role.getFactor()); // already rounded

        roleParameter.getOptions().add(roleBasedOption);
    }

    /**
     * Creates and fill the parameter related data objects for a price model.
     * 
     * @param nodePriceModel
     *            the price model document node
     * @param priceModel
     *            the price model data object
     * @param parser
     *            the price converter to use for parsing prices.
     * @return an array of <code>RDOParameterData</code>
     */
    private ListPriceResult getSubscriptionParameter(Node nodePriceModel,
            int parentEntryNr) {

        List<RDOParameter> list = new ArrayList<RDOParameter>();
        BigDecimal subtotal = BigDecimal.ZERO;

        Node nodeParameters = XMLConverter.getLastChildNode(nodePriceModel,
                BillingResultXMLTags.PARAMETERS_NODE_NAME);
        if (nodeParameters != null) {

            NodeList params = nodeParameters.getChildNodes();
            for (int index = 0; index < params.getLength(); index++) {

                Node nodeParameter = params.item(index);
                if (BillingResultXMLTags.PARAMETER_NODE_NAME
                        .equals(nodeParameter.getNodeName())) {

                    Node nodePeriodFee = XMLConverter.getLastChildNode(
                            nodeParameter,
                            BillingResultXMLTags.PERIOD_FEE_NODE_NAME);
                    Node nodeOptions = XMLConverter.getLastChildNode(
                            nodeParameter,
                            BillingResultXMLTags.OPTIONS_NODE_NAME);

                    if (nodePeriodFee != null || nodeOptions != null) {
                        // create RDO and start parsing of fees...
                        String parameterId = XMLConverter.getStringAttValue(
                                nodeParameter,
                                BillingResultXMLTags.ID_ATTRIBUTE_NAME);

                        // parse period fees
                        if (nodePeriodFee != null) {
                            RDOParameter parameter = new RDOParameter();
                            parameter.setParentEntryNr(parentEntryNr);
                            parameter.setEntryNr(sequence.nextValue());
                            parameter.setId(parameterId);

                            BasicBillingInformation basicBillingInfo = parseParameterPeriodFee(
                                    nodeParameter, nodePeriodFee, parameter,
                                    formatter);
                            if (!basicBillingInfo.zeroFactors) {
                                list.add(parameter);
                                subtotal = subtotal.add(basicBillingInfo.price);
                            }
                        } else {
                            // parse option
                            ParameterOptionInformation pResult = getParameterOption(
                                    BillingResultXMLTags.PERIOD_FEE_NODE_NAME,
                                    nodeOptions, formatter);

                            if (pResult != null
                                    && !pResult.containsZeroFactors()) {
                                // ENUM parameter: The Parameter XML element may
                                // exist several times with different options!
                                RDOParameter parameter = findOrCreateParameter(
                                        list, parameterId, parentEntryNr);

                                pResult.rdoOption.setParentEntryNr(parameter
                                        .getEntryNr());
                                parameter.getOptions().add(pResult.rdoOption);
                                subtotal = subtotal.add(pResult.getPrice());
                            }
                        }

                    }

                }
            }
        }

        return new ListPriceResult(list, subtotal);
    }

    /**
     * Search the parameter with the specified ID in the list. If the parameter
     * is not found, create a new one.
     * 
     * @param parameterList
     *            a parameter list
     * @param parameterId
     *            the parameter ID to search for
     * @param parentEntryNr
     *            the parent entry number for a new RDOParameter
     * @return the <code>RDOParameter</code> with the required ID or
     *         <code>null</code> if none is found
     */
    private RDOParameter findOrCreateParameter(
            List<RDOParameter> parameterList, String parameterId,
            int parentEntryNr) {
        for (RDOParameter parameter : parameterList) {
            if (parameter.getId().equals(parameterId)) {
                return parameter;
            }
        }

        // Parameter was not found -> create a new one
        RDOParameter parameter = new RDOParameter();
        parameter.setParentEntryNr(parentEntryNr);
        parameter.setEntryNr(sequence.nextValue());
        parameter.setId(parameterId);
        parameterList.add(parameter);
        return parameter;
    }

    @SuppressWarnings("unchecked")
    private BasicBillingInformation parseParameterPeriodFee(Node nodeParameter,
            Node nodePeriodFee, RDOParameter parameter, PriceConverter formatter) {

        BasicBillingInformation basicBillingInfo = null;

        if (nodeParameter != null && nodePeriodFee != null) {
            // read PARAMETERVALUE and -TYPE
            String parameterValue = null;
            String parameterType = null;
            Node value = XMLConverter.getLastChildNode(nodeParameter,
                    BillingResultXMLTags.PARAMETER_VALUE_NODE_NAME);
            if (value != null) {
                parameterValue = XMLConverter.getStringAttValue(value,
                        BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME);
                parameterType = XMLConverter.getStringAttValue(value,
                        BillingResultXMLTags.TYPE_ATTRIBUTE_NAME);
            }

            // STEPPEDPRICES
            ListPriceResult steppedPrices = null;
            if (parameterValue != null
                    && ("INTEGER".equals(parameterType) || "LONG"
                            .equals(parameterType))) {
                steppedPrices = getSteppedPrices(nodePeriodFee, formatter,
                        parameter.getId(), parameter.getEntryNr(), false);
                parameter
                        .setSteppedPrices((List<RDOSteppedPrice>) steppedPrices.list);
            }

            if (steppedPrices == null || steppedPrices.list.isEmpty()) {
                // PARAMETERVALUE
                parameter.setValue(parameterValue);

                // VALUEFACTOR
                BigDecimal valueFactor = XMLConverter.getBigDecimalAttValue(
                        nodePeriodFee,
                        BillingResultXMLTags.VALUE_FACTOR_ATTRIBUTE_NAME);
                parameter.setValueFactor(formatter.getValueToDisplay(
                        valueFactor, true));

                basicBillingInfo = readBasicBillingInformation(nodePeriodFee,
                        parameter, formatter, valueFactor);
            } else {
                // parse PRICE but don't set as parameter attribute
                basicBillingInfo = new BasicBillingInformation(parseBigDecimal(
                        nodePeriodFee,
                        BillingResultXMLTags.PRICE_ATTRIBUTE_NAME), false);
            }
        }

        return basicBillingInfo;
    }

    private ParameterOptionInformation getParameterOption(
            String typePeriodOrUser, Node nodeOptions, PriceConverter formatter) {
        ParameterOptionInformation result = null;

        if (nodeOptions != null) {
            // An <Options> node may contain only one <Option> child node!
            NodeList nodes = nodeOptions.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {

                Node nodeOption = nodes.item(i);
                if (BillingResultXMLTags.OPTION_NODE_NAME.equals(nodeOption
                        .getNodeName())) {
                    // get fee of type 'typePeriodOrUser'
                    Node nodeFee = XMLConverter.getLastChildNode(nodeOption,
                            typePeriodOrUser);
                    if (nodeFee != null) {
                        RDOOption option = new RDOOption();
                        option.setEntryNr(sequence.nextValue());
                        option.setValue(XMLConverter.getStringAttValue(
                                nodeOption,
                                BillingResultXMLTags.ID_ATTRIBUTE_NAME));

                        result = new ParameterOptionInformation(option);
                        result.basicBillingInfo = readBasicBillingInformation(
                                nodeFee, option, formatter);
                    }

                    break;
                }
            }
        }

        return result;
    }

    /**
     * Creates and fills the stepped price related data objects for an object.
     * 
     * @param nodeParent
     *            the parent document node
     * @param formatter
     *            the price converter to use for displaying prices.
     * @return an list of <code>RDOSteppedPriceData</code>
     */
    private ListPriceResult getSteppedPrices(Node nodeParent,
            PriceConverter formatter, String eventId, int parentEntryNr,
            boolean isEventSteppedPrice) {

        List<RDOSteppedPrice> listSteppedPrices = new ArrayList<RDOSteppedPrice>();
        BigDecimal total = BigDecimal.ZERO
                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

        Node nodeSteppedPrices = XMLConverter.getLastChildNode(nodeParent,
                BillingResultXMLTags.STEPPED_PRICES_NODE_NAME);
        if (nodeSteppedPrices != null) {
            total = parseBigDecimal(nodeSteppedPrices,
                    BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME);

            NodeList steppedPrices = nodeSteppedPrices.getChildNodes();
            for (int index = 0; index < steppedPrices.getLength(); index++) {

                Node nodeSteppedPrice = steppedPrices.item(index);
                if (BillingResultXMLTags.STEPPED_PRICE_NODE_NAME
                        .equals(nodeSteppedPrice.getNodeName())) {

                    // BASE PRICE
                    BigDecimal basePrice = parseBigDecimal(nodeSteppedPrice,
                            BillingResultXMLTags.BASE_PRICE_ATTRIBUTE_NAME);

                    // STEP ENTITY COUNT
                    BigDecimal stepEntityCount = parseBigDecimal(
                            nodeSteppedPrice,
                            BillingResultXMLTags.STEP_ENTITY_COUNT_ATTRIBUTE_NAME);
                    stepEntityCount.setScale(ValueRounder.SCALING_FACTORS,
                            BigDecimal.ROUND_HALF_UP);

                    // reports do not need to display empty steps
                    if (stepEntityCount.compareTo(BigDecimal.ZERO) != 0
                            && basePrice.compareTo(BigDecimal.ZERO) != 0) {

                        // create rdo and fill...
                        RDOSteppedPrice steppedPrice = new RDOSteppedPrice();
                        steppedPrice.setBasePrice(formatter.getValueToDisplay(
                                basePrice, true));
                        steppedPrice.setId(eventId);
                        steppedPrice.setParentEntryNr(parentEntryNr);
                        steppedPrice.setEntryNr(sequence.nextValue());

                        // LIMIT
                        String limit = XMLConverter.getStringAttValue(
                                nodeSteppedPrice,
                                BillingResultXMLTags.LIMIT_ATTRIBUTE_NAME);
                        steppedPrice.setLimit(limit);

                        // PRICE
                        BigDecimal price = parseBigDecimal(nodeSteppedPrice,
                                BillingResultXMLTags.STEP_AMOUNT_ATTRIBUTE_NAME);
                        if (BillingResultXMLTags.PERIOD_FEE_NODE_NAME
                                .equals(nodeParent.getNodeName())) {
                            // A parameter period fee contains stepped
                            // prices -> we must consider the period fee
                            // factor when computing the price!

                            double periodFeeFactor = XMLConverter
                                    .getDoubleAttValue(
                                            nodeParent,
                                            BillingResultXMLTags.FACTOR_ATTRIBUTE_NAME);
                            price = BigDecimals
                                    .multiply(price, periodFeeFactor);
                            steppedPrice.setQuantity(ValueRounder.roundValue(
                                    stepEntityCount,
                                    formatter.getActiveLocale(), 0));
                            steppedPrice.setFactor(buildFactorForSteppedPrice(
                                    BigDecimal.valueOf(periodFeeFactor),
                                    formatter.getActiveLocale(),
                                    isEventSteppedPrice));
                        } else {
                            steppedPrice.setFactor(buildFactorForSteppedPrice(
                                    stepEntityCount,
                                    formatter.getActiveLocale(),
                                    isEventSteppedPrice));
                        }
                        steppedPrice.setPrice(ValueRounder.roundValue(
                                formatter, price, new BigDecimal[0]));

                        listSteppedPrices.add(steppedPrice);
                    }
                }
            }

        }

        return new ListPriceResult(listSteppedPrices, total);
    }

    /**
     * @return never null
     */
    private BigDecimal parseBigDecimal(Node node, String attName) {
        String valueAsString = XMLConverter.getStringAttValue(node, attName);
        try {
            return parser.parse(valueAsString);
        } catch (ParseException e) {
            throw new NumberFormatException("The String '" + valueAsString
                    + "' could not be parsed.");
        }
    }

    private String buildFactorForSteppedPrice(BigDecimal factor, Locale locale,
            boolean isEventSteppedPrice) {
        if (isEventSteppedPrice) {
            return ValueRounder.roundValue(factor, locale, 0);
        } else {
            return ValueRounder.roundValue(factor, locale,
                    ValueRounder.SCALING_FACTORS);
        }
    }
}
