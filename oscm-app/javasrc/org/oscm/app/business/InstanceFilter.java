/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 19.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business;

import java.util.ArrayList;
import java.util.List;

import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.domain.InstanceAttribute;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;

/**
 * Class to filter the parameters that are only meant for proxy internal use and
 * thus should not be exposed to other services.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class InstanceFilter {

    /**
     * Parses all parameters of the service instance and returns those that do
     * not start with the prefix {@link InstanceParameter#APP_PARAM_KEY_PREFIX}
     * .
     * 
     * @param serviceInstance
     *            The service instance containing the parameters.
     * @return The list of parameters to be passed to the technical service.
     * @throws BadResultException
     *             thrown when parameter values can not be decrypted
     */
    public static List<ServiceParameter> getFilteredInstanceParametersForService(
            ServiceInstance serviceInstance) throws BadResultException {
        List<InstanceParameter> parameters = serviceInstance
                .getInstanceParameters();
        List<ServiceParameter> result = new ArrayList<>();
        for (InstanceParameter param : parameters) {
            String parameterKey = param.getParameterKey();
            if (includeParameter(parameterKey)) {
                ServiceParameter returnParam = new ServiceParameter();
                returnParam.setParameterId(parameterKey);
                returnParam.setValue(param.getDecryptedValue());
                result.add(returnParam);
            }
        }
        return result;
    }

    /**
     * Parses all specified parameters and returns those that do not start with
     * the prefix {@link InstanceParameter#APP_PARAM_KEY_PREFIX} .
     * 
     * @param serviceParameter
     *            The parameters to filter the unused from.
     * @return The list of parameters to be passed to the technical service.
     */
    public static List<ServiceParameter> getFilteredInstanceParametersForService(
            List<ServiceParameter> serviceParameter) {
        List<ServiceParameter> result = new ArrayList<>();
        for (ServiceParameter param : serviceParameter) {
            String parameterKey = param.getParameterId();
            if (includeParameter(parameterKey)) {
                result.add(param);
            }
        }
        return result;
    }

    /**
     * Determines whether the given parameter key does not match the exclusion
     * patter.
     * 
     * @param parameterKey
     *            The parameter key to check.
     * @return <code>true</code> in case the parameter should be included,
     *         <code>false</code> otherwise.
     */
    private static boolean includeParameter(String parameterKey) {
        return !parameterKey.startsWith(InstanceParameter.APP_PARAM_KEY_PREFIX);
    }

    public static List<ServiceAttribute> getFilteredInstanceAttributeForService(
            ServiceInstance serviceInstance) throws BadResultException {
        List<InstanceAttribute> attributes = serviceInstance
                .getInstanceAttributes();

        List<ServiceAttribute> result = new ArrayList<>();
        for (InstanceAttribute attr : attributes) {
            ServiceAttribute sa = new ServiceAttribute();
            sa.setAttributeId(attr.getAttributeKey());
            sa.setValue(attr.getDecryptedValue());
            result.add(sa);
        }

        return result;
    }
}
