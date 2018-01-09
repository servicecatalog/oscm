/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *  Completion Time: 19.09.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.oscm.test.Numbers.L100;
import static org.oscm.test.Numbers.L200;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.billingservice.dao.model.EventPricingData;
import org.oscm.billingservice.dao.model.OrganizationAddressData;
import org.oscm.billingservice.dao.model.SteppedPriceData;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Discount;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.CatalogEntries;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PaymentInfos;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Test class for BilllingDataRetrievalServiceBean.
 * 
 */
public class BillingDataRetrievalServiceBeanContainerIT extends EJBTestBase {

    private static final String PRODUCT_1 = "PRODUCT_1";
    private static final String TECHNICAL_PRODUCT = "testTechProd";

    private DataService dm;
    private BillingDataRetrievalServiceLocal bdr;
    private Organization supplier;
    private Organization secondSupplierForDiscount;
    private Organization customer;
    private Subscription sub;
    private static final String EUR = "EUR";

    /**
     * Set in {@link #prepareDiscount(BigDecimal, long, long, long, long, long)}
     * - use only in discount tests
     */
    private long discountCustomerKey;

    private TechnicalProduct technicalProduct;
    private Product product;
    private PriceModel priceModel;

    private long eventKey;

    /**
     * Common setup for the test class.
     */
    @Override
    public void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());

        dm = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);

        supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                createPaymentTypes(dm);
                createOrganizationRoles(dm);
                SupportedCountries.createSomeSupportedCountries(dm);
                Organization org = Organizations.createOrganization(dm,
                        OrganizationRoleType.SUPPLIER);
                return org;
            }
        });
        customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization customer = Organizations.createCustomer(dm,
                        supplier);
                secondSupplierForDiscount = Organizations.createOrganization(dm,
                        OrganizationRoleType.SUPPLIER);
                return customer;
            }
        });

        technicalProduct = createTechnicalProduct();
        product = createProduct(technicalProduct);
        priceModel = createPriceModel(product);

        sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Subscription sub = Subscriptions.createSubscription(dm,
                        customer.getOrganizationId(), product.getProductId(),
                        "subscriptionId", supplier);
                PaymentType pt = new PaymentType();
                pt.setPaymentTypeId("INVOICE");
                pt = (PaymentType) dm.getReferenceByBusinessKey(pt);
                PaymentInfo pi = PaymentInfos.createPaymentInfo(customer, dm,
                        pt);
                sub.setPaymentInfo(pi);
                BillingContact bc = PaymentInfos.createBillingContact(dm,
                        customer);
                sub.setBillingContact(bc);

                dm.flush();
                return sub;
            }
        });
    }

    /**
     * Test for getting stepped prices for Price Model.
     */
    @Test
    public void testGetSteppedPricesForPriceModel() throws Exception {
        // price model is already created
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SteppedPrice steppedPrice = new SteppedPrice();

                steppedPrice.setPriceModel(priceModel);

                steppedPrice.setLimit(L100);

                dm.persist(steppedPrice);
                dm.flush();

                List<SteppedPriceData> steppedPriceHistory = bdr
                        .loadSteppedPricesForPriceModel(priceModel.getKey(),
                                System.currentTimeMillis() * 2);

                assertEquals(1, steppedPriceHistory.size());
                assertEquals(100,
                        steppedPriceHistory.get(0).getLimit().longValue());
                return null;
            }
        });
    }

    /**
     * Test for getting event pricing for price model.
     */
    @Test
    public void testGetEventPricing() throws Exception {
        // price model is already created
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SteppedPrice steppedPrice = new SteppedPrice();

                steppedPrice.setPriceModel(priceModel);

                steppedPrice.setLimit(L100);

                dm.persist(steppedPrice);
                dm.flush();

                Map<String, EventPricingData> eventPrices = bdr
                        .loadEventPricing(priceModel.getKey(),
                                System.currentTimeMillis());

                // Check if the eventPricingData has set the correct event key
                // value.
                EventPricingData eventPricingData = eventPrices
                        .get("eventIdentifier");
                Assert.assertNotNull(eventPricingData);
                Assert.assertTrue(eventKey > 0);
                Assert.assertEquals(eventPricingData.getEventKey(), eventKey);

                return null;
            }
        });
    }

    /**
     * Test for getting stepped prices for priced event.
     */
    @Test
    public void testGetSteppedPricesForEvent() throws Exception {
        // price model is already created
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SteppedPrice steppedPrice = new SteppedPrice();
                steppedPrice.setPricedEvent(
                        priceModel.getConsideredEvents().get(0));
                steppedPrice.setLimit(L200);

                dm.persist(steppedPrice);
                dm.flush();

                List<SteppedPriceData> steppedPriceHistory = bdr
                        .loadSteppedPricesForEvent(
                                priceModel.getConsideredEvents().get(0)
                                        .getKey(),
                                System.currentTimeMillis() * 2);

                assertEquals(1, steppedPriceHistory.size());
                assertEquals(200,
                        steppedPriceHistory.get(0).getLimit().longValue());
                return null;
            }
        });
    }

    /**
     * Test for getting discount value. Discount started before billing and
     * ended after billing period. There is a discount for this billing period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue1() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final long discountStar = 60;
        final long discountEnd = 240;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(expectedValue, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertEquals(getSecondDiscount(expectedValue), actualValue2);

    }

    /**
     * Test for getting discount value. Discount started before billing and
     * ended before billing period. No discount for this billing period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue2() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final long discountStar = 70;
        final long discountEnd = 80;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(null, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertEquals(getSecondDiscount(expectedValue), actualValue2);

    }

    /**
     * Test for getting discount value. Discount started before billing and
     * ended inside of billing period. There is a discount for this billing
     * period.
     * 
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue3() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final long discountStar = 90;
        final long discountEnd = 110;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(expectedValue, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertEquals(getSecondDiscount(expectedValue), actualValue2);

    }

    /**
     * Test for getting discount value. Discount started inside billing and
     * ended inside of billing period. There is a discount for this billing
     * period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue4() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final long discountStar = 120;
        final long discountEnd = 130;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(expectedValue, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertEquals(getSecondDiscount(expectedValue), actualValue2);

    }

    /**
     * Test for getting discount value. Discount started inside billing and
     * ended after billing period. There is a discount for this billing period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue5() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final long discountStar = 140;
        final long discountEnd = 210;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(expectedValue, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertEquals(getSecondDiscount(expectedValue), actualValue2);

    }

    /**
     * Test for getting discount value. Discount started after billing and ended
     * after billing period. There is no discount for this billing period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue6() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final long discountStar = 220;
        final long discountEnd = 230;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(null, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertNull(actualValue2);

    }

    /**
     * Test for getting discount value. Discount started before billing and
     * ended exactly at billing start period. There is a discount for this
     * billing period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue7() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final long discountStar = 90;
        final long discountEnd = billingStart;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(expectedValue, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertEquals(getSecondDiscount(expectedValue), actualValue2);

    }

    /**
     * Test for getting discount value. Discount started exactly at billing
     * start and ended inside billing period. There is a discount for this
     * billing period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue8() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final long discountStar = billingStart;
        final long discountEnd = 110;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(expectedValue, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertEquals(getSecondDiscount(expectedValue), actualValue2);

    }

    /**
     * Test for getting discount value. Discount started inside billing period
     * and ended exactly at billing end. There is a discount for this billing
     * period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue9() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final long discountStar = 190;
        final long discountEnd = billingEnd;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(expectedValue, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertEquals(getSecondDiscount(expectedValue), actualValue2);
    }

    /**
     * Test to retrieve the discount value for a already deleted discount.
     * Expected value is <code>null</code>.
     */
    @Test
    public void testGetDiscountValue_Deleted() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final long discountStar = 190;
        final long discountEnd = billingEnd;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(expectedValue, actualValue);
        removeDiscounts();

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        assertNull(actualValue2);
    }

    /**
     * Test to retrieve the discount value for a period where the latest history
     * entry is not effective because it is after the period.
     */
    @Test
    public void testGetDiscountValue_Updated_NotValidForCurrentPeriodAfter()
            throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final Long discountStart = Long.valueOf(190);
        final long discountEnd = billingEnd;

        prepareDiscount(expectedValue, billingStart, billingEnd,
                discountStart.longValue(), discountEnd, supplier.getKey());
        updateDiscounts(expectedValue, Long.valueOf(201), null);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        assertNull("no discount must be found, but received: " + actualValue2,
                actualValue2);
    }

    /**
     * Test to retrieve the discount value for a period where the latest history
     * entry is not effective because it is before the period.
     */
    @Test
    public void testGetDiscountValue_Updated_NotValidForCurrentPeriodBefore()
            throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final Long discountStart = Long.valueOf(190);
        final long discountEnd = billingEnd;

        prepareDiscount(expectedValue, billingStart, billingEnd,
                discountStart.longValue(), discountEnd, supplier.getKey());
        updateDiscounts(expectedValue, Long.valueOf(91), Long.valueOf(99));

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        assertNull("no discount must be found, but received: " + actualValue2,
                actualValue2);
    }

    /**
     * Test to retrieve the discount value for a period where the latest history
     * entry is not effective because it is before the period.
     */
    @Test
    public void testGetDiscountValue_Updated_ValidForCurrentPeriod()
            throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 200;

        final Long discountStart = Long.valueOf(190);
        final long discountEnd = billingEnd;

        prepareDiscount(expectedValue, billingStart, billingEnd,
                discountStart.longValue(), discountEnd, supplier.getKey());
        updateDiscounts(expectedValue, Long.valueOf(195), null);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        assertNotNull("discount must be found, but received: " + actualValue2,
                actualValue2);
        assertEquals(expectedValue, actualValue2);
    }

    private void removeDiscounts() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Query query = dm.createQuery("SELECT d FROM Discount d");
                List<Discount> list = ParameterizedTypes
                        .list(query.getResultList(), Discount.class);
                for (Discount discount : list) {
                    dm.remove(discount);
                }
                return null;
            }
        });
    }

    /**
     * Test for getting discount value. Discount started exactly at billing end
     * period and ended after billing end. Billing period end is first
     * millisecond of next month. In algorithm period is always reduced to last
     * millisecond to one millisecond There is a no discount for this billing
     * period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue10() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 201;
        final long discountStar = billingEnd;
        final long discountEnd = 210;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertNull(actualValue);
        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertNull(actualValue2);

    }

    /**
     * Test for getting discount value.Discount started before billing start
     * period and has no end period - null value. In algorithm period is always
     * reduced to last millisecond to one millisecond There is a discount for
     * this billing period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue11() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 201;
        final long discountStar = 99;
        final long discountEnd = -1;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(expectedValue, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertEquals(getSecondDiscount(expectedValue), actualValue2);

    }

    /**
     * Test for getting discount value.Discount started in billing period and
     * has no end period - null value. In algorithm period is always reduced to
     * last millisecond to one millisecond There is a discount for this billing
     * period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue12() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 201;
        final long discountStar = 101;
        final long discountEnd = -1;

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertEquals(expectedValue, actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertEquals(getSecondDiscount(expectedValue), actualValue2);

    }

    /**
     * Test for getting discount value.Discount started after billing period and
     * has no end period - null value. In algorithm period is always reduced to
     * last millisecond to one millisecond There is no discount for this billing
     * period.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDiscountValue13() throws Exception {
        final BigDecimal expectedValue = new BigDecimal("5.00");
        final long billingStart = 100;
        final long billingEnd = 201;
        final long discountStar = billingEnd + 1;
        final long discountEnd = -1; // null

        BigDecimal actualValue = prepareDiscount(expectedValue, billingStart,
                billingEnd, discountStar, discountEnd, supplier.getKey());
        Assert.assertNull(actualValue);

        BigDecimal actualValue2 = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, secondSupplierForDiscount.getKey());
            }
        });
        Assert.assertNull(actualValue2);

    }

    /**
     * Helper method for preparing discount.
     * 
     * @param expectedValue
     * @param billingStart
     * @param billingEnd
     * @param discountStart
     * @param discountEnd
     * @return
     * @throws Exception
     */
    private BigDecimal prepareDiscount(final BigDecimal expectedValue,
            final long billingStart, final long billingEnd,
            final long discountStart, final long discountEnd,
            final long supplierKey) throws Exception {
        discountCustomerKey = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Organization supplier = dm.getReference(Organization.class,
                        supplierKey);
                Organization customer = Organizations.createCustomer(dm,
                        supplier);

                Organization secondSupplier = dm.getReference(
                        Organization.class, secondSupplierForDiscount.getKey());
                Organizations.addSupplierToCustomer(dm, secondSupplier,
                        customer);
                return new Long(customer.getKey());
            }
        }).longValue();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (discountEnd == -1) {
                    createDiscount(discountCustomerKey, expectedValue,
                            Long.valueOf(discountStart), null, supplierKey);
                } else {
                    createDiscount(discountCustomerKey, expectedValue,
                            Long.valueOf(discountStart),
                            Long.valueOf(discountEnd), supplierKey);
                }

                // also create a discount from the second supplier to check
                // that there is no interference
                BigDecimal secondSupplierDiscount = getSecondDiscount(
                        expectedValue);
                long secondSupplierKey = secondSupplierForDiscount.getKey();
                createDiscount(discountCustomerKey, secondSupplierDiscount,
                        Long.valueOf(discountStart), null, secondSupplierKey);
                return null;
            }
        });

        final BigDecimal actualValue = runTX(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                return bdr.loadDiscountValue(discountCustomerKey, billingStart,
                        billingEnd, supplierKey);
            }
        });
        return actualValue;
    }

    private void updateDiscounts(final BigDecimal discountValue,
            final Long discountStart, final Long discountEnd) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Query query = dm.createQuery("SELECT d FROM Discount d");
                List<Discount> list = ParameterizedTypes
                        .list(query.getResultList(), Discount.class);
                for (Discount discount : list) {
                    discount.setEndTime(discountEnd);
                    discount.setStartTime(discountStart);
                    discount.setValue(discountValue);
                }
                return null;
            }
        });
    }

    /**
     * Helper method for discount creating.
     * 
     * @param value
     * @param startTime
     * @param endTime
     * @return
     * 
     * @throws Exception
     */
    private long createDiscount(long customerKey, BigDecimal value,
            Long startTime, Long endTime, long supplierKey) throws Exception {
        Discount discount = new Discount();
        Organization organization = dm.getReference(Organization.class,
                customerKey);
        List<OrganizationReference> sources = organization.getSources();
        OrganizationReference suppToCust = null;
        for (OrganizationReference ref : sources) {
            if (ref.getSourceKey() == supplierKey) {
                suppToCust = ref;
            }
        }
        Assert.assertNotNull("OrganizationReference to supplier not found",
                suppToCust);
        discount.setOrganizationReference(suppToCust);
        discount.setValue(value);
        discount.setStartTime(startTime);
        discount.setEndTime(endTime);
        dm.persist(discount);
        final long key = discount.getKey();

        return key;
    }

    @Test
    public void testGetOrganizationBillingAddressFromHistoryNoMatch()
            throws Exception {
        OrganizationAddressData result = runTX(
                new Callable<OrganizationAddressData>() {
                    @Override
                    public OrganizationAddressData call() {
                        return bdr.loadOrganizationBillingDataFromHistory(1L,
                                0L);
                    }
                });
        Assert.assertNull(result);
    }

    @Test
    public void testGetOrganizationBillingAddressFromHistoryNoBillingContact()
            throws Exception {
        OrganizationAddressData orgData = runTX(
                new Callable<OrganizationAddressData>() {
                    @Override
                    public OrganizationAddressData call() {
                        return bdr.loadOrganizationBillingDataFromHistory(
                                supplier.getKey(), 0L);
                    }
                });
        Assert.assertEquals(supplier.getName(), orgData.getOrganizationName());
        Assert.assertEquals(supplier.getAddress(), orgData.getAddress());
        Assert.assertEquals(supplier.getEmail(), orgData.getEmail());
    }

    @Test
    public void testGetOrganizationBillingAddressFromHistoryEvaluateOrgData()
            throws Exception {
        useOrgAdress();
        OrganizationAddressData orgData = runTX(
                new Callable<OrganizationAddressData>() {
                    @Override
                    public OrganizationAddressData call() {
                        return bdr.loadOrganizationBillingDataFromHistory(
                                supplier.getKey(), sub.getKey());
                    }
                });
        Assert.assertEquals(supplier.getName(), orgData.getOrganizationName());
        Assert.assertEquals(supplier.getAddress(), orgData.getAddress());
        Assert.assertEquals(supplier.getEmail(), orgData.getEmail());
    }

    @Test
    public void testGetOrganizationBillingAddressFromHistoryEvaluateBillingContact()
            throws Exception {
        OrganizationAddressData orgData = runTX(
                new Callable<OrganizationAddressData>() {
                    @Override
                    public OrganizationAddressData call() {
                        return bdr.loadOrganizationBillingDataFromHistory(
                                supplier.getKey(), sub.getKey());
                    }
                });
        Assert.assertEquals(sub.getBillingContact().getCompanyName(),
                orgData.getOrganizationName());
        Assert.assertEquals(sub.getBillingContact().getAddress(),
                orgData.getAddress());
        Assert.assertEquals(sub.getBillingContact().getEmail(),
                orgData.getEmail());
    }

    @Test
    public void testGetOrganizationBillingAddressFromHistoryEvaluateOrgDataMultipleEntries()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization organization = (Organization) dm
                        .getReferenceByBusinessKey(supplier);
                organization.setAddress("new Address");
                organization.setName("new Name");
                organization.setEmail("new Mail");
                return null;
            }
        });
        useOrgAdress();
        OrganizationAddressData orgData = runTX(
                new Callable<OrganizationAddressData>() {
                    @Override
                    public OrganizationAddressData call() {
                        return bdr.loadOrganizationBillingDataFromHistory(
                                supplier.getKey(), sub.getKey());
                    }
                });
        Assert.assertEquals("new Name", orgData.getOrganizationName());
        Assert.assertEquals("new Address", orgData.getAddress());
        Assert.assertEquals("new Mail", orgData.getEmail());
    }

    private void useOrgAdress() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = (Subscription) dm
                        .getReferenceByBusinessKey(sub);
                BillingContact bc = subscription.getBillingContact();
                bc.setOrgAddressUsed(true);
                return null;
            }
        });
    }

    @Test
    public void testGetOrganizationBillingAddressFromHistoryEvaluateBillingContactMultipleEntries()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = (Subscription) dm
                        .getReferenceByBusinessKey(sub);
                BillingContact bc = subscription.getBillingContact();
                bc.setAddress("new bc Address");
                bc.setCompanyName("new bc Name");
                bc.setEmail("new bc Mail");
                return null;
            }
        });
        OrganizationAddressData orgData = runTX(
                new Callable<OrganizationAddressData>() {
                    @Override
                    public OrganizationAddressData call() {
                        return bdr.loadOrganizationBillingDataFromHistory(
                                supplier.getKey(), sub.getKey());
                    }
                });
        Assert.assertEquals("new bc Name", orgData.getOrganizationName());
        Assert.assertEquals("new bc Address", orgData.getAddress());
        Assert.assertEquals("new bc Mail", orgData.getEmail());
    }

    @Test
    public void getChargingOrgKeyForSubscription() throws Exception {
        Long supplierKey = runTX(new Callable<Long>() {
            @Override
            public Long call() {
                return Long.valueOf(
                        bdr.loadChargingOrgKeyForSubscription(sub.getKey()));
            }
        });
        assertEquals(supplier.getKey(), supplierKey.longValue());
    }

    @Test
    public void getChargingOrgKeyForSubscription_SubOfSecondCustomer()
            throws Exception {
        final Long subKey = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {

                Organization supp = dm.getReference(Organization.class,
                        supplier.getKey());
                Organization customer = Organizations.createCustomer(dm, supp);
                Subscription subscription = Subscriptions.createSubscription(dm,
                        customer.getOrganizationId(), product.getProductId(),
                        "test", supp);
                return Long.valueOf(subscription.getKey());
            }
        });
        Long supplierKey = runTX(new Callable<Long>() {
            @Override
            public Long call() {
                return Long.valueOf(bdr
                        .loadChargingOrgKeyForSubscription(subKey.longValue()));
            }
        });
        assertEquals(supplier.getKey(), supplierKey.longValue());
    }

    @Test
    public void getChargingOrgKeyForSubscription_OnBrokerService()
            throws Exception {
        // given
        final Organization broker = givenOrganization(
                OrganizationRoleType.BROKER);
        final Marketplace mp = givenMarketplace(broker);
        final Product partnerTemplate = createPartnerProductCopy(product,
                broker, mp, ServiceStatus.ACTIVE);
        final Subscription subscription = givenSubscription(partnerTemplate,
                broker, mp);

        // when
        Long chargingOrgKey = runTX(new Callable<Long>() {
            @Override
            public Long call() {
                return Long.valueOf(bdr.loadChargingOrgKeyForSubscription(
                        subscription.getKey()));
            }
        });

        // then
        assertEquals(
                "Supplier must be the charging organization for a subscription on a service sold by a broker.",
                supplier.getKey(), chargingOrgKey.longValue());
    }

    @Test
    public void getChargingOrgKeyForSubscription_OnResellerService()
            throws Exception {
        // given
        final Organization reseller = givenOrganization(
                OrganizationRoleType.RESELLER);
        final Marketplace mp = givenMarketplace(reseller);
        final Product partnerTemplate = createPartnerProductCopy(product,
                reseller, mp, ServiceStatus.ACTIVE);
        final Subscription subscription = givenSubscription(partnerTemplate,
                reseller, mp);

        // when
        Long chargingOrgKey = runTX(new Callable<Long>() {
            @Override
            public Long call() {
                return Long.valueOf(bdr.loadChargingOrgKeyForSubscription(
                        subscription.getKey()));
            }
        });

        // then
        assertEquals(reseller.getKey(), chargingOrgKey.longValue());
    }

    @Test
    public void getVendorRolesForSubscription_Supplier() throws Exception {
        // when
        List<OrganizationRoleType> roles = runTX(
                new Callable<List<OrganizationRoleType>>() {
                    @Override
                    public List<OrganizationRoleType> call() {
                        return bdr.loadVendorRolesForSubscription(sub.getKey());
                    }
                });

        // then
        assertEquals(2, roles.size());
        assertEquals(OrganizationRoleType.CUSTOMER, roles.get(0));
        assertEquals(OrganizationRoleType.SUPPLIER, roles.get(1));
    }

    @Test
    public void getVendorRolesForSubscription_Reseller() throws Exception {
        // given
        final Organization reseller = givenOrganization(
                OrganizationRoleType.RESELLER);
        final Marketplace mp = givenMarketplace(reseller);
        final Product partnerTemplate = createPartnerProductCopy(product,
                reseller, mp, ServiceStatus.ACTIVE);
        final Subscription subscription = givenSubscription(partnerTemplate,
                reseller, mp);

        // when
        List<OrganizationRoleType> roles = runTX(
                new Callable<List<OrganizationRoleType>>() {
                    @Override
                    public List<OrganizationRoleType> call() {
                        return bdr.loadVendorRolesForSubscription(
                                subscription.getKey());
                    }
                });

        // then
        assertEquals(3, roles.size());
        assertEquals(OrganizationRoleType.CUSTOMER, roles.get(0));
        assertEquals(OrganizationRoleType.MARKETPLACE_OWNER, roles.get(1));
        assertEquals(OrganizationRoleType.RESELLER, roles.get(2));
    }

    @Test
    public void getVendorRolesForSubscription_Broker() throws Exception {
        // given
        final Organization reseller = givenOrganization(
                OrganizationRoleType.BROKER);
        final Marketplace mp = givenMarketplace(reseller);
        final Product partnerTemplate = createPartnerProductCopy(product,
                reseller, mp, ServiceStatus.ACTIVE);
        final Subscription subscription = givenSubscription(partnerTemplate,
                reseller, mp);

        // when
        List<OrganizationRoleType> roles = runTX(
                new Callable<List<OrganizationRoleType>>() {
                    @Override
                    public List<OrganizationRoleType> call() {
                        return bdr.loadVendorRolesForSubscription(
                                subscription.getKey());
                    }
                });

        // then
        assertEquals(3, roles.size());
        assertEquals(OrganizationRoleType.BROKER, roles.get(0));
        assertEquals(OrganizationRoleType.CUSTOMER, roles.get(1));
        assertEquals(OrganizationRoleType.MARKETPLACE_OWNER, roles.get(2));
    }

    @Test
    public void getSupplierKeyForSubscription_supplier() throws Exception {
        // when
        Long supplierKey = runTX(new Callable<Long>() {
            @Override
            public Long call() {
                return Long.valueOf(
                        bdr.loadSupplierKeyForSubscription(sub.getKey()));
            }
        });

        // then
        assertEquals(supplier.getKey(), supplierKey.longValue());
    }

    @Test
    public void getSupplierKeyForSubscription_vendor() throws Exception {
        // given
        final Organization reseller = givenOrganization(
                OrganizationRoleType.RESELLER);
        final Marketplace mp = givenMarketplace(reseller);
        final Product partnerTemplate = createPartnerProductCopy(product,
                reseller, mp, ServiceStatus.ACTIVE);
        final Subscription subscription = givenSubscription(partnerTemplate,
                reseller, mp);

        // when
        Long supplierKey = runTX(new Callable<Long>() {
            @Override
            public Long call() {
                return Long.valueOf(bdr
                        .loadSupplierKeyForSubscription(subscription.getKey()));
            }
        });

        // then
        assertEquals(supplier.getKey(), supplierKey.longValue());
    }

    private Subscription givenSubscription(final Product template,
            final Organization vendor, final Marketplace mp) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Organization customer = Organizations.createCustomer(dm,
                        vendor);
                Subscription subscription = Subscriptions.createSubscription(dm,
                        customer.getOrganizationId(), template, mp, 1);
                return subscription;
            }
        });
    }

    private Organization givenOrganization(final OrganizationRoleType... roles)
            throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(dm,
                        roles[0].name(), roles);
                return org;
            }
        });
    }

    private Marketplace givenMarketplace(final Organization owner)
            throws Exception {
        return runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace mp = Marketplaces.createGlobalMarketplace(owner,
                        "mp_" + owner.getOrganizationId(), dm, BigDecimal.TEN,
                        BigDecimal.TEN, BigDecimal.TEN);
                Organizations.addOrganizationToRole(dm, owner,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                dm.flush();
                return mp;
            }
        });
    }

    /**
     * Create price model for testing.
     * 
     * @param product
     * @return
     * @throws Exception
     */
    private PriceModel createPriceModel(final Product product)
            throws Exception {
        final PriceModel priceModel = runTX(new Callable<PriceModel>() {
            @Override
            public PriceModel call() throws Exception {
                PriceModel priceModel = null;
                priceModel = new PriceModel();

                priceModel.setType(PriceModelType.PRO_RATA);
                priceModel.setProduct(product);

                List<PricedEvent> events = new ArrayList<>();

                Event event = new Event();
                event.setEventIdentifier("eventIdentifier");
                event.setEventType(EventType.PLATFORM_EVENT);

                dm.persist(event);

                PricedEvent pricedEvent = new PricedEvent();
                pricedEvent.setEvent(event);
                pricedEvent.setPriceModel(priceModel);
                events.add(pricedEvent);

                priceModel.setConsideredEvents(events);

                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance(EUR));
                sc = (SupportedCurrency) dm.find(sc);
                priceModel.setCurrency(sc);

                dm.persist(priceModel);
                dm.flush();
                eventKey = event.getKey();
                return priceModel;
            }
        });
        return priceModel;
    }

    /**
     * Create product for testing.
     * 
     * @param technicalProduct
     * @return
     * @throws Exception
     */
    private Product createProduct(final TechnicalProduct technicalProduct)
            throws Exception {
        final Product product = runTX(new Callable<Product>() {
            @Override
            public Product call() {
                Product product = null;

                try {
                    product = Products.createProduct(supplier, technicalProduct,
                            true, PRODUCT_1, null, dm);

                } catch (NonUniqueBusinessKeyException ex) {
                    Assert.fail(
                            "Error on creating input data for test. SaasNonUniqueBusinessKeyException");
                }
                return product;
            }
        });
        return product;
    }

    private Product createPartnerProductCopy(final Product template,
            final Organization vendor, final Marketplace mp,
            final ServiceStatus status) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product product = Products.createProductResaleCopy(template,
                        vendor, dm);
                product.setProductId(
                        "resaleProd_" + vendor.getOrganizationId());
                product = Products.setStatusForProduct(dm, product, status);

                CatalogEntries.createWithBrokerShare(dm, mp, product,
                        BigDecimal.TEN);
                return product;
            }
        });
    }

    /**
     * Create technical product for testing.
     * 
     * @return technical product.
     * @throws Exception
     */
    private TechnicalProduct createTechnicalProduct() throws Exception {
        final TechnicalProduct technicalProduct = runTX(
                new Callable<TechnicalProduct>() {
                    @Override
                    public TechnicalProduct call() {
                        TechnicalProduct technicalProduct = null;

                        try {
                            technicalProduct = TechnicalProducts
                                    .createTechnicalProduct(dm, supplier,
                                            TECHNICAL_PRODUCT, false,
                                            ServiceAccessType.LOGIN);

                        } catch (NonUniqueBusinessKeyException ex) {
                            Assert.fail(
                                    "Error on creating input data for test. SaasNonUniqueBusinessKeyException");
                        }
                        return technicalProduct;
                    }
                });
        return technicalProduct;
    }

    /**
     * Adds 50 to the the provided discount
     * 
     * @param discount
     * @return discount + 50
     */
    private BigDecimal getSecondDiscount(final BigDecimal discount) {
        long longValue = discount.longValue();
        longValue += 50;
        String value = String.valueOf(longValue) + ".00";
        BigDecimal secondSupplierDiscount = new BigDecimal(value);
        return secondSupplierDiscount;
    }

}
