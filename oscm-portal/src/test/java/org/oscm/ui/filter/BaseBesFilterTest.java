/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 14, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.BaseBean;

/**
 * @author tokoda
 * 
 */
public class BaseBesFilterTest {

    AuthorizationRequestData rdoMock;
    HttpServletRequest requestMock;
    BaseBesFilter filter;

    @Before
    public void setup() {
        rdoMock = mock(AuthorizationRequestData.class);

        requestMock = mock(HttpServletRequest.class);

        filter = new BaseBesFilter() {
            @Override
            public void doFilter(ServletRequest arg0, ServletResponse arg1,
                    FilterChain arg2) throws IOException, ServletException {
            }
        };
    }

    @Test
    public void getDefaultUrl_NoUserDetailsAndNotMarketplace() {
        String defaultUrl = filter.getDefaultUrl(null, rdoMock, requestMock);
        assertEquals("/public/error.jsf", defaultUrl);
    }

    @Test
    public void getDefaultUrl_NoUserDetailsForMarketplace() {
        when(Boolean.valueOf(rdoMock.isMarketplace())).thenReturn(
                Boolean.valueOf(true));

        String defaultUrl = filter.getDefaultUrl(null, rdoMock, requestMock);
        assertEquals(BaseBean.MARKETPLACE_ERROR_PAGE, defaultUrl);
    }
}
