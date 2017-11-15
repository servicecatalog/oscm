/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 13.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.applicationservice.bean.ApplicationServiceStub;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.types.enumtypes.EmailType;

/**
 * Parameter related tests for the subscription service.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class SubscriptionServiceBeanParameterIT extends EJBTestBase {

    private SubscriptionService subSvc;
    private DataService mgr;

    private Organization org;
    private TechnicalProduct techProd;
    private Product product;
    private ParameterDefinition paramDef;
    private PlatformUser user;
    private LocalizerServiceLocal localizer;

    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        container.enableInterfaceMocking(true);
        container.addBean(mock(TaskQueueServiceLocal.class));
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }

            @Override
            public String getLocalizedTextFromBundle(
                    LocalizedObjectTypes objectType, Marketplace shop,
                    String localeString, String key) {
                return "";
            }

            @Override
            public List<VOLocalizedText> getLocalizedValues(long objectKey,
                    LocalizedObjectTypes objectType) {
                return null;
            }
        });
        container.addBean(new CommunicationServiceStub() {
            @Override
            public void sendMail(PlatformUser recipient, EmailType type,
                    Object[] params, Marketplace marketplace) {
                return;
            }
        });
        container.addBean(new ApplicationServiceStub());
        container.addBean(new TenantProvisioningServiceBean());
        container.addBean(new IdentityServiceStub() {
            @Override
            public PlatformUser getPlatformUser(String userId,
                    boolean validateOrganization) {
                return user;
            }
        });
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                TriggerProcess tp = new TriggerProcess();
                tp.setTriggerDefinition(null);
                tp.setUser(user);
                TriggerProcessMessageData data = new TriggerProcessMessageData(
                        tp, null);
                return Collections.singletonList(data);
            }

        });
        container.addBean(new DataServiceBean());
        container.addBean(new SessionServiceStub());
        container.addBean(mock(SubscriptionListServiceBean.class));
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new TerminateSubscriptionBean());

        subSvc = container.get(SubscriptionService.class);
        mgr = container.get(DataService.class);
        localizer = container.get(LocalizerServiceLocal.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOrganizationRoles(mgr);
                createPaymentTypes(mgr);
                createSupportedCurrencies(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                // create organization
                org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                user = Organizations.createUserForOrg(mgr, org, true, "admin");
                return null;
            }
        });
        container.login(user.getKey());
    }

    @Test
    // Bug 5497
    public void testSubscribeToProductWithParametersUpdateDefaultValue()
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                // create technical product with param
                techProd = TechnicalProducts.createTechnicalProduct(mgr, org,
                        "orgId", false, ServiceAccessType.DIRECT);

                paramDef = TechnicalProducts.addParameterDefinition(
                        ParameterValueType.INTEGER, "param1",
                        ParameterType.SERVICE_PARAMETER, techProd, mgr, null,
                        null, true);

                // create product and price model
                product = Products.createProduct(org, techProd, false,
                        "product", null, mgr);

                Products.createParameter(paramDef, product, mgr);

                return null;
            }
        });
        VOService template = runTX(new Callable<VOService>() {

            @Override
            public VOService call() {
                product = mgr.find(Product.class, product.getKey());
                // subscribe
                VOService template = ProductAssembler.toVOProduct(product,
                        new LocalizerFacade(localizer, "en"));
                return template;
            }
        });

        template.getParameters().get(0).setValue("200");
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId("mySub");
        VOUser adminUser = new VOUser();
        adminUser.setKey(user.getKey());
        VOUsageLicense usageLicense = new VOUsageLicense();
        usageLicense.setUser(adminUser);
        container.login(String.valueOf(user.getKey()), ROLE_ORGANIZATION_ADMIN);
        List<VOUsageLicense> usageLicenses = new ArrayList<>();
        usageLicenses.add(usageLicense);
        final VOSubscription subscription1 = subSvc.subscribeToService(
                subscription, template, usageLicenses, null, null,
                new ArrayList<VOUda>());

        // check parameter settings for copy and original product
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Subscription sub = mgr.find(Subscription.class,
                        subscription1.getKey());
                Parameter subParam = sub.getProduct().getParameterSet()
                        .getParameters().get(0);
                Assert.assertEquals("200", subParam.getValue());

                Product prod = mgr.find(Product.class, product.getKey());
                Parameter prodParam = prod.getParameterSet().getParameters()
                        .get(0);
                Assert.assertEquals("123", prodParam.getValue());
                return null;
            }
        });
    }
}
