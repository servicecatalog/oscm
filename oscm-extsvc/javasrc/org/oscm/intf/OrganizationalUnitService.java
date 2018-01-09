/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2015-07-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.oscm.pagination.Pagination;
import org.oscm.types.enumtypes.UnitRoleType;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.vo.VOOrganizationalUnit;
import org.oscm.vo.VOService;
import org.oscm.vo.VOUser;

/**
 * Remote interface used for handling organization units.
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface OrganizationalUnitService {

    /**
     * Assigns the given roles to the specified user within the given
     * organizational unit. If the user already has roles in the unit, the
     * method has no effect. The roles which are already assigned to the user
     * remain unchanged.
     * <p>
     * Required role: administrator of the organization to which the
     * organizational unit belongs, or OU administrator of the unit
     * 
     * @param user
     *            the value object specifying the user to whom the roles are to
     *            be assigned
     * @param roles
     *            the roles to be set
     * @param organizationalUnit
     *            the organizational unit for which the roles are to be granted
     *            to the user.
     * @throws ObjectNotFoundException
     *             if the specified user is not found or is not a member of the
     *             given organizational unit
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * 
     */
    @WebMethod(operationName = "grantUserRolesInUnit")
    @RequestWrapper(className = "grantUserRolesRequest")
    @ResponseWrapper(className = "grantUserRolesResponse")
    void grantUserRoles(@WebParam(name = "user") VOUser user,
            @WebParam(name = "roles") List<UnitRoleType> roles,
            @WebParam(name = "organizationalUnit") VOOrganizationalUnit organizationalUnit)
                    throws ObjectNotFoundException,
                    OperationNotPermittedException;

    /**
     * Removes the given roles from the specified user within the given
     * organizational unit. If the user does not have any roles in the unit, the
     * method has no effect.
     * <p>
     * Required role: administrator of the organization to which the
     * organizational unit belongs, or OU administrator of the unit
     * 
     * @param user
     *            the value object specifying the user from whom the roles are
     *            to be removed
     * @param roles
     *            the roles to be removed
     * @param organizationalUnit
     *            the organizational unit for which the roles are to be removed
     *            from the user.
     * @throws ObjectNotFoundException
     *             if the specified user is not found or is not a member of the
     *             given organizational unit
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * 
     */
    @WebMethod(operationName = "revokeUserRolesInUnit")
    @RequestWrapper(className = "revokeUserRolesRequest")
    @ResponseWrapper(className = "revokeUserRolesResponse")
    void revokeUserRoles(@WebParam(name = "user") VOUser user,
            @WebParam(name = "roles") List<UnitRoleType> roles,
            @WebParam(name = "organizationalUnit") VOOrganizationalUnit organizationalUnit)
                    throws ObjectNotFoundException,
                    OperationNotPermittedException;

    /**
     * Returns the organizational units existing in the organization of the
     * calling user.
     * <p>
     * Required role: administrator of an organization
     * 
     * @param pagination
     *            a pagination object specifying the offset and number of
     *            organizational units to be retrieved, or <code>null</code> to
     *            retrieve all units
     * @return the list of organizational units
     */
    @WebMethod
    List<VOOrganizationalUnit> getOrganizationalUnits(
            @WebParam(name = "pagination") Pagination pagination);

    /**
     * Creates an organizational unit in the organization of the calling user.
     * <p>
     * Required role: administrator of an organization
     * 
     * @param unitName
     *            the name of the new unit
     * @param description
     *            the description of the new unit
     * @param referenceId
     *            an identifier for referencing the new unit, for example, in
     *            customer billing data
     * @return the new organizational unit
     * @throws NonUniqueBusinessKeyException
     *             if a unit with the given name already exists
     */
    @WebMethod
    VOOrganizationalUnit createUnit(
            @WebParam(name = "unitName") String unitName,
            @WebParam(name = "description") String description,
            @WebParam(name = "referenceId") String referenceId)
                    throws NonUniqueBusinessKeyException;

    /**
     * Deletes the given organizational unit in the organization of the calling
     * user.
     * <p>
     * Required role: administrator of the organization to which the
     * organizational unit belongs
     * 
     * @param organizationalUnitName
     *            the name of the unit to be deleted
     * @throws ObjectNotFoundException
     *             if the unit is not found in the calling user's organization
     * @throws OperationNotPermittedException
     *             if the unit does not belong to the calling user's
     *             organization or if subscriptions are assigned to it
     * @throws DeletionConstraintException
     * @throws MailOperationException
     */
    @WebMethod
    void deleteUnit(
            @WebParam(name = "organizationalUnitName") String organizationalUnitName)
                    throws ObjectNotFoundException,
                    OperationNotPermittedException, DeletionConstraintException,
                    MailOperationException;

    /**
     * Retrieves the services which are visible to the OU administrators of the
     * specified organizational unit. The services are also visible to the
     * administrators of the organization to which the unit belongs, but not to
     * the normal users within the unit.
     * <p>
     * The services returned are in the <code>ACTIVE</code> or
     * <code>SUSPENDED</code>.
     * <p>
     * Required role: administrator of the organization to which the
     * organizational unit belongs, or OU administrator of the unit
     *
     * @param unitId
     *            the name of the organizational unit for which to return the
     *            services
     * @param pagination
     *            a pagination object specifying the offset and number of
     *            services to be retrieved, or <code>null</code> to retrieve all
     *            available services
     * @param marketplaceId
     *            the identifier of the marketplace on which the services to be
     *            returned are published
     * @return the list of services. The list is empty if the given
     *         organizational unit or marketplace does not exist.
     */
    @WebMethod
    List<VOService> getVisibleServices(@WebParam(name = "unitId") String unitId,
            @WebParam(name = "pagination") Pagination pagination,
            @WebParam(name = "marketplaceId") String marketplaceId);

    /**
     * Retrieves the services which are visible to the members of the specified
     * organizational unit, independent of their role within the unit. The
     * services are also visible to the administrators of the organization to
     * which the unit belongs.
     * <p>
     * The services returned are in the <code>ACTIVE</code> or
     * <code>SUSPENDED</code>.
     * <p>
     * Required role: administrator of the organization to which the
     * organizational unit belongs, or member of the unit
     *
     * @param unitId
     *            the name of the organizational unit for which to return the
     *            services
     * @param pagination
     *            a pagination object specifying the offset and number of
     *            services to be retrieved, or <code>null</code> to retrieve all
     *            available services
     * @param marketplaceId
     *            the identifier of the marketplace on which the services to be
     *            returned are published
     * @return the list of services. The list is empty if the given
     *         organizational unit or marketplace does not exist.
     */
    @WebMethod
    List<VOService> getAccessibleServices(
            @WebParam(name = "unitId") String unitId,
            @WebParam(name = "pagination") Pagination pagination,
            @WebParam(name = "marketplaceId") String marketplaceId);

    /**
     * Sets the services which are to be visible to the OU administrators of the
     * specified organizational unit. The services are also visible to the
     * administrators of the organization to which the unit belongs, but not to
     * the normal users within the unit.
     * <p>
     * If any of the given services is already visible to the OU administrators,
     * it is ignored.
     * <p>
     * Required role: administrator of the organization to which the
     * organizational unit belongs
     *
     * @param unitId
     *            the name of the organizational unit for which to set the
     *            services
     * @param visibleServices
     *            the keys of the services to be made visible
     */
    @WebMethod
    void addVisibleServices(
            @WebParam(name = "unitId") String unitId,
            @WebParam(name = "services") List<String> visibleServices);

    /**
     * Specifies the services which are to be made invisible for the OU
     * administrators of the specified organizational unit. The services remain
     * visible to the administrators of the organization to which the unit
     * belongs, and invisible to the normal users within the unit.
     * <p>
     * If any of the given services is already invisible for the OU
     * administrators, it is ignored.
     * <p>
     * Required role: administrator of the organization to which the
     * organizational unit belongs
     *
     * @param unitId
     *            the name of the organizational unit for which to set the
     *            services
     * @param visibleServices
     *            the keys of the services to be made invisible to the OU
     *            administrators
     */
    @WebMethod
    void revokeVisibleServices(
            @WebParam(name = "unitId") String unitId,
            @WebParam(name = "services") List<String> visibleServices);

    /**
     * Sets the services which are to be visible to the members of the specified
     * organizational unit, independent of their role within the unit. The
     * services are also visible to the administrators of the organization to
     * which the unit belongs.
     * <p>
     * If any of the given services is already visible to the unit members, it
     * is ignored.
     * <p>
     * Required role: administrator of the organization to which the
     * organizational unit belongs, or OU administrator of the unit
     *
     * @param unitId
     *            the name of the organizational unit for which to set the
     *            services
     * @param accessibleServices
     *            the keys of the services to be made visible
     */
    @WebMethod
    void addAccessibleServices(
            @WebParam(name = "unitId") String unitId,
            @WebParam(name = "services") List<String> accessibleServices);

    /**
     * Specifies the services which are to be made invisible for the normal
     * users of the specified organizational unit. The services remain visible
     * to the administrators of the organization to which the unit belongs and
     * to the OU administrators of the unit.
     * <p>
     * If any of the given services is already invisible for the normal unit
     * users, it is ignored.
     * <p>
     * Required role: administrator of the organization to which the
     * organizational unit belongs, or OU administrator of the unit
     *
     * @param unitId
     *            the name of the organizational unit for which to set the
     *            services
     * @param accessibleServices
     *            the keys of the services to be made invisible to the normal
     *            unit users
     */
    @WebMethod
    void revokeAccessibleServices(
            @WebParam(name = "unitId") String unitId,
            @WebParam(name = "services") List<String> accessibleServices);

}
