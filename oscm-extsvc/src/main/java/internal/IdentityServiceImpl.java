/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package internal;

import java.util.Collections;
import java.util.List;

import javax.jws.WebService;

import org.oscm.intf.IdentityService;
import org.oscm.types.enumtypes.UserAccountStatus;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.BulkUserImportException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.UserActiveException;
import org.oscm.types.exceptions.UserModificationConstraintException;
import org.oscm.types.exceptions.UserRoleAssignmentException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * This is a stub implementation of the {@link IdentityService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Aleh Khomich
 */
@WebService(serviceName = "IdentityService", targetNamespace = "http://oscm.org/xsd", portName = "IdentityServicePort", endpointInterface = "org.oscm.intf.IdentityService")
public class IdentityServiceImpl implements IdentityService {

    @Override
    public void grantUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revokeUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException,
            UserModificationConstraintException, UserActiveException,
            OperationNotPermittedException, UserRoleAssignmentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails createUser(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void confirmAccount(VOUser user, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUser(VOUser user, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUser getUser(VOUser user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails getUserDetails(VOUser user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails getCurrentUserDetails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails getCurrentUserDetailsIfPresent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOUserDetails> getUsersForOrganization() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void importLdapUsers(List<VOUserDetails> users, String marketplaceId)
            throws MailOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lockUserAccount(VOUser userToBeLocked,
            UserAccountStatus newStatus, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails updateUser(VOUserDetails userDetails) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifyOnLoginAttempt(VOUser user, boolean attemptSuccessful)
            throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void requestResetOfUserPassword(VOUser user, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOUserDetails> searchLdapUsers(String userIdPattern)
            throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendAccounts(String email, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unlockUserAccount(VOUser user, String marketplaceId) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public VOUserDetails createOnBehalfUser(String organizationId, String string)
            throws ObjectNotFoundException, OperationNotPermittedException,
            NonUniqueBusinessKeyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanUpCurrentUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshLdapUser() throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void importUsersInOwnOrganization(byte[] csvData,
            String marketplaceId) throws BulkUserImportException,
            ObjectNotFoundException {

    }

    @Override
    public void importUsers(byte[] csvData, String organizationId,
            String marketplaceId) throws BulkUserImportException,
            ObjectNotFoundException {
    }

    @Override
    public boolean addRevokeUserGroupAssignment(String groupName,
            List<VOUser> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException {
        return false;
    }

    @Override
    public boolean addRevokeUserUnitAssignment(String unitName,
            List<VOUser> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException {
        return false;
    }
}
