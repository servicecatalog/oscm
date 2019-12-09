/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 6 Dec 2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.text.Collator;
import java.util.Comparator;

import org.oscm.internal.vo.VOMarketplace;

/**
 * Sort marketplace names in locale-sensitive alphabetical order.
 * 
 * @author farmaki
 *
 */
public class MarketplacesComparator implements Comparator<VOMarketplace> {

    Collator collator = Collator.getInstance();

    @Override
    public int compare(VOMarketplace mp1, VOMarketplace mp2) {
        int rc;
        if ((mp1.getName() == null) && (mp2.getName() == null)) {
            rc = collator.compare(mp1.getMarketplaceId(),
                    mp2.getMarketplaceId());
        } else {
            rc = collator.compare(mp1.getName(), mp2.getName());
            if (rc == 0) {
                rc = collator.compare(mp1.getMarketplaceId(),
                        mp2.getMarketplaceId());
            }
        }

        return rc;
    }
}




