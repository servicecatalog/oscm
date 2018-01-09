/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.domobjects.PublicLandingpage;
import org.oscm.domobjects.LandingpageProduct;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceOperationException.Reason;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTechnicalService;

public class ServiceProvisioningServiceBeanDeleteServiceIT extends
        ServiceProvisioningServiceTestBase {

    @Test
    public void deleteService_Inactive() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOService refProduct = createProduct(techProduct, "product", svcProv);

        Product prod = getDOFromServer(Product.class, refProduct.getKey());

        // delete it
        svcProv.deleteService(refProduct);

        // verify changes on server side
        prod = (Product) refresh(prod);
        Assert.assertEquals("State has not been changed",
                ServiceStatus.DELETED, prod.getStatus());

        // verify behaviour of client side
        VOServiceDetails productDetails = svcProv.getServiceDetails(refProduct);
        Assert.assertNull("Product has not been deleted", productDetails);

        List<VOService> productsAfterDeletion = svcProv.getSuppliedServices();
        for (VOService product : productsAfterDeletion) {
            Assert.assertFalse(
                    "The deleted product must not be found in the list",
                    product.getKey() == refProduct.getKey());
        }
    }

    @Test
    public void deleteService_checkLandingpage() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);

        // create product
        VOService refProduct = createProduct(techProduct, "product", svcProv);
        final Product prod = getDOFromServer(Product.class, refProduct.getKey());

        // publish product to marketplace
        publishToLocalMarketplaceSupplier(refProduct, mpSupplier);

        // add to featured product list of landingpage
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                mpSupplier = (Marketplace) mgr
                        .getReferenceByBusinessKey(mpSupplier);

                PublicLandingpage landingpage = mpSupplier.getPublicLandingpage();
                List<LandingpageProduct> featuredList = landingpage
                        .getLandingpageProducts();

                LandingpageProduct landingpageProduct = new LandingpageProduct();
                landingpageProduct.setPosition(1);
                landingpageProduct.setProduct(prod);
                landingpageProduct.setLandingpage(landingpage);
                featuredList.add(landingpageProduct);
                mgr.persist(mpSupplier);
                return null;
            }
        });

        svcProv.deleteService(refProduct);

        // verify that service was removed from featured list of landingpage
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                mpSupplier = (Marketplace) mgr
                        .getReferenceByBusinessKey(mpSupplier);
                PublicLandingpage landingpage = mpSupplier.getPublicLandingpage();
                assertNotNull(landingpage);

                // verify product must be removed
                assertEquals(0, landingpage.getLandingpageProducts().size());
                return null;
            }
        });
    }

    @Test(expected = ServiceStateException.class)
    public void deleteService_InactiveCustSpecActive() throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails template = createProduct(techProduct, "product",
                svcProv);

        VOOrganization customer = createCustomerOrg();
        VOServiceDetails custSpec = svcProv.savePriceModelForCustomer(template,
                new VOPriceModel(), customer);

        svcProv.activateService(custSpec);
        svcProv.deleteService(template);
    }

    @Test
    public void deleteService_CustomerCopy() throws Exception {
        // given
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails template = createProduct(techProduct, "product",
                svcProv);

        VOOrganization customer = createCustomerOrg();
        VOServiceDetails custSpec = svcProv.savePriceModelForCustomer(template,
                new VOPriceModel(), customer);
        Product custProduct = getDOFromServer(Product.class, custSpec.getKey());
        assertNotNull("Customer product not found", custProduct);

        // when
        svcProv.deleteService(custSpec);

        // then
        custProduct = (Product) refresh(custProduct);
        assertNull("Customer product was not deleted", custProduct);
    }

    private VOOrganization createCustomerOrg() throws Exception {
        VOOrganization customer = runTX(new Callable<VOOrganization>() {
            public VOOrganization call() throws Exception {
                Organization result = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                Organization currentOrg = new Organization();
                currentOrg.setOrganizationId(supplierOrgId);
                currentOrg = (Organization) mgr.find(currentOrg);
                OrganizationReference ref = new OrganizationReference(
                        currentOrg, result,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                mgr.persist(ref);
                return OrganizationAssembler.toVOOrganization(result, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });
        return customer;
    }

    @Test(expected = ServiceStateException.class)
    public void deleteService_Obsolete() throws Exception {
        final VOService refProduct = prepareServiceForDeletion(ServiceStatus.OBSOLETE);
        svcProv.deleteService(refProduct);
    }

    @Test(expected = ServiceStateException.class)
    public void deleteService_Active() throws Exception {
        final VOService refProduct = prepareServiceForDeletion(ServiceStatus.ACTIVE);
        svcProv.deleteService(refProduct);
    }

    @Test(expected = ServiceStateException.class)
    public void deleteService_Deleted() throws Exception {
        final VOService refProduct = prepareServiceForDeletion(ServiceStatus.DELETED);
        svcProv.deleteService(refProduct);
    }

    @Test
    public void deleteService_CatalogEntry() throws Exception {

        final Long createdProductKey = runTX(new Callable<Long>() {
            public Long call() throws Exception {
                container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
                VOService refProduct = createProduct(techProduct, "product1",
                        svcProv);

                Product prod = mgr.find(Product.class, refProduct.getKey());

                assertEquals("New catalog entry expected", 1, prod
                        .getCatalogEntries().size());
                // delete it
                svcProv.deleteService(refProduct);

                return Long.valueOf(refProduct.getKey());
            }
        });

        // verify changes on server side
        runTX(new Callable<Long>() {
            public Long call() {
                Product prod = mgr.find(Product.class,
                        createdProductKey.longValue());
                assertEquals("State has not been changed",
                        ServiceStatus.DELETED, prod.getStatus());

                assertEquals("One catalog entry is expected", 1, prod
                        .getCatalogEntries().size());
                return null;
            }
        });
    }

    @Test(expected = ServiceOperationException.class)
    public void deleteService_OfSubscription() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        product = svcProv.getServiceForSubscription(customer, subId);
        svcProv.deleteService(product);
    }

    @Test
    public void deleteService_Suspended() throws Exception {
        final VOService refProduct = prepareServiceForDeletion(ServiceStatus.SUSPENDED);
        svcProv.deleteService(refProduct);
        // verify behaviour of client side
        VOServiceDetails productDetails = svcProv.getServiceDetails(refProduct);
        Assert.assertNull("Product has not been deleted", productDetails);
        List<VOService> productsAfterDeletion = svcProv.getSuppliedServices();
        for (VOService product : productsAfterDeletion) {
            Assert.assertFalse(
                    "The deleted product must not be found in the list",
                    product.getKey() == refProduct.getKey());
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteService_WrongSupplier() throws Exception {
        // given
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOService refProduct = createProduct(techProduct, "product", svcProv);

        container.logout();
        container.login(providerUserKey, ROLE_SERVICE_MANAGER);

        // when
        svcProv.deleteService(refProduct);
    }

    @Test
    public void deleteService_ExistingResalePermission() throws Exception {
        // given
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails template = createProduct(techProduct, "product",
                svcProv);
        createResaleCopy(template.getKey(), ServiceStatus.INACTIVE);

        try {
            // when
            svcProv.deleteService(template);
            fail("ServiceOperationException expected");
        } catch (ServiceOperationException soe) {
            // then
            assertTrue(
                    "Wrong exception reason",
                    soe.getMessageKey().contains(
                            Reason.DELETION_FAILED_EXISTING_RESALE_PERMISSION
                                    .toString()));
        }
    }

    @Test
    public void deleteService_DeletedResalePermission() throws Exception {
        // given
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        VOServiceDetails template = createProduct(techProduct, "product",
                svcProv);
        Product productTemplate = getDOFromServer(Product.class,
                template.getKey());
        createResaleCopy(template.getKey(), ServiceStatus.DELETED);

        // when
        svcProv.deleteService(template);

        // then
        productTemplate = (Product) refresh(productTemplate);
        Assert.assertEquals("State has not been changed",
                ServiceStatus.DELETED, productTemplate.getStatus());
    }

    private Product createResaleCopy(long templateKey,
            final ServiceStatus copyStatus) throws Exception {
        final Product template = getDOFromServer(Product.class, templateKey);

        return runTX(new Callable<Product>() {
            public Product call() throws Exception {
                Organization broker = Organizations.createOrganization(mgr,
                        OrganizationRoleType.BROKER);
                Product resaleCopy = Products.createProductResaleCopy(template,
                        broker, mgr);
                resaleCopy.setStatus(copyStatus);
                return resaleCopy;
            }
        });
    }
}
