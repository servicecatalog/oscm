/* 
 *  Copyright FUJITSU LIMITED 2017
 **
 * 
 */
package org.oscm.reportingservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.reportingservice.business.model.billing.RDOCustomerPaymentPreview;
import org.oscm.reportingservice.business.model.billing.RDODetailedBilling;
import org.oscm.reportingservice.business.model.billing.RDOPaymentPreviewSummary;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

/**
 * @author weiser
 * 
 */
public class ReportingResultCacheIT extends EJBTestBase {

    private static final String CACHEKEY_BILLING_1 = "SESSION1#0001";
    private static final String CACHEKEY_BILLING_2 = "SESSION2#0001";
    private static final String CACHEKEY_CUSTOMER_PP_1 = "SESSION1";
    private static final String CACHEKEY_CUSTOMER_PP_2 = "SESSION2";
    private RDODetailedBilling detailedBilling1;
    private RDODetailedBilling detailedBilling2;
    private RDOCustomerPaymentPreview customerPaymentPreview1;
    private RDOCustomerPaymentPreview customerPaymentPreview2;
    private long now;
    private long past = 240 * 10000L;
    private DataService dm;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        dm = container.get(DataService.class);

        now = System.currentTimeMillis();

        RDOPaymentPreviewSummary summOrg1 = new RDOPaymentPreviewSummary();
        summOrg1.setOrganizationName("OrgResult1");
        ArrayList<RDOPaymentPreviewSummary> summResult1 = new ArrayList<RDOPaymentPreviewSummary>();
        summResult1.add(summOrg1);

        RDOPaymentPreviewSummary summOrg2 = new RDOPaymentPreviewSummary();
        summOrg2.setOrganizationName("OrgResult2");
        ArrayList<RDOPaymentPreviewSummary> summResult2 = new ArrayList<RDOPaymentPreviewSummary>();
        summResult2.add(summOrg2);

        RDOSummary summOrg3 = new RDOSummary();
        summOrg3.setOrganizationName("OrgResult3");
        ArrayList<RDOSummary> summResult3 = new ArrayList<RDOSummary>();
        summResult3.add(summOrg3);

        RDOSummary summOrg4 = new RDOSummary();
        summOrg4.setOrganizationName("OrgResult4");
        ArrayList<RDOSummary> summResult4 = new ArrayList<RDOSummary>();
        summResult4.add(summOrg4);

        detailedBilling1 = new RDODetailedBilling();
        detailedBilling1.setEntryNr(10);
        detailedBilling1.setParentEntryNr(15);
        detailedBilling1.setSummaries(summResult3);

        detailedBilling2 = new RDODetailedBilling();
        detailedBilling2.setEntryNr(20);
        detailedBilling2.setParentEntryNr(25);
        detailedBilling2.setSummaries(summResult4);

        customerPaymentPreview1 = new RDOCustomerPaymentPreview();
        customerPaymentPreview1.setEntryNr(11);
        customerPaymentPreview1.setParentEntryNr(16);
        customerPaymentPreview1.setSummaries(summResult1);

        customerPaymentPreview2 = new RDOCustomerPaymentPreview();
        customerPaymentPreview2.setEntryNr(21);
        customerPaymentPreview2.setParentEntryNr(26);
        customerPaymentPreview2.setSummaries(summResult2);

    }

    @Test
    public void testEmpty() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                assertNull(ReportingResultCache.get(dm, CACHEKEY_BILLING_1));
                assertNull(ReportingResultCache.get(dm, CACHEKEY_BILLING_2));
                assertNull(ReportingResultCache.get(dm, CACHEKEY_CUSTOMER_PP_1));
                assertNull(ReportingResultCache.get(dm, CACHEKEY_CUSTOMER_PP_2));
                return null;
            }
        });
    }

    @Test
    public void testNullRDO() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                ReportingResultCache.put(dm, CACHEKEY_BILLING_1, now, null);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                assertNull(ReportingResultCache.get(dm, CACHEKEY_BILLING_1));
                return null;
            }
        });
    }

    @Test
    public void testCachekey() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                ReportingResultCache.put(dm, CACHEKEY_BILLING_1, now,
                        detailedBilling1);
                ReportingResultCache.put(dm, CACHEKEY_BILLING_1, now,
                        detailedBilling2);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                RDODetailedBilling resultBilling1 = (RDODetailedBilling) ReportingResultCache
                        .get(dm, CACHEKEY_BILLING_1);
                verifyResults(detailedBilling1, resultBilling1);
                return null;
            }
        });
    }

    @Test
    public void testOneEntry() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                ReportingResultCache.put(dm, CACHEKEY_BILLING_1, now,
                        detailedBilling1);
                ReportingResultCache.put(dm, CACHEKEY_CUSTOMER_PP_1, now,
                        customerPaymentPreview1);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                RDODetailedBilling resultBilling1 = (RDODetailedBilling) ReportingResultCache
                        .get(dm, CACHEKEY_BILLING_1);
                verifyResults(detailedBilling1, resultBilling1);
                assertNull(ReportingResultCache.get(dm, CACHEKEY_BILLING_2));

                RDOCustomerPaymentPreview resultCustomer1 = (RDOCustomerPaymentPreview) ReportingResultCache
                        .get(dm, CACHEKEY_CUSTOMER_PP_1);
                verifyResults(customerPaymentPreview1, resultCustomer1);
                assertNull(ReportingResultCache.get(dm, CACHEKEY_CUSTOMER_PP_2));
                return null;
            }
        });
    }

    @Test
    public void testTwoEntries() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                ReportingResultCache.put(dm, CACHEKEY_BILLING_1, now,
                        detailedBilling1);
                ReportingResultCache.put(dm, CACHEKEY_BILLING_2, now,
                        detailedBilling2);
                ReportingResultCache.put(dm, CACHEKEY_CUSTOMER_PP_1, now,
                        customerPaymentPreview1);
                ReportingResultCache.put(dm, CACHEKEY_CUSTOMER_PP_2, now,
                        customerPaymentPreview2);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                RDODetailedBilling resultBilling1 = (RDODetailedBilling) ReportingResultCache
                        .get(dm, CACHEKEY_BILLING_1);
                verifyResults(detailedBilling1, resultBilling1);
                RDODetailedBilling resultBilling2 = (RDODetailedBilling) ReportingResultCache
                        .get(dm, CACHEKEY_BILLING_2);
                verifyResults(detailedBilling2, resultBilling2);

                RDOCustomerPaymentPreview resultCustomer1 = (RDOCustomerPaymentPreview) ReportingResultCache
                        .get(dm, CACHEKEY_CUSTOMER_PP_1);
                verifyResults(customerPaymentPreview1, resultCustomer1);
                RDOCustomerPaymentPreview resultCustomer2 = (RDOCustomerPaymentPreview) ReportingResultCache
                        .get(dm, CACHEKEY_CUSTOMER_PP_2);
                verifyResults(customerPaymentPreview2, resultCustomer2);

                return null;
            }
        });

    }

    @Test
    public void testOneToOld() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                ReportingResultCache.put(dm, CACHEKEY_BILLING_1, now,
                        detailedBilling1);
                ReportingResultCache.put(dm, CACHEKEY_BILLING_2, (now - past),
                        detailedBilling2);
                ReportingResultCache.put(dm, CACHEKEY_CUSTOMER_PP_1, now,
                        customerPaymentPreview1);
                ReportingResultCache.put(dm, CACHEKEY_CUSTOMER_PP_2,
                        (now - past), customerPaymentPreview2);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                RDODetailedBilling resultBilling1 = (RDODetailedBilling) ReportingResultCache
                        .get(dm, CACHEKEY_BILLING_1);
                verifyResults(detailedBilling1, resultBilling1);
                assertNull(ReportingResultCache.get(dm, CACHEKEY_BILLING_2));
                RDOCustomerPaymentPreview resultCustomer1 = (RDOCustomerPaymentPreview) ReportingResultCache
                        .get(dm, CACHEKEY_CUSTOMER_PP_1);
                verifyResults(customerPaymentPreview1, resultCustomer1);
                assertNull(ReportingResultCache.get(dm, CACHEKEY_CUSTOMER_PP_2));

                return null;
            }
        });
    }

    @Test
    public void testTwoToOld() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                ReportingResultCache.put(dm, CACHEKEY_BILLING_1, (now - past),
                        detailedBilling1);
                ReportingResultCache.put(dm, CACHEKEY_BILLING_2, (now - past),
                        detailedBilling2);
                ReportingResultCache.put(dm, CACHEKEY_CUSTOMER_PP_1,
                        (now - past), customerPaymentPreview1);
                ReportingResultCache.put(dm, CACHEKEY_CUSTOMER_PP_2,
                        (now - past), customerPaymentPreview2);

                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                assertNull(ReportingResultCache.get(dm, CACHEKEY_BILLING_1));
                assertNull(ReportingResultCache.get(dm, CACHEKEY_BILLING_2));
                assertNull(ReportingResultCache.get(dm, CACHEKEY_CUSTOMER_PP_1));
                assertNull(ReportingResultCache.get(dm, CACHEKEY_CUSTOMER_PP_2));
                return null;
            }
        });
    }

    private void verifyResults(RDODetailedBilling resultExpected,
            RDODetailedBilling resultCurrent) {
        assertEquals(resultExpected.getEntryNr(), resultCurrent.getEntryNr());
        assertEquals(resultExpected.getParentEntryNr(),
                resultCurrent.getParentEntryNr());
        List<RDOSummary> summListExpected = resultExpected.getSummaries();
        List<RDOSummary> summListCurrent = resultCurrent.getSummaries();
        RDOSummary summExpected = summListExpected.get(0);
        RDOSummary summCurrent = summListCurrent.get(0);

        assertEquals(summListExpected.size(), summListCurrent.size());
        assertEquals(summExpected.getOrganizationName(),
                summCurrent.getOrganizationName());
    }

    private void verifyResults(RDOCustomerPaymentPreview resultExpected,
            RDOCustomerPaymentPreview resultCurrent) {
        assertEquals(resultExpected.getEntryNr(), resultCurrent.getEntryNr());
        assertEquals(resultExpected.getParentEntryNr(),
                resultCurrent.getParentEntryNr());
        List<RDOPaymentPreviewSummary> summListExpected = resultExpected
                .getSummaries();
        List<RDOPaymentPreviewSummary> summListCurrent = resultCurrent
                .getSummaries();
        RDOSummary summExpected = summListExpected.get(0);
        RDOSummary summCurrent = summListCurrent.get(0);

        assertEquals(summListExpected.size(), summListCurrent.size());
        assertEquals(summExpected.getOrganizationName(),
                summCurrent.getOrganizationName());
    }

}
