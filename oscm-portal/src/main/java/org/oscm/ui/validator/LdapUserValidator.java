/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.11.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationRemovedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usermanagement.UserManagementService;
import org.oscm.internal.vo.VOUser;

/**
 * @author kulle
 * 
 */
public class LdapUserValidator implements Validator {

    private UiDelegate ui = new UiDelegate();
    private ServiceLocator srvLocator = new ServiceLocator();

    @Override
    public void validate(FacesContext context, UIComponent component,
            Object valueToValidate) throws ValidatorException {

        if (valueToValidate == null) {
            return;
        }

        UserManagementService service = srvLocator
                .findService(UserManagementService.class);

        String organizationId;
        try {
            organizationId = retrieveOrganizationId(valueToValidate);
            if (service.isOrganizationLDAPManaged(organizationId)) {
                ValidationException e = new ValidationException(
                        ValidationException.ReasonEnum.LDAP_USER_ID, null, null);
                String text = JSFUtils
                        .getText(e.getMessageKey(), null, context);
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, text, null));
            }
        } catch (ObjectNotFoundException | OperationNotPermittedException
                | OrganizationRemovedException e1) {
            return;
        }
    }

    private String retrieveOrganizationId(Object valueToValidate)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OrganizationRemovedException {
        String userId = (String) valueToValidate;
        VOUser reqUser = new VOUser();
        reqUser.setUserId(userId);
        VOUser user = ui.findService(IdentityService.class).getUser(reqUser);
        String organizationId = user.getOrganizationId();
        return organizationId;
    }
}
