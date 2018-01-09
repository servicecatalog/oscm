/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui.pages.clusterconfig;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration2.XMLConfiguration;
import org.oscm.app.vmware.business.balancer.XMLHostConfiguration;
import org.oscm.app.vmware.business.model.Cluster;
import org.oscm.app.vmware.business.model.VCenter;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.oscm.app.vmware.ui.UiBeanBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "targetLocationBean")
@ViewScoped
public class TargetLocationBean extends UiBeanBase {

    private static final long serialVersionUID = 4584243999849571470L;
    private static final Logger logger = LoggerFactory
            .getLogger(UiBeanBase.class);
    private static final String ELEMENT_HOST = "host";
    private static final String ELEMENT_STORAGE = "storage";
    private static final String ELEMENT_BALANCER = "balancer";

    //private List<HostConfig> hostList;
    //private List<StorageConfig> storageList;
    //private HostBalancerConfig hostBalancer;
    private XMLConfiguration xmlConfig;
    private int selectedRowNum;

    private int currentVCenter;
    //private String currentDatacenter;
    private int currentCluster = -1;

    private VCenter selectedVCenter;
    //private Cluster selectedCluster;

    private List<SelectItem> vcenterList = new ArrayList<SelectItem>();
    //private List<SelectItem> datacenterList = new ArrayList<SelectItem>();
    //private List<SelectItem> clusterList = new ArrayList<SelectItem>();
    //private List<SelectItem> hostBalancerList = new ArrayList<SelectItem>();
    //private List<SelectItem> storageBalancerList = new ArrayList<SelectItem>();
    private boolean dirty = false;

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
                /*for (Datacenter dc : vc.datacenter) {
                    SelectItem item2 = new SelectItem(dc.name, dc.name);
                    datacenterList.add(item2);
                    if (datacenterList.size() == 1) {
                        for (Cluster cluster : dc.cluster) {
                            SelectItem item3 = new SelectItem(
                                    Integer.valueOf(cluster.tkey),
                                    cluster.name);
                            clusterList.add(item3);
                            if (clusterList.size() == 1) {
                                selectedCluster = cluster;
                                currentCluster = cluster.tkey;
                            }
                        }
                    }
                }*/
            }
        }

        /*hostBalancerList.add(new SelectItem(
                "org.oscm.app.vmware.business.balancer.DynamicEquipartitionHostBalancer",
                "Distribute VMs equally over all hosts (dynamically)"));
        hostBalancerList.add(new SelectItem(
                "org.oscm.app.vmware.business.balancer.EquipartitionHostBalancer",
                "Distribute VMs equally over all hosts (static configuration)"));
        hostBalancerList.add(new SelectItem(
                "org.oscm.app.vmware.business.balancer.SequentialHostBalancer",
                "Distribute VMs in the order of the configured hosts"));

        storageBalancerList.add(new SelectItem(
                "org.oscm.app.vmware.business.balancer.EquipartitionStorageBalancer",
                "Distribute VMs equally over all storages"));
        storageBalancerList.add(new SelectItem(
                "org.oscm.app.vmware.business.balancer.SequentialStorageBalancer",
                "Distribute VMs in the order of the configured storages"));
        */
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
        /*hostList = new ArrayList<HostConfig>();
        storageList = new ArrayList<StorageConfig>();

        xmlConfig = new XMLHostConfiguration();
        Cluster cl = getCluster(currentCluster);
        try {
            if (cl.loadbalancer == null) {
                String message = Messages.get(getDefaultLanguage(),
                        "ui.config.status.noconfig");
                throw new ConfigurationException(message);
            }
            xmlConfig.load(new StringReader(cl.loadbalancer));

        } catch (ConfigurationException e) {
            logger.error("Failed to parse load balancer XML", e);
            status = Messages.get(getDefaultLanguage(),
                    "ui.config.status.invalidconfig",
                    new Object[] { e.getMessage() });
            xmlConfig.setProperty(ELEMENT_BALANCER, "");
        }

        HierarchicalConfiguration hostBalancerConfig = xmlConfig
                .configurationAt(ELEMENT_BALANCER, true);
        hostBalancer = new HostBalancerConfig(hostBalancerConfig);

        List<HierarchicalConfiguration> hosts = xmlConfig
                .configurationsAt(ELEMENT_HOST);
        for (HierarchicalConfiguration host : hosts) {
            HostConfig vmHost = new HostConfig(host);
            hostList.add(vmHost);

            HierarchicalConfiguration storageBalancerConfig = host
                    .configurationAt(ELEMENT_BALANCER, true);
            StorageBalancerConfig hBalancer = new StorageBalancerConfig(
                    storageBalancerConfig);
            vmHost.setBalancer(hBalancer);
        }

        List<HierarchicalConfiguration> storages = xmlConfig
                .configurationsAt(ELEMENT_STORAGE);
        for (HierarchicalConfiguration storage : storages) {
            StorageConfig vmStorage = new StorageConfig(storage);
            storageList.add(vmStorage);
        }*/
    }

    /**
     * Save modified values to database
     */
    public void save() {
        //logger.debug("Save settings for cluster " + selectedCluster.name);
        status = null;
        dirty = true;

        /*for (HostConfig host : getHosts()) {
            String[] storages = host.getBalancer().getStorages().split(",");
            boolean foundStorage = false;
            for (String hoststorage : storages) {
                for (StorageConfig storage : getStorages()) {
                    if (hoststorage.equals(storage.getName())) {
                        foundStorage = true;
                    }
                }

                if (!foundStorage) {
                    status = Messages.get(getDefaultLanguage(),
                            "ui.config.status.invalidhostconfig",
                            new Object[] { hoststorage, host.getName() });
                    logger.error("The storage " + hoststorage + " for host "
                            + host.getName() + " is not defined.");
                    logger.error(
                            "Failed to save load balancer settings to VMware controller database.");
                    return;
                }
            }
        }*/

        try {
            //selectedCluster.loadbalancer = getXml();
            settings.saveTargetVCenter(selectedVCenter);
            dirty = false;
        } catch (Exception e) {
            status = Messages.get(getDefaultLanguage(),
                    "ui.config.status.save.failed",
                    new Object[] { e.getMessage() });
            logger.error(
                    "Failed to save load balancer settings to VMware controller database.",
                    e);
        }
    }

    /*public void reset() {
        status = null;
        parseConfiguration();
        readControllerSettings();
    }*/

    public boolean isDirty() {
        return dirty;
    }

    private String getXml() throws Exception {
        StringWriter sw = new StringWriter();
        xmlConfig.write(sw);
        return sw.toString();
    }

    protected String getDefaultLanguage() {
        return FacesContext.getCurrentInstance().getApplication()
                .getDefaultLocale().getLanguage();
    }

    /*public List<StorageConfig> getStorages() {
        return storageList;
    }

    public List<HostConfig> getHosts() {
        return hostList;
    }

    public HostBalancerConfig getHostBalancer() {
        return hostBalancer;
    }

    public void addHost() {
        String hostRef = ELEMENT_HOST + "(" + getHosts().size() + ")";
        xmlConfig.addProperty(hostRef, "");
        HierarchicalConfiguration hxml = xmlConfig.configurationAt(hostRef,
                true);
        hxml.setProperty(ELEMENT_BALANCER, "");

        HostConfig cfg = new HostConfig(hxml);
        cfg.setName("");
        cfg.setEnabled(false);

        StorageBalancerConfig bal = new StorageBalancerConfig(
                hxml.configurationAt(ELEMENT_BALANCER, true));
        bal.setStorages("");
        cfg.setBalancer(bal);
        hostList.add(cfg);
    }

    public void addStorage() {
        String hostRef = ELEMENT_STORAGE + "(" + getStorages().size() + ")";
        xmlConfig.addProperty(hostRef, "");
        HierarchicalConfiguration hxml = xmlConfig.configurationAt(hostRef,
                true);

        StorageConfig cfg = new StorageConfig(hxml);
        cfg.setName("");
        cfg.setEnabled(false);
        cfg.setLimit("");
        storageList.add(cfg);
    }

    public void deleteHost() {
        if (selectedRowNum < 0 || selectedRowNum >= getHosts().size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        String elmRef = ELEMENT_HOST + "(" + selectedRowNum + ")";
        xmlConfig.clearTree(elmRef);
        hostList.remove(selectedRowNum);
    }

    public void deleteStorage() {
        if (selectedRowNum < 0 || selectedRowNum >= getStorages().size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        String elmRef = ELEMENT_STORAGE + "(" + selectedRowNum + ")";
        xmlConfig.clearTree(elmRef);
        storageList.remove(selectedRowNum);
    }*/

    public int getSelectedRowNum() {
        return selectedRowNum;
    }

    public void setSelectedRowNum(int selectedRowNum) {
        this.selectedRowNum = selectedRowNum;
    }

    /*public List<SelectItem> getHostBalancerList() {
        return hostBalancerList;
    }

    public List<SelectItem> getStorageBalancerList() {
        return storageBalancerList;
    }
*/
    public List<SelectItem> getVcenterList() {
        return vcenterList;
    }
/*
    public List<SelectItem> getDatacenterList() {
        return datacenterList;
    }

    public List<SelectItem> getClusterList() {
        return clusterList;
    }
*/
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
            /*datacenterList.clear();
            for (Datacenter dc : selectedVCenter.datacenter) {
                SelectItem item = new SelectItem(dc.name, dc.name);
                datacenterList.add(item);
                if (datacenterList.size() == 1) {
                    clusterList.clear();
                    for (Cluster cluster : dc.cluster) {
                        SelectItem it = new SelectItem(
                                Integer.valueOf(cluster.tkey), cluster.name);
                        clusterList.add(it);
                        if (clusterList.size() == 1) {
                            selectedCluster = cluster;
                            currentCluster = cluster.tkey;
                            parseConfiguration();
                        }
                    }
                }
            }*/

        }
    }

    /*public void valueChangeDatacenter(ValueChangeEvent event) {
        status = null;
        if (event.getNewValue() != null) {
            currentDatacenter = (String) event.getNewValue();
            Datacenter dc = getDatacenter(currentDatacenter);
            logger.debug(dc.name);
            clusterList.clear();
            for (Cluster cluster : dc.cluster) {
                SelectItem item = new SelectItem(cluster.name, cluster.name);
                clusterList.add(item);
                if (clusterList.size() == 1) {
                    selectedCluster = cluster;
                    currentCluster = cluster.tkey;
                    parseConfiguration();
                }
            }
        }
    }

    public void valueChangeCluster(ValueChangeEvent event) {
        status = null;
        if (event.getNewValue() != null) {
            currentCluster = Integer.parseInt((String) event.getNewValue());
            Cluster cl = getCluster(currentCluster);
            selectedCluster = cl;
            logger.debug(cl.name);
            hostBalancer = null;
            parseConfiguration();
        }
    }*/

    private VCenter getVCenter(int tkey) {
        for (VCenter vc : vcenter) {
            if (vc.tkey == tkey) {
                return vc;
            }
        }
        return null;
    }

    /*private Datacenter getDatacenter(String name) {
        for (Datacenter dc : selectedVCenter.datacenter) {
            if (dc.name.equals(name)) {
                return dc;
            }
        }
        return null;
    }

    private Cluster getCluster(int tkey) {
        for (VCenter vc : vcenter) {
            for (Datacenter dc : vc.datacenter) {
                for (Cluster cl : dc.cluster) {
                    if (cl.tkey == tkey) {
                        return cl;
                    }
                }
            }
        }
        return null;
    }*/

    public String getCurrentVCenter() {
        return Integer.toString(currentVCenter);
    }

    public void setCurrentVCenter(String currentVCenter) {
        this.currentVCenter = Integer.parseInt(currentVCenter);
    }
/*
    public String getCurrentDatacenter() {
        return currentDatacenter;
    }

    public void setCurrentDatacenter(String currentDatacenter) {
        this.currentDatacenter = currentDatacenter;
    }
*/
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
/*
    public Cluster getSelectedCluster() {
        return selectedCluster;
    }

    public void setSelectedCluster(Cluster selectedCluster) {
        this.selectedCluster = selectedCluster;
    }*/

    public String getLoggedInUserId() {
        FacesContext facesContext = getFacesContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
            .getSession(false);
        if (session != null) {
            String loggedInUserId = ""
                + session.getAttribute("loggedInUserId");
            return loggedInUserId;
        }
        return null;
    }

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }
}