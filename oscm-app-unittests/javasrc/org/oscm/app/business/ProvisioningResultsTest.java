/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business;

import org.junit.Test;
import static org.junit.Assert.*;

import org.oscm.provisioning.data.BaseResult;

/**
 * @author Tests provisioning result
 *
 */
public class ProvisioningResultsTest {
    
    @Test
    public void testgetSuccesfulResult(){
        
        //given
        ProvisioningResults provResult = new ProvisioningResults();
        Class<BaseResult> clazz = BaseResult.class;
        String successMsg = "successMsg";
        
        //when
        BaseResult result = provResult.getSuccesfulResult(clazz, successMsg);
        
        //then
        assertEquals(successMsg, result.getDesc());
        assertEquals(0, result.getRc());  
    }
}
