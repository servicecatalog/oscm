/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.control.SendMailControl;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.components.response.ReturnCode;
import org.oscm.internal.components.response.ReturnType;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.UserModificationConstraintException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.paginator.Pagination;
import org.oscm.paginator.PaginationInt;
import org.oscm.permission.PermissionCheck;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionWithRoles;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.validation.VersionAndKeyValidator;

/**
 * @author weiser
 * 
 */
@Remote(UserService.class)
@Stateless
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class UserServiceBean implements UserService {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UserServiceBean.class);

    @EJB(beanInterface = IdentityServiceLocal.class)
    IdentityServiceLocal isl;

    @EJB(beanInterface = IdentityService.class)
    IdentityService isr;

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @EJB(beanInterface = LdapSettingsManagementServiceLocal.class)
    LdapSettingsManagementServiceLocal lsmsl;

    @EJB(beanInterface = SubscriptionListServiceLocal.class)
    SubscriptionListServiceLocal slsl;

    @EJB(beanInterface = SubscriptionServiceLocal.class)
    SubscriptionServiceLocal ssl;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal lsl;

    @Resource
    SessionContext sc;

    @Inject
    UserGroupServiceLocalBean userGroupService;

    DataConverter dc = new DataConverter();

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public List<POUser> getUsers() {

        List<POUser> userList = new ArrayList<>();
        List<PlatformUser> users = isl.getOrganizationUsers();
        for (PlatformUser u : users) {
            userList.add(dc.toPOUser(u));
        }

        return userList;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public POUserDetails getUserDetails(String userId, String tenantId)
            throws SaaSApplicationException {

        PlatformUser u = isl.getPlatformUser(userId, tenantId, true);
        Set<UserRoleType> availableRoles = isl.getAvailableUserRolesForUser(u);
        POUserDetails result = dc.toPOUserDetails(u, availableRoles);
        result.setMappedAttributes(lsmsl.getMappedAttributes());

        return result;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public Response saveUser(POUserDetails user)
            throws SaaSApplicationException {

        try {
            // read user by key as id may have been changed
            PlatformUser existing = ds.getReference(PlatformUser.class,
                    user.getKey());

            // keep a reference (not managed) with the old email
            PlatformUser old = existing.getEmail() != null ? UserDataAssembler
                    .copyPlatformUser(existing) : null;

            updateUserAndRoles(user, existing);

            // notify subscriptions
            isl.notifySubscriptionsAboutUserUpdate(existing);

            // send mail if email was changed
            isl.sendUserUpdatedMail(existing, old);

            if (user.getAssignedRoles().isEmpty()) {
                PlatformUser administrator = ds.getReference(
                        PlatformUser.class, 1000);

                // send mail to administrator if no role selected
                isl.sendAdministratorNotifyMail(administrator, user.getUserId());
            }
        } catch (SaaSApplicationException e) {
            sc.setRollbackOnly();
            throw e;
        }

        return new Response();
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public Response importUsersInOwnOrganization(byte[] users,
            String marketplaceId) throws SaaSApplicationException {
        isr.importUsersInOwnOrganization(users, marketplaceId);
        return new Response();
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public Response importUsers(byte[] users, String orgID, String marketplaceId)
            throws SaaSApplicationException {
        isr.importUsers(users, orgID, marketplaceId);
        return new Response();
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public Response resetUserPassword(POUser user, String marketplaceId)
            throws SaaSApplicationException {

        try {
            PlatformUser u = isl.getPlatformUser(user.getUserId(), true);
            VersionAndKeyValidator.verify(u, user.getKey(), user.getVersion());
            isl.resetUserPassword(u, marketplaceId);
        } catch (SaaSApplicationException e) {
            sc.setRollbackOnly();
            throw e;
        }

        return new Response();
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public Response deleteUser(POUser user, String marketplaceId,
            String tenantId) throws SaaSApplicationException {

        try {

            PlatformUser u = isl.getPlatformUser(user.getUserId(), tenantId,
                    true);
            VersionAndKeyValidator.verify(u, user.getKey(), user.getVersion());
            userGroupService.addLogEntryWhenDeleteUser(u);
            isl.deleteUser(u, marketplaceId);
        } catch (ObjectNotFoundException e) {
            // do nothing as we wanted to delete the user
        } catch (SaaSApplicationException e) {
            sc.setRollbackOnly();
            throw e;
        }

        return new Response();
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public POUserAndSubscriptions getNewUserData() {

        PlatformUser user = ds.getCurrentUser();
        List<SubscriptionWithRoles> list = slsl.getSubcsriptionsWithRoles(
                user.getOrganization(),
                Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS);
        Set<UserRoleType> availableRoles = isl
                .getAvailableUserRolesForUser(user);
        List<Long> roleKeys = toRoleKeyList(list);
        LocalizerFacade lf = new LocalizerFacade(lsl, user.getLocale());
        lf.prefetch(roleKeys,
                Collections.singletonList(LocalizedObjectTypes.ROLE_DEF_NAME));
        POUserAndSubscriptions result = dc.toPOUserAndSubscriptionsNew(list,
                availableRoles, lf);
        result.setLocale(user.getLocale());

        return result;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public Response createNewUser(POUserAndSubscriptions user,
            String marketplaceId) throws SaaSApplicationException {

        VOUserDetails ud = convertToUser(user);
        ud.setOrganizationId(ds.getCurrentUser().getOrganization()
                .getOrganizationId());
        Response response = new Response();
        try {

            SendMailControl.setSendMail(Boolean.FALSE);
            ud = isl.createUser(ud, marketplaceId);
            SendMailControl.setSendMail(null);
            if (ud == null) {
                // trigger for create user defined and action suspended
                response.getReturnCodes().add(warn("progress.createUser"));
            } else {
                user.setKey(ud.getKey());
                user.setVersion(ud.getVersion());

                PlatformUser pu = ds.getReference(PlatformUser.class,
                        ud.getKey());
                isl.sendMailToCreatedUser(SendMailControl.getPassword(), true,
                        SendMailControl.getMarketplace(), pu);
            }
        } catch (SaaSApplicationException e) {
            sc.setRollbackOnly();
            throw e;
        } finally {
            SendMailControl.clear();
        }

        return response;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public POUserAndSubscriptions getUserAndSubscriptionDetails(String userId,
            String tenantId) throws SaaSApplicationException {

        PlatformUser u = isl.getPlatformUser(userId, tenantId, true);
        PlatformUser user = ds.getCurrentUser();

        List<SubscriptionWithRoles> list = Collections.emptyList();
        List<UsageLicense> assignments = Collections.emptyList();

        Set<UserRoleType> availableRoles = isl.getAvailableUserRolesForUser(u);
        List<Long> roleKeys = toRoleKeyList(list);
        LocalizerFacade lf = new LocalizerFacade(lsl, user.getLocale());
        lf.prefetch(roleKeys,
                Collections.singletonList(LocalizedObjectTypes.ROLE_DEF_NAME));
        Set<SettingType> mappedLdapAttributes = lsmsl.getMappedAttributes();
        return dc.toPOUserAndSubscriptionsExisting(u, list, availableRoles,
                assignments, lf, mappedLdapAttributes);
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public Response saveUserAndSubscriptionAssignment(
            POUserAndSubscriptions user, List<POUserGroup> allGroups)
            throws SaaSApplicationException {

        Response response = new Response();
        try {
            Map<UserGroup, UnitUserRole> groupsToBeAssigned = new HashMap<>();
            if (allGroups != null && !allGroups.isEmpty()) {
                groupsToBeAssigned = validateGroupsExist(allGroups,
                        user.getGroupsToBeAssigned());
            }
            // read user by key as id may have been changed
            PlatformUser existing = ds.getReference(PlatformUser.class,
                    user.getKey());
            // keep a reference (not managed) with the old email
            PlatformUser old = existing.getEmail() != null ? UserDataAssembler
                    .copyPlatformUser(existing) : null;

            // update user data and roles
            updateUserAndRoles(user, existing);

            updateUserGroups(groupsToBeAssigned, user, existing);
            List<UsageLicense> assignments = slsl.getSubscriptionAssignments(
                    existing, Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS);
            boolean done = updateSubscriptionAssignment(user, assignments);
            if (!done) {
                // trigger for add revoke user defined and suspended
                response.getReturnCodes().add(warn("progress.default"));
            }

            // notify subscriptions
            ds.flush();
            PlatformUser userInDB = ds.getReference(PlatformUser.class,
                    user.getKey());
            if (dc.isUserInformationUpdated(user, userInDB)
                    || dc.isUserRoleUpdated(user.getAssignedRoles(), existing)) {
                isl.notifySubscriptionsAboutUserUpdate(existing);
            }
            if (!canUserBeSubscriptionOwner(user)) {
                removeOwnerIfUserCantOwnSubscription(existing);
            }
            // send mail if email was changed
            isl.sendUserUpdatedMail(existing, old);
        } catch (SaaSApplicationException e) {
            sc.setRollbackOnly();
            throw e;
        }

        return response;
    }

    private void removeOwnerIfUserCantOwnSubscription(PlatformUser user) {
        List<Subscription> subscriptions = slsl.getSubscriptionsForOwner(user);
        for (Subscription subscription : subscriptions) {
            ssl.removeSubscriptionOwner(subscription);
        }
    }

    private boolean canUserBeSubscriptionOwner(POUserAndSubscriptions user) {
        Set<UserRoleType> roles = user.getAssignedRoles();
        return roles.contains(UserRoleType.ORGANIZATION_ADMIN)
                || roles.contains(UserRoleType.UNIT_ADMINISTRATOR)
                || roles.contains(UserRoleType.SUBSCRIPTION_MANAGER);
    }

    Map<String, VOUsageLicense> getAssignments(List<UsageLicense> assignments,
            List<POSubscription> subscriptions, VOUser u) {

        Map<String, VOUsageLicense> result = new HashMap<>();
        for (POSubscription sub : subscriptions) {

            if (sub.isAssigned()) {
                UsageLicense lic = getLicense(sub.getId(), assignments);
                if (lic != null) {
                    if (!sameRole(lic, sub)) {
                        // role change
                        result.put(sub.getId(), convertToLicense(sub, u));
                    }
                } else {
                    // added
                    result.put(sub.getId(), convertToLicense(sub, u));
                }
            }
        }

        return result;
    }

    boolean sameRole(UsageLicense lic, POSubscription sub) {
        return lic.getRoleDefinition() == null
                && sub.getUsageLicense().getPoServieRole() == null
                || lic.getRoleDefinition()
                        .getRoleId()
                        .equals(sub.getUsageLicense().getPoServieRole().getId());
    }

    Set<String> getUnassignments(List<UsageLicense> assignments,
            List<POSubscription> subscriptions) {

        Set<String> result = new HashSet<>();
        Set<String> existingSubs = new HashSet<>();

        for (UsageLicense lic : assignments) {
            existingSubs.add(lic.getSubscription().getSubscriptionId());
        }

        for (POSubscription sub : subscriptions) {

            if (!sub.isAssigned() && existingSubs.contains(sub.getId())) {
                result.add(sub.getId());
            }
        }

        return result;
    }

    private UsageLicense getLicense(String subId, List<UsageLicense> assignments) {

        UsageLicense result = null;
        for (UsageLicense lic : assignments) {
            if (subId.equals(lic.getSubscription().getSubscriptionId())) {
                result = lic;
                break;
            }
        }

        return result;
    }

    private VOUsageLicense convertToLicense(POSubscription s, VOUser u) {

        VOUsageLicense lic = new VOUsageLicense();
        lic.setUser(u);
        POUsagelicense poLic = s.getUsageLicense();
        if (poLic != null && poLic.getPoServieRole() != null) {
            POServiceRole sr = poLic.getPoServieRole();
            VORoleDefinition rd = new VORoleDefinition();
            rd.setKey(sr.getKey());
            rd.setRoleId(sr.getId());
            rd.setVersion(sr.getVersion());

            lic.setRoleDefinition(rd);
            lic.setKey(poLic.getKey());
            lic.setVersion(poLic.getVersion());
        }

        return lic;
    }

    VOUserDetails convertToUser(POUserAndSubscriptions u) {

        VOUserDetails ud = new VOUserDetails();
        ud.setEMail(u.getEmail());
        ud.setFirstName(u.getFirstName());
        ud.setLastName(u.getLastName());
        ud.setLocale(u.getLocale());
        ud.setSalutation(u.getSalutation());
        ud.setUserId(u.getUserId());
        ud.setKey(u.getKey());
        ud.setVersion(u.getVersion());
        ud.setTenantId(u.getTenantId());

        return ud;
    }

    List<Long> toRoleKeyList(List<SubscriptionWithRoles> list) {

        Set<Long> result = new HashSet<>();
        if (list == null) {
            return Collections.emptyList();
        }
        for (SubscriptionWithRoles swr : list) {
            for (RoleDefinition rd : swr.getRoles()) {
                result.add(Long.valueOf(rd.getKey()));
            }
        }

        return new ArrayList<>(result);
    }

    ReturnCode warn(String msgKey) {

        ReturnCode rc = new ReturnCode(ReturnType.INFO, msgKey);
        rc.setMember("progressPanel");

        return rc;
    }

    void updateUserAndRoles(POUserDetails user, PlatformUser existing)
            throws SaaSApplicationException {

        PermissionCheck.sameOrg(ds.getCurrentUser(), existing, logger);
        VersionAndKeyValidator.verify(existing, user.getKey(),
                user.getVersion());

        // validation
        PlatformUser updated = dc.updatePlatformUser(user,
                UserDataAssembler.copyPlatformUser(existing));

        // ldap check, verify bk uniqueness
        isl.verifyIdUniquenessAndLdapAttributes(existing, updated);

        // update user data
        dc.updatePlatformUser(user, existing);

        // update roles
        isl.setUserRolesInt(user.getAssignedRoles(), existing);
    }

    private void updateUserGroups(
            Map<UserGroup, UnitUserRole> userGroupsToBeAssigned,
            POUserAndSubscriptions user, PlatformUser existing)
            throws NonUniqueBusinessKeyException,
            OperationNotPermittedException, ObjectNotFoundException,
            MailOperationException, UserModificationConstraintException {

        Map<Long, Entry<UserGroup, UnitUserRole>> groupsToBeAssignedMap = new HashMap<>();

        for (Entry<UserGroup, UnitUserRole> groupsWithRoles : userGroupsToBeAssigned
                .entrySet()) {
            groupsToBeAssignedMap.put(
                    Long.valueOf(groupsWithRoles.getKey().getKey()),
                    groupsWithRoles);
        }
        List<UserGroup> groupsAssigned = userGroupService
                .getUserGroupsForUserWithoutDefault(existing.getKey());
        Map<UserGroup, UnitUserRole> groupsToAdd = new HashMap<>();
        List<UserGroup> groupsToRemove = new ArrayList<>();
        for (UserGroup g : groupsAssigned) {
            Long key = Long.valueOf(g.getKey());
            if (groupsToBeAssignedMap.containsKey(key)) {
                groupsToBeAssignedMap.remove(key);
            } else {
                groupsToRemove.add(g);
            }
        }
        for (Entry<Long, Entry<UserGroup, UnitUserRole>> entry : groupsToBeAssignedMap
                .entrySet()) {
            groupsToAdd.put(entry.getValue().getKey(), entry.getValue()
                    .getValue());
        }
        if (!groupsToAdd.isEmpty()) {
            userGroupService.assignUserToGroups(existing, groupsToAdd);
        }
        if (!groupsToRemove.isEmpty()) {
            userGroupService.revokeUserFromGroups(existing, groupsToRemove);
        }
        handleUnitRoleAssignments(user, existing, userGroupsToBeAssigned);
    }

    private void handleUnitRoleAssignments(POUserAndSubscriptions user,
            PlatformUser existing,
            Map<UserGroup, UnitUserRole> userGroupsToBeAssigned)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserModificationConstraintException {
        List<UnitRoleType> allAvailableUnitRoleTypes;
        allAvailableUnitRoleTypes = new ArrayList<>(Arrays.asList(UnitRoleType
                .values()));
        for (Entry<UserGroup, UnitUserRole> groupWithRoles : userGroupsToBeAssigned
                .entrySet()) {
            userGroupService.revokeUserRoles(existing,
                    allAvailableUnitRoleTypes, groupWithRoles.getKey());
            userGroupService.grantUserRoles(existing, Collections
                    .singletonList(groupWithRoles.getValue().getRoleName()),
                    groupWithRoles.getKey());
        }

        Map<UserGroup, UnitRoleType> allUserAssignments = userGroupService
                .getUserGroupsForUserWithRoles(user.getUserId());
        if (allUserAssignments.values().contains(UnitRoleType.ADMINISTRATOR)) {
            isl.grantUnitRole(existing, UserRoleType.UNIT_ADMINISTRATOR);
            if (!user.getAssignedRoles().contains(
                    UserRoleType.UNIT_ADMINISTRATOR)) {
                user.getAssignedRoles().add(UserRoleType.UNIT_ADMINISTRATOR);
            }
        } else {
            isl.revokeUnitRole(existing, UserRoleType.UNIT_ADMINISTRATOR);
            if (user.getAssignedRoles().contains(
                    UserRoleType.UNIT_ADMINISTRATOR)) {
                user.getAssignedRoles().remove(UserRoleType.UNIT_ADMINISTRATOR);
            }
        }
    }

    private Map<UserGroup, UnitUserRole> validateGroupsExist(
            List<POUserGroup> allGroups, List<POUserGroup> groups)
            throws ObjectNotFoundException {
        Map<UserGroup, UnitUserRole> groupsToBeAssigned = new HashMap<>();
        Map<Long, UserGroup> groupsToBeAssignedMap = new HashMap<>();
        for (POUserGroup group : allGroups) {
            UserGroup userGroup;
            try {
                userGroup = ds.getReference(UserGroup.class, group.getKey());
                groupsToBeAssignedMap.put(Long.valueOf(group.getKey()),
                        userGroup);
            } catch (ObjectNotFoundException ex) {
                String groupName = group.getGroupName();
                ex.setMessageParams(new String[] { groupName });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                        LogMessageIdentifier.WARN_GROUP_NOT_EXIST, groupName);
                throw ex;
            }
        }
        if (groups != null && !groups.isEmpty()) {
            for (POUserGroup group : groups) {
                UnitUserRole unitUserRole = userGroupService
                        .getUnitRoleByName(group.getSelectedRole());
                groupsToBeAssigned.put(groupsToBeAssignedMap.remove(Long
                        .valueOf(group.getKey())), unitUserRole);
            }
        }
        return groupsToBeAssigned;
    }

    boolean updateSubscriptionAssignment(POUserAndSubscriptions user,
            List<UsageLicense> assignments) throws SaaSApplicationException {

        Set<String> subsToRemove = getUnassignments(assignments,
                user.getSubscriptions());
        VOUser u = convertToUser(user);
        Map<String, VOUsageLicense> subsToAdd = getAssignments(assignments,
                user.getSubscriptions(), u);

        boolean done = true;

        for (String subId : subsToRemove) {
            done = done
                    && ssl.addRevokeUser(subId, null,
                            Collections.singletonList(u));
        }
        for (String subId : subsToAdd.keySet()) {
            VOUsageLicense lic = subsToAdd.get(subId);
            done = done
                    && ssl.addRevokeUser(subId, Collections.singletonList(lic),
                            null);
        }

        return done;
    }

    @Override
    public List<POSubscription> getUserAssignableSubscriptions(
            Pagination pagination, String userId)
            throws SaaSApplicationException {

        // TODO MULTITENANT
        PlatformUser user = isl.getPlatformUser(userId, ds.getCurrentUser()
                .getTenantId(), true);

        List<POSubscription> subscriptions = slsl
                .getUserAssignableSubscriptions(pagination, user,
                        Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS);

        return subscriptions;
    }

    @Override
    public Long getUserAssignableSubscriptionsNumber(PaginationInt pagination,
            String userId, String tenantId) throws SaaSApplicationException {

        PlatformUser user;
        user = isl.getPlatformUser(userId, tenantId, true);

        Long number = slsl.getUserAssignableSubscriptionsNumber(pagination,
                user, Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS);

        return number;
    }

}
