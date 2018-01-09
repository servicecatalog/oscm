/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;

public class PricedOptionIT extends DomainObjectTestBase {

    private PricedOption pricedOption;

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

        ParameterDefinition parameterDefinition = new ParameterDefinition();
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

        ParameterSet set = new ParameterSet();
        set.setProduct(prod);
        mgr.persist(set);
        Parameter parameter = new Parameter();
        parameter.setConfigurable(true);
        parameter.setParameterDefinition(parameterDefinition);
        parameter.setValue("1");
        parameter.setParameterSet(set);
        mgr.persist(parameter);

        PriceModel priceModel = prod.getPriceModel();
        PricedParameter pricedParameter = new PricedParameter();
        pricedParameter.setParameter(parameter);
        pricedParameter.setPriceModel(priceModel);
        mgr.persist(pricedParameter);

        pricedOption = new PricedOption();
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
        PricedParameter pricedParameter = new PricedParameter();
        PricedOption copy = pricedOption.copy(pricedParameter);
        Assert.assertTrue(pricedOption.getKey() != copy.getKey());
        Assert.assertEquals(pricedParameter, copy.getPricedParameter());
        Assert.assertEquals(pricedOption.getParameterOptionKey(),
                copy.getParameterOptionKey());
        Assert.assertEquals(pricedOption.getPricePerSubscription(),
                copy.getPricePerSubscription());
        Assert.assertEquals(pricedOption.getPricePerUser(),
                copy.getPricePerUser());
    }

    @Test
    public void testHistory() throws Exception {
        List<DomainHistoryObject<?>> history = runTX(new Callable<List<DomainHistoryObject<?>>>() {

            @Override
            public List<DomainHistoryObject<?>> call() throws Exception {
                return mgr.findHistory(pricedOption);
            }
        });
        for (DomainHistoryObject<?> dho : history) {
            Assert.assertTrue(dho instanceof PricedOptionHistory);
            PricedOptionHistory hist = (PricedOptionHistory) dho;
            Assert.assertEquals(pricedOption.getKey(), hist.getObjKey());
            Assert.assertEquals(pricedOption.getParameterOptionKey(),
                    hist.getParameterOptionObjKey());
            Assert.assertEquals(pricedOption.getPricePerSubscription(),
                    hist.getPricePerSubscription());
            Assert.assertEquals(pricedOption.getPricePerUser(),
                    hist.getPricePerUser());
        }
    }

}
