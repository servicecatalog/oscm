/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 14, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author tokoda
 * 
 */
public class DisableUrlFilterTest {

    DisableUrlFilter filter;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private FilterChain mockFilterChain;

    @Before
    public void setup() {
        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
        filter = spy(new DisableUrlFilter());
    }

    @Test
    public void testdoFilter() throws IOException, ServletException {
        //given
        Mockito.when(requestMock.getServletPath()).thenReturn("path");
        //when
        filter.doFilter(requestMock, responseMock, mockFilterChain);
        //then
        verify(mockFilterChain, times(1)).doFilter(requestMock, responseMock);
    }

    @Test
    public void testdoFilter_notHiddenPage() throws IOException, ServletException {
        //given
        doReturn(false).when(filter).isHiddenPage(requestMock);
        //when
        filter.doFilter(requestMock, responseMock, mockFilterChain);
        //then
        verify(mockFilterChain, times(1)).doFilter(requestMock, responseMock);
    }

    @Test
    public void testdoFilter_hiddenPage() throws IOException, ServletException {
        //given
        doReturn(true).when(filter).isHiddenPage(requestMock);
        final StringBuffer requestURL = new StringBuffer("URL");
        doReturn(requestURL).when(requestMock).getRequestURL();
        //when
        filter.doFilter(requestMock, responseMock, mockFilterChain);
        //then
        verify(responseMock, times(1)).sendError(404);
    }

}
