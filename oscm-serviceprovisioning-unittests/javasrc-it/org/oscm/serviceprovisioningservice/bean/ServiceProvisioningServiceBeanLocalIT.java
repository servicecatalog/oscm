/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 21.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.accountservice.bean.AccountServiceBean;
import org.oscm.app.control.ApplicationServiceBaseStub;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOService;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.marketplace.bean.MarketplaceServiceBean;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.LdapAccessServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.triggerservice.bean.TriggerQueueServiceBean;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.types.exceptions.InvalidUserSession;

public class ServiceProvisioningServiceBeanLocalIT extends EJBTestBase {

    private DataService dm;

    private PlatformUser user;
    private ServiceProvisioningServiceLocal svcProv;
    private boolean sendNonSuspendingCalled = false;
    private TriggerType type;

    private TriggerDefinition triggerDef;
    private TriggerProcess triggerProc;
    private LocalizerServiceLocal localizer;
    private MarketplaceService mpProv;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                if (user == null) {
                    throw new InvalidUserSession("No user yet.");
                }
                return dm.find(PlatformUser.class, user.getKey());
            }
        });
        dm = container.get(DataService.class);
        container.addBean(new TriggerQueueServiceBean() {
            @Override
            public void sendAllNonSuspendingMessages(
                    List<TriggerMessage> messageData) {
                sendNonSuspendingCalled = true;
                type = messageData.get(0).getTriggerType();
            }

        });
        container.addBean(new CommunicationServiceStub());
        container.addBean(new ApplicationServiceBaseStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new TagServiceBean());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(new PaymentServiceStub());
        container.addBean(new LdapAccessServiceStub());
        container.addBean(mock(LandingpageServiceLocal.class));
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new IdentityServiceStub());
        container.addBean(new AccountServiceBean());
        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new MarketplaceServiceBean());
        svcProv = container.get(ServiceProvisioningServiceLocal.class);
        localizer = container.get(LocalizerServiceLocal.class);
        mpProv = container.get(MarketplaceService.class);
    }

    private void createOrganization(final String userName,
            final OrganizationRoleType... organizationRoleTypes)
            throws Exception {
        user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                createOrganizationRoles(dm);
                createPaymentTypes(dm);
                SupportedCountries.createAllSupportedCountries(dm);
                Organization org = Organizations.createOrganization(dm,
                        organizationRoleTypes);
                if (org.hasRole(OrganizationRoleType.SUPPLIER)) {
                    Marketplaces.ensureMarketplace(org, org.getOrganizationId(),
                            dm);
                }
                PlatformUser user = Organizations.createUserForOrg(dm, org,
                        true, userName);
                return user;
            }
        });
    }

    private void createTriggerDefinition(final TriggerType type,
            final boolean isSuspendable) throws Exception {
        runTX(new Callable<TriggerDefinition>() {
            @Override
            public TriggerDefinition call() throws Exception {
                TriggerDefinition td = new TriggerDefinition();
                td.setOrganization(user.getOrganization());
                td.setTargetType(TriggerTargetType.WEB_SERVICE);
                td.setTarget("some URL");
                td.setType(type);
                td.setSuspendProcess(isSuspendable);
                td.setName("testTrigger");
                dm.persist(td);
                dm.flush();
                triggerDef = td;
                return td;
            }
        });
    }

    private void createTriggerProcess(final boolean persist) throws Exception {
        runTX(new Callable<TriggerProcess>() {
            @Override
            public TriggerProcess call() throws Exception {
                TriggerDefinition td = dm.getReference(TriggerDefinition.class,
                        triggerDef.getKey());

                TriggerProcess tp = new TriggerProcess();
                tp.setTriggerDefinition(td);
                tp.setState(TriggerProcessStatus.INITIAL);
                tp.setUser(user);

                if (persist) {
                    dm.persist(tp);
                    dm.flush();
                }
                triggerProc = tp;
                return tp;
            }
        });
    }

    private void createTriggerProcessParameter(final boolean persist,
            final TriggerProcessParameterName name, final Object value)
            throws Exception {
        TriggerProcess tp = null;
        if (!persist) {
            tp = triggerProc;
        } else {
            tp = dm.getReference(TriggerProcess.class, triggerProc.getKey());
        }
        tp.addTriggerProcessParameter(name, value);
        triggerProc = tp;
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testActivateProductSuspendingTriggerDefNoProduct()
            throws Exception {
        createOrganization("admin");
        container.login(user.getKey(), ROLE_SERVICE_MANAGER);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    createTriggerDefinition(TriggerType.ACTIVATE_SERVICE, true);
                    createTriggerProcess(true);
                    createTriggerProcessParameter(true,
                            TriggerProcessParameterName.PRODUCT,
                            new VOService());
                    triggerProc = dm.getReference(TriggerProcess.class,
                            triggerProc.getKey());
                    svcProv.activateServiceInt(triggerProc);
                    return null;
                }
            });
        } finally {
            Assert.assertFalse(sendNonSuspendingCalled);
        }
    }

    @Test(expected = EJBException.class)
    public void testActivateProductSuspendingTriggerDefNotAuthorized()
            throws Exception {
        createOrganization("admin");
        container.login(user.getKey());
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    createTriggerDefinition(TriggerType.ACTIVATE_SERVICE, true);
                    createTriggerProcess(true);
                    TechnicalProduct techProd = TechnicalProducts
                            .createTechnicalProduct(dm, user.getOrganization(),
                                    "prod", false, ServiceAccessType.DIRECT);
                    Product prod = Products.createProduct(
                            user.getOrganization(), techProd, false, "prod",
                            null, dm);

                    VOService product = ProductAssembler.toVOCustomerProduct(
                            prod, new LocalizerFacade(localizer, "en"));
                    createTriggerProcessParameter(true,
                            TriggerProcessParameterName.PRODUCT, product);
                    triggerProc = dm.getReference(TriggerProcess.class,
                            triggerProc.getKey());
                    svcProv.activateServiceInt(triggerProc);
                    return null;
                }
            });
        } finally {
            Assert.assertFalse(sendNonSuspendingCalled);
        }
    }

    private VOMarketplace createMarketplace(String id) throws Exception {
        Marketplace mp = Marketplaces.createMarketplace(user.getOrganization(),
                id, false, dm);
        Marketplaces.grantPublishing(user.getOrganization(), mp, dm, true);
        return MarketplaceAssembler.toVOMarketplace(mp,
                new LocalizerFacade(localizer, "en"));
    }

    private void createCatalogEntry(VOMarketplace mp, VOService product)
            throws Exception {
        VOCatalogEntry mpcat = new VOCatalogEntry();
        mpcat.setMarketplace(mp);
        List<VOCatalogEntry> mpcatentries = new ArrayList<>();
        mpcatentries.add(mpcat);
        mpProv.publishService(product, mpcatentries);
    }

    @Test
    public void testActivateProductSuspendingTriggerDef() throws Exception {
        createOrganization("admin", OrganizationRoleType.SUPPLIER);
        container.login(user.getKey(), ROLE_SERVICE_MANAGER);
        VOService product = runTX(new Callable<VOService>() {
            @Override
            public VOService call() throws Exception {
                // refresh the user as he left the transaction
                user = dm.getReference(PlatformUser.class, user.getKey());
                createTriggerDefinition(TriggerType.ACTIVATE_SERVICE, true);
                createTriggerProcess(true);
                TechnicalProduct techProd = TechnicalProducts
                        .createTechnicalProduct(dm, user.getOrganization(),
                                "prod", false, ServiceAccessType.DIRECT);
                Product prod = Products.createProduct(user.getOrganization(),
                        techProd, false, "prod", null, dm);
                prod.setStatus(ServiceStatus.INACTIVE);
                VOService product = ProductAssembler.toVOProduct(prod,
                        new LocalizerFacade(localizer, "en"));

                VOMarketplace mp = createMarketplace("FUJITSU");
                createCatalogEntry(mp, product);

                VOCatalogEntry entry = new VOCatalogEntry();
                entry.setMarketplace(mp);
                entry.setVisibleInCatalog(false);
                List<VOCatalogEntry> entries = new ArrayList<>();
                entries.add(entry);

                createTriggerProcessParameter(true,
                        TriggerProcessParameterName.PRODUCT, product);
                createTriggerProcessParameter(true,
                        TriggerProcessParameterName.CATALOG_ENTRIES, entries);

                triggerProc = dm.getReference(TriggerProcess.class,
                        triggerProc.getKey());
                svcProv.activateServiceInt(triggerProc);
                return ProductAssembler.toVOProduct(prod,
                        new LocalizerFacade(localizer, "en"));
            }
        });
        Assert.assertEquals(ServiceStatus.ACTIVE, product.getStatus());
        Assert.assertTrue(sendNonSuspendingCalled);
        Assert.assertEquals(TriggerType.ACTIVATE_SERVICE, type);

        VOCatalogEntry ce1 = mpProv.getMarketplacesForService(product).get(0);
        Assert.assertFalse(ce1.isVisibleInCatalog());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeActivateProductSuspendingTriggerDefNoProduct()
            throws Exception {
        createOrganization("admin");
        container.login(user.getKey(), ROLE_SERVICE_MANAGER);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    createTriggerDefinition(TriggerType.DEACTIVATE_SERVICE,
                            true);
                    createTriggerProcess(true);
                    createTriggerProcessParameter(true,
                            TriggerProcessParameterName.PRODUCT,
                            new VOService());
                    triggerProc = dm.getReference(TriggerProcess.class,
                            triggerProc.getKey());
                    svcProv.deactivateServiceInt(triggerProc);
                    return null;
                }
            });
        } finally {
            Assert.assertFalse(sendNonSuspendingCalled);
        }
    }

    @Test(expected = EJBException.class)
    public void testDeActivateProductSuspendingTriggerDefNotAuthorized()
            throws Exception {
        createOrganization("admin");
        container.login(user.getKey());
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    createTriggerDefinition(TriggerType.ACTIVATE_SERVICE, true);
                    createTriggerProcess(true);
                    TechnicalProduct techProd = TechnicalProducts
                            .createTechnicalProduct(dm, user.getOrganization(),
                                    "prod", false, ServiceAccessType.DIRECT);
                    Product prod = Products.createProduct(
                            user.getOrganization(), techProd, false, "prod",
                            null, dm);

                    VOService product = ProductAssembler.toVOCustomerProduct(
                            prod, new LocalizerFacade(localizer, "en"));
                    createTriggerProcessParameter(true,
                            TriggerProcessParameterName.PRODUCT, product);
                    triggerProc = dm.getReference(TriggerProcess.class,
                            triggerProc.getKey());
                    svcProv.deactivateServiceInt(triggerProc);
                    return null;
                }
            });
        } finally {
            Assert.assertFalse(sendNonSuspendingCalled);
        }
    }

    @Test
    public void testDeActivateProductSuspendingTriggerDef() throws Exception {
        createOrganization("admin", OrganizationRoleType.SUPPLIER);
        container.login(user.getKey(), ROLE_SERVICE_MANAGER);
        final Product product = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                // refresh the user as he left the transaction
                user = dm.getReference(PlatformUser.class, user.getKey());
                createTriggerDefinition(TriggerType.ACTIVATE_SERVICE, true);
                createTriggerProcess(true);
                TechnicalProduct techProd = TechnicalProducts
                        .createTechnicalProduct(dm, user.getOrganization(),
                                "prod", false, ServiceAccessType.DIRECT);
                Product prod = Products.createProduct(user.getOrganization(),
                        techProd, false, "prod", null, dm);
                VOService product = ProductAssembler.toVOCustomerProduct(prod,
                        new LocalizerFacade(localizer, "en"));

                VOMarketplace mp = createMarketplace("FUJITSU");
                createCatalogEntry(mp, product);

                VOCatalogEntry entry = new VOCatalogEntry();
                entry.setMarketplace(mp);
                entry.setVisibleInCatalog(false);
                List<VOCatalogEntry> entries = new ArrayList<>();
                entries.add(entry);

                createTriggerProcessParameter(true,
                        TriggerProcessParameterName.PRODUCT, product);
                createTriggerProcessParameter(true,
                        TriggerProcessParameterName.CATALOG_ENTRIES, entries);

                triggerProc = dm.getReference(TriggerProcess.class,
                        triggerProc.getKey());
                svcProv.deactivateServiceInt(triggerProc);
                return prod;
            }
        });
        Assert.assertEquals(ServiceStatus.INACTIVE, product.getStatus());
        Assert.assertTrue(sendNonSuspendingCalled);
        Assert.assertEquals(TriggerType.DEACTIVATE_SERVICE, type);

        VOService voProduct = runTX(new Callable<VOService>() {
            @Override
            public VOService call() throws Exception {
                return ProductAssembler.toVOCustomerProduct(product,
                        new LocalizerFacade(localizer, "en"));
            }
        });
        VOCatalogEntry ce1 = mpProv.getMarketplacesForService(voProduct).get(0);
        Assert.assertFalse(ce1.isVisibleInCatalog());
    }

}
