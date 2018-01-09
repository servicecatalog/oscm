/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 3, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 3, 2011                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.bean;

import java.util.List;
import java.util.Properties;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * The bean implementation for the task queue service.
 * 
 * @author tokoda
 * 
 */
@Stateless
@Local(TaskQueueServiceLocal.class)
public class TaskQueueServiceBean implements TaskQueueServiceLocal {

    protected static Log4jLogger logger = LoggerFactory
            .getLogger(TaskQueueServiceBean.class);

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @Override
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendAllMessages(List<TaskMessage> messages) {
        validateMessages(messages);
        double msgSize = Math.ceil(messages.size()/1000.0);
        int counter = 0;
        while(counter < msgSize) {
            int fromIndex = counter * 1000;
            int toIndex = Math.min(fromIndex + 1000, messages.size());
            sendObjectMessage(messages.subList(fromIndex, toIndex));
            counter++;
        }
    }

    /**
     * Checks if the injected JMS resources are not <code>null</code>. If they
     * are, a system exception will be thrown.
     * @param queue
     * @param qFactory
     */
    private void validateJMSResources(Queue queue, ConnectionFactory qFactory) {
        if (queue == null || qFactory == null) {
            SaaSSystemException sse = new SaaSSystemException(
                    "JMS resources are not initialized!");
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_JMS_RESOURCE_NOT_INITIALIZED);

            throw sse;
        }
    }

    /**
     * Checks if the sent list of Messages are not <code>null</code>, and if the
     * payload of the Messages are not <code>null</code>.
     */
    private void validateMessages(List<TaskMessage> messages) {
        if (messages == null) {
            SaaSSystemException sse = new SaaSSystemException(
                    "The list of task messages are not initialized!");
            logger.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_LIST_OF_MESSAGES_NOT_INITIALIZED);

            throw sse;
        }

        for (TaskMessage message : messages) {
            if (message.getHandlerClass() == null) {
                SaaSSystemException sse = new SaaSSystemException(
                        "Handler class of the message is empty!");
                logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                        LogMessageIdentifier.ERROR_HANDLER_OF_MESSAGE_EMPTY);

                throw sse;
            }
            if (message.getPayload() == null) {
                SaaSSystemException sse = new SaaSSystemException(
                        "Payload of the message is empty!");
                logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                        LogMessageIdentifier.ERROR_PAYLOAD_OF_MESSAGE_EMPTY);

                throw sse;
            }
        }
    }

    /**
     * Sends the message objects to the JMS queue, each of them in a single
     * message.
     * 
     * @param messages
     *            The message objects to be sent.
     * @throws TaskHandlingException
     *             Thrown in case the send operation fails.
     */
    private void sendObjectMessage(List<TaskMessage> messages) {
        Session session = null;
        Connection conn = null;
        Queue queue;
        Context jndiContext = null;
        MessageProducer producer = null;
        int sentMsgCount = 0;
        try {
            if (messages.size() > 0) {
                jndiContext = new InitialContext();
                ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("openejb:Resource/JmsConnectionFactory");
                queue = (Queue) jndiContext.lookup("openejb:Resource/OSCMTaskQueue");

                validateJMSResources(queue, connectionFactory);

                conn = connectionFactory.createConnection();

                session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                producer = session.createProducer(queue);
                for (TaskMessage objectToSend : messages) {
                    PlatformUser user = dm.getCurrentUserIfPresent();
                    if (user != null) {
                        objectToSend.setCurrentUserKey(user.getKey());
                    }
                    ObjectMessage msg = session.createObjectMessage();
                    msg.setObject(objectToSend);
                    producer.send(msg);
                    sentMsgCount++;
                }

            }
        } catch (JMSException | NamingException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sse,
                    LogMessageIdentifier.ERROR_SEND_MESSAGE_TO_JMS_QUEUE_FAILED,
                    getMessagesStatues(messages, sentMsgCount));
            String status = getMessagesStatues(messages, sentMsgCount);
            logger.logError(
                    LogMessageIdentifier.ERROR_SEND_MESSAGE_TO_JMS_QUEUE_FAILED_DETAILS,
                    Integer.toString(sentMsgCount),
                    Integer.toString(messages.size()), status);
            throw sse;
        } finally {
            closeProducer(producer);
            closeSession(session);
            closeConnection(conn);
            closeJNDI(jndiContext);
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

    void closeProducer(MessageProducer producer) {
        if (producer != null) {
            try {
                producer.close();
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

    void closeJNDI(Context context) {
        if (context != null) {
            try {
                context.close();
            } catch (Exception e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_CLOSE_RESOURCE_FAILED);
            }
        }
    }

    private String getMessagesStatues(List<TaskMessage> messages, int sentCount) {
        String status = "";

        for (int i = 0; i < messages.size(); i++) {
            TaskMessage msg = messages.get(i);
            String sta = "Sent Message #" + i
                    + ((i < sentCount) ? " Successful " : " Failed ") + "::"
                    + getMessageInfo(msg, ", ");
            if (i < sentCount) {
                sta += ", " + getMessageStatus(msg);
            }
            status += sta + ";";
        }
        return status;
    }

    private String getMessageInfo(TaskMessage msg, String seperator) {
        String info = "";
        info += "Handler: " + msg.getHandlerClass().getCanonicalName()
                + seperator + "Payload: "
                + msg.getPayload().getClass().getCanonicalName();
        info += seperator + "Details: " + msg.getPayload().getInfo();

        return info;
    }

    private String getMessageStatus(TaskMessage msg) {
        return "Number Of Attempt: " + msg.getNumberOfAttempt();
    }
}
