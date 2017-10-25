/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.billingdataexport;

import org.oscm.internal.base.BasePO;

public class POCutOffDay extends BasePO {

    private static final long serialVersionUID = 3155612416606694236L;

    private int cutOffDay = 1;

    public POCutOffDay(int cutOffDay) {
        this.cutOffDay = cutOffDay;
    }

    public POCutOffDay() {

    }

    public void setCutOffDay(int cutOffDay) {
        validateValue(cutOffDay);
        this.cutOffDay = cutOffDay;
    }

    public int getCutOffDay() {
        return cutOffDay;
    }

    private void validateValue(int cutOffDay) {
        if (cutOffDay < 1 || cutOffDay > 28) {
            throw new IllegalArgumentException();
        }
    }
}
