/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;

public class ServiceProvisioningServiceBeanCopyDefaultPaymentIT extends
        EJBTestBase {
    private static final String SUPPLIER_ID = "MySupplier";
    private static final String RESELLER_ID = "MyReseller";
    private static final String TECHPRODUCT_ID = "testTechnicalProduct";
    private static final String PRODUCT_ID = "testProduct";

    private DataService ds;
    private ServiceProvisioningServiceLocal spsl;

    private Organization supplier;
    private Organization platformOperator;
    private Organization reseller;
    private Product productTemplate;
    private Product resaleCopy;
    private List<PaymentType> paymentTypes;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(new ServiceProvisioningServiceBean());

        ds = container.get(DataService.class);
        spsl = container.get(ServiceProvisioningServiceLocal.class);

        createPaymentTypes();
        createOrganizations();
        createProducts();
    }

    private void createPaymentTypes() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                paymentTypes = createPaymentTypes(ds);

                return null;
            }
        });
    }

    private void createOrganizations() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                platformOperator = Organizations.createOrganization(ds,
                        OrganizationRoleType.PLATFORM_OPERATOR.toString(),
                        OrganizationRoleType.PLATFORM_OPERATOR);

                supplier = Organizations.createOrganization(ds, SUPPLIER_ID,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);

                reseller = Organizations.createOrganization(ds, RESELLER_ID,
                        OrganizationRoleType.RESELLER);

                return null;
            }
        });
    }

    private void createProducts() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // Create technical product and product without price model
                productTemplate = Products.createProduct(
                        supplier.getOrganizationId(), PRODUCT_ID,
                        TECHPRODUCT_ID, ds, ServiceAccessType.LOGIN);

                resaleCopy = Products.createProductResaleCopy(productTemplate,
                        reseller, ds);

                return null;
            }
        });
    }

    private void createDefaultPaymentTypes(final long vendorKey,
            final OrganizationReferenceType orgRefType,
            final OrganizationRoleType orgRoleType) throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                platformOperator = ds.getReference(Organization.class,
                        platformOperator.getKey());
                Organization vendor = ds.getReference(Organization.class,
                        vendorKey);

                OrganizationReference ref = new OrganizationReference(
                        platformOperator, vendor, orgRefType);
                ds.persist(ref);

                OrganizationRole role = OrganizationRole.class.cast(ds
                        .getReferenceByBusinessKey(new OrganizationRole(
                                orgRoleType)));

                for (PaymentType pt : paymentTypes) {
                    PaymentType paymentType = ds.getReference(
                            PaymentType.class, pt.getKey());

                    OrganizationRefToPaymentType orgRefToPT = new OrganizationRefToPaymentType();
                    orgRefToPT.setOrganizationReference(ref);
                    orgRefToPT.setOrganizationRole(role);
                    orgRefToPT.setPaymentType(paymentType);
                    orgRefToPT.setUsedAsServiceDefault(true);
                    ds.persist(orgRefToPT);

                    ref.getPaymentTypes().add(orgRefToPT);
                }

                return null;
            }
        });
    }

    @Test
    public void copyDefaultPaymentEnablement_Supplier() throws Exception {
        // given
        createDefaultPaymentTypes(supplier.getKey(),
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER,
                OrganizationRoleType.SUPPLIER);

        // when, then
        copyDefaultPaymentEnablement(supplier.getKey(),
                productTemplate.getKey());
    }

    @Test
    public void copyDefaultPaymentEnablement_Reseller() throws Exception {
        // given
        createDefaultPaymentTypes(reseller.getKey(),
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_RESELLER,
                OrganizationRoleType.RESELLER);

        // when, then
        copyDefaultPaymentEnablement(reseller.getKey(), resaleCopy.getKey());
    }

    private void copyDefaultPaymentEnablement(final long vendorKey,
            final long productKey) throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization vendor = Organization.class.cast(ds.find(
                        Organization.class, vendorKey));
                Product product = Product.class.cast(ds.find(Product.class,
                        productKey));

                // when
                spsl.copyDefaultPaymentEnablement(product, vendor);

                // then
                for (PaymentType pt : paymentTypes) {
                    assertNotNull("",
                            ds.find(new ProductToPaymentType(product, pt)));
                }

                return null;
            }
        });
    }
}
