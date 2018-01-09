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

import static org.junit.Assert.*;
import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.oscm.test.matchers.JavaMatchers.hasOneItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.oscm.billingservice.business.model.suppliershare.OfferingType;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceBean;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Integration test for the broker share calculation
 * 
 * @author cheld
 */
public class SharesCalculatorBeanIT extends EJBTestBase {

    private static final long BILLING_PERIOD_START = 0L;
    private static final long BILLING_PERIOD_END = System.currentTimeMillis()
            + 1000 * 60;
    private static final BigDecimal NET_REVENUE = new BigDecimal(50);
    private static final BigDecimal GROSS_REVENUE = new BigDecimal(53);
    private static final BigDecimal REVENUE_SHARE_PERCENT = BigDecimal.TEN;
    private static final BigDecimal REVENUE_SHARE = new BigDecimal(5);
    private static final BigDecimal MP_REVENUE_SHARE = BigDecimal.ONE;
    private static final BigDecimal MP_BROKER_REVENUE_SHARE = BigDecimal.ONE;
    private static final BigDecimal MP_RESELLER_REVENUE_SHARE = BigDecimal.ONE;

    List<String> SupplierResult_RevenueShareDetails_DIRECT = Arrays.asList(null,
            null, null, null, "0.50", "1.00", "50.00", "49.50");
    List<String> SupplierResult_RevenueShareDetails_BROKER = Arrays.asList(
            "5.00", "10.00", null, null, "0.50", "1.00", "50.00", "44.50");
    List<String> SupplierResult_RevenueShareDetails_RESELLER = Arrays.asList(
            null, null, "5.00", "10.00", "0.50", "1.00", "50.00", "44.50");
    List<String> SupplierResult_RevenuePerMarketplace = Arrays.asList("138.50",
            "5.00", "5.00", "1.50", "150.00");
    List<String> MarketplaceOwnerResult_RevenueShareDetails_DIRECT = Arrays
            .asList("50.00", "1.00", "0.50", "0.00", "0.00", "0.50", "49.50",
                    "0.00", "0.00", "0.00");
    List<String> MarketplaceOwnerResult_RevenueShareDetails_BROKER = Arrays
            .asList("50.00", "1.00", "0.50", "0.00", "0.00", "0.50", "44.50",
                    "10.00", "5.00", "5.00");
    List<String> MarketplaceOwnerResult_RevenueShareDetails_RESELLER = Arrays
            .asList("50.00", "1.00", "0.50", "0.00", "0.00", "0.50", "44.50",
                    "10.00", "5.00", "5.00");

    private DataService ds;
    private Organization supplier;
    private Organization supplier2;
    private Organization customer;
    private Organization broker;
    private Organization broker2;
    private Organization reseller;
    private Organization reseller2;
    private Marketplace mp;
    private Product product;
    private SharesCalculatorLocal calculator;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        container.addBean(new SharesDataRetrievalServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(new SharesCalculatorBean());
        calculator = container.get(SharesCalculatorLocal.class);
    }

    private void givenBillingResult() throws Exception {
        createSupplierProductAndSubscriptions(true);
        createBrokerProductAndSubscriptions(true);
        createResellerProductAndSubscriptions(true);
    }

    @SuppressWarnings({ "unchecked", "boxing" })
    private List<BillingSharesResult> loadSharesResult(final long orgKey,
            final long fromDate, final long toDate,
            final BillingSharesResultType type) throws Exception {
        return runTX(new Callable<List<BillingSharesResult>>() {
            @Override
            public List<BillingSharesResult> call() {
                Query query = ds.createNamedQuery(
                        "BillingSharesResult.getSharesResultForOrganization");
                query.setParameter("orgKey", orgKey);
                query.setParameter("resultType", type);
                query.setParameter("fromDate", fromDate);
                query.setParameter("toDate", toDate);
                List<BillingSharesResult> result = query.getResultList();
                return result;
            }
        });
    }

    private void createSupplierProductAndSubscriptions(
            final boolean withBillingResult) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(ds, "supplierId",
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                mp = Marketplaces.createGlobalMarketplace(supplier,
                        "mp_" + supplier.getOrganizationId(), ds,
                        MP_REVENUE_SHARE, MP_BROKER_REVENUE_SHARE,
                        MP_RESELLER_REVENUE_SHARE);
                product = Products.createProduct(supplier, "techProd", true,
                        "product_supplier", "priceModelId", ds);
                customer = Organizations.createOrganization(ds, "customerId",
                        OrganizationRoleType.CUSTOMER);
                CatalogEntries.create(ds, mp, product);

                Subscription sub = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), product, mp, 1);

                if (withBillingResult) {
                    BillingResults.createBillingResult(ds, sub,
                            supplier.getKey(), supplier.getKey(),
                            BILLING_PERIOD_START, BILLING_PERIOD_END,
                            NET_REVENUE, GROSS_REVENUE,
                            "<PriceModel id=\"" + sub.getPriceModel().getKey()
                                    + "\"><PriceModelCosts amount=\""
                                    + NET_REVENUE
                                    + "\"></PriceModelCosts></PriceModel>");
                }
                return null;
            }
        });
    }

    private void createSupplierProductAndUnpublish(
            final boolean withBillingResult) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier2 = Organizations.createOrganization(ds, "supplierId2",
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                Product product2 = Products.createProduct(supplier2, "techprod",
                        true, "product_supplier2", "priceModelId2", ds);
                CatalogEntry ce = CatalogEntries.create(ds, mp, product2);

                Subscription sub = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), product2, mp, 1);

                CatalogEntries.updateMarketplace(ds, ce, null);

                if (withBillingResult) {
                    BillingResults.createBillingResult(ds, sub,
                            supplier2.getKey(), supplier2.getKey(),
                            BILLING_PERIOD_START, BILLING_PERIOD_END,
                            NET_REVENUE, GROSS_REVENUE,
                            "<PriceModel id=\"" + sub.getPriceModel().getKey()
                                    + "\"><PriceModelCosts amount=\""
                                    + NET_REVENUE
                                    + "\"></PriceModelCosts></PriceModel>");
                }
                return null;
            }
        });
    }

    @Test
    public void performBrokerSharesCalculationRun() throws Exception {

        // given a billing results for a products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator.performBrokerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then
        String xmlAsString = loadResultXML(broker,
                BillingSharesResultType.BROKER);
        verify_ResaleOrganizationResult(xmlAsString, supplier, broker,
                OrganizationRoleType.BROKER);
    }

    @Test
    public void performResellerSharesCalculationRun() throws Exception {

        // given a billing results for a products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator.performResellerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then
        String xmlAsString = loadResultXML(reseller,
                BillingSharesResultType.RESELLER);
        verify_ResaleOrganizationResult(xmlAsString, supplier, reseller,
                OrganizationRoleType.RESELLER);
    }

    @Test
    public void performSupplierSharesCalculationRun() throws Exception {

        // given a billing results for a products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator.performSupplierSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then
        String xmlAsString = loadResultXML(supplier,
                BillingSharesResultType.SUPPLIER);
        verify_SupplierResult(xmlAsString);

    }

    @Test
    public void performMarketplacesSharesCalculationRun() throws Exception {

        // given a billing results for a products of vendors that are published
        // and subscribed
        givenBillingResult();

        // when
        calculator.performMarketplacesSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then
        String xmlAsString = loadResultXML(supplier,
                BillingSharesResultType.MARKETPLACE_OWNER);
        verify_MarketplaceOwnerResult(xmlAsString);

    }

    /**
     * In case the broker has made no revenue, then the body (broker id +
     * period) of the xml should be generated. So it is clear that the
     * calculation has not failed.
     */
    @Test
    public void performBrokerSharesCalculationRun_noBillingResult()
            throws Exception {

        // given a broker with a published product, but no subscriptions
        givenVendorsWithoutBillingResult();

        // when
        calculator.performBrokerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that root node and period exist
        Document xml = XMLConverter.convertToDocument(
                loadResultXML(broker, BillingSharesResultType.BROKER), false);
        assertNotNull(XMLConverter.getNodeByXPath(xml,
                "/BrokerRevenueShareResult/Period"));
    }

    /**
     * In case the reseller has made no revenue, then the body (reseller id +
     * period) of the xml should be generated. So it is clear that the
     * calculation has not failed.
     */
    @Test
    public void performResellerSharesCalculationRun_noBillingResult()
            throws Exception {

        // given a reseller with a published product, but no subscriptions
        givenVendorsWithoutBillingResult();

        // when
        calculator.performResellerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that root node and period exist
        Document xml = XMLConverter.convertToDocument(
                loadResultXML(reseller, BillingSharesResultType.RESELLER),
                false);
        assertNotNull(XMLConverter.getNodeByXPath(xml,
                "/ResellerRevenueShareResult/Period"));
    }

    /**
     * In case the reseller has made no revenue, then the body (broker id +
     * period) of the xml should be generated. So it is clear that the
     * calculation has not failed.
     */
    @Test
    public void performSupplierSharesCalculationRun_noBillingResult()
            throws Exception {

        // given a reseller with a published product, but no subscriptions
        givenVendorsWithoutBillingResult();

        // when
        calculator.performSupplierSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that root node and period exist
        Document xml = XMLConverter.convertToDocument(
                loadResultXML(supplier, BillingSharesResultType.SUPPLIER),
                false);
        assertNotNull(XMLConverter.getNodeByXPath(xml,
                "/SupplierRevenueShareResult/Period"));
    }

    /**
     * In case the vendors have made no revenue, then the body (broker id +
     * period) of the xml should be generated. So it is clear that the
     * calculation has not failed.
     */
    @Test
    public void performMarketplacesSharesCalculationRun_noBillingResult()
            throws Exception {

        // given vendors(supplier, broker, reseller) with a published product,
        // but no subscriptions
        givenVendorsWithoutBillingResult();

        // when
        calculator.performMarketplacesSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that root node and period exist
        Document xml = XMLConverter.convertToDocument(loadResultXML(supplier,
                BillingSharesResultType.MARKETPLACE_OWNER), false);
        assertNotNull(XMLConverter.getNodeByXPath(xml,
                "/MarketplaceOwnerRevenueShareResult/Period"));
    }

    private void givenVendorsWithoutBillingResult() throws Exception {
        createSupplierProductAndSubscriptions(false);
        createBrokerProductAndSubscriptions(false);
        createResellerProductAndSubscriptions(false);
    }

    /**
     * Assert that the billing does not fail completely in case of error, but
     * only for one broker.
     */
    @Test
    public void performBrokerSharesCalculationRun_oneFailing()
            throws Exception {

        // given two brokers. The second has data that causes the calculation to
        // fail.
        givenTwoResaleOrganizationsWithCorruptData();

        // when
        calculator.performBrokerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that one result exists for the fist but no result for the
        // 2nd.
        List<BillingSharesResult> resultBroker1 = loadSharesResult(
                broker.getKey(), BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.BROKER);
        List<BillingSharesResult> resultBroker2 = loadSharesResult(
                broker2.getKey(), BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.BROKER);
        assertThat(resultBroker1, hasOneItem());
        assertThat(resultBroker2, hasNoItems());
    }

    /**
     * Assert that the billing does not fail in case of unpublished product with
     * existing subscription.
     */
    @Test
    public void performBrokerSharesCalculationRun_oneUnpublished()
            throws Exception {

        // given two brokers. The second has unpublished subscription,
        // calculation must not fail.
        givenTwoResaleOrganizationsOneUnpublishedProduct();

        // when
        calculator.performBrokerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that both result exist
        List<BillingSharesResult> resultBroker1 = loadSharesResult(
                broker.getKey(), BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.BROKER);
        List<BillingSharesResult> resultBroker2 = loadSharesResult(
                broker2.getKey(), BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.BROKER);
        assertThat(resultBroker1, hasOneItem());
        assertThat(resultBroker2, hasOneItem());
    }

    /**
     * Assert that the billing does not fail completely in case of error, but
     * only for one reseller.
     */
    @Test
    public void performResellerSharesCalculationRun_oneFailing()
            throws Exception {

        // given two resellers. The second has data that causes the calculation
        // to fail.
        givenTwoResaleOrganizationsWithCorruptData();

        // when
        calculator.performResellerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that one result exists for the fist but no result for the
        // 2nd.
        List<BillingSharesResult> resultReseller1 = loadSharesResult(
                reseller.getKey(), BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.RESELLER);
        List<BillingSharesResult> resultReseller2 = loadSharesResult(
                reseller2.getKey(), BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.RESELLER);
        assertThat(resultReseller1, hasOneItem());
        assertThat(resultReseller2, hasNoItems());
    }

    /**
     * Assert that the billing does not fail in case of unpublished product with
     * existing subscription.
     */
    @Test
    public void performResellerSharesCalculationRun_oneUnpublished()
            throws Exception {

        // given two resellers. The second has unpublished subscription,
        // calculation must not fail.
        givenTwoResaleOrganizationsOneUnpublishedProduct();

        // when
        calculator.performResellerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that both results exist
        List<BillingSharesResult> resultReseller1 = loadSharesResult(
                reseller.getKey(), BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.RESELLER);
        List<BillingSharesResult> resultReseller2 = loadSharesResult(
                reseller2.getKey(), BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.RESELLER);
        assertThat(resultReseller1, hasOneItem());
        assertThat(resultReseller2, hasOneItem());
    }

    /**
     * Assert that the billing does not fail in case of unpublished product with
     * existing subscription.
     */
    @Test
    public void performSupplierSharesCalculationRun_oneUnpublished()
            throws Exception {

        // given two suppliers. The second has unpublished product with
        // subscription. The calculation must not fail.
        givenTwoSuppliersOneUnpublishedSub();

        // when
        calculator.performSupplierSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that both results exist
        List<BillingSharesResult> resultSupplier1 = loadSharesResult(
                supplier.getKey(), BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.SUPPLIER);
        List<BillingSharesResult> resultSupplier2 = loadSharesResult(
                supplier2.getKey(), BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.SUPPLIER);
        assertThat(resultSupplier1, hasOneItem());
        assertThat(resultSupplier2, hasOneItem());
        loadResultXML(supplier, BillingSharesResultType.SUPPLIER);
        loadResultXML(supplier2, BillingSharesResultType.SUPPLIER);
    }

    /**
     * Assert that the billing does not fail in case of unpublished product with
     * existing subscription.
     */
    @Test
    public void performMarketplacesSharesCalculationRun_oneUnpublished()
            throws Exception {

        // given two products. The second has been unpublished. The calculation
        // must not fail.
        givenTwoSuppliersOneUnpublishedSub();

        // when
        calculator.performMarketplacesSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that both services exist in the result
        Document xml = XMLConverter.convertToDocument(loadResultXML(supplier,
                BillingSharesResultType.MARKETPLACE_OWNER), false);
        NodeList services = XMLConverter.getNodeListByXPath(xml,
                "/MarketplaceOwnerRevenueShareResult/Currency/Marketplace/Service");
        assertEquals(2, services.getLength());
    }

    @Test
    public void performBrokerSharesCalculationRun_twice() throws Exception {

        // given vendors with a published product, but no subscriptions
        givenVendorsWithoutBillingResult();
        calculator.performBrokerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // when
        calculator.performBrokerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that result exists
        List<BillingSharesResult> result = loadSharesResult(broker.getKey(),
                BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.BROKER);
        assertThat(result, hasOneItem());
    }

    @Test
    public void performResellerSharesCalculationRun_twice() throws Exception {

        // given vendors with a published product, but no subscriptions
        givenVendorsWithoutBillingResult();
        calculator.performResellerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // when
        calculator.performResellerSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that result exists
        List<BillingSharesResult> result = loadSharesResult(reseller.getKey(),
                BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.RESELLER);
        assertThat(result, hasOneItem());
    }

    @Test
    public void performSupplierSharesCalculationRun_twice() throws Exception {

        // given vendors with a published product, but no subscriptions
        givenVendorsWithoutBillingResult();
        calculator.performSupplierSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // when
        calculator.performSupplierSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that result exists
        List<BillingSharesResult> result = loadSharesResult(supplier.getKey(),
                BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.SUPPLIER);
        assertThat(result, hasOneItem());
    }

    @Test
    public void performMarketplacesSharesCalculationRun_twice()
            throws Exception {

        // given vendors with a published product, but no subscriptions
        givenVendorsWithoutBillingResult();
        calculator.performMarketplacesSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // when
        calculator.performMarketplacesSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that result exists
        List<BillingSharesResult> result = loadSharesResult(supplier.getKey(),
                BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.MARKETPLACE_OWNER);
        assertThat(result, hasOneItem());
    }

    @Test
    public void performMarketplacesAndSupplierSharesCalculationRun()
            throws Exception {

        // given vendors with a published product, but no subscriptions
        givenVendorsWithoutBillingResult();
        calculator.performSupplierSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // when
        calculator.performMarketplacesSharesCalculationRun(BILLING_PERIOD_START,
                BILLING_PERIOD_END);

        // then verify that result exists
        List<BillingSharesResult> result = loadSharesResult(supplier.getKey(),
                BILLING_PERIOD_START, BILLING_PERIOD_END,
                BillingSharesResultType.MARKETPLACE_OWNER);
        assertThat(result, hasOneItem());
    }

    /**
     * Create two broker/reseller organizations. The second has a catalog entry
     * without a marketplace. This will cause a failure in the calculation
     * later.
     */
    private void givenTwoResaleOrganizationsWithCorruptData() throws Exception {
        createSupplierProductAndSubscriptions(true);
        createBrokerProductAndSubscriptions(true);
        createBrokerProductCorruptedData(true);
        createResellerProductAndSubscriptions(true);
        createResellerProductCorruptedData(true);
    }

    /**
     * Create two broker/reseller organizations. The second has unpublished
     * product for existing subscription. This must not cause a failure in the
     * calculation later.
     */
    private void givenTwoResaleOrganizationsOneUnpublishedProduct()
            throws Exception {
        createSupplierProductAndSubscriptions(true);
        createBrokerProductAndSubscriptions(true);
        createBrokerProductAndUnpublish(true);
        createResellerProductAndSubscriptions(true);
        createResellerProductAndUnpublish(true);
    }

    /**
     * Create two supplier organizations. The second has a catalog entry without
     * a marketplace(unpublished). This must not cause a failure in the
     * calculation later.
     */
    private void givenTwoSuppliersOneUnpublishedSub() throws Exception {
        createSupplierProductAndSubscriptions(true);
        createSupplierProductAndUnpublish(true);
    }

    private void createBrokerProductAndSubscriptions(
            final boolean withBillingResult) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                broker = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER.name(),
                        OrganizationRoleType.BROKER);
                Product resaleCopy = Products.createProductResaleCopy(product,
                        broker, ds);
                resaleCopy.setProductId(
                        "resaleProd_" + OrganizationRoleType.BROKER.name());

                // publish to marketplace
                CatalogEntries.createWithBrokerShare(ds, mp, resaleCopy,
                        REVENUE_SHARE_PERCENT);

                // subscribe to resale copy
                Subscription sub = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), resaleCopy, mp, 1);

                if (withBillingResult) {
                    // normal billing run as prerequisite
                    BillingResults.createBillingResult(ds, sub, broker.getKey(),
                            broker.getKey(), BILLING_PERIOD_START,
                            BILLING_PERIOD_END, NET_REVENUE, GROSS_REVENUE,
                            "<PriceModel id=\"" + sub.getPriceModel().getKey()
                                    + "\"><PriceModelCosts amount=\""
                                    + NET_REVENUE
                                    + "\"></PriceModelCosts></PriceModel>");
                }
                return null;
            }
        });
    }

    private void createBrokerProductAndUnpublish(
            final boolean withBillingResult) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                broker2 = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER.name() + "2",
                        OrganizationRoleType.BROKER);
                Product resaleCopy = Products.createProductResaleCopy(product,
                        broker2, ds);
                resaleCopy.setProductId(
                        "resaleProd_" + OrganizationRoleType.BROKER.name());

                // publish to marketplace
                CatalogEntry ce = CatalogEntries.createWithBrokerShare(ds, mp,
                        resaleCopy, REVENUE_SHARE_PERCENT);

                // subscribe to resale copy
                Subscription sub = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), resaleCopy, mp, 1);

                // unpublish
                CatalogEntries.updateMarketplace(ds, ce, null);

                if (withBillingResult) {
                    // normal billing run as prerequisite
                    BillingResults.createBillingResult(ds, sub, broker.getKey(),
                            broker.getKey(), BILLING_PERIOD_START,
                            BILLING_PERIOD_END, NET_REVENUE, GROSS_REVENUE,
                            "<PriceModel id=\"" + sub.getPriceModel().getKey()
                                    + "\"><PriceModelCosts amount=\""
                                    + NET_REVENUE
                                    + "\"></PriceModelCosts></PriceModel>");
                }
                return null;
            }
        });
    }

    private void createBrokerProductCorruptedData(
            final boolean withBillingResult) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                broker2 = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER.name() + "2",
                        OrganizationRoleType.BROKER);
                Product resaleCopy = Products.createProductResaleCopy(product,
                        broker2, ds);
                resaleCopy.setProductId(
                        "resaleProd_" + OrganizationRoleType.BROKER.name());

                // publish to marketplace
                CatalogEntries.createWithBrokerShare(ds, null, resaleCopy,
                        REVENUE_SHARE_PERCENT);

                // subscribe to resale copy
                Subscription sub = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), resaleCopy, mp, 1);

                if (withBillingResult) {
                    // normal billing run as prerequisite
                    BillingResults.createBillingResult(ds, sub, broker.getKey(),
                            broker.getKey(), BILLING_PERIOD_START,
                            BILLING_PERIOD_END, NET_REVENUE, GROSS_REVENUE);
                }
                return null;
            }
        });
    }

    private void createResellerProductAndSubscriptions(
            final boolean withBillingResult) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER.name(),
                        OrganizationRoleType.RESELLER);
                Product resaleCopy = Products.createProductResaleCopy(product,
                        reseller, ds);
                resaleCopy.setProductId(
                        "resaleProd_" + OrganizationRoleType.RESELLER.name());

                // publish to marketplace
                CatalogEntries.createWithResellerShare(ds, mp, resaleCopy,
                        REVENUE_SHARE_PERCENT);

                // subscribe to resale copy
                Subscription sub = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), resaleCopy, mp, 1);

                if (withBillingResult) {
                    // normal billing run as prerequisite
                    BillingResults.createBillingResult(ds, sub,
                            reseller.getKey(), reseller.getKey(),
                            BILLING_PERIOD_START, BILLING_PERIOD_END,
                            NET_REVENUE, GROSS_REVENUE,
                            "<PriceModel id=\"" + sub.getPriceModel().getKey()
                                    + "\"><PriceModelCosts amount=\""
                                    + NET_REVENUE
                                    + "\"></PriceModelCosts></PriceModel>");
                }
                return null;
            }
        });
    }

    private void createResellerProductCorruptedData(
            final boolean withBillingResult) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                reseller2 = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER.name() + "2",
                        OrganizationRoleType.RESELLER);
                Product resaleCopy = Products.createProductResaleCopy(product,
                        reseller2, ds);
                resaleCopy.setProductId(
                        "resaleProd_" + OrganizationRoleType.RESELLER.name());

                // publish to marketplace
                CatalogEntries.createWithResellerShare(ds, null, resaleCopy,
                        REVENUE_SHARE_PERCENT);

                // subscribe to resale copy
                Subscription sub = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), resaleCopy, mp, 1);

                if (withBillingResult) {
                    // normal billing run as prerequisite
                    BillingResults.createBillingResult(ds, sub,
                            reseller2.getKey(), reseller2.getKey(),
                            BILLING_PERIOD_START, BILLING_PERIOD_END,
                            NET_REVENUE, GROSS_REVENUE);
                }
                return null;
            }
        });
    }

    private void createResellerProductAndUnpublish(
            final boolean withBillingResult) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                reseller2 = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER.name() + "2",
                        OrganizationRoleType.RESELLER);
                Product resaleCopy = Products.createProductResaleCopy(product,
                        reseller2, ds);
                resaleCopy.setProductId(
                        "resaleProd_" + OrganizationRoleType.RESELLER.name());

                // publish to marketplace
                CatalogEntry ce = CatalogEntries.createWithResellerShare(ds, mp,
                        resaleCopy, REVENUE_SHARE_PERCENT);

                // subscribe to resale copy
                Subscription sub = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), resaleCopy, mp, 1);

                // unpublish
                CatalogEntries.updateMarketplace(ds, ce, null);

                if (withBillingResult) {
                    // normal billing run as prerequisite
                    BillingResults.createBillingResult(ds, sub,
                            reseller2.getKey(), reseller2.getKey(),
                            BILLING_PERIOD_START, BILLING_PERIOD_END,
                            NET_REVENUE, GROSS_REVENUE,
                            "<PriceModel id=\"" + sub.getPriceModel().getKey()
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

        // get strings
        String roleString = getRoleString(role);
        OfferingType offerType = getOfferingType(role);
        assertNotNull(roleString);
        assertNotNull(offerType);

        // verify root node
        verify_RootNode(xml, org, roleString);

        // verify period
        verify_Period(xml, roleString);

        // verify currency
        verify_Currency(xml, roleString);

        // verify supplier
        verify_Supplier(xml, supplier, roleString);

        // verify service
        Product prod = findProduct(org, "resaleProd_" + role.name());
        verify_ResaleResult_Service(xml, prod, roleString);

        // verify ServiceRevenue
        verify_ServiceRevenue(xml, roleString);

        // verify BrokerRevenuePerSupplier/ResellerRevenuePerSupplier
        verify_ResaleOrganization_RevenuePerSupplier(xml, roleString);

        // verify revenue
        verify_ResaleOrganization_Revenue(xml, roleString);

    }

    private void verify_SupplierResult(String xmlAsString) throws Exception {
        Document xml = XMLConverter.convertToDocument(xmlAsString, false);

        // get strings
        String roleString = getRoleString(OrganizationRoleType.SUPPLIER);

        // verify root node
        verify_RootNode(xml, supplier, roleString);

        // verify period
        verify_Period(xml, roleString);

        // verify currency
        verify_Currency(xml, roleString);

        // verify marketplace
        verify_Marketplace(xml, mp, roleString);

        // verify direct service and revenue share details
        verify_SupplierResult_Service(xml, null, product, OfferingType.DIRECT,
                roleString);

        // verify broker service
        Product prodBroker = findProduct(broker, "resaleProd_BROKER");
        verify_SupplierResult_Service(xml, broker, prodBroker,
                OfferingType.BROKER, roleString);

        // verify reseller service
        Product prodReseller = findProduct(reseller, "resaleProd_RESELLER");
        verify_SupplierResult_Service(xml, reseller, prodReseller,
                OfferingType.RESELLER, roleString);

    }

    private void verify_MarketplaceOwnerResult(String xmlAsString)
            throws Exception {
        Document xml = XMLConverter.convertToDocument(xmlAsString, false);

        // get strings
        String roleString = getRoleString(
                OrganizationRoleType.MARKETPLACE_OWNER);

        // verify root node
        verify_RootNode(xml, supplier, roleString);

        // verify period
        verify_Period(xml, roleString);

        // verify currency
        verify_Currency(xml, roleString);

        // verify marketplace
        verify_Marketplace(xml, mp, roleString);

        // verify direct service and revenue share details
        verify_MarketplaceOwnerResult_Service(xml, null, product,
                OfferingType.DIRECT, roleString);

        // verify broker service
        Product prodBroker = findProduct(broker, "resaleProd_BROKER");
        verify_MarketplaceOwnerResult_Service(xml, broker, prodBroker,
                OfferingType.BROKER, roleString);

        // verify reseller service
        Product prodReseller = findProduct(reseller, "resaleProd_RESELLER");
        verify_MarketplaceOwnerResult_Service(xml, reseller, prodReseller,
                OfferingType.RESELLER, roleString);

        // verify revenue all marketplaces
        verify_RevenuesOverAllMarketplaces(xml);

    }

    private void verify_RootNode(Document xml, Organization org,
            String roleString) throws XPathExpressionException {
        Node rootNode = XMLConverter.getNodeByXPath(xml,
                "/" + roleString + "RevenueShareResult");
        assertEquals(org.getKey(),
                XMLConverter.getLongAttValue(rootNode, "organizationKey"));
        assertEquals(org.getOrganizationId(),
                XMLConverter.getStringAttValue(rootNode, "organizationId"));
    }

    private void verify_Period(Document xml, String roleString)
            throws XPathExpressionException {
        Node period = XMLConverter.getNodeByXPath(xml,
                "/" + roleString + "RevenueShareResult/Period");
        assertEquals(BILLING_PERIOD_START,
                XMLConverter.getLongAttValue(period, "startDate"));
        assertEquals(BILLING_PERIOD_END,
                XMLConverter.getLongAttValue(period, "endDate"));
    }

    private void verify_Currency(Document xml, String roleString)
            throws XPathExpressionException {
        Node currency = XMLConverter.getNodeByXPath(xml,
                "/" + roleString + "RevenueShareResult/Currency");
        assertEquals("EUR", XMLConverter.getStringAttValue(currency, "id"));
    }

    private void verify_Supplier(Document xml, Organization supplier,
            String roleString) throws XPathExpressionException {
        Node organizationData = XMLConverter.getNodeByXPath(xml, "/"
                + roleString
                + "RevenueShareResult/Currency/Supplier/OrganizationData");
        assertEquals(supplier.getKey(),
                XMLConverter.getLongAttValue(organizationData, "key"));
    }

    private void verify_ResaleOrganization(Node service,
            Organization resaleOrg) {
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
        Node serviceRevenue = XMLConverter.getNodeByXPath(xml, "/" + roleString
                + "RevenueShareResult/Currency/Supplier/Service/ServiceRevenue");
        assertEquals(REVENUE_SHARE + ".00", XMLConverter.getStringAttValue(
                serviceRevenue, roleString.toLowerCase() + "Revenue"));
        assertEquals(REVENUE_SHARE_PERCENT.intValue() + ".00",
                XMLConverter.getStringAttValue(serviceRevenue,
                        roleString.toLowerCase() + "RevenueSharePercentage"));
        assertEquals(NET_REVENUE.intValue() + ".00",
                XMLConverter.getStringAttValue(serviceRevenue, "totalAmount"));
    }

    private void verify_SupplierResult_RevenueShareDetails(Node service) {

        String model = XMLConverter.getStringAttValue(service, "model");

        List<String> expected = new ArrayList<>();
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
        assertEquals(expected.get(0), XMLConverter
                .getStringAttValue(revenueShareDetails, "brokerRevenue"));
        assertEquals(expected.get(1), XMLConverter.getStringAttValue(
                revenueShareDetails, "brokerRevenueSharePercentage"));
        assertEquals(expected.get(2), XMLConverter
                .getStringAttValue(revenueShareDetails, "resellerRevenue"));
        assertEquals(expected.get(3), XMLConverter.getStringAttValue(
                revenueShareDetails, "resellerRevenueSharePercentage"));
        assertEquals(expected.get(4), XMLConverter
                .getStringAttValue(revenueShareDetails, "marketplaceRevenue"));
        assertEquals(expected.get(5), XMLConverter.getStringAttValue(
                revenueShareDetails, "marketplaceRevenueSharePercentage"));
        assertEquals(expected.get(6), XMLConverter
                .getStringAttValue(revenueShareDetails, "serviceRevenue"));
        assertEquals(expected.get(7), XMLConverter
                .getStringAttValue(revenueShareDetails, "amountForSupplier"));
    }

    private void verify_MarketplaceOwnerResult_RevenueShareDetails(Node service,
            OfferingType type) {

        String model = XMLConverter.getStringAttValue(service, "model");

        List<String> expected = new ArrayList<>();
        if (OfferingType.DIRECT.name().equals(model)) {
            expected = MarketplaceOwnerResult_RevenueShareDetails_DIRECT;
        } else if (OfferingType.BROKER.name().equals(model)) {
            expected = MarketplaceOwnerResult_RevenueShareDetails_BROKER;
        } else if (OfferingType.RESELLER.name().equals(model)) {
            expected = MarketplaceOwnerResult_RevenueShareDetails_RESELLER;
        }
        assertEquals(10, expected.size());

        Node revenueShareDetails = XMLConverter.getLastChildNode(service,
                "RevenueShareDetails");
        assertEquals(expected.get(0), XMLConverter
                .getStringAttValue(revenueShareDetails, "serviceRevenue"));
        assertEquals(expected.get(1), XMLConverter.getStringAttValue(
                revenueShareDetails, "marketplaceRevenueSharePercentage"));
        assertEquals(expected.get(2), XMLConverter
                .getStringAttValue(revenueShareDetails, "marketplaceRevenue"));
        assertEquals(expected.get(6), XMLConverter
                .getStringAttValue(revenueShareDetails, "amountForSupplier"));
        if (OfferingType.BROKER.equals(type)) {
            assertEquals(expected.get(7), XMLConverter.getStringAttValue(
                    revenueShareDetails, "brokerRevenueSharePercentage"));
            assertEquals(expected.get(8), XMLConverter
                    .getStringAttValue(revenueShareDetails, "brokerRevenue"));
        } else if (OfferingType.RESELLER.equals(type)) {
            assertEquals(expected.get(7), XMLConverter.getStringAttValue(
                    revenueShareDetails, "resellerRevenueSharePercentage"));
            assertEquals(expected.get(8), XMLConverter
                    .getStringAttValue(revenueShareDetails, "resellerRevenue"));
        }
    }

    private void verify_ResaleOrganization_RevenuePerSupplier(Document xml,
            String roleString) throws XPathExpressionException {
        Node resaleRevenuePerSupplier = XMLConverter.getNodeByXPath(xml,
                "/" + roleString + "RevenueShareResult/Currency/Supplier/"
                        + roleString + "RevenuePerSupplier");
        assertEquals(REVENUE_SHARE + ".00", XMLConverter
                .getStringAttValue(resaleRevenuePerSupplier, "amount"));
    }

    private void verify_ResaleOrganization_Revenue(Document xml,
            String roleString) throws XPathExpressionException {
        Node revenue = XMLConverter.getNodeByXPath(xml, "/" + roleString
                + "RevenueShareResult/Currency/" + roleString + "Revenue");
        assertEquals(REVENUE_SHARE + ".00",
                XMLConverter.getStringAttValue(revenue, "amount"));
    }

    private Product findProduct(final Organization org, final String prodId)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() {
                Product product = Products.findProduct(ds, org, prodId);
                Product template = unproxyEntity(product.getTemplate());
                product.setTemplate(template);
                return product;
            }
        });
    }

    private void verify_Marketplace(Document xml, Marketplace mp,
            String roleString) throws XPathExpressionException {
        Node marketplace = XMLConverter.getNodeByXPath(xml,
                "/" + roleString + "RevenueShareResult/Currency/Marketplace");
        assertEquals(mp.getMarketplaceId(),
                XMLConverter.getStringAttValue(marketplace, "id"));
        if ("Supplier".equals(roleString)) {
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
        assertEquals(supplier.getName(),
                XMLConverter.getStringAttValue(org, "name"));
        assertEquals("138.50", XMLConverter.getStringAttValue(org, "amount"));
    }

    private void verify_RevenuesPerMarketplace_Brokers(Node revenue) {
        Node brokers = XMLConverter.getLastChildNode(revenue, "Brokers");
        Node org = XMLConverter.getLastChildNode(brokers, "Organization");
        assertEquals(broker.getOrganizationId(),
                XMLConverter.getStringAttValue(org, "identifier"));
        assertEquals(broker.getName(),
                XMLConverter.getStringAttValue(org, "name"));
        assertEquals("5.00", XMLConverter.getStringAttValue(org, "amount"));
    }

    private void verify_RevenuesPerMarketplace_Resellers(Node revenue) {
        Node resellers = XMLConverter.getLastChildNode(revenue, "Resellers");
        Node org = XMLConverter.getLastChildNode(resellers, "Organization");
        assertEquals(reseller.getOrganizationId(),
                XMLConverter.getStringAttValue(org, "identifier"));
        assertEquals(reseller.getName(),
                XMLConverter.getStringAttValue(org, "name"));
        assertEquals("5.00", XMLConverter.getStringAttValue(org, "amount"));
    }

    private void verify_RevenuesOverAllMarketplaces(Document xml)
            throws XPathExpressionException {
        Node allMarketplaces = XMLConverter.getNodeByXPath(xml,
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
        assertEquals(SupplierResult_RevenuePerMarketplace.get(2), XMLConverter
                .getStringAttValue(revenuePerMp, "resellerRevenue"));
        assertEquals(SupplierResult_RevenuePerMarketplace.get(3), XMLConverter
                .getStringAttValue(revenuePerMp, "marketplaceRevenue"));
        assertEquals(SupplierResult_RevenuePerMarketplace.get(4),
                XMLConverter.getStringAttValue(revenuePerMp, "serviceRevenue"));
    }

    private void verify_ResaleResult_Service(Document xml, Product prod,
            String roleString) throws XPathExpressionException {
        Node service = XMLConverter.getNodeByXPath(xml, "/" + roleString
                + "RevenueShareResult/Currency/Supplier/Service");

        assertEquals(prod.getKey(),
                XMLConverter.getLongAttValue(service, "key"));
        assertEquals(prod.getTemplate().getKey(),
                XMLConverter.getLongAttValue(service, "templateKey"));
        assertEquals(prod.getProductId(),
                XMLConverter.getStringAttValue(service, "id"));

    }

    private void verify_SupplierResult_Service(Document xml,
            Organization resaleOrg, Product prod, OfferingType type,
            String roleString) throws XPathExpressionException {

        NodeList services = XMLConverter.getNodeListByXPath(xml,
                "/" + roleString
                        + "RevenueShareResult/Currency/Marketplace/Service");

        for (int i = 0; i < services.getLength(); i++) {
            String model = XMLConverter.getStringAttValue(services.item(i),
                    "model");

            if (type.name().equals(model)) {
                assertEquals(prod.getKey(),
                        XMLConverter.getLongAttValue(services.item(i), "key"));
                assertEquals(prod.getProductId(),
                        XMLConverter.getStringAttValue(services.item(i), "id"));

                if (!type.name().equals(OfferingType.DIRECT.name())) {
                    assertEquals(prod.getTemplate().getKey(), XMLConverter
                            .getLongAttValue(services.item(i), "templateKey"));
                }

                verify_SupplierResult_RevenueShareDetails(services.item(i));
                if (resaleOrg != null) {
                    verify_ResaleOrganization(services.item(i), resaleOrg);
                }

            }
        }
    }

    private void verify_MarketplaceOwnerResult_Service(Document xml,
            Organization resaleOrg, Product prod, OfferingType type,
            String roleString) throws XPathExpressionException {

        NodeList services = XMLConverter.getNodeListByXPath(xml,
                "/" + roleString
                        + "RevenueShareResult/Currency/Marketplace/Service");

        for (int i = 0; i < services.getLength(); i++) {
            String model = XMLConverter.getStringAttValue(services.item(i),
                    "model");

            if (type.name().equals(model)) {
                assertEquals(prod.getKey(),
                        XMLConverter.getLongAttValue(services.item(i), "key"));
                assertEquals(prod.getProductId(),
                        XMLConverter.getStringAttValue(services.item(i), "id"));

                if (!type.name().equals(OfferingType.DIRECT.name())) {
                    assertEquals(prod.getTemplate().getKey(), XMLConverter
                            .getLongAttValue(services.item(i), "templateKey"));
                }

                verify_MarketplaceOwnerResult_RevenueShareDetails(
                        services.item(i), type);
                if (resaleOrg != null) {
                    verify_ResaleOrganization(services.item(i), resaleOrg);
                }

            }
        }
    }

    private String loadResultXML(Organization org, BillingSharesResultType role)
            throws Exception {
        List<BillingSharesResult> result = loadSharesResult(org.getKey(),
                BILLING_PERIOD_START, BILLING_PERIOD_END, role);
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

}
