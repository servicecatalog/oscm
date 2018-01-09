/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *  Completion Time: 22.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;
import org.oscm.billingservice.dao.model.UdaBillingData;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Scenario;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.Udas;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * Test class for UDA related retrieval operations for billing.
 * 
 */
public class BillingDataRetrievalServiceBeanUDAIT extends EJBTestBase {

    private static final String TEST123 = "Test123";
    private DataService dm;
    private BillingDataRetrievalServiceLocal bdr;
    private long supplierKey;
    private long secondSupplierKey;
    protected UdaDefinition secondSupplierUda;

    /**
     * Common setup for the test class.
     */
    @Override
    public void setup(final TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }
        });
        container.addBean(new BillingDataRetrievalServiceBean());

        dm = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCountries.createSomeSupportedCountries(dm);
                Scenario.setup(container, true);

                Organization customer = Scenario.getCustomer();
                // create a second supplier that also defines a UDA for the same
                // customer - test that there is no interference between the
                // suppliers
                Organization secondSupplier = Organizations
                        .createOrganization(dm, OrganizationRoleType.SUPPLIER);
                secondSupplierKey = secondSupplier.getKey();
                Organizations.addSupplierToCustomer(dm, secondSupplier,
                        customer);
                secondSupplierUda = Udas.createUdaDefinition(dm, secondSupplier,
                        UdaTargetType.CUSTOMER, TEST123, null,
                        UdaConfigurationType.SUPPLIER);
                Udas.createUda(dm, customer, secondSupplierUda, TEST123);
                return null;
            }
        });
        supplierKey = Scenario.getSupplier().getKey();
    }

    @Test
    public void testGetUdasForCustomer_NoUdas() throws Exception {
        final long customerKey = Scenario.getSecondCustomer().getKey();
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForCustomer(customerKey,
                                supplierKey);
                    }
                });
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetUdasForCustomer_InvalidOrgHist() throws Exception {
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForCustomer(-5, supplierKey);
                    }
                });
        // we don't expect an exception in this case, just return nothing...
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetUdasForCustomer_FoundUdas() throws Exception {
        final long customerKey = Scenario.getCustomer().getKey();
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForCustomer(customerKey,
                                supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("UdaCust1_Value", uh1.getValue());
        assertEquals("UdaDefCust1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaCust2_Value", uh2.getValue());
        assertEquals("UdaDefCust2", uh2.getIdentifier());
    }

    @Test
    public void testGetUdasForCustomer_FoundUdasSecondSupplier()
            throws Exception {
        final long customerKey = Scenario.getCustomer().getKey();
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForCustomer(customerKey,
                                secondSupplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(1, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals(TEST123, uh1.getValue());
        assertEquals(TEST123, uh1.getIdentifier());
    }

    @Test
    public void testGetUdasForCustomer_ModifyUdaDef() throws Exception {
        final long customerKey = Scenario.getCustomer().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UdaDefinition udaDefCust1 = Scenario.getUdaDefCust1();

                UdaDefinition udaDef = dm.find(UdaDefinition.class,
                        udaDefCust1.getKey());
                udaDef.setDefaultValue("changedValue");
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForCustomer(customerKey,
                                supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("UdaCust1_Value", uh1.getValue());
        assertEquals("UdaDefCust1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaCust2_Value", uh2.getValue());
        assertEquals("UdaDefCust2", uh2.getIdentifier());
    }

    @Test
    public void testGetUdasForCustomer_ModifyUdaDefSecondSupplier()
            throws Exception {
        final long customerKey = Scenario.getCustomer().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UdaDefinition udaDef = dm.find(UdaDefinition.class,
                        secondSupplierUda.getKey());
                udaDef.setDefaultValue("changedValue");
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForCustomer(customerKey,
                                secondSupplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(1, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals(TEST123, uh1.getValue());
        assertEquals(TEST123, uh1.getIdentifier());
    }

    @Test
    public void testGetUdasForCustomer_ModifyCustomer() throws Exception {
        final long customerKey = Scenario.getCustomer().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization cust = dm.find(Organization.class, customerKey);
                cust.setAddress("xxx");
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForCustomer(customerKey,
                                supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("UdaCust1_Value", uh1.getValue());
        assertEquals("UdaDefCust1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaCust2_Value", uh2.getValue());
        assertEquals("UdaDefCust2", uh2.getIdentifier());
    }

    @Test
    public void testGetUdasForCustomer_ModifiedUdas() throws Exception {
        final long customerKey = Scenario.getCustomer().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Uda customerUda1 = Scenario.getCustomerUda1();
                customerUda1 = dm.getReference(Uda.class,
                        customerUda1.getKey());
                customerUda1.setUdaValue("ModifiedValue1");

                customerUda1.getUdaDefinition().getUdas().size();
                dm.flush();
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForCustomer(customerKey,
                                supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("ModifiedValue1", uh1.getValue());
        assertEquals("UdaDefCust1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaCust2_Value", uh2.getValue());
        assertEquals("UdaDefCust2", uh2.getIdentifier());
    }

    @Test
    public void testGetUdasForCustomer_RemovedUdas() throws Exception {
        final long customerKey = Scenario.getCustomer().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Uda customerUda1 = Scenario.getCustomerUda1();
                customerUda1 = dm.getReference(Uda.class,
                        customerUda1.getKey());

                customerUda1.getUdaDefinition().getUdas().size();
                dm.remove(customerUda1);
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForCustomer(customerKey,
                                supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(1, result.size());
        UdaBillingData uh = result.get(0);
        assertEquals("UdaCust2_Value", uh.getValue());
        assertEquals("UdaDefCust2", uh.getIdentifier());
    }

    @Test
    public void testGetUdasForCustomer_RemovedAndModifiedUdas()
            throws Exception {
        final long customerKey = Scenario.getCustomer().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Uda customerUda1 = Scenario.getCustomerUda1();
                customerUda1 = dm.getReference(Uda.class,
                        customerUda1.getKey());

                Uda customerUda2 = Scenario.getCustomerUda2();
                customerUda2 = dm.getReference(Uda.class,
                        customerUda2.getKey());
                customerUda2.setUdaValue("ModifiedValue");

                customerUda1.getUdaDefinition().getUdas().size();
                customerUda2.getUdaDefinition().getUdas().size();
                dm.remove(customerUda1);
                dm.flush();
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForCustomer(customerKey,
                                supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(1, result.size());
        UdaBillingData uh = result.get(0);
        assertEquals("ModifiedValue", uh.getValue());
        assertEquals("UdaDefCust2", uh.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_NoUdas() throws Exception {
        Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Product prod = Scenario.getProduct();
                return Subscriptions.createSubscription(dm,
                        Scenario.getSecondCustomer().getOrganizationId(),
                        prod.getProductId(), "sub_without_udas",
                        prod.getVendor());
            }
        });
        final long subKey = sub.getKey();
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetUdasForSubscription_InvalidSubHist() throws Exception {
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(-5, supplierKey);
                    }
                });
        // we don't expect an exception in this case, just return nothing...
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetUdasForSubscription_FoundUdas() throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("UdaSub_Value1", uh1.getValue());
        assertEquals("UdaDefSub1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaSub_Value2", uh2.getValue());
        assertEquals("UdaDefSub2", uh2.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_NotOwner() throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey,
                                secondSupplierKey);
                    }
                });
        assertNotNull(result);
        // the second supplier must not see the UDAs of the first one
        assertEquals(0, result.size());
    }

    @Test
    public void testGetUdasForSubscription_ModifiedUdaDef() throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UdaDefinition uda1 = Scenario.getUdaDefSub1();

                UdaDefinition udaDef = dm.find(UdaDefinition.class,
                        uda1.getKey());
                udaDef.setDefaultValue("changedValue");
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("UdaSub_Value1", uh1.getValue());
        assertEquals("UdaDefSub1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaSub_Value2", uh2.getValue());
        assertEquals("UdaDefSub2", uh2.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_ModifiedSub() throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = dm.find(Subscription.class, subKey);
                sub.setBaseURL("xxx");
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("UdaSub_Value1", uh1.getValue());
        assertEquals("UdaDefSub1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaSub_Value2", uh2.getValue());
        assertEquals("UdaDefSub2", uh2.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_ModifiedUdas() throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Uda uda1 = Scenario.getSubUda1();
                uda1 = dm.getReference(Uda.class, uda1.getKey());
                uda1.setUdaValue("ModifiedValue1");

                uda1.getUdaDefinition().getUdas().size();
                dm.flush();
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("ModifiedValue1", uh1.getValue());
        assertEquals("UdaDefSub1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaSub_Value2", uh2.getValue());
        assertEquals("UdaDefSub2", uh2.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_RemovedUdas() throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Uda uda1 = Scenario.getSubUda1();
                uda1 = dm.getReference(Uda.class, uda1.getKey());

                uda1.getUdaDefinition().getUdas().size();
                dm.remove(uda1);
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(1, result.size());
        UdaBillingData uh = result.get(0);
        assertEquals("UdaSub_Value2", uh.getValue());
        assertEquals("UdaDefSub2", uh.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_RemovedAndModifiedUdas()
            throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Uda uda1 = Scenario.getSubUda1();
                uda1 = dm.getReference(Uda.class, uda1.getKey());

                Uda uda2 = Scenario.getSubUda2();
                uda2 = dm.getReference(Uda.class, uda2.getKey());
                uda2.setUdaValue("ModifiedValue");

                uda1.getUdaDefinition().getUdas().size();
                uda2.getUdaDefinition().getUdas().size();
                dm.remove(uda1);
                dm.flush();
                return null;
            }
        });
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(1, result.size());
        UdaBillingData uh = result.get(0);
        assertEquals("ModifiedValue", uh.getValue());
        assertEquals("UdaDefSub2", uh.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_ModifyAndDeleteUda()
            throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        updateSubUda1("updatedValue");
        removeSubUda1();
        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertNotNull(result);
        assertEquals(1, result.size());
        UdaBillingData uh = result.get(0);
        assertEquals("UdaSub_Value2", uh.getValue());
        assertEquals("UdaDefSub2", uh.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_ModifyDeleteAndDeleteSub()
            throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        updateSubUda1("newValue");
        removeSubUda1();
        removeSubscription(subKey);

        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertEquals(1, result.size());
        UdaBillingData uh = result.get(0);
        assertEquals("UdaSub_Value2", uh.getValue());
        assertEquals("UdaDefSub2", uh.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_DeleteSub() throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        removeSubscription(subKey);

        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("UdaSub_Value1", uh1.getValue());
        assertEquals("UdaDefSub1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaSub_Value2", uh2.getValue());
        assertEquals("UdaDefSub2", uh2.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_ModifyUdaAndDeleteSub()
            throws Exception {
        final long subKey = Scenario.getSubscription().getKey();
        updateSubUda1("updatedValue");
        removeSubscription(subKey);

        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(subKey, supplierKey);
                    }
                });
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("updatedValue", uh1.getValue());
        assertEquals("UdaDefSub1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaSub_Value2", uh2.getValue());
        assertEquals("UdaDefSub2", uh2.getIdentifier());
    }

    @Test
    public void testGetUdasForSubscription_TwoSubsDeleteOneModifyUdas()
            throws Exception {
        final long sub1Key = Scenario.getSubscription().getKey();
        final Long sub2Key = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Organization secondCustomer = Scenario.getSecondCustomer();
                secondCustomer = (Organization) dm.find(secondCustomer);

                Product product = Scenario.getProduct();

                Subscription newSub = Subscriptions.createSubscription(dm,
                        secondCustomer.getOrganizationId(),
                        product.getProductId(), "newCustomersSub",
                        product.getVendor());

                Udas.createUda(dm, newSub, Scenario.getUdaDefSub1(),
                        "newSub_Value1");
                Udas.createUda(dm, newSub, Scenario.getUdaDefSub2(),
                        "newSub_Value2");

                Long keyValue = Long.valueOf(newSub.getKey());
                return keyValue;
            }
        });
        removeSubscription(sub2Key.longValue());
        updateSubUda1("newSub1UdaValue");
        removeSubscription(sub1Key);

        List<UdaBillingData> result = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(sub1Key,
                                supplierKey);
                    }
                });
        assertEquals(2, result.size());
        UdaBillingData uh1 = result.get(0);
        assertEquals("newSub1UdaValue", uh1.getValue());
        assertEquals("UdaDefSub1", uh1.getIdentifier());
        UdaBillingData uh2 = result.get(1);
        assertEquals("UdaSub_Value2", uh2.getValue());
        assertEquals("UdaDefSub2", uh2.getIdentifier());

        List<UdaBillingData> result2 = runTX(
                new Callable<List<UdaBillingData>>() {
                    @Override
                    public List<UdaBillingData> call() throws Exception {
                        return bdr.loadUdasForSubscription(sub2Key.longValue(),
                                supplierKey);
                    }
                });
        assertEquals(2, result2.size());
        uh1 = result2.get(0);
        assertEquals("newSub_Value1", uh1.getValue());
        assertEquals("UdaDefSub1", uh1.getIdentifier());
        uh2 = result2.get(1);
        assertEquals("newSub_Value2", uh2.getValue());
        assertEquals("UdaDefSub2", uh2.getIdentifier());
    }

    // **********************************************************************
    // internal methods

    /**
     * Removes the first subsctiption related UDA.
     * 
     * @throws Exception
     */
    private void removeSubUda1() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Uda uda1 = Scenario.getSubUda1();
                uda1 = dm.getReference(Uda.class, uda1.getKey());
                uda1.getUdaDefinition().getUdas().size();
                dm.remove(uda1);
                dm.flush();
                return null;
            }
        });
    }

    /**
     * Modifies the value of the first subscription UDA to given value.
     * 
     * @throws Exception
     */
    private void updateSubUda1(final String value) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Uda uda1 = Scenario.getSubUda1();
                uda1 = dm.getReference(Uda.class, uda1.getKey());
                uda1.setUdaValue(value);
                uda1.getUdaDefinition().getUdas().size();
                return null;
            }
        });
    }

    /**
     * Removes the subscription and its UDAs.
     * 
     * @param subKey
     *            The key of the subscription to remove.
     * 
     * @throws Exception
     */
    private void removeSubscription(final long subKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Long timeMillis = Long.valueOf(System.currentTimeMillis() + 1);
                Subscription reference = dm.getReference(Subscription.class,
                        subKey);
                reference.setHistoryModificationTime(timeMillis);
                dm.remove(reference);
                Query query = dm.createQuery(
                        "SELECT uda FROM Uda uda WHERE uda.dataContainer.targetObjectKey = :key");
                query.setParameter("key", Long.valueOf(subKey));
                List<Uda> udas = ParameterizedTypes.list(query.getResultList(),
                        Uda.class);
                for (Uda uda : udas) {
                    uda.setHistoryModificationTime(timeMillis);
                    uda.getUdaDefinition().getUdas().size();
                    dm.remove(uda);
                }
                return null;
            }
        });
    }

}
