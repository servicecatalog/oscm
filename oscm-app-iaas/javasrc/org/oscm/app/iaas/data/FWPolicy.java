/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-12-18                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

/**
 * 
 */
public class FWPolicy {

    public enum Action {
        Accept, Reject
    };

    public enum Protocol {
        TCP, UDP, TCP_UDP, ICMP
    }

    public static final String ZONE_INTERNET = "INTERNET";
    public static final String ZONE_INTRANET = "INTRANET";

    private String srcZone; // INTERNET, INTRANET, DMZ, SECURE1, SECURE2
    private String dstZone; // INTERNET, INTRANET, DMZ, SECURE1, SECURE2
    private Action action;
    private String dst; // IP address, FQDN
    private String dstPort; // Port or port range
    private boolean log; // Turn on/off logging for this policy
    private Protocol protocol;
    private String src; // IP address, FQDN
    private String srcPort; // Port or port range
    private String dstService; // DNS, NTP, WSUS, yum, ...
    private String dstType; // IP FQDN

    public String getDstService() {
        return dstService;
    }

    public void setDstService(String dstService) {
        this.dstService = dstService;
    }

    public String getDstType() {
        return dstType;
    }

    public void setDstType(String dstType) {
        this.dstType = dstType;
    }

    public String getSrcZone() {
        return srcZone;
    }

    public void setSrcZone(String srcZone) {
        this.srcZone = srcZone;
    }

    public String getDstZone() {
        return dstZone;
    }

    public void setDstZone(String dstZone) {
        this.dstZone = dstZone;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getDstPort() {
        return dstPort;
    }

    public void setDstPort(String dstPort) {
        this.dstPort = dstPort;
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(String srcPort) {
        this.srcPort = srcPort;
    }

    @Override
    public FWPolicy clone() {
        FWPolicy p = new FWPolicy();
        p.setAction(action);
        p.setDst(dst);
        p.setDstService(dstService);
        p.setDstType(dstType);
        p.setDstPort(dstPort);
        p.setDstZone(dstZone);
        p.setLog(log);
        p.setProtocol(protocol);
        p.setSrc(src);
        p.setSrcPort(srcPort);
        p.setSrcZone(srcZone);
        return p;
    }

}
