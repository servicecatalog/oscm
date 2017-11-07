/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.naming.Context;

import org.junit.Test;

import org.oscm.domobjects.BillingAdapter;
import org.oscm.internal.types.exception.BillingAdapterConnectionException;
import org.oscm.internal.types.exception.BillingApplicationException;

/**
 * @author iversen
 * 
 */
public class PluginServiceFactoryTest {

    private String jndiName = "java:global/oscm-file-adapter/oscm-file-adapter/FileBillingAdapter";
    protected final static String ORBINITIALHOST = "org.omg.CORBA.ORBInitialHost";
    protected final static String ORBINITIALPORT = "org.omg.CORBA.ORBInitialPort";

    @Test
    public void getConnectionProperties() throws Exception {
        // given
        BillingAdapter ba = spy(new BillingAdapter());
        Properties props = createConnectionProperties("FileAdapter");
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();) {
            props.storeToXML(stream, null, StandardCharsets.UTF_8.toString());
            ba.setConnectionProperties(stream.toString());
        }

        // when
        Properties p = PluginServiceFactory.getConnectionProperties(ba);

        // then
        assertEquals(createConnectionProperties("FileAdapter"), p);
    }

    @Test
    public void getConnectionPropertiesMissingProperties() {
        // given
        BillingAdapter ba = spy(new BillingAdapter());
        ba.setConnectionProperties(null);

        // when
        try {
            PluginServiceFactory.getConnectionProperties(ba);
            fail();
        }
        // then
        catch (BillingApplicationException e) {
            assertTrue(e.getCause() instanceof BillingAdapterConnectionException);
            assertTrue(e.getCause().getMessage()
                    .endsWith("Connection properties are missing"));
        }

    }

    @Test
    public void getConnectionPropertiesCorruptedProperties() {
        // given
        BillingAdapter ba = spy(new BillingAdapter());
        String corruptedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><entry key=\"JNDI_NAME";
        ba.setConnectionProperties(corruptedXml);
        // when
        try {
            PluginServiceFactory.getConnectionProperties(ba);
            fail();
        }
        // then
        catch (BillingApplicationException e) {
            assertTrue(e.getCause() instanceof BillingAdapterConnectionException);
            assertTrue(e.getCause().getMessage()
                    .endsWith("Unable to load Connection properties"));
        }

    }

    @Test
    public void getAdapterJndiName() throws BillingApplicationException {
        // given
        Properties props = createConnectionProperties(jndiName);

        // when
        String result = PluginServiceFactory.getAdapterJndiName(props);

        // then
        assertEquals(jndiName, result);
    }

    @Test
    public void getAdapterJndiNameMissingJndiName() {
        // given
        Properties props = createConnectionProperties(null);

        // when
        try {
            PluginServiceFactory.getAdapterJndiName(props);
            fail();
        }
        // then
        catch (BillingApplicationException e) {
            assertTrue(e.getCause() instanceof BillingAdapterConnectionException);
            assertTrue(e.getCause().getMessage()
                    .endsWith("JndiName is missing"));
        }
    }

    @Test
    public void createJndiProperties() {
        // given
        Properties props = createConnectionProperties(jndiName);

        // when
        Properties jndiProps = PluginServiceFactory.createJndiProperties(props);

        // then
        assertEquals(props.get(Context.PROVIDER_URL),
                jndiProps.get(Context.PROVIDER_URL));
        assertEquals(props.get(Context.INITIAL_CONTEXT_FACTORY),
                jndiProps.get(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals(props.get(ORBINITIALHOST), jndiProps.get(ORBINITIALHOST));
        assertEquals(props.get(ORBINITIALPORT), jndiProps.get(ORBINITIALPORT));
        assertEquals(4, jndiProps.size());
        assertNull(props.get(PluginServiceFactory.JNDI_NAME));

    }

    private Properties createConnectionProperties(String jndiName) {
        Properties testProperties = new Properties();
        testProperties.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.enterprise.naming.SerialInitContextFactory");
        testProperties.put(ORBINITIALHOST, "testHost");
        testProperties.put(ORBINITIALPORT, "1354");
        testProperties.put(Context.PROVIDER_URL, "http://test:1354");
        if (jndiName != null) {
            testProperties.put(PluginServiceFactory.JNDI_NAME, jndiName);
        }
        return testProperties;
    }

}
