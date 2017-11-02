/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.10.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.common.saml2;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.servlet.http.HttpServletRequest;

import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.types.exception.MarketplaceRemovedException;
import org.oscm.internal.types.exception.NotExistentTenantException;
import org.oscm.internal.types.exception.SAML2AuthnRequestException;
import org.oscm.internal.types.exception.WrongTenantConfigurationException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.saml2.api.AuthnRequestGenerator;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;
import org.oscm.ui.filter.AuthenticationSettings;

/**
 * @author roderus
 * 
 */
@ManagedBean
@RequestScoped
public class Saml2Ctrl extends BaseBean {

    protected static final String SAML_SP_REDIRECT_IFRAME = "/saml2/saml2PostInclude.jsf";

    @ManagedProperty(value = "#{saml2Model}")
    private Saml2Model model;

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    @EJB
    private TenantService tenantService;

    @EJB
    private MarketplaceCacheService mkpServiceCache;

    private AuthenticationSettings authenticationSettings;

    public String initModelAndCheckForErrors() {

        AuthnRequestGenerator reqGenerator;

        try {
            reqGenerator = getAuthnRequestGenerator();
            model.setEncodedAuthnRequest(reqGenerator.getEncodedAuthnRequest());
            model.setRelayState(this.getRelayState());
            model.setAcsUrl(this.getAcsUrl().toExternalForm());
            storeRequestIdInSession(reqGenerator.getRequestId());
        } catch (SAML2AuthnRequestException e) {
            getLogger().logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_AUTH_REQUEST_GENERATION_FAILED);
            ui.handleError(null, ERROR_GENERATE_AUTHNREQUEST);
            return getErrorOutcome();
        } catch (MalformedURLException e) {
            getLogger().logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_MISSING_IDP_URL);
            ui.handleError(null, ERROR_INVALID_IDP_URL);
            return getErrorOutcome();
        } catch (NotExistentTenantException | MarketplaceRemovedException | WrongTenantConfigurationException e) {
            getLogger().logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_TENANT_NOT_FOUND);
            ui.handleError(null, ERROR_MISSING_TENANTID);
            return getErrorOutcome();
        }

        return null;
    }

    public void setModel(Saml2Model model) {
        this.model = model;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    String getErrorOutcome() {
        if (isOnMarketplace().booleanValue()) {
            return OUTCOME_MARKETPLACE_LOGOUT;
        } else {
            return OUTCOME_PUBLIC_ERROR_PAGE;
        }
    }

    String getRelayState() {
        return (String) getSessionAttribute(Constants.SESS_ATTR_RELAY_STATE);
    }

    void storeRequestIdInSession(String requestId) {
        setSessionAttribute(Constants.SESS_ATTR_IDP_REQUEST_ID, requestId);
    }
    URL getAcsUrl() throws MalformedURLException, NotExistentTenantException, WrongTenantConfigurationException, MarketplaceRemovedException {
        String acsURL = getAuthenticationSettings().getIdentityProviderURL();
        return new URL(acsURL);
    }

    protected AuthenticationSettings getAuthenticationSettings() throws NotExistentTenantException, WrongTenantConfigurationException, MarketplaceRemovedException {
        if (authenticationSettings == null) {
            authenticationSettings = new AuthenticationSettings(
                    tenantService, getConfigurationService());
            authenticationSettings.init(getTenantID());
        }
        return authenticationSettings;
    }

    private String getTenantID() throws MarketplaceRemovedException {
        return sessionBean.getTenantID();
    }

    AuthnRequestGenerator getAuthnRequestGenerator()
            throws SAML2AuthnRequestException, NotExistentTenantException, WrongTenantConfigurationException, MarketplaceRemovedException {
        Boolean isHttps = Boolean.valueOf(getRequest().isSecure());
        return new AuthnRequestGenerator(getIssuer(), isHttps);
    }

    String getIssuer() throws SAML2AuthnRequestException, NotExistentTenantException, WrongTenantConfigurationException, MarketplaceRemovedException {
        String issuer = getAuthenticationSettings().getIssuer();
        if (ADMStringUtils.isBlank(issuer)) {
            throw new SAML2AuthnRequestException(
                    "No issuer set in the configuration settings",
                    SAML2AuthnRequestException.ReasonEnum.MISSING_ISSUER);
        }
        return issuer;
    }

    Log4jLogger getLogger() {
        return LoggerFactory.getLogger(Saml2Ctrl.class);
    }

    Boolean isOnMarketplace() {
        return new Boolean(super.isMarketplaceSet());
    }

    @Override
    protected HttpServletRequest getRequest() {
        return super.getRequest();
    }

    public String getSaml2PostUrl() {
        String url = this.getRequest().getRequestURL().toString();
        url = url.replaceAll(getRequest().getServletPath(),
                SAML_SP_REDIRECT_IFRAME);
        return url;
    }
}
