/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-03-08                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ui;

import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.oscm.app.ui.i18n.Messages;
import org.oscm.app.v2_0.data.ServiceUser;

/**
 * Base class for backing beans implementing UI controllers.
 * 
 * @author goebel
 */
public class BaseCtrl {

    protected static final String OUTCOME_ERROR = "error";
    protected static final String OUTCOME_SUCCESS = "success";
    protected static final String OUTCOME_SAMEPAGE = "refresh";
    protected static final String SUCCESS_SAVED = "ui.config.status.saved";
    protected static final String ERROR_ORGANIZATIONID_MANDATORY = "ui.error.organizationId.mandatory";
    protected static final String ERROR_SERVICE_LOOKUP_FAILED = "ui.error.service.lookup.failed";
    protected static final String ERROR_CONTROLLERID_EXISTS = "ui.error.controllerId.exists";
    protected static final String ERROR_ADD_BOTH = "ui.error.add.both";
    protected static final String ERROR_INVALID_CONTROLLER = "app.message.error.invalid.controllerId";
    protected static final String ERROR_NO_INSTANCE = "app.message.error.noinstance";
    protected static final String ERROR_OPERATION_NOT_ALLOWED = "app.message.error.operation.not.allowed";
    protected static final String OPERATION_SUCCESS = "app.message.info.operation.succeed";
    protected static final String ABORT_PENDING_SUCCESS = "app.message.info.abortPending.succeed";
    protected static final String RESTART_SUCCESS = "app.message.info.restart.succeed";
    protected static final String RESTART_FAILURE = "app.message.info.restart.failed";
    protected static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss SSS z";

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    protected void addError(Exception e) {
        addErrorMessage(e.getMessage());
    }

    protected void addError(String key) {
        addErrorMessage(message(key));
    }

    protected void addMessage(String s) {
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message(s), null));
    }

    protected void addErrorMessage(String s) {
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, s, null));
    }

    protected String message(String key) {
        String locale = readUserFromSession().getLocale();
        return Messages.get(locale, key);
    }

    protected HttpServletRequest getRequest() {
        if (getFacesContext() == null) {
            throw new RuntimeException("FacesContext not initialized!");
        }
        return (HttpServletRequest) getFacesContext().getExternalContext()
                .getRequest();
    }

    protected static boolean notIsEmpty(String param) {
        return param != null && param.trim().length() > 0;
    }

    protected static boolean isEmpty(String param) {
        return param == null || param.trim().isEmpty();
    }

    protected InitialContext getInitialContext() throws NamingException {
        return new InitialContext();
    }

    @SuppressWarnings("unchecked")
    protected <T> T lookup(Class<T> service) {

        Pattern p = Pattern.compile("(.*)Bean$");
        String s = p.matcher(service.getSimpleName()).replaceFirst("$1");

        try {
            return (T) getInitialContext().lookup("java:comp/env/ejb/" + s);

        } catch (NamingException e) {
            addError(ERROR_SERVICE_LOOKUP_FAILED);
        }
        return null;
    }

    protected ServiceUser readUserFromSession() {
        ServiceUser user = null;
        HttpSession httpSession = getRequest().getSession();
        String userId = (String) httpSession
                .getAttribute(SessionConstants.SESSION_USER_ID);
        String locale = (String) httpSession
                .getAttribute(SessionConstants.SESSION_USER_LOCALE);
        if (userId != null && userId.trim().length() > 0) {
            user = new ServiceUser();
            user.setUserId(userId);
            user.setLocale(locale);
        }
        return user;
    }

    protected String readUserLocaleFromSession() {
        String currentUserLocale = ""
                + getRequest().getSession().getAttribute(
                        SessionConstants.SESSION_USER_LOCALE);
        return currentUserLocale;
    }
}
