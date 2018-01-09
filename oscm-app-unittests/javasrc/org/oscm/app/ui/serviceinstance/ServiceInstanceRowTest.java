/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-3-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ui.serviceinstance;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.faces.event.ValueChangeEvent;

import org.junit.Before;
import org.junit.Test;

import org.oscm.app.ui.serviceinstance.ServiceInstanceRow;

/**
 * Unit test for Service Instance Row
 * 
 * @author Mao
 * 
 */
public class ServiceInstanceRowTest {

    private ServiceInstanceRow serviceInstanceRow;

    private ValueChangeEvent event;

    @Before
    public void setup() throws Exception {

        serviceInstanceRow = new ServiceInstanceRow(null, "");
        event = mock(ValueChangeEvent.class);

    }

    @Test
    public void operationChanged_NoOperaionSelected() throws Exception {
        // given
        ValueChangeEvent vcmock = mock(ValueChangeEvent.class);
        when(vcmock.getNewValue()).thenReturn("");

        // when
        serviceInstanceRow.operationChanged(vcmock);

        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(serviceInstanceRow.isButtonDisabled()));
    }

    @Test
    public void operationChanged_OperaionSelected() throws Exception {
        // given
        doReturn("DELETE").when(event).getNewValue();

        // when
        serviceInstanceRow.operationChanged(event);

        // then
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(serviceInstanceRow.isButtonDisabled()));

    }

}
