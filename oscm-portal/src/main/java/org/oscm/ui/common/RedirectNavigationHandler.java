/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 06.08.15 13:25
 *
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.Map;
import java.util.Set;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;

/**
 * Handles redirection to avoid f.e. multiple form resubmission after refreshing
 * the page. Uses Post-Redirect-Get pattern.
 */
public class RedirectNavigationHandler extends ConfigurableNavigationHandler {
    
    /**
     * see {@link NavigationHandler}
     */
    private NavigationHandler parent;
    
    /**
     * Used in JSF 2.0 with Post-Redirect-Get pattern
     */
    private final String FACES_REDIRECT = "?faces-redirect=true";
    
    /**
     * @param parent
     *            - see {@link NavigationHandler}
     */
    public RedirectNavigationHandler(NavigationHandler parent) {
        this.parent = parent;
    }
    
    /**
     * Handles redirection. After POST action is done on the page and redirect
     * to the same page is found, informs navigation handler to do GET in order
     * to avoid f.e. Form Resubmission.
     *
     * @param context
     *            - FacesContext
     * @param from
     *            - navigate from
     * @param outcome
     *            - navigate to
     */
    @Override
    public void handleNavigation(FacesContext context, String from,
            String outcome) {
        NavigationCase navCase = getNavigationCase(context, from, outcome);
        
        if (notNull(context, navCase, from, outcome)
                && isRedirectNeeded(context, outcome)
                && isSamePageRedirect(navCase, context)) {
            outcome = navCase.getToViewId(context) + FACES_REDIRECT;
        }
        
        parent.handleNavigation(context, from, outcome);
    }
    
    /**
     * @param context
     * @param fromAction
     * @param outcome
     * @return
     */
    @Override
    public NavigationCase getNavigationCase(FacesContext context,
            String fromAction, String outcome) {
        if (parent instanceof ConfigurableNavigationHandler) {
            return ((ConfigurableNavigationHandler) parent).getNavigationCase(
                    context, fromAction, outcome);
        } else {
            return null;
        }
    }
    
    /**
     * @return
     */
    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases() {
        if (parent instanceof ConfigurableNavigationHandler) {
            return ((ConfigurableNavigationHandler) parent)
                    .getNavigationCases();
        } else {
            return null;
        }
    }
    
    /**
     * @param context
     *            - FacesContext
     * @param outcome
     *            - navigate to
     * @return - true if redirection is needed, false otherwise
     */
    private boolean isRedirectNeeded(FacesContext context, String outcome) {
        HttpServletRequest request = (HttpServletRequest) context
                .getExternalContext().getRequest();
        
        return notNull(request)
                && notNull(request.getMethod())
                && !outcome.endsWith(FACES_REDIRECT)
                && HttpMethod.POST.equals(request.getMethod());
    }
    
    /**
     * @param navCase
     *            - NavigationCase
     * @param context
     *            - FacesContext
     * @return - true if redirect is done to the same page it is currently on
     *         and page is registered in PRG_REGISTRY, false otherwise
     */
    private boolean isSamePageRedirect(NavigationCase navCase,
            FacesContext context) {
        
        return notNull(navCase.getFromViewId())
                && navCase.getFromViewId().equals(navCase.getToViewId(context));
    }
    
    /**
     * List of objects for null check
     *
     * @param objects
     * @return
     */
    private boolean notNull(Object... objects) {
        for (Object obj : objects) {
            if (obj == null) {
                return false;
            }
        }
        
        return true;
    }
    
}
