/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 12.07.2011                                                      
 *                                                                              
 *  Completion Time: 12.07.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.permission;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.ejb.SessionContext;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.logging.Log4jLogger;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.MarketplaceToOrganization;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOSubscription;

/**
 * @author weiser
 * 
 */
public class PermissionCheckTest {

    private Organization owner;
    private Organization notOwner;
    private TechnicalProduct technicalProduct;

    private SessionContext sessionMock;
    private Log4jLogger loggerMock;
    private final long organizationKey = 1;

    private static final int LOGLEVEL_SYSTEM = Log4jLogger.SYSTEM_LOG;

    @Before
    public void setup() {
        owner = new Organization();
        owner.setOrganizationId("owner");
        owner.setKey(1234);

        notOwner = new Organization();
        notOwner.setOrganizationId("notOwner");
        notOwner.setKey(4321);

        technicalProduct = new TechnicalProduct();
        technicalProduct.setKey(9876);
        technicalProduct.setTechnicalProductId("TP Id");

        sessionMock = mock(SessionContext.class);
        loggerMock = mock(Log4jLogger.class);
    }

    @Test
    public void owns_BillingContact() throws Exception {
        BillingContact bc = new BillingContact();
        bc.setOrganization(owner);
        PermissionCheck.owns(bc, owner, loggerMock);
        verifyZeroInteractions(loggerMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_BillingContact_LoggerNull() throws Exception {
        BillingContact bc = new BillingContact();
        bc.setOrganization(owner);
        PermissionCheck.owns(bc, notOwner, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_BillingContact_NotOwner() throws Exception {
        BillingContact bc = new BillingContact();
        bc.setOrganization(owner);
        try {
            PermissionCheck.owns(bc, notOwner, loggerMock);
        } finally {
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_BILLING_CONTACT_ACCESS),
                            eq(notOwner.getOrganizationId()),
                            eq(String.valueOf(bc.getKey())));
        }
    }

    @Test
    public void owns_PaymentInfo() throws Exception {
        PaymentInfo pi = new PaymentInfo();
        pi.setOrganization(owner);
        PermissionCheck.owns(pi, owner, loggerMock);
        verifyZeroInteractions(loggerMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_PaymentInfo_LoggerNull() throws Exception {
        PaymentInfo pi = new PaymentInfo();
        pi.setOrganization(owner);
        PermissionCheck.owns(pi, notOwner, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_PaymentInfo_NotOwner() throws Exception {
        PaymentInfo pi = new PaymentInfo();
        pi.setOrganization(owner);
        try {
            PermissionCheck.owns(pi, notOwner, loggerMock);
        } finally {
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_PAYMENT_INFO_ACCESS),
                            eq(notOwner.getOrganizationId()),
                            eq(String.valueOf(pi.getKey())));
        }
    }

    @Test
    public void owns_Subscription() throws Exception {
        Subscription sub = new Subscription();
        sub.setOrganization(owner);
        PermissionCheck.owns(sub, owner, loggerMock);
        verifyZeroInteractions(loggerMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_Subscription_LoggerNull() throws Exception {
        Subscription sub = new Subscription();
        sub.setOrganization(owner);
        PermissionCheck.owns(sub, notOwner, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_Subscription_NotOwner() throws Exception {
        Subscription sub = new Subscription();
        sub.setOrganization(owner);
        try {
            PermissionCheck.owns(sub, notOwner, loggerMock);
        } finally {
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_SUBSCRIPTION_ACCESS),
                            eq(notOwner.getOrganizationId()),
                            eq(String.valueOf(sub.getKey())));
        }
    }

    @Test
    public void ownsSubscription_AdminOwner() throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        Subscription subscription = givenSubscription(customer, owner);

        // when
        PermissionCheck.owns(subscription, owner, null, loggerMock);

        // then
        verify(loggerMock, times(0)).logWarn(eq(LOGLEVEL_SYSTEM),
                any(OperationNotPermittedException.class),
                any(LogMessageIdentifier.class));
    }

    @Test
    public void ownsSubscription_AdminNotOwner() throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        PlatformUser notOwner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        Subscription subscription = givenSubscription(customer, owner);

        // when
        PermissionCheck.owns(subscription, notOwner, null, loggerMock);

        // then
        verify(loggerMock, times(0)).logWarn(eq(LOGLEVEL_SYSTEM),
                any(OperationNotPermittedException.class),
                any(LogMessageIdentifier.class));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void ownsSubscription_AdminOtherOrg() throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        Organization otherOrg = givenOrganization("otherOrg");
        otherOrg.setKey(222);
        PlatformUser adminOtherOrg = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                otherOrg);
        Subscription subscription = givenSubscription(customer, owner);

        // when
        PermissionCheck.owns(subscription, adminOtherOrg, null, loggerMock);
    }

    @Test
    public void ownsSubscription_ManagerOwner() throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        Subscription subscription = givenSubscription(customer, owner);

        // when
        PermissionCheck.owns(subscription, owner, null, loggerMock);

        // then
        verify(loggerMock, times(0)).logWarn(eq(LOGLEVEL_SYSTEM),
                any(OperationNotPermittedException.class),
                any(LogMessageIdentifier.class));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void ownsSubscription_ManagerNotOwner() throws Exception {

        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        PlatformUser notOwner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        Subscription subscription = givenSubscription(customer, owner);

        // when
        PermissionCheck.owns(subscription, notOwner, null, loggerMock);
    }

    @Test
    public void ownsSubscription_UnitAdmin() throws Exception {
        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser unitAdmin = givenUser(UserRoleType.UNIT_ADMINISTRATOR,
                customer);
        Subscription subscription = givenSubscription(customer, unitAdmin);

        // when
        PermissionCheck.owns(subscription, unitAdmin,
                Collections.singletonList(subscription.getUserGroup()),
                loggerMock);
    }

    @Test
    public void ownsSubscription_UnitAdminWithUnit() throws Exception {
        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser unitAdmin = givenUser(UserRoleType.UNIT_ADMINISTRATOR,
                customer);
        UserGroup ug = givenUserGroup("sampleUnit");
        Subscription subscription = givenSubscription(customer, unitAdmin, ug);

        // when
        PermissionCheck.owns(subscription, unitAdmin,
                Collections.singletonList(subscription.getUserGroup()),
                loggerMock);
    }

    private PlatformUser givenUser(UserRoleType roleType, Organization org) {
        PlatformUser user = new PlatformUser();
        user.setUserId("");
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setUser(user);
        roleAssignment.setRole(new UserRole(roleType));
        user.getAssignedRoles().add(roleAssignment);
        user.setOrganization(org);
        org.getPlatformUsers().add(user);
        return user;
    }

    private UserGroup givenUserGroup(String name) {
        UserGroup ug = new UserGroup();
        ug.setName(name);
        return ug;
    }

    private Organization givenOrganization(String orgId) {
        Organization org = new Organization();
        org.setOrganizationId(orgId);
        org.setKey(organizationKey);
        return org;
    }

    private Subscription givenSubscription(Organization customer,
            PlatformUser owner) {
        Subscription sub = new Subscription();
        sub.setOrganization(customer);
        sub.setOwner(owner);
        return sub;
    }

    private Subscription givenSubscription(Organization customer,
            PlatformUser owner, UserGroup ug) {
        Subscription sub = new Subscription();
        sub.setOrganization(customer);
        sub.setOwner(owner);
        sub.setUserGroup(ug);
        return sub;
    }

    @Test
    public void owns_Marketplace() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setOrganization(owner);
        PermissionCheck.owns(mp, owner, loggerMock, sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_Marketplace_LoggerAndContextNull() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setOrganization(owner);
        PermissionCheck.owns(mp, notOwner, null, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_Marketplace_NotOwner() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setOrganization(owner);
        try {
            PermissionCheck.owns(mp, notOwner, loggerMock, sessionMock);
        } finally {
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_MARKETPLACE_ACCESS),
                            eq(notOwner.getOrganizationId()),
                            eq(String.valueOf(mp.getKey())));
            verify(sessionMock, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void owns_Product() throws Exception {
        Product p = new Product();
        p.setVendor(owner);
        PermissionCheck.owns(p, owner, loggerMock, sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_Product_LoggerAndContextNull() throws Exception {
        Product p = new Product();
        p.setVendor(owner);
        PermissionCheck.owns(p, notOwner, null, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_Product_NotOwner() throws Exception {
        Product p = new Product();
        p.setVendor(owner);
        try {
            PermissionCheck.owns(p, notOwner, loggerMock, sessionMock);
        } finally {
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_SERVICE_ACCESS),
                            eq(notOwner.getOrganizationId()),
                            eq(String.valueOf(p.getKey())));
            verify(sessionMock, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void owns_TechnicalProduct() throws Exception {
        TechnicalProduct tp = new TechnicalProduct();
        tp.setOrganization(owner);
        PermissionCheck.owns(tp, owner, loggerMock, sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_TechnicalProduct_LoggerAndContextNull() throws Exception {
        TechnicalProduct tp = new TechnicalProduct();
        tp.setOrganization(owner);
        PermissionCheck.owns(tp, notOwner, null, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_TechnicalProduct_NotOwner() throws Exception {
        TechnicalProduct tp = new TechnicalProduct();
        tp.setOrganization(owner);
        try {
            PermissionCheck.owns(tp, notOwner, loggerMock, sessionMock);
        } finally {
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_TECH_SERVICE_ACCESS),
                            eq(notOwner.getOrganizationId()),
                            eq(String.valueOf(tp.getKey())));
            verify(sessionMock, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void owns_UdaDefinition() throws Exception {
        UdaDefinition uda = new UdaDefinition();
        uda.setOrganization(owner);
        PermissionCheck.owns(uda, owner, loggerMock, sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_UdaDefinition_LoggerAndContextNull() throws Exception {
        UdaDefinition uda = new UdaDefinition();
        uda.setOrganization(owner);
        PermissionCheck.owns(uda, notOwner, null, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void owns_UdaDefinition_NotOwner() throws Exception {
        UdaDefinition uda = new UdaDefinition();
        uda.setOrganization(owner);
        try {
            PermissionCheck.owns(uda, notOwner, loggerMock, sessionMock);
        } finally {
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_UDA_DEFINITION_ACCESS),
                            eq(notOwner.getOrganizationId()),
                            eq(String.valueOf(uda.getKey())));
            verify(sessionMock, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void supplierOfCustomer() throws Exception {
        Organization sup = new Organization();
        Organization cust = new Organization();
        OrganizationReference ref = new OrganizationReference(sup, cust,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        sup.getTargets().add(ref);
        cust.getSources().add(ref);
        PermissionCheck.supplierOfCustomer(sup, cust, loggerMock, sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void supplierOfCustomer_LoggerAndContextNull() throws Exception {
        Organization sup = new Organization();
        Organization cust = new Organization();
        try {
            PermissionCheck.supplierOfCustomer(sup, cust, null, null);
        } finally {
            verifyZeroInteractions(loggerMock);
            verifyZeroInteractions(sessionMock);
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void supplierOfCustomer_NotSupplierOfCustomer() throws Exception {
        Organization sup = new Organization();
        Organization cust = new Organization();
        try {
            PermissionCheck.supplierOfCustomer(sup, cust, loggerMock,
                    sessionMock);
        } finally {
            verify(loggerMock, times(1)).logWarn(eq(LOGLEVEL_SYSTEM),
                    any(OperationNotPermittedException.class),
                    eq(LogMessageIdentifier.WARN_NO_SUPPLIER_OF_CUSTOMER),
                    eq(sup.getOrganizationId()), eq(cust.getOrganizationId()));
            verify(sessionMock, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void brokerOfCustomer() throws Exception {
        // given
        Organization broker = new Organization();
        Organization cust = new Organization();
        OrganizationReference ref = new OrganizationReference(broker, cust,
                OrganizationReferenceType.BROKER_TO_CUSTOMER);
        broker.getTargets().add(ref);
        cust.getSources().add(ref);

        // when
        PermissionCheck.brokerOfCustomer(broker, cust, loggerMock, sessionMock);

        // then
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test
    public void resellerOfCustomer() throws Exception {
        Organization sup = new Organization();
        Organization cust = new Organization();
        OrganizationReference ref = new OrganizationReference(sup, cust,
                OrganizationReferenceType.RESELLER_TO_CUSTOMER);
        sup.getTargets().add(ref);
        cust.getSources().add(ref);
        PermissionCheck.resellerOfCustomer(sup, cust, loggerMock, sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void resellerOfCustomer_LoggerAndContextNull() throws Exception {
        Organization sup = new Organization();
        Organization cust = new Organization();
        try {
            PermissionCheck.resellerOfCustomer(sup, cust, null, null);
        } finally {
            verifyZeroInteractions(loggerMock);
            verifyZeroInteractions(sessionMock);
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void resellerOfCustomer_NotSupplierOfCustomer() throws Exception {
        Organization sup = new Organization();
        Organization cust = new Organization();
        try {
            PermissionCheck.resellerOfCustomer(sup, cust, loggerMock,
                    sessionMock);
        } finally {
            verify(loggerMock, times(1)).logWarn(eq(LOGLEVEL_SYSTEM),
                    any(OperationNotPermittedException.class),
                    eq(LogMessageIdentifier.WARN_NO_RESELLER_OF_CUSTOMER),
                    eq(sup.getOrganizationId()), eq(cust.getOrganizationId()));
            verify(sessionMock, times(1)).setRollbackOnly();
        }
    }

    private void addRole(Organization org, OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganizationRole(role);
        org.setGrantedRoles(Collections.singleton(otr));
    }

    @Test
    public void sellerOfCustomer_supplier() throws Exception {
        // given
        Organization sup = new Organization();
        addRole(sup, OrganizationRoleType.SUPPLIER);
        Organization cust = new Organization();
        OrganizationReference ref = new OrganizationReference(sup, cust,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        sup.getTargets().add(ref);
        cust.getSources().add(ref);

        // when
        PermissionCheck.sellerOfCustomer(sup, cust, loggerMock, sessionMock);

        // then
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test
    public void sellerOfCustomer_broker() throws Exception {
        // given
        Organization broker = new Organization();
        addRole(broker, OrganizationRoleType.BROKER);
        Organization cust = new Organization();
        OrganizationReference ref = new OrganizationReference(broker, cust,
                OrganizationReferenceType.BROKER_TO_CUSTOMER);
        broker.getTargets().add(ref);
        cust.getSources().add(ref);

        // when
        PermissionCheck.sellerOfCustomer(broker, cust, loggerMock, sessionMock);

        // then
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test
    public void sellerOfCustomer_reseller() throws Exception {
        // given
        Organization reseller = new Organization();
        addRole(reseller, OrganizationRoleType.RESELLER);
        Organization cust = new Organization();
        OrganizationReference ref = new OrganizationReference(reseller, cust,
                OrganizationReferenceType.RESELLER_TO_CUSTOMER);
        reseller.getTargets().add(ref);
        cust.getSources().add(ref);

        // when
        PermissionCheck.sellerOfCustomer(reseller, cust, loggerMock,
                sessionMock);

        // then
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test
    public void canPublish() throws Exception {
        Marketplace mp = new Marketplace();
        MarketplaceToOrganization rel = new MarketplaceToOrganization(mp, owner);
        mp.getMarketplaceToOrganizations().add(rel);
        owner.getMarketplaceToOrganizations().add(rel);
        PermissionCheck.canPublish(mp, owner, loggerMock, sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void canPublish_NotAllowed() throws Exception {
        Marketplace mp = new Marketplace();
        try {
            PermissionCheck.canPublish(mp, owner, loggerMock, sessionMock);
        } finally {
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_PUBLISH_ON_MARKETPLACE),
                            eq(owner.getOrganizationId()),
                            eq(mp.getMarketplaceId()));
            verify(sessionMock, times(1)).setRollbackOnly();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void canPublish_LoggerAndContextNull() throws Exception {
        Marketplace mp = new Marketplace();
        try {
            PermissionCheck.canPublish(mp, owner, null, null);
        } finally {
            verifyZeroInteractions(loggerMock);
            verifyZeroInteractions(sessionMock);
        }
    }

    @Test
    public void canPublish_Open_TwoSuppliersOneWithRelation() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setOpen(true);
        MarketplaceToOrganization rel = new MarketplaceToOrganization(mp, owner);
        mp.getMarketplaceToOrganizations().add(rel);
        owner.getMarketplaceToOrganizations().add(rel);

        Organization anotherSupplier = new Organization();
        anotherSupplier.setOrganizationId("anotherSupplier");
        anotherSupplier.setKey(5678);

        PermissionCheck
                .canPublish(mp, anotherSupplier, loggerMock, sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test
    public void canPublish_Open() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setOpen(true);
        PermissionCheck.canPublish(mp, owner, loggerMock, sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void canPublish_Open_NotAllowed() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setOpen(true);
        MarketplaceToOrganization rel = new MarketplaceToOrganization(mp,
                owner, PublishingAccess.PUBLISHING_ACCESS_DENIED);
        mp.getMarketplaceToOrganizations().add(rel);
        owner.getMarketplaceToOrganizations().add(rel);
        try {
            PermissionCheck.canPublish(mp, owner, loggerMock, sessionMock);
        } finally {
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_PUBLISH_ON_MARKETPLACE),
                            eq(owner.getOrganizationId()),
                            eq(mp.getMarketplaceId()));
            verify(sessionMock, times(1)).setRollbackOnly();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void hasMarketingPermission_NoResult()
            throws OperationNotPermittedException {
        TechnicalProduct tpMock = mock(TechnicalProduct.class);
        Organization supplierMock = mock(Organization.class);
        DataService dsMock = mock(DataService.class);
        Query queryMock = mock(Query.class);
        when(dsMock.createNamedQuery(anyString())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenThrow(new NoResultException());

        PermissionCheck.hasMarketingPermission(tpMock, supplierMock, dsMock,
                loggerMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void hasMarketingPermission_NonUniqueResult()
            throws OperationNotPermittedException {
        TechnicalProduct tpMock = mock(TechnicalProduct.class);
        Organization supplierMock = mock(Organization.class);
        DataService dsMock = mock(DataService.class);
        Query queryMock = mock(Query.class);
        when(dsMock.createNamedQuery(anyString())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenThrow(
                new NonUniqueResultException());

        PermissionCheck.hasMarketingPermission(tpMock, supplierMock, dsMock,
                loggerMock);
    }

    @Test
    public void same() throws Exception {
        PermissionCheck.same(owner, owner, loggerMock, sessionMock);
        verifyZeroInteractions(loggerMock, sessionMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void same_NotSame() throws Exception {
        try {
            PermissionCheck.same(owner, notOwner, loggerMock, sessionMock);
        } finally {
            verify(sessionMock, times(1)).setRollbackOnly();
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_ORGANIZATION_ACCESS),
                            eq(owner.getOrganizationId()),
                            eq(notOwner.getOrganizationId()));
        }
    }

    @Test
    public void sameUdaTarget() throws Exception {
        Uda uda = new Uda();
        uda.setTargetObjectKey(1234);

        PermissionCheck.sameUdaTarget(owner, uda, uda.getTargetObjectKey(),
                loggerMock, sessionMock);

        verifyZeroInteractions(loggerMock, sessionMock);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void sameUdaTarget_NotSame() throws Exception {
        Uda uda = new Uda();
        uda.setTargetObjectKey(1234);

        try {
            PermissionCheck.sameUdaTarget(owner, uda, 1111, loggerMock,
                    sessionMock);
        } finally {
            verify(sessionMock, times(1)).setRollbackOnly();
            verify(loggerMock, times(1))
                    .logWarn(
                            eq(LOGLEVEL_SYSTEM),
                            any(OperationNotPermittedException.class),
                            eq(LogMessageIdentifier.WARN_UNPERMITTED_UDA_TARGET_SWITCH),
                            eq(owner.getOrganizationId()),
                            eq(Long.toString(uda.getKey())),
                            eq(Long.toString(uda.getTargetObjectKey())),
                            eq(Long.toString(1111)));
        }
    }

    @Test
    public void ownsPriceModel_subscription() throws Exception {
        Product subscrCopy = new Product();
        subscrCopy.setVendor(owner);
        subscrCopy.setType(ServiceType.SUBSCRIPTION);

        PermissionCheck.ownsPriceModel(subscrCopy, owner, loggerMock,
                sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test
    public void ownsPriceModel_brokerSubscription() throws Exception {
        Organization broker = new Organization();
        addRole(broker, OrganizationRoleType.BROKER);

        Product template = new Product();
        template.setVendor(owner);

        Product partnerTemplate = new Product();
        partnerTemplate.setVendor(broker);
        partnerTemplate.setTemplate(template);

        Product subscrCopy = new Product();
        subscrCopy.setVendor(broker);
        subscrCopy.setTemplate(partnerTemplate);
        subscrCopy.setType(ServiceType.PARTNER_SUBSCRIPTION);

        PermissionCheck.ownsPriceModel(subscrCopy, owner, loggerMock,
                sessionMock);
        verifyZeroInteractions(loggerMock);
        verifyZeroInteractions(sessionMock);
    }

    @Test
    public void canModifyOwnerTestNoOwnerChangedNoOrgAdmin() {
        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        owner.setUserId("Owner");
        VOSubscription voSub = new VOSubscription();
        voSub.setOwnerId(owner.getUserId());
        Subscription sub = new Subscription();
        sub.setOwner(owner);
        // when
        boolean result = PermissionCheck
                .shouldWeProceedWithUpdatingSubscription(voSub, sub, owner);
        // then
        assertTrue(result);
    }

    @Test
    public void canModifyOwnerTestOrgAdminOwnerChanged() {
        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.ORGANIZATION_ADMIN,
                customer);
        owner.setUserId("Owner");
        VOSubscription voSub = new VOSubscription();
        voSub.setOwnerId("OwnerNew");
        Subscription sub = new Subscription();
        sub.setOwner(owner);
        // when
        boolean result = PermissionCheck
                .shouldWeProceedWithUpdatingSubscription(voSub, sub, owner);
        // then
        assertTrue(result);
    }

    @Test
    public void canModifyOwnerTestUnitAdminOwnerChanged() {
        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.UNIT_ADMINISTRATOR,
                customer);
        owner.setUserId("Owner");
        VOSubscription voSub = new VOSubscription();
        voSub.setOwnerId("OwnerNew");
        Subscription sub = new Subscription();
        sub.setOwner(owner);
        // when
        boolean result = PermissionCheck
                .shouldWeProceedWithUpdatingSubscription(voSub, sub, owner);
        // then
        assertTrue(result);
    }

    @Test
    public void canModifyOwnerTestOwnerChangedNoOrgAdmin() {
        // given
        Organization customer = givenOrganization("customerOrg");
        PlatformUser owner = givenUser(UserRoleType.SUBSCRIPTION_MANAGER,
                customer);
        owner.setUserId("Owner");
        VOSubscription voSub = new VOSubscription();
        voSub.setOwnerId("OwnerNew");
        Subscription sub = new Subscription();
        sub.setOwner(owner);
        // when
        boolean result = PermissionCheck
                .shouldWeProceedWithUpdatingSubscription(voSub, sub, owner);
        // then
        assertFalse(result);
    }
}
