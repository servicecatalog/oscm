/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 14.10.2011                                                      
 *                                                                              
 *  Completion Time: 14.10.2011                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.retrieval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.PSPAccountHistory;
import org.oscm.domobjects.PSPHistory;
import org.oscm.domobjects.PSPSettingHistory;
import org.oscm.domobjects.PaymentInfoHistory;
import org.oscm.domobjects.PaymentTypeHistory;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.PSPProcessingException;
import org.oscm.paymentservice.data.PaymentHistoryData;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class PaymentHistoryReaderIT extends EJBTestBase {

    private DataService ds;
    private PaymentHistoryReader reader;
    private long subscriptionKey, prodKey;

    private int keyCounter = 1;
    private int versionCounter = 1;

    @Override
    protected void setup(TestContainer container) throws Exception {
        ds = new DataServiceBean();
        container.addBean(new ConfigurationServiceStub());
        container.addBean(ds);
        reader = new PaymentHistoryReader(ds);
        initData();
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_NullDS() throws Exception {
        new PaymentHistoryReader(null);
    }

    @Test(expected = PSPProcessingException.class)
    public void getPaymentHistory_NonExistingSub() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                reader.getPaymentHistory(-12);
                return null;
            }
        });
    }

    @Test(expected = PSPProcessingException.class)
    public void getPaymentHistory_NoPaymentInformation() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deleteDbEntries(PaymentInfoHistory.class);
                reader.getPaymentHistory(subscriptionKey);
                return null;
            }
        });
    }

    @Test(expected = PSPProcessingException.class)
    public void getPaymentHistory_NoPaymentType() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deleteDbEntries(PaymentTypeHistory.class);
                reader.getPaymentHistory(subscriptionKey);
                return null;
            }
        });
    }

    @Test(expected = PSPProcessingException.class)
    public void getPaymentHistory_NoPSP() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deleteDbEntries(PSPHistory.class);
                reader.getPaymentHistory(subscriptionKey);
                return null;
            }
        });
    }

    @Test
    public void getPaymentHistory_NoPSPAccount() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deleteDbEntries(PSPAccountHistory.class);
                PaymentHistoryData data = reader
                        .getPaymentHistory(subscriptionKey);
                assertNull(data.getPspAccountHistory());
                return null;
            }
        });
    }

    @Test
    public void getPaymentHistory_NoPSPSettings() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deleteDbEntries(PSPSettingHistory.class);
                PaymentHistoryData ph = reader
                        .getPaymentHistory(subscriptionKey);
                PaymentInfoHistory pih = ph.getPaymentInfoHistory();
                assertEquals("accountNumber2", pih.getAccountNumber());
                PaymentTypeHistory pth = ph.getPaymentTypeHistory();
                assertEquals("CREDIT_CARD2",
                        pth.getDataContainer().getPaymentTypeId());
                PSPHistory pspHistory = ph.getPspHistory();
                assertEquals("distinguishedName2",
                        pspHistory.getDataContainer().getDistinguishedName());
                assertNotNull(ph.getPspSettingsHistory());
                assertTrue(ph.getPspSettingsHistory().isEmpty());
                return null;
            }
        });
    }

    @Test
    public void getPaymentHistory_SubscriptionSuspended() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SubscriptionHistory sh2 = new SubscriptionHistory();
                setInitialHistData(sh2, new Date(60000));
                sh2.setObjKey(subscriptionKey);
                sh2.setProductObjKey(prodKey);
                sh2.getDataContainer().setCreationDate(
                        Long.valueOf(new Date(20000).getTime()));
                sh2.getDataContainer()
                        .setStatus(SubscriptionStatus.DEACTIVATED);
                sh2.getDataContainer().setSubscriptionId("subId");
                sh2.setPaymentInfoObjKey(null);
                sh2.setCutOffDay(1);
                ds.persist(sh2);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PaymentHistoryData ph = reader
                        .getPaymentHistory(subscriptionKey);
                PaymentInfoHistory pih = ph.getPaymentInfoHistory();
                assertEquals("accountNumber2", pih.getAccountNumber());
                PaymentTypeHistory pth = ph.getPaymentTypeHistory();
                assertEquals("CREDIT_CARD2",
                        pth.getDataContainer().getPaymentTypeId());
                PSPHistory pspHistory = ph.getPspHistory();
                assertEquals("distinguishedName2",
                        pspHistory.getDataContainer().getDistinguishedName());
                assertEquals("psp_identifier2",
                        ph.getPspAccountHistory().getPspIdentifier());
                assertNotNull(ph.getPspSettingsHistory());
                assertEquals(1, ph.getPspSettingsHistory().size());
                return null;
            }
        });
    }

    @Test
    public void getPaymentHistory_Positive() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PaymentHistoryData ph = reader
                        .getPaymentHistory(subscriptionKey);
                PaymentInfoHistory pih = ph.getPaymentInfoHistory();
                assertEquals("accountNumber2", pih.getAccountNumber());
                PaymentTypeHistory pth = ph.getPaymentTypeHistory();
                assertEquals("CREDIT_CARD2",
                        pth.getDataContainer().getPaymentTypeId());
                PSPHistory pspHistory = ph.getPspHistory();
                assertEquals("distinguishedName2",
                        pspHistory.getDataContainer().getDistinguishedName());
                assertEquals("psp_identifier2",
                        ph.getPspAccountHistory().getPspIdentifier());
                assertNotNull(ph.getPspSettingsHistory());
                assertEquals(1, ph.getPspSettingsHistory().size());
                return null;
            }
        });
    }

    private void initData() throws Exception {
        final Date modDate1 = new Date(10000);
        final Date modDate2 = new Date(50000);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // create two entries for every history type of subscription,
                // paymentinfo, paymenttype, psp, pspsetting, pspaccount
                OrganizationHistory supplier = new OrganizationHistory();
                setInitialHistData(supplier, modDate1);
                supplier.getDataContainer().setOrganizationId("xyz_supplier");
                supplier.setCutOffDay(1);
                ds.persist(supplier);

                PSPHistory psph1 = new PSPHistory();
                setInitialHistData(psph1, modDate1);
                psph1.setObjKey(++keyCounter);
                psph1.getDataContainer().setIdentifier("pspid");
                psph1.getDataContainer().setWsdlUrl("http://www.google.de");
                ds.persist(psph1);
                PSPHistory psph2 = new PSPHistory();
                setInitialHistData(psph2, modDate2);
                psph2.setObjKey(psph1.getObjKey());
                psph2.getDataContainer().setIdentifier(psph1.getIdentifier());
                psph2.getDataContainer().setWsdlUrl(psph1.getWsdlUrl());
                psph2.getDataContainer()
                        .setDistinguishedName("distinguishedName2");
                ds.persist(psph2);

                PSPSettingHistory pspsh1 = new PSPSettingHistory();
                setInitialHistData(pspsh1, modDate1);
                pspsh1.setObjKey(++keyCounter);
                pspsh1.setSettingKey("settingKey");
                pspsh1.setSettingValue("settingValue");
                pspsh1.setPspObjKey(psph1.getObjKey());
                ds.persist(pspsh1);
                PSPSettingHistory pspsh2 = new PSPSettingHistory();
                setInitialHistData(pspsh2, modDate2);
                pspsh2.setObjKey(pspsh1.getObjKey());
                pspsh2.setSettingKey("settingKey");
                pspsh2.setSettingValue("settingValue");
                pspsh2.setPspObjKey(psph1.getObjKey());
                ds.persist(pspsh2);

                PaymentTypeHistory pth1 = new PaymentTypeHistory();
                setInitialHistData(pth1, modDate1);
                pth1.setObjKey(++keyCounter);
                pth1.getDataContainer().setCollectionType(
                        PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
                pth1.getDataContainer().setPaymentTypeId("CREDIT_CARD");
                pth1.setPspObjKey(psph1.getObjKey());
                ds.persist(pth1);
                PaymentTypeHistory pth2 = new PaymentTypeHistory();
                setInitialHistData(pth2, modDate2);
                pth2.setObjKey(pth1.getObjKey());
                pth2.getDataContainer().setCollectionType(
                        PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
                pth2.getDataContainer().setPaymentTypeId("CREDIT_CARD2");
                pth2.setPspObjKey(psph1.getObjKey());
                ds.persist(pth2);

                PaymentInfoHistory ph1 = new PaymentInfoHistory();
                setInitialHistData(ph1, modDate1);
                ph1.setObjKey(++keyCounter);
                ph1.getDataContainer().setAccountNumber("accountNumber");
                ph1.getDataContainer()
                        .setExternalIdentifier("externalIdentifier");
                ph1.getDataContainer().setPaymentInfoId("paymentInfoId");
                ph1.getDataContainer().setProviderName("providerName");
                ph1.setPaymentTypeObjKey(pth1.getObjKey());
                ds.persist(ph1);

                PaymentInfoHistory ph2 = new PaymentInfoHistory();
                setInitialHistData(ph2, modDate2);
                ph2.setObjKey(ph1.getObjKey());
                ph2.getDataContainer()
                        .setAccountNumber(ph1.getAccountNumber() + "2");
                ph2.getDataContainer()
                        .setExternalIdentifier(ph1.getExternalIdentifier());
                ph2.getDataContainer().setPaymentInfoId(ph1.getPaymentInfoId());
                ph2.getDataContainer().setProviderName(ph1.getProviderName());
                ph2.setPaymentTypeObjKey(pth1.getObjKey());
                ds.persist(ph2);

                PSPAccountHistory pah1 = new PSPAccountHistory();
                setInitialHistData(pah1, modDate1);
                pah1.setObjKey(++keyCounter);
                pah1.setPspObjKey(psph1.getObjKey());
                pah1.setPspIdentifier("psp_identifier");
                pah1.setOrganizationObjKey(supplier.getObjKey());
                ds.persist(pah1);
                PSPAccountHistory pah2 = new PSPAccountHistory();
                setInitialHistData(pah2, modDate2);
                pah2.setObjKey(pah1.getObjKey());
                pah2.setPspObjKey(psph1.getObjKey());
                pah2.setPspIdentifier("psp_identifier2");
                pah2.setOrganizationObjKey(supplier.getObjKey());
                ds.persist(pah2);

                ProductHistory prod = new ProductHistory();
                setInitialHistData(prod, modDate1);
                prod.getDataContainer().setProductId("xyz_product");
                prod.getDataContainer().setStatus(ServiceStatus.ACTIVE);
                prod.getDataContainer().setType(ServiceType.TEMPLATE);
                prod.setObjKey(prodKey = ++keyCounter);
                prod.setVendorObjKey(supplier.getObjKey());
                ds.persist(prod);

                subscriptionKey = ++keyCounter;
                SubscriptionHistory sh1 = new SubscriptionHistory();
                setInitialHistData(sh1, modDate1);
                sh1.setObjKey(subscriptionKey);
                sh1.setProductObjKey(prod.getObjKey());
                sh1.getDataContainer()
                        .setCreationDate(Long.valueOf(modDate1.getTime()));
                sh1.getDataContainer().setStatus(SubscriptionStatus.ACTIVE);
                sh1.getDataContainer().setSubscriptionId("subId");
                sh1.setPaymentInfoObjKey(Long.valueOf(ph1.getObjKey()));
                sh1.setCutOffDay(1);
                ds.persist(sh1);
                SubscriptionHistory sh2 = new SubscriptionHistory();
                setInitialHistData(sh2, modDate2);
                sh2.setObjKey(subscriptionKey);
                sh2.setProductObjKey(prod.getObjKey());
                sh2.getDataContainer()
                        .setCreationDate(Long.valueOf(modDate1.getTime()));
                sh2.getDataContainer().setStatus(SubscriptionStatus.ACTIVE);
                sh2.getDataContainer().setSubscriptionId("subId");
                sh2.setPaymentInfoObjKey(Long.valueOf(ph1.getObjKey()));
                sh2.setCutOffDay(1);
                ds.persist(sh2);

                return null;
            }
        });
    }

    private void setInitialHistData(DomainHistoryObject<?> data, Date date) {
        data.setObjVersion(++versionCounter);
        data.setInvocationDate(date);
        data.setModdate(date);
        data.setModtype(ModificationType.MODIFY);
        data.setModuser("1000");
    }

    private void deleteDbEntries(
            Class<? extends DomainHistoryObject<?>> clazz) {
        Query query = ds.createQuery(
                String.format("DELETE FROM %s x", clazz.getSimpleName()));
        query.executeUpdate();
    }

}
