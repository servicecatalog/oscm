/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 28.04.2010                                                      
 *                                                                              
 *  Completion Time: 22.07.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.validation;

import java.util.List;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentInformationException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;

/**
 * Auxiliary class to ensure consistency of payment related information.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PaymentDataValidator {

    public static final Log4jLogger logger = LoggerFactory
            .getLogger(PaymentDataValidator.class);

    /**
     * Checks if the specified payment type is handled by a PSP. If it is not,
     * an exception is created, logged and thrown.
     * 
     * @param paymentType
     *            The payment type to be checked.
     * @throws PaymentDataException
     *             Thrown in case the payment is not marked for being handled by
     *             an organization.
     */
    public static void validatePaymentTypeHandledByPSP(PaymentType paymentType)
            throws PaymentDataException {
        if (paymentType == null
                || paymentType.getCollectionType() != PaymentCollectionType.PAYMENT_SERVICE_PROVIDER) {
            PaymentDataException ex = new PaymentDataException(
                    "Validation of the payment type information failed",
                    PaymentDataException.Reason.PAYMENT_TYPE_UNSUPPORTED_BY_PSP);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_PAYMENT_TYPE_NOT_HANDLED_BY_PSP);
            throw ex;
        }
    }

    private static Product getTemplate(Product product) {
        while (ServiceType.isSubscription(product.getType())
                || product.getType() == ServiceType.CUSTOMER_TEMPLATE) {
            product = product.getTemplate();
        }
        // if vendor is broker, get the supplier product (for evaluating its
        // payment types)
        if (product.getType() == ServiceType.PARTNER_TEMPLATE
                && product.getVendor().getGrantedRoleTypes()
                        .contains(OrganizationRoleType.BROKER)) {
            product = product.getTemplate();
        }
        return product;
    }

    /**
     * Validates if the provided {@link PaymentType} is enabled by the supplier
     * {@link Organization} of the provided product {@link Product} for the
     * provided customer {@link Organization} in general and especially for the
     * product. If not, a {@link PaymentInformationException} is thrown.
     * 
     * @param customer
     *            the customer organization
     * @param product
     *            the product to be subscribed
     * @param paymentType
     *            the payment type the customer wants to use for the
     *            subscription
     * @throws PaymentInformationException
     */
    public static void validatePaymentTypeSupportedBySupplier(
            Organization customer, Product product, PaymentType paymentType)
            throws PaymentInformationException {
        if (product == null) {
            throw new IllegalArgumentException(
                    "Product parameter must not be null");
        }
        product = getTemplate(product);

        if (!isPaymentTypeSupportedBySupplier(customer, product.getVendor(),
                paymentType)) {
            PaymentInformationException ex = new PaymentInformationException(
                    "Payment type not supported by supplier");
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_PAYMENT_TYPE_UNSUPPORTED);
            throw ex;
        }
        if (!isPaymentTypeSupportedForProduct(product, paymentType)) {
            PaymentInformationException ex = new PaymentInformationException(
                    "Payment type not supported for this service");
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    ex,
                    LogMessageIdentifier.WARN_PAYMENT_TYPE_UNSUPPORTED_FOR_SERVICE);
            throw ex;
        }
    }

    /**
     * Checks if the provided {@link PaymentType} is enabled by the provided
     * supplier {@link Organization} for the provided customer
     * {@link Organization}.
     * 
     * @param customer
     *            the customer organization.
     * @param supplier
     *            the supplier of the service.
     * @param paymentType
     *            the payment type the customer wants to use for the
     *            subscription.
     * @return <code>true</code> if the {@link PaymentType} is enabled by the
     *         supplier otherwise <code>false</code>.
     */
    private static boolean isPaymentTypeSupportedBySupplier(
            Organization customer, Organization supplier,
            PaymentType paymentType) {
        final List<OrganizationRefToPaymentType> types;
        if (relationExists(customer, supplier)) {
            // if we have a relation to the supplier return the customer
            // specific configuration
            types = customer
                    .getPaymentTypes(false, OrganizationRoleType.CUSTOMER,
                            supplier.getOrganizationId());
        } else {
            // otherwise return the suppliers default configuration
            types = supplier
                    .getPaymentTypes(
                            true,
                            supplier.getGrantedRoleTypes().contains(
                                    OrganizationRoleType.RESELLER) ? OrganizationRoleType.RESELLER
                                    : OrganizationRoleType.SUPPLIER,
                            OrganizationRoleType.PLATFORM_OPERATOR.name());
        }
        for (OrganizationRefToPaymentType orgToPt : types) {
            if (orgToPt.getPaymentType().equals(paymentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the provided {@link PaymentType} is enabled by the provided
     * supplier {@link Organization} for the provided customer
     * {@link Organization}.
     * 
     * @param customer
     *            the customer organization.
     * @param supplier
     *            the supplier of the service.
     * @param paymentType
     *            the payment type the customer wants to use for the
     *            subscription.
     * @return <code>true</code> if the {@link PaymentType} is enabled by the
     *         supplier otherwise <code>false</code>.
     */
    public static boolean isPaymentTypeSupportedBySupplier(
            Organization customer, Product product, PaymentType paymentType) {
        if (product != null) {
            product = getTemplate(product);
            Organization supplier = product.getVendor();
            return isPaymentTypeSupportedBySupplier(customer, supplier,
                    paymentType)
                    && isPaymentTypeSupportedForProduct(product, paymentType);
        }
        return false;
    }

    /**
     * Checks if the provided {@link PaymentType} is enabled for the provided
     * product {@link Organization}.
     * 
     * @param product
     * @param paymentType
     *            the payment type the customer wants to use for the
     *            subscription.
     * @return <code>true</code> if the {@link PaymentType} is enabled for the
     *         product otherwise <code>false</code>.
     */
    private static boolean isPaymentTypeSupportedForProduct(Product product,
            PaymentType paymentType) {
        if (product != null) {
            product = getTemplate(product);
            final List<ProductToPaymentType> types = product.getPaymentTypes();
            for (ProductToPaymentType productToPt : types) {
                if (productToPt.getPaymentType().equals(paymentType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the supplier-customer-relation exists between the supplier and
     * the customer organization
     * 
     * @param customer
     *            the customer organization
     * @param supplier
     *            the supplier organization
     * @return <code>true</code> if they have the relation, otherwise
     *         <code>false</code>.
     */
    private static boolean relationExists(Organization customer,
            Organization supplier) {
        
        boolean result = false;
        List<OrganizationReference> supplierOrgReferences = customer
                .getSourcesForType(OrganizationReferenceType
                        .getOrgRefTypeForSourceRoles(supplier
                                .getGrantedRoleTypes()));
        for (OrganizationReference orgRef : supplierOrgReferences) {
            if (orgRef.getSource() == supplier) {
                result = true;
                break;
            }
        }
        
        return result;
    }

    /**
     * Validates that the provided {@link VOBillingContact} and
     * {@link VOPaymentInfo} are not <code>null</code>.
     * 
     * @param pi
     *            the {@link VOPaymentInfo} to validate
     * @param bc
     *            the {@link VOBillingContact} to validate
     * @throws PaymentInformationException
     *             Thrown in case one of them is <code>null</code>
     */
    public static void validateNotNull(VOPaymentInfo pi, VOBillingContact bc)
            throws PaymentInformationException {
        if (pi == null) {
            PaymentInformationException ex = new PaymentInformationException(
                    "No payment info has been passed.");
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_PAYMENT_INFO_MISSING);
            throw ex;
        }
        if (bc == null) {
            PaymentInformationException ex = new PaymentInformationException(
                    "No billing contact has been passed.");
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_BILLING_CONTACT_MISSING);
            throw ex;
        }
    }

    /**
     * Check that all required data is provided for using the passed
     * {@link PaymentInfo} - this means for PSP handled payment the identifier
     * must be set.
     * 
     * @param pi
     *            the {@link PaymentInfo} to check.
     * @throws PaymentInformationException
     *             thrown in case of missing data
     */
    public static void validatePaymentInfoDataForUsage(PaymentInfo pi)
            throws PaymentInformationException {
        if (pi.getPaymentType().getCollectionType() == PaymentCollectionType.PAYMENT_SERVICE_PROVIDER) {
            if (Strings.isEmpty(pi.getExternalIdentifier())) {
                PaymentInformationException pie = new PaymentInformationException(
                        "PSP identifier is missing.");
                logger.logWarn(Log4jLogger.SYSTEM_LOG, pie,
                        LogMessageIdentifier.WARN_PSP_IDENTIFIER_MISSING);
                throw pie;
            }
        }
    }

    /**
     * Checks if the provided {@link VOPaymentType} is not <code>null</code> and
     * its id is not empty.
     * 
     * @param pt
     *            the {@link VOPaymentType} to check
     * @throws PaymentDataException
     *             Thrown when the provided {@link VOPaymentType} is
     *             <code>null</code> or its id is empty.
     */
    public static void validateVOPaymentType(VOPaymentType pt)
            throws PaymentDataException {
        if (pt == null || Strings.isEmpty(pt.getPaymentTypeId())) {
            String errMessage = "Payment type and payment type id must be provided";
            PaymentDataException ex = new PaymentDataException(errMessage,
                    PaymentDataException.Reason.INVALID_PAYMENT_DATA);
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    ex,
                    LogMessageIdentifier.WARN_PAYMENT_TYPE_OR_PAYMENT_TYPE_ID_NEEDED);
            throw ex;
        }
    }
}
