/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 30.08.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author falkenhahn
 * 
 */
public class BigDecimalComparator {

    /**
     * Compares and returns the result. If one of them or both are null, false
     * is returned.
     * 
     * @param expected
     *            the value to which will be scaled
     * @param comparator
     *            the value to be compared to
     * @return true if the values are equal to the same scale
     */
    public static boolean isEqual(BigDecimal expected, BigDecimal comparator) {
        if (expected == null || comparator == null) {
            return false;
        }
        return expected.equals(comparator.setScale(expected.scale(),
                RoundingMode.HALF_UP));
    }

    /**
     * Compares with the same scale as value to BigDecimal.ZERO
     * 
     * @param value
     *            test for zero
     * @return true only if value is not null and zero
     */
    public static boolean isZero(BigDecimal value) {
        return isEqual(value, BigDecimal.ZERO);
    }

}
