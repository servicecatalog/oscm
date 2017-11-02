/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.common;

import java.math.BigDecimal;

/**
 * 
 * Helper class to convert average rating decimal value into star presentation
 * class
 * 
 * @author ryumshyn
 * 
 */
public final class RatingCssMapper {

    public static String getRatingClass(BigDecimal avgRating) {

        if (avgRating == null || avgRating.equals(BigDecimal.ZERO)) {
            return "0_0";
        }

        double f = avgRating.doubleValue();

        if (avgRating.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() != avgRating
                .doubleValue()) {
            f = (Math.round(avgRating.doubleValue() * 2 + 0.5f)) / 2.0f;
        }

        if (f > 5) {
            f = 5.0f;
        }

        String v_str = f + "";

        if (v_str.length() > 0) {
            return v_str.replace('.', '_');
        }
        return "0_0";
    }
}
