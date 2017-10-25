/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.applicationservice.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.provisioning.data.ServiceParameter;

public class ParameterFilter {

    /**
     * Converts the parameter list of the subscription related product and the
     * non-configurable parameter definitions of the related technical product
     * into a ServiceParameter list.
     * 
     * @param subscription
     *            The ParameterSet containing subscription, whose parameters
     *            have to be converted.
     * @return the created ServiceParameter list.
     */
    public static List<ServiceParameter> getServiceParameterList(
            Subscription subscription) {
        if (subscription == null) {
            return Collections.emptyList();
        }

        return getServiceParameterList(subscription.getParameterSet(),
                subscription.getProduct().getTechnicalProduct(), false);
    }

    /**
     * Converts the parameter list of the subscription related product and the
     * non-configurable parameter definitions of the related technical product
     * into a ServiceParameter list.
     * 
     * @param parameterSet
     *            The ParameterSet of parameters which have to be converted.
     * @param technicalProduct
     *            The related technical product the
     * @param filterOnetimeParameter
     *            To filter out the product parameters with ONE_TIME
     *            modificationType
     * 
     * @return the created ServiceParameter list.
     */
    private static List<ServiceParameter> getServiceParameterList(
            ParameterSet parameterSet, TechnicalProduct technicalProduct,
            boolean filterOnetimeParameter) {
        List<ServiceParameter> list = new ArrayList<>();

        // first add the product parameters
        if (parameterSet != null && parameterSet.getParameters() != null) {
            for (Parameter parameter : parameterSet.getParameters()) {
                if (!(filterOnetimeParameter
                        && ParameterModificationType.ONE_TIME == parameter
                                .getParameterDefinition()
                                .getModificationType())) {
                    ServiceParameter param = new ServiceParameter();
                    param.setParameterId(parameter.getParameterDefinition()
                            .getParameterId());
                    param.setValue(parameter.getValue());
                    param.setEncrypted(parameter.getParameterDefinition()
                            .getValueType() == ParameterValueType.PWD);
                    list.add(param);
                }
            }
        }
        // add technical product related, non-configurable parameter
        // definitions
        List<ParameterDefinition> parameterDefinitions = technicalProduct
                .getParameterDefinitions();
        for (ParameterDefinition paramDef : parameterDefinitions) {
            if (!paramDef.isConfigurable()) {
                ServiceParameter param = new ServiceParameter();
                param.setParameterId(paramDef.getParameterId());
                param.setValue(paramDef.getDefaultValue());
                param.setEncrypted(
                        paramDef.getValueType() == ParameterValueType.PWD);
                list.add(param);
            }
        }
        return list;
    }

    /**
     * Converts the parameter list of the subscription related product and the
     * non-configurable parameter definitions of the related technical product
     * into a ServiceParameter list.
     * 
     * @param subscription
     *            The containing subscription, whose parameters have to be
     *            converted.
     * @param filterOnetimeParameter
     *            To filter out the product parameters with ONE_TIME
     *            modificationType
     * 
     * @return the created ServiceParameter list.
     */
    public static List<ServiceParameter> getServiceParameterList(
            Subscription subscription, boolean filterOnetimeParameter) {
        if (subscription == null) {
            return Collections.emptyList();
        }
        return getServiceParameterList(subscription.getParameterSet(),
                subscription.getProduct().getTechnicalProduct(),
                filterOnetimeParameter);
    }

    /**
     * Converts the parameter list of the given product and the non-configurable
     * parameter definitions of the related technical product into a
     * ServiceParameter list.
     * 
     * @param product
     *            The product which's parameters have to be converted.
     * @param filterOnetimeParameter
     *            To filter out the product parameters with ONE_TIME
     *            modificationType
     * 
     * @return the created ServiceParameter list.
     */
    public static List<ServiceParameter> getServiceParameterList(
            Product product, boolean filterOnetimeParameter) {
        if (product == null) {
            return Collections.emptyList();
        }
        return getServiceParameterList(product.getParameterSet(),
                product.getTechnicalProduct(), filterOnetimeParameter);
    }
}
