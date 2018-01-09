/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.ws.base;

import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.oscm.test.setup.PropertiesReader;
import org.oscm.types.constants.Configuration;
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.intf.AccountService;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.intf.SubscriptionService;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.enumtypes.PaymentInfoType;
import org.oscm.types.enumtypes.PriceModelType;
import org.oscm.types.enumtypes.TriggerTargetType;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOBillingContact;
import org.oscm.vo.VOCatalogEntry;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOPaymentInfo;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUserDetails;

public class WebserviceTestBase {

    private static int counter = 0;

    public static final String DEFAULT_PASSWORD = "secret";
    public static final String CURRENCY_EUR = "EUR";
    public static final String EXAMPLE_BASE_URL = "example.https.url";
    public static final String CS_MAIL_ADDRESS = "mail.address";
    public static final String CS_MAIL_USERNAME = "mail.username";

    private static OperatorService operator;
    private static MailReader mailReader;
    private static String platformOperatorKey;
    private static String platformOperatorPassword;
    private static Properties configSettings;
    private static String mailAddress;
    private static VOMarketplace globalMarketplace;
    private static VOFactory factory = new VOFactory();

    public static OperatorService getOperator() throws Exception {
        synchronized (WebserviceTestBase.class) {
            if (operator == null) {
                operator = ServiceFactory.getDefault().getOperatorService();
            }
        }
        return operator;
    }

    public static MailReader getMailReader() throws Exception {
        synchronized (WebserviceTestBase.class) {
            if (mailReader == null) {
                mailReader = new MailReader();
            }
            if (mailAddress == null) {
                mailAddress = getConfigSetting(CS_MAIL_ADDRESS);
            }
            if (mailAddress == null || mailAddress.trim().equals("")) {
                mailAddress = getConfigSetting(CS_MAIL_USERNAME);
            }
        }
        return mailReader;
    }

    public static String getPlatformOperatorKey() throws Exception {
        ensureProperties();
        return platformOperatorKey;
    }

    public static String getPlatformOperatorPassword() throws Exception {
        ensureProperties();
        return platformOperatorPassword;
    }

    public static String getConfigSetting(String key) throws Exception {
        if (configSettings == null) {
            ensureProperties();
        }
        return configSettings.getProperty(key);
    }

    public static Properties getConfigSetting() throws Exception {
        if (configSettings == null) {
            ensureProperties();
        }
        return configSettings;
    }

    private static void ensureProperties() throws Exception {
        synchronized (WebserviceTestBase.class) {
            if (platformOperatorKey == null) {
                PropertiesReader reader = new PropertiesReader();
                configSettings = reader.load();
                platformOperatorKey = configSettings
                        .getProperty("DEFAULT_USER");
                platformOperatorPassword = configSettings
                        .getProperty("DEFAULT_PASSWORD");
            }
        }
    }

    public static synchronized String createUniqueKey() {
        counter++;
        return Long.toString(System.currentTimeMillis())
                + Integer.toString(counter);
    }

    /**
     * Grants usage for the marketplace to the specified orgs.
     * 
     * @param mpId
     *            The id of the marketplace.
     * @param orgId
     *            The id of the supplier to grant access to.
     * @param granteeUserKey
     *            The calling user's key. If <code>null</code>, then the
     *            platform operator is used.
     * @param password
     *            The password to be set. If <code>null</code>, then the
     *            platform operator pwd is used.
     * @throws Exception
     */
    public static void grantMarketplaceUsage(String mpId, String orgId,
            String granteeUserKey, String password) throws Exception {
        String userKey = (granteeUserKey == null) ? getPlatformOperatorKey()
                : granteeUserKey;
        String pwd = (password == null) ? getPlatformOperatorPassword()
                : password;
        MarketplaceService mps = ServiceFactory.getDefault()
                .getMarketPlaceService(userKey, pwd);
        mps.addOrganizationsToMarketplace(Collections.singletonList(orgId),
                mpId);
    }

    public static VOService createMarketableService(String technicalServiceId,
            String userKey, String serviceId, VOPriceModel voPriceModel,
            List<VOParameter> parameters) throws Exception {

        VOService voService = factory.createMarketableServiceVO(serviceId);
        voService.setParameters(parameters);
        ServiceProvisioningService sps = ServiceFactory.getDefault()
                .getServiceProvisioningService(userKey, DEFAULT_PASSWORD);
        VOTechnicalService ts = findTechnicalService(userKey,
                technicalServiceId);
        VOServiceDetails voServiceDetails = sps.createService(ts, voService,
                null);
        voServiceDetails = sps.savePriceModel(voServiceDetails, voPriceModel);
        return voServiceDetails;
    }

    public static VOService createAndActivateMarketableServiceFreeOfCharge(
            String serviceId, VOTechnicalService ts, VOMarketplace marketplace,
            String userKey, String password) throws Exception {

        VOService service = factory.createMarketableServiceVO(serviceId);
        ServiceProvisioningService sps = ServiceFactory.getDefault()
                .getServiceProvisioningService(userKey, password);
        VOServiceDetails createdService = sps.createService(ts, service, null);
        VOPriceModel pm = new VOPriceModel();
        pm.setType(PriceModelType.FREE_OF_CHARGE);
        createdService = sps.savePriceModel(createdService, pm);
        MarketplaceService mps = ServiceFactory.getDefault()
                .getMarketPlaceService(userKey, password);
        VOCatalogEntry ce = new VOCatalogEntry();
        ce.setAnonymousVisible(true);
        ce.setVisibleInCatalog(true);
        ce.setMarketplace(marketplace);
        mps.publishService(createdService, Collections.singletonList(ce));
        return sps.activateService(createdService);
    }

    public static VOMarketplace getMarketplace(String mpName, String userKey,
            String password) throws Exception {
        MarketplaceService mps = ServiceFactory.getDefault()
                .getMarketPlaceService(userKey, password);
        return mps.getMarketplaceById(mpName);
    }

    /**
     * Looking for the last mail of mail server and read password and userkey
     * from the mail. At the same time, the user password is changed to common
     * password.
     * @deprecated Use #{@link WebserviceTestBase#readLastMailAndSetCommonPassword(String)} Using this method
     * will cause 401 Unauthorized on WS tests!
     * @return user key written in last mail
     * @throws Exception
     */
    @Deprecated
    public static String readLastMailAndSetCommonPassword() throws Exception {
        String userKey = getMailReader().readUserKeyFromMail();
        String userPwd = getMailReader().readPasswordFromMail();

        IdentityService id = ServiceFactory.getDefault().getIdentityService(
                userKey, userPwd);
        id.changePassword(userPwd, DEFAULT_PASSWORD);

        // sometimes WS tests fail at id.changePassword() with "Unauthorized"
        // probably because the new mail arrives later in inbox, so a previous
        // mail is read - ensure this is not happening
        getMailReader().deleteMails();

        return userKey;
    }

    /**
     * Looking for the last mail of mail server and read password and userkey
     * from the mail. At the same time, the user password is changed to common
     * password.
     *
     * @return user key written in last mail
     * @throws Exception
     */
    public static String readLastMailAndSetCommonPassword(String userName) throws Exception {
        String[] userKeyAndPass = getMailReader().readPassAndKeyFromEmail(userName);
        String userKey = userKeyAndPass[0];
        String userPwd = userKeyAndPass[1];

        IdentityService id = ServiceFactory.getDefault().getIdentityService(
                userKey, userPwd);
        id.changePassword(userPwd, DEFAULT_PASSWORD);
        return userKey;
    }

    public static void savePaymentInfoToSupplier(VOOrganization supplier,
            PaymentInfoType... types) throws Exception {
        Set<String> typesSet = new HashSet<String>();
        for (PaymentInfoType type : types) {
            typesSet.add(type.name());
        }

        getOperator().addAvailablePaymentTypes(
                VOConverter.convertToUp(supplier), typesSet);
    }

    public static Map<String, Object> createMarketplace(String organizationId)
            throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();

        boolean notOpen = false;
        VOMarketplace voMarketplace = factory.createMarketplaceVO(
                organizationId, notOpen, "m_" + createUniqueKey());
        MarketplaceService mps = ServiceFactory.getDefault()
                .getMarketPlaceService(
                        WebserviceTestBase.getPlatformOperatorKey(),
                        WebserviceTestBase.getPlatformOperatorPassword());
        voMarketplace = mps.createMarketplace(voMarketplace);
        result.put("voMarketplace", voMarketplace);
        result.put("marketplaceKey", String.valueOf(voMarketplace.getKey()));
        result.put("marketplaceId", voMarketplace.getMarketplaceId());
        return result;
    }

    /**
     * Setup a test organization with supplier and technology manager role.
     * Creates a marketplace and activates invoice payment type.
     * 
     * @return keys:
     *         userId,userKey,organizationId,serviceManagerUserKey,voOrganization
     */
    public static Map<String, Object> setupSupplier(String prefixUserId)
            throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();

        // create organization
        String supplierUserId = prefixUserId + "_" + createUniqueKey();
        VOOrganization supplier = createOrganization(supplierUserId,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        String supplierUserKey = WebserviceTestBase
                .readLastMailAndSetCommonPassword();
        result.put("userId", supplierUserId);
        result.put("userKey", supplierUserKey);
        result.put("organizationId", supplier.getOrganizationId());

        // activate payment type
        savePaymentInfoToSupplier(supplier, PaymentInfoType.INVOICE);
        AccountService as = ServiceFactory.getDefault().getAccountService(
                supplierUserKey, DEFAULT_PASSWORD);
        setDefaultPaymentType(as, PaymentInfoType.INVOICE);

        // create marketplace
        Map<String, Object> mpResult = createMarketplace(supplier
                .getOrganizationId());
        result.putAll(mpResult);

        // add additional user with service manager role
        List<UserRoleType> userRoles = new ArrayList<UserRoleType>();
        userRoles.add(UserRoleType.SERVICE_MANAGER);
        VOUserDetails user = factory.createUserVO("ServiceManager" + "_"
                + createUniqueKey());
        user.setOrganizationId(supplier.getOrganizationId());
        IdentityService is = ServiceFactory.getDefault().getIdentityService(
                supplierUserKey, DEFAULT_PASSWORD);
        is.createUser(user, userRoles, (String) mpResult.get("marketplaceId"));
        String srvManagerUserKey = readLastMailAndSetCommonPassword();
        result.put("serviceManagerUserKey", srvManagerUserKey);

        result.put("voOrganization", supplier);
        return result;
    }

    public static VOOrganization createOrganization(String administratorId,
            OrganizationRoleType... rolesToGrant) throws Exception {
        return createOrganization(administratorId, null, rolesToGrant);
    }

    public static VOOrganization createOrganization(String administratorId,
            String organizationName, OrganizationRoleType... rolesToGrant)
            throws Exception {

        VOUserDetails adminUser = factory.createUserVO(administratorId);
        VOOrganization organization = factory.createOrganizationVO();
        if (organizationName != null && organizationName.trim().length() > 0) {
            organization.setName(organizationName);
        }

        List<org.oscm.internal.types.enumtypes.OrganizationRoleType> convertedRoles = new ArrayList<>();
        for (OrganizationRoleType r : rolesToGrant) {
            convertedRoles
                    .add(EnumConverter
                            .convert(
                                    r,
                                    org.oscm.internal.types.enumtypes.OrganizationRoleType.class));
        }

        org.oscm.internal.vo.VOOrganization internalVOOrg = VOConverter
                .convertToUp(organization);
        if (Arrays.asList(rolesToGrant).contains(OrganizationRoleType.SUPPLIER)) {
            internalVOOrg.setOperatorRevenueShare(BigDecimal.ZERO);
        }

        organization = VOConverter
                .convertToApi(getOperator()
                        .registerOrganization(
                                internalVOOrg,
                                null,
                                VOConverter.convertToUp(adminUser),
                                null,
                                null,
                                convertedRoles
                                        .toArray(new org.oscm.internal.types.enumtypes.OrganizationRoleType[convertedRoles
                                                .size()])));
        System.out.println("created organization, adminId=" + administratorId);
        return organization;
    }
    
    public static VOOrganization createOrganization(String administratorId,
            String organizationName, long tenantKey, OrganizationRoleType... rolesToGrant)
            throws Exception {

        VOUserDetails adminUser = factory.createUserVO(administratorId);
        VOOrganization organization = factory.createOrganizationVO();
        if (organizationName != null && organizationName.trim().length() > 0) {
            organization.setName(organizationName);
        }

        List<org.oscm.internal.types.enumtypes.OrganizationRoleType> convertedRoles = new ArrayList<>();
        for (OrganizationRoleType r : rolesToGrant) {
            convertedRoles
                    .add(EnumConverter
                            .convert(
                                    r,
                                    org.oscm.internal.types.enumtypes.OrganizationRoleType.class));
        }

        org.oscm.internal.vo.VOOrganization internalVOOrg = VOConverter
                .convertToUp(organization);
        if (Arrays.asList(rolesToGrant).contains(OrganizationRoleType.SUPPLIER)) {
            internalVOOrg.setOperatorRevenueShare(BigDecimal.ZERO);
        }

        internalVOOrg.setTenantKey(tenantKey);
        
        organization = VOConverter
                .convertToApi(getOperator()
                        .registerOrganization(
                                internalVOOrg,
                                null,
                                VOConverter.convertToUp(adminUser),
                                null,
                                null,
                                convertedRoles
                                        .toArray(new org.oscm.internal.types.enumtypes.OrganizationRoleType[convertedRoles
                                                .size()])));
        System.out.println("created organization, adminId=" + administratorId);
        return organization;
    }

    public static Object[] createOrganizationAndReturnUser(String administratorId,
            String organizationName, OrganizationRoleType... rolesToGrant)
            throws Exception {
        Object[] result = new Object[2];
        VOUserDetails adminUser = factory.createUserVO(administratorId);
        VOOrganization organization = factory.createOrganizationVO();
        if (organizationName != null && organizationName.trim().length() > 0) {
            organization.setName(organizationName);
        }

        List<org.oscm.internal.types.enumtypes.OrganizationRoleType> convertedRoles = new ArrayList<>();
        for (OrganizationRoleType r : rolesToGrant) {
            convertedRoles
                    .add(EnumConverter
                            .convert(
                                    r,
                                    org.oscm.internal.types.enumtypes.OrganizationRoleType.class));
        }

        org.oscm.internal.vo.VOOrganization internalVOOrg = VOConverter
                .convertToUp(organization);
        if (Arrays.asList(rolesToGrant).contains(OrganizationRoleType.SUPPLIER)) {
            internalVOOrg.setOperatorRevenueShare(BigDecimal.ZERO);
        }

        organization = VOConverter
                .convertToApi(getOperator()
                        .registerOrganization(
                                internalVOOrg,
                                null,
                                VOConverter.convertToUp(adminUser),
                                null,
                                null,
                                convertedRoles
                                        .toArray(new org.oscm.internal.types.enumtypes.OrganizationRoleType[convertedRoles
                                                .size()])));
        System.out.println("created organization, adminId=" + administratorId);
        result[0] = organization;
        result[1] = adminUser.getUserId();
        return result;
    }

    public static VOTechnicalService createTechnicalService(String tsxml,
            ServiceProvisioningService serviceProvisioningSV) throws Exception {
        serviceProvisioningSV.importTechnicalServices(tsxml.getBytes("UTF-8"));
        return serviceProvisioningSV.getTechnicalServices(
                OrganizationRoleType.TECHNOLOGY_PROVIDER).get(0);
    }

    public static VOTechnicalService createAsynTechnicalService(String tsxml,
            String id, ServiceProvisioningService serviceProvisioningSV)
            throws Exception {
        serviceProvisioningSV.importTechnicalServices(tsxml.getBytes("UTF-8"));
        List<VOTechnicalService> tsList = serviceProvisioningSV
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        for (VOTechnicalService technicalService : tsList) {
            if (technicalService.getTechnicalServiceId().equals(id))
                return technicalService;
        }
        return null;
    }

    public static VOTechnicalService createTechnicalService(String tsxml,
            String id, ServiceProvisioningService serviceProvisioningSV)
            throws Exception {
        serviceProvisioningSV.importTechnicalServices(tsxml.getBytes("UTF-8"));
        List<VOTechnicalService> tsList = serviceProvisioningSV
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        for (VOTechnicalService technicalService : tsList) {
            if (technicalService.getTechnicalServiceId().equals(id))
                return technicalService;
        }
        return null;
    }

    public static VOTechnicalService findTechnicalService(String userKey,
            String technicalServiceId) throws Exception {

        ServiceProvisioningService sps = ServiceFactory.getDefault()
                .getServiceProvisioningService(userKey, DEFAULT_PASSWORD);
        List<VOTechnicalService> tsList = sps
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        for (VOTechnicalService technicalService : tsList) {
            if (technicalService.getTechnicalServiceId().equals(
                    technicalServiceId))
                return technicalService;
        }
        return null;
    }

    public static void importTechnicalService(byte[] bytes, String userkey)
            throws Exception {
        ServiceProvisioningService sps = ServiceFactory.getDefault()
                .getServiceProvisioningService(userkey, DEFAULT_PASSWORD);
        sps.importTechnicalServices(bytes);
    }

    public static VOServiceDetails publishToMarketplace(VOService service,
            boolean isPublic, MarketplaceService mpService,
            VOMarketplace marketPlace) throws Exception {
        VOCatalogEntry voCE = new VOCatalogEntry();
        voCE.setAnonymousVisible(isPublic);
        voCE.setMarketplace(marketPlace);
        return mpService.publishService(service, Arrays.asList(voCE));
    }

    public static VOPaymentType setDefaultPaymentType(AccountService accSrv,
            PaymentInfoType paymentInfoType) throws Exception {

        Set<VOPaymentType> defaultPaymentTypes = accSrv
                .getDefaultPaymentConfiguration();
        VOPaymentType voPaymentType = getPaymentTypeVO(
                accSrv.getAvailablePaymentTypesForOrganization(),
                paymentInfoType);
        defaultPaymentTypes.add(voPaymentType);

        accSrv.savePaymentConfiguration(defaultPaymentTypes, null,
                defaultPaymentTypes, null);
        return voPaymentType;
    }

    public static VOPaymentType getPaymentTypeVO(
            Set<VOPaymentType> defaultPaymentTypes,
            PaymentInfoType paymentInfoType) {
        for (VOPaymentType voPaymentType : defaultPaymentTypes) {
            if (voPaymentType.getPaymentTypeId().equals(paymentInfoType.name())) {
                return voPaymentType;
            }
        }
        fail("Payment type not found for " + paymentInfoType.name());
        return null;
    }

    public static VOPaymentInfo getPaymentInfoVO(AccountService accSrv,
            VOPaymentType voPaymentType) {
        for (VOPaymentInfo voPaymentInfo : accSrv.getPaymentInfos()) {
            if (voPaymentInfo.getPaymentType().getPaymentTypeId()
                    .equals(voPaymentType.getPaymentTypeId())) {
                return voPaymentInfo;
            }
        }
        fail("Payment info not found for ");
        return null;
    }

    public static String convertStacktrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static VOMarketplace registerMarketplace(String organizationId,
            String marketplaceName) throws Exception {
        MarketplaceService srvMarketplace = ServiceFactory.getDefault()
                .getMarketPlaceService(
                        WebserviceTestBase.getPlatformOperatorKey(),
                        WebserviceTestBase.getPlatformOperatorPassword());
        return srvMarketplace.createMarketplace(factory.createMarketplaceVO(
                organizationId, false, marketplaceName));
    }

    public static VOSubscription createSubscription(
            AccountService accountService,
            SubscriptionService subscriptionService, String name,
            VOService service) throws Exception {
        // billing contact
        List<VOPaymentInfo> paymentInfos = accountService.getPaymentInfos();
        VOPaymentInfo voPaymentInfo = paymentInfos.get(0);
        VOBillingContact voBillingContact = accountService
                .saveBillingContact(factory.createBillingContactVO());

        // create subscription
        VOSubscription s = factory.createSubscriptionVO(name);
        List<VOUsageLicense> users = new ArrayList<VOUsageLicense>();
        return subscriptionService.subscribeToService(s, service, users,
                voPaymentInfo, voBillingContact, new ArrayList<VOUda>());
    }

    public static VOTriggerDefinition createTriggerDefinition()
            throws Exception {
        VOTriggerDefinition triggerDef = new VOTriggerDefinition();
        triggerDef.setName("name");
        triggerDef
                .setTarget(WebserviceTestBase
                        .getConfigSetting(WebserviceTestBase.EXAMPLE_BASE_URL)
                        + "/oscm-integrationtests-mockproduct/NotificationService?wsdl");
        triggerDef.setType(TriggerType.ACTIVATE_SERVICE);
        triggerDef.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDef.setSuspendProcess(true);
        return triggerDef;
    }

    public static String getGlobalMarketplaceId() throws Exception {
        return getGlobalMarketplace().getMarketplaceId();
    }

    public static VOMarketplace getGlobalMarketplace() throws Exception {
        synchronized (WebserviceTestBase.class) {
            if (globalMarketplace == null) {
                MarketplaceService ms = ServiceFactory.getDefault()
                        .getMarketPlaceService(getPlatformOperatorKey(),
                                getPlatformOperatorPassword());
                List<VOMarketplace> list = ms.getMarketplacesOwned();
                if (list.isEmpty()) {
                    VOMarketplace mp = new VOMarketplace();
                    mp.setName("Global Operator Marketplace");
                    globalMarketplace = ms.createMarketplace(mp);
                } else {
                    globalMarketplace = list.get(0);
                }
            }
        }
        return globalMarketplace;
    }

    public static void deleteMarketplaces() throws Exception {
        MarketplaceService ms = ServiceFactory.getDefault()
                .getMarketPlaceService(getPlatformOperatorKey(),
                        getPlatformOperatorPassword());
        List<VOMarketplace> mps = ms.getMarketplacesForOperator();
        for (VOMarketplace mp : mps) {
            ms.deleteMarketplace(mp.getMarketplaceId());
        }
        globalMarketplace = null;
    }

    public static void storeGlobalConfigurationSetting(
            ConfigurationKey configurationKey, boolean enabled)
            throws Exception {
        OperatorService os = getOperator();
        List<VOConfigurationSetting> settings = os.getConfigurationSettings();
        VOConfigurationSetting s = null;
        for (VOConfigurationSetting setting : settings) {
            if (setting.getInformationId() == configurationKey) {
                s = setting;
                break;
            }
        }
        if (s == null) {
            s = new VOConfigurationSetting(configurationKey,
                    Configuration.GLOBAL_CONTEXT, String.valueOf(enabled));
        } else {
            s.setValue(String.valueOf(enabled));
        }
        os.saveConfigurationSetting(s);
    }
}
