/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.profile;

import java.io.Serializable;

/**
 * Represents profile data for user and his organization.
 * 
 * @author jaeger
 * 
 */
public class POProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private POUser user;
    private POOrganization organization;
    private POImageResource image;

    public POProfile(POUser user, POOrganization organization) {
        this.user = user;
        this.organization = organization;
    }

    public POProfile(POUser user, POOrganization organization,
            POImageResource image) {
        this(user, organization);
        this.image = image;
    }

    public POUser getUser() {
        return user;
    }

    public void setUser(POUser user) {
        this.user = user;
    }

    public POOrganization getOrganization() {
        return organization;
    }

    public void setOrganization(POOrganization organization) {
        this.organization = organization;
    }

    public POImageResource getImage() {
        return image;
    }

    public void setImage(POImageResource image) {
        this.image = image;
    }

}
