/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.oscm.ui.filter.BaseBesFilter.sendRedirectStatic;

import java.io.IOException;

import javax.enterprise.context.NonexistentConversationException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.ui.common.Constants;
import org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants;


/**
 * This Filter is used to handling NonexistentConversationException in subscription process
 * in situation when conversation is lost and user is trying to continue subscription process.
 *
 */
@WebFilter(filterName = "NonexistentConversationFilter", urlPatterns = {"/marketplace/subscriptions/creation/*", "/marketplace/subscriptions/upgrade/*"})
public class NonexistentConversationFilter implements Filter  {

	@Override
	public void doFilter(ServletRequest servletRequest,
            ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception t) {
            Throwable exc = t;
            while(exc != null && !(exc instanceof NonexistentConversationException)) {
                exc = exc.getCause();
            }
            if (exc != null) {
                //Refresh from subscription creation and upgrade
                String requestURI = request.getRequestURI();
                if (requestURI.contains("/marketplace/subscriptions/upgrade/confirmUpgrade.jsf") ||
                        requestURI.contains("/marketplace/subscriptions/creation/confirmAdd.jsf")){
                    sendRedirectStatic(request, response,
                            "/marketplace/account/subscriptionDetails.jsf");
                } else {
                    request.setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                            SubscriptionDetailsCtrlConstants.ERROR_SUBSCRIPTION_REPEATSTEPS);
                    sendRedirectStatic(request, response, "/marketplace/index.jsf");
                }
            } else {
        		throw t;
        	}
        }
    }

    @Override
    public void destroy() {
    }

    @Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}
