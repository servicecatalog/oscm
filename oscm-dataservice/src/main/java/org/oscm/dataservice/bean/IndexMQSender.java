/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.dataservice.bean;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.search.cfg.Environment;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.index.IndexRequestMessage;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Sends object messages to the local, indexer related JMS queue.
 */
public class IndexMQSender {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(IndexMQSender.class);

    private boolean jndiChecked = false;
    ConnectionFactory qFactory;
    Queue queue;
    private boolean notifyIndexer = false;

    public IndexMQSender() {
        String autoRegister = System
                .getProperty(Environment.AUTOREGISTER_LISTENERS);
        notifyIndexer = (autoRegister == null || Boolean
                .parseBoolean(autoRegister));
    }

    boolean checkJMSResources() {
        if (qFactory != null && queue != null) {
            return true;
        }
        if (jndiChecked) {
            // do not look up again
            return false;
        }
        jndiChecked = true;
        Context jndiContext = null;
        try {
            jndiContext = getContext();
            try {
                qFactory = (ConnectionFactory) jndiContext
                        .lookup("jms/bss/indexerQueueFactory");
                queue = (Queue) jndiContext.lookup("jms/bss/indexerQueue");
                return true;
            } catch (NamingException e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_JNDI_LOOKUP_FAILED);
            }
        } catch (NamingException e) {
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_JNDI_INITIAL_CONTEXT_CREATION_FAILED);
        }
        return false;
    }

    InitialContext getContext() throws NamingException {
        return new InitialContext();
    }

    public void sendMessage(Serializable requestMsg) throws JMSException {
        if (requestMsg != null && checkJMSResources()) {
            javax.jms.Session jmsSession = null;
            Connection conn = null;
            MessageProducer producer = null;
            try {
                conn = qFactory.createConnection();
                jmsSession = conn.createSession(false,
                        javax.jms.Session.AUTO_ACKNOWLEDGE);
                producer = jmsSession.createProducer(queue);
                ObjectMessage msg = jmsSession.createObjectMessage();
                msg.setObject(requestMsg);
                producer.send(msg);
            } finally {
                closeProducer(producer);
                closeSession(jmsSession);
                closeConnection(conn);
            }
        }
    }

    void closeSession(javax.jms.Session jmsSession) {
        if (jmsSession != null) {
            try {
                jmsSession.close();
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

    public void notifyIndexer(Object entity, ModificationType type) {
        System.out.println("IndexMQSender: " + this.toString());
        if (notifyIndexer && entity instanceof DomainObject<?>) {
            try {
                sendMessage(IndexRequestMessage.get(entity, type));
            } catch (Exception e) {
                throw new SaaSSystemException(
                        "Exception notifying indexer queue for "
                                + entity.getClass().getSimpleName(), e);
            }
        }
    }

    public boolean isNotifyIndexer() {
        return notifyIndexer;
    }

}
