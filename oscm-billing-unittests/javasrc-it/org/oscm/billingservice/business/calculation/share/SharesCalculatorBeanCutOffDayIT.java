/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                  
 *                                                                              
 *  Creation Date: 29.08.2012                                                      
 *                                                                                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import static org.oscm.test.matchers.JavaMatchers.hasOneItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.billingservice.business.model.suppliershare.OfferingType;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceBean;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingSharesResult;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.interceptor.DateFactory;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.BillingResults;
import org.oscm.test.data.CatalogEntries;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * Integration test for the share calculations with cut-off day
 * 
 * @author stavreva
 */
public class SharesCalculatorBeanCutOffDayIT extends EJBTestBase {

    private static final long DAY_MILLIS = 86400000L;
    private static final long PERIOD_START = getStartMonth(
            System.currentTimeMillis(), 1);
    private static final long PERIOD_END = getEndMonth(
            System.currentTimeMillis(), 1);
    private static final long PERIOD_START_NO_RESULTS = getStartMonth(
            System.currentTimeMillis(), 2);
    private static final long PERIOD_END_NO_RESULTS = getEndMonth(
            System.currentTimeMillis(), 2);
    private static final int NUM_BILLING_RESULTS = 10;

    private static final BigDecimal NET_REVENUE = BigDecimal.valueOf(50);
    private static final BigDecimal GROSS_REVENUE = BigDecimal.valueOf(23.4);
    private static final BigDecimal REVENUE_SHARE_PERCENT = BigDecimal.TEN;
    private static final BigDecimal REVENUE_SHARE = BigDecimal.valueOf(5);
    private static final BigDecimal MP_REVENUE_SHARE = BigDecimal.valueOf(1);
    private static final BigDecimal MP_BROKER_REVENUE_SHARE = BigDecimal.ONE;
    private static final BigDecimal MP_RESELLER_REVENUE_SHARE = BigDecimal.ONE;

    private static final List<String> SupplierResult_RevenueShareDetails_DIRECT = Arrays
            .asList(null, null, null, null, "0.50", "1.00", "50.00", "49.50");
    private static final List<String> SupplierResult_RevenueShareDetails_BROKER = Arrays
            .asList("5.00", "10.00", null, null, "0.50", "1.00", "50.00",
                    "44.50");
    private static final List<String> SupplierResult_RevenueShareDetails_RESELLER = Arrays
            .asList(null, null, "5.00", "10.00", "0.50", "1.00", "50.00",
                    "44.50");
    private static final List<String> SupplierResult_RevenuePerMarketplace = Arrays
            .asList("1385.00", "50.00", "50.00", "15.00", "1500.00");
    private static final List<String> MarketplaceOwnerResult_RevenueShareDetails_DIRECT = Arrays
            .asList("50.00", "1.00", "0.50", "0.00", "0.00", "0.50", "49.50",
                    "0.00", "0.00", "0.00");
    private static final List<String> MarketplaceOwnerResult_RevenueShareDetails_BROKER = Arrays
            .asList("50.00", "1.00", "0.50", "0.00", "0.00", "0.50", "44.50",
                    "10.00", "5.00", "5.00");
    private static final List<String> MarketplaceOwnerResult_RevenueShareDetails_RESELLER = Arrays
            .asList("50.00", "1.00", "0.50", "0.00", "0.00", "0.50", "44.50",
                    "10.00", "5.00", "5.00");

    private DataService ds;
    private Organization supplier;
    private Organization customer;
    private Organization broker;
    private Organization reseller;
    private Marketplace mp;
    private Product product;
    private SharesCalculatorLocal calculator;
    private long transactionTime;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        container.addBean(new SharesDataRetrievalServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(new SharesCalculatorBean());
        calculator = container.get(SharesCalculatorLocal.class);
        givenTransactionTime();
    }

    @Test
    public void performBrokerSharesCalculationRun() throws Exception {

        // given a billing results for products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator.performBrokerSharesCalculationRun(PERIOD_START, PERIOD_END);

        // then
        String xmlAsString = loadResultXML(broker,
                BillingSharesResultType.BROKER, PERIOD_START, PERIOD_END);
        verify_ResaleOrganizationResult(xmlAsString, supplier, broker,
                OrganizationRoleType.BROKER);
        verifyTransactionTimeSet();
    }

    @Test
    public void performBrokerSharesCalculationRun_Empty() throws Exception {

        // given a billing results for products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator.performBrokerSharesCalculationRun(PERIOD_START_NO_RESULTS,
                PERIOD_END_NO_RESULTS);

        // then
        List<BillingSharesResult> result = loadSharesResult(broker.getKey(),
                PERIOD_START_NO_RESULTS, PERIOD_END_NO_RESULTS,
                BillingSharesResultType.BROKER);
        assertThat(result, hasOneItem());
        String xmlAsString = loadResultXML(broker,
                BillingSharesResultType.BROKER, PERIOD_START_NO_RESULTS,
                PERIOD_END_NO_RESULTS);
        Document xml = XMLConverter.convertToDocument(xmlAsString, false);
        verify_Empty(xml, getRoleString(OrganizationRoleType.BROKER));

    }

    @Test
    public void performResellerSharesCalculationRun() throws Exception {

        // given a billing results for products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator
                .performResellerSharesCalculationRun(PERIOD_START, PERIOD_END);

        // then
        String xmlAsString = loadResultXML(reseller,
                BillingSharesResultType.RESELLER, PERIOD_START, PERIOD_END);
        verify_ResaleOrganizationResult(xmlAsString, supplier, reseller,
                OrganizationRoleType.RESELLER);
    }

    @Test
    public void performResellerSharesCalculationRun_Empty() throws Exception {

        // given a billing results for products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator.performResellerSharesCalculationRun(PERIOD_START_NO_RESULTS,
                PERIOD_END_NO_RESULTS);

        // then
        List<BillingSharesResult> result = loadSharesResult(reseller.getKey(),
                PERIOD_START_NO_RESULTS, PERIOD_END_NO_RESULTS,
                BillingSharesResultType.RESELLER);
        assertThat(result, hasOneItem());
        String xmlAsString = loadResultXML(reseller,
                BillingSharesResultType.RESELLER, PERIOD_START_NO_RESULTS,
                PERIOD_END_NO_RESULTS);
        Document xml = XMLConverter.convertToDocument(xmlAsString, false);
        verify_Empty(xml, getRoleString(OrganizationRoleType.RESELLER));

    }

    @Test
    public void performSupplierSharesCalculationRun() throws Exception {

        // given a billing results for products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator
                .performSupplierSharesCalculationRun(PERIOD_START, PERIOD_END);

        // then
        String xmlAsString = loadResultXML(supplier,
                BillingSharesResultType.SUPPLIER, PERIOD_START, PERIOD_END);
        verify_SupplierResult(xmlAsString);

    }

    @Test
    public void performSupplierSharesCalculationRun_Empty() throws Exception {

        // given a billing results for products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator.performSupplierSharesCalculationRun(PERIOD_START_NO_RESULTS,
                PERIOD_END_NO_RESULTS);

        // then
        List<BillingSharesResult> result = loadSharesResult(supplier.getKey(),
                PERIOD_START_NO_RESULTS, PERIOD_END_NO_RESULTS,
                BillingSharesResultType.SUPPLIER);
        assertThat(result, hasOneItem());
        String xmlAsString = loadResultXML(supplier,
                BillingSharesResultType.SUPPLIER, PERIOD_START_NO_RESULTS,
                PERIOD_END_NO_RESULTS);
        Document xml = XMLConverter.convertToDocument(xmlAsString, false);
        verify_Empty(xml, getRoleString(OrganizationRoleType.SUPPLIER));

    }

    @Test
    public void performMarketplacesSharesCalculationRun() throws Exception {

        // given a billing results for products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator.performMarketplacesSharesCalculationRun(PERIOD_START,
                PERIOD_END);

        // then
        String xmlAsString = loadResultXML(supplier,
                BillingSharesResultType.MARKETPLACE_OWNER, PERIOD_START,
                PERIOD_END);
        verify_MarketplaceOwnerResult(xmlAsString);

    }

    @Test
    public void performMarketplacesSharesCalculationRun_Empty()
            throws Exception {

        // given a billing results for products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator.performMarketplacesSharesCalculationRun(
                PERIOD_START_NO_RESULTS, PERIOD_END_NO_RESULTS);

        // then
        List<BillingSharesResult> result = loadSharesResult(supplier.getKey(),
                PERIOD_START_NO_RESULTS, PERIOD_END_NO_RESULTS,
                BillingSharesResultType.MARKETPLACE_OWNER);
        assertThat(result, hasOneItem());
        String xmlAsString = loadResultXML(supplier,
                BillingSharesResultType.MARKETPLACE_OWNER,
                PERIOD_START_NO_RESULTS, PERIOD_END_NO_RESULTS);
        Document xml = XMLConverter.convertToDocument(xmlAsString, false);
        verify_Empty(xml, getRoleString(OrganizationRoleType.MARKETPLACE_OWNER));

    }

    private void createSupplierProductAndSubscriptions(
            final int numBillingResults) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(ds, "supplierId",
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                customer = Organizations.createOrganization(ds, "customerId",
                        OrganizationRoleType.CUSTOMER);
                Organization customer1 = Organizations.createOrganization(ds,
                        "customerId1", OrganizationRoleType.CUSTOMER);
                mp = Marketplaces.createGlobalMarketplace(supplier, "mp_"
                        + supplier.getOrganizationId(), ds, MP_REVENUE_SHARE,
                        MP_BROKER_REVENUE_SHARE, MP_RESELLER_REVENUE_SHARE);

                int num = numBillingResults;
                for (int i = 0; i < num; i++) {

                    long billingPeriodStart = PERIOD_START - i * DAY_MILLIS;
                    long billingPeriodEnd = PERIOD_END - i * DAY_MILLIS;
                    int cutOffDay = getCutOffDay(billingPeriodStart);
                    if (cutOffDay > 28) {
                        num++;
                        continue;
                    }

                    product = Products.createProduct(supplier, "techProd",
                            true, "product_supplier_" + i, "priceModelId_" + i,
                            ds);

                    CatalogEntries.create(ds, mp, product);

                    Subscription sub = Subscriptions.createSubscription(ds,
                            customer.getOrganizationId(), product, mp,
                            cutOffDay);

                    BillingResults.createBillingResult(ds, sub,
                            supplier.getKey(), supplier.getKey(),
                            billingPeriodStart, billingPeriodEnd, NET_REVENUE,
                            GROSS_REVENUE, "<PriceModel id=\""
                                    + sub.getPriceModel().getKey()
                                    + "\"><PriceModelCosts amount=\""
                                    + NET_REVENUE
                                    + "\"></PriceModelCosts></PriceModel>");
                }

                Subscription sub = Subscriptions.createSubscription(ds,
                        customer1.getOrganizationId(), product, mp, 1);

                BillingResults.createBillingResult(ds, sub, supplier.getKey(),
                        supplier.getKey(),
                        getStartMonth(System.currentTimeMillis(), 3),
                        getEndMonth(System.currentTimeMillis(), 3),
                        NET_REVENUE, GROSS_REVENUE, "<PriceModel id=\""
                                + sub.getPriceModel().getKey()
                                + "\"><PriceModelCosts amount=\"" + NET_REVENUE
                                + "\"></PriceModelCosts></PriceModel>");

                return null;
            }
        });
    }

    private void givenBillingResult() throws Exception {
        createSupplierProductAndSubscriptions(NUM_BILLING_RESULTS);
        createBrokerProductAndSubscriptions(NUM_BILLING_RESULTS);
        createResellerProductAndSubscriptions(NUM_BILLING_RESULTS);
    }

    private void givenTransactionTime() {
        transactionTime = DateFactory.getInstance().getTransactionTime();
    }

    private void verifyTransactionTimeSet() {
        assertEquals(Boolean.FALSE, Boolean.valueOf(transactionTime == 0));
        assertEquals("Transaction time not set.", Boolean.FALSE,
                Boolean.valueOf(transactionTime == DateFactory.getInstance()
                        .getTransactionTime()));
    }

    @SuppressWarnings({ "unchecked", "boxing" })
    private List<BillingSharesResult> loadSharesResult(final long orgKey,
            final long fromDate, final long toDate,
            final BillingSharesResultType type) throws Exception {
        return runTX(new Callable<List<BillingSharesResult>>() {
            @Override
            public List<BillingSharesResult> call() {
                Query query = ds
                        .createNamedQuery("BillingSharesResult.getSharesResultForOrganization");
                query.setParameter("orgKey", orgKey);
                query.setParameter("resultType", type);
                query.setParameter("fromDate", fromDate);
                query.setParameter("toDate", toDate);
                List<BillingSharesResult> result = query.getResultList();
                return result;
            }
        });
    }

    private void createBrokerProductAndSubscriptions(final int numBillingResults)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                broker = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER.name(),
                        OrganizationRoleType.BROKER);

                int num = numBillingResults;
                for (int i = 0; i < num; i++) {

                    long billingPeriodStart = PERIOD_START - i * DAY_MILLIS;
                    long billingPeriodEnd = PERIOD_END - i * DAY_MILLIS;
                    int cutOffDay = getCutOffDay(billingPeriodStart);
                    if (cutOffDay > 28) {
                        num++;
                        continue;
                    }

                    Product resaleCopy = Products.createProductResaleCopy(
                            product, broker, ds);
                    resaleCopy.setProductId("resaleProd_" + i
                            + OrganizationRoleType.BROKER.name());

                    // publish to marketplace
                    CatalogEntries.createWithBrokerShare(ds, mp, resaleCopy,
                            REVENUE_SHARE_PERCENT);

                    // subscribe to resale copy
                    Subscription sub = Subscriptions.createSubscription(ds,
                            customer.getOrganizationId(), resaleCopy, mp,
                            cutOffDay);

                    BillingResults.createBillingResult(ds, sub,
                            broker.getKey(), broker.getKey(),
                            billingPeriodStart, billingPeriodEnd, NET_REVENUE,
                            GROSS_REVENUE, "<PriceModel id=\""
                                    + sub.getPriceModel().getKey()
                                    + "\"><PriceModelCosts amount=\""
                                    + NET_REVENUE
                                    + "\"></PriceModelCosts></PriceModel>");
                }
                return null;
            }
        });
    }

    private void createResellerProductAndSubscriptions(
            final int numBillingResults) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER.name(),
                        OrganizationRoleType.RESELLER);

                int num = numBillingResults;
                for (int i = 0; i < num; i++) {

                    long billingPeriodStart = PERIOD_START - i * DAY_MILLIS;
                    long billingPeriodEnd = PERIOD_END - i * DAY_MILLIS;
                    int cutOffDay = getCutOffDay(billingPeriodStart);
                    if (cutOffDay > 28) {
                        num++;
                        continue;
                    }

                    Product resaleCopy = Products.createProductResaleCopy(
                            product, reseller, ds);
                    resaleCopy.setProductId("resaleProd_" + i
                            + OrganizationRoleType.RESELLER.name());

                    // publish to marketplace
                    CatalogEntries.createWithResellerShare(ds, mp, resaleCopy,
                            REVENUE_SHARE_PERCENT);

                    // subscribe to resale copy
                    Subscription sub = Subscriptions.createSubscription(ds,
                            customer.getOrganizationId(), resaleCopy, mp,
                            cutOffDay);

                    BillingResults.createBillingResult(ds, sub,
                            reseller.getKey(), reseller.getKey(),
                            billingPeriodStart, billingPeriodEnd, NET_REVENUE,
                            GROSS_REVENUE, "<PriceModel id=\""
                                    + sub.getPriceModel().getKey()
                                    + "\"><PriceModelCosts amount=\""
                                    + NET_REVENUE
                                    + "\"></PriceModelCosts></PriceModel>");
                }
                return null;
            }
        });
    }

    private void verify_ResaleOrganizationResult(String xmlAsString,
            Organization supplier, Organization org, OrganizationRoleType role)
            throws Exception {

        Document xml = XMLConverter.convertToDocument(xmlAsString, false);
        String roleString = getRoleString(role);
        OfferingType offerType = getOfferingType(role);
        assertNotNull(roleString);
        assertNotNull(offerType);

        verify_RootNode(xml, org, roleString);
        verify_Period(xml, roleString);
        verify_Currency(xml, roleString);
        verify_Supplier(xml, supplier, roleString);
        verify_ResaleResult_Service(xml, NUM_BILLING_RESULTS, roleString);
        verify_ServiceRevenue(xml, roleString);
        verify_ResaleOrganization_RevenuePerSupplier(xml, roleString);
        verify_ResaleOrganization_Revenue(xml, roleString);
    }

    private void verify_SupplierResult(String xmlAsString) throws Exception {
        Document xml = XMLConverter.convertToDocument(xmlAsString, false);
        String roleString = getRoleString(OrganizationRoleType.SUPPLIER);
        verify_RootNode(xml, supplier, roleString);
        verify_Period(xml, roleString);
        verify_Currency(xml, roleString);
        verify_Marketplace(xml, mp, roleString);
        verify_SupplierResult_Service(xml, null, OfferingType.DIRECT,
                roleString);
        verify_SupplierResult_Service(xml, broker, OfferingType.BROKER,
                roleString);
        verify_SupplierResult_Service(xml, reseller, OfferingType.RESELLER,
                roleString);
    }

    private void verify_MarketplaceOwnerResult(String xmlAsString)
            throws Exception {

        Document xml = XMLConverter.convertToDocument(xmlAsString, false);
        String roleString = getRoleString(OrganizationRoleType.MARKETPLACE_OWNER);
        verify_RootNode(xml, supplier, roleString);
        verify_Period(xml, roleString);
        verify_Currency(xml, roleString);
        verify_Marketplace(xml, mp, roleString);
        verify_MarketplaceOwnerResult_Service(xml, null, OfferingType.DIRECT,
                roleString);
        verify_MarketplaceOwnerResult_Service(xml, broker, OfferingType.BROKER,
                roleString);
        verify_MarketplaceOwnerResult_Service(xml, reseller,
                OfferingType.RESELLER, roleString);
        verify_RevenuesOverAllMarketplaces(xml);
    }

    private void verify_RootNode(Document xml, Organization org,
            String roleString) throws XPathExpressionException {
        Node rootNode = XMLConverter.getNodeByXPath(xml, "/" + roleString
                + "RevenueShareResult");
        assertEquals(org.getKey(),
                XMLConverter.getLongAttValue(rootNode, "organizationKey"));
        assertEquals(org.getOrganizationId(),
                XMLConverter.getStringAttValue(rootNode, "organizationId"));
    }

    private void verify_Period(Document xml, String roleString)
            throws XPathExpressionException {
        Node period = XMLConverter.getNodeByXPath(xml, "/" + roleString
                + "RevenueShareResult/Period");
        assertEquals(PERIOD_START,
                XMLConverter.getLongAttValue(period, "startDate"));
        assertEquals(PERIOD_END,
                XMLConverter.getLongAttValue(period, "endDate"));
    }

    private void verify_Currency(Document xml, String roleString)
            throws XPathExpressionException {
        Node currency = XMLConverter.getNodeByXPath(xml, "/" + roleString
                + "RevenueShareResult/Currency");
        assertEquals("EUR", XMLConverter.getStringAttValue(currency, "id"));
    }

    private void verify_Empty(Document xml, String roleString)
            throws XPathExpressionException {
        NodeList currency = XMLConverter.getNodeListByXPath(xml, "/"
                + roleString + "RevenueShareResult/Currency");
        assertEquals(0, currency.getLength());
    }

    private void verify_Supplier(Document xml, Organization supplier,
            String roleString) throws XPathExpressionException {
        Node organizationData = XMLConverter.getNodeByXPath(xml, "/"
                + roleString
                + "RevenueShareResult/Currency/Supplier/OrganizationData");
        assertEquals(supplier.getKey(),
                XMLConverter.getLongAttValue(organizationData, "key"));
    }

    private void verify_ResaleOrganization(Node service, Organization resaleOrg) {
        String model = XMLConverter.getStringAttValue(service, "model");

        String resaleRole = null;
        if (OfferingType.BROKER.name().equals(model)) {
            resaleRole = "Broker";
        } else if (OfferingType.RESELLER.name().equals(model)) {
            resaleRole = "Reseller";
        }
        assertNotNull(resaleRole);

        Node org = XMLConverter.getLastChildNode(service, resaleRole);
        Node organizationData = XMLConverter.getLastChildNode(org,
                "OrganizationData");

        assertEquals(resaleOrg.getKey(),
                XMLConverter.getLongAttValue(organizationData, "key"));

    }

    private void verify_ServiceRevenue(Document xml, String roleString)
            throws XPathExpressionException {
        Node serviceRevenue = XMLConverter
                .getNodeByXPath(
                        xml,
                        "/"
                                + roleString
                                + "RevenueShareResult/Currency/Supplier/Service/ServiceRevenue");
        assertEquals(
                REVENUE_SHARE.intValue() + ".00",
                XMLConverter.getStringAttValue(serviceRevenue,
                        roleString.toLowerCase() + "Revenue"));
        assertEquals(
                REVENUE_SHARE_PERCENT.intValue() + ".00",
                XMLConverter.getStringAttValue(serviceRevenue,
                        roleString.toLowerCase() + "RevenueSharePercentage"));
        assertEquals(REVENUE_SHARE.intValue() * NUM_BILLING_RESULTS + ".00",
                XMLConverter.getStringAttValue(serviceRevenue, "totalAmount"));
    }

    private void verify_SupplierResult_RevenueShareDetails(Node service) {

        String model = XMLConverter.getStringAttValue(service, "model");

        List<String> expected = new ArrayList<String>();
        if (OfferingType.DIRECT.name().equals(model)) {
            expected = SupplierResult_RevenueShareDetails_DIRECT;
        } else if (OfferingType.BROKER.name().equals(model)) {
            expected = SupplierResult_RevenueShareDetails_BROKER;
        } else if (OfferingType.RESELLER.name().equals(model)) {
            expected = SupplierResult_RevenueShareDetails_RESELLER;
        }
        assertEquals(8, expected.size());

        Node revenueShareDetails = XMLConverter.getLastChildNode(service,
                "RevenueShareDetails");
        assertEquals(expected.get(0), XMLConverter.getStringAttValue(
                revenueShareDetails, "brokerRevenue"));
        assertEquals(expected.get(1), XMLConverter.getStringAttValue(
                revenueShareDetails, "brokerRevenueSharePercentage"));
        assertEquals(expected.get(2), XMLConverter.getStringAttValue(
                revenueShareDetails, "resellerRevenue"));
        assertEquals(expected.get(3), XMLConverter.getStringAttValue(
                revenueShareDetails, "resellerRevenueSharePercentage"));
        assertEquals(expected.get(4), XMLConverter.getStringAttValue(
                revenueShareDetails, "marketplaceRevenue"));
        assertEquals(expected.get(5), XMLConverter.getStringAttValue(
                revenueShareDetails, "marketplaceRevenueSharePercentage"));
        assertEquals(expected.get(6), XMLConverter.getStringAttValue(
                revenueShareDetails, "serviceRevenue"));
        assertEquals(expected.get(7), XMLConverter.getStringAttValue(
                revenueShareDetails, "amountForSupplier"));
    }

    private void verify_MarketplaceOwnerResult_RevenueShareDetails(
            Node service, OfferingType type) {

        String model = XMLConverter.getStringAttValue(service, "model");

        List<String> expected = new ArrayList<String>();
        if (OfferingType.DIRECT.name().equals(model)) {
            expected = MarketplaceOwnerResult_RevenueShareDetails_DIRECT;
        } else if (OfferingType.BROKER.name().equals(model)) {
            expected = MarketplaceOwnerResult_RevenueShareDetails_BROKER;
        } else if (OfferingType.RESELLER.name().equals(model)) {
            expected = MarketplaceOwnerResult_RevenueShareDetails_RESELLER;
        }
        assertEquals(NUM_BILLING_RESULTS, expected.size());

        Node revenueShareDetails = XMLConverter.getLastChildNode(service,
                "RevenueShareDetails");
        assertEquals(expected.get(0), XMLConverter.getStringAttValue(
                revenueShareDetails, "serviceRevenue"));
        assertEquals(expected.get(1), XMLConverter.getStringAttValue(
                revenueShareDetails, "marketplaceRevenueSharePercentage"));
        assertEquals(expected.get(2), XMLConverter.getStringAttValue(
                revenueShareDetails, "marketplaceRevenue"));
        assertEquals(expected.get(6), XMLConverter.getStringAttValue(
                revenueShareDetails, "amountForSupplier"));
        if (OfferingType.BROKER.equals(type)) {
            assertEquals(expected.get(7), XMLConverter.getStringAttValue(
                    revenueShareDetails, "brokerRevenueSharePercentage"));
            assertEquals(expected.get(8), XMLConverter.getStringAttValue(
                    revenueShareDetails, "brokerRevenue"));
        } else if (OfferingType.RESELLER.equals(type)) {
            assertEquals(expected.get(7), XMLConverter.getStringAttValue(
                    revenueShareDetails, "resellerRevenueSharePercentage"));
            assertEquals(expected.get(8), XMLConverter.getStringAttValue(
                    revenueShareDetails, "resellerRevenue"));
        }
    }

    private void verify_ResaleOrganization_RevenuePerSupplier(Document xml,
            String roleString) throws XPathExpressionException {
        Node resaleRevenuePerSupplier = XMLConverter.getNodeByXPath(xml, "/"
                + roleString + "RevenueShareResult/Currency/Supplier/"
                + roleString + "RevenuePerSupplier");
        assertEquals(REVENUE_SHARE.intValue() * NUM_BILLING_RESULTS + ".00",
                XMLConverter.getStringAttValue(resaleRevenuePerSupplier,
                        "amount"));
    }

    private void verify_ResaleOrganization_Revenue(Document xml,
            String roleString) throws XPathExpressionException {
        Node revenue = XMLConverter.getNodeByXPath(xml, "/" + roleString
                + "RevenueShareResult/Currency/" + roleString + "Revenue");
        assertEquals(REVENUE_SHARE.intValue() * NUM_BILLING_RESULTS + ".00",
                XMLConverter.getStringAttValue(revenue, "amount"));
    }

    private void verify_Marketplace(Document xml, Marketplace mp,
            String roleString) throws XPathExpressionException {

        Node marketplace = XMLConverter.getNodeByXPath(xml, "/" + roleString
                + "RevenueShareResult/Currency/Marketplace");
        assertEquals(mp.getMarketplaceId(),
                XMLConverter.getStringAttValue(marketplace, "id"));
        if (roleString.equals("Supplier")) {
            verify_SupplierResult_MarketplaceRevenue(marketplace);
        } else {
            Node revenue = XMLConverter.getLastChildNode(marketplace,
                    "RevenuesPerMarketplace");
            verify_RevenuesPerMarketplace_Suppliers(revenue);
            verify_RevenuesPerMarketplace_Brokers(revenue);
            verify_RevenuesPerMarketplace_Resellers(revenue);
        }
    }

    private void verify_RevenuesPerMarketplace_Suppliers(Node revenue) {
        Node suppliers = XMLConverter.getLastChildNode(revenue, "Suppliers");
        Node org = XMLConverter.getLastChildNode(suppliers, "Organization");
        assertEquals(supplier.getOrganizationId(),
                XMLConverter.getStringAttValue(org, "identifier"));
        assertEquals("1385.00", XMLConverter.getStringAttValue(org, "amount"));
    }

    private void verify_RevenuesPerMarketplace_Brokers(Node revenue) {
        Node brokers = XMLConverter.getLastChildNode(revenue, "Brokers");
        Node org = XMLConverter.getLastChildNode(brokers, "Organization");
        assertEquals(broker.getOrganizationId(),
                XMLConverter.getStringAttValue(org, "identifier"));
        assertEquals("50.00", XMLConverter.getStringAttValue(org, "amount"));
    }

    private void verify_RevenuesPerMarketplace_Resellers(Node revenue) {
        Node resellers = XMLConverter.getLastChildNode(revenue, "Resellers");
        Node org = XMLConverter.getLastChildNode(resellers, "Organization");
        assertEquals(reseller.getOrganizationId(),
                XMLConverter.getStringAttValue(org, "identifier"));
        assertEquals("50.00", XMLConverter.getStringAttValue(org, "amount"));
    }

    private void verify_RevenuesOverAllMarketplaces(Document xml)
            throws XPathExpressionException {
        Node allMarketplaces = XMLConverter
                .getNodeByXPath(xml,
                        "/MarketplaceOwnerRevenueShareResult/Currency/RevenuesOverAllMarketplaces");
        verify_RevenuesPerMarketplace_Suppliers(allMarketplaces);
        verify_RevenuesPerMarketplace_Brokers(allMarketplaces);
        verify_RevenuesPerMarketplace_Resellers(allMarketplaces);
    }

    private void verify_SupplierResult_MarketplaceRevenue(Node mp) {

        Node revenuePerMp = XMLConverter.getLastChildNode(mp,
                "RevenuePerMarketplace");
        assertEquals(SupplierResult_RevenuePerMarketplace.get(0),
                XMLConverter.getStringAttValue(revenuePerMp, "overallRevenue"));
        assertEquals(SupplierResult_RevenuePerMarketplace.get(1),
                XMLConverter.getStringAttValue(revenuePerMp, "brokerRevenue"));
        assertEquals(SupplierResult_RevenuePerMarketplace.get(2),
                XMLConverter.getStringAttValue(revenuePerMp, "resellerRevenue"));
        assertEquals(SupplierResult_RevenuePerMarketplace.get(3),
                XMLConverter.getStringAttValue(revenuePerMp,
                        "marketplaceRevenue"));
        assertEquals(SupplierResult_RevenuePerMarketplace.get(4),
                XMLConverter.getStringAttValue(revenuePerMp, "serviceRevenue"));
    }

    private void verify_ResaleResult_Service(Document xml, int numServices,
            String roleString) throws XPathExpressionException {
        NodeList services = XMLConverter.getNodeListByXPath(xml, "/"
                + roleString + "RevenueShareResult/Currency/Supplier/Service");

        assertEquals(numServices, services.getLength());
    }

    private void verify_SupplierResult_Service(Document xml,
            Organization resaleOrg, OfferingType type, String roleString)
            throws XPathExpressionException {

        NodeList services = XMLConverter.getNodeListByXPath(xml, "/"
                + roleString
                + "RevenueShareResult/Currency/Marketplace/Service");

        for (int i = 0; i < services.getLength(); i++) {
            String model = XMLConverter.getStringAttValue(services.item(i),
                    "model");

            if (type.name().equals(model)) {

                verify_SupplierResult_RevenueShareDetails(services.item(i));
                if (resaleOrg != null) {
                    verify_ResaleOrganization(services.item(i), resaleOrg);
                }

            }
        }
    }

    private void verify_MarketplaceOwnerResult_Service(Document xml,
            Organization resaleOrg, OfferingType type, String roleString)
            throws XPathExpressionException {

        NodeList services = XMLConverter.getNodeListByXPath(xml, "/"
                + roleString
                + "RevenueShareResult/Currency/Marketplace/Service");

        for (int i = 0; i < services.getLength(); i++) {
            String model = XMLConverter.getStringAttValue(services.item(i),
                    "model");

            if (type.name().equals(model)) {
                verify_MarketplaceOwnerResult_RevenueShareDetails(
                        services.item(i), type);
                if (resaleOrg != null) {
                    verify_ResaleOrganization(services.item(i), resaleOrg);
                }

            }
        }
    }

    private String loadResultXML(Organization org,
            BillingSharesResultType role, long startPeriod, long endPeriod)
            throws Exception {
        List<BillingSharesResult> result = loadSharesResult(org.getKey(),
                startPeriod, endPeriod, role);
        String xmlAsString = result.get(0).getResultXML();
        System.out.println(xmlAsString);
        return xmlAsString;
    }

    private String getRoleString(OrganizationRoleType role) {

        if (OrganizationRoleType.BROKER.equals(role)) {
            return "Broker";
        }
        if (OrganizationRoleType.RESELLER.equals(role)) {
            return "Reseller";
        }
        if (OrganizationRoleType.SUPPLIER.equals(role)) {
            return "Supplier";
        }
        if (OrganizationRoleType.MARKETPLACE_OWNER.equals(role)) {
            return "MarketplaceOwner";
        }
        return null;
    }

    private OfferingType getOfferingType(OrganizationRoleType role) {

        if (OrganizationRoleType.BROKER.equals(role)) {
            return OfferingType.BROKER;
        }
        if (OrganizationRoleType.RESELLER.equals(role)) {
            return OfferingType.RESELLER;
        }
        if (OrganizationRoleType.SUPPLIER.equals(role)) {
            return OfferingType.DIRECT;
        }
        return null;
    }

    private static long getStartMonth(long baseTime, int numMonths) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(baseTime);
        cal.add(Calendar.MONTH, numMonths);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private static long getEndMonth(long baseTime, int numMonths) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getStartMonth(baseTime, numMonths));
        cal.add(Calendar.MONTH, 1);
        return cal.getTimeInMillis();
    }

    private int getCutOffDay(long baseTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(baseTime);
        return cal.get(Calendar.DAY_OF_MONTH);
    }
}
