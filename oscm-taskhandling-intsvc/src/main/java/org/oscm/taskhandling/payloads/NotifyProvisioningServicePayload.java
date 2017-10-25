/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.07.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.payloads;

/**
 * @author afschar
 * 
 */
public class NotifyProvisioningServicePayload implements TaskPayload {

    private static final long serialVersionUID = 908453105018490385L;

    private long tkey;
    private boolean deactivate;

    public NotifyProvisioningServicePayload(long subTkey, boolean subDeactivate) {
        tkey = subTkey;
        deactivate = subDeactivate;
    }

    @Override
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Users: ");
        return sb.toString();
    }

    public long getTkey() {
        return tkey;
    }

    public boolean isDeactivate() {
        return deactivate;
    }
}
