/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 31.07.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.adapter.bean;

import java.util.Properties;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.oscm.billing.external.pricemodel.service.PriceModel;

/**
 * Pushes subscription price models to OSCM
 */
@LocalBean
@Stateless
public class SubscriptionAgent {

    private static final String JMS_QUEUE_FACTORY_JNDI_NAME = "openejb:Resource/JmsConnectionFactory";
    private static final String JMS_QUEUE_JNDI_NAME = "openejb:Resource/OSCMTaskQueue";

    /**
     * Create the initial JNDI context
     */
    private InitialContext createJndiContext() {
        Properties jndiProperties = new Properties();
        InitialContext context = null;
        try {
            context = new InitialContext(jndiProperties);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return context;
    }

    /**
     * Get the OSCM task queue factory
     * 
     * @param context
     *            a JNDI context
     * @return the task queue factory
     */
    private ConnectionFactory getJmsConnectionFactory(InitialContext context) {
        try {
            Object lookup = context.lookup(JMS_QUEUE_FACTORY_JNDI_NAME);
            return ConnectionFactory.class.cast(lookup);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the OSCM task queue
     * 
     * @param context
     *            a JNDI context
     * @return the task queue
     */
    private Queue getJmsQueue(InitialContext context) {
        try {
            Object lookup = context.lookup(JMS_QUEUE_JNDI_NAME);
            return Queue.class.cast(lookup);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean pushPriceModel(PriceModel pm) {

        InitialContext context = createJndiContext();
        if (context == null) {
            System.out.println("Couldn't create JNDI context");
            return false;
        }

        ConnectionFactory qFactory = getJmsConnectionFactory(context);
        Queue queue = getJmsQueue(context);
        if (qFactory == null || queue == null) {
            System.out.println("Missing JMS Queue or Queue Factory");
            return false;
        }

        Session session = null;
        Connection conn = null;

        boolean success = false;
        try {
            conn = qFactory.createConnection();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(queue);
            ObjectMessage msg = session.createObjectMessage();
            msg.setObject(pm);
            producer.send(msg);
            success = true;
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception ignored) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }

        return success;
    }

}
