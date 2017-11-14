/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.identityservice.bean;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.BulkUserImportException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.SecurityCheckException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.UserActiveException;
import org.oscm.internal.types.exception.UserDeletionConstraintException;
import org.oscm.internal.types.exception.UserModificationConstraintException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

@Stateless
@Remote(IdentityService.class)
@Local(IdentityServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class IdManagementStub implements IdentityService, IdentityServiceLocal {

    @Override
    public void grantUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException {

    }

    @Override
    public void revokeUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, UserModificationConstraintException,
            UserActiveException, OperationNotPermittedException,
            UserRoleAssignmentException {

    }

    @EJB
    DataService dataManager;

    @Override
    public VOUserDetails createUser(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException {
        return null;
    }

    @Override
    public VOUserDetails createUser(VOUserDetails user, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {
        return null;
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
    }

    @Override
    public void confirmAccount(VOUser user, String marketplaceId)
            throws OperationNotPermittedException, ObjectNotFoundException {

    }

    @Override
    public void deleteUser(VOUser user, String marketplaceId)
            throws UserDeletionConstraintException {

    }

    @Override
    public List<VOUserDetails> getUsersForOrganization() {

        return null;
    }

    @Override
    public void lockUserAccount(VOUser userToBeLocked,
            UserAccountStatus newStatus, String marketplaceId)
            throws OperationNotPermittedException, ObjectNotFoundException {

    }

    @Override
    public VOUserDetails updateUser(VOUserDetails userDetails)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        return null;
    }

    @Override
    public void unlockUserAccount(VOUser user, String marketplaceId) {

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deletePlatformUser(PlatformUser user, Marketplace marketplace)
            throws UserDeletionConstraintException {

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void setUserAccountStatus(PlatformUser user,
            UserAccountStatus newStatus) {

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public PlatformUser getPlatformUser(String userId,
            boolean validateOrganization)
            throws ObjectNotFoundException, OperationNotPermittedException {
        return getPlatformUser(userId, null, validateOrganization);
    }

    @Override
    public PlatformUser getPlatformUser(String userId, String tenantId,
            boolean validateOrganization)
            throws ObjectNotFoundException, OperationNotPermittedException {

        PlatformUser platformUser = new PlatformUser();
        platformUser.setUserId(userId);
        platformUser.setTenantId(tenantId);
        platformUser = dataManager.find(platformUser);

        if (platformUser == null) {
            ObjectNotFoundException onf = new ObjectNotFoundException(
                    ObjectNotFoundException.ClassEnum.USER, userId);
            throw onf;
        }

        if (validateOrganization) {
            PlatformUser cu = dataManager.getCurrentUser();
            if (cu.getOrganization().getKey() != platformUser.getOrganization()
                    .getKey()) {
                throw new OperationNotPermittedException();
            }
        }

        return platformUser;
    }

    @Override
    public VOUserDetails getUserDetails(VOUser user) {

        return null;
    }

    @Override
    public void notifyOnLoginAttempt(VOUser user, boolean attemptSuccessful)
            throws ObjectNotFoundException, SecurityCheckException,
            ValidationException {

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void createOrganizationAdmin(VOUserDetails userDetails,
            Organization organization, String password, Long serviceKey,
            Marketplace marketplace) throws NonUniqueBusinessKeyException,
            ObjectNotFoundException, MailOperationException {

    }

    @Override
    public VOUser getUser(VOUser user) throws ObjectNotFoundException {

        return null;
    }

    @Override
    public VOUserDetails getCurrentUserDetails() {

        return null;
    }

    @Override
    public VOUserDetails getCurrentUserDetailsIfPresent() {

        return null;
    }

    @Override
    public void requestResetOfUserPassword(VOUser user, String marketplaceId) {

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<PlatformUser> getOverdueOrganizationAdmins(long currentTime) {

        return null;
    }

    @Override
    public void sendAccounts(String email, String marketplaceId)
            throws ValidationException, MailOperationException {

    }

    @Override
    public List<VOUserDetails> searchLdapUsers(String userIdPattern)
            throws ValidationException {

        return null;
    }

    @Override
    public void importLdapUsers(List<VOUserDetails> users, String marketplaceId)
            throws NonUniqueBusinessKeyException, ValidationException,
            MailOperationException {

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public PlatformUser modifyUserData(PlatformUser existingUser,
            VOUserDetails tempUser, boolean modifyOwnUser, boolean sendMail)
            throws OperationNotPermittedException,
            NonUniqueBusinessKeyException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        return null;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void resetPasswordForUser(PlatformUser user,
            Marketplace marketplace) {

    }

    @Override
    public List<UserRoleType> getAvailableUserRoles(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException {
        return Collections.emptyList();
    }

    @Override
    public void setUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserModificationConstraintException, UserRoleAssignmentException,
            UserActiveException {

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean removeInactiveOnBehalfUsers() {

        return false;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean removeInactiveOnBehalfUsersImpl() {

        return false;
    }

    @Override
    public VOUserDetails createOnBehalfUser(String organizationId,
            String string) throws ObjectNotFoundException,
            OperationNotPermittedException, NonUniqueBusinessKeyException {

        return null;
    }

    @Override
    public void cleanUpCurrentUser() {

    }

    @Override
    public void grantUserRoles(PlatformUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException {
    }

    @Override
    public void refreshLdapUser() throws ValidationException {

    }

    @Override
    public VOUserDetails createUserInt(TriggerProcess tp)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException {
        return null;
    }

    @Override
    public List<PlatformUser> getOrganizationUsers() {
        return null;
    }

    @Override
    public Set<UserRoleType> getAvailableUserRolesForUser(PlatformUser pu) {
        return null;
    }

    @Override
    public void resetUserPassword(PlatformUser platformUser,
            String marketplaceId)
            throws UserActiveException, MailOperationException {
    }

    @Override
    public void deleteUser(PlatformUser pUser, String marketplaceId)
            throws OperationNotPermittedException,
            UserDeletionConstraintException {
    }

    @Override
    public void sendUserUpdatedMail(PlatformUser existingUser,
            PlatformUser oldUser) {
    }

    @Override
    public void notifySubscriptionsAboutUserUpdate(PlatformUser existingUser) {
    }

    @Override
    public void setUserRolesInt(Set<UserRoleType> roles, PlatformUser pUser)
            throws UserModificationConstraintException, UserActiveException,
            OperationNotPermittedException, UserRoleAssignmentException,
            ObjectNotFoundException {
    }

    @Override
    public void verifyIdUniquenessAndLdapAttributes(PlatformUser existingUser,
            PlatformUser modUser) throws NonUniqueBusinessKeyException {
    }

    @Override
    public void sendMailToCreatedUser(String password, boolean userLocalLdap,
            Marketplace marketplace, PlatformUser pu)
            throws MailOperationException {
    }

    @Override
    public boolean isUserLoggedIn(long userKey) {
        return false;
    }

    @Override
    public void importUsersInOwnOrganization(byte[] csvData,
            String marketplaceId)
            throws BulkUserImportException, ObjectNotFoundException {
    }

    @Override
    public void importUsers(byte[] csvData, String organizationId,
            String marketplaceId) throws BulkUserImportException,
            ObjectNotFoundException, IllegalArgumentException {
    }

    @Override
    public void importUser(VOUserDetails user, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            ObjectNotFoundException {
    }

    @Override
    public boolean isCallerOrganizationAdmin() {
        return false;
    }

    @Override
    public boolean addRevokeUserUnitAssignment(String unitName,
            List<VOUser> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException {
        return false;
    }

    @Override
    public boolean searchLdapUsersOverLimit(String userIdPattern)
            throws ValidationException {
        return false;
    }

    @Override
    public void sendAdministratorNotifyMail(PlatformUser administrator,
            String userId) {

    }

    @Override
    public VOUserDetails createUserWithGroups(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId,
            Map<Long, UnitUserRole> groups)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {
        return null;
    }

    @Override
    public void grantUnitRole(PlatformUser user, UserRoleType role) {
    }

    @Override
    public void revokeUnitRole(PlatformUser user, UserRoleType role)
            throws UserModificationConstraintException {
    }

    @Override
    public void grantUnitRole(VOUser user, UserRoleType role)
            throws ObjectNotFoundException, OperationNotPermittedException {
    }

    @Override
    public void revokeUnitRole(VOUser user, UserRoleType role)
            throws ObjectNotFoundException, OperationNotPermittedException {
    }

    @Override
    public PlatformUser getPlatformUserByOrganization(String userId,
            String orgId) throws ObjectNotFoundException {
        return null;
    }

}
