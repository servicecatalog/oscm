/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 30.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.domobjects.PricedProductRole;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOPricedRole;

/**
 * Assembler for the priced product role data.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PricedProductRoleAssembler extends BaseAssembler {

    private static final String FIELD_NAME_PRICE_PER_USER = "pricePerUser";
    
    private static final String FIELD_NAME_ROLE = "role";

    /**
     * Converts a priced product role domain object into the corresponding value
     * object representation.
     * 
     * @param role
     *            The domain object to be assembled.
     * @return The value object representation of the priced product role.
     */
    public static VOPricedRole toVOPricedProductRole(PricedProductRole role,
            LocalizerFacade facade) {
        VOPricedRole voRole = new VOPricedRole();
        updateValueObject(voRole, role);
        voRole.setPricePerUser(role.getPricePerUser());
        voRole.setRole(RoleAssembler.toVORoleDefinition(
                role.getRoleDefinition(), facade));
        return voRole;
    }

    /**
     * Converts a list of priced product role domain objects into the
     * corresponding value object representation.
     * 
     * @param role
     *            The domain objects to be assembled.
     * @return The value object representation of the priced product roles.
     */
    public static List<VOPricedRole> toVOPricedProductRoles(
            List<PricedProductRole> roles, LocalizerFacade facade) {
        ArrayList<VOPricedRole> result = new ArrayList<VOPricedRole>();
        for (PricedProductRole currentRole : roles) {
            VOPricedRole voRole = new VOPricedRole();
            updateValueObject(voRole, currentRole);
            voRole.setPricePerUser(currentRole.getPricePerUser());
            voRole.setRole(RoleAssembler.toVORoleDefinition(
                    currentRole.getRoleDefinition(), facade));
            result.add(voRole);
        }
        return result;
    }

    /**
     * Validates the value object and returns a domain object representation of
     * it. Only the primitive typed fields will be considered.
     * 
     * @param pricedProductRole
     *            The role to be converted to a domain object.
     * @return The domain object representation of the priced product role.
     * @throws ValidationException
     *             Thrown in case a negative price is configured.
     */
    public static PricedProductRole toPricedProductRole(
            VOPricedRole pricedProductRole) throws ValidationException {
        PricedProductRole result = new PricedProductRole();
        validatePricedProductRole(pricedProductRole);
        copyAttributes(pricedProductRole, result);
        return result;
    }

    /**
     * Validates the value object and returns the updated domain object
     * representation of it. Only the primitive typed fields will be considered.
     * 
     * @param pricedProductRole
     *            The role to be converted to a domain object.
     * @param doPricedProductRole
     *            The role as domain object that should be updated.
     * @return The domain object representation of the priced product role.
     * @throws ValidationException
     *             Thrown in case a negative price is configured.
     * @throws ConcurrentModificationException
     *             Thrown in case the version attribute of the value object does
     *             not match the domain object.
     */
    public static PricedProductRole updatePricedProductRole(
            VOPricedRole pricedProductRole,
            PricedProductRole doPricedProductRole) throws ValidationException,
            ConcurrentModificationException {
        validatePricedProductRole(pricedProductRole);
        verifyVersionAndKey(doPricedProductRole, pricedProductRole);
        copyAttributes(pricedProductRole, doPricedProductRole);
        return doPricedProductRole;
    }

    /**
     * Copies the attributes from the value object to the specified domain
     * object. Does not perform any validation.
     * 
     * @param pricedProductRole
     *            The value object to read the data from.
     * @param doPricedProductRole
     *            The domain object which attributes will be updated.
     */
    private static void copyAttributes(VOPricedRole pricedProductRole,
            PricedProductRole doPricedProductRole) {
        doPricedProductRole
                .setPricePerUser(pricedProductRole.getPricePerUser());
    }

    /**
     * Validates the value object representation of the priced product role.
     * 
     * @param pricedProductRole
     *            The object to be validated.
     * @throws ValidationException
     *             Thrown in case the validation failed, e.g. a price is given a
     *             negative value.
     */
    private static void validatePricedProductRole(VOPricedRole pricedProductRole)
            throws ValidationException {
        BLValidator.isNonNegativeNumber(FIELD_NAME_PRICE_PER_USER,
                pricedProductRole.getPricePerUser());
        BLValidator.isValidPriceScale(FIELD_NAME_PRICE_PER_USER,
                pricedProductRole.getPricePerUser());
    }

    static void validatePricedProductRoles(List<VOPricedRole> productRoles)
            throws ValidationException {
        Set<Long> usedRoleKeys = new HashSet<Long>();
        for (VOPricedRole productRole : productRoles) {
            if (productRole.getRole() == null) {
                throw new ValidationException(ReasonEnum.REQUIRED,
                        FIELD_NAME_ROLE, new Object[] { productRole });
            }
            if (productRole.getRole().getKey() == 0) {
                throw new ValidationException(ReasonEnum.INVALID_REFERENCE,
                        FIELD_NAME_ROLE, new Object[] { productRole });
            }
            if (!usedRoleKeys.add(Long.valueOf(productRole.getRole().getKey()))) {
                throw new ValidationException(ReasonEnum.DUPLICATE_VALUE,
                        FIELD_NAME_ROLE, new Object[] { productRole });
            }
        }
    }
}
