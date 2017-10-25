/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 27.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the usage details (duration and corresponding factor) for one
 * object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class UsageDetails {

    /**
     * The usage periods for the related object
     */
    private List<UsagePeriod> usagePeriods = new ArrayList<UsagePeriod>();

    /**
     * The factor to be used for billing, based on the duration and the
     * corresponding period setting.
     */
    private double factor = 0.0D;

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public void addUsagePeriod(long startTime, long endTime) {
        usagePeriods.add(new UsagePeriod(startTime, endTime));
    }

    public void addUsagePeriods(List<UsagePeriod> newUsagePeriods) {
        usagePeriods.addAll(newUsagePeriods);
    }

    public List<UsagePeriod> getUsagePeriods() {
        return usagePeriods;
    }

    public static class UsagePeriod {
        private long startTime;
        private long endTime;

        public UsagePeriod(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UsagePeriod) {
                UsagePeriod period = (UsagePeriod) obj;
                return (startTime == period.startTime && endTime == period.endTime);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hc = 13;
            int multiplier = 37;
            hc = hc * multiplier + Long.valueOf(startTime).hashCode();
            hc = hc * multiplier + Long.valueOf(endTime).hashCode();
            return hc;
        }
    }

}
