package org.oscm.identityservice.model;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.internal.vo.VOUserDetails;

public class UserImportModel {

    Organization organization;
    
    VOUserDetails user;
    
    Marketplace  marketplace;
    
    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organizatin) {
        this.organization = organizatin;
    }

    public VOUserDetails getUser() {
        return user;
    }

    public void setUser(VOUserDetails user) {
        this.user = user;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }


}
