/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 03.03.2009
 *
 * <p>*****************************************************************************
 */
package org.oscm.communicationservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.oscm.communicationservice.Constants.MAIL_SMTP_HOST;
import static org.oscm.communicationservice.Constants.TENANT_ID;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.smtp.SMTPAuthenticator;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerServiceStub;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.logging.LoggerFactory;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.enumtypes.EmailType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

/**
 * Basic tests of the e-mailing capability.
 *
 * @author Mike J&auml;ger
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*", "jdk.internal.reflect.*"})
@PrepareForTest({Session.class})
public class CommunicationServiceBeanTest {

  private static final String TO = "mike.jaeger@est.fujitsu.com";
  private static final String INVALID_TO = "guenther.schmid.est.fujitsu.com";

  private CommunicationServiceBean commSrv;
  private ConfigurationServiceLocal confServ;

  private String baseUrl = "http://localhost:8180/oscm-portal";
  private String baseUrlHttps = "https://localhost:8180/oscm-portal";
  @Captor ArgumentCaptor<String> userName, password;

  @Captor ArgumentCaptor<Properties> sessionProps;

  private Properties properties;
  private InternetAddress internetAddress;

  private InitialContext ics;

  @Before
  public void setup() throws Exception {
    confServ = new ConfigurationServiceStub();
    properties = new Properties();
    commSrv = spy(new CommunicationServiceBean());
    doReturn(baseUrl).when(commSrv).getBaseUrl();
    doReturn(baseUrlHttps).when(commSrv).getBaseUrlHttps();
    doNothing().when(commSrv).sendMessage(any());
    ics = mock(InitialContext.class);
    internetAddress = mock(InternetAddress.class);
    doReturn(internetAddress).when(commSrv).newInternetAddress(any());
    Session sm = mock(Session.class);
    doReturn(properties).when(sm).getProperties();
    when(sm.getProperty(anyString()))
        .thenAnswer(
            new Answer<String>() {

              @Override
              public String answer(InvocationOnMock invocation) throws Throwable {
                String key = (String) invocation.getArguments()[0];
                return properties.getProperty(key);
              }
            });
    doReturn(sm)
        .when(commSrv)
        .newAuthenticatedSession(sessionProps.capture(), userName.capture(), password.capture());
    doReturn(ics).when(commSrv).newInitialContext();
    doReturn(sm).when(ics).lookup(anyString());

    doReturn(sm).when(ics).lookup(anyString());

    commSrv.confSvc = confServ;
    commSrv.localizer = new LocalizerServiceStub();

    LoggerFactory.activateRollingFileAppender("./logs", null, "DEBUG");
  }

  @Test
  public void testSendMailToOrganization() throws SaaSApplicationException {
    // given
    Organization org = new Organization();
    addOrganizationRole(org, OrganizationRoleType.CUSTOMER);
    Organization supplier = createSupplier();
    OrganizationReference ref =
        new OrganizationReference(supplier, org, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
    supplier.getTargets().add(ref);
    org.getSources().add(ref);
    org.setEmail(TO);
    org.setLocale(Locale.ENGLISH.toString());

    // when
    commSrv.sendMail(org, EmailType.ORGANIZATION_DISCOUNT_ADDED, null, null);

    // then no exception
  }

  @Test
  public void testSendMailSimple() throws SaaSApplicationException {
    // given
    PlatformUser user = new PlatformUser();
    user.setUserId("TestUser");
    user.setEmail(TO);
    user.setLocale(Locale.ENGLISH.toString());
    user.setOrganization(createSupplier());

    // when
    commSrv.sendMail(user, EmailType.ORGANIZATION_UPDATED, null, null);

    // then no exception
  }

  @Test
  public void sendMailOrganizations() throws SaaSApplicationException {
    // given
    Organization sup = createSupplier();
    PlatformUser user = new PlatformUser();
    user.setUserId("TestUser");
    user.setEmail(TO);
    user.setLocale(Locale.ENGLISH.toString());
    user.setOrganization(sup);

    // when
    commSrv.sendMail(EmailType.ORGANIZATION_UPDATED, null, null, sup);

    // then no exception
  }

  @Test
  public void testSendMailNoRecipient() throws SaaSApplicationException {
    // given
    PlatformUser user = new PlatformUser();
    user.setUserId("TestUser");
    user.setEmail(TO);
    user.setLocale(Locale.ENGLISH.toString());
    user.setOrganization(createSupplier());

    // when
    commSrv.sendMail(null, user, EmailType.ORGANIZATION_UPDATED, null, null);

    // then no exception
  }

  @Test
  public void testSendMailSimpleWithSalutation() throws Exception {
    // given
    PlatformUser user = new PlatformUser();
    user.setUserId("TestUser");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setEmail(TO);
    user.setLocale(Locale.ENGLISH.toString());
    user.setSalutation(Salutation.MR);
    Organization org = new Organization();
    addOrganizationRole(org, OrganizationRoleType.CUSTOMER);
    Organization supplier = createSupplier();
    OrganizationReference ref =
        new OrganizationReference(supplier, org, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
    supplier.getTargets().add(ref);
    org.getSources().add(ref);
    user.setOrganization(org);
    whenNew(InternetAddress.class).withAnyArguments().thenReturn(internetAddress);

    // when
    commSrv.sendMail(user, EmailType.ORGANIZATION_UPDATED, null, null);

    // then no exception
  }

  @Test(expected = MailOperationException.class)
  public void testSendMailInvalidAddress() throws MailOperationException, AddressException {
    // given
    givenInvalidMailAddress();

    PlatformUser user = new PlatformUser();
    user.setUserId("TestUser");
    user.setEmail(INVALID_TO);
    user.setLocale(Locale.ENGLISH.toString());
    // when
    commSrv.sendMail(user, EmailType.ORGANIZATION_UPDATED, null, null);
  }

  private Organization createSupplier() {
    Organization supplier = new Organization();
    supplier.setOrganizationId("supplier");
    addOrganizationRole(supplier, OrganizationRoleType.SUPPLIER);
    return supplier;
  }

  private void addOrganizationRole(Organization organization, OrganizationRoleType roleType) {
    OrganizationRole role = new OrganizationRole();
    role.setRoleName(roleType);
    OrganizationToRole toSupplierRole = new OrganizationToRole();
    toSupplierRole.setOrganization(organization);
    toSupplierRole.setOrganizationRole(role);
    organization.getGrantedRoles().add(toSupplierRole);
  }

  @Test
  public void testSendMailStatus() {
    String[] values = new String[] {"a", "b"};

    SendMailStatus<String> mailStatus = new SendMailStatus<String>();
    assertEquals(0, mailStatus.getMailStatus().size());

    mailStatus.addMailStatus(values[0]);
    assertEquals(1, mailStatus.getMailStatus().size());

    mailStatus.addMailStatus(values[1]);
    assertEquals(2, mailStatus.getMailStatus().size());

    for (int i = 0; i < mailStatus.getMailStatus().size(); i++) {
      SendMailStatus.SendMailStatusItem<String> ms = mailStatus.getMailStatus().get(i);
      assertEquals(values[i], ms.getInstance());
      Assert.assertNull(ms.getException());
      Assert.assertFalse(ms.errorOccurred());
    }
  }

  @Test
  public void testSendMailStatusWithErrors() {
    String[] values = new String[] {"a", "b"};
    Exception[] exceptions =
        new Exception[] {new MailOperationException(), new NullPointerException()};

    SendMailStatus<String> mailStatus = new SendMailStatus<String>();
    assertEquals(0, mailStatus.getMailStatus().size());

    mailStatus.addMailStatus(values[0], exceptions[0]);
    assertEquals(1, mailStatus.getMailStatus().size());

    mailStatus.addMailStatus(values[1], exceptions[1]);
    assertEquals(2, mailStatus.getMailStatus().size());

    for (int i = 0; i < mailStatus.getMailStatus().size(); i++) {
      SendMailStatus.SendMailStatusItem<String> ms = mailStatus.getMailStatus().get(i);
      assertEquals(values[i], ms.getInstance());
      assertEquals(exceptions[i].getClass().getName(), ms.getException().getClass().getName());
      Assert.assertTrue(ms.errorOccurred());
    }
  }

  @Test
  public void testSendMailsToPlatformUsers() throws Exception {
    sendAndVerify(3, true);
  }

  @Test
  public void testSendMailsToPlatformUsersWithErrors() throws Exception {
    sendAndVerify(3, false);
  }

  private void sendAndVerify(int numberPlatformUsers, boolean validEmail) throws Exception {
    String email = validEmail ? TO : givenInvalidMailAddress();

    PlatformUser[] users = new PlatformUser[numberPlatformUsers];
    for (int i = 0; i < users.length; i++) {
      PlatformUser user = new PlatformUser();
      user.setUserId("TestUser");
      user.setEmail(email);
      user.setLocale(Locale.ENGLISH.toString());
      user.setOrganization(createSupplier());
      users[i] = user;
    }

    SendMailStatus<PlatformUser> sendMailStatus =
        commSrv.sendMail(EmailType.ORGANIZATION_UPDATED, null, null, users);
    assertEquals(users.length, sendMailStatus.getMailStatus().size());

    for (int i = 0; i < sendMailStatus.getMailStatus().size(); i++) {
      SendMailStatus.SendMailStatusItem<PlatformUser> ms = sendMailStatus.getMailStatus().get(i);
      assertEquals(users[i], ms.getInstance());

      if (validEmail) {
        Assert.assertNull(ms.getException());
      } else {
        assertEquals(
            MailOperationException.class.getName(), ms.getException().getClass().getName());
      }
    }
  }

  /**
   * Test send mail with given mail address.
   *
   * @throws MailOperationException
   */
  @Test
  public void testSendMailToMailAddress() throws Exception, ValidationException {
    CommunicationServiceBean cb = (CommunicationServiceBean) commSrv;
    // Ensure method correctly delegates for mail sending.
    doAnswer(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                // Check the arguments
                List<?> arg = (List<?>) args[0];
                assertEquals(4, args.length);
                Assert.assertTrue(arg.contains(TO));
                String defaultString = "pseudo-stub-return-value";
                assertEquals(args[1], defaultString);
                assertEquals(args[2], defaultString);
                assertEquals(args[3], "en");
                return null;
              }
            })
        .when(cb)
        .sendMail(
            Matchers.anyListOf(String.class),
            Matchers.anyString(),
            Matchers.anyString(),
            Matchers.anyString());

    Organization org = new Organization();
    addOrganizationRole(org, OrganizationRoleType.CUSTOMER);
    Organization supplier = createSupplier();
    OrganizationReference ref =
        new OrganizationReference(supplier, org, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
    supplier.getTargets().add(ref);
    org.getSources().add(ref);
    org.setEmail(TO);
    org.setLocale(Locale.ENGLISH.toString());

    PowerMockito.whenNew(InternetAddress.class).withAnyArguments().thenReturn(internetAddress);

    // when
    commSrv.sendMail(org.getEmail(), EmailType.SUPPORT_ISSUE, null, null, "en");
    // then
    verify(commSrv)
        .sendMail(
            Matchers.anyListOf(String.class), Matchers.anyString(), Matchers.anyString(), eq("en"));
  }

  @Test(expected = ValidationException.class)
  public void testSendMailToMailAddress_InvalidMailAddress()
      throws MailOperationException, ValidationException {
    commSrv.sendMail(INVALID_TO, EmailType.SUPPORT_ISSUE, null, null, null);
  }

  @Test(expected = ValidationException.class)
  public void testSendMailToMailAddress_NullMailAddress()
      throws MailOperationException, ValidationException {
    commSrv.sendMail(null, EmailType.SUPPORT_ISSUE, null, null, null);
  }

  @Test
  public void getMarketplaceUrl() throws Exception {
    assertEquals(
        baseUrl + "/marketplace?mId=marketplaceId", commSrv.getMarketplaceUrl("marketplaceId"));
    assertEquals(baseUrl, commSrv.getMarketplaceUrl(""));
    assertEquals(baseUrl, commSrv.getMarketplaceUrl(null));
    assertEquals(baseUrl, commSrv.getMarketplaceUrl("     "));
  }

  @Test
  public void getMarketplaceUrlHttps() throws Exception {
    assertEquals(
        baseUrlHttps + "/marketplace?mId=marketplaceId",
        commSrv.getMarketplaceUrlHttps("marketplaceId"));
  }

  @Test
  public void getBaseUrlWithTenant() throws Exception {
    assertEquals(baseUrl + "?" + TENANT_ID + "=tenant1", commSrv.getBaseUrlWithTenant("tenant1"));
  }

  @Test
  public void getBaseUrlHttpsWithTenant() throws Exception {
    assertEquals(
        baseUrlHttps + "?" + TENANT_ID + "=tenant1", commSrv.getBaseUrlHttpsWithTenant("tenant1"));
  }

  @Test
  public void lookup() throws Exception {
    // given
    properties.put("mail.transport.protocol", "ftp");

    // when
    Session ms = ((CommunicationServiceBean) commSrv).lookupMailResource();

    // then
    assertNotNull(ms);
  }

  @Test(expected = ValidationException.class)
  public void testSendMailToOrganization_InvalidMailAddress() throws Exception {
    // given
    Organization org = new Organization();
    addOrganizationRole(org, OrganizationRoleType.CUSTOMER);
    Organization supplier = createSupplier();
    OrganizationReference ref =
        new OrganizationReference(supplier, org, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
    supplier.getTargets().add(ref);
    org.getSources().add(ref);
    org.setEmail(givenInvalidMailAddress());
    org.setLocale(Locale.ENGLISH.toString());

    // when
    commSrv.sendMail(org.getEmail(), EmailType.SUPPORT_ISSUE, null, null, null);

    // then Exception
  }

  @Test
  public void isMailMock_yes() {
    // given
    givenMailMockUsed();

    // when
    boolean rc = commSrv.isMailMock();

    // then
    assertEquals(Boolean.TRUE, Boolean.valueOf(rc));
  }

  @Test
  public void isMailMock_no() {
    // given
    givenAnySMTPServer();

    // when
    boolean rc = commSrv.isMailMock();

    // then
    assertEquals(Boolean.FALSE, Boolean.valueOf(rc));
  }

  @Test
  public void lookupSMTPProperties() {
    // given

    properties.setProperty("mail.transport.protocol", "smtp");
    // when
    boolean rc = commSrv.isMailMock();

    // then
    assertEquals(Boolean.FALSE, Boolean.valueOf(rc));
  }

  @Test(expected = SaaSSystemException.class)
  public void lookupMailResource_Failed() throws Exception {

    // given
    properties.setProperty("mail.transport.protocol", "smtp");
    doThrow(new NamingException()).when(ics).lookup(anyString());

    // when
    ((CommunicationServiceBean) commSrv).lookupMailResource();

    // then Exception
  }

  @Test
  public void createSMTPAuthenticator() throws Exception {

    // given
    Authenticator auth = spy(SMTPAuthenticator.getInstance("test", "password"));

    // when
    PasswordAuthentication pa = Whitebox.invokeMethod(auth, "getPasswordAuthentication");

    // then
    assertEquals("password", pa.getPassword());
    assertEquals("test", pa.getUserName());
  }

  private void givenMailMockUsed() {
    properties.setProperty(MAIL_SMTP_HOST, "oscm-maildev");
  }

  private void givenAnySMTPServer() {
    properties.setProperty(MAIL_SMTP_HOST, "smtp.mydomain.com");
  }

  protected String givenInvalidMailAddress() throws AddressException {
    doThrow(new AddressException("")).when(commSrv).newInternetAddress(eq(INVALID_TO));
    return INVALID_TO;
  }
}
