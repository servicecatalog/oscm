/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2017-07-07                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.kafka.result;

public enum PublishingResult {

    SUCCESS, ERROR;

    public boolean isError() {
        return this.equals(ERROR);
    }

    public boolean isSuccess() {
        return !isError();
    }
    
}