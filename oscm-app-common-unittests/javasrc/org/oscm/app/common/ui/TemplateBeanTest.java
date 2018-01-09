/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 10.04.2017                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.common.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.OutputStream;
import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.servlet.http.HttpSession;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscm.app.common.APPTemplateServiceMockup;
import org.oscm.app.v2_0.intf.APPTemplateService;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

/**
 * Unit test of template bean
 */
public class TemplateBeanTest extends EJBTestBase {

    // Local mockups
    private APPTemplateServiceMockup templateService;
    private FacesContext facesContext;
    private ExternalContext externalContext;
    private Application application;
    private HttpSession httpSession;
    private UIViewRoot viewRoot;
    private ControllerAccess controllerAccess;

    @Override
    protected void setup(TestContainer container) throws Exception {
    }

    /**
     * Init and return testing bean
     */
    private TemplateBean getTestBean() throws Exception {
        InitialContext context = new InitialContext();
        context.bind(APPTemplateService.JNDI_NAME, templateService);

        templateService = new APPTemplateServiceMockup();
        facesContext = Mockito.mock(FacesContext.class);
        externalContext = Mockito.mock(ExternalContext.class);
        httpSession = Mockito.mock(HttpSession.class);
        application = Mockito.mock(Application.class);
        controllerAccess = Mockito.mock(ControllerAccess.class);
        viewRoot = Mockito.mock(UIViewRoot.class);

        Mockito.when(facesContext.getExternalContext())
                .thenReturn(externalContext);
        Mockito.when(facesContext.getApplication()).thenReturn(application);
        Mockito.when(externalContext.getSession(Matchers.anyBoolean()))
                .thenReturn(httpSession);
        Mockito.when(httpSession.getAttribute(Matchers.anyString()))
                .thenReturn("aValue");
        Mockito.when(facesContext.getViewRoot()).thenReturn(viewRoot);
        Mockito.when(viewRoot.getLocale()).thenReturn(new Locale("en"));
        Mockito.when(controllerAccess.getControllerId())
                .thenReturn("ess.common");

        // Init testing bean
        TemplateBean bean = new TemplateBean() {

            @Override
            protected FacesContext getContext() {
                return facesContext;
            }
        };
        bean.setTemplateService(templateService);
        bean.setControllerAccess(controllerAccess);
        return bean;
    }

    @Test
    public void testLoad() throws Exception {
        TemplateBean bean = getTestBean();

        assertNull(bean.getModel());
        bean.load();
        assertNotNull(bean.getModel());
    }

    @Test
    public void testSave() throws Exception {
        TemplateBean bean = getTestBean();

        UploadedFile uf = Mockito.mock(UploadedFile.class);
        Mockito.when(uf.getName()).thenReturn("test.txt");

        bean.setUploadedFile(uf);

        assertNull(bean.getStatus());
        bean.save();

        assertNotNull(bean.getStatus());
    }

    @Test
    public void testDelete() throws Exception {
        TemplateBean bean = getTestBean();

        assertNull(bean.getModel());
        bean.delete("file");
        assertNotNull(bean.getModel());
    }

    @Test
    public void testExport() throws Exception {
        TemplateBean bean = getTestBean();

        OutputStream os = Mockito.mock(OutputStream.class);
        Mockito.when(externalContext.getResponseOutputStream()).thenReturn(os);

        bean.export("file");
        Mockito.verify(facesContext).responseComplete();

    }

}
