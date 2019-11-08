package org.oscm.ws;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.intf.IdentityService;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;

public class IdentityServiceTest {

  private static IdentityService identityService;

  @BeforeClass
  public static void setUp() throws Exception {
    identityService = ServiceFactory.getDefault().getIdentityService();
  }

  @Test
  public void testGetCurrentUserDetails() throws Exception {

    // given
    String userId = ServiceFactory.getDefault().getDefaultUserId();
    String userKey = ServiceFactory.getDefault().getDefaultUserKey();

    // when

    VOUserDetails userDetails = identityService.getCurrentUserDetails();

    // then
    assertEquals(userId, userDetails.getUserId());
    assertEquals(userKey, userDetails.getKey());
  }
}
