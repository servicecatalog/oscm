/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 14.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.permission.PermissionCheck;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOServicePaymentConfiguration;

/**
 * @author weiser
 * 
 */
public class PaymentConfigurationFilter {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PaymentConfigurationFilter.class);

    DataService ds;

    public PaymentConfigurationFilter(DataService ds) {
        this.ds = ds;
    }

    public boolean isDefaultCustomerConfigurationChanged(Set<VOPaymentType> conf) {
        Organization vendor = ds.getCurrentUser().getOrganization();
        Set<String> paymentTypeIds = convertToIdSet(conf);
        List<OrganizationRefToPaymentType> types = vendor.getPaymentTypes(
                false, vendor.getVendorRoleForPaymentConfiguration(),
                OrganizationRoleType.PLATFORM_OPERATOR.name());
        boolean changed = false;
        for (OrganizationRefToPaymentType ref : types) {
            // if it is used as default and the id is also be removable from the
            // list, it is no change. If it is not the same, it is a change.
            changed = changed
                    || !(ref.isUsedAsDefault() == paymentTypeIds.remove(ref
                            .getPaymentType().getPaymentTypeId()));
        }
        // if there are still ids in the set, it is a change
        return (changed || !paymentTypeIds.isEmpty());
    }

    public boolean isDefaultServiceConfigurationChanged(Set<VOPaymentType> conf) {
        Organization vendor = ds.getCurrentUser().getOrganization();
        Set<String> paymentTypeIds = convertToIdSet(conf);
        List<OrganizationRefToPaymentType> types = vendor.getPaymentTypes(
                false, vendor.getVendorRoleForPaymentConfiguration(),
                OrganizationRoleType.PLATFORM_OPERATOR.name());
        boolean changed = false;
        for (OrganizationRefToPaymentType ref : types) {
            // if it is used as default and the id is also be removable from the
            // list, it is no change. If it is not the same, it is a change.
            changed = changed
                    || !(ref.isUsedAsServiceDefault() == paymentTypeIds
                            .remove(ref.getPaymentType().getPaymentTypeId()));
        }
        // if there are still ids in the set, it is a change
        return (changed || !paymentTypeIds.isEmpty());
    }

    public List<VOOrganizationPaymentConfiguration> filterCustomerConfiguration(
            List<VOOrganizationPaymentConfiguration> conf)
            throws ObjectNotFoundException, OperationNotPermittedException {
        List<VOOrganizationPaymentConfiguration> result = new ArrayList<VOOrganizationPaymentConfiguration>();
        if (conf == null) {
            return result;
        }
        Organization vendor = ds.getCurrentUser().getOrganization();
        for (VOOrganizationPaymentConfiguration c : conf) {
            Organization org = new Organization();
            org.setOrganizationId(c.getOrganization().getOrganizationId());
            org = (Organization) ds.getReferenceByBusinessKey(org);

            final OrganizationReference ref = checkSellerRelationship(vendor,
                    org);
            if (customerConfigurationChanged(c, ref)) {
                result.add(c);
            }
        }
        return result;
    }

    public OrganizationReference checkSellerRelationship(Organization vendor,
            Organization org) throws OperationNotPermittedException {
        OrganizationRoleType type = vendor
                .getVendorRoleForPaymentConfiguration();
        if (type == null) {
            throw noVendorOfCustomer(vendor, org);
        }
        switch (type) {
        case SUPPLIER:
            PermissionCheck.supplierOfCustomer(vendor, org, logger, null);
            return vendor.getCustomerReference(org,
                    OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        case RESELLER:
            PermissionCheck.resellerOfCustomer(vendor, org, logger, null);
            return vendor.getCustomerReference(org,
                    OrganizationReferenceType.RESELLER_TO_CUSTOMER);
        default:
            // won't happen as type will be null which is handled above
            throw noVendorOfCustomer(vendor, org);
        }
    }

    public List<VOServicePaymentConfiguration> filterServiceConfiguration(
            List<VOServicePaymentConfiguration> conf)
            throws ObjectNotFoundException, OperationNotPermittedException {
        List<VOServicePaymentConfiguration> result = new ArrayList<VOServicePaymentConfiguration>();
        if (conf == null) {
            return result;
        }
        Organization vendor = ds.getCurrentUser().getOrganization();
        for (VOServicePaymentConfiguration c : conf) {
            // try to find the product
            Product product = ds.getReference(Product.class, c.getService()
                    .getKey());
            // check if i'm the owner
            PermissionCheck.owns(product, vendor, logger, null);
            // check if it is a template
            checkIsTemplate(vendor, product);
            if (serviceConfigurationChanged(c, product)) {
                result.add(c);
            }
        }
        return result;
    }

    public void checkIsTemplate(Organization vendor, Product product)
            throws OperationNotPermittedException {
        if (product.getType() == ServiceType.TEMPLATE
                || vendor.getGrantedRoleTypes().contains(
                        OrganizationRoleType.RESELLER)) {
            return;
        }
        String message = String
                .format("Seller '%s' tried to configure payment for service '%s' that is not a global template.",
                        vendor.getOrganizationId(),
                        Long.valueOf(product.getKey()));
        OperationNotPermittedException e = new OperationNotPermittedException(
                message);
        logger.logWarn(
                Log4jLogger.SYSTEM_LOG,
                e,
                LogMessageIdentifier.WARN_CONFIGURE_SERVICE_PAYMENT_FAILED_NO_GLOBAL_TEMPLATE,
                vendor.getOrganizationId(), String.valueOf(product.getKey()));
        throw e;
    }

    private static Set<String> convertToIdSet(Set<VOPaymentType> conf) {
        HashSet<String> result = new HashSet<String>();
        for (VOPaymentType pt : conf) {
            result.add(pt.getPaymentTypeId());
        }
        return result;
    }

    boolean customerConfigurationChanged(VOOrganizationPaymentConfiguration c,
            final OrganizationReference ref) {
        Set<String> paymentTypeIds = convertToIdSet(c.getEnabledPaymentTypes());
        List<OrganizationRefToPaymentType> paymentTypes = ref.getPaymentTypes();
        boolean changed = false;
        for (OrganizationRefToPaymentType refToPt : paymentTypes) {
            changed = changed
                    || !paymentTypeIds.remove(refToPt.getPaymentType()
                            .getPaymentTypeId());
        }
        return (changed || !paymentTypeIds.isEmpty());
    }

    boolean serviceConfigurationChanged(VOServicePaymentConfiguration c,
            Product product) {
        List<ProductToPaymentType> existing = product.getPaymentTypes();
        Set<String> paymentTypeIds = convertToIdSet(c.getEnabledPaymentTypes());
        boolean changed = false;
        for (ProductToPaymentType ref : existing) {
            changed = changed
                    || !paymentTypeIds.remove(ref.getPaymentType()
                            .getPaymentTypeId());
        }
        return (changed || !paymentTypeIds.isEmpty());
    }

    private OperationNotPermittedException noVendorOfCustomer(
            Organization vendor, Organization org) {
        String message = String
                .format("Organization '%s' is not supplier or reseller of customer '%s'",
                        vendor.getOrganizationId(), org.getOrganizationId());
        OperationNotPermittedException e = new OperationNotPermittedException(
                message);
        logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                LogMessageIdentifier.WARN_NO_SUPPLIER_OR_RESELLER_OF_CUSTOMER,
                vendor.getOrganizationId(), org.getOrganizationId());
        return e;
    }

}
