/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 22.09.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.delegates.ServiceLocator;

@FacesValidator("TenantValidator")
public class TenantValidator implements Validator {
    
    ServiceLocator serviceLocator = new ServiceLocator();
    
    @Override
    public void validate(FacesContext context, UIComponent uiComponent,
            Object input) throws ValidatorException {
        
        TenantService tenantService = serviceLocator.findService(TenantService.class);
        
        String tenantKey = input.toString();

        if (StringUtils.isBlank(tenantKey) || "0".equals(tenantKey)) {
            return;
        }

        try {
            tenantService.getTenantByKey(Long.parseLong(tenantKey));
        } catch (ObjectNotFoundException e) {

            String msg = JSFUtils
                    .getText(BaseBean.ERROR_TENANT_NO_LONGER_EXISTS, null);
            FacesMessage facesMessage = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, msg, null);
            throw new ValidatorException(facesMessage);
        }
    }
}
