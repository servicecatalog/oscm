/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.L123;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.converter.BigDecimalComparator;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ObjectNotFoundException;

public class PricedParameterIT extends DomainObjectTestBase {

    private PricedParameter pricedParameter;
    private ParameterDefinition parameterDefinition;
    private ParameterSet parameterSet;

    @Override
    protected void dataSetup() throws Exception {
        createSupportedCurrencies(mgr);
        createOrganizationRoles(mgr);
        Organization org = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(mgr,
                org, "testTP", false, ServiceAccessType.LOGIN);
        Product prod = Products.createProduct(org, tp, true, "testProduct",
                null, mgr);

        parameterDefinition = new ParameterDefinition();
        parameterDefinition.setConfigurable(true);
        parameterDefinition.setParameterId("testParam");
        parameterDefinition.setParameterType(ParameterType.SERVICE_PARAMETER);
        parameterDefinition.setTechnicalProduct(tp);
        parameterDefinition.setValueType(ParameterValueType.ENUMERATION);
        mgr.persist(parameterDefinition);

        ParameterOption option = new ParameterOption();
        option.setOptionId("1");
        option.setParameterDefinition(parameterDefinition);
        mgr.persist(option);
        parameterDefinition.setOptionList(Collections.singletonList(option));

        tp.setParameterDefinitions(Collections
                .singletonList(parameterDefinition));

        parameterSet = new ParameterSet();
        parameterSet.setProduct(prod);
        mgr.persist(parameterSet);
        Parameter parameter = new Parameter();
        parameter.setConfigurable(true);
        parameter.setParameterDefinition(parameterDefinition);
        parameter.setValue("1");
        parameter.setParameterSet(parameterSet);
        mgr.persist(parameter);

        PriceModel priceModel = prod.getPriceModel();
        pricedParameter = new PricedParameter();
        pricedParameter.setParameter(parameter);
        pricedParameter.setPriceModel(priceModel);
        pricedParameter.setPricedOptionList(new ArrayList<PricedOption>());
        mgr.persist(pricedParameter);
        PricedOption pricedOption = new PricedOption();
        pricedOption.setPricedParameter(pricedParameter);
        pricedOption.setParameterOptionKey(option.getKey());
        mgr.persist(pricedOption);
        pricedParameter.setPricedOptionList(Collections
                .singletonList(pricedOption));
        priceModel.setSelectedParameters(Collections
                .singletonList(pricedParameter));
    }

    @Test
    public void testCopy() {
        Parameter param = new Parameter();
        param.setParameterDefinition(parameterDefinition);
        ParameterSet set = new ParameterSet();
        set.setParameters(Collections.singletonList(param));
        PriceModel priceModel = new PriceModel();
        PricedParameter copy = pricedParameter.copy(priceModel, set);
        Assert.assertTrue(pricedParameter.getKey() != copy.getKey());
        Assert.assertEquals(param, copy.getParameter());
        Assert.assertEquals(priceModel, copy.getPriceModel());
        Assert.assertEquals(pricedParameter.getPricePerSubscription(),
                copy.getPricePerSubscription());
        Assert.assertEquals(pricedParameter.getPricePerUser(),
                copy.getPricePerUser());
        Assert.assertEquals(pricedParameter.getPricedOptionList().size(), copy
                .getPricedOptionList().size());
    }

    @Test
    public void testHistory() throws Exception {
        List<DomainHistoryObject<?>> history = runTX(new Callable<List<DomainHistoryObject<?>>>() {

            @Override
            public List<DomainHistoryObject<?>> call() throws Exception {
                return mgr.findHistory(pricedParameter);
            }
        });
        for (DomainHistoryObject<?> dho : history) {
            Assert.assertTrue(dho instanceof PricedParameterHistory);
            PricedParameterHistory hist = (PricedParameterHistory) dho;
            Assert.assertEquals(pricedParameter.getKey(), hist.getObjKey());
            Assert.assertEquals(pricedParameter.getParameter().getKey(),
                    hist.getParameterObjKey());
            Assert.assertEquals(pricedParameter.getPriceModel().getKey(),
                    hist.getPriceModelObjKey());
            Assert.assertEquals(pricedParameter.getPricePerSubscription(),
                    hist.getPricePerSubscription());
            Assert.assertEquals(pricedParameter.getPricePerUser(),
                    hist.getPricePerUser());
        }
    }

    @Test
    public void testSaveWithSteppedPrice() throws Exception {
        List<SteppedPrice> steps = addAndGetSteppedPrices();
        verifySteppedPrices(steps);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeleteWithSteppedPrice() throws Exception {
        final SteppedPrice sp = addSteppedPrice();
        verifySteppedPriceDeletion(sp);
    }

    private void verifySteppedPrices(List<SteppedPrice> steps) {
        Assert.assertNotNull(steps);
        Assert.assertEquals(1, steps.size());
        SteppedPrice sp = steps.get(0);
        Assert.assertEquals(new BigDecimal(123), sp.getAdditionalPrice());
        Assert.assertEquals(123, sp.getFreeEntityCount());
        Assert.assertEquals(Long.valueOf(123), sp.getLimit());
        Assert.assertEquals(new BigDecimal(123), sp.getPrice());
        Assert.assertNotNull(sp.getPricedParameter());
    }

    private List<SteppedPrice> addAndGetSteppedPrices() throws Exception {
        addSteppedPrice();
        List<SteppedPrice> steps = runTX(new Callable<List<SteppedPrice>>() {
            @Override
            public List<SteppedPrice> call() throws Exception {
                PricedParameter pp = mgr.getReference(PricedParameter.class,
                        pricedParameter.getKey());
                List<SteppedPrice> steppedPrices;
                steppedPrices = pp.getSteppedPrices();
                steppedPrices.size();
                return steppedPrices;
            }
        });
        return steps;
    }

    private void verifySteppedPriceDeletion(final SteppedPrice sp)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PricedParameter pp = mgr.getReference(PricedParameter.class,
                        pricedParameter.getKey());
                mgr.remove(pp);
                return null;
            }
        });
        try {
            runTX(new Callable<Void>() {
                @Override
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
            @Override
            public SteppedPrice call() throws Exception {
                PricedParameter pp = mgr.getReference(PricedParameter.class,
                        pricedParameter.getKey());
                SteppedPrice sp = new SteppedPrice();
                sp.setAdditionalPrice(new BigDecimal(123));
                sp.setFreeEntityCount(123);
                sp.setLimit(L123);
                sp.setPrice(new BigDecimal(123));
                sp.setPricedParameter(pp);
                pp.setSteppedPrices(Collections.singletonList(sp));
                return sp;
            }
        });
    }

    @Test
    public void testCopyWithSteppedPrice() throws Exception {
        List<SteppedPrice> steps = addAndGetSteppedPrices();
        PricedParameter copy = copyPricedParameter();
        Assert.assertEquals(steps.size(), copy.getSteppedPrices().size());
        SteppedPrice spCopy = copy.getSteppedPrices().get(0);
        Assert.assertEquals(copy, spCopy.getPricedParameter());
    }

    private PricedParameter copyPricedParameter() throws Exception {
        PricedParameter copy = runTX(new Callable<PricedParameter>() {
            @Override
            public PricedParameter call() throws Exception {
                PricedParameter pp = mgr.getReference(PricedParameter.class,
                        pricedParameter.getKey());
                parameterSet = mgr.getReference(ParameterSet.class,
                        parameterSet.getKey());
                PricedParameter copy = pp
                        .copy(pp.getPriceModel(), parameterSet);
                return copy;
            }
        });
        return copy;
    }

    /**
     * Accessing a non defined price must return ZERO (instead of null)
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultPrice() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                pricedParameter = new PricedParameter();
                BigDecimal defaultPrice = pricedParameter
                        .getPricePerSubscription();
                Assert.assertTrue(BigDecimalComparator.isZero(defaultPrice));
                mgr.persist(pricedParameter);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PricedParameter pp = mgr.getReference(PricedParameter.class,
                        pricedParameter.getKey());
                BigDecimal defaultPrice = pp.getPricePerSubscription();
                Assert.assertTrue(BigDecimalComparator.isZero(defaultPrice));
                return null;
            }
        });
    }
}
