/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *  Completion Time: 19.09.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Products;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Test class for BilllingDataRetrievalServiceBean.
 * 
 */
public class BillingDataRetrievalServiceBeanProductIT extends EJBTestBase {

    private static final String PRODUCT_ID_1 = "PRODUCT_1";
    private static final String PRODUCT_ID_2 = "PRODUCT_2";

    private DataService dm;
    private BillingDataRetrievalServiceLocal bdr;

    @Override
    public void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());

        dm = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);
    }

    @Test
    public void loadProductTemplateHistoryForSubscriptionHistory_Subscription()
            throws Exception {
        // given
        final long TEMPLATE_KEY = 4711;
        final long SUB_PRODUCT_KEY = 4712;
        final long BILLING_PERIOD_END = DateTimeHandling
                .calculateMillis("2015-05-01 00:00:00");

        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 0, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 1, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-08 00:00:00"),
                SUB_PRODUCT_KEY, 0, ServiceType.SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-08 00:00:00"),
                SUB_PRODUCT_KEY, 1, ServiceType.SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));

        final SubscriptionHistory subHistory = new SubscriptionHistory();
        subHistory.setProductObjKey(SUB_PRODUCT_KEY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                ProductHistory prodHistory = bdr
                        .loadProductTemplateHistoryForSubscriptionHistory(
                                subHistory, BILLING_PERIOD_END);

                // then
                assertEquals("Wrong product ID", PRODUCT_ID_1,
                        prodHistory.getDataContainer().getProductId());
                return null;
            }
        });
    }

    @Test
    public void loadProductTemplateHistoryForSubscriptionHistory_Subscription_TemplID_Changed()
            throws Exception {
        // given
        final long TEMPLATE_KEY = 4711;
        final long SUB_PRODUCT_KEY = 4712;
        final long BILLING_PERIOD_END = DateTimeHandling
                .calculateMillis("2015-05-01 00:00:00");

        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 0, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 1, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-08 00:00:00"),
                SUB_PRODUCT_KEY, 0, ServiceType.SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-04-15 00:00:00"),
                SUB_PRODUCT_KEY, 1, ServiceType.SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-30 23:59:59"),
                TEMPLATE_KEY, 2, ServiceType.TEMPLATE, PRODUCT_ID_2, null);

        final SubscriptionHistory subHistory = new SubscriptionHistory();
        subHistory.setProductObjKey(SUB_PRODUCT_KEY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                ProductHistory prodHistory = bdr
                        .loadProductTemplateHistoryForSubscriptionHistory(
                                subHistory, BILLING_PERIOD_END);

                // then
                assertEquals("Wrong product ID", PRODUCT_ID_2,
                        prodHistory.getDataContainer().getProductId());
                return null;
            }
        });
    }

    @Test
    public void loadProductTemplateHistoryForSubscriptionHistory_Subscription_TemplID_Changed2()
            throws Exception {
        // given
        final long TEMPLATE_KEY = 4711;
        final long SUB_PRODUCT_KEY = 4712;
        final long BILLING_PERIOD_END = DateTimeHandling
                .calculateMillis("2015-05-01 00:00:00");

        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 0, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 1, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-08 00:00:00"),
                SUB_PRODUCT_KEY, 0, ServiceType.SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                TEMPLATE_KEY, 2, ServiceType.TEMPLATE, PRODUCT_ID_2, null);
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-05-01 00:00:01"),
                SUB_PRODUCT_KEY, 1, ServiceType.SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));

        final SubscriptionHistory subHistory = new SubscriptionHistory();
        subHistory.setProductObjKey(SUB_PRODUCT_KEY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                ProductHistory prodHistory = bdr
                        .loadProductTemplateHistoryForSubscriptionHistory(
                                subHistory, BILLING_PERIOD_END);

                // then
                assertEquals("Wrong product ID", PRODUCT_ID_1,
                        prodHistory.getDataContainer().getProductId());
                return null;
            }
        });
    }

    @Test
    public void loadProductTemplateHistoryForSubscriptionHistory_Customer_Subscription()
            throws Exception {
        // given
        final long TEMPLATE_KEY = 4711;
        final long CUSTOMER_TEMPLATE_KEY = 4712;
        final long CUSTOMER_SUB_PRODUCT_KEY = 4713;
        final long BILLING_PERIOD_END = DateTimeHandling
                .calculateMillis("2015-05-01 00:00:00");

        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 0, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-02 00:00:00"),
                TEMPLATE_KEY, 1, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-02 13:00:00"),
                CUSTOMER_TEMPLATE_KEY, 0, ServiceType.CUSTOMER_TEMPLATE,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-08 00:00:00"),
                CUSTOMER_SUB_PRODUCT_KEY, 0, ServiceType.CUSTOMER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-04-13 00:00:00"),
                CUSTOMER_SUB_PRODUCT_KEY, 1, ServiceType.CUSTOMER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(TEMPLATE_KEY));

        final SubscriptionHistory subHistory = new SubscriptionHistory();
        subHistory.setProductObjKey(CUSTOMER_SUB_PRODUCT_KEY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                ProductHistory prodHistory = bdr
                        .loadProductTemplateHistoryForSubscriptionHistory(
                                subHistory, BILLING_PERIOD_END);

                // then
                assertEquals("Wrong product ID", PRODUCT_ID_1,
                        prodHistory.getDataContainer().getProductId());
                return null;
            }
        });
    }

    @Test
    public void loadProductTemplateHistoryForSubscriptionHistory_Customer_Subscription_TemplID_Changed()
            throws Exception {
        // given
        final long TEMPLATE_KEY = 4711;
        final long CUSTOMER_TEMPLATE_KEY = 4712;
        final long CUSTOMER_SUB_PRODUCT_KEY = 4713;
        final long BILLING_PERIOD_END = DateTimeHandling
                .calculateMillis("2015-05-01 00:00:00");

        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 0, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-02 00:00:00"),
                TEMPLATE_KEY, 1, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-02 13:00:00"),
                CUSTOMER_TEMPLATE_KEY, 0, ServiceType.CUSTOMER_TEMPLATE,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-08 00:00:00"),
                CUSTOMER_SUB_PRODUCT_KEY, 0, ServiceType.CUSTOMER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-04-09 13:00:00"),
                CUSTOMER_TEMPLATE_KEY, 0, ServiceType.CUSTOMER_TEMPLATE,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-04-20 00:00:00"),
                CUSTOMER_SUB_PRODUCT_KEY, 1, ServiceType.CUSTOMER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-30 23:59:59"),
                TEMPLATE_KEY, 2, ServiceType.TEMPLATE, PRODUCT_ID_2, null);

        final SubscriptionHistory subHistory = new SubscriptionHistory();
        subHistory.setProductObjKey(CUSTOMER_SUB_PRODUCT_KEY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                ProductHistory prodHistory = bdr
                        .loadProductTemplateHistoryForSubscriptionHistory(
                                subHistory, BILLING_PERIOD_END);

                // then
                assertEquals("Wrong product ID", PRODUCT_ID_2,
                        prodHistory.getDataContainer().getProductId());
                return null;
            }
        });
    }

    @Test
    public void loadProductTemplateHistoryForSubscriptionHistory_Customer_Subscription_TemplID_Changed2()
            throws Exception {
        // given
        final long TEMPLATE_KEY = 4711;
        final long CUSTOMER_TEMPLATE_KEY = 4712;
        final long CUSTOMER_SUB_PRODUCT_KEY = 4713;
        final long BILLING_PERIOD_END = DateTimeHandling
                .calculateMillis("2015-05-01 00:00:00");

        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 0, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-02 00:00:00"),
                TEMPLATE_KEY, 1, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-02 13:00:00"),
                CUSTOMER_TEMPLATE_KEY, 0, ServiceType.CUSTOMER_TEMPLATE,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-08 00:00:00"),
                CUSTOMER_SUB_PRODUCT_KEY, 0, ServiceType.CUSTOMER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                TEMPLATE_KEY, 2, ServiceType.TEMPLATE, PRODUCT_ID_2, null);
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-05-01 00:00:01"),
                CUSTOMER_SUB_PRODUCT_KEY, 1, ServiceType.CUSTOMER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(TEMPLATE_KEY));

        final SubscriptionHistory subHistory = new SubscriptionHistory();
        subHistory.setProductObjKey(CUSTOMER_SUB_PRODUCT_KEY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                ProductHistory prodHistory = bdr
                        .loadProductTemplateHistoryForSubscriptionHistory(
                                subHistory, BILLING_PERIOD_END);

                // then
                assertEquals("Wrong product ID", PRODUCT_ID_1,
                        prodHistory.getDataContainer().getProductId());
                return null;
            }
        });
    }

    @Test
    public void loadProductTemplateHistoryForSubscriptionHistory_Partner_Subscription()
            throws Exception {
        // given
        final long TEMPLATE_KEY = 4711;
        final long PARTNER_TEMPLATE_KEY = 4712;
        final long PARTNER_SUB_PRODUCT_KEY = 4713;
        final long BILLING_PERIOD_END = DateTimeHandling
                .calculateMillis("2015-05-01 00:00:00");

        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 0, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-02 00:00:00"),
                TEMPLATE_KEY, 1, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-02 13:00:00"),
                PARTNER_TEMPLATE_KEY, 0, ServiceType.PARTNER_TEMPLATE,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-08 00:00:00"),
                PARTNER_SUB_PRODUCT_KEY, 0, ServiceType.PARTNER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(PARTNER_TEMPLATE_KEY));
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-04-09 13:00:00"),
                PARTNER_TEMPLATE_KEY, 0, ServiceType.PARTNER_TEMPLATE,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-04-13 00:00:00"),
                PARTNER_SUB_PRODUCT_KEY, 1, ServiceType.PARTNER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(PARTNER_TEMPLATE_KEY));

        final SubscriptionHistory subHistory = new SubscriptionHistory();
        subHistory.setProductObjKey(PARTNER_SUB_PRODUCT_KEY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                ProductHistory prodHistory = bdr
                        .loadProductTemplateHistoryForSubscriptionHistory(
                                subHistory, BILLING_PERIOD_END);

                // then
                assertEquals("Wrong product ID", PRODUCT_ID_1,
                        prodHistory.getDataContainer().getProductId());
                return null;
            }
        });
    }

    @Test
    public void loadProductTemplateHistoryForSubscriptionHistory_Partner_Subscription_TemplID_changed()
            throws Exception {
        // given
        final long TEMPLATE_KEY = 4711;
        final long PARTNER_TEMPLATE_KEY = 4712;
        final long PARTNER_SUB_PRODUCT_KEY = 4713;
        final long BILLING_PERIOD_END = DateTimeHandling
                .calculateMillis("2015-05-01 00:00:00");

        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 0, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-02 00:00:00"),
                TEMPLATE_KEY, 1, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-02 13:00:00"),
                PARTNER_TEMPLATE_KEY, 0, ServiceType.PARTNER_TEMPLATE,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-08 00:00:00"),
                PARTNER_SUB_PRODUCT_KEY, 0, ServiceType.PARTNER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(PARTNER_TEMPLATE_KEY));
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-04-09 13:00:00"),
                PARTNER_TEMPLATE_KEY, 0, ServiceType.PARTNER_TEMPLATE,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-04-20 00:00:00"),
                PARTNER_SUB_PRODUCT_KEY, 1, ServiceType.PARTNER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(PARTNER_TEMPLATE_KEY));
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-30 23:59:59"),
                TEMPLATE_KEY, 2, ServiceType.TEMPLATE, PRODUCT_ID_2, null);

        final SubscriptionHistory subHistory = new SubscriptionHistory();
        subHistory.setProductObjKey(PARTNER_SUB_PRODUCT_KEY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                ProductHistory prodHistory = bdr
                        .loadProductTemplateHistoryForSubscriptionHistory(
                                subHistory, BILLING_PERIOD_END);

                // then
                assertEquals("Wrong product ID", PRODUCT_ID_2,
                        prodHistory.getDataContainer().getProductId());
                return null;
            }
        });
    }

    @Test
    public void loadProductTemplateHistoryForSubscriptionHistory_Partner_Subscription_TemplID_changed2()
            throws Exception {
        // given
        final long TEMPLATE_KEY = 4711;
        final long PARTNER_TEMPLATE_KEY = 4712;
        final long PARTNER_SUB_PRODUCT_KEY = 4713;
        final long BILLING_PERIOD_END = DateTimeHandling
                .calculateMillis("2015-05-01 00:00:00");

        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                TEMPLATE_KEY, 0, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-04-02 00:00:00"),
                TEMPLATE_KEY, 1, ServiceType.TEMPLATE, PRODUCT_ID_1, null);
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-02 13:00:00"),
                PARTNER_TEMPLATE_KEY, 0, ServiceType.PARTNER_TEMPLATE,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.ADD,
                DateTimeHandling.calculateMillis("2015-04-08 00:00:00"),
                PARTNER_SUB_PRODUCT_KEY, 0, ServiceType.PARTNER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(PARTNER_TEMPLATE_KEY));
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-04-09 13:00:00"),
                PARTNER_TEMPLATE_KEY, 0, ServiceType.PARTNER_TEMPLATE,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631001",
                Long.valueOf(TEMPLATE_KEY));
        createProductHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                TEMPLATE_KEY, 2, ServiceType.TEMPLATE, PRODUCT_ID_2, null);
        createProductHistory(ModificationType.DELETE,
                DateTimeHandling.calculateMillis("2015-05-01 00:00:01"),
                PARTNER_SUB_PRODUCT_KEY, 1, ServiceType.PARTNER_SUBSCRIPTION,
                PRODUCT_ID_1 + "#d52726a7-7165-422a-9c73-210b206d27c631002",
                Long.valueOf(PARTNER_TEMPLATE_KEY));

        final SubscriptionHistory subHistory = new SubscriptionHistory();
        subHistory.setProductObjKey(PARTNER_SUB_PRODUCT_KEY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                ProductHistory prodHistory = bdr
                        .loadProductTemplateHistoryForSubscriptionHistory(
                                subHistory, BILLING_PERIOD_END);

                // then
                assertEquals("Wrong product ID", PRODUCT_ID_1,
                        prodHistory.getDataContainer().getProductId());
                return null;
            }
        });
    }

    private ProductHistory createProductHistory(
            final ModificationType modificationType, final long modDate,
            final long objKey, final long objVersion, final ServiceType type,
            final String productId, final Long templateObjKey)
            throws Exception {
        final ProductHistory productHistory = runTX(
                new Callable<ProductHistory>() {
                    @Override
                    public ProductHistory call() {
                        try {
                            return Products.createProductHistory(dm,
                                    modificationType, modDate, objKey,
                                    objVersion, type, productId,
                                    templateObjKey);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Assert.fail(
                                    "Error on creating product history data for test.");
                            return null;
                        }
                    }
                });
        return productHistory;
    }

}
