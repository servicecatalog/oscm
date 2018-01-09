/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.ui.dialog.common.ldapsettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.Part;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.PropertiesLoader;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.usermanagement.POLdapOrganization;
import org.oscm.internal.usermanagement.POLdapSetting;
import org.oscm.internal.usermanagement.UserManagementService;

/**
 * Controller to handle the display of LDAP configuration settings.
 * 
 * @author jaeger
 * 
 */
@ViewScoped
@ManagedBean(name="ldapConfigurationCtrl")
public class LdapConfigurationCtrl extends BaseBean {

    static final String ERROR_NO_IMPORT_FILE = "error.organization.ldapsettings.nofile";

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(LdapConfigurationCtrl.class);
    
    @ManagedProperty(value="#{ldapConfigurationModel}")
    private LdapConfigurationModel model;
    private transient UserManagementService userMgmtSvc;

    public String getInitialize() throws SaaSApplicationException {
        if (model == null) {
            model = ui.findBean("ldapConfigurationModel");
        }
        initModelData();
        return "";
    }

    void initModelData() throws SaaSApplicationException {
        boolean platformOperator = getUserManagementService()
                .isPlatformOperator();
        model.setOrganizationSelectionSupported(platformOperator);
        if (platformOperator) {
            initOrgData();
        }
        initSettingData();
    }

    /**
     * Initializes the list of LDAP managed organizations.
     */
    void initOrgData() {
        Set<POLdapOrganization> ldapManagedOrganizations = getUserManagementService()
                .getLdapManagedOrganizations();
        model.getOrganizations().clear();
        for (POLdapOrganization poLdapOrganization : ldapManagedOrganizations) {
            String nameToDisplay = poLdapOrganization.getName() == null ? poLdapOrganization
                    .getIdentifier() : String.format("%s (%s)",
                    poLdapOrganization.getName(),
                    poLdapOrganization.getIdentifier());
            model.getOrganizations().add(
                    new SelectItem(poLdapOrganization.getIdentifier(),
                            nameToDisplay));
        }
    }

    void initSettingData() throws SaaSApplicationException {
        getModel().getSettings().clear();
        if (handlePlatformSettings()) {
            initPlatformSettings();
        } else {
            initResolvedSettings();
        }
    }

    private boolean handlePlatformSettings() {
        boolean handlePlatformSettings = model.getOrganizationIdentifier() == null
                && model.isOrganizationSelectionSupported();
        return handlePlatformSettings;
    }

    void initResolvedSettings() throws SaaSApplicationException {

        model.getSettings().clear();
        Set<POLdapSetting> resolvedProperties = null;
        if (model.getOrganizationIdentifier() != null) {
            resolvedProperties = getUserManagementService()
                    .getOrganizationSettingsResolved(
                            model.getOrganizationIdentifier());
        } else {
            resolvedProperties = getUserManagementService()
                    .getOrganizationSettingsResolved();
        }
        for (POLdapSetting entry : resolvedProperties) {
            model.getSettings().add(new LdapSetting(entry));
        }
        model.setShowIsPlatformSettingColumnVisible(true);
        model.setShowClearButtonVisible(false);

    }

    void initPlatformSettings() {

        new LdapSettingConverter().addToModel(model.getSettings(),
                getUserManagementService().getPlatformSettings());
        model.setShowIsPlatformSettingColumnVisible(false);
        model.setShowClearButtonVisible(true);

    }

    public String testConnection() throws SaaSApplicationException {

        boolean canConnect = false;
        if (handlePlatformSettings()) {
            canConnect = getUserManagementService().canConnect(null);
        } else {
            if (model.getOrganizationIdentifier() != null) {
                canConnect = getUserManagementService().canConnect(
                        model.getOrganizationIdentifier());
            } else {
                canConnect = getUserManagementService().canConnect();
            }
        }
        if (!canConnect) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    "error.ldap.connectionfailure");
            return "";
        }
        addSuccessMessage("info.organization.ldapsettings.tested");

        return OUTCOME_SUCCESS;
    }

    public String reset() throws SaaSApplicationException {

        if (model.getOrganizationIdentifier() == null) {
            getUserManagementService().resetOrganizationSettings();
        } else {
            getUserManagementService().resetOrganizationSettings(
                    model.getOrganizationIdentifier());
        }
        reinitModel();
        // apply possibly changed attribute mapping for user in session
        // (otherwise, 'import' page may contain wrong columns)
        updateUserInSession();
        addSuccessMessage("info.organization.ldapsettings.reset");

        return OUTCOME_SUCCESS;
    }

    public String clear() throws SaaSApplicationException {

        getUserManagementService().clearPlatformSettings();
        reinitModel();
        addSuccessMessage("info.organization.ldapsettings.clear");

        return OUTCOME_SUCCESS;
    }

    private void reinitModel() throws SaaSApplicationException {
        getModel().getSettings().clear();
        getModel().setFile(null);
        initSettingData();
    }

    public String importSettings() throws SaaSApplicationException {

        Properties propsToStore = new Properties();
        Part file = model.getFile();
        // TODO temporary fix for bug 9815 - for a proper fix, file upload
        // should have required="true" and a <mp:message /> added. But message
        // positioning is is hard for this case due to used layout and styles
        // and additionally currently for masked inputs (file upload, select
        // boxed), highlighting does not work.
        if (file == null) {
            ui.handleError(null, ERROR_NO_IMPORT_FILE);
            return OUTCOME_ERROR;
        }
        try {
            propsToStore = PropertiesLoader.loadProperties(file
                    .getInputStream());
        } catch (IOException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_IMPORT_LDAP_SETTINGS);
            addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_UPLOAD);
            return OUTCOME_ERROR;
        }
        if (handlePlatformSettings()) {
            getUserManagementService().setPlatformSettings(propsToStore);
        } else {
            if (model.getOrganizationIdentifier() == null) {
                getUserManagementService()
                        .setOrganizationSettings(propsToStore);
            } else {
                getUserManagementService().setOrganizationSettings(
                        model.getOrganizationIdentifier(), propsToStore);
            }
        }
        reinitModel();
        // apply possibly changed attribute mapping for user in session
        // (otherwise, 'import' page may contain wrong columns)
        updateUserInSession();
        addSuccessMessage("info.organization.ldapsettings.imported");

        return OUTCOME_SUCCESS;
    }

    public String exportSettings() throws IOException {

        Properties props = new LdapSettingConverter().toProperties(
                model.getSettings(),
                Strings.isEmpty(model.getOrganizationIdentifier()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            props.store(baos, null);
            writeSettings(baos.toByteArray());
        } finally {
            baos.close();
        }

        return OUTCOME_SUCCESS;
    }

    protected UserManagementService getUserManagementService() {
        if (userMgmtSvc == null) {
            userMgmtSvc = sl.findService(UserManagementService.class);
        }
        return userMgmtSvc;
    }

    public LdapConfigurationModel getModel() {
        return model;
    }

    public void setModel(LdapConfigurationModel model) {
        this.model = model;
    }

    @Override
    public void addMessage(final String clientId,
            final FacesMessage.Severity severity, final String key) {
        super.addMessage(clientId, severity, key, (Object[]) null);
    }

    public void orgChanged(ValueChangeEvent event)
            throws SaaSApplicationException {
        final String orgId = (String) event.getNewValue();
        final String oldId = model.getOrganizationIdentifier();
        model.setOrganizationIdentifier(orgId);
        if (Strings.isEmpty(orgId)) {
            model.getSettings().clear();
            model.setFile(null);
            model.setShowIsPlatformSettingColumnVisible(false);
            model.setShowClearButtonVisible(true);
            initPlatformSettings();
        } else if (!orgId.equals(oldId)) {
            initSettingData();
        }
    }

    void writeSettings(byte[] content) throws IOException {
        super.writeContentToResponse(content, "ldapSettings.properties",
                "text/plain");
    }

    void addSuccessMessage(String key) {
        addMessage(null, FacesMessage.SEVERITY_INFO, key);
    }

    public boolean isNoSettingsDefined() {
        return model.getSettings().isEmpty();
    }

    @Override
    public void updateUserInSession() {
        super.updateUserInSession();
    }

}
