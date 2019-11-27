package org.oscm.ws;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.intf.IdentityService;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;

public class IdentityServiceTest {

  @BeforeClass
  public static void setUp() throws Exception {

    WebserviceTestBase.getMailReader().deleteMails();
    WebserviceTestBase.getOperator().addCurrency("EUR");
  }

  @Test
  public void testGetCurrentUserDetails() throws Exception {

    // given
    String userId = ServiceFactory.getDefault().getDefaultUserId();
    String userKey = ServiceFactory.getDefault().getDefaultUserKey();

    IdentityService defaultIdService = ServiceFactory.getDefault().getIdentityService();

    // when
    VOUserDetails userDetails = defaultIdService.getCurrentUserDetails();

    // then
    assertEquals(userId, userDetails.getUserId());
    assertEquals(Long.parseLong(userKey), userDetails.getKey());
  }

  @Test
  public void testGetUsersForOrganization() throws Exception {

    // given
	ServiceFactory serviceFactory = ServiceFactory.getDefault();
    String supplierUserId = serviceFactory.getSupplierUserId();
    String supplierPwd = serviceFactory.getSupplierUserPassword();
    
    WebserviceTestBase.createOrganization(
        supplierUserId, OrganizationRoleType.TECHNOLOGY_PROVIDER, OrganizationRoleType.SUPPLIER);

    String supplierKey = WebserviceTestBase.readLastMailAndGetKey(supplierUserId, supplierPwd, serviceFactory.isSSOMode());

    IdentityService identityService =
        ServiceFactory.getDefault().getIdentityService(supplierKey, supplierPwd);

    // when
    List<VOUserDetails> users = identityService.getUsersForOrganization();

    // then
    assertEquals(1, users.size());
    assertEquals(supplierUserId, users.get(0).getUserId());
  }
}
