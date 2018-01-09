/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 05.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ui.common;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * PhaseListener which verifies that the view locale is set to the user's locale
 * 
 */
public class LocaleListener implements PhaseListener {

    private static final long serialVersionUID = -3561934585292843214L;

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.event.PhaseListener#getPhaseId()
     */
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
     */
    public void beforePhase(PhaseEvent event) {
        JSFUtils.verifyViewLocale();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
     */
    public void afterPhase(PhaseEvent e) {
    }

}
