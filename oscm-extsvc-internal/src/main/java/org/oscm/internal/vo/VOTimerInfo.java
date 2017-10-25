/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                  
 *                                                                              
 *  Creation Date: 31.01.2011                                                      
 *                                                                              
 *  Completion Time: <date>                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author weiser
 * 
 */
public class VOTimerInfo implements Serializable {

    private static final long serialVersionUID = 7195383417720133305L;

    private Date expirationDate;
    private String timerType;

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getTimerType() {
        return timerType;
    }

    public void setTimerType(String timerType) {
        this.timerType = timerType;
    }
}
