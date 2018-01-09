/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.ui.filter;

import java.io.IOException;
import java.security.AccessController;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.Subject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;

import com.google.common.collect.Sets;

/**
 * Created by Marcin Maciaszczyk on 2015-09-11.
 */
public class RoleBasedFilter extends BaseBesFilter {

    private static final String CONFIG_FILE_LOCATION = "/WEB-INF/role-based-filter-config.xml";
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(RoleBasedFilter.class);
    private RoleBasedFilterConfig config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        loadConfig(filterConfig.getServletContext());
    }

    private void loadConfig(ServletContext servletContext) {
        try {
            final JAXBContext context = JAXBContext
                    .newInstance(RoleBasedFilterConfig.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StreamSource source = new StreamSource(
                    servletContext.getResourceAsStream(CONFIG_FILE_LOCATION));
            config = unmarshaller.unmarshal(source, RoleBasedFilterConfig.class)
                    .getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
            ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        for (RoleBasedFilterConfigEntry entry : config.getEntries()) {
            if (httpRequest.getRequestURI().endsWith(entry.getPage())
                    && !isPrincipalRoleAllowed(entry.getRolesAllowed(),
                            httpRequest)) {
                logger.logInfo(Log4jLogger.ACCESS_LOG,
                        LogMessageIdentifier.INFO_INSUFFICIENT_ROLE,
                        entry.getRolesAllowed().toString(), entry.getPage(),
                        entry.getRolesAllowed().toString());
                JSFUtils.sendRedirect((HttpServletResponse) servletResponse,
                        httpRequest.getContextPath()
                                + Marketplace.MARKETPLACE_ROOT
                                + Constants.INSUFFICIENT_AUTHORITIES_URI);
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isPrincipalRoleAllowed(Set<String> rolesAllowed, HttpServletRequest httpRequest) {
        for (String role : rolesAllowed) {
            if (httpRequest.isUserInRole(role)) {
                return true;
            }
        }
        return false;
    }

}
