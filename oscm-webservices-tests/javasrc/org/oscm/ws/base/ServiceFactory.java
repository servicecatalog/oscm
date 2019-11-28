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
 */
public class ServiceFactory {
	
  private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
  private static final String TRUST_STORE_PWD_PROPERTY = "javax.net.ssl.trustStorePassword";
  private static final String AUTH_MODE = "auth.mode";
  
  private final Properties localProperties;
  private String authMode;
  private static ServiceFactory defaultFactory;

  public ServiceFactory() throws Exception {
	  
    PropertiesReader reader = new PropertiesReader();
    localProperties = reader.load();
    logProperties(localProperties);
    
    this.authMode = localProperties.getProperty(AUTH_MODE);
    
    // Set system properties to pass certificates.
    System.setProperty(TRUST_STORE_PROPERTY, localProperties.getProperty(TRUST_STORE_PROPERTY));
    System.setProperty(TRUST_STORE_PWD_PROPERTY, localProperties.getProperty(TRUST_STORE_PWD_PROPERTY));
  }

  public static synchronized ServiceFactory getDefault() throws Exception {
    
	if (defaultFactory == null) {
      defaultFactory = new ServiceFactory();
      return defaultFactory;
    }

    return defaultFactory;
  }

  private String getWebServiceBaseUrl() {
    return localProperties.getProperty("bes.https.url");
  }

  public String getDefaultUserId() {
    return localProperties.getProperty("user.administrator.id");
  }

  public String getDefaultUserKey() {
    return localProperties.getProperty("user.administrator.key");
  }

  private String getDefaultUserPassword() {
    return localProperties.getProperty("user.administrator.password");
  }

  public String getSupplierUserId() {
    return localProperties.getProperty("user.supplier.id");
  }

  public String getSupplierUserPassword() {
    return localProperties.getProperty("user.supplier.password");
  }

  public IdentityService getIdentityService() throws Exception {
    return getIdentityService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public IdentityService getIdentityService(String userName, String password) throws Exception {
    return connectToWebService(IdentityService.class, userName, password);
  }

  public SearchService getSearchService() throws Exception {
    return getSearchService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public SearchService getSearchService(String userName, String password) throws Exception {
    return connectToWebService(SearchService.class, userName, password);
  }

  public ServiceProvisioningService getServiceProvisioningService() throws Exception {
    return getServiceProvisioningService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public ServiceProvisioningService getServiceProvisioningService(String userName, String password)
      throws Exception {
    return connectToWebService(ServiceProvisioningService.class, userName, password);
  }

  public ReportingService getReportingService() throws Exception {
    return getReportingService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public ReportingService getReportingService(String userName, String password) throws Exception {
    return connectToWebService(ReportingService.class, userName, password);
  }

  public MarketplaceService getMarketPlaceService() throws Exception {
    return connectToWebService(
        MarketplaceService.class, getDefaultUserKey(), getDefaultUserPassword());
  }

  public MarketplaceService getMarketPlaceService(String userName, String password)
      throws Exception {
    return connectToWebService(MarketplaceService.class, userName, password);
  }

  public ReviewService getReviewService() throws Exception {
    return getReviewService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public ReviewService getReviewService(String userName, String password) throws Exception {
    return connectToWebService(ReviewService.class, userName, password);
  }

  public SubscriptionService getSubscriptionService() throws Exception {
    return getSubscriptionService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public SubscriptionService getSubscriptionService(String userName, String password)
      throws Exception {
    return connectToWebService(SubscriptionService.class, userName, password);
  }

  public EventService getEventService() throws Exception {
    return getEventService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public EventService getEventService(String userName, String password) throws Exception {
    return connectToWebService(EventService.class, userName, password);
  }

  public AccountService getAccountService() throws Exception {
    return getAccountService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public AccountService getAccountService(String userName, String password) throws Exception {
    return connectToWebService(AccountService.class, userName, password);
  }

  public SessionService getSessionService() throws Exception {
    return getSessionService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public VatService getVatService() throws Exception {
    return getVatService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public VatService getVatService(String userName, String password) throws Exception {
    return connectToWebService(VatService.class, userName, password);
  }

  public DiscountService getDiscountService() throws Exception {
    return getDiscountService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public DiscountService getDiscountService(String userName, String password) throws Exception {
    return connectToWebService(DiscountService.class, userName, password);
  }

  public SessionService getSessionService(String userName, String password) throws Exception {
    return connectToWebService(SessionService.class, userName, password);
  }

  public TagService getTagService() throws Exception {
    return getTagService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public TagService getTagService(String userName, String password) throws Exception {
    return connectToWebService(TagService.class, userName, password);
  }

  public CategorizationService getCategorizationService() throws Exception {
    return getCategorizationService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public CategorizationService getCategorizationService(String userName, String password)
      throws Exception {
    return connectToWebService(CategorizationService.class, userName, password);
  }

  public <T> T connectToWebService(Class<T> remoteInterface, String userName, String password)
      throws Exception {

    return WebServiceProxy.get(
        getWebServiceBaseUrl(),
        getAuthMode(),
        "http://oscm.org/xsd",
        remoteInterface,
        userName,
        password);
  }

  public OperatorService getOperatorService() throws Exception {
    return getOperatorService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public OperatorService getOperatorService(String userName, String password) throws Exception {
    return connectToEJB(OperatorService.class, userName, password);
  }

  public TriggerDefinitionService getTriggerDefinitionService() throws Exception {
    return getTriggerDefinitionService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public TriggerDefinitionService getTriggerDefinitionService(String userName, String password)
      throws Exception {
    return connectToWebService(TriggerDefinitionService.class, userName, password);
  }

  public TriggerService getTriggerService(String userName, String password) throws Exception {
    return connectToWebService(TriggerService.class, userName, password);
  }

  public OrganizationalUnitService getOrganizationalUnitService(String userName, String password)
      throws Exception {
    return connectToWebService(OrganizationalUnitService.class, userName, password);
  }

  public OrganizationalUnitService getOrganizationalUnitService() throws Exception {
    return getOrganizationalUnitService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public BillingService getBillingService() throws Exception {
    return getBillingService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public BillingService getBillingService(String userName, String password) throws Exception {
    return connectToWebService(BillingService.class, userName, password);
  }

  public TenantService getTenantService() throws Exception {
    return getTenantService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public TenantService getTenantService(String userName, String password) throws Exception {
    return connectToEJB(TenantService.class, userName, password);
  }

  public ConfigurationService getConfigurationService() throws Exception {
    return getConfigurationService(getDefaultUserKey(), getDefaultUserPassword());
  }

  public ConfigurationService getConfigurationService(String userName, String password)
      throws Exception {
    return connectToEJB(ConfigurationService.class, userName, password);
  }

  @SuppressWarnings("unchecked")
  private <T> T connectToEJB(Class<T> remoteInterface, String userName, String password)
      throws SaaSSystemException {
    try {

      if ("OIDC".equals(getAuthMode())) {
        password = "WS" + password;
      }
      
      localProperties.put(Context.SECURITY_PRINCIPAL, userName);
      localProperties.put(Context.SECURITY_CREDENTIALS, password);

      Context context = new InitialContext(localProperties);
      T service = (T) context.lookup(remoteInterface.getName());
      return service;
    } catch (NamingException e) {
      throw new SaaSSystemException("Service lookup failed!", e);
    }
  }

  public static void logProperties(Properties properties) {
    StringBuilder sb =
        new StringBuilder("Starting WebService test with the following " + "properties:\n");
    for (Object key : properties.keySet()) {
      sb.append("\n\t").append(key).append("=").append(properties.getProperty((String) key));
    }

    System.out.println(sb.toString());
  }

  public String getAuthMode() {
    return authMode;
  }

  public void setAuthMode(String authMode) {
    this.authMode = authMode;
  }
  
  public boolean isSSOMode() {
	  return "OIDC".equals(this.authMode);
  }
}
