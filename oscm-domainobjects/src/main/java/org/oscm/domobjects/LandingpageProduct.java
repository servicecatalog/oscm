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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

/**
 * JPA managed entity representing the Landingpageservice.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "LandingpageProduct.getLandingpageProducts", query = "SELECT lp FROM LandingpageProduct lp WHERE lp.landingpage = :landingpage AND lp.product.dataContainer.status NOT IN (:filterOutWithStatus) ORDER BY lp.dataContainer.position ASC"),
        @NamedQuery(name = "LandingpageProduct.deleteLandingpageProductForProduct", query = "DELETE FROM LandingpageProduct lp WHERE lp.product.key = :productKey"),
        @NamedQuery(name = "LandingpageProduct.deleteLandingpageProducts", query = "DELETE FROM LandingpageProduct lp WHERE lp.landingpage = :landingpage") })
public class LandingpageProduct extends
        DomainObjectWithVersioning<LandingpageProductData> {

    private static final long serialVersionUID = 647267865084845678L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PublicLandingpage landingpage;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    public LandingpageProduct() {
        super();
        dataContainer = new LandingpageProductData();
    }

    public int getPosition() {
        return dataContainer.getPosition();
    }

    public void setPosition(int position) {
        dataContainer.setPosition(position);
    }

    public PublicLandingpage getLandingpage() {
        return landingpage;
    }

    public void setLandingpage(PublicLandingpage landingpage) {
        this.landingpage = landingpage;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
