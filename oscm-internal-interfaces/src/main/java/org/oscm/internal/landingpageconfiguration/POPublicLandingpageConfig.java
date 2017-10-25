/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.landingpageconfiguration;

import java.util.ArrayList;
import java.util.List;

import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.internal.base.BasePO;

public class POPublicLandingpageConfig extends BasePO {

    private static final long serialVersionUID = 2314052924441819919L;

    String marketplaceId;
    int numberOfServicesOnLp = 0;
    FillinCriterion fillinCriterion = FillinCriterion.ACTIVATION_DESCENDING;
    List<POService> featuredServices;

    public int getNumberOfServicesOnLp() {
        return numberOfServicesOnLp;
    }

    public void setNumberOfServicesOnLp(int numberOfServicesOnLp) {
        this.numberOfServicesOnLp = numberOfServicesOnLp;
    }

    public FillinCriterion getFillinCriterion() {
        return fillinCriterion;
    }

    public void setFillinCriterion(FillinCriterion fillin) {
        fillinCriterion = fillin;
    }

    public List<POService> getFeaturedServices() {
        if (featuredServices == null) {
            featuredServices = new ArrayList<POService>();
        }
        return featuredServices;
    }

    public void setFeaturedServices(List<POService> featuredServices) {
        this.featuredServices = featuredServices;

    }

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

}
