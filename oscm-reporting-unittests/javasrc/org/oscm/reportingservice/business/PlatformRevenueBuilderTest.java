/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.converter.PriceConverter;
import org.oscm.reportingservice.business.model.supplierrevenue.RDOPlatformRevenue;
import org.oscm.reportingservice.dao.PlatformRevenueDao;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;

public class PlatformRevenueBuilderTest {

    private static String BILLING_RESULT1;
    private static String BILLING_RESULT2;

    private PlatformRevenueDao EMPTY = new PlatformRevenueDao(null, null);
    private PlatformRevenueDao sqlResult = new PlatformRevenueDao(null, null);

    @BeforeClass
    public static void beforeClass() throws Exception {
        BILLING_RESULT1 = readBillingResultFromFile(new File(
                "javares/BillingResult.xml"));
        BILLING_RESULT2 = readBillingResultFromFile(new File(
                "javares/BillingResult2.xml"));
    }

    /**
     * Extract the currency from the billing result xml file
     * 
     * @throws Exception
     */
    @Test
    public void parseCurrency() throws Exception {
        // parse test documents
        String currency1 = new PlatformRevenueDao(null, null).new RowData("",
                "", "", BILLING_RESULT1, "").getCurrency();
        String currency2 = new PlatformRevenueDao(null, null).new RowData("",
                "", "", BILLING_RESULT2, "").getCurrency();

        assertEquals("EUR", currency1);
        assertEquals("EUR", currency2);
    }

    /**
     * Extract the amount from the billing result xml file
     * 
     * @throws Exception
     */
    @Test
    public void parseAmount() throws Exception {

        // parse test documents
        BigDecimal amount1 = new PlatformRevenueDao(null, null).new RowData("",
                "", "", BILLING_RESULT1, "").getAmount();
        BigDecimal amount2 = new PlatformRevenueDao(null, null).new RowData("",
                "", "", BILLING_RESULT2, "").getAmount();

        // assert
        final PriceConverter pc = new PriceConverter(Locale.US);
        assertEquals("14,681.00", pc.getValueToDisplay(amount1, true));
        assertEquals("6,188.00", pc.getValueToDisplay(amount2, true));
    }

    /**
     * Execute aggregate on empty SQL result
     */
    @Test
    public void build_empty() {
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(EMPTY,
                Locale.US);
        RDOPlatformRevenue result = builder.build();
        assertTrue(result.getSupplierDetails().isEmpty());
    }

    @Test
    public void build_oneMarketplace() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP (1)");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals(1, result.getSummaryByMarketplace().size());
        assertEquals("MP (1)", result.getSummaryByMarketplace().get(0)
                .getMarketplace());
    }

    @Test
    public void build_oneCountrySameCurrency() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP (1)");
        addSqlData("Amazon", "9876", "DE", "EUR", 5, "MP2 (2)");

        // execute
        RDOPlatformRevenue result = new PlatformRevenueBuilder(sqlResult,
                Locale.US).build();

        // assert
        assertEquals(1, result.getSummaryByCountry().size());
        assertEquals("DE", result.getSummaryByCountry().get(0).getCountry());
        assertEquals("EUR", result.getSummaryByCountry().get(0).getCurrency());
        assertEquals("10.00", result.getSummaryByCountry().get(0).getAmount());
    }

    @Test
    public void build_oneCountryTwoCurrencies() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP (1)");
        addSqlData("Amazon", "9876", "DE", "USD", 15, "MP2 (2)");

        // execute
        RDOPlatformRevenue result = new PlatformRevenueBuilder(sqlResult,
                Locale.US).build();

        // assert
        assertEquals(2, result.getSummaryByCountry().size());
        assertEquals("DE", result.getSummaryByCountry().get(0).getCountry());
        assertEquals("EUR", result.getSummaryByCountry().get(0).getCurrency());
        assertEquals("5.00", result.getSummaryByCountry().get(0).getAmount());
        assertEquals("DE", result.getSummaryByCountry().get(1).getCountry());
        assertEquals("USD", result.getSummaryByCountry().get(1).getCurrency());
        assertEquals("15.00", result.getSummaryByCountry().get(1).getAmount());
    }

    @Test
    public void build_twoCountries() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP (1)");
        addSqlData("Amazon", "9876", "US", "USD", 15, "MP2 (2)");

        // execute
        RDOPlatformRevenue result = new PlatformRevenueBuilder(sqlResult,
                Locale.US).build();

        // assert
        assertEquals(2, result.getSummaryByCountry().size());
        assertEquals("DE", result.getSummaryByCountry().get(0).getCountry());
        assertEquals("EUR", result.getSummaryByCountry().get(0).getCurrency());
        assertEquals("5.00", result.getSummaryByCountry().get(0).getAmount());
        assertEquals("US", result.getSummaryByCountry().get(1).getCountry());
        assertEquals("USD", result.getSummaryByCountry().get(1).getCurrency());
        assertEquals("15.00", result.getSummaryByCountry().get(1).getAmount());
    }

    @Test
    public void build_sameMarketplaceDifferentSuppliers() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP (1)");
        addSqlData("Amazon", "9876", "DE", "EUR", 5, "MP (1)");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals(1, result.getSummaryByMarketplace().size());
        assertEquals("MP (1)", result.getSummaryByMarketplace().get(0)
                .getMarketplace());
    }

    @Test
    public void build_differentMarketplacesDifferentSuppliers() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP (1)");
        addSqlData("Amazon", "9876", "DE", "EUR", 5, "MP (2)");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals(2, result.getSummaryByMarketplace().size());
        assertEquals("MP (1)", result.getSummaryByMarketplace().get(0)
                .getMarketplace());
        assertEquals("MP (2)", result.getSummaryByMarketplace().get(1)
                .getMarketplace());
    }

    /**
     * Two billing results for the same supplier must be aggregated to one
     * revenue per supplier
     */
    @Test
    public void build_twoBillingsOneSupplier() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP");
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals(1, result.getSupplierDetails().size());
    }

    /**
     * Amounts for the same supplier and same currency must be aggregated
     */
    @Test
    public void build_twoBillingsOneCurrency() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 523, "MP");
        addSqlData("Google", "1234", "DE", "EUR", 1029839203, "MP");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals("1,029,839,726.00", result.getSupplierDetails().get(0)
                .getAmount());
    }

    /**
     * Amounts for different currencies must not be aggregated
     */
    @Test
    public void build_twoBillingsTwoCurrencies() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP");
        addSqlData("Google", "1234", "DE", "US", 5, "MP");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals("5.00", result.getSupplierDetails().get(0).getAmount());
        assertEquals("5.00", result.getSupplierDetails().get(0).getAmount());
    }

    /**
     * Billing results for different suppliers must not be aggregated.
     */
    @Test
    public void build_twoBillingsTwoSuppliers() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP");
        addSqlData("Amazon", "9876", "DE", "EUR", 5, "MP");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals("5.00", result.getSupplierDetails().get(0).getAmount());
        assertEquals("5.00", result.getSupplierDetails().get(0).getAmount());
    }

    /**
     * Check that no NPE is thrown in case of amount '0'
     */
    @Test
    public void build_zero() {
        // given
        addSqlData("Google", "1234", "DE", "EUR", 0, "MP");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals("0.00", result.getSupplierDetails().get(0).getAmount());
    }

    /**
     * The supplier are sorted alphabetically by the supplier name.
     */
    @Test
    public void build_sortSupplier() {
        // given in wrong sort order
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP");
        addSqlData("Amazon", "9876", "US", "EUR", 5, "MP");

        // execute
        RDOPlatformRevenue result = new PlatformRevenueBuilder(sqlResult,
                Locale.US).build();

        // assert alphabetical sort order
        assertEquals("Amazon (9876)", result.getSupplierDetails().get(0)
                .getName());
        assertEquals("US", result.getSupplierDetails().get(0).getCountry());
        assertEquals("Google (1234)", result.getSupplierDetails().get(1)
                .getName());
        assertEquals("DE", result.getSupplierDetails().get(1).getCountry());
    }

    /**
     * The currencies of each supplier must be sorted
     */
    @Test
    public void build_sortCurrency() {

        // given in wrong sort order
        addSqlData("Google", "1234", "DE", "YEN", 5, "MP");
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP2");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert alphabetical sort order
        assertEquals("EUR", result.getSupplierDetails().get(0).getCurrency());
        assertEquals("YEN", result.getSupplierDetails().get(1).getCurrency());
        assertEquals("2", result.getNoMarketplaces());
        assertEquals("1", result.getNoSuppliers());
    }

    /**
     * Sorting currencies. Multiple supplies.
     */
    @Test
    public void build_sortCurrencyAndSuppliers() {

        // given in wrong sort order
        addSqlData("Google", "1234", "DE", "YEN", 5, "MP");
        addSqlData("Amazon", "9876", "DE", "YEN", 5, "MP");
        addSqlData("Amazon", "9876", "DE", "EUR", 5, "MP");
        addSqlData("Google", "1234", "DE", "EUR", 5, "MP");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert alphabetical sort order
        assertEquals("Amazon (9876)", result.getSupplierDetails().get(0)
                .getName());
        assertEquals("EUR", result.getSupplierDetails().get(0).getCurrency());
        assertEquals("Amazon (9876)", result.getSupplierDetails().get(1)
                .getName());
        assertEquals("YEN", result.getSupplierDetails().get(1).getCurrency());
        assertEquals("Google (1234)", result.getSupplierDetails().get(2)
                .getName());
        assertEquals("EUR", result.getSupplierDetails().get(2).getCurrency());
        assertEquals("Google (1234)", result.getSupplierDetails().get(3)
                .getName());
        assertEquals("YEN", result.getSupplierDetails().get(3).getCurrency());
        assertEquals("1", result.getNoMarketplaces());
        assertEquals("2", result.getNoSuppliers());
    }

    @Test
    public void build_multipleMarketplaces() {
        addSqlData("Amazon", "9876", "DE", "YEN", 1, "MP1");
        addSqlData("Amazon", "9876", "DE", "EUR", 10, "MP2");
        addSqlData("Google", "1234", "DE", "YEN", 4, "MP3");
        addSqlData("Google", "1234", "DE", "EUR", 40, "MP4");
        addSqlData("Google", "1234", "DE", "EUR", 40, "MP5");
        addSqlData("Google", "1234", "DE", "EUR", 40, "MP5");
        addSqlData("Google", "1234", "DE", "EUR", 40, "MP6");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals("6", result.getNoMarketplaces());
        assertEquals("2", result.getNoSuppliers());
    }

    /**
     * The total revenues aggregate the revenues of all suppliers.
     */
    @Test
    public void build_totalRevenue() {

        // given
        addSqlData("Amazon", "9876", "DE", "YEN", 1, "MP");
        addSqlData("Amazon", "9876", "DE", "EUR", 10, "MP");
        addSqlData("Google", "1234", "DE", "YEN", 4, "MP");
        addSqlData("Google", "1234", "DE", "EUR", 40, "MP");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals("50.00", result.getTotalByCurrency().get(0).getAmount());
        assertEquals("5.00", result.getTotalByCurrency().get(1).getAmount());
    }

    /**
     * The total revenues must be sorted, also.
     */
    @Test
    public void build_sortTotalRevenue() {

        // given in wrong sort order
        addSqlData("Amazon", "9876", "DE", "YEN", 1, "MP");
        addSqlData("Amazon", "9876", "DE", "EUR", 10, "MP");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert alphabetical sort order
        assertEquals("EUR", result.getTotalByCurrency().get(0).getCurrency());
        assertEquals("YEN", result.getTotalByCurrency().get(1).getCurrency());
        assertEquals("1", result.getNoMarketplaces());
        assertEquals("1", result.getNoSuppliers());
    }

    /**
     * Sorting must not fail for one entry
     */
    @Test
    public void build_sortOneCurrency() {

        // given
        addSqlData("Google", "1234", "DE", "YEN", 5, "MP");
        PlatformRevenueBuilder builder = new PlatformRevenueBuilder(sqlResult,
                Locale.US);

        // execute
        RDOPlatformRevenue result = builder.build();

        // assert
        assertEquals("YEN", result.getSummaryBySupplier().get(0).getCurrency());
    }

    @Test
    public void build_billingKey() {
        // given
        addSqlData("Amazon", "9876", "DE", "EUR", 1, "MP");

        // when
        RDOPlatformRevenue result = new PlatformRevenueBuilder(sqlResult,
                Locale.US).build();

        // then
        assertEquals("10001", result.getSummaryBySupplier().get(0)
                .getBillingKey());
    }

    private void addSqlData(String supplierName, String supplierID,
            String supplierCountry, String currency, long amount,
            String marketplace) {
        sqlResult.getRowData().add(
                sqlResult.new RowData(supplierName, supplierID,
                        supplierCountry, createBillingResultXML(currency,
                                amount), marketplace));
    }

    private String createBillingResultXML(String currency, long amount) {
        StringBuffer sb = new StringBuffer();
        sb.append("<BillingDetails key=\"10001\"><OverallCosts netAmount=\"");
        sb.append(amount);
        sb.append("\" currency=\"");
        sb.append(currency);
        sb.append("\">");
        sb.append("</OverallCosts></BillingDetails>");
        return sb.toString();
    }

    private static String readBillingResultFromFile(File testFile)
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

}
