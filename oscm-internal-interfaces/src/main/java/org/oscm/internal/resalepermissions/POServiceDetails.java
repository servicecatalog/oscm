/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.resalepermissions;

import org.oscm.internal.base.BasePO;

public class POServiceDetails extends BasePO {

    private static final long serialVersionUID = -1781005999877038205L;

    /**
     * The short name that uniquely identifies the service in the entire
     * platform.
     */
    private String serviceId;

    /**
     * The name of the service (long version).
     */
    private String name;

    /**
     * the organization id of vendor
     */
    private String organizationId;

    /**
     * @return the organizationId
     */
    public String getOrganizationId() {
        return organizationId;
    }

    public POServiceDetails(long key, int version, String serviceId) {
        super(key, version);
        this.serviceId = serviceId;
    }

    public POServiceDetails(long key, int version, String organizationId,
            String serviceId) {
        super(key, version);
        this.organizationId = organizationId;
        this.serviceId = serviceId;
    }

    /**
     * @param organizationId
     *            the organizationId to set
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public POServiceDetails(long key, int version) {
        super(key, version);
    }

    public POServiceDetails() {
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
