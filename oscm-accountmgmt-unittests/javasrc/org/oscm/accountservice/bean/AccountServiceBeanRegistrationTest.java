package org.oscm.accountservice.bean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.VOUserDetails;

import java.util.Properties;

import static com.ibm.icu.impl.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceBeanRegistrationTest {

  @Mock private ImageResource imageResource;
  @Mock private VOUserDetails user;
  @Mock private Properties organizationProperties;
  @InjectMocks private AccountServiceBean accountServiceBean = new AccountServiceBean();

  private Organization organization;

  @Before
  public void setUp() {
          organization  = new Organization();
          organization.setOrganizationId("orgId");
  }
  @Test
  public void
      shouldThrowAnException_whenRegisteringTheOrganization_givenOrganizationAlreadyExists() {
//
//    try {
//      accountServiceBean.registerOrganization(
//          organization,
//          imageResource,
//          user,
//          organizationProperties,
//          "country",
//          "mId",
//          "description");
//    } catch (NonUniqueBusinessKeyException e) {
//      // No handling here. Exception is expected
//    } catch (ValidationException
//        | MailOperationException
//        | ObjectNotFoundException
//        | IncompatibleRolesException
//        | OrganizationAuthorityException e) {
//      fail(e);
//    }
  }
}
