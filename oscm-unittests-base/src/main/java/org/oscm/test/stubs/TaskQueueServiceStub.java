/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.List;

import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;

public class TaskQueueServiceStub implements TaskQueueServiceLocal {

    @Override
    public void sendAllMessages(List<TaskMessage> messages) {
        throw new UnsupportedOperationException();
    }

}
