/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 22.11.2010                                                      
 *                                                                              
 *  Completion Time: 22.11.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.*;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.MarketingPermissions;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.Mockito.mock;

/**
 * @author weiser
 * 
 */
public class ServiceProvisioningPriceModelDirectAccessIT extends EJBTestBase {

    private ServiceProvisioningService provisioningService;
    private DataService mgr;
    private Organization provider;
    private String providerUserKey;
    private VOServiceDetails serviceDetails;
    protected TechnicalProduct marketingPermServ_getTechnicalProduct;
    private MarketingPermissionServiceLocal marketingPermissionSvcMock;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);

        container.login("1");
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(mock(ImageResourceServiceLocal.class));
        container.addBean(new ApplicationServiceStub() {
            @Override
            public void validateCommunication(TechnicalProduct techProduct)
                    throws TechnicalServiceNotAliveException {
            }
        });
        container.addBean(new SessionServiceStub());
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LdapAccessServiceStub());
        container.addBean(new TaskQueueServiceStub());
        container.addBean(mock(IdentityServiceLocal.class));
        container.addBean(new PaymentServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        marketingPermissionSvcMock = mock(MarketingPermissionServiceLocal.class);
        container.addBean(marketingPermissionSvcMock);
        container.addBean(mock(AccountServiceLocal.class));
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new TagServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());

        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);

        provisioningService = container.get(ServiceProvisioningService.class);
        mgr = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("EUR"));
                mgr.persist(sc);
                createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                SupportedCountries.createAllSupportedCountries(mgr);
                return null;
            }
        });

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                provider = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        provider, true, "admin");

                providerUserKey = String.valueOf(user.getKey());
                return null;
            }
        });
        container.login(providerUserKey, new String[] {
                ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                ROLE_SERVICE_MANAGER });
        // create tp
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider, "tp", false, ServiceAccessType.DIRECT);
                TechnicalProducts.addParameterDefinition(
                        ParameterValueType.LONG, "param1",
                        ParameterType.SERVICE_PARAMETER, tp, mgr, null, null,
                        true);
                ParameterDefinition enumParam = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.ENUMERATION,
                                "param2", ParameterType.SERVICE_PARAMETER, tp,
                                mgr, null, null, true);
                TechnicalProducts.addParameterOption(enumParam, "1", mgr);
                MarketingPermissions.createMarketingPermission(tp.getKey(),
                        provider.getKey(), provider.getKey(), mgr);
                marketingPermServ_getTechnicalProduct = tp;
                return null;
            }
        });

        Mockito.doAnswer(new Answer<List<TechnicalProduct>>() {
            @Override
            public List<TechnicalProduct> answer(InvocationOnMock invocation)
                    throws Throwable {
                return Collections
                        .singletonList(marketingPermServ_getTechnicalProduct);
            }
        })
                .when(marketingPermissionSvcMock)
                .getTechnicalServicesForSupplier(
                        Matchers.any(Organization.class));

        // create service
        serviceDetails = initService();
    }

    /**
     * @return
     * 
     */
    private VOServiceDetails initService() throws Exception {
        List<VOTechnicalService> list = provisioningService
                .getTechnicalServices(OrganizationRoleType.SUPPLIER);
        Assert.assertNotNull(list);
        VOTechnicalService tp = list.get(0);
        List<VOParameterDefinition> paramDefs = tp.getParameterDefinitions();
        VOServiceDetails svc = new VOServiceDetails();
        svc.setName("name");
        svc.setServiceId("service1");
        List<VOParameter> parameters = new ArrayList<VOParameter>();
        for (VOParameterDefinition def : paramDefs) {
            List<VOParameterOption> parameterOptions = new ArrayList<VOParameterOption>();
            VOParameterOption parameterOption = new VOParameterOption();
            parameterOption.setKey(123);
            parameterOption.setOptionId("1");
            parameterOptions.add(parameterOption);
            def.setParameterOptions(parameterOptions);
            VOParameter p = new VOParameter(def);
            p.setConfigurable(true);
            p.setValue("1");
            parameters.add(p);
        }
        svc.setParameters(parameters);
        return provisioningService.createService(tp, svc, null);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_subscriptionChargePerUser() throws Exception {
        VOPriceModel pm = createPriceModel(BigDecimal.valueOf(5),
                BigDecimal.ZERO, BigDecimal.ZERO, false);
        provisioningService.savePriceModel(serviceDetails, pm);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_parameterChargePerUser() throws Exception {
        VOPriceModel pm = createPriceModel(BigDecimal.ZERO,
                BigDecimal.valueOf(5), BigDecimal.ZERO, false);
        provisioningService.savePriceModel(serviceDetails, pm);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_optionChargePerUser() throws Exception {
        VOPriceModel pm = createPriceModel(BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(5), false);
        provisioningService.savePriceModel(serviceDetails, pm);
    }

    @Test(expected = ValidationException.class)
    public void testSavePriceModel_subscriptionSteppedPrices() throws Exception {
        VOPriceModel pm = createPriceModel(BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, true);
        provisioningService.savePriceModel(serviceDetails, pm);
    }

    private VOPriceModel createPriceModel(BigDecimal userPrice,
            BigDecimal paramPrice, BigDecimal optionPrice, boolean steppedPrices) {
        VOPriceModel pm = new VOPriceModel();
        pm.setType(PriceModelType.PRO_RATA);
        pm.setCurrencyISOCode("EUR");
        pm.setPeriod(PricingPeriod.HOUR);
        pm.setPricePerUserAssignment(userPrice);
        if (steppedPrices) {
            List<VOSteppedPrice> list = new ArrayList<VOSteppedPrice>();
            VOSteppedPrice sp = new VOSteppedPrice();
            sp.setPrice(BigDecimal.valueOf(5));
            sp.setLimit(Long.valueOf(5));
            list.add(sp);

            sp = new VOSteppedPrice();
            sp.setPrice(BigDecimal.valueOf(3));
            sp.setLimit(Long.valueOf(Long.MAX_VALUE));
            list.add(sp);
            pm.setSteppedPrices(list);
        }
        List<VOPricedParameter> pps = new ArrayList<VOPricedParameter>();
        List<VOParameter> parameters = serviceDetails.getParameters();
        for (VOParameter p : parameters) {
            VOParameterDefinition pd = p.getParameterDefinition();
            if (pd.getValueType() == ParameterValueType.STRING) {
                continue;
            }
            if (pd.getValueType() == ParameterValueType.ENUMERATION) {
                VOPricedParameter pp = new VOPricedParameter(pd);
                pp.setParameterKey(p.getKey());
                List<VOPricedOption> ppos = pp.getPricedOptions();
                List<VOParameterOption> pos = pd.getParameterOptions();
                for (VOParameterOption po : pos) {
                    VOPricedOption ppo = new VOPricedOption();
                    ppo.setParameterOptionKey(po.getKey());
                    ppo.setPricePerUser(optionPrice);
                    ppos.add(ppo);
                }
                pp.setPricedOptions(ppos);
                pps.add(pp);
            } else {
                VOPricedParameter pp = new VOPricedParameter(pd);
                pp.setParameterKey(p.getKey());
                pp.setPricePerUser(paramPrice);
                pps.add(pp);
            }
        }
        pm.setSelectedParameters(pps);
        return pm;
    }
}
