/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 23.08.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Properties;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.test.setup.PropertiesReader;
import org.oscm.ws.base.CertificateHandler;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.intf.AccountService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOTechnicalService;

/**
 * Tests a service method that uses authorization checks via annotations in a WS
 * call using certificate based authentication.
 * 
 * @author Mike J&auml;ger
 */
public class ClientCertWithAuthorizationWSTest {

    private static ServiceFactory factory;
    private static CertificateHandler certHandler;
    private static VOOrganization organization;

    private static Properties props;

    @BeforeClass
    public static void staticSetup() throws Exception {
        PropertiesReader reader = new PropertiesReader();
        props = reader.load();
        certHandler = new CertificateHandler();

        // enable debug
        // System.setProperty("javax.net.debug", "ssl");

        // add system properties for client certificate authentication
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStore",
                props.getProperty("rootca.keystore"));
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
    }

    @AfterClass
    public static void cleanUp() {
        // delete all by tests created certificates
        certHandler.deleteCreatedCerts();
    }

    @Before
    public void setUp() throws Exception {
        // create organization with initial user
        organization = WebserviceTestBase.createOrganization(
                "adminId" + System.currentTimeMillis(),
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        // create a new client certificate for the organization
        certHandler.createSignedClientCertificate(organization
                .getOrganizationId());

        // update organization's dn
        WebserviceTestBase.getOperator().setDistinguishedName(
                organization.getOrganizationId(),
                "CN=" + organization.getOrganizationId());

        factory = ServiceFactory.getDefault();

        // enable certificate authentication, still user and pwd should be given
        // as null values (e.g. "factory.getAccountService(null, null);")
        factory.setUseCertAuth(true);

        System.setProperty("javax.net.ssl.keyStore",
                props.getProperty("certificates.path") + File.separator
                        + "keystore-" + organization.getOrganizationId()
                        + ".jks");

        System.out.println("JavaSystemProperties:");
        for (Object key : System.getProperties().keySet()) {
            if (((String) key).startsWith("javax")) {
                System.out.println("     " + key + ": "
                        + System.getProperty((String) key));
            }
        }
    }

    @Test
    public void clientCertLogin() throws Exception {
        // requires authorization
        factory.getServiceProvisioningService(null, null);
    }

    @Test(expected = SOAPFaultException.class)
    public void createTechnicalService() throws Exception {
        ServiceProvisioningService sps = factory.getServiceProvisioningService(
                null, null);

        // fails because client certificate authentication is mapped to
        // organization admin role only, and this call requires
        // TECHNOLOGY_MANAGER role
        sps.createTechnicalService(new VOTechnicalService());
    }

    /**
     * Reason for Ignore: The JVM initializes the javax.net.ssl.keyStore only
     * once and caches it for all test classes (see
     * defaultprojectbuild.xml/webservicetests: < junit fork="yes"
     * forkmode="once" ).
     */
    @Test
    @Ignore
    public void getOrganizationData() throws Exception {
        AccountService accountService = factory.getAccountService(null, null);
        assertNotNull(accountService.getOrganizationData());
    }

}
