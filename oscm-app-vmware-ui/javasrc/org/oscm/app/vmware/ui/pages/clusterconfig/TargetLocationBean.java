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

@ManagedBean(name = "targetLocationBean")
@ViewScoped
public class TargetLocationBean extends UiBeanBase {

    private static final long serialVersionUID = 4584243999849571470L;
    private static final Logger logger = LoggerFactory
            .getLogger(UiBeanBase.class);

    private int selectedRowNum;
    private int currentVCenter;
    private int currentCluster = -1;
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
            SelectItem item = new SelectItem(Integer.valueOf(vc.tkey), vc.name);
            vcenterList.add(item);
            if (vcenterList.size() == 1) {
                selectedVCenter = vc;
                currentVCenter = vc.tkey;
            }
        }

        // TODO refactor
        int i = 0;
        importFileTypes.add(new SelectItem(i++, "vCenter"));
        importFileTypes.add(new SelectItem(i++, "Datacenter"));
        importFileTypes.add(new SelectItem(i++, "Cluster"));
        importFileTypes.add(new SelectItem(i++, "VLAN"));
        importFileTypes.add(new SelectItem(i, "IP Pool"));
        currentImportFileType = 0;

        parseConfiguration();
    }

    /**
     * Convert the XML for the host and storage configuration to Java objects.
     */
    private void parseConfiguration() {
        if (currentCluster == -1) {
            logger.debug("Cluster not yet set");
            return;
        }
    }

    /**
     * Save modified values to database
     */
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
                    "Failed to save load balancer settings to VMware controller database.",
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

    public String getCurrentCluster() {
        return Integer.toString(currentCluster);
    }

    public void setCurrentCluster(String currentCluster) {
        this.currentCluster = Integer.parseInt(currentCluster);
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

    public void importData(String tableName, InputStream csvFile) {
        Importer importer = ImporterFactory.getImporter(tableName, this.settings.getDataAccessService());

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

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public void uploadCSV() {
        status = null;
        //dirty = true; // TODO dirty for csv

        try {
            System.out.println("======================");
            System.out.println(convertStreamToString(this.file.getInputStream()));
            System.out.println("======================");

            //settings.saveTargetVCenter(selectedVCenter);
            //dirty = false;
        } catch (Exception e) {
//            status = Messages.get(getDefaultLanguage(),
//                    "ui.config.status.save.failed", e.getMessage());
            logger.error("Failed to upload CSV configuration file.", e);
        }
    }
}