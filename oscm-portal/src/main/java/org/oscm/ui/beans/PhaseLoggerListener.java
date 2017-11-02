/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;

/**
 * JSF listener for logging JSF lifecycle
 */
public class PhaseLoggerListener implements PhaseListener {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PhaseLoggerListener.class);
    private static final long serialVersionUID = -2711750237038802009L;

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    public void beforePhase(PhaseEvent event) {
        if (logger.isDebugLoggingEnabled()) {
            List<FacesMessage> msgs = event.getFacesContext().getMessageList();
            UIViewRoot viewRoot = event.getFacesContext().getViewRoot();
            if (viewRoot != null) {
                logger.logDebug("------------------\nSTART PHASE: " + viewRoot.getViewId());
                for (FacesMessage msg : msgs) {
                    logger.logDebug("Message " + msg.getSummary() + " :: " + msg.getDetail());
                }
            }
        }
    }

    public void afterPhase(PhaseEvent event) {
        if (logger.isDebugLoggingEnabled()) {
            UIViewRoot viewRoot = event.getFacesContext().getViewRoot();
            if (viewRoot != null) {
                logger.logDebug("END PHASE " + event.getPhaseId() + " for view " + viewRoot.getViewId() + "\n------------------");
            }
        }
    }

}
