/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizedDomainObject;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class LocalizerServiceIT extends EJBTestBase {

    private LocalizerServiceLocal localizer;
    private DataService mgr;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceBean());

        localizer = container.get(LocalizerServiceLocal.class);
        mgr = container.get(DataService.class);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(mgr);
                return null;
            }
        });
    }

    @Test
    public void testGetLocalizedTextFromDatabaseNoHitInDBWithCountryAndVariant()
            throws Exception {
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de_DE_xy", 1L,
                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC);
            }
        });
        assertEquals("Wrong return text", "", text);
    }

    @Test
    public void testGetLocalizedTextFromDatabaseNoHitInDBWithCountry()
            throws Exception {
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de_DE_xy", 1L,
                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC);
            }
        });
        assertEquals("Wrong return text", "".toString(), text);
    }

    @Test
    public void testGetLocalizedTextFromDatabaseNoHitInDB() throws Exception {
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de_DE_xy", 1L,
                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC);
            }
        });
        assertEquals("Wrong return text", "", text);
    }

    @Test
    public void testGetLocalizedTextFromDatabaseHit() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(2L);
                resource.setLocale("de");
                resource.setObjectType(
                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC);
                resource.setValue("testValue");
                mgr.persist(resource);
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de", 2L,
                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC);
            }
        });
        assertEquals("Wrong return text", "testValue", text);
    }

    @Test
    public void testGetLocalizedTextFromDatabaseProductRelatedKey()
            throws Exception {
        final Product product = createProduct();
        final Product copy = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product copy = product.copyForCustomer(product.getVendor());
                mgr.persist(copy);
                mgr.flush();
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(copy.getKey());
                resource.setLocale("de");
                resource.setObjectType(
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
                resource.setValue("testValue");
                mgr.persist(resource);

                LocalizedResource resource2 = new LocalizedResource();
                resource2.setLocale("de");
                resource2.setObjectType(
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
                resource2.setObjectKey(product.getKey());
                resource2.setValue("parentValue");
                mgr.persist(resource2);
                return copy;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de",
                        copy.getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
            }
        });
        assertEquals("Wrong return text", "testValue", text);
        text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de",
                        product.getKey(),
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
            }
        });
        assertEquals("Wrong return text", "parentValue", text);
    }

    @Test
    public void testGetLocalizedTextFromDatabasePriceModelRelatedKey()
            throws Exception {
        final Product product = createProduct();
        final Product copy = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product copy = product.copyForCustomer(product.getVendor());
                mgr.persist(copy);
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(product.getPriceModel().getKey());
                resource.setLocale("de");
                resource.setObjectType(
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
                resource.setValue("testValue");
                mgr.persist(resource);
                return copy;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de",
                        copy.getPriceModel().getKey(),
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
            }
        });
        assertEquals("Wrong return text", "testValue", text);
    }

    @Test
    public void testLoadLocalizedPropertiesFromDatabaseNoHit()
            throws Exception {
        Properties result = runTX(new Callable<Properties>() {
            @Override
            public Properties call() throws Exception {
                return localizer.loadLocalizedPropertiesFromDatabase(2L,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, "de");
            }
        });
        assertEquals("Wrong number of props returned", 0, result.size());
    }

    @Test
    public void testLoadLocalizedPropertiesFromDatabaseHit() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(2L);
                resource.setLocale("de");
                resource.setObjectType(
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);
                resource.setValue("testValue=bla");
                mgr.persist(resource);
                return null;
            }
        });
        Properties result = runTX(new Callable<Properties>() {
            @Override
            public Properties call() throws Exception {
                return localizer.loadLocalizedPropertiesFromDatabase(2L,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, "de");
            }
        });
        assertEquals("Wrong number of props returned", 1, result.size());
    }

    @Test
    public void testLoadLocalizedPropertiesFromDatabaseHitTwoProps()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(2L);
                resource.setLocale("de");
                resource.setObjectType(
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);
                resource.setValue("testValue=bla\r\nanotherEntry=present");
                mgr.persist(resource);
                return null;
            }
        });
        Properties result = runTX(new Callable<Properties>() {
            @Override
            public Properties call() throws Exception {
                return localizer.loadLocalizedPropertiesFromDatabase(2L,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, "de");
            }
        });
        assertEquals("Wrong number of props returned", 2, result.size());
        assertEquals("bla", result.getProperty("testValue"));
        assertEquals("present", result.getProperty("anotherEntry"));
    }

    @Test
    public void testStoreLocalizedResource() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere");
                return null;
            }
        });
        // as the persisting works, retrieve the object again
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC);
            }
        });
        assertEquals("Wrong localized text returned", "sphere", text);
    }

    @Test
    public void storeLocalizedResource_PlatformObjects() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("en", 1000L,
                        LocalizedObjectTypes.EVENT_DESC, "event");
                return null;
            }
        });
        List<VOLocalizedText> localizedValues = runTX(
                new Callable<List<VOLocalizedText>>() {
                    @Override
                    public List<VOLocalizedText> call() throws Exception {
                        return localizer.getLocalizedValues(1000L,
                                LocalizedObjectTypes.EVENT_DESC);
                    }
                });
        sort(localizedValues);
        assertEquals("de", localizedValues.get(0).getLocale());
        assertEquals("Anmeldung eines Benutzers bei dem Service.",
                localizedValues.get(0).getText());
        assertEquals("en", localizedValues.get(1).getLocale());
        assertEquals("Wrong localized text returned", "event",
                localizedValues.get(1).getText());
    }

    @Test
    public void testStoreLocalizedResourceUpdate() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere2");
                return null;
            }
        });
        // as the persisting works, retrieve the object again
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC);
            }
        });
        assertEquals("Wrong localized text returned", "sphere2", text);
    }

    @Test(expected = EJBException.class)
    public void testStoreLocalizedResourceWrongResource() {
        localizer.storeLocalizedResource("es", 123L,
                LocalizedObjectTypes.MAIL_CONTENT, "sphere");
    }

    @Test
    public void testRemoveLocalizedValues() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.removeLocalizedValues(123L,
                        LocalizedObjectTypes.EVENT_DESC);
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC);
            }
        });
        assertEquals("Wrong localized text returned", "", text);
    }

    @Test
    public void testRemoveLocalizedValuesMultipleLocalesAffected()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("en", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.removeLocalizedValues(123L,
                        LocalizedObjectTypes.EVENT_DESC);
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("en", 123L,
                        LocalizedObjectTypes.EVENT_DESC);
            }
        });
        assertEquals("Wrong localized text returned", "", text);
    }

    @Test
    public void testRemoveLocalizedValue_ok() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.removeLocalizedValue(123L,
                        LocalizedObjectTypes.EVENT_DESC, "es");
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC);
            }
        });
        assertEquals("Wrong localized text returned", "", text);
    }

    @Test
    public void testRemoveLocalizedValue_multi() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("ja", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.removeLocalizedValue(123L,
                        LocalizedObjectTypes.EVENT_DESC, "es");
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC);
            }
        });
        assertEquals("Wrong localized text returned", "", text);

        text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("ja", 123L,
                        LocalizedObjectTypes.EVENT_DESC);
            }
        });
        assertEquals("Wrong localized text returned", "sphere", text);
    }

    /**
     * Store an english and a german resource. Access with different locales.
     */
    @Test
    public void testLocalizeMultipleObjects() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("en", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "english");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("de", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "deutsch");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                // retrieve text for german locale
                LocalizedDomainObject localizedObject = localizer
                        .getLocalizedTextFromDatabase("de",
                                Collections.singletonList(Long.valueOf(123L)),
                                Collections.singletonList(
                                        LocalizedObjectTypes.EVENT_DESC))
                        .get(0);
                String localicedResource = localizedObject
                        .getLocalizedResources()
                        .get(LocalizedObjectTypes.EVENT_DESC);
                assertEquals("deutsch", localicedResource);

                // retrieve text for english locale
                localizedObject = localizer
                        .getLocalizedTextFromDatabase("en",
                                Collections.singletonList(Long.valueOf(123L)),
                                Collections.singletonList(
                                        LocalizedObjectTypes.EVENT_DESC))
                        .get(0);
                localicedResource = localizedObject.getLocalizedResources()
                        .get(LocalizedObjectTypes.EVENT_DESC);
                assertEquals("english", localicedResource);

                // retrieve text for japanese locale
                localizedObject = localizer
                        .getLocalizedTextFromDatabase("ja",
                                Collections.singletonList(Long.valueOf(123L)),
                                Collections.singletonList(
                                        LocalizedObjectTypes.EVENT_DESC))
                        .get(0);
                localicedResource = localizedObject.getLocalizedResources()
                        .get(LocalizedObjectTypes.EVENT_DESC);
                assertEquals("english", localicedResource);
                return null;
            }
        });
    }

    /**
     * First create a german resource, than create a english resource (this is
     * the opposite of the normal ordering). Access with different locales.
     */
    @Test
    public void testLocalizeMultipleObjects_reverseLocaleOrder()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("de", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "deutsch");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("en", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "english");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                // retrieve text for german locale
                LocalizedDomainObject localizedObject = localizer
                        .getLocalizedTextFromDatabase("de",
                                Collections.singletonList(Long.valueOf(123L)),
                                Collections.singletonList(
                                        LocalizedObjectTypes.EVENT_DESC))
                        .get(0);
                String localicedResource = localizedObject
                        .getLocalizedResources()
                        .get(LocalizedObjectTypes.EVENT_DESC);
                assertEquals("deutsch", localicedResource);

                // retrieve text for english locale
                localizedObject = localizer
                        .getLocalizedTextFromDatabase("en",
                                Collections.singletonList(Long.valueOf(123L)),
                                Collections.singletonList(
                                        LocalizedObjectTypes.EVENT_DESC))
                        .get(0);
                localicedResource = localizedObject.getLocalizedResources()
                        .get(LocalizedObjectTypes.EVENT_DESC);
                assertEquals("english", localicedResource);

                // retrieve text for japanese locale
                localizedObject = localizer
                        .getLocalizedTextFromDatabase("ja",
                                Collections.singletonList(Long.valueOf(123L)),
                                Collections.singletonList(
                                        LocalizedObjectTypes.EVENT_DESC))
                        .get(0);
                localicedResource = localizedObject.getLocalizedResources()
                        .get(LocalizedObjectTypes.EVENT_DESC);
                assertEquals("english", localicedResource);
                return null;
            }
        });
    }

    @Test
    public void testRemoveLocalizedValue_multi_default() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.storeLocalizedResource("en", 123L,
                        LocalizedObjectTypes.EVENT_DESC, "sphere");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.removeLocalizedValue(123L,
                        LocalizedObjectTypes.EVENT_DESC, "es");
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC);
            }
        });
        assertEquals("Wrong localized text returned", "sphere", text);
    }

    @Test
    public void testRemoveLocalizedValue_notExiting() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.removeLocalizedValue(123L,
                        LocalizedObjectTypes.EVENT_DESC, "es");
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("es", 123L,
                        LocalizedObjectTypes.EVENT_DESC);
            }
        });
        assertEquals("Wrong localized text returned", "", text);
    }

    @Test
    public void testSetLocalizedValues() throws Exception {
        final List<VOLocalizedText> values = new ArrayList<>();
        values.add(new VOLocalizedText("en", "house"));
        values.add(new VOLocalizedText("de", "Haus"));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.setLocalizedValues(12L,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, values);
                return null;
            }
        });
        List<VOLocalizedText> localizedValues = runTX(
                new Callable<List<VOLocalizedText>>() {
                    @Override
                    public List<VOLocalizedText> call() throws Exception {
                        return localizer.getLocalizedValues(12L,
                                LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);
                    }
                });
        sort(localizedValues);

        assertEquals("Wrong number of values returned", 2,
                localizedValues.size());

        assertEquals("de", localizedValues.get(0).getLocale());
        assertEquals("Haus", localizedValues.get(0).getText());
        assertEquals("en", localizedValues.get(1).getLocale());
        assertEquals("house", localizedValues.get(1).getText());
    }

    @Test
    public void getLocalizedValues() throws Exception {
        List<VOLocalizedText> localizedValues = runTX(
                new Callable<List<VOLocalizedText>>() {
                    @Override
                    public List<VOLocalizedText> call() throws Exception {
                        return localizer.getLocalizedValues(1000L,
                                LocalizedObjectTypes.EVENT_DESC);
                    }
                });
        sort(localizedValues);

        assertEquals("Wrong number of values returned", 3,
                localizedValues.size());
        assertEquals("de", localizedValues.get(0).getLocale());
        assertEquals("Anmeldung eines Benutzers bei dem Service.",
                localizedValues.get(0).getText());
        assertEquals("en", localizedValues.get(1).getLocale());
        assertEquals("Login of a user to the service.",
                localizedValues.get(1).getText());
    }

    @Test
    public void testUpdateLocalizedValues() throws Exception {
        // CREATE
        final List<VOLocalizedText> values = new ArrayList<>();
        values.add(new VOLocalizedText("en", "house"));
        values.add(new VOLocalizedText("de", "Haus"));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.setLocalizedValues(12L,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, values);
                return null;
            }
        });
        // UPDATE
        final List<VOLocalizedText> values1 = runTX(
                new Callable<List<VOLocalizedText>>() {
                    @Override
                    public List<VOLocalizedText> call() throws Exception {
                        return localizer.getLocalizedValues(12L,
                                LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);
                    }
                });
        sort(values1);
        assertEquals("Wrong number of values returned", 2, values1.size());
        values1.get(0).setText("Wolkenkratzer");
        values1.get(1).setText("skyscraper");

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // VERIFY
                localizer.setLocalizedValues(12L,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, values1);
                return null;
            }
        });
        final List<VOLocalizedText> values2 = runTX(
                new Callable<List<VOLocalizedText>>() {
                    @Override
                    public List<VOLocalizedText> call() throws Exception {
                        return localizer.getLocalizedValues(12L,
                                LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);
                    }
                });
        sort(values2);
        assertEquals("Wrong number of values returned", 2, values2.size());
        assertEquals("de", values2.get(0).getLocale());
        assertEquals("Wolkenkratzer", values2.get(0).getText());
        assertEquals("en", values2.get(1).getLocale());
        assertEquals("skyscraper", values2.get(1).getText());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentSetLocalizedValues() throws Exception {
        final List<VOLocalizedText> values = new ArrayList<>();
        values.add(new VOLocalizedText("en", "house"));
        values.add(new VOLocalizedText("de", "Haus"));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.setLocalizedValues(12L,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, values);
                return null;
            }
        });
        values.get(0).setText("tent");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.setLocalizedValues(12L,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, values);
                return null;
            }
        });
    }

    @Test
    public void testSetLocalizedValuesRemoveOldEntries() throws Exception {
        final List<VOLocalizedText> values_0 = new ArrayList<>();
        values_0.add(new VOLocalizedText("en", "house"));
        values_0.add(new VOLocalizedText("de", "Haus"));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.setLocalizedValues(12L,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, values_0);
                return null;
            }
        });
        final List<VOLocalizedText> values = runTX(
                new Callable<List<VOLocalizedText>>() {
                    @Override
                    public List<VOLocalizedText> call() throws Exception {
                        return localizer.getLocalizedValues(12L,
                                LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);
                    }
                });
        sort(values);
        values.get(0).setText(null);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.setLocalizedValues(12L,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, values);
                return null;
            }
        });
        // now retrieve the values again
        final List<VOLocalizedText> localizedValues = runTX(
                new Callable<List<VOLocalizedText>>() {
                    @Override
                    public List<VOLocalizedText> call() throws Exception {
                        return localizer.getLocalizedValues(12L,
                                LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);
                    }
                });

        assertEquals("Wrong number of values returned", 1,
                localizedValues.size());
        assertEquals("en", localizedValues.get(0).getLocale());
        assertEquals("house", localizedValues.get(0).getText());
    }

    @Test
    public void testCheckExistenceOfBundleFiles() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localizer.checkExistenceOfBundleFiles();
                return null;
            }
        });
    }

    @Test
    public void testGetLocalizedTextFromBundle() throws Exception {
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromBundle(
                        LocalizedObjectTypes.MAIL_CONTENT, null, "en",
                        "SUBSCRIPTION_ACTIVATED.text");
            }
        });
        assertEquals("Wrong mail content retrieved",
                "Your subscription {0} has been activated.", text);
    }

    @Test
    public void getLocalizedTextFromBundle_PlatformLocalizations()
            throws Exception {
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromBundle(
                        LocalizedObjectTypes.EVENT_DESC, null, "en",
                        "EVENT_DESC.1000");
            }
        });
        assertEquals("Login of a user to the service.", text);
    }

    /**
     * Load an exception message from the resource bundle
     * 
     */
    @Test
    public void testGetLocalizedTextFromBundle_exceptionProperties()
            throws Exception {
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromBundle(
                        LocalizedObjectTypes.EXCEPTION_PROPERTIES, null, "en",
                        "ex.ImageException.UPLOAD");
            }
        });
        assertEquals("The image file upload failed.", text);
    }

    /**
     * Exception messages can be customized by the user and imported to the
     * database for each marketplace and for the complete BES installation.<br>
     * 
     * This test checks that messages stored in the database overwrite messages
     * stored in the resource bundle
     * 
     */
    @Test
    public void testGetLocalizedTextFromBundle_exceptionPropertiesFromShop()
            throws Exception {

        // given a marketplace with imported messages from user
        final Marketplace mp = createMarketplace();
        importMessagesForMarketplace(mp, "ex.ImageException.UPLOAD=new_text");

        // when loading exception messages
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromBundle(
                        LocalizedObjectTypes.EXCEPTION_PROPERTIES, mp, "en",
                        "ex.ImageException.UPLOAD");
            }
        });

        // then result contains value from DB and not from resource bundle
        assertEquals("new_text", text);
    }

    private Marketplace createMarketplace() throws Exception {
        final Marketplace shop = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                createOrganizationRoles(mgr);
                Organization supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                return Marketplaces.ensureMarketplace(supplier, null, mgr);
            }
        });
        return shop;
    }

    /**
     * Simulate a user import of localized messages for one marketplace
     */
    private void importMessagesForMarketplace(final Marketplace mp,
            final String properties) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(mp.getKey());
                resource.setLocale("en");
                resource.setObjectType(
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);
                resource.setValue(properties);
                mgr.persist(resource);
                return null;
            }
        });
    }

    @Test
    public void testGetLocalizedTextFromBundleNonExistingKey()
            throws Exception {
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromBundle(
                        LocalizedObjectTypes.MAIL_CONTENT, null, "en",
                        "SUBSCRIPTION_ACTIVATED.text_suffix");
            }
        });
        assertEquals("Wrong mail content retrieved",
                "SUBSCRIPTION_ACTIVATED.text_suffix", text);
    }

    @Test
    public void testGetLocalizedTextFromBundleShopWithoutProperties()
            throws Exception {
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromBundle(
                        LocalizedObjectTypes.MAIL_CONTENT, null, "en",
                        "SUBSCRIPTION_ACTIVATED.text_suffix");
            }
        });
        assertEquals("Wrong mail content retrieved",
                "SUBSCRIPTION_ACTIVATED.text_suffix", text);
    }

    @Test
    public void testGetLocalizedTextFromBundleShopWithProperties()
            throws Exception {
        final Marketplace shop = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                createOrganizationRoles(mgr);
                Organization supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                return Marketplaces.ensureMarketplace(supplier, null, mgr);
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(shop.getKey());
                resource.setLocale("de");
                resource.setObjectType(
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES);
                resource.setValue("testValue=bla\r\nanotherEntry=present");
                mgr.persist(resource);
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromBundle(
                        LocalizedObjectTypes.MAIL_CONTENT, shop, "de",
                        "testValue");
            }
        });
        assertEquals("Wrong mail content retrieved", "bla", text);
    }

    @Test
    public void testLoadLocalizedPropertiesFromFile() throws Exception {
        Properties props = runTX(new Callable<Properties>() {
            @Override
            public Properties call() throws Exception {
                return localizer.loadLocalizedPropertiesFromFile("Mail", "de");
            }
        });
        assertNotNull("Properties object must not be null", props);
        assertTrue("No content found", props.size() > 0);
    }

    @Test
    public void testLoadLocalizedPropertiesFromNonExistingFile()
            throws Exception {
        Properties props = runTX(new Callable<Properties>() {
            @Override
            public Properties call() throws Exception {
                return localizer.loadLocalizedPropertiesFromFile("Mails", "de");
            }
        });
        assertNotNull("Properties object must not be null", props);
        assertEquals(
                "A file of that name cannot be found, so no props must be returned",
                0, props.size());
    }

    @Test
    public void testObjectTypes_GetLocalizedTextFromDatabaseHit()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(2L);
                resource.setLocale("de");
                resource.setObjectType(
                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC);
                resource.setValue("testValue");
                mgr.persist(resource);
                return null;
            }
        });
        Map<LocalizedObjectTypes, String> resultMap = runTX(
                new Callable<Map<LocalizedObjectTypes, String>>() {
                    @Override
                    public Map<LocalizedObjectTypes, String> call()
                            throws Exception {
                        return localizer.getLocalizedTextFromDatabase("de", 2L,
                                Collections.singletonList(
                                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC));
                    }
                });
        assertEquals(1, resultMap.size());
        assertEquals("Wrong return text", "testValue",
                resultMap.get(LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC));
    }

    @Test
    public void testObjectTypes_GetLocalizedTextFromDatabaseProductRelatedKey()
            throws Exception {
        final Product product = createProduct();
        final Product copy = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product copy = product.copyForCustomer(product.getVendor());
                mgr.persist(copy);
                mgr.flush();

                // first object type
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(copy.getKey());
                resource.setLocale("de");
                resource.setObjectType(
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
                resource.setValue("testLicenseValue");
                mgr.persist(resource);

                LocalizedResource resource2 = new LocalizedResource();
                resource2.setLocale("de");
                resource2.setObjectType(
                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
                resource2.setObjectKey(product.getKey());
                resource2.setValue("parentValue");
                mgr.persist(resource2);

                // second object type
                LocalizedResource marketingResource = new LocalizedResource();
                marketingResource.setObjectKey(copy.getKey());
                marketingResource.setLocale("de");
                marketingResource.setObjectType(
                        LocalizedObjectTypes.PRODUCT_MARKETING_DESC);
                marketingResource.setValue("testDescriptionValue");
                mgr.persist(marketingResource);

                return copy;
            }
        });

        Map<LocalizedObjectTypes, String> resultMap = runTX(
                new Callable<Map<LocalizedObjectTypes, String>>() {
                    @Override
                    public Map<LocalizedObjectTypes, String> call()
                            throws Exception {
                        return localizer.getLocalizedTextFromDatabase("de",
                                copy.getKey(),
                                Arrays.asList(
                                        LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                                        LocalizedObjectTypes.PRODUCT_MARKETING_DESC));
                    }
                });

        assertEquals(2, resultMap.size());
        assertEquals("testLicenseValue",
                resultMap.get(LocalizedObjectTypes.PRODUCT_LICENSE_DESC));
        assertEquals("testDescriptionValue",
                resultMap.get(LocalizedObjectTypes.PRODUCT_MARKETING_DESC));
    }

    @Test
    public void testObjectTypes_GetLocalizedTextFromDatabasePriceModelRelatedKey()
            throws Exception {

        final Product product = createProduct();
        final Product copy = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product copy = product.copyForCustomer(product.getVendor());
                mgr.persist(copy);

                // resource 1
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(product.getPriceModel().getKey());
                resource.setLocale("de");
                resource.setObjectType(
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
                resource.setValue("deTestValue");
                mgr.persist(resource);

                // resource 2
                LocalizedResource resource2 = new LocalizedResource();
                resource2.setObjectKey(product.getPriceModel().getKey());
                resource2.setLocale("en");
                resource2.setObjectType(
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
                resource2.setValue("enTestValue");
                mgr.persist(resource2);

                return copy;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de",
                        copy.getPriceModel().getKey(),
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
            }
        });
        assertEquals("Wrong return text", "deTestValue", text);
    }

    /**
     * Creates a technical product and marketing product as well as the required
     * organization.
     * 
     * @throws Exception
     */
    private Product createProduct() throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                createOrganizationRoles(mgr);
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, org, "tp", false, ServiceAccessType.LOGIN);
                Product product = Products.createProduct(org, tp, true, "mp",
                        null, mgr);
                return product;
            }
        });
    }

    private void sort(List<VOLocalizedText> texts) {
        Collections.sort(texts, new Comparator<VOLocalizedText>() {
            @Override
            public int compare(VOLocalizedText t1, VOLocalizedText t2) {
                return t1.getLocale().compareTo(t2.getLocale());
            }
        });
    }

    @Test
    public void testGetLocalizedMPLStageFromDatabase() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource resource = new LocalizedResource();
                resource.setObjectKey(1L);
                resource.setLocale("de");
                resource.setObjectType(LocalizedObjectTypes.MARKETPLACE_STAGE);
                resource.setValue("testValue");
                mgr.persist(resource);
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de", 1L,
                        LocalizedObjectTypes.MARKETPLACE_STAGE);
            }
        });
        assertEquals("Wrong return text", "testValue", text);
    }

    /**
     * Test if the price model short description was properly localized.
     */
    @Test
    public void testGetLocalizedProductShortDescription() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource resource_de = new LocalizedResource();
                resource_de.setObjectKey(1L);
                resource_de.setLocale("de");
                resource_de.setObjectType(
                        LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
                resource_de.setValue("testValue_de");
                mgr.persist(resource_de);

                LocalizedResource resource_en = new LocalizedResource();
                resource_en.setObjectKey(1L);
                resource_en.setLocale("en");
                resource_en.setObjectType(
                        LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
                resource_en.setValue("testValue_en");
                mgr.persist(resource_en);
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de", 1L,
                        LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
            }
        });
        assertEquals("Wrong return text", "testValue_de", text);

        text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return // Test the default mechanism
                localizer.getLocalizedTextFromDatabase("ja", 1L,
                        LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
            }
        });
        assertEquals("Wrong return text", "testValue_en", text);
    }

    @Test
    public void testGetLocalizedCustomTabName() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LocalizedResource resource_de = new LocalizedResource();
                resource_de.setObjectKey(1L);
                resource_de.setLocale("de");
                resource_de.setObjectType(
                        LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME);
                resource_de.setValue("testValue_de");
                mgr.persist(resource_de);

                LocalizedResource resource_en = new LocalizedResource();
                resource_en.setObjectKey(1L);
                resource_en.setLocale("en");
                resource_en.setObjectType(
                        LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME);
                resource_en.setValue("testValue_en");
                mgr.persist(resource_en);
                return null;
            }
        });
        String text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localizer.getLocalizedTextFromDatabase("de", 1L,
                        LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME);
            }
        });
        assertEquals("Wrong return text", "testValue_de", text);

        text = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return // Test the default mechanism
                localizer.getLocalizedTextFromDatabase("ja", 1L,
                        LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME);
            }
        });
        assertEquals("Wrong return text", "testValue_en", text);
    }

}
