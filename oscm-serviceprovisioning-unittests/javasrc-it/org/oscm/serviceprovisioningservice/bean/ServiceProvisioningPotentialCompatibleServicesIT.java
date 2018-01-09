/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 07.12.2011                                                      
 *                                                                              
 *  Completion Time: 07.12.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductReference;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOCompatibleService;
import org.oscm.internal.vo.VOService;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author weiser
 * 
 */
@SuppressWarnings("boxing")
public class ServiceProvisioningPotentialCompatibleServicesIT
        extends EJBTestBase {

    private DataService ds;
    private ServiceProvisioningService sps;

    private Product[] products;
    private Product custSpec;

    private VOService productVO;
    private VOService custSpecVO;
    private VOService prodNoMPVO;

    private long userKey;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ServiceProvisioningServiceBean());

        ds = container.get(DataService.class);
        sps = container.get(ServiceProvisioningService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(ds);
                createOrganizationRoles(ds);
                createSupportedCurrencies(ds);
                SupportedCountries.createSomeSupportedCountries(ds);
                return null;
            }
        });

        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization s = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                PlatformUser u = Organizations.createUserForOrg(ds, s, true,
                        "admin");
                PlatformUsers.grantRoles(ds, u, UserRoleType.SERVICE_MANAGER);

                Marketplace mp = Marketplaces.ensureMarketplace(s, "local", ds);

                TechnicalProduct tp1 = TechnicalProducts.createTechnicalProduct(
                        ds, s, "tp1", false, ServiceAccessType.LOGIN);

                products = new Product[4];
                products[0] = Products.createProduct(s, tp1, true, "p1", "pm1",
                        mp, ds);
                LocalizerFacade facade = mock(LocalizerFacade.class);
                productVO = ProductAssembler.toVOProduct(products[0], facade);
                custSpec = Products.createCustomerSpecifcProduct(ds, s,
                        products[0], ServiceStatus.ACTIVE);
                custSpecVO = ProductAssembler.toVOProduct(custSpec, facade);
                products[1] = Products.createProduct(s, tp1, true, "p2", "pm1",
                        mp, ds);
                products[2] = Products.createProduct(s, tp1, true, "p3", "pm1",
                        ds);
                prodNoMPVO = ProductAssembler.toVOProduct(products[2], facade);
                products[3] = Products.createProduct(s, tp1, false, "p4", "pm1",
                        mp, ds);

                TechnicalProduct tp2 = TechnicalProducts.createTechnicalProduct(
                        ds, s, "tp2", false, ServiceAccessType.LOGIN);
                Products.createProduct(s, tp2, false, "notVisible",
                        "notVisible", ds);
                return u;
            }
        });
        userKey = user.getKey();
    }

    @Test(expected = EJBAccessException.class)
    public void getPotentialCompatibleServices_MarketplaceOwner()
            throws Exception {
        container.login(userKey, UserRoleType.MARKETPLACE_OWNER.name());
        try {
            sps.getPotentialCompatibleServices(null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void getPotentialCompatibleServices_OrganizationAdmin()
            throws Exception {
        container.login(userKey, UserRoleType.ORGANIZATION_ADMIN.name());
        try {
            sps.getPotentialCompatibleServices(null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void getPotentialCompatibleServices_PlatformOperator()
            throws Exception {
        container.login(userKey, UserRoleType.PLATFORM_OPERATOR.name());
        try {
            sps.getPotentialCompatibleServices(null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void getPotentialCompatibleServices_TechnologyManager()
            throws Exception {
        container.login(userKey, UserRoleType.TECHNOLOGY_MANAGER.name());
        try {
            sps.getPotentialCompatibleServices(null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getPotentialCompatibleServices_NotFound() throws Exception {
        container.login(userKey, UserRoleType.SERVICE_MANAGER.name());
        productVO.setKey(0);
        sps.getPotentialCompatibleServices(productVO);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getPotentialCompatibleServices_NotOwned() throws Exception {
        PlatformUser user = runTX(new Callable<PlatformUser>() {

            @Override
            public PlatformUser call() throws Exception {
                Organization s = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser u = Organizations.createUserForOrg(ds, s, true,
                        "admin");
                return u;
            }
        });
        container.login(user.getKey(), UserRoleType.SERVICE_MANAGER.name());
        sps.getPotentialCompatibleServices(productVO);
    }

    @Test
    public void getPotentialCompatibleServices() throws Exception {
        container.login(userKey, UserRoleType.SERVICE_MANAGER.name());
        List<VOCompatibleService> list = sps
                .getPotentialCompatibleServices(productVO);
        validateResult(list, false);
    }

    /**
     * Bug 9850 - if the service is not yet published to a MP, no exception must
     * occur and the result list must be empty.
     */
    @Test
    public void getPotentialCompatibleServices_NoMp() throws Exception {
        container.login(userKey, UserRoleType.SERVICE_MANAGER.name());
        List<VOCompatibleService> list = sps
                .getPotentialCompatibleServices(prodNoMPVO);
        assertEquals(0, list.size());
    }

    @Test
    public void getPotentialCompatibleServices_CustSpec() throws Exception {
        container.login(userKey, UserRoleType.SERVICE_MANAGER.name());
        List<VOCompatibleService> list = sps
                .getPotentialCompatibleServices(custSpecVO);
        validateResult(list, false);
    }

    @Test
    public void getPotentialCompatibleServices_DifferentCurrency()
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = SupportedCurrencies.findOrCreate(ds,
                        "USD");
                Product p = ds.getReference(Product.class,
                        products[1].getKey());
                p.getPriceModel().setCurrency(sc);
                return null;
            }
        });
        validateSingleResult();
    }

    @Test
    public void getPotentialCompatibleServices_DifferentMarketplace()
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product p = ds.getReference(Product.class,
                        products[1].getKey());
                Marketplace mp = Marketplaces
                        .createGlobalMarketplace(p.getVendor(), "global", ds);
                p.getCatalogEntries().get(0).setMarketplace(mp);
                return null;
            }
        });
        validateSingleResult();
    }

    @Test
    public void getPotentialCompatibleServices_TargetDeleted()
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product p = ds.getReference(Product.class,
                        products[1].getKey());
                p.setStatus(ServiceStatus.DELETED);
                return null;
            }
        });
        validateSingleResult();
    }

    @Test
    public void getPotentialCompatibleServices_TargetObsolete()
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product p = ds.getReference(Product.class,
                        products[1].getKey());
                p.setStatus(ServiceStatus.OBSOLETE);
                return null;
            }
        });
        validateSingleResult();
    }

    @Test
    public void getPotentialCompatibleServices_CompatibleSet()
            throws Exception {
        setCompatible();
        container.login(userKey, UserRoleType.SERVICE_MANAGER.name());
        List<VOCompatibleService> list = sps
                .getPotentialCompatibleServices(productVO);
        validateResult(list, true);
    }

    @Test
    public void getPotentialCompatibleServices_CustSpec_CompatibleSet()
            throws Exception {
        setCompatible();
        container.login(userKey, UserRoleType.SERVICE_MANAGER.name());
        List<VOCompatibleService> list = sps
                .getPotentialCompatibleServices(custSpecVO);
        validateResult(list, true);
    }

    protected void validateResult(List<VOCompatibleService> list,
            boolean compatible) {
        assertNotNull(list);
        Set<Long> keys = new HashSet<>();
        // 0 is the passed one and must not be returned; 2 has no marketplace
        // assigned
        keys.add(Long.valueOf(products[1].getKey()));
        keys.add(Long.valueOf(products[3].getKey()));
        assertEquals(2, list.size());
        for (VOCompatibleService s : list) {
            assertEquals(compatible, s.isCompatible());
            assertTrue(keys.remove(Long.valueOf(s.getKey())));
        }
    }

    protected void setCompatible() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product s = ds.getReference(Product.class,
                        products[0].getKey());
                for (int i = 1; i < products.length; i++) {
                    Product p = ds.getReference(Product.class,
                            products[i].getKey());
                    ProductReference ref = new ProductReference(s, p);
                    ds.persist(ref);
                }
                return null;
            }
        });
    }

    protected void validateSingleResult()
            throws ObjectNotFoundException, OperationNotPermittedException {
        container.login(userKey, UserRoleType.SERVICE_MANAGER.name());
        List<VOCompatibleService> list = sps
                .getPotentialCompatibleServices(custSpecVO);
        assertNotNull(list);
        assertEquals(1, list.size());

        VOCompatibleService s = list.get(0);
        assertFalse(s.isCompatible());
        assertEquals(products[3].getKey(), s.getKey());
    }

}
