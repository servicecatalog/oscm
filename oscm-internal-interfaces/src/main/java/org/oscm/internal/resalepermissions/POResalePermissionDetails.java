/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.resalepermissions;

import org.oscm.internal.base.BasePO;
import org.oscm.internal.pricing.POOrganization;
import org.oscm.internal.types.enumtypes.OfferingType;

public class POResalePermissionDetails extends BasePO {

    private static final long serialVersionUID = 740387744736964441L;

    /**
     * The related marketable service
     */
    private POServiceDetails service;

    /**
     * The organization, which grants the resale permission
     */
    private POOrganization grantor;

    /**
     * The organization, which receives the resale permission
     */
    private POOrganization grantee;

    /**
     * The resale permission type
     */
    private OfferingType offeringType;

    public POResalePermissionDetails(long key, int version) {
        super(key, version);
    }

    public POResalePermissionDetails() {
        super(0, 0);
    }

    public POServiceDetails getService() {
        return service;
    }

    public void setService(POServiceDetails service) {
        this.service = service;
    }

    public POOrganization getGrantor() {
        return grantor;
    }

    public void setGrantor(POOrganization grantor) {
        this.grantor = grantor;
    }

    public POOrganization getGrantee() {
        return grantee;
    }

    public void setGrantee(POOrganization grantee) {
        this.grantee = grantee;
    }

    public OfferingType getOfferingType() {
        return offeringType;
    }

    public void setOfferingType(OfferingType offeringType) {
        this.offeringType = offeringType;
    }
}
