/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Mar 31, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.oscm.app.dao.TemplateFileDAO;
import org.oscm.app.domain.TemplateFile;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.Template;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.intf.APPTemplateService;

/**
 * Implementation of the template service.
 * 
 * @author miethaner
 */
@Stateless
@Remote(APPTemplateService.class)
public class APPTemplateServiceBean implements APPTemplateService {

    @EJB
    protected APPAuthenticationServiceBean authService;

    @EJB
    protected TemplateFileDAO templateDAO;

    @Override
    public void saveTemplate(Template template, String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, APPlatformException {

        authService.authenticateTMForController(controllerId, authentication);

        templateDAO.saveTemplateFile(new TemplateFile(0L,
                template.getFileName(), template.getContent(), controllerId));
    }

    @Override
    public void deleteTemplate(String fileName, String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, APPlatformException {

        authService.authenticateTMForController(controllerId, authentication);

        templateDAO.deleteTemplateFile(
                new TemplateFile(0L, fileName, null, controllerId));
    }

    @Override
    public List<Template> getTemplateList(String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, APPlatformException {

        authService.authenticateTMForController(controllerId, authentication);

        List<Template> list = new ArrayList<>();

        for (TemplateFile tf : templateDAO
                .getTemplateFilesByControllerId(controllerId)) {
            list.add(toTemplate(tf));
        }

        return list;
    }

    @Override
    public Template getTemplate(String fileName, String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, APPlatformException {

        authService.authenticateTMForController(controllerId, authentication);

        TemplateFile tf = templateDAO.getTemplateFileByUnique(fileName,
                controllerId);

        if (tf != null) {
            return toTemplate(tf);
        } else {
            throw new APPlatformException("Template not found");
        }
    }

    private Template toTemplate(TemplateFile tf) {
        Template t = new Template();
        t.setFileName(tf.getFileName());
        t.setContent(tf.getContent());
        t.setLastChange(tf.getLastChange());

        return t;
    }

}
