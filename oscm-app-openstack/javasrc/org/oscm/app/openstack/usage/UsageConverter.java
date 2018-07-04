/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 06.06.2018                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.openstack.usage;

import static java.time.LocalDateTime.parse;
import static java.time.ZoneOffset.UTC;
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
 * Handle usage data and register events.
 */
public class UsageConverter {

    static final String EVENT_DISK = "EVENT_DISK_GIGABYTE_HOURS";
    static final String EVENT_CPU = "EVENT_CPU_HOURS";
    static final String EVENT_RAM = "EVENT_RAM_MEGABYTE_HOURS";
    static final String EVENT_TOTAL = "EVENT_TOTAL_HOURS";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(UsageConverter.class);

    private static final String ZONEID_UTC = "UTC";

    PropertyHandler ph;
    OpenstackClient osClient;

    public UsageConverter() {

    }

    public UsageConverter(PropertyHandler ph) throws MalformedURLException {
        this.ph = ph;
        osClient = new OpenstackClient(ph);
    }

    public void registerUsageEvents(String startTime, String endTime)
            throws ConfigurationException, MalformedURLException,
            ObjectNotFoundException, OrganizationAuthoritiesException,
            ValidationException {

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
