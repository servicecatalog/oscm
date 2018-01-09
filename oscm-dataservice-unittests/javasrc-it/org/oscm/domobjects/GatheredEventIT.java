/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.TIMESTAMP;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.test.data.SupportedCurrencies;
import org.oscm.internal.types.enumtypes.EventType;

public class GatheredEventIT extends DomainObjectTestBase {

    @Test
    public void testCreation() throws Exception {
        final GatheredEvent event = runTX(new Callable<GatheredEvent>() {

            public GatheredEvent call() throws Exception {
                BillingResult br = new BillingResult();
                br.setCreationTime(TIMESTAMP);
                br.setOrganizationTKey(1234L);
                br.setPeriodEndTime(TIMESTAMP);
                br.setPeriodStartTime(TIMESTAMP);
                br.setResultXML("resultXML");
                br.setNetAmount(BigDecimal.ZERO);
                br.setGrossAmount(BigDecimal.ZERO);
                br.setCurrency(SupportedCurrencies.findOrCreate(mgr, "EUR"));
                mgr.persist(br);
                GatheredEvent ge = new GatheredEvent();
                GatheredEventData data = new GatheredEventData();
                data.setActor("actor");
                data.setEventIdentifier("eventId");
                data.setMultiplier(12L);
                data.setOccurrenceTime(TIMESTAMP);
                data.setSubscriptionTKey(1234L);
                data.setType(EventType.SERVICE_EVENT);
                ge.setDataContainer(data);
                ge.setBillingResult(br);
                mgr.persist(ge);
                return ge;
            }
        });
        runTX(new Callable<Void>() {

            public Void call() throws Exception {
                GatheredEvent ge = mgr.getReference(GatheredEvent.class,
                        event.getKey());
                Assert.assertEquals(event.getBillingResult().getKey(), ge
                        .getBillingResult().getKey());
                GatheredEventData data = ge.getDataContainer();
                Assert.assertEquals(event.getActor(), data.getActor());
                Assert.assertEquals(event.getEventId(),
                        data.getEventIdentifier());
                Assert.assertEquals(event.getOccurrenceTime(),
                        data.getOccurrenceTime());
                Assert.assertEquals(event.getSubscriptionTKey(),
                        data.getSubscriptionTKey());
                Assert.assertEquals(event.getMultiplier(), data.getMultiplier());
                Assert.assertEquals(event.getType(), data.getType());
                return null;
            }
        });
    }
}
