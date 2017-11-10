/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 17, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.BillingSharesResults;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author tokoda
 * 
 */
public class PartnerRevenueDaoIT extends EJBTestBase {

    private DataService ds;
    private Organization tpSup;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        createOrg();
    }

    private void createOrg() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tpSup = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                return null;
            }
        });
    }

    @Test
    public void readTemplateServiceIdsForSupplier_serviceIdModified()
            throws Exception {
        // given
        Calendar calender = Calendar.getInstance();
        long moddateCurrent = calender.getTimeInMillis();
        calender.add(Calendar.MONTH, 1);
        final long periodEnd = calender.getTimeInMillis();
        calender.add(Calendar.MONTH, 1);
        long moddateFuture = calender.getTimeInMillis();

        Product productA = createProduct("serviceA", "techServiceA");
        modifyProductId(productA.getKey(), "serviceA2", moddateCurrent);
        modifyProductId(productA.getKey(), "serviceA3", moddateFuture);

        Product productB = createProduct("serviceB", "techServiceB");
        modifyProductId(productB.getKey(), "serviceB2", moddateCurrent);
        modifyProductId(productB.getKey(), "serviceB3", moddateFuture);

        // when
        Map<String, String> serviceIdMap = runTX(
                new Callable<Map<String, String>>() {
                    @Override
                    public Map<String, String> call() throws Exception {
                        return new PartnerRevenueDao(ds)
                                .readTemplateServiceIdsForSupplier(
                                        tpSup.getOrganizationId(), periodEnd);
                    }
                });

        // then
        assertEquals(2, serviceIdMap.size());
        assertEquals("serviceA2",
                serviceIdMap.get(Long.toString(productA.getKey())));
        assertEquals("serviceB2",
                serviceIdMap.get(Long.toString(productB.getKey())));
    }

    @Test
    public void readTemplateServiceIdsForSupplier_deletedProduct()
            throws Exception {
        // given
        Product product = createProduct("serviceA", "techServiceA");
        deleteProduct(product);

        Calendar calender = Calendar.getInstance();
        calender.add(Calendar.MONTH, 1);
        final long periodEnd = calender.getTimeInMillis();

        // when
        Map<String, String> serviceIdMap = runTX(
                new Callable<Map<String, String>>() {
                    @Override
                    public Map<String, String> call() throws Exception {
                        return new PartnerRevenueDao(ds)
                                .readTemplateServiceIdsForSupplier(
                                        tpSup.getOrganizationId(), periodEnd);
                    }
                });

        // then
        assertEquals(1, serviceIdMap.size());
        assertEquals("serviceA",
                serviceIdMap.get(Long.toString(product.getKey())));
    }

    @Test
    public void executePartnerQuery_query_partner() throws Exception {
        createPartnerOrg_Broker();
        Calendar calender = Calendar.getInstance();
        final long periodEnd = calender.getTimeInMillis();

        calender.add(Calendar.MONTH, -1);
        final long periodStart = calender.getTimeInMillis();

        createBillingSharesResult(periodStart, periodEnd);
        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                PartnerRevenueDao dao = new PartnerRevenueDao(ds);
                dao.executePartnerQuery(periodStart, periodEnd);
                // then
                assertEquals(1, dao.getReportData().size());
                assertEquals("DE",
                        dao.getReportData().get(0).getCountryIsoCode());
                return null;
            }
        });
    }

    private Product createProduct(final String productId,
            final String techProductId) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                return Products.createProduct(tpSup.getOrganizationId(),
                        productId, techProductId, ds);
            }
        });
    }

    private void modifyProductId(final long productKey, final String productId,
            final long moddate) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = ds.getReference(Product.class, productKey);
                product.setProductId(productId);
                product.setHistoryModificationTime(Long.valueOf(moddate));
                ds.persist(product);
                return null;
            }
        });
    }

    private void deleteProduct(final Product product) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Products.setStatusForProduct(ds, product,
                        ServiceStatus.DELETED);
                return null;
            }
        });
    }

    private void createPartnerOrg_Broker() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tpSup = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);
                return null;
            }
        });
    }

    private void createBillingSharesResult(final long periodStart,
            final long periodEnd) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BillingSharesResults.createBillingSharesResult(ds,
                        BillingSharesResultType.BROKER, tpSup.getKey(),
                        periodStart, periodEnd);
                return null;
            }
        });
    }

}
