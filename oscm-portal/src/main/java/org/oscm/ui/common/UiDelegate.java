/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.MarketplaceBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.TriggerProcessBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.beans.marketplace.CategorySelectionBean;
import org.oscm.ui.beans.marketplace.ServicePagingBean;
import org.oscm.ui.beans.marketplace.TagCloudBean;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOUserDetails;

/**
 * A delegation class that wraps all UI calls to the web-container. The purpose
 * of this class is to enable backing beans to be tested without a container.
 * 
 * @author Enes Sejfi
 */
public class UiDelegate {
    private static final String PROGRESS_DEFAULT = "progress.default";
    private static final String PROGRESS_PANEL = "progressPanel";

    public CategorySelectionBean findCategorySelectionBean() {
        return findBean("categorySelectionBean");
    }

    public TagCloudBean findTagCloudBean() {
        return findBean("tagCloudBean");
    }

    public ServicePagingBean findServicePagingBean() {
        return findBean("servicePagingBean");
    }

    public TriggerProcessBean findTriggerProcessBean() {
        return findBean("triggerProcessBean");
    }

    public MessageHolderBean findMessageHolderBean() {
        return findBean("messageHolderBean");
    }

    public UserBean findUserBean() {
        return findBean("userBean");
    }

    public MarketplaceBean findMarketplaceBean() {
        return findBean("marketplaceBean");
    }

    public SessionBean findSessionBean() {
        return findBean("sessionBean");
    }

    public MenuBean findMenuBean() {
        return findBean("menuBean");
    }

    public <T> T findBean(String beanName) {
        return JSFUtils.findBean(beanName);
    }

    public void handle(Response response, String successMessageKey,
            Object... params) {
        ResponseHandler.handle(response, successMessageKey, params);
    }

    public void handle(String successMessageKey, Object... params) {
        JSFUtils.addMessage(null, FacesMessage.SEVERITY_INFO,
                successMessageKey, params);
    }

    public void handleProgress() {
        JSFUtils.addMessage(PROGRESS_PANEL, FacesMessage.SEVERITY_INFO,
                PROGRESS_DEFAULT, (String[]) null);
    }

    public void handleError(String clientId, String messageKey,
            Object... params) {
        JSFUtils.addMessage(clientId, FacesMessage.SEVERITY_ERROR, messageKey,
                params);
    }

    public void showInfoMessage(String clientId, String messageKey,
            Object... params) {
        JSFUtils.addMessage(clientId, FacesMessage.SEVERITY_INFO, messageKey,
                params);
    }

    public boolean hasErrors() {
        return JSFUtils.hasErrors(FacesContext.getCurrentInstance());
    }

    public boolean hasWarnings() {
        return JSFUtils.hasWarnings(FacesContext.getCurrentInstance());
    }

    public void handleException(SaaSApplicationException ex) {
        ExceptionHandler.execute(ex);
    }

    public void handleException(SaaSApplicationException ex, boolean clean) {
        ExceptionHandler.execute(ex, clean);
    }

    public void resetDirty() {
        ExceptionHandler.resetDirty();
    }

    public String getText(String key, Object... params) {
        return JSFUtils.getText(key, params);
    }

    public String getMarketplaceId() {
        return BaseBean.getMarketplaceIdStatic();
    }

    public void setMarketplaceId(String marketplaceId) {
        BaseBean.setMarketplaceIdStatic(marketplaceId);
    }

    public String getMyUserId() {
        VOUserDetails u = BaseBean
                .getUserFromSessionWithoutException(FacesContext
                        .getCurrentInstance());
        if (u != null) {
            return u.getUserId();
        }
        return null;
    }

    public <T> T findService(Class<T> clazz) {
        return new ServiceLocator().findService(clazz);
    }

    public boolean isNameSequenceReversed() {
        Locale locale = new Locale(getUserLanguage());
        return (locale.equals(Locale.JAPAN) || locale.equals(Locale.JAPANESE));
    }

    protected String getUserLanguage() {
        FacesContext fc = getFacesContext();
        if (fc == null) {
            return "en";
        }
        VOUserDetails voUserDetails = getUserFromSessionWithoutException(fc);
        if (voUserDetails == null) {
            return fc.getViewRoot().getLocale().getLanguage();
        }
        return voUserDetails.getLocale();
    }

    public Locale getViewLocale() {
        return JSFUtils.getViewLocale();
    }

    public void updateAndVerifyViewLocale() {
        JSFUtils.verifyViewLocale();
    }

    public HttpServletRequest getRequest() {
        return JSFUtils.getRequest();
    }

    public HttpSession getSession() {
        return JSFUtils.getSession();
    }

    public HttpSession getSession(boolean create) {
        return getRequest().getSession(create);
    }

    public ExternalContext getExternalContext() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

    public void redirect(String url) throws IOException {
        JSFUtils.redirect(FacesContext.getCurrentInstance()
                .getExternalContext(), url);
    }

    public FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public String getSelectedServiceKeyQueryPart(String selectedServiceKey) {
        String selectedService = "";
        ExternalContext extContext = getFacesContext().getExternalContext();
        String charEncoding = extContext.getRequestCharacterEncoding();

        try {
            String name = URLEncoder.encode("selectedServiceKey", charEncoding);
            String value = URLEncoder.encode(selectedServiceKey, charEncoding);
            selectedService = '?' + name + '=' + value;
        } catch (UnsupportedEncodingException e) {
            extContext.log(getClass().getName()
                    + ".getSelectedServiceQueryPart()", e);

        }
        return selectedService;
    }

    public String getSelectedServiceQueryPart(SessionBean sessionBean) {
        return getSelectedServiceKeyQueryPart(String.valueOf(sessionBean
                .getSelectedServiceKeyForCustomer()));
    }

    public String getMarketplaceIdQueryPart() {
        String marketplaceId = "";
        if (!ADMStringUtils.isBlank(getMarketplaceId())) {
            marketplaceId = "&mId=" + getMarketplaceId();
        }
        return marketplaceId;
    }

    public String getClientId(UIComponent comp) {
        return comp.getClientId(FacesContext.getCurrentInstance());
    }

    /**
     * Get the current user from the session without throwing an exception if
     * the user is not found in the session.
     * 
     * @return the current user from the session, or <code>null</code> if no
     *         user is found in the session.
     */
    public VOUserDetails getUserFromSessionWithoutException() {
        return getUserFromSessionWithoutException(FacesContext
                .getCurrentInstance());
    }

    public static VOUserDetails getUserFromSessionWithoutException(
            FacesContext facesContext) {
        HttpServletRequest request = (HttpServletRequest) facesContext
                .getExternalContext().getRequest();
        VOUserDetails voUserDetails = (VOUserDetails) request.getSession()
                .getAttribute(Constants.SESS_ATTR_USER);
        return voUserDetails;
    }

    public boolean isLoggedIn() {
        return getUserFromSessionWithoutException() != null;
    }

    protected Object getSessionAttribute(String key) {
        HttpSession session = getSession();
        if (session != null) {
            return session.getAttribute(key);
        }
        return null;
    }

    public void resetComponent(String componentId) {
        HashSet<String> set = new HashSet<>(Arrays.asList(componentId));
        JSFUtils.resetUIComponents(FacesContext.getCurrentInstance()
                .getViewRoot(), set);
    }

    public String getSystemProperty(String key) {
        return System.getProperty(key);
    }

}
