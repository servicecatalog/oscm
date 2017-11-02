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

package org.oscm.ui.beans.operator;

import java.io.Serializable;
import java.util.List;

import org.oscm.ui.common.ExceptionHandler;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTimerInfo;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Bean for displaying and reinitializing timers.
 * 
 * @author weiser
 * 
 */
@ManagedBean
@ViewScoped
public class TimerBean extends BaseOperatorBean implements Serializable {

    private static final long serialVersionUID = 7320053068914015996L;

    private List<VOTimerInfo> expirationInfo;

    public String reInitTimers() throws OrganizationAuthoritiesException {

        try {
            expirationInfo = getOperatorService().reInitTimers();
        } catch (ValidationException e) {
            ExceptionHandler.execute(e, true);
            return OUTCOME_ERROR;
        }

        return getOutcome(true);
    }

    public List<VOTimerInfo> getExpirationInfo() {
        if (expirationInfo == null) {
            try {
                expirationInfo = getOperatorService()
                        .getTimerExpirationInformation();
            } catch (OrganizationAuthoritiesException e) {
                ExceptionHandler.execute(e);
            }
        }
        return expirationInfo;
    }
}
