/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * ActionListener which catches SaaSApplicationException thrown from action
 * methods of the backing beans and adds the corresponding error message to the
 * faces context.
 * 
 */
public class ExceptionActionListener implements ActionListener {

    private final ActionListener delegate;
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ExceptionActionListener.class);

    public ExceptionActionListener(ActionListener delegate) {
        this.delegate = delegate;
    }

    public void processAction(ActionEvent event)
            throws AbortProcessingException {
        try {
            if (delegate != null) {
                delegate.processAction(event);
            }
        } catch (FacesException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR);
            boolean handled = ExceptionHandler.execute(e);
            if (!handled) {
                JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                        Constants.BUNDLE_ERR_KEY, null);
            }
        }
    }

}
