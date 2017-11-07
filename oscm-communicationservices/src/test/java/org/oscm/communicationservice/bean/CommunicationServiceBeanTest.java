/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 03.03.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.communicationservice.bean;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.logging.LoggerFactory;
import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Basic tests of the e-mailing capability.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class CommunicationServiceBeanTest {

    private static final String TO = "mike.jaeger@est.fujitsu.com";
    private static final String INVALID_TO = "guenther.schmid.est.fujitsu.com";

    private CommunicationServiceLocal commSrv;
    private ConfigurationServiceLocal confServ;

    private String baseUrl = "http://localhost:8180/oscm-portal";

    @Before
    public void setup() throws Exception {
        confServ = new ConfigurationServiceStub();

        CommunicationServiceBean cs = new CommunicationServiceBean() {
            @Override
            public String getBaseUrl() {
                return baseUrl;
            }
        };
        cs.confSvc = confServ;
        cs.localizer = new LocalizerServiceStub();

        commSrv = cs;
        LoggerFactory.activateRollingFileAppender("./logs", null, "DEBUG");
    }

    /**
     * Test mail for organization.
     * 
     * @throws SaaSApplicationException
     */
    @Test
    @Ignore
    public void testSendMailToOrganization() throws SaaSApplicationException {
        Organization org = new Organization();
        addOrganizationRole(org, OrganizationRoleType.CUSTOMER);
        Organization supplier = createSupplier();
        OrganizationReference ref = new OrganizationReference(supplier, org,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        supplier.getTargets().add(ref);
        org.getSources().add(ref);
        org.setEmail(TO);
        org.setLocale(Locale.ENGLISH.toString());
        commSrv.sendMail(org, EmailType.ORGANIZATION_DISCOUNT_ADDED, null, null);
    }

    @Test
    @Ignore
    public void testSendMailSimple() throws SaaSApplicationException {
        PlatformUser user = new PlatformUser();
        user.setUserId("TestUser");
        user.setEmail(TO);
        user.setLocale(Locale.ENGLISH.toString());
        user.setOrganization(createSupplier());
        commSrv.sendMail(user, EmailType.ORGANIZATION_UPDATED, null, null);
    }

    @Test
    @Ignore
    public void testSendMailSimpleWithSalutation()
            throws SaaSApplicationException {
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
        OrganizationReference ref = new OrganizationReference(supplier, org,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        supplier.getTargets().add(ref);
        org.getSources().add(ref);
        user.setOrganization(org);
        commSrv.sendMail(user, EmailType.ORGANIZATION_UPDATED, null, null);
    }

    @Test(expected = MailOperationException.class)
    // TODO Mail server not available during unit testing
    @Ignore
    public void testSendMailInvalidAddress() throws MailOperationException {
        PlatformUser user = new PlatformUser();
        user.setUserId("TestUser");
        user.setEmail(INVALID_TO);
        user.setLocale(Locale.ENGLISH.toString());
        commSrv.sendMail(user, EmailType.ORGANIZATION_UPDATED, null, null);
    }

    private Organization createSupplier() {
        Organization supplier = new Organization();
        supplier.setOrganizationId("supplier");
        addOrganizationRole(supplier, OrganizationRoleType.SUPPLIER);
        return supplier;
    }

    private void addOrganizationRole(Organization organization,
            OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole toSupplierRole = new OrganizationToRole();
        toSupplierRole.setOrganization(organization);
        toSupplierRole.setOrganizationRole(role);
        organization.getGrantedRoles().add(toSupplierRole);
    }

    @Test
    @Ignore
    public void testSendMailStatus() {
        String[] values = new String[] { "a", "b" };

        SendMailStatus<String> mailStatus = new SendMailStatus<String>();
        Assert.assertEquals(0, mailStatus.getMailStatus().size());

        mailStatus.addMailStatus(values[0]);
        Assert.assertEquals(1, mailStatus.getMailStatus().size());

        mailStatus.addMailStatus(values[1]);
        Assert.assertEquals(2, mailStatus.getMailStatus().size());

        for (int i = 0; i < mailStatus.getMailStatus().size(); i++) {
            SendMailStatus.SendMailStatusItem<String> ms = mailStatus
                    .getMailStatus().get(i);
            Assert.assertEquals(values[i], ms.getInstance());
            Assert.assertNull(ms.getException());
            Assert.assertFalse(ms.errorOccurred());
        }
    }

    @Test
    @Ignore
    public void testSendMailStatusWithErrors() {
        String[] values = new String[] { "a", "b" };
        Exception[] exceptions = new Exception[] {
                new MailOperationException(), new NullPointerException() };

        SendMailStatus<String> mailStatus = new SendMailStatus<String>();
        Assert.assertEquals(0, mailStatus.getMailStatus().size());

        mailStatus.addMailStatus(values[0], exceptions[0]);
        Assert.assertEquals(1, mailStatus.getMailStatus().size());

        mailStatus.addMailStatus(values[1], exceptions[1]);
        Assert.assertEquals(2, mailStatus.getMailStatus().size());

        for (int i = 0; i < mailStatus.getMailStatus().size(); i++) {
            SendMailStatus.SendMailStatusItem<String> ms = mailStatus
                    .getMailStatus().get(i);
            Assert.assertEquals(values[i], ms.getInstance());
            Assert.assertEquals(exceptions[i].getClass().getName(), ms
                    .getException().getClass().getName());
            Assert.assertTrue(ms.errorOccurred());
        }
    }

    @Test
    @Ignore
    public void testSendMailsToPlatformUsers() {
        sendAndVerify(3, true);
    }

    @Test
    @Ignore
    public void testSendMailsToPlatformUsersWithErrors() {
        sendAndVerify(3, false);
    }

    private void sendAndVerify(int numberPlatformUsers, boolean validEmail) {
        String email = validEmail ? TO : INVALID_TO;

        PlatformUser[] users = new PlatformUser[numberPlatformUsers];
        for (int i = 0; i < users.length; i++) {
            PlatformUser user = new PlatformUser();
            user.setUserId("TestUser");
            user.setEmail(email);
            user.setLocale(Locale.ENGLISH.toString());
            user.setOrganization(createSupplier());
            users[i] = user;
        }

        SendMailStatus<PlatformUser> sendMailStatus = commSrv.sendMail(
                EmailType.ORGANIZATION_UPDATED, null, null, users);
        Assert.assertEquals(users.length, sendMailStatus.getMailStatus().size());

        for (int i = 0; i < sendMailStatus.getMailStatus().size(); i++) {
            SendMailStatus.SendMailStatusItem<PlatformUser> ms = sendMailStatus
                    .getMailStatus().get(i);
            Assert.assertEquals(users[i], ms.getInstance());

            if (validEmail) {
                Assert.assertNull(ms.getException());
            } else {
                Assert.assertEquals(MailOperationException.class.getName(), ms
                        .getException().getClass().getName());
            }
        }
    }

    /**
     * Test send mail with given mail address.
     * 
     * @throws MailOperationException
     */
    @Test
    public void testSendMailToMailAddress() throws MailOperationException,
            ValidationException {
        CommunicationServiceBean lCommSrv = Mockito
                .spy((CommunicationServiceBean) commSrv);
        // Ensure method correctly delegates for mail sending.
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                // Check the arguments
                List<?> arg = (List<?>) args[0];
                Assert.assertEquals(4, args.length);
                Assert.assertTrue(arg.contains(TO));
                String defaultString = "pseudo-stub-return-value";
                Assert.assertEquals(args[1], defaultString);
                Assert.assertEquals(args[2], defaultString);
                Assert.assertEquals(args[3], null);
                return null;
            }
        })
                .when(lCommSrv)
                .sendMail(Matchers.anyListOf(String.class),
                        Matchers.anyString(), Matchers.anyString(),
                        Matchers.anyString());

        Organization org = new Organization();
        addOrganizationRole(org, OrganizationRoleType.CUSTOMER);
        Organization supplier = createSupplier();
        OrganizationReference ref = new OrganizationReference(supplier, org,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        supplier.getTargets().add(ref);
        org.getSources().add(ref);
        org.setEmail(TO);
        org.setLocale(Locale.ENGLISH.toString());
        lCommSrv.sendMail(org.getEmail(), EmailType.SUPPORT_ISSUE, null, null,
                null);
        Mockito.verify(lCommSrv).sendMail(Matchers.anyListOf(String.class),
                Matchers.anyString(), Matchers.anyString(),
                Matchers.anyString());
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

    /**
     * Test get marketplaceUrl
     * 
     * The marketplace is added only if the marketplace id is provided
     * 
     * */
    @Test
    public void getMarketplaceUrl() throws Exception {
        assertEquals(baseUrl + "/marketplace?mId=marketplaceId",
                commSrv.getMarketplaceUrl("marketplaceId"));
        assertEquals(baseUrl, commSrv.getMarketplaceUrl(""));
        assertEquals(baseUrl, commSrv.getMarketplaceUrl(null));
        assertEquals(baseUrl, commSrv.getMarketplaceUrl("     "));
    }

    /**
     * Test send mail to invalid mail address.
     * 
     * @throws SaaSApplicationException
     */
    @Ignore
    @Test(expected = MailOperationException.class)
    public void testSendMailToOrganization_InvalidMailAddress()
            throws SaaSApplicationException {
        Organization org = new Organization();
        addOrganizationRole(org, OrganizationRoleType.CUSTOMER);
        Organization supplier = createSupplier();
        OrganizationReference ref = new OrganizationReference(supplier, org,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        supplier.getTargets().add(ref);
        org.getSources().add(ref);
        org.setEmail(INVALID_TO);
        org.setLocale(Locale.ENGLISH.toString());
        commSrv.sendMail(org.getEmail(), EmailType.SUPPORT_ISSUE, null, null,
                null);
    }
}
