/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Unit tests for {@link ProviderSupplierDao} using the test EJB container.
 * 
 * @author zankov
 */
public class ProviderSupplierDaoIT extends EJBTestBase {

    private DataService ds;
    private ProviderSupplierDao dao;
    private Organization supplier;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new ProviderSupplierDao(ds);

        supplier = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
    }

    @Test
    public void retrieveProviderInstanceReportData() throws Exception {
        // given
        final Organization supplierCustomer = registerCustomer(
                "supplierCustomer", supplier);

        TechnicalProduct techProdStandardParams = createTechProduct(
                "techServiceWithParams", supplier, ServiceAccessType.LOGIN,
                true);

        Product product = createProduct("productWithParams",
                techProdStandardParams, supplier);

        createSubscription(supplierCustomer.getOrganizationId(),
                product.getProductId(), "sub", supplier);

        // when
        List<ReportResultData> result = runTX(
                new Callable<List<ReportResultData>>() {
                    @Override
                    public List<ReportResultData> call() throws Exception {
                        return dao.retrieveProviderInstanceReportData(
                                supplier.getOrganizationId());
                    }
                });

        // then
        assertEquals(1, result.size());
        assertEquals("techServiceWithParams",
                result.get(0).getColumnValue().get(0));
        assertEquals("productWithParams",
                ((String) result.get(0).getColumnValue().get(1)).split("#")[0]);
        assertEquals("productWithParams",
                ((String) result.get(0).getColumnValue().get(2)).split("#")[0]);
        assertEquals("stringParam", result.get(0).getColumnValue().get(3));
        assertEquals("123", result.get(0).getColumnValue().get(4));
    }

    @Test
    public void retrieveProviderInstanceReportData_WithoutParameter()
            throws Exception {
        // given
        final Organization supplierCustomer = registerCustomer(
                "supplierCustomer", supplier);

        TechnicalProduct techProdWithoutParams = createTechProduct(
                "techServiceNoParams", supplier, ServiceAccessType.LOGIN,
                false);

        Product product = createProduct("productWithoutParams",
                techProdWithoutParams, supplier);

        createSubscription(supplierCustomer.getOrganizationId(),
                product.getProductId(), "subNoParams", supplier);

        // when
        List<ReportResultData> result = runTX(
                new Callable<List<ReportResultData>>() {
                    @Override
                    public List<ReportResultData> call() throws Exception {
                        return dao.retrieveProviderInstanceReportData(
                                supplier.getOrganizationId());
                    }
                });

        // then
        assertEquals(1, result.size());
        assertEquals("techServiceNoParams",
                result.get(0).getColumnValue().get(0));
        assertEquals("productWithoutParams",
                ((String) result.get(0).getColumnValue().get(1)).split("#")[0]);
        assertEquals("productWithoutParams",
                ((String) result.get(0).getColumnValue().get(2)).split("#")[0]);
        assertNull(result.get(0).getColumnValue().get(3));
        assertNull(result.get(0).getColumnValue().get(4));
    }

    protected TechnicalProduct createTechProduct(final String techProductId,
            final Organization organization, final ServiceAccessType type,
            final boolean withParams) throws Exception {
        return runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                TechnicalProduct result = TechnicalProducts
                        .createTechnicalProduct(ds, organization, techProductId,
                                false, type);

                if (withParams) {
                    TechnicalProducts.addParameterDefinition(
                            ParameterValueType.STRING, "stringParam",
                            ParameterType.SERVICE_PARAMETER, result, ds, null,
                            null, true);
                }

                return result;
            }
        });
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
            final TechnicalProduct techProduct, final Organization organization)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product result = Products.createProduct(organization,
                        techProduct, false, productId, null, ds);
                for (ParameterDefinition paramDef : techProduct
                        .getParameterDefinitions()) {
                    Products.createParameter(paramDef, result, ds);
                }

                return result;
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
}
