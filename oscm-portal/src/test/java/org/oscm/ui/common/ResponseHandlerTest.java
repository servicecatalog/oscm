/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld //TODO                                                      
 *                                                                              
 *  Creation Date: 20.06.2012                                                      
 *                                                                              
 *  Completion Time: <date> //TODO                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertEquals;

import javax.faces.application.FacesMessage;

import org.junit.Test;

import org.oscm.internal.components.response.ReturnType;

/**
 * @author cheld
 * 
 */
public class ResponseHandlerTest {

    @Test
    public void mapToFaces() {
        assertEquals(FacesMessage.SEVERITY_INFO,
                ResponseHandler.mapToFaces(ReturnType.INFO));
        assertEquals(FacesMessage.SEVERITY_WARN,
                ResponseHandler.mapToFaces(ReturnType.WARNING));
    }

}
