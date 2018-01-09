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

import org.junit.Assert;
import org.junit.Test;

import org.oscm.app.business.InstanceFilter;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.provisioning.data.ServiceParameter;

public class InstanceParameterFilterTest {

    private List<org.oscm.provisioning.data.ServiceParameter> serviceParamInput = new ArrayList<org.oscm.provisioning.data.ServiceParameter>();
    private ServiceInstance instance = new ServiceInstance();

    @Test
    public void testFilterInstanceParametersForServiceEmptyInput()
            throws Exception {
        new InstanceFilter(); // coverage
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(serviceParamInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testFilterInstanceParametersForServiceSingleInput()
            throws Exception {
        addServiceParameter("param", "value");
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(serviceParamInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        ServiceParameter entry = result.get(0);
        Assert.assertEquals("param", entry.getParameterId());
        Assert.assertEquals("value", entry.getValue());
    }

    @Test
    public void testFilterInstanceParametersForServiceSingleFilterInput()
            throws Exception {
        addServiceParameter("APP_param", "value");
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(serviceParamInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testFilterInstanceParametersForServiceMultipleInput()
            throws Exception {
        addServiceParameter("param1", "value1");
        addServiceParameter("param2", "value2");
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(serviceParamInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        ServiceParameter entry = result.get(0);
        Assert.assertEquals("param1", entry.getParameterId());
        Assert.assertEquals("value1", entry.getValue());
        entry = result.get(1);
        Assert.assertEquals("param2", entry.getParameterId());
        Assert.assertEquals("value2", entry.getValue());
    }

    @Test
    public void testFilterInstanceParametersForServiceMultipleInputFilterAll()
            throws Exception {
        addServiceParameter("APP_param1", "value1");
        addServiceParameter("APP_param2", "value2");
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(serviceParamInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testFilterInstanceParametersForServiceMultipleInputFilterAllFilterSome()
            throws Exception {
        addServiceParameter("APP_param1", "value1");
        addServiceParameter("param2", "value2");
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(serviceParamInput);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        ServiceParameter entry = result.get(0);
        Assert.assertEquals("param2", entry.getParameterId());
        Assert.assertEquals("value2", entry.getValue());
    }

    @Test
    public void testFilterInstanceParametersForServiceInstanceEmptyInput()
            throws Exception {
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(instance);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testFilterInstanceParametersForServiceInstanceSingleInput()
            throws Exception {
        addInstanceParameterToService("param1", "value1");
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(instance);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        ServiceParameter entry = result.get(0);
        Assert.assertEquals("param1", entry.getParameterId());
        Assert.assertEquals("value1", entry.getValue());
    }

    @Test
    public void testFilterInstanceParametersForServiceInstanceSingleFilterInput()
            throws Exception {
        addInstanceParameterToService("APP_param1", "value1");
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(instance);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testFilterInstanceParametersForServiceInstanceMultipleInput()
            throws Exception {
        addInstanceParameterToService("param1", "value1");
        addInstanceParameterToService("param2", "value2");
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(instance);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        ServiceParameter entry = result.get(0);
        Assert.assertEquals("param1", entry.getParameterId());
        Assert.assertEquals("value1", entry.getValue());
        entry = result.get(1);
        Assert.assertEquals("param2", entry.getParameterId());
        Assert.assertEquals("value2", entry.getValue());
    }

    @Test
    public void testFilterInstanceParametersForServiceInstanceMultipleInputFilterAll()
            throws Exception {
        addInstanceParameterToService("APP_param1", "value1");
        addInstanceParameterToService("APP_param2", "value2");
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(instance);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testFilterInstanceParametersForServiceInstanceMultipleInputFilterAllFilterSome()
            throws Exception {
        addInstanceParameterToService("APP_param1", "value1");
        addInstanceParameterToService("param2", "value2");
        List<ServiceParameter> result = InstanceFilter
                .getFilteredInstanceParametersForService(instance);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        ServiceParameter entry = result.get(0);
        Assert.assertEquals("param2", entry.getParameterId());
        Assert.assertEquals("value2", entry.getValue());
    }

    /**
     * Adds a new service parameter.
     * 
     * @param paramKey
     *            The key of the parameter.
     * @param paramValue
     *            The value of the parameter.
     */
    private void addServiceParameter(String paramKey, String paramValue) {
        org.oscm.provisioning.data.ServiceParameter param = new org.oscm.provisioning.data.ServiceParameter();
        param.setParameterId(paramKey);
        param.setValue(paramValue);
        serviceParamInput.add(param);
    }

    /**
     * Creates an instance parameter and adds it to the service instance.
     * 
     * @param paramKey
     *            The key of the parameter to be added.
     * @param paramValue
     *            The value of the parameter to be added.
     */
    private void addInstanceParameterToService(String paramKey,
            String paramValue) {
        InstanceParameter param = new InstanceParameter();
        param.setParameterKey(paramKey);
        param.setParameterValue(paramValue);
        param.setServiceInstance(instance);
        instance.getInstanceParameters().add(param);
    }
}
