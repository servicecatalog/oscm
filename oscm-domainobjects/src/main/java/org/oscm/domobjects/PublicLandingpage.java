/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi                      
 *                                                                              
 *  Creation Date: 11.06.2012                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import org.oscm.types.enumtypes.FillinCriterion;

/**
 * The landingpage represents the startpage with per default shown services.
 * This landing page is used for large public marketplaces that has typical
 * features like an app store (reviews, categories, tag cloud, etc)
 */
@Entity
public class PublicLandingpage extends
        DomainObjectWithVersioning<PublicLandingpageData> {

    private static final long serialVersionUID = 1098523770937046486L;

    public static final FillinCriterion DEFAULT_FILLINCRITERION = FillinCriterion
            .getDefault();
    public static final int DEFAULT_NUMBERSERVICES = 6;

    public PublicLandingpage() {
        super();
        dataContainer = new PublicLandingpageData();
    }

    @OneToOne(mappedBy = "publicLandingpage", fetch = FetchType.LAZY, optional = false)
    private Marketplace marketplace;

    @OneToMany(mappedBy = "landingpage", fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST, CascadeType.REMOVE })
    @OrderBy(value = "dataContainer.position ASC")
    private List<LandingpageProduct> landingpageProducts = new LinkedList<LandingpageProduct>();

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public List<LandingpageProduct> getLandingpageProducts() {
        return landingpageProducts;
    }

    public void setLandingpageProducts(
            List<LandingpageProduct> landingpageProducts) {
        this.landingpageProducts = landingpageProducts;
    }

    public int getNumberServices() {
        return dataContainer.getNumberServices();
    }

    public void setNumberServices(int numberServices) {
        this.dataContainer.setNumberServices(numberServices);
    }

    public FillinCriterion getFillinCriterion() {
        return dataContainer.getFillinCriterion();
    }

    public void setFillinCriterion(FillinCriterion fillinCriterion) {
        this.dataContainer.setFillinCriterion(fillinCriterion);
    }

    public static PublicLandingpage newDefault() {
        PublicLandingpage defaultLandingPage = new PublicLandingpage();
        defaultLandingPage.setDefaults();
        return defaultLandingPage;
    }

    public void setDefaults() {
        this.setFillinCriterion(DEFAULT_FILLINCRITERION);
        this.setNumberServices(DEFAULT_NUMBERSERVICES);
        this.getLandingpageProducts().clear();
    }
}
