/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 29.08.2016
 *
 *******************************************************************************/
package org.oscm.internal.tenant;

import org.oscm.internal.base.BasePO;
import org.oscm.internal.vo.VOTenant;

public class POTenant extends BasePO {

	private static final long serialVersionUID = -3225367553154478323L;
	private String name;
	private String description;
	private String tenantId;

	public POTenant() {

	}

	public POTenant(VOTenant voTenant) {
		this.tenantId = voTenant.getTenantId();
		this.name = voTenant.getName();
		this.description = voTenant.getDescription();
		this.setKey(voTenant.getKey());
		this.setVersion(voTenant.getVersion());
	}

	public VOTenant toVOTenanat() {
		VOTenant voTenant = new VOTenant();
		voTenant.setKey(this.getKey());
		voTenant.setVersion(this.getVersion());
		voTenant.setTenantId(this.getTenantId());
		voTenant.setName(this.getName());
		voTenant.setDescription(this.getDescription());
		return voTenant;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
