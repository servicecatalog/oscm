/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                          
 *                                                                              
 *  Creation Date: 28.10.2011                                                      
 *                                                                              
 *  Completion Time: 28.10.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.TriggerType;

/**
 * @author kulle
 * 
 */
public class TriggerMessageTest {

    @Test
    public void testEqualityNegative() {
        TriggerMessage data1 = new TriggerMessage(TriggerType.ACTIVATE_SERVICE);
        TriggerMessage data2 = new TriggerMessage(
                TriggerType.DEACTIVATE_SERVICE);

        assertFalse(data1.equals(data2));
    }

    @Test
    public void testHashCodeDifferentObj() {
        TriggerMessage data1 = new TriggerMessage(TriggerType.ACTIVATE_SERVICE);
        TriggerMessage data2 = new TriggerMessage(
                TriggerType.DEACTIVATE_SERVICE);

        assertFalse(data1.hashCode() == data2.hashCode());
    }

    @Test
    public void testHashCode_SameObject() {
        TriggerMessage data1 = new TriggerMessage(TriggerType.ACTIVATE_SERVICE);
        TriggerMessage data2 = data1;

        assertSame("the two objects should be the same", data1, data2);
        assertEquals(data1.hashCode(), data2.hashCode());
    }

}
