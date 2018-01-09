/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.TIMESTAMP;

import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.types.enumtypes.TimerType;

@SuppressWarnings("boxing")
public class TimerProcessingIT extends DomainObjectTestBase {

    @Test
    public void testCreation() throws Exception {
        final TimerProcessing timer = runTX(new Callable<TimerProcessing>() {

            public TimerProcessing call() throws Exception {
                TimerProcessing tp = new TimerProcessing();
                TimerProcessingData data = new TimerProcessingData();
                data.setDuration(50);
                data.setNodeName("nodeName");
                data.setStartTime(TIMESTAMP);
                data.setStartTimeMutex(5000);
                data.setSuccess(false);
                data.setTimerType(TimerType.ORGANIZATION_UNCONFIRMED);
                tp.setDataContainer(data);
                mgr.persist(tp);
                return tp;
            }
        });
        runTX(new Callable<Void>() {

            public Void call() throws Exception {
                TimerProcessing tp = mgr.getReference(TimerProcessing.class,
                        timer.getKey());
                TimerProcessingData data = tp.getDataContainer();
                Assert.assertEquals(timer.getDuration(), data.getDuration());
                Assert.assertEquals(timer.getNodeName(), data.getNodeName());
                Assert.assertEquals(timer.getStartTime(), data.getStartTime());
                Assert.assertEquals(timer.getStartTimeMutex(), data
                        .getStartTimeMutex());
                Assert.assertEquals(timer.getTimerType(), data.getTimerType());
                Assert.assertEquals(timer.isSuccess(), data.isSuccess());
                return null;
            }
        });
    }

}
