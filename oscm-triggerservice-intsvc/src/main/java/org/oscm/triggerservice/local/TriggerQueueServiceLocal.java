/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 16.06.2010                                                      
 *                                                                              
 *  Completion Time: 16.06.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.local;

import java.util.List;

import javax.ejb.Local;
import javax.jms.JMSException;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Service to provide functionality to post a trigger message to the queue.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Local
public interface TriggerQueueServiceLocal {

    /**
     * Sends operation related messages to the JMS queue, if required, and
     * informs whether the processing should continue or halted, as it has to
     * wait for external confirmation.
     * 
     * @param messageData
     *            The message data contains the type of triggers indicating the
     *            current operations.
     * 
     * @return The list of trigger process objects corresponding to the
     *         organization's trigger definitions. In case there is no
     *         suspending trigger definition for a trigger process, it will not
     *         be persisted and will not refer to any trigger definition at all.
     */
    public List<TriggerProcessMessageData> sendSuspendingMessages(
            List<TriggerMessage> messageData);

    /**
     * Sends operation related messages to the JMS queue. The message delivery
     * has no impact on the completion of the processing. It only serves
     * notification purposes. The messages are sent to the specified
     * organizations.
     * 
     * @param messages
     *            The messages to send.
     */
    public void sendAllNonSuspendingMessages(List<TriggerMessage> messages);

    /**
     * 
     * Sends operation related messages to the JMS queue. The message delivery
     * has no impact on the completion of the processing. It only serves
     * notification purposes. The messages are sent to the specified
     * organizations.
     * 
     * @param messages
     *            The messages to send.
     * @param currentUser
     *            The current logged in user or null in case of anonymous or in
     *            case of a timer execution.
     */
    public void sendAllNonSuspendingMessages(List<TriggerMessage> messages,
            PlatformUser currentUser);

    /**
     * For each {@link TriggerMessage}:
     * <ul>
     * <li>Checks if a trigger definition of the type in the
     * {@link TriggerMessage} exists for the receiver {@link Organization}</li>
     * <li>Create and persist the {@link TriggerProcess} and add its key to a
     * {@link List}</li>
     * <li>Persist the parameters if existing</li>
     * </ul>
     * After that the notification for all created trigger processes is sent in
     * one session.
     * 
     * @param messages
     *            the {@link TriggerMessage}s with their type, receivers and
     *            parameters.
     * @param the
     *            current {@link PlatformUser} or <code>null</code>.
     * @throws NonUniqueBusinessKeyException
     * @throws JMSException
     */
    public void sendMessagesIfRequired(List<TriggerMessage> messages,
            PlatformUser currentUser) throws NonUniqueBusinessKeyException,
            JMSException;
}
