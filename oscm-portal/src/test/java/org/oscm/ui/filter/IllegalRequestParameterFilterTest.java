/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Goebel                                                      
 *                                                                              
 *  Creation Date: 2014-05-13                                                      
                                                                                                                        
 *******************************************************************************/
package org.oscm.ui.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class IllegalRequestParameterFilterTest {

    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private FilterChain chainMock;
    private FilterConfig filterConfigMock;
    private IllegalRequestParameterFilter filter;

    @Before
    public void setup() {
        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        chainMock = mock(FilterChain.class);

        filter = new IllegalRequestParameterFilter();

        filterConfigMock = mock(FilterConfig.class);

        doReturn("/marketplace/index.jsp").when(requestMock).getServletPath();
        doReturn("mid=\"abs\"").when(requestMock).getQueryString();
    }

    @Test
    public void init() throws Exception {
        // given
        givenFilterConfig();

        // then
        assertNotNull(filter.ignorePatterns);
        assertEquals(2, filter.ignorePatterns.length);

        assertNotNull(filter.replaceMap);
        assertEquals(4, filter.replaceMap.size());
        for (String value : filter.replaceMap.values()) {
            assertEquals("", value);
        }

    }

    @Test
    public void doFilter_QueryString() throws Exception {
        // given
        givenFilterConfig();

        // when
        filter.doFilter(requestMock, responseMock, chainMock);

        // then
        assertParametersFiltered();
    }

    @Test
    public void doFilter_NoQueryString() throws Exception {
        // given
        givenFilterConfig();
        doReturn("").when(requestMock).getQueryString();

        // when
        filter.doFilter(requestMock, responseMock, chainMock);

        // then
        assertParametersNotFiltered();
    }

    @Test
    public void doFilter_NullQueryString() throws Exception {
        // given
        givenFilterConfig();
        doReturn(null).when(requestMock).getQueryString();

        // when
        filter.doFilter(requestMock, responseMock, chainMock);

        // then
        assertParametersNotFiltered();
    }

    @Test
    public void doFilter_Excluded() throws Exception {
        // given
        givenFilterConfig();
        doReturn("/rich/a4j/ping.js").when(requestMock).getServletPath();

        // when
        filter.doFilter(requestMock, responseMock, chainMock);

        // then
        assertParametersNotFiltered();
    }

    @Test
    public void toPatterns_Null() {
        filter.toPatterns(null);
    }

    @Test
    public void toPatterns_Invalid() {
        assertEquals(0, filter.toPatterns(new String[] { "**" }).length);
    }

    @Test
    public void destroy() throws Exception {
        filter.destroy();
    }

    private void givenFilterConfig() throws ServletException {
        String ignorePatterns = "^SAML,^token";
        String forbiddenPatterns = (char) 0 + "," + (char) 4 + "," + (char) 8
                + "," + (char) 13;
        doReturn(ignorePatterns).when(filterConfigMock).getInitParameter(
                eq("ignore-patterns"));
        doReturn(forbiddenPatterns).when(filterConfigMock).getInitParameter(
                eq("forbidden-patterns"));
        doReturn("(.*/a4j/.*|.*/img/.*|.*/css/.*|.*/fonts/.*|.*/scripts/.*)")
                .when(filterConfigMock).getInitParameter(
                        eq("exclude-url-pattern"));
        filter.init(filterConfigMock);
    }

    private void assertParametersFiltered() throws IOException,
            ServletException {
        assertTrue(areParametersFiltered());
    }

    private void assertParametersNotFiltered() throws IOException,
            ServletException {
        assertFalse(areParametersFiltered());
    }

    private boolean areParametersFiltered() throws IOException,
            ServletException {
        ArgumentCaptor<HttpServletRequest> ac = ArgumentCaptor
                .forClass(HttpServletRequest.class);
        verify(chainMock).doFilter(ac.capture(), eq(responseMock));
        HttpServletRequest src = ac.getValue();
        return (src instanceof RequestWithCleanParameters);
    }
}
