/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2019-04-04
 *
 *******************************************************************************/


package org.oscm.app.vmware.ui.pages.clusterconfig;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.model.Cluster;
import org.oscm.app.vmware.business.model.IPAddress;
import org.oscm.app.vmware.business.model.IPPool;
import org.oscm.app.vmware.business.model.VLAN;

@ManagedBean(name = "clusterDetailsBean")
@ViewScoped
public class ClusterDetailsBean {

    private Cluster cluster;
    private List<VLAN> vlans;
    private VMPropertyHandler settings;
    private String xml;

    public ClusterDetailsBean() {
    }

    public ClusterDetailsBean(VMPropertyHandler settings, Cluster cluster) {
        this.settings = settings;
        this.cluster = cluster;
        initBean();
    }

    public String formatXML(String xmlFile) {
        try {
            Source xml = new StreamSource(new StringReader(xmlFile));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xml, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            return "No Valid XML";
        }
    }

    private void initBean() {
        this.vlans = settings.getVLANs(cluster);
        this.setXml(formatXML(cluster.getLoadbalancer()));
        getIpAdresses();
    }

    private void getIpAdresses() {
        for (int i = 0; vlans.size() > i; i++) {
            List<IPPool> ippools = settings.getIPPool(vlans.get(i));
            List<IPAddress> ipList = new ArrayList<>();
            for (int j = 0; ippools.size() > j; j++) {
                IPAddress ip = new IPAddress();
                ip.setIPAddress(ippools.get(j).getIp_adress());
                ipList.add(ip);
            }
            vlans.get(i).setIPAddresses(ipList);
        }
    }

    public void setVlans(List<VLAN> vlans) {
        this.vlans = vlans;
    }

    public List<VLAN> getVlans() {
        return vlans;
    }

    public String getIpsAsString(int tkey) {
        String ips = "";
        for (int i = 0; vlans.size() > i; i++) {
            if (tkey == vlans.get(i).getTkey()) {
                for (int j = 0; vlans.get(i).getIPAddresses().size() > j; j++) {
                    ips = ips + vlans.get(i).getIPAddresses().get(j)
                            .getIPAddress();
                    if (j + 1 < vlans.get(i).getIPAddresses().size()) {
                        ips = ips + ", ";
                    }
                }
            }
        }
        return ips;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getXml() {
        return xml;
    }

}
