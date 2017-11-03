/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 14.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.test.data.OrganizationReferences;
import org.oscm.test.data.Organizations;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServicePaymentConfiguration;

/**
 * @author weiser
 * 
 */
public class PaymentConfigurationFilterTest {

    private static final String ORG_NOT_FOUND_ID = "notFound";
    private static final long PROD_NOT_FOUND_KEY = 6789;

    private DataService ds;
    private PaymentConfigurationFilter pcf;

    private Organization supplier;
    private Organization reseller;
    private Organization customer;
    private Product product;
    private PaymentType pt;
    private PlatformUser user;
    private Organization noCustomer;

    @Before
    public void setup() throws Exception {
        ds = mock(DataService.class);

        pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        pt.setKey(12345);

        Organization op = new Organization();
        op.setOrganizationId(OrganizationRoleType.PLATFORM_OPERATOR.name());

        supplier = new Organization();
        supplier.setOrganizationId("supplier");
        Organizations.grantOrganizationRole(supplier,
                OrganizationRoleType.SUPPLIER);

        reseller = new Organization();
        reseller.setOrganizationId("reseller");
        Organizations.grantOrganizationRole(reseller,
                OrganizationRoleType.RESELLER);

        OrganizationReference ref = OrganizationReferences.addReference(op,
                supplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
        OrganizationReferences.enablePayment(ref, pt).setOrganizationRole(
                new OrganizationRole(OrganizationRoleType.SUPPLIER));

        ref = OrganizationReferences.addReference(op, reseller,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_RESELLER);
        OrganizationReferences.enablePayment(ref, pt).setOrganizationRole(
                new OrganizationRole(OrganizationRoleType.RESELLER));

        customer = new Organization();
        customer.setOrganizationId("customer");

        noCustomer = new Organization();
        noCustomer.setOrganizationId("noCustomer");

        ref = OrganizationReferences.addReference(supplier, customer,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        OrganizationReferences.enablePayment(ref, pt);

        ref = OrganizationReferences.addReference(reseller, customer,
                OrganizationReferenceType.RESELLER_TO_CUSTOMER);
        OrganizationReferences.enablePayment(ref, pt);

        product = new Product();
        product.setKey(9876);
        product.setVendor(supplier);
        product.setType(ServiceType.TEMPLATE);
        product.setPaymentTypes(Arrays.asList(new ProductToPaymentType(product,
                pt)));

        pcf = new PaymentConfigurationFilter(ds);

        user = new PlatformUser();
        user.setOrganization(supplier);

        when(ds.getCurrentUser()).thenReturn(user);
        when(ds.getReference(eq(Product.class), eq(product.getKey())))
                .thenReturn(product);
        when(ds.getReference(eq(Product.class), eq(PROD_NOT_FOUND_KEY)))
                .thenThrow(
                        new ObjectNotFoundException(ClassEnum.SERVICE,
                                "product"));

        when(ds.getReferenceByBusinessKey(any(DomainObject.class))).thenAnswer(
                new Answer<DomainObject<?>>() {

                    public DomainObject<?> answer(InvocationOnMock invocation)
                            throws Throwable {
                        Object object = invocation.getArguments()[0];
                        if (object instanceof Organization) {
                            Organization o = (Organization) object;
                            if (customer.getOrganizationId().equals(
                                    o.getOrganizationId())) {
                                return customer;
                            }
                            if (noCustomer.getOrganizationId().equals(
                                    o.getOrganizationId())) {
                                return noCustomer;
                            }
                            throw new ObjectNotFoundException(
                                    ClassEnum.ORGANIZATION, o
                                            .getOrganizationId());
                        }
                        throw new ObjectNotFoundException(
                                ClassEnum.ORGANIZATION, object.getClass()
                                        .getName());
                    }
                });
    }

    @Test
    public void isDefaultCustomerConfigurationChanged() {
        VOPaymentType voPt = createPaymentType(PaymentType.CREDIT_CARD);

        assertTrue(pcf.isDefaultCustomerConfigurationChanged(Collections
                .singleton(voPt)));
    }

    @Test
    public void isDefaultCustomerConfigurationChanged_NotDefault() {
        supplier.getSources().get(0).getPaymentTypes().get(0)
                .setUsedAsDefault(false);
        VOPaymentType voPt = createPaymentType(PaymentType.CREDIT_CARD);

        assertTrue(pcf.isDefaultCustomerConfigurationChanged(Collections
                .singleton(voPt)));
    }

    @Test
    public void isDefaultCustomerConfigurationChanged_Empty() {
        assertTrue(pcf
                .isDefaultCustomerConfigurationChanged(new HashSet<VOPaymentType>()));
    }

    @Test
    public void isDefaultCustomerConfigurationChanged_Negative() {
        VOPaymentType voPt = createPaymentType(PaymentType.INVOICE);

        assertFalse(pcf.isDefaultCustomerConfigurationChanged(Collections
                .singleton(voPt)));
    }

    @Test
    public void isDefaultCustomerConfigurationChanged_Reseller() {
        user.setOrganization(reseller);
        VOPaymentType voPt = createPaymentType(PaymentType.CREDIT_CARD);

        assertTrue(pcf.isDefaultCustomerConfigurationChanged(Collections
                .singleton(voPt)));
    }

    @Test
    public void isDefaultCustomerConfigurationChanged_Reseller_Empty() {
        user.setOrganization(reseller);

        assertTrue(pcf
                .isDefaultCustomerConfigurationChanged(new HashSet<VOPaymentType>()));
    }

    @Test
    public void isDefaultCustomerConfigurationChanged_Reseller_Negative() {
        user.setOrganization(reseller);
        VOPaymentType voPt = createPaymentType(PaymentType.INVOICE);

        assertFalse(pcf.isDefaultCustomerConfigurationChanged(Collections
                .singleton(voPt)));
    }

    @Test
    public void isDefaultServiceConfigurationChanged() {
        VOPaymentType voPt = createPaymentType(PaymentType.CREDIT_CARD);

        assertTrue(pcf.isDefaultServiceConfigurationChanged(Collections
                .singleton(voPt)));
    }

    @Test
    public void isDefaultServiceConfigurationChanged_NotDefault() {
        supplier.getSources().get(0).getPaymentTypes().get(0)
                .setUsedAsDefault(false);
        VOPaymentType voPt = createPaymentType(PaymentType.CREDIT_CARD);

        assertTrue(pcf.isDefaultServiceConfigurationChanged(Collections
                .singleton(voPt)));
    }

    @Test
    public void isDefaultServiceConfigurationChanged_Empty() {
        assertTrue(pcf
                .isDefaultServiceConfigurationChanged(new HashSet<VOPaymentType>()));
    }

    @Test
    public void isDefaultServiceConfigurationChanged_Negative() {
        VOPaymentType voPt = createPaymentType(PaymentType.INVOICE);

        assertFalse(pcf.isDefaultServiceConfigurationChanged(Collections
                .singleton(voPt)));
    }

    @Test
    public void isDefaultServiceConfigurationChanged_Reseller() {
        user.setOrganization(reseller);
        VOPaymentType voPt = createPaymentType(PaymentType.CREDIT_CARD);

        assertTrue(pcf.isDefaultServiceConfigurationChanged(Collections
                .singleton(voPt)));
    }

    @Test
    public void isDefaultServiceConfigurationChanged_Reseller_Empty() {
        user.setOrganization(reseller);

        assertTrue(pcf
                .isDefaultServiceConfigurationChanged(new HashSet<VOPaymentType>()));
    }

    @Test
    public void isDefaultServiceConfigurationChanged_Reseller_Negative() {
        user.setOrganization(reseller);
        VOPaymentType voPt = createPaymentType(PaymentType.INVOICE);

        assertFalse(pcf.isDefaultServiceConfigurationChanged(Collections
                .singleton(voPt)));
    }

    @Test
    public void filterCustomerConfiguration_Null() throws Exception {
        assertEquals(new ArrayList<VOOrganizationPaymentConfiguration>(),
                pcf.filterCustomerConfiguration(null));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void filterCustomerConfiguration_CustomerNotFound() throws Exception {
        VOOrganizationPaymentConfiguration conf = createCustomerConfiguration(
                ORG_NOT_FOUND_ID, PaymentType.INVOICE);

        pcf.filterCustomerConfiguration(Arrays.asList(conf));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void filterCustomerConfiguration_NotPermitted() throws Exception {
        VOOrganizationPaymentConfiguration conf = createCustomerConfiguration(
                noCustomer.getOrganizationId(), PaymentType.INVOICE);

        pcf.filterCustomerConfiguration(Arrays.asList(conf));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void filterCustomerConfiguration_Reseller_NotPermitted()
            throws Exception {
        user.setOrganization(reseller);
        VOOrganizationPaymentConfiguration conf = createCustomerConfiguration(
                noCustomer.getOrganizationId(), PaymentType.INVOICE);

        pcf.filterCustomerConfiguration(Arrays.asList(conf));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void filterCustomerConfiguration_Customer_NotPermitted()
            throws Exception {
        user.setOrganization(noCustomer);
        VOOrganizationPaymentConfiguration conf = createCustomerConfiguration(
                noCustomer.getOrganizationId(), PaymentType.INVOICE);

        pcf.filterCustomerConfiguration(Arrays.asList(conf));
    }

    @Test
    public void filterCustomerConfiguration_NoChange() throws Exception {
        VOOrganizationPaymentConfiguration conf = createCustomerConfiguration(
                customer.getOrganizationId(), PaymentType.INVOICE);

        List<VOOrganizationPaymentConfiguration> result = pcf
                .filterCustomerConfiguration(Arrays.asList(conf));

        assertEquals(new ArrayList<VOOrganizationPaymentConfiguration>(),
                result);
    }

    @Test
    public void filterCustomerConfiguration_Change() throws Exception {
        VOOrganizationPaymentConfiguration conf = createCustomerConfiguration(
                customer.getOrganizationId(), PaymentType.CREDIT_CARD);
        List<VOOrganizationPaymentConfiguration> input = Arrays.asList(conf);

        List<VOOrganizationPaymentConfiguration> result = pcf
                .filterCustomerConfiguration(input);

        assertEquals(input, result);
    }

    @Test
    public void filterCustomerConfiguration_Reseller_NoChange()
            throws Exception {
        user.setOrganization(reseller);
        VOOrganizationPaymentConfiguration conf = createCustomerConfiguration(
                customer.getOrganizationId(), PaymentType.INVOICE);

        List<VOOrganizationPaymentConfiguration> result = pcf
                .filterCustomerConfiguration(Arrays.asList(conf));

        assertEquals(new ArrayList<VOOrganizationPaymentConfiguration>(),
                result);
    }

    @Test
    public void filterCustomerConfiguration_Reseller_Change() throws Exception {
        user.setOrganization(reseller);
        VOOrganizationPaymentConfiguration conf = createCustomerConfiguration(
                customer.getOrganizationId(), PaymentType.CREDIT_CARD);
        List<VOOrganizationPaymentConfiguration> input = Arrays.asList(conf);

        List<VOOrganizationPaymentConfiguration> result = pcf
                .filterCustomerConfiguration(input);

        assertEquals(input, result);
    }

    @Test
    public void filterServiceConfiguration_Null() throws Exception {
        assertEquals(new ArrayList<VOOrganizationPaymentConfiguration>(),
                pcf.filterServiceConfiguration(null));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void filterServiceConfiguration_ServiceNotFound() throws Exception {
        VOServicePaymentConfiguration conf = createServiceConfiguration(
                PROD_NOT_FOUND_KEY, PaymentType.INVOICE);

        pcf.filterServiceConfiguration(Arrays.asList(conf));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void filterServiceConfiguration_NotPermitted() throws Exception {
        product.setVendor(reseller);
        VOServicePaymentConfiguration conf = createServiceConfiguration(
                product.getKey(), PaymentType.INVOICE);

        pcf.filterServiceConfiguration(Arrays.asList(conf));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void filterServiceConfiguration_NoTemplate() throws Exception {
        product.setType(ServiceType.CUSTOMER_TEMPLATE);
        VOServicePaymentConfiguration conf = createServiceConfiguration(
                product.getKey(), PaymentType.INVOICE);

        pcf.filterServiceConfiguration(Arrays.asList(conf));
    }

    @Test
    public void filterServiceConfiguration_NoChange() throws Exception {
        VOServicePaymentConfiguration conf = createServiceConfiguration(
                product.getKey(), PaymentType.INVOICE);

        List<VOServicePaymentConfiguration> list = pcf
                .filterServiceConfiguration(Arrays.asList(conf));

        assertEquals(new ArrayList<VOServicePaymentConfiguration>(), list);
    }

    @Test
    public void filterServiceConfiguration_Change() throws Exception {
        VOServicePaymentConfiguration conf = createServiceConfiguration(
                product.getKey(), PaymentType.CREDIT_CARD);
        List<VOServicePaymentConfiguration> input = Arrays.asList(conf);

        List<VOServicePaymentConfiguration> list = pcf
                .filterServiceConfiguration(input);

        assertEquals(input, list);
    }

    @Test
    public void filterServiceConfiguration_ChangeEmpty() throws Exception {
        List<VOServicePaymentConfiguration> input = Collections.emptyList();

        List<VOServicePaymentConfiguration> list = pcf
                .filterServiceConfiguration(input);

        assertEquals(input, list);
    }

    @Test
    public void checkIsTemplate() throws Exception {
        pcf.checkIsTemplate(supplier, product);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkIsTemplate_Negative() throws Exception {
        product.setType(ServiceType.CUSTOMER_TEMPLATE);

        pcf.checkIsTemplate(supplier, product);
    }

    @Test
    public void checkSellerRelationship_Supplier() throws Exception {
        OrganizationReference ref = pcf.checkSellerRelationship(supplier,
                customer);

        assertNotNull(ref);
        assertEquals(supplier, ref.getSource());
        assertEquals(customer, ref.getTarget());
        assertEquals(OrganizationReferenceType.SUPPLIER_TO_CUSTOMER,
                ref.getReferenceType());
    }

    @Test
    public void checkSellerRelationship_Reseller() throws Exception {
        OrganizationReference ref = pcf.checkSellerRelationship(reseller,
                customer);

        assertNotNull(ref);
        assertEquals(reseller, ref.getSource());
        assertEquals(customer, ref.getTarget());
        assertEquals(OrganizationReferenceType.RESELLER_TO_CUSTOMER,
                ref.getReferenceType());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkSellerRelationship_SupplierNegative() throws Exception {
        pcf.checkSellerRelationship(supplier, noCustomer);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkSellerRelationship_ResellerNegative() throws Exception {
        pcf.checkSellerRelationship(reseller, noCustomer);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkSellerRelationship_NoSeller() throws Exception {
        pcf.checkSellerRelationship(noCustomer, customer);
    }

    private static VOServicePaymentConfiguration createServiceConfiguration(
            long prodKey, String paymentId) {
        VOServicePaymentConfiguration conf = new VOServicePaymentConfiguration();
        conf.setEnabledPaymentTypes(Collections
                .singleton(createPaymentType(paymentId)));
        VOService service = new VOService();
        service.setKey(prodKey);
        conf.setService(service);
        return conf;
    }

    private static VOOrganizationPaymentConfiguration createCustomerConfiguration(
            String orgId, String paymentId) {
        VOOrganizationPaymentConfiguration conf = new VOOrganizationPaymentConfiguration();
        conf.setEnabledPaymentTypes(Collections
                .singleton(createPaymentType(paymentId)));
        VOOrganization org = new VOOrganization();
        org.setOrganizationId(orgId);
        conf.setOrganization(org);
        return conf;
    }

    private static VOPaymentType createPaymentType(String id) {
        VOPaymentType voPt = new VOPaymentType();
        voPt.setPaymentTypeId(id);
        return voPt;
    }
}
