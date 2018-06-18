/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 06.06.2018                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.openstack.usage;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.time.Instant.ofEpochMilli;
import static java.time.LocalDateTime.parse;
import static java.time.ZoneId.of;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.ZonedDateTime.ofInstant;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.math.BigDecimal;
import java.net.MalformedURLException;

import org.openstack4j.model.compute.SimpleTenantUsage;
import org.oscm.app.openstack.OpenstackClient;
import org.oscm.app.openstack.controller.PropertyHandler;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.intf.EventService;
import org.oscm.types.exceptions.DuplicateEventException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOGatheredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class UsageConverter {

    public static final String EVENT_DISK = "EVENT_DISK_GIGABYTE_HOURS";
    public static final String EVENT_CPU = "EVENT_CPU_HOURS";
    public static final String EVENT_RAM = "EVENT_RAM_MEGABYTE_HOURS";
    public static final String EVENT_TOTAL = "EVENT_TOTAL_HOURS";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(UsageConverter.class);

    private static final String ZONEID_UTC = "UTC";

    PropertyHandler ph;
    OpenstackClient osClient;
    AppDb appDb;

    public UsageConverter() {

    }

    public UsageConverter(PropertyHandler ph) throws MalformedURLException {
        this.ph = ph;
        osClient = new OpenstackClient(ph);
        appDb = new AppDb();
    }

    public void registerUsageEvents() throws Exception {
        String startTime = getStartTime();
        String endTime = now(of(ZONEID_UTC)).format(ISO_LOCAL_DATE_TIME);
        SimpleTenantUsage usage = osClient.getUsage(startTime, endTime);

        if (usage.getTotalHours() != null) {
            long totalHours = new BigDecimal(usage.getTotalHours()).longValue();
            submit(EVENT_TOTAL, totalHours, endTime);
        }

        if (usage.getTotalMemoryMbUsage() != null) {
            long totalMemory = usage.getTotalMemoryMbUsage().longValue();
            submit(EVENT_RAM, totalMemory, endTime);
        }

        if (usage.getTotalVcpusUsage() != null) {
            long totalCpu = usage.getTotalVcpusUsage().longValue();
            submit(EVENT_CPU, totalCpu, endTime);
        }

        if (usage.getTotalLocalGbUsage() != null) {
            long totalGb = usage.getTotalLocalGbUsage().longValue();
            submit(EVENT_DISK, totalGb, endTime);
        }

        appDb.updateLastUsageFetch(ph.getInstanceId(), endTime);
    }

    String getStartTime() throws Exception {
        String lastUsageFetch = ph.getLastUsageFetch();
        if (!isNullOrEmpty(lastUsageFetch)) {
            return lastUsageFetch;
        }

        long requestTime = appDb.loadRequestTime(ph.getInstanceId());
        return ofInstant(ofEpochMilli(requestTime), of(ZONEID_UTC))
                .format(ISO_LOCAL_DATE_TIME);
    }

    void submit(String eventId, long multiplier, String occurence)
            throws ConfigurationException, MalformedURLException,
            ObjectNotFoundException, OrganizationAuthoritiesException,
            ValidationException {

        if (multiplier <= 0) {
            return;
        }

        VOGatheredEvent event = new VOGatheredEvent();
        event.setActor(ph.getTPAuthentication().getUserName());
        event.setEventId(eventId);
        event.setMultiplier(multiplier);
        event.setOccurrenceTime(parse(occurence, ISO_LOCAL_DATE_TIME)
                .toInstant(UTC).toEpochMilli());
        event.setUniqueId(eventId + "_" + occurence);

        try {
            EventService svc = ph.getWebService(EventService.class);
            svc.recordEventForInstance(ph.getTechnicalServiceId(),
                    ph.getInstanceId(), event);
        } catch (DuplicateEventException e) {
            LOGGER.debug("Event already inserted");
        }
    }

}
