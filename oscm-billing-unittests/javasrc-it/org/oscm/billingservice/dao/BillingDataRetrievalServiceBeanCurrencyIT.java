/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *  Completion Time: 10.05.2010                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.billingservice.service.model.CustomerData;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Scenario;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;

/**
 * Test class for BilllingDataRetrievalServiceBean functionality related to
 * currencies.
 * 
 */
public class BillingDataRetrievalServiceBeanCurrencyIT extends EJBTestBase {

    private DataService dm;
    private BillingDataRetrievalServiceLocal bdr;

    /**
     * Common setup for the test class.
     */
    @Override
    public void setup(final TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }
        });
        container.addBean(new BillingDataRetrievalServiceBean());

        dm = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCountries.createSomeSupportedCountries(dm);
                Scenario.setup(container, true);
                return null;
            }
        });
    }

    /**
     * Create billing input for non-existing organization
     */
    @Test
    public void testGetSubscriptionsForCustomer_NonExistingOrg()
            throws Exception {
        final CustomerData result = runTX(new Callable<CustomerData>() {
            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(-1, 1,
                        System.currentTimeMillis(), -1));
            }
        });
        assertNotNull(result);
        assertTrue(result.getSubscriptionKeys().isEmpty());
    }

    /**
     * Added subscription must be part of billing input
     */
    @Test
    public void testGetSubscriptionsForCustomer_CustomerOneCurrency()
            throws Exception {
        CustomerData result = runTX(new Callable<CustomerData>() {
            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(
                        Scenario.getCustomer().getKey(), 1,
                        System.currentTimeMillis(), -1));
            }
        });
        List<SubscriptionHistory> list = result.iterator().next();
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertEquals(ModificationType.ADD, list.get(0).getModtype());
    }

    /**
     * Modify subscription. Two SubscriptionHistory objects must be part of
     * billing input.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSubscriptionsForCustomer_ComplexScenario()
            throws Exception {
        // price model, subscription and product were changed in the meantime
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = Scenario.getSubscription();
                sub = (Subscription) dm.getReferenceByBusinessKey(sub);
                sub.setSubscriptionId(sub.getSubscriptionId() + "x");

                Product p = sub.getProduct();
                p = (Product) dm.getReferenceByBusinessKey(p);
                p.setProductId(p.getProductId() + "x");

                PriceModel pm = p.getPriceModel();
                pm = dm.getReference(PriceModel.class, pm.getKey());
                pm.setPricePerPeriod(
                        pm.getPricePerPeriod().add(new BigDecimal(1)));
                return null;
            }
        });
        // add a mill to ensure in Windows different time stamp now comparing to
        // scenario setup (BES queries exclude the end billing period):
        final long now = System.currentTimeMillis() + 1;
        CustomerData result = runTX(new Callable<CustomerData>() {
            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(
                        Scenario.getCustomer().getKey(), 1, now, -1));
            }
        });
        List<SubscriptionHistory> list = result.iterator().next();
        assertFalse(list.isEmpty());
        assertEquals(2, list.size());
        assertEquals(ModificationType.MODIFY, list.get(0).getModtype());
        assertEquals(ModificationType.ADD, list.get(1).getModtype());
    }

    /**
     * Create two subscriptions with 2 currencies. Create the billing input
     * after the creation of the subscriptions. The currencies must be up to
     * date.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSubscriptionsForCustomer_TwoCurrencies()
            throws Exception {
        final long billingTime = System.currentTimeMillis();
        long modTime = billingTime - 1000;
        createSubUsingUSD(modTime);
        CustomerData result = runTX(new Callable<CustomerData>() {
            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(
                        Scenario.getCustomer().getKey(), 1, billingTime, -1));
            }
        });

        final long subscriptionKey1 = result.getSubscriptionKeys().get(0)
                .longValue();

        SupportedCurrency entry1 = runTX(new Callable<SupportedCurrency>() {
            @Override
            public SupportedCurrency call() throws Exception {
                return bdr.loadCurrency(subscriptionKey1, billingTime);
            }
        });
        assertEquals("EUR", entry1.getCurrencyISOCode());

        final long subscriptionKey2 = result.getSubscriptionKeys().get(1)
                .longValue();
        SupportedCurrency entry2 = runTX(new Callable<SupportedCurrency>() {
            @Override
            public SupportedCurrency call() throws Exception {
                return bdr.loadCurrency(subscriptionKey2, billingTime);
            }
        });
        assertEquals("USD", entry2.getCurrencyISOCode());
    }

    /**
     * Create two subscriptions with 2 currencies. One subscription is created
     * AFTER the billing run. The currency retrieved from history will not
     * reflect this later change.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSubscriptionsForCustomer_TwoCurrenciesFutureChange()
            throws Exception {
        final long billingTime = System.currentTimeMillis();
        long modTime = billingTime + 5000;
        createSubUsingUSD(modTime);
        CustomerData result = runTX(new Callable<CustomerData>() {
            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(
                        Scenario.getCustomer().getKey(), 1, billingTime, -1));
            }
        });

        final long subscriptionKey1 = result.getSubscriptionKeys().get(0)
                .longValue();
        SupportedCurrency entry1 = runTX(new Callable<SupportedCurrency>() {
            @Override
            public SupportedCurrency call() throws Exception {
                return bdr.loadCurrency(subscriptionKey1, billingTime);
            }
        });
        assertEquals("EUR", entry1.getCurrencyISOCode());

        final long subscriptionKey2 = result.getSubscriptionKeys().get(1)
                .longValue();
        SupportedCurrency entry2 = runTX(new Callable<SupportedCurrency>() {
            @Override
            public SupportedCurrency call() throws Exception {
                return bdr.loadCurrency(subscriptionKey2, billingTime);
            }
        });
        assertEquals("EUR", entry2.getCurrencyISOCode());
    }

    // ----------------------------------------------------------------------------
    // internal methods

    /**
     * Creates a subscription that is based on a price model using USD as
     * currency.
     * 
     * @param modTime
     *            The time the change to use USD should be performed at.
     * @throws Exception
     */
    private void createSubUsingUSD(final long modTime) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                dm.persist(sc);

                Subscription subNew = Subscriptions.createSubscription(dm,
                        Scenario.getCustomer().getOrganizationId(),
                        Scenario.getProduct().getProductId(), "SubUSD",
                        Scenario.getSupplier());
                dm.flush();
                subNew.setHistoryModificationTime(Long.valueOf(modTime));
                PriceModel priceModel = subNew.getPriceModel();
                priceModel.setCurrency(sc);
                priceModel.setHistoryModificationTime(Long.valueOf(modTime));

                return null;
            }
        });
    }
}
