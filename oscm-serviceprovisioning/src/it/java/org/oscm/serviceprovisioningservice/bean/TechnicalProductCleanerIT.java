/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 25.10.2010                                                      
 *                                                                              
 *  Completion Time: 25.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Scenario;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;

import javax.ejb.EJBException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.Mockito.mock;

/**
 * @author weiser
 * 
 */
public class TechnicalProductCleanerIT extends EJBTestBase {

    private DataService mgr;
    private TenantProvisioningServiceBean tps;

    protected PlatformUser providerSupplierUser;
    protected PlatformUser customerUser;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }
        });

        container.addBean(mock(ApplicationServiceLocal.class));
        container.addBean(mock(TenantProvisioningServiceBean.class));

        mgr = container.get(DataService.class);
        tps = container.get(TenantProvisioningServiceBean.class);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                SupportedCountries.createSomeSupportedCountries(mgr);
                Scenario.setup(container, true);
                return null;
            }
        });
    }

    /**
     * Check if udas will be deleted when deleting subscriptions is possible
     * with same moddate.
     * 
     * @throws Exception
     */
    @Test
    public void testCleanupTechnicalProduct_ExpiredSubWithUdas()
            throws Exception {
        deleteProductsAndSubscriptions();
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    TechnicalProduct tp = Scenario.getTechnicalProduct();
                    tp = mgr.getReference(TechnicalProduct.class, tp.getKey());
                    TechnicalProductCleaner cleaner = new TechnicalProductCleaner(
                            mgr, tps);
                    cleaner.cleanupTechnicalProduct(tp);
                    return null;
                }

            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    List<DomainHistoryObject<?>> hist = mgr
                            .findHistory(Scenario.getSubscription());
                    DomainHistoryObject<?> historyObject = hist
                            .get(hist.size() - 1);
                    Assert.assertEquals(ModificationType.DELETE,
                            historyObject.getModtype());
                    Date moddate = historyObject.getModdate();
                    checkUdaExistence(Scenario.getSubUda1(), moddate);
                    checkUdaExistence(Scenario.getSubUda2(), moddate);
                    return null;
                }

            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }

    }

    private void deleteProductsAndSubscriptions() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                TechnicalProduct tp = Scenario.getTechnicalProduct();
                tp = mgr.getReference(TechnicalProduct.class, tp.getKey());
                List<Product> products = tp.getProducts();
                for (Product product : products) {
                    product.setStatus(ServiceStatus.DELETED);
                    Subscription sub = product.getOwningSubscription();
                    if (sub != null) {
                        sub.setStatus(SubscriptionStatus.DEACTIVATED);
                    }
                }
                return null;
            }

        });
    }

    protected void checkUdaExistence(Uda uda, Date moddate) {
        try {
            mgr.getReference(Uda.class, uda.getKey());
            Assert.fail("Uda found: " + uda.getKey());
        } catch (ObjectNotFoundException e) {
            // expected
        }
        List<DomainHistoryObject<?>> history = mgr.findHistory(uda);
        DomainHistoryObject<?> historyObject = history.get(history.size() - 1);
        Assert.assertEquals(ModificationType.DELETE,
                historyObject.getModtype());
        Assert.assertEquals(moddate, historyObject.getModdate());
    }

}
