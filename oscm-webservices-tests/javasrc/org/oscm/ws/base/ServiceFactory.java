/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 30.05.2011
 *
 * <p>*****************************************************************************
 */
package org.oscm.ws.base;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
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
import org.oscm.security.SOAPSecurityHandler;

/**
 * Factory class to retrieve service references in the web service tests.
 *
 * @author Mike J&auml;ger
 */
public class ServiceFactory {

  private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
  private static final String TRUST_STORE_PWD_PROPERTY = "javax.net.ssl.trustStorePassword";
  private static final String CONTEXT_ROOT = "/oscm-webservices/";
  private static final String WS_NAMESPACE = "http://oscm.org/xsd";

  private final WSProperties wsProperties;
  private static ServiceFactory defaultFactory;

  public ServiceFactory() throws Exception {

    wsProperties = WSProperties.load();

    // Set system properties to pass certificates.
    System.setProperty(TRUST_STORE_PROPERTY, wsProperties.getTrustStore());
    System.setProperty(TRUST_STORE_PWD_PROPERTY, wsProperties.getTrustStorePassword());
  }

  public static synchronized ServiceFactory getDefault() throws Exception {
    if (defaultFactory == null) {
      defaultFactory = new ServiceFactory();
      return defaultFactory;
    }

    return defaultFactory;
  }

  public IdentityService getIdentityService() throws Exception {
    return getIdentityService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public IdentityService getIdentityService(String userName, String password) throws Exception {
    return connectToWebService(IdentityService.class, userName, password);
  }

  public SearchService getSearchService() throws Exception {
    return getSearchService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public SearchService getSearchService(String userName, String password) throws Exception {
    return connectToWebService(SearchService.class, userName, password);
  }

  public ServiceProvisioningService getServiceProvisioningService() throws Exception {
    return getServiceProvisioningService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public ServiceProvisioningService getServiceProvisioningService(String userName, String password)
      throws Exception {
    return connectToWebService(ServiceProvisioningService.class, userName, password);
  }

  public ReportingService getReportingService() throws Exception {
    return getReportingService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public ReportingService getReportingService(String userName, String password) throws Exception {
    return connectToWebService(ReportingService.class, userName, password);
  }

  public MarketplaceService getMarketPlaceService() throws Exception {
    return getMarketPlaceService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public MarketplaceService getMarketPlaceService(String userName, String password)
      throws Exception {
    return connectToWebService(MarketplaceService.class, userName, password);
  }

  public ReviewService getReviewService() throws Exception {
    return getReviewService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public ReviewService getReviewService(String userName, String password) throws Exception {
    return connectToWebService(ReviewService.class, userName, password);
  }

  public SubscriptionService getSubscriptionService() throws Exception {
    return getSubscriptionService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public SubscriptionService getSubscriptionService(String userName, String password)
      throws Exception {
    return connectToWebService(SubscriptionService.class, userName, password);
  }

  public EventService getEventService() throws Exception {
    return getEventService(wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public EventService getEventService(String userName, String password) throws Exception {
    return connectToWebService(EventService.class, userName, password);
  }

  public AccountService getAccountService() throws Exception {
    return getAccountService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public AccountService getAccountService(String userName, String password) throws Exception {
    return connectToWebService(AccountService.class, userName, password);
  }

  public SessionService getSessionService() throws Exception {
    return getSessionService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public VatService getVatService() throws Exception {
    return getVatService(wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public VatService getVatService(String userName, String password) throws Exception {
    return connectToWebService(VatService.class, userName, password);
  }

  public DiscountService getDiscountService() throws Exception {
    return getDiscountService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public DiscountService getDiscountService(String userName, String password) throws Exception {
    return connectToWebService(DiscountService.class, userName, password);
  }

  public SessionService getSessionService(String userName, String password) throws Exception {
    return connectToWebService(SessionService.class, userName, password);
  }

  public TagService getTagService() throws Exception {
    return getTagService(wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public TagService getTagService(String userName, String password) throws Exception {
    return connectToWebService(TagService.class, userName, password);
  }

  public CategorizationService getCategorizationService() throws Exception {
    return getCategorizationService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public CategorizationService getCategorizationService(String userName, String password)
      throws Exception {
    return connectToWebService(CategorizationService.class, userName, password);
  }

  public OperatorService getOperatorService() throws Exception {
    return getOperatorService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public OperatorService getOperatorService(String userName, String password) throws Exception {
    return connectToEJB(OperatorService.class, userName, password);
  }

  public TriggerDefinitionService getTriggerDefinitionService() throws Exception {
    return getTriggerDefinitionService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public TriggerDefinitionService getTriggerDefinitionService(String userName, String password)
      throws Exception {
    return connectToWebService(TriggerDefinitionService.class, userName, password);
  }

  public TriggerService getTriggerService(String userName, String password) throws Exception {
    return connectToWebService(TriggerService.class, userName, password);
  }

  public OrganizationalUnitService getOrganizationalUnitService() throws Exception {
    return getOrganizationalUnitService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public OrganizationalUnitService getOrganizationalUnitService(String userName, String password)
      throws Exception {
    return connectToWebService(OrganizationalUnitService.class, userName, password);
  }

  public BillingService getBillingService() throws Exception {
    return getBillingService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public BillingService getBillingService(String userName, String password) throws Exception {
    return connectToWebService(BillingService.class, userName, password);
  }

  public TenantService getTenantService() throws Exception {
    return getTenantService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public TenantService getTenantService(String userName, String password) throws Exception {
    return connectToEJB(TenantService.class, userName, password);
  }

  public ConfigurationService getConfigurationService() throws Exception {
    return getConfigurationService(
        wsProperties.getDefaultUserKey(), wsProperties.getDefaultUserPassword());
  }

  public ConfigurationService getConfigurationService(String userName, String password)
      throws Exception {
    return connectToEJB(ConfigurationService.class, userName, password);
  }

  public <T> T connectToWebService(Class<T> remoteInterface, String userName, String password)
      throws Exception {

    String wsdlUrl =
        wsProperties.getBaseUrl() + CONTEXT_ROOT + remoteInterface.getSimpleName() + "/BASIC?wsdl";

    URL url = new URL(wsdlUrl);
    QName qName = new QName(WS_NAMESPACE, remoteInterface.getSimpleName());
    Service service = Service.create(url, qName);

    T port = service.getPort(remoteInterface);
    BindingProvider bindingProvider = (BindingProvider) port;

    if (isSSOMode()) {
      password = "WS" + password;
    }

    Binding binding = bindingProvider.getBinding();
    List<Handler> handlerChain = binding.getHandlerChain();
    if (handlerChain == null) {
      handlerChain = new ArrayList<>();
    }

    handlerChain.add(new SOAPSecurityHandler(userName, password));
    binding.setHandlerChain(handlerChain);

    return port;
  }

  @SuppressWarnings("unchecked")
  private <T> T connectToEJB(Class<T> remoteInterface, String userName, String password)
      throws SaaSSystemException {
    try {
      if (isSSOMode()) {
        password = "WS" + password;
      }

      Properties localProperties = new Properties();
      localProperties.put(Context.SECURITY_PRINCIPAL, userName);
      localProperties.put(Context.SECURITY_CREDENTIALS, password);
      localProperties.put(Context.INITIAL_CONTEXT_FACTORY, wsProperties.getNamingFactory());
      localProperties.put(Context.PROVIDER_URL, wsProperties.getNamingProvider());
      localProperties.put(wsProperties.getRealmNameProperty(), wsProperties.getRealmName());

      Context context = new InitialContext(localProperties);
      T service = (T) context.lookup(remoteInterface.getName());
      return service;
    } catch (NamingException e) {
      throw new SaaSSystemException("Service lookup failed!", e);
    }
  }

  public boolean isSSOMode() {
    return "OIDC".equals(wsProperties.getAuthMode());
  }
}
