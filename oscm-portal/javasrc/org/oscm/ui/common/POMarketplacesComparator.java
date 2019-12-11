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

import org.oscm.internal.components.POMarketplace;

/**
 * Sort marketplace names in locale-sensitive alphabetical order.
 * 
 * @author farmaki
 *
 */
public class POMarketplacesComparator implements Comparator<POMarketplace> {

    Collator collator = Collator.getInstance();

    @Override
    public int compare(POMarketplace mp1, POMarketplace mp2) {
        int rc;
        if (mp1.getDisplayName() == null && mp2.getDisplayName() == null) {
            rc = collator.compare(mp1.getMarketplaceId(),
                    mp2.getMarketplaceId());
        } else {
            rc = collator.compare(mp1.getDisplayName(), mp2.getDisplayName());
        }
        return rc;
    }
}




