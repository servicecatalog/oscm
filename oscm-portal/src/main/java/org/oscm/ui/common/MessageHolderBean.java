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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.faces.application.FacesMessage;

/**
 * session scoped bean for storing message over multiple requests. used for
 * workaround MessageHandler. becomes obsolete in JSF2.0 with view scope and get
 * support.
 * 
 * holds AdmMessages in session scope
 */
public class MessageHolderBean implements Serializable {

    private static final long serialVersionUID = 1466172704187521031L;

    Set<MessageWithClientId> messages = new HashSet<MessageWithClientId>();

    public Set<MessageWithClientId> getMessages() {
        return messages;
    }

    public void addAll(Collection<MessageWithClientId> messages) {
        this.messages.addAll(messages);
    }

    public void resetMessages() {
        messages.clear();
    }

    static class MessageWithClientId {

        private FacesMessage message;
        private String clientId;

        MessageWithClientId(String clientId, FacesMessage message) {
            this.clientId = clientId;
            this.message = message;
        }

        public FacesMessage getMessage() {
            return message;
        }

        public String getClientId() {
            return clientId;
        }

        @Override
        public int hashCode() {
            int hash = 37;
            if (clientId != null) {
                hash *= clientId.hashCode();
            }
            if (message != null) {
                hash *= message.hashCode();
            }
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MessageWithClientId) {
                MessageWithClientId other = (MessageWithClientId) obj;
                if (this.clientId == null) {
                    return other.clientId == null;
                } else if (!this.clientId.equals(other.clientId)) {
                    return false;
                }
                // now as the client ids are equal, compare the messages
                if (this.message == null) {
                    return other.message == null;
                } else {
                    return this.message.equals(other.message);
                }
            }
            return false;
        }

    }
}
