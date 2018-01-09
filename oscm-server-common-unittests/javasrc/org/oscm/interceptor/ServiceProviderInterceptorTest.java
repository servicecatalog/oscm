/*******************************************************************************
 *                                                                      
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jul 2, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

/**
 * @author farmaki
 * 
 */

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.interceptor.InvocationContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.operatorservice.bean.OperatorServiceBean;
import org.oscm.internal.types.exception.UnsupportedOperationException;

public class ServiceProviderInterceptorTest {
    private ServiceProviderInterceptor spInterceptor;

    @Before
    public void setup() throws Exception {
        spInterceptor = spy(new ServiceProviderInterceptor());
        spInterceptor.configService = mock(ConfigurationServiceLocal.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isServiceProvider() throws Exception {
        // given
        InvocationContext context = mock(InvocationContext.class);
        doReturn(Boolean.TRUE).when(spInterceptor.configService)
                .isServiceProvider();
        doReturn(new OperatorServiceBean()).when(context).getTarget();

        // when
        spInterceptor.ensureIsNotServiceProvider(context);

        // then
    }

    @Test
    public void isNotServiceProvider() throws Exception {
        // given
        InvocationContext context = mock(InvocationContext.class);
        doReturn(Boolean.FALSE).when(spInterceptor.configService)
                .isServiceProvider();

        // when
        spInterceptor.ensureIsNotServiceProvider(context);

        // then
        verify(context, times(1)).proceed();
    }
}
