/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: feistle                                                      
 *                                                                              
 *  Creation Date: 29.09.2011                                           
 *                                                                              
 *  Completion Time: 29.09.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.MessageHolderBean.MessageWithClientId;

/**
 * this class is a workaround for displaying messages (success and error) after
 * redirect. redirect is required to handle F5 (browser refresh).
 * 
 * this becomes obsolete as soon as jsf2 and view scope is available.
 * 
 * messages are saved in phases APPLY_REQUEST_VALUES, PROCESS_VALIDATIONS,
 * INVOKE_APPLICATION and restored and written back to FacesMessages.
 * 
 * This listener must be registered BEFORE MessageListener.
 * 
 */
public class MessageHandler implements PhaseListener {

    private static final long serialVersionUID = -8436727742151079155L;

    UiDelegate ui = new UiDelegate();

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    /**
     * in RENDER_RESPONSE restore facesMessages
     */
    public void beforePhase(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            FacesContext facesContext = event.getFacesContext();
            restoreMessages(facesContext);
        }
    }

    /**
     * save messages after APPLY_REQUEST_VALUES, PROCESS_VALIDATIONS,
     * INVOKE_APPLICATION
     */
    public void afterPhase(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES
                || event.getPhaseId() == PhaseId.PROCESS_VALIDATIONS
                || event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
            FacesContext facesContext = event.getFacesContext();
            saveMessages(facesContext);
        }
    }

    /**
     * Remove the messages that are not associated with any particular component
     * from the faces context and store them in the bean.
     * 
     * @return the number of removed messages.
     */
    private void saveMessages(FacesContext facesContext) {

        List<MessageWithClientId> messages = getAndResetFacesMessages(facesContext);
        if (messages.size() > 0) {
            writeToSessionBean(messages);
        }
    }

    /**
     * get messages from facesContext and empty facesMessages.
     * 
     * @param facesContext
     * @return Set<FacesMessage>
     */
    private List<MessageWithClientId> getAndResetFacesMessages(
            FacesContext facesContext) {
        List<MessageWithClientId> md = new ArrayList<MessageWithClientId>();

        md.addAll(getMessagesForClientId(null, facesContext));
        // also include the progress messages
        md.addAll(getMessagesForClientId(BaseBean.PROGRESS_PANEL, facesContext));
        return md;
    }

    /**
     * Gets the messages with the given client identifier from the faces context
     * .
     * 
     * @param clientId
     *            The client identifier to filter for.
     * @param facesContext
     *            The faces context.
     * @return The messsage with the given id.
     */
    private List<MessageWithClientId> getMessagesForClientId(String clientId,
            FacesContext facesContext) {
        List<MessageWithClientId> md = new ArrayList<MessageWithClientId>();
        for (Iterator<FacesMessage> it = facesContext.getMessages(clientId); it
                .hasNext();) {
            FacesMessage msg = it.next();
            md.add(new MessageWithClientId(clientId, msg));
            it.remove();
        }
        return md;
    }

    private void writeToSessionBean(List<MessageWithClientId> messages) {
        ui.findMessageHolderBean().addAll(messages);
    }

    /**
     * Substitute the faces messages that are not associated with a particular
     * component by the saved messages from the MESSAGEHOLDERBEAN.
     * 
     * @return the number of removed messages.
     */
    private void restoreMessages(FacesContext facesContext) {

        MessageHolderBean bean = ui.findMessageHolderBean();
        Set<MessageWithClientId> messages = bean.getMessages();

        // remove global messages in facesContext
        Set<FacesMessage> facesContextMessages = new HashSet<FacesMessage>();
        for (Iterator<FacesMessage> i = facesContext.getMessages(null); i
                .hasNext();) {
            FacesMessage msg = i.next();
            facesContextMessages.add(msg);
            i.remove();
        }

        // add saved messages if not yet existing
        if (messages != null && messages.size() > 0) {
            for (MessageWithClientId message : messages) {
                if (!facesContextMessages.contains(message.getMessage()))
                    facesContext.addMessage(message.getClientId(),
                            message.getMessage());
            }
            bean.resetMessages();
        }
    }

}
