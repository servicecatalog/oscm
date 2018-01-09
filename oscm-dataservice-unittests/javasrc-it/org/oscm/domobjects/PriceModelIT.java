/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 07.05.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.L123;
import static org.oscm.test.Numbers.TIMESTAMP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.test.data.Organizations;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Test of the price model domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PriceModelIT extends DomainObjectTestBase {

    private List<PriceModel> models = new ArrayList<PriceModel>();

    @Override
    protected void dataSetup() throws Exception {
        super.dataSetup();
        createSupportedCurrencies(mgr);
    }

    @Test
    public void testAdd() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestCheckCreation();
                return null;
            }
        });
    }

    private void doTestAdd() throws Exception {
        Organization organization = Organizations.createOrganization(mgr);
        // create technical product
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID", false, ServiceAccessType.LOGIN);

        // create two products for it
        Product prod1 = new Product();
        prod1.setVendor(organization);
        prod1.setProductId("Product1");
        prod1.setTechnicalProduct(tProd);
        prod1.setProvisioningDate(TIMESTAMP);
        prod1.setStatus(ServiceStatus.ACTIVE);
        prod1.setType(ServiceType.TEMPLATE);
        ParameterSet emptyPS1 = new ParameterSet();
        prod1.setParameterSet(emptyPS1);

        // Create a parent event
        Event event = new Event();
        event.setEventIdentifier("TEST_EVENT");
        event.setEventType(EventType.SERVICE_EVENT);

        // Create a priced event
        PricedEvent pricedEvent = new PricedEvent();
        pricedEvent.setEventPrice(new BigDecimal(50));
        pricedEvent.setEvent(event);

        // create a price model
        PriceModel pModel = new PriceModel();
        prod1.setPriceModel(pModel);
        pModel.setProduct(prod1);
        List<PricedEvent> events = new ArrayList<PricedEvent>();
        events.add(pricedEvent);
        pModel.setConsideredEvents(events);
        pricedEvent.setPriceModel(pModel);

        // currency
        SupportedCurrency currency = new SupportedCurrency();
        currency.setCurrency(Currency.getInstance("EUR"));
        currency = (SupportedCurrency) mgr.getReferenceByBusinessKey(currency);
        pModel.setCurrency(currency);
        int days = 14;
        pModel.setFreePeriod(days);

        // store the data
        mgr.persist(event);
        mgr.persist(prod1);
        models.add(pModel);
    }

    private void doTestCheckCreation() throws ObjectNotFoundException {
        PriceModel expected = models.get(0);
        PriceModel model = mgr
                .getReference(PriceModel.class, expected.getKey());
        Assert.assertNotNull("Model has not been created", model);
        Assert.assertEquals(expected.getProduct(), model.getProduct());
        Assert.assertEquals(0, model.getSelectedParameters().size());
        Assert.assertEquals(expected.getConsideredEvents().get(0), model
                .getConsideredEvents().get(0));

        List<DomainHistoryObject<?>> history = mgr.findHistory(model);
        Assert.assertEquals(1, history.size());
        PriceModelHistory pmh = (PriceModelHistory) history.get(0);
        Assert.assertEquals(model.getCurrency().getKey(), pmh
                .getCurrencyObjKey().longValue());
        Assert.assertEquals(model.getKey(), pmh.getObjKey());
        Assert.assertEquals(model.getOneTimeFee(), pmh.getOneTimeFee());
        Assert.assertEquals(model.getPeriod(), pmh.getPeriod());
        Assert.assertEquals(model.getPricePerPeriod(), pmh.getPricePerPeriod());
        Assert.assertEquals(model.getPricePerUserAssignment(),
                pmh.getPricePerUserAssignment());
        Assert.assertEquals(model.getProduct().getKey(), pmh.getProductObjKey());
        Assert.assertEquals(model.getFreePeriod(), pmh.getFreePeriod());
    }

    @Test
    public void testSaveWithSteppedPrice() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        addSteppedPrice();
        List<SteppedPrice> steps = runTX(new Callable<List<SteppedPrice>>() {
            public List<SteppedPrice> call() throws Exception {
                PriceModel pm = mgr.getReference(PriceModel.class, models
                        .get(0).getKey());
                List<SteppedPrice> steppedPrices = pm.getSteppedPrices();
                steppedPrices.size();
                return steppedPrices;
            }
        });
        Assert.assertNotNull(steps);
        Assert.assertEquals(1, steps.size());
        SteppedPrice sp = steps.get(0);
        Assert.assertEquals(new BigDecimal(123), sp.getAdditionalPrice());
        Assert.assertEquals(123, sp.getFreeEntityCount());
        Assert.assertEquals(Long.valueOf(123), sp.getLimit());
        Assert.assertEquals(new BigDecimal(123), sp.getPrice());
        Assert.assertNotNull(sp.getPriceModel());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeleteWithSteppedPrice() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        final SteppedPrice sp = addSteppedPrice();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                PriceModel pm = mgr.getReference(PriceModel.class, models
                        .get(0).getKey());
                mgr.remove(pm.getProduct());
                return null;
            }
        });
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    mgr.getReference(SteppedPrice.class, sp.getKey());
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    private SteppedPrice addSteppedPrice() throws Exception {
        return runTX(new Callable<SteppedPrice>() {
            public SteppedPrice call() throws Exception {
                PriceModel pm = mgr.getReference(PriceModel.class, models
                        .get(0).getKey());
                SteppedPrice sp = new SteppedPrice();
                sp.setAdditionalPrice(new BigDecimal(123));
                sp.setFreeEntityCount(123);
                sp.setLimit(L123);
                sp.setPrice(new BigDecimal(123));
                sp.setPriceModel(pm);
                pm.setSteppedPrices(Collections.singletonList(sp));
                return sp;
            }
        });
    }

    @Test
    public void testCopyWithSteppedPrice() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        addSteppedPrice();
        List<SteppedPrice> steps = runTX(new Callable<List<SteppedPrice>>() {
            public List<SteppedPrice> call() throws Exception {
                PriceModel pm = mgr.getReference(PriceModel.class, models
                        .get(0).getKey());
                List<SteppedPrice> steppedPrices = pm.getSteppedPrices();
                steppedPrices.size();
                return steppedPrices;
            }
        });
        PriceModel copy = runTX(new Callable<PriceModel>() {
            public PriceModel call() throws Exception {
                PriceModel pm = mgr.getReference(PriceModel.class, models
                        .get(0).getKey());
                PriceModel copy = pm.copy(null);
                return copy;
            }
        });
        Assert.assertEquals(steps.size(), copy.getSteppedPrices().size());
        SteppedPrice spCopy = copy.getSteppedPrices().get(0);
        Assert.assertEquals(copy, spCopy.getPriceModel());
    }

}
