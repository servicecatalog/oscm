/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Christoph Held                     
 *                                                                              
 *  Creation Date: 29.1.2014                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * The landingpage represents the startpage with per default shown services.
 * This landing page is intended for internal enterprise marketplaces that
 * typically offer only a small number of services. Many features (reviews,
 * categories, tag cloud, etc) are not needed.
 */
@Entity
public class EnterpriseLandingpage extends
        DomainObjectWithVersioning<EnterpriseLandingpageData> {

    private static final long serialVersionUID = 1098523770937046486L;

    public EnterpriseLandingpage() {
        super();
        dataContainer = new EnterpriseLandingpageData();
    }

    @OneToOne(mappedBy = "enterpriseLandingpage", fetch = FetchType.LAZY, optional = false)
    private Marketplace marketplace;

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

}
