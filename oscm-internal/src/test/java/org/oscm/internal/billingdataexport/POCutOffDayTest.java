/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.billingdataexport;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class POCutOffDayTest {

    private POCutOffDay cutOffDay;

    @Test
    public void initValueTest() {
        cutOffDay = new POCutOffDay();
        assertEquals(1, cutOffDay.getCutOffDay());
    }

    @Test
    public void constructorTest() {
        cutOffDay = new POCutOffDay(1);
        assertEquals(1, cutOffDay.getCutOffDay());
    }

    @Test
    public void setCutOffDay_OK() {
        cutOffDay = new POCutOffDay();
        cutOffDay.setCutOffDay(28);
        assertEquals(28, cutOffDay.getCutOffDay());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCutOffDay_invalidValue() throws Exception {
        cutOffDay = new POCutOffDay();
        cutOffDay.setCutOffDay(34);
    }

}
