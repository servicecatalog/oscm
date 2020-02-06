/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: weiser                                                   
 *                                                                              
 *  Creation Date: 13.12.2011                                                      
 *                                                                              
 *  Completion Time: 13.12.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.intf.IdentityService;
import org.oscm.string.Strings;
import org.oscm.types.exceptions.BulkUserImportException;
import org.oscm.types.exceptions.BulkUserImportException.Reason;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;

/**
 * Tests for {@link IdentityService} web service.
 *
 * @author weiser
 */
public class IdentityServiceWSPlatformOperatorTest {
  private static WebserviceTestSetup setup;
  private static IdentityService is;
  private static VOOrganization supplier1;

  @BeforeClass
  public static void setUp() throws Exception {
    assumeFalse(ServiceFactory.getDefault().isSSOMode());
    WebserviceTestBase.getOperator().addCurrency("EUR");

    setup = new WebserviceTestSetup();
    supplier1 = setup.createSupplier("Supplier1");
    is = ServiceFactory.getDefault().getIdentityService();
    VOUserDetails userDetails = is.getCurrentUserDetails();	
    userDetails.setEMail("sampleuseremail@fujitsu.com");	
    is.updateUser(userDetails);
  }

  @Test
  public void importUsers() throws Exception {
    // given
    byte[] csvData =
        bytes(
            "user_"
                + System.currentTimeMillis()
                + ","
                + "user1@org.com,en,MR,John,Doe,SERVICE_MANAGER");

    // when
    is.importUsers(csvData, supplier1.getOrganizationId(), "");
  }

  @Test(expected = ObjectNotFoundException.class)
  public void importUsers_InvalidOrganizationId() throws Exception {
    // given
    String orgId = "not_existing_id";
    byte[] csvData =
        bytes(
            "user_"
                + System.currentTimeMillis()
                + ","
                + "user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN");

    // when
    is.importUsers(csvData, orgId, "");
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
