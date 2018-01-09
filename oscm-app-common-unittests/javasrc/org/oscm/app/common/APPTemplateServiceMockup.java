/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Apr 11, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.common;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.Template;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.intf.APPTemplateService;

/**
 * Mockup for the APP template service
 */
public class APPTemplateServiceMockup implements APPTemplateService {

    @Override
    public void saveTemplate(Template template, String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, APPlatformException {
    }

    @Override
    public void deleteTemplate(String fileName, String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, APPlatformException {
    }

    @Override
    public List<Template> getTemplateList(String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, APPlatformException {
        Template t = new Template();
        t.setFileName("file");
        t.setContent("test".getBytes());
        t.setLastChange(new Date());

        return Arrays.asList(t);
    }

    @Override
    public Template getTemplate(String fileName, String controllerId,
            PasswordAuthentication authentication)
            throws AuthenticationException, APPlatformException {
        Template t = new Template();
        t.setFileName("file");
        t.setContent("test".getBytes());
        t.setLastChange(new Date());

        return t;
    }

}
