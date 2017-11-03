/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 09.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jms.*;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.junit.Ignore;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.PropertiesLoader;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.logging.LoggerFactory;
import org.oscm.setup.DefaultConfigFileCreator;
import org.oscm.test.data.BillingAdapters;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.data.UserRoles;
import org.oscm.test.ejb.FifoJMSQueue;
import org.oscm.test.ejb.TestNamingContext;
import org.oscm.test.ejb.TestNamingContextFactoryBuilder;
import org.oscm.test.ejb.TestPersistence;
import org.oscm.test.stubs.ObjectMessageStub;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.PlatformEventIdentifier;

/**
 * Base class for all Container Tests
 * <nl>
 * <li>Bootstraps openEJB</li>
 * <li>Deploys transactionBean for transactional calls</li>
 * </nl>
 * 
 */
@Ignore
public class BaseAdmUmTest {

    public static final String GLOBAL_MARKETPLACE_NAME = "FUJITSU";

    public static final String INVOICE = PaymentType.INVOICE;
    public static final String DIRECT_DEBIT = PaymentType.DIRECT_DEBIT;
    public static final String CREDIT_CARD = PaymentType.CREDIT_CARD;

    public static final String HS_SEARCH_LISTENERS = "hibernate.search.autoregister_listeners";

    public static final String[] PAYMENT_TYPE_IDS = new String[] { INVOICE,
            DIRECT_DEBIT, CREDIT_CARD };
    public static final String[] PAYMENT_TYPE_IDS_INV_CC = new String[] {
            INVOICE, CREDIT_CARD };
    public static final String[] PAYMENT_TYPE_IDS_INV_DD = new String[] {
            INVOICE, DIRECT_DEBIT };
    public static final String[] PAYMENT_TYPE_IDS_CC_DD = new String[] {
            CREDIT_CARD, DIRECT_DEBIT };
    public static final String[] PAYMENT_TYPE_IDS_INV = new String[] { INVOICE };
    public static final String[] PAYMENT_TYPE_IDS_CC = new String[] { CREDIT_CARD };
    public static final String[] PAYMENT_TYPE_IDS_DD = new String[] { DIRECT_DEBIT };

    protected static final TestPersistence PERSISTENCE = new TestPersistence();

    protected static final String ROLE_ORGANIZATION_ADMIN = UserRoleType.ORGANIZATION_ADMIN
            .name();
    protected static final String ROLE_PLATFORM_OPERATOR = UserRoleType.PLATFORM_OPERATOR
            .name();
    protected static final String ROLE_TECHNOLOGY_MANAGER = UserRoleType.TECHNOLOGY_MANAGER
            .name();
    protected static final String ROLE_SERVICE_MANAGER = UserRoleType.SERVICE_MANAGER
            .name();
    protected static final String ROLE_MARKETPLACE_OWNER = UserRoleType.MARKETPLACE_OWNER
            .name();
    protected static final String ROLE_SUBSCRIPTION_MANAGER = UserRoleType.SUBSCRIPTION_MANAGER
            .name();
    protected static final String ROLE_BROKER_MANAGER = UserRoleType.BROKER_MANAGER
            .name();
    protected static final String ROLE_RESELLER_MANAGER = UserRoleType.RESELLER_MANAGER
            .name();
    protected static final String ROLE_UNIT_ADMINISTRATOR = UserRoleType.UNIT_ADMINISTRATOR
            .name();

    public static final String TOO_LONG_ID = "id_123456789_123456789_123456789_12345678";
    public static final String TOO_LONG_NAME = "name_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789" + "_123456789_123456";
    public static final String TOO_LONG_DESCRIPTION = "desription_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789"
            + "_123456789_123456789_123456789_123456789" + "_12345";

    protected static String SERVICE_EVENT_FILE_UPLOAD = "FILE_UPLOAD";

    private static final String TARGET_NAMESPACE_TECHNICAL_SERVICES = "xmlns:tns=\"oscm.serviceprovisioning/1.9/TechnicalService.xsd\"";

    private static final String TARGET_NAMESPACE_MARKETABLE_SERVICES = "xmlns:mns=\"oscm.serviceprovisioning/1.9/MarketableService.xsd\"";

    protected static final String TECHNICAL_SERVICES_XML = "<tns:TechnicalServices "
            + TARGET_NAMESPACE_TECHNICAL_SERVICES
            + ">"
            + "<tns:TechnicalService id=\"example\" \n"
            + " accessType=\"LOGIN\"\n"
            + " baseUrl=\"http://estadmue:8089/example-dev/\"\n"
            + " provisioningType=\"SYNCHRONOUS\"\n"
            + " provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\"\n"
            + " provisioningVersion=\"1.0\"\n"
            + " loginPath=\"\\login/\"\n"
            + " billingIdentifier=\"NATIVE_BILLING\"\n"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "<LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">LocalizedDescriptionDE</LocalizedDescription>"
            + "<LocalizedDescription locale=\"jp\">LocalizedDescriptionJP</LocalizedDescription>"
            + "<LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>"
            + "<LocalizedLicense locale=\"de\">LocalizedLicenseDeutsch</LocalizedLicense>"
            + " <ParameterDefinition id=\"MAX_FILE_NUMBER\" valueType=\"INTEGER\" minValue=\"1\" "
            + "maxValue=\"10\" default=\"5\">"
            + "  <LocalizedDescription locale=\"en\">MAX_FILE_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"HAS_OPTIONS\" valueType=\"ENUMERATION\" mandatory=\"false\" configurable=\"true\" "
            + " default=\"2\"> "
            + "<Options>"
            + "<Option id=\"1\">"
            + "<LocalizedOption  locale=\"en\">Minimum Storage</LocalizedOption>"
            + "</Option>"
            + "<Option id=\"2\">"
            + "<LocalizedOption  locale=\"en\">Optimum storage</LocalizedOption>"
            + "</Option>"
            + "<Option id=\"3\"> "
            + "<LocalizedOption  locale=\"en\">Maximum storage</LocalizedOption>"
            + "</Option>"
            + "</Options>"
            + "<LocalizedDescription locale=\"en\">"
            + "MEMORY_STORAGE"
            + "</LocalizedDescription>"
            + "</ParameterDefinition>"
            + " <ParameterDefinition id=\"MAX_FOLDER_NUMBER\" valueType=\"INTEGER\" mandatory=\"false\" configurable=\"true\" minValue=\"12\" "
            + "  maxValue=\"500\" default=\"200\">"
            + "   <LocalizedDescription locale=\"en\">MAX_FOLDER_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"FOLDER_NEW\" valueType=\"INTEGER\" mandatory=\"false\" configurable=\"true\" minValue=\"12\" "
            + "maxValue=\"500\" default=\"200\">"
            + "   <LocalizedDescription locale=\"en\">FOLDER_NEW</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"BOOLEAN_PARAMETER\" valueType=\"BOOLEAN\" default=\"\">"
            + "  <LocalizedDescription locale=\"en\">BOOLEAN_PARAMETER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"STRING_PARAMETER\" valueType=\"STRING\" default=\"\">"
            + "  <LocalizedDescription locale=\"en\">STRING_PARAMETER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"LONG_NUMBER\" valueType=\"LONG\" minValue=\"1\" "
            + "maxValue=\"40000000000\" default=\"250\">"
            + "  <LocalizedDescription locale=\"en\">LONG_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <Event id=\"FILE_DOWNLOAD\">"
            + "   <LocalizedDescription locale=\"en\">FILE_DOWNLOAD</LocalizedDescription>"
            + " </Event>"
            + " <Event id=\"FILE_UPLOAD\">"
            + "   <LocalizedDescription locale=\"en\">FILE_UPLOAD</LocalizedDescription>"
            + " </Event>"
            + " <Event id=\"FOLDER_NEW\">"
            + "   <LocalizedDescription locale=\"en\">FOLDER_NEW</LocalizedDescription>"
            + " </Event>"
            + "<Role id=\"ADMIN\">"
            + "<LocalizedName locale=\"en\">Administrator</LocalizedName>"
            + "<LocalizedName locale=\"de\">Administrator</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Administrators have full access to all data entities and can execute administartive tasks such as role assignments and user creation.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Administratoren haben vollen Datenzugriff und können administartive Aufgaben erledigen wie Rollen zuweisen oder Benutzer anlegen.</LocalizedDescription>"
            + "</Role><Role id=\"USER\">"
            + "<LocalizedName locale=\"en\">User</LocalizedName>"
            + "<LocalizedName locale=\"de\">Benutzer</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Users have full access to all data entities but cannot execute adminstartive tasks.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Benutzer haben vollen Datenzugriff aber können keine administrativen Aufgaben erledigen.</LocalizedDescription>"
            + "</Role><Role id=\"GUEST\">"
            + "<LocalizedName locale=\"en\">Guest</LocalizedName>"
            + "<LocalizedName locale=\"de\">Gast</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Guests only have limited read access.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Gäste haben nur eingeschränkten Lesezugriff.</LocalizedDescription>"
            + " </Role>"
            + "<Operation id=\"BACKUP\"  actionURL=\"http://backupurl\">"
            + "<LocalizedName locale=\"en\">Backup</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Performs a backup operation for the subscription data.</LocalizedDescription>"
            + "<OperationParameter id=\"COMMENT\" mandatory=\"true\" type=\"INPUT_STRING\">"
            + "<LocalizedName locale=\"en\">Comment</LocalizedName>"
            + "</OperationParameter>"
            + "</Operation>"
            + "<Operation id=\"CLEANUP\" actionURL=\"http://cleanupurl\">"
            + "<LocalizedName locale=\"en\">Cleanup</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Deletes temporary data and removes data marked as deleted.</LocalizedDescription>"
            + "</Operation>"
            + "</tns:TechnicalService>"

            + " <tns:TechnicalService id=\"ssh\" build=\"1000\" "
            + "  provisioningType=\"SYNCHRONOUS\" "
            + " provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\" "
            + " provisioningVersion=\"1.0\" accessType=\"DIRECT\" baseUrl=\"\" billingIdentifier=\"NATIVE_BILLING\">"
            + " <AccessInfo locale=\"en\">"
            + " Please open a ssh client and connect to the server estadmue.dyn.lan.est.fujitsu.de. Use your EST userId and password for the log in."
            + " </AccessInfo>"
            + " <LocalizedDescription locale=\"de\">"
            + " SSH ermöglicht eine sichere, authentifizierte und verschlüsselte Verbindung zwischen zwei Rechnern über ein unsicheres Netzwerk. Dadurch dient es unter anderem als Ersatz für die Vorgänger rlogin, telnet und rsh; diese übertragen jeglichen Netzverkehr, darunter auch die Passwörter, unverschlüsselt."
            + " </LocalizedDescription>"
            + " <LocalizedDescription locale=\"en\">"
            + " SSH is a protocol that can be used for many applications. Some of the applications below may require features that are only available or compatible with specific SSH clients or servers. For example, using the SSH protocol to implement a VPN is possible, but presently only with the OpenSSH server and client implementation.    </LocalizedDescription>"
            + " <LocalizedLicense locale=\"en\">"
            + " &lt;b&gt;License Agreement&lt;/b&gt;&lt;br/&gt;&lt;br/&gt;"
            + " </LocalizedLicense>"
            + " <LocalizedTag locale=\"en\">"
            + " storage"
            + " </LocalizedTag>"
            + " <LocalizedTag locale=\"de\">"
            + " speicher"
            + " </LocalizedTag>"
            + " </tns:TechnicalService>"

            + " <tns:TechnicalService id=\"saml\" build=\"1000\" "
            + " billingIdentifier=\"NATIVE_BILLING\" "
            + " provisioningType=\"SYNCHRONOUS\" "
            + " provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\" "
            + " provisioningVersion=\"1.0\" accessType=\"USER\">"
            + " <AccessInfo locale=\"en\">"
            + " Please open a ssh client and connect to the server estadmue.dyn.lan.est.fujitsu.de. Use your EST userId and password for the log in."
            + " </AccessInfo>"
            + " <LocalizedDescription locale=\"de\">"
            + " SSH ermöglicht eine sichere, authentifizierte und verschlüsselte Verbindung zwischen zwei Rechnern über ein unsicheres Netzwerk. Dadurch dient es unter anderem als Ersatz für die Vorgänger rlogin, telnet und rsh; diese übertragen jeglichen Netzverkehr, darunter auch die Passwörter, unverschlüsselt."
            + " </LocalizedDescription>"
            + " <LocalizedDescription locale=\"en\">"
            + " SSH is a protocol that can be used for many applications. Some of the applications below may require features that are only available or compatible with specific SSH clients or servers. For example, using the SSH protocol to implement a VPN is possible, but presently only with the OpenSSH server and client implementation.    </LocalizedDescription>"
            + " <LocalizedLicense locale=\"en\">"
            + " &lt;b&gt;License Agreement&lt;/b&gt;&lt;br/&gt;&lt;br/&gt;"
            + " </LocalizedLicense>"
            + " <LocalizedTag locale=\"en\">"
            + " storage"
            + " </LocalizedTag>"
            + " <LocalizedTag locale=\"de\">"
            + " speicher"
            + " </LocalizedTag>"
            + " </tns:TechnicalService>"
            + "</tns:TechnicalServices>";

    protected static final String TECHNICAL_SERVICES_ASYNC_XML = "<tns:TechnicalServices "
            + TARGET_NAMESPACE_TECHNICAL_SERVICES
            + ">"
            + "<tns:TechnicalService id=\"exampleAsync\" \n"
            + " accessType=\"LOGIN\"\n"
            + " baseUrl=\"http://estadmue:8089/example-dev/\"\n"
            + " provisioningType=\"ASYNCHRONOUS\"\n"
            + " provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\"\n"
            + " provisioningVersion=\"1.0\"\n"
            + " loginPath=\"\\login/\"\n"
            + " billingIdentifier=\"NATIVE_BILLING\"\n"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "<LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">LocalizedDescriptionDE</LocalizedDescription>"
            + "<LocalizedDescription locale=\"jp\">LocalizedDescriptionJP</LocalizedDescription>"
            + "<LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>"
            + "<LocalizedLicense locale=\"de\">LocalizedLicenseDeutsch</LocalizedLicense>"
            + " <ParameterDefinition id=\"MAX_FILE_NUMBER\" valueType=\"INTEGER\" minValue=\"1\" "
            + "maxValue=\"10\" default=\"5\">"
            + "  <LocalizedDescription locale=\"en\">MAX_FILE_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"HAS_OPTIONS\" valueType=\"ENUMERATION\" mandatory=\"false\" configurable=\"true\" "
            + " default=\"2\"> "
            + "<Options>"
            + "<Option id=\"1\">"
            + "<LocalizedOption  locale=\"en\">Minimum Storage</LocalizedOption>"
            + "</Option>"
            + "<Option id=\"2\">"
            + "<LocalizedOption  locale=\"en\">Optimum storage</LocalizedOption>"
            + "</Option>"
            + "<Option id=\"3\"> "
            + "<LocalizedOption  locale=\"en\">Maximum storage</LocalizedOption>"
            + "</Option>"
            + "</Options>"
            + "<LocalizedDescription locale=\"en\">"
            + "MEMORY_STORAGE"
            + "</LocalizedDescription>"
            + "</ParameterDefinition>"
            + " <ParameterDefinition id=\"MAX_FOLDER_NUMBER\" valueType=\"INTEGER\" mandatory=\"false\" configurable=\"true\" minValue=\"12\" "
            + "  maxValue=\"500\" default=\"200\">"
            + "   <LocalizedDescription locale=\"en\">MAX_FOLDER_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"FOLDER_NEW\" valueType=\"INTEGER\" mandatory=\"false\" configurable=\"true\" minValue=\"12\" "
            + "maxValue=\"500\" default=\"200\">"
            + "   <LocalizedDescription locale=\"en\">FOLDER_NEW</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"BOOLEAN_PARAMETER\" valueType=\"BOOLEAN\" default=\"\">"
            + "  <LocalizedDescription locale=\"en\">BOOLEAN_PARAMETER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"STRING_PARAMETER\" valueType=\"STRING\" default=\"\">"
            + "  <LocalizedDescription locale=\"en\">STRING_PARAMETER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"LONG_NUMBER\" valueType=\"LONG\" minValue=\"1\" "
            + "maxValue=\"40000000000\" default=\"250\">"
            + "  <LocalizedDescription locale=\"en\">LONG_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <Event id=\"FILE_DOWNLOAD\">"
            + "   <LocalizedDescription locale=\"en\">FILE_DOWNLOAD</LocalizedDescription>"
            + " </Event>"
            + " <Event id=\"FILE_UPLOAD\">"
            + "   <LocalizedDescription locale=\"en\">FILE_UPLOAD</LocalizedDescription>"
            + " </Event>"
            + " <Event id=\"FOLDER_NEW\">"
            + "   <LocalizedDescription locale=\"en\">FOLDER_NEW</LocalizedDescription>"
            + " </Event>"
            + "<Role id=\"ADMIN\">"
            + "<LocalizedName locale=\"en\">Administrator</LocalizedName>"
            + "<LocalizedName locale=\"de\">Administrator</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Administrators have full access to all data entities and can execute administartive tasks such as role assignments and user creation.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Administratoren haben vollen Datenzugriff und können administartive Aufgaben erledigen wie Rollen zuweisen oder Benutzer anlegen.</LocalizedDescription>"
            + "</Role><Role id=\"USER\">"
            + "<LocalizedName locale=\"en\">User</LocalizedName>"
            + "<LocalizedName locale=\"de\">Benutzer</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Users have full access to all data entities but cannot execute adminstartive tasks.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Benutzer haben vollen Datenzugriff aber können keine administrativen Aufgaben erledigen.</LocalizedDescription>"
            + "</Role><Role id=\"GUEST\">"
            + "<LocalizedName locale=\"en\">Guest</LocalizedName>"
            + "<LocalizedName locale=\"de\">Gast</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Guests only have limited read access.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Gäste haben nur eingeschränkten Lesezugriff.</LocalizedDescription>"
            + " </Role>"
            + " </tns:TechnicalService>"
            + "</tns:TechnicalServices>";

    protected static final String TECHNICAL_SERVICES_WITH_MANDATORY_PARAM_XML = "<tns:TechnicalServices "
            + TARGET_NAMESPACE_TECHNICAL_SERVICES
            + ">"
            + "<tns:TechnicalService id=\"example\" \n"
            + " accessType=\"LOGIN\"\n"
            + " baseUrl=\"http://estadmue:8089/example-dev/\"\n"
            + " provisioningType=\"SYNCHRONOUS\"\n"
            + " provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\"\n"
            + " provisioningVersion=\"1.0\"\n"
            + " loginPath=\"\\login/\"\n"
            + " billingIdentifier=\"NATIVE_BILLING\"\n"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "<LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">LocalizedDescriptionDE</LocalizedDescription>"
            + "<LocalizedDescription locale=\"jp\">LocalizedDescriptionJP</LocalizedDescription>"
            + "<LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>"
            + "<LocalizedLicense locale=\"de\">LocalizedLicenseDeutsch</LocalizedLicense>"
            + " <ParameterDefinition id=\"MAX_FILE_NUMBER\" valueType=\"INTEGER\" minValue=\"1\" "
            + "maxValue=\"10\" default=\"5\">"
            + "  <LocalizedDescription locale=\"en\">MAX_FILE_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"HAS_OPTIONS\" valueType=\"ENUMERATION\" mandatory=\"false\" configurable=\"true\" "
            + " default=\"2\"> "
            + "<Options>"
            + "<Option id=\"1\">"
            + "<LocalizedOption  locale=\"en\">Minimum Storage</LocalizedOption>"
            + "</Option>"
            + "<Option id=\"2\">"
            + "<LocalizedOption  locale=\"en\">Optimum storage</LocalizedOption>"
            + "</Option>"
            + "<Option id=\"3\"> "
            + "<LocalizedOption  locale=\"en\">Maximum storage</LocalizedOption>"
            + "</Option>"
            + "</Options>"
            + "<LocalizedDescription locale=\"en\">"
            + "MEMORY_STORAGE"
            + "</LocalizedDescription>"
            + "</ParameterDefinition>"
            + " <ParameterDefinition id=\"MAX_FOLDER_NUMBER\" valueType=\"INTEGER\" mandatory=\"true\" configurable=\"true\" minValue=\"12\" "
            + "  maxValue=\"500\" default=\"200\">"
            + "   <LocalizedDescription locale=\"en\">MAX_FOLDER_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"FOLDER_NEW\" valueType=\"INTEGER\" mandatory=\"false\" configurable=\"true\" minValue=\"12\" "
            + "maxValue=\"500\" default=\"200\">"
            + "   <LocalizedDescription locale=\"en\">FOLDER_NEW</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"BOOLEAN_PARAMETER\" valueType=\"BOOLEAN\" default=\"\">"
            + "  <LocalizedDescription locale=\"en\">BOOLEAN_PARAMETER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"STRING_PARAMETER\" valueType=\"STRING\" default=\"\">"
            + "  <LocalizedDescription locale=\"en\">STRING_PARAMETER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <Event id=\"FILE_DOWNLOAD\">"
            + "   <LocalizedDescription locale=\"en\">FILE_DOWNLOAD</LocalizedDescription>"
            + " </Event>"
            + " <Event id=\"FILE_UPLOAD\">"
            + "   <LocalizedDescription locale=\"en\">FILE_UPLOAD</LocalizedDescription>"
            + " </Event>"
            + " <Event id=\"FOLDER_NEW\">"
            + "   <LocalizedDescription locale=\"en\">FOLDER_NEW</LocalizedDescription>"
            + " </Event>"
            + "<Role id=\"ADMIN\">"
            + "<LocalizedName locale=\"en\">Administrator</LocalizedName>"
            + "<LocalizedName locale=\"de\">Administrator</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Administrators have full access to all data entities and can execute administartive tasks such as role assignments and user creation.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Administratoren haben vollen Datenzugriff und können administartive Aufgaben erledigen wie Rollen zuweisen oder Benutzer anlegen.</LocalizedDescription>"
            + "</Role><Role id=\"USER\">"
            + "<LocalizedName locale=\"en\">User</LocalizedName>"
            + "<LocalizedName locale=\"de\">Benutzer</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Users have full access to all data entities but cannot execute adminstartive tasks.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Benutzer haben vollen Datenzugriff aber können keine administrativen Aufgaben erledigen.</LocalizedDescription>"
            + "</Role><Role id=\"GUEST\">"
            + "<LocalizedName locale=\"en\">Guest</LocalizedName>"
            + "<LocalizedName locale=\"de\">Gast</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Guests only have limited read access.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Gäste haben nur eingeschränkten Lesezugriff.</LocalizedDescription>"
            + " </Role>"
            + "<Operation id=\"BACKUP\" actionURL=\"http://backupurl\">"
            + "<LocalizedName locale=\"en\">Backup</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Performs a backup operation for the subscription data.</LocalizedDescription>"
            + "</Operation>"
            + "<Operation id=\"CLEANUP\" actionURL=\"http://cleanupurl\">"
            + "<LocalizedName locale=\"en\">Cleanup</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Deletes temporary data and removes data marked as deleted.</LocalizedDescription>"
            + "</Operation>"
            + "</tns:TechnicalService>"

            + " <tns:TechnicalService id=\"ssh\" build=\"1000\" "
            + "  provisioningType=\"SYNCHRONOUS\" "
            + " provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\" "
            + " provisioningVersion=\"1.0\" accessType=\"DIRECT\" baseUrl=\"\" loginPath=\" \" billingIdentifier=\"NATIVE_BILLING\">"
            + " <AccessInfo locale=\"en\">"
            + " Please open a ssh client and connect to the server estadmue.dyn.lan.est.fujitsu.de. Use your EST userId and password for the log in."
            + " </AccessInfo>"
            + " <LocalizedDescription locale=\"de\">"
            + " SSH ermöglicht eine sichere, authentifizierte und verschlüsselte Verbindung zwischen zwei Rechnern über ein unsicheres Netzwerk. Dadurch dient es unter anderem als Ersatz für die Vorgänger rlogin, telnet und rsh; diese übertragen jeglichen Netzverkehr, darunter auch die Passwörter, unverschlüsselt."
            + " </LocalizedDescription>"
            + " <LocalizedDescription locale=\"en\">"
            + " SSH is a protocol that can be used for many applications. Some of the applications below may require features that are only available or compatible with specific SSH clients or servers. For example, using the SSH protocol to implement a VPN is possible, but presently only with the OpenSSH server and client implementation.    </LocalizedDescription>"
            + " <LocalizedLicense locale=\"en\">"
            + " &lt;b&gt;License Agreement&lt;/b&gt;&lt;br/&gt;&lt;br/&gt;"
            + " </LocalizedLicense>"
            + " <LocalizedTag locale=\"en\">"
            + " storage"
            + " </LocalizedTag>"
            + " <LocalizedTag locale=\"de\">"
            + " speicher"
            + " </LocalizedTag>"
            + " </tns:TechnicalService>"

            + " <tns:TechnicalService id=\"saml\" build=\"1000\" "
            + " baseUrl=\"http://estadmue:8089/example-dev/\"\n "
            + " provisioningType=\"SYNCHRONOUS\" "
            + " provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\" "
            + " provisioningVersion=\"1.0\" accessType=\"USER\" loginPath=\"\\login/\" billingIdentifier=\"NATIVE_BILLING\">"
            + " <AccessInfo locale=\"en\">"
            + " Please open a ssh client and connect to the server estadmue.dyn.lan.est.fujitsu.de. Use your EST userId and password for the log in."
            + " </AccessInfo>"
            + " <LocalizedDescription locale=\"de\">"
            + " SSH ermöglicht eine sichere, authentifizierte und verschlüsselte Verbindung zwischen zwei Rechnern über ein unsicheres Netzwerk. Dadurch dient es unter anderem als Ersatz für die Vorgänger rlogin, telnet und rsh; diese übertragen jeglichen Netzverkehr, darunter auch die Passwörter, unverschlüsselt."
            + " </LocalizedDescription>"
            + " <LocalizedDescription locale=\"en\">"
            + " SSH is a protocol that can be used for many applications. Some of the applications below may require features that are only available or compatible with specific SSH clients or servers. For example, using the SSH protocol to implement a VPN is possible, but presently only with the OpenSSH server and client implementation.    </LocalizedDescription>"
            + " <LocalizedLicense locale=\"en\">"
            + " &lt;b&gt;License Agreement&lt;/b&gt;&lt;br/&gt;&lt;br/&gt;"
            + " </LocalizedLicense>"
            + " <LocalizedTag locale=\"en\">"
            + " storage"
            + " </LocalizedTag>"
            + " <LocalizedTag locale=\"de\">"
            + " speicher"
            + " </LocalizedTag>"
            + " </tns:TechnicalService>"
            + "</tns:TechnicalServices>";

    public static final String TECHNICAL_SERVICE_EXAMPLE2_XML = "<tns:TechnicalServices "
            + TARGET_NAMESPACE_TECHNICAL_SERVICES
            + ">"
            + "<tns:TechnicalService id=\"example2\" \n"
            + " accessType=\"LOGIN\"\n"
            + " baseUrl=\"http://estadmue:8089/example-dev/\"\n"
            + " provisioningType=\"SYNCHRONOUS\"\n"
            + " provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\"\n"
            + " provisioningVersion=\"1.0\"\n"
            + " loginPath=\"\\login/\"\n"
            + " billingIdentifier=\"NATIVE_BILLING\"\n"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "<LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">LocalizedDescriptionDE</LocalizedDescription>"
            + "<LocalizedDescription locale=\"jp\">LocalizedDescriptionJP</LocalizedDescription>"
            + "<LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>"
            + "<LocalizedLicense locale=\"de\">LocalizedLicenseDeutsch</LocalizedLicense>"
            + " <ParameterDefinition id=\"MAX_FILE_NUMBER\" valueType=\"INTEGER\" minValue=\"1\" "
            + "maxValue=\"10\" default=\"5\">"
            + "  <LocalizedDescription locale=\"en\">MAX_FILE_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"HAS_OPTIONS\" valueType=\"ENUMERATION\" mandatory=\"false\" configurable=\"true\" "
            + " default=\"2\"> "
            + "<Options>"
            + "<Option id=\"1\">"
            + "<LocalizedOption  locale=\"en\">Minimum Storage</LocalizedOption>"
            + "</Option>"
            + "<Option id=\"2\">"
            + "<LocalizedOption  locale=\"en\">Optimum storage</LocalizedOption>"
            + "</Option>"
            + "<Option id=\"3\"> "
            + "<LocalizedOption  locale=\"en\">Maximum storage</LocalizedOption>"
            + "</Option>"
            + "</Options>"
            + "<LocalizedDescription locale=\"en\">"
            + "MEMORY_STORAGE"
            + "</LocalizedDescription>"
            + "</ParameterDefinition>"
            + " <ParameterDefinition id=\"MAX_FOLDER_NUMBER\" valueType=\"INTEGER\" mandatory=\"false\" configurable=\"true\" minValue=\"12\" "
            + "  maxValue=\"500\" default=\"200\">"
            + "   <LocalizedDescription locale=\"en\">MAX_FOLDER_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"FOLDER_NEW\" valueType=\"INTEGER\" mandatory=\"false\" configurable=\"true\" minValue=\"12\" "
            + "maxValue=\"500\" default=\"200\">"
            + "   <LocalizedDescription locale=\"en\">FOLDER_NEW</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"BOOLEAN_PARAMETER\" valueType=\"BOOLEAN\" default=\"\">"
            + "  <LocalizedDescription locale=\"en\">BOOLEAN_PARAMETER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"STRING_PARAMETER\" valueType=\"STRING\" default=\"\">"
            + "  <LocalizedDescription locale=\"en\">STRING_PARAMETER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"LONG_NUMBER\" valueType=\"LONG\" minValue=\"1\" "
            + "maxValue=\"40000000000\" default=\"250\">"
            + "  <LocalizedDescription locale=\"en\">LONG_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <Event id=\"FILE_DOWNLOAD\">"
            + "   <LocalizedDescription locale=\"en\">FILE_DOWNLOAD</LocalizedDescription>"
            + " </Event>"
            + " <Event id=\"FILE_UPLOAD\">"
            + "   <LocalizedDescription locale=\"en\">FILE_UPLOAD</LocalizedDescription>"
            + " </Event>"
            + " <Event id=\"FOLDER_NEW\">"
            + "   <LocalizedDescription locale=\"en\">FOLDER_NEW</LocalizedDescription>"
            + " </Event>"
            + "<Role id=\"ADMIN\">"
            + "<LocalizedName locale=\"en\">Administrator</LocalizedName>"
            + "<LocalizedName locale=\"de\">Administrator</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Administrators have full access to all data entities and can execute administartive tasks such as role assignments and user creation.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Administratoren haben vollen Datenzugriff und können administartive Aufgaben erledigen wie Rollen zuweisen oder Benutzer anlegen.</LocalizedDescription>"
            + "</Role><Role id=\"USER\">"
            + "<LocalizedName locale=\"en\">User</LocalizedName>"
            + "<LocalizedName locale=\"de\">Benutzer</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Users have full access to all data entities but cannot execute adminstartive tasks.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Benutzer haben vollen Datenzugriff aber können keine administrativen Aufgaben erledigen.</LocalizedDescription>"
            + "</Role><Role id=\"GUEST\">"
            + "<LocalizedName locale=\"en\">Guest</LocalizedName>"
            + "<LocalizedName locale=\"de\">Gast</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Guests only have limited read access.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Gäste haben nur eingeschränkten Lesezugriff.</LocalizedDescription>"
            + " </Role>"
            + "<Operation id=\"BACKUP\"  actionURL=\"http://backupurl\">"
            + "<LocalizedName locale=\"en\">Backup</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Performs a backup operation for the subscription data.</LocalizedDescription>"
            + "</Operation>"
            + "<Operation id=\"CLEANUP\"  actionURL=\"http://cleanupurl\">"
            + "<LocalizedName locale=\"en\">Cleanup</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Deletes temporary data and removes data marked as deleted.</LocalizedDescription>"
            + "</Operation>"
            + "</tns:TechnicalService>"
            + "</tns:TechnicalServices>";

    public static final String TECHNICAL_SERVICE_EXAMPLE2_ASYNC_XML = "<tns:TechnicalServices "
            + TARGET_NAMESPACE_TECHNICAL_SERVICES
            + ">"
            + "<tns:TechnicalService id=\"example2Async\" \n"
            + " accessType=\"LOGIN\"\n"
            + " baseUrl=\"http://estadmue:8089/example-dev/\"\n"
            + " provisioningType=\"ASYNCHRONOUS\"\n"
            + " provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\"\n"
            + " provisioningVersion=\"1.0\"\n"
            + " loginPath=\"\\login/\"\n"
            + " billingIdentifier=\"NATIVE_BILLING\"\n"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "<LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">LocalizedDescriptionDE</LocalizedDescription>"
            + "<LocalizedDescription locale=\"jp\">LocalizedDescriptionJP</LocalizedDescription>"
            + "<LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>"
            + "<LocalizedLicense locale=\"de\">LocalizedLicenseDeutsch</LocalizedLicense>"
            + " <ParameterDefinition id=\"MAX_FILE_NUMBER\" valueType=\"INTEGER\" minValue=\"1\" "
            + "maxValue=\"10\" default=\"5\">"
            + "  <LocalizedDescription locale=\"en\">MAX_FILE_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"HAS_OPTIONS\" valueType=\"ENUMERATION\" mandatory=\"false\" configurable=\"true\" "
            + " default=\"2\"> "
            + "<Options>"
            + "<Option id=\"1\">"
            + "<LocalizedOption  locale=\"en\">Minimum Storage</LocalizedOption>"
            + "</Option>"
            + "<Option id=\"2\">"
            + "<LocalizedOption  locale=\"en\">Optimum storage</LocalizedOption>"
            + "</Option>"
            + "<Option id=\"3\"> "
            + "<LocalizedOption  locale=\"en\">Maximum storage</LocalizedOption>"
            + "</Option>"
            + "</Options>"
            + "<LocalizedDescription locale=\"en\">"
            + "MEMORY_STORAGE"
            + "</LocalizedDescription>"
            + "</ParameterDefinition>"
            + " <ParameterDefinition id=\"MAX_FOLDER_NUMBER\" valueType=\"INTEGER\" mandatory=\"false\" configurable=\"true\" minValue=\"12\" "
            + "  maxValue=\"500\" default=\"200\">"
            + "   <LocalizedDescription locale=\"en\">MAX_FOLDER_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"FOLDER_NEW\" valueType=\"INTEGER\" mandatory=\"false\" configurable=\"true\" minValue=\"12\" "
            + "maxValue=\"500\" default=\"200\">"
            + "   <LocalizedDescription locale=\"en\">FOLDER_NEW</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"BOOLEAN_PARAMETER\" valueType=\"BOOLEAN\" default=\"\">"
            + "  <LocalizedDescription locale=\"en\">BOOLEAN_PARAMETER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"STRING_PARAMETER\" valueType=\"STRING\" default=\"\">"
            + "  <LocalizedDescription locale=\"en\">STRING_PARAMETER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <ParameterDefinition id=\"LONG_NUMBER\" valueType=\"LONG\" minValue=\"1\" "
            + "maxValue=\"40000000000\" default=\"250\">"
            + "  <LocalizedDescription locale=\"en\">LONG_NUMBER</LocalizedDescription>"
            + " </ParameterDefinition>"
            + " <Event id=\"FILE_DOWNLOAD\">"
            + "   <LocalizedDescription locale=\"en\">FILE_DOWNLOAD</LocalizedDescription>"
            + " </Event>"
            + " <Event id=\"FILE_UPLOAD\">"
            + "   <LocalizedDescription locale=\"en\">FILE_UPLOAD</LocalizedDescription>"
            + " </Event>"
            + " <Event id=\"FOLDER_NEW\">"
            + "   <LocalizedDescription locale=\"en\">FOLDER_NEW</LocalizedDescription>"
            + " </Event>"
            + "<Role id=\"ADMIN\">"
            + "<LocalizedName locale=\"en\">Administrator</LocalizedName>"
            + "<LocalizedName locale=\"de\">Administrator</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Administrators have full access to all data entities and can execute administartive tasks such as role assignments and user creation.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Administratoren haben vollen Datenzugriff und können administartive Aufgaben erledigen wie Rollen zuweisen oder Benutzer anlegen.</LocalizedDescription>"
            + "</Role><Role id=\"USER\">"
            + "<LocalizedName locale=\"en\">User</LocalizedName>"
            + "<LocalizedName locale=\"de\">Benutzer</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Users have full access to all data entities but cannot execute adminstartive tasks.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Benutzer haben vollen Datenzugriff aber können keine administrativen Aufgaben erledigen.</LocalizedDescription>"
            + "</Role><Role id=\"GUEST\">"
            + "<LocalizedName locale=\"en\">Guest</LocalizedName>"
            + "<LocalizedName locale=\"de\">Gast</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Guests only have limited read access.</LocalizedDescription>"
            + "<LocalizedDescription locale=\"de\">Gäste haben nur eingeschränkten Lesezugriff.</LocalizedDescription>"
            + " </Role>"
            + "<Operation id=\"BACKUP\" actionURL=\"http://backupurl\">"
            + "<LocalizedName locale=\"en\">Backup</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Performs a backup operation for the subscription data.</LocalizedDescription>"
            + "</Operation>"
            + "<Operation id=\"CLEANUP\" actionURL=\"http://cleanupurl\">"
            + "<LocalizedName locale=\"en\">Cleanup</LocalizedName>"
            + "<LocalizedDescription locale=\"en\">Deletes temporary data and removes data marked as deleted.</LocalizedDescription>"
            + "</Operation>"
            + "</tns:TechnicalService>"
            + "</tns:TechnicalServices>";

    protected static final String MARKETABLE_SERVICE_FREE_XML_TEMPLATE = "<mns:MarketableServices "
            + TARGET_NAMESPACE_MARKETABLE_SERVICES
            + ">\n"
            + "<mns:MarketableService id=\"%s\" technicalServiceId=\"example\" version=\"1.3\" "
            + "%s\n>\n"
            + "<LocalizedResource locale=\"en\">\n"
            + "    <ServiceNameForCustomers>%s<ServiceNameForCustomers>\n"
            + "    <ServiceShortDescription>%s<ServiceShortDescription/>\n"
            + "    <ServiceDescription>%s<ServiceDescription/>\n"
            + "    <License>\n"
            + "    &lt;b&gt;License Agreement&lt;/b&gt;&lt;br/&gt;&lt;br/&gt;\n"
            + "    </License>\n"
            + "</LocalizedResource>\n"
            + "<PriceModel chargeable=\"false\"/>\n"
            + "</mns:MarketableService>\n" + "<mns:MarketableServices>";

    protected static final String MARKETABLE_SERVICE_CHARGEABLE_XML_TEMPLATE = "<mns:MarketableServices "
            + TARGET_NAMESPACE_MARKETABLE_SERVICES
            + "\n>\n"
            + "<mns:MarketableService id=\"%s\" technicalServiceId=\"example\""
            + "version=\"1.3\" %s >\n"
            + "  <LocalizedResource locale=\"en\">\n"
            + "    <ServiceNameForCustomers>%s<ServiceNameForCustomers>\n"
            + "    <ServiceShortDescription>%s<ServiceShortDescription/>\n"
            + "    <ServiceDescription>%s<ServiceDescription/>\n"
            + "    <License>\n"
            + "    &lt;b&gt;License Agreement&lt;/b&gt;&lt;br/&gt;&lt;br/&gt;\n"
            + "    </License>\n"
            + "  </LocalizedResource>\n"
            + "  <PriceModel chargeable=\"true\" period=\"%s\""
            + "    pricePerPeriod=\"%s\""
            + "    pricePerUser=\"%f\""
            + "    oneTimeFee=\"%f\" currencyISOCode=\"%s\">"
            + "    <PricedEvent type=\"PLATFORM_EVENT\" id=\""
            + PlatformEventIdentifier.USER_LOGIN_TO_SERVICE
            + "\" price=\"%f\"/>"
            + "    <PricedEvent type=\"PLATFORM_EVENT\" id=\""
            + PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE
            + "\" price=\"%f\"/>\n"
            + "    <PricedEvent type=\"SERVICE_EVENT\" id=\""
            + SERVICE_EVENT_FILE_UPLOAD
            + "\" price=\"%f\"/>\n"
            + "  </PriceModel>\n"
            + "</mns:MarketableService>\n"
            + "<mns:MarketableServices>";

    protected static final String PRODUCT_CHARGEABLE_XML_TEMPLATE = "<Product id=\"%s\">"
            + "<LocalizedResource locale=\"en\">"
            + " <MarketingName></MarketingName>"
            + " <MarketingDescription></MarketingDescription>"
            + " <PriceDescription></PriceDescription>"
            + "</LocalizedResource>"
            + "<PriceModel chargeable=\"true\" period=\"%s\""
            + " pricePerPeriod=\"%s\""
            + " pricePerUser=\"%f\""
            + " oneTimeFee=\"%f\">"
            + "  <PricedEvent type=\"PLATFORM_EVENT\" id=\""
            + PlatformEventIdentifier.USER_LOGIN_TO_SERVICE
            + "\" price=\"%f\"/>"
            + "  <PricedEvent type=\"PLATFORM_EVENT\" id=\""
            + PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE
            + "\" price=\"%f\"/>"
            + "  <PricedEvent type=\"SERVICE_EVENT\" id=\""
            + SERVICE_EVENT_FILE_UPLOAD
            + "\" price=\"%f\"/>"
            + "</PriceModel>"
            + "</Product>";

    protected static final String PRODUCT_FREE_XML_TEMPLATE = "<Product id=\"%s\">"
            + "<LocalizedResource locale=\"en\">"
            + " <MarketingName></MarketingName>"
            + " <MarketingDescription></MarketingDescription>"
            + " <PriceDescription></PriceDescription>"
            + "</LocalizedResource>"
            + "<PriceModel chargeable=\"false\"/>" + "</Product>";

    public static final String LOCALIZED_TECHNICAL_SERVICE_XML = "<?xml version='1.0' encoding='UTF-8'?> \n"
            + "<tns:TechnicalServices \n"
            + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "  xmlns:tns=\"oscm.serviceprovisioning/1.9/TechnicalService.xsd\"\n"
            + "  xsi:schemaLocation=\"oscm.serviceprovisioning/1.9/TechnicalService.xsd ../../oscm-serviceprovisioning/javares/TechnicalServices.xsd\">\n"
            + "  <tns:TechnicalService \n"
            + "    id=\"Localized test service\"\n"
            + "    build=\"1000\"\n"
            + "    provisioningType=\"SYNCHRONOUS\"\n"
            + "    provisioningUrl=\"http://localhost:8082/oscm-integrationtests-mockproduct/ProvisioningService?wsdl\"\n"
            + "    provisioningVersion=\"1.0\"\n"
            + "    accessType=\"LOGIN\" \n"
            + "    baseUrl=\"http://localhost:8082/oscm-integrationtests-mockproduct\" \n"
            + "    loginPath=\"/login\">\n"
            + "      <LocalizedDescription locale=\"en\">%s</LocalizedDescription>\n"
            + "      <LocalizedDescription locale=\"de\">German description</LocalizedDescription>\n"
            + "      <LocalizedLicense locale=\"en\">License Agreement</LocalizedLicense>\n"
            + "      <LocalizedLicense locale=\"de\"/>\n"
            + "  </tns:TechnicalService>\n" + "</tns:TechnicalServices>";

    protected static final String TEST_MAIL_ADDRESS = "asm-ue-test@est.fujitsu.com";

    protected static void setUpDirServerStub(ConfigurationServiceLocal cfg) {

        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS,
                Configuration.GLOBAL_CONTEXT, "3"));
        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT,
                "ignored for unit tests - "));
        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_ORGANIZATION,
                Configuration.GLOBAL_CONTEXT, "2000"));
        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.PERMITTED_PERIOD_UNCONFIRMED_ORGANIZATIONS,
                Configuration.GLOBAL_CONTEXT, "1000"));
        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_SUBSCRIPTION_EXPIRATION,
                Configuration.GLOBAL_CONTEXT, "2000"));
        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.TAGGING_MAX_TAGS,
                Configuration.GLOBAL_CONTEXT, "20"));
        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.TAGGING_MIN_SCORE,
                Configuration.GLOBAL_CONTEXT, "5"));
        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                Configuration.GLOBAL_CONTEXT, "0"));
        LoggerFactory.activateRollingFileAppender("./logs", null, "DEBUG");
    }

    public static void createOrganizationRoles(DataService mgr)
            throws NonUniqueBusinessKeyException {
        for (OrganizationRoleType roleType : OrganizationRoleType.values()) {
            OrganizationRole orgRoleTemplate = new OrganizationRole();
            orgRoleTemplate.setRoleName(roleType);
            try {
                mgr.getReferenceByBusinessKey(orgRoleTemplate);
            } catch (ObjectNotFoundException e) {
                OrganizationRole role = new OrganizationRole();
                role.setRoleName(roleType);
                mgr.persist(role);
            }
        }
    }

    public static void createUserRoles(DataService mgr)
            throws NonUniqueBusinessKeyException {
        UserRoles.createSetupRoles(mgr);
    }

    public static void createBillingAdapter(DataService mgr)
            throws NonUniqueBusinessKeyException {
        BillingAdapters.createBillingAdapter(mgr,
                BillingAdapterIdentifier.NATIVE_BILLING.toString(), true);
    }

    public static SupportedCurrency createSupportedCurrencies(DataService mgr)
            throws NonUniqueBusinessKeyException {
        return SupportedCurrencies.createOneSupportedCurrency(mgr);
    }

    public static List<PaymentType> createPaymentTypes(DataService mgr)
            throws NonUniqueBusinessKeyException {
        PSP psp = new PSP();
        psp.setIdentifier("pspIdentifier");
        psp.setWsdlUrl("http://www.google.com");
        mgr.persist(psp);
        List<PaymentType> result = new ArrayList<PaymentType>();
        PaymentType pt = new PaymentType();
        pt.setCollectionType(PaymentCollectionType.ORGANIZATION);
        pt.setPaymentTypeId(INVOICE);
        pt.setPsp(psp);
        mgr.persist(pt);
        result.add(pt);
        pt = new PaymentType();
        pt.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        pt.setPaymentTypeId(DIRECT_DEBIT);
        pt.setPsp(psp);
        mgr.persist(pt);
        result.add(pt);
        pt = new PaymentType();
        pt.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        pt.setPaymentTypeId(CREDIT_CARD);
        pt.setPsp(psp);
        mgr.persist(pt);
        result.add(pt);
        return result;
    }

    protected static VOPaymentType getPaymentType(String id) {
        VOPaymentType voPaymentType = new VOPaymentType();
        voPaymentType.setPaymentTypeId(id);
        return voPaymentType;
    }

    public static PaymentType findPaymentType(String id, DataService mgr)
            throws ObjectNotFoundException {
        PaymentType paymentType = new PaymentType();
        paymentType.setPaymentTypeId(id);
        paymentType = (PaymentType) mgr.getReferenceByBusinessKey(paymentType);
        return paymentType;
    }

    protected static final List<VOUsageLicense> getUsersToAdd(VOUser[] admins,
            VOUser[] users) {
        List<VOUsageLicense> result = new ArrayList<VOUsageLicense>();
        if (admins != null) {
            for (VOUser u : admins) {
                VOUsageLicense lic = new VOUsageLicense();
                lic.setUser(u);
                result.add(lic);
            }
        }
        if (users != null) {
            for (VOUser u : users) {
                VOUsageLicense lic = new VOUsageLicense();
                lic.setUser(u);
                result.add(lic);
            }
        }
        return result;
    }

    public static byte[] getFileAsByteArray(Class<?> clazz, String path)
            throws IOException {
        URL fileURL = clazz.getClassLoader().getResource(path);

        InputStream is = null;
        byte[] bytes;
        try {
            File file = new File(fileURL.getFile());

            is = new FileInputStream(file);

            long length = file.length();
            bytes = new byte[(int) length];

            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file "
                        + file.getName());
            }
        } finally {
            if (is != null)
                is.close();
        }
        return bytes;

    }

    /**
     * Decides whether Hibernate Search registers itself as event listener in
     * order to index the modified entities. Call this method from within
     * setup() <b>before</b> adding the DataServiceBean to the test container.
     * 
     * @param enabled
     */
    protected static void enableHibernateSearchListeners(boolean enabled) {
        System.setProperty(HS_SEARCH_LISTENERS, Boolean.toString(enabled));
    }

    /**
     * Tries to register a mocked <code>NamingContextFactoryBuilder</code> to
     * enable basic JNDI functionality in the test code.
     * 
     * @throws NamingException
     * @see {@link TestNamingContext}
     */
    protected static void enableJndiMock() throws NamingException {
        if (!NamingManager.hasInitialContextFactoryBuilder()) {
            NamingManager
                    .setInitialContextFactoryBuilder(new TestNamingContextFactoryBuilder(PERSISTENCE));
        }
    }

    protected static FifoJMSQueue createIndexerQueue() throws NamingException,
            JMSException {
        enableJndiMock();
        Context jndiContext = new InitialContext();
        // static queue required since hibernate listener caches queue object
        FifoJMSQueue indexerQueue = new FifoJMSQueue();
        jndiContext.bind("jms/bss/indexerQueueFactory",
                initMockFactory(indexerQueue));
        jndiContext.bind("jms/bss/indexerQueue", indexerQueue);
        return indexerQueue;
    }

    private static QueueConnectionFactory initMockFactory(
            final FifoJMSQueue indexerQueue) throws JMSException {
        QueueConnectionFactory factoryMock = mock(QueueConnectionFactory.class);
        QueueConnection conn = mock(QueueConnection.class);
        doReturn(conn).when(factoryMock).createQueueConnection();
        doReturn(conn).when(factoryMock).createConnection();
        QueueSession session = mock(QueueSession.class);
        doReturn(session).when(conn).createSession(eq(false),
                eq(Session.AUTO_ACKNOWLEDGE));
        MessageProducer producer = mock(MessageProducer.class);
        doReturn(producer).when(session).createProducer(any(Queue.class));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object object = invocation.getArguments()[0];
                indexerQueue.add(object);
                return null;
            }
        }).when(producer).send(any(ObjectMessage.class));
        doAnswer(new Answer<ObjectMessage>() {
            @Override
            public ObjectMessage answer(InvocationOnMock invocation) {
                return new ObjectMessageStub();
            }
        }).when(session).createObjectMessage();
        return factoryMock;
    }

    protected static Properties loadConfigurationSettings() {
        Properties props = PropertiesLoader.load(
                DefaultConfigFileCreator.class, "configsettings.properties");
        props.putAll(PropertiesLoader.load(DefaultConfigFileCreator.class,
                "local/configsettings.properties"));
        return props;
    }
}
