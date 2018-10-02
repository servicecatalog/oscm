/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.oscm.app.azure.i18n.Messages;

public class AccessInfo {

    /**
     * Azure
     */
    private List<AzureAccess> azureAccesses;

    /**
     * Azure
     */
    public AccessInfo() {
        this.azureAccesses = new ArrayList<>();
    }

    /**
     * Azure
     */
    public List<AzureAccess> getAzureAccesses() {
        return azureAccesses;
    }

    /**
     * Azure
     */
    public void setAzureAccesses(List<AzureAccess> azureAccesses) {
        this.azureAccesses = azureAccesses;
    }

    /**
     * Azure
     */
    public String getOutput(String locale) {
        if (azureAccesses == null || azureAccesses.isEmpty()) {
            return Messages.get(locale, "accessInfo_NOT_AVAILABLE");
        }
        Iterator<AzureAccess> accessesItr = azureAccesses.iterator();
        List<String> messages = new ArrayList<>();
        while (accessesItr.hasNext()) {
            AzureAccess access = accessesItr.next();
            String message ;
            
        	if((access.getPublicIpAddress().isEmpty()&&access.getPrivateIpAddress().isEmpty()))
            {
            	message = Messages.get(locale, "accessInfo_RUNNING",access.getVmName(),access.getState());
            }
        	else if((access.getPublicIpAddress().isEmpty()))
            {
            	message = Messages.get(locale, "accessInfo_RUNNING_with_privateIP",access.getVmName(),access.getPrivateIpAddress(),access.getState());
            }
        	else if((access.getPrivateIpAddress().isEmpty()))
            {
            	message = Messages.get(locale, "accessInfo_RUNNING_with_publicIP",access.getVmName(),access.getPublicIpAddress(),access.getState());
            }
            else
            	message = Messages.get(locale, "accessInfo_RUNNING_with_all_IP",access.getVmName(),access.getPublicIpAddress(),access.getPrivateIpAddress(),access.getState());

            messages.add(message);
        }
        return StringUtils.join(messages, "; ").replace("\n", " ");
    }
}
