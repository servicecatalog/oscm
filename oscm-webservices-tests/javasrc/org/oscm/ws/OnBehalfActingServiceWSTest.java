/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                    
 *                                                                              
 *  Creation Date: 01.06.2011                                                      
 *                                                                              
 *  Completion Time: 03.06.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.TSXMLForWebService;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.intf.SessionService;
import org.oscm.intf.SubscriptionService;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.enumtypes.PaymentInfoType;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.UnchangeableAllowingOnBehalfActingException;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUserDetails;

public class OnBehalfActingServiceWSTest {

    private String commonUserPWD;
    private static VOFactory factory = new VOFactory();
    private String hostUserKey;
    private VOOrganization hostOrg;

    private String techProviderUserId;
    private VOOrganization techProviderOrg;
    private String techProviderUserKey;
    private VOSubscription subscription;

    private VOService activatedService;

    private MarketplaceService mpSrvAsSupplier;
    private VOMarketplace localMarketplace;

    @Before
    public void setup() throws Exception {

        // define test parameters
        commonUserPWD = "secret";

        // create host organization 'PaaS'
        WebserviceTestBase.getMailReader().deleteMails();
        String hostUserId = "PaaS_" + WebserviceTestBase.createUniqueKey();
        hostOrg = WebserviceTestBase.createOrganization(hostUserId,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        hostUserKey = WebserviceTestBase.readLastMailAndSetCommonPassword();
        WebserviceTestBase.savePaymentInfoToSupplier(hostOrg,
                PaymentInfoType.INVOICE);

        // Create the local market place
        MarketplaceService mpSrvOperator = ServiceFactory.getDefault()
                .getMarketPlaceService(
                        WebserviceTestBase.getPlatformOperatorKey(),
                        WebserviceTestBase.getPlatformOperatorPassword());
        localMarketplace = mpSrvOperator.createMarketplace(factory
                .createMarketplaceVO(hostOrg.getOrganizationId(), false,
                        "Local Marketplace"));
        mpSrvAsSupplier = ServiceFactory.getDefault().getMarketPlaceService(
                hostUserKey, WebserviceTestBase.DEFAULT_PASSWORD);

        // create Technology Provider 'TP1'
        WebserviceTestBase.getMailReader().deleteMails();
        techProviderUserId = "tp_" + WebserviceTestBase.createUniqueKey();
        techProviderOrg = WebserviceTestBase.createOrganization(
                techProviderUserId, OrganizationRoleType.TECHNOLOGY_PROVIDER);

        techProviderUserKey = WebserviceTestBase
                .readLastMailAndSetCommonPassword();
    }

    /**
     * In order to make the condition for onBehalfActing, the subscribable
     * service is created by hostUser('PaaS'), and technology provider subscribe
     * it. If 'allowingOnBehalfActing' flag is true, host user can create
     * OnBehalf user of the technology provider.
     * 
     * @param allowOnBehalfActing
     *            the flag setting 'allowBehalfActing' of technical service
     *            created
     * 
     * @throws Exception
     */
    private void createMarketableServiceAndSubscribe(boolean allowOnBehalfActing)
            throws Exception {

        // By PaaS: create Technical Service with OnBehalfActing flag and
        // marketable service and activate it
        activatedService = createMarketableService(allowOnBehalfActing);

        // By TP1: subscribe the service
        subscription = factory.createSubscriptionVO("sub_"
                + WebserviceTestBase.createUniqueKey());
        List<VOUsageLicense> users = new ArrayList<VOUsageLicense>();
        users.add(factory.createUsageLicenceVO(techProviderUserId));
        SubscriptionService subscriptionSV = ServiceFactory.getDefault()
                .getSubscriptionService(techProviderUserKey, commonUserPWD);
        subscription = subscriptionSV.subscribeToService(subscription,
                activatedService, users, null, null, new ArrayList<VOUda>());
    }

    private VOService createMarketableService(boolean allowOnBehalfActing)
            throws Exception {
        ServiceProvisioningService serviceProvisioningSV = ServiceFactory
                .getDefault().getServiceProvisioningService(hostUserKey,
                        commonUserPWD);

        String tsxml = TSXMLForWebService
                .createTSXMLWithAllowingOnBehalfActingConnectable(Boolean
                        .toString(allowOnBehalfActing));
        VOTechnicalService voTechnicalService = WebserviceTestBase
                .createTechnicalService(tsxml, serviceProvisioningSV);

        VOServiceDetails marketableServiceDetail = serviceProvisioningSV
                .createService(
                        voTechnicalService,
                        factory.createMarketableServiceVO("ms_"
                                + WebserviceTestBase.createUniqueKey()), null);
        WebserviceTestBase.publishToMarketplace(marketableServiceDetail, true,
                mpSrvAsSupplier, localMarketplace);
        VOPriceModel priceModel = factory.createPriceModelVO();
        marketableServiceDetail = serviceProvisioningSV.savePriceModel(
                marketableServiceDetail, priceModel);
        VOService service = serviceProvisioningSV
                .activateService(marketableServiceDetail);
        return service;

    }

    /**
     * Test for 'Blue Line'. When subscription of technical service having
     * 'true' allowingOnBehalfActing flag has existed, creating onBehalfUser
     * must be successful. After subscription is unsubscribed, logging in by
     * onBehalfUser and creating onBehalfUser must be failed.
     * 
     * @throws Exception
     */
    @Test
    public void testConditionOfCreateBehalfUser() throws Exception {
        // set up of blue line test
        createMarketableServiceAndSubscribe(true);

        // By PaaS: create OnBehalfUser
        IdentityService identifySV = ServiceFactory.getDefault()
                .getIdentityService(hostUserKey, commonUserPWD);
        VOUserDetails onBehalfUser = identifySV.createOnBehalfUser(
                techProviderOrg.getOrganizationId(), commonUserPWD);
        String onBehalfUserKey = Long.toString(onBehalfUser.getKey());

        // By OnBehalfUser: login to service
        long subscriptionKey = subscription.getKey();
        SessionService sessionSV = ServiceFactory.getDefault()
                .getSessionService(onBehalfUserKey, commonUserPWD);
        String sessionId = "session" + WebserviceTestBase.createUniqueKey();
        sessionSV.createServiceSession(subscriptionKey, sessionId, "token1");
        sessionSV.deleteServiceSession(subscriptionKey, sessionId);

        // By TP1: unsubscribe the service
        String subscriptionId = subscription.getSubscriptionId();
        SubscriptionService subscriptionSV = ServiceFactory.getDefault()
                .getSubscriptionService(techProviderUserKey, commonUserPWD);
        subscriptionSV.unsubscribeFromService(subscriptionId);

        // By OnBehalfUser: login to service must be failed
        sessionSV = ServiceFactory.getDefault().getSessionService(
                onBehalfUserKey, commonUserPWD);
        subscriptionId = subscription.getSubscriptionId();
        try {
            sessionSV.createServiceSession(subscription.getKey(), sessionId,
                    "token2");
            fail();
        } catch (Exception e) {
            // expected
            assertNotNull(e);
        }

        // By PaaS: creating OnBehalfUser is failed
        identifySV = ServiceFactory.getDefault().getIdentityService(
                hostUserKey, commonUserPWD);
        try {
            identifySV.createOnBehalfUser(techProviderOrg.getOrganizationId(),
                    commonUserPWD);
            fail();
        } catch (OperationNotPermittedException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test for 'Blue Line', however in this test onBehalfUser create another
     * subscription. At that case, if first subscription is deleted, the
     * onBehalfUser must not be deleted and another onBehalfUser also can be
     * made.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateTwoSubscription() throws Exception {
        // set up of blue line test
        createMarketableServiceAndSubscribe(true);

        // By PaaS: create OnBehalfUser
        IdentityService identifySV = ServiceFactory.getDefault()
                .getIdentityService(hostUserKey, commonUserPWD);
        VOUserDetails onBehalfUser = identifySV.createOnBehalfUser(
                techProviderOrg.getOrganizationId(), commonUserPWD);
        String onBehalfUserKey = Long.toString(onBehalfUser.getKey());

        // By OnBehalfUser: create another subscription
        VOSubscription subscription2 = factory.createSubscriptionVO("sub2_"
                + WebserviceTestBase.createUniqueKey());
        List<VOUsageLicense> users = new ArrayList<VOUsageLicense>();
        users.add(factory.createUsageLicenceVO(techProviderUserId));
        SubscriptionService subscriptionSV = ServiceFactory.getDefault()
                .getSubscriptionService(onBehalfUserKey, commonUserPWD);
        subscription2 = subscriptionSV.subscribeToService(subscription2,
                activatedService, users, null, null, new ArrayList<VOUda>());

        // By OnBehalfUser: unsubscribe the first subscription
        String subscriptionId = subscription.getSubscriptionId();
        subscriptionSV = ServiceFactory.getDefault().getSubscriptionService(
                Long.toString(onBehalfUser.getKey()), commonUserPWD);
        subscriptionSV.unsubscribeFromService(subscriptionId);

        // By OnBehalfUser: login to second subscription
        long subscriptionKey = subscription2.getKey();
        SessionService sessionSV = ServiceFactory.getDefault()
                .getSessionService(onBehalfUserKey, commonUserPWD);
        String sessionId = "session" + WebserviceTestBase.createUniqueKey();
        sessionSV.createServiceSession(subscriptionKey, sessionId, "token2");
        sessionSV.deleteServiceSession(subscriptionKey, sessionId);

        // By PaaS: create another OnBehalfUser
        identifySV = ServiceFactory.getDefault().getIdentityService(
                hostUserKey, commonUserPWD);
        identifySV.createOnBehalfUser(techProviderOrg.getOrganizationId(),
                commonUserPWD);

    }

    @Test
    public void testChangeOnBehalfFlag() throws Exception {
        // create Technical Service with OnBehalfActing flag and
        // marketable service and activate it
        createMarketableService(true);

        // By PaaS: change the OnBehalf Flag and re-import technical service
        String tsxml = TSXMLForWebService
                .createTSXMLWithAllowingOnBehalfActingConnectable("false");
        ServiceProvisioningService serviceProvisioningSV = ServiceFactory
                .getDefault().getServiceProvisioningService(hostUserKey,
                        commonUserPWD);
        serviceProvisioningSV.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }

    @Test(expected = UnchangeableAllowingOnBehalfActingException.class)
    public void testErrorChangeOnBehalfFlag() throws Exception {
        // set up of blue line test with false
        createMarketableServiceAndSubscribe(true);

        // By PaaS: change the OnBehalf Flag and re-import technical service
        String tsxml = TSXMLForWebService
                .createTSXMLWithAllowingOnBehalfActingConnectable("false");
        ServiceProvisioningService serviceProvisioningSV = ServiceFactory
                .getDefault().getServiceProvisioningService(hostUserKey,
                        commonUserPWD);
        serviceProvisioningSV.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }

    @Test
    public void testRemoveLastSubscriptionChangeOnBehalfFlag() throws Exception {
        createMarketableServiceAndSubscribe(true);

        // remove last subscription
        SubscriptionService subscriptionSV = ServiceFactory.getDefault()
                .getSubscriptionService(techProviderUserKey, commonUserPWD);
        subscriptionSV.unsubscribeFromService(subscription.getSubscriptionId());

        // By PaaS: change the OnBehalf Flag and re-import technical service
        String tsxml = TSXMLForWebService
                .createTSXMLWithAllowingOnBehalfActingConnectable("false");
        ServiceProvisioningService serviceProvisioningSV = ServiceFactory
                .getDefault().getServiceProvisioningService(hostUserKey,
                        commonUserPWD);
        serviceProvisioningSV.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }

    /**
     * Test for 'Blue Line' with 'false' flag. If subscription of technical
     * service has existed but it has 'false' allowingOnBehalfActing flag,
     * creating onBehalfUser must be failed.
     * 
     * @throws Exception
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testFailToCreateOnBehalfUserWithFalseFlag() throws Exception {

        // set up of blue line test with false flag
        createMarketableServiceAndSubscribe(false);

        // creating OnBehalfUser must be failed
        IdentityService identifySV = ServiceFactory.getDefault()
                .getIdentityService(hostUserKey, commonUserPWD);
        identifySV.createOnBehalfUser(techProviderOrg.getOrganizationId(),
                commonUserPWD);
        fail();
    }

    /**
     * Test for restriction. Without subscription, creating OnBehalfUser must be
     * failed.
     * 
     * @throws Exception
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testFailToCreateOnBehalfUserWithoutTechnicalService()
            throws Exception {

        // create Technical Service with OnBehalfActing flag and
        // marketable service and activate it
        createMarketableService(true);

        // creating OnBehalfUser must be failed
        String techProviderOrgId = techProviderOrg.getOrganizationId();
        IdentityService identifySV = ServiceFactory.getDefault()
                .getIdentityService(hostUserKey, commonUserPWD);
        identifySV.createOnBehalfUser(techProviderOrgId, commonUserPWD);
        fail();
    }

    /**
     * Test for restriction. Operator cannot call createOnBehalfUser.
     * 
     * @throws Exception
     */
    @Test
    public void testFailToCreateOnBehalfUserByOperator() throws Exception {

        // set up of blue line test with false
        createMarketableServiceAndSubscribe(true);

        // By PaaS: creating OnBehalf user must be failed
        String techProviderOrgId = techProviderOrg.getOrganizationId();
        IdentityService identifySV = ServiceFactory.getDefault()
                .getIdentityService();
        try {
            identifySV.createOnBehalfUser(techProviderOrgId, commonUserPWD);
            fail();
        } catch (Exception e) {
            if (e instanceof SOAPFaultException) {
                assertTrue(e.getMessage().contains("Unauthorized"));
            }
        }

    }

}
