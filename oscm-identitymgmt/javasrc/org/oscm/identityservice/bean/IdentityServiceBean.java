/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *  Completion Time: 16.03.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.oscm.authorization.PasswordHash;
import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.data.SendMailStatus.SendMailStatusItem;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterEncoder;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.OnBehalfUserReference;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Tenant;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.id.IdGenerator;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.bean.BulkUserImportReader.Row;
import org.oscm.identityservice.control.SendMailControl;
import org.oscm.identityservice.ldap.UserModificationCheck;
import org.oscm.identityservice.local.ILdapResultMapper;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.identityservice.local.LdapAccessServiceLocal;
import org.oscm.identityservice.local.LdapConnector;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.identityservice.local.LdapVOUserDetailsMapper;
import org.oscm.identityservice.pwdgen.PasswordGenerator;
import org.oscm.identityservice.rest.AccessGroup;
import org.oscm.identityservice.rest.AccessToken;
import org.oscm.identityservice.rest.Userinfo;
import org.oscm.interceptor.DateFactory;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.interceptor.ServiceProviderInterceptor;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.BulkUserImportException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.OrganizationRemovedException;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.SecurityCheckException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.UnsupportedOperationException;
import org.oscm.internal.types.exception.UserActiveException;
import org.oscm.internal.types.exception.UserDeletionConstraintException;
import org.oscm.internal.types.exception.UserDeletionConstraintException.Reason;
import org.oscm.internal.types.exception.UserModificationConstraintException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.permission.PermissionCheck;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.taskhandling.operations.ImportUserHandler;
import org.oscm.taskhandling.operations.UpdateUserHandler;
import org.oscm.taskhandling.payloads.ImportUserPayload;
import org.oscm.taskhandling.payloads.UpdateUserPayload;
import org.oscm.triggerservice.bean.TriggerProcessIdentifiers;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.triggerservice.validator.TriggerProcessValidator;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;

/**
 * Session Bean implementation class IdentityServiceBean
 */
@DeclareRoles("ORGANIZATION_ADMIN")
@Stateless
@Remote(IdentityService.class)
@Local(IdentityServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class IdentityServiceBean
        implements IdentityService, IdentityServiceLocal {
 
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(IdentityServiceBean.class);

    private static final Random random = new SecureRandom();

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @EJB(beanInterface = SubscriptionServiceLocal.class)
    SubscriptionServiceLocal sm;

    @EJB(beanInterface = CommunicationServiceLocal.class)
    CommunicationServiceLocal cm;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    ConfigurationServiceLocal cs;

    @EJB(beanInterface = SessionServiceLocal.class)
    private SessionServiceLocal prodSessionMgmt;

    @EJB(beanInterface = LdapAccessServiceLocal.class)
    public LdapAccessServiceLocal ldapAccess;

    @EJB(beanInterface = TaskQueueServiceLocal.class)
    public TaskQueueServiceLocal tqs;

    @Inject
    public ReviewServiceLocalBean rvs;

    @Inject
    UserGroupServiceLocalBean userGroupService;

    @EJB(beanInterface = TriggerQueueServiceLocal.class)
    protected TriggerQueueServiceLocal triggerQS;

    @EJB(beanInterface = LdapSettingsManagementServiceLocal.class)
    protected LdapSettingsManagementServiceLocal ldapSettingsMS;

    @EJB(beanInterface = SessionServiceLocal.class)
    SessionServiceLocal sessionService;
    
    @Resource
    SessionContext sessionCtx;

    public IdentityServiceBean() {

    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public VOUserDetails createUser(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {

        return createUser(user, roles, marketplaceId, null);
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public VOUserDetails createUser(VOUserDetails user, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {

        List<UserRoleType> roles = Collections.emptyList();
        return createUser(user, roles, marketplaceId, null);
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void importUsersInOwnOrganization(byte[] csvData,
            String marketplaceId) throws BulkUserImportException,
            ObjectNotFoundException, IllegalArgumentException {

        ArgumentValidator.notNull("csvData", csvData);

        Organization organization = dm.getCurrentUser().getOrganization();

        importUsers(csvData, organization, marketplaceId);
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void importUsers(byte[] csvData, String organizationId,
            String marketplaceId) throws BulkUserImportException,
            ObjectNotFoundException, IllegalArgumentException {

        ArgumentValidator.notNull("csvData", csvData);
        ArgumentValidator.notNull("organizationId", organizationId);

        Organization organization = getOrganization(organizationId);

        importUsers(csvData, organization, marketplaceId);
    }

    protected void importUsers(byte[] csvData, Organization organization,
            String marketplaceId)
            throws BulkUserImportException, ObjectNotFoundException {
        // preconditions
        BulkUserImportReader.validate(csvData);
        ensureNoRemoteLdapUsed(organization,
                LogMessageIdentifier.ERROR_ADDING_USER_FORBIDDEN_REMOTE_LDAP);
        ensureEmailSet();
        if (marketplaceId != null && marketplaceId.trim().length() != 0) {
            checkIfMarketplaceExists(marketplaceId);
        }

        // payload
        ImportUserPayload payload = new ImportUserPayload();
        payload.setImportingUserKey(Long.valueOf(dm.getCurrentUser().getKey()));
        payload.setOrganizationId(organization.getOrganizationId());
        payload.setMarketplaceId(marketplaceId);

        // read csv data
        BulkUserImportReader reader = null;
        try {
            reader = new BulkUserImportReader(csvData);
            for (Row row : reader) {
                VOUserDetails userDetails = row.getUserDetails();
                List<UserRoleType> roles = row.getRoles();
                payload.addUser(userDetails, roles);
            }
        } finally {
            Streams.close(reader);
        }

        // create async task
        TaskMessage message = new TaskMessage(ImportUserHandler.class, payload);
        tqs.sendAllMessages(Collections.singletonList(message));
    }

    /**
     * The importing user must have an email defined. Otherwise, the import
     * report cannot be sent by mail.
     */
    void ensureEmailSet() throws BulkUserImportException {
        if (Strings.isEmpty(dm.getCurrentUser().getEmail())) {
            throw new BulkUserImportException(
                    BulkUserImportException.Reason.EMAIL_REQUIRED, null);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void importUser(VOUserDetails user, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            ObjectNotFoundException {
        try {
            Marketplace marketplace = getMarketplace(marketplaceId);
            Organization organization = getOrganization(
                    user.getOrganizationId());
            String password = new PasswordGenerator().generatePassword();
            if (organization != null && organization.getTenant() != null) {
                user.setTenantId(organization.getTenant().getTenantId());
            }
            addPlatformUser(user, organization, password,
                    UserAccountStatus.PASSWORD_MUST_BE_CHANGED, true, true,
                    marketplace, true);
        } catch (NonUniqueBusinessKeyException | MailOperationException
                | ValidationException | UserRoleAssignmentException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    /**
     * Bug 9324 Trigger should not be fired if user exists
     */
    private void checkIfUserExists(String userId, Tenant tenant)
            throws NonUniqueBusinessKeyException {
        PlatformUser dbUser = loadUser(userId, tenant);
        if (dbUser != null) {
            throw new NonUniqueBusinessKeyException(ClassEnum.USER, userId);
        }
    }

    /**
     * Loads a user from database.
     * 
     * @param userId
     *            user id
     * @return the user otherwise null if the user does not exist.
     */
    PlatformUser loadUser(String userId, Tenant tenant) {
        try {
            // if (tenant == null) {
            PlatformUser user = new PlatformUser();
            user.setUserId(userId);
            if (tenant != null) {
                user.setTenantId(tenant.getTenantId());
            }
            return (PlatformUser) dm.getReferenceByBusinessKey(user);
            // }
            // Query q =
            // dm.createNamedQuery("PlatformUser.findByUserIdAndTenant");
            // q.setParameter("userId", userId);
            // q.setParameter("tenantId", tenant.getTenantId());
            // return (PlatformUser) q.getSingleResult();
        } catch (ObjectNotFoundException | NoResultException e) {
            return null;
        }
    }

    @Override
    public VOUserDetails createUserInt(TriggerProcess tp)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException {

        VOUserDetails user = tp
                .getParamValueForName(TriggerProcessParameterName.USER)
                .getValue(VOUserDetails.class);
        List<UserRoleType> roles = ParameterizedTypes.list(tp
                .getParamValueForName(
                        TriggerProcessParameterName.USER_ROLE_TYPE)
                .getValue(List.class), UserRoleType.class);
        String marketplaceId = tp
                .getParamValueForName(
                        TriggerProcessParameterName.MARKETPLACE_ID)
                .getValue(String.class);
        Map<Long, UnitUserRole> userGroupKeyToRole = ParameterizedTypes
                .hashmap(
                        tp.getParamValueForName(
                                TriggerProcessParameterName.USER_GROUPS_WITH_ROLES)
                                .getValue(Map.class),
                        Long.class, UnitUserRole.class);

        // generate a one-time password for the user
        VOUserDetails result;
        Organization organization = dm.getCurrentUser().getOrganization();
        try {
            ensureNoRemoteLdapUsed(organization,
                    LogMessageIdentifier.ERROR_ADDING_USER_FORBIDDEN_REMOTE_LDAP);
            Marketplace marketplace = getMarketplace(marketplaceId);
            PasswordGenerator gen = new PasswordGenerator();
            String password = gen.generatePassword();

            // set the user roles also into the VO
            // because addPlatformUser() checks them
            user.setUserRoles(new HashSet<>(roles));
            PlatformUser addedUser = addPlatformUser(user, organization,
                    password, UserAccountStatus.PASSWORD_MUST_BE_CHANGED, true,
                    true, marketplace, true);
            if (userGroupKeyToRole != null && !userGroupKeyToRole.isEmpty()) {
                Map<UserGroup, UnitUserRole> userToGroup = getUserGroupToRoleMap(
                        userGroupKeyToRole);
                userGroupService.assignUserToGroups(addedUser, userToGroup);
                List<UnitRoleType> unitRoleTypes = new ArrayList<>();
                for (Entry<Long, UnitUserRole> groupWithRole : userGroupKeyToRole
                        .entrySet()) {
                    unitRoleTypes.add(groupWithRole.getValue().getRoleName());
                }
                for (Entry<UserGroup, UnitUserRole> groupWithRole : userToGroup
                        .entrySet()) {
                    userGroupService.grantUserRoles(addedUser,
                            Arrays.asList(
                                    groupWithRole.getValue().getRoleName()),
                            groupWithRole.getKey());
                }
                if (unitRoleTypes.contains(UnitRoleType.ADMINISTRATOR)) {
                    grantRole(addedUser, UserRoleType.UNIT_ADMINISTRATOR);
                }
            }
            result = UserDataAssembler.toVOUserDetails(addedUser);
        } catch (NonUniqueBusinessKeyException | MailOperationException
                | ValidationException | UserRoleAssignmentException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ObjectNotFoundException | OperationNotPermittedException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR);
            throw new ValidationException(e.getMessage());
        }

        triggerQS.sendAllNonSuspendingMessages(TriggerMessage.create(
                TriggerType.REGISTER_OWN_USER, tp.getTriggerProcessParameters(),
                dm.getCurrentUser().getOrganization()));

        return result;
    }

    private Map<UserGroup, UnitUserRole> getUserGroupToRoleMap(
            Map<Long, UnitUserRole> userGroupKeyToRole) {
        Map<UserGroup, UnitUserRole> userGroupToRole = new HashMap<>();
        if (userGroupKeyToRole != null) {
            for (Map.Entry<Long, UnitUserRole> e : userGroupKeyToRole
                    .entrySet()) {
                try {
                    userGroupToRole.put(dm.getReference(UserGroup.class,
                            e.getKey().longValue()), e.getValue());
                } catch (ObjectNotFoundException ignored) {
                }
            }
        }
        return userGroupToRole;
    }

    @Override
    /*@Interceptors({ PlatformOperatorServiceProviderInterceptor.class })*/
    public void changePassword(String oldPassword, String newPassword)
            throws SecurityCheckException, ValidationException {
        
        ArgumentValidator.notNull("oldPassword", oldPassword);
        ArgumentValidator.notNull("newPassword", newPassword);
        BLValidator.isPassword("newPassword", newPassword);
        PlatformUser pUser = dm.getCurrentUser();
        ensureNoRemoteLdapUsed(pUser.getOrganization(),
                LogMessageIdentifier.ERROR_PASSWORD_OPERATION_FORBIDDEN_REMOTE_LDAP);

        // two steps have to be performed:
        // 1. validate old password
        if (!PasswordHash.verifyPassword(pUser.getPasswordSalt(),
                pUser.getPasswordHash(), oldPassword)) {
            // So the authentication failed. Log the problem, increase the
            // user's failed login counter and throw the exception and throw an
            // exception.
            SecurityCheckException scf = new SecurityCheckException(
                    "Authentication failed, a wrong password has been specified.");
            logger.logWarn(Log4jLogger.SYSTEM_LOG, scf,
                    LogMessageIdentifier.WARN_CHANGE_PASSWORD_FAILED);
            int count = pUser.getFailedLoginCounter();
            count++;
            pUser.setFailedLoginCounter(count);
            if (count >= getMaxRetryAttempts()) {
                pUser.setStatus(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS);
            }
            scf.setMessageKey("error.changePassword");
            throw scf;
        }

        // 2. change password to new one
        setPassword(pUser, newPassword);

        // 3. if the user account status is indicating that the password must be
        // changed, revert this setting to active
        if (pUser.getStatus() == UserAccountStatus.PASSWORD_MUST_BE_CHANGED) {
            pUser.setStatus(UserAccountStatus.ACTIVE);
        }
    }

    @Override
    public void confirmAccount(VOUser user, String marketplaceId)
            throws OperationNotPermittedException, ObjectNotFoundException,
            MailOperationException {
        // user is not logged in, so don't log user and organization key
        // information

        ArgumentValidator.notNull("user", user);

        // confirmation means releasing the lock of type
        // UserAccountStatus.LOCKED_NOT_CONFIRMED
        PlatformUser pUser = dm.getReference(PlatformUser.class, user.getKey());
        UserAccountStatus currentStatus = pUser.getStatus();
        if (currentStatus == UserAccountStatus.LOCKED_NOT_CONFIRMED) {
            pUser.setStatus(UserAccountStatus.ACTIVE);

            // send acknowledge e-mail
            try {
                if (pUser.hasManagerRole()) {
                    cm.sendMail(pUser, EmailType.USER_CONFIRM_ACKNOWLEDGE,
                            new Object[] { cm.getBaseUrl(), pUser.getUserId(),
                                    String.valueOf(pUser.getKey()) },
                            getMarketplace(marketplaceId));
                } else {
                    cm.sendMail(pUser, EmailType.USER_CONFIRM_ACKNOWLEDGE,
                            new Object[] { cm.getMarketplaceUrl(marketplaceId),
                                    pUser.getUserId(),
                                    String.valueOf(pUser.getKey()) },
                            getMarketplace(marketplaceId));
                }
            } catch (MailOperationException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
        }
        // do nothing if account is already active, but otherwise throw an
        // exception
        else if (currentStatus != UserAccountStatus.ACTIVE) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "The account of user '" + user.getUserId()
                            + "' cannot be confirmed.");
            logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                    LogMessageIdentifier.WARN_CONFIRM_OPERATOR_ACCOUNT_FAILED,
                    user.getUserId());
            throw onp;
        }

    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void grantUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException {

        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("roles", roles);
        PlatformUser pUser = getPlatformUser(user.getUserId(),
                dm.getCurrentUser().getTenantId(), true);
        grantUserRoles(pUser, roles);

    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public void grantUnitRole(VOUser user, UserRoleType role)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("role", role);
        PlatformUser pUser = getPlatformUser(user.getUserId(),
                dm.getCurrentUser().getTenantId(), true);
        grantUnitRole(pUser, role);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public void revokeUnitRole(VOUser user, UserRoleType role)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("role", role);
        PlatformUser pUser = getPlatformUser(user.getUserId(),
                dm.getCurrentUser().getTenantId(), true);
        revokeUnitRole(pUser, role);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void grantUserRoles(PlatformUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException {
        for (UserRoleType role : roles) {
            createUserRoleRelationInt(user, role);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void resetPasswordForUser(PlatformUser user, Marketplace marketplace)
            throws MailOperationException {

        ensureNoRemoteLdapUsed(user.getOrganization(),
                LogMessageIdentifier.ERROR_PASSWORD_OPERATION_FORBIDDEN_REMOTE_LDAP);

        final String newPwd = new PasswordGenerator().generatePassword();
        setPassword(user, newPwd);
        // send mail to the affected user
        try {
            cm.sendMail(user, EmailType.USER_PASSWORD_RESET,
                    new Object[] { newPwd }, marketplace);
        } catch (MailOperationException e) {
            // Mail reception is essential, otherwise the user cannot work with
            // his account
            MailOperationException mof = new MailOperationException(
                    "Mail with your new password cannot be sent. Operation aborted!",
                    e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, mof,
                    LogMessageIdentifier.WARN_OPERATOR_RESET_PASSWORD_FAILED,
                    dm.getCurrentUser().getUserId(), user.getUserId(),
                    e.getId());
            sessionCtx.setRollbackOnly();
            throw mof;
        }

        // unlock the account, if not locked by a platform operator
        if (user.getStatus().getLockLevel() <= UserAccountStatus.LOCKED
                .getLockLevel()) {
            user.setStatus(UserAccountStatus.PASSWORD_MUST_BE_CHANGED);
            user.setFailedLoginCounter(0);
        }

    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    @Interceptors({ ServiceProviderInterceptor.class })
    public void requestResetOfUserPassword(VOUser user, String marketplaceId)
            throws MailOperationException, ObjectNotFoundException,
            OperationNotPermittedException, UserActiveException,
            ConcurrentModificationException {

        ArgumentValidator.notNull("user", user);
        PlatformUser platformUser = getPlatformUser(user.getUserId(),
                user.getTenantId(), true);
        BaseAssembler.verifyVersionAndKey(platformUser, user);

        resetUserPassword(platformUser, marketplaceId);

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void resetUserPassword(PlatformUser platformUser,
            String marketplaceId)
            throws UserActiveException, MailOperationException {
        // determine if the user has an active session, if so, throw an
        // exception
        List<Session> sessionsForUserKey = prodSessionMgmt
                .getSessionsForUserKey(platformUser.getKey());
        if (sessionsForUserKey.size() > 0) {
            UserActiveException uae = new UserActiveException(
                    "Reset of password for user '" + platformUser.getKey()
                            + "' failed, as the user is still active",
                    new Object[] { platformUser.getUserId() });
            logger.logWarn(Log4jLogger.SYSTEM_LOG, uae,
                    LogMessageIdentifier.WARN_OPERATOR_RESET_PASSWORD_FAILED);
            throw uae;
        }

        // reset the password
        resetPasswordForUser(platformUser, getMarketplace(marketplaceId));
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void deleteUser(VOUser user, String marketplaceId)
            throws UserDeletionConstraintException, ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        ArgumentValidator.notNull("user", user);
        PlatformUser pUser = dm.getReference(PlatformUser.class, user.getKey());
        BaseAssembler.verifyVersionAndKey(pUser, user);

        deleteUser(pUser, marketplaceId);

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deleteUser(PlatformUser pUser, String marketplaceId)
            throws OperationNotPermittedException,
            UserDeletionConstraintException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // Check whether user belongs to same organization than the caller
        PermissionCheck.sameOrg(dm.getCurrentUser(), pUser, logger);
        deletePlatformUser(pUser, false, false, getMarketplace(marketplaceId));
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void revokeUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, UserModificationConstraintException,
            UserActiveException, OperationNotPermittedException,
            UserRoleAssignmentException {

        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("roles", roles);

        PlatformUser pUser = dm.getReference(PlatformUser.class, user.getKey());
        revokeUserRolesInt(pUser, roles);

    }

    private void revokeUserRolesInt(PlatformUser user, List<UserRoleType> roles)
            throws UserModificationConstraintException, UserActiveException,
            OperationNotPermittedException, UserRoleAssignmentException {
        if (roles.isEmpty()) {
            return;
        }

        // Bug #7223: Determine if the specified user is not the user calling
        // this method and has an active session. If so, throw an exception,
        // because updating the roles of another user is not possible
        if (dm.getCurrentUser().getKey() != user.getKey()) {
            List<Session> sessionsForUserKey = prodSessionMgmt
                    .getSessionsForUserKey(user.getKey());
            if (sessionsForUserKey.size() > 0) {
                UserActiveException uae = new UserActiveException(
                        "Revoking of roles for user '" + user.getKey()
                                + "' failed, as the user is still active",
                        new Object[] { user.getUserId() });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, uae,
                        LogMessageIdentifier.WARN_REVOKE_USER_ROLE_FAILED,
                        user.getUserId());
                throw uae;
            }
        }

        // check whether user belongs to same organization than the caller
        PermissionCheck.sameOrg(dm.getCurrentUser(), user, logger);

        for (UserRoleType role : roles) {
            // if the user is revoked the organization admin role, the
            // corresponding domain object has to be updated
            checkRoleConstrains(user, role);
            revokeRole(user, role);
        }
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public VOUserDetails getUserDetails(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("user", user);
        PlatformUser pUser = getPlatformUser(user.getUserId(),
                dm.getCurrentUser().getTenantId(), true);

        return UserDataAssembler.toVOUserDetails(pUser);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public List<VOUserDetails> getUsersForOrganization() {
        List<PlatformUser> organizationUsers = getOrganizationUsers();
        List<VOUserDetails> resultList = new ArrayList<>();
        for (PlatformUser currentUser : organizationUsers) {
            resultList.add(UserDataAssembler.toVOUserDetails(currentUser));
        }
        return resultList;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<PlatformUser> getOrganizationUsers() {

        // The organization is determined by the currently logged in user. To
        // obtain the users for this organization, the organization domain
        // object has to be loaded, which then references the users.
        PlatformUser user = dm.getCurrentUser();

        // 1. determine the correlating organization
        Organization organization = user.getOrganization();

        Query q = dm.createNamedQuery("PlatformUser.getVisibleForOrganization");
        q.setParameter("organization", organization);

        return ParameterizedTypes.list(q.getResultList(), PlatformUser.class);
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void lockUserAccount(VOUser user, UserAccountStatus newStatus,
            String marketplaceId) throws OperationNotPermittedException,
            ObjectNotFoundException, ConcurrentModificationException {

        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("newStatus", newStatus);

        // get the corresponding platform user
        PlatformUser userObj = dm.getReference(PlatformUser.class,
                user.getKey());

        // check whether user belongs to same organization than the caller
        PermissionCheck.sameOrg(dm.getCurrentUser(), userObj, logger);

        ensureNoRemoteLdapUsed(userObj.getOrganization(),
                LogMessageIdentifier.ERROR_LOCK_OPERATION_FORBIDDEN_REMOTE_LDAP);

        BaseAssembler.verifyVersionAndKey(userObj, user);

        // ensure that the given lock type is really a locking type
        if (newStatus == UserAccountStatus.ACTIVE
                || newStatus == UserAccountStatus.PASSWORD_MUST_BE_CHANGED) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "To lock an account a locking state must be set!");
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.WARN_OPERATOR_LOCK_FAILED,
                    dm.getCurrentUser().getUserId(), userObj.getUserId());
            throw onp;
        }
        if (newStatus == UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS
                || newStatus == UserAccountStatus.LOCKED_NOT_CONFIRMED) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "A user must not set those locking states!");
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.WARN_OPERATOR_LOCK_FAILED,
                    dm.getCurrentUser().getUserId(), userObj.getUserId());
            throw onp;
        }
        if (user.getStatus().getLockLevel() > newStatus.getLockLevel()) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "The lock level must not be reduced, current lock level is higher than the one specified!");
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.WARN_OPERATOR_LOCK_FAILED,
                    dm.getCurrentUser().getUserId(), userObj.getUserId());
            throw onp;
        }
        setUserAccountStatusInternal(userObj, newStatus,
                getMarketplace(marketplaceId));

    }

    @Override
    public VOUserDetails updateUser(VOUserDetails user)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ValidationException, NonUniqueBusinessKeyException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            ConcurrentModificationException {

        ArgumentValidator.notNull("user", user);

        // 1. obtain the user's domain object.
        PlatformUser existingUser = dm.getReference(PlatformUser.class,
                user.getKey());

        final PlatformUser updatedUser;
        try {
            updatedUser = modifyUserData(existingUser, user, false, true);
        } catch (NonUniqueBusinessKeyException
                | TechnicalServiceNotAliveException
                | TechnicalServiceOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        // enforce version update
        dm.flush();

        return UserDataAssembler.toVOUserDetails(updatedUser);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public PlatformUser modifyUserData(PlatformUser existingUser,
            VOUserDetails updatedUser, boolean modifyOwnUser, boolean sendMail)
            throws OperationNotPermittedException,
            NonUniqueBusinessKeyException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ValidationException,
            ConcurrentModificationException {

        // check whether user belongs to same organization than the caller
        PermissionCheck.sameOrg(dm.getCurrentUser(), existingUser, logger);

        PlatformUser oldUser = existingUser.getEmail() != null
                ? UserDataAssembler.copyPlatformUser(existingUser) : null;

        // validate permissions for the call, administrator may change any user,
        // a non administrator may only change his own account
        if (!dm.getCurrentUser().isOrganizationAdmin() || modifyOwnUser) {
            if (!String.valueOf(updatedUser.getKey())
                    .equals(sessionCtx.getCallerPrincipal().getName())) {
                OperationNotPermittedException onp = new OperationNotPermittedException(
                        "User is not permitted to modify the specified user account.");
                logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        onp,
                        LogMessageIdentifier.WARN_USER_ACCOUNT_MODIFICATION_FAILED,
                        Long.toString(dm.getCurrentUser().getKey()),
                        updatedUser.getUserId());
                throw onp;
            }
        }

        PlatformUser modUser = UserDataAssembler.toPlatformUser(updatedUser);
        modUser.setKey(existingUser.getKey());
        verifyIdUniquenessAndLdapAttributes(existingUser, modUser);

        // now change the user
        UserDataAssembler.updatePlatformUser(updatedUser, existingUser);

        if (sendMail) {
            sendUserUpdatedMail(existingUser, oldUser);
        }

        notifySubscriptionsAboutUserUpdate(existingUser);

        return existingUser;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void verifyIdUniquenessAndLdapAttributes(PlatformUser existingUser,
            PlatformUser modUser) throws NonUniqueBusinessKeyException {

        if (!existingUser.getUserId().equals(modUser.getUserId())) {
            final PlatformUser tmpUser = new PlatformUser();
            tmpUser.setKey(modUser.getKey());
            tmpUser.setUserId(modUser.getUserId());
            dm.validateBusinessKeyUniqueness(tmpUser);
        }

        // before performing any changes to the platform user, ensure that none
        // of the LDAP managed attributes will be modified
        UserModificationCheck umc = new UserModificationCheck(
                ldapSettingsMS.getMappedAttributes());
        umc.check(existingUser, modUser);

    }

    @Override
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void notifySubscriptionsAboutUserUpdate(PlatformUser existingUser) {

        // 2. notify all products the user is subscribed to
        List<Subscription> subscriptions = sm
                .getSubscriptionsForUserInt(existingUser);
        List<TaskMessage> messages = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            SubscriptionStatus status = subscription.getStatus();
            // in these states the product instance is not existing
            if (status != SubscriptionStatus.PENDING
                    && status != SubscriptionStatus.INVALID) {
                UsageLicense license = getUsgeLicenseForUserAndSubscription(
                        existingUser, subscription);
                if (license != null) {
                    UpdateUserPayload payload = new UpdateUserPayload(
                            subscription.getKey(), license.getKey());
                    TaskMessage message = new TaskMessage(
                            UpdateUserHandler.class, payload);
                    messages.add(message);
                }
            }
        }
        tqs.sendAllMessages(messages);

    }

    @Override
    @Asynchronous
    public void sendUserUpdatedMail(PlatformUser existingUser,
            PlatformUser oldUser) {

        // bugfix 8183
        // Send an email to the current organization admin
        List<PlatformUser> platformUsers = new LinkedList<>();
        platformUsers.add(existingUser);

        if (oldUser != null && !oldUser.getEmail().trim()
                .equals(existingUser.getEmail().trim())) {
            platformUsers.add(oldUser);
        }

        SendMailStatus<PlatformUser> mailStatus = cm.sendMail(
                EmailType.USER_UPDATED, null, null,
                platformUsers.toArray(new PlatformUser[platformUsers.size()]));

        for (SendMailStatusItem<PlatformUser> sendMailStatusItem : mailStatus
                .getMailStatus()) {
            if (sendMailStatusItem.errorOccurred()) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        sendMailStatusItem.getException(),
                        LogMessageIdentifier.WARN_MAIL_USER_UPDATE_FAILED);
            }
        }

    }

    @Override
    public void sendAdministratorNotifyMail(PlatformUser administrator,
            String userId) {
        List<PlatformUser> platformUsers = new LinkedList<>();
        platformUsers.add(administrator);

        SendMailStatus<PlatformUser> mailStatus = cm.sendMail(
                EmailType.USER_UPDATED_WITH_NOROLE, new Object[] { userId },
                null,
                platformUsers.toArray(new PlatformUser[platformUsers.size()]));

        for (SendMailStatusItem<PlatformUser> sendMailStatusItem : mailStatus
                .getMailStatus()) {
            if (sendMailStatusItem.errorOccurred()) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        sendMailStatusItem.getException(),
                        LogMessageIdentifier.WARN_MAIL_USER_UPDATE_FAILED);
            }
        }
    }

    @Override
    public void notifyOnLoginAttempt(VOUser user, boolean attemptSuccessful)
            throws ObjectNotFoundException, SecurityCheckException,
            ValidationException {

        ArgumentValidator.notNull("user", user);

        PlatformUser pUser = dm.getReference(PlatformUser.class, user.getKey());
        if (attemptSuccessful) {
            if (pUser.getStatus()
                    .getLockLevel() >= UserAccountStatus.LOCK_LEVEL_LOCKED) {
                SecurityCheckException scf = new SecurityCheckException(
                        "User must not login, as he failed to do so before too many times. Account locked!");
                logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        scf, LogMessageIdentifier.WARN_USER_LOCKED,
                        pUser.getUserId());
                throw scf;
            }
            pUser.setFailedLoginCounter(0);
            if (pUser.getOrganization().isRemoteLdapActive()) {
                syncUserWithLdap(pUser);
            }
        } else {
            int failedAttempts = pUser.getFailedLoginCounter() + 1;
            pUser.setFailedLoginCounter(failedAttempts);
            if (failedAttempts >= getMaxRetryAttempts()) {
                if (pUser.getStatus()
                        .getLockLevel() <= UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS
                                .getLockLevel()) {
                    pUser.setStatus(
                            UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS);
                }
            }
        }
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void unlockUserAccount(VOUser user, String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException {

        ArgumentValidator.notNull("user", user);

        // check if user may unlock the account
        PlatformUser pUser = dm.getReference(PlatformUser.class, user.getKey());

        // check whether user belongs to same organization than the caller
        PermissionCheck.sameOrg(dm.getCurrentUser(), pUser, logger);

        ensureNoRemoteLdapUsed(pUser.getOrganization(),
                LogMessageIdentifier.ERROR_LOCK_OPERATION_FORBIDDEN_REMOTE_LDAP);

        BaseAssembler.verifyVersionAndKey(pUser, user);

        if (pUser.getStatus() == UserAccountStatus.PASSWORD_MUST_BE_CHANGED) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "User must change his password at login, unlock not permitted");
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.WARN_OPERATOR_UNLOCK_FAILED,
                    pUser.getUserId());
            throw onp;
        }

        setUserAccountStatusInternal(pUser, UserAccountStatus.ACTIVE,
                getMarketplace(marketplaceId));

    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void setUserAccountStatus(PlatformUser user,
            UserAccountStatus status) throws OrganizationAuthoritiesException {

        // no marketplace passed as this method is only called by the operator
        setUserAccountStatusInternal(user, status, null);

    }

    private void setUserAccountStatusInternal(PlatformUser user,
            UserAccountStatus status, Marketplace marketplace) {

        // update the domain object
        user.setStatus(status);
        if (UserAccountStatus.ACTIVE == status) {
            user.setFailedLoginCounter(0);
        }

        // send e-mail
        try {
            if (UserAccountStatus.ACTIVE == status) {
                cm.sendMail(user, EmailType.USER_UNLOCKED, null, marketplace);
            } else if (UserAccountStatus.LOCKED == status) {
                cm.sendMail(user, EmailType.USER_LOCKED, null, marketplace);
            }
        } catch (MailOperationException e) {
            // do nothing. although the operation failed, the locking of the
            // account must succeed
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_SEND_CONFIRMATION_MAIL_FAILED,
                    e.getId());
        }
    }

    @Override
    public VOUser getUser(VOUser user) throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationRemovedException {

        ArgumentValidator.notNull("user", user);
        PlatformUser pUser;
        try {
            String userId = user.getUserId();
            String orgId = user.getOrganizationId();
            String tenantId = user.getTenantId();

            if (StringUtils.isNotBlank(orgId)) {
                pUser = getPlatformUserByOrganization(userId, orgId);
            } else {
                pUser = getPlatformUser(userId, tenantId, false);
            }

            if (pUser.getOrganization().getDeregistrationDate() != null) {
                OperationNotPermittedException onp = new OperationNotPermittedException(
                        "The user doesn't belong to a active organization.");
                logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.ACCESS_LOG,
                        onp, LogMessageIdentifier.WARN_OPERATOR_LOGIN_FAILED,
                        pUser.getUserId());
                throw onp;
            }
        } catch (ObjectNotFoundException e) {
            // user could not be found. so check the history entries, if the
            // organization had been removed because the account was not
            // confirmed.
            // If this holds, throw a OrganizationRemovedException, otherwise
            // pass the ObjectNotFound
            Query query = dm.createNamedQuery(
                    "PlatformUserHistory.findUnconfirmedUserForRemovedOrganization");
            query.setParameter("userStatus",
                    UserAccountStatus.LOCKED_NOT_CONFIRMED);
            query.setParameter("modType", ModificationType.DELETE);
            query.setParameter("userId", user.getUserId());
            query.setParameter("organizationId", user.getOrganizationId());
            Object singleResult = null;
            try {
                singleResult = query.getSingleResult();
            } catch (NoResultException nre) {
                // no entry found, so entry never existed. Do nothing to throw
                // the initial ObjectNotFound exception later
            } catch (NonUniqueResultException nue) {
                // should never be caught, indicates inconsistency of data.
                // Throw the object not found exception, but log the problem
                logger.logError(Log4jLogger.SYSTEM_LOG, nue,
                        LogMessageIdentifier.ERROR_NONUNIQUE_ENTRY_FOR_REMOVED_ORGANIZATION);
            }

            if (singleResult != null) {
                // the organization has been removed by the system
                OrganizationRemovedException cre = new OrganizationRemovedException(
                        "Trying to get a user for a organization that was removed by the system",
                        new Object[] {}, e);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, cre,
                        LogMessageIdentifier.WARN_USER_REMOVED_ORGANIZATION_FAILED);
                throw cre;
            }

            throw e;

        }

        return UserDataAssembler.toVOUser(pUser);
    }

    @Override
    public VOUserDetails getCurrentUserDetails() {
        return UserDataAssembler.toVOUserDetails(dm.getCurrentUser());
    }

    @Override
    public VOUserDetails getCurrentUserDetailsIfPresent() {
        return UserDataAssembler.toVOUserDetails(dm.getCurrentUserIfPresent());
    }

    @Override
    public void sendAccounts(String email, String marketplaceId)
            throws ValidationException, MailOperationException {

        ArgumentValidator.notNull("email", email);
        BLValidator.isNotBlank("email", email);

        final List<PlatformUser> users = getUsersByEmail(email);
        if (users != null && !users.isEmpty()) {

            String baseUrl = cs.getBaseURL();

            StringBuffer urlBuffer = new StringBuffer();
            urlBuffer.append(baseUrl);
            // remove any trailing slashes from the base url
            removeTrailingSlashes(urlBuffer);
            urlBuffer.append("?oId=");

            String url = urlBuffer.toString();

            StringBuffer text = new StringBuffer();
            for (PlatformUser currentUser : users) {
                String organizationId = currentUser.getOrganization()
                        .getOrganizationId();
                text.append("  ").append(organizationId).append(", ");
                text.append(currentUser.getUserId()).append(": ");
                text.append(url);
                try {
                    text.append(URLEncoder.encode(organizationId, "UTF-8"));
                    if (marketplaceId != null
                            && marketplaceId.trim().length() > 0) {
                        text.append("&mId=");
                        text.append(URLEncoder.encode(marketplaceId, "UTF-8"));
                    }
                    text.append("\n");
                } catch (UnsupportedEncodingException e) {
                    logger.logError(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.ERROR_ENCODE_URL_FAILED,
                            currentUser.getOrganization().getOrganizationId());
                }
            }
            cm.sendMail(users.get(0), EmailType.USER_ACCOUNTS,
                    new Object[] { text.toString() },
                    getMarketplace(marketplaceId));
        }

    }

    @Override
    @Interceptors({ ServiceProviderInterceptor.class })
    public List<VOUserDetails> searchLdapUsers(final String userIdPattern)
            throws ValidationException {

        ArgumentValidator.notNull("userIdPattern", userIdPattern);

        Organization organization = dm.getCurrentUser().getOrganization();

        LdapConnector connector = getLdapConnectionForOrganization(
                organization);
        Properties dirProperties = connector.getDirProperties();
        Map<SettingType, String> attrMap = connector.getAttrMap();
        String baseDN = connector.getBaseDN();

        List<SettingType> attrList = new ArrayList<>(attrMap.keySet());
        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, attrMap);
        try {
            // read user from LDAP
            List<VOUserDetails> voUserList = ldapAccess.search(dirProperties,
                    baseDN, getLdapSearchFilter(attrMap, userIdPattern), mapper,
                    false);

            int size = voUserList.size();
            for (int i = 0; i < size; i++) {
                VOUserDetails voUser = voUserList.get(i);
                PlatformUser user = getPlatformUserByOrgAndReamUserId(
                        organization, voUser.getRealmUserId());
                if (null != user) {
                    // update the domain object with possibly changed LDAP
                    // attributes and return a complete value object
                    UserDataAssembler.updatePlatformUser(voUser, attrList,
                            user);
                    voUserList.set(i, UserDataAssembler.toVOUserDetails(user));
                } else {
                    // set some mandatory attributes
                    voUser.setOrganizationId(organization.getOrganizationId());
                    String locale = voUser.getLocale();
                    if (locale == null || locale.trim().length() == 0) {
                        voUser.setLocale(organization.getLocale());
                    }
                }
            }
            return voUserList;
        } catch (NamingException e) {
            Object[] params = new Object[] {
                    dirProperties.get(Context.PROVIDER_URL), e.getMessage() };
            ValidationException vf = new ValidationException(
                    ReasonEnum.LDAP_CONNECTION_REFUSED, null, params);
            logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                    LogMessageIdentifier.ERROR_LDAP_SYSTEM_CONNECTION_REFUSED);
            throw vf;
        }
    }

    @Override
    @Interceptors({ ServiceProviderInterceptor.class })
    public boolean searchLdapUsersOverLimit(final String userIdPattern)
            throws ValidationException {
        ArgumentValidator.notNull("userIdPattern", userIdPattern);

        Organization organization = dm.getCurrentUser().getOrganization();

        LdapConnector connector = getLdapConnectionForOrganization(
                organization);
        Properties dirProperties = connector.getDirProperties();
        Map<SettingType, String> attrMap = connector.getAttrMap();
        String baseDN = connector.getBaseDN();
        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, attrMap);
        try {
            return ldapAccess.searchOverLimit(dirProperties, baseDN,
                    getLdapSearchFilter(attrMap, userIdPattern), mapper, false);
        } catch (NamingException e) {
            Object[] params = new Object[] {
                    dirProperties.get(Context.PROVIDER_URL), e.getMessage() };
            ValidationException vf = new ValidationException(
                    ReasonEnum.LDAP_CONNECTION_REFUSED, null, params);
            logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                    LogMessageIdentifier.ERROR_LDAP_SYSTEM_CONNECTION_REFUSED);
            throw vf;
        }
    }

    private PlatformUser getPlatformUserByOrgAndReamUserId(Organization org,
            String realmUserId) {
        Query query = dm
                .createNamedQuery("PlatformUser.findByOrgAndReamUserId");
        query.setParameter("organization", org);
        query.setParameter("realmUserId", realmUserId);
        try {
            return (PlatformUser) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Interceptors({ ServiceProviderInterceptor.class })
    public void importLdapUsers(List<VOUserDetails> users, String marketplaceId)
            throws NonUniqueBusinessKeyException, ValidationException,
            MailOperationException {

        ArgumentValidator.notNull("users", users);

        Organization organization = dm.getCurrentUser().getOrganization();

        LdapConnector connector = getLdapConnectionForOrganization(
                organization);
        Properties dirProperties = connector.getDirProperties();
        Map<SettingType, String> attrMap = connector.getAttrMap();
        String baseDN = connector.getBaseDN();

        Marketplace marketplace = getMarketplace(marketplaceId);
        for (VOUserDetails user : users) {
            try {
                ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                        user, attrMap);
                List<VOUserDetails> list = ldapAccess.search(dirProperties,
                        baseDN,
                        getLdapSearchFilter(attrMap, user.getRealmUserId()),
                        mapper, false);
                int size = list.size();
                if (size == 1) {
                    user = list.get(0);
                    if (GenericValidator.isBlankOrNull(user.getLocale())) {
                        user.setLocale(organization.getLocale());
                    }
                    try {
                        addPlatformUser(user, organization, null,
                                UserAccountStatus.ACTIVE, true, false,
                                marketplace, false);
                    } catch (UserRoleAssignmentException e) {
                        sessionCtx.setRollbackOnly();
                        ValidationException vf = new ValidationException(
                                e.getMessage());
                        logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                                LogMessageIdentifier.ERROR_VALIDATION_PARAMETER_LDAP_FOUND_ERROR,
                                "User");
                        throw vf;
                    }

                } else if (size == 0) {
                    sessionCtx.setRollbackOnly();
                    ValidationException vf = new ValidationException(
                            ReasonEnum.LDAP_USER_NOT_FOUND, null,
                            new Object[] { user.getRealmUserId() });
                    logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                            LogMessageIdentifier.ERROR_VALIDATION_PARAMETER_LDAP_FOUND_ERROR,
                            "User");
                    throw vf;
                } else {
                    sessionCtx.setRollbackOnly();
                    ValidationException vf = new ValidationException(
                            ReasonEnum.LDAP_USER_NOT_UNIQUE, null,
                            new Object[] { user.getRealmUserId() });
                    logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                            LogMessageIdentifier.ERROR_VALIDATION_PARAMETER_LDAP_FOUND_ERROR,
                            "User");
                    throw vf;
                }
            } catch (NamingException e) {
                Object[] params = new Object[] {
                        dirProperties.get(Context.PROVIDER_URL),
                        e.getMessage() };
                ValidationException vf = new ValidationException(
                        ReasonEnum.LDAP_CONNECTION_REFUSED, null, params);
                logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                        LogMessageIdentifier.ERROR_LDAP_SYSTEM_CONNECTION_REFUSED);
                throw vf;
            }
        }
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "TECHNOLOGY_MANAGER" })
    public VOUserDetails createOnBehalfUser(String organizationId,
            String password) throws ObjectNotFoundException,
            OperationNotPermittedException, NonUniqueBusinessKeyException {

        PlatformUser currentUser = dm.getCurrentUser();

        Organization customer = validateForOnBehalfUserCreation(organizationId,
                password, currentUser);
        ensureNoRemoteLdapUsed(customer,
                LogMessageIdentifier.ERROR_ON_BEHALF_USER_OPERATION_REMOTE_LDAP);
        PlatformUser customerUser = createOnBehalfUser(password, currentUser,
                customer);
        dm.refresh(customerUser);
        grantMaxUserRoles(customerUser);
        dm.flush();
        dm.refresh(customerUser);

        return UserDataAssembler.toVOUserDetails(customerUser);
    }

    /**
     * Grants the max set of roles to the user as allowed by his organization.
     * 
     * @param customerUser
     *            The user to grant the roles to.
     */
    private void grantMaxUserRoles(PlatformUser customerUser) {
        grantRole(customerUser, UserRoleType.ORGANIZATION_ADMIN);
        if (customerUser.getOrganization()
                .hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER)) {
            grantRole(customerUser, UserRoleType.TECHNOLOGY_MANAGER);
        }
        if (customerUser.getOrganization()
                .hasRole(OrganizationRoleType.SUPPLIER)) {
            grantRole(customerUser, UserRoleType.SERVICE_MANAGER);
        }
    }

    @Override
    public void cleanUpCurrentUser() {

        OnBehalfUserReference onBehalf = dm.getCurrentUser().getMaster();
        if (onBehalf != null) {
            dm.remove(onBehalf);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void createOrganizationAdmin(VOUserDetails userDetails,
            Organization organization, String password, Long serviceKey,
            Marketplace marketplace)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException,
            ValidationException, MailOperationException {
        ArgumentValidator.notNull("organization", organization);
        ArgumentValidator.notNull("userDetails", userDetails);

        PlatformUser organizationAdmin = null;

        // 1. create the user with the given password or with a generated one,
        // if none was set
        String pwd = password;
        boolean isAutoGeneratedPassword = false;
        if (password == null) {
            PasswordGenerator gen = new PasswordGenerator();
            pwd = gen.generatePassword();
            isAutoGeneratedPassword = true;
        }

        try {
            if (organization.isRemoteLdapActive()) {
                organizationAdmin = addPlatformUser(userDetails, organization,
                        pwd, UserAccountStatus.ACTIVE, true, false, marketplace,
                        false, true);

            } else {
                if (isAutoGeneratedPassword) {
                    organizationAdmin = addPlatformUser(userDetails,
                            organization, pwd,
                            UserAccountStatus.PASSWORD_MUST_BE_CHANGED, true,
                            true, marketplace, false, true);
                } else {
                    organizationAdmin = addPlatformUser(userDetails,
                            organization, pwd,
                            UserAccountStatus.LOCKED_NOT_CONFIRMED, false, true,
                            marketplace, false, true);
                }
            }
        } catch (UserRoleAssignmentException e) {
            throw new ValidationException(e.getMessage());
        }

        if (marketplace == null) {
            checkMinimumUserRoleConstrains(organizationAdmin);
        }

        // 2. send an email to the user to indicate the need to confirm the
        // account. Only required, if a self-chosen password is used
        if (!isAutoGeneratedPassword) {
            String marketplaceId = null;
            if (marketplace != null) {
                marketplaceId = marketplace.getMarketplaceId();
            }
            StringBuffer url = new StringBuffer();

            url.append(cs.getBaseURL());

            // remove any trailing slashes from the base url
            removeTrailingSlashes(url);

            String[] urlParam = new String[4];
            urlParam[0] = organizationAdmin.getOrganization()
                    .getOrganizationId();
            urlParam[1] = organizationAdmin.getUserId();
            urlParam[2] = marketplaceId;
            urlParam[3] = serviceKey == null ? null
                    : String.valueOf(serviceKey.longValue());

            if (!organizationAdmin.hasManagerRole()) {
                url.append("/marketplace/confirm.jsf?")
                        .append((marketplaceId != null)
                                ? "mId=" + marketplaceId + "&" : "")
                        .append("enc=");
            } else {
                url.append("/public/confirm.jsf?")
                        .append((marketplaceId != null)
                                ? "mId=" + marketplaceId + "&" : "")
                        .append("enc=");
            }

            try {
                url.append(URLEncoder.encode(
                        ParameterEncoder.encodeParameters(urlParam), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                MailOperationException mof = new MailOperationException(
                        "Confirmation URL creation failed!", e);
                logger.logError(Log4jLogger.SYSTEM_LOG, mof,
                        LogMessageIdentifier.ERROR_UNSUPPORTED_ENCODING);
                throw mof;
            }

            // Bug 7865: Add a dummy postfix to avoid problems with the
            // automatic link recognition in outlook.
            url.append("&et");

            String confirmationURL = url.toString();
            cm.sendMail(organizationAdmin, EmailType.USER_CONFIRM,
                    new Object[] { confirmationURL }, marketplace);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deletePlatformUser(PlatformUser user, Marketplace marketplace)
            throws UserDeletionConstraintException, ObjectNotFoundException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        dm.getReference(PlatformUser.class, user.getKey());
        // internal call, so also admin users may be revoked
        deletePlatformUser(user, true, true, marketplace);
    }

    /**
     * Method has been deprecated. Use #{@getPlatformUser} with tenant parmeter.
     * 
     * @param userId
     *            The user identifying attributes' representation.
     * @param validateOrganization
     *            <code>true</code> if the calling user must be part of the same
     *            organization as the requested user.
     * @return
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    @Deprecated
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public PlatformUser getPlatformUser(String userId,
            boolean validateOrganization)
            throws ObjectNotFoundException, OperationNotPermittedException {

        return getPlatformUser(userId, null, validateOrganization);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public PlatformUser getPlatformUser(String userId, String tenantId,
            boolean validateOrganization)
            throws ObjectNotFoundException, OperationNotPermittedException {

        logger.logDebug("Get user " + userId + " for tenant " + tenantId);

        PlatformUser platformUser = new PlatformUser();
        platformUser.setUserId(userId);
        platformUser.setTenantId(tenantId);
        platformUser = dm.find(platformUser);

        if (platformUser == null) {
            ObjectNotFoundException onf = new ObjectNotFoundException(
                    ObjectNotFoundException.ClassEnum.USER, userId);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, onf,
                    LogMessageIdentifier.WARN_USER_NOT_FOUND);
            throw onf;
        }

        if (validateOrganization) {
            // Validate whether the calling user belongs to the same
            // organization as the requested user. Otherwise an exception will
            // be thrown.
            PermissionCheck.sameOrg(dm.getCurrentUser(), platformUser, logger);
        }

        return platformUser;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<PlatformUser> getOverdueOrganizationAdmins(long currentTime) {
        String period = cs.getConfigurationSetting(
                ConfigurationKey.PERMITTED_PERIOD_UNCONFIRMED_ORGANIZATIONS,
                Configuration.GLOBAL_CONTEXT).getValue();
        long maxTime = currentTime - Long.parseLong(period);

        Query query = dm
                .createNamedQuery("PlatformUser.getOverdueOrganizationAdmins");
        query.setParameter("status", UserAccountStatus.LOCKED_NOT_CONFIRMED);
        query.setParameter("date", Long.valueOf(maxTime));
        return ParameterizedTypes.list(query.getResultList(),
                PlatformUser.class);
    }

    //
    // internal methods, not considering txn at all
    //

    void deletePlatformUser(PlatformUser userToBeDeleted,
            boolean deleteLastAdmin, boolean selfDeletionAllowed,
            Marketplace marketplace) throws UserDeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        // 1a. check if the user belongs to any subscription. If so, he must not
        // be deleted
        List<Subscription> subscriptions = sm
                .getSubscriptionsForUserInt(userToBeDeleted);
        boolean ifAllExpired = isAllSubscriptionExpired(subscriptions);
        if (subscriptions.size() > 0 && !ifAllExpired) {
            UserDeletionConstraintException udcv = new UserDeletionConstraintException(
                    "User '" + userToBeDeleted.getUserId()
                            + "' is registered for a subscription and cannot be deleted!",
                    Reason.HAS_ACTIVE_SUBSCRIPTIONS);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, udcv,
                    LogMessageIdentifier.WARN_USER_DELETION_FAILED);
            throw udcv;
        }
        if (isUserLoggedIn(userToBeDeleted.getKey())) {
            UserDeletionConstraintException udcv = new UserDeletionConstraintException(
                    "User '" + userToBeDeleted.getUserId()
                            + "' is logged in and cannot be deleted!",
                    Reason.IS_USER_LOGGED_IN);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, udcv,
                    LogMessageIdentifier.WARN_USER_DELETION_FAILED);
            throw udcv;
        }
        if (!selfDeletionAllowed) {
            if (sessionCtx.getCallerPrincipal().getName()
                    .equals(String.valueOf(userToBeDeleted.getKey()))) {
                UserDeletionConstraintException udcv = new UserDeletionConstraintException(
                        "User '" + userToBeDeleted.getUserId()
                                + "' cannot delete himself!",
                        Reason.FORBIDDEN_SELF_DELETION);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, udcv,
                        LogMessageIdentifier.WARN_USER_DELETION_FAILED);
                throw udcv;
            }
        }
        if (!deleteLastAdmin) {
            if (isUserLastAdminForOrganization(userToBeDeleted)) {
                UserDeletionConstraintException udcv = new UserDeletionConstraintException(
                        "User '" + userToBeDeleted.getUserId()
                                + "' cannot be deleted as he is the last admin for the organization.",
                        Reason.LAST_ADMIN);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, udcv,
                        LogMessageIdentifier.WARN_USER_DELETION_FAILED);
                throw udcv;
            }
        }

        // It's needed to delete review data before user is deleted
        try {
            if (subscriptions.size() > 0 && ifAllExpired) {
                for (Subscription subscription : subscriptions) {
                    sm.revokeUserFromSubscriptionInt(subscription,
                            Collections.singletonList(userToBeDeleted));
                }
            }
            rvs.deleteReviewsOfUser(userToBeDeleted, false);
        } catch (OperationNotPermittedException e) {
            // permission check of delete review is off, so this exception must
            // not happen
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_DELETE_USER_FAILED_BY_DELETION_REVIEW_PERMISSION_ERROR);
            throw sse;
        }

        // 2. as all constraints are met, remove the user from the database
        dm.remove(userToBeDeleted);

        // 3. send email to inform the user about the deleted account
        try {
            String organizationName = userToBeDeleted.getOrganization()
                    .getName();
            cm.sendMail(userToBeDeleted, EmailType.USER_DELETED,
                    new Object[] {
                            organizationName == null ? "" : organizationName },
                    marketplace);
        } catch (MailOperationException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_SEND_USER_DELETED_MAIL_FAILED);
        }

    }

    PlatformUser addPlatformUser(VOUserDetails userDetails,
            Organization organization, String password,
            UserAccountStatus lockLevel, boolean sendMail,
            boolean userLocalLdap, Marketplace marketplace,
            boolean performRoleCheck)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException {
        return addPlatformUser(userDetails, organization, password, lockLevel,
                sendMail, userLocalLdap, marketplace, performRoleCheck, false);
    }

    // TODO: platform user persisting
    private PlatformUser addPlatformUser(VOUserDetails userDetails,
            Organization organization, String password,
            UserAccountStatus lockLevel, boolean sendMail,
            boolean userLocalLdap, Marketplace marketplace,
            boolean performRoleCheck, boolean createOrgAdminRole)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException {

        if (!userLocalLdap) {
            // Remote LDAP active
            if (!isUserIdUnique(userDetails.getRealmUserId())) {
                // The user name in the LDAP system is not unique in the
                // context of BES, a new unique user name need to be created for
                // BES usage.
                String uniqueId = createUniqueUserId(userDetails);
                try {
                    BLValidator.isUserId("uniqueId", uniqueId, true);
                } catch (ValidationException e) {
                    ValidationException vf = new ValidationException(
                            ReasonEnum.LDAP_CREATED_ID_INVALID, null,
                            new Object[] { uniqueId });
                    logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                            LogMessageIdentifier.ERROR_VALIDATION_PARAMETER_LDAP_FOUND_ERROR,
                            "User ID");
                    throw vf;
                }
                userDetails.setUserId(uniqueId);
            } else {
                userDetails.setUserId(userDetails.getRealmUserId());
            }
        }
        PlatformUser pu = UserDataAssembler.toPlatformUser(userDetails);

        pu.setOrganization(organization);
        if (userLocalLdap) {
            // Do not set the user password if CT-MG is a SAML SP
            if (!cs.isServiceProvider()) {
                setPassword(pu, password);
            }
        }
        pu.setCreationDate(DateFactory.getInstance().getTransactionDate());
        if (userDetails.getRealmUserId() != null) {
            pu.setRealmUserId(userDetails.getRealmUserId());
        } else {
            // If the realm id is not available, use the normal BES user id
            // instead
            pu.setRealmUserId(userDetails.getUserId());
        }

        // Set the user account status to active if CT-MG is a SAML SP
        if (cs.isServiceProvider() && UserAccountStatus.PASSWORD_MUST_BE_CHANGED
                .equals(lockLevel)) {
            pu.setStatus(UserAccountStatus.ACTIVE);
        } else {
            pu.setStatus(lockLevel);
        }
        if (organization.getTenant() != null) {
            pu.setTenantId(organization.getTenant().getTenantId());
        }
        dm.persist(pu);

        for (UserRoleType role : userDetails.getUserRoles()) {
            if (!pu.hasRole(role)) {
                if (performRoleCheck) {
                    checkRoleConstrains(pu, role);
                }
                grantRole(pu, role);
            }
        }

        if (createOrgAdminRole && (!userDetails.hasAdminRole())) {
            grantRole(pu, UserRoleType.ORGANIZATION_ADMIN);
        }

        if (performRoleCheck && marketplace == null) {
            checkMinimumUserRoleConstrains(pu);
        }

        dm.flush();
        dm.refresh(organization);
        dm.refresh(pu);

        if (sendMail) {
            sendMailToCreatedUser(password, userLocalLdap, marketplace, pu);
        }
        return pu;
    }

    UserGroup getDefaultUserGroupForOrganization(Organization org) {
        ArgumentValidator.notNull("organization", org);

        for (UserGroup group : org.getUserGroups()) {
            if (group.isDefault()) {
                return group;
            }
        }
        return null;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void sendMailToCreatedUser(String password, boolean userLocalLdap,
            Marketplace marketplace, PlatformUser pu)
            throws MailOperationException {
        String tenantId = getTenantIdForEmail(pu);
        if (!SendMailControl.isSendMail()) {
            // keep password for later sending
            SendMailControl.setMailData(password, marketplace);
            return;
        }
        String marketplaceId = null;
        if (marketplace != null) {
            marketplaceId = marketplace.getMarketplaceId();
        }

        if (userLocalLdap) {
            // if the user role is manager and the marketplaceId exists, which
            // happens only by registering users as a supplier in the
            // marketplace portal, then append both the base url and the
            // marketplace url.
            if (pu.hasManagerRole()) {
                if (marketplaceId != null) {
                    if (cs.isServiceProvider()) {
                        cm.sendMail(pu,
                                EmailType.USER_CREATED_WITH_MARKETPLACE_SAML_SP,
                                new Object[] { pu.getUserId(),
                                        cm.getBaseUrlWithTenant(tenantId),
                                        cm.getMarketplaceUrl(marketplaceId),
                                        cm.getBaseUrlHttpsWithTenant(tenantId),
                                        cm.getMarketplaceUrlHttps(
                                                marketplaceId) },
                                marketplace);

                    } else {
                        cm.sendMail(pu, EmailType.USER_CREATED_WITH_MARKETPLACE,
                                new Object[] { pu.getUserId(), password,
                                        cm.getBaseUrl(),
                                        cm.getMarketplaceUrl(marketplaceId),
                                        String.valueOf(pu.getKey()),
                                        cm.getBaseUrlHttps(),
                                        cm.getMarketplaceUrlHttps(
                                                marketplaceId) },
                                marketplace);

                    }
                } else {
                    if (cs.isServiceProvider()) {
                        cm.sendMail(pu, EmailType.USER_CREATED_SAML_SP,
                                new Object[] { pu.getUserId(),
                                        cm.getBaseUrlWithTenant(tenantId),
                                        cm.getBaseUrlHttpsWithTenant(
                                                tenantId) },
                                marketplace);
                    } else {
                        cm.sendMail(pu, EmailType.USER_CREATED,
                                new Object[] { pu.getUserId(), password,
                                        cm.getBaseUrl(),
                                        String.valueOf(pu.getKey()),
                                        cm.getBaseUrlHttps() },
                                marketplace);

                    }
                }

            } else {
                if (cs.isServiceProvider()) {
                    cm.sendMail(pu, EmailType.USER_CREATED_SAML_SP,
                            new Object[] { pu.getUserId(),
                                    cm.getMarketplaceUrl(marketplaceId),
                                    cm.getMarketplaceUrlHttps(marketplaceId) },
                            marketplace);
                } else {
                    cm.sendMail(pu, EmailType.USER_CREATED,
                            new Object[] { pu.getUserId(), password,
                                    cm.getMarketplaceUrl(marketplaceId),
                                    String.valueOf(pu.getKey()),
                                    cm.getMarketplaceUrlHttps(marketplaceId) },
                            marketplace);
                }

            }
        } else {
            if (pu.hasManagerRole()) {
                if (marketplaceId != null) {
                    cm.sendMail(pu, EmailType.USER_IMPORTED_WITH_MARKETPLACE,
                            new Object[] { pu.getUserId(), "", cm.getBaseUrl(),
                                    cm.getMarketplaceUrl(marketplaceId),
                                    String.valueOf(pu.getKey()),
                                    cm.getMarketplaceUrlHttps(marketplaceId) },
                            marketplace);

                } else {
                    cm.sendMail(pu, EmailType.USER_IMPORTED,
                            new Object[] { pu.getUserId(), "", cm.getBaseUrl(),
                                    String.valueOf(pu.getKey()),
                                    cm.getBaseUrlHttps() },
                            marketplace);

                }
            } else {
                cm.sendMail(pu, EmailType.USER_IMPORTED,
                        new Object[] { pu.getUserId(), "",
                                cm.getMarketplaceUrl(marketplaceId),
                                String.valueOf(pu.getKey()),
                                cm.getMarketplaceUrlHttps(marketplaceId) },
                        marketplace);
            }
        }
    }

    private String getTenantIdForEmail(PlatformUser user) {
        final Tenant tenant = user.getOrganization().getTenant();
        String tenantId = null;
        if (tenant != null) {
            tenantId = tenant.getTenantId();
        }
        return tenantId;
    }

    /**
     * 
     * Returns the configured number of retry attempts. If none is specified,
     * the fallback value will be returned.
     * 
     * @return The number of max. allowed login retry attempts.
     */
    private int getMaxRetryAttempts() {
        return Integer.parseInt(cs.getConfigurationSetting(
                ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS,
                Configuration.GLOBAL_CONTEXT).getValue());
    }

    private void createUserRoleRelationInt(PlatformUser user, UserRoleType role)
            throws UserRoleAssignmentException {
        if (!user.hasRole(role)) {
            checkRoleConstrains(user, role);
            grantRole(user, role);
        }
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void grantUnitRole(PlatformUser user, UserRoleType role) {
        if (role.isUnitRole()) {
            grantRole(user, role);
        }
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void revokeUnitRole(PlatformUser user, UserRoleType role) {
        if (!user.hasRole(role)) {
            return;
        }
        if (!role.isUnitRole()) {
            return;
        }
        RoleAssignment roleAssignment = user.getAssignedRole(role);
        if (roleAssignment != null) {
            dm.remove(roleAssignment);
        }
    }

    void checkMinimumUserRoleConstrains(PlatformUser user)
            throws ValidationException {
        if (user.getKey() > 0) {
            dm.flush();
            dm.refresh(user);
        }
        dm.refresh(user.getOrganization());
        final Set<OrganizationRoleType> orgRoles = user.getOrganization()
                .getGrantedRoleTypes();
        if (orgRoles.isEmpty()) {
            final ValidationException ve = new ValidationException(
                    "Organization must have at least one role, before creating a user. OrgId: "
                            + user.getOrganization().getOrganizationId());
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, ve,
                    LogMessageIdentifier.ERROR_USER_CREATION_FAILED_WITH_VALIDATION_ERROR);
            throw ve;
        }
        if (orgRoles.size() == 1 && orgRoles.iterator()
                .next() == OrganizationRoleType.CUSTOMER) {
            ValidationException ve = new ValidationException(
                    ReasonEnum.CUSTOMER_CREATION_ONLY_ON_MARKETPLACE, null,
                    null);
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, ve,
                    LogMessageIdentifier.ERROR_USER_CREATION_FAILED_WITH_VALIDATION_ERROR);
            throw ve;
        }
        final List<UserRoleType> requiredUserRoles = new ArrayList<>();
        for (OrganizationRoleType orgRole : orgRoles) {
            final UserRoleType userRole = orgRole.correspondingUserRole();
            if (userRole != null) {
                if (user.hasRole(userRole)
                        || user.hasRole(UserRoleType.ORGANIZATION_ADMIN)) {
                    return;
                }
                requiredUserRoles.add(userRole);
            }
        }

        final String[] s = new String[requiredUserRoles.size()];
        for (int i = 0; i < requiredUserRoles.size(); i++) {
            s[i] = requiredUserRoles.get(i).name();
        }

        final StringBuffer orgRoleString = new StringBuffer();
        for (OrganizationRoleType orgRole : orgRoles) {
            orgRoleString.append(" and ").append(orgRole.name());
        }
        orgRoleString.delete(0, 5);

        final StringBuffer userRoleString = new StringBuffer();
        for (UserRoleType userRole : requiredUserRoles) {
            userRoleString.append(" or ").append(userRole.name());
        }
        userRoleString.delete(0, 4);

        final ValidationException ve = new ValidationException(
                ReasonEnum.ROLE_REQUIRED, null, s);
        logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, ve,
                LogMessageIdentifier.WARN_USER_ROLE_REQUIRED, user.getUserId(),
                userRoleString.toString());
        throw ve;
    }

    void checkRoleConstrains(PlatformUser user, UserRoleType role)
            throws UserRoleAssignmentException {
        if (!dm.getCurrentUser().hasRole(UserRoleType.ORGANIZATION_ADMIN) && !dm
                .getCurrentUser().hasRole(UserRoleType.PLATFORM_OPERATOR)) {
            String msg = String.format(
                    "Role assign/revoke violation. Only %s and %s can assign/revoke roles.",
                    UserRoleType.ORGANIZATION_ADMIN,
                    UserRoleType.PLATFORM_OPERATOR);
            UserRoleAssignmentException ure = new UserRoleAssignmentException(
                    msg);
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, ure,
                    LogMessageIdentifier.WARN_USER_ROLE_REQUIRED,
                    user.getUserId());
            throw ure;
        }
        if (!isAllowedUserRole(user.getOrganization(), role)
                && !role.isUnitRole()) {
            OrganizationRoleType orgRole = OrganizationRoleType
                    .correspondingOrgRoleForUserRole(role);
            String msg = String.format(
                    "Role constraint violation. User role '%s' can only be assigned/revoked to %s.",
                    role.name(), orgRole.name());
            UserRoleAssignmentException ure = new UserRoleAssignmentException(
                    msg, new Object[] { orgRole.name(), role.name() });
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, ure,
                    LogMessageIdentifier.WARN_USER_ROLE_REQUIRED,
                    user.getUserId(), orgRole.name());
            throw ure;
        }
    }

    void grantRole(PlatformUser user, UserRoleType userRole) {
        if (user.hasRole(userRole)) {
            return;
        }
        UserRole uRole = (UserRole) dm.find(new UserRole(userRole));
        if (uRole == null) {
            throw new SaaSSystemException("UserRole " + userRole
                    + " not found. This object is created by the SQL setup scripts.");
        }
        RoleAssignment assignment = new RoleAssignment();
        assignment.setRole(uRole);
        assignment.setUser(user);
        try {
            dm.persist(assignment);
        } catch (NonUniqueBusinessKeyException ignore) {
            // user has role already
        }
    }

    void revokeRole(PlatformUser user, UserRoleType userRole)
            throws UserModificationConstraintException {

        if (userRole == UserRoleType.ORGANIZATION_ADMIN) {

            if (isUserLastAdminForOrganization(user)) {
                UserModificationConstraintException e = new UserModificationConstraintException(
                        UserModificationConstraintException.Reason.LAST_ADMIN);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_REVOKE_USER_ROLE_FAILED,
                        String.valueOf(UserRoleType.ORGANIZATION_ADMIN));
                throw e;
            }
        }
        RoleAssignment roleAssignment = user.getAssignedRole(userRole);
        if (roleAssignment != null) {
            dm.remove(roleAssignment);
        }
    }

    /**
     * Checks if the given user is the last administrative user for the
     * organization.
     * 
     * @param userToBeDeleted
     *            The user the check is performed for
     * @return <code>true</code>in case the user is the last administrative user
     *         for the organization, <code>false</code> otherwise.
     */
    private boolean isUserLastAdminForOrganization(
            PlatformUser userToBeDeleted) {
        if (userToBeDeleted.isOrganizationAdmin()) {
            List<VOUserDetails> usersForOrganization = getUsersForOrganization();
            int count = 0;
            for (VOUserDetails currentUser : usersForOrganization) {
                if (currentUser.hasAdminRole()) {
                    count++;
                }
            }
            if (count <= 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves all users with the given email (the search is performed case
     * insensitive).
     * 
     * @param email
     *            the email for the search
     * @return A list of registered users.
     */
    private List<PlatformUser> getUsersByEmail(String email) {
        Query query = dm.createNamedQuery("PlatformUser.listByEmail");
        query.setParameter("email", email);
        return ParameterizedTypes.list(query.getResultList(),
                PlatformUser.class);
    }

    private LdapConnector getLdapConnectionForOrganization(
            Organization organization) throws ValidationException {
        Properties resolvedProps = null;
        try {
            resolvedProps = ldapSettingsMS.getOrganizationSettingsResolved(
                    organization.getOrganizationId());
        } catch (ObjectNotFoundException e) {
            // must not happen cause method can only be invoked by platform
            // operator
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_UNKNOWN_ORGANIZATION,
                    organization.getOrganizationId());
            throw sse;
        }
        LdapConnector connector = getLdapConnector(resolvedProps);
        connector.ensureAllMandatoryLdapPropertiesPresent();
        return connector;
    }

    LdapConnector getLdapConnector(Properties resolvedProps) {
        return new LdapConnector(ldapAccess, resolvedProps);
    }

    /**
     * Create the LDAP search filter an return it.
     * 
     * @param attrMap
     *            the map with all LDAP attribute setting types.
     * @param pattern
     *            the pattern for the search.
     * @return the search filter
     */
    private String getLdapSearchFilter(Map<SettingType, String> attrMap,
            String pattern) {
        String uid = attrMap.get(SettingType.LDAP_ATTR_UID);
        if (uid == null) {
            uid = SettingType.LDAP_ATTR_UID.getDefaultValue();
        }
        return uid += "=" + pattern;
    }

    /**
     * Creates and persists a new platform user using the settings of the
     * current user, except the reference to the organization and the user
     * identifier. The user will be created as administrative user.
     * 
     * @param password
     *            The password to be assigned to the user. In case the
     *            authentication mode is SAML_SP, the password parameter is used
     *            as the userId to be set for the created user.
     * 
     * @param currentUser
     *            The master user for the user to be created.
     * @param customer
     *            The customer organization.
     * 
     * @return
     * @throws NonUniqueBusinessKeyException
     */
    private PlatformUser createOnBehalfUser(String password,
            PlatformUser currentUser, Organization customer)
            throws NonUniqueBusinessKeyException {
        VOUserDetails voUserDetails = UserDataAssembler
                .toVOUserDetails(currentUser);
        voUserDetails.setOrganizationId(customer.getOrganizationId());
        PlatformUser customerUser = null;
        int retryCount = 0;
        boolean isSAML = cs.isServiceProvider();
        while (customerUser == null) {
            try {
                // If the authentication mode is in SAML_SP, no automatic user
                // id
                // must be generated, as the user must already be present in the
                // IdP.
                // Instead, the password parameter is used as the userId.
                if (isSAML) {
                    voUserDetails.setUserId(password);
                } else {
                    voUserDetails.setUserId(
                            IdGenerator.generateArtificialIdentifier());
                }
                customerUser = addPlatformUser(voUserDetails, customer,
                        password, UserAccountStatus.ACTIVE, false, true, null,
                        false);
                OnBehalfUserReference onBehalf = new OnBehalfUserReference();
                onBehalf.setMasterUser(currentUser);
                onBehalf.setSlaveUser(customerUser);
                dm.persist(onBehalf);
            } catch (NonUniqueBusinessKeyException e) {
                if (isSAML) {
                    logger.logDebug("User with the userId " + password
                            + " already exists. But this is not a problem"
                            + " as you creating on behalf user.");
                    // Ignore the error (bug11001)
                    PlatformUser findTemplate = new PlatformUser();
                    findTemplate.setUserId(password);
                    try {
                        customerUser = (PlatformUser) dm
                                .getReferenceByBusinessKey(findTemplate);
                    } catch (ObjectNotFoundException e1) {
                        // impossible because we have got here after non unique
                        // business key exception has been
                        // thrown
                    }
                    break;
                }
                retryCount++;
                if (retryCount > 10) {
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.WARN_NON_UNIQUE_BUSINESS_KEY);
                    throw e;
                }
            } catch (MailOperationException e) {
                SaaSSystemException se = new SaaSSystemException(
                        "A mail operation failed although we didn't try to send a mail '"
                                + currentUser.getOrganization() + "'.",
                        e);
                logger.logError(Log4jLogger.SYSTEM_LOG, se,
                        LogMessageIdentifier.ERROR_MAIL_OPERATION_FAILED);
                throw se;
            } catch (ValidationException e) {
                // this never happens
                SaaSSystemException se = new SaaSSystemException(
                        "The user creation failed with a validation exception.",
                        e);
                logger.logError(Log4jLogger.SYSTEM_LOG, se,
                        LogMessageIdentifier.ERROR_USER_CREATION_FAILED_WITH_VALIDATION_ERROR);
                throw se;
            } catch (UserRoleAssignmentException e) {
                // this never happens
                SaaSSystemException se = new SaaSSystemException(
                        "The user creation failed with a user role assignment exception.",
                        e);
                logger.logError(Log4jLogger.SYSTEM_LOG, se,
                        LogMessageIdentifier.ERROR_USER_CREATION_FAILED_WITH_VALIDATION_ERROR);
                throw se;
            }
        }
        return customerUser;
    }

    /**
     * Validates that the preconditions for the creation of a on-behalf user are
     * met.
     * 
     * @param organizationId
     *            The identifier of the customer organization the user should be
     *            created for.
     * @param password
     *            The password to be given to the u ser.
     * @param currentUser
     *            The caller and master user for the user to be created.
     * @return The customer organization.
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    private Organization validateForOnBehalfUserCreation(String organizationId,
            String password, PlatformUser currentUser)
            throws ObjectNotFoundException, OperationNotPermittedException {
        ArgumentValidator.notNull("organizationId", organizationId);
        ArgumentValidator.notNull("password", password);

        Organization customer = new Organization();
        customer.setOrganizationId(organizationId);
        customer = (Organization) dm.getReferenceByBusinessKey(customer);

        if (!currentUser.getOrganization().isActingOnBehalf(customer)) {
            OperationNotPermittedException onpe = new OperationNotPermittedException();
            logger.logWarn(Log4jLogger.SYSTEM_LOG, onpe,
                    LogMessageIdentifier.WARN_USER_CREATE_CUSTOMER_FAILED,
                    currentUser.getUserId(),
                    currentUser.getOrganization().getOrganizationId(),
                    customer.getOrganizationId());
            throw onpe;
        }
        return customer;
    }

    /**
     * Read the values from the remote LDAP and update the corresponding
     * platform user attributes.
     * 
     * @param pUser
     *            The platform user to be processed.
     * @throws ValidationException
     *             if not all mandatory LDAP parameters can be resolved for the
     *             underlying LDAP managed organization of the given user
     */
    void syncUserWithLdap(PlatformUser pUser) throws ValidationException {
        LdapConnector connector = getLdapConnectionForOrganization(
                pUser.getOrganization());
        Properties dirProperties = connector.getDirProperties();
        Map<SettingType, String> attrMap = connector.getAttrMap();
        String baseDN = connector.getBaseDN();

        List<SettingType> attrList = new ArrayList<>(attrMap.keySet());
        ILdapResultMapper<VOUserDetails> mapper = new LdapVOUserDetailsMapper(
                null, attrMap);
        try {
            List<VOUserDetails> list = ldapAccess.search(dirProperties, baseDN,
                    getLdapSearchFilter(attrMap, pUser.getUserId()), mapper,
                    false);

            if (list.size() > 0) {
                UserDataAssembler.updatePlatformUser(list.get(0), attrList,
                        pUser);
            }
        } catch (NamingException e) {
            SaaSSystemException se = new SaaSSystemException(
                    "The LDAP search for the user '" + pUser.getKey()
                            + "' failed although the login succeeded.",
                    e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_LDAP_SEARCH_OF_USER_FAILED,
                    Long.toString(pUser.getKey()));
            throw se;
        }
    }

    private void setPassword(final PlatformUser user, final String password) {
        final long salt = random.nextLong();
        user.setPasswordSalt(salt);
        user.setPasswordHash(PasswordHash.calculateHash(salt, password));
    }

    /**
     * Returns the subscriptions UsageLicense corresponding with the given user.
     * 
     * @param user
     *            the user to find the usage license for
     * @param subscription
     *            the subscription to get the usage licenses from
     * @return The UsageLicense or <code>null</code> in case no matching license
     *         was found
     */
    private UsageLicense getUsgeLicenseForUserAndSubscription(PlatformUser user,
            Subscription subscription) {
        List<UsageLicense> licenses = subscription.getUsageLicenses();
        for (UsageLicense license : licenses) {
            if (license.getUser().getKey() == user.getKey()) {
                return license;
            }
        }
        return null;
    }

    /**
     * Tries to find the marketplace with the provided id. Returns
     * <code>null</code> if the id is <code>null</code> or empty.
     * 
     * @param marketplaceId
     *            the marketplace id
     * @return the {@link Marketplace} or <code>null</code>
     */
    private Marketplace getMarketplace(String marketplaceId) {
        if (Strings.isEmpty(marketplaceId)) {
            return null;
        }
        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(marketplaceId);
        marketplace = (Marketplace) dm.find(marketplace);
        return marketplace;
    }

    private Organization getOrganization(String organizationId)
            throws ObjectNotFoundException {
        Organization template = new Organization();
        template.setOrganizationId(organizationId);
        return (Organization) dm.getReferenceByBusinessKey(template);
    }

    /**
     * The small helper function to create a user id which is unique in the
     * context of BES id the regular user id is not unique. The function follows
     * the scheme: 1. use email as user id 2. create string userid@orgid and us
     * it as user id.
     * 
     * @param userDetails
     *            Information about the user which should be created.
     * @return a globally unique user id.
     */
    private String createUniqueUserId(VOUserDetails userDetails) {
        String uniqueId = userDetails.getEMail();
        if (!isUserIdUnique(uniqueId)) {
            String orgId = userDetails.getOrganizationId();
            String userId = userDetails.getRealmUserId();
            uniqueId = userId + "@" + orgId;
            int i = 1;
            while (!isUserIdUnique(uniqueId) && i < 1000) {
                uniqueId = userDetails.getRealmUserId() + "_" + i;
                i++;
            }
        }
        return uniqueId;
    }

    /**
     * Checks if a user with the passed user id already exists.
     * 
     * @param userId
     *            the user id which should be checked.
     * @return <code>true</code> if the user id is unique, else false.
     */
    private boolean isUserIdUnique(String userId) {
        if (userId == null) {
            return false;
        }
        PlatformUser queryUser = new PlatformUser();
        queryUser.setUserId(userId);
        return (dm.find(queryUser) == null);
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void setUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, UserModificationConstraintException,
            OperationNotPermittedException, UserRoleAssignmentException,
            UserActiveException {

        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("roles", roles);

        PlatformUser pUser = dm.getReference(PlatformUser.class, user.getKey());

        setUserRolesInt(new HashSet<>(roles), pUser);

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void setUserRolesInt(Set<UserRoleType> roles, PlatformUser pUser)
            throws UserModificationConstraintException, UserActiveException,
            OperationNotPermittedException, UserRoleAssignmentException,
            ObjectNotFoundException {

        List<UserRoleType> listForRevoke = new ArrayList<>();
        Iterator<RoleAssignment> roleIterator = pUser.getAssignedRoles()
                .iterator();
        while (roleIterator.hasNext()) {
            RoleAssignment roleAssignment = roleIterator.next();
            if (!roles.contains(roleAssignment.getRole().getRoleName())
                    && !roleAssignment.getRole().getRoleName().isUnitRole()) {
                listForRevoke.add(roleAssignment.getRole().getRoleName());
            }
        }

        revokeUserRolesInt(pUser, listForRevoke);
        grantUserRoles(pUser, new ArrayList<>(roles));

    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public List<UserRoleType> getAvailableUserRoles(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("user", user);

        PlatformUser pUser = dm.getReference(PlatformUser.class, user.getKey());
        PermissionCheck.sameOrg(dm.getCurrentUser(), pUser, logger);

        Set<UserRoleType> roleSet = getAvailableUserRolesForUser(pUser);

        return new ArrayList<>(roleSet);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Set<UserRoleType> getAvailableUserRolesForUser(PlatformUser pu) {

        Query query = dm.createNamedQuery("UserRole.getAllUserRoles");
        List<UserRole> userRoleList = ParameterizedTypes
                .list(query.getResultList(), UserRole.class);
        Organization org = pu.getOrganization();
        Set<UserRoleType> roleList = new HashSet<>();
        for (UserRole userRole : userRoleList) {
            if (isAllowedUserRole(org, userRole.getRoleName())) {
                roleList.add(userRole.getRoleName());
            }
        }

        return roleList;
    }

    @Override
    public boolean isUserLoggedIn(long userKey) {
        List<Session> sessionsForUserKey = sessionService
                .getSessionsForUserKey(userKey);
        return (sessionsForUserKey.size() > 0);
    }

    boolean isAllowedUserRole(Organization org, UserRoleType userRole) {
        if (isOrganizationSpecificRole(userRole)) {
            OrganizationRoleType orgTypesForUserRole = OrganizationRoleType
                    .correspondingOrgRoleForUserRole(userRole);
            if (!org.hasRole(orgTypesForUserRole)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method already exists in UserRoleType class, but there are problem
     * on Windows. The method cannot be found at runtime.
     */
    boolean isOrganizationSpecificRole(UserRoleType roleType) {
        return !(UserRoleType.ORGANIZATION_ADMIN.equals(roleType)
                || UserRoleType.SUBSCRIPTION_MANAGER.equals(roleType));
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean removeInactiveOnBehalfUsers() {
        return prepareForNewTransaction().removeInactiveOnBehalfUsersImpl();
    }

    private IdentityServiceLocal prepareForNewTransaction() {
        DateFactory.getInstance().takeCurrentTime();
        return sessionCtx.getBusinessObject(IdentityServiceLocal.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean removeInactiveOnBehalfUsersImpl() {
        ConfigurationSetting setting = cs.getConfigurationSetting(
                ConfigurationKey.PERMITTED_PERIOD_INACTIVE_ON_BEHALF_USERS,
                Configuration.GLOBAL_CONTEXT);
        long period = setting.getLongValue();
        Long lowerPeriodBound = Long
                .valueOf(System.currentTimeMillis() - period);
        List<OnBehalfUserReference> inactiveUsers = findInactiveOnBehalfUsers(
                lowerPeriodBound);
        for (OnBehalfUserReference toBeRemoved : inactiveUsers) {
            dm.remove(toBeRemoved);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private List<OnBehalfUserReference> findInactiveOnBehalfUsers(
            Long lowerPeriodBound) {
        Query query = dm.createNamedQuery(
                "OnBehalfUserReference.findInactiveBeforePeriod");
        query.setParameter("leastPermittedTime", lowerPeriodBound);
        return query.getResultList();
    }

    @Override
    public void refreshLdapUser() throws ValidationException {

        PlatformUser user = dm.getCurrentUser();
        if (user.getOrganization().isRemoteLdapActive()) {
            syncUserWithLdap(user);
        }

    }

    private void removeTrailingSlashes(StringBuffer url) {
        while (url.length() > 0 && url.charAt(url.length() - 1) == '/') {
            url.replace(url.length() - 1, url.length(), "");
        }
    }

    /**
     * Checks if the specified organization uses a remote LDAP system for user
     * authentication. If so, an UnsupportedOperationException will be thrown.
     * 
     * @param organization
     *            The organization to check.
     * @param messageId
     *            The log message identifier to use in case an exception is
     *            thrown.
     */
    private void ensureNoRemoteLdapUsed(Organization organization,
            LogMessageIdentifier messageId) {
        if (organization.isRemoteLdapActive()) {
            UnsupportedOperationException e = new UnsupportedOperationException(
                    "It is forbidden to perform this operation if a remote LDAP is active.");
            logger.logError(Log4jLogger.SYSTEM_LOG, e, messageId);

            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    @Override
    public boolean isCallerOrganizationAdmin() {
        return sessionCtx
                .isCallerInRole(UserRoleType.ORGANIZATION_ADMIN.name());
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public VOUserDetails createUserWithGroups(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId,
            Map<Long, UnitUserRole> userGroupKeyToRole)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {
        return createUser(user, roles, marketplaceId, userGroupKeyToRole);
    }

    private VOUserDetails createUser(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId,
            Map<Long, UnitUserRole> userGroupKeyToRole)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {
        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("roles", roles);

        // TODO DEL
        Tenant tenant = null;
        try {
            if (StringUtils.isNotBlank(user.getTenantId())) {
                Tenant t = new Tenant();
                t.setTenantId(user.getTenantId());
                tenant = (Tenant) dm.getReferenceByBusinessKey(t);
            }
        } catch (ObjectNotFoundException e) {
            // ignore
        }

        checkIfUserExists(user.getUserId(), tenant);
        // TODO DEL

        TriggerProcessValidator validator = new TriggerProcessValidator(dm);
        if (validator.isRegisterOwnUserPending(user.getUserId())) {
            OperationPendingException ope = new OperationPendingException(
                    String.format(
                            "Operation cannot be performed. There is already another pending request to register a user with conflicting id '%s'.",
                            user.getUserId()),
                    OperationPendingException.ReasonEnum.REGISTER_USER_IN_OWN_ORGANIZATION,
                    new Object[] { user.getUserId() });
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_CREATE_USER_FAILED_DUE_TO_TRIGGER_CONFLICT,
                    user.getUserId());
            throw ope;
        }

        TriggerMessage message = new TriggerMessage(
                TriggerType.REGISTER_OWN_USER);
        List<TriggerProcessMessageData> list = triggerQS
                .sendSuspendingMessages(Collections.singletonList(message));
        TriggerProcess triggerProcess = list.get(0).getTrigger();
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.USER, user);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.USER_ROLE_TYPE, roles);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.MARKETPLACE_ID, marketplaceId);
        TriggerDefinition triggerDefinition = triggerProcess
                .getTriggerDefinition();
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.USER_GROUPS_WITH_ROLES,
                userGroupKeyToRole);

        VOUserDetails result = null;
        if (triggerDefinition == null) {
            try {
                result = createUserInt(triggerProcess);
            } catch (ValidationException | NonUniqueBusinessKeyException
                    | MailOperationException | UserRoleAssignmentException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
        } else if (triggerDefinition.isSuspendProcess()) {
            triggerProcess.setTriggerProcessIdentifiers(
                    TriggerProcessIdentifiers.createRegisterOwnUser(dm,
                            triggerDefinition.getType(), user));
            dm.merge(triggerProcess);
        }

        return result;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean addRevokeUserUnitAssignment(String unitName,
            List<VOUser> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException,
            OperationNotPermittedException, MailOperationException {
        ArgumentValidator.notNull("unitName", unitName);
        ArgumentValidator.notNull("usersToBeAdded", usersToBeAdded);
        ArgumentValidator.notNull("usersToBeRevoked", usersToBeRevoked);

        List<PlatformUser> added = new ArrayList<>();
        List<PlatformUser> revoked = new ArrayList<>();
        Set<Long> onBehalfUserKeys = new HashSet<>();

        List<PlatformUser> platformUsers = dm.getCurrentUser().getOrganization()
                .getPlatformUsers();
        UserGroup group = userGroupService.getUserGroupByName(unitName);
        for (PlatformUser user : platformUsers) {
            if (user.isOnBehalfUser()) {
                onBehalfUserKeys.add(Long.valueOf(user.getKey()));
            }
        }

        String currentUserTenant = dm.getCurrentUser().getTenantId();

        for (VOUser user : usersToBeAdded) {
            validateForOnBehalfUserGroupAssignment(user, onBehalfUserKeys);
            if (StringUtils.isBlank(user.getTenantId())) {
                user.setTenantId(currentUserTenant);
            }
            PlatformUser platformUser = new PlatformUser();
            platformUser.setUserId(user.getUserId());
            platformUser.setTenantId(user.getTenantId());
            added.add(platformUser);
        }
        for (VOUser user : usersToBeRevoked) {
            validateForOnBehalfUserGroupAssignment(user, onBehalfUserKeys);
            if (StringUtils.isBlank(user.getTenantId())) {
                user.setTenantId(currentUserTenant);
            }
            PlatformUser platformUser = new PlatformUser();
            platformUser.setUserId(user.getUserId());
            platformUser.setTenantId(user.getTenantId());
            revoked.add(platformUser);
        }

        if (!added.isEmpty() && group.isDefault()) {
            OperationNotPermittedException onpe = new OperationNotPermittedException();
            logger.logWarn(Log4jLogger.SYSTEM_LOG, onpe,
                    LogMessageIdentifier.WARN_DEFAULT_USERGROUP_OPERATION_NOT_PERMITTED);
            throw onpe;
        }

        try {
            userGroupService.assignUsersToGroup(group, added);
            userGroupService.revokeUsersFromGroup(group, revoked);
        } catch (NonUniqueBusinessKeyException | MailOperationException
                | OperationNotPermittedException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return true;
    }

    private void validateForOnBehalfUserGroupAssignment(VOUser user,
            Set<Long> onBehalfUserKeys) throws OperationNotPermittedException {
        if (onBehalfUserKeys.contains(Long.valueOf(user.getKey()))) {
            OperationNotPermittedException onpe = new OperationNotPermittedException();
            logger.logWarn(Log4jLogger.SYSTEM_LOG, onpe,
                    LogMessageIdentifier.WARN_ADDREVOKE_USERGROUP_ASSIGNMENT_FOR_ONBEHALFUSER_NOT_PERMITTED,
                    user.getUserId());
            throw onpe;
        }
    }

    private void checkIfMarketplaceExists(String marketplaceId)
            throws ObjectNotFoundException {
        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(marketplaceId);
        dm.getReferenceByBusinessKey(marketplace);
    }

    private boolean isAllSubscriptionExpired(List<Subscription> subscriptions) {
        boolean allExpired = true;
        for (Subscription subscription : subscriptions) {
            if (!subscription.getStatus().isExpired()) {
                allExpired = false;
                break;
            }
        }
        return allExpired;
    }

    @Override
    public PlatformUser getPlatformUserByOrganization(String userId,
            String orgId) throws ObjectNotFoundException {

        Query query = dm.createNamedQuery("PlatformUser.findByUserIdAndOrgId");

        query.setParameter("userId", userId);
        query.setParameter("organizationId", orgId);

        PlatformUser platformUser = null;

        try {
            platformUser = (PlatformUser) query.getSingleResult();
        } catch (NoResultException e) {
            throwONFExcp(userId);
        }

        if (platformUser == null) {
            throwONFExcp(userId);
        }

        return platformUser;  
    }

    private void throwONFExcp(String userId) throws ObjectNotFoundException {

        ObjectNotFoundException onf = new ObjectNotFoundException(
                ObjectNotFoundException.ClassEnum.USER, userId);
        logger.logWarn(Log4jLogger.SYSTEM_LOG, onf,
                LogMessageIdentifier.WARN_USER_NOT_FOUND);
        throw onf;
    }
    
    @Override
    public VOUserDetails loadUserDetailsFromOIDCProvider(String userId,
            String tenantId, String token) throws RegistrationException {
        try {
            return Userinfo.getUserinfoFromIdentityService(userId,
                    tenantId, token);
        } catch (Exception e) {
            throw createRegistrationException(e);
        }
    }        

    @Override
    public String createAccessGroupInOIDCProvider(String tenantId,
            String token, String groupName) throws RegistrationException {
        String caller = dm.getCurrentUser().getOrganization().getName();
        try {
            return AccessGroup.createGroup(tenantId, token, groupName, caller);
        } catch (Exception e) {
            throw createRegistrationException(e);
        }

    }

    @Override
    public void addMemberToAccessGroupInOIDCProvider(String groupId,
            String tenantId, String token, VOUserDetails userInfo)
            throws RegistrationException {
        try {
            AccessGroup.addMemberToGroup(groupId, tenantId, token, userInfo);
        } catch (Exception e) {
            RegistrationException rf = createRegistrationException(e);
            throw rf;
        }
    }
    
    private RegistrationException createRegistrationException(Exception e) {
        logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                LogMessageIdentifier.ERROR_CREATE_ORGANIZATION);
        RegistrationException rf = new RegistrationException(
                "Can not connect to the OIDC service.");
        rf.setMessageKey("ex.RegistrationException.OIDC_ERROR");
        return rf;
    }
    
    @Override
    public boolean synchronizeUsersWithOIDCProvider(String tenantId) {

        Userinfo userinfo = new Userinfo();
        String token = getOIDCTokenForTenant(tenantId);
        List<Organization> organizations = getAllOrganizationsFromDb();
        for (int i = 0; i < organizations.size(); i++) {
            if (organizations.get(i).getKey() == 18000) { //organizations.get(i).getTenant().getTenantId() != null;
//                organizations.get(i).getTenant().getTenantId() ;
                List<VOUserDetails> usersInGroup = userListMock(); //userinfo.getAllUserDetailsForGroup(organizations.get(i).getGroupId(), tenantId, token);
                 for(int j = 0; j < usersInGroup.size(); j++){
                    VOUserDetails user = userMock(); //loadUserDetailsFromOIDCProvider(usersInGroup.get(j).getUserId(), tenantId, token);
                    PlatformUser platformuser =  null; //loadUser(user.getUserId(), organizations.get(i).getTenant());
                    if (platformuser == null) {
                        user.setOrganizationId(organizations.get(i).getOrganizationId());
                        String mp = getFirstMarktplaceIdFromOrganization(organizations.get(i));
                        try {
                            importUser(user, mp);
                        } catch (NonUniqueBusinessKeyException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ObjectNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (MailOperationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ValidationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (UserRoleAssignmentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        System.out.println("test");
                    }
                 }
            }
        }
        return false;
    }
    
    private List<Organization> getAllOrganizationsFromDb(){
        Query query = dm.createNamedQuery("Organization.getAllOrganizations");
        return ParameterizedTypes.list(query.getResultList(),
                Organization.class);
    }
    
    private String getOIDCTokenForTenant(String tenantId) {
        AccessToken oidcToken = new AccessToken();
        return oidcToken.getOidcToken(tenantId);
    }
    
    private List<VOUserDetails> userListMock(){
        VOUserDetails user = new VOUserDetails();
        List<VOUserDetails> users = new ArrayList();
        user.setKey(17000);
        user.setEMail("customer@ctmg.onmicrosoft.com");
        user.setUserId("customer@ctmg.onmicrosoft.com");
        user.setRealmUserId("customer@ctmg.onmicrosoft.com");
        users.add(user);
        return users;
    }
    
    private VOUserDetails userMock(){
        VOUserDetails user = new VOUserDetails();
        user.setKey(17000);
        user.setEMail("customer@ctmg.onmicrosoft.com");
        user.setUserId("customer@ctmg.onmicrosoft.com");
        user.setRealmUserId("customer@ctmg.onmicrosoft.com");
        user.setLocale("en");
        return user;
    }
    
    private String getFirstMarktplaceIdFromOrganization(Organization organization) {
        List<Marketplace> marketplaces = organization.getMarketplaces();
        if(marketplaces.size() > 0) {
            return marketplaces.get(0).getMarketplaceId();
        }
        return null;
    }


}
