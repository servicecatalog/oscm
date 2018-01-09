/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Sep 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.dao.TemplateFileDAO;
import org.oscm.app.domain.TemplateFile;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.Template;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPTemplateService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

public class APPTemplateServiceBeanIT extends EJBTestBase {

    private APPTemplateService templateSvc;
    private TemplateFileDAO tfDAO;
    private PasswordAuthentication defaultAuth;

    @Override
    protected void setup(TestContainer container) throws Exception {
        EntityManager em = container.getPersistenceUnit("oscm-app");
        container.enableInterfaceMocking(true);
        container.addBean(new TemplateFileDAO());
        container.addBean(new APPTemplateServiceBean());
        container.addBean(Mockito.mock(APPAuthenticationServiceBean.class));
        tfDAO = container.get(TemplateFileDAO.class);
        templateSvc = container.get(APPTemplateService.class);
        tfDAO.em = em;
        defaultAuth = new PasswordAuthentication("user", "password");
    }

    @Test
    public void testGetTemplate() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                TemplateFile tf = new TemplateFile();
                tf.setFileName("file");
                tf.setContent("test".getBytes());
                tf.setControllerId("controller");

                tfDAO.saveTemplateFile(tf);
                return null;
            }
        });

        Template t = templateSvc.getTemplate("file", "controller", defaultAuth);

        assertEquals("file", t.getFileName());
        assertEquals("test", new String(t.getContent()));
    }

    @Test(expected = APPlatformException.class)
    public void testGetTemplate_nonexisting() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                TemplateFile tf = new TemplateFile();
                tf.setFileName("file");
                tf.setContent("test".getBytes());
                tf.setControllerId("controller");

                tfDAO.saveTemplateFile(tf);
                return null;
            }
        });

        templateSvc.getTemplate("file2", "controller", defaultAuth);
    }

    @Test
    public void testGetTemplateList() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                TemplateFile tf = new TemplateFile();
                tf.setFileName("file");
                tf.setContent("test".getBytes());
                tf.setControllerId("controller");

                tfDAO.saveTemplateFile(tf);
                return null;
            }
        });

        List<Template> list = templateSvc.getTemplateList("controller",
                defaultAuth);

        assertEquals("file", list.get(0).getFileName());
        assertEquals("test", new String(list.get(0).getContent()));
    }

    @Test
    public void testSaveTemplate() throws Exception {

        Template t = new Template();
        t.setFileName("file");
        t.setContent("test".getBytes());

        templateSvc.saveTemplate(t, "controller", defaultAuth);

        List<Template> list = templateSvc.getTemplateList("controller",
                defaultAuth);

        assertEquals(1, list.size());
    }

    @Test
    public void testDeleteTemplate() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                TemplateFile tf = new TemplateFile();
                tf.setFileName("file");
                tf.setContent("test".getBytes());
                tf.setControllerId("controller");

                tfDAO.saveTemplateFile(tf);
                return null;
            }
        });

        templateSvc.deleteTemplate("file", "controller", defaultAuth);

        List<Template> list = templateSvc.getTemplateList("controller",
                defaultAuth);

        assertEquals(0, list.size());
    }
}
