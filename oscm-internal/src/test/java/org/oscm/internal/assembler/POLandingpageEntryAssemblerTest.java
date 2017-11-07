/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 26.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.oscm.test.Numbers.L_TIMESTAMP;
import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.oscm.test.matchers.JavaMatchers.hasOneItem;
import static org.oscm.test.matchers.JavaMatchers.isNullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.landingpage.POLandingpageEntry;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.test.stubs.LocalizerServiceStub;

/**
 * @author zankov
 * 
 */
public class POLandingpageEntryAssemblerTest {

    private LocalizerFacade facade;
    @Captor
    ArgumentCaptor<List<Long>> objectKeyCaptor;

    @Before
    public void setup() throws Exception {
        facade = new LocalizerFacade(new LocalizerServiceStub() {

            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return objectType.name();
            }
        }, "en");

        MockitoAnnotations.initMocks(this);
    }

    private Organization givenSupplier() {
        Organization supplier = new Organization();
        supplier.setOrganizationId("organizationId");
        supplier.setName("name");

        return supplier;
    }

    private TechnicalProduct givenTechnicalProduct(ServiceAccessType type) {
        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setAccessType(type);
        technicalProduct.setBaseURL("baseURL");
        technicalProduct.setKey(1234);
        technicalProduct.setLoginPath("loginPath");
        technicalProduct.setTechnicalProductId("technicalProductId");

        return technicalProduct;
    }

    private Product givenProduct(ServiceAccessType type) {
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setVendor(givenSupplier());
        product.setTechnicalProduct(givenTechnicalProduct(type));
        product.setKey(4321);
        product.setProductId("productId");
        product.setStatus(ServiceStatus.ACTIVE);

        return product;
    }

    @Test
    public void toPOLandingpageEntries() {
        // given
        List<Subscription> subscriptions = Arrays.asList(givenSubscription(
                "SUB_ID", ServiceAccessType.LOGIN));
        List<Product> services = Arrays
                .asList(givenProduct(ServiceAccessType.LOGIN));

        // when
        List<POLandingpageEntry> result = POLandingpageEntryAssembler
                .toPOLandingpageEntries(services, subscriptions, facade);

        // than
        assertThat(result, hasOneItem());
    }

    /**
     * A specific case for the fcip enterprise landingpage If there are more
     * than one subscription for some product than the entry should not contain
     * baseURL. The UI should redirect in this case to "My Subscriptions" and
     * let the user choose the right subscription. The normal case for fcip ist
     * that there is only one or none subscription on given product.
     */
    @Test
    public void toPOLandingpageEntries_TwoSuscriptions() {
        // given
        List<Subscription> subscriptions = Arrays.asList(
                givenSubscription("sub_1", ServiceAccessType.LOGIN),
                givenSubscription("sub_2", ServiceAccessType.LOGIN));
        List<Product> services = Arrays
                .asList(givenProduct(ServiceAccessType.LOGIN));

        // when
        List<POLandingpageEntry> result = POLandingpageEntryAssembler
                .toPOLandingpageEntries(services, subscriptions, facade);

        // than
        assertThat(result, hasOneItem());
        assertThat(result.get(0).getServiceAccessURL(), isNullValue());
    }

    @Test
    public void toPOLandingpageEntries_Empty() {
        // given
        List<Subscription> subscriptions = noSubscriptions();
        List<Product> services = noServices();

        // when
        List<POLandingpageEntry> result = POLandingpageEntryAssembler
                .toPOLandingpageEntries(services, subscriptions, facade);

        // than
        assertThat(result, hasNoItems());
    }

    /**
     * The landingpage should show all available services even if there are no
     * subscriptions for them.
     */
    @Test
    public void toPOLandingpageEntries_NoSubscriptions() {
        // given
        List<Product> services = Arrays
                .asList(givenProduct(ServiceAccessType.LOGIN));
        List<Subscription> subscriptions = noSubscriptions();

        // when
        List<POLandingpageEntry> result = POLandingpageEntryAssembler
                .toPOLandingpageEntries(services, subscriptions, facade);

        // than
        assertThat(result, hasOneItem());
    }

    /**
     * Subscriptions for services which should not be shown should be ignored.
     */
    @Test
    public void toPOLandingpageEntries_NoServices() {
        // given
        List<Product> services = noServices();
        List<Subscription> subscriptions = Arrays.asList(givenSubscription(
                "SUB_ID", ServiceAccessType.LOGIN));

        // when
        List<POLandingpageEntry> result = POLandingpageEntryAssembler
                .toPOLandingpageEntries(services, subscriptions, facade);

        // than
        assertThat(result, hasNoItems());
    }

    private List<Product> noServices() {
        return new ArrayList<Product>();
    }

    private List<Subscription> noSubscriptions() {
        return new ArrayList<Subscription>();
    }

    /**
     * Test assignment of the required parameters form the services
     */
    @Test
    public void fillServiceFields() {
        // given
        Product product = givenProduct(ServiceAccessType.LOGIN);

        // when
        POLandingpageEntry entry = new POLandingpageEntry();
        POLandingpageEntryAssembler.fillProductFields(product, entry, facade);

        // than
        verifyServiceFields(product, entry);
    }

    private void verifyServiceFields(Product product, POLandingpageEntry entry) {
        assertEquals(product.getStatus(), entry.getServiceStatus());
        assertEquals(product.getKey(), entry.getServiceKey());
        assertEquals(product.getProductId(), entry.getServiceId());
        assertEquals(LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION.name(),
                entry.getShortDescription());
        assertEquals(product.getStatus(), entry.getServiceStatus());
        assertEquals(LocalizedObjectTypes.PRODUCT_MARKETING_NAME.name(),
                entry.getName());
    }

    /**
     * Test assignment of the required parameter from the subscription
     */
    @Test
    public void fillSubscriptionFields() {
        // given
        Subscription subscription = givenSubscription("SUB_ID",
                ServiceAccessType.LOGIN);

        // when
        POLandingpageEntry entry = new POLandingpageEntry();
        POLandingpageEntryAssembler.updateEntryForSubscription(subscription,
                entry);

        // then
        verifySubscriptionFields(subscription, entry);
    }

    private void verifySubscriptionFields(Subscription subscription,
            POLandingpageEntry entry) {
        assertEquals(subscription.getStatus(), entry.getSubscriptionStatus());
        assertTrue(entry.isSubscribed());
        assertEquals(subscription.getSubscriptionId(),
                entry.getSubscriptionId());
        assertEquals(subscription.getBaseURL(), entry.getServiceAccessURL());
    }

    private Subscription givenSubscription(String subId, ServiceAccessType type) {
        Subscription subscription = new Subscription();
        subscription.bindToProduct(givenProduct(type));
        subscription.setSubscriptionId(subId.isEmpty() ? "SUB_ID" : subId);
        subscription.setCreationDate(L_TIMESTAMP);
        subscription.setActivationDate(L_TIMESTAMP);
        subscription.setDeactivationDate(L_TIMESTAMP);
        subscription.setTimeoutMailSent(false);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setAccessInfo("accessInfo");
        subscription.setBaseURL("baseURL");
        subscription.setLoginPath("loginPath");
        subscription.setProductInstanceId("productInstanceId");
        subscription.setPurchaseOrderNumber("purchaseOrderNumber");
        PlatformUser owner = new PlatformUser();
        owner.setUserId("OWNER_USER_ID");
        subscription.setOwner(owner);
        return subscription;
    }

    @Test
    public void toPOLandingpageEntries_prefetchForPoducts_withoutTemplates_bug12479() {
        // given
        List<Subscription> subscriptions = Arrays.asList(
                givenSubscription("sub_1", ServiceAccessType.LOGIN),
                givenSubscription("sub_2", ServiceAccessType.LOGIN));
        List<Product> services = Arrays
                .asList(givenProduct(ServiceAccessType.LOGIN));
        LocalizerFacade facadeMock = spy(facade);

        // when
        POLandingpageEntryAssembler.toPOLandingpageEntries(services,
                subscriptions, facadeMock);

        // than
        verify(facadeMock, times(2)).prefetch(objectKeyCaptor.capture(),
                Matchers.anyListOf(LocalizedObjectTypes.class));
        List<List<Long>> args = objectKeyCaptor.getAllValues();
        List<Long> objectkeys = args.get(0);
        assertEquals(1, objectkeys.size());
        assertEquals(Long.valueOf(4321), objectkeys.get(0));
    }

    @Test
    public void toPOLandingpageEntries_prefetchForPoducts_withTemplates_bug12479() {
        // given
        List<Subscription> subscriptions = Arrays.asList(
                givenSubscription("sub_1", ServiceAccessType.LOGIN),
                givenSubscription("sub_2", ServiceAccessType.LOGIN));
        List<Product> services = new ArrayList<Product>();
        services.add(givenProduct(ServiceAccessType.LOGIN));
        Product template = new Product();
        template.setType(ServiceType.TEMPLATE);
        template.setVendor(givenSupplier());
        template.setTechnicalProduct(givenTechnicalProduct(ServiceAccessType.LOGIN));
        template.setKey(4322);
        template.setProductId("template");
        template.setStatus(ServiceStatus.ACTIVE);
        services.add(template);
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setVendor(givenSupplier());
        product.setTechnicalProduct(givenTechnicalProduct(ServiceAccessType.LOGIN));
        product.setKey(4323);
        product.setProductId("product");
        product.setStatus(ServiceStatus.ACTIVE);
        product.setTemplate(template);
        services.add(product);
        LocalizerFacade facadeMock = spy(facade);

        // when
        POLandingpageEntryAssembler.toPOLandingpageEntries(services,
                subscriptions, facadeMock);

        // than
        verify(facadeMock, times(2)).prefetch(objectKeyCaptor.capture(),
                Matchers.anyListOf(LocalizedObjectTypes.class));
        List<List<Long>> args = objectKeyCaptor.getAllValues();
        List<Long> objectkeys = args.get(0);
        assertEquals(3, objectkeys.size());
        assertEquals(Long.valueOf(4321), objectkeys.get(0));
        assertEquals(Long.valueOf(4322), objectkeys.get(1));
        assertEquals(Long.valueOf(4322), objectkeys.get(2));
    }
}
