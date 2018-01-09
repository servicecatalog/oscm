/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.oscm.ui.common.Constants.REQ_PARAM_TENANT_ID;
import static org.oscm.ui.common.Constants.SESSION_PARAM_SAML_LOGOUT_REQUEST;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.MarketplaceRemovedException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.*;

/**
 * Managed bean to store session specific values which are not persisted in the
 * database.
 * 
 */
@SessionScoped
@ManagedBean(name = "sessionBean")
public class SessionBean implements Serializable {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SessionBean.class);
    public static final String USER_AGENT_HEADER = "user-agent";
    private static final long serialVersionUID = -8453011510859899681L;
    private Boolean ie;
    private int navHeight = 580;
    private int navWidth = 240;
    private Long subscribeToServiceKey;
    private transient MarketplaceService marketplaceService = null;
    private Boolean selfRegistrationEnabled = null;
    @EJB
    private MarketplaceCacheService mkpCache;
    @EJB
    private MarketplaceService mkpService;


    /**
     * The key of the last edited user group.
     */
    private String selectedUserGroupId;

    /**
     * The key of the last edited user.
     */
    private String selectedUserIdToEdit;

    /**
     * The key of the last selected technical service - applies to technology
     * provider and supplier (operations on technical and marketable services).
     */
    private long selectedTechnicalServiceKey;

    /**
     * The key of the last selected marketable service - applies to supplier
     * (operations on price model and marketable services).
     */
    private Long selectedServiceKeyForSupplier;

    /**
     * The id of the last selected customer - applies to supplier (operations on
     * customer and price model).
     */
    private String selectedCustomerId;

    /**
     * The id of the last selected subscription id - applies to customer
     * (operations on subscriptions).
     */
    private String selectedSubscriptionId;

    /**
     * TODO add jdoc
     */
    private long selectedSubscriptionKey;

    /**
     * The id of the last selected User id -
     */
    private String selectedUserId;

    /**
     * The key for the selected service
     */
    private Long serviceKeyForPayment;

    /**
     * The key of the last selected marketable service - applies to customer
     * (browsing in marketplace).
     */
    private long selectedServiceKeyForCustomer;

    /**
     * The marketplace brand URL which is currently used as a String.
     */
    private Map<String, String> brandUrlMidMapping = new HashMap<>();

    /**
     * Caching marketplace trackingCodes
     */
    private Map<String, String> trackingCodeMapping = new HashMap<>();

    /**
     * The name of selected user group
     */
    private String selectedGroupId;

    /**
     * The name of selected tab
     */
    private String selectedTab;

    /**
     * The status of check box "Display my operations only"
     */
    private boolean myOperationsOnly = true;

    /**
     * The status of check box "Display my processes only check box"
     */
    private boolean myProcessesOnly = true;

    private PriceModel selectedExternalPriceModel;
    private String samlLogoutRequest;
    private String tenantID;

    public boolean isMyOperationsOnly() {
        return myOperationsOnly;
    }

    public void setMyOperationsOnly(boolean myOperationsOnly) {
        this.myOperationsOnly = myOperationsOnly;
    }

    public boolean isMyProcessesOnly() {
        return myProcessesOnly;
    }

    public void setMyProcessesOnly(boolean myProcessesOnly) {
        this.myProcessesOnly = myProcessesOnly;
    }

    /**
     * Initial state - no service key set
     */
    static int SERIVE_KEY_NOT_SET = 0;

    /**
     * A valid service key was given, but service could not be retrieved
     */
    static int SERIVE_KEY_ERROR = -1;

    /**
     * @return true if the browser of the current user is an internet explorer
     */
    public boolean isIe() {
        if (ie != null) {
            return ie.booleanValue();
        }
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return false;
        }
        Object obj = context.getExternalContext().getRequest();
        if (!(obj instanceof HttpServletRequest)) {
            return false;
        }
        HttpServletRequest request = (HttpServletRequest) obj;

        // The user-agent string contains information about which
        // browser is used to view the pages
        String useragent = request.getHeader(USER_AGENT_HEADER);
        if (useragent == null) {
            ie = Boolean.FALSE;
            return ie.booleanValue();
        }
        if (useragent.toLowerCase().contains("msie")) {
            ie = Boolean.TRUE;
            return ie.booleanValue();
        }
        // Check if browser is IE11
        if (useragent.toLowerCase().contains("trident")
                && useragent.toLowerCase().contains("rv:11")) {
            ie = Boolean.TRUE;
        } else {
            ie = Boolean.FALSE;
        }
        return ie.booleanValue();
    }

    /**
     * @return true if the browser of the current user is an internet explorer
     */
    public boolean isAutoOpenMpLogonDialog() {
        final FacesContext context = getFacesContext();
        if (context == null) {
            return false;
        }
        final Object obj = context.getExternalContext().getRequest();
        return obj instanceof HttpServletRequest && Boolean.TRUE.toString()
                .equals(((ServletRequest) obj).getParameter(
                        Constants.REQ_PARAM_AUTO_OPEN_MP_LOGIN_DIALOG));
    }

    protected HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
    }

    public int getNavWidth() {
        return navWidth;
    }

    public void setNavWidth(int width) {
        this.navWidth = width;
    }

    public int getNavHeight() {
        return navHeight;
    }

    public void setNavHeight(int height) {
        this.navHeight = height;
    }

    public Map<Long, Long> getTableHeightMap() {
        return new TableHeightMap(navHeight, isIe());
    }

    public void setSelectedTechnicalServiceKey(
            long selectedTechnicalServiceKey) {
        this.selectedTechnicalServiceKey = selectedTechnicalServiceKey;
    }

    public long getSelectedTechnicalServiceKey() {
        return selectedTechnicalServiceKey;
    }

    public void setSelectedServiceKeyForSupplier(Long selectedServiceKey) {
        this.selectedServiceKeyForSupplier = selectedServiceKey;
    }

    public Long getSelectedServiceKeyForSupplier() {
        return selectedServiceKeyForSupplier;
    }

    public void setSelectedCustomerId(String selectedCustomerId) {
        this.selectedCustomerId = selectedCustomerId;
    }

    public String getSelectedCustomerId() {
        return selectedCustomerId;
    }

    /**
     * Set the subscription id selected by the customer.
     * 
     * @param selectedSubscriptionId
     *            the subscription id
     */
    public void setSelectedSubscriptionId(String selectedSubscriptionId) {
        this.selectedSubscriptionId = selectedSubscriptionId;
    }

    /**
     * Get the subscription id last selected by the customer.
     * 
     * @return the subscription id
     */
    public String getSelectedSubscriptionId() {
        return selectedSubscriptionId;
    }

    /**
     * @return the selectedUserId
     */
    public String getSelectedUserId() {
        return selectedUserId;
    }

    /**
     * @param selectedUserId
     *            the selectedUserId to set
     */
    public void setSelectedUserId(String selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public void setSubscribeToServiceKey(Long subscribeToServiceKey) {
        this.subscribeToServiceKey = subscribeToServiceKey;
    }

    public Long getSubscribeToServiceKey() {
        return subscribeToServiceKey;
    }

    public long determineSelectedServiceKeyForCustomer() {
        if (selectedServiceKeyForCustomer == SERIVE_KEY_NOT_SET) {
            HttpServletRequest httpRequest = getRequest();
            String key = httpRequest
                    .getParameter(Constants.REQ_PARAM_SELECTED_SERVICE_KEY);
            if (ADMStringUtils.isBlank(key)) {
                // Bug 9466: Read the service key from temporary cookie
                // be able to continue a subscription in case of a possible
                // session timeout
                String serviceKeyVal = JSFUtils.getCookieValue(httpRequest,
                        Constants.REQ_PARAM_SERVICE_KEY);
                if (serviceKeyVal != null && serviceKeyVal.length() > 0)
                    selectedServiceKeyForCustomer = Long
                            .parseLong(serviceKeyVal);
            }

        }
        return selectedServiceKeyForCustomer;
    }

    public void setServiceKeyForPayment(Long serviceKeyForPayment) {
        this.serviceKeyForPayment = serviceKeyForPayment;
    }

    public Long getServiceKeyForPayment() {
        return serviceKeyForPayment;
    }

    public long getSelectedServiceKeyForCustomer() {
        return selectedServiceKeyForCustomer;
    }

    public void setSelectedServiceKeyForCustomer(
            long selectedServiceKeyForCustomer) {
        this.selectedServiceKeyForCustomer = selectedServiceKeyForCustomer;
        if (isValidServiceKey(selectedServiceKeyForCustomer)) {
            try {
                final HttpServletResponse httpResponse = JSFUtils.getResponse();
                if (httpResponse != null) {
                    // store the service key in a temporary cookie in order to
                    // be able to continue a subscription in case of a possible
                    // session timeout
                    JSFUtils.setCookieValue(JSFUtils.getRequest(), httpResponse,
                            Constants.REQ_PARAM_SERVICE_KEY,
                            URLEncoder.encode(
                                    Long.valueOf(selectedServiceKeyForCustomer)
                                            .toString(),
                                    Constants.CHARACTER_ENCODING_UTF8),
                            -1);
                }
            } catch (SaaSSystemException e) {
                // Faces context is not initialized, just return
                logger.logDebug(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_UNSUPPORTED_ENCODING);
            }
        }
    }

    public String getMarketplaceBrandUrl() {
        String marketplaceBrandUrl = brandUrlMidMapping.get(getMarketplaceId());
        if (marketplaceBrandUrl == null) {
            try {
                marketplaceBrandUrl = getMarketplaceService()
                        .getBrandingUrl(getMarketplaceId());
                if (marketplaceBrandUrl == null) {
                    marketplaceBrandUrl = getWhiteLabelBrandingUrl();
                }
            } catch (ObjectNotFoundException e) {
                marketplaceBrandUrl = getWhiteLabelBrandingUrl();
            }
            setMarketplaceBrandUrl(marketplaceBrandUrl);
        }
        return marketplaceBrandUrl;
    }

    public void setMarketplaceBrandUrl(String marketplaceBrandUrl) {
        brandUrlMidMapping.put(getMarketplaceId(), marketplaceBrandUrl);
    }

    public String getMarketplaceTrackingCode() {
        return trackingCodeMapping.get(getMarketplaceId());
    }

    public void setMarketplaceTrackingCode(String marketplaceTrackingCode) {
        trackingCodeMapping.put(getMarketplaceId(), marketplaceTrackingCode);
    }

    public String getMarketplaceId() {
        return BaseBean.getMarketplaceIdStatic();
    }

    FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public String getWhiteLabelBrandingUrl() {
        return getFacesContext().getExternalContext().getRequestContextPath()
                + "/marketplace/css/mp.css";
    }

    /**
     * Checks if the error in the request header was also added to the faces
     * context. This method is used to avoid that the same error is rendered
     * twice.
     * 
     * @return boolean
     */
    public boolean isErrorMessageDuplicate() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String errorKey = (String) getRequest()
                .getAttribute(Constants.REQ_ATTR_ERROR_KEY);
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Object param = getRequest()
                    .getAttribute(Constants.REQ_ATTR_ERROR_PARAM + i);
            if (param != null) {
                params.add(param);
            }
        }
        String errorMessage = JSFUtils.getText(errorKey, params.toArray());
        return JSFUtils.existMessageInList(fc, errorMessage);
    }

    public boolean isHasWarnings() {
        return JSFUtils.hasWarnings(FacesContext.getCurrentInstance());
    }

    protected MarketplaceService getMarketplaceService() {
        if (marketplaceService == null) {
            marketplaceService = ServiceAccess
                    .getServiceAcccessFor(JSFUtils.getRequest().getSession())
                    .getService(MarketplaceService.class);
        }
        return marketplaceService;
    }

    public void redirectToIdpLogout() throws IOException {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        externalContext.redirect(getSamlLogoutRequest());
    }

    public void setSelfRegistrationEnabled(Boolean selfRegistrationEnabled) {
        this.selfRegistrationEnabled = selfRegistrationEnabled;
    }

    public Boolean getSelfRegistrationEnabled() {
        return selfRegistrationEnabled;
    }

    public boolean getNameSequenceReversed() {
        return new UiDelegate().isNameSequenceReversed();
    }

    static boolean isValidServiceKey(long key) {
        return ((key != SERIVE_KEY_ERROR) && (key != SERIVE_KEY_NOT_SET));
    }

    /**
     * @return the selectedGroupId
     */
    public String getSelectedGroupId() {
        return selectedGroupId;
    }

    /**
     * @param selectedGroupId
     *            the selectedGroupId to set
     */
    public void setSelectedGroupId(String selectedGroupId) {
        this.selectedGroupId = selectedGroupId;
    }

    /**
     * @return the selectedTab
     */
    public String getSelectedTab() {
        return selectedTab;
    }

    /**
     * @param selectedTab
     *            the selectedTab to set
     */
    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
    }

    /**
     * @return - selected subscription key
     */
    public long getSelectedSubscriptionKey() {
        return selectedSubscriptionKey;
    }

    /**
     * @param selectedSubscriptionKey
     *            - the selected subscription key
     */
    public void setSelectedSubscriptionKey(long selectedSubscriptionKey) {
        this.selectedSubscriptionKey = selectedSubscriptionKey;
    }

    public String getSelectedUserGroupId() {
        return selectedUserGroupId;
    }

    public void setSelectedUserGroupId(String selectedUserGroupId) {
        this.selectedUserGroupId = selectedUserGroupId;
    }

    public String getSelectedUserIdToEdit() {
        return selectedUserIdToEdit;
    }

    public void setSelectedUserIdToEdit(String selectedUserIdToEdit) {
        this.selectedUserIdToEdit = selectedUserIdToEdit;
    }

    public PriceModel getSelectedExternalPriceModel() {
        return selectedExternalPriceModel;
    }

    public void setSelectedExternalPriceModel(
            PriceModel selectedExternalPriceModel) {
        this.selectedExternalPriceModel = selectedExternalPriceModel;
    }

    public void setSamlLogoutRequest(String samlLogoutRequest) {
        this.samlLogoutRequest = samlLogoutRequest;
    }

    public String getSamlLogoutRequest() {
        if(samlLogoutRequest == null) {
            samlLogoutRequest = (String) new UiDelegate().getSession().getAttribute(SESSION_PARAM_SAML_LOGOUT_REQUEST);
        }
        return samlLogoutRequest;
    }

    public String getTenantID() throws MarketplaceRemovedException {
        if(StringUtils.isBlank(tenantID)) {
            tenantID = getTenantIDFromMarketplace();
        }
        if(StringUtils.isBlank(tenantID)) {
            tenantID = (String) new UiDelegate().getSession().getAttribute(REQ_PARAM_TENANT_ID);
        }
        return tenantID;
    }

    public void setTenantID(String tenantId) {
        this.tenantID = tenantId;
    }

    private String getTenantIDFromMarketplace() throws MarketplaceRemovedException {
        String tenantID = null;
        String marketplaceId = getMarketplaceId();
        if (StringUtils.isNotBlank(marketplaceId)) {
            tenantID = mkpCache
                    .getConfiguration(marketplaceId).getTenantId();
            if (StringUtils.isBlank(tenantID)) {
                try {
                    tenantID = mkpService
                            .getMarketplaceById(marketplaceId).getTenantId();
                } catch (ObjectNotFoundException e) {
                    throw new MarketplaceRemovedException();
                }
            }
        }
        return tenantID;
    }

}
