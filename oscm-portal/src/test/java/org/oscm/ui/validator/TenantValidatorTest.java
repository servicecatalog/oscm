/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 22.09.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOTenant;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.ui.stubs.FacesContextStub;

public class TenantValidatorTest {

    private FacesContext context;
    private UIComponent component;
    private TenantValidator tenantValidator;
    private ServiceLocator serviceLocator;
    private TenantService tenantService;

    @Before
    public void setup() {

        context = new FacesContextStub(Locale.ENGLISH);
        component = mock(UIComponent.class);

        tenantValidator = spy(new TenantValidator());
        tenantService = mock(TenantService.class);
        serviceLocator = mock(ServiceLocator.class);

        tenantValidator.serviceLocator = serviceLocator;
        when(serviceLocator.findService(TenantService.class))
                .thenReturn(tenantService);
    }

    @Test
    public void testValidateWithValidTenant() throws Exception {

        // given
        long tenantKey = 12111;
        when(tenantService.getTenantByKey(tenantKey))
                .thenReturn(new VOTenant());

        // when
        tenantValidator.validate(context, component, tenantKey);

        // then
        verify(tenantService, times(1)).getTenantByKey(anyLong());
    }

    @Test(expected = ValidatorException.class)
    public void testValidateWithNotExistingTenant() throws Exception {

        // given
        long tenantKey = 12111;
        when(tenantService.getTenantByKey(tenantKey))
                .thenThrow(new ObjectNotFoundException());

        // when
        tenantValidator.validate(context, component, tenantKey);

        // then
        verify(tenantService, times(1)).getTenantByKey(anyLong());
    }

    @Test
    public void testValidateWithEmptyTenant() throws Exception {

        // given
        long tenantKey = 0;

        // when
        tenantValidator.validate(context, component, tenantKey);

        // then
        verify(tenantService, times(0)).getTenantByKey(anyLong());
    }
}
