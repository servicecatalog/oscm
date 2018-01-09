/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015年1月23日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationtests.apiversioning.ws;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.oscm.integrationtests.apiversioning.client.CTMGClient;
import org.oscm.integrationtests.apiversioning.factory.ServiceFactory;
import org.oscm.integrationtests.apiversioning.factory.VOFactory;
import org.oscm.intf.IdentityService;
import org.oscm.types.enumtypes.UserAccountStatus;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.OrganizationRemovedException;
import org.oscm.types.exceptions.UserActiveException;
import org.oscm.types.exceptions.UserDeletionConstraintException;
import org.oscm.types.exceptions.UserModificationConstraintException;
import org.oscm.types.exceptions.UserRoleAssignmentException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * Client of IdentityService
 */
public class IdentityServiceClient {

    private static final String wsdlFile = "/IdentityService.wsdl";
    private IdentityService idServ;

    public IdentityServiceClient(String userKey, String password) {
        URL wsdlURL = CTMGClient.class.getResource(wsdlFile);
        ServiceFactory factory = new ServiceFactory(userKey, password, wsdlURL);
        try {
            idServ = factory.getBESWebService(IdentityService.class);
        } catch (ParserConfigurationException e) {
            System.err.println("Connect to OSCM failed, reason: "
                    + e.getMessage());
        }
    }

    public VOUserDetails getUserDetails() {
        return idServ.getCurrentUserDetails();
    }

    public VOUserDetails createUser(String organizationId) {
        VOUserDetails user = VOFactory.createVOUserDetails(organizationId);
        try {
            return idServ.createUser(user,
                    Arrays.asList(UserRoleType.MARKETPLACE_OWNER), null);
        } catch (NonUniqueBusinessKeyException | MailOperationException
                | ValidationException | UserRoleAssignmentException
                | OperationPendingException e) {
            System.err.println("Create user " + user.getUserId()
                    + " faild. Reason: " + e.getMessage());
            return null;
        }
    }

    public void deleteUser(VOUser user) {
        try {
            idServ.deleteUser(user, null);
        } catch (ObjectNotFoundException | UserDeletionConstraintException
                | ConcurrentModificationException
                | OperationNotPermittedException e) {
            System.err.println("Delete user " + user.getUserId()
                    + " faild. Reason: " + e.getMessage());
        }
    }

    public void grantUserRoles(String userId, UserRoleType role) {
        VOUser user = VOFactory.createVOUser(userId);
        try {
            idServ.grantUserRoles(user, Arrays.asList(role));
        } catch (ObjectNotFoundException | OperationNotPermittedException
                | UserRoleAssignmentException e) {
            System.err.println("Grant role " + role.toString() + " for user "
                    + userId + " faild. Reason: " + e.getMessage());
        }
    }

    public void revokeUserRoles(long userKey, UserRoleType role) {
        VOUser user = VOFactory.createVOUserWithKey(userKey);
        try {
            idServ.revokeUserRoles(user, Arrays.asList(role));
        } catch (ObjectNotFoundException | OperationNotPermittedException
                | UserRoleAssignmentException
                | UserModificationConstraintException | UserActiveException e) {
            System.err.println("Revoke role " + role.toString() + " from user "
                    + userKey + " faild. Reason: " + e.getMessage());
        }
    }

    public void lockUserAccount(VOUser user) {
        try {
            idServ.lockUserAccount(user, UserAccountStatus.LOCKED, null);
        } catch (ObjectNotFoundException | OperationNotPermittedException
                | ConcurrentModificationException e) {
            System.err.println("Lock user " + user.getUserId()
                    + " faild. Reason: " + e.getMessage());
        }
    }

    public void unlockUserAccount(VOUser user) {
        try {
            idServ.unlockUserAccount(user, null);
        } catch (ObjectNotFoundException | OperationNotPermittedException
                | ConcurrentModificationException e) {
            System.err.println("Lock user " + user.getUserId()
                    + " faild. Reason: " + e.getMessage());
        }
    }

    public List<VOUserDetails> getUsersForOrganization() {
        return idServ.getUsersForOrganization();
    }

    public VOUser getVOUser(String userId) {
        VOUser user = VOFactory.createVOUser(userId);
        try {
            return idServ.getUser(user);
        } catch (ObjectNotFoundException | OperationNotPermittedException
                | OrganizationRemovedException e) {
            System.err.println("Get user information failed, reason: "
                    + e.getMessage());
            return null;
        }
    }

    public VOUser refreshUserValue(VOUser user) {
        try {
            return idServ.getUser(user);
        } catch (ObjectNotFoundException | OperationNotPermittedException
                | OrganizationRemovedException e) {
            System.err.println("Get user information failed, reason: "
                    + e.getMessage());
            return null;
        }
    }
}
