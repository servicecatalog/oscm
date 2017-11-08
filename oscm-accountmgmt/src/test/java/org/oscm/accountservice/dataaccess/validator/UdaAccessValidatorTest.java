/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 26.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dataaccess.validator;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.test.data.OrganizationReferences;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

/**
 * @author weiser
 * 
 */
public class UdaAccessValidatorTest {

    private UdaAccessValidator uav;

    private DataService ds;
    private UdaDefinition def;
    private Organization customer;
    private Organization supplier;
    private Uda uda;

    private Subscription sub;

    @Before
    public void setup() throws Exception {
        ds = mock(DataService.class);

        uav = new UdaAccessValidator(ds, mock(SessionContext.class));
        uav.mandatoryValidator = mock(MandatoryUdaValidator.class);

        customer = new Organization();
        customer.setKey(1234);
        supplier = new Organization();
        supplier.setKey(4321);

        OrganizationReferences.addSupplierReference(supplier, customer);
        OrganizationReferences.addSupplierReference(supplier, supplier);

        def = new UdaDefinition();
        def.setOrganization(supplier);
        def.setTargetType(UdaTargetType.CUSTOMER);
        def.setConfigurationType(UdaConfigurationType.SUPPLIER);

        uda = new Uda();
        uda.setUdaDefinition(def);
        uda.setTargetObjectKey(customer);

        sub = new Subscription();
        sub.setKey(5678);
        sub.setOrganization(customer);

        when(ds.getReference(eq(Organization.class), eq(customer.getKey())))
                .thenReturn(customer);
        when(ds.getReference(eq(Subscription.class), eq(sub.getKey())))
                .thenReturn(sub);

        when(ds.find(eq(Organization.class), eq(customer.getKey())))
                .thenReturn(customer);
        when(ds.find(eq(Subscription.class), eq(sub.getKey()))).thenReturn(sub);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void canSaveUda_CustomerNotFound() throws Exception {
        when(ds.getReference(eq(Organization.class), anyLong())).thenThrow(
                new ObjectNotFoundException(ClassEnum.ORGANIZATION, "org"));

        uav.canSaveUda(def, supplier, 1111);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void canSaveUda_SubscriptionNotFound() throws Exception {
        def.setTargetType(UdaTargetType.CUSTOMER_SUBSCRIPTION);
        when(ds.getReference(eq(Subscription.class), anyLong())).thenThrow(
                new ObjectNotFoundException(ClassEnum.SUBSCRIPTION, "sub"));

        uav.canSaveUda(def, customer, 1111);
    }

    @Test
    public void canSaveUda() throws Exception {
        uav.canSaveUda(def, supplier, customer.getKey());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void canSaveUda_Negative() throws Exception {
        uav.canSaveUda(def, customer, customer.getKey());
    }

    @Test
    public void canDeleteUda() throws Exception {
        uav.canDeleteUda(uda, supplier);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void canDeleteUda_Negative() throws Exception {
        uav.canDeleteUda(uda, customer);
    }

    @Test
    public void canDeleteUda_TargetNotFound() throws Exception {
        def.setConfigurationType(UdaConfigurationType.USER_OPTION_MANDATORY);
        uda.setTargetObjectKey(1111);

        uav.canDeleteUda(uda, supplier);
    }

    @Test
    public void checkPermission_Supplier_SUPPLIER() throws Exception {
        uav.checkWritePermission(def, supplier, customer);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkPermission_Supplier_USER_OPTION_MANDATORY()
            throws Exception {
        def.setConfigurationType(UdaConfigurationType.USER_OPTION_MANDATORY);

        uav.checkWritePermission(def, supplier, customer);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkPermission_Supplier_USER_OPTION_OPTIONAL()
            throws Exception {
        def.setConfigurationType(UdaConfigurationType.USER_OPTION_OPTIONAL);

        uav.checkWritePermission(def, supplier, customer);
    }

    @Test
    public void checkPermission_SupplierForItself_USER_OPTION_OPTIONAL()
            throws Exception {
        def.setConfigurationType(UdaConfigurationType.USER_OPTION_OPTIONAL);

        uav.checkWritePermission(def, supplier, supplier);
    }

    @Test
    public void checkPermission_SupplierForAnotherSupplier_SUPPLIER()
            throws Exception {
        Organization anotherSupplier = new Organization();
        OrganizationReferences.addSupplierReference(anotherSupplier, customer);
        OrganizationReferences.addSupplierReference(anotherSupplier,
                anotherSupplier);
        def.setOrganization(anotherSupplier);

        uav.checkWritePermission(def, anotherSupplier, customer);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkPermission_Customer_SUPPLIER() throws Exception {
        uav.checkWritePermission(def, customer, customer);
    }

    @Test
    public void checkPermission_Customer_USER_OPTION_MANDATORY()
            throws Exception {
        def.setConfigurationType(UdaConfigurationType.USER_OPTION_MANDATORY);

        uav.checkWritePermission(def, customer, customer);
    }

    @Test
    public void checkPermission_Customer_USER_OPTION_OPTIONAL()
            throws Exception {
        def.setConfigurationType(UdaConfigurationType.USER_OPTION_OPTIONAL);

        uav.checkWritePermission(def, customer, customer);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkPermission_CustomerForSupplier_USER_OPTION_OPTIONAL()
            throws Exception {
        def.setConfigurationType(UdaConfigurationType.USER_OPTION_OPTIONAL);

        uav.checkWritePermission(def, customer, supplier);
    }

    @Test
    public void findTargetOrganization_CUSTOMER() throws Exception {
        Organization org = uav.findTargetOrganization(UdaTargetType.CUSTOMER,
                customer.getKey());

        assertSame(customer, org);
    }

    @Test
    public void findTargetOrganization_CUSTOMER_SUBSCRIPTION() throws Exception {
        Organization org = uav.findTargetOrganization(
                UdaTargetType.CUSTOMER_SUBSCRIPTION, sub.getKey());

        assertSame(customer, org);
    }

    @Test
    public void findTargetOrganization_CUSTOMER_NotFound() throws Exception {
        Organization org = uav.findTargetOrganization(UdaTargetType.CUSTOMER,
                1111);

        assertNull(org);
    }

    @Test
    public void findTargetOrganization_CUSTOMER_SUBSCRIPTION_NotFound()
            throws Exception {
        Organization org = uav.findTargetOrganization(
                UdaTargetType.CUSTOMER_SUBSCRIPTION, 1111);

        assertNull(org);
    }

    @Test
    public void checkSellerReadPermission_Customer() throws Exception {
        uav.checkSellerReadPermission(supplier, UdaTargetType.CUSTOMER,
                customer.getKey());
    }

    @Test
    public void checkSellerReadPermission_Subscription() throws Exception {
        uav.checkSellerReadPermission(supplier,
                UdaTargetType.CUSTOMER_SUBSCRIPTION, sub.getKey());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkSellerReadPermission_Negative() throws Exception {
        uav.checkSellerReadPermission(new Organization(),
                UdaTargetType.CUSTOMER, customer.getKey());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void checkSellerReadPermission_CustomerNotFound() throws Exception {
        doThrow(new ObjectNotFoundException()).when(ds).getReference(
                eq(Organization.class), anyLong());

        try {
            uav.checkSellerReadPermission(supplier, UdaTargetType.CUSTOMER,
                    customer.getKey());
        } finally {
            verify(uav.ctx, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void checkCustomerReadPermission_Customer() throws Exception {
        uav.checkCustomerReadPermission(customer, UdaTargetType.CUSTOMER,
                customer.getKey());
    }

    @Test
    public void checkCustomerReadPermission_Subscription() throws Exception {
        uav.checkCustomerReadPermission(customer,
                UdaTargetType.CUSTOMER_SUBSCRIPTION, sub.getKey());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkCustomerReadPermission_Negative() throws Exception {
        uav.checkCustomerReadPermission(supplier, UdaTargetType.CUSTOMER,
                customer.getKey());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void checkCustomerReadPermission_SubscriptionNotFound()
            throws Exception {
        doThrow(new ObjectNotFoundException()).when(ds).getReference(
                eq(Subscription.class), anyLong());

        try {
            uav.checkCustomerReadPermission(customer,
                    UdaTargetType.CUSTOMER_SUBSCRIPTION, sub.getKey());
        } finally {
            verify(uav.ctx, times(1)).setRollbackOnly();
        }
    }

}
