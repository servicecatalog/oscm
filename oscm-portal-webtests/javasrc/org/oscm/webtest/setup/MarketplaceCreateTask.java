/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Sep 7, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 7, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOMarketplace;

/**
 * Custom ANT task creating marketplaces using the WS-API.
 * 
 * @author Dirk Bernsau
 * 
 */
public class MarketplaceCreateTask extends WebtestTask {

    private boolean isOpen = false;
    private String name;
    private String ownerId;
    private boolean failonerror = true;
    private String mIdProperty;
    private boolean isReviewEnabled = true;
    private boolean isTaggingEnabled = true;
    private boolean isSocialBookmarksEnabled = true;

    public void setOpen(String value) {
        this.isOpen = Boolean.parseBoolean(value);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setFailOnError(String value) {
        failonerror = Boolean.parseBoolean(value);
    }

    public void setMIdProperty(String mIdProperty) {
        this.mIdProperty = mIdProperty;
    }

    public void setReviewEnabled(String value) {
        this.isReviewEnabled = Boolean.parseBoolean(value);
    }

    public void setSocialBookmarksEnabled(String value) {
        this.isSocialBookmarksEnabled = Boolean.parseBoolean(value);
    }

    public void setTaggingEnabled(String value) {
        this.isTaggingEnabled = Boolean.parseBoolean(value);
    }

    @Override
    public void executeInternal() throws Exception {

        MarketplaceService mpSvc = getServiceInterface(MarketplaceService.class);
        AccountService accSvc = getServiceInterface(AccountService.class);
        if (isEmpty(ownerId)) {
            // use ID of calling platform operator
            ownerId = accSvc.getOrganizationData().getOrganizationId();
        } else {
            ownerId = ownerId.trim();
        }
        if (isEmpty(name)) {
            name = "MP of " + accSvc.getOrganizationData().getName();
        }
        try {
            VOMarketplace vo = new VOMarketplace();
            vo.setOpen(isOpen);
            vo.setName(name);
            vo.setReviewEnabled(isReviewEnabled);
            vo.setTaggingEnabled(isTaggingEnabled);
            vo.setSocialBookmarkEnabled(isSocialBookmarksEnabled);
            vo.setOwningOrganizationId(ownerId);
            vo = mpSvc.createMarketplace(vo);
            if (isEmpty(mIdProperty)) {
                getProject().setProperty("createdMarketplaceId",
                        vo.getMarketplaceId());
            } else {
                getProject().setProperty(mIdProperty, vo.getMarketplaceId());
            }
            log("Created marketplace with ID '" + vo.getMarketplaceId() + "'");
        } catch (SaaSApplicationException e) {
            if (failonerror) {
                throw e;
            } else {
                handleErrorOutput("Problem while creating marketplace '" + name
                        + "': " + e.getMessage());
            }
        }
    }
}
