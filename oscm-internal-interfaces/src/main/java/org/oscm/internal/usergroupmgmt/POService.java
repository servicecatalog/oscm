/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.usergroupmgmt;

import org.oscm.internal.base.BasePO;

public class POService extends BasePO {

    private static final long serialVersionUID = -5030048296052716670L;

    String serviceName;

    String providerName;

    String productId;

    public POService(long key, int version) {
        super(key, version);
    }

    public POService() {
        super(0, 0);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((productId == null) ? 0 : productId.hashCode());
        result = prime * result
                + ((providerName == null) ? 0 : providerName.hashCode());
        result = prime * result
                + ((serviceName == null) ? 0 : serviceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        POService other = (POService) obj;
        if (productId == null) {
            if (other.productId != null)
                return false;
        } else if (!productId.equals(other.productId))
            return false;
        if (providerName == null) {
            if (other.providerName != null)
                return false;
        } else if (!providerName.equals(other.providerName))
            return false;
        if (serviceName == null) {
            if (other.serviceName != null)
                return false;
        } else if (!serviceName.equals(other.serviceName))
            return false;
        return true;
    }

}
