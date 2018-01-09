/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 07.07.2010                                                      
 *                                                                              
 *  Completion Time: 07.07.2010                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.billingservice.dao.model.ParameterOptionRolePricingData;
import org.oscm.billingservice.dao.model.RolePricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Tests for the billing data retrieval service that focus on the role pricing
 * related functionality.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class BillingDataRetrievalServiceBeanRolePricesIT extends EJBTestBase {

    private DataService mgr;
    private BillingDataRetrievalServiceLocal bdrs;

    private Organization supplierAndProvider;
    private TechnicalProduct technicalProduct;
    private Product product;
    private Subscription subscription;
    private PriceModel priceModel;
    private boolean isRootDataInitialized = false;
    private PricedOption pricedOption;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());

        mgr = container.get(DataService.class);
        bdrs = container.get(BillingDataRetrievalServiceLocal.class);
    }

    @Test
    public void testGetRoleDefinitionsForPriceModelNonExistingPM()
            throws Exception {
        Map<Long, RoleDefinitionHistory> roleDefinitionsForPriceModel = runTX(
                new Callable<Map<Long, RoleDefinitionHistory>>() {
                    @Override
                    public Map<Long, RoleDefinitionHistory> call()
                            throws Exception {
                        return bdrs.loadRoleDefinitionsForPriceModel(11L, 11L);
                    }
                });
        Assert.assertNotNull(roleDefinitionsForPriceModel);
        Assert.assertEquals(0, roleDefinitionsForPriceModel.size());
    }

    @Test
    public void testGetRoleDefinitionsForPriceModelOneHit() throws Exception {
        initData(1, false);
        Map<Long, RoleDefinitionHistory> roles = runTX(
                new Callable<Map<Long, RoleDefinitionHistory>>() {
                    @Override
                    public Map<Long, RoleDefinitionHistory> call()
                            throws Exception {
                        return bdrs.loadRoleDefinitionsForPriceModel(
                                subscription.getPriceModel().getKey(),
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertEquals(1, roles.size());
        Set<Long> keys = roles.keySet();
        for (Long key : keys) {
            Assert.assertEquals("roleDefinition0", roles.get(key).getRoleId());
        }
    }

    @Test
    public void testGetRoleDefinitionsForPriceModelRoleDefForDifferentProduct()
            throws Exception {
        initData(0, false);
        final long priceModelKey = subscription.getPriceModel().getKey();
        initData(1, false);
        Map<Long, RoleDefinitionHistory> roles = runTX(
                new Callable<Map<Long, RoleDefinitionHistory>>() {
                    @Override
                    public Map<Long, RoleDefinitionHistory> call()
                            throws Exception {
                        return bdrs.loadRoleDefinitionsForPriceModel(
                                priceModelKey,
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertEquals(0, roles.size());
    }

    @Test
    public void testGetRoleDefinitionsForPriceModelTwoHitsForMultipleExistingPMs()
            throws Exception {
        initData(4, false);
        initData(2, false);
        final long priceModelKey = subscription.getPriceModel().getKey();
        initData(1, false);
        Map<Long, RoleDefinitionHistory> roles = runTX(
                new Callable<Map<Long, RoleDefinitionHistory>>() {
                    @Override
                    public Map<Long, RoleDefinitionHistory> call()
                            throws Exception {
                        return bdrs.loadRoleDefinitionsForPriceModel(
                                priceModelKey,
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertEquals(2, roles.size());
        Set<Long> keys = roles.keySet();
        long[] keyArray = { 0, 0 };
        int i = 0;
        for (long key : keys) {
            keyArray[i] = key;
            i++;
        }

        // swap keys if not sorted order
        if (keyArray[0] > keyArray[1]) {
            long tmp = keyArray[0];
            keyArray[0] = keyArray[1];
            keyArray[1] = tmp;
        }

        Assert.assertEquals("roleDefinition0",
                roles.get(Long.valueOf(keyArray[0])).getRoleId());
        Assert.assertEquals("roleDefinition1",
                roles.get(Long.valueOf(keyArray[1])).getRoleId());
    }

    @Test
    public void testGetRoleDefinitionsForPriceModelNoHitOutOfPeriod()
            throws Exception {
        initData(1, false);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                technicalProduct = mgr.find(TechnicalProduct.class,
                        technicalProduct.getKey());
                long rdKey = technicalProduct.getRoleDefinitions().get(0)
                        .getKey();
                Query query = mgr.createQuery(
                        "UPDATE RoleDefinitionHistory rdh SET rdh.modDate = :date WHERE rdh.objKey = :objKey");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 1);
                query.setParameter("date", cal.getTime());
                query.setParameter("objKey", Long.valueOf(rdKey));
                query.executeUpdate();
                return null;
            }
        });
        Map<Long, RoleDefinitionHistory> roles = runTX(
                new Callable<Map<Long, RoleDefinitionHistory>>() {
                    @Override
                    public Map<Long, RoleDefinitionHistory> call()
                            throws Exception {
                        return bdrs.loadRoleDefinitionsForPriceModel(
                                subscription.getPriceModel().getKey(),
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertEquals(0, roles.size());
    }

    @Test
    public void testGetRoleDefinitionsForPriceModelCheckVersionForEditedEntry()
            throws Exception {
        initData(4, false);
        initData(2, false);
        final long priceModelKey = subscription.getPriceModel().getKey();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscription = mgr.find(Subscription.class,
                        subscription.getKey());
                RoleDefinition rd = subscription.getProduct()
                        .getTechnicalProduct().getRoleDefinitions().get(0);
                rd.setRoleId("updatedRoleId");
                return null;
            }
        });

        Map<Long, RoleDefinitionHistory> roles = runTX(
                new Callable<Map<Long, RoleDefinitionHistory>>() {
                    @Override
                    public Map<Long, RoleDefinitionHistory> call()
                            throws Exception {
                        return bdrs.loadRoleDefinitionsForPriceModel(
                                priceModelKey,
                                System.currentTimeMillis() + 5000);
                    }
                });
        Set<Long> keys = roles.keySet();
        long[] keyArray = { 0, 0 };
        int i = 0;
        for (long key : keys) {
            keyArray[i] = key;
            i++;
        }

        // swap keys if not sorted order
        if (keyArray[0] > keyArray[1]) {
            long tmp = keyArray[0];
            keyArray[0] = keyArray[1];
            keyArray[1] = tmp;
        }
        Assert.assertEquals(2, roles.size());
        Long key = Long.valueOf(keyArray[0]);
        Assert.assertEquals("updatedRoleId", roles.get(key).getRoleId());
        Assert.assertEquals(1,
                roles.get(Long.valueOf(keyArray[0])).getObjVersion());
        Assert.assertEquals("roleDefinition1",
                roles.get(Long.valueOf(keyArray[1])).getRoleId());
        Assert.assertEquals(0,
                roles.get(Long.valueOf(keyArray[1])).getObjVersion());
    }

    @Test
    public void testGetRoleRelatedCostsForPriceModelNoHit() throws Exception {
        Map<Long, RolePricingDetails> result = runTX(
                new Callable<Map<Long, RolePricingDetails>>() {
                    @Override
                    public Map<Long, RolePricingDetails> call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForPriceModel(1111L,
                                1111L);
                    }
                });
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.keySet().size());
    }

    @Test
    public void testGetRoleRelatedCostsForPriceModelOneHit() throws Exception {
        initData(1, false);
        Map<Long, RolePricingDetails> result = runTX(
                new Callable<Map<Long, RolePricingDetails>>() {
                    @Override
                    public Map<Long, RolePricingDetails> call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForPriceModel(
                                subscription.getPriceModel().getKey(),
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.keySet().size());
    }

    @Test
    public void testGetRoleRelatedCostsForPriceModelTwoHits() throws Exception {
        initData(2, false);
        Map<Long, RolePricingDetails> result = runTX(
                new Callable<Map<Long, RolePricingDetails>>() {
                    @Override
                    public Map<Long, RolePricingDetails> call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForPriceModel(
                                subscription.getPriceModel().getKey(),
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.keySet().size());
        Iterator<Long> iterator = result.keySet().iterator();
        List<Long> keys = new ArrayList<>();
        keys.add(iterator.next());
        keys.add(iterator.next());
        Collections.sort(keys);
        Assert.assertEquals(new BigDecimal(12),
                result.get(keys.get(0)).getPricePerUser());
        Assert.assertEquals(new BigDecimal(24),
                result.get(keys.get(1)).getPricePerUser());
    }

    @Test
    public void testGetRoleRelatedCostsForPriceModelOneHitMultipleSubscriptions()
            throws Exception {
        initData(1, false);
        final long priceModelKey = subscription.getPriceModel().getKey();
        initData(2, false);
        Map<Long, RolePricingDetails> result = runTX(
                new Callable<Map<Long, RolePricingDetails>>() {
                    @Override
                    public Map<Long, RolePricingDetails> call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForPriceModel(
                                priceModelKey,
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.keySet().size());
        Assert.assertEquals(new BigDecimal(12), result
                .get(result.keySet().iterator().next()).getPricePerUser());
    }

    @Test
    public void testGetRoleRelatedCostsForPriceModelOneHitLatestVersion()
            throws Exception {
        initData(1, false);
        final long priceModelKey = subscription.getPriceModel().getKey();

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                subscription = mgr.find(Subscription.class,
                        subscription.getKey());
                PricedProductRole ppr = subscription.getPriceModel()
                        .getRoleSpecificUserPrices().get(0);
                ppr.setPricePerUser(new BigDecimal(123L));
                mgr.persist(ppr);
                return null;
            }
        });

        initData(2, false);
        Map<Long, RolePricingDetails> result = runTX(
                new Callable<Map<Long, RolePricingDetails>>() {
                    @Override
                    public Map<Long, RolePricingDetails> call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForPriceModel(
                                priceModelKey,
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.keySet().size());
        Assert.assertEquals(new BigDecimal(123), result
                .get(result.keySet().iterator().next()).getPricePerUser());
    }

    @Test
    public void testGetRoleRelatedCostsForPriceModelNoHitInPeriod()
            throws Exception {
        initData(1, false);
        final long priceModelKey = subscription.getPriceModel().getKey();

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                subscription = mgr.find(Subscription.class,
                        subscription.getKey());
                PricedProductRole ppr = subscription.getPriceModel()
                        .getRoleSpecificUserPrices().get(0);
                Query query = mgr.createQuery(
                        "UPDATE PricedProductRoleHistory pprh SET pprh.modDate = :modDate WHERE pprh.objKey = :objKey");
                query.setParameter("objKey", Long.valueOf(ppr.getKey()));
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 1);
                query.setParameter("modDate", cal.getTime());
                query.executeUpdate();
                return null;
            }
        });

        initData(2, false);
        Map<Long, RolePricingDetails> result = runTX(
                new Callable<Map<Long, RolePricingDetails>>() {
                    @Override
                    public Map<Long, RolePricingDetails> call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForPriceModel(
                                priceModelKey,
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.keySet().size());
    }

    @Test
    public void testGetRoleRelatedCostsForParametersForNonExistingPM()
            throws Exception {
        RolePricingData paramCosts = runTX(new Callable<RolePricingData>() {
            @Override
            public RolePricingData call() throws Exception {
                return bdrs.loadRoleRelatedCostsForParameters(1111L, 1111L);
            }
        });
        Assert.assertNotNull(paramCosts);
        Assert.assertEquals(0, paramCosts.getContainerKeys().size());
    }

    @Test
    public void testGetRoleRelatedCostsForParametersForOneParamMultiplePricedRoles()
            throws Exception {
        initData(3, false);
        RolePricingData paramCosts = runTX(new Callable<RolePricingData>() {
            @Override
            public RolePricingData call() throws Exception {
                return bdrs.loadRoleRelatedCostsForParameters(
                        subscription.getPriceModel().getKey(),
                        System.currentTimeMillis() + 5000);
            }
        });
        Assert.assertNotNull(paramCosts);
        Set<Long> parameterKeys = paramCosts.getContainerKeys();
        Assert.assertEquals(1, parameterKeys.size());
        Map<Long, RolePricingDetails> rolePrices = paramCosts
                .getRolePricesForContainerKey(parameterKeys.iterator().next());
        Assert.assertNotNull(rolePrices);
        Assert.assertEquals(3, rolePrices.keySet().size());
    }

    @Test
    public void testGetRoleRelatedCostsForParametersForOneParamPPROutOfPeriod()
            throws Exception {
        initData(3, false);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscription = mgr.find(Subscription.class,
                        subscription.getKey());
                PricedProductRole ppr = subscription.getPriceModel()
                        .getSelectedParameters().get(0)
                        .getRoleSpecificUserPrices().get(0);
                Query query = mgr.createQuery(
                        "UPDATE PricedProductRoleHistory pprh SET pprh.modDate = :modDate WHERE pprh.objKey = :ppKey");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 1);
                query.setParameter("modDate", cal.getTime());
                query.setParameter("ppKey", Long.valueOf(ppr.getKey()));
                query.executeUpdate();
                return null;
            }
        });

        RolePricingData paramCosts = runTX(new Callable<RolePricingData>() {
            @Override
            public RolePricingData call() throws Exception {
                return bdrs.loadRoleRelatedCostsForParameters(
                        subscription.getPriceModel().getKey(),
                        System.currentTimeMillis() + 5000);
            }
        });
        Assert.assertNotNull(paramCosts);
        Set<Long> parameterKeys = paramCosts.getContainerKeys();
        Assert.assertEquals(1, parameterKeys.size());
        Map<Long, RolePricingDetails> rolePrices = paramCosts
                .getRolePricesForContainerKey(parameterKeys.iterator().next());
        Assert.assertNotNull(rolePrices);
        Assert.assertEquals(2, rolePrices.keySet().size());
    }

    @Test
    public void testGetRoleRelatedCostsForParametersForOneParamPPRNewVersion()
            throws Exception {
        initData(3, false);
        Long pprKey = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                subscription = mgr.find(Subscription.class,
                        subscription.getKey());
                PricedProductRole ppr = subscription.getPriceModel()
                        .getSelectedParameters().get(0)
                        .getRoleSpecificUserPrices().get(0);
                ppr.setPricePerUser(new BigDecimal(333L));
                return Long.valueOf(ppr.getRoleDefinition().getKey());
            }
        });

        RolePricingData paramCosts = runTX(new Callable<RolePricingData>() {
            @Override
            public RolePricingData call() throws Exception {
                return bdrs.loadRoleRelatedCostsForParameters(
                        subscription.getPriceModel().getKey(),
                        System.currentTimeMillis() + 5000);
            }
        });
        Assert.assertNotNull(paramCosts);
        Set<Long> parameterKeys = paramCosts.getContainerKeys();
        Assert.assertEquals(1, parameterKeys.size());
        Map<Long, RolePricingDetails> rolePrices = paramCosts
                .getRolePricesForContainerKey(parameterKeys.iterator().next());
        Assert.assertNotNull(rolePrices);
        Assert.assertEquals(3, rolePrices.keySet().size());
        RolePricingDetails pprh = rolePrices.get(pprKey);
        Assert.assertEquals(new BigDecimal(333L), pprh.getPricePerUser());
        Assert.assertEquals(1,
                pprh.getPricedProductRoleHistory().getObjVersion());
    }

    @Test
    public void testGetRoleRelatedCostsForParametersForTwoParamsMultiplePricedRoles()
            throws Exception {
        initData(3, true);
        RolePricingData paramCosts = runTX(new Callable<RolePricingData>() {
            @Override
            public RolePricingData call() throws Exception {
                return bdrs.loadRoleRelatedCostsForParameters(
                        subscription.getPriceModel().getKey(),
                        System.currentTimeMillis() + 5000);
            }
        });
        Assert.assertNotNull(paramCosts);
        Set<Long> parameterKeys = paramCosts.getContainerKeys();
        Assert.assertEquals(2, parameterKeys.size());
        Iterator<Long> iterator = parameterKeys.iterator();
        while (iterator.hasNext()) {
            Long paramKey = iterator.next();
            Map<Long, RolePricingDetails> rolePrices = paramCosts
                    .getRolePricesForContainerKey(paramKey);
            Assert.assertNotNull(rolePrices);
            Assert.assertEquals(3, rolePrices.keySet().size());
        }
    }

    @Test
    public void testGetRoleRelatedCostsForOptionsNoHit() throws Exception {
        ParameterOptionRolePricingData optionCosts = runTX(
                new Callable<ParameterOptionRolePricingData>() {
                    @Override
                    public ParameterOptionRolePricingData call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForOptions(1111L,
                                1111L);
                    }
                });
        Assert.assertNotNull(optionCosts);
        Assert.assertEquals(0, optionCosts.getPricedParameterKeys().size());
    }

    @Test
    public void testGetRoleRelatedCostsForOptionsOneOption() throws Exception {
        initData(2, false);
        ParameterOptionRolePricingData optionCosts = runTX(
                new Callable<ParameterOptionRolePricingData>() {
                    @Override
                    public ParameterOptionRolePricingData call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForOptions(
                                subscription.getPriceModel().getKey(),
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertNotNull(optionCosts);
        Assert.assertEquals(1, optionCosts.getPricedParameterKeys().size());
        Long parameterKey = optionCosts.getPricedParameterKeys().iterator()
                .next();
        RolePricingData rolePrices = optionCosts
                .getRolePricingDataForPricedParameterKey(parameterKey);
        Assert.assertEquals(1, rolePrices.getContainerKeys().size());
        Long optionKey = rolePrices.getContainerKeys().iterator().next();
        Map<Long, RolePricingDetails> optionRolePrices = rolePrices
                .getRolePricesForContainerKey(optionKey);
        Assert.assertEquals(2, optionRolePrices.keySet().size());
    }

    @Test
    public void testGetRoleRelatedCostsForOptionsOneOptionRoleDefOutOfPeriod()
            throws Exception {
        initData(2, false);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscription = mgr.find(Subscription.class,
                        subscription.getKey());
                priceModel = unproxyEntity(subscription.getPriceModel());

                pricedOption = mgr.find(PricedOption.class,
                        pricedOption.getKey());

                Query query = mgr.createQuery(
                        "UPDATE PricedProductRoleHistory poh SET poh.modDate = :modDate WHERE poh.objKey = :objKey");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 1);
                query.setParameter("modDate", cal.getTime());
                query.setParameter("objKey", Long.valueOf(pricedOption
                        .getRoleSpecificUserPrices().get(0).getKey()));
                query.executeUpdate();
                return null;
            }
        });

        ParameterOptionRolePricingData optionCosts = runTX(
                new Callable<ParameterOptionRolePricingData>() {
                    @Override
                    public ParameterOptionRolePricingData call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForOptions(
                                priceModel.getKey(),
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertNotNull(optionCosts);
        Assert.assertEquals(1, optionCosts.getPricedParameterKeys().size());
        Long parameterKey = optionCosts.getPricedParameterKeys().iterator()
                .next();
        RolePricingData rolePrices = optionCosts
                .getRolePricingDataForPricedParameterKey(parameterKey);
        Assert.assertEquals(1, rolePrices.getContainerKeys().size());
        Long optionKey = rolePrices.getContainerKeys().iterator().next();
        Map<Long, RolePricingDetails> optionRolePrices = rolePrices
                .getRolePricesForContainerKey(optionKey);
        Assert.assertEquals(1, optionRolePrices.keySet().size());
    }

    @Test
    public void testGetRoleRelatedCostsForOptionsOneOptionRoleUpdated()
            throws Exception {
        initData(2, false);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscription = mgr.find(Subscription.class,
                        subscription.getKey());
                priceModel = unproxyEntity(subscription.getPriceModel());

                pricedOption = mgr.find(PricedOption.class,
                        pricedOption.getKey());

                PricedProductRole pricedProductRole = pricedOption
                        .getRoleSpecificUserPrices().get(0);
                pricedProductRole.setPricePerUser(new BigDecimal(999L));
                return null;
            }
        });

        ParameterOptionRolePricingData optionCosts = runTX(
                new Callable<ParameterOptionRolePricingData>() {
                    @Override
                    public ParameterOptionRolePricingData call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForOptions(
                                priceModel.getKey(),
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertNotNull(optionCosts);
        Assert.assertEquals(1, optionCosts.getPricedParameterKeys().size());
        Long parameterKey = optionCosts.getPricedParameterKeys().iterator()
                .next();
        RolePricingData rolePrices = optionCosts
                .getRolePricingDataForPricedParameterKey(parameterKey);
        Assert.assertEquals(1, rolePrices.getContainerKeys().size());
        Long optionKey = rolePrices.getContainerKeys().iterator().next();
        Map<Long, RolePricingDetails> optionRolePrices = rolePrices
                .getRolePricesForContainerKey(optionKey);
        Assert.assertEquals(2, optionRolePrices.keySet().size());
        List<Long> roleKeys = new ArrayList<>(optionRolePrices.keySet());
        Collections.sort(roleKeys);
        Assert.assertEquals(1, optionRolePrices.get(roleKeys.get(0))
                .getPricedProductRoleHistory().getObjVersion());
        Assert.assertEquals(new BigDecimal(999),
                optionRolePrices.get(roleKeys.get(0)).getPricePerUser());
        Assert.assertEquals(0, optionRolePrices.get(roleKeys.get(1))
                .getPricedProductRoleHistory().getObjVersion());
        Assert.assertEquals(new BigDecimal(46),
                optionRolePrices.get(roleKeys.get(1)).getPricePerUser());
    }

    @Test
    public void testGetRoleRelatedCostsForOptionsMultipleOption()
            throws Exception {
        initData(7, true);
        ParameterOptionRolePricingData optionCosts = runTX(
                new Callable<ParameterOptionRolePricingData>() {
                    @Override
                    public ParameterOptionRolePricingData call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForOptions(
                                subscription.getPriceModel().getKey(),
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertNotNull(optionCosts);
        Assert.assertEquals(1, optionCosts.getPricedParameterKeys().size());
        Long parameterKey = optionCosts.getPricedParameterKeys().iterator()
                .next();
        RolePricingData rolePrices = optionCosts
                .getRolePricingDataForPricedParameterKey(parameterKey);
        Assert.assertEquals(2, rolePrices.getContainerKeys().size());
        Iterator<Long> iterator = rolePrices.getContainerKeys().iterator();
        Long optionKey = iterator.next();
        Map<Long, RolePricingDetails> optionRolePrices = rolePrices
                .getRolePricesForContainerKey(optionKey);
        Assert.assertEquals(7, optionRolePrices.keySet().size());
        optionKey = iterator.next();
        optionRolePrices = rolePrices.getRolePricesForContainerKey(optionKey);
        Assert.assertEquals(7, optionRolePrices.keySet().size());
    }

    @Test
    public void testGetRoleRelatedCostsForOptionsMultipleParamsAndOptions()
            throws Exception {
        initData(7, true);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                PriceModel pm = subscription.getPriceModel();
                ParameterDefinition enumParamDef = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.ENUMERATION,
                                "enumParamDef", ParameterType.SERVICE_PARAMETER,
                                technicalProduct, mgr, null, null, true);

                ParameterOption option = new ParameterOption();
                option.setOptionId("optionId1");
                option.setParameterDefinition(enumParamDef);
                mgr.persist(option);

                ParameterOption option2 = new ParameterOption();
                option2.setOptionId("optionId2");
                option2.setParameterDefinition(enumParamDef);
                mgr.persist(option2);

                ParameterOption option3 = new ParameterOption();
                option3.setOptionId("optionId3");
                option3.setParameterDefinition(enumParamDef);
                mgr.persist(option3);

                List<ParameterOption> paramOptions = new ArrayList<>();
                paramOptions.add(option);
                paramOptions.add(option2);
                paramOptions.add(option3);
                enumParamDef.setOptionList(paramOptions);

                Parameter enumParam = Products.createParameter(enumParamDef,
                        product, mgr);

                PricedParameter pricedEnumParam = new PricedParameter();
                pricedEnumParam.setParameter(enumParam);
                pricedEnumParam.setPriceModel(pm);
                pm.getSelectedParameters().add(pricedEnumParam);

                PricedOption pricedOption2 = new PricedOption();
                pricedOption2.setParameterOptionKey(option.getKey());
                pricedOption2.setPricedParameter(pricedEnumParam);
                pricedOption2.setPricePerSubscription(new BigDecimal(888));
                pricedOption2.setPricePerUser(new BigDecimal(876));
                List<PricedOption> pricedOptions = new ArrayList<>();
                pricedOptions.add(pricedOption2);
                pricedEnumParam.setPricedOptionList(pricedOptions);
                mgr.persist(pricedEnumParam);

                RoleDefinition rd = subscription.getProduct()
                        .getTechnicalProduct().getRoleDefinitions().get(0);

                PricedProductRole pprForOption = new PricedProductRole();
                pprForOption.setPricedOption(pricedOption2);
                pprForOption.setPricePerUser(new BigDecimal(23));
                pprForOption.setRoleDefinition(rd);
                mgr.persist(pprForOption);

                return null;
            }
        });

        ParameterOptionRolePricingData optionCosts = runTX(
                new Callable<ParameterOptionRolePricingData>() {
                    @Override
                    public ParameterOptionRolePricingData call()
                            throws Exception {
                        return bdrs.loadRoleRelatedCostsForOptions(
                                subscription.getPriceModel().getKey(),
                                System.currentTimeMillis() + 5000);
                    }
                });
        Assert.assertNotNull(optionCosts);
        Assert.assertEquals(2, optionCosts.getPricedParameterKeys().size());
        List<Long> paramKeys = new ArrayList<>(
                optionCosts.getPricedParameterKeys());
        Collections.sort(paramKeys);
        Long parameterKey = paramKeys.get(0);
        RolePricingData rolePrices = optionCosts
                .getRolePricingDataForPricedParameterKey(parameterKey);
        Assert.assertEquals(2, rolePrices.getContainerKeys().size());
        Iterator<Long> iterator = rolePrices.getContainerKeys().iterator();
        Long optionKey = iterator.next();
        Map<Long, RolePricingDetails> optionRolePrices = rolePrices
                .getRolePricesForContainerKey(optionKey);
        Assert.assertEquals(7, optionRolePrices.keySet().size());
        optionKey = iterator.next();
        optionRolePrices = rolePrices.getRolePricesForContainerKey(optionKey);
        Assert.assertEquals(7, optionRolePrices.keySet().size());

        rolePrices = optionCosts
                .getRolePricingDataForPricedParameterKey(paramKeys.get(1));
        Assert.assertEquals(1, rolePrices.getContainerKeys().size());
    }

    /**
     * Creates a price model and corresponding objects with as many role
     * definitions as specified.
     * 
     * @param roleCount
     *            The number of roles to be created.
     * @param createSecondPricedParam
     *            Indicates whether to create a second priced param or not.
     */

    private void initData(final int roleCount,
            final boolean createSecondPricedParam) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                if (!isRootDataInitialized) {
                    createOrganizationRoles(mgr);
                    createSupportedCurrencies(mgr);
                    createPaymentTypes(mgr);
                    SupportedCountries.createSomeSupportedCountries(mgr);
                    isRootDataInitialized = true;
                }

                supplierAndProvider = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                technicalProduct = TechnicalProducts.createTechnicalProduct(mgr,
                        supplierAndProvider,
                        "techProdId_" + System.currentTimeMillis(), false,
                        ServiceAccessType.LOGIN);

                ParameterDefinition paramDef = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.STRING,
                                "stringParam", ParameterType.SERVICE_PARAMETER,
                                technicalProduct, mgr, null, null, true);

                ParameterDefinition booleanParamDef = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.BOOLEAN,
                                "booleanParam", ParameterType.SERVICE_PARAMETER,
                                technicalProduct, mgr, null, null, true);

                ParameterDefinition enumParamDef = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.ENUMERATION,
                                "enumParam", ParameterType.SERVICE_PARAMETER,
                                technicalProduct, mgr, null, null, true);

                ParameterOption option = new ParameterOption();
                option.setOptionId("optionId1");
                option.setParameterDefinition(enumParamDef);
                mgr.persist(option);

                ParameterOption option2 = new ParameterOption();
                option2.setOptionId("optionId2");
                option2.setParameterDefinition(enumParamDef);
                mgr.persist(option2);

                List<ParameterOption> paramOptions = new ArrayList<>();
                paramOptions.add(option);
                paramOptions.add(option2);
                enumParamDef.setOptionList(paramOptions);

                for (int i = 0; i < roleCount; i++) {
                    TechnicalProducts.addRoleDefinition("roleDefinition" + i,
                            technicalProduct, mgr);
                }

                product = Products.createProduct(supplierAndProvider,
                        technicalProduct, true, "productId", null, mgr);

                Parameter param = Products.createParameter(paramDef, product,
                        mgr);

                Parameter booleanParam = Products
                        .createParameter(booleanParamDef, product, mgr);

                Parameter enumParam = Products.createParameter(enumParamDef,
                        product, mgr);

                Organization customer = Organizations.createCustomer(mgr,
                        supplierAndProvider);
                subscription = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), product.getProductId(),
                        "subscriptionId", supplierAndProvider);

                PriceModel pm = subscription.getPriceModel();
                PricedParameter pricedParam = new PricedParameter();
                pricedParam.setParameter(param);
                pricedParam.setPriceModel(pm);
                pricedParam.setPricePerSubscription(new BigDecimal(555));
                pricedParam.setPricePerUser(new BigDecimal(543));
                pm.setSelectedParameters(new ArrayList<PricedParameter>());
                pm.getSelectedParameters().add(pricedParam);
                mgr.persist(pricedParam);

                PricedParameter pricedBooleanParam = new PricedParameter();
                pricedBooleanParam.setParameter(booleanParam);
                pricedBooleanParam.setPriceModel(pm);
                pricedBooleanParam.setPricePerSubscription(new BigDecimal(666));
                pricedBooleanParam.setPricePerUser(new BigDecimal(654));
                pm.getSelectedParameters().add(pricedBooleanParam);
                mgr.persist(pricedBooleanParam);

                PricedParameter pricedEnumParam = new PricedParameter();
                pricedEnumParam.setParameter(enumParam);
                pricedEnumParam.setPriceModel(pm);
                pm.getSelectedParameters().add(pricedEnumParam);

                pricedOption = new PricedOption();
                pricedOption.setParameterOptionKey(option.getKey());
                pricedOption.setPricedParameter(pricedEnumParam);
                pricedOption.setPricePerSubscription(new BigDecimal(777));
                pricedOption.setPricePerUser(new BigDecimal(765));
                List<PricedOption> pricedOptions = new ArrayList<>();
                pricedOptions.add(pricedOption);

                PricedOption pricedOption2 = new PricedOption();
                pricedOption2.setParameterOptionKey(option.getKey());
                pricedOption2.setPricedParameter(pricedEnumParam);
                pricedOption2.setPricePerSubscription(new BigDecimal(888));
                pricedOption2.setPricePerUser(new BigDecimal(876));
                pricedOptions.add(pricedOption2);

                pricedEnumParam.setPricedOptionList(pricedOptions);
                mgr.persist(pricedEnumParam);

                // for every role, add a priced product role information to the
                // price model and to the parameter
                for (int i = 0; i < roleCount; i++) {
                    technicalProduct = mgr.find(TechnicalProduct.class,
                            technicalProduct.getKey());
                    RoleDefinition rd = technicalProduct.getRoleDefinitions()
                            .get(i);
                    PricedProductRole ppr = new PricedProductRole();
                    ppr.setPriceModel(subscription.getPriceModel());
                    ppr.setPricePerUser(new BigDecimal(12 * (i + 1)));
                    ppr.setRoleDefinition(rd);
                    mgr.persist(ppr);

                    PricedProductRole pprForParam = new PricedProductRole();
                    pprForParam.setPricedParameter(pricedParam);
                    pprForParam.setPricePerUser(new BigDecimal(17 * (i + 1)));
                    pprForParam.setRoleDefinition(rd);
                    mgr.persist(pprForParam);

                    PricedProductRole pprForOption = new PricedProductRole();
                    pprForOption.setPricedOption(pricedOption);
                    pprForOption.setPricePerUser(new BigDecimal(23 * (i + 1)));
                    pprForOption.setRoleDefinition(rd);
                    mgr.persist(pprForOption);

                    if (createSecondPricedParam) {
                        PricedProductRole pprForBooleanParam = new PricedProductRole();
                        pprForBooleanParam
                                .setPricedParameter(pricedBooleanParam);
                        pprForBooleanParam
                                .setPricePerUser(new BigDecimal(19 * (i + 1)));
                        pprForBooleanParam.setRoleDefinition(rd);
                        mgr.persist(pprForBooleanParam);

                        PricedProductRole pprForOption2 = new PricedProductRole();
                        pprForOption2.setPricedOption(pricedOption2);
                        pprForOption2
                                .setPricePerUser(new BigDecimal(27 * (i + 1)));
                        pprForOption2.setRoleDefinition(rd);
                        mgr.persist(pprForOption2);

                    }
                }

                return null;
            }
        });
    }

}
