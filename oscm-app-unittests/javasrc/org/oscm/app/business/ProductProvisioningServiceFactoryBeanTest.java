/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 29.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business;

import java.net.ConnectException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.app.business.ProductProvisioningServiceFactoryBean;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.provisioning.intf.ProvisioningService;

/**
 * @author Dirk Bernsau
 * 
 */
public class ProductProvisioningServiceFactoryBeanTest {

    private ProductProvisioningServiceFactoryBean factory;
    private InstanceParameter PUBLIC_IP;
    private InstanceParameter WSDL;
    private InstanceParameter PROTOCOL;
    private InstanceParameter PORT;
    private InstanceParameter USER;
    private InstanceParameter USER_PWD;

    @Before
    public void setup() throws Exception {

        factory = new ProductProvisioningServiceFactoryBean();

        PUBLIC_IP = new InstanceParameter();
        PUBLIC_IP.setParameterKey(InstanceParameter.PUBLIC_IP);
        PUBLIC_IP.setParameterValue("127.0.0.1");
        WSDL = new InstanceParameter();
        WSDL.setParameterKey(InstanceParameter.SERVICE_RELATIVE_PROVSERV_WSDL);
        WSDL.setParameterValue("wsdl");
        PROTOCOL = new InstanceParameter();
        PROTOCOL.setParameterKey(InstanceParameter.SERVICE_RELATIVE_PROVSERV_PROTOCOL);
        PROTOCOL.setParameterValue("http");
        PORT = new InstanceParameter();
        PORT.setParameterKey(InstanceParameter.SERVICE_RELATIVE_PROVSERV_PORT);
        PORT.setParameterValue("1234");
        USER = new InstanceParameter();
        USER.setParameterKey(InstanceParameter.SERVICE_USER);
        USER.setParameterValue("mustermann");
        USER_PWD = new InstanceParameter();
        USER_PWD.setParameterKey(InstanceParameter.SERVICE_USER_PWD);
        USER_PWD.setParameterValue("secret");
    }

    @Test(expected = BadResultException.class)
    public void test_null() throws Exception {
        getInstance(null);
    }

    @Test(expected = BadResultException.class)
    public void test_noIp() throws Exception {
        ServiceInstance instance = new ServiceInstance();
        instance.setInstanceParameters(new ArrayList<InstanceParameter>());
        getInstance(instance);
    }

    @Test(expected = BadResultException.class)
    public void test_noRelativeWSDL() throws Exception {
        ServiceInstance instance = new ServiceInstance();
        ArrayList<InstanceParameter> params = new ArrayList<InstanceParameter>();
        params.add(PUBLIC_IP);
        instance.setInstanceParameters(params);
        getInstance(instance);
    }

    @Test(expected = BadResultException.class)
    public void test_noProtocol() throws Exception {
        ServiceInstance instance = new ServiceInstance();
        ArrayList<InstanceParameter> params = new ArrayList<InstanceParameter>();
        params.add(PUBLIC_IP);
        params.add(WSDL);
        instance.setInstanceParameters(params);
        getInstance(instance);
    }

    @Test(expected = BadResultException.class)
    public void test_noPort() throws Exception {
        ServiceInstance instance = new ServiceInstance();
        ArrayList<InstanceParameter> params = new ArrayList<InstanceParameter>();
        params.add(PUBLIC_IP);
        params.add(WSDL);
        params.add(PROTOCOL);
        instance.setInstanceParameters(params);
        getInstance(instance);
    }

    @Ignore
    @Test(expected = ConnectException.class)
    public void test_NoUser() throws Throwable {
        ServiceInstance instance = new ServiceInstance();
        ArrayList<InstanceParameter> params = new ArrayList<InstanceParameter>();
        params.add(PUBLIC_IP);
        params.add(WSDL);
        params.add(PROTOCOL);
        params.add(PORT);
        instance.setInstanceParameters(params);
        try {
            getInstance(instance);
        } catch (BadResultException e) {
            throw e.getCause();
        }
    }

    @Ignore
    @Test(expected = ConnectException.class)
    public void test_NoPassword() throws Throwable {
        ServiceInstance instance = new ServiceInstance();
        ArrayList<InstanceParameter> params = new ArrayList<InstanceParameter>();
        params.add(PUBLIC_IP);
        params.add(WSDL);
        params.add(PROTOCOL);
        params.add(PORT);
        params.add(USER);
        instance.setInstanceParameters(params);
        try {
            getInstance(instance);
        } catch (BadResultException e) {
            throw e.getCause();
        }
    }

    @Ignore
    @Test(expected = ConnectException.class)
    public void test() throws Throwable {
        ServiceInstance instance = new ServiceInstance();
        ArrayList<InstanceParameter> params = new ArrayList<InstanceParameter>();
        params.add(PUBLIC_IP);
        params.add(WSDL);
        params.add(PROTOCOL);
        params.add(PORT);
        params.add(USER);
        params.add(USER_PWD);
        instance.setInstanceParameters(params);
        try {
            getInstance(instance);
        } catch (BadResultException e) {
            throw e.getCause();
        }
    }

    private ProvisioningService getInstance(final ServiceInstance instance)
            throws Exception {
        return factory.getInstance(instance);
    }
}
