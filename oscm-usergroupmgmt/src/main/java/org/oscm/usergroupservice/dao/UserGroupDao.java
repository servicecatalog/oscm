/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.usergroupservice.dao;

import java.util.List;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.UnitRoleAssignment;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToInvisibleProduct;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.paginator.Pagination;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * @author yuyin
 * 
 */
@Stateless
@LocalBean
public class UserGroupDao {
    @EJB(beanInterface = DataService.class)
    DataService dm;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UserGroupDao.class);

    public List<UserGroup> getUserGroupsForOrganizationWithoutDefault() {
        long orgKey = dm.getCurrentUser().getOrganization().getKey();
        Query query = dm
                .createNamedQuery("UserGroup.findByOrganizationKeyWithoutDefault");
        query.setParameter("organization_tkey", Long.valueOf(orgKey));
        return ParameterizedTypes.list(query.getResultList(), UserGroup.class);
    }

    public UserGroup getUserGroupDetails(long userGroupKey)
            throws ObjectNotFoundException {
        return dm.getReference(UserGroup.class, userGroupKey);
    }

    public List<UserGroup> getUserGroupsForOrganization() {
        long orgKey = dm.getCurrentUser().getOrganization().getKey();

        return this.getUserGroupsForOrganization(orgKey);
    }

    public List<UserGroup> getUserGroupsForOrganization(Pagination pagination) {
        long orgKey = dm.getCurrentUser().getOrganization().getKey();
        Query query = dm.createNamedQuery("UserGroup.findByOrganizationKey");

        query.setParameter("organization_tkey", Long.valueOf(orgKey));
        query.setFirstResult(pagination.getOffset());
        query.setMaxResults(pagination.getLimit());

        return ParameterizedTypes.list(query.getResultList(), UserGroup.class);
    }

    public List<UserGroup> getUserGroupsForOrganization(long orgKey) {
        Query query = dm.createNamedQuery("UserGroup.findByOrganizationKey");
        query.setParameter("organization_tkey", Long.valueOf(orgKey));
        return ParameterizedTypes.list(query.getResultList(), UserGroup.class);
    }

    public List<UserGroup> getUserGroupsForUserWithoutDefault(long userKey) {
        Query query = dm.createNamedQuery("UserGroup.findByUserWithoutDefault");
        query.setParameter("userKey", userKey);
        return ParameterizedTypes.list(query.getResultList(), UserGroup.class);
    }

    public List<UserGroup> getUserGroupsForUser(String userId) {
        Query query = dm.createNamedQuery("UserGroup.findByUserId");
        query.setParameter("userId", userId);
        List<UserGroup> groups = ParameterizedTypes.list(query.getResultList(),
                UserGroup.class);
        return groups;
    }

    public List<Long> getInvisibleProductKeysForUser(long userKey) {
        Query query = dm.createNamedQuery("UserGroup.findInvisibleProductKeys");
        query.setParameter("user_tkey", Long.valueOf(userKey));
        return ParameterizedTypes.list(query.getResultList(), Long.class);
    }

    public List<UserGroupToInvisibleProduct> getInvisibleProducts(
            long userGroupKey) {
        Query query = dm.createNamedQuery("UserGroup.getInvisibleProducts");
        query.setParameter("usergroup_tkey", Long.valueOf(userGroupKey));
        return ParameterizedTypes.list(query.getResultList(),
                UserGroupToInvisibleProduct.class);
    }

    public List<Long> getInvisibleProductKeysForGroup(long userGroupKey) {
        Query query = dm
                .createNamedQuery("UserGroup.findInvisibleProductKeysForGroup");
        query.setParameter("usergroup_tkey", Long.valueOf(userGroupKey));
        return ParameterizedTypes.list(query.getResultList(), Long.class);
    }

    public long getUserGroupCountForUser(long userKey) {
        Query query = dm.createNamedQuery("UserGroup.countUserGroup");
        query.setParameter("user_tkey", Long.valueOf(userKey));
        Long count = (Long) query.getSingleResult();
        return count.longValue();

    }

    public long getUserCountForGroup(long groupKey) {
        Query query = dm.createNamedQuery("UserGroup.countUser");
        query.setParameter("usergroup_tkey", Long.valueOf(groupKey));
        Long count = (Long) query.getSingleResult();
        return count.longValue();
    }

    public long getUserCountForDefaultGroup(String organizationId) {
        Query query = dm.createNamedQuery("UserGroup.countUserForDefaultGroup");
        query.setParameter("organizationId", organizationId);
        Long count = (Long) query.getSingleResult();
        return count.longValue();
    }

    public List<String> getAssignedUserIdsForGroup(long groupKey) {
        Query query = dm.createNamedQuery("UserGroup.findAssignedUserIds");
        query.setParameter("usergroup_tkey", Long.valueOf(groupKey));
        return ParameterizedTypes.list(query.getResultList(), String.class);
    }

    public boolean isNotTerminatedSubscriptionAssignedToUnit(long groupKey) {
        Query query = dm
                .createNamedQuery("Subscription.isNotTerminatedSubscriptionAssignedToUnit");
        query.setParameter("unitKey", Long.valueOf(groupKey));
        query.setParameter("subscriptionStatus", SubscriptionStatus.DEACTIVATED);
        query.setMaxResults(1);
        try {
            query.getSingleResult();
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }

    /**
     * Method is used to get all user unit roles from the database.
     * 
     * @return list of user unit roles
     */
    public List<UnitUserRole> getRolesAvailableForUnit() {
        Query query = dm.createNamedQuery("UnitUserRole.findAllRoles");
        List<UnitUserRole> unitUserRoles = ParameterizedTypes.list(
                query.getResultList(), UnitUserRole.class);
        return unitUserRoles;
    }

    public UnitUserRole getUnitRoleByName(String roleName) {
        Query query = dm.createNamedQuery("UnitUserRole.findByBusinessKey");
        query.setParameter("roleName", UnitRoleType.valueOf(roleName));
        return (UnitUserRole) query.getSingleResult();
    }

    public UserGroupToUser getUserGroupAssignment(UserGroup userGroup,
            PlatformUser platformUser) throws ObjectNotFoundException {
        UserGroupToUser findTemplate = new UserGroupToUser();

        findTemplate.setUserGroup(userGroup);
        findTemplate.setPlatformuser(platformUser);

        return (UserGroupToUser) dm.getReferenceByBusinessKey(findTemplate);
    }

    public UnitRoleAssignment getRoleAssignmentByUserAndGroup(long groupKey,
            String userId) {
        Query query = dm
                .createNamedQuery("UnitRoleAssignment.findByUserAndGroup");
        query.setParameter("usergroup_tkey", Long.valueOf(groupKey));
        query.setParameter("userId", userId);
        UnitRoleAssignment unitRoleAssignment = null;
        try {
            unitRoleAssignment = (UnitRoleAssignment) query.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
        return unitRoleAssignment;
    }

    public List<UserGroup> getUserGroupsForUserWithRole(long userKey,
            long userRoleKey) {
        Query query = dm.createNamedQuery("UserGroup.findByUserWithRole");
        query.setParameter("platformuser_tkey", Long.valueOf(userKey));
        query.setParameter("unituserrole_tkey", Long.valueOf(userRoleKey));
        return ParameterizedTypes.list(query.getResultList(), UserGroup.class);
    }

    public List<UserGroup> getUserGroupsForUserWithRoleWithoutDefault(
            long userKey, long userRoleKey) {
        Query query = dm
                .createNamedQuery("UserGroup.findByUserWithRoleWithoutDefault");
        query.setParameter("platformuser_tkey", Long.valueOf(userKey));
        query.setParameter("unituserrole_tkey", Long.valueOf(userRoleKey));
        return ParameterizedTypes.list(query.getResultList(), UserGroup.class);
    }

    public UnitRoleAssignment modifyUnitRoleTypeForUserGroup(
            Entry<UserGroup, UnitUserRole> groupWithRoles,
            PlatformUser platformUser) throws ObjectNotFoundException {
        UnitRoleAssignment unitRoleAssignment = getRoleAssignmentByUserAndGroupOrNew(
                groupWithRoles, platformUser);

        unitRoleAssignment.setUnitUserRole(groupWithRoles.getValue());
        return (UnitRoleAssignment) dm.merge(unitRoleAssignment);
    }

    public List<Product> getAccessibleServices(String unitId,
            Pagination pagination, String marketplaceId) {
        Query query = dm.createNamedQuery("UserGroup.findAccessibleServices");
        query.setParameter("userGroupKey", Long.valueOf(unitId));
        query.setParameter("marketplaceKey", Long.valueOf(marketplaceId));
        query.setFirstResult(pagination.getOffset());
        query.setMaxResults(pagination.getLimit());
        List<Product> accessibleProducts = ParameterizedTypes.list(
                query.getResultList(), Product.class);
        return accessibleProducts;
    }

    public List<Product> getVisibleServices(String unitId,
            Pagination pagination, String marketplaceId) {
        Query query = dm.createNamedQuery("UserGroup.findVisibleServices");
        query.setParameter("userGroupKey", Long.valueOf(unitId));
        query.setParameter("marketplaceKey", Long.valueOf(marketplaceId));
        query.setFirstResult(pagination.getOffset());
        query.setMaxResults(pagination.getLimit());
        List<Product> visibleProducts = ParameterizedTypes.list(
                query.getResultList(), Product.class);
        return visibleProducts;
    }

    private UnitRoleAssignment getRoleAssignmentByUserAndGroupOrNew(
            Entry<UserGroup, UnitUserRole> groupWithRoles,
            PlatformUser platformUser) {
        UnitRoleAssignment roleAssignmentByUserAndGroup = getRoleAssignmentByUserAndGroup(
                groupWithRoles.getKey().getKey(), platformUser.getUserId());
        if (roleAssignmentByUserAndGroup == null) {
            roleAssignmentByUserAndGroup = new UnitRoleAssignment();
            UserGroupToUser usgtu = getUserGroupToUserOrNew(platformUser,
                    groupWithRoles.getKey());
            roleAssignmentByUserAndGroup.setUserGroupToUser(usgtu);
        }
        return roleAssignmentByUserAndGroup;
    }

    private UserGroupToUser getUserGroupToUserOrNew(PlatformUser platformUser,
            UserGroup key) {
        UserGroupToUser usgtu = new UserGroupToUser();
        usgtu.setPlatformuser(platformUser);
        usgtu.setUserGroup(key);
        UserGroupToUser foundUgtu = null;
        try {
            foundUgtu = (UserGroupToUser) dm.getReferenceByBusinessKey(usgtu);
        } catch (ObjectNotFoundException e) {
            logger.logDebug("Nothing found. So let's create.");
        }
        if (foundUgtu == null) {
            try {
                dm.persist(usgtu);
            } catch (NonUniqueBusinessKeyException e) {
                // It should never happen, as we can't find object above.
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR);
            }
        } else {
            usgtu = foundUgtu;
        }
        return usgtu;
    }

}
