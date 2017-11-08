/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 17.06.2010                                                      
 *                                                                              
 *  Completion Time: 17.06.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

public class TriggerQueueServiceStub implements TriggerQueueServiceLocal {

    @Override
    public void sendAllNonSuspendingMessages(List<TriggerMessage> messages) {

    }

    @Override
    public List<TriggerProcessMessageData> sendSuspendingMessages(
            List<TriggerMessage> messageData) {

        List<TriggerProcessMessageData> result = new ArrayList<TriggerProcessMessageData>();
        for (TriggerMessage m : messageData) {
            TriggerProcessMessageData data = new TriggerProcessMessageData(
                    new TriggerProcess(), m);
            result.add(data);
        }

        return result;
    }

    @Override
    public void sendAllNonSuspendingMessages(List<TriggerMessage> messages,
            PlatformUser currentUser) {

    }

    @Override
    public void sendMessagesIfRequired(List<TriggerMessage> messages,
            PlatformUser currentUser) throws NonUniqueBusinessKeyException,
            JMSException {
    }

}
