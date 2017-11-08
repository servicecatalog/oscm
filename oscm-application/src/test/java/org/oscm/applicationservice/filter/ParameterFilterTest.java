/* 
 *  Copyright FUJITSU LIMITED 2017
 **
 * 
 */
package org.oscm.applicationservice.filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.provisioning.data.ServiceParameter;

/**
 * @author Mao
 * 
 */
public class ParameterFilterTest {
    private Subscription subscription;
    private Product product;

    private ParameterSet parameterSet;

    private LinkedList<Parameter> parameters;

    private LinkedList<ParameterDefinition> parameterDefinitions;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        // non-configurable parameter
        parameterDefinitions = new LinkedList<ParameterDefinition>();

        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setParameterDefinitions(parameterDefinitions);

        product = new Product();
        product.setTechnicalProduct(technicalProduct);

        subscription = spy(new Subscription());
        subscription.setProduct(product);

        // init ParameterSet
        parameterSet = new ParameterSet();
        parameters = new LinkedList<Parameter>();
        parameters.add(createParameter(
                1L,
                true,
                "param value one_time",
                createParameterDefinition(1001L,
                        ParameterModificationType.ONE_TIME,
                        "one_time parameterId", true)));

        parameters.add(createParameter(
                2L,
                true,
                "param value standard",
                createParameterDefinition(1002L,
                        ParameterModificationType.STANDARD,
                        "standard parameterId", true)));

        parameterSet.setParameters(parameters);

        product.setParameterSet(parameterSet);

        // init technical product related, non-configurable parameter
        // definitions

        parameterDefinitions.add(createParameterDefinition(1003L,
                ParameterModificationType.STANDARD, "irrelative param", true));

    }

    // the product parameters
    private Parameter createParameter(final long key, final boolean isValueSet,
            final String value, final ParameterDefinition def) {
        // Parameter parameter = mock(Parameter.class);
        // doReturn(new Long(key)).when(parameter).getKey();
        // doReturn(new Boolean(isValueSet)).when(parameter).isValueSet();
        // doReturn(value).when(parameter).getValue();
        // doReturn(def).when(parameter).getParameterDefinition();

        Parameter parameter = new Parameter() {

            private static final long serialVersionUID = 4735247005192370874L;

            @Override
            public long getKey() {
                return key;
            }

            @Override
            public ParameterDefinition getParameterDefinition() {
                return def;
            }

            @Override
            public String getValue() {
                return value;
            }

            @Override
            public boolean isValueSet() {
                return isValueSet;
            }

        };
        return parameter;
    }

    private ParameterDefinition createParameterDefinition(final long key,
            final ParameterModificationType modificationType,
            final String parameterId, final boolean isConfigurable) {
        ParameterDefinition parameterDefinition = new ParameterDefinition() {
            private static final long serialVersionUID = 1285790149289090806L;

            @Override
            public long getKey() {
                return key;
            }

            @Override
            public ParameterModificationType getModificationType() {
                return modificationType;
            }

            @Override
            public String getParameterId() {
                return parameterId;
            }

            @Override
            public boolean isConfigurable() {
                return isConfigurable;
            }
        };
        return parameterDefinition;
    }

    @Test
    public void getServiceParameterList_nullSubscription() {
        Subscription subscription = null;
        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(subscription, true);
        assertEquals(0, parameterList.size());
    }

    @Test
    public void getServiceParameterList_subscriptionParameter_filterOneTimeParameter() {
        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(subscription, true);
        assertEquals(1, parameterList.size());
    }

    @Test
    public void getServiceParameterList_subscriptionParameter_allpass() {
        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(subscription, false);
        assertEquals(2, parameterList.size());
    }

    @Test
    public void getServiceParameterList_subscriptionParameter_defaultAllpass() {
        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(subscription);
        assertEquals(2, parameterList.size());
    }

    @Test
    public void getServiceParameterList_subscriptionParameter_valueNotSet() {
        parameters.add(createParameter(
                1L,
                false,
                "param value one_time (not set)",
                createParameterDefinition(1001L,
                        ParameterModificationType.ONE_TIME,
                        "one_time parameterId", true)));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(subscription, true);
        assertEquals(1, parameterList.size());
    }

    @Test
    public void getServiceParameterList_productParameter_valueNotSet_noFilter() {
        parameters.add(createParameter(
                1L,
                false,
                "param value one_time (not set)",
                createParameterDefinition(1001L,
                        ParameterModificationType.ONE_TIME,
                        "one_time parameterId", true)));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(product, false);
        assertEquals(3, parameterList.size());
    }
    
    @Test
    public void getServiceParameterList_productParameter_valueNotSet() {
        parameters.add(createParameter(
                1L,
                false,
                "param value one_time (not set)",
                createParameterDefinition(1001L,
                        ParameterModificationType.ONE_TIME,
                        "one_time parameterId", true)));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(product, true);
        assertEquals(1, parameterList.size());
    }

    @Test
    public void getServiceParameterList_subscriptionParameter_valueNotSet_noFilter() {
        parameters.add(createParameter(
                1L,
                false,
                "param value one_time (not set)",
                createParameterDefinition(1001L,
                        ParameterModificationType.ONE_TIME,
                        "one_time parameterId", true)));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(product, false);
        assertEquals(3, parameterList.size());
    }
    
    @Test
    public void getServiceParameterList_subscriptionParameter_valueNotSet_Standard() {
        parameters.add(createParameter(
                1L,
                false,
                "param value one_time (not set)",
                createParameterDefinition(1001L,
                        ParameterModificationType.STANDARD,
                        "one_time parameterId", true)));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(subscription, true);
        assertEquals(2, parameterList.size());
    }


    
    @Test
    public void getServiceParameterList_subscriptionParameter_valueNotSet_Standard_noFilter() {
        parameters.add(createParameter(
                1L,
                false,
                "param value one_time (not set)",
                createParameterDefinition(1001L,
                        ParameterModificationType.STANDARD,
                        "one_time parameterId", true)));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(subscription, false);
        assertEquals(3, parameterList.size());
    }

    @Test
    public void getServiceParameterList_nonconfigurableParameter_configurable() {
        parameterDefinitions.add(createParameterDefinition(1003L,
                ParameterModificationType.STANDARD, "irrelative param", true));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(subscription, false);
        assertEquals(2, parameterList.size());
    }

    @Test
    public void getServiceParameterList_nonconfigurableParameter_unconfigurable() {
        parameterDefinitions.add(createParameterDefinition(1003L,
                ParameterModificationType.STANDARD, "irrelative param", false));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(subscription, false);
        assertEquals(3, parameterList.size());
    }

    @Test
    public void getServiceParameterList_nullProduct() {
        Product product = null;
        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(product, true);
        assertEquals(0, parameterList.size());
    }

    @Test
    public void getServiceParameterList_productParameter_filterOneTimeParameter_Product() {
        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(product, true);
        assertEquals(1, parameterList.size());
    }

    @Test
    public void getServiceParameterList_productParameter_allpass_Product() {
        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(product, false);
        assertEquals(2, parameterList.size());
    }

    @Test
    public void getServiceParameterList_productParameter_valueNotSet_Product() {
        parameters.add(createParameter(
                1L,
                false,
                "param value one_time (not set)",
                createParameterDefinition(1001L,
                        ParameterModificationType.ONE_TIME,
                        "one_time parameterId", true)));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(product, true);
        assertEquals(1, parameterList.size());
    }

    @Test
    public void getServiceParameterList_nonconfigurableParameter_configurable_Product() {
        parameterDefinitions.add(createParameterDefinition(1003L,
                ParameterModificationType.STANDARD, "irrelative param", true));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(product, false);
        assertEquals(2, parameterList.size());
    }

    @Test
    public void getServiceParameterList_nonconfigurableParameter_unconfigurable_Product() {
        parameterDefinitions.add(createParameterDefinition(1003L,
                ParameterModificationType.STANDARD, "irrelative param", false));

        List<ServiceParameter> parameterList = ParameterFilter
                .getServiceParameterList(product, false);
        assertEquals(3, parameterList.size());
    }

}
