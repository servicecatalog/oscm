/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.webservices;

import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.intf.IdentityService;
import org.oscm.types.enumtypes.UserAccountStatus;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.BulkUserImportException;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.OrganizationRemovedException;
import org.oscm.types.exceptions.SaaSSystemException;
import org.oscm.types.exceptions.SecurityCheckException;
import org.oscm.types.exceptions.TechnicalServiceNotAliveException;
import org.oscm.types.exceptions.TechnicalServiceOperationException;
import org.oscm.types.exceptions.UserActiveException;
import org.oscm.types.exceptions.UserDeletionConstraintException;
import org.oscm.types.exceptions.UserModificationConstraintException;
import org.oscm.types.exceptions.UserRoleAssignmentException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * End point facade for WS.
 * 
 * @author Aleh Khomich.
 * 
 */
@WebService(name = "IdentityService", serviceName = "IdentityService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.IdentityService")
public class IdentityServiceWS implements IdentityService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(IdentityServiceWS.class));

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(IdentityServiceWS.class);
    org.oscm.internal.intf.IdentityService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public VOUserDetails createUser(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(
                    delegate.createUser(VOConverter.convertToUp(user),
                            EnumConverter.convertList(roles,
                                    org.oscm.internal.types.enumtypes.UserRoleType.class),
                            marketplaceId));
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserRoleAssignmentException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void changePassword(String oldPassword, String newPassword)
            throws SecurityCheckException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.changePassword(oldPassword, newPassword);
        } catch (org.oscm.internal.types.exception.SecurityCheckException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void confirmAccount(VOUser user, String marketplaceId)
            throws OperationNotPermittedException, ObjectNotFoundException,
            MailOperationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.confirmAccount(VOConverter.convertToUp(user),
                    marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void grantUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.grantUserRoles(VOConverter.convertToUp(user),
                    EnumConverter.convertList(roles,
                            org.oscm.internal.types.enumtypes.UserRoleType.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserRoleAssignmentException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deleteUser(VOUser user, String marketplaceId)
            throws UserDeletionConstraintException, ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deleteUser(VOConverter.convertToUp(user), marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserDeletionConstraintException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            SaaSSystemException se = new SaaSSystemException(e);
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_CONVERT_TO_RUNTIME_EXCEPTION_FOR_COMPATIBILITY);
            throw se;
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            SaaSSystemException se = new SaaSSystemException(e);
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_CONVERT_TO_RUNTIME_EXCEPTION_FOR_COMPATIBILITY);
            throw se;
        }
    }

    @Override
    public void revokeUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, UserModificationConstraintException,
            UserActiveException, OperationNotPermittedException,
            UserRoleAssignmentException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.revokeUserRoles(VOConverter.convertToUp(user),
                    EnumConverter.convertList(roles,
                            org.oscm.internal.types.enumtypes.UserRoleType.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserModificationConstraintException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserActiveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserRoleAssignmentException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOUserDetails getCurrentUserDetails() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOConverter.convertToApi(delegate.getCurrentUserDetails());
    }

    @Override
    public VOUserDetails getCurrentUserDetailsIfPresent() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOConverter
                .convertToApi(delegate.getCurrentUserDetailsIfPresent());
    }

    @Override
    public VOUser getUser(VOUser user) throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationRemovedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(
                    delegate.getUser(VOConverter.convertToUp(user)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationRemovedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOUserDetails getUserDetails(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(
                    delegate.getUserDetails(VOConverter.convertToUp(user)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOUserDetails> getUsersForOrganization() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getUsersForOrganization(),
                org.oscm.vo.VOUserDetails.class);
    }

    @Override
    public void importLdapUsers(List<VOUserDetails> users, String marketplaceId)
            throws NonUniqueBusinessKeyException, ValidationException,
            MailOperationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.importLdapUsers(
                    VOCollectionConverter.convertList(users,
                            org.oscm.internal.vo.VOUserDetails.class),
                    marketplaceId);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void lockUserAccount(VOUser userToBeLocked,
            UserAccountStatus newStatus, String marketplaceId)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.lockUserAccount(VOConverter.convertToUp(userToBeLocked),
                    EnumConverter.convert(newStatus,
                            org.oscm.internal.types.enumtypes.UserAccountStatus.class),
                    marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOUserDetails updateUser(VOUserDetails userDetails)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ValidationException, NonUniqueBusinessKeyException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(
                    delegate.updateUser(VOConverter.convertToUp(userDetails)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void notifyOnLoginAttempt(VOUser user, boolean attemptSuccessful)
            throws ObjectNotFoundException, SecurityCheckException,
            ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.notifyOnLoginAttempt(VOConverter.convertToUp(user),
                    attemptSuccessful);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SecurityCheckException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void requestResetOfUserPassword(VOUser user, String marketplaceId)
            throws MailOperationException, ObjectNotFoundException,
            OperationNotPermittedException, UserActiveException,
            ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.requestResetOfUserPassword(VOConverter.convertToUp(user),
                    marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserActiveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOUserDetails> searchLdapUsers(String userIdPattern)
            throws ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.searchLdapUsers(userIdPattern),
                    org.oscm.vo.VOUserDetails.class);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void sendAccounts(String email, String marketplaceId)
            throws ValidationException, MailOperationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.sendAccounts(email, marketplaceId);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void unlockUserAccount(VOUser user, String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.unlockUserAccount(VOConverter.convertToUp(user),
                    marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<UserRoleType> getAvailableUserRoles(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return EnumConverter.convertList(
                    delegate.getAvailableUserRoles(
                            VOConverter.convertToUp(user)),
                    org.oscm.types.enumtypes.UserRoleType.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOUserDetails createOnBehalfUser(String organizationId,
            String string) throws ObjectNotFoundException,
            OperationNotPermittedException, NonUniqueBusinessKeyException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(
                    delegate.createOnBehalfUser(organizationId, string));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void cleanUpCurrentUser() {
        WS_LOGGER.logAccess(wsContext, ds);
        delegate.cleanUpCurrentUser();
    }

    @Override
    public void refreshLdapUser() throws ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.refreshLdapUser();
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void setUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserModificationConstraintException, UserRoleAssignmentException,
            UserActiveException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.setUserRoles(VOConverter.convertToUp(user),
                    EnumConverter.convertList(roles,
                            org.oscm.internal.types.enumtypes.UserRoleType.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserModificationConstraintException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserRoleAssignmentException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserActiveException e) {
            throw ExceptionConverter.convertToApi(e);
        }

    }

    @Override
    public void importUsersInOwnOrganization(byte[] csvData,
            String marketplaceId)
            throws BulkUserImportException, ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.importUsersInOwnOrganization(csvData, marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.BulkUserImportException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void importUsers(byte[] csvData, String organizationId,
            String marketplaceId)
            throws BulkUserImportException, ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.importUsers(csvData, organizationId, marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.BulkUserImportException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public boolean addRevokeUserGroupAssignment(String groupName,
            List<VOUser> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException {
        return addRevokeUserUnitAssignment(groupName, usersToBeAdded,
                usersToBeRevoked);
    }

    @Override
    public boolean addRevokeUserUnitAssignment(String unitName,
            List<VOUser> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.addRevokeUserUnitAssignment(unitName,
                    VOCollectionConverter.convertList(usersToBeAdded,
                            org.oscm.internal.vo.VOUser.class),
                    VOCollectionConverter.convertList(usersToBeRevoked,
                            org.oscm.internal.vo.VOUser.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }
}
