/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.billingadapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.oscm.domobjects.BillingAdapter;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.string.Strings;
import org.oscm.billing.application.bean.PluginServiceFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * @author stavreva
 * 
 */
public class BillingAdapterAssembler {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(BillingAdapterAssembler.class);

    public static List<POBaseBillingAdapter> toPOBaseBillingAdapter(
            List<BillingAdapter> adapters) {
        List<POBaseBillingAdapter> result = new ArrayList<POBaseBillingAdapter>();
        for (BillingAdapter adapter : adapters) {
            result.add(toPOBaseBillingAdapter(adapter));
        }
        return result;
    }

    public static POBaseBillingAdapter toPOBaseBillingAdapter(
            BillingAdapter adapter) {
        POBaseBillingAdapter result = new POBaseBillingAdapter();
        result.setKey(adapter.getKey());
        result.setBillingIdentifier(adapter.getBillingIdentifier());
        result.setName(adapter.getName());
        result.setVersion(adapter.getVersion());
        return result;
    }

    public static POBillingAdapter toPOBillingAdapter(BillingAdapter adapter) {
        POBillingAdapter result = new POBillingAdapter();
        if(adapter==null){
            return result;
        }
        result.setKey(adapter.getKey());
        result.setBillingIdentifier(adapter.getBillingIdentifier());
        result.setName(adapter.getName());
        result.setDefaultAdapter(adapter.isDefaultAdapter());
        result.setVersion(adapter.getVersion());
        if (adapter.getBillingIdentifier().equals(
                BillingAdapterIdentifier.NATIVE_BILLING.name())) {
            result.setNativeBilling(true);
        } else {
            result.setNativeBilling(false);
        }
        result.setActive(adapter.isActive());

        Properties properties = convertXMLToProperties(adapter
                .getConnectionProperties());

        Set<ConnectionPropertyItem> connectionPropertyItems = new TreeSet<>();

        if (properties != null) {
            for (final String key : properties.stringPropertyNames()) {
                ConnectionPropertyItem propertyItem = new ConnectionPropertyItem(
                        key, properties.getProperty(key));

                connectionPropertyItems.add(propertyItem);
            }
        }

        else {
            if (!result.isNativeBilling()) {
                ConnectionPropertyItem jndiName = new ConnectionPropertyItem(
                        PluginServiceFactory.JNDI_NAME, null);
                connectionPropertyItems.add(jndiName);
            }
        }
        result.setConnectionProperties(connectionPropertyItems);

        return result;
    }

    public static List<POBillingAdapter> toPOBillingAdapter(
            List<BillingAdapter> adapters) {
        List<POBillingAdapter> result = new ArrayList<>();
        for (BillingAdapter adapter : adapters) {
            result.add(toPOBillingAdapter(adapter));
        }
        return result;
    }

    public static BillingAdapter toBillingAdapter(
            POBillingAdapter poBillingAdapter) throws IOException {
        BillingAdapter billingAdapter = new BillingAdapter();
        billingAdapter.setKey(poBillingAdapter.getKey());
        billingAdapter.setBillingIdentifier(poBillingAdapter
                .getBillingIdentifier());
        billingAdapter.setName(poBillingAdapter.getName());
        billingAdapter.setDefaultAdapter(poBillingAdapter.isDefaultAdapter());

        Set<ConnectionPropertyItem> propertyItems = poBillingAdapter
                .getConnectionProperties();

        if (propertyItems != null) {
            Properties properties = new Properties();

            for (ConnectionPropertyItem propertyItem : propertyItems) {
                if (!Strings.isEmpty(propertyItem.getKey())) {
                    properties.setProperty(propertyItem.getKey(),
                            propertyItem.getValue());
                }
            }

            billingAdapter
                    .setConnectionProperties(convertPropertiesToXML(properties));

        }
        return billingAdapter;
    }

    public static List<BillingAdapter> toBillingAdapters(
            List<POBillingAdapter> poBillingAdapters) throws IOException {
        List<BillingAdapter> result = new ArrayList<BillingAdapter>();
        for (POBillingAdapter poBillingAdapter : poBillingAdapters) {
            result.add(toBillingAdapter(poBillingAdapter));
        }
        return result;
    }

    static String convertPropertiesToXML(Properties properties)
            throws IOException {
        if (properties != null) {
            String xmlString;
            try (OutputStream out = new ByteArrayOutputStream()) {
                properties.storeToXML(out, null, "UTF-8");
                xmlString = out.toString();
            }
            return xmlString;
        }
        return null;
    }

    static Properties convertXMLToProperties(String xmlString) {
        if (xmlString != null) {
            Properties properties = new Properties();
            try (InputStream in = new ByteArrayInputStream(xmlString.getBytes())) {
                properties.loadFromXML(in);
            } catch (IOException e) {
                LOGGER.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR);
            }
            return properties;
        }
        return null;
    }
}
