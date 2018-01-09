/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: 09.08.2011                                                      
 *                                                                              
 *  Completion Time: 09.08.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.converter.PriceConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.BillingServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.timerservice.bean.TimerServiceBean;

/**
 * Tests for the SQL query class for the command to get supplier revenue list.
 * 
 * @author tokoda
 * 
 */
public class SupplierRevenueSqlResultIT extends EJBTestBase {

    private static final String MP_ID_S1 = "s1Id";
    private static final String MP_ID_S2 = "s2Id";

    private DataService dm;

    private PlatformUser operatorAdmin;
    private Organization platformOp;
    private Organization supplier1;
    private Organization customer11;
    private Organization customer12;
    private Organization supplier2;
    private Organization customer21;
    private Organization customer22;

    SupplierRevenueSqlResult sqlResult;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login(1);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new BillingServiceStub());
        container.addBean(new AccountServiceStub());
        container.addBean(new PaymentServiceStub());
        container.addBean(new IdentityServiceStub());
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(mock(TimerServiceBean.class));

        dm = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(dm);
                platformOp = Organizations.createOrganization(dm,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                operatorAdmin = Organizations.createUserForOrg(dm, platformOp,
                        true, "admin");

                supplier1 = Organizations.createOrganization(dm,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                supplier1.setName("supplier1");
                final TechnicalProduct tp1 = TechnicalProducts
                        .createTechnicalProduct(dm, supplier1, "tp1", false,
                                ServiceAccessType.DIRECT);
                final Product p1 = Products.createProduct(supplier1, tp1, false,
                        "p1", null, dm);
                final Marketplace mp1 = Marketplaces
                        .createGlobalMarketplace(supplier1, MP_ID_S1, dm);

                customer11 = Organizations.createCustomer(dm, supplier1);
                customer11.setName("customer11");
                customer12 = Organizations.createCustomer(dm, supplier1);
                customer12.setName("customer12");

                supplier2 = Organizations.createOrganization(dm,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                supplier2.setName("supplier2");
                final TechnicalProduct tp2 = TechnicalProducts
                        .createTechnicalProduct(dm, supplier2, "tp2", false,
                                ServiceAccessType.DIRECT);
                final Product p2 = Products.createProduct(supplier2, tp2, false,
                        "p2", null, dm);
                final Marketplace mp2 = Marketplaces
                        .createGlobalMarketplace(supplier2, MP_ID_S2, dm);
                customer21 = Organizations.createCustomer(dm, supplier2);
                customer21.setName("customer21");
                customer22 = Organizations.createCustomer(dm, supplier2);
                customer22.setName("customer22");
                dm.flush();

                final BigDecimal bd10000 = new BigDecimal(10000);
                final BigDecimal bd1000 = new BigDecimal(1000);
                final BigDecimal bd100 = new BigDecimal(100);
                final BigDecimal bd10 = new BigDecimal(10);
                final BigDecimal bd1_5 = new BigDecimal(1.5);
                // the data which should be calculated
                createBillingResultAndSubscription(customer11, "2011-07",
                        "2011-08", createResultXML(bd1000, "EUR"), supplier1,
                        mp1, p1);
                createBillingResultAndSubscription(customer11, "2011-07",
                        "2011-08", createResultXML(bd1000, "JPY"), supplier1,
                        mp2, p1);

                createBillingResultAndSubscription(customer12, "2011-07",
                        "2011-08", createResultXML(bd100, "USD"), supplier1,
                        mp1, p1);
                createBillingResultAndSubscription(customer12, "2011-07",
                        "2011-08", createResultXML(bd100, "EUR"), supplier1,
                        mp2, p1);
                createBillingResultAndSubscription(customer12, "2011-07",
                        "2011-08", createResultXML(bd100, "JPY"), supplier1,
                        mp1, p1);

                createBillingResultAndSubscription(customer21, "2011-07",
                        "2011-08", createResultXML(bd10, "EUR"), supplier2, mp2,
                        p2);
                createBillingResultAndSubscription(customer21, "2011-07",
                        "2011-08", createResultXML(bd10, "JPY"), supplier2, mp2,
                        p2);

                createBillingResultAndSubscription(customer22, "2011-07",
                        "2011-08", createResultXML(bd1_5, "USD"), supplier2,
                        mp1, p2);
                createBillingResultAndSubscription(customer22, "2011-07",
                        "2011-08", createResultXML(bd1_5, "EUR"), supplier2,
                        null, p2);
                createBillingResultAndSubscription(customer22, "2011-07",
                        "2011-08", createResultXML(bd1_5, "JPY"), supplier2,
                        mp1, p2);

                // the data which shouldn't be calculated
                createBillingResultAndSubscription(customer11, "2011-06",
                        "2011-07", createResultXML(bd10000, "SGD"), supplier1,
                        mp1, p1);
                createBillingResultAndSubscription(customer11, "2011-08",
                        "2011-09", createResultXML(bd10000, "SGD"), supplier1,
                        mp2, p1);
                createBillingResultAndSubscription(customer11, "2011-08",
                        "2011-09", "", supplier1, mp1, p1);

                return null;
            }
        });
        container.login(operatorAdmin.getKey());
    }

    /**
     * Test of SQL query execution for supplier revenue list.
     * 
     * @throws Exception
     */
    @Test
    public void testExecuteQuery() throws Exception {

        final String expectedStart = "2011-07-01 00:00:00.0";
        final String expectedEnd = "2011-08-01 00:00:00.0";
        final long month = createTimestamp("2011-07");

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                sqlResult = SupplierRevenueSqlResult.executeQuery(dm, month);
                return null;
            }
        });

        List<SupplierRevenueSqlResult.RowData> resultList = sqlResult
                .getRowData();
        assertEquals(10, resultList.size());
        verifyResult(resultList.get(0), expectedStart, expectedEnd,
                supplier1.getOrganizationId(), supplier1.getName(), "1000.00",
                "EUR", MP_ID_S1);
        verifyResult(resultList.get(1), expectedStart, expectedEnd,
                supplier1.getOrganizationId(), supplier1.getName(), "100.00",
                "EUR", MP_ID_S2);
        verifyResult(resultList.get(2), expectedStart, expectedEnd,
                supplier1.getOrganizationId(), supplier1.getName(), "100.00",
                "JPY", MP_ID_S1);
        verifyResult(resultList.get(3), expectedStart, expectedEnd,
                supplier1.getOrganizationId(), supplier1.getName(), "1000.00",
                "JPY", MP_ID_S2);
        verifyResult(resultList.get(4), expectedStart, expectedEnd,
                supplier1.getOrganizationId(), supplier1.getName(), "100.00",
                "USD", MP_ID_S1);
        verifyResult(resultList.get(5), expectedStart, expectedEnd,
                supplier2.getOrganizationId(), supplier2.getName(), "10.00",
                "EUR", MP_ID_S2);
        verifyResult(resultList.get(6), expectedStart, expectedEnd,
                supplier2.getOrganizationId(), supplier2.getName(), "1.50",
                "EUR", "");
        verifyResult(resultList.get(7), expectedStart, expectedEnd,
                supplier2.getOrganizationId(), supplier2.getName(), "1.50",
                "JPY", MP_ID_S1);
        verifyResult(resultList.get(8), expectedStart, expectedEnd,
                supplier2.getOrganizationId(), supplier2.getName(), "10.00",
                "JPY", MP_ID_S2);
        verifyResult(resultList.get(9), expectedStart, expectedEnd,
                supplier2.getOrganizationId(), supplier2.getName(), "1.50",
                "USD", MP_ID_S1);
    }

    /**
     * Verify the result of the SQL for supplier revenue list.
     * 
     * @param result
     * @param expectedStart
     * @param expectedEnd
     * @param expectedId
     * @param expectedName
     * @param expectedAmount
     * @param expectedCurrency
     */
    private void verifyResult(SupplierRevenueSqlResult.RowData result,
            String expectedStart, String expectedEnd, String expectedId,
            String expectedName, String expectedAmount, String expectedCurrency,
            String mp) {
        assertEquals(expectedStart, result.fromDate);
        assertEquals(expectedEnd, result.toDate);
        assertEquals(expectedId, result.supplierId);
        assertEquals(expectedName, result.supplierName);
        assertEquals(expectedAmount, result.amount);
        assertEquals(expectedCurrency, result.currency);
        assertEquals(mp, result.marketplace);
    }

    /**
     * Return the month parameter of long type which is created from 'yyyy-MM'
     * String parameter. Sets hour, minute, second and millisecond of the
     * provided time stamp to zero and converts it to the server time zone.
     * 
     * @param monthStr
     *            month parameter 'yyyy-MM' form
     * @return the time stamp representing the day 00:00:00 000 passed in the
     *         time stamp in the current time zone
     */
    private long createTimestamp(String monthStr) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        long month = sdf.parse(monthStr).getTime();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(month);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Persist the billing result for test to DB.
     * 
     * @param orgKey
     * @param periodStartTime
     * @param periodEndTime
     * @param resultXML
     * @throws Exception
     */
    private void createBillingResultAndSubscription(Organization cust,
            String periodStartTime, String periodEndTime, String resultXML,
            Organization sup, Marketplace mp, Product p) throws Exception {

        Subscription sub = Subscriptions.createSubscription(dm,
                cust.getOrganizationId(), p);
        sub.setMarketplace(mp);
        dm.flush();

        BillingResult billingResult = new BillingResult();
        billingResult.setCreationTime(1282816800000L);
        billingResult.setOrganizationTKey(cust.getKey());
        billingResult.setPeriodStartTime(createTimestamp(periodStartTime));
        billingResult.setPeriodEndTime(createTimestamp(periodEndTime));
        billingResult.setResultXML(resultXML);
        billingResult.setNetAmount(BigDecimal.ZERO);
        billingResult.setGrossAmount(BigDecimal.ZERO);
        billingResult.setCurrency(SupportedCurrencies.findOrCreate(dm, "EUR"));
        billingResult.setChargingOrgKey(sup.getKey());
        billingResult.setSubscriptionKey(Long.valueOf(sub.getKey()));
        dm.persist(billingResult);
    }

    /**
     * Return the billing result xml for test.
     * 
     * @param amount
     * @param currency
     * @return
     */
    private String createResultXML(BigDecimal amount, String currency) {
        PriceConverter pc = new PriceConverter();
        StringBuffer resultxml = new StringBuffer("");
        resultxml.append("<BillingDetails>");
        resultxml.append("<OverallCosts netAmount=\""
                + pc.getValueToDisplay(amount, false) + "\" currency=\""
                + currency + "\" grossAmount=\"14691\">");
        resultxml.append("<Discount percent=\"0.00\" discountNetAmount=\"0\" ");
        resultxml.append(
                "netAmountAfterDiscount=\"0\" netAmountBeforeDiscount=\"0\" />");
        resultxml.append("<VAT percent=\"5.00\" amount=\"10\"/>");
        resultxml.append("</OverallCosts>");
        resultxml.append("</BillingDetails>");
        return resultxml.toString();
    }
}
