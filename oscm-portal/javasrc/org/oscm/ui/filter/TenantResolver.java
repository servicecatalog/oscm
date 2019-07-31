/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 30.07.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.oscm.internal.types.enumtypes.ConfigurationKey.SSO_DEFAULT_TENANT_ID;
import static org.oscm.types.constants.Configuration.GLOBAL_CONTEXT;
import static org.oscm.ui.common.Constants.REQ_PARAM_TENANT_ID;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.MarketplaceRemovedException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.ui.common.ServiceAccess;

/**
 * @author goebel
 *
 */
@LocalBean
public class TenantResolver {

  private static final Log4jLogger logger = LoggerFactory.getLogger(TenantResolver.class);

  @EJB
  private MarketplaceService marketplaceService;

  public MarketplaceConfiguration getConfig(String marketplaceId) {
    return marketplaceService.getCachedMarketplaceConfiguration(marketplaceId);
  }

  /**
   * Get Tenant ID from context. TODO LG - Replace extracted logic in
   * AuthorizationFilter
   */
  public String getTenantID(AuthorizationRequestData ard, HttpServletRequest httpRequest)
      throws MarketplaceRemovedException {
    String tenantID;
    if (ard.isMarketplace()) {
      tenantID = getTenantIDFromMarketplace(httpRequest, ard);
    } else {
      tenantID = getTenantIDFromRequest(httpRequest);
    }
    if (StringUtils.isNotBlank(tenantID)) {
      httpRequest.getSession().setAttribute(REQ_PARAM_TENANT_ID, tenantID);
    } else {
      tenantID = (String) httpRequest.getSession().getAttribute(REQ_PARAM_TENANT_ID);
    }
    if (StringUtils.isBlank(tenantID)) {
      logger.logDebug("TenantID is missing. Using default.");
      tenantID = getConfigurationService(httpRequest)
          .getVOConfigurationSetting(SSO_DEFAULT_TENANT_ID, GLOBAL_CONTEXT).getValue();

      httpRequest.getSession().setAttribute(REQ_PARAM_TENANT_ID, tenantID);
    }
    return tenantID;
  }

  private String getTenantIDFromMarketplace(HttpServletRequest httpRequest,
      AuthorizationRequestData ard) throws MarketplaceRemovedException {
    String marketplaceId = ard.getMarketplaceId();
    String tenantID = null;
    if (StringUtils.isNotBlank(marketplaceId)) {
      tenantID = getConfig(marketplaceId).getTenantId();
    }
    return tenantID;
  }

  private String getTenantIDFromRequest(HttpServletRequest request) {
    return request.getParameter(REQ_PARAM_TENANT_ID);
  }

  ConfigurationService getConfigurationService(HttpServletRequest request) {
    ServiceAccess serviceAccess = ServiceAccess.getServiceAcccessFor(request.getSession());
    ConfigurationService cfgService = serviceAccess.getService(ConfigurationService.class);
    return cfgService;
  }
}
