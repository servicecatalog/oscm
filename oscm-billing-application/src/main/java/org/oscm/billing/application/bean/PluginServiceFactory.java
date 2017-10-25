/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.commons.io.IOUtils;

import org.oscm.domobjects.BillingAdapter;
import org.oscm.internal.types.exception.BillingAdapterConnectionException;
import org.oscm.internal.types.exception.BillingApplicationException;

/**
 * The plugin service factory class is responsible for locating the billing
 * plugin service for a specified billing adapter, using JNDI lookup.
 * 
 * @author baumann
 * 
 */
public class PluginServiceFactory {
    public final static String JNDI_NAME = "JNDI_NAME";

    public static <T> T getPluginService(Class<T> serviceInterface,
            BillingAdapter billingAdapter) throws BillingApplicationException {
        try {
            Properties connectionProperties = getConnectionProperties(
                    billingAdapter);
            String jndiName = getAdapterJndiName(connectionProperties);

            InitialContext context = new InitialContext(
                    createJndiProperties(connectionProperties));

            String simpleName = serviceInterface.getSimpleName();
            Object lookup = context.lookup(jndiName + "ejb/"
                    + simpleName.substring(0, simpleName.indexOf("Service"))
                    + "!" + serviceInterface.getName());

            return serviceInterface.cast(lookup);
        } catch (Exception e) {
            throw new BillingApplicationException(
                    "No Connection to Billing Adapter",
                    new BillingAdapterConnectionException(
                            "JNDI-Lookup to Billing Adapter failed", e));
        }
    }

    protected static Properties getConnectionProperties(
            BillingAdapter billingAdapter) throws BillingApplicationException {
        Properties connectionProperties = new Properties();
        if (billingAdapter.getConnectionProperties() != null) {
            try (InputStream in = IOUtils.toInputStream(
                    billingAdapter.getConnectionProperties(),
                    StandardCharsets.UTF_8.toString());) {
                connectionProperties.loadFromXML(in);
            } catch (IOException e) {
                throw new BillingApplicationException(
                        "No Connection to Billing Adapter",
                        new BillingAdapterConnectionException(
                                "Unable to load Connection properties", e));
            }
        } else {
            throw new BillingApplicationException(
                    "No Connection to Billing Adapter",
                    new BillingAdapterConnectionException(
                            "Connection properties are missing"));
        }
        return connectionProperties;

    }

    protected static Properties createJndiProperties(
            Properties connectionProperties) {
        connectionProperties.remove(PluginServiceFactory.JNDI_NAME);
        return connectionProperties;
    }

    protected static String getAdapterJndiName(Properties connectionProperties)
            throws BillingApplicationException {
        String adapterJndiName = (String) connectionProperties
                .get(PluginServiceFactory.JNDI_NAME);
        if (adapterJndiName == null) {
            throw new BillingApplicationException(
                    "No Connection to Billing Adapter",
                    new BillingAdapterConnectionException(
                            "JndiName is missing"));
        }
        return adapterJndiName;
    }
}
