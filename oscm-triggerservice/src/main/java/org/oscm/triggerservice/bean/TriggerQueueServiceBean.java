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

package org.oscm.triggerservice.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * The bean implementation for the trigger queue service.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Stateless
@Local(TriggerQueueServiceLocal.class)
public class TriggerQueueServiceBean implements TriggerQueueServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(TriggerQueueServiceBean.class);

    @Resource(name = "JmsConnectionFactory")
    protected ConnectionFactory qFactory;

    @Resource(name = "OSCMTriggerQueue")
    protected Queue queue;

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @Resource
    protected SessionContext sessionCtx;

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<TriggerProcessMessageData> sendSuspendingMessages(
            List<TriggerMessage> messageData) {

        validateJMSResources();

        List<TriggerProcessMessageData> result = new ArrayList<TriggerProcessMessageData>();
        List<Long> messages = new ArrayList<Long>();

        PlatformUser currentUser = dm.getCurrentUser();
        Organization org = currentUser.getOrganization();
        for (TriggerMessage data : messageData) {
            // find trigger definition for organization and type
            TriggerDefinition triggerDefinition = org
                    .getSuspendingTriggerDefinition(data.getTriggerType());

            // create a trigger process object
            TriggerProcess tp = new TriggerProcess();
            tp.setActivationDate(System.currentTimeMillis());
            tp.setState(TriggerProcessStatus.INITIAL);
            tp.setTriggerDefinition(triggerDefinition);
            tp.setUser(currentUser);

            TriggerProcessMessageData processData = new TriggerProcessMessageData(
                    tp, data);
            result.add(processData);

            if (triggerDefinition != null) {
                try {
                    dm.persist(tp);
                    dm.flush();
                    messages.add(tp.getKey());
                } catch (NonUniqueBusinessKeyException e) {
                    SaaSSystemException sse = new SaaSSystemException(e);
                    logger.logError(
                            Log4jLogger.SYSTEM_LOG,
                            sse,
                            LogMessageIdentifier.ERROR_PERSIST_TRIGGER_PROCESS_FAILED);
                    throw sse;
                }
            }
        }

        // send Messages
        try {
            sendObjectMessage(messages);
        } catch (JMSException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_SEND_MESSAGE_TO_JMS_QUEUE_FAILED);
            throw sse;
        }

        return result;
    }

    /**
     * Checks if the injected JMS resources are not <code>null</code>. If they
     * are, a system exception will be thrown.
     */
    private void validateJMSResources() {
        if (queue == null || qFactory == null) {
            SaaSSystemException sse = new SaaSSystemException(
                    "JMS resources are not initialized!");
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_JMS_RESOURCE_NOT_INITIALIZED);

            throw sse;
        }
    }

    /**
     * Sends the specified objects as message objects to the JMS queue, each of
     * them in a single message.
     * 
     * @param objectsToSend
     *            The message objects to be sent.
     * @throws JMSException
     *             Thrown in case the send operation fails.
     */
    private void sendObjectMessage(List<? extends Serializable> objectsToSend)
            throws JMSException {
        double msgSize = Math.ceil(objectsToSend.size()/1000.0);
        int counter = 0;
        while(counter < msgSize) {
            int fromIndex = counter * 1000;
            int toIndex = Math.min(fromIndex + 1000, objectsToSend.size());
            sendObjectMsgSingleSession(objectsToSend.subList(fromIndex, toIndex));
            counter++;
        }
    }

    private void sendObjectMsgSingleSession(List<? extends Serializable> objectsToSend) throws JMSException {
        Session session = null;
        Connection conn = null;
        try {
            conn = qFactory.createConnection();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(queue);
            for (Serializable objectToSend : objectsToSend) {
                ObjectMessage msg = session.createObjectMessage();
                msg.setObject(objectToSend);
                producer.send(msg);
            }
        } finally {
            closeSession(session);
            closeConnection(conn);
        }
    }

    void closeSession(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_CLOSE_RESOURCE_FAILED);
            }
        }
    }

    void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_CLOSE_RESOURCE_FAILED);
            }
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void sendAllNonSuspendingMessages(List<TriggerMessage> messages) {
        PlatformUser currentUser = dm.getCurrentUser();
        sendAllNonSuspendingMessages(messages, currentUser);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void sendAllNonSuspendingMessages(List<TriggerMessage> messages,
            PlatformUser currentUser) {

        validateJMSResources();
        // for all non-suspending triggers, create a trigger process in state
        // initial and send a process request to the JMS queue
        try {
            prepareForNewTransaction().sendMessagesIfRequired(messages,
                    currentUser);
        } catch (NonUniqueBusinessKeyException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_PERSIST_TRIGGER_PROCESS_FAILED);
            throw sse;
        } catch (JMSException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_SEND_MESSAGE_TO_JMS_QUEUE_FAILED);
            throw sse;
        }

    }

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
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendMessagesIfRequired(List<TriggerMessage> messages,
            PlatformUser currentUser) throws NonUniqueBusinessKeyException,
            JMSException {
        List<Long> tpKeys = new ArrayList<Long>();
        for (TriggerMessage message : messages) {

            if (message.getParams() != null) {
                TriggerProcess tpCancelled = message.getParams().get(0)
                        .getTriggerProcess();
                if (tpCancelled != null) {
                    if (tpCancelled.getStatus() == TriggerProcessStatus.CANCELLED) {
                        tpKeys.add(tpCancelled.getKey());
                    }
                }
            }

            List<Organization> orgs = message.getReceiverOrgs();
            for (Organization org : orgs) {
                List<TriggerDefinition> definitions = org
                        .getTriggerDefinitions();
                TriggerDefinition def = getTriggerDefinition(
                        message.getTriggerType(), definitions);
                if (def == null) {
                    continue;
                }
                TriggerProcess tp = new TriggerProcess();
                tp.setActivationDate(System.currentTimeMillis());
                tp.setState(TriggerProcessStatus.INITIAL);
                tp.setUser(currentUser);
                tp.setTriggerDefinition(def);

                dm.persist(tp);
                dm.flush();
                tpKeys.add(tp.getKey());

                if (message.getParams() != null) {
                    for (TriggerProcessParameter param : message.getParams()) {
                        tp.addTriggerProcessParameter(param.getName(),
                                param.getValue(Object.class));
                    }
                }
            }
        }
        sendObjectMessage(tpKeys);
    }

    protected TriggerQueueServiceLocal prepareForNewTransaction() {
        DateFactory.getInstance().takeCurrentTime();
        return sessionCtx.getBusinessObject(TriggerQueueServiceLocal.class);
    }

    private static TriggerDefinition getTriggerDefinition(
            TriggerType triggerType, List<TriggerDefinition> definitions) {
        for (TriggerDefinition td : definitions) {
            if (td.getType() == triggerType && !td.isSuspendProcess()) {
                return td;
            }
        }
        return null;
    }
}
