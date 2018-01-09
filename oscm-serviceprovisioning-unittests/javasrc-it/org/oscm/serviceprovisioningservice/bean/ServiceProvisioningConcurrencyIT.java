/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 07.10.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.MarketingPermission;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.assembler.TechnicalProductAssembler;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Scenario;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.ImageResourceServiceStub;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSteppedPrice;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * Tests for concurrency checks for price model modifications.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ServiceProvisioningConcurrencyIT extends EJBTestBase {

    private ServiceProvisioningService svcProv;
    private DataService dm;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);

        // container init
        container.addBean(new DataServiceBean());
        container.addBean(new SessionServiceStub());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new ImageResourceServiceStub() {

            @Override
            public ImageResource read(long objectKey, ImageType imageType) {
                return null;
            }
        });
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new TagServiceBean());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());

        // data setup
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Scenario.setup(container, true);
                return null;
            }
        });

        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_SERVICE_MANAGER);

        // retrieve required resources
        svcProv = container.get(ServiceProvisioningService.class);
        dm = container.get(DataService.class);
    }

    private void createMarketingPermission(final long tpKey,
            final long orgRefKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct technicalProduct = dm.find(
                        TechnicalProduct.class, tpKey);
                OrganizationReference reference = dm.find(
                        OrganizationReference.class, orgRefKey);

                MarketingPermission permission = new MarketingPermission();
                permission.setOrganizationReference(reference);
                permission.setTechnicalProduct(technicalProduct);
                dm.persist(permission);
                return null;
            }
        });
    }

    private OrganizationReference createOrgRef(final long orgKey)
            throws Exception {
        return runTX(new Callable<OrganizationReference>() {
            @Override
            public OrganizationReference call() throws Exception {
                Organization organization = dm.find(Organization.class, orgKey);
                OrganizationReference orgRef = new OrganizationReference(
                        organization,
                        organization,
                        OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
                dm.persist(orgRef);
                return orgRef;
            }
        });
    }

    // ****************************************************************************
    // test methods

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelConcurrent() throws Exception {
        VOServiceDetails serviceDetails = getService();

        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        int version = retrievedPriceModel.getVersion();

        retrievedPriceModel.setFreePeriod(2);
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
        retrievedPriceModel.setFreePeriod(4);
        retrievedPriceModel.setVersion(version);
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelChangesOnPMAttributes() throws Exception {
        VOServiceDetails serviceDetails = getService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        retrievedPriceModel.setOneTimeFee(BigDecimal.valueOf(999999L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelChangesOnPricedEvent() throws Exception {
        VOServiceDetails serviceDetails = getService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedEvent voPricedEvent = getPricedEvent(retrievedPriceModel);
        voPricedEvent.setEventPrice(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getService().getPriceModel();
        getPricedEvent(retrievedPriceModel).setVersion(
                voPricedEvent.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelChangesOnPricedParameter() throws Exception {
        VOServiceDetails serviceDetails = getService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedParameter voPricedParameter = getPricedParameter(retrievedPriceModel);
        voPricedParameter.setPricePerUser(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getService().getPriceModel();
        getPricedParameter(retrievedPriceModel).setVersion(
                voPricedParameter.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelChangesOnPricedOption() throws Exception {
        VOServiceDetails serviceDetails = getService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedOption voPricedOption = getPricedOption(retrievedPriceModel);
        voPricedOption.setPricePerUser(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getService().getPriceModel();
        getPricedOption(retrievedPriceModel).setVersion(
                voPricedOption.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelChangesOnSteppedPriceForPMBaseValues()
            throws Exception {
        addSteppedPrices(getService());
        VOServiceDetails serviceDetails = getService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOSteppedPrice voSteppedPrice = getSteppedPricesForPriceModel(retrievedPriceModel);
        voSteppedPrice.setPrice(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getService().getPriceModel();
        getSteppedPricesForPriceModel(retrievedPriceModel).setVersion(
                voSteppedPrice.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelChangesOnSteppedPriceForPricedEventValues()
            throws Exception {
        addSteppedPrices(getService());
        VOServiceDetails serviceDetails = getService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOSteppedPrice voSteppedPrice = getSteppedPricesForEvent(retrievedPriceModel);
        voSteppedPrice.setPrice(BigDecimal.valueOf(5555L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getService().getPriceModel();
        getSteppedPricesForEvent(retrievedPriceModel).setVersion(
                voSteppedPrice.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelChangesOnSteppedPriceForPricedParameterValues()
            throws Exception {
        addSteppedPrices(getService());
        VOServiceDetails serviceDetails = getService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOSteppedPrice voSteppedPrice = getSteppedPricesForParameter(retrievedPriceModel);
        voSteppedPrice.setPrice(BigDecimal.valueOf(6666L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getService().getPriceModel();
        getSteppedPricesForParameter(retrievedPriceModel).setVersion(
                voSteppedPrice.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelChangesOnRolePriceForPMBaseValues()
            throws Exception {
        VOServiceDetails serviceDetails = getService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedRole voPricedRole = getPricedRoleForPriceModel(retrievedPriceModel);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getService().getPriceModel();
        getPricedRoleForPriceModel(retrievedPriceModel).setVersion(
                voPricedRole.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelChangesOnRolePriceForPricedParameter()
            throws Exception {
        VOServiceDetails serviceDetails = getService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedRole voPricedRole = getPricedRoleForParameter(retrievedPriceModel);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(4444L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getService().getPriceModel();
        getPricedRoleForParameter(retrievedPriceModel).setVersion(
                voPricedRole.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelChangesOnRolePriceForPricedOption()
            throws Exception {
        VOServiceDetails serviceDetails = getService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedRole voPricedRole = getPricedRoleForOption(retrievedPriceModel);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(5555L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getService().getPriceModel();
        getPricedRoleForOption(retrievedPriceModel).setVersion(
                voPricedRole.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForSubscriptionChangesOnPMAttributes()
            throws Exception {
        VOServiceDetails serviceDetails = getSubscriptionService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        retrievedPriceModel.setOneTimeFee(BigDecimal.valueOf(999999L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForSubscriptionChangesOnPricedEvent()
            throws Exception {
        VOServiceDetails serviceDetails = getSubscriptionService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedEvent voPricedEvent = getPricedEvent(retrievedPriceModel);
        voPricedEvent.setEventPrice(BigDecimal.valueOf(99999L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getSubscriptionService().getPriceModel();
        getPricedEvent(retrievedPriceModel).setVersion(
                voPricedEvent.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForSubscriptionChangesOnPricedParameter()
            throws Exception {
        VOServiceDetails serviceDetails = getSubscriptionService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedParameter voPricedParameter = getPricedParameter(retrievedPriceModel);
        voPricedParameter.setPricePerUser(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getSubscriptionService().getPriceModel();
        getPricedParameter(retrievedPriceModel).setVersion(
                voPricedParameter.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForSubscriptionChangesOnPricedOption()
            throws Exception {
        VOServiceDetails serviceDetails = getSubscriptionService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedOption voPricedOption = getPricedOption(retrievedPriceModel);
        voPricedOption.setPricePerUser(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getSubscriptionService().getPriceModel();
        getPricedOption(retrievedPriceModel).setVersion(
                voPricedOption.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForSubscriptionChangesOnSteppedPriceForPMBaseValues()
            throws Exception {
        addSteppedPrices(getSubscriptionService());
        VOServiceDetails serviceDetails = getSubscriptionService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOSteppedPrice voSteppedPrice = getSteppedPricesForPriceModel(retrievedPriceModel);
        voSteppedPrice.setPrice(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getSubscriptionService().getPriceModel();
        getSteppedPricesForPriceModel(retrievedPriceModel).setVersion(
                voSteppedPrice.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForSubscriptionChangesOnSteppedPriceForPricedEventValues()
            throws Exception {
        addSteppedPrices(getSubscriptionService());
        VOServiceDetails serviceDetails = getSubscriptionService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOSteppedPrice voSteppedPrice = getSteppedPricesForEvent(retrievedPriceModel);
        voSteppedPrice.setPrice(BigDecimal.valueOf(5555L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getSubscriptionService().getPriceModel();
        getSteppedPricesForEvent(retrievedPriceModel).setVersion(
                voSteppedPrice.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForSubscriptionChangesOnSteppedPriceForPricedParameterValues()
            throws Exception {
        addSteppedPrices(getSubscriptionService());
        VOServiceDetails serviceDetails = getSubscriptionService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOSteppedPrice voSteppedPrice = getSteppedPricesForParameter(retrievedPriceModel);
        voSteppedPrice.setPrice(BigDecimal.valueOf(6666L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getSubscriptionService().getPriceModel();
        getSteppedPricesForParameter(retrievedPriceModel).setVersion(
                voSteppedPrice.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForSubscriptionChangesOnRolePriceForPMBaseValues()
            throws Exception {
        VOServiceDetails serviceDetails = getSubscriptionService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedRole voPricedRole = getPricedRoleForPriceModel(retrievedPriceModel);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getSubscriptionService().getPriceModel();
        getPricedRoleForPriceModel(retrievedPriceModel).setVersion(
                voPricedRole.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForSubscriptionChangesOnRolePriceForPricedParameter()
            throws Exception {
        VOServiceDetails serviceDetails = getSubscriptionService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedRole voPricedRole = getPricedRoleForParameter(retrievedPriceModel);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(4444L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getSubscriptionService().getPriceModel();
        getPricedRoleForParameter(retrievedPriceModel).setVersion(
                voPricedRole.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceForSubscriptionModelChangesOnRolePriceForPricedOption()
            throws Exception {
        VOServiceDetails serviceDetails = getSubscriptionService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedRole voPricedRole = getPricedRoleForOption(retrievedPriceModel);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(5555L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getSubscriptionService().getPriceModel();
        getPricedRoleForOption(retrievedPriceModel).setVersion(
                voPricedRole.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForCustomerChangesOnPMAttributes()
            throws Exception {
        VOServiceDetails serviceDetails = getCustomerService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        retrievedPriceModel.setOneTimeFee(BigDecimal.valueOf(999999L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForCustomerChangesOnPricedEvent()
            throws Exception {
        VOServiceDetails serviceDetails = getCustomerService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedEvent voPricedEvent = getPricedEvent(retrievedPriceModel);
        voPricedEvent.setEventPrice(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getCustomerService().getPriceModel();
        getPricedEvent(retrievedPriceModel).setVersion(
                voPricedEvent.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForCustomerChangesOnPricedParameter()
            throws Exception {
        VOServiceDetails serviceDetails = getCustomerService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedParameter voPricedParameter = getPricedParameter(retrievedPriceModel);
        voPricedParameter.setPricePerUser(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getCustomerService().getPriceModel();
        getPricedParameter(retrievedPriceModel).setVersion(
                voPricedParameter.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForCustomerChangesOnPricedOption()
            throws Exception {
        VOServiceDetails serviceDetails = getCustomerService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedOption voPricedOption = getPricedOption(retrievedPriceModel);
        voPricedOption.setPricePerUser(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getCustomerService().getPriceModel();
        getPricedOption(retrievedPriceModel).setVersion(
                voPricedOption.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForCustomerChangesOnSteppedPriceForPMBaseValues()
            throws Exception {
        addSteppedPrices(getCustomerService());
        VOServiceDetails serviceDetails = getCustomerService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOSteppedPrice voSteppedPrice = getSteppedPricesForPriceModel(retrievedPriceModel);
        voSteppedPrice.setPrice(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getCustomerService().getPriceModel();
        getSteppedPricesForPriceModel(retrievedPriceModel).setVersion(
                voSteppedPrice.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForCustomerChangesOnSteppedPriceForPricedEventValues()
            throws Exception {
        addSteppedPrices(getCustomerService());
        VOServiceDetails serviceDetails = getCustomerService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOSteppedPrice voSteppedPrice = getSteppedPricesForEvent(retrievedPriceModel);
        voSteppedPrice.setPrice(BigDecimal.valueOf(5555L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getCustomerService().getPriceModel();
        getSteppedPricesForEvent(retrievedPriceModel).setVersion(
                voSteppedPrice.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForCustomerChangesOnSteppedPriceForPricedParameterValues()
            throws Exception {
        addSteppedPrices(getCustomerService());
        VOServiceDetails serviceDetails = getCustomerService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOSteppedPrice voSteppedPrice = getSteppedPricesForParameter(retrievedPriceModel);
        voSteppedPrice.setPrice(BigDecimal.valueOf(6666L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getCustomerService().getPriceModel();
        getSteppedPricesForParameter(retrievedPriceModel).setVersion(
                voSteppedPrice.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForCustomerChangesOnRolePriceForPMBaseValues()
            throws Exception {
        VOServiceDetails serviceDetails = getCustomerService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedRole voPricedRole = getPricedRoleForPriceModel(retrievedPriceModel);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(3333L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getCustomerService().getPriceModel();
        getPricedRoleForPriceModel(retrievedPriceModel).setVersion(
                voPricedRole.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForCustomerChangesOnRolePriceForPricedParameter()
            throws Exception {
        VOServiceDetails serviceDetails = getCustomerService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedRole voPricedRole = getPricedRoleForParameter(retrievedPriceModel);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(4444L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getCustomerService().getPriceModel();
        getPricedRoleForParameter(retrievedPriceModel).setVersion(
                voPricedRole.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSavePriceModelForCustomerChangesOnRolePriceForPricedOption()
            throws Exception {
        VOServiceDetails serviceDetails = getCustomerService();
        VOPriceModel retrievedPriceModel = serviceDetails.getPriceModel();
        VOPricedRole voPricedRole = getPricedRoleForOption(retrievedPriceModel);
        voPricedRole.setPricePerUser(BigDecimal.valueOf(5555L));
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);

        retrievedPriceModel = getCustomerService().getPriceModel();
        getPricedRoleForOption(retrievedPriceModel).setVersion(
                voPricedRole.getVersion());
        svcProv.savePriceModel(serviceDetails, retrievedPriceModel);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testDeleteServiceModifiedService() throws Exception {
        VOServiceDetails service = getService();
        service.setVersion(service.getVersion() - 1);
        svcProv.deleteService(service);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testDeleteTechnicalServiceModifiedService() throws Exception {
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService technicalService = createTechnicalService();
        technicalService.setVersion(technicalService.getVersion() - 1);
        svcProv.deleteTechnicalService(technicalService);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testDeleteTechnicalServiceModifiedEvent() throws Exception {
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService technicalService = createTechnicalService();
        VOEventDefinition voEventDefinition = technicalService
                .getEventDefinitions().get(0);
        voEventDefinition.setVersion(voEventDefinition.getVersion() - 1);
        svcProv.deleteTechnicalService(technicalService);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testDeleteTechnicalServiceMissingEvent() throws Exception {
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_TECHNOLOGY_MANAGER);
        final VOTechnicalService technicalService = createTechnicalService();
        // there is a new event, so the delete must fail
        technicalService.getEventDefinitions().remove(0);
        svcProv.deleteTechnicalService(technicalService);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testDeleteTechnicalServiceModifiedParamDef() throws Exception {
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService technicalService = createTechnicalService();
        VOParameterDefinition parameterDefinition = technicalService
                .getParameterDefinitions().get(0);
        parameterDefinition.setVersion(parameterDefinition.getVersion() - 1);
        svcProv.deleteTechnicalService(technicalService);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testDeleteTechnicalServiceMissingParamDef() throws Exception {
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_TECHNOLOGY_MANAGER);
        final VOTechnicalService technicalService = createTechnicalService();
        // there is a new event, so the delete must fail
        technicalService.getParameterDefinitions().remove(0);
        svcProv.deleteTechnicalService(technicalService);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testDeleteTechnicalServiceModifiedRoleDef() throws Exception {
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService technicalService = createTechnicalService();
        VORoleDefinition voRoleDefinition = technicalService
                .getRoleDefinitions().get(0);
        voRoleDefinition.setVersion(voRoleDefinition.getVersion() - 1);
        svcProv.deleteTechnicalService(technicalService);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testDeleteTechnicalServiceMissingRoleDef() throws Exception {
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_TECHNOLOGY_MANAGER);
        final VOTechnicalService technicalService = createTechnicalService();
        // there is a new event, so the delete must fail
        technicalService.getRoleDefinitions().remove(0);
        svcProv.deleteTechnicalService(technicalService);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testDeleteTechnicalServiceModifiedOperation() throws Exception {
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService technicalService = createTechnicalService();
        VOTechnicalServiceOperation voTechnicalServiceOperation = technicalService
                .getTechnicalServiceOperations().get(0);
        voTechnicalServiceOperation.setVersion(voTechnicalServiceOperation
                .getVersion() - 1);
        svcProv.deleteTechnicalService(technicalService);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testDeleteTechnicalServiceMissingOperation() throws Exception {
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_TECHNOLOGY_MANAGER);
        final VOTechnicalService technicalService = createTechnicalService();
        // there is a new event, so the delete must fail
        technicalService.getTechnicalServiceOperations().remove(0);
        svcProv.deleteTechnicalService(technicalService);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCreateServiceObsoleteTechnicalService() throws Exception {
        VOServiceDetails service = getService();
        // container.login(Scenario.getSupplierAdminUser().getKey());
        service.setKey(0);
        service.setServiceId("newService");
        VOTechnicalService technicalService = service.getTechnicalService();
        technicalService.setVersion(technicalService.getVersion() - 1);
        svcProv.createService(technicalService, service, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCreateServiceObsoleteEventDefinition() throws Exception {
        VOServiceDetails service = getService();
        // container.login(Scenario.getSupplierAdminUser().getKey());
        service.setKey(0);
        service.setServiceId("newService");
        VOTechnicalService technicalService = service.getTechnicalService();
        VOEventDefinition voEventDefinition = technicalService
                .getEventDefinitions().get(0);
        voEventDefinition.setVersion(voEventDefinition.getVersion() - 1);
        svcProv.createService(technicalService, service, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCreateServiceMissingEventDefinition() throws Exception {
        VOServiceDetails service = getService();
        // container.login(Scenario.getSupplierAdminUser().getKey());
        service.setKey(0);
        service.setServiceId("newService");
        VOTechnicalService technicalService = service.getTechnicalService();
        technicalService.getEventDefinitions().remove(0);
        svcProv.createService(technicalService, service, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCreateServiceObsoleteParameterDefinition() throws Exception {
        VOServiceDetails service = getService();
        // container.login(Scenario.getSupplierAdminUser().getKey());
        service.setKey(0);
        service.setServiceId("newService");
        VOTechnicalService technicalService = service.getTechnicalService();
        VOParameterDefinition voParameterDefinition = technicalService
                .getParameterDefinitions().get(0);
        voParameterDefinition
                .setVersion(voParameterDefinition.getVersion() - 1);
        svcProv.createService(technicalService, service, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCreateServiceMissingParameterDefinition() throws Exception {
        VOServiceDetails service = getService();
        // container.login(Scenario.getSupplierAdminUser().getKey());
        service.setKey(0);
        service.setServiceId("newService");
        VOTechnicalService technicalService = service.getTechnicalService();
        technicalService.getParameterDefinitions().remove(0);
        svcProv.createService(technicalService, service, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCreateServiceObsoleteRoleDefinition() throws Exception {
        VOServiceDetails service = getService();
        // container.login(Scenario.getSupplierAdminUser().getKey());
        service.setKey(0);
        service.setServiceId("newService");
        VOTechnicalService technicalService = service.getTechnicalService();
        VORoleDefinition voRoleDefinition = technicalService
                .getRoleDefinitions().get(0);
        voRoleDefinition.setVersion(voRoleDefinition.getVersion() - 1);
        svcProv.createService(technicalService, service, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCreateServiceMissingRoleDefinition() throws Exception {
        VOServiceDetails service = getService();
        // container.login(Scenario.getSupplierAdminUser().getKey());
        service.setKey(0);
        service.setServiceId("newService");
        VOTechnicalService technicalService = service.getTechnicalService();
        technicalService.getRoleDefinitions().remove(0);
        svcProv.createService(technicalService, service, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCreateServiceObsoleteServiceOperation() throws Exception {
        VOServiceDetails service = getService();
        // container.login(Scenario.getSupplierAdminUser().getKey());
        service.setKey(0);
        service.setServiceId("newService");
        VOTechnicalService technicalService = service.getTechnicalService();
        VOTechnicalServiceOperation voTechnicalServiceOperation = technicalService
                .getTechnicalServiceOperations().get(0);
        voTechnicalServiceOperation.setVersion(voTechnicalServiceOperation
                .getVersion() - 1);
        svcProv.createService(technicalService, service, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testCreateServiceMissingServiceOperation() throws Exception {
        VOServiceDetails service = getService();
        // container.login(Scenario.getSupplierAdminUser().getKey());
        service.setKey(0);
        service.setServiceId("newService");
        VOTechnicalService technicalService = service.getTechnicalService();
        technicalService.getTechnicalServiceOperations().remove(0);
        svcProv.createService(technicalService, service, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSetCompatibleServicesOutdatedSource() throws Exception {
        VOServiceDetails service = getService();
        service.setVersion(service.getVersion() - 1);
        VOServiceDetails target = getService();
        List<VOService> targets = new ArrayList<VOService>();
        targets.add(target);
        svcProv.setCompatibleServices(service, targets);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSetCompatibleServicesOutdatedTarget() throws Exception {
        VOServiceDetails service = getService();
        VOServiceDetails target = getService();
        target.setVersion(target.getVersion() - 1);
        List<VOService> targets = new ArrayList<VOService>();
        targets.add(target);
        svcProv.setCompatibleServices(service, targets);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testUpdateServiceOutdatedService() throws Exception {
        VOServiceDetails service = getService();

        OrganizationReference orgRef = createOrgRef(Scenario.getSupplier()
                .getKey());
        createMarketingPermission(service.getTechnicalService().getKey(),
                orgRef.getKey());

        service.setVersion(service.getVersion() - 1);
        svcProv.updateService(service, null);
    }

    @Test
    public void testCreateServiceWithNonConfigurableParams() throws Exception {
        final VOTechnicalService technicalService = createTechnicalService();

        OrganizationReference orgRef = createOrgRef(Scenario.getSupplier()
                .getKey());
        createMarketingPermission(technicalService.getKey(), orgRef.getKey());

        VOTechnicalService updTS = runTX(new Callable<VOTechnicalService>() {
            @Override
            public VOTechnicalService call() throws Exception {
                TechnicalProduct reference = dm.getReference(
                        TechnicalProduct.class, technicalService.getKey());
                TechnicalProducts.addParameterDefinition(
                        ParameterValueType.STRING, "stringNC",
                        ParameterType.PLATFORM_PARAMETER, reference, dm, null,
                        null, false);
                List<Event> platformEvents = new ArrayList<Event>();
                List<ParameterDefinition> platformParams = new ArrayList<ParameterDefinition>();
                return TechnicalProductAssembler.toVOTechnicalProduct(
                        reference,
                        platformParams,
                        platformEvents,
                        new LocalizerFacade(container
                                .get(LocalizerServiceLocal.class), "en"), true);
            }
        });
        VOService service = new VOService();
        service.setServiceId("myService");
        VOServiceDetails createdService = svcProv.createService(updTS, service,
                null);
        assertTrue(createdService.getKey() > 0);
        assertEquals(0, createdService.getVersion());
    }

    // ****************************************************************************
    // private methods

    /**
     * Creates a technical service.
     * 
     * @return The value object representation of the technical service.
     * @throws Exception
     */
    private VOTechnicalService createTechnicalService() throws Exception {
        VOTechnicalService technicalService = runTX(new Callable<VOTechnicalService>() {
            @Override
            public VOTechnicalService call() throws Exception {
                TechnicalProduct ts = Scenario.createTechnicalService(dm,
                        "newTP");
                List<Event> platformEvents = new ArrayList<Event>();
                List<ParameterDefinition> platformParams = new ArrayList<ParameterDefinition>();
                return TechnicalProductAssembler.toVOTechnicalProduct(ts,
                        platformParams, platformEvents, new LocalizerFacade(
                                container.get(LocalizerServiceLocal.class),
                                "en"), true);
            }
        });
        return technicalService;
    }

    /**
     * Retrieves the priced event data.
     * 
     * @param retrievedPriceModel
     *            The price model containing the pricing information.
     * @return The priced event.
     */
    private VOPricedEvent getPricedEvent(VOPriceModel retrievedPriceModel) {
        return retrievedPriceModel.getConsideredEvents().get(0);
    }

    /**
     * Retrieves the priced parameter data.
     * 
     * @param retrievedPriceModel
     *            The price model containing the pricing information.
     * @return The priced parameter.
     */
    private VOPricedParameter getPricedParameter(
            VOPriceModel retrievedPriceModel) {
        return retrievedPriceModel.getSelectedParameters().get(0);
    }

    /**
     * Retrieves the priced option data.
     * 
     * @param retrievedPriceModel
     *            The price model containing the pricing information.
     * @return The priced option.
     */
    private VOPricedOption getPricedOption(VOPriceModel retrievedPriceModel) {
        return retrievedPriceModel.getSelectedParameters().get(2)
                .getPricedOptions().get(0);
    }

    /**
     * Retrieves the stepped prices for the priced parameter of the price model.
     * 
     * @param retrievedPriceModel
     *            The price model containing the pricing information.
     * @return The stepped prices.
     */
    private VOSteppedPrice getSteppedPricesForParameter(
            VOPriceModel retrievedPriceModel) {
        return getPricedParameter(retrievedPriceModel).getSteppedPrices()
                .get(0);
    }

    /**
     * Retrieves the stepped prices for the priced event of the price model.
     * 
     * @param retrievedPriceModel
     *            The price model containing the pricing information.
     * @return The stepped prices.
     */
    private VOSteppedPrice getSteppedPricesForEvent(
            VOPriceModel retrievedPriceModel) {
        return getPricedEvent(retrievedPriceModel).getSteppedPrices().get(0);
    }

    /**
     * Retrieves the stepped prices of the price model.
     * 
     * @param retrievedPriceModel
     *            The price model containing the pricing information.
     * @return The stepped prices.
     */
    private VOSteppedPrice getSteppedPricesForPriceModel(
            VOPriceModel retrievedPriceModel) {
        return retrievedPriceModel.getSteppedPrices().get(0);
    }

    /**
     * Retrieves the priced role for a priced option of the price model.
     * 
     * @param retrievedPriceModel
     *            The price model containing the pricing information.
     * @return The priced role.
     */
    private VOPricedRole getPricedRoleForOption(VOPriceModel retrievedPriceModel) {
        return getPricedOption(retrievedPriceModel).getRoleSpecificUserPrices()
                .get(0);
    }

    /**
     * Retrieves the priced role for a priced parameter of the price model.
     * 
     * @param retrievedPriceModel
     *            The price model containing the pricing information.
     * @return The priced role.
     */
    private VOPricedRole getPricedRoleForParameter(
            VOPriceModel retrievedPriceModel) {
        return getPricedParameter(retrievedPriceModel)
                .getRoleSpecificUserPrices().get(0);
    }

    /**
     * Retrieves the priced role of the price model.
     * 
     * @param retrievedPriceModel
     *            The price model containing the pricing information.
     * @return The priced role.
     */
    private VOPricedRole getPricedRoleForPriceModel(
            VOPriceModel retrievedPriceModel) {
        return retrievedPriceModel.getRoleSpecificUserPrices().get(0);
    }

    /**
     * Determines the service provided by the supplier.
     * 
     * @return The service in vo representation.
     */
    private VOServiceDetails getService() throws Exception {
        List<VOService> services = svcProv.getSuppliedServices();
        assertEquals(1, services.size());
        VOService voService = services.get(0);
        VOServiceDetails serviceDetails = svcProv.getServiceDetails(voService);
        return serviceDetails;
    }

    /**
     * Determines the subscription specific service for the customer.
     * 
     * @return The service in vo representation.
     * @throws ObjectNotFoundException
     * @throws OrganizationAuthoritiesException
     */
    private VOServiceDetails getSubscriptionService()
            throws ObjectNotFoundException, OrganizationAuthoritiesException {
        VOServiceDetails service = svcProv.getServiceForSubscription(Scenario
                .getVoCustomer(), Scenario.getSubscription()
                .getSubscriptionId());
        return service;
    }

    /**
     * Determines the customer specific service for the customer.
     * 
     * @return The service in vo representation.
     */
    private VOServiceDetails getCustomerService() throws Exception,
            OperationNotPermittedException {
        VOServiceDetails custService = svcProv.getServiceForCustomer(
                Scenario.getVoCustomer(), getService());
        return custService;
    }

    private void addSteppedPrices(final VOService svc) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product p = dm.getReference(Product.class, svc.getKey());
                PriceModel pm = p.getPriceModel();
                PricedEvent pe = pm.getConsideredEvents().get(0);
                PricedParameter pp = pm.getSelectedParameters().get(0);
                Scenario.addSteppedPrices(pm, pe, pp);
                return null;
            }
        });
    }

}
