/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 19.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPaymentType;

/**
 * Assembler to convert the organization value objects to the according domain
 * object and vice versa.
 * 
 * @author pock
 */
public class OrganizationAssembler extends BaseAssembler {

    public static final String FIELD_NAME_ADDRESS = "address";

    public static final String FIELD_NAME_ORGANIZATION_ID = "organizationId";

    public static final String FIELD_NAME_EMAIL = "email";

    public static final String FIELD_NAME_SUPPORT_EMAIL = "supportEmail";

    public static final String FIELD_NAME_LOCALE = "locale";

    public static final String FIELD_NAME_NAME = "name";

    public static final String FIELD_NAME_PHONE = "phone";

    public static final String FIELD_NAME_DN = "distinguishedName";

    public static final String FIELD_NAME_URL = "url";

    public static final String FIELD_NAME_DESCRIPTION = "description";

    /**
     * Creates a new VOOrganization object and fills the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param organization
     *            The domain object containing the values to be set.
     * @param imageDefined
     *            <code>true</code> if an image exists for the organization
     * @param localizerFacade
     *            a localizer
     * @return The created value object or null if the domain object was null.
     */
    public static VOOrganization toVOOrganization(Organization organization,
            boolean imageDefined, LocalizerFacade localizerFacade) {
        return toVOOrganization(organization, imageDefined, localizerFacade,
                PerformanceHint.ALL_FIELDS);
    }

    /**
     * Creates a new VOOrganization object and fills the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param organization
     *            The domain object containing the values to be set.
     * @param imageDefined
     *            <code>true</code> if an image exists for the organization
     * @param localizerFacade
     *            a localizer
     * @return The created value object or null if the domain object was null.
     */
    public static VOOrganization toVOOrganization(Organization organization) {
        return toVOOrganization(organization, false, null,
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
    }

    public static VOOrganization toVOOrganization(Organization organization,
            boolean imageDefined, LocalizerFacade localizerFacade,
            PerformanceHint scope) {
        if (organization == null) {
            return null;
        }

        VOOrganization voOrganization = new VOOrganization();
        switch (scope) {
        case ONLY_IDENTIFYING_FIELDS:
            fillIdentifyingFields(voOrganization, organization);
            break;
        case ONLY_FIELDS_FOR_LISTINGS:
            fillListingFields(voOrganization, organization);
            break;
        default:
            fillVOOrganization(voOrganization, organization, imageDefined,
                    null, localizerFacade);
        }
        updateValueObject(voOrganization, organization);

        return voOrganization;
    }

    /**
     * Creates new VOOrganization objects and fills the fields with the
     * corresponding data from the domain objects. The image defined information
     * is set to <code>false</code> for all organizations involved.
     * 
     * @param organizations
     *            The organizations to be returned in value object
     *            representation.
     * @param localizerFacade
     *            The localizer facade object.
     * @return The value object representation of the domain objects.
     */
    public static List<VOOrganization> toVOOrganizations(
            List<Organization> organizations, LocalizerFacade localizerFacade) {
        List<VOOrganization> result = new ArrayList<VOOrganization>();
        List<Long> orgKeys = new ArrayList<Long>();
        for (Organization org : organizations) {
            orgKeys.add(Long.valueOf(org.getKey()));
        }
        localizerFacade.prefetch(orgKeys, Collections
                .singletonList(LocalizedObjectTypes.ORGANIZATION_DESCRIPTION));
        for (Organization org : organizations) {
            result.add(toVOOrganization(org, false, localizerFacade));
        }
        return result;
    }

    public static VOOrganization toVOOrganizationWithDiscount(
            Organization organization, boolean imageDefined, Discount discount,
            LocalizerFacade localizerFacade) {
        if (organization == null) {
            return null;
        }
        VOOrganization voOrganization = new VOOrganization();

        fillVOOrganization(voOrganization, organization, imageDefined,
                discount, localizerFacade);

        updateValueObject(voOrganization, organization);
        return voOrganization;
    }

    /**
     * Creates a new VOOperatorOrganization object and fills the fields with the
     * corresponding fields from the given organization domain object. This
     * value object is used only by operations of the operator client. <br>
     * In contrast to the <code>toVOOrganization</code> method, the
     * VOOperatorOrganization will be filled with values which are only of
     * interest in the context of execution operator functions (e.g. the roles
     * of the organization).
     * 
     * @param organization
     *            The domain object containing the values to be set.
     * @param imageDefined
     *            <code>true</code> if an image exists for the organization
     * @param localizerFacade
     *            a localizer
     * @return A new VOOperatorOrganization object or null if the domain obejct
     *         is null.
     */
    public static VOOperatorOrganization toVOOperatorOrganization(
            Organization organization, boolean imageDefined,
            LocalizerFacade localizerFacade) {
        VOOperatorOrganization voOperatorOrganization = new VOOperatorOrganization();
        fillVOOrganization(voOperatorOrganization, organization, imageDefined,
                null, localizerFacade);

        // Add organization roles to VO
        Set<OrganizationToRole> orgToRole = organization.getGrantedRoles();
        if (orgToRole != null) {
            List<OrganizationRoleType> organizationRoles = new ArrayList<OrganizationRoleType>();
            for (OrganizationToRole organizationToRole : orgToRole) {
                organizationRoles.add(organizationToRole.getOrganizationRole()
                        .getRoleName());
            }
            voOperatorOrganization.setOrganizationRoles(organizationRoles);
        }

        // Add payment types to VO
        List<OrganizationRefToPaymentType> orgPaymentTyps = organization
                .getPaymentTypes(OrganizationRoleType.PLATFORM_OPERATOR.name());
        if (orgPaymentTyps != null) {
            List<VOPaymentType> voPaymentType = new ArrayList<VOPaymentType>();
            for (OrganizationRefToPaymentType organizationToPaymentType : orgPaymentTyps) {
                voPaymentType.add(PaymentTypeAssembler.toVOPaymentType(
                        organizationToPaymentType.getPaymentType(),
                        localizerFacade));
            }
            voOperatorOrganization.setPaymentTypes(voPaymentType);
        }

        updateValueObject(voOperatorOrganization, organization);
        return voOperatorOrganization;
    }

    private static void fillIdentifyingFields(VOOrganization voOrganization,
            Organization organization) {
        voOrganization.setOrganizationId(organization.getOrganizationId());
        voOrganization.setName(organization.getName());
    }

    private static void fillListingFields(VOOrganization voOrganization,
            Organization organization) {
        voOrganization.setOrganizationId(organization.getOrganizationId());
        voOrganization.setName(organization.getName());
        voOrganization.setAddress(organization.getAddress());
    }

    private static void fillVOOrganization(VOOrganization voOrganization,
            Organization organization, boolean imageDefined, Discount discount,
            LocalizerFacade localizerFacade) {
        fillIdentifyingFields(voOrganization, organization);
        voOrganization.setAddress(organization.getAddress());
        voOrganization.setEmail(organization.getEmail());
        voOrganization.setLocale(organization.getLocale());
        voOrganization
                .setDomicileCountry(organization.getDomicileCountryCode());
        voOrganization.setPhone(organization.getPhone());
        voOrganization
                .setDistinguishedName(organization.getDistinguishedName());
        voOrganization.setUrl(organization.getUrl());
        if (localizerFacade != null) {
            voOrganization.setDescription(localizerFacade.getText(
                    organization.getKey(),
                    LocalizedObjectTypes.ORGANIZATION_DESCRIPTION));
        }
        voOrganization.setImageDefined(imageDefined);

        voOrganization.setSupportEmail(organization.getSupportEmail());

        if (discount != null) {
            voOrganization
                    .setDiscount(DiscountAssembler.toVODiscount(discount));
        }

        if (organization.getOperatorPriceModel() != null) {
            voOrganization.setOperatorRevenueShare(organization
                    .getOperatorPriceModel().getRevenueShare());
        }
        
        if(organization.getTenant()!=null){
            voOrganization.setTenantKey(organization.getTenant().getKey());
        }
    }

    /**
     * Creates a new customer Organization object and fills the fields with the
     * corresponding fields from the given value object.
     * 
     * @param voOrganization
     *            The value object containing the values to be set.
     * @return The created domain object or null if the value object was null.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     */
    public static Organization toCustomer(VOOrganization voOrganization)
            throws ValidationException {
        validate(voOrganization);
        final Organization organization = new Organization();
        copyAttributes(organization, voOrganization);
        return organization;
    }

    /**
     * Updates the fields in the Supplier or Technology Provider Organization
     * object to reflect the changes performed in the value object.
     * 
     * Creates a new Vendor Organization (Supplier or Technology Provider)
     * object and fills the fields with the corresponding fields from the given
     * value object. For vendors, email contact, phone, url, name and address
     * are mandatory; for customers not.
     * 
     * @param organization
     *            The domain object to be updated.
     * @param voOrganization
     *            The value object.
     * @return The updated domain object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public static Organization toVendor(VOOrganization voOrganization)
            throws ValidationException {
        validateVendorMandatoryFields(voOrganization);
        return toCustomer(voOrganization);
    }

    /**
     * Updates the fields in the Customer Organization object to reflect the
     * changes performed in the value object.
     * 
     * @param organization
     *            The domain object to be updated.
     * @param voOrganization
     *            The value object.
     * @return The updated domain object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public static Organization updateCustomer(Organization organization,
            VOOrganization voOrganization) throws ValidationException,
            ConcurrentModificationException {
        validate(voOrganization);
        verifyVersionAndKey(organization, voOrganization);
        copyAttributes(organization, voOrganization);
        return organization;
    }

    /**
     * Updates the fields in the Vendor (Supplier or Technology Provider)
     * Organization object to reflect the changes performed in the value object.
     * For vendors, email contact, phone, url, name and address are mandatory;
     * for customers not.
     * 
     * @param organization
     *            The domain object to be updated.
     * @param voOrganization
     *            The value object.
     * @return The updated domain object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public static Organization updateVendor(Organization organization,
            VOOrganization voOrganization) throws ValidationException,
            ConcurrentModificationException {
        validateVendorMandatoryFields(voOrganization);
        return updateCustomer(organization, voOrganization);
    }

    private static void copyAttributes(Organization organization,
            VOOrganization voOrganization) {
        organization.setAddress(voOrganization.getAddress());
        organization.setEmail(voOrganization.getEmail());
        organization.setLocale(voOrganization.getLocale());
        organization.setName(voOrganization.getName());
        organization.setPhone(voOrganization.getPhone());
        organization
                .setDistinguishedName(voOrganization.getDistinguishedName());
        organization.setUrl(voOrganization.getUrl());
        organization.setSupportEmail(voOrganization.getSupportEmail());
        long tenantKey = voOrganization.getTenantKey();
        if (tenantKey != 0) {
            Tenant t = new Tenant();
            t.setKey(tenantKey);
            organization.setTenant(t);
        }
    }

    /**
     * Validate a organization value object.
     * 
     * @param voOrganization
     *            the value object to validate
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     */
    private static void validate(VOOrganization voOrganization)
            throws ValidationException {
        BLValidator.isDescription(FIELD_NAME_ADDRESS,
                voOrganization.getAddress(), false);
        BLValidator.isEmail(FIELD_NAME_EMAIL, voOrganization.getEmail(), false);
        BLValidator.isLocale(FIELD_NAME_LOCALE, voOrganization.getLocale(),
                true);
        BLValidator.isName(FIELD_NAME_NAME, voOrganization.getName(), false);
        BLValidator.isName(FIELD_NAME_PHONE, voOrganization.getPhone(), false);
        BLValidator.isDN(FIELD_NAME_DN, voOrganization.getDistinguishedName(),
                false);
        BLValidator.isUrl(FIELD_NAME_URL, voOrganization.getUrl(), false);
        BLValidator.isDescription(FIELD_NAME_DESCRIPTION,
                voOrganization.getDescription(), false);
        BLValidator.isEmail(FIELD_NAME_SUPPORT_EMAIL,
                voOrganization.getSupportEmail(), false);
    }

    /**
     * Validate the fields that are mandatory only for a vendor, but not for a
     * customer.
     * 
     * @param voOrganization
     *            the value object to validate
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     */
    private static void validateVendorMandatoryFields(
            VOOrganization voOrganization) throws ValidationException {
        BLValidator.isNotBlank(FIELD_NAME_EMAIL, voOrganization.getEmail());
        BLValidator.isNotBlank(FIELD_NAME_PHONE, voOrganization.getPhone());
        BLValidator.isNotBlank(FIELD_NAME_URL, voOrganization.getUrl());
        BLValidator.isNotBlank(FIELD_NAME_NAME, voOrganization.getName());
        BLValidator.isNotBlank(FIELD_NAME_ADDRESS, voOrganization.getAddress());

        if (voOrganization.getOperatorRevenueShare() != null) {
            BLValidator.isInRange("operatorRevenueShare",
                    voOrganization.getOperatorRevenueShare(),
                    RevenueShareModel.MIN_REVENUE_SHARE,
                    RevenueShareModel.MAX_REVENUE_SHARE);
        }
    }

    public static Organization toOrganization(VOOrganization voOrganization) {
        Organization organization = new Organization();
        organization.setKey(voOrganization.getKey());
        organization.setOrganizationId(voOrganization.getOrganizationId());
        organization.setName(voOrganization.getName());
        return organization;
    }
}
