/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *******************************************************************************/

package org.oscm.webservices;

import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.converter.api.Converter;
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.UserGroup;
import org.oscm.internal.types.exception.DeletingUnitWithSubscriptionsNotPermittedException;
import org.oscm.intf.OrganizationalUnitService;
import org.oscm.logging.LoggerFactory;
import org.oscm.pagination.Pagination;
import org.oscm.types.enumtypes.UnitRoleType;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.DomainObjectException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.vo.VOOrganizationalUnit;
import org.oscm.vo.VOService;
import org.oscm.vo.VOUser;
import org.oscm.webservices.logger.WebServiceLogger;

/**
 * Endpoint facade for {@link OrganizationalUnitService}
 */
@WebService(name = "OrganizationalUnitService", serviceName = "OrganizationalUnitService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.OrganizationalUnitService")
public class OrganizationalUnitServiceWS implements OrganizationalUnitService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(OrganizationalUnitServiceWS.class));

    UserGroupServiceLocalBean localService;
    DataService dataService;
    WebServiceContext wsContext;

    @Override
    public void grantUserRoles(VOUser user, List<UnitRoleType> roles,
            VOOrganizationalUnit organizationalUnit)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, dataService);

        PlatformUser pUser = Converter.convert(user, VOUser.class,
                PlatformUser.class);
        UserGroup group = Converter.convert(organizationalUnit,
                VOOrganizationalUnit.class, UserGroup.class);

        try {
            localService.grantUserRolesWithHandleUnitAdminRole(pUser,
                    EnumConverter.convertList(roles,
                            org.oscm.internal.types.enumtypes.UnitRoleType.class),
                    group);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void revokeUserRoles(VOUser user, List<UnitRoleType> roles,
            VOOrganizationalUnit organizationalUnit)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, dataService);

        PlatformUser pUser = Converter.convert(user, VOUser.class,
                PlatformUser.class);
        UserGroup group = Converter.convert(organizationalUnit,
                VOOrganizationalUnit.class, UserGroup.class);

        try {
            localService.revokeUserRoles(pUser,
                    EnumConverter.convertList(roles,
                            org.oscm.internal.types.enumtypes.UnitRoleType.class),
                    group);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOOrganizationalUnit> getOrganizationalUnits(
            Pagination pagination) {
        WS_LOGGER.logAccess(wsContext, dataService);

        List<UserGroup> units = localService
                .getOrganizationalUnits(Converter.convert(pagination,
                        Pagination.class, org.oscm.paginator.Pagination.class));

        return Converter.convertList(units, UserGroup.class,
                VOOrganizationalUnit.class);
    }

    @Override
    public VOOrganizationalUnit createUnit(String unitName, String description,
            String referenceId) throws NonUniqueBusinessKeyException {
        WS_LOGGER.logAccess(wsContext, dataService);

        try {
            UserGroup unit = localService.createUserGroup(unitName, description,
                    referenceId);

            return Converter.convert(unit, UserGroup.class,
                    VOOrganizationalUnit.class);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deleteUnit(String organizationalUnitName)
            throws ObjectNotFoundException, OperationNotPermittedException,
            DeletionConstraintException, MailOperationException {
        WS_LOGGER.logAccess(wsContext, dataService);

        try {
            localService.deleteUserGroup(organizationalUnitName);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (DeletingUnitWithSubscriptionsNotPermittedException e) {
            throw new DeletionConstraintException(
                    DomainObjectException.ClassEnum.USER_GROUP,
                    organizationalUnitName,
                    DomainObjectException.ClassEnum.SUBSCRIPTION);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOService> getVisibleServices(String unitId,
            Pagination pagination, String marketplaceId) {
        final org.oscm.paginator.Pagination paginationNew = Converter.convert(
                pagination, Pagination.class,
                org.oscm.paginator.Pagination.class);
        List<Product> visibleServices = localService.getVisibleServices(unitId,
                paginationNew, marketplaceId);
        return Converter.convertList(visibleServices, Product.class,
                VOService.class, dataService);
    }

    @Override
    public List<VOService> getAccessibleServices(String unitId,
            Pagination pagination, String marketplaceId) {
        final org.oscm.paginator.Pagination paginationNew = Converter.convert(
                pagination, Pagination.class,
                org.oscm.paginator.Pagination.class);
        List<Product> accessibleServices = localService
                .getAccessibleServices(unitId, paginationNew, marketplaceId);
        return Converter.convertList(accessibleServices, Product.class,
                VOService.class, dataService);

    }

    @Override
    public void addVisibleServices(String unitId,
            List<String> visibleServices) {
        localService.addVisibleServices(unitId, visibleServices);
    }

    @Override
    public void revokeVisibleServices(String unitId,
            List<String> visibleServices) {
        localService.revokeVisibleServices(unitId, visibleServices);
    }

    @Override
    public void addAccessibleServices(String unitId,
            List<String> accessibleServices) {
        localService.addAccessibleServices(unitId, accessibleServices);
    }

    @Override
    public void revokeAccessibleServices(String unitId,
            List<String> accessibleServices) {
        localService.revokeAccessibleServices(unitId, accessibleServices);
    }
}
