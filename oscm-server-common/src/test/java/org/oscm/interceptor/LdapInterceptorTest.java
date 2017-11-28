/*******************************************************************************
 *                                                                      
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
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

import java.util.Properties;

import javax.interceptor.InvocationContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.UnsupportedOperationException;
import org.oscm.internal.vo.LdapProperties;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

public class LdapInterceptorTest {
    private LdapInterceptor ldapInterceptor;

    @Before
    public void setup() throws Exception {
        ldapInterceptor = spy(new LdapInterceptor());
        ldapInterceptor.configService = mock(ConfigurationServiceLocal.class);
    }

    @Test
    public void isServiceProvider_LdapPropertiesNull() throws Exception {
        // given
        InvocationContext context = mock(InvocationContext.class);
        Object[] parameters = { new VOOrganization(), new VOUserDetails(),
                null, "marketplaceId" };
        doReturn(parameters).when(context).getParameters();
        doReturn(Boolean.TRUE).when(ldapInterceptor.configService)
                .isServiceProvider();
        doReturn(mock(OperatorService.class)).when(context).getTarget();

        // when
        ldapInterceptor.ensureLdapDisabledForServiceProvider(context);

        // then
        verify(context, times(1)).proceed();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isServiceProvider_LdapPropertiesNotNull() throws Exception {
        // Given a SAML_SP authentication mode and non-empty LdapProperties for
        // an organization
        InvocationContext context = mock(InvocationContext.class);
        Object[] parameters = { new VOOrganization(), new VOUserDetails(),
                new LdapProperties(), "marketplaceId" };
        doReturn(parameters).when(context).getParameters();

        doReturn(Boolean.TRUE).when(ldapInterceptor.configService)
                .isServiceProvider();
        doReturn(mock(OperatorService.class)).when(context).getTarget();

        // when
        ldapInterceptor.ensureLdapDisabledForServiceProvider(context);

        // then the creation of a customer for an LDAP-managed organization
        // should be unsupported
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isServiceProvider_PropertiesNotNull() throws Exception {
        // Given a SAML_SP authentication mode and non-empty Properties for
        // an organization
        InvocationContext context = mock(InvocationContext.class);
        Object[] parameters = { new Organization(), new ImageResource(),
                new VOUserDetails(), new Properties(), "domicileCountry",
                "marketplaceId", "description",
                OrganizationRoleType.PLATFORM_OPERATOR };
        doReturn(parameters).when(context).getParameters();

        doReturn(Boolean.TRUE).when(ldapInterceptor.configService)
                .isServiceProvider();
        doReturn(mock(OperatorService.class)).when(context).getTarget();

        // when
        ldapInterceptor.ensureLdapDisabledForServiceProvider(context);

        // then the creation of an LDAP-managed organization
        // should be unsupported
    }

    @Test
    public void isNotServiceProvider_PropertiesNotNull() throws Exception {
        // Given an INTERNAL authentication mode and non-empty Properties for an
        // organization
        InvocationContext context = mock(InvocationContext.class);
        Object[] parameters = { new Organization(), new ImageResource(),
                new VOUserDetails(), new Properties(), "domicileCountry",
                "marketplaceId", "description",
                OrganizationRoleType.PLATFORM_OPERATOR };
        doReturn(parameters).when(context).getParameters();
        doReturn(Boolean.FALSE).when(ldapInterceptor.configService)
                .isServiceProvider();

        // when
        ldapInterceptor.ensureLdapDisabledForServiceProvider(context);

        // then
        verify(context, times(1)).proceed();
    }
}
