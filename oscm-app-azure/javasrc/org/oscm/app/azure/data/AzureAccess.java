/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.data;

import java.util.ArrayList;
import java.util.List;

public class AzureAccess {

	/**
	 * Azure
	 */
    private List<String> vmName;

	/**
	 * Azure
	 */
    private List<String> publicIpAddress;

	/**
	 * Azure
	 */
    private List<String> privateIpAddress;

	/**
	 * Azure
	 */
    private List<String> state;

	/**
	 * Azure
	 */
    public AzureAccess() {
		this.vmName = new ArrayList<>();
		this.publicIpAddress = new ArrayList<>();
		this.privateIpAddress = new ArrayList<>();
		this.state = new ArrayList<>();
    }

	public List<String> getVmName() {
        return vmName;
    }

	public void setVmName(List<String> vmName) {
        this.vmName = vmName;
    }

	public List<String> getPublicIpAddress() {
        return publicIpAddress;
    }

	public void setPublicIpAddress(List<String> publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

	public List<String> getPrivateIpAddress() {
		return privateIpAddress;
	}

	public void setPrivateIpAddress(List<String> privateIpAddress) {
		this.privateIpAddress = privateIpAddress;
	}

	public List<String> getState() {
		return state;
	}

	public void setState(List<String> state) {
		this.state = state;
	}

    

    
}
