/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-4-4                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

/**
 * NoCachePhaseListener which adds no-cache parameters into response headers, to
 * avoid cache problem in UI.
 * 
 * @author Mao
 * 
 */
public class NoCachePhaseListener implements PhaseListener {

    private static final long serialVersionUID = -5433620841966054769L;

    @Override
    public void afterPhase(PhaseEvent phaseEvent) {

    }

    @Override
    public void beforePhase(PhaseEvent phaseEvent) {
        FacesContext facesContext = phaseEvent.getFacesContext();
        HttpServletResponse response = (HttpServletResponse) facesContext
                .getExternalContext().getResponse();
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        response.addHeader("Expires", "0");

    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }
}
