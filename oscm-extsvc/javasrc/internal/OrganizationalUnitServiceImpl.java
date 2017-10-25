/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/

package internal;

import java.util.List;

import javax.jws.WebService;

import org.oscm.intf.OrganizationalUnitService;
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
 * This is a stub implementation of the {@link OrganizationalUnitService} as the
 * Metro jax-ws tools do not allow to generate WSDL files from the service
 * interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 */
@WebService(serviceName = "OrganizationalUnitService", targetNamespace = "http://oscm.org/xsd", portName = "OrganizationalUnitServicePort", endpointInterface = "org.oscm.intf.OrganizationalUnitService")
public class OrganizationalUnitServiceImpl implements OrganizationalUnitService {

    @Override
    public void grantUserRoles(VOUser user, List<UnitRoleType> roles,
            VOOrganizationalUnit organizationalUnit)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revokeUserRoles(VOUser user, List<UnitRoleType> roles,
            VOOrganizationalUnit organizationalUnit)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganizationalUnit> getOrganizationalUnits(
            Pagination pagination) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOOrganizationalUnit createUnit(String unitName, String description,
            String referenceId) throws NonUniqueBusinessKeyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> getVisibleServices(String unitId, Pagination pagination, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> getAccessibleServices(String unitId, Pagination pagination, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addVisibleServices(String unitId, List<String> visibleServices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revokeVisibleServices(String unitId, List<String> visibleServices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAccessibleServices(String unitId, List<String> accessibleServices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revokeAccessibleServices(String unitId, List<String> accessibleServices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUnit(String organizationalUnitName)
            throws ObjectNotFoundException, OperationNotPermittedException,
            DeletionConstraintException, MailOperationException {
        throw new UnsupportedOperationException();
    }
}
