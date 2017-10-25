/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2012-06-11                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.landingpageService.local;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.internal.vo.BaseVO;

/**
 * Represents the landing page (home page) of a marketplace with a list of
 * services.
 */
public class VOPublicLandingpage extends BaseVO  {
    private static final long serialVersionUID = -3149524403189204525L;

    /**
     * The short name that uniquely identifies the marketplace in the platform
     * (business key).
     */
    private String marketplaceId;

    /**
     * The maximum number of services to be displayed on the page.
     */
    private int numberServices;

    /**
     * The filling criterion defined for the page.
     */
    private FillinCriterion fillinCriterion;

    /**
     * The services that are displayed on the page.
     */
    private List<VOLandingpageService> landingpageServices = new ArrayList<VOLandingpageService>();

    /**
     * Returns the identifier of the marketplace the landing page belongs to.
     * 
     * @return the marketplace ID
     */
    public String getMarketplaceId() {
        return marketplaceId;
    }

    /**
     * Sets the identifier of the marketplace the landing page belongs to.
     * 
     * @param marketplaceId
     *            the marketplace ID
     */
    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    /**
     * Retrieves the maximum number of services displayed on the landing page.
     * 
     * @return the number of services
     */
    public int getNumberServices() {
        return numberServices;
    }

    /**
     * Sets the maximum number of services to be displayed on the landing page.
     * 
     * @param numberServices
     *            the number of services
     */
    public void setNumberServices(int numberServices) {
        this.numberServices = numberServices;
    }

    /**
     * Retrieves the criterion according to which non-featured services are
     * added to the landing page if there are not enough featured services to
     * fill the page to its defined maximum.
     * 
     * @return the filling criterion
     */
    public FillinCriterion getFillinCriterion() {
        return fillinCriterion;
    }

    /**
     * Specifies the criterion according to which non-featured services are to
     * be added to the landing page if there are not enough featured services to
     * fill the page to its defined maximum.
     * 
     * @param fillinCriterion
     *            fill in criterion
     */
    public void setFillinCriterion(FillinCriterion fillinCriterion) {
        this.fillinCriterion = fillinCriterion;
    }

    /**
     * Retrieves the featured services defined for the landing page.
     * 
     * @return the list of featured services
     */
    public List<VOLandingpageService> getLandingpageServices() {
        return landingpageServices;
    }

    /**
     * Sets the featured services to be displayed on the landing page.
     * 
     * @param landingpageServices
     *            the list of featured services
     */
    public void setLandingpageServices(
            List<VOLandingpageService> landingpageServices) {
        this.landingpageServices = landingpageServices;
    }
}
