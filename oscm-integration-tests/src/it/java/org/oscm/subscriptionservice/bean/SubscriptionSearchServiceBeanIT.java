/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.internal.intf.SubscriptionSearchService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.InvalidPhraseException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.ejb.FifoJMSQueue;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.enumtypes.UdaTargetType;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionSearchServiceBeanIT extends EJBTestBase {

    @BeforeClass
    public static void before() throws Exception {
        PERSISTENCE.clearEntityManagerFactoryCache();
        FifoJMSQueue indexerQueue = createIndexerQueue();
        indexerQueue.clear();
    }

    private SubscriptionSearchService sssb = new SubscriptionSearchServiceBean();
    private static long SUB1KEY;
    private static long SUB2KEY;
    private static long SUB3KEY;

    @SuppressWarnings("boxing")
    @Override
    public void setup(TestContainer container) throws Exception {
        DataService ds = new DataServiceBean();

        enableHibernateSearchListeners(true);
        FifoJMSQueue indexerQueue = createIndexerQueue();
        indexerQueue.clear();
        container.addBean(new ConfigurationServiceStub());
        container.addBean(ds);
        container.addBean(sssb);

        Organization tpAndSupplier = Organizations.createOrganization(ds,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Marketplace mp = Marketplaces.ensureMarketplace(tpAndSupplier,
                tpAndSupplier.getOrganizationId(), ds);
        Marketplaces.grantPublishing(tpAndSupplier, mp, ds, false);

        Organization org = Organizations.createOrganization(ds,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        org.addPlatformUser(
                Organizations.createUserForOrg(ds, org, true, "admin"));
        UdaDefinition udaDef1 = Udas.createUdaDefinition(ds, org,
                UdaTargetType.CUSTOMER_SUBSCRIPTION, "test", "default",
                UdaConfigurationType.USER_OPTION_OPTIONAL);
        UdaDefinition udaDef2 = Udas.createUdaDefinition(ds, org,
                UdaTargetType.CUSTOMER_SUBSCRIPTION, "test2", "normal",
                UdaConfigurationType.USER_OPTION_OPTIONAL);

        org.setUdaDefinitions(Arrays.asList(udaDef1, udaDef2));

        TechnicalProduct techProd = TechnicalProducts.createTechnicalProduct(ds,
                org, "techProd", false, ServiceAccessType.DIRECT);
        Product prod = Products.createProduct(org, techProd, false, "prodId",
                null, ds);

        ParameterDefinition paramDef = TechnicalProducts.addParameterDefinition(
                ParameterValueType.STRING, "test",
                ParameterType.SERVICE_PARAMETER, techProd, ds, 0L, 1L, true);

        Parameter param = Products.createParameter(paramDef, prod, ds);
        param.setValue("param");

        Organization customer = Organizations.createCustomer(ds, org);
        customer.addPlatformUser(
                Organizations.createUserForOrg(ds, customer, true, "admin"));
        Subscription sub1 = Subscriptions.createSubscription(ds,
                customer.getOrganizationId(), "prodId", "one", org);
        sub1.setPurchaseOrderNumber("reference");
        sub1.setMarketplace(mp);

        SUB1KEY = sub1.getKey();

        Subscription sub2 = Subscriptions.createSubscription(ds,
                customer.getOrganizationId(), "prodId", "two", org);
        sub2.setPurchaseOrderNumber(null);
        sub2.setMarketplace(mp);

        SUB2KEY = sub2.getKey();

        Subscription sub3 = Subscriptions.createSubscription(ds,
                customer.getOrganizationId(), "prodId",
                "search+-&&||!(){}[]^\"~*?:\\", org);
        sub3.setMarketplace(mp);

        SUB3KEY = sub3.getKey();

        Uda uda = Udas.createUda(ds, sub1, udaDef1, "value");

        udaDef1.setUdas(Arrays.asList(uda));

        ds.flush();

    }

    @Test
    public void searchSubRefParamUda() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    Collection<Long> col = sssb.searchSubscriptions(
                            "one reference param value normal");
                    assertEquals(1, col.size());
                    assertEquals(new Long(SUB1KEY), col.iterator().next());
                } catch (InvalidPhraseException | ObjectNotFoundException e) {
                    fail();
                }

                return null;
            }
        });

    }

    @Test
    public void searchTwoSubs() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    Collection<Long> col = sssb.searchSubscriptions("one two");
                    assertEquals(0, col.size());
                } catch (InvalidPhraseException | ObjectNotFoundException e) {
                    fail();
                }
                return null;
            }
        });
    }

    @Test
    public void searchSubWithDefaultUda() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    Collection<Long> col = sssb
                            .searchSubscriptions("two default normal");
                    assertEquals(1, col.size());
                    assertEquals(new Long(SUB2KEY), col.iterator().next());
                } catch (InvalidPhraseException | ObjectNotFoundException e) {
                    fail();
                }

                return null;
            }
        });

    }

    @Test
    public void searchSub4SpecialCharacters() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    Collection<Long> col = sssb.searchSubscriptions(
                            "search+-&&||!(){}[]^\"~\\*\\?:\\\\");
                    assertEquals(1, col.size());
                    assertEquals(new Long(SUB3KEY), col.iterator().next());
                } catch (InvalidPhraseException | ObjectNotFoundException e) {
                    fail();
                }
                return null;
            }
        });

    }

    @Test
    public void searchSubWithPartialWords() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    Collection<Long> col = sssb
                            .searchSubscriptions("two default normal");
                    final Collection<Long> col2 = sssb.searchSubscriptions("tw fault orm");
                    assertTrue(col.size() == col2.size());
                } catch (InvalidPhraseException | ObjectNotFoundException e) {
                    fail();
                }

                return null;
            }
        });
    }

    @Test
    public void searchSubWithPartialWords_SpecialCharacters() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    Collection<Long> col = sssb
                            .searchSubscriptions("two default+-&&||!(){}[]^\"~*?:\\ normal");
                    final Collection<Long> col2 = sssb.searchSubscriptions("tw fault orm+-&&||!(){}[]^\"~*?:\\");
                    assertTrue(col.size() == col2.size());
                } catch (InvalidPhraseException | ObjectNotFoundException e) {
                    fail();
                }

                return null;
            }
        });
    }

}
