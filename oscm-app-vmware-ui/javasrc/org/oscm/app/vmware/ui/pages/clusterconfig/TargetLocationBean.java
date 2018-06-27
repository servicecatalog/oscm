/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui.pages.clusterconfig;

import org.oscm.app.vmware.business.model.VCenter;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.importer.Importer;
import org.oscm.app.vmware.importer.ImporterFactory;
import org.oscm.app.vmware.importer.model.ConfigurationType;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.oscm.app.vmware.ui.UiBeanBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean(name = "targetLocationBean")
@ViewScoped
public class TargetLocationBean extends UiBeanBase {
    private static final long serialVersionUID = 4584243999849571470L;
    private static final Logger logger = LoggerFactory.getLogger(UiBeanBase.class);
    private int selectedRowNum;
    private int currentVCenter;
    private List<VCenter> vcenter;
    private VCenter selectedVCenter;
    private List<SelectItem> vcenterList = new ArrayList<>();
    private int currentConfigFileType = 0;
    private List<SelectItem> configFileTypes = new ArrayList<>();
    private boolean dirty = false;
    private Part file;


    public TargetLocationBean(DataAccessService das) {
        settings.useMock(das);
        initBean();
    }

    public TargetLocationBean() {
        initBean();
    }

    private void initBean() {
        initVCenters();
        initConfigFileTypes();
    }

    private void initVCenters() {
        vcenter = settings.getTargetVCenter();
        vcenterList = vcenter
                .stream()
                .map(e -> new SelectItem(e.tkey, e.name))
                .collect(Collectors.toList());

        if(vcenter.size() >= 1) {
            selectedVCenter = vcenter.get(0);
            currentVCenter = selectedVCenter.tkey;
        }
    }

    private void initConfigFileTypes() {
        configFileTypes = Arrays.stream(ConfigurationType.values())
                .map(ct -> new SelectItem(ct.getId(), ct.getDisplayName()))
                .collect(Collectors.toList());

        if(configFileTypes.size() >= 1) {
            currentConfigFileType = 0;
        }
    }

    public void save() {
        status = null;
        dirty = true;

        try {
            settings.saveTargetVCenter(selectedVCenter);
            dirty = false;
        } catch (Exception e) {
            status = Messages.get(getDefaultLanguage(),
                    "ui.config.status.save.failed", e.getMessage());
            logger.error(
                    "Failed to save vSphere API settings to VMware controller database.",
                    e);
        }
    }

    public void uploadConfig() {
        status = null;

        try {
            ConfigurationType ct = ConfigurationType.values()[this.currentConfigFileType];
            Importer importer = ImporterFactory.getImporter(ct,
                    this.settings.getDataAccessService());
            importer.load(this.file.getInputStream());

            status = Messages.get(getDefaultLanguage(),
                    "ui.config.status.uploaded");
        } catch (Exception e) {
            status = Messages.get(getDefaultLanguage(),
                    "ui.config.status.upload.failed", e.getMessage());
            logger.error("Failed to upload CSV configuration file.", e);
        }
    }

    public void valueChangeVCenter(ValueChangeEvent event) {
        status = null;
        if (event.getNewValue() != null) {
            currentVCenter = Integer.parseInt((String) event.getNewValue());
            selectedVCenter = getVCenter(currentVCenter);
            logger.debug(selectedVCenter.name);
        }
    }

    private VCenter getVCenter(int tkey) {
        for (VCenter vc : vcenter) {
            if (vc.tkey == tkey) {
                return vc;
            }
        }
        return null;
    }

    public String getLoggedInUserId() {
        FacesContext facesContext = getFacesContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);
        if (session != null) {
            return "" + session.getAttribute("loggedInUserId");
        }
        return null;
    }

    public String getUnsavedChangesMsg() {
        return Messages.get(getDefaultLanguage(),
                "confirm.unsavedChanges.lost");
    }

    protected String getDefaultLanguage() {
        return FacesContext.getCurrentInstance().getApplication()
                .getDefaultLocale().getLanguage();
    }

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public boolean isDirty() {
        return dirty;
    }

    public List<SelectItem> getVcenterList() {
        return vcenterList;
    }

    public List<SelectItem> getConfigFileTypes() {
        return configFileTypes;
    }

    public int getSelectedRowNum() {
        return selectedRowNum;
    }

    public void setSelectedRowNum(int selectedRowNum) {
        this.selectedRowNum = selectedRowNum;
    }

    public String getCurrentConfigFileType() {
        return Integer.toString(currentConfigFileType);
    }

    public void setCurrentConfigFileType(String currentConfigFileType) {
        this.currentConfigFileType = Integer.parseInt(currentConfigFileType);
    }

    public String getCurrentVCenter() {
        return Integer.toString(currentVCenter);
    }

    public void setCurrentVCenter(String currentVCenter) {
        this.currentVCenter = Integer.parseInt(currentVCenter);
    }

    public VCenter getSelectedVCenter() {
        return selectedVCenter;
    }

    public void setSelectedVCenter(VCenter selectedVCenter) {
        this.selectedVCenter = selectedVCenter;
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }
}