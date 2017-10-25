/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.11.2013       
 *  
 *  author cmin
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.operatorservice;

import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.SupportedLanguage;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.operatorservice.bean.OperatorServiceLocalBean;
import org.oscm.internal.assembler.SupportedLanguageAssembler;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Manage Language Service
 * 
 * @author cmin
 * 
 */
@Stateless
@Remote(ManageLanguageService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ManageLanguageServiceBean implements ManageLanguageService {

    @Inject
    OperatorServiceLocalBean operatorService;

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @Override
    public List<POSupportedLanguage> getLanguages(boolean isOnlyActive) {
        List<SupportedLanguage> list = operatorService
                .getLanguages(isOnlyActive);
        return SupportedLanguageAssembler.toPOLanguages(list, new Locale(ds
                .getCurrentUser().getLocale()));
    }

    @Override
    public String getDefaultLanguage() throws ObjectNotFoundException {
        return operatorService.getDefaultLanguage();
    }

    @Override
    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public void saveLanguages(List<POSupportedLanguage> poLanguages)
            throws IllegalArgumentException, ValidationException {
        operatorService.saveLanguages(SupportedLanguageAssembler
                .toLanguages(poLanguages));
    }
}
