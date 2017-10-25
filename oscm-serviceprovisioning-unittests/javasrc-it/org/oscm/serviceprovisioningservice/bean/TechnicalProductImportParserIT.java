/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.oscm.test.Numbers.L_TIMESTAMP;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.MarketingPermission;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.TechnicalProductTag;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.BillingAdapterNotFoundException;
import org.oscm.internal.types.exception.DomainObjectException;
import org.oscm.internal.types.exception.ImportException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.TechnicalServiceActiveException;
import org.oscm.internal.types.exception.TechnicalServiceMultiSubscriptions;
import org.oscm.internal.types.exception.UnchangeableAllowingOnBehalfActingException;
import org.oscm.internal.types.exception.UpdateConstraintException;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOTechnicalServiceOperation;
import org.oscm.serviceprovisioningservice.local.TagServiceLocal;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.BillingAdapters;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TSXML;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.ImageResourceServiceStub;
import org.oscm.test.stubs.LdapAccessServiceStub;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.types.enumtypes.ProvisioningType;

@SuppressWarnings("boxing")
public class TechnicalProductImportParserIT extends EJBTestBase {

    private TechnicalProductImportParser parser;
    private DataService mgr;
    private ServiceProvisioningService svcProv;
    private TagServiceLocal tagServiceLocal;
    private LocalizerServiceLocal localizer;
    private SessionServiceLocal productSessionManagement;
    private TenantProvisioningServiceBean tenantProvisioning;
    private MarketingPermissionServiceLocal mpsMock;
    private String providerOrganizationId;
    private long providerKey;

    private boolean techProdActiveSessions = false;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);

        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        mgr = container.get(DataService.class);
        mpsMock = mock(MarketingPermissionServiceLocal.class);
        container.addBean(mpsMock);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new IdentityServiceStub());
        container.addBean(new SessionServiceStub() {
            @Override
            public boolean hasTechnicalProductActiveSessions(
                    long technicalProductKey) {
                return techProdActiveSessions;
            }
        });
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LdapAccessServiceStub());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new ImageResourceServiceStub() {
            @Override
            public ImageResource read(long objectKey, ImageType imageType) {
                return null;
            }
        });
        container.addBean(new PaymentServiceStub() {
            @Override
            public void deregisterPaymentInPSPSystem(PaymentInfo payment) {
            }
        });

        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new TagServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());

        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);
        svcProv = container.get(ServiceProvisioningService.class);
        tagServiceLocal = container.get(TagServiceLocal.class);
        localizer = container.get(LocalizerServiceLocal.class);
        productSessionManagement = container.get(SessionServiceLocal.class);
        tenantProvisioning = container.get(TenantProvisioningServiceBean.class);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createSupportedCurrencies(mgr);
                createPaymentTypes(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                BillingAdapters.createBillingAdapter(mgr,
                        BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                        true);
                return null;
            }
        });

        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                createOrganizationRoles(mgr);
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                providerOrganizationId = organization.getOrganizationId();
                providerKey = organization.getKey();
                return Organizations.createUserForOrg(mgr, organization, true,
                        "admin");

            }
        });
        container.login(user.getKey(), ROLE_TECHNOLOGY_MANAGER,
                ROLE_SERVICE_MANAGER);
        parser = runTX(new Callable<TechnicalProductImportParser>() {

            @Override
            public TechnicalProductImportParser call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                load(org);
                return new TechnicalProductImportParser(mgr, localizer, org,
                        productSessionManagement, tenantProvisioning,
                        tagServiceLocal,
                        container.get(MarketingPermissionServiceLocal.class),
                        container.get(ConfigurationServiceLocal.class), false);
            }
        });
    }

    /**
     * Parse the xml of an technical service where the attribute
     * allowOneSubscriptionPerUser and allowingOnBehalfActing does not exists.
     * 
     * @throws Exception
     */
    @Test
    public void testParseTechnicalService_OptionalAttributeMissing()
            throws Exception {
        final String tsxml = TSXML.createTSXML("test", "1",
                ProvisioningType.SYNCHRONOUS.name(), "http://someurl", "1.0",
                ServiceAccessType.LOGIN.name(), "http://baseurl", "loginpath",
                new String[] { "en", "en", "en" });

        Integer returnCode = runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return new Integer(parser.parse(tsxml.getBytes("UTF-8")));
            }
        });
        assertEquals(TechnicalProductImportParser.RC_OK, returnCode.intValue());
    }

    @Test(expected = ImportException.class)
    public void testParseTechnicalService_InvalidBooleanAttribute_Configurable_B10915()
            throws Exception {
        // given
        final String xml = anyTSXMLWithIntParameters();

        // when
        final String tsxml = xml.replaceAll("configurable=\"true\"",
                "configurable=\"ture\"");
        try {
            svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        } catch (ImportException e) {
            final String[] errMsg = e.getDetails().split("\n");
            assertTrue("Unexpected error:" + errMsg,
                    errMsg[errMsg.length - 1].matches(
                            ".*ture.*configurable.*ParameterDefinition.*boolean.*"));
            throw e;
        }

    }

    @Test(expected = ImportException.class)
    public void testParseTechnicalService_InvalidBooleanAttribute_Mandatory_B10915()
            throws Exception {
        // given
        final String xml = anyTSXMLWithIntParameters();

        // when
        final String tsxml = xml.replaceAll("mandatory=\"true\"",
                "mandatory=\"ture\"");
        try {
            svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        } catch (ImportException e) {
            final String[] errMsg = e.getDetails().split("\n");
            assertTrue("Unexpected error:" + errMsg,
                    errMsg[errMsg.length - 1].matches(
                            ".*ture.*mandatory.*ParameterDefinition.*boolean.*"));
            throw e;
        }
    }

    @Test
    public void testParseTechnicalService_InvalidIntAttribute_Default_B10915()
            throws Exception {
        // given
        final String xml = anyTSXMLWithIntParameters();

        // when
        final String tsxml = xml.replaceAll("default=\".*\"",
                "default=\"misst\"");
        try {
            svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
            fail("ImportException expected.");
        } catch (ImportException e) {
            assertTrue("Unexpected error:" + e.getDetails(),
                    e.getDetails().indexOf(
                            "The value 'misst' for the attribute 'default' does not match with the required type INTEGER.") > 0);
        }
    }

    @Test
    public void testParseTechnicalService_InvalidIntAttribute_MinValue_B10915()
            throws Exception {
        // given
        final String xml = anyTSXMLWithIntParameters();

        // when
        final String tsxml = xml.replaceAll("minValue=\".*\"",
                "minValue=\"misst\"");
        try {
            svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
            fail("ImportException expected.");
        } catch (ImportException e) {
            assertTrue("Unexpected error:" + e.getDetails(),
                    e.getDetails().indexOf(
                            "The value 'misst' for the attribute 'minValue' does not match with the required type INTEGER.") > 0);
        }
    }

    @Test(expected = ImportException.class)
    public void testParseTechnicalService_InvalidBaseURL_B10941()
            throws Exception {
        // given
        final String xml = anyTSXMLWithIntParameters();

        // when
        final String tsxml = xml.replaceAll("baseUrl=\"http://someurl\"",
                "baseUrl=\"Invalid url\"");
        try {
            svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        } catch (ImportException e) {
            final String[] errMsg = e.getDetails().split("\n");
            assertTrue("Unexpected error:" + errMsg, errMsg[errMsg.length - 1]
                    .matches(".*baseUrl.*is not valid url*"));
            throw e;
        }
    }

    /**
     * Parse the xml of an technical service where the attribute
     * allowOneSubscriptionPerUser is set to a boolean value;
     * 
     * @throws Exception
     */
    @Test
    public void testParseTechnicalService_OneSubscriptionBooleanValue()
            throws Exception {
        final String tsxml = TSXML
                .createTSXMLWithSubscriptionRestriction("false");

        Integer returnCode = runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return new Integer(parser.parse(tsxml.getBytes("UTF-8")));
            }
        });
        assertEquals(TechnicalProductImportParser.RC_OK, returnCode.intValue());
    }

    /**
     * Parse the xml of an technical service where the attribute
     * allowOneSubscriptionPerUser is set to an invalid value;
     * 
     * @throws Exception
     */
    @Test
    public void testParseTechnicalService_OneSubscriptionWrongValue()
            throws Exception {
        final String tsxml = TSXML
                .createTSXMLWithSubscriptionRestriction("wrong value");

        Integer returnCode = runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return new Integer(parser.parse(tsxml.getBytes("UTF-8")));
            }
        });
        assertEquals(TechnicalProductImportParser.RC_ERROR,
                returnCode.intValue());
    }

    /**
     * Import technical service where the subscription restriction is not set.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalService_OneSubscriptionNotSet()
            throws Exception {
        svcProv.importTechnicalServices(
                TECHNICAL_SERVICES_XML.getBytes("UTF-8"));

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                assertEquals(false, techPrd.isOnlyOneSubscriptionAllowed());
                return null;
            }
        });

    }

    /**
     * Import technical service where the subscription restriction is set to
     * true.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalService_OneSubscription() throws Exception {
        String tsxml = TSXML.createTSXMLWithSubscriptionRestriction("true");

        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                createOrganizationRoles(mgr);
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.TECHNOLOGY_PROVIDER);
                providerOrganizationId = organization.getOrganizationId();
                providerKey = organization.getKey();
                return Organizations.createUserForOrg(mgr, organization, true,
                        "admin");

            }
        });
        container.login(user.getKey(), ROLE_TECHNOLOGY_MANAGER,
                ROLE_SERVICE_MANAGER);

        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));

        Mockito.verify(mpsMock, Mockito.never()).addMarketingPermission(
                Mockito.any(Organization.class), Mockito.anyLong(),
                Mockito.anyListOf(String.class));

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                assertTrue(techPrd.isOnlyOneSubscriptionAllowed());
                return null;
            }
        });
    }

    @Test
    public void testImportTechnicalService_withMarketingPermissions_B10219()
            throws Exception {
        String tsxml = TSXML.createTSXMLWithSubscriptionRestriction("false");

        loginAsSupplier();

        // when
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));

        // then
        verify(mpsMock).addMarketingPermission(any(Organization.class),
                anyLong(), anyListOf(String.class));

    }

    @Test
    public void testImportTechnicalService_withoutMarketingPermissions_B10219()
            throws Exception {
        String tsxml = TSXML.createTSXMLWithSubscriptionRestriction("false");

        loginAsTechnologyProvider();

        // when
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));

        // then
        verify(mpsMock, never()).addMarketingPermission(any(Organization.class),
                anyLong(), anyListOf(String.class));

    }

    private Organization createSupplier() throws Exception {
        Organization organization = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        providerOrganizationId = organization.getOrganizationId();
        providerKey = organization.getKey();
        return organization;
    }

    private Organization createTechnologyProvider() throws Exception {
        Organization organization = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        providerOrganizationId = organization.getOrganizationId();
        providerKey = organization.getKey();
        return organization;
    }

    private void loginAsSupplier() throws Exception {
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                createOrganizationRoles(mgr);
                Organization supplier = createSupplier();
                return Organizations.createUserForOrg(mgr, supplier, true,
                        "admin");

            }
        });
        container.login(user.getKey(), ROLE_TECHNOLOGY_MANAGER,
                ROLE_SERVICE_MANAGER);
    }

    private void loginAsTechnologyProvider() throws Exception {
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                createOrganizationRoles(mgr);
                Organization techProvider = createTechnologyProvider();
                return Organizations.createUserForOrg(mgr, techProvider, true,
                        "admin");

            }
        });
        container.login(user.getKey(), ROLE_TECHNOLOGY_MANAGER,
                ROLE_SERVICE_MANAGER);
    }

    /**
     * Parse the xml of an technical service where the attribute
     * allowOnBehalfActing is set to a boolean value;
     * 
     * @throws Exception
     */
    @Test
    public void testParseTechnicalService_OnBehalfActingBooleanValue()
            throws Exception {
        final String tsxml = TSXML
                .createTSXMLWithAllowingOnBehalfActing("false");

        Integer returnCode = runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return new Integer(parser.parse(tsxml.getBytes("UTF-8")));
            }
        });
        assertEquals(TechnicalProductImportParser.RC_OK, returnCode.intValue());
    }

    /**
     * Parse the xml of an technical service where the attribute
     * allowOnBehalfActing is set to an invalid value;
     * 
     * @throws Exception
     */
    @Test
    public void testParseTechnicalService_OnBehalfActingWrongValue()
            throws Exception {
        final String tsxml = TSXML
                .createTSXMLWithAllowingOnBehalfActing("wrong value");

        Integer returnCode = runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return new Integer(parser.parse(tsxml.getBytes("UTF-8")));
            }
        });
        assertEquals(TechnicalProductImportParser.RC_ERROR,
                returnCode.intValue());
    }

    /**
     * Import technical service where the allowing on behalf acting is not set.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalService_OnBehalfActingNotSet()
            throws Exception {
        svcProv.importTechnicalServices(
                TECHNICAL_SERVICES_XML.getBytes("UTF-8"));

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                assertEquals(false, techPrd.isAllowingOnBehalfActing());
                return null;
            }
        });

    }

    /**
     * Import technical service where the allowing on behalf acting is set to
     * true.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalService_AllowingOnBehalfActing()
            throws Exception {
        String tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("true");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                assertTrue(techPrd.isAllowingOnBehalfActing());
                return null;
            }
        });
    }

    @Test
    public void testParseDangerousXml() throws Exception {
        final byte[] xml = createTestXml();
        Integer returnCode = runTX(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return new Integer(parser.parse(xml));
            }
        });
        assertEquals(TechnicalProductImportParser.RC_FATAL,
                returnCode.intValue());
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicessSyntaxError() throws Exception {
        svcProv.importTechnicalServices(
                TECHNICAL_SERVICES_XML.replaceAll("/", "").getBytes("UTF-8"));
    }

    @Test
    public void testImportTechnicalServicessValidationError() throws Exception {
        try {
            svcProv.importTechnicalServices(TECHNICAL_SERVICES_XML
                    .replaceAll("LocalizedDescription", "Description")
                    .getBytes("UTF-8"));
            fail("The import must fail");
        } catch (ImportException e) {
            System.out.println(TECHNICAL_SERVICES_XML);
            System.out.println(e.getDetails());
            assertEquals("10:", e.getDetails().substring(0, 3));
        }
    }

    @Test
    public void testImportTechnicslProductsMissingAccessType()
            throws Exception {
        String tpXml = TECHNICAL_SERVICES_XML
                .replace(" accessType=\"PLATFORM\"\n", "");
        try {
            svcProv.importTechnicalServices(tpXml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertEquals("7:", e.getDetails().substring(0, 2));
        }
    }

    @Test
    public void testImportTechnicslProductExternal() throws Exception {
        String tpXml = TECHNICAL_SERVICES_XML
                .replace(" accessType=\"EXTERNAL\"\n", "");
        svcProv.importTechnicalServices(tpXml.getBytes("UTF-8"));
    }

    /**
     * Test for ID. Blanks in middle are allowed.
     * 
     * @throws Exception
     *             On error.
     */
    @Test
    public void testImportTechnicslProductsOneBlanksInMiddleId()
            throws Exception {
        String tpXml = TECHNICAL_SERVICES_XML.replace("id=\"example\"",
                "id=\"ex ample\"");

        svcProv.importTechnicalServices(tpXml.getBytes("UTF-8"));
        List<VOTechnicalService> techProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);

        VOTechnicalService techProduct = techProducts.get(0);
        assertEquals("ex ample", techProduct.getTechnicalServiceId());
    }

    /**
     * Test for ID. Blanks in middle are allowed.
     * 
     * @throws Exception
     *             On error.
     */
    @Test
    public void testImportTechnicslProductsManyBlanksInMiddleId()
            throws Exception {
        String tpXml = TECHNICAL_SERVICES_XML.replace("id=\"example\"",
                "id=\"ex     ample\"");

        svcProv.importTechnicalServices(tpXml.getBytes("UTF-8"));
        List<VOTechnicalService> techProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);

        VOTechnicalService techProduct = techProducts.get(0);
        assertEquals("ex     ample", techProduct.getTechnicalServiceId());
    }

    /**
     * Test for ID. There is a trim for such fields.
     * 
     * @throws Exception
     *             On error.
     */
    @Test(expected = ImportException.class)
    public void testImportTechnicslProductsLeadingBlankInId() throws Exception {
        String tpXml = TECHNICAL_SERVICES_XML.replace("id=\"example\"",
                "id=\"   example\"");

        svcProv.importTechnicalServices(tpXml.getBytes("UTF-8"));
    }

    /**
     * Test for ID. There is a trim for such fields.
     * 
     * @throws Exception
     *             On error.
     */
    @Test(expected = ImportException.class)
    public void testImportTechnicslProductsTrailingBlankInId()
            throws Exception {
        String tpXml = TECHNICAL_SERVICES_XML.replace("id=\"example\"",
                "id=\"example   \"");

        svcProv.importTechnicalServices(tpXml.getBytes("UTF-8"));
    }

    @Test
    public void testImportTechnicalServicess() throws Exception {
        createTechnicalProduct();

        List<VOTechnicalService> techProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);

        assertEquals("Wrong number of technical products", 3,
                techProducts.size());
        VOTechnicalService techProduct = techProducts.get(0);
        assertEquals("example", techProduct.getTechnicalServiceId());
        assertEquals("LocalizedDescription",
                techProduct.getTechnicalServiceDescription());

        assertEquals("Wrong number of parameter definitions", 10,
                techProduct.getParameterDefinitions().size());

        VOParameterDefinition pramDef = techProduct.getParameterDefinitions()
                .get(4);

        assertEquals("HAS_OPTIONS", pramDef.getParameterId());
        assertEquals(ParameterValueType.ENUMERATION, pramDef.getValueType());
        assertEquals("Wrong number of parameter options", 3,
                pramDef.getParameterOptions().size());

        assertEquals("MEMORY_STORAGE", pramDef.getDescription());

        pramDef = techProduct.getParameterDefinitions().get(5);
        assertEquals("MAX_FOLDER_NUMBER", pramDef.getParameterId());
        assertEquals(ParameterValueType.INTEGER, pramDef.getValueType());
        assertEquals("MAX_FOLDER_NUMBER", pramDef.getDescription());

    }

    @Test
    public void testReimportTechnicalProducts() throws Exception {
        createTechnicalProduct();

        svcProv.importTechnicalServices(getXmlWithModifiedLicense());
        assertEquals("License update failed.", "",
                getTechnicalProduct().getLicense());

        svcProv.importTechnicalServices(getXmlWithModifiedParameterDefs());
        assertEquals("ParameterDefinition update failed.",
                ParameterValueType.LONG, getTechnicalProduct()
                        .getParameterDefinitions().get(3).getValueType());

        svcProv.importTechnicalServices(getXmlWithoutParameterDefs());
        // the platform parameter definitions are still assigned to the
        // technical product
        assertEquals("Removing parameter definitions failed", 3,
                getTechnicalProduct().getParameterDefinitions().size());

        svcProv.importTechnicalServices(getXmlWithoutEvents());
        // the platform events are still assigned to the technical product
        assertEquals("Removing event definitions failed", 2,
                getTechnicalProduct().getEventDefinitions().size());
    }

    @Test
    public void testReimportTechnicalProductsUpdateLicense() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct();
        OrganizationReference orgRef = createOrgRef(providerKey);
        createMarketingPermission(techProduct.getKey(), orgRef.getKey());
        createProduct(techProduct, "prod");

        // create a subscription for the product
        final Organization cust = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                Organization customer = Organizations.createCustomer(mgr, org);

                Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), "prod", "sub", org);

                return customer;
            }
        });

        byte[] xml = getXmlWithModifiedLicense();

        // new license can be changed for technical product every time
        svcProv.importTechnicalServices(xml);

        // deactivate the subscription
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = new Subscription();
                sub.setOrganization(Organizations.findOrganization(mgr,
                        cust.getOrganizationId()));
                sub.setSubscriptionId("sub");
                sub = (Subscription) mgr.getReferenceByBusinessKey(sub);
                sub.setStatus(SubscriptionStatus.DEACTIVATED);
                return null;
            }
        });

        // positive test case
        svcProv.importTechnicalServices(
                TECHNICAL_SERVICES_XML.getBytes("UTF-8"));
        assertEquals("The existing license was not updated.",
                "LocalizedLicense", getTechnicalProduct().getLicense());
    }

    @Test
    public void testReimportTechnicalProductsUpdateParameter()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct();
        OrganizationReference orgRef = createOrgRef(providerKey);
        createMarketingPermission(techProduct.getKey(), orgRef.getKey());
        VOService product = createProductWithParm(techProduct, "prod");

        byte[] xml = getXmlWithModifiedParameterDefs();

        // negative test case
        try {
            svcProv.importTechnicalServices(xml);
            fail("Update parameter definitions must not be possible.");
        } catch (UpdateConstraintException e) {
            assertEquals(DomainObjectException.ClassEnum.TECHNICAL_SERVICE,
                    e.getDomainObjectClassEnum());
        }
        assertEquals("The parameter definitions must not be changes.",
                ParameterValueType.INTEGER, getTechnicalProduct()
                        .getParameterDefinitions().get(3).getValueType());

        svcProv.deleteService(product);

        // positive test case
        assertEquals("", svcProv.importTechnicalServices(xml));
        boolean found = false;
        for (VOParameterDefinition parmDef : getTechnicalProduct()
                .getParameterDefinitions()) {
            if (parmDef.getParameterId().equals("MAX_FILE_NUMBER")) {
                assertEquals("Paramter value type update failed.",
                        ParameterValueType.LONG, parmDef.getValueType());
                found = true;
            }
        }
        assertTrue("Parameter MAX_FILE_NUMBER not found.", found);
    }

    @Test
    public void testReimportTechnicalProductsDeleteParameter()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct();
        OrganizationReference orgRef = createOrgRef(providerKey);
        createMarketingPermission(techProduct.getKey(), orgRef.getKey());
        final VOService product = createProductWithParm(techProduct, "prod");

        byte[] xml = getXmlWithoutParameterDefs();

        // negative test case
        try {
            svcProv.importTechnicalServices(xml);
            fail("Delete parameter definitions must not be possible.");
        } catch (UpdateConstraintException e) {
            assertEquals(DomainObjectException.ClassEnum.TECHNICAL_SERVICE,
                    e.getDomainObjectClassEnum());
        }
        assertEquals("The existing parameter definitions must not be deleted.",
                10, getTechnicalProduct().getParameterDefinitions().size());

        svcProv.deleteService(product);

        // positive test case
        assertEquals("", svcProv.importTechnicalServices(xml));
        // 3 Platform parameter definitions remain...
        assertEquals("Delete parameter definition failed.", 3,
                getTechnicalProduct().getParameterDefinitions().size());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    mgr.getReference(Product.class, product.getKey());
                    fail("The product must not exist anymore.");
                } catch (ObjectNotFoundException e) {
                    // empty block is ok for logic of the test
                }
                return null;
            }
        });
    }

    @Test
    public void importTechnicalServices_deleteOptions() throws Exception {
        // given
        VOTechnicalService techProduct = createTechnicalProduct();
        OrganizationReference orgRef = createOrgRef(providerKey);
        createMarketingPermission(techProduct.getKey(), orgRef.getKey());

        byte[] xml = getXmlWithoutOptions();

        // when
        String result = svcProv.importTechnicalServices(xml);

        // then
        assertEquals("", result);

        List<VOParameterDefinition> parDefinitions = getTechnicalProduct()
                .getParameterDefinitions();
        List<VOParameterOption> parOptions = new ArrayList<>();
        for (VOParameterDefinition parDef : parDefinitions) {
            if ((parDef.getParameterType() == ParameterType.SERVICE_PARAMETER)
                    && (parDef
                            .getValueType() == ParameterValueType.ENUMERATION)) {
                parOptions = parDef.getParameterOptions();
                break;
            }
        }

        assertNotNull("One Option must be still available", parOptions);
        assertTrue("Exactly one Option must be available",
                parOptions.size() == 1);
        assertTrue("Option 2 must be still available",
                "2".equals(parOptions.get(0).getOptionId()));
    }

    @Test
    public void testReimportTechnicalProductsDeleteEvent() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct();
        OrganizationReference orgRef = createOrgRef(providerKey);
        createMarketingPermission(techProduct.getKey(), orgRef.getKey());
        VOService product = createProductWithPricedEvent(techProduct, "prod");

        byte[] xml = TECHNICAL_SERVICES_XML.replaceAll("<Event.*</Event>", "")
                .getBytes("UTF-8");

        // negative test case
        try {
            svcProv.importTechnicalServices(xml);
            fail("Delete priced events must not be possible.");
        } catch (UpdateConstraintException e) {
            assertEquals(DomainObjectException.ClassEnum.TECHNICAL_SERVICE,
                    e.getDomainObjectClassEnum());
            product = svcProv.getServiceDetails(product);
            assertEquals("The existing priced event must not be deleted.", 1,
                    product.getPriceModel().getConsideredEvents().size());
        }

        svcProv.deleteService(product);

        // positive test case
        assertEquals("", svcProv.importTechnicalServices(xml));
        List<VOTechnicalService> techProducts = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals("The existing event definition must be deleted.", 2,
                techProducts.get(0).getEventDefinitions().size());
    }

    // Bug-12684 - locale no longer mandatory
    @Test // (expected = ImportException.class)
    public void testImportTechnicalServicesMissingAccessInfoLocale()
            throws Exception {
        String xml = TSXML.createTSXML("test", "1",
                ProvisioningType.SYNCHRONOUS.name(), "http://someurl", "1.0",
                ServiceAccessType.LOGIN.name(), "http://baseurl", "loginpath",
                new String[] { "", "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    // Bug-12684 - locale no longer mandatory
    @Test // (expected = ImportException.class)
    public void testImportTechnicalServicesMissingDescriptionLocale()
            throws Exception {
        String xml = TSXML.createTSXML("test", "1",
                ProvisioningType.SYNCHRONOUS.name(), "http://someurl", "1.0",
                ServiceAccessType.LOGIN.name(), "http://baseurl", "loginpath",
                new String[] { "en", "", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    // Bug-12684 - locale no longer mandatory
    @Test // (expected = ImportException.class)
    public void testImportTechnicalServicesMissingLicenseLocale()
            throws Exception {
        String xml = TSXML.createTSXML("test", "1",
                ProvisioningType.SYNCHRONOUS.name(), "http://someurl", "1.0",
                ServiceAccessType.LOGIN.name(), "http://baseurl", "loginpath",
                new String[] { "en", "en", "" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesMissingBaseUrl() throws Exception {
        String xml = TSXML.createTSXML("test", "1",
                ProvisioningType.SYNCHRONOUS.name(), "http://someurl", "1.0",
                ServiceAccessType.LOGIN.name(), "", "loginpath",
                new String[] { "en", "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesMissingMandatoryTPAttributes()
            throws Exception {
        String xml = TSXML.createTSXML("", "1", "", "", "", "",
                "http://baseurl", "loginpath",
                new String[] { "en", "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesMissingEventId() throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.STRING.name(), "", "", "", "true", "false",
                "", new String[] { "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesMissingParameterId()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("",
                ParameterValueType.STRING.name(), "", "", "", "true", "false",
                "event", new String[] { "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesMissingEventLocale()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.STRING.name(), "", "", "", "true", "false",
                "event", new String[] { "en", "" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesMissingParameterLocale()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.STRING.name(), "", "", "", "true", "false",
                "event", new String[] { "", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test
    public void testImportTechnicalServicesNonMandatoryParameterAttributes()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.INTEGER.name(), "1", "100", "50", "false",
                "true", "event", new String[] { "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(1, list.size());
        VOTechnicalService tp = list.get(0);
        VOParameterDefinition pd = tp.getParameterDefinitions()
                .get(tp.getParameterDefinitions().size() - 1);
        assertEquals("param", pd.getParameterId());
        assertEquals(ParameterValueType.INTEGER, pd.getValueType());
        assertEquals(1, pd.getMinValue().longValue());
        assertEquals(100, pd.getMaxValue().longValue());
        assertEquals("50", pd.getDefaultValue());
        assertEquals(false, pd.isConfigurable());
        assertEquals(true, pd.isMandatory());
        VOEventDefinition ed = tp.getEventDefinitions()
                .get(tp.getEventDefinitions().size() - 1);
        assertEquals("event", ed.getEventId());
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesDefaultOutOfRange()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.INTEGER.name(), "5", "10", "1", "true",
                "false", "event", new String[] { "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesInvalidDefaultOption()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.ENUMERATION.name(), "", "", "7", "true",
                "false", "event", new String[] { "en", "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesMissiongOptionLocale()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.ENUMERATION.name(), "", "", "1", "true",
                "false", "event", new String[] { "en", "en", "" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test
    public void testImportTechnicalServicesChangedParameterAttributes()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.INTEGER.name(), "1", "100", "50", "false",
                "true", "event", new String[] { "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.INTEGER.name(), "5", "200", "10", "true",
                "false", "event", new String[] { "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(1, list.size());
        VOTechnicalService tp = list.get(0);
        VOParameterDefinition pd = tp.getParameterDefinitions()
                .get(tp.getParameterDefinitions().size() - 1);
        assertEquals("param", pd.getParameterId());
        assertEquals(ParameterValueType.INTEGER, pd.getValueType());
        assertEquals(5, pd.getMinValue().longValue());
        assertEquals(200, pd.getMaxValue().longValue());
        assertEquals("10", pd.getDefaultValue());
        assertEquals(true, pd.isConfigurable());
        assertEquals(false, pd.isMandatory());
        VOEventDefinition ed = tp.getEventDefinitions()
                .get(tp.getEventDefinitions().size() - 1);
        assertEquals("event", ed.getEventId());
    }

    @Test
    public void testImportTechnicalServicesChangedParameterAttributesEmpty()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.INTEGER.name(), "1", "100", "50", "false",
                "true", "event", new String[] { "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.INTEGER.name(), "0", "1000", "", "true",
                "false", "event", new String[] { "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(1, list.size());
        VOTechnicalService tp = list.get(0);
        VOParameterDefinition pd = tp.getParameterDefinitions()
                .get(tp.getParameterDefinitions().size() - 1);
        assertEquals("param", pd.getParameterId());
        assertEquals(ParameterValueType.INTEGER, pd.getValueType());
        assertEquals(Long.valueOf(0), pd.getMinValue());
        assertEquals(Long.valueOf(1000), pd.getMaxValue());
        assertEquals(null, pd.getDefaultValue());
        assertEquals(true, pd.isConfigurable());
        assertEquals(false, pd.isMandatory());
        VOEventDefinition ed = tp.getEventDefinitions()
                .get(tp.getEventDefinitions().size() - 1);
        assertEquals("event", ed.getEventId());
    }

    @Test
    public void testImportTechnicalServicesRemoveEventDescription()
            throws Exception {
        String[] eventIds = new String[] { "Event" };
        // import xml with localized description "en" and "de"
        String[] locales = new String[] { "en", "de" };
        String xml = TSXML.createTSXMLWithEvents(eventIds, locales);
        VOTechnicalService tp = importAndGetTechnicalService(xml);
        VOEventDefinition ed = tp.getEventDefinitions()
                .get(tp.getEventDefinitions().size() - 1);
        // verify the en description is correct
        assertEquals("description_en", ed.getEventDescription());

        // remove "en" localized description
        locales = new String[] { "de" };
        xml = TSXML.createTSXMLWithEvents(eventIds, locales);
        tp = importAndGetTechnicalService(xml);
        ed = tp.getEventDefinitions().get(tp.getEventDefinitions().size() - 1);
        // verify the "en" description is blank
        assertEquals("", ed.getEventDescription());
    }

    @Test
    public void testImportTechnicalServicesRemoveRoleDescription()
            throws Exception {
        String[] roleIds = new String[] { "Role" };
        // import xml with localized description "en" and "de"
        String[] locales = new String[] { "en", "de" };
        String xml = TSXML.createTSXMLWithRoles(roleIds, locales);
        VOTechnicalService tp = importAndGetTechnicalService(xml);
        VORoleDefinition role = tp.getRoleDefinitions().get(0);
        // verify the en description is correct
        assertEquals("description_en", role.getDescription());

        // remove "en" localized description
        locales = new String[] { "de" };
        xml = TSXML.createTSXMLWithRoles(roleIds, locales);
        tp = importAndGetTechnicalService(xml);
        role = tp.getRoleDefinitions().get(0);
        // verify the "en" description is blank
        assertEquals("", role.getDescription());
    }

    @Test
    public void testImportTechnicalServicesRemoveOperationDescription()
            throws Exception {
        String[] opIds = new String[] { "Action" };
        String[] actionUrls = new String[] { "http://someUrl0" };
        // import xml with localized description "en" and "de"
        String[] locales = new String[] { "en", "de" };
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        VOTechnicalService tp = importAndGetTechnicalService(xml);
        VOTechnicalServiceOperation operation = tp
                .getTechnicalServiceOperations().get(0);
        // verify the en description is correct
        assertEquals("description_en", operation.getOperationDescription());

        // remove "en" localized description
        locales = new String[] { "de" };
        xml = TSXML.createTSXMLWithOperations(opIds, actionUrls, locales);
        tp = importAndGetTechnicalService(xml);
        operation = tp.getTechnicalServiceOperations().get(0);
        // verify the "en" description is blank
        assertEquals("", operation.getOperationDescription());
    }

    @Test(expected = EJBAccessException.class)
    public void testImportTechnicalServicesAsSupplier() throws Exception {
        try {
            String xml = TSXML.createTSXMLWithEventAndParameter("param",
                    ParameterValueType.INTEGER.name(), "1", "100", "50",
                    "false", "true", "event", new String[] { "en", "en" });
            container.login(createOrganization(OrganizationRoleType.SUPPLIER));
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void testImportTechnicalServicesAsCustomer() throws Exception {
        try {
            String xml = TSXML.createTSXMLWithEventAndParameter("param",
                    ParameterValueType.INTEGER.name(), "1", "100", "50",
                    "false", "true", "event", new String[] { "en", "en" });
            container.login(createOrganization(OrganizationRoleType.CUSTOMER));
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = TechnicalServiceActiveException.class)
    public void testImportTechnicalServicesUpdateWithActiveSessions()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.INTEGER.name(), "1", "100", "50", "false",
                "true", "event", new String[] { "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        techProdActiveSessions = true;
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test
    public void testImportTechnicalServicesChangeMainAttributes()
            throws Exception {
        String xml = TSXML.createTSXML("test", "1",
                ProvisioningType.SYNCHRONOUS.name(), "http://someurl", "1.0",
                ServiceAccessType.LOGIN.name(), "http://baseurl", "loginpath",
                new String[] { "en", "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        xml = TSXML.createTSXML("test", "1",
                ProvisioningType.ASYNCHRONOUS.name(), "http://provurl", "1.0",
                ServiceAccessType.LOGIN.name(), "http://baseurl", "loginpath",
                new String[] { "en", "en", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(1, list.size());
        final VOTechnicalService tp = list.get(0);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                TechnicalProduct reference = mgr
                        .getReference(TechnicalProduct.class, tp.getKey());
                assertEquals(ProvisioningType.ASYNCHRONOUS,
                        reference.getProvisioningType());
                assertEquals(ServiceAccessType.LOGIN,
                        reference.getAccessType());
                return null;
            }
        });
        assertEquals("http://provurl", tp.getProvisioningUrl());
        assertEquals("1.0", tp.getProvisioningVersion());
    }

    @Test
    public void testVerifyBaseUrlWithoutSlash() throws Exception {
        // given an xml file with a technical service definition
        // with a base url without slash
        String xml = TSXML.createTSXML("test", "1",
                ProvisioningType.ASYNCHRONOUS.name(), "http://provurl", "1.0",
                ServiceAccessType.LOGIN.name(), "http://baseurl", "loginpath",
                new String[] { "en", "en", "en" });

        // when importing the technical service
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        // then
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        final VOTechnicalService tp = list.get(0);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct reference = mgr
                        .getReference(TechnicalProduct.class, tp.getKey());
                assertEquals(ProvisioningType.ASYNCHRONOUS,
                        reference.getProvisioningType());
                assertEquals(ServiceAccessType.LOGIN,
                        reference.getAccessType());
                return null;
            }
        });
        assertEquals("http://provurl", tp.getProvisioningUrl());
        assertEquals("1.0", tp.getProvisioningVersion());
        assertEquals("http://baseurl", tp.getBaseUrl());
        assertEquals("/loginpath", tp.getLoginPath());
    }

    @Test
    public void testVerifyBaseUrlWithoutSlashLoginPathWithSlash()
            throws Exception {
        // given an xml file with a technical service definition
        // with a base url without slash, a login path with slash,
        // and a relative provisioning url
        String xml = TSXML.createTSXML("test", "1",
                ProvisioningType.ASYNCHRONOUS.name(), "/provurl", "1.0",
                ServiceAccessType.USER.name(), "http://baseurl", "/loginpath",
                new String[] { "en", "en", "en" });

        // when importing the technical service
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        // then verify that the provisioning url, base url and login path
        // of the imported technical service have the correct values.
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        final VOTechnicalService tp = list.get(0);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct reference = mgr
                        .getReference(TechnicalProduct.class, tp.getKey());
                assertEquals(ProvisioningType.ASYNCHRONOUS,
                        reference.getProvisioningType());
                assertEquals(ServiceAccessType.USER, reference.getAccessType());
                return null;
            }
        });
        assertEquals("http://baseurl/provurl", tp.getProvisioningUrl());
        assertEquals("1.0", tp.getProvisioningVersion());
        assertEquals("http://baseurl", tp.getBaseUrl());
        assertEquals("/loginpath", tp.getLoginPath());
    }

    @Test
    public void testVerifyBaseUrlWithSlash() throws Exception {
        // given an xml file with a technical service definition
        // with a base url with an ending slash
        String xml = TSXML.createTSXML("test", "1",
                ProvisioningType.ASYNCHRONOUS.name(), "/provurl", "1.0",
                ServiceAccessType.USER.name(), "http://baseurl/", "loginpath",
                new String[] { "en", "en", "en" });

        // when importing the technical service
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        // then verify that the provisioning url, base url and login path
        // of the imported technical service have the correct values.
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);

        final VOTechnicalService tp = list.get(0);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                TechnicalProduct reference = mgr
                        .getReference(TechnicalProduct.class, tp.getKey());
                assertEquals(ProvisioningType.ASYNCHRONOUS,
                        reference.getProvisioningType());
                assertEquals(ServiceAccessType.USER, reference.getAccessType());
                return null;
            }
        });
        assertEquals("http://baseurl/provurl", tp.getProvisioningUrl());
        assertEquals("1.0", tp.getProvisioningVersion());
        assertEquals("http://baseurl/", tp.getBaseUrl());
        assertEquals("/loginpath", tp.getLoginPath());
    }

    @Test
    public void testVerifyBaseUrlAndLoginPathWithSlashes() throws Exception {
        // given an xml file with a technical service definition
        // with both base url and login path with slashes
        String xml = TSXML.createTSXML("test", "1",
                ProvisioningType.ASYNCHRONOUS.name(), "http://provurl", "1.0",
                ServiceAccessType.LOGIN.name(), "http://baseurl/", "/loginpath",
                new String[] { "en", "en", "en" });

        // when importing the technical service
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        // then verify that the provisioning url, base url and login path
        // of the imported technical service have the correct values.
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        final VOTechnicalService tp = list.get(0);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                TechnicalProduct reference = mgr
                        .getReference(TechnicalProduct.class, tp.getKey());
                assertEquals(ProvisioningType.ASYNCHRONOUS,
                        reference.getProvisioningType());
                assertEquals(ServiceAccessType.LOGIN,
                        reference.getAccessType());
                return null;
            }
        });
        assertEquals("http://provurl", tp.getProvisioningUrl());
        assertEquals("1.0", tp.getProvisioningVersion());
        assertEquals("http://baseurl/", tp.getBaseUrl());
        assertEquals("/loginpath", tp.getLoginPath());
    }

    /**
     * Test for trim for IDs.
     * 
     * @throws Exception
     */
    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithRolesWithLeadingBlanks()
            throws Exception {
        final String[] roleIdsBeforeSaving = new String[] { "  ADMIN",
                "GUEST" };
        String[] locales = new String[] { "de", "en" };
        String xml = TSXML.createTSXMLWithRoles(roleIdsBeforeSaving, locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

    }

    /**
     * Test for trim for IDs.
     * 
     * @throws Exception
     */
    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithRolesWithTrailingBlanks()
            throws Exception {
        final String[] roleIdsBeforeSaving = new String[] { "ADMIN",
                "GUEST  " };
        String[] locales = new String[] { "de", "en" };
        String xml = TSXML.createTSXMLWithRoles(roleIdsBeforeSaving, locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

    }

    @Test
    public void testImportTechnicalServicesWithRoles() throws Exception {
        final String[] roleIds = new String[] { "ADMIN", "GUEST" };
        String[] locales = new String[] { "de", "en" };
        String xml = TSXML.createTSXMLWithRoles(roleIds, locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(1, list.size());
        final VOTechnicalService tp = list.get(0);
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    TechnicalProduct reference = mgr
                            .getReference(TechnicalProduct.class, tp.getKey());
                    List<RoleDefinition> roles = reference.getRoleDefinitions();
                    assertNotNull(roles);
                    assertEquals(2, roles.size());
                    Set<String> ids = new HashSet<>(Arrays.asList(roleIds));
                    for (RoleDefinition role : roles) {
                        assertTrue(ids.remove(role.getRoleId()));
                    }
                    assertTrue(ids.isEmpty());
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testImportTechnicalServicesWithRolesRoleId() throws Exception {
        String[] roleIds = new String[] { "AD MIN", "GUEST" };
        String[] locales = new String[] { "de", "en" };
        String xml = TSXML.createTSXMLWithRoles(roleIds, locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithRolesTooLongRoleId()
            throws Exception {
        String[] roleIds = new String[] { TOO_LONG_ID, "GUEST" };
        String[] locales = new String[] { "de", "en" };
        String xml = TSXML.createTSXMLWithRoles(roleIds, locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithRolesTooLongRoleName()
            throws Exception {
        String[] roleIds = new String[] { "GUEST" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithRoles(roleIds, locales);
        xml = xml.replaceAll("name_en", TOO_LONG_NAME);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithRolesMissingLocalizedData()
            throws Exception {
        String[] roleIds = new String[] { "ADMIN", "GUEST" };
        String[] locales = new String[] {};
        String xml = TSXML.createTSXMLWithRoles(roleIds, locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    // //Bug-12684 - locale no longer mandatory
    @Test // (expected = ImportException.class)
    public void testImportTechnicalServicesWithRolesMissingLocale()
            throws Exception {
        String[] roleIds = new String[] { "ADMIN", "GUEST" };
        String[] locales = new String[] { "" };
        String xml = TSXML.createTSXMLWithRoles(roleIds, locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithNonConfigurableParamDefButNoDefault()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.INTEGER.name(), "", "", "", "false", "false",
                "eventId", new String[] { "de", "en" });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithOperationsTooLongOperationId()
            throws Exception {
        String[] opIds = new String[] { TOO_LONG_ID };
        String[] actionUrls = new String[] { "http://someUrl/test" };
        String[] locales = new String[] { "de", "en" };
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithOperationsTooLongOperationParameterId()
            throws Exception {
        String[] opIds = new String[] { "ACTION" };
        String[] actionUrls = new String[] { "http://someUrl/test" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithOperationsAndOperationParams(opIds,
                actionUrls, locales,
                new String[][] { new String[] { TOO_LONG_ID + ":true:"
                        + OperationParameterType.REQUEST_SELECT.name() } });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithOperationsMissingOperationParameterId()
            throws Exception {
        String[] opIds = new String[] { "ACTION" };
        String[] actionUrls = new String[] { "http://someUrl/test" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithOperationsAndOperationParams(opIds,
                actionUrls, locales, new String[][] { new String[] { ":true:"
                        + OperationParameterType.REQUEST_SELECT.name() } });
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithOperationsTooLongOperationName()
            throws Exception {
        String[] opIds = new String[] { "ACTION" };
        String[] actionUrls = new String[] { "http://someUrl/test" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithOperationsAndOperationParams(opIds,
                actionUrls, locales,
                new String[][] { new String[] { "param1:true:"
                        + OperationParameterType.REQUEST_SELECT.name() } });
        xml = xml.replaceAll("parametername_en", TOO_LONG_NAME);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithOperationsTooLongOperationParameterName()
            throws Exception {
        String[] opIds = new String[] { "ACTION" };
        String[] actionUrls = new String[] { "http://someUrl/test" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        xml = xml.replaceAll("name_en", TOO_LONG_NAME);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithOperationsMissingLocalizedData()
            throws Exception {
        String[] opIds = new String[] { "ACTION" };
        String[] actionUrls = new String[] { "http://someUrl/test" };
        String[] locales = new String[] {};
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    // Bug-12684 - locale no longer mandatory
    @Test // (expected = ImportException.class)
    public void testImportTechnicalServicesWithOperationsMissingLocale()
            throws Exception {
        String[] opIds = new String[] { "ACTION" };
        String[] actionUrls = new String[] { "http://someUrl/test" };
        String[] locales = new String[] { "" };
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithOperationsMissingAction()
            throws Exception {
        String[] opIds = new String[] { "ACTION" };
        String[] actionUrls = new String[] { "" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithOperationsParameterMissingId()
            throws Exception {
        String[] opIds = new String[] { "ACTION" };
        String[] actionUrls = new String[] { "" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
    }

    @Test
    public void testImportTechnicalServicesWithOperations() throws Exception {
        String[] opIds = new String[] { "ACTION0", "ACTION1" };
        String[] actionUrls = new String[] { "http://someUrl0",
                "http://someUrl1" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(1, list.size());
        final VOTechnicalService tp = list.get(0);
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    TechnicalProduct reference = mgr
                            .getReference(TechnicalProduct.class, tp.getKey());
                    List<TechnicalProductOperation> operations = reference
                            .getTechnicalProductOperations();
                    assertNotNull(operations);
                    assertEquals(2, operations.size());
                    for (int index = 0; index < operations.size(); index++) {
                        TechnicalProductOperation op = operations.get(index);
                        assertEquals("ACTION" + index, op.getOperationId());
                        assertEquals("http://someUrl" + index,
                                op.getActionUrl());
                    }
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
        List<VOTechnicalServiceOperation> operations = tp
                .getTechnicalServiceOperations();
        assertNotNull(operations);
        assertEquals(2, operations.size());
        for (int index = 0; index < operations.size(); index++) {
            VOTechnicalServiceOperation op = operations.get(index);
            assertEquals("ACTION" + index, op.getOperationId());
            assertEquals("description_en", op.getOperationDescription());
            assertEquals("name_en", op.getOperationName());
        }
    }

    @Test
    public void testImportTechnicalServicesWithOperationsAndOperationParameters()
            throws Exception {
        String[] opIds = new String[] { "ACTION0", "ACTION1" };
        String[] actionUrls = new String[] { "http://someUrl0",
                "http://someUrl1" };
        String[] locales = new String[] { "en" };
        String[][] params = new String[][] {
                new String[] {
                        "param1:true:"
                                + OperationParameterType.REQUEST_SELECT.name(),
                        "param2:false:"
                                + OperationParameterType.INPUT_STRING.name() },
                new String[] { "param1:false:"
                        + OperationParameterType.INPUT_STRING.name() } };
        String xml = TSXML.createTSXMLWithOperationsAndOperationParams(opIds,
                actionUrls, locales, params);
        final VOTechnicalService tp = importAndGetTechnicalService(xml);
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    TechnicalProduct reference = mgr
                            .getReference(TechnicalProduct.class, tp.getKey());
                    List<TechnicalProductOperation> operations = reference
                            .getTechnicalProductOperations();
                    assertNotNull(operations);
                    assertEquals(2, operations.size());

                    TechnicalProductOperation tpo = operations.get(0);
                    List<OperationParameter> parameters = tpo.getParameters();
                    assertEquals(2, parameters.size());
                    validateOperationParameter(parameters.get(0), "param1",
                            true, OperationParameterType.REQUEST_SELECT, "en");
                    validateOperationParameter(parameters.get(1), "param2",
                            false, OperationParameterType.INPUT_STRING, "en");

                    tpo = operations.get(1);
                    parameters = tpo.getParameters();
                    assertEquals(1, parameters.size());
                    validateOperationParameter(parameters.get(0), "param1",
                            false, OperationParameterType.INPUT_STRING, "en");
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    /**
     * Only to be used within a transaction
     */
    protected void validateOperationParameter(OperationParameter op, String id,
            boolean mandatory, OperationParameterType type, String locale) {
        assertEquals(id, op.getId());
        assertEquals(mandatory, op.isMandatory());
        assertEquals(type, op.getType());
        assertTrue(op.getKey() > 0);
        String text = localizer.getLocalizedTextFromDatabase(locale,
                op.getKey(),
                LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_PARAMETER_NAME);
        assertEquals("parametername_" + locale, text);
    }

    @Test
    public void testBug11709() throws Exception {
        final String INIT_VALUE = "English description";
        final String EMPTY_STRING = "   ";
        final String CHANGED_VALUE = "Changed description";

        VOTechnicalService tp = importAndGetTechnicalService(
                String.format(LOCALIZED_TECHNICAL_SERVICE_XML, INIT_VALUE));
        assertEquals(tp.getTechnicalServiceDescription(), INIT_VALUE);

        tp = importAndGetTechnicalService(
                String.format(LOCALIZED_TECHNICAL_SERVICE_XML, EMPTY_STRING));
        assertEquals(tp.getTechnicalServiceDescription(), INIT_VALUE);

        tp = importAndGetTechnicalService(
                String.format(LOCALIZED_TECHNICAL_SERVICE_XML, CHANGED_VALUE));
        assertEquals(tp.getTechnicalServiceDescription(), CHANGED_VALUE);
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithOperations_DuplicateOperationIds()
            throws Exception {
        String[] opIds = new String[] { "ACTION1", "ACTION1" };
        String[] actionUrls = new String[] { "http://someUrl0",
                "http://someUrl1" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails()
                    .indexOf("Duplicate operation id: ACTION1") > 0);
            throw e;
        }
    }

    @Test
    public void testImportTechnicalServicesWithOperations_TwoTPsWithSameOperations()
            throws Exception {
        String[] opIds = new String[] { "ACTION0", "ACTION1" };
        String[] actionUrls = new String[] { "http://someUrl0",
                "http://someUrl1" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        xml = xml + xml.replaceAll("tp1", "tp2");
        xml = xml.replaceAll(
                "</tns:TechnicalServices><tns:TechnicalServices xmlns:tns=\"oscm.serviceprovisioning/1.9/TechnicalService.xsd\">",
                "");
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        final List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(2, list.size());
        try {
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    for (VOTechnicalService tp : list) {
                        TechnicalProduct reference = mgr.getReference(
                                TechnicalProduct.class, tp.getKey());
                        List<TechnicalProductOperation> operations = reference
                                .getTechnicalProductOperations();
                        assertNotNull(operations);
                        assertEquals(2, operations.size());
                        for (int index = 0; index < operations
                                .size(); index++) {
                            TechnicalProductOperation op = operations
                                    .get(index);
                            assertEquals("ACTION" + index, op.getOperationId());
                            assertEquals("http://someUrl" + index,
                                    op.getActionUrl());
                        }
                    }
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
        for (VOTechnicalService tp : list) {
            List<VOTechnicalServiceOperation> operations = tp
                    .getTechnicalServiceOperations();
            assertNotNull(operations);
            assertEquals(2, operations.size());
            for (int index = 0; index < operations.size(); index++) {
                VOTechnicalServiceOperation op = operations.get(index);
                assertEquals("ACTION" + index, op.getOperationId());
                assertEquals("description_en", op.getOperationDescription());
                assertEquals("name_en", op.getOperationName());
            }
        }
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServices_DuplicateOperationId()
            throws Exception {
        String[] opIds = new String[] { "ACTION", "ACTION" };
        String[] actionUrls = new String[] { "http://someUrl0",
                "http://someUrl1" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithOperations(opIds, actionUrls,
                locales);
        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails()
                    .indexOf("Duplicate operation id: ACTION") > 0);
            throw e;
        }
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServices_DuplicateOperationParameterId()
            throws Exception {
        String[] opIds = new String[] { "ACTION" };
        String[] actionUrls = new String[] { "http://someUrl0", };
        String[] locales = new String[] { "en" };
        String[][] params = new String[][] { new String[] {
                "param1:true:" + OperationParameterType.REQUEST_SELECT.name(),
                "param1:false:"
                        + OperationParameterType.INPUT_STRING.name() } };
        String xml = TSXML.createTSXMLWithOperationsAndOperationParams(opIds,
                actionUrls, locales, params);
        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails()
                    .indexOf("Duplicate operation parameter id: param1") > 0);
            throw e;
        }
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServices_DuplicateRoleId() throws Exception {
        String[] roleIds = new String[] { "ADMIN", "ADMIN" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithRoles(roleIds, locales);
        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails().indexOf("Duplicate role id: ADMIN") > 0);
            throw e;
        }
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServices_DuplicateEventId()
            throws Exception {
        String[] eventIds = new String[] { "EVENT", "EVENT" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithEvents(eventIds, locales);
        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails().indexOf("Duplicate event id: EVENT") > 0);
            throw e;
        }
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServices_DuplicateParameterId()
            throws Exception {
        String[] parameterIds = new String[] { "PARAM", "PARAM" };
        ParameterValueType[] types = new ParameterValueType[] {
                ParameterValueType.BOOLEAN, ParameterValueType.STRING };
        String[] minMaxDefault = new String[] { "", "" };
        boolean[] confMand = new boolean[] { true, true };
        String[] optionIds = new String[0];
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithParameters(parameterIds, types,
                minMaxDefault, minMaxDefault, minMaxDefault, confMand, confMand,
                optionIds, locales);
        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails()
                    .indexOf("Duplicate parameter id: PARAM") > 0);
            throw e;
        }
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServices_DuplicateOptionId()
            throws Exception {
        String[] parameterIds = new String[] { "PARAM" };
        ParameterValueType[] types = new ParameterValueType[] {
                ParameterValueType.ENUMERATION };
        String[] minValue = new String[] { "0" };
        String[] maxValue = new String[] { "1000" };
        String[] defaultValue = new String[] { "" };
        boolean[] confMand = new boolean[] { true };
        String[] optionIds = new String[] { "1", "1" };
        String[] locales = new String[] { "en" };
        String xml = TSXML.createTSXMLWithParameters(parameterIds, types,
                minValue, maxValue, defaultValue, confMand, confMand, optionIds,
                locales);
        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails().indexOf("Duplicate option id: 1") > 0);
            throw e;
        }
    }

    // corresponds to bug 6552
    @Test
    public void testImportTechnicalServices_DuplicateServiceOneFile()
            throws Exception {
        String fileName = "Bug6552.xml";
        svcProv.importTechnicalServices(readBytesFromFile(fileName));

        List<VOTechnicalService> technicalServices = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(1, technicalServices.size());
        VOTechnicalService voTechnicalService = technicalServices.get(0);
        List<VOParameterDefinition> parameterDefinitions = voTechnicalService
                .getParameterDefinitions();
        int numberOfPlatformParams = 3;
        assertEquals(7 + numberOfPlatformParams, parameterDefinitions.size());
    }

    /**
     * Corresponds to bug 6633 - the parser must realize when a new technical
     * service element is read. However, in this case the endElement method id
     * not called, so the internal collection members are not cleared. Hence Ids
     * of one service might interfere with those of another one. Bug is to
     * reproduce this scenario and to ensure that this does not happen. There is
     * no persistence problem, but a check that no service was created has been
     * added.
     */
    @Test
    public void testImportTechnicalServices_MultipleEntriesWrongParamId()
            throws Exception {
        String fileName = "TwoServicesWrongParamId.xml";
        try {
            svcProv.importTechnicalServices(readBytesFromFile(fileName));
            fail("Must fail as parameter id is wrong");
        } catch (ImportException e) {
            String details = e.getDetails();
            assertNotNull(details);
            assertTrue(details.startsWith("21: cvc-pattern-valid"));
            assertFalse(details.contains("Dupli"));
        }
        List<VOTechnicalService> technicalServices = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(0, technicalServices.size());
    }

    /**
     * Corresponds to bug 6633 - see above method
     * {@link #testImportTechnicalServices_MultipleEntriesWrongParamId()} - here
     * no duplicate option must be detected, as it belongs to another service.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalServices_MultipleEntriesWrongParamIdOptionIdClash()
            throws Exception {
        String fileName = "TwoServicesWrongParamIdOptionConflict.xml";
        try {
            svcProv.importTechnicalServices(readBytesFromFile(fileName));
            fail("Must fail as parameter id is wrong");
        } catch (ImportException e) {
            String details = e.getDetails();
            assertNotNull(details);
            assertTrue(details.startsWith("22: cvc-pattern-valid: "));
            assertFalse(details.contains("Option: Dupli"));
        }
        List<VOTechnicalService> technicalServices = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(0, technicalServices.size());
    }

    /**
     * Try to change the 'onlyOneSubscriptionAllowed' flag with multiple
     * subscriptions already existing. Must fail.
     * 
     * @throws Exception
     */
    @Test(expected = TechnicalServiceMultiSubscriptions.class)
    public void testImportTechnicalServices_changeFlagWithContraintConflict()
            throws Exception {

        // given technical service with two subscriptions
        String tsxml = TSXML.createTSXMLWithSubscriptionRestriction("false");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        createTwoSubscriptions();

        // then re-import with 'onlyOneSubscriptionAllowed' must fail
        tsxml = TSXML.createTSXMLWithSubscriptionRestriction("true");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }

    void createTwoSubscriptions() throws Exception {
        // create subscriptions
        runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                Product prod = createProduct(techPrd, "myProd", org);
                createSubscription(prod, "sub1", org,
                        SubscriptionStatus.ACTIVE);
                createSubscription(prod, "sub2", org,
                        SubscriptionStatus.ACTIVE);
                return null;
            }
        });
    }

    /**
     * The re-import of a technical service must not fail in case the
     * 'onlyOneSubscriptionAllowed' flag is false, even if more than one
     * subscription exists.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalServices_keepFlag() throws Exception {

        // given technical service with two subscriptions
        String tsxml = TSXML.createTSXMLWithSubscriptionRestriction("false");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        createTwoSubscriptions();

        // then re-import without 'onlyOneSubscriptionAllowed' must not fail
        tsxml = TSXML.createTSXMLWithSubscriptionRestriction("false");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }

    /**
     * Changing 'onlyOneSubscriptionAllowed' must be possible if no or only one
     * subscription exists.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalServices_changeOnlyOneSubsriptionFlag()
            throws Exception {

        // given technical service with one subscription
        String tsxml = TSXML.createTSXMLWithSubscriptionRestriction("false");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        createOneSubscription(SubscriptionStatus.ACTIVE);

        // then re-import with 'onlyOneSubscriptionAllowed' flag must not fail
        tsxml = TSXML.createTSXMLWithSubscriptionRestriction("true");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }

    /**
     * Changing 'allowingOnBehalfActing' must be possible if no subscription
     * exists.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalServices_changeOnBehalfActingFlagNoSubscription()
            throws Exception {

        // given technical service with one subscription
        String tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("false");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));

        // then changing with 'allowingOnBehalfActing' from false to true must
        // not fail
        tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("true");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                assertTrue(techPrd.isAllowingOnBehalfActing());
                return null;
            }
        });

        // then changing with 'allowingOnBehalfActing' from true to false must
        // not fail
        tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("false");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                assertEquals(false, techPrd.isAllowingOnBehalfActing());
                return null;
            }
        });
    }

    /**
     * The re-import of a technical service must not fail in case the
     * 'allowingOnBehalfActing' flag is false, even if one subscription exists.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalServices_keepOnBehalfActingFlagWithSubscription()
            throws Exception {

        // given technical service with one subscription
        String tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("true");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        createOneSubscription(SubscriptionStatus.ACTIVE);

        // then re-import with 'allowingOnBehalfActing' must not fail
        tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("true");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                assertTrue(techPrd.isAllowingOnBehalfActing());
                return null;
            }
        });
    }

    /**
     * Try to change the 'allowingOnBehalfActing' flag from 'false' to 'true'
     * with one subscription already existing. Must fail.
     * 
     * @throws Exception
     */
    @Test(expected = UnchangeableAllowingOnBehalfActingException.class)
    public void testImportTechnicalServices_changeOnBehalfActingFlagToTrueWithSubscription()
            throws Exception {

        // given technical service with one subscription
        String tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("false");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        createOneSubscription(SubscriptionStatus.ACTIVE);

        // then re-import with 'allowingOnBehalfActing' must fail
        tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("true");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }

    /**
     * Try to change the 'allowingOnBehalfActing' flag from 'false' to 'true'
     * with one 'DEACTIVATED' subscription already existing. Must successful.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalServices_changeOnBehalfActingFlagToTrueWithDeactivatedSubscription()
            throws Exception {

        // given technical service with one subscription
        String tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("false");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        createOneSubscription(SubscriptionStatus.DEACTIVATED);

        // then re-import with 'allowingOnBehalfActing' must fail
        tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("true");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }

    /**
     * Try to change the 'allowingOnBehalfActing' flag from 'false' to 'true'
     * with one 'INVALID' subscription already existing. Must successful.
     * 
     * @throws Exception
     */
    @Test
    public void testImportTechnicalServices_changeOnBehalfActingFlagToTrueWithInvalidSubscription()
            throws Exception {

        // given technical service with one subscription
        String tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("true");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        createOneSubscription(SubscriptionStatus.INVALID);

        // then re-import with 'allowingOnBehalfActing' must fail
        tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("false");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }

    /**
     * Try to change the 'allowingOnBehalfActing' flag from 'true' to 'false'
     * with one subscription already existing. Must fail.
     * 
     * @throws Exception
     */
    @Test(expected = UnchangeableAllowingOnBehalfActingException.class)
    public void testImportTechnicalServices_changeOnBehalfActingFlagToFalseWithSubscription()
            throws Exception {

        // given technical service with one subscription
        String tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("true");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        createOneSubscription(SubscriptionStatus.ACTIVE);

        // then re-import with 'allowingOnBehalfActing' must fail
        tsxml = TSXML.createTSXMLWithAllowingOnBehalfActing("false");
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }

    void createOneSubscription(final SubscriptionStatus status)
            throws Exception {
        // create subscriptions
        runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                Product prod = createProduct(techPrd, "myProd", org);
                createSubscription(prod, "sub1", org, status);
                return null;
            }
        });
    }

    Product createProduct(TechnicalProduct tProd, String productId,
            Organization supplier)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Product prod = Products.createProduct(supplier.getOrganizationId(),
                productId, tProd.getTechnicalProductId(), mgr);
        return prod;
    }

    Subscription createSubscription(Product prod, String subscriptionId,
            Organization org, SubscriptionStatus status)
            throws NonUniqueBusinessKeyException {
        Subscription sub = new Subscription();
        sub.setCreationDate(L_TIMESTAMP);
        sub.setStatus(status);
        sub.setSubscriptionId(subscriptionId);
        sub.setOrganization(org);
        sub.bindToProduct(prod);
        sub.setCutOffDay(1);
        sub.setUuid(UUID.randomUUID());
        mgr.persist(sub);
        return sub;
    }

    private byte[] getXmlWithModifiedLicense()
            throws UnsupportedEncodingException {
        return TECHNICAL_SERVICES_XML.replaceAll(">LocalizedLicense<", "><")
                .getBytes("UTF-8");
    }

    private byte[] getXmlWithModifiedParameterDefs()
            throws UnsupportedEncodingException {
        return TECHNICAL_SERVICES_XML.replaceAll("INTEGER", "LONG")
                .getBytes("UTF-8");
    }

    private byte[] getXmlWithoutParameterDefs()
            throws UnsupportedEncodingException {
        return TECHNICAL_SERVICES_XML
                .replaceAll("<ParameterDefinition.*</ParameterDefinition>", "")
                .getBytes("UTF-8");
    }

    private byte[] getXmlWithoutOptions() throws UnsupportedEncodingException {
        String xml = TECHNICAL_SERVICES_XML
                .replaceAll("<Option id=\\\"1.*?</Option>", "");
        xml = xml.replaceAll("<Option id=\\\"3.*?</Option>", "");
        return xml.getBytes("UTF-8");
    }

    private byte[] getXmlWithoutEvents() throws UnsupportedEncodingException {
        return TECHNICAL_SERVICES_XML.replaceAll("<Event.*</Event>", "")
                .getBytes("UTF-8");
    }

    private VOTechnicalService getTechnicalProduct() throws Exception {
        return svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                .get(0);
    }

    private VOTechnicalService createTechnicalProduct() throws Exception {
        String rc = svcProv.importTechnicalServices(
                TECHNICAL_SERVICES_XML.getBytes("UTF-8"));
        assertEquals("", rc);
        return getTechnicalProduct();
    }

    private byte[] createTestXml() throws UnsupportedEncodingException {
        int entities = 3;
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version='1.0' encoding='UTF-8' ?>\n");
        buffer.append("<!DOCTYPE root [ \n");
        buffer.append("<!ELEMENT root (#PCDATA)>\n");
        buffer.append("<!ENTITY ha0 \"Ha !\">\n");
        for (int i = 1; i <= entities; i++) {
            buffer.append("<!ENTITY ha");
            buffer.append(i);
            buffer.append("  \"&ha");
            buffer.append(i - 1);
            buffer.append(";&ha");
            buffer.append(i - 1);
            buffer.append(";\" >\n");
        }
        buffer.append("]>\n");
        buffer.append("<root>&ha");
        buffer.append(entities);
        buffer.append(";</root>");
        return buffer.toString().getBytes("UTF-8");
    }

    private VOServiceDetails createProduct(VOTechnicalService technicalProduct,
            String id) throws Exception {
        VOService product = new VOService();
        product.setServiceId(id);
        return svcProv.createService(technicalProduct, product, null);
    }

    private VOServiceDetails createProductWithParm(
            VOTechnicalService technicalProduct, String id) throws Exception {
        VOService product = new VOService();
        product.setServiceId(id);
        List<VOParameter> paramList = new ArrayList<>();
        for (VOParameterDefinition paramDef : technicalProduct
                .getParameterDefinitions()) {
            if (paramDef.getParameterId().equals("MAX_FILE_NUMBER")) {
                VOParameter param = new VOParameter(paramDef);
                param.setValue("10");
                param.setConfigurable(false);
                paramList.add(param);
            }
        }
        product.setParameters(paramList);
        return svcProv.createService(technicalProduct, product, null);
    }

    private VOServiceDetails createProductWithPricedEvent(
            VOTechnicalService technicalProduct, String id) throws Exception {
        VOServiceDetails product = createProduct(technicalProduct, id);
        List<VOPricedEvent> pricedEventList = new ArrayList<>();
        for (VOEventDefinition event : technicalProduct.getEventDefinitions()) {
            if (event.getEventId().equals("FILE_DOWNLOAD")) {
                VOPricedEvent pricedEvent = new VOPricedEvent(event);
                pricedEvent.setEventPrice(BigDecimal.valueOf(1000));
                pricedEventList.add(pricedEvent);
            }
        }
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setConsideredEvents(pricedEventList);
        priceModel.setSelectedParameters(new ArrayList<VOPricedParameter>());
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setPeriod(PricingPeriod.MONTH);
        product.setParameters(new ArrayList<VOParameter>());
        svcProv.savePriceModel(product, priceModel);
        return svcProv.getServiceDetails(product);
    }

    private long createOrganization(final OrganizationRoleType... roles)
            throws Exception {
        return runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Organization organization = Organizations
                        .createOrganization(mgr, roles);
                return Long.valueOf(Organizations
                        .createUserForOrg(mgr, organization, true, "admin")
                        .getKey());
            }
        }).longValue();
    }

    @Test
    public void testImportTechnicalServicesWithTags() throws Exception {
        String[] tagValues = new String[] { "speicher", "storage" };
        String[] locales = new String[] { "de", "en" };
        String xml = TSXML.createTSXMLWithTags(tagValues, locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        // validate result
        runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                validateTags(techPrd, new String[] { "de", "en" },
                        new String[] { "speicher", "storage" });
                return null;
            }
        });
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithDuplicateTags()
            throws Exception {
        String[] tagValues = new String[] { "storage", "storage" };
        String[] locales = new String[] { "en", "en" };
        String xml = TSXML.createTSXMLWithTags(tagValues, locales);

        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails()
                    .indexOf("Duplicate tag storage for locale en") > 0);
            throw e;
        }
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServicesWithTooManyTags() throws Exception {
        String[] tagValues = new String[] { "tag1", "tag2", "tag3", "tag4",
                "tag5", "tag6" };
        String[] locales = new String[] { "en", "en", "en", "en", "en", "en" };
        String xml = TSXML.createTSXMLWithTags(tagValues, locales);

        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails().indexOf("TAGS_MAX_COUNT") > 0);
            throw e;
        }
    }

    @Test
    public void testImportTechnicalServicesMultipleTags() throws Exception {
        String[] tagValues = new String[] { "tag1", "tag2", "tag3", "tag4",
                "de_tag5", "tag6" };
        String[] locales = new String[] { "en", "en", "en", "en", "de", "en" };
        String xml = TSXML.createTSXMLWithTags(tagValues, locales);

        // This import must work! (all together there are more than 5 tags
        // defined, but within every local <= 5)
        // This must not fail!
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        // validate result
        runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                validateTags(techPrd,
                        new String[] { "en", "en", "en", "en", "de", "en" },
                        new String[] { "tag1", "tag2", "tag3", "tag4",
                                "de_tag5", "tag6" });
                return null;
            }
        });
    }

    @Test
    public void testReimportTechnicalServicesWithChangedTags()
            throws Exception {
        String[] tagValues = new String[] { "speicher", "storage" };
        String[] locales = new String[] { "de", "en" };
        String xml = TSXML.createTSXMLWithTags(tagValues, locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        String[] tagValuesUpdated = new String[] { "speicher", "huge storage" };
        String[] localesUpdated = new String[] { "de", "en" };
        xml = TSXML.createTSXMLWithTags(tagValuesUpdated, localesUpdated);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        // validate result
        runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                validateTags(techPrd, new String[] { "de", "en" },
                        new String[] { "speicher", "huge storage" });
                return null;
            }
        });

    }

    @Test
    public void testReimportTechnicalServicesWithNoTags() throws Exception {
        String[] tagValues = new String[] { "speicher", "storage" };
        String[] locales = new String[] { "de", "en" };
        String xml = TSXML.createTSXMLWithTags(tagValues, locales);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        String[] tagValuesUpdated = new String[0];
        String[] localesUpdated = new String[0];
        xml = TSXML.createTSXMLWithTags(tagValuesUpdated, localesUpdated);
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));

        // validate result
        runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        providerOrganizationId);
                TechnicalProduct techPrd = org.getTechnicalProducts().get(0);
                validateTags(techPrd, new String[0], new String[0]);
                return null;
            }
        });

    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServices_AccessType_DIRECT_NullAccessInfo()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.INTEGER.name(), "1", "100", "50", "false",
                "true", "event", new String[] { "en", "en" });
        xml = xml.replaceAll(
                "<AccessInfo locale=\"en\">AccessInfo</AccessInfo>",
                "<AccessInfo locale=\"en\"></AccessInfo>");
        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails().indexOf(
                    "AccessInfo is required if accessType is DIRECT") > 0);
            throw e;
        }
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServices_AccessType_DIRECT_MissingAccessInfo()
            throws Exception {
        String xml = TSXML.createTSXMLWithEventAndParameter("param",
                ParameterValueType.INTEGER.name(), "1", "100", "50", "false",
                "true", "event", new String[] { "en", "en" });
        xml = xml.replaceAll(
                "<AccessInfo locale=\"en\">AccessInfo</AccessInfo>", "");
        try {
            svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails().indexOf(
                    "AccessInfo is required if accessType is DIRECT") > 0);
            throw e;
        }
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServices_AccessType_USER_NullAccessInfo()
            throws Exception {
        String tsxml = TSXML.createTSXML("test", "1",
                ProvisioningType.SYNCHRONOUS.name(), "http://someurl", "1.0",
                ServiceAccessType.USER.name(), "http://baseurl", "loginpath",
                new String[] { "en", "en", "en" });
        tsxml = tsxml.replaceAll(
                "<AccessInfo locale=\"en\">AccessInfo</AccessInfo>",
                "<AccessInfo locale=\"en\"></AccessInfo>");
        try {
            svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails().indexOf(
                    "AccessInfo is required if accessType is USER") > 0);
            throw e;
        }
    }

    @Test(expected = ImportException.class)
    public void testImportTechnicalServices_AccessType_USER_MissingAccessInfo()
            throws Exception {
        String tsxml = TSXML.createTSXML("test", "1",
                ProvisioningType.SYNCHRONOUS.name(), "http://someurl", "1.0",
                ServiceAccessType.USER.name(), "http://baseurl", "loginpath",
                new String[] { "en", "en", "en" });
        tsxml = tsxml.replaceAll(
                "<AccessInfo locale=\"en\">AccessInfo</AccessInfo>", "");
        try {
            svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
        } catch (ImportException e) {
            assertTrue(e.getDetails().indexOf(
                    "AccessInfo is required if accessType is USER") > 0);
            throw e;
        }
    }

    /**
     * Validates the defined tags of the given technical product.
     * 
     * @param techProduct
     *            the technical product
     * @param locales
     *            the expected locales
     * @param tags
     *            the expected tag values related to the given locales
     */
    private void validateTags(TechnicalProduct techProduct, String[] locales,
            String[] tags) {
        // Get all defined tags of the product
        List<TechnicalProductTag> result = techProduct.getTags();
        assertEquals(locales.length, result.size());

        // Check whether all tags are contained in the list
        for (int i = 0; i < locales.length; i++) {
            boolean isInList = isTagInList(result, locales[i], tags[i]);
            assertTrue("Expected tag not found", isInList);
        }
    }

    /**
     * Checks whether given tag is part of the list.
     */
    private boolean isTagInList(final List<TechnicalProductTag> list,
            String locale, String value) {
        Iterator<TechnicalProductTag> iter = list.iterator();
        while (iter.hasNext()) {
            TechnicalProductTag exTag = iter.next();
            if (exTag.getTag().getLocale().equals(locale)
                    && exTag.getTag().getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    private void createMarketingPermission(final long tpKey,
            final long orgRefKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct technicalProduct = mgr
                        .find(TechnicalProduct.class, tpKey);
                OrganizationReference reference = mgr
                        .find(OrganizationReference.class, orgRefKey);

                MarketingPermission permission = new MarketingPermission();
                permission.setOrganizationReference(reference);
                permission.setTechnicalProduct(technicalProduct);
                mgr.persist(permission);
                return null;
            }
        });
    }

    private OrganizationReference createOrgRef(final long orgKey)
            throws Exception {
        return runTX(new Callable<OrganizationReference>() {
            @Override
            public OrganizationReference call() throws Exception {
                Organization organization = mgr.find(Organization.class,
                        orgKey);
                OrganizationReference orgRef = new OrganizationReference(
                        organization, organization,
                        OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
                mgr.persist(orgRef);
                return orgRef;
            }
        });
    }

    private VOTechnicalService importAndGetTechnicalService(String xml)
            throws Exception {
        svcProv.importTechnicalServices(xml.getBytes("UTF-8"));
        List<VOTechnicalService> list = svcProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        assertEquals(1, list.size());
        VOTechnicalService tp = list.get(0);
        return tp;
    }

    private String anyTSXMLWithIntParameters() {
        String[] parameterIds = new String[] { "PARAM2" };
        ParameterValueType[] types = new ParameterValueType[] {
                ParameterValueType.INTEGER };
        String[] minValue = new String[] { "0" };
        String[] maxValue = new String[] { "1000" };
        String[] defaultValue = new String[] { "2" };
        boolean[] confMand = new boolean[] { true };
        String[] optionIds = new String[] {};

        return TSXML.createTSXMLWithParameters(parameterIds, types, minValue,
                maxValue, defaultValue, confMand, confMand, optionIds,
                new String[] { "en" });
    }

    @Test
    public void testImportTechnicalService_NativeBilling() throws Exception {
        // given
        String tsxml = TSXML.createTSXMLWithBillingIdentifier(
                BillingAdapterIdentifier.NATIVE_BILLING.toString() + "   ");

        // when
        VOTechnicalService voTechService = importAndGetTechnicalService(tsxml);

        // then
        assertEquals(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                voTechService.getBillingIdentifier());
    }

    @Test
    public void testImportTechnicalService_WithOutBilling() throws Exception {
        // given
        String tsxml = TSXML.createTSXMLWithOutBillingIdentifier();

        // when
        VOTechnicalService voTechService = importAndGetTechnicalService(tsxml);

        // then
        assertEquals(BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                voTechService.getBillingIdentifier());
    }

    @Test
    public void testImportTechnicalService_MultipleDefaultAdapters()
            throws Exception {
        // given
        String tsxml = TSXML.createTSXMLWithOutBillingIdentifier();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BillingAdapters.createBillingAdapter(mgr, "FILE_BILLING", true);
                return null;
            }
        });

        // when
        try {
            importAndGetTechnicalService(tsxml);
            fail("ImportException expected.");
        } catch (Exception e) {
            // then
            assertTrue(e.getCause() instanceof SaaSSystemException);
            assertTrue("Unexpected error:" + e.getLocalizedMessage(),
                    e.getLocalizedMessage().indexOf(
                            "More than one default billing adapter were found") > 0);
        }

    }

    @Test(expected = BillingAdapterNotFoundException.class)
    public void testImportTechnicalService_NonExistingBilling()
            throws Exception {
        // given
        String tsxml = TSXML
                .createTSXMLWithBillingIdentifier("some identifier");

        // when
        svcProv.importTechnicalServices(tsxml.getBytes("UTF-8"));
    }
}
