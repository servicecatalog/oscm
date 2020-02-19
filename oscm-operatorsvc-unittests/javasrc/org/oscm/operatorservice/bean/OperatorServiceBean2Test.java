/**
 * ***************************************************************************** Copyright FUJITSU
 * LIMITED 2018 *****************************************************************************
 */
package org.oscm.operatorservice.bean;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Properties;
import javax.ejb.SessionContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

public class OperatorServiceBean2Test {

  private OperatorServiceBean operatorServiceBean;
  private SessionContext sessionCtxMock;
  private AccountServiceLocal accountServiceMock;
  private DataService ds;
  private ConfigurationServiceLocal configurationServiceLocal;

  @Before
  public void setUp() throws Exception {
    operatorServiceBean = new OperatorServiceBean();
    sessionCtxMock = Mockito.mock(SessionContext.class);
    operatorServiceBean.sessionCtx = sessionCtxMock;
    accountServiceMock = Mockito.mock(AccountServiceLocal.class);
    operatorServiceBean.accMgmt = accountServiceMock;
    ds = mock(DataService.class);
    operatorServiceBean.dm = ds;
    configurationServiceLocal = mock(ConfigurationServiceLocal.class);
    operatorServiceBean.configService = configurationServiceLocal;
  }

  /** BE07787 */
  @Test
  public void registerOrganization_mailServerUnavailable() throws Exception {
    final VOOrganization organization = new VOOrganization();
    organization.setOrganizationId("MyOrg");
    organization.setName("MyOrganization");
    organization.setEmail("asm-ue-test@est.fujitsu.com");
    organization.setPhone("+49894711");
    organization.setUrl("http://www.fujitsu.com");
    organization.setAddress("Schwanthaler Str. 75a  Munich");
    organization.setLocale("en");
    organization.setOperatorRevenueShare(BigDecimal.valueOf(15));

    final VOUserDetails userDetails = new VOUserDetails();
    userDetails.setFirstName("Hans");
    userDetails.setLastName("Meier");
    userDetails.setEMail("asm-ue-test@est.fujitsu.com");
    userDetails.setUserId("admin");
    userDetails.setSalutation(Salutation.MR);
    userDetails.setPhone("(089) 123 456 78");
    userDetails.setLocale("de");

    when(accountServiceMock.registerOrganization(
            any(Organization.class),
            any(ImageResource.class),
            any(VOUserDetails.class),
            any(Properties.class),
            any(String.class),
            any(String.class),
            any(String.class),
            any(OrganizationRoleType.class)))
        .thenThrow(new MailOperationException("Mail cannot be sent"));

    try {
      operatorServiceBean.registerOrganization(
          organization, null, userDetails, null, null, OrganizationRoleType.SUPPLIER);

      fail("MailOperationException expected");
    } catch (MailOperationException e) {
      // There must be a transaction rollback if the
      // mail server is unreachable
      verify(sessionCtxMock, times(1)).setRollbackOnly();
    }
  }
}
