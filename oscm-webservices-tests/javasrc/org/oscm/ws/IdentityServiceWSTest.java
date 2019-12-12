/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Author: weiser
 *
 * <p>Creation Date: 13.12.2011
 *
 * <p>Completion Time: 13.12.2011
 *
 * <p>*****************************************************************************
 */
package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.intf.IdentityService;
import org.oscm.intf.OrganizationalUnitService;
import org.oscm.intf.SessionService;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.enumtypes.Salutation;
import org.oscm.types.enumtypes.UserAccountStatus;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.BulkUserImportException;
import org.oscm.types.exceptions.BulkUserImportException.Reason;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.DomainObjectException.ClassEnum;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.SecurityCheckException;
import org.oscm.types.exceptions.UserActiveException;
import org.oscm.types.exceptions.UserModificationConstraintException;
import org.oscm.types.exceptions.UserRoleAssignmentException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.types.exceptions.ValidationException.ReasonEnum;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationalUnit;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;

/**
 * Tests for {@link IdentityService} web service.
 *
 * @author weiser
 */
public class IdentityServiceWSTest {

  private static final String VERSION = "Version not updated: %s > %s";

  private static WebserviceTestSetup setup;
  private static IdentityService is;
  private static VOFactory factory = new VOFactory();
  private static VOOrganization supplier1;
  private static VOUserDetails supplier2User;
  private static OrganizationalUnitService unitService;

  private static boolean isInternalMode;

  @BeforeClass
  public static void setUp() throws Exception {

    ServiceFactory serviceFactory = ServiceFactory.getDefault();
    isInternalMode = !serviceFactory.isSSOMode();

    WebserviceTestBase.getMailReader().deleteMails();
    WebserviceTestBase.getOperator().addCurrency("EUR");

    String supplierUserId = serviceFactory.getSupplierUserId();
    String supplierPwd = serviceFactory.getSupplierUserPassword();

    supplier1 =
        WebserviceTestBase.createOrganization(
            supplierUserId,
            OrganizationRoleType.TECHNOLOGY_PROVIDER,
            OrganizationRoleType.SUPPLIER);

    String supplierKey =
        WebserviceTestBase.readLastMailAndGetKey(
            supplierUserId, supplierPwd, serviceFactory.isSSOMode());

    is = ServiceFactory.getDefault().getIdentityService(supplierKey, supplierPwd);

    unitService =
        ServiceFactory.getDefault().getOrganizationalUnitService(supplierKey, supplierPwd);
    WebserviceTestBase.getMailReader().deleteMails();
    
    if (isInternalMode) {
      setup = new WebserviceTestSetup();
      setup.createSupplier("Supplier2");
      IdentityService is =
          ServiceFactory.getDefault()
              .getIdentityService(setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD);
      supplier2User = is.getUsersForOrganization().get(0);
    }
  }

  @Test
  public void createUser() throws Exception {
    VOUserDetails u = createUniqueUser();
    u.setAdditionalName("additionalName");
    u.setAddress("address");
    u.setFirstName("firstName");
    u.setLastName("lastName");
    u.setPhone("08154711");
    u.setSalutation(Salutation.MR);
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);

    VOUserDetails read = is.getUserDetails(u);

    validateUser(u, read);
    is.deleteUser(read, null);
  }

  @Test(expected = UserRoleAssignmentException.class)
  public void createUser_WrongRole() throws Exception {
    VOUserDetails u = createUniqueUser();
    try {
      is.createUser(u, Arrays.asList(UserRoleType.PLATFORM_OPERATOR), null);
    } catch (UserRoleAssignmentException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = NonUniqueBusinessKeyException.class)
  public void createUser_NonUniqueUserId() throws Exception {
    VOUserDetails u = createUniqueUser();
    u.setUserId(is.getCurrentUserDetails().getUserId());
    try {
      is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    } catch (NonUniqueBusinessKeyException e) {
      validateException(u.getUserId(), e);
      throw e;
    }
  }

  @Test(expected = ValidationException.class)
  public void createUser_InvalidData() throws Exception {
    VOUserDetails u = createUniqueUser();
    u.setEMail("mail");
    try {
      is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    } catch (ValidationException e) {
      validateException(ReasonEnum.EMAIL, "email", new String[] {u.getEMail()}, e);
      throw e;
    }
  }

  @Test(expected = ObjectNotFoundException.class)
  public void getUser_NotExisting() throws Exception {
    VOUserDetails u = createUniqueUser();
    try {
      is.getUser(u);
    } catch (ObjectNotFoundException e) {
      // find by id
      validateException(u.getUserId(), e);
      throw e;
    }
  }

  @Test(expected = ObjectNotFoundException.class)
  public void getUserDetails_NotExisting() throws Exception {
    VOUserDetails u = createUniqueUser();
    try {
      is.getUserDetails(u);
    } catch (ObjectNotFoundException e) {
      // find by id
      validateException(u.getUserId(), e);
      throw e;
    }
  }

  @Test(expected = OperationNotPermittedException.class)
  public void getUserDetails_NotOwned() throws Exception {
    assumeTrue(isInternalMode);
    try {
      is.getUserDetails(supplier2User);
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test
  public void updateUser() throws Exception {
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    VOUserDetails read = is.getUserDetails(u);
    validateUser(u, read);

    u = read;
    u.setAdditionalName("x");
    u.setAddress("x");
    u.setFirstName("x");
    u.setLastName("x");
    u.setPhone("x");
    u.setSalutation(Salutation.MS);
    read = is.updateUser(u);

    validateUser(u, read);
    is.deleteUser(read, null);
  }

  @Test(expected = ValidationException.class)
  public void updateUser_InvalidData() throws Exception {
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    VOUserDetails read = is.getUserDetails(u);
    read.setEMail("mail");
    try {
      is.updateUser(read);
    } catch (ValidationException e) {
      validateException(ReasonEnum.EMAIL, "email", new String[] {read.getEMail()}, e);
      throw e;
    } finally {
      is.deleteUser(read, null);
    }
  }

  @Test(expected = OperationNotPermittedException.class)
  public void updateUser_NotOwned() throws Exception {
    assumeTrue(isInternalMode);
    try {
      is.updateUser(supplier2User);
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = ObjectNotFoundException.class)
  public void updateUser_NotExisting() throws Exception {
    try {
      is.updateUser(createUniqueUser());
    } catch (ObjectNotFoundException e) {
      // find by key
      validateException("0", e);
      throw e;
    }
  }

  @Test(expected = NonUniqueBusinessKeyException.class)
  public void updateUser_NonUniqueUserId() throws Exception {
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    u = is.getUserDetails(u);
    u.setUserId(is.getCurrentUserDetails().getUserId());
    try {
      is.updateUser(u);
    } catch (NonUniqueBusinessKeyException e) {
      validateException(u.getUserId(), e);
      throw e;
    } finally {
      is.deleteUser(u, null);
    }
  }

  @Test(expected = ConcurrentModificationException.class)
  public void updateUser_Concurrent() throws Exception {
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    u = is.getUserDetails(u);
    u.setAdditionalName("additionalName");
    u = is.updateUser(u);
    u.setVersion(0);
    try {
      is.updateUser(u);
    } catch (ConcurrentModificationException e) {
      validateException(e);
      throw e;
    } finally {
      is.deleteUser(is.getUser(u), null);
    }
  }

  @Test
  public void getAvailableUserRoles() throws Exception {
    List<UserRoleType> roles = is.getAvailableUserRoles(is.getCurrentUserDetails());
    EnumSet<UserRoleType> expected =
        EnumSet.of(
            UserRoleType.ORGANIZATION_ADMIN,
            UserRoleType.SERVICE_MANAGER,
            UserRoleType.TECHNOLOGY_MANAGER,
            UserRoleType.SUBSCRIPTION_MANAGER);
    assertEquals(expected.size(), roles.size());
    assertEquals(expected, EnumSet.copyOf(roles));
  }

  @Test(expected = ObjectNotFoundException.class)
  public void getAvailableUserRoles_NotFound() throws Exception {
    try {
      is.getAvailableUserRoles(createUniqueUser());
    } catch (ObjectNotFoundException e) {
      validateException("0", e);
      throw e;
    }
  }

  @Test(expected = OperationNotPermittedException.class)
  public void getAvailableUserRoles_NotOwned() throws Exception {
    assumeTrue(isInternalMode);
    try {
      is.getAvailableUserRoles(supplier2User);
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test
  public void grantUserRoles() throws Exception {
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.TECHNOLOGY_MANAGER), null);
    u = is.getUserDetails(u);
    EnumSet<UserRoleType> expected =
        EnumSet.of(
            UserRoleType.ORGANIZATION_ADMIN,
            UserRoleType.SERVICE_MANAGER,
            UserRoleType.TECHNOLOGY_MANAGER);
    is.grantUserRoles(u, new ArrayList<UserRoleType>(expected));
    u = is.getUserDetails(u);

    Set<UserRoleType> roles = u.getUserRoles();
    assertEquals(expected.size(), roles.size());
    assertEquals(expected, roles);
    is.deleteUser(u, null);
  }

  @Test(expected = UserRoleAssignmentException.class)
  public void grantUserRoles_WrongRole() throws Exception {
    try {
      is.grantUserRoles(is.getCurrentUserDetails(), Arrays.asList(UserRoleType.PLATFORM_OPERATOR));
    } catch (UserRoleAssignmentException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = ObjectNotFoundException.class)
  public void grantUserRoles_NotExisting() throws Exception {
    VOUserDetails u = createUniqueUser();
    try {
      is.grantUserRoles(u, Arrays.asList(UserRoleType.SERVICE_MANAGER));
    } catch (ObjectNotFoundException e) {
      // find by user id
      validateException(u.getUserId(), e);
      throw e;
    }
  }

  @Test(expected = OperationNotPermittedException.class)
  public void grantUserRoles_NotOwned() throws Exception {
    assumeTrue(isInternalMode);
    try {
      is.grantUserRoles(supplier2User, Arrays.asList(UserRoleType.SERVICE_MANAGER));
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test
  public void setUserRoles() throws Exception {
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    u = is.getUserDetails(u);

    EnumSet<UserRoleType> expected =
        EnumSet.of(UserRoleType.ORGANIZATION_ADMIN, UserRoleType.TECHNOLOGY_MANAGER);
    is.setUserRoles(u, new ArrayList<UserRoleType>(expected));
    u = is.getUserDetails(u);

    Set<UserRoleType> roles = u.getUserRoles();
    assertEquals(expected.size(), roles.size());
    assertEquals(expected, roles);
    is.deleteUser(u, null);
  }

  @Test(expected = ObjectNotFoundException.class)
  public void setUserRoles_NotExisting() throws Exception {
    try {
      is.setUserRoles(createUniqueUser(), Arrays.asList(UserRoleType.SERVICE_MANAGER));
    } catch (ObjectNotFoundException e) {
      // find by user key
      validateException("0", e);
      throw e;
    }
  }

  @Test(expected = OperationNotPermittedException.class)
  public void setUserRoles_NotOwned() throws Exception {
    assumeTrue(isInternalMode);
    try {
      is.setUserRoles(supplier2User, Arrays.asList(UserRoleType.SERVICE_MANAGER));
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = UserModificationConstraintException.class)
  public void setUserRoles_LastAdmin() throws Exception {
    // setting only SERVICE_MANAGER would remove ORGANIZATION_ADMIN
    try {
      is.setUserRoles(is.getCurrentUserDetails(), Arrays.asList(UserRoleType.SERVICE_MANAGER));
    } catch (UserModificationConstraintException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = UserRoleAssignmentException.class)
  public void setUserRoles_WrongRole() throws Exception {
    try {
      is.setUserRoles(
          is.getCurrentUserDetails(),
          Arrays.asList(UserRoleType.ORGANIZATION_ADMIN, UserRoleType.PLATFORM_OPERATOR));
    } catch (UserRoleAssignmentException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = UserActiveException.class)
  public void setUserRoles_ActiveSession() throws Exception {
    assumeTrue(isInternalMode);
    WebserviceTestBase.getMailReader().deleteMails();
    // create another user
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    u = is.getUserDetails(u);
    // read key and reset password
    String userKey = WebserviceTestBase.readLastMailAndSetCommonPassword();
    // get session service ...
    SessionService ss =
        ServiceFactory.getDefault().getSessionService(userKey, WebserviceTestBase.DEFAULT_PASSWORD);
    String sessionId = String.valueOf(System.currentTimeMillis());
    // ... and create a session for the new user
    ss.createPlatformSession(sessionId);
    try {
      is.setUserRoles(u, new ArrayList<UserRoleType>());
    } catch (UserActiveException e) {
      validateException(u.getUserId(), e);
      throw e;
    } finally {
      ss.deletePlatformSession(sessionId);
      is.deleteUser(is.getUser(u), null);
    }
  }

  @Test
  public void revokeUserRoles() throws Exception {
    VOUserDetails u = createUniqueUser();
    is.createUser(
        u,
        Arrays.asList(
            UserRoleType.SERVICE_MANAGER,
            UserRoleType.ORGANIZATION_ADMIN,
            UserRoleType.TECHNOLOGY_MANAGER),
        null);
    u = is.getUserDetails(u);

    is.revokeUserRoles(
        u, Arrays.asList(UserRoleType.ORGANIZATION_ADMIN, UserRoleType.TECHNOLOGY_MANAGER));
    u = is.getUserDetails(u);

    Set<UserRoleType> roles = u.getUserRoles();
    assertEquals(1, roles.size());
    assertEquals(EnumSet.of(UserRoleType.SERVICE_MANAGER), roles);
    is.deleteUser(u, null);
  }

  @Test(expected = ObjectNotFoundException.class)
  public void revokeUserRoles_NotExisting() throws Exception {
    try {
      is.revokeUserRoles(createUniqueUser(), Arrays.asList(UserRoleType.ORGANIZATION_ADMIN));
    } catch (ObjectNotFoundException e) {
      validateException("0", e);
      throw e;
    }
  }

  @Test(expected = OperationNotPermittedException.class)
  public void revokeUserRoles_NotOwned() throws Exception {
    assumeTrue(isInternalMode);
    try {
      is.revokeUserRoles(supplier2User, Arrays.asList(UserRoleType.ORGANIZATION_ADMIN));
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = UserModificationConstraintException.class)
  public void revokeUserRoles_LastAdmin() throws Exception {
    try {
      is.revokeUserRoles(
          is.getCurrentUserDetails(), Arrays.asList(UserRoleType.ORGANIZATION_ADMIN));
    } catch (UserModificationConstraintException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = UserRoleAssignmentException.class)
  public void revokeUserRoles_WrongRole() throws Exception {
    // this looks strange but removing a user role which the user cannot
    // have due to the restrictions of the organization role should throw
    // the exception
    try {
      is.revokeUserRoles(is.getCurrentUserDetails(), Arrays.asList(UserRoleType.PLATFORM_OPERATOR));
    } catch (UserRoleAssignmentException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = UserActiveException.class)
  public void revokeUserRoles_ActiveSession() throws Exception {
    assumeTrue(isInternalMode);
    WebserviceTestBase.getMailReader().deleteMails();
    // create another user
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    u = is.getUserDetails(u);
    // read key and reset password
    String userKey = WebserviceTestBase.readLastMailAndSetCommonPassword();
    // get session service ...
    SessionService ss =
        ServiceFactory.getDefault().getSessionService(userKey, WebserviceTestBase.DEFAULT_PASSWORD);
    String sessionId = String.valueOf(System.currentTimeMillis());
    // ... and create a session for the new user
    ss.createPlatformSession(sessionId);
    try {
      is.revokeUserRoles(u, Arrays.asList(UserRoleType.SERVICE_MANAGER));
    } catch (UserActiveException e) {
      validateException(u.getUserId(), e);
      throw e;
    } finally {
      ss.deletePlatformSession(sessionId);
      is.deleteUser(is.getUser(u), null);
    }
  }

  @Test
  public void lockUserAccount() throws Exception {
    assumeTrue(isInternalMode);
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    u = is.getUserDetails(u);
    assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED, u.getStatus());
    is.lockUserAccount(u, UserAccountStatus.LOCKED, null);
    u = is.getUserDetails(u);
    assertEquals(UserAccountStatus.LOCKED, u.getStatus());
    is.deleteUser(u, null);
  }

  @Test(expected = ObjectNotFoundException.class)
  public void lockUserAccount_NotExisting() throws Exception {
    try {
      is.lockUserAccount(createUniqueUser(), UserAccountStatus.LOCKED, null);
    } catch (ObjectNotFoundException e) {
      validateException("0", e);
      throw e;
    }
  }

  @Test(expected = OperationNotPermittedException.class)
  public void lockUserAccount_NotOwned() throws Exception {
    assumeTrue(isInternalMode);
    try {
      is.lockUserAccount(supplier2User, UserAccountStatus.LOCKED, null);
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = ConcurrentModificationException.class)
  public void lockUserAccount_Concurrent() throws Exception {
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    u = is.getUserDetails(u);
    is.lockUserAccount(u, UserAccountStatus.LOCKED, null);
    try {
      is.lockUserAccount(u, UserAccountStatus.LOCKED, null);
    } catch (ConcurrentModificationException e) {
      validateException(e);
      throw e;
    } finally {
      is.deleteUser(is.getUserDetails(u), null);
    }
  }

  @Test
  public void requestResetOfUserPassword() throws Exception {
    assumeTrue(isInternalMode);
    WebserviceTestBase.getMailReader().deleteMails();
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    WebserviceTestBase.readLastMailAndSetCommonPassword();
    u = is.getUserDetails(u);
    assertEquals(UserAccountStatus.ACTIVE, u.getStatus());
    is.requestResetOfUserPassword(u, null);
    u = is.getUserDetails(u);
    assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED, u.getStatus());
    is.deleteUser(u, null);
  }

  @Test(expected = UserActiveException.class)
  public void requestResetOfUserPassword_ActiveSession() throws Exception {
    assumeTrue(isInternalMode);
    WebserviceTestBase.getMailReader().deleteMails();
    // create another user
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    // read key and reset password
    String userKey = WebserviceTestBase.readLastMailAndSetCommonPassword();
    // get session service ...
    SessionService ss =
        ServiceFactory.getDefault().getSessionService(userKey, WebserviceTestBase.DEFAULT_PASSWORD);
    String sessionId = String.valueOf(System.currentTimeMillis());
    // ... and create a session for the new user
    ss.createPlatformSession(sessionId);
    u = is.getUserDetails(u);
    assertEquals(UserAccountStatus.ACTIVE, u.getStatus());
    try {
      is.requestResetOfUserPassword(u, null);
    } catch (UserActiveException e) {
      validateException(u.getUserId(), e);
      u = is.getUserDetails(u);
      assertEquals(UserAccountStatus.ACTIVE, u.getStatus());
      throw e;
    } finally {
      ss.deletePlatformSession(sessionId);
      is.deleteUser(u, null);
    }
  }

  @Test(expected = ObjectNotFoundException.class)
  public void requestResetOfUserPassword_NotExisting() throws Exception {
    assumeTrue(isInternalMode);
    VOUserDetails u = createUniqueUser();
    try {
      is.requestResetOfUserPassword(u, null);
    } catch (ObjectNotFoundException e) {
      validateException(u.getUserId(), e);
      throw e;
    }
  }

  @Test(expected = OperationNotPermittedException.class)
  public void requestResetOfUserPassword_NotOwned() throws Exception {
    assumeTrue(isInternalMode);
    try {
      is.requestResetOfUserPassword(supplier2User, null);
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = ConcurrentModificationException.class)
  public void requestResetOfUserPassword_Concurrent() throws Exception {
    assumeTrue(isInternalMode);
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    u = is.getUserDetails(u);
    u.setAdditionalName("additionalName");
    u = is.updateUser(u);
    u.setVersion(0);
    try {
      is.requestResetOfUserPassword(u, null);
    } catch (ConcurrentModificationException e) {
      validateException(e);
      throw e;
    } finally {
      is.deleteUser(is.getUser(u), null);
    }
  }

  @Test
  public void unlockUserAccount() throws Exception {
    assumeTrue(isInternalMode);
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    u = is.getUserDetails(u);
    assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED, u.getStatus());
    is.lockUserAccount(u, UserAccountStatus.LOCKED, null);
    u = is.getUserDetails(u);
    assertEquals(UserAccountStatus.LOCKED, u.getStatus());
    is.unlockUserAccount(u, null);
    u = is.getUserDetails(u);
    assertEquals(UserAccountStatus.ACTIVE, u.getStatus());
    is.deleteUser(u, null);
  }

  @Test(expected = ObjectNotFoundException.class)
  public void unlockUserAccount_NotExisting() throws Exception {
    try {
      is.unlockUserAccount(createUniqueUser(), null);
    } catch (ObjectNotFoundException e) {
      validateException("0", e);
      throw e;
    }
  }

  @Test(expected = OperationNotPermittedException.class)
  public void unlockUserAccount_NotOwned() throws Exception {
    assumeTrue(isInternalMode);
    try {
      is.unlockUserAccount(supplier2User, null);
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = ConcurrentModificationException.class)
  public void unlockUserAccount_Concurrent() throws Exception {
    VOUserDetails u = createUniqueUser();
    is.createUser(u, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    u = is.getUserDetails(u);
    is.lockUserAccount(u, UserAccountStatus.LOCKED, null);
    try {
      is.unlockUserAccount(u, null);
    } catch (ConcurrentModificationException e) {
      validateException(e);
      throw e;
    } finally {
      is.deleteUser(is.getUserDetails(u), null);
    }
  }

  @Test(expected = SecurityCheckException.class)
  public void changePassword_WrongCurrent() throws Exception {
    try {
      is.changePassword("wrong", "newPassword");
    } catch (SecurityCheckException e) {
      assertEquals("error.changePassword", e.getMessageKey());
      throw e;
    }
  }

  @Test(expected = ValidationException.class)
  public void changePassword_InvalidToShort() throws Exception {
    String pw = getStringOfLength(5);
    try {
      is.changePassword(WebserviceTestBase.DEFAULT_PASSWORD, pw);
    } catch (ValidationException e) {
      validateException(ReasonEnum.MIN_LENGTH, "newPassword", new String[] {pw, "6"}, e);
      throw e;
    }
  }

  @Test(expected = ValidationException.class)
  public void changePassword_InvalidToLong() throws Exception {
    String pw = getStringOfLength(101);
    try {
      is.changePassword(WebserviceTestBase.DEFAULT_PASSWORD, pw);
    } catch (ValidationException e) {
      validateException(ReasonEnum.LENGTH, "newPassword", new String[] {pw, "100"}, e);
      throw e;
    }
  }

  @Test(expected = ValidationException.class)
  public void changePassword_InvalidEmpty() throws Exception {
    String pw = "";
    try {
      is.changePassword(WebserviceTestBase.DEFAULT_PASSWORD, pw);
    } catch (ValidationException e) {
      validateException(ReasonEnum.MIN_LENGTH, "newPassword", new String[] {pw, "6"}, e);
      throw e;
    }
  }

  @SuppressWarnings("deprecation")
  @Test(expected = ObjectNotFoundException.class)
  public void addRevokeUserGroupAssignment_GroupNotExist() throws Exception {
    List<VOUser> usersToBeAdded = new ArrayList<VOUser>();
    List<VOUser> usersToBeRevoked = new ArrayList<VOUser>();
    String groupName = "groupName";
    try {
      is.addRevokeUserGroupAssignment(groupName, usersToBeAdded, usersToBeRevoked);
    } catch (ObjectNotFoundException e) {
      validateException(e);
      throw e;
    }
  }

  @SuppressWarnings("deprecation")
  @Test(expected = OperationNotPermittedException.class)
  public void addRevokeUserGroupAssignment_DefaultGroup() throws Exception {
    List<VOUser> usersToBeAdded = new ArrayList<VOUser>();
    VOUser user = new VOUser();
    user.setKey(111L);
    usersToBeAdded.add(user);
    List<VOUser> usersToBeRevoked = new ArrayList<VOUser>();
    String groupName = "default";
    try {
      is.addRevokeUserGroupAssignment(groupName, usersToBeAdded, usersToBeRevoked);
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = ObjectNotFoundException.class)
  public void addRevokeUserUnitAssignment_UnitNotExist() throws Exception {
    List<VOUser> usersToBeAdded = new ArrayList<VOUser>();
    List<VOUser> usersToBeRevoked = new ArrayList<VOUser>();
    String unitName = "unitName";
    try {
      is.addRevokeUserUnitAssignment(unitName, usersToBeAdded, usersToBeRevoked);
    } catch (ObjectNotFoundException e) {
      validateException(e);
      throw e;
    }
  }

  @Test(expected = OperationNotPermittedException.class)
  public void addRevokeUserUnitAssignment_DefaultUnit() throws Exception {
    List<VOUser> usersToBeAdded = new ArrayList<VOUser>();
    VOUser user = new VOUser();
    user.setKey(111L);
    usersToBeAdded.add(user);
    List<VOUser> usersToBeRevoked = new ArrayList<VOUser>();
    String unitName = "default";
    try {
      is.addRevokeUserUnitAssignment(unitName, usersToBeAdded, usersToBeRevoked);
    } catch (OperationNotPermittedException e) {
      validateException(e);
      throw e;
    }
  }

  @Test
  public void addRevokeUserUnitAssignment() throws Exception {
    VOUserDetails uniqueUser = createUniqueUser();
    uniqueUser.setFirstName("User");
    is.createUser(uniqueUser, Arrays.asList(UserRoleType.SERVICE_MANAGER), null);
    VOOrganizationalUnit unit = unitService.createUnit("someUnit", "description", "123456");
    List<VOUser> usersToBeAdded = new ArrayList<>();
    usersToBeAdded.add(uniqueUser);
    List<VOUser> usersToBeRevoked = new ArrayList<>();
    boolean result =
        is.addRevokeUserUnitAssignment(unit.getName(), usersToBeAdded, usersToBeRevoked);
    assertTrue(result);
  }

  protected static void validateException(ConcurrentModificationException e) {
    assertEquals("ex.ConcurrentModificationException", e.getMessageKey());
  }

  protected static void validateException(ObjectNotFoundException e) {
    assertEquals("ex.ObjectNotFoundException.USER_GROUP", e.getMessageKey());
  }

  protected static void validateException(String userId, UserActiveException e) {
    assertEquals("ex.UserActiveException", e.getMessageKey());
    String[] params = e.getMessageParams();
    assertNotNull(params);
    assertEquals(1, params.length);
    assertEquals(userId, params[0]);
  }

  protected static void validateException(UserModificationConstraintException e) {
    assertEquals(UserModificationConstraintException.Reason.LAST_ADMIN, e.getReason());
    assertEquals("ex.UserModificationConstraintException.LAST_ADMIN", e.getMessageKey());
  }

  protected static void validateException(String param, ObjectNotFoundException e) {
    assertEquals(ClassEnum.USER, e.getDomainObjectClassEnum());
    String[] params = e.getMessageParams();
    assertNotNull(params);
    assertEquals(1, params.length);
    assertEquals(param, params[0]);
    assertEquals("ex.ObjectNotFoundException.USER", e.getMessageKey());
  }

  protected static void validateException(
      ReasonEnum reason, String field, String[] values, ValidationException e) {
    assertEquals(reason, e.getFaultInfo().getReason());
    assertEquals(field, e.getFaultInfo().getMember());
    String[] params = e.getMessageParams();
    assertNotNull(params);
    assertEquals(values.length, params.length);
    for (int i = 0; i < values.length; i++) {
      assertEquals(values[i], params[i]);
    }
    assertEquals("ex.ValidationException." + reason.name(), e.getMessageKey());
  }

  protected static void validateException(String userId, NonUniqueBusinessKeyException e) {
    assertEquals(ClassEnum.USER, e.getDomainObjectClassEnum());
    String[] params = e.getMessageParams();
    assertNotNull(params);
    assertEquals(1, params.length);
    assertEquals(userId, params[0]);
    assertEquals("ex.NonUniqueBusinessKeyException.USER", e.getMessageKey());
  }

  protected static void validateException(UserRoleAssignmentException e) {
    String[] params = e.getMessageParams();
    assertNotNull(params);
    assertEquals(2, params.length);
    assertEquals(OrganizationRoleType.PLATFORM_OPERATOR.name(), params[0]);
    assertEquals(UserRoleType.PLATFORM_OPERATOR.name(), params[1]);
    assertEquals("ex.UserRoleAssignmentException", e.getMessageKey());
  }

  protected static void validateException(OperationNotPermittedException e) {
    assertEquals("ex.OperationNotPermittedException", e.getMessageKey());
  }

  protected static VOUserDetails createUniqueUser() throws Exception {
    VOUserDetails u = factory.createUserVO(Long.toHexString(System.currentTimeMillis()));
    u.setOrganizationId(supplier1.getOrganizationId());
    return u;
  }

  protected static void validateUser(VOUserDetails before, VOUserDetails after) {
    assertEquals(before.getAdditionalName(), after.getAdditionalName());
    assertEquals(before.getAddress(), after.getAddress());
    assertEquals(before.getEMail(), after.getEMail());
    assertEquals(before.getFirstName(), after.getFirstName());
    assertEquals(before.getLastName(), after.getLastName());
    assertEquals(before.getLocale(), after.getLocale());
    assertEquals(before.getPhone(), after.getPhone());
    assertEquals(before.getSalutation(), after.getSalutation());
    assertEquals(before.getUserId(), after.getUserId());
    assertEquals(EnumSet.of(UserRoleType.SERVICE_MANAGER), after.getUserRoles());
    assertTrue(after.getKey() > 0);
    if (before.getKey() > 0) {
      // update case
      assertEquals(before.getKey(), after.getKey());
      // versions are not different on creation as the initial version
      // value is 0 so only compare versions after updates...
      assertTrue(
          String.format(
              VERSION, String.valueOf(after.getVersion()), String.valueOf(before.getVersion())),
          after.getVersion() > before.getVersion());
    }
  }

  protected static String getStringOfLength(int length) {
    StringBuffer b = new StringBuffer(length);
    for (int i = 0; i < length; i++) {
      int codePoint = 97 + (int) (Math.random() * 26);
      char[] chars = Character.toChars(codePoint);
      b.append(chars);
    }
    return b.toString();
  }

  @Ignore
  public void importUsersInOwnOrganization() throws Exception {
    // given
    String userId = "importedUser_" + System.currentTimeMillis();
    byte[] csvData = bytes(userId + "," + "user1@org.com,en,MR,John,Doe,SERVICE_MANAGER");

    // when
    is.importUsersInOwnOrganization(csvData, "");

    // than
    String lastMailContent =
        WebserviceTestBase.getMailReader()
            .getLastMailContentWithSubject("Bulk user import finished");
    assertNotNull(lastMailContent);
    assertTrue(lastMailContent.indexOf("1 out of 1") > 0);
  }

  @Test
  public void importUsersInOwnOrganization_InvalidUserData() throws Exception {
    // given data with missing user id
    byte[] csvData = bytes(",user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN");

    // when
    try {
      is.importUsersInOwnOrganization(csvData, "");
    } catch (BulkUserImportException e) {
      validateException(e, BulkUserImportException.Reason.MISSING_USERID);
    }
  }

  @Test
  public void importUsers_InsufficientAuthorityRights() throws Throwable {
    // given user not platform operator
    byte[] csvData =
        bytes(
            "user_"
                + System.currentTimeMillis()
                + ","
                + "user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN");

    // when
    try {
      is.importUsers(csvData, supplier1.getOrganizationId(), "");
    } catch (Exception e) {
      assertTrue(e.getMessage().indexOf("Unauthorized") > 0);
    }
  }

  void validateException(BulkUserImportException e, Reason reason) {
    assertEquals(
        "ex.BulkUserImportException." + e.getFaultInfo().getReason().name(), e.getMessageKey());
    assertEquals(reason, e.getFaultInfo().getReason());
  }

  private byte[] bytes(String value) {
    return Strings.toBytes(value);
  }
}
