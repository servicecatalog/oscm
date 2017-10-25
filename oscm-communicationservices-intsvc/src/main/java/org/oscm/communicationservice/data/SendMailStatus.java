/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 28.10.2011
 *                                                                              
 *******************************************************************************/

package org.oscm.communicationservice.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds a list of sended mail status containing an exception.
 * 
 * @author Enes Sejfi
 * 
 * @param <E>
 *            Any kind of java type
 */
public class SendMailStatus<E> {
    private List<SendMailStatusItem<E>> items = new LinkedList<SendMailStatusItem<E>>();

    public void addMailStatus(E type) {
        items.add(new SendMailStatusItem<E>(type));
    }

    /**
     * Add a new mail status to the mail status list.
     * 
     * @param type
     *            Java class type
     * @param exception
     *            Occurred exception
     */
    public void addMailStatus(E type, Exception exception) {
        items.add(new SendMailStatusItem<E>(type, exception));
    }

    /**
     * Returns a list of the send mail status
     * 
     * @return list of send mail status
     */
    public List<SendMailStatusItem<E>> getMailStatus() {
        return items;
    }

    /**
     * Single mail status item containing the instance and a possibly occurred
     * exception.
     * 
     * @param <E>
     *            Any kind of java type
     */
    public static class SendMailStatusItem<E> {
        private E instance;
        private Exception exception;

        public SendMailStatusItem(E instance) {
            setInstance(instance);
        }

        public SendMailStatusItem(E type, Exception exception) {
            this(type);
            setException(exception);
        }

        public E getInstance() {
            return instance;
        }

        public void setInstance(E instance) {
            this.instance = instance;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public boolean errorOccurred() {
            return exception != null;
        }
    }
}
