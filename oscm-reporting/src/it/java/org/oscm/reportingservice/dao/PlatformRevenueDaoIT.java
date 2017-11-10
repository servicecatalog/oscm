/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author kulle
 * 
 */
public class PlatformRevenueDaoIT extends EJBTestBase {

    private PlatformRevenueDao dao;
    private DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new PlatformRevenueDao(ds, "en");
    }

    /**
     * Supplier subscribed to own product and terminates it after usage. Then
     * the marketable product and the technical product is deleted. The query
     * must still find the corresponding billing result.
     */
    @Test
    public void executeQuery_technicalServiceDeleted() throws Exception {
        // given
        OrganizationHistory supplier = createOrganization(1L,
                "2012-11-01 08:00:00");
        createSupportedCountriesAndCurrencies();
        createTechnicalPrioducthistory(supplier.getObjKey(), 1L,
                "2012-11-02 15:10:42", 0, ModificationType.ADD);
        createTechnicalPrioducthistory(supplier.getObjKey(), 1L,
                "2012-11-05 15:10:42", 1, ModificationType.DELETE);
        createProductHistory(1L, 1000L, supplier.getObjKey(),
                "2012-11-03 15:10:42", 0, ModificationType.ADD);
        createProductHistory(1L, 1000L, supplier.getObjKey(),
                "2012-11-05 15:10:42", 1, ModificationType.DELETE);
        createSubscriptionHistory(10L, supplier.getObjKey(),
                "2012-11-04 15:10:42", 0, ModificationType.ADD,
                SubscriptionStatus.ACTIVE, 100L, 200L);
        createSubscriptionHistory(10L, supplier.getObjKey(),
                "2012-11-05 15:10:42", 1, ModificationType.DELETE,
                SubscriptionStatus.ACTIVE, 100L, 200L);
        createBillingResult(1349049600000L, 1354320000000L, Long.valueOf(10L),
                1000L, supplier.getObjKey());

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                // when
                dao.executeQuery(new Date(1349049600000L),
                        new Date(1354320000000L));

                // then
                assertEquals(1, dao.getRowData().size());
                return null;
            }

        });
    }

    @Test
    public void executeQuery_multipleMarketplaceHistoryEntries()
            throws Exception {
        // given
        OrganizationHistory supplier = createOrganization(1L,
                "2012-11-01 00:00:00");
        createMarketplaceHistory(200L, "2012-11-02 15:00:00",
                ModificationType.ADD, 0, supplier.getObjKey());
        createMarketplaceHistory(200L, "2012-11-02 15:10:00",
                ModificationType.MODIFY, 1, supplier.getObjKey());
        createSupportedCountriesAndCurrencies();
        createTechnicalPrioducthistory(supplier.getObjKey(), 1L,
                "2012-11-02 15:10:42", 0, ModificationType.ADD);
        createProductHistory(1L, 1000L, supplier.getObjKey(),
                "2012-11-03 15:10:42", 0, ModificationType.ADD);
        createSubscriptionHistory(10L, supplier.getObjKey(),
                "2012-11-04 15:10:42", 0, ModificationType.ADD,
                SubscriptionStatus.ACTIVE, 100L, 200L);
        createBillingResult(1349049600000L, 1354320000000L, Long.valueOf(10L),
                1000L, supplier.getObjKey());

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                // when
                dao.executeQuery(new Date(1349049600000L),
                        new Date(1354320000000L));

                // then
                assertEquals(1, dao.getRowData().size());
                return null;
            }

        });
    }

    private OrganizationHistory createOrganization(final long objKey,
            final String modDate) throws Exception {

        final SupportedCountry c = runTX(new Callable<SupportedCountry>() {
            @Override
            public SupportedCountry call() throws Exception {
                return SupportedCountries.findOrCreate(ds, "de");
            }
        });

        return runTX(new Callable<OrganizationHistory>() {
            @Override
            public OrganizationHistory call() throws Exception {
                return Organizations.createOrganizationHistory(ds, objKey,
                        modDate, 0, c.getKey());
            }
        });
    }

    private void createMarketplaceHistory(final long mpObjKey,
            final String modificationDate, final ModificationType modType,
            final int version, final long organizationObjKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplaces.createMarketplaceHistory(ds, mpObjKey,
                        modificationDate, version, modType, organizationObjKey,
                        12000L, 13000L, 14000L);
                return null;
            }
        });
    }

    private void createSupportedCountriesAndCurrencies() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCountries.createOneSupportedCountry(ds);
                SupportedCurrencies.createOneSupportedCurrency(ds);
                return null;
            }
        });
    }

    private void createSubscriptionHistory(final long subscriptionObjKey,
            final long customerOrganizationKey, final String modificationDate,
            final int version, final ModificationType modificationType,
            final SubscriptionStatus subscriptionStatus,
            final long productobjKey, final long mpObjKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscriptions.createSubscriptionHistory(ds, subscriptionObjKey,
                        customerOrganizationKey, modificationDate, version,
                        modificationType, subscriptionStatus, productobjKey,
                        mpObjKey);
                return null;
            }

        });
    }

    public void createBillingResult(final long periodStart,
            final long periodEnd, final Long subscriptionKey,
            final long customerOrgKey, final long sellerKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BillingResult br = new BillingResult();
                br.setCreationTime(0);
                br.setOrganizationTKey(customerOrgKey);
                br.setPeriodEndTime(periodEnd);
                br.setPeriodStartTime(periodStart);
                br.setResultXML(getXml());
                br.setChargingOrgKey(sellerKey);
                br.setCurrency((SupportedCurrency) ds
                        .find(new SupportedCurrency("EUR")));
                br.setNetAmount(BigDecimal.ZERO);
                br.setGrossAmount(BigDecimal.TEN);
                br.setSubscriptionKey(subscriptionKey);
                br.setVendorKey(sellerKey);
                ds.persist(br);
                return null;
            }
        });
    }

    private String getXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<BillingDetails key=\"10001\">");
        sb.append(
                "<Period endDate=\"1354320000000\" endDateIsoFormat=\"2012-12-01T00:00:00.000Z\" startDate=\"1349049600000\" startDateIsoFormat=\"2012-11-01T00:00:00.000Z\"/>");
        sb.append("<OrganizationDetails>");
        sb.append("<Email>sven.kulle@est.fujitsu.com</Email>");
        sb.append("<Name>EST</Name>");
        sb.append("<Address>Street. Nr");
        sb.append("Zip City</Address>");
        sb.append("<Paymenttype>INVOICE</Paymenttype>");
        sb.append("</OrganizationDetails>");
        sb.append("<Subscriptions>");
        sb.append(
                "<Subscription id=\"Php Product(2)\" purchaseOrderNumber=\"\">");
        sb.append("<PriceModels>");
        sb.append("<PriceModel id=\"11000\">");
        sb.append(
                "<UsagePeriod endDate=\"1352809470838\" endDateIsoFormat=\"2012-11-13T12:24:30.838Z\" startDate=\"1352809213216\" startDateIsoFormat=\"2012-11-13T12:20:13.216Z\"/>");
        sb.append("<GatheredEvents>");
        sb.append("<GatheredEventsCosts amount=\"0.00\"/>");
        sb.append("</GatheredEvents>");
        sb.append(
                "<PeriodFee basePeriod=\"MONTH\" basePrice=\"0.10\" factor=\"9.939120370370371E-5\" price=\"0.00\"/>");
        sb.append(
                "<UserAssignmentCosts basePeriod=\"MONTH\" basePrice=\"10.00\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0.00\"/>");
        sb.append(
                "<OneTimeFee amount=\"110.00\" baseAmount=\"110.00\" factor=\"1\"/>");
        sb.append(
                "<PriceModelCosts amount=\"110.00\" currency=\"EUR\" grossAmount=\"110.00\"/>");
        sb.append("</PriceModel>");
        sb.append("</PriceModels>");
        sb.append("</Subscription>");
        sb.append("</Subscriptions>");
        sb.append(
                "<OverallCosts currency=\"EUR\" grossAmount=\"110.00\" netAmount=\"110.00\"/>");
        sb.append("</BillingDetails>");
        return sb.toString();
    }

    private void createTechnicalPrioducthistory(final long orgKey,
            final long prdObjKey, final String modificationDate,
            final int version, final ModificationType modificationType)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProducts.createTechnicalProductHistory(ds, orgKey,
                        prdObjKey, modificationDate, version, modificationType);
                return null;
            }
        });
    }

    private void createProductHistory(final long technicalProductObjKey,
            final long prdObjKey, final long sellerKey,
            final String modificationDate, final int version,
            final ModificationType modificationType) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Products.createProductHistory(ds, technicalProductObjKey,
                        prdObjKey, sellerKey, modificationDate, version,
                        modificationType);
                return null;
            }
        });
    }

}
