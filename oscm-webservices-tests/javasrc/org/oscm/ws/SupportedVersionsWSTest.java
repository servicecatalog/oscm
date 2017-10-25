/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.11.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import org.junit.Test;

import org.oscm.intf.AccountService;
import org.oscm.intf.BillingService;
import org.oscm.intf.CategorizationService;
import org.oscm.intf.DiscountService;
import org.oscm.intf.EventService;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.OrganizationalUnitService;
import org.oscm.intf.ReportingService;
import org.oscm.intf.ReviewService;
import org.oscm.intf.SearchService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.intf.SessionService;
import org.oscm.intf.SubscriptionService;
import org.oscm.intf.TagService;
import org.oscm.intf.TriggerDefinitionService;
import org.oscm.intf.TriggerService;
import org.oscm.intf.VatService;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;

//import com.sun.xml.ws.fault.ServerSOAPFaultException;

/**
 * @author stavreva
 * 
 */
public class SupportedVersionsWSTest {

//    @Test(expected = ServerSOAPFaultException.class)
//    public void AccountServiceTest() throws Exception {
//        AccountService wsProxy = getService(AccountService.class);
//        wsProxy.getOrganizationData();
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void BillingServiceTest() throws Exception {
//        BillingService wsProxy = getService(BillingService.class);
//        wsProxy.getRevenueShareData(null, null, null);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void CategorizationServiceTest() throws Exception {
//        CategorizationService wsProxy = getService(CategorizationService.class);
//        wsProxy.getServicesForCategory(0);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void DiscountServiceTest() throws Exception {
//        DiscountService wsProxy = getService(DiscountService.class);
//        wsProxy.getDiscountForService(0);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void EventServiceTest() throws Exception {
//        EventService wsProxy = getService(EventService.class);
//        wsProxy.recordEventForSubscription(0, null);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void IdentityServiceTest() throws Exception {
//        IdentityService wsProxy = getService(IdentityService.class);
//        wsProxy.cleanUpCurrentUser();
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void MarketplaceServiceTest() throws Exception {
//        MarketplaceService wsProxy = getService(MarketplaceService.class);
//        wsProxy.getMarketplacesForOperator();
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void OrganizationalUnitServiceTest() throws Exception {
//        OrganizationalUnitService wsProxy = getService(OrganizationalUnitService.class);
//        wsProxy.deleteUnit(null);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void ReportingServiceTest() throws Exception {
//        ReportingService wsProxy = getService(ReportingService.class);
//        wsProxy.getAvailableReports(null);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void ReviewServiceTest() throws Exception {
//        ReviewService wsProxy = getService(ReviewService.class);
//        wsProxy.writeReview(null);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void SearchServiceTest() throws Exception {
//        SearchService wsProxy = getService(SearchService.class);
//        wsProxy.searchServices(null, null, null);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void ServiceProvisioningServiceTest() throws Exception {
//        ServiceProvisioningService wsProxy = getService(ServiceProvisioningService.class);
//        wsProxy.getSuppliedServices();
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void SessionServiceTest() throws Exception {
//        SessionService wsProxy = getService(SessionService.class);
//        wsProxy.getNumberOfServiceSessions(0);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void SubscriptionServiceTest() throws Exception {
//        SubscriptionService wsProxy = getService(SubscriptionService.class);
//        wsProxy.getSubscriptionIdentifiers();
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void TagServiceTest() throws Exception {
//        TagService wsProxy = getService(TagService.class);
//        wsProxy.getTagsByLocale(null);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void TriggerDefinitionServiceTest() throws Exception {
//        TriggerDefinitionService wsProxy = getService(TriggerDefinitionService.class);
//        wsProxy.deleteTriggerDefinition(0);
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void TriggerServiceTest() throws Exception {
//        TriggerService wsProxy = getService(TriggerService.class);
//        wsProxy.getAllActions();
//    }
//
//    @Test(expected = ServerSOAPFaultException.class)
//    public void VatServiceTest() throws Exception {
//        VatService wsProxy = getService(VatService.class);
//        wsProxy.getDefaultVat();
//    }
//
//    private <T> T getService(Class<T> remoteInterface) throws Exception {
//        return ServiceFactory.getDefault().connectToWebService(remoteInterface,
//                WebserviceTestBase.getPlatformOperatorKey(),
//                WebserviceTestBase.getPlatformOperatorPassword(), "v1.2");
//    }
}
