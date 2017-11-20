/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 5, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;

import org.junit.Test;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOSubscription;

public class SubscriptionServiceBeanResellerIT extends EJBTestBase {

    SubscriptionService subscrService;
    TerminateSubscriptionBean terminateBean;

    DataService dsMock;
    Query queryMock;
    SessionServiceLocal sessionMgmtMock;
    ApplicationServiceLocal appManagerMock;
    CommunicationServiceLocal commServMock;
    SendMailPayload asyncMail;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        SubscriptionServiceBean subSrvBean = new SubscriptionServiceBean();
        container.addBean(subSrvBean);
        container.addBean(new TerminateSubscriptionBean());
        subscrService = container.get(SubscriptionService.class);
        terminateBean = container.get(TerminateSubscriptionBean.class);

        dsMock = terminateBean.dataManager;
        sessionMgmtMock = terminateBean.prodSessionMgmt;
        appManagerMock = terminateBean.appManager;
        commServMock = terminateBean.commService;
        queryMock = mock(Query.class);

        subSrvBean.dataManager = dsMock;
        terminateBean.tqs = new TaskQueueServiceLocal() {
            @Override
            public void sendAllMessages(List<TaskMessage> messages) {
                asyncMail = (SendMailPayload) messages.get(0).getPayload();
            }
        };
        doReturn("{0}").when(terminateBean.localizer)
                .getLocalizedTextFromBundle(
                        eq(LocalizedObjectTypes.MAIL_CONTENT),
                        (Marketplace) any(), anyString(),
                        eq("SUBSCRIPTION_TERMINATED_BY_SUPPLIER_REASON"));
    }

    @Test
    public void terminateSubscription_asReseller() throws Exception {
        // given
        PlatformUser resellerAdmin = createReseller(12345L);
        container.login(resellerAdmin.getKey(),
                UserRoleType.RESELLER_MANAGER.name());
        PlatformUser customerAdmin = createCustomer();
        Product product = createProduct(resellerAdmin);

        UsageLicense usageLicense = new UsageLicense();
        usageLicense.setUser(customerAdmin);
        List<UsageLicense> usageLicenses = new ArrayList<UsageLicense>();
        List<PlatformUser> users = new ArrayList<>();
        users.add(customerAdmin);
        usageLicenses.add(usageLicense);

        String subscriptionId = "MySubscription";
        Subscription subscr = createSubscription(customerAdmin, product,
                usageLicenses, subscriptionId);
        VOSubscription voSubscr = createVOSubscription(subscr);

        when(dsMock.createNamedQuery(anyString())).thenReturn(queryMock);
        when(dsMock.getCurrentUser()).thenReturn(resellerAdmin);
        when(dsMock.getReference(Subscription.class, subscr.getKey()))
                .thenReturn(subscr);
        when(queryMock.getResultList()).thenReturn(users);

        final Session userSession = new Session();
        when(
                sessionMgmtMock.getProductSessionsForSubscriptionTKey(subscr
                        .getKey())).thenReturn(Arrays.asList(userSession));

        // when
        String reason = "I don't like this subscription";
        subscrService.terminateSubscription(voSubscr, reason);

        // then
        verify(dsMock).remove(userSession);
        verify(dsMock).remove(usageLicense);

        assertEquals(1, asyncMail.getMailObjects().size());
        assertEquals(customerAdmin.getKey(), asyncMail.getMailObjects().get(0)
                .getKey().longValue());
        assertEquals(EmailType.SUBSCRIPTION_USER_REMOVED, asyncMail
                .getMailObjects().get(0).getType());
        assertNull(asyncMail.getMailObjects().get(0).getMarketplaceKey());
        assertEquals(subscriptionId, asyncMail.getMailObjects().get(0)
                .getParams()[0]);

        verify(appManagerMock).deleteInstance(subscr);
        verify(commServMock).sendMail(customerAdmin,
                EmailType.SUBSCRIPTION_TERMINATED_BY_SUPPLIER,
                new Object[] { subscriptionId, reason }, null);

        assertTrue("Licensed user hasn't been revoked",
                usageLicenses.isEmpty());
        assertEquals("Wrong product status", product.getStatus(),
                ServiceStatus.DELETED);
        assertEquals("Subscription was not deactivated", subscr.getStatus(),
                SubscriptionStatus.DEACTIVATED);
        assertFalse("SubscriptionID was not changed", subscr
                .getSubscriptionId().equals(subscriptionId));
    }

    private VOSubscription createVOSubscription(Subscription subscr) {
        VOSubscription voSubscr = new VOSubscription();
        voSubscr.setKey(subscr.getKey());
        return voSubscr;
    }

    private Subscription createSubscription(PlatformUser customerAdmin,
            Product product, List<UsageLicense> usageLicenses,
            String subscriptionId) {
        long subscriptionKey = 8888L;
        Subscription subscr = new Subscription();
        subscr.setKey(subscriptionKey);
        subscr.setSubscriptionId(subscriptionId);
        subscr.setStatus(SubscriptionStatus.ACTIVE);
        subscr.bindToProduct(product);
        subscr.setOrganization(customerAdmin.getOrganization());
        subscr.setUsageLicenses(usageLicenses);
        return subscr;
    }

    private Product createProduct(PlatformUser resellerAdmin) {
        TechnicalProduct techProd = new TechnicalProduct();
        techProd.setAccessType(ServiceAccessType.LOGIN);
        Product product = new Product();
        product.setVendor(resellerAdmin.getOrganization());
        product.setTechnicalProduct(techProd);
        product.setStatus(ServiceStatus.ACTIVE);
        return product;
    }

    private PlatformUser createCustomer() {
        Organization customerOrg = new Organization();
        customerOrg.setKey(7777L);
        PlatformUser customerAdmin = new PlatformUser();
        customerAdmin.setKey(7778L);
        customerAdmin.setOrganization(customerOrg);
        RoleAssignment roleAss = new RoleAssignment();
        roleAss.setRole(new UserRole(UserRoleType.ORGANIZATION_ADMIN));
        roleAss.setUser(customerAdmin);
        customerAdmin.getAssignedRoles().add(roleAss);
        customerOrg.getPlatformUsers().add(customerAdmin);
        return customerAdmin;
    }

    private PlatformUser createReseller(long resellerKey) {
        Organization resellerOrg = new Organization();
        resellerOrg.setKey(4711L);
        PlatformUser resellerAdmin = new PlatformUser();
        resellerAdmin.setKey(resellerKey);
        resellerAdmin.setOrganization(resellerOrg);
        resellerOrg.getPlatformUsers().add(resellerAdmin);
        resellerAdmin.setLocale("en");
        return resellerAdmin;
    }
}
