/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.Comparator;

import org.oscm.internal.vo.VOSteppedPrice;

public class SteppedPriceComparator implements Comparator<VOSteppedPrice> {

    public int compare(VOSteppedPrice arg0, VOSteppedPrice arg1) {
        Long limit0 = arg0.getLimit();
        Long limit1 = arg1.getLimit();

        if (limit0 == null && limit1 == null) {
            return 0;
        }
        if (limit0 == null) {
            return 1;
        }
        if (limit1 == null) {
            return -1;
        }
        return limit0.compareTo(limit1);
    }

}
