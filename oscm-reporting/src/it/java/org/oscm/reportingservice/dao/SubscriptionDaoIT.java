/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Unit tests for {@link SubscriptionDao} using the test EJB container.
 * 
 * @author barzu
 */
public class SubscriptionDaoIT extends EJBTestBase {

    private DataService ds;
    private SubscriptionDao dao;

    private Organization supplier;
    private Subscription subscription;
    private final String NON_EXISTING_SUPPLIER_ID = "NonExistingSupplier";

    @Override
    protected void setup(TestContainer container) throws Exception {

        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new SubscriptionDao(ds);

        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        final Organization supplierCustomer = registerCustomer(
                "supplierCustomer", supplier);

        Product product = createProduct("serviceB", "techServiceB",
                supplier.getOrganizationId(), ServiceAccessType.LOGIN);

        subscription = createSubscription(supplierCustomer.getOrganizationId(),
                product.getProductId(), "sub1", supplier);

        final Organization broker = createOrg("broker",
                OrganizationRoleType.BROKER);
        final Organization brokerCustomer1 = registerCustomer("brokerCustomer1",
                broker);
        registerCustomer("brokerCustomer2", broker);
        Product partnerProduct = createPartnerProduct(product, broker);
        createPartnerSubscription(brokerCustomer1.getOrganizationId(),
                partnerProduct, "brokercustomer1Sub", broker);

        final Organization reseller = createOrg("reseller",
                OrganizationRoleType.RESELLER);
        final Organization resellerCustomer1 = registerCustomer(
                "resellerCustomer1", reseller);
        registerCustomer("resellerCustomer2", reseller);
        Product partnerProductReseller = createPartnerProduct(product,
                reseller);
        createPartnerSubscription(resellerCustomer1.getOrganizationId(),
                partnerProductReseller, "resellercustomer1Sub", reseller);
    }

    @Test
    public void retrieveSupplierCustomerReportData() throws Exception {
        // given

        // when
        List<ReportResultData> result = runTX(
                new Callable<List<ReportResultData>>() {
                    @Override
                    public List<ReportResultData> call() throws Exception {
                        return dao.retrieveSupplierCustomerReportData(
                                supplier.getOrganizationId());
                    }
                });

        // then
        assertEquals(2, result.size());
        assertEquals("brokercustomer1Sub",
                result.get(0).getColumnValue().get(0));
        assertEquals("brokerCustomer1", result.get(0).getColumnValue().get(3));
        assertEquals("sub1", result.get(1).getColumnValue().get(0));
        assertEquals("supplierCustomer", result.get(1).getColumnValue().get(3));
    }

    @Test
    public void retrieveSupplierCustomerReportData_teminateSubscription()
            throws Exception {
        // given
        teminateSubscription(subscription);
        Map<String, String> lastSubIdMap = retrieveLastValidSubscriptionIdMap();
        // when
        List<ReportResultData> result = runTX(
                new Callable<List<ReportResultData>>() {
                    @Override
                    public List<ReportResultData> call() throws Exception {
                        return dao.retrieveSupplierCustomerReportData(
                                supplier.getOrganizationId());
                    }
                });

        // then
        assertEquals(2, result.size());

        assertEquals("brokercustomer1Sub",
                result.get(0).getColumnValue().get(0));
        assertEquals("ACTIVE", result.get(0).getColumnValue().get(1));
        assertEquals("brokerCustomer1", result.get(0).getColumnValue().get(3));
        assertEquals("sub1",
                lastSubIdMap.get(result.get(1).getColumnValue().get(0)));
        assertEquals("DEACTIVATED", result.get(1).getColumnValue().get(1));
        assertEquals("supplierCustomer", result.get(1).getColumnValue().get(3));
    }

    @Test
    public void retrieveSupplierCustomerReportOfASupplierData_nonExistingSupplierId()
            throws Exception {
        // when
        List<ReportResultData> reportData = retrieveSupplierCustomerReportOfASupplierData(
                NON_EXISTING_SUPPLIER_ID);

        // then
        assertTrue(reportData.isEmpty());
    }

    private List<ReportResultData> retrieveSupplierCustomerReportOfASupplierData(
            final String supplierOrgId) throws Exception {
        return runTX(new Callable<List<ReportResultData>>() {
            @Override
            public List<ReportResultData> call() throws Exception {
                // when
                return dao.retrieveSupplierCustomerReportOfASupplierData(
                        supplierOrgId);
            }
        });
    }

    @Test
    public void retrieveSupplierCustomerReportOfASupplierData()
            throws Exception {
        // when
        List<ReportResultData> result = retrieveSupplierCustomerReportOfASupplierData(
                supplier.getOrganizationId());

        // then
        assertEquals(2, result.size());
        assertEquals("brokercustomer1Sub",
                result.get(0).getColumnValue().get(0));
        assertEquals("ACTIVE", result.get(0).getColumnValue().get(1));
        assertEquals("brokerCustomer1", result.get(0).getColumnValue().get(2));
        assertTrue(result.get(0).getColumnValue().get(3).toString()
                .startsWith("serviceB"));
        assertEquals("sub1", result.get(1).getColumnValue().get(0));
        assertEquals("ACTIVE", result.get(1).getColumnValue().get(1));
        assertEquals("supplierCustomer", result.get(1).getColumnValue().get(2));
        assertTrue(result.get(1).getColumnValue().get(3).toString()
                .startsWith("serviceB"));
    }

    @Test
    public void retrieveSupplierCustomerReportOfASupplierData_teminateSubscription()
            throws Exception {
        // given
        teminateSubscription(subscription);
        // when
        List<ReportResultData> result = retrieveSupplierCustomerReportOfASupplierData(
                supplier.getOrganizationId());
        Map<String, String> lastSubIdMap = retrieveLastValidSubscriptionIdMap();

        // then
        assertEquals(2, result.size());
        assertEquals("brokercustomer1Sub",
                result.get(0).getColumnValue().get(0));
        assertEquals("ACTIVE", result.get(0).getColumnValue().get(1));
        assertEquals("brokerCustomer1", result.get(0).getColumnValue().get(2));
        assertTrue(result.get(0).getColumnValue().get(3).toString()
                .startsWith("serviceB"));
        assertEquals("sub1",
                lastSubIdMap.get(result.get(1).getColumnValue().get(0)));
        assertEquals("DEACTIVATED", result.get(1).getColumnValue().get(1));
        assertEquals("supplierCustomer", result.get(1).getColumnValue().get(2));
        assertTrue(result.get(1).getColumnValue().get(3).toString()
                .startsWith("serviceB"));
    }

    private List<ReportResultData> retrieveSupplierProductReportData(
            final String supplierOrgId) throws Exception {
        return runTX(new Callable<List<ReportResultData>>() {
            @Override
            public List<ReportResultData> call() throws Exception {
                // when
                return dao.retrieveSupplierProductReportData(supplierOrgId);
            }
        });
    }

    @Test
    public void retrieveSupplierProductReportData() throws Exception {
        // when
        List<ReportResultData> result = retrieveSupplierProductReportData(
                supplier.getOrganizationId());

        // then
        assertEquals(2, result.size());
        assertTrue(result.get(0).getColumnValue().get(3).toString()
                .startsWith("serviceB"));
        assertTrue(result.get(1).getColumnValue().get(3).toString()
                .startsWith("serviceB"));

        List<String> subIds = new ArrayList<>();
        subIds.add(result.get(0).getColumnValue().get(0).toString());
        subIds.add(result.get(1).getColumnValue().get(0).toString());

        assertTrue(subIds.contains("brokercustomer1Sub"));
        assertTrue(subIds.contains("sub1"));
    }

    @Test
    public void retrieveSupplierProductReportData_teminateSubscription()
            throws Exception {
        // given
        teminateSubscription(subscription);
        Map<String, String> lastSubIdMap = retrieveLastValidSubscriptionIdMap();
        // when
        List<ReportResultData> result = retrieveSupplierProductReportData(
                supplier.getOrganizationId());

        // then
        assertEquals(2, result.size());
        assertTrue(result.get(0).getColumnValue().get(3).toString()
                .startsWith("serviceB"));
        assertTrue(result.get(1).getColumnValue().get(3).toString()
                .startsWith("serviceB"));

        List<String> subIds = new ArrayList<>();
        subIds.add(lastSubIdMap
                .get(result.get(0).getColumnValue().get(0).toString()));
        subIds.add(lastSubIdMap
                .get(result.get(1).getColumnValue().get(0).toString()));

        assertTrue(subIds.contains("brokercustomer1Sub"));
        assertTrue(subIds.contains("sub1"));
    }

    @Test
    public void retrieveSupplierProductReportData_nonExistingSupplierId()
            throws Exception {
        // when
        List<ReportResultData> reportData = retrieveSupplierProductReportData(
                NON_EXISTING_SUPPLIER_ID);

        // then
        assertTrue(reportData.isEmpty());
    }

    private Organization createOrg(final String organizationId,
            final OrganizationRoleType... roles) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, organizationId,
                        roles);
            }
        });
    }

    private Organization registerCustomer(final String customerId,
            final Organization vendor) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createCustomer(ds, vendor, customerId,
                        false);
            }
        });
    }

    private Product createProduct(final String productId,
            final String techProductId, final String organizationId,
            final ServiceAccessType type) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                return Products.createProduct(organizationId, productId,
                        techProductId, ds, type);
            }
        });
    }

    private Product createPartnerProduct(final Product productTemplate,
            final Organization partner) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product resaleCopy = productTemplate.copyForResale(partner);
                ds.persist(resaleCopy);
                return resaleCopy;
            }
        });
    }

    private Subscription createSubscription(final String customerId,
            final String productId, final String subscriptionId,
            final Organization supplier) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createSubscription(ds, customerId,
                        productId, subscriptionId, supplier);
            }
        });
    }

    /**
     * teminate the subscription
     */
    private void teminateSubscription(final Subscription subscription)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscriptions.teminateSubscription(ds, subscription);
                return null;
            }
        });
    }

    private Subscription createPartnerSubscription(final String customerId,
            final Product product, final String subscriptionId,
            final Organization partner) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return Subscriptions.createPartnerSubscription(ds, customerId,
                        product.getProductId(), subscriptionId, partner);
            }
        });
    }

    /**
     * @return Map the subscriptionId with the latest valid one
     */
    private Map<String, String> retrieveLastValidSubscriptionIdMap()
            throws Exception {
        return runTX(new Callable<Map<String, String>>() {
            @Override
            public Map<String, String> call() throws Exception {
                return dao.retrieveLastValidSubscriptionIdMap();
            }
        });
    }

}
