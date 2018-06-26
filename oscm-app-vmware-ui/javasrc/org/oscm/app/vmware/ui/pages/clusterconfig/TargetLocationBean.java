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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean(name = "targetLocationBean")
@ViewScoped
public class TargetLocationBean extends UiBeanBase {

    private static final long serialVersionUID = 4584243999849571470L;
    private static final Logger logger = LoggerFactory
            .getLogger(UiBeanBase.class);

    private int selectedRowNum;
    private int currentVCenter;
    private VCenter selectedVCenter;
    private List<SelectItem> vcenterList = new ArrayList<>();
    private List<SelectItem> importFileTypes = new ArrayList<>();
    private int currentImportFileType;
    private boolean dirty = false;

    private Part file;

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }

    List<VCenter> vcenter;

    public TargetLocationBean(DataAccessService das) {
        settings.useMock(das);
        initBean();
    }

    public TargetLocationBean() {
        initBean();
    }

    private void initBean() {
        vcenter = settings.getTargetVCenter();
        for (VCenter vc : vcenter) {
            SelectItem item = new SelectItem(vc.tkey, vc.name);
            vcenterList.add(item);
            if (vcenterList.size() == 1) {
                selectedVCenter = vc;
                currentVCenter = vc.tkey;
            }
        }

        importFileTypes = ConfigurationFileType.ALL
                .entrySet()
                .stream()
                .map(e -> new SelectItem(e.getKey(), e.getValue().getDisplayName()))
                .collect(Collectors.toList());

        currentImportFileType = 0;
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

    public boolean isDirty() {
        return dirty;
    }

    protected String getDefaultLanguage() {
        return FacesContext.getCurrentInstance().getApplication()
                .getDefaultLocale().getLanguage();
    }

    public int getSelectedRowNum() {
        return selectedRowNum;
    }

    public void setSelectedRowNum(int selectedRowNum) {
        this.selectedRowNum = selectedRowNum;
    }

    public List<SelectItem> getVcenterList() {
        return vcenterList;
    }

    public List<SelectItem> getImportFileTypes() {
        return importFileTypes;
    }

    public String getUnsavedChangesMsg() {
        return Messages.get(getDefaultLanguage(),
                "confirm.unsavedChanges.lost");
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

    public String getLoggedInUserId() {
        FacesContext facesContext = getFacesContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);
        if (session != null) {
            return "" + session.getAttribute("loggedInUserId");
        }
        return null;
    }

    public void uploadConfig() {
        status = null;
        //dirty = true; // TODO dirty for csv

        try {
            String tableName = ConfigurationFileType.ALL
                    .get(this.currentImportFileType).getTableName();
            importData(tableName, this.file.getInputStream());

            //dirty = false;
        } catch (Exception e) {
//            status = Messages.get(getDefaultLanguage(),
//                    "ui.config.status.save.failed", e.getMessage());
            logger.error("Failed to upload CSV configuration file.", e);
        }
    }

    private void importData(String tableName, InputStream csvFile) {
        Importer importer = ImporterFactory.getImporter(tableName,
                this.settings.getDataAccessService());

        try {
            importer.load(csvFile);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public String getCurrentImportFileType() {
        return Integer.toString(currentImportFileType);
    }

    public void setCurrentImportFileType(String currentImportFileType) {
        this.currentImportFileType = Integer.parseInt(currentImportFileType);
    }
}