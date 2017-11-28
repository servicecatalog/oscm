/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 28.04.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentInformationException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;

public class PaymentDataValidatorTest {

    private PaymentType creditCard;
    private PaymentType directDebit;
    private PaymentType invoice;

    private Organization operator;
    private Organization supplier;
    private Organization customer;

    private Product product_invoice;
    private Product product_invoice_customer;
    private Product product_all;

    private OrganizationReference operatorToSupplier;

    private OrganizationRole supplierRole = new OrganizationRole(
            OrganizationRoleType.SUPPLIER);
    private OrganizationRole customerRole = new OrganizationRole(
            OrganizationRoleType.CUSTOMER);
    private Organization supplier2;
    private Product product_none;
    private Product product_credit;

    @Before
    public void setup() {
        creditCard = new PaymentType();
        creditCard
                .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        creditCard.setPaymentTypeId(PaymentType.CREDIT_CARD);

        invoice = new PaymentType();
        invoice.setCollectionType(PaymentCollectionType.ORGANIZATION);
        invoice.setPaymentTypeId(PaymentType.INVOICE);

        directDebit = new PaymentType();
        directDebit
                .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        directDebit.setPaymentTypeId(PaymentType.DIRECT_DEBIT);

        operator = new Organization();
        operator.setOrganizationId(OrganizationRoleType.PLATFORM_OPERATOR
                .name());

        supplier = new Organization();
        supplier.setOrganizationId("supplier");

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(createOrgToRole(OrganizationRoleType.SUPPLIER));
        supplier.setGrantedRoles(roles);

        customer = new Organization();
        customer.setOrganizationId("customer");

        operatorToSupplier = new OrganizationReference(operator, supplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
        operator.getTargets().add(operatorToSupplier);
        supplier.getSources().add(operatorToSupplier);

        supplier2 = new Organization();
        supplier2.setOrganizationId("supplier2");

        OrganizationReference operatorToSupplier2 = new OrganizationReference(
                operator, supplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
        operator.getTargets().add(operatorToSupplier2);
        supplier2.getSources().add(operatorToSupplier2);

        // invoice enabled and as default for customers without relation
        OrganizationRefToPaymentType ref = new OrganizationRefToPaymentType();
        ref.setOrganizationReference(operatorToSupplier);
        ref.setOrganizationRole(supplierRole);
        ref.setPaymentType(invoice);
        ref.setUsedAsDefault(true);
        operatorToSupplier.getPaymentTypes().add(ref);

        // credit card enabled but not default
        ref = new OrganizationRefToPaymentType();
        ref.setOrganizationReference(operatorToSupplier);
        ref.setOrganizationRole(supplierRole);
        ref.setPaymentType(creditCard);
        ref.setUsedAsDefault(false);
        operatorToSupplier.getPaymentTypes().add(ref);

        // invoice enabled and as default for customers without relation
        ref = new OrganizationRefToPaymentType();
        ref.setOrganizationReference(operatorToSupplier2);
        ref.setOrganizationRole(supplierRole);
        ref.setPaymentType(invoice);
        ref.setUsedAsDefault(true);
        operatorToSupplier2.getPaymentTypes().add(ref);

        // credit card enabled
        ref = new OrganizationRefToPaymentType();
        ref.setOrganizationReference(operatorToSupplier2);
        ref.setOrganizationRole(supplierRole);
        ref.setPaymentType(creditCard);
        ref.setUsedAsDefault(true);
        operatorToSupplier2.getPaymentTypes().add(ref);

        // direct debit enabled
        ref = new OrganizationRefToPaymentType();
        ref.setOrganizationReference(operatorToSupplier2);
        ref.setOrganizationRole(supplierRole);
        ref.setPaymentType(directDebit);
        ref.setUsedAsDefault(true);
        operatorToSupplier2.getPaymentTypes().add(ref);

        product_none = new Product();
        product_none.setVendor(supplier);
        product_none.setType(ServiceType.TEMPLATE);

        product_invoice = new Product();
        product_invoice.setVendor(supplier);
        product_invoice.setType(ServiceType.TEMPLATE);
        ProductToPaymentType ptpt = new ProductToPaymentType(product_invoice,
                invoice);
        product_invoice.setPaymentTypes(Collections.singletonList(ptpt));
        product_invoice_customer = product_invoice.copyForCustomer(customer);

        product_credit = new Product();
        product_credit.setVendor(supplier);
        product_credit.setType(ServiceType.TEMPLATE);
        ptpt = new ProductToPaymentType(product_credit, creditCard);
        product_credit.setPaymentTypes(Collections.singletonList(ptpt));

        product_all = new Product();
        product_all.setType(ServiceType.TEMPLATE);
        product_all.setVendor(supplier2);
        List<ProductToPaymentType> list = new ArrayList<ProductToPaymentType>();
        list.add(new ProductToPaymentType(product_all, invoice));
        list.add(new ProductToPaymentType(product_all, directDebit));
        list.add(new ProductToPaymentType(product_all, creditCard));
        product_all.setPaymentTypes(list);
    }

    @Test
    public void testConstructor() throws Exception {
        new PaymentDataValidator();
    }

    @Test(expected = PaymentDataException.class)
    public void validatePaymentTypeHandledByPSP_Null() throws Exception {
        PaymentDataValidator.validatePaymentTypeHandledByPSP(null);
    }

    @Test
    public void validatePaymentTypeHandledByPSP() throws Exception {
        PaymentDataValidator.validatePaymentTypeHandledByPSP(creditCard);
    }

    @Test(expected = PaymentDataException.class)
    public void validatePaymentTypeHandledByPSP_PaymentCollectionTypeOrganization()
            throws Exception {
        PaymentDataValidator.validatePaymentTypeHandledByPSP(invoice);
    }

    @Test
    public void validateNotNull() throws Exception {
        PaymentDataValidator.validateNotNull(new VOPaymentInfo(),
                new VOBillingContact());
    }

    @Test(expected = PaymentInformationException.class)
    public void validateNotNull_PaymentInfoNull() throws Exception {
        PaymentDataValidator.validateNotNull(null, new VOBillingContact());
    }

    @Test(expected = PaymentInformationException.class)
    public void validateNotNull_BillingContactNull() throws Exception {
        PaymentDataValidator.validateNotNull(new VOPaymentInfo(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePaymentTypeSupportedBySupplier_NullProduct()
            throws Exception {
        Assert.assertFalse(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, null, invoice));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                null, invoice);
    }

    @Test
    public void validatePaymentTypeSupportedBySupplier_CustomerSpecific()
            throws Exception {
        Assert.assertTrue(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer,
                        product_invoice_customer, invoice));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_invoice_customer, invoice);
    }

    @Test
    public void validatePaymentTypeSupportedBySupplier_NoRefEnabledDefault()
            throws Exception {
        Assert.assertTrue(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_invoice,
                        invoice));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_invoice, invoice);
    }

    @Test(expected = PaymentInformationException.class)
    public void validatePaymentTypeSupportedBySupplier_NoRefEnabledNotDefault()
            throws Exception {
        Assert.assertFalse(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_none,
                        creditCard));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_none, creditCard);
    }

    @Test(expected = PaymentInformationException.class)
    public void validatePaymentTypeSupportedBySupplier_NoRefNotEnabled()
            throws Exception {
        Assert.assertFalse(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_none,
                        directDebit));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_none, directDebit);
    }

    @Test
    public void validatePaymentTypeSupportedBySupplier_RefEnabled()
            throws Exception {
        OrganizationReference ref = createSupplierCustomerReference();
        OrganizationRefToPaymentType ortpt = new OrganizationRefToPaymentType();
        ortpt.setOrganizationReference(ref);
        ortpt.setOrganizationRole(customerRole);
        ortpt.setPaymentType(creditCard);
        ref.getPaymentTypes().add(ortpt);
        Assert.assertTrue(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_credit,
                        creditCard));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_credit, creditCard);
    }

    @Test(expected = PaymentInformationException.class)
    public void validatePaymentTypeSupportedBySupplier_RefNotEnabled()
            throws Exception {
        createSupplierCustomerReference();
        Assert.assertFalse(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_none,
                        creditCard));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_none, creditCard);
    }

    @Test(expected = PaymentInformationException.class)
    public void validatePaymentTypeSupportedBySupplier_RefNotEnabledDefault()
            throws Exception {
        createSupplierCustomerReference();
        Assert.assertFalse(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_none,
                        invoice));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_none, invoice);
    }

    @Test
    public void validatePaymentTypeSupportedForProduct_Present()
            throws Exception {
        Assert.assertTrue(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_invoice,
                        invoice));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_invoice, invoice);
    }

    @Test
    public void validatePaymentTypeSupportedForProduct_Template()
            throws Exception {
        Product subProduct = new Product();
        subProduct.setVendor(supplier);
        subProduct.setTemplate(product_invoice);
        subProduct.setPaymentTypes(new ArrayList<ProductToPaymentType>());
        subProduct.setType(ServiceType.SUBSCRIPTION);
        Product subSubProduct = new Product();
        subSubProduct.setVendor(supplier);
        subSubProduct.setTemplate(subProduct);
        subSubProduct.setPaymentTypes(new ArrayList<ProductToPaymentType>());
        subSubProduct.setType(ServiceType.SUBSCRIPTION);
        // check that validation is always done on the template
        Assert.assertTrue(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, subProduct, invoice));
        Assert.assertTrue(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, subSubProduct,
                        invoice));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                subSubProduct, invoice);
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                subProduct, invoice);
    }

    @Test(expected = PaymentInformationException.class)
    public void validatePaymentTypeSupportedForProduct_Template_NoPayment()
            throws Exception {
        Product subProduct = new Product();
        subProduct.setVendor(supplier);
        subProduct.setPaymentTypes(new ArrayList<ProductToPaymentType>());
        Product subSubProduct = new Product();
        subSubProduct.setTemplate(subProduct);
        subSubProduct.setVendor(supplier);
        subSubProduct.setPaymentTypes(new ArrayList<ProductToPaymentType>());
        // check that validation is always done on the template
        Assert.assertFalse(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, subProduct, invoice));
        Assert.assertFalse(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, subSubProduct,
                        invoice));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                subSubProduct, invoice);
    }

    @Test(expected = PaymentInformationException.class)
    public void validatePaymentTypeSupportedForProduct_NotPresent()
            throws Exception {
        Assert.assertFalse(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_invoice,
                        creditCard));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_invoice, creditCard);
    }

    @Test
    public void validatePaymentTypeSupportedForProduct_All() throws Exception {
        Assert.assertTrue(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_all,
                        invoice));
        Assert.assertTrue(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_all,
                        creditCard));
        Assert.assertTrue(PaymentDataValidator
                .isPaymentTypeSupportedBySupplier(customer, product_all,
                        directDebit));
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_all, invoice);
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_all, creditCard);
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                product_all, directDebit);
    }

    @Test
    public void validatePaymentInfoDataForUsage() throws Exception {
        PaymentInfo pi = new PaymentInfo();
        pi.setPaymentType(creditCard);
        pi.setExternalIdentifier("identifier");
        PaymentDataValidator.validatePaymentInfoDataForUsage(pi);
    }

    @Test
    public void validatePaymentInfoDataForUsage_HandledByOrg() throws Exception {
        PaymentInfo pi = new PaymentInfo();
        pi.setPaymentType(invoice);
        PaymentDataValidator.validatePaymentInfoDataForUsage(pi);
    }

    @Test(expected = PaymentInformationException.class)
    public void validatePaymentInfoDataForUsage_IdentifierNull()
            throws Exception {
        PaymentInfo pi = new PaymentInfo();
        pi.setPaymentType(creditCard);
        PaymentDataValidator.validatePaymentInfoDataForUsage(pi);
    }

    @Test(expected = PaymentInformationException.class)
    public void validatePaymentInfoDataForUsage_IdentifierEmpty()
            throws Exception {
        PaymentInfo pi = new PaymentInfo();
        pi.setPaymentType(creditCard);
        pi.setExternalIdentifier("   ");
        PaymentDataValidator.validatePaymentInfoDataForUsage(pi);
    }

    @Test(expected = PaymentDataException.class)
    public void validateVOPaymentType_Null() throws Exception {
        PaymentDataValidator.validateVOPaymentType(null);
    }

    @Test(expected = PaymentDataException.class)
    public void validateVOPaymentType_IdNull() throws Exception {
        PaymentDataValidator.validateVOPaymentType(new VOPaymentType());
    }

    @Test(expected = PaymentDataException.class)
    public void validateVOPaymentType_IdEmpty() throws Exception {
        VOPaymentType pt = new VOPaymentType();
        pt.setPaymentTypeId("   ");
        PaymentDataValidator.validateVOPaymentType(pt);
    }

    @Test
    public void validateVOPaymentType() throws Exception {
        VOPaymentType pt = new VOPaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        PaymentDataValidator.validateVOPaymentType(pt);
    }

    private OrganizationReference createSupplierCustomerReference() {
        OrganizationReference ref = new OrganizationReference(supplier,
                customer, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        supplier.getTargets().add(ref);
        customer.getSources().add(ref);
        return ref;
    }

    private OrganizationToRole createOrgToRole(OrganizationRoleType role) {
        OrganizationToRole orgToRole = new OrganizationToRole();
        OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(role);
        orgToRole.setOrganizationRole(orgRole);
        return orgToRole;
    }

}
