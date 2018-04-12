/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 30.05.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws.base;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.intf.AccountService;
import org.oscm.intf.BillingService;
import org.oscm.intf.CategorizationService;
import org.oscm.intf.DiscountService;
import org.oscm.intf.EventService;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.OrganizationalUnitService;
import org.oscm.intf.ReportingService;
import org.oscm.intf.ReviewService;
import org.oscm.intf.SearchService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.intf.SessionService;
import org.oscm.intf.SubscriptionService;
import org.oscm.intf.TagService;
import org.oscm.intf.TriggerDefinitionService;
import org.oscm.intf.TriggerService;
import org.oscm.intf.VatService;
import org.oscm.test.setup.PropertiesReader;
import org.oscm.test.ws.WebServiceProxy;

/**
 * Factory class to retrieve service references in the web service tests.
 *
 * @author Mike J&auml;ger
 *
 */
public class ServiceFactory {
    private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
    private static final String TRUST_STORE_PWD_PROPERTY = "javax.net.ssl.trustStorePassword";
    private static final String DEFAULT_USER_PASSWORD = "secret";
    private static final String BASIC_AUTH = "BASIC";
    private static final String STS_AUTH = "STS";
    private static final String CLIENT_CERT = "CLIENTCERT";
    private boolean useCertAuth = false;
    private boolean useSTSAuth = false;
    private final Properties props;
    private String tenantId;
    private String orgId;

    private static ServiceFactory defaultFactory;

    public ServiceFactory() throws Exception {
        PropertiesReader reader = new PropertiesReader();
        props = reader.load();
        logProperties(props);

        // Set system properties to pass certificates.
        System.setProperty(TRUST_STORE_PROPERTY, props.getProperty(TRUST_STORE_PROPERTY));
        System.setProperty(TRUST_STORE_PWD_PROPERTY, props.getProperty(TRUST_STORE_PWD_PROPERTY));
    }

    public static synchronized ServiceFactory getDefault() throws Exception {
        if (defaultFactory == null) {
            defaultFactory = new ServiceFactory();
            return defaultFactory;
        }
        defaultFactory.setUseCertAuth(false);
        defaultFactory.setTenantId(null);
        defaultFactory.setOrgId(null);
        return defaultFactory;
    }

    public static synchronized ServiceFactory getSTSServiceFactory()
            throws Exception {
        if (defaultFactory == null) {
            defaultFactory = new ServiceFactory();
            defaultFactory.setUseSTSAuth(true);
            return defaultFactory;
        }
        defaultFactory.setUseCertAuth(false);
        defaultFactory.setUseSTSAuth(true);
        defaultFactory.setTenantId(null);
        defaultFactory.setOrgId(null);
        return defaultFactory;
    }

    public static synchronized ServiceFactory getSTSServiceFactory(
            String tenantId, String orgId) throws Exception {

        defaultFactory = getSTSServiceFactory();
        defaultFactory.setOrgId(orgId);
        defaultFactory.setTenantId(tenantId);
        return defaultFactory;
    }

    private void setUseSTSAuth(boolean useSTSAuth) {
        this.useSTSAuth = useSTSAuth;
    }

    private String getWebServiceBaseUrl() {
        return props.getProperty("bes.https.url");
    }

    private String getDefaultUserName() {
        return props.getProperty("DEFAULT_USER");
    }

    private String getDefaultPassword() {
        return props.getProperty("DEFAULT_PASSWORD");
    }

    public IdentityService getIdentityService() throws Exception {
        return getIdentityService(getDefaultUserName(), getDefaultPassword());
    }

    public IdentityService getIdentityService(String userName, String password)
            throws Exception {
        return connectToWebService(IdentityService.class, userName, password);
    }

    public SearchService getSearchService() throws Exception {
        return getSearchService(getDefaultUserName(), getDefaultPassword());
    }

    public SearchService getSearchService(String userName, String password)
            throws Exception {
        return connectToWebService(SearchService.class, userName, password);
    }

    public ServiceProvisioningService getServiceProvisioningService()
            throws Exception {
        return getServiceProvisioningService(getDefaultUserName(),
                getDefaultPassword());
    }

    public ServiceProvisioningService getServiceProvisioningService(
            String userName, String password) throws Exception {
        return connectToWebService(ServiceProvisioningService.class, userName,
                password);
    }

    public ReportingService getReportingService() throws Exception {
        return getReportingService(getDefaultUserName(), getDefaultPassword());
    }

    public ReportingService getReportingService(String userName, String password)
            throws Exception {
        return connectToWebService(ReportingService.class, userName, password);
    }

    public MarketplaceService getMarketPlaceService(String userName,
            String password) throws Exception {
        return connectToWebService(MarketplaceService.class, userName, password);
    }

    public ReviewService getReviewService() throws Exception {
        return getReviewService(getDefaultUserName(), getDefaultPassword());
    }

    public ReviewService getReviewService(String userName, String password)
            throws Exception {
        return connectToWebService(ReviewService.class, userName, password);
    }

    public SubscriptionService getSubscriptionService() throws Exception {
        return getSubscriptionService(getDefaultUserName(),
                getDefaultPassword());
    }

    public SubscriptionService getSubscriptionService(String userName)
            throws Exception {
        return getSubscriptionService(userName, DEFAULT_USER_PASSWORD);
    }

    public SubscriptionService getSubscriptionService(String userName,
            String password) throws Exception {
        return connectToWebService(SubscriptionService.class, userName,
                password);
    }

    public EventService getEventService() throws Exception {
        return getEventService(getDefaultUserName(), getDefaultPassword());
    }

    public EventService getEventService(String userName, String password)
            throws Exception {
        return connectToWebService(EventService.class, userName, password);
    }

    public AccountService getAccountService() throws Exception {
        return getAccountService(getDefaultUserName(), getDefaultPassword());
    }

    public AccountService getAccountService(String userName, String password)
            throws Exception {
        return connectToWebService(AccountService.class, userName, password);
    }

    public SessionService getSessionService() throws Exception {
        return getSessionService(getDefaultUserName(), getDefaultPassword());
    }

    public VatService getVatService() throws Exception {
        return getVatService(getDefaultUserName(), getDefaultPassword());
    }

    public VatService getVatService(String userName, String password)
            throws Exception {
        return connectToWebService(VatService.class, userName, password);
    }

    public DiscountService getDiscountService() throws Exception {
        return getDiscountService(getDefaultUserName(), getDefaultPassword());
    }

    public DiscountService getDiscountService(String userName, String password)
            throws Exception {
        return connectToWebService(DiscountService.class, userName, password);
    }

    public SessionService getSessionService(String userName, String password)
            throws Exception {
        return connectToWebService(SessionService.class, userName, password);
    }

    public TagService getTagService() throws Exception {
        return getTagService(getDefaultUserName(), getDefaultPassword());
    }

    public TagService getTagService(String userName, String password)
            throws Exception {
        return connectToWebService(TagService.class, userName, password);
    }

    public CategorizationService getCategorizationService() throws Exception {
        return getCategorizationService(getDefaultUserName(),
                getDefaultPassword());
    }

    public CategorizationService getCategorizationService(String userName,
            String password) throws Exception {
        return connectToWebService(CategorizationService.class, userName,
                password);
    }

    private <T> T connectToWebService(Class<T> remoteInterface,
            String userName, String password) throws Exception {
        //version not really used for now
        return WebServiceProxy.get(getWebServiceBaseUrl(), "v1.9", getAuth(),
                "http://oscm.org/xsd", remoteInterface, userName, password,
                getTenantId(), getOrgId());
    }

    public <T> T connectToWebService(Class<T> remoteInterface, String userName,
            String password, String versionInHeader) throws Exception {
        return WebServiceProxy.get(getWebServiceBaseUrl(), "v1.9",
                versionInHeader, getAuth(), "http://oscm.org/xsd",
                remoteInterface, userName, password, getTenantId(), getOrgId());
    }

    public OperatorService getOperatorService() throws Exception {
        return getOperatorService(getDefaultUserName(), getDefaultPassword());
    }

    public OperatorService getOperatorService(String userName, String password)
            throws Exception {
        return connectToEJB(OperatorService.class, userName, password);
    }

    public TriggerDefinitionService getTriggerDefinitionService()
            throws Exception {
        return getTriggerDefinitionService(getDefaultUserName(),
                getDefaultPassword());
    }

    public TriggerDefinitionService getTriggerDefinitionService(
            String userName, String password) throws Exception {
        return connectToWebService(TriggerDefinitionService.class, userName,
                password);
    }

    public TriggerService getTriggerService(String userName, String password)
            throws Exception {
        return connectToWebService(TriggerService.class, userName, password);
    }

    public OrganizationalUnitService getOrganizationalUnitService(
            String userName, String password) throws Exception {
        return connectToWebService(OrganizationalUnitService.class, userName,
                password);
    }

    public OrganizationalUnitService getOrganizationalUnitService()
            throws Exception {
        return getOrganizationalUnitService(getDefaultUserName(),
                getDefaultPassword());
    }

    public BillingService getBillingService() throws Exception {
        return getBillingService(getDefaultUserName(), getDefaultPassword());
    }

    public BillingService getBillingService(String userName, String password)
            throws Exception {
        return connectToWebService(BillingService.class, userName, password);
    }

    public TenantService getTenantService() throws Exception {
        return getTenantService(getDefaultUserName(), getDefaultPassword());
    }

    public TenantService getTenantService(String userName, String password)
            throws Exception {
        return connectToEJB(TenantService.class, userName, password);
    }

    public ConfigurationService getConfigurationService() throws Exception {
        return getConfigurationService(getDefaultUserName(),
                getDefaultPassword());
    }

    public ConfigurationService getConfigurationService(String userName,
            String password) throws Exception {
        return connectToEJB(ConfigurationService.class, userName, password);
    }

    @SuppressWarnings("unchecked")
    private <T> T connectToEJB(Class<T> remoteInterface, String userName,
            String password) throws SaaSSystemException {
        try {
            props.put(Context.SECURITY_PRINCIPAL, userName);
            props.put(Context.SECURITY_CREDENTIALS, password);

            Context context = new InitialContext(props);
            T service = (T) context.lookup(remoteInterface.getName());
            return service;
        } catch (NamingException e) {
            throw new SaaSSystemException("Service lookup failed!", e);
        }
    }

    private String getAuth() {
        if (useCertAuth) {
            return CLIENT_CERT;
        }
        if (useSTSAuth) {
            return STS_AUTH;
        }
        return BASIC_AUTH;
    }

    public boolean isUseCertAuth() {
        return useCertAuth;
    }

    public void setUseCertAuth(boolean useCertAuth) {
        this.useCertAuth = useCertAuth;
    }

    private String getDomainHome() {
        return props.getProperty("glassfish.bes.domain");
    }

    public static void logProperties(Properties properties) {
        StringBuilder sb = new StringBuilder("Starting WebService test with the following " +
                "properties:\n");
        for (Object key : properties.keySet()) {
            sb.append("\n\t").append(key).append("=").append(properties.getProperty((String) key));
        }
        sb.append("\n\nIf the environment specific properties are wrong, especially bes" +
                ".https.url and glassfish.bes.domain\n\tplease override them in " +
                "oscm-devruntime/javares/local/<hostname>/test.properties!\n");
        System.out.println(sb.toString());
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
