/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.validator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.IncompatibleRolesException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

public class OrganizationRoleValidator {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(OrganizationRoleValidator.class);

    /**
     * @param organizationRoles
     * @throws IncompatibleRolesException
     *             thrown if the given collection of roles contains more than
     *             one seller role, but multiple roles of the same type are
     *             allowed
     */
    public static void containsMultipleSellerRoles(
            Collection<OrganizationRoleType> organizationRoles,
            LogMessageIdentifier logMessageIdentifier)
            throws IncompatibleRolesException {

        Set<OrganizationRoleType> intersection = new HashSet<OrganizationRoleType>(
                Arrays.asList(OrganizationRoleType.BROKER,
                        OrganizationRoleType.RESELLER,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER));

        intersection.retainAll(organizationRoles);

        if (intersection.size() > 1) {
            if (onlySupplierAndTechnologyProvider(intersection)) {
                return;
            }
            throwIncompatibleRolesException(intersection, logMessageIdentifier);
        }
    }

    public static void containsSupplier(
            Collection<OrganizationRoleType> organizationRoles,
            String organizationId) throws OperationNotPermittedException {
        if (!organizationRoles.contains(OrganizationRoleType.SUPPLIER)) {
            OperationNotPermittedException ope = new OperationNotPermittedException(
                    "Operation failed, as organization " + organizationId
                            + " does not have required role(s) "
                            + OrganizationRoleType.SUPPLIER);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_ORGANIZATION_ROLE_REQUIRED,
                    organizationId, OrganizationRoleType.SUPPLIER.name());
            throw ope;
        }
    }

    private static void throwIncompatibleRolesException(
            Set<OrganizationRoleType> intersection,
            LogMessageIdentifier logMessageIdentifier)
            throws IncompatibleRolesException {
        IncompatibleRolesException ire = new IncompatibleRolesException(
                "The following roles " + intersection + " are not compatible");
        logger.logWarn(Log4jLogger.SYSTEM_LOG, ire, logMessageIdentifier);

        throw ire;
    }

    private static boolean onlySupplierAndTechnologyProvider(
            Set<OrganizationRoleType> intersection) {
        return intersection.size() == 2
                && intersection.containsAll(Arrays.asList(
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER));

    }
}
