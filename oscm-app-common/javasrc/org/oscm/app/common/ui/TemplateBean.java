/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Apr 3, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.common.ui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.oscm.app.v2_0.APPTemplateServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.Template;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.i18n.Messages;
import org.oscm.app.v2_0.intf.APPTemplateService;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean for template files
 * 
 * @author miethaner
 */
@ManagedBean
@SessionScoped
public class TemplateBean {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TemplateBean.class);

    private static final String[] allowedControllers = { "ess.openstack" };

    private static final String STATUS_CLASS_INFO = "statusInfo";
    private static final String STATUS_CLASS_ERROR = "statusError";

    private APPTemplateService templateService;

    @Inject
    private ControllerAccess controllerAccess;

    private List<Template> model;

    private UploadedFile uploadedFile;

    private String locale;
    private String status;
    private String statusClass;

    public APPTemplateService getTemplateService() {
        if (templateService == null) {
            templateService = APPTemplateServiceFactory.getInstance();
        }
        return templateService;
    }

    public void setTemplateService(APPTemplateService templateService) {
        this.templateService = templateService;
    }

    public ControllerAccess getControllerAccess() {
        return controllerAccess;
    }

    public void setControllerAccess(ControllerAccess controllerAccess) {
        this.controllerAccess = controllerAccess;
    }

    public List<Template> getModel() {
        return model;
    }

    public void setModel(List<Template> model) {
        this.model = model;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("Initialize template bean");

        Locale currentLocale = getContext().getViewRoot().getLocale();
        this.locale = currentLocale.getLanguage();

        load();
    }

    /**
     * Loads the templates from the service into the model.
     */
    public void load() {

        try {
            model = getTemplateService().getTemplateList(
                    controllerAccess.getControllerId(), getAuthentication());

        } catch (APPlatformException e) {
            LOGGER.error(e.getMessage());
            setErrorStatus(e);
        }
    }

    /**
     * Saves the uploaded template into the service.
     */
    public void save() {

        if (uploadedFile == null) {
            return;
        }

        try {
            Template template = new Template();
            template.setFileName(new File(uploadedFile.getName()).getName());
            template.setContent(uploadedFile.getBytes());

            getTemplateService().saveTemplate(template,
                    controllerAccess.getControllerId(), getAuthentication());

            load();

            setInfoStatus(Messages.get(locale, "ui.config.status.imported"));
        } catch (IOException | APPlatformException e) {
            LOGGER.error(e.getMessage());
            setErrorStatus(e);
        }
    }

    /**
     * Delete the template with the given name in the service.
     * 
     * @param fileName
     *            the file name
     */
    public void delete(String fileName) {

        try {
            getTemplateService().deleteTemplate(fileName,
                    controllerAccess.getControllerId(), getAuthentication());

            load();

            setInfoStatus(Messages.get(locale, "ui.config.status.deleted"));
        } catch (APPlatformException e) {
            LOGGER.error(e.getMessage());
            setErrorStatus(e);
        }
    }

    /**
     * Exports the template with the given name from the service.
     * 
     * @param fileName
     *            the file name
     */
    public void export(String fileName) {

        OutputStream out = null;
        try {
            Template t = getTemplateService().getTemplate(fileName,
                    controllerAccess.getControllerId(), getAuthentication());

            FacesContext fc = getContext();
            ExternalContext ec = fc.getExternalContext();

            byte[] content = t.getContent();

            ec.responseReset();
            ec.setResponseContentType(ec.getMimeType(fileName));
            ec.setResponseCharacterEncoding(StandardCharsets.UTF_8.name());
            ec.setResponseHeader("Content-disposition",
                    "attachment; filename=\"" + fileName + "\"");
            ec.setResponseContentLength(content.length);

            out = ec.getResponseOutputStream();
            out.write(content);
            out.flush();

            fc.responseComplete();

        } catch (IOException | APPlatformException e) {
            LOGGER.error(e.getMessage());
            setErrorStatus(e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    protected PasswordAuthentication getAuthentication() {
        FacesContext facesContext = getContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);

        String username = "" + session.getAttribute("loggedInUserId");
        String password = "" + session.getAttribute("loggedInUserPassword");

        return new PasswordAuthentication(username, password);
    }

    // allow stubbing in unit tests
    protected FacesContext getContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Checks if the controller is allowed to use local templates.
     * 
     * @return true if controller is allowed to use local templates
     */
    public boolean isAllowedController() {

        for (String id : allowedControllers) {
            if (id.equals(controllerAccess.getControllerId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the status of the most recent operation.
     * 
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the status of the most recent operation.
     * 
     * @return the status
     */
    public String getStatusClass() {
        return statusClass;
    }

    /**
     * Sets an error status which will be displayed to the user
     */
    private void setErrorStatus(Throwable e) {
        status = "*** " + ((e.getMessage() != null) ? e.getMessage()
                : e.getClass().getName());
        statusClass = STATUS_CLASS_ERROR;
    }

    /**
     * Sets an info status which will be displayed to the user
     */
    private void setInfoStatus(String message) {
        status = message;
        statusClass = STATUS_CLASS_INFO;
    }
}
